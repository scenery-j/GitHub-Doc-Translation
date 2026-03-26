# GitHub Doc Translation — 前端接口文档 (MVP)

> **版本**: v1.0 | **基础路径**: `/api` | **日期**: 2026-03-18

---

## 通用约定

### 认证方式
除登录/回调/Webhook 外，所有接口需在请求头携带：
```
Authorization: Bearer {JWT_TOKEN}
```
JWT 过期或无效返回 `401`，前端应跳转登录页。

### 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```
错误时：
```json
{
  "code": 40001,
  "message": "仓库不存在",
  "data": null
}
```

### 分页参数与响应
请求：`?page=1&size=10`（page 从 1 开始）

```json
{
  "code": 200,
  "data": {
    "records": [ ... ],
    "total": 56,
    "page": 1,
    "size": 10,
    "pages": 6
  }
}
```

### 常用错误码
| code | 含义 |
|------|------|
| 200 | 成功 |
| 40001 | 参数错误 |
| 40101 | 未登录 / Token 过期 |
| 40301 | 无权操作 |
| 40401 | 资源不存在 |
| 40901 | 额度不足 |
| 50001 | 服务内部错误 |

---

## 一、认证模块

### 1.1 跳转 GitHub 登录
```
GET /api/auth/github
```
**行为**：302 重定向到 GitHub OAuth 授权页面。前端直接 `window.location.href` 跳转。

---

### 1.2 OAuth 回调
```
GET /api/auth/github/callback?code={code}&state={state}
```
**行为**：后端处理后 302 重定向到前端页面，URL 带上 token 参数：
- 已安装 App → `{FRONTEND_URL}/dashboard?token={JWT}`
- 未安装 App → `{FRONTEND_URL}/setup?token={JWT}`

前端从 URL 取出 token 存入 `localStorage`，然后清除 URL 参数。

---

### 1.3 获取当前用户信息
```
GET /api/auth/me
```
**Response**:
```json
{
  "id": 1,
  "username": "jiangjing1219",
  "avatarUrl": "https://avatars.githubusercontent.com/u/xxx",
  "email": "xxx@gmail.com",
  "freeQuota": 100000,
  "usedQuota": 45230,
  "hasOpenrouterKey": true,
  "hasInstallation": true,
  "createdAt": "2026-03-18T10:00:00Z"
}
```

---

### 1.4 退出登录
```
POST /api/auth/logout
```
**Response**: `{ "code": 200 }`

---

## 二、仓库管理模块

### 2.1 获取平台已添加的仓库列表
```
GET /api/repos?page=1&size=10&search=keyword&status=active
```
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 默认 1 |
| size | int | 否 | 默认 10 |
| search | string | 否 | 仓库名关键词 |
| status | string | 否 | active / paused / all(默认) |

**Response** (`data.records` 单项):
```json
{
  "id": 1,
  "fullName": "jiangjing1219/IM-SERVICE",
  "defaultBranch": "main",
  "baseLanguage": "zh",
  "targetLanguages": ["en", "ja", "ko"],
  "aiModel": "deepseek/deepseek-chat",
  "status": "active",
  "webhookActive": true,
  "translatedFileCount": 12,
  "lastSyncAt": "2026-03-18T14:30:00Z",
  "createdAt": "2026-03-18T10:00:00Z"
}
```

---

### 2.2 获取 GitHub 已授权仓库列表（用于添加）
```
GET /api/repos/available?page=1&size=20
```
**Response** (`data.records` 单项):
```json
{
  "githubRepoId": 123456,
  "fullName": "jiangjing1219/IM-SERVICE",
  "defaultBranch": "main",
  "description": "即时通讯服务",
  "starCount": 128,
  "language": "Java",
  "isPrivate": false,
  "alreadyAdded": true
}
```
> `alreadyAdded=true` 表示已添加到平台，前端应置灰不可选。

---

### 2.3 添加仓库到平台
```
POST /api/repos
```
**Request Body**:
```json
{
  "githubRepoId": 123456,
  "fullName": "jiangjing1219/IM-SERVICE",
  "defaultBranch": "main"
}
```
**Response**: 返回创建的仓库对象（同 2.1 格式）。

---

### 2.4 获取仓库详情
```
GET /api/repos/{id}
```
**Response**:
```json
{
  "id": 1,
  "fullName": "jiangjing1219/IM-SERVICE",
  "defaultBranch": "main",
  "baseLanguage": "zh",
  "targetLanguages": ["en", "ja", "ko"],
  "aiModel": "deepseek/deepseek-chat",
  "status": "active",
  "webhookActive": true,
  "lastSyncAt": "2026-03-18T14:30:00Z",
  "stats": {
    "totalFiles": 12,
    "translatedFiles": 10,
    "pendingChanges": 2,
    "totalPRs": 5,
    "languageProgress": [
      { "language": "en", "total": 12, "completed": 12, "percentage": 100 },
      { "language": "ja", "total": 12, "completed": 9, "percentage": 75 },
      { "language": "ko", "total": 12, "completed": 6, "percentage": 50 }
    ]
  }
}
```

---

### 2.5 移除仓库
```
DELETE /api/repos/{id}
```
**Response**: `{ "code": 200 }`

---

### 2.6 获取仓库 Markdown 文件树
```
GET /api/repos/{id}/tree
```
**Response**:
```json
{
  "tree": [
    {
      "path": "README.md",
      "type": "file",
      "size": 2048
    },
    {
      "path": "docs",
      "type": "directory",
      "children": [
        { "path": "docs/guide.md", "type": "file", "size": 5120 },
        { "path": "docs/api.md", "type": "file", "size": 3200 },
        {
          "path": "docs/faq",
          "type": "directory",
          "children": [
            { "path": "docs/faq/common.md", "type": "file", "size": 1500 }
          ]
        }
      ]
    }
  ]
}
```
> 仅返回 `.md` 文件和包含 `.md` 文件的目录。

---

## 三、翻译配置模块

### 3.1 获取翻译配置
```
GET /api/repos/{id}/config
```
**Response**:
```json
{
  "baseLanguage": "zh",
  "targetLanguages": ["en", "ja", "ko"],
  "selectedPaths": ["README.md", "docs/guide.md", "docs/api.md", "docs/faq/common.md"],
  "ignorePatterns": ["CHANGELOG.md", "docs/internal/", "**/draft-*.md"],
  "aiModel": "deepseek/deepseek-chat",
  "outputPathPattern": "translations/{lang}/"
}
```

---

### 3.2 更新翻译配置
```
PUT /api/repos/{id}/config
```
**Request Body**:
```json
{
  "baseLanguage": "zh",
  "targetLanguages": ["en", "ja", "ko"],
  "selectedPaths": ["README.md", "docs/guide.md", "docs/api.md"],
  "ignorePatterns": ["CHANGELOG.md", "docs/internal/"],
  "aiModel": "deepseek/deepseek-chat"
}
```
**Response**: 返回更新后的配置（同 3.1 格式）。

---

### 3.3 获取可用 AI 模型列表
```
GET /api/models
```
**Response**:
```json
{
  "models": [
    {
      "id": "deepseek/deepseek-chat",
      "name": "DeepSeek Chat",
      "contextLength": 65536,
      "inputPrice": 0.32,
      "outputPrice": 0.89,
      "priceUnit": "$/1M tokens"
    },
    {
      "id": "anthropic/claude-4.5-haiku",
      "name": "Claude 4.5 Haiku",
      "contextLength": 200000,
      "inputPrice": 1.0,
      "outputPrice": 5.0,
      "priceUnit": "$/1M tokens"
    }
  ],
  "recommended": {
    "costEffective": "deepseek/deepseek-chat",
    "highQuality": "anthropic/claude-4.5-haiku",
    "fast": "google/gemini-2.5-flash"
  }
}
```
> 后端缓存 1 小时，前端无需额外缓存。

---

## 四、翻译任务模块

### 4.1 手动触发翻译
```
POST /api/repos/{id}/translate
```
**Request Body**:
```json
{
  "type": "full"
}
```
| type | 说明 |
|------|------|
| full | 全量翻译所有选中文件 |
| incremental | 仅翻译自上次以来变更的文件 |

**Response**:
```json
{
  "taskId": 42
}
```

---

### 4.2 获取仓库任务列表
```
GET /api/repos/{id}/tasks?page=1&size=10&status=all
```
| 参数 | 说明 |
|------|------|
| status | queued / running / completed / failed / cancelled / all(默认) |

**Response** (`data.records` 单项):
```json
{
  "id": 42,
  "triggerType": "webhook",
  "status": "completed",
  "targetLanguages": ["en", "ja", "ko"],
  "totalFiles": 12,
  "completedFiles": 12,
  "failedFiles": 0,
  "tokensUsed": 45230,
  "estimatedCost": 0.06,
  "prNumber": 15,
  "prUrl": "https://github.com/jiangjing1219/IM-SERVICE/pull/15",
  "startedAt": "2026-03-18T14:30:00Z",
  "completedAt": "2026-03-18T14:33:15Z",
  "createdAt": "2026-03-18T14:30:00Z"
}
```

---

### 4.3 获取任务详情（含文件级进度）
```
GET /api/tasks/{id}
```
> 翻译进行中时，前端每 **3 秒** 轮询此接口。

**Response**:
```json
{
  "id": 42,
  "repositoryId": 1,
  "repositoryName": "jiangjing1219/IM-SERVICE",
  "triggerType": "manual",
  "status": "running",
  "targetLanguages": ["en", "ja", "ko"],
  "totalFiles": 12,
  "completedFiles": 8,
  "failedFiles": 0,
  "tokensUsed": 32000,
  "estimatedCost": 0.04,
  "prNumber": null,
  "prUrl": null,
  "startedAt": "2026-03-18T14:30:00Z",
  "completedAt": null,
  "files": [
    {
      "id": 101,
      "sourcePath": "README.md",
      "targetLanguage": "en",
      "targetPath": "translations/en/README.md",
      "status": "completed",
      "tokensUsed": 1240,
      "translatedAt": "2026-03-18T14:30:45Z"
    },
    {
      "id": 102,
      "sourcePath": "docs/guide.md",
      "targetLanguage": "en",
      "targetPath": "translations/en/docs/guide.md",
      "status": "translating",
      "tokensUsed": 0,
      "translatedAt": null
    },
    {
      "id": 103,
      "sourcePath": "docs/api.md",
      "targetLanguage": "ja",
      "targetPath": "translations/ja/docs/api.md",
      "status": "pending",
      "tokensUsed": 0,
      "translatedAt": null
    }
  ]
}
```

**文件状态值**：
| status | 含义 | 建议展示 |
|--------|------|---------|
| pending | 排队中 | el-tag type="info" |
| translating | 翻译中 | el-tag type="warning" |
| completed | 已完成 | el-tag type="success" |
| failed | 失败 | el-tag type="danger"，显示 errorMessage |

---

### 4.4 取消翻译任务
```
POST /api/tasks/{id}/cancel
```
**Response**: `{ "code": 200 }`
> 仅 `queued` 和 `running` 状态可取消。

---

## 五、仪表盘模块

### 5.1 获取总览统计
```
GET /api/dashboard/stats
```
**Response**:
```json
{
  "totalRepos": 3,
  "totalTranslatedFiles": 127,
  "totalLanguages": 5,
  "monthlyTasks": 12
}
```

---

### 5.2 获取最近翻译任务
```
GET /api/dashboard/recent-tasks?limit=10
```
**Response**:
```json
[
  {
    "id": 42,
    "repositoryName": "jiangjing1219/IM-SERVICE",
    "triggerType": "webhook",
    "status": "completed",
    "totalFiles": 5,
    "completedFiles": 5,
    "createdAt": "2026-03-18T14:30:00Z"
  }
]
```

---

## 六、额度与设置模块

### 6.1 查询额度信息
```
GET /api/quota
```
**Response**:
```json
{
  "freeQuota": 100000,
  "usedQuota": 45230,
  "remaining": 54770,
  "hasCustomApiKey": true
}
```

---

### 6.2 查询额度使用记录
```
GET /api/quota/usage?page=1&size=20
```
**Response** (`data.records` 单项):
```json
{
  "id": 1,
  "repositoryName": "jiangjing1219/IM-SERVICE",
  "taskId": 42,
  "tokensUsed": 12400,
  "source": "platform",
  "createdAt": "2026-03-18T14:33:15Z"
}
```
| source 值 | 含义 |
|-----------|------|
| platform | 消耗平台额度 |
| custom_key | 使用用户自带 Key |

---

### 6.3 配置 OpenRouter API Key
```
PUT /api/settings/api-key
```
**Request Body**:
```json
{
  "apiKey": "sk-or-v1-xxxxxxxxxxxx"
}
```
**Response**: `{ "code": 200 }`

---

### 6.4 验证 API Key
```
POST /api/settings/api-key/verify
```
**Request Body**:
```json
{
  "apiKey": "sk-or-v1-xxxxxxxxxxxx"
}
```
**Response**:
```json
{
  "valid": true,
  "message": "API Key 验证通过"
}
```

---

### 6.5 清除 API Key
```
DELETE /api/settings/api-key
```
**Response**: `{ "code": 200 }`

---

## 七、操作日志

### 7.1 获取仓库操作日志
```
GET /api/repos/{id}/logs?page=1&size=20
```
**Response** (`data.records` 单项):
```json
{
  "id": 1,
  "action": "translation.started",
  "detail": "手动触发全量翻译，目标语言: en, ja, ko",
  "createdAt": "2026-03-18T14:30:00Z"
}
```

**常见 action 值**:
| action | 含义 |
|--------|------|
| translation.started | 翻译开始 |
| translation.completed | 翻译完成 |
| translation.failed | 翻译失败 |
| pr.created | PR 已创建 |
| pr.merged | PR 已合并 |
| webhook.received | 收到 Webhook |
| config.updated | 配置已更新 |

---

## 附录：支持的语言列表

| 代码 | 语言 | 代码 | 语言 |
|------|------|------|------|
| zh | 中文 | fr | Français |
| en | English | de | Deutsch |
| ja | 日本語 | ru | Русский |
| ko | 한국어 | ar | العربية |
| es | Español | hi | हिन्दी |
| pt | Português | | |

---

## 附录：任务状态流转

```
queued → running → completed
                 → failed
       → cancelled
```

## 附录：前端轮询策略

| 场景 | 接口 | 频率 | 停止条件 |
|------|------|------|---------|
| 任务进度 | `GET /api/tasks/{id}` | 3秒 | status 为 completed / failed / cancelled |
