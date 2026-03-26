# =============================================================
# 前端 Dockerfile — doc_translation_website
# 采用多阶段构建（Multi-stage build）：
#   Stage 1 (builder)  : Node.js 环境，安装依赖并 Vite 构建
#   Stage 2 (runtime)  : Nginx Alpine，只保留 dist/ 静态文件
# 最终镜像约 40MB（对比 Node.js 镜像的 ~1GB）
# =============================================================

# ── Stage 1: 构建 Vue 应用 ────────────────────────────────────
# 使用 Node 25 LTS Alpine 版（体积小，约 130MB）
FROM node:25 AS builder

WORKDIR /app

# ★ 关键优化：先只复制 package.json 和 lock 文件
#   Docker 层缓存机制：只要这两个文件不变，npm install 层就会被缓存
#   后续只改业务代码时，直接复用 node_modules 缓存，构建快几倍
COPY package.json package-lock.json ./

# 安装依赖（--frozen-lockfile 确保严格按 lock 文件版本安装，避免版本漂移）
RUN npm ci --frozen-lockfile

# 复制全部源码（.dockerignore 会自动排除 node_modules、dist 等）
COPY . .

# 执行 Vite 生产构建
# 产物输出到 /app/dist/，包含 index.html + assets/（带 hash 的 JS/CSS）
RUN npm run build

# ── Stage 2: Nginx 服务器 ────────────────────────────────────
# nginx:alpine 体积极小（约 40MB），专为生产静态文件服务设计
FROM nginx:1.28.3-alpine

# 删除 Nginx 默认的示例配置，替换为我们自己的配置
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从 builder 阶段复制构建产物到 Nginx 默认静态文件目录
COPY --from=builder /app/dist /usr/share/nginx/html

# 声明容器监听端口 80
EXPOSE 80

# ── 健康检查 ──────────────────────────────────────────────────
# 检查 Nginx 是否正常响应 HTTP 请求
#
# 参数说明：
#   --interval=30s   : 每 30 秒检查一次
#   --timeout=5s     : 单次检查超时 5 秒
#   --start-period=10s: Nginx 启动极快，10 秒预热足够
#   --retries=3      : 连续 3 次失败才标记为 unhealthy
#
# nginx:alpine 基于 Alpine Linux，内置 BusyBox wget，无需额外安装。
# 探测逻辑：
#   GET http://localhost/ → Nginx 返回 index.html → 退出码 0（healthy）
#   Nginx 未启动或崩溃  → 连接拒绝             → 退出码 1（unhealthy）
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --spider -q http://localhost/ || exit 1

# 使用 Nginx 官方镜像默认的启动命令（前台运行，不要改成 daemon）
# CMD ["nginx", "-g", "daemon off;"]  ← 已由基础镜像默认提供，无需重复声明
