package com.gitdoc.translation.task.queue;

import com.gitdoc.translation.common.exception.BusinessException;
import com.gitdoc.translation.common.exception.ErrorCode;
import com.gitdoc.translation.entity.TranslationTaskEntity;
import com.gitdoc.translation.entity.repository.TaskJpaRepository;
import com.gitdoc.translation.translation.service.TranslationExecutor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBoundedBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Redis-backed translation task queue powered by Redisson {@link RBoundedBlockingQueue}.
 *
 * <h3>Queue-full policy</h3>
 * {@link RBoundedBlockingQueue#offer} returns {@code false} immediately when the queue has
 * reached its capacity.  We translate that into a {@link ErrorCode#TASK_QUEUE_FULL} exception
 * so the caller receives a clear 429-style error instead of blocking indefinitely.
 *
 * <h3>Concurrency model</h3>
 * {@code maxConcurrentTasks} platform threads are started at startup.  Each thread blocks on
 * {@link RBoundedBlockingQueue#take}, executes the task synchronously, then immediately picks
 * up the next item.  This naturally caps the number of simultaneously running tasks without any
 * additional synchronisation primitives.
 *
 * <h3>Crash recovery</h3>
 * Because the queue is stored in Redis it survives application restarts.  Additionally, at
 * startup we scan the database for:
 * <ol>
 *   <li>{@code running} tasks — reset to {@code queued} (they were interrupted mid-flight)</li>
 *   <li>{@code queued} tasks  — re-inserted into the Redis queue (covers the case where they
 *       were in the JVM queue at the time of the crash)</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TranslationTaskQueue {

    private static final String QUEUE_KEY = "translation:task:queue";

    @Value("${app.translation.task-queue-size:100}")
    private int queueCapacity;

    @Value("${app.translation.max-concurrent-tasks:3}")
    private int maxConcurrentTasks;

    private final RedissonClient redisson;
    private final TranslationExecutor translationExecutor;
    private final TaskJpaRepository taskRepository;

    private RBoundedBlockingQueue<Long> queue;

    @PostConstruct
    public void init() {
        queue = redisson.getBoundedBlockingQueue(QUEUE_KEY);
        // trySetCapacity is idempotent: no-op if the key already exists with a capacity
        queue.trySetCapacity(queueCapacity);
        recoverTasks();
        startConsumers();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Enqueues a task for async execution.
     *
     * @throws BusinessException {@link ErrorCode#TASK_QUEUE_FULL} when the bounded queue is at capacity
     */
    public void enqueue(Long taskId) {
        if (!queue.offer(taskId)) {
            throw BusinessException.of(ErrorCode.TASK_QUEUE_FULL);
        }
        log.info("Task {} enqueued | size={}/{}", taskId, queue.size(), queueCapacity);
    }

    // ── Startup recovery ─────────────────────────────────────────────────────

    private void recoverTasks() {
        // 1. Tasks marked "running" at shutdown were interrupted — reset them so they re-run
        List<TranslationTaskEntity> staleRunning = taskRepository.findByStatus("running");
        //
        if (!staleRunning.isEmpty()) {
            staleRunning.forEach(t -> t.setStatus("queued"));
            taskRepository.saveAll(staleRunning);
            log.warn("Startup recovery: reset {} stale 'running' tasks to 'queued'", staleRunning.size());
        }

        // 2. Re-enqueue tasks that were waiting in the (now-gone) JVM queue
        List<TranslationTaskEntity> pending = taskRepository.findByStatus("queued");
        int requeued = 0;
        for (TranslationTaskEntity task : pending) {
            if (queue.offer(task.getId())) {
                requeued++;
            } else {
                log.warn("Startup recovery: queue full, task {} not re-enqueued", task.getId());
            }
        }
        if (requeued > 0) {
            log.info("Startup recovery: re-enqueued {} pending tasks", requeued);
        }
    }

    // ── Consumer threads ──────────────────────────────────────────────────────

    private void startConsumers() {
        for (int i = 0; i < maxConcurrentTasks; i++) {
            Thread.ofPlatform()
                    .name("translation-consumer-" + i)
                    .daemon(true)
                    .start(this::consumeLoop);
        }
        log.info("Started {} translation consumer thread(s)", maxConcurrentTasks);
    }

    /**
     * Each consumer thread runs this loop indefinitely.
     * {@link RBoundedBlockingQueue#take} blocks until an item is available,
     * so the thread never spin-waits.
     */
    private void consumeLoop() {
        log.info("Consumer [{}] ready", Thread.currentThread().getName());
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Long taskId = queue.take();
                log.info("Consumer [{}] executing task {}", Thread.currentThread().getName(), taskId);
                translationExecutor.execute(taskId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Consumer [{}] interrupted — shutting down", Thread.currentThread().getName());
            } catch (Exception e) {
                log.error("Consumer [{}] error", Thread.currentThread().getName(), e);
            }
        }
    }
}
