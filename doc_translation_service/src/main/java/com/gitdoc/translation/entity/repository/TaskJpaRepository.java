package com.gitdoc.translation.entity.repository;

import com.gitdoc.translation.entity.TranslationTaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskJpaRepository extends JpaRepository<TranslationTaskEntity, Long> {

    @Query("SELECT t FROM TranslationTaskEntity t WHERE t.repositoryId = :repoId " +
            "AND (:status IS NULL OR :status = 'all' OR t.status = :status) " +
            "ORDER BY t.createdAt DESC")
    Page<TranslationTaskEntity> findByRepositoryIdWithFilter(Long repoId, String status, Pageable pageable);

    List<TranslationTaskEntity> findTop10ByRepositoryIdOrderByCreatedAtDesc(Long repositoryId);

    @Query("SELECT t FROM TranslationTaskEntity t WHERE t.repositoryId = :repoId " +
            "AND t.status IN ('queued', 'running') ORDER BY t.createdAt DESC")
    List<TranslationTaskEntity> findActiveTasksByRepositoryId(Long repoId);

    // For dashboard: recent tasks across all repos of a user
    @Query("SELECT t FROM TranslationTaskEntity t " +
            "JOIN RepositoryEntity r ON t.repositoryId = r.id " +
            "WHERE r.userId = :userId ORDER BY t.createdAt DESC")
    Page<TranslationTaskEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find the most recent completed task that already has a PR for the same repository
     * AND the same source branch.  Used to decide whether to append new translations to
     * an existing open PR or create a fresh one.
     *
     * <p>Uses a native query with {@code LIMIT 1} because JPQL has no LIMIT clause;
     * returning {@code Optional} from a JPQL query that matches multiple rows would
     * throw {@link org.springframework.dao.IncorrectResultSizeDataAccessException}.
     *
     * <p>{@code branchName} may be {@code null} (meaning "default branch").
     */
    @Query(value = "SELECT * FROM translation_tasks " +
            "WHERE repository_id = :repositoryId " +
            "AND status = :status " +
            "AND pr_number IS NOT NULL " +
            "AND ((:branchName IS NULL AND branch_name IS NULL) OR branch_name = :branchName) " +
            "ORDER BY created_at DESC " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<TranslationTaskEntity> findLatestCompletedTaskWithPRForBranch(
            Long repositoryId, String status, String branchName);

    // Used by TranslationTaskQueue on startup to recover tasks after restart
    List<TranslationTaskEntity> findByStatus(String status);

    /**
     * Find all tasks linked to a specific PR in a repository.
     * Used by the Webhook handler to update prStatus when a PR is merged/closed.
     */
    List<TranslationTaskEntity> findByRepositoryIdAndPrNumber(Long repositoryId, Integer prNumber);
}
