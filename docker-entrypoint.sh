#!/bin/bash
# =============================================================
# 容器入口脚本
#
# 职责：
#   1. 设置默认环境变量（DATABASE_URL / REDIS_HOST / REDIS_PORT）
#   2. 首次启动时，临时拉起 PostgreSQL 并创建数据库 + 用户（幂等）
#   3. 停止临时 PostgreSQL，交由 supervisord 统一接管所有进程
# =============================================================
set -e

# ── 环境变量默认值 ────────────────────────────────────────────
DB_NAME="${DATABASE_NAME:-doc_translation}"
DB_USER="${DATABASE_USERNAME:-root}"
DB_PASS="${DATABASE_PASSWORD:-root}"

if [ -z "$DB_PASS" ]; then
    echo "[entrypoint] ERROR: DATABASE_PASSWORD is required." >&2
    exit 1
fi

# 单容器内 PG/Redis 地址固定为 localhost；允许外部覆盖以连接云服务
export DATABASE_URL="${DATABASE_URL:-jdbc:postgresql://localhost:5432/$DB_NAME}"
export DATABASE_USERNAME="$DB_USER"
export DATABASE_PASSWORD="$DB_PASS"
export REDIS_HOST="${REDIS_HOST:-localhost}"
export REDIS_PORT="${REDIS_PORT:-6379}"

# ── PostgreSQL 初始化（仅首次运行）──────────────────────────────
PG_VERSION="16"
PG_DATA="/var/lib/postgresql/${PG_VERSION}/main"
PG_CTL="/usr/lib/postgresql/${PG_VERSION}/bin/pg_ctl"
PG_CONF="/etc/postgresql/${PG_VERSION}/main/postgresql.conf"
INIT_FLAG="/var/lib/postgresql/.app_db_initialized"

if [ ! -f "$INIT_FLAG" ]; then
    echo "[entrypoint] First run detected — initializing application database..."

    # 使用 pg_ctlcluster 启动（Ubuntu 默认 cluster 名为 main）
    su -s /bin/bash postgres -c "pg_ctlcluster $PG_VERSION main start -- -w"

    # 等待 PostgreSQL 完全就绪
    until su -s /bin/bash postgres -c "pg_isready -q"; do
        echo "[entrypoint] Waiting for PostgreSQL to be ready..."
        sleep 1
    done

    # 幂等创建用户（存在则跳过）
    su -s /bin/bash postgres -c \
        "psql -v ON_ERROR_STOP=0 postgres <<'SQL'
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '$DB_USER') THEN
    CREATE USER \"$DB_USER\" WITH PASSWORD '$DB_PASS';
  END IF;
END
\$\$;
SQL"

    # 幂等创建数据库（存在则跳过）
    su -s /bin/bash postgres -c \
        "psql -v ON_ERROR_STOP=0 postgres -tc \
         \"SELECT 1 FROM pg_database WHERE datname='$DB_NAME'\" \
         | grep -q 1 \
         || psql postgres -c \"CREATE DATABASE \\\"$DB_NAME\\\" OWNER \\\"$DB_USER\\\";\""

    # 停止临时 PostgreSQL（supervisord 会重新拉起）
    su -s /bin/bash postgres -c "pg_ctlcluster $PG_VERSION main stop -- -w"

    touch "$INIT_FLAG"
    echo "[entrypoint] Database initialization complete."
else
    echo "[entrypoint] Database already initialized, skipping."
fi

# ── 启动所有服务 ─────────────────────────────────────────────
echo "[entrypoint] Starting all services via supervisord..."
exec /usr/bin/supervisord -n -c /etc/supervisord.conf
