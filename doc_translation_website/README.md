<!--TRANSLATION_LINKS_START-->
#### Supported by [GitHub Doc Translation](https://github.com/scenery-j/GitHub-Doc-Translation)
> 📖 **其他语言版本**：[English (en)](../translations/en/doc_translation_website/README.md) | [日本語 (ja)](../translations/ja/doc_translation_website/README.md) | [한국어 (ko)](../translations/ko/doc_translation_website/README.md)
<!--TRANSLATION_LINKS_END-->

# GitHub Doc Translation — 前端快速启动说明

> **前端技术栈**: Vue 3 + Vite + Element Plus + TypeScript + Pinia + Vue Router

---

## 目录

1. [环境要求](#环境要求)
2. [快速启动](#快速启动)
3. [项目结构说明](#项目结构说明)
4. [页面路由](#页面路由)
5. [开发代理配置](#开发代理配置)
6. [主题颜色说明](#主题颜色说明)
7. [构建与部署](#构建与部署)
8. [常见问题](#常见问题)

---

## 环境要求

| 工具 | 最低版本 | 说明 |
|------|---------|------|
| Node.js | ≥ 20.x | 推荐使用 LTS 版本 |
| npm | ≥ 9.x | 或使用 pnpm / yarn |

验证：

```bash
node --version
npm --version
```

---

## 快速启动

### 1. 安装依赖

```bash
# 进入前端目录
cd doc_translation_website

# 使用 npm
npm install

# 或使用 pnpm（推荐，速度更快）
# npm install -g pnpm
# pnpm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

> 访问 [http://localhost:5173](http://localhost:5173)

### 3. 前置条件：后端服务

前端开发服务器默认将 `/api/*` 请求代理到 `http://localhost:8080`（后端 Spring Boot 服务）。

请确保后端服务已启动：

```bash
# 在项目根目录（非 doc_translation_website 目录）
docker compose -f docker-compose.dev.yml up -d   # 启动 PostgreSQL + Redis
./mvnw spring-boot:run                             # 启动 Spring Boot
```

如需修改后端地址，编辑 `vite.config.ts` 中的 proxy 配置：

```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',  // 修改为实际后端地址
    changeOrigin: true,
  },
},
```

---

## 项目结构说明

```
doc_translation_website/
├── public/
│   └── favicon.svg           # 网站图标
├── src/
│   ├── api/                  # 后端 API 请求封装（axios）
│   │   ├── index.ts          # axios 实例 + 拦截器（JWT、错误处理）
│   │   ├── auth.ts           # 认证相关 API
│   │   ├── repos.ts          # 仓库管理 API
│   │   ├── tasks.ts          # 翻译任务 API
│   │   ├── dashboard.ts      # 仪表盘 API
│   │   └── quota.ts          # 额度与设置 API
│   ├── components/
│   │   ├── layout/           # 布局组件
│   │   │   ├── AppLayout.vue   # 主应用布局（含侧边栏 + 顶部导航）
│   │   │   ├── AppHeader.vue   # 顶部导航栏（用户信息、额度显示）
│   │   │   └── AppSidebar.vue  # 左侧菜单（仪表盘/仓库/设置）
│   │   └── common/           # 通用组件
│   │       ├── EmptyState.vue  # 空状态占位
│   │       └── StatusTag.vue   # 状态标签（任务/仓库状态）
│   ├── router/
│   │   └── index.ts          # Vue Router 路由配置 + 路由守卫
│   ├── stores/               # Pinia 状态管理
│   │   ├── auth.ts           # 用户认证状态（JWT、用户信息）
│   │   └── repos.ts          # 仓库列表状态
│   ├── types/
│   │   └── index.ts          # TypeScript 类型定义
│   ├── views/                # 页面组件
│   │   ├── landing/          # 落地页（首页）
│   │   ├── login/            # 登录页
│   │   ├── setup/            # GitHub App 安装引导
│   │   ├── dashboard/        # 仪表盘总览
│   │   ├── repos/            # 仓库管理（列表/详情/配置）
│   │   ├── tasks/            # 翻译任务详情
│   │   └── settings/         # 个人设置（信息/API Key/额度）
│   ├── App.vue               # 根组件（处理 OAuth 回调 token）
│   ├── main.ts               # 应用入口
│   └── style.css             # 全局样式 + Element Plus 主题覆盖
├── index.html                # HTML 入口
├── vite.config.ts            # Vite 配置
├── tsconfig.json             # TypeScript 配置
└── package.json              # 项目依赖
```

---

## 页面路由

| 路由 | 页面 | 认证要求 |
|------|------|---------|
| `/` | 落地页 | 无 |
| `/login` | 登录页 | 无 |
| `/setup` | GitHub App 安装引导 | 需要登录 |
| `/dashboard` | 仪表盘总览 | 需要登录 |
| `/repos` | 仓库管理列表 | 需要登录 |
| `/repos/:id` | 仓库详情（概览/任务/PR/日志） | 需要登录 |
| `/repos/:id/config` | 翻译配置（文件选择/语言/模型） | 需要登录 |
| `/repos/:id/tasks/:taskId` | 翻译任务实时进度 | 需要登录 |
| `/settings/profile` | 个人信息 | 需要登录 |
| `/settings/api-key` | OpenRouter API Key 管理 | 需要登录 |
| `/settings/quota` | 翻译额度与用量明细 | 需要登录 |

---

## 开发代理配置

前端开发时，所有 `/api/*` 请求自动代理到后端（避免跨域）：

```typescript
// vite.config.ts
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
},
```

**GitHub OAuth 登录流程**：

1. 用户点击"使用 GitHub 登录" → 前端直接跳转到 `/api/auth/github`（后端重定向到 GitHub）
2. GitHub 授权后回调 `/api/auth/github/callback`
3. 后端处理后重定向到前端 `/dashboard?token=xxx` 或 `/setup?token=xxx`
4. `App.vue` 中自动提取 URL 中的 `token` 参数并存入 `localStorage`
5. 所有后续 API 请求自动附加 `Authorization: Bearer {token}` 请求头

---

## 主题颜色说明

本项目使用**中国红**（`#DE2910`）作为主题色，通过覆盖 Element Plus CSS 变量实现：

```css
/* src/style.css */
:root {
  --el-color-primary: #DE2910;
  --el-color-primary-light-3: #e75540;
  --el-color-primary-light-5: #ef8a76;
  /* ... 更多变量覆盖 */
}
```

字体使用 **IBM Plex Sans**（正文）和 **JetBrains Mono**（代码/数字），通过 Google Fonts 引入。

---

## 构建与部署

### 开发构建

```bash
npm run dev         # 启动开发服务器（含热重载）
npm run build       # 生产构建（输出到 dist/）
npm run preview     # 预览生产构建
```

### 生产部署（配合 Nginx）

```bash
# 1. 构建前端
npm run build

# 2. 将 dist/ 目录内容部署到 Nginx
# Nginx 配置参考（见 技术实现方案.md 第十二章）：
# location / { root /path/to/dist; try_files $uri $uri/ /index.html; }
# location /api/ { proxy_pass http://backend:8080; }
```

---

## 常见问题

### Q: 访问页面白屏/空白

- 确保 npm install 已完成
- 检查 Node.js 版本 ≥ 20
- 查看浏览器控制台报错

### Q: API 请求 404 / 网络错误

- 确保后端 Spring Boot 服务在 8080 端口运行
- 检查 vite.config.ts 中的代理配置

### Q: 登录后跳转到 /setup 而非 /dashboard

- 说明 GitHub App 尚未安装，按照安装引导步骤完成安装即可

### Q: 翻译配置页文件树为空

- 确认仓库已被添加到平台
- 确认 GitHub App 有该仓库的访问权限
- 后端接口 GET /api/repos/{id}/tree 需正常返回数据

### Q: 如何切换后端 API 地址

修改 `vite.config.ts` 中的 proxy target，或在生产环境通过 Nginx 反向代理配置。

---

> 如有问题，请提交 [GitHub Issue](https://github.com/issues)
