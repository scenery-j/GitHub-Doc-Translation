# =============================================================
# 全量单镜像 Dockerfile —— 云托管（单容器）部署版
#
# 架构：三阶段多阶段构建
#   Stage 1 (frontend-builder) : Node.js 构建 Vue3 前端
#   Stage 2 (backend-builder)  : Maven + JDK21 构建 Spring Boot
#   Stage 3 (runtime)          : JRE21 + PostgreSQL + Redis + Nginx + Supervisord
#
# 容器内运行的 4 个进程（supervisord 统一守护）：
#   postgres  (5432, 仅内部) → 数据库
#   redis     (6379, 仅内部) → 缓存
#   backend   (8080, 仅内部) → Spring Boot
#   nginx     (80,   对外)   → 前端静态 + /api/* 反代到 localhost:8080
#
# 环境变量（云托管部署时手动传入）：
#   DATABASE_NAME       数据库名（默认 doc_translation）
#   DATABASE_USERNAME   数据库用户（默认 docuser）
#   DATABASE_PASSWORD   数据库密码（必填）
#   GITHUB_APP_*        GitHub App 配置（必填）
#   OPENROUTER_API_KEY  AI 翻译 Key（必填）
#   JWT_SECRET          JWT 签名密钥（必填）
#   ENCRYPTION_KEY      AES 加密密钥，必须 32 字符（必填）
#   FRONTEND_URL        前端公网地址，如 https://yourdomain.com
#
# 数据持久化（挂载卷）：
#   /var/lib/postgresql   → PostgreSQL 数据目录
#   /var/lib/redis        → Redis AOF 文件
# =============================================================

# ── Stage 1: 构建 Vue 前端 ────────────────────────────────────
FROM node:25-alpine AS frontend-builder

WORKDIR /frontend

COPY doc_translation_website/package.json doc_translation_website/package-lock.json ./
RUN npm ci --frozen-lockfile

COPY doc_translation_website/ .
RUN npm run build
# 产物：/frontend/dist/

# ── Stage 2: 构建 Spring Boot 后端 ───────────────────────────
FROM maven:3.9.14-eclipse-temurin-21 AS backend-builder

WORKDIR /backend

COPY doc_translation_service/pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress -q

COPY doc_translation_service/src ./src
RUN mvn package -DskipTests --no-transfer-progress -q
# 产物：/backend/target/*.jar

# ── Stage 3: 运行时镜像 ───────────────────────────────────────
FROM eclipse-temurin:21-jre-noble

ENV DEBIAN_FRONTEND=noninteractive

# 安装 PostgreSQL 16、Redis、Nginx、Supervisor
RUN apt-get update && apt-get install -y --no-install-recommends \
        postgresql \
        redis-server \
        nginx \
        supervisor \
        curl \
    && rm -rf /var/lib/apt/lists/*

# 创建专用用户运行 Java 应用
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# 复制后端 Jar
COPY --from=backend-builder /backend/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

# 复制前端构建产物到 Nginx 静态目录
COPY --from=frontend-builder /frontend/dist /usr/share/nginx/html

# 替换 Nginx 默认配置（代理目标从 backend:8080 改为 localhost:8080）
RUN rm -f /etc/nginx/sites-enabled/default
COPY nginx-single.conf /etc/nginx/sites-available/default
RUN ln -s /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default

# 复制 Supervisord 配置（管理 postgres + redis + nginx + java 四个进程）
COPY supervisord.conf /etc/supervisord.conf

# 复制启动脚本
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
COPY start-backend.sh /app/start-backend.sh
RUN chmod +x /app/docker-entrypoint.sh /app/start-backend.sh \
    && chown appuser:appgroup /app/start-backend.sh

# 声明数据持久化目录
VOLUME ["/var/lib/postgresql", "/var/lib/redis"]

# 只对外暴露 80 端口；其余端口仅容器内部使用
EXPOSE 80

# 健康检查：Nginx 存活（前端）+ /api/health（后端），预热时间 120s（等 Spring Boot 启动）
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -sf http://localhost/api/health || exit 1

# 入口脚本：初始化 PG 数据库后，交由 supervisord 管理所有进程
ENTRYPOINT ["/app/docker-entrypoint.sh"]
