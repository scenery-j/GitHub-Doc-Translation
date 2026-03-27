# GitHub Doc Translation— Frontend Quick Start Guide

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

## Environment Requirements| Tool | Minimum Version | Description |
|------|-----------------|-------------|
| Node.js | ≥ 20.x | Recommend using LTS version |
| npm | ≥ 9.x | Or use pnpm / yarn |

Verification:

%%CODEBLOCK_0%%

---

## Quick Start

### 1. Install Dependencies

%%CODEBLOCK_1%%### 2. Start Development Server

%%CODEBLOCK_2%%

> Visit [http://localhost:5173](http://localhost:5173)

### 3. Prerequisite: Backend Service

The frontend development server by default proxies `/api/*` requests to `http://localhost:8080` (backend Spring Boot service).

Please ensure the backend service is running:

%%CODEBLOCK_3%%

To modify the backend address, edit the proxy configuration in `vite.config.ts`:

%%CODEBLOCK_4%%

---

## Project Structure Explanation

%%CODEBLOCK_5%%

---

## Page Routing

| Route | Page | Auth Requirement |
|-------|------|------------------|
| `/` | Landing Page | None |
| `/login` | Login Page | None |
| `/setup` | GitHub App Installation Guide | Requires Login |
| `/dashboard` | Dashboard Overview | Requires Login |
| `/repos` | Repository Management List | Requires Login |
| `/repos/:id` | Repository Details (Overview/Tasks/PR/Logs) | Requires Login |
| `/repos/:id/config` | Translation Configuration (File Selection/Language/Model) | Requires Login |
| `/repos/:id/tasks/:taskId` | Real-time Translation Task Progress | Requires Login |
| `/settings/profile` | Personal Information | Requires Login |
| `/settings/api-key` | OpenRouter API Key Management | Requires Login |
| `/settings/quota` | Translation Quota and Usage Details | Requires Login |

---

## Development Proxy ConfigurationDuring frontend development, all `/api/*` requests are automatically proxied to the backend (to avoid CORS):

%%CODEBLOCK_6%%

**GitHub OAuth Login Flow**:

1. User clicks "Login with GitHub" → frontend directly redirects to `/api/auth/github` (backend redirects to GitHub)
2. After GitHub authorization, callback to `/api/auth/github/callback`
3. After backend processing, redirect to frontend `/dashboard?token=xxx` or `/setup?token=xxx`
4. In `App.vue`, automatically extract the `token` parameter from the URL and store it in `localStorage`
5. All subsequent API requests automatically attach the `Authorization: Bearer {token}` header

---

## Theme Color Explanation

This project uses **Chinese Red** (`#DE2910`) as the theme color, implemented by overriding Element Plus CSS variables:

%%CODEBLOCK_7%%

The font uses **IBM Plex Sans** (body) and **JetBrains Mono** (code/numbers), imported via Google Fonts.

---

## Build and Deployment

### Development Build

%%CODEBLOCK_8%%

### Production Deployment (with Nginx)

%%CODEBLOCK_9%%

---

## FAQ### Q: Blank/White Page

- Ensure npm install has been completed
- Check Node.js version ≥ 20
- Check browser console for errors

### Q: API Request 404 / Network Error

- Ensure the backend Spring Boot service is running on port 8080
- Check the proxy configuration in vite.config.ts

### Q: After login, redirected to /setup instead of /dashboard

- Indicates that the GitHub App is not yet installed; follow the installation guide steps to complete installation### Q: Translation Configuration Page File Tree is Empty

- Confirm the repository has been added to the platform
- Confirm the GitHub App has access rights to this repository
- The backend GET /api/repos/{id}/tree interface must return data normally

### Q: How to Switch Backend API Address

Modify the proxy target in `vite.config.ts`, or configure Nginx reverse proxy in production environment.

---

> If you have any issues, please submit a [GitHub Issue](https://github.com/issues)