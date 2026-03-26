#!/bin/bash
# =============================================================
# Spring Boot 后端启动包装脚本
#
# 职责：在启动 Java 进程前，轮询等待 PostgreSQL 和 Redis 就绪，
# 确保依赖服务可用后再执行 java -jar，避免 Spring Boot 因
# 数据库连接失败而启动报错。
# =============================================================

echo "[backend] Waiting for PostgreSQL on localhost:5432..."
until pg_isready -h localhost -p 5432 -q; do
    sleep 2
done
echo "[backend] PostgreSQL is ready."

echo "[backend] Waiting for Redis on localhost:6379..."
until redis-cli -h localhost -p 6379 ping 2>/dev/null | grep -q PONG; do
    sleep 2
done
echo "[backend] Redis is ready."

echo "[backend] Starting Spring Boot..."
exec java \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -jar /app/app.jar
