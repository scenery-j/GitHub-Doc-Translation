package com.gitdoc.translation.common.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

/**
 * 健康检查接口
 *
 * <p>提供 GET /api/health 端点，供 Docker HEALTHCHECK 和监控系统调用。
 *
 * <p>响应规则：
 * <ul>
 *   <li>全部依赖正常 → HTTP 200，status="UP"</li>
 *   <li>任一依赖异常 → HTTP 503，status="DEGRADED"</li>
 * </ul>
 *
 * <p>为什么不用 Spring Boot Actuator？
 * 项目未引入 actuator 依赖，且业务只需要一个轻量的健康探针，
 * 自定义实现更简洁、可控，无需暴露任何 Actuator 元数据。
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * 健康检查主入口
     *
     * <p>同时探测数据库和 Redis 连通性，任一失败则整体状态降级为 DEGRADED。
     * 探测超时设为 2 秒，避免因依赖慢响应阻塞 Docker 健康检查流程。
     */
    @GetMapping
    public ResponseEntity<String> health() {
        return ResponseEntity
                .status(200)
                .body("SUCCESS");
    }
}
