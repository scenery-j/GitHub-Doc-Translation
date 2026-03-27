# GitHub DocTranslation — Frontend Quick Start Guide

> **Frontend Tech Stack**: Vue 3 + Vite + Element Plus + TypeScript + Pinia + Vue Router

---

## Table of Contents

1. [Environment Requirements](#环境要求)
2. [Quick Start](#快速启动)
3. [Project Structure Explanation](#项目结构说明)
4. [Page Routing](#页面路由)
5. [Development Proxy Configuration](#开发代理配置)
6. [Theme Color Explanation](#主题颜色说明)
7. [Build and Deployment](#构建与部署)
8. [FAQ](#常见问题)

---

## Environment Requirements

| Tool | Minimum Version | Description |
|------|-----------------|-------------|
| Node.js | ≥ 20.x | Recommended to use LTS version |
| npm | ≥ 9.x | Or use pnpm / yarn |

Verification:

%%CODEBLOCK_0%%

---

## Quick Start

### 1. Install Dependencies

%%CODEBLOCK_1%%

### 2. Start Development Server

%%CODEBLOCK_2%%

> Visit [http://localhost:5173](http://localhost:5173)

### 3. Prerequisite: Backend Service

The frontend development server by default proxies `/api/*` requests to `http://localhost:8080` (backend Spring Boot service).

Please ensure the backend service is running:

%%CODEBLOCK_3%%

If you need to modify the backend address, edit the proxy configuration in `vite.config.ts`:

%%CODEBLOCK_4%%

---

## Project Structure Explanation

%%CODEBLOCK_5%%

---

## Page Routing

| Route | Page | Auth Required |
|-------|------|---------------|
| `/` | Landing Page | None |
| `/login` | Login Page | None |
| `/setup` | GitHub App Installation Guide | Login Required |
| `/dashboard` | Dashboard Overview | Login Required |
| `/repos` | Repository Management List | Login Required |
| `/repos/:id` | Repository Details (Overview/Tasks/PR/Logs) | Login Required |
| `/repos/:id/config` | Translation Configuration (File Selection/Language/Model) | Login Required |
| `/repos/:id/tasks/:taskId` | Translation Task Real-time Progress | Login Required |
| `/settings/profile` | Personal Information | Login Required |
| `/settings/api-key` | OpenRouter API Key Management | Login Required |
| `/settings/quota` | Translation Quota and Usage Details | Login Required |

---

## Development Proxy Configuration

During frontend development, all `/api/*` requests are automatically proxied to the backend (to avoid CORS):

%%CODEBLOCK_6%%**GitHub OAuth Login Flow**:

1. User clicks "Login with GitHub" → frontend redirects directly to `/api/auth/github` (backend redirects to GitHub)
2. After GitHub authorization, callback to `/api/auth/github/callback`
3. After backend processing, redirect to frontend `/dashboard?token=xxx` or `/setup?token=xxx`
4. In `App.vue`, automatically extract the `token` parameter from the URL and store it in `localStorage`
5. All subsequent API requests automatically attach the `Authorization: Bearer {token}` header

---

## Theme Color ExplanationThis project uses **Chinese Red** (`#DE2910`) as the theme color, implemented by overriding Element Plus CSS variables:

%%CODEBLOCK_7%%

Fonts use **IBM Plex Sans** (body) and **JetBrains Mono** (code/numbers), imported via Google Fonts.

---

## Build and Deployment

### Development Build

%%CODEBLOCK_8%%

### Production Deployment (with Nginx)

%%CODEBLOCK_9%%

---

## FAQ

### Q: Page shows blank/white

- Ensure npm install has been completed
- Check Node.js version ≥ 20
- Check browser console for errors

### Q: API request 404 / network error

- Ensure the backend Spring Boot service is running on port 8080
- Check the proxy configuration in vite.config.ts

### Q: After login, redirects to /setup instead of /dashboard

- Indicates that the GitHub App is not yet installed; follow the installation guide steps to complete installation.

### Q: File tree empty on translation configuration page

- Confirm the repository has been added to the platform
- Confirm the GitHub App has access permissions for that repository
- Backend endpoint GET /api/repos/{id}/tree must return data normally

### Q: How to switch backend API address

Modify the proxy target in `vite.config.ts`, or configure Nginx reverse proxy in production environment.

---

> If you have any issues, please submit a [GitHub Issue](https://github.com/issues)