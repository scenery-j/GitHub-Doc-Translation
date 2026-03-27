<!--TRANSLATION_LINKS_START-->
#### Supported by [GitHub Doc Translation](https://github.com/scenery-j/GitHub-Doc-Translation)
> 📖 **其他语言版本**：[English (en)](translations/en/README.md) | [日本語 (ja)](translations/ja/README.md) | [한국어 (ko)](translations/ko/README.md)
<!--TRANSLATION_LINKS_END-->

# GitHub Doc Translation

<p align="center">
  <strong>AI 驱动的 GitHub 文档多语言翻译平台</strong>
</p>

<p align="center">
  让你的开源项目，被全世界看见
</p>

<p align="center">
  <a href="#功能特性">功能特性</a> •
  <a href="#技术栈">技术栈</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#部署">部署</a> •
  <a href="#项目结构">项目结构</a> •
  <a href="#页面操作指导">页面操作指导</a> •
</p>

---

## 简介

GitHub Doc Translation 是一个 **SaaS 平台**，为 GitHub 开源仓库提供 AI 驱动的多语言文档自动翻译服务。

用户通过 GitHub OAuth 登录，安装 GitHub App 授权仓库后，平台会自动将仓库中的 Markdown 文档翻译成多种目标语言，并以 Pull Request 的形式提交到原仓库。当基准语言的文档发生变更时，平台自动检测并增量翻译同步。

### 为什么需要这个项目？

许多中文开源项目质量优秀但只有中文文档，导致国际用户难以使用，限制了项目的影响力和流量增长。人工翻译成本高、难以持续维护。现有工具多为 CLI / GitHub Action 形态，配置复杂，且缺乏可视化管理能力。

本项目旨在提供**一站式、零配置的 SaaS 翻译服务**，降低开源项目国际化的门槛。


---

## 项目文档

| 文档                                    | 说明                    |
|---------------------------------------|-----------------------|
| [需求规格文档](docs/需求规格文档.md)              | 产品需求、功能模块、用户画像等       |
| [技术实现方案](docs/技术实现方案.md)              | 系统架构、数据库设计、API 设计等    |
| [API 接口文档](docs/API接口文档.md)           | 后端 API 详细说明           |
| [快速启动指南](docs/快速启动指南.md)              | 本地开发环境搭建步骤            |
| [人工配置文档](docs/人工配置文档.md)              | GitHub App 创建、环境变量配置等 |
| [Vibe Coding 开发过程提示词](docs/VibeCoding过程提示词.md) | Vibe Coding 开发过程      |

---

## 功能特性

- **🔌 零配置接入** - 安装 GitHub App 即可使用，无需配置 CI/CD
- **🤖 AI 翻译引擎** - 通过 OpenRouter 接入 300+ 大模型，自由选择翻译引擎
- **🔄 自动同步** - Webhook 监听文档变更，自动增量翻译并提交 PR
- **📁 可视化配置** - 树形目录选择翻译文件，支持 `.gitdoc-ignore` 配置
- **🌍 多语言支持** - 支持 10+ 种主流语言（英语、日语、韩语、法语、德语等）
- **📊 仪表盘管理** - 翻译状态一目了然，任务进度实时追踪
- **💰 灵活计费** - 平台免费额度 + 用户充值 + 自带 OpenRouter API Key

---

## 技术栈

| 层级 | 技术选型 |
|------|---------|
| **前端** | Vue 3 + Vite + Element Plus + TypeScript + Pinia |
| **后端** | Java 21 + Spring Boot 3.4 + Spring AI 1.1 |
| **AI 集成** | OpenRouter API (OpenAI 兼容协议) |
| **数据库** | PostgreSQL 17 |
| **缓存** | Redis 7 |
| **任务队列** | Redisson (Redis 分布式队列) |
| **GitHub 集成** | GitHub App + OAuth 2.0 + Webhook |
| **容器化** | Docker + Docker Compose |

---

## 快速开始

### 环境要求

| 工具 | 版本 |
|------|------|
| Java | 21 (LTS) |
| Maven | 3.9+ |
| Docker + Docker Compose | 任意新版 |
| Node.js | ≥ 20.x (前端开发) |

### 1. 克隆项目

```bash
git clone https://github.com/your-username/github_doc_translation.git
cd github_doc_translation
```

### 2. 配置环境变量

```bash
# 复制示例配置
cp .env.example .env

# 编辑配置文件
vim .env
```

**必须填写的配置项：**

```bash
# GitHub App 信息（从 https://github.com/settings/apps 获取）
GITHUB_APP_ID=123456
GITHUB_APP_CLIENT_ID=Iv1.xxxxxxxxxxxxxxxx
GITHUB_APP_CLIENT_SECRET=xxxxxxxxxxxxxxxxxxxx
GITHUB_APP_WEBHOOK_SECRET=your-webhook-secret
GITHUB_APP_SLUG=your-app-slug

# OpenRouter API Key（从 https://openrouter.ai/keys 获取）
OPENROUTER_API_KEY=sk-or-v1-xxxxxxxxxx

# 安全密钥（生成命令: openssl rand -hex 32）
JWT_SECRET=your-random-jwt-secret-at-least-32-chars
ENCRYPTION_KEY=your-32-char-encryption-key
```

### 3. 放置 GitHub App 私钥

将从 GitHub App 设置页下载的 `.pem` 私钥文件放到项目根目录：

```bash
cp ~/Downloads/your-app.private-key.pem ./doc-translation-github-app.private-key.pem
```

### 4. 启动服务

```bash
# 一键启动所有服务（PostgreSQL + Redis + 后端 + 前端）
docker compose up -d --build
```

### 5. 访问应用

打开浏览器访问 http://localhost

---

## 本地开发

### 启动后端服务

```bash
cd doc_translation_service

# 启动 PostgreSQL + Redis
docker compose -f docker-compose.dev.yml up -d

# 启动 Spring Boot
export $(cat ../.env | grep -v ^# | xargs) && mvn spring-boot:run
```

后端 API 地址: http://localhost:8080

### 启动前端服务

```bash
cd doc_translation_website

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端开发服务器地址: http://localhost:5173

### Webhook 本地调试

使用 Smee 转发 GitHub Webhook 到本地：

```bash
npm install -g smee-client
smee --url https://smee.io/your-channel-id --path /api/webhook/github --port 8080
```

---

## 部署

### 生产环境部署

1. **修改环境变量**
   ```bash
   # 编辑 .env，设置生产环境配置
   FRONTEND_URL=https://yourdomain.com
   ```

2. **构建并启动**
   ```bash
   docker compose up -d --build
   ```

3. **配置域名和 HTTPS**
   - 在 `nginx.conf` 中配置 SSL 证书
   - 在 `docker-compose.yml` 中开放 443 端口

### 端口说明

| 服务 | 端口 |
|------|------|
| 前端 (Nginx) | 80 |
| 后端 API | 8080 |
| PostgreSQL | 5432 |
| Redis | 6379 |

---

## 项目结构

```
github_doc_translation/
├── doc_translation_service/     # 后端服务 (Spring Boot)
│   ├── src/main/java/           # Java 源码
│   ├── src/main/resources/      # 配置文件
│   ├── Dockerfile               # 后端 Docker 镜像
│   ├── docker-compose.dev.yml   # 开发环境数据库/缓存
│   └── pom.xml                  # Maven 依赖
│
├── doc_translation_website/     # 前端网站 (Vue 3)
│   ├── src/                     # Vue 源码
│   │   ├── api/                 # API 请求封装
│   │   ├── components/          # 组件
│   │   ├── views/               # 页面
│   │   ├── stores/              # Pinia 状态管理
│   │   └── router/              # Vue Router 配置
│   ├── Dockerfile               # 前端 Docker 镜像
│   ├── nginx.conf               # Nginx 配置
│   └── package.json             # npm 依赖
│
├── docs/                        # 项目文档
│   ├── 需求规格文档.md
│   ├── 技术实现方案.md
│   ├── API接口文档.md
│   ├── 快速启动指南.md
│   └── 人工配置文档.md
│
├── docker-compose.yml           # 生产部署编排
├── .env.example                 # 环境变量模板
└── README.md                    # 本文件
```

---

## API 概览

### 认证相关

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/auth/github` | GitHub OAuth 授权 |
| GET | `/api/auth/github/callback` | OAuth 回调 |
| GET | `/api/auth/me` | 获取当前用户信息 |

### 仓库管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/repos` | 获取仓库列表 |
| POST | `/api/repos` | 添加仓库 |
| GET | `/api/repos/{id}` | 仓库详情 |
| GET | `/api/repos/{id}/tree` | 仓库文件树 |

### 翻译任务

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/repos/{id}/translate` | 触发翻译 |
| GET | `/api/repos/{id}/tasks` | 任务列表 |
| GET | `/api/tasks/{id}` | 任务详情 |

### Webhook

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/webhook/github` | GitHub Webhook 接收 |

---

## 支持的目标语言

| 语言 | 代码 | 语言 | 代码 |
|------|------|------|------|
| English | en | Français | fr |
| 日本語 | ja | Deutsch | de |
| 한국어 | ko | Русский | ru |
| Español | es | العربية | ar |
| Português | pt | हिन्दी | hi |

---

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---


## 页面操作指导



---

## 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [OpenRouter](https://openrouter.ai/)
- [GitHub API](https://docs.github.com/)
