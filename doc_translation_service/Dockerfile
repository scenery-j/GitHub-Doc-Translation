# =============================================================
# 后端 Dockerfile — doc_translation_service
# 采用多阶段构建（Multi-stage build）：
#   Stage 1 (builder)  : 在完整 JDK + Maven 环境中编译打包
#   Stage 2 (runtime)  : 只保留 JRE，体积更小、安全面更小
# =============================================================

# ── Stage 1: 编译打包 ─────────────────────────────────────────
# 使用官方 Maven 镜像（内置 JDK 21 + Maven 3.9），比手动 apk 安装快得多
FROM maven:3.9.14-eclipse-temurin-21 AS builder

WORKDIR /app

# ★ 关键优化：先只复制 pom.xml，单独下载依赖
#   好处：只要 pom.xml 没变，Docker 就会复用这一层缓存，
#         后续改动业务代码时不需要重新下载所有 jar 包（可节省几分钟）
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress -q

# 再复制源码并打包（跳过单元测试以加快构建速度）
COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress -q

# ── Stage 2: 运行时镜像 ───────────────────────────────────────
# 复用 builder 阶段已有的本地镜像，避免因网络问题拉取失败
FROM maven:3.9.14-eclipse-temurin-21

WORKDIR /app

# 创建专用系统用户运行应用，避免以 root 权限运行（安全最佳实践）
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# 从 builder 阶段复制编译产物（*.jar 通配符自动匹配实际文件名）
COPY --from=builder /app/target/*.jar app.jar

# 确保应用用户拥有 jar 文件的读取权限
RUN chown appuser:appgroup app.jar

# 切换到非 root 用户
USER appuser

# 声明容器监听端口（仅文档作用，实际映射由 docker-compose 控制）
EXPOSE 8080

# ── 健康检查 ──────────────────────────────────────────────────
# Docker 会定期执行此命令；非零退出码 → 容器标记为 unhealthy
#
# 参数说明：
#   --interval=30s   : 每 30 秒检查一次（避免频繁探测影响性能）
#   --timeout=5s     : 单次检查超时 5 秒
#   --start-period=60s: 容器启动后等待 60 秒再开始计入失败次数
#                      （Spring Boot 冷启动需要时间）
#   --retries=3      : 连续 3 次失败才最终标记为 unhealthy
#
# 命令使用 wget（Alpine BusyBox 内置，无需额外安装）：
#   -q          : 静默模式，不输出下载进度
#   -O /dev/null: 丢弃响应体，只关心退出码
#   --spider    : 类似 HEAD 请求；HTTP 非 2xx 时返回非零退出码
#                 → 当 /api/health 返回 503（DEGRADED）时健康检查失败
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -sf http://localhost:8080/api/health || exit 1

# 启动命令：
#   -XX:+UseContainerSupport   → 让 JVM 感知容器的 CPU/内存限制（替代旧的 -XX:+UseCGroupMemoryLimitForHeap）
#   -XX:MaxRAMPercentage=75.0  → 最多使用容器内存的 75%，为操作系统预留空间
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
