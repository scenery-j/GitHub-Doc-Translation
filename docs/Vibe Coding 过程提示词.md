# 文档准备

## 一、进行需求调研

```markdown
## 角色
你是一位 AI 编程 + 产品调研专家。

## 任务
现在我需要开发一个完整的项目，请你先通过【全网联网搜索】帮我进行需求调研，看看需求是否合理、市面上有没有类似的开源项目或产品，并且通过跟我交互式问答来一步一步确认和完善需求，在你完全理解了需求之后，生成一个标准的《需求规格文档》。

## 约束
1. 你要尽可能详细、全方位地进行调研，至少搜索 10 个文档
2. 你必须只专注于 MVP 最小可行性产品功能的实现，不要做太多无关的功能。

## 我提供给你的信息
我的项目名称：GitHub 仓库翻译工具 github-doc-translation

### 背景
我目前有一个开源仓库：https://github.com/jiangjing1219/IM-SERVICE
现在是纯中文版的，为了能够让国外的用户也来访问我的仓库，增加流量。
我希望能把仓库翻译成英文版，不能人工翻译
● AI 自动翻译
● 能够在修改内容之后自动更新翻译后的内容
为了不局限于我自己的项目，我不打算开发针对单个项目仓库的 GitHub Workflow，干脆做一个更通用的工具。

### 具体需求
输入一个 GitHub 仓库，AI 会自动将 GitHub 仓库的文档翻译成多种不同的语言，并且设置一个基准语言，只要基准语言的内容发生了变更，自动同步翻译其他语言。

```

### 1、gpt-5.3-codex  调研回复

```markdown
已完成第一轮全网调研，覆盖了**开源工具、SaaS 产品、GitHub 自动化机制、翻译 API 限制**四个面向。你的需求整体**合理且可做**，并且市场上已有类似方向产品，说明需求真实存在，但“通用化 + 自动增量同步 + 多语言质量可控”这三点同时做好，才有差异化。

## 已检索资料（>=10）

1. [zdoc / readme-i18n](https://readme-i18n.com/en)  
2. [GitLocalize 官网](https://gitlocalize.com/)  
3. [Translate Readme Action（GitHub Marketplace）](https://github.com/marketplace/actions/translate-readme-action)  
4. [ephraimduncan/translate-readme](https://github.com/ephraimduncan/translate-readme)  
5. [mrf0rtuna4/Github-Readme-AutoTranslator](https://github.com/mrf0rtuna4/Github-Readme-AutoTranslator)  
6. [Docusaurus i18n Introduction](https://docusaurus.io/docs/i18n/introduction)  
7. [Docusaurus + Crowdin](https://docusaurus.io/docs/i18n/crowdin)  
8. [Crowdin GitHub Integration 官方文档](https://support.crowdin.com/github-integration)  
9. [Crowdin GitHub Action README](https://raw.githubusercontent.com/crowdin/github-action/master/README.md)  
10. [DeepL Translate API（官方）](https://developers.deepl.com/api-reference/translate/request-translation)  
11. [GitHub Actions 触发事件（官方）](https://docs.github.com/en/actions/reference/events-that-trigger-workflows)  
12. [GitHub Contents API（官方）](https://docs.github.com/en/rest/repos/contents)  
13. [Weblate Continuous localization（官方）](https://docs.weblate.org/en/latest/admin/continuous.html#github-setup)  
14. [OpenAiTx README](https://raw.githubusercontent.com/OpenAiTx/OpenAiTx/main/README.md)  
15. [@qwen-code/translator（npm）](https://www.npmjs.com/package/@qwen-code/translator?activeTab=readme)

## 初步结论（聚焦 MVP）

- **需求可行**：已有 GitLocalize/Crowdin/Weblate 等证明“仓库文档持续本地化”是成熟场景。  
- **痛点真实**：开源作者常见问题是“只做一次翻译，后续无法持续更新”；你提出的“基准语言变更自动同步”正中痛点。  
- **差异化机会**：很多现有方案要么偏 SaaS、要么偏人工翻译协作、要么只翻 README；你可做“纯 AI + 通用仓库 + 增量同步”的轻量工具。  
- **关键技术风险**：Markdown 直接翻译容易破坏格式（尤其表格/链接/代码块）；MVP 必须做“结构保护 + 增量翻译 + 回滚”。  
- **MVP 边界建议（唯一方案）**：先只支持 GitHub 仓库中的 `README` + `docs/**/*.md(x)`，以 `push` 事件和定时任务触发增量翻译，输出到 `i18n/<lang>/...` 并通过 PR 回写。

## 我建议的 MVP 功能清单（单方案）

- 仓库接入：输入 GitHub repo URL + token（最小权限）。
- 语言配置：1 个基准语言（source）+ N 个目标语言（targets）。
- 文件发现：只处理 Markdown（`.md/.mdx`），可配 include/exclude。
- 增量检测：基于 commit diff 仅翻译变更文件与变更片段。
- AI 翻译：保留代码块、链接、frontmatter，不改技术标识符。
- 自动同步：基准语言变更后自动触发，生成更新 PR。
- 失败处理：翻译失败文件重试 + 失败报告。
- 最小质控：术语表（可选）+ 长文分段 + 基础校验（格式未破坏）。

---

接下来进入**交互确认第 1 轮**（先定产品形态，后面我再继续第 2~3 轮，并最终输出完整《需求规格文档》）。

请你按下面 5 个问题回复（直接写 `Q1:...` 即可）：

1. **Q1 部署形态**：你要做成 `GitHub App`、`CLI 工具`，还是 `Web 平台 + GitHub OAuth`？  
2. **Q2 目标用户**：先服务“你自己和少量开发者”，还是一开始就面向公开多租户？  
3. **Q3 语言策略**：MVP 首批目标语言你想固定哪几种？（建议先 `en` + 1~2 种）  
4. **Q4 翻译引擎**：你是否有指定模型/API（如 OpenAI、Qwen、DeepL）和预算上限？  
5. **Q5 回写策略**：每次更新是“自动提 PR”还是“直接提交到目标分支”？

你答完这 5 个问题后，我会继续下一轮细化，并输出标准化的 SRS（含：范围、角色、流程、功能需求、非功能需求、验收标准、MVP 里程碑）。
```

回复太笼统简陋，信息也不准确，不具备需求调研的要求，不予采纳，更换模型。

### 2、claude 4.6 opus 调研回复

![image-20260318103017422](/Users/scenery/Library/Application Support/typora-user-images/image-20260318103017422.png)

```markdown
调研完成！我已经搜索了超过 10 个相关文档和产品。下面是我的调研总结和交互式问答。

---

## 一、市场调研结果

### 需求合理性判断：**非常合理且有市场需求**

翻译 GitHub 仓库文档是一个真实且普遍的痛点。Ollama (164K stars) 曾因无法维护多语言 README 而关闭了中文翻译 PR，说明**自动化翻译+自动同步**是刚需。

### 现有竞品分析（已发现 8+ 个相关产品/工具）

| 产品/工具 | 类型 | Stars | 核心特点 | 主要不足 |
|---|---|---|---|---|
| **Localizeflow** | SaaS + GitHub App | - | 零配置、自动创建PR、AI路径检测 | 免费层限制大（50k tokens一次性，最多3语言） |
| **Co-op Translator** (微软) | CLI/Docker/Python包 | 597 | 支持50+语言、翻译Markdown/Notebook/图片文字 | 依赖Azure服务，配置复杂 |
| **zdoc (readme-i18n.com)** | SaaS平台 | - | 免费、自动保持更新 | 仅限README，通过第三方域名访问 |
| **OpenAiTx** | 平台服务 | 160 | 20种语言、SEO友好 | 需提交到其平台，无法自主控制 |
| **GitLocalize** | SaaS平台 | - | 双向同步、翻译记忆、术语表 | 更偏向人工翻译管理，非AI驱动 |
| **Lingo.dev CLI** | CLI工具 | - | Delta增量翻译、SHA-256变更检测、5步流水线 | 主要面向应用i18n文件，非文档翻译 |
| **GPT Translate** (GitHub Action) | GitHub Action | - | 通过issue评论触发翻译 | 单文件操作，非批量 |
| **@qwen-code/translator** | Node.js CLI | - | 多语言、自动生成Nextra文档站、Git同步 | 绑定Qwen API，语言有限（5种） |
| **gh-action-markdown-translate-ai** | GitHub Action | - | 多AI提供商、保留Markdown格式、翻译统计 | 仅翻译Markdown文件 |

---

## 二、现有产品的共同痛点（你的机会点）

1. **大多只翻译 README**，不覆盖整个文档目录
2. **平台锁定严重** — 很多工具依赖特定AI供应商或SaaS平台
3. **增量同步能力弱** — 多数工具无法精确追踪基准语言的变更并只翻译差异部分
4. **配置复杂** — Co-op Translator需要Azure配置，GitLocalize需要学习平台
5. **翻译形式单一** — 要么生成文件到仓库，要么只能通过第三方网站访问

---

## 三、交互式问答（请逐一回答）

为了更好地确定你的 MVP 范围和差异化方向，我需要确认以下几个问题：

**Q1：翻译范围**
你希望翻译哪些内容？
- A) 只翻译 README.md
- B) ☑️ 翻译 README + docs 文件夹下的所有 Markdown 文档 （允许用户自己设置需要翻译哪些目录下的内容，通过可视化的方式选择（采用树形结构将用户仓库中包含 markdown 文件的文件目录展示出来，也可以通过.**-ignore 文件配置，要保证配置和可视化选择的内容同步。暂时不翻译图片 
- C) 翻译仓库中所有 Markdown 文件（包括 CONTRIBUTING.md, CHANGELOG.md 等）
- D) 其他（请说明）

**Q2：产品形态**
你更倾向哪种产品形态？
- A) **GitHub Action** — 用户在自己仓库的 workflow 中配置使用
- B) **CLI 工具** — 用户本地命令行运行
- C) ☑️ **GitHub App / SaaS 平台** — 用户通过网站安装，全自动运行（直接为 github 仓库提供现成的 saas 服务）
- D) 组合方案（请说明）

**Q3：翻译后的文件存放方式**
翻译结果怎么存放？
- A) 直接在原仓库中生成翻译文件（如 `README.en.md`、`docs/en/xxx.md`）
		 采用这种方式，翻译完成直接向目标仓库提交一个 PR ,但是存放的目录改为： translations/en/xxx.md
- B) 创建一个新的分支存放翻译内容
- C) Fork 到新仓库
- D) 其他方案


**Q4：AI 翻译引擎**
你希望支持哪些翻译引擎？（可多选）
- A) OpenAI (GPT-4o等)
- B) DeepSeek
- C) 通义千问 (Qwen)
- D) Claude (Anthropic)
- E) 用户自定义 API（兼容 OpenAI 格式）
- F) ☑️ 其他，直接使用 OpenRouter 服务，可以同时对接多个大模型 ，官方链接：https://openrouter.ai/models

**Q5：变更检测与增量翻译**
当基准语言内容发生变更时，你期望的同步方式是？
- A) 全量重新翻译所有文件
- B) 只翻译发生变更的文件（文件级增量）
- C) 只翻译文件中发生变更的段落/章节（段落级增量）
答：目前初略想法是采用【只翻译发生变更的文件（文件级增量）】的方式，但是如果你能实现【只翻译文件中发生变更的段落/章节（段落级增量）】更好了。

**Q6：技术栈偏好**
你偏好用什么语言/技术栈开发？
- A) TypeScript/Node.js
- B) Python
- C) ☑️ Java （后端采用 Java （ai 接入框架采用 Spring AI Alibaba）， 前端使用 vue （组建库采用 https://element-plus.org/zh-CN/component ））
- D) Go
- E) 无偏好，你来推荐

**Q7：目标用户**
这个工具的主要目标用户是？
- A) 中文开源作者（想把项目推向国际）
- B) 全球所有开源项目维护者
- C) ☑️ 两者兼顾，但优先服务中文开源社区（选择）

请回答以上问题，我会根据你的回答进一步细化需求，并生成完整的《需求规格文档》。
```



### 4、 claude 4.6 还需要补充问答

```markdown
感谢你的详细回答！你的需求已经非常清晰了。在我生成《需求规格文档》之前，还有几个关键的 MVP 细节需要确认，这些会直接影响系统架构设计：

---

## 补充问答（第二轮）

**Q8：用户认证与授权**
作为 SaaS 平台，用户登录方式确认：
- A) ☑️ 用户通过 GitHub OAuth 登录，并安装你的 GitHub App 授权访问仓库（采用）
- B) 用户手动填写 GitHub Personal Access Token
- C) 其他方案

**Q9：AI 翻译费用承担**
OpenRouter 的 API 调用是有成本的，MVP 阶段的付费模式：
- A) **平台承担** — 平台提供免费额度，用户无需配置任何 API Key
- B) **用户承担** — 用户在平台中配置自己的 OpenRouter API Key
- C) ☑️ **混合模式** — 平台提供少量免费额度，超出后用户填自己的 Key （平台免费额度使用之后，支持用户充值后继续使用，或者用户在平台中配置自己的 OpenRouter API Key）
- D) 其他

**Q10：MVP 目标语言**
MVP 阶段默认支持翻译到哪些目标语言？（用户可选）
- A) 仅支持 中文 ↔ 英文
- B) 中文 + 英文 + 日文 + 韩文（东亚优先）
- C) ☑️ 支持 10+ 种主流语言（英/日/韩/法/德/西/俄/葡/阿/印地等）（采用）
- D) 支持 OpenRouter 模型能力范围内的所有语言，让用户自选

**Q11：自动同步触发机制**
当基准语言内容发生变更时，如何触发自动翻译同步？
- A) **Webhook 实时触发** — 通过 GitHub App 监听 push 事件，检测到基准语言文件变更后自动触发翻译
- B) **定时轮询** — 定期检查仓库是否有变更
- C) **手动触发** — 用户在 SaaS 平台上点击按钮手动触发同步
- D) ☑️ A + C 结合（Webhook 自动 + 可手动触发）（采用）

**Q12：PR 提交策略**
翻译完成后的 PR 提交方式：
- A) 每种语言一个 PR（如：`translation/en`、`translation/ja` 各一个 PR）
- B) ✅ 所有语言合并到一个 PR （采用）
- C) 用户可在平台上配置选择 A 或 B

**Q13：翻译审核机制**
翻译结果提交 PR 之前，是否需要用户审核？
- A) ✅ **直接提交 PR** — 翻译完成自动提交，用户通过 GitHub PR Review 流程审核（先采这种方式，B 方式可作为后期的扩展功能）
- B) **平台内预览** — 用户先在 SaaS 平台上预览翻译结果，确认后再提交 PR
- C) A 为默认，B 作为可选高级功能

**Q14：部署方式**
SaaS 平台你打算怎么部署？
- A) 云服务器自建部署
- B) ✅ Docker 容器化部署
- C) 既支持 SaaS 也支持用户私有化部署（开源项目）
- D) MVP 阶段先不考虑，先把功能做出来

---

请回答以上补充问题，回答完毕后我将立即生成完整的《需求规格文档》。
```

### 5、 最终生成的需求文档

````markdown
# GitHub Doc Translation — 需求规格文档 (PRD)

> **项目名称**: GitHub Doc Translation (github-doc-translation)
> **文档版本**: v1.0
> **创建日期**: 2026-03-18
> **文档状态**: MVP 需求确认

---

## 一、项目概述

### 1.1 项目简介

GitHub Doc Translation 是一个 **SaaS 平台**，为 GitHub 开源仓库提供 **AI 驱动的多语言文档自动翻译服务**。用户通过 GitHub OAuth 登录，安装 GitHub App 授权仓库后，平台会自动将仓库中的 Markdown 文档翻译成多种目标语言，并以 Pull Request 的形式提交到原仓库。当基准语言的文档发生变更时，平台自动检测并增量翻译同步。

### 1.2 项目背景

许多中文开源项目（如 IM-SERVICE）质量优秀但只有中文文档，导致国际用户难以使用，限制了项目的影响力和流量增长。人工翻译成本高、难以持续维护。现有工具多为 CLI / GitHub Action 形态，配置复杂，且缺乏可视化管理能力。本项目旨在提供一站式、零配置的 SaaS 翻译服务，降低开源项目国际化的门槛。

### 1.3 核心目标

| 目标 | 描述 |
|------|------|
| **降低门槛** | 开源作者无需任何翻译/i18n 知识，一键接入即可完成文档多语言化 |
| **自动同步** | 基准语言文档变更后，自动增量翻译并提交 PR |
| **尊重 GitHub 原生工作流** | 翻译结果以 PR 形式提交，维护者通过标准 Review 流程审核 |
| **灵活的 AI 引擎** | 通过 OpenRouter 接入 300+ 大模型，用户可自选翻译引擎 |

---

## 二、市场分析与竞品调研

### 2.1 竞品全景

| 竞品 | 类型 | Stars | 翻译引擎 | 增量同步 | 可视化管理 | 文件范围 | 价格 |
|------|------|-------|----------|----------|------------|----------|------|
| **Localizeflow** | SaaS / GitHub App | - | Azure OpenAI (Co-op Translator) | ✅ Hash-based | ❌ | 文档目录 | 免费层受限 (50k tokens + 7k/月) |
| **Co-op Translator** (微软) | CLI / Docker | 597 | Azure OpenAI | ❌ | ❌ | Markdown / Notebook / 图片 | 免费开源（需自备Azure） |
| **zdoc (readme-i18n.com)** | SaaS 平台 | - | 未公开 | ✅ | ❌ | 仅 README | 免费 |
| **OpenAiTx** | 平台服务 | 160 | AI (未明确) | ✅ 自动更新 | ❌ | README / Wiki | 免费 |
| **GitLocalize** | SaaS 平台 | - | 人工 + 机器 | ✅ 双向同步 | ✅ 基础 | Markdown / HTML / JSON / YAML | 免费 |
| **Lingo.dev CLI** | CLI 工具 | - | 多 LLM | ✅ SHA-256 Delta | ❌ | i18n 文件 | 免费开源 |
| **GPT Translate** | GitHub Action | - | OpenAI GPT | ❌ | ❌ | 单文件 | 免费开源 |
| **@qwen-code/translator** | Node.js CLI | - | Qwen API | ✅ Git 同步 | ❌ | 文档目录 | 免费开源 |
| **gh-action-markdown-translate-ai** | GitHub Action | - | 多 AI 提供商 | ❌ | ❌ | Markdown | 免费开源 |

### 2.2 差异化分析

| 差异化维度 | 现有竞品现状 | 本项目方案 | 优势 |
|-----------|-------------|-----------|------|
| **产品形态** | 多数为 CLI / GitHub Action，需手动配置 workflow | SaaS 平台 + GitHub App，零配置安装 | 极大降低使用门槛，非技术用户也能使用 |
| **翻译范围选择** | 硬编码或简单配置文件指定翻译目录 | **可视化树形目录选择** + `.gitdoc-ignore` 配置文件双向同步 | 直观灵活，所见即所得 |
| **AI 引擎** | 绑定单一供应商 (Azure OpenAI / Qwen / GPT) | 通过 **OpenRouter 接入 300+ 模型**，用户自选 | 无供应商锁定，可选最优性价比模型 |
| **增量翻译** | 多数为全量翻译或简单文件级增量 | **文件级增量**（MVP）+ **段落级增量**（后续） | 节省 API 成本，翻译速度更快 |
| **付费模式** | 免费但功能受限 / 需自备云服务 | **混合模式**：平台免费额度 + 用户充值 + 自带 Key | 灵活，降低试用门槛 |
| **目标社区** | 面向英语开发者为主 | **优先服务中文开源社区**，帮助中文项目走向国际 | 细分市场定位精准，差异化明显 |
| **PR 工作流** | 部分工具直接 commit 到仓库 | **标准 PR 工作流**，尊重维护者审核流程 | 安全可控，符合开源协作最佳实践 |
| **多语言支持** | 通常 5-20 种语言 | **10+ 种主流语言**，覆盖全球主要开发者群体 | 覆盖面广 |

### 2.3 核心竞争力总结

1. **零配置 SaaS 体验** — 区别于 CLI/Action 的繁琐配置
2. **可视化翻译管理** — 树形目录选择，翻译状态一目了然
3. **OpenRouter 多模型自由选择** — 打破供应商锁定
4. **中文开源社区优先** — 精准定位，填补市场空白

---

## 三、用户画像

### 3.1 主要用户角色

| 角色 | 描述 | 核心诉求 |
|------|------|----------|
| **中文开源作者** | 有优质项目但只有中文文档，希望吸引国际用户 | 一键翻译、自动同步、零维护成本 |
| **开源项目维护者** | 管理多语言文档的项目维护者 | 集中管理翻译状态、控制翻译质量 |
| **国际化推广者** | 帮助开源社区推广项目的贡献者 | 批量翻译、多语言覆盖 |

### 3.2 用户使用旅程（核心流程）

```
用户访问平台 → GitHub OAuth 登录 → 安装 GitHub App → 选择仓库
→ 可视化选择翻译目录/文件 → 设置基准语言 & 目标语言 → 选择 AI 模型
→ 启动翻译 → 平台自动翻译 → 自动提交 PR 到原仓库
→ 用户在 GitHub 上 Review & Merge PR
→ 后续基准语言变更 → Webhook 触发 → 自动增量翻译 → 自动提交新 PR
```

---

## 四、功能需求（MVP）

### 4.1 功能模块总览

| 模块编号 | 模块名称 | 优先级 | MVP 包含 |
|---------|---------|--------|---------|
| M1 | 用户认证与授权 | P0 | ✅ |
| M2 | 仓库管理 | P0 | ✅ |
| M3 | 翻译配置 | P0 | ✅ |
| M4 | 翻译引擎 | P0 | ✅ |
| M5 | 变更检测与增量同步 | P0 | ✅ |
| M6 | PR 管理 | P0 | ✅ |
| M7 | 额度与计费 | P1 | ✅ |
| M8 | 仪表盘与状态监控 | P1 | ✅ |
| M9 | 翻译预览与审核 | P2 | ❌ (后续) |
| M10 | 术语表 & 翻译记忆 | P2 | ❌ (后续) |

---

### 4.2 详细功能说明

#### M1: 用户认证与授权

| 功能 | 描述 |
|------|------|
| F1.1 GitHub OAuth 登录 | 用户通过 GitHub OAuth 2.0 登录平台，获取用户基本信息（用户名、头像、邮箱） |
| F1.2 GitHub App 安装引导 | 首次登录后引导用户安装 GitHub App，授权平台访问其仓库 |
| F1.3 权限管理 | GitHub App 需要的权限：`contents: read/write`（读写仓库内容）、`pull_requests: write`（创建 PR）、`metadata: read`（读取仓库元信息） |
| F1.4 Token 管理 | 安全存储 GitHub Installation Token，自动刷新过期 Token |

#### M2: 仓库管理

| 功能 | 描述 |
|------|------|
| F2.1 仓库列表 | 展示用户已授权的所有 GitHub 仓库列表（支持分页/搜索） |
| F2.2 添加仓库 | 用户选择仓库并添加到平台进行翻译管理 |
| F2.3 仓库详情 | 展示仓库的翻译状态、最近同步时间、已翻译语言等信息 |
| F2.4 移除仓库 | 从平台中移除仓库的翻译管理（不删除已生成的翻译文件） |

#### M3: 翻译配置

| 功能 | 描述 |
|------|------|
| F3.1 可视化目录选择 | 以**树形结构**展示仓库中包含 Markdown 文件的目录，用户通过勾选/取消勾选来选择需要翻译的文件和目录 |
| F3.2 .gitdoc-ignore 配置 | 支持 `.gitdoc-ignore` 文件配置忽略规则（语法兼容 `.gitignore`），可在平台中编辑 |
| F3.3 配置双向同步 | 树形可视化选择与 `.gitdoc-ignore` 文件保持**双向同步**：UI 修改自动更新配置文件，配置文件变更自动反映到 UI |
| F3.4 基准语言设置 | 设置仓库的基准语言（如：中文），平台以此为翻译源语言 |
| F3.5 目标语言选择 | 从预置的 10+ 种主流语言中选择目标翻译语言 |
| F3.6 AI 模型选择 | 从 OpenRouter 可用模型列表中选择翻译使用的 AI 模型，展示模型价格参考 |
| F3.7 翻译输出路径 | 翻译文件默认存放在 `translations/{lang}/` 目录下，保持原有目录结构。例如：`docs/guide.md` → `translations/en/docs/guide.md` |

**支持的目标语言（MVP）**：

| 语言 | 代码 | 语言 | 代码 |
|------|------|------|------|
| English | en | Français | fr |
| 日本語 | ja | Deutsch | de |
| 한국어 | ko | Русский | ru |
| Español | es | العربية | ar |
| Português | pt | हिन्दी | hi |

#### M4: 翻译引擎

| 功能 | 描述 |
|------|------|
| F4.1 OpenRouter 集成 | 通过 OpenRouter API（兼容 OpenAI 格式）调用大模型进行翻译 |
| F4.2 Markdown 感知翻译 | 翻译时保留 Markdown 格式，包括：标题层级、代码块（不翻译）、链接、图片引用、表格结构、列表层级、YAML front matter（不翻译） |
| F4.3 上下文翻译 | 翻译时提供文件路径、项目名称等上下文信息，提高翻译的专业性和一致性 |
| F4.4 翻译 Prompt 工程 | 内置专业翻译 Prompt 模板，确保翻译质量。包含：保持技术术语准确性、保留代码/命令不翻译、尊重目标语言的习惯用法 |
| F4.5 并发翻译 | 支持多文件并行翻译，提升翻译速度 |
| F4.6 翻译失败重试 | API 调用失败时自动重试（最多 3 次），超过上限标记为失败并通知用户 |

#### M5: 变更检测与增量同步

| 功能 | 描述 |
|------|------|
| F5.1 Webhook 监听 | 通过 GitHub App Webhook 监听 `push` 事件，检测基准语言文件的变更 |
| F5.2 文件级增量检测 | 对比 push 事件的 commit diff，识别出基准语言中新增、修改、删除的 Markdown 文件 |
| F5.3 增量翻译 | 只对发生变更的文件执行翻译，跳过未变更文件 |
| F5.4 删除同步 | 当基准语言的文件被删除时，同步删除对应的翻译文件 |
| F5.5 手动触发 | 用户可在平台上手动触发全量或增量翻译同步 |
| F5.6 变更记录 | 记录每次同步的变更文件列表、翻译状态、耗时等信息 |

#### M6: PR 管理

| 功能 | 描述 |
|------|------|
| F6.1 自动创建 PR | 翻译完成后自动向原仓库创建 PR，所有目标语言的翻译合并到一个 PR 中 |
| F6.2 PR 内容规范 | PR 标题格式：`[doc-translation] Update translations ({lang_list})`；PR 描述包含翻译摘要（变更文件列表、目标语言、使用的 AI 模型） |
| F6.3 分支策略 | 创建专用翻译分支 `doc-translation/{timestamp}` 作为 PR 的 source branch |
| F6.4 PR 状态追踪 | 在平台中展示每个 PR 的状态（pending / merged / closed），与 GitHub 实时同步 |
| F6.5 冲突处理 | 若已有未合并的翻译 PR，新的翻译更新追加到现有 PR 而非创建新 PR |

#### M7: 额度与计费

| 功能 | 描述 |
|------|------|
| F7.1 免费额度 | 新用户注册后获得平台提供的免费翻译额度（以 token 数计量） |
| F7.2 额度查询 | 用户可在平台查看当前剩余额度、历史使用量 |
| F7.3 用户充值 | 免费额度用完后，支持用户在平台充值购买更多额度 |
| F7.4 自带 API Key | 支持用户配置自己的 OpenRouter API Key，使用自己的 Key 时不消耗平台额度 |
| F7.5 用量统计 | 记录每次翻译的 token 消耗、费用估算（基于 OpenRouter 定价） |

#### M8: 仪表盘与状态监控

| 功能 | 描述 |
|------|------|
| F8.1 总览仪表盘 | 展示用户所有仓库的翻译总览：已翻译仓库数、已翻译文件数、已翻译语言数 |
| F8.2 仓库翻译状态 | 单个仓库的翻译详情：各语言翻译进度、最后同步时间、待处理变更数 |
| F8.3 翻译任务列表 | 展示翻译任务队列，包含状态（排队中 / 翻译中 / 已完成 / 失败）、进度百分比 |
| F8.4 操作日志 | 记录并展示所有操作日志：翻译任务、PR 创建、Webhook 接收等 |

---

## 五、非功能需求

### 5.1 性能要求

| 指标 | 要求 |
|------|------|
| 单文件翻译延迟 | ≤ 30 秒（中等长度 Markdown 文件） |
| 并发翻译 | 支持同时翻译至少 5 个文件 |
| Webhook 响应 | 收到 Webhook 后 5 秒内确认接收，异步处理翻译任务 |
| 页面加载 | 首页/仪表盘加载时间 ≤ 2 秒 |

### 5.2 安全要求

| 项目 | 要求 |
|------|------|
| 认证 | GitHub OAuth 2.0，JWT Token 鉴权 |
| GitHub Token | Installation Token 加密存储，定时刷新 |
| API Key | 用户 OpenRouter API Key 使用 AES 加密存储 |
| HTTPS | 全站 HTTPS |
| 权限最小化 | GitHub App 仅申请必要权限 |

### 5.3 可靠性要求

| 项目 | 要求 |
|------|------|
| 翻译任务 | 失败自动重试 3 次，超限告警 |
| 数据持久化 | 翻译配置与任务记录持久化存储 |
| Webhook 幂等 | 相同事件重复投递不会触发重复翻译 |

### 5.4 可扩展性

| 项目 | 要求 |
|------|------|
| 语言扩展 | 新增目标语言仅需配置，无需改代码 |
| AI 模型 | 通过 OpenRouter 自动获取最新模型列表 |
| 文件格式 | 架构预留对 RST、AsciiDoc 等格式的扩展能力 |

---

## 六、系统架构设计（概要）

### 6.1 技术栈

| 层级 | 技术选型 | 说明 |
|------|---------|------|
| **前端** | Vue 3 + Element Plus | 用户管理界面、仪表盘、可视化配置 |
| **后端** | Java 17 + Spring Boot 3 | 核心业务逻辑 |
| **AI 集成** | Spring AI Alibaba + OpenRouter | AI 翻译引擎，通过 OpenAI 兼容协议对接 OpenRouter |
| **数据库** | MySQL 8.0 | 用户数据、仓库配置、翻译任务记录 |
| **缓存** | Redis | Token 缓存、任务队列、限流 |
| **任务队列** | Redis (或 RabbitMQ) | 异步翻译任务调度 |
| **GitHub 集成** | GitHub REST API v3 / GitHub App | OAuth、Webhook、PR 操作 |
| **容器化** | Docker + Docker Compose | 一键部署 |

### 6.2 系统架构图（文本描述）

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Vue 3)                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────┐  │
│  │  登录页   │ │ 仓库管理  │ │ 翻译配置  │ │  仪表盘   │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────┘  │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP/REST API
┌──────────────────────▼──────────────────────────────────┐
│                 Backend (Spring Boot)                     │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────────────┐  │
│  │ Auth Module   │ │ Repo Module  │ │ Config Module   │  │
│  │ (GitHub OAuth)│ │ (仓库管理)    │ │ (翻译配置管理)  │  │
│  └──────────────┘ └──────────────┘ └─────────────────┘  │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────────────┐  │
│  │ Translation  │ │ Webhook      │ │ PR Module       │  │
│  │ Engine       │ │ Handler      │ │ (PR 管理)       │  │
│  └──────┬───────┘ └──────────────┘ └─────────────────┘  │
│         │         ┌──────────────┐ ┌─────────────────┐  │
│         │         │ Quota Module │ │ Task Scheduler  │  │
│         │         │ (额度计费)    │ │ (任务调度)       │  │
│         │         └──────────────┘ └─────────────────┘  │
└─────────┼───────────────────────────────────────────────┘
          │ OpenAI-compatible API
┌─────────▼───────────┐  ┌────────────────┐  ┌───────────┐
│  OpenRouter API     │  │  GitHub API    │  │  MySQL    │
│  (300+ AI Models)   │  │  (REST v3)     │  │  Redis    │
└─────────────────────┘  └────────────────┘  └───────────┘
```

### 6.3 核心数据模型（概要）

```
User (用户)
├── id, github_id, username, avatar_url, email
├── openrouter_api_key (encrypted)
├── free_quota, used_quota
└── created_at, updated_at

Repository (仓库)
├── id, user_id, github_repo_id, full_name, default_branch
├── base_language, target_languages (JSON)
├── translation_paths (JSON), ignore_patterns (JSON)
├── ai_model, webhook_active
├── status (active / paused / error)
└── last_sync_at, created_at

TranslationTask (翻译任务)
├── id, repository_id, trigger_type (webhook / manual)
├── status (queued / running / completed / failed)
├── changed_files (JSON), target_languages (JSON)
├── total_files, completed_files, failed_files
├── tokens_used, estimated_cost
├── pr_number, pr_url
├── started_at, completed_at
└── error_message

TranslationFile (翻译文件记录)
├── id, task_id, repository_id
├── source_path, target_language, target_path
├── status (pending / translating / completed / failed)
├── source_hash (用于增量检测)
├── tokens_used
└── translated_at

OperationLog (操作日志)
├── id, user_id, repository_id
├── action, detail
└── created_at
```

---

## 七、API 接口设计（概要）

### 7.1 认证相关

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/auth/github` | 跳转 GitHub OAuth 授权 |
| GET | `/api/auth/github/callback` | GitHub OAuth 回调 |
| GET | `/api/auth/me` | 获取当前用户信息 |
| POST | `/api/auth/logout` | 退出登录 |

### 7.2 仓库管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/repos` | 获取用户已授权的仓库列表 |
| POST | `/api/repos` | 添加仓库到平台 |
| GET | `/api/repos/{id}` | 获取仓库详情 |
| DELETE | `/api/repos/{id}` | 移除仓库 |
| GET | `/api/repos/{id}/tree` | 获取仓库 Markdown 文件树形结构 |

### 7.3 翻译配置

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/repos/{id}/config` | 获取翻译配置 |
| PUT | `/api/repos/{id}/config` | 更新翻译配置（目录选择、语言、模型等） |
| GET | `/api/repos/{id}/ignore` | 获取 .gitdoc-ignore 内容 |
| PUT | `/api/repos/{id}/ignore` | 更新 .gitdoc-ignore 内容 |
| GET | `/api/models` | 获取可用 AI 模型列表（从 OpenRouter） |

### 7.4 翻译任务

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/repos/{id}/translate` | 手动触发翻译 |
| GET | `/api/repos/{id}/tasks` | 获取翻译任务列表 |
| GET | `/api/tasks/{id}` | 获取任务详情（含进度） |
| POST | `/api/tasks/{id}/cancel` | 取消翻译任务 |

### 7.5 Webhook

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/webhook/github` | 接收 GitHub App Webhook 事件 |

### 7.6 额度与计费

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/quota` | 查询当前额度 |
| GET | `/api/quota/usage` | 查询使用记录 |
| PUT | `/api/settings/api-key` | 配置用户 OpenRouter API Key |

---

## 八、核心业务流程

### 8.1 首次翻译流程

```
1. 用户登录 → 安装 GitHub App → 授权仓库
2. 选择仓库 → 加载文件树 → 可视化选择翻译目录/文件
3. 设置基准语言 (如: zh) → 选择目标语言 (如: en, ja, ko)
4. 选择 AI 模型 (如: deepseek/deepseek-chat)
5. 点击 "开始翻译"
6. 系统创建翻译任务 → 入队列
7. 任务调度器取出任务 → 遍历选中的文件
8. 对每个文件:
   a. 读取 GitHub 仓库中的原文件内容
   b. 计算文件 hash 并记录
   c. 调用 OpenRouter API 翻译为各目标语言
   d. 保存翻译结果
9. 所有文件翻译完成 → 创建翻译分支
10. 将翻译文件写入 translations/{lang}/ 目录
11. 创建 PR → 记录 PR 信息
12. 通知用户翻译完成
```

### 8.2 增量同步流程

```
1. GitHub Webhook 推送 push 事件
2. 系统接收并验证 Webhook 签名
3. 解析 commit diff，提取变更的 Markdown 文件列表
4. 过滤：只保留在翻译配置范围内的文件
5. 过滤：只保留基准语言的文件（排除 translations/ 目录下的变更）
6. 如果有变更文件:
   a. 创建增量翻译任务
   b. 仅翻译变更的文件
   c. 检查是否有未合并的翻译 PR
      - 有：追加到现有 PR
      - 无：创建新 PR
7. 如果无变更文件：跳过，记录日志
```

---

## 九、MVP 范围与排期建议

### 9.1 MVP 功能边界

**MVP 包含（Must Have）**：
- ✅ GitHub OAuth 登录 + GitHub App 安装
- ✅ 仓库列表与添加
- ✅ 可视化目录树选择翻译范围
- ✅ .gitdoc-ignore 配置（基础版）
- ✅ 基准语言 & 目标语言设置
- ✅ OpenRouter AI 模型选择与翻译
- ✅ Markdown 格式保留翻译
- ✅ 文件级增量翻译
- ✅ Webhook 自动检测 + 手动触发
- ✅ 自动创建 PR（所有语言合并一个 PR）
- ✅ 免费额度 + 自带 API Key
- ✅ 基础仪表盘 & 任务状态
- ✅ Docker 部署

**MVP 不包含（后续迭代）**：
- ❌ 段落级增量翻译
- ❌ 平台内翻译预览与编辑
- ❌ 术语表 & 翻译记忆
- ❌ 图片中文字翻译
- ❌ 多人协作翻译
- ❌ 付费充值系统（MVP 阶段仅免费额度 + BYOK）
- ❌ 翻译质量评分
- ❌ 邮件/通知系统

### 9.2 里程碑建议

| 阶段 | 内容 | 预估周期 |
|------|------|---------|
| **M0: 项目初始化** | 技术选型确认、项目骨架搭建、数据库设计、Docker 配置 | 1 周 |
| **M1: 认证与仓库管理** | GitHub OAuth、GitHub App 注册、仓库列表/添加、文件树加载 | 1.5 周 |
| **M2: 翻译配置** | 可视化目录选择、.gitdoc-ignore、语言设置、AI 模型选择 | 1 周 |
| **M3: 核心翻译引擎** | OpenRouter 集成、Markdown 感知翻译、并发翻译、任务队列 | 2 周 |
| **M4: PR 管理 & 增量同步** | 自动创建 PR、Webhook 监听、文件级增量翻译 | 1.5 周 |
| **M5: 额度 & 仪表盘** | 额度管理、用量统计、仪表盘、任务监控 | 1 周 |
| **M6: 测试 & 部署** | 集成测试、Docker 部署脚本、文档编写 | 1 周 |
| **合计** | | **约 9 周** |

---

## 十、翻译文件存放规范

### 10.1 目录结构示例

原始仓库结构：
```
my-project/
├── README.md
├── docs/
│   ├── guide.md
│   ├── api.md
│   └── faq/
│       └── common.md
├── .gitdoc-ignore
└── src/
    └── ...
```

翻译后的仓库结构：
```
my-project/
├── README.md                          # 基准语言 (中文)
├── docs/
│   ├── guide.md
│   ├── api.md
│   └── faq/
│       └── common.md
├── translations/
│   ├── en/                            # English
│   │   ├── README.md
│   │   └── docs/
│   │       ├── guide.md
│   │       ├── api.md
│   │       └── faq/
│   │           └── common.md
│   ├── ja/                            # 日本語
│   │   ├── README.md
│   │   └── docs/
│   │       └── ...
│   └── ko/                            # 한국어
│       ├── README.md
│       └── docs/
│           └── ...
├── .gitdoc-ignore
└── src/
    └── ...
```

### 10.2 .gitdoc-ignore 文件示例

```gitignore
# 忽略 CHANGELOG（变更频繁，无需翻译）
CHANGELOG.md

# 忽略特定目录
docs/internal/
docs/deprecated/

# 忽略特定模式
**/draft-*.md
**/WIP-*.md
```

---

## 十一、风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|---------|
| AI 翻译质量不稳定 | 翻译结果不可用，用户体验差 | 精心设计 Prompt、提供模型选择、用户通过 PR Review 把关 |
| OpenRouter API 不稳定 | 翻译任务失败 | 重试机制 + 超时处理 + 任务状态监控 |
| GitHub API Rate Limit | 大量仓库时 API 调用受限 | 请求限流 + Token 池化 + 缓存 |
| 大文件翻译超时 | 超长 Markdown 文件翻译失败 | 文件分段翻译 + 合并结果 |
| Webhook 重复投递 | 重复翻译造成资源浪费 | Webhook 幂等处理（基于 delivery ID 去重） |
| 翻译成本过高 | 平台免费额度消耗过快 | 增量翻译 + 额度限制 + 引导用户自带 Key |

---

## 十二、后续迭代规划

| 版本 | 功能 | 优先级 |
|------|------|--------|
| v1.1 | 段落级增量翻译（基于 Markdown AST diff） | 高 |
| v1.2 | 平台内翻译预览与手动修改 | 高 |
| v1.3 | 术语表管理（确保关键术语翻译一致） | 中 |
| v1.4 | 翻译记忆（缓存已翻译的段落，相似内容复用） | 中 |
| v1.5 | 用户充值系统（接入支付） | 中 |
| v2.0 | 支持更多文件格式（RST、AsciiDoc、Jupyter Notebook） | 低 |
| v2.1 | 图片文字翻译（OCR + 翻译） | 低 |
| v2.2 | 多人协作翻译工作流 | 低 |
| v2.3 | 翻译质量评分与反馈机制 | 低 |

---

> **文档结束**
>
> 本文档为 GitHub Doc Translation 项目的 MVP 需求规格说明，所有功能范围以"MVP 包含"部分为准。后续迭代功能将根据用户反馈和业务优先级逐步推进。

````

## 二、方案设计

### 1、编写提示词

```markdown
## 角色
你是一位专业的程序员架构师，擅长根据需求文档实现技术方案。

## 任务
我会给你一个完整的需求文档，你需要仔细完整分析需求，并且通过【从全网联网搜索信息】来确保你的技术实现方案是可行的，你需要通过 Context7 来获取最新的技术文档信息（尤其是怎么对接 github、GitHub 官方的开发文档）

如果你有不确认的信息，先不要完整生成文档，而是先通过交互式问答找我进行人工确认。

在你给出初版方案后，需要找我进行人工确认，最终帮我输出完整的《技术实现方案文档》

## 约束
1. 你要尽可能详细、全方位地进行网络搜索，确保获取到了最新的技术文档和实现方案（2025~2026年）
2. 你必须只专注于 MVP 最小可行性产品功能的实现，不要做太多无关的功能。
3. 你的任务不包括编写代码，只专注于方案的设计和文档编写

## 我提供给你的信息

@需求文档

```

期间会要求重新确认问题：

![image-20260318124649680](/Users/scenery/Library/Application Support/typora-user-images/image-20260318124649680.png)

### 2、生成的方案设计文档

````markdown
# GitHub Doc Translation — 技术实现方案文档

> **项目名称**: GitHub Doc Translation (github-doc-translation)
> **文档版本**: v1.0
> **创建日期**: 2026-03-18
> **基于**: 需求规格文档 v1.0 (MVP 范围)
> **文档状态**: 初版待确认

---

## 一、技术栈确认与版本

### 1.1 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Java** | 21 (LTS) | 长期支持版本，支持虚拟线程(Virtual Threads)、Record、Pattern Matching 等特性 |
| **Spring Boot** | 4.0.2 | 2026.01 发布的稳定版，基于 Spring Framework 7 |
| **Spring AI** | 1.0.x (最新稳定版) | OpenAI 兼容协议对接 OpenRouter，使用 `spring-ai-openai-spring-boot-starter` |
| **Spring Security** | 7.x (Spring Boot 4 内置) | OAuth 2.0 Client 实现 GitHub 登录 |
| **PostgreSQL** | 17 | 主数据库，存储用户/仓库/任务等业务数据 |
| **Redis** | 7.x | Token 缓存、限流、后续扩展为任务队列 |
| **hub4j/github-api** | 2.x | Java GitHub API 客户端，原生支持 GitHub App JWT 认证 |
| **Flyway** | 最新版 | 数据库版本管理与迁移 |
| **MapStruct** | 1.6.x | 对象映射，DTO/Entity 转换 |
| **Docker** | 最新版 | 容器化部署 |

### 1.2 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Vue** | 3.5.x | 组合式 API (Composition API) |
| **Vite** | 6.x | 极速构建工具，Vue 3 官方推荐 |
| **Element Plus** | 2.11.x | Vue 3 组件库 |
| **Vue Router** | 4.x | 路由管理 |
| **Pinia** | 2.x | 状态管理 |
| **Axios** | 1.x | HTTP 客户端 |
| **Monaco Editor** | 最新版 | `.gitdoc-ignore` 编辑器 |
| **TypeScript** | 5.x | 类型安全 |

### 1.3 开发与部署工具

| 工具 | 用途 |
|------|------|
| **Maven** | 后端依赖管理与构建 |
| **pnpm** | 前端包管理 |
| **Docker Compose** | 本地开发与生产部署编排 |
| **Smee.io** | 本地开发 Webhook 转发代理 |

---

## 二、系统架构设计

### 2.1 整体架构

```
                         ┌──────────────────┐
                         │   用户浏览器       │
                         └────────┬─────────┘
                                  │
                         ┌────────▼─────────┐
                         │  Nginx (反向代理)  │
                         │  HTTPS / 静态资源  │
                         └──┬──────────┬────┘
                            │          │
              ┌─────────────▼──┐   ┌───▼──────────────┐
              │  前端 (Vue 3)   │   │  后端 (Spring Boot)│
              │  Vite 构建      │   │  REST API         │
              │  SPA 单页应用   │   │  Webhook 端点      │
              └────────────────┘   └───┬───┬───┬───────┘
                                       │   │   │
                    ┌──────────────────┘   │   └──────────────────┐
                    │                      │                      │
           ┌────────▼───────┐   ┌─────────▼────────┐   ┌────────▼───────┐
           │  PostgreSQL 17  │   │    Redis 7.x      │   │  GitHub API    │
           │  业务数据存储    │   │  缓存/Token/限流   │   │  REST v3       │
           └────────────────┘   └──────────────────┘   └────────────────┘
                                                                │
                                                       ┌────────▼───────┐
                                                       │  OpenRouter API │
                                                       │  AI 翻译引擎    │
                                                       └────────────────┘
```

### 2.2 后端模块架构

采用 **按业务模块分包** 的方式组织代码，而非传统的按层分包。

```
com.github.doctranslation
├── common/                     # 公共模块
│   ├── config/                 # 全局配置
│   ├── exception/              # 全局异常处理
│   ├── security/               # 安全配置 (JWT、OAuth)
│   └── util/                   # 工具类
├── auth/                       # M1: 认证与授权模块
│   ├── controller/
│   ├── service/
│   └── dto/
├── repository/                 # M2: 仓库管理模块
│   ├── controller/
│   ├── service/
│   ├── entity/
│   └── dto/
├── config/                     # M3: 翻译配置模块
│   ├── controller/
│   ├── service/
│   └── dto/
├── translation/                # M4: 翻译引擎模块
│   ├── engine/                 # AI 翻译核心逻辑
│   ├── markdown/               # Markdown 解析与保留
│   ├── prompt/                 # Prompt 模板管理
│   └── service/
├── sync/                       # M5: 变更检测与同步模块
│   ├── webhook/                # Webhook 接收与处理
│   ├── detector/               # 文件变更检测
│   └── service/
├── pullrequest/                # M6: PR 管理模块
│   ├── controller/
│   ├── service/
│   └── dto/
├── quota/                      # M7: 额度管理模块
│   ├── controller/
│   ├── service/
│   └── dto/
├── dashboard/                  # M8: 仪表盘模块
│   ├── controller/
│   └── service/
└── task/                       # 异步任务调度模块
    ├── queue/                  # 内存任务队列
    ├── executor/               # 任务执行器
    └── model/                  # 任务模型
```

### 2.3 前端项目结构

```
doc_translation_website/
├── public/
├── src/
│   ├── api/                    # API 请求封装
│   │   ├── auth.ts
│   │   ├── repos.ts
│   │   ├── tasks.ts
│   │   └── quota.ts
│   ├── assets/                 # 静态资源
│   ├── components/             # 公共组件
│   │   ├── layout/             # 布局组件 (导航栏、侧边栏)
│   │   ├── common/             # 通用组件 (空状态、加载等)
│   │   └── tree/               # 文件树组件
│   ├── composables/            # 组合式函数
│   ├── router/                 # 路由配置
│   ├── stores/                 # Pinia 状态管理
│   │   ├── auth.ts
│   │   ├── repos.ts
│   │   └── tasks.ts
│   ├── types/                  # TypeScript 类型定义
│   ├── utils/                  # 工具函数
│   └── views/                  # 页面组件
│       ├── landing/            # 落地页
│       ├── login/              # 登录页
│       ├── setup/              # GitHub App 安装引导
│       ├── dashboard/          # 仪表盘
│       ├── repos/              # 仓库管理
│       │   ├── RepoList.vue
│       │   ├── RepoDetail.vue
│       │   └── RepoConfig.vue
│       ├── tasks/              # 任务管理
│       │   └── TaskDetail.vue
│       └── settings/           # 个人设置
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

---

## 三、GitHub App 集成方案（核心）

### 3.1 GitHub App 注册配置

在 GitHub 上注册 GitHub App 时需要设置以下参数：

| 配置项 | 值 | 说明 |
|--------|-----|------|
| **App Name** | `GitHub Doc Translation` | 全局唯一 |
| **Homepage URL** | `https://your-domain.com` | 平台首页 |
| **Callback URL** | `https://your-domain.com/api/auth/github/callback` | OAuth 回调地址 |
| **Setup URL** | `https://your-domain.com/setup` | App 安装后重定向 |
| **Webhook URL** | `https://your-domain.com/api/webhook/github` | Webhook 接收地址 |
| **Webhook Secret** | `<随机生成的密钥>` | 用于验证 Webhook 签名 |

**所需权限 (Permissions)**：

| 权限 | 级别 | 用途 |
|------|------|------|
| **Contents** | Read & Write | 读取仓库文件内容、创建翻译文件 |
| **Pull Requests** | Read & Write | 创建和管理 PR |
| **Metadata** | Read-only | 读取仓库元信息（名称、分支等） |
| **Webhooks** | Read-only | 管理 Webhook（GitHub App 自带） |

**订阅事件 (Events)**：

| 事件 | 用途 |
|------|------|
| `push` | 检测基准语言文件变更，触发增量翻译 |
| `pull_request` | 追踪翻译 PR 的状态（merged/closed） |
| `installation` | 检测 App 安装/卸载事件 |
| `installation_repositories` | 检测用户授权仓库变更 |

### 3.2 用户认证流程（GitHub App OAuth-like Flow）

GitHub App 内置的 Web Application Flow 与标准 OAuth 2.0 Authorization Code Flow 基本一致：

```
┌──────────┐                    ┌──────────────┐                    ┌────────────┐
│  浏览器   │                    │  后端 Server   │                    │  GitHub    │
└────┬─────┘                    └──────┬───────┘                    └─────┬──────┘
     │                                 │                                  │
     │  1. 点击"GitHub登录"             │                                  │
     │────────────────────────────────>│                                  │
     │                                 │                                  │
     │  2. 302 重定向到 GitHub          │                                  │
     │<────────────────────────────────│                                  │
     │  Location: https://github.com/login/oauth/authorize               │
     │  ?client_id={APP_CLIENT_ID}                                       │
     │  &redirect_uri={CALLBACK_URL}                                     │
     │  &state={RANDOM_STATE}                                            │
     │                                 │                                  │
     │  3. 用户在 GitHub 上授权          │                                  │
     │────────────────────────────────────────────────────────────────────>
     │                                 │                                  │
     │  4. GitHub 重定向回 callback       │                                  │
     │  ?code={AUTH_CODE}&state={STATE} │                                  │
     │────────────────────────────────>│                                  │
     │                                 │                                  │
     │                                 │  5. 用 code 换取 access_token     │
     │                                 │  POST /login/oauth/access_token  │
     │                                 │─────────────────────────────────>│
     │                                 │                                  │
     │                                 │  6. 返回 user access token        │
     │                                 │<─────────────────────────────────│
     │                                 │                                  │
     │                                 │  7. 用 token 获取用户信息          │
     │                                 │  GET /user                       │
     │                                 │─────────────────────────────────>│
     │                                 │                                  │
     │                                 │  8. 返回用户信息                   │
     │                                 │<─────────────────────────────────│
     │                                 │                                  │
     │  9. 返回 JWT + 用户信息           │                                  │
     │  Set-Cookie: token=xxx          │                                  │
     │<────────────────────────────────│                                  │
     │                                 │                                  │
```

**关键实现要点**：

1. **State 参数**：生成随机 UUID 存入 Redis（TTL 5 分钟），回调时校验防 CSRF
2. **User Access Token**：用于获取用户信息（用户名、头像、邮箱），存储到数据库
3. **JWT Token**：后端生成自己的 JWT（有效期 7 天），作为平台的会话凭证，前端通过 `Authorization: Bearer {JWT}` 携带
4. **安装检测**：认证成功后，调用 `GET /user/installations` 检查是否已安装 App，未安装则引导安装

### 3.3 GitHub App 认证体系（Server-to-Server）

GitHub App 操作仓库（读取内容、创建 PR 等）需要 **Installation Access Token**，获取流程：

```
1. 使用 App 私钥 + App Client ID 生成 JWT (RS256, 有效期 10 分钟)
2. 通过 JWT 调用: POST /app/installations/{installation_id}/access_tokens
3. 获得 Installation Access Token (有效期 1 小时)
4. 使用该 Token 操作仓库
```

**Token 管理策略**：

```java
// 伪代码: Installation Token 缓存策略
@Service
public class GitHubTokenManager {

    // 缓存 key: github:installation_token:{installation_id}
    // 缓存 TTL: 50 分钟 (Token 实际有效 60 分钟, 预留 10 分钟缓冲)

    public String getInstallationToken(long installationId) {
        String cached = redis.get(cacheKey(installationId));
        if (cached != null) {
            return cached;
        }
        String token = generateNewInstallationToken(installationId);
        redis.setex(cacheKey(installationId), 50 * 60, token);
        return token;
    }

    private String generateNewInstallationToken(long installationId) {
        // 1. 生成 JWT
        String jwt = generateAppJwt();
        // 2. 调用 GitHub API 获取 Installation Token
        // POST /app/installations/{installationId}/access_tokens
        // Authorization: Bearer {jwt}
        return callGitHubApi(jwt, installationId);
    }
}
```

**使用 hub4j/github-api 库的实现方式**：

```java
// hub4j/github-api 原生支持 GitHub App 认证
GitHub github = new GitHubBuilder()
    .withAppInstallationToken(installationToken)
    .build();

GHRepository repo = github.getRepository("owner/repo");
```

### 3.4 Webhook 处理方案

#### 3.4.1 Webhook 签名验证

```java
// Spring Boot Controller 接收 Webhook
@PostMapping("/api/webhook/github")
public ResponseEntity<Void> handleWebhook(
        @RequestHeader("X-Hub-Signature-256") String signature,
        @RequestHeader("X-GitHub-Event") String event,
        @RequestHeader("X-GitHub-Delivery") String deliveryId,
        @RequestBody String payload) {

    // 1. 验证 HMAC-SHA256 签名
    if (!verifySignature(payload, signature, webhookSecret)) {
        return ResponseEntity.status(401).build();
    }

    // 2. 幂等性检查: deliveryId 去重 (Redis SETNX, TTL 24h)
    if (!idempotencyCheck(deliveryId)) {
        return ResponseEntity.ok().build(); // 重复投递，直接返回
    }

    // 3. 快速响应 (5秒内)
    // 将事件放入内存队列，异步处理
    webhookEventQueue.enqueue(event, payload);

    return ResponseEntity.ok().build();
}
```

#### 3.4.2 Push 事件处理（2025.10 变更适配）

> **重要**：自 2025 年 10 月 7 日起，GitHub Push Webhook payload 不再包含详细的 commit 文件变更信息（added/modified/removed）。需通过 Compare API 获取。

```java
// Push 事件处理流程
public void handlePushEvent(PushEventPayload payload) {
    String repoFullName = payload.getRepository().getFullName();
    String before = payload.getBefore();  // push 前的 commit SHA
    String after = payload.getAfter();    // push 后的 commit SHA

    // 1. 查找该仓库在平台中的配置
    Repository repo = repoService.findByFullName(repoFullName);
    if (repo == null || repo.getStatus() != ACTIVE) return;

    // 2. 只处理默认分支的 push
    String ref = payload.getRef(); // refs/heads/main
    if (!ref.equals("refs/heads/" + repo.getDefaultBranch())) return;

    // 3. 通过 Compare API 获取变更文件列表
    //    GET /repos/{owner}/{repo}/compare/{before}...{after}
    List<ChangedFile> changedFiles = githubApiService.compareCommits(
        repoFullName, before, after, installationToken);

    // 4. 过滤: 只保留 Markdown 文件 (.md)
    // 5. 过滤: 排除 translations/ 目录下的文件
    // 6. 过滤: 只保留翻译配置范围内的文件
    List<ChangedFile> targetFiles = filterRelevantFiles(changedFiles, repo);

    if (targetFiles.isEmpty()) {
        logService.log(repo.getId(), "push事件未检测到需翻译的文件变更");
        return;
    }

    // 7. 创建增量翻译任务
    translationTaskService.createIncrementalTask(repo, targetFiles);
}
```

#### 3.4.3 本地开发 Webhook 方案

使用 **Smee.io** 作为本地开发的 Webhook 代理：

```bash
# 1. 访问 https://smee.io/ 获取一个代理 URL
# 2. GitHub App 的 Webhook URL 设置为 Smee 代理 URL
# 3. 本地运行 Smee 客户端转发到本地

npx smee-client --url https://smee.io/your-channel --target http://localhost:8080/api/webhook/github
```

---

## 四、翻译引擎方案

### 4.1 Spring AI + OpenRouter 集成

#### 4.1.1 依赖配置

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
```

#### 4.1.2 应用配置

```yaml
# application.yml - 平台默认配置
spring:
  ai:
    openai:
      api-key: ${OPENROUTER_API_KEY}       # 平台的 OpenRouter API Key
      base-url: https://openrouter.ai/api   # OpenRouter 端点
      chat:
        options:
          model: deepseek/deepseek-chat     # 默认模型
          temperature: 0.3                   # 翻译场景偏低温度，更准确
          max-tokens: 8192
```

#### 4.1.3 动态模型切换（支持用户自选模型 + 自带 Key）

```java
@Service
public class TranslationAIService {

    private final OpenAiChatModel baseChatModel;
    private final OpenAiApi baseOpenAiApi;

    /**
     * 根据用户配置构建对应的 ChatModel
     * 支持: 平台默认Key + 用户自带Key + 不同模型
     */
    public ChatModel buildChatModel(Repository repo, User user) {
        String apiKey = resolveApiKey(user);   // 用户自带 Key 或平台 Key
        String model = repo.getAiModel();       // 用户选择的模型

        // 使用 Spring AI 的 mutate() 模式动态创建模型实例
        OpenAiApi customApi = baseOpenAiApi.mutate()
            .baseUrl("https://openrouter.ai/api")
            .apiKey(apiKey)
            .build();

        return baseChatModel.mutate()
            .openAiApi(customApi)
            .defaultOptions(OpenAiChatOptions.builder()
                .model(model)
                .temperature(0.3)
                .build())
            .build();
    }

    private String resolveApiKey(User user) {
        if (user.getOpenrouterApiKey() != null) {
            return decryptApiKey(user.getOpenrouterApiKey());
        }
        return platformApiKey;
    }
}
```

#### 4.1.4 获取可用模型列表

```java
// 从 OpenRouter 获取模型列表
// GET https://openrouter.ai/api/v1/models
@Service
public class OpenRouterModelService {

    private final RestClient restClient;

    /**
     * 获取 OpenRouter 支持的模型列表
     * 按价格排序，返回适合翻译的文本模型
     */
    public List<AIModelInfo> getAvailableModels() {
        // 调用 OpenRouter Models API
        // 缓存 1 小时 (模型列表变更频率低)
        OpenRouterModelsResponse response = restClient.get()
            .uri("https://openrouter.ai/api/v1/models")
            .retrieve()
            .body(OpenRouterModelsResponse.class);

        return response.getData().stream()
            .filter(m -> m.getArchitecture().getModality().equals("text->text"))
            .map(this::toModelInfo)
            .sorted(Comparator.comparing(AIModelInfo::getInputPrice))
            .toList();
    }
}
```

### 4.2 Markdown 感知翻译

#### 4.2.1 翻译 Prompt 模板

```
你是一位专业的技术文档翻译专家。请将以下 Markdown 文档从 {source_language} 翻译为 {target_language}。

翻译规则（必须严格遵守）：
1. **保留所有 Markdown 格式**：标题层级(#)、粗体(**)、斜体(*)、列表(- 或 1.)、表格(|)、引用(>)等
2. **不翻译以下内容**：
   - 代码块（``` 或 ` 包裹的内容）
   - YAML Front Matter（--- 包裹的元数据）
   - 链接 URL（仅翻译链接显示文本）
   - 图片路径和 alt 属性中的文件名
   - HTML 标签和属性
   - 变量名、命令行指令、API 端点
3. **技术术语处理**：常见技术术语保留英文原文或使用目标语言的通用翻译（如 API、SDK、Docker、Kubernetes 等保留英文）
4. **保持一致性**：同一术语在整篇文档中使用相同的翻译
5. **自然流畅**：翻译结果应符合 {target_language} 的表达习惯，而非逐字翻译
6. **保持结构完整**：输出的 Markdown 文档结构必须与原文完全对应

项目上下文：
- 项目名称：{project_name}
- 文件路径：{file_path}

请直接输出翻译后的完整 Markdown 文档，不要添加任何额外说明。
```

#### 4.2.2 Markdown 预处理策略

在调用 AI 翻译前，对 Markdown 内容进行预处理，以提高翻译质量和减少 Token 消耗：

```
[预处理流程]
1. 提取 YAML Front Matter → 保存，不发送给 AI
2. 提取代码块 → 替换为占位符 {{CODE_BLOCK_1}} 等
3. 提取行内代码 → 替换为占位符 {{INLINE_CODE_1}} 等
4. 将处理后的内容发送给 AI 翻译
5. 翻译完成后，将占位符还原为原始内容
6. 恢复 YAML Front Matter
```

这种策略的优势：
- 避免 AI 意外修改代码块内容
- 减少 Token 消耗（代码块通常较长）
- 提高翻译的准确性和稳定性

#### 4.2.3 大文件分段翻译

当 Markdown 文件过大（超过模型上下文窗口的 60%）时，按照 Markdown 结构进行分段：

```
分段策略:
1. 按一级标题 (#) 或二级标题 (##) 切分
2. 每段保持在 4000 tokens 以内
3. 每段翻译时携带前一段的最后 2 行作为上下文
4. 翻译完成后按原顺序拼接
```

### 4.3 并发翻译策略

```java
@Service
public class TranslationExecutor {

    // 使用 Java 21 虚拟线程的 Executor
    private final ExecutorService virtualThreadExecutor =
        Executors.newVirtualThreadPerTaskExecutor();

    // 并发限制: 同时最多 5 个文件翻译
    private final Semaphore concurrencyLimit = new Semaphore(5);

    public void executeTranslationTask(TranslationTask task) {
        List<TranslationFile> files = task.getFiles();
        List<String> targetLanguages = task.getTargetLanguages();

        // 构建所有翻译单元: 文件 × 语言
        List<TranslationUnit> units = new ArrayList<>();
        for (TranslationFile file : files) {
            for (String lang : targetLanguages) {
                units.add(new TranslationUnit(file, lang));
            }
        }

        // 使用虚拟线程并发执行，受信号量限制
        List<CompletableFuture<Void>> futures = units.stream()
            .map(unit -> CompletableFuture.runAsync(() -> {
                try {
                    concurrencyLimit.acquire();
                    translateSingleUnit(unit);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    concurrencyLimit.release();
                }
            }, virtualThreadExecutor))
            .toList();

        // 等待所有翻译完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
```

### 4.4 翻译失败重试

```java
// 重试策略: 最多 3 次，指数退避
@Retryable(
    retryFor = {OpenRouterApiException.class, TimeoutException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000, multiplier = 2)  // 2s, 4s, 8s
)
public String translateContent(String content, String sourceLang,
                                String targetLang, ChatModel model) {
    Prompt prompt = buildTranslationPrompt(content, sourceLang, targetLang);
    ChatResponse response = model.call(prompt);
    return response.getResult().getOutput().getContent();
}
```

---

## 五、核心业务流程实现

### 5.1 首次翻译流程

```
用户点击"保存并开始翻译"
    │
    ▼
[1] 保存翻译配置到数据库
    (base_language, target_languages, selected_files, ai_model)
    │
    ▼
[2] 创建 TranslationTask (status: QUEUED)
    │
    ▼
[3] 任务入队 (内存队列 LinkedBlockingQueue)
    │
    ▼
[4] 任务执行器取出任务 (status: RUNNING)
    │
    ▼
[5] 获取 Installation Access Token (缓存/新建)
    │
    ▼
[6] 遍历选中的文件:
    │  ├─ 通过 GitHub API 读取文件内容
    │  │   GET /repos/{owner}/{repo}/contents/{path}
    │  │   (返回 Base64 编码内容, 解码后获取原文)
    │  │
    │  ├─ 计算文件内容 SHA-256 Hash (用于增量检测)
    │  │
    │  ├─ 预处理 Markdown (提取代码块等)
    │  │
    │  ├─ 对每种目标语言调用 AI 翻译
    │  │   (并发执行, 信号量限制 5 并发)
    │  │
    │  ├─ 后处理: 还原代码块, 恢复 Front Matter
    │  │
    │  └─ 记录翻译结果 (tokens_used, status)
    │
    ▼
[7] 所有文件翻译完成
    │
    ▼
[8] 创建翻译分支
    │  ├─ 获取默认分支最新 commit SHA
    │  │   GET /repos/{owner}/{repo}/git/ref/heads/{default_branch}
    │  │
    │  └─ 创建新分支 doc-translation/{timestamp}
    │      POST /repos/{owner}/{repo}/git/refs
    │      {"ref": "refs/heads/doc-translation/{timestamp}", "sha": "{commit_sha}"}
    │
    ▼
[9] 将翻译文件写入分支
    │  ├─ 构建文件 Tree (包含所有翻译文件)
    │  │   POST /repos/{owner}/{repo}/git/trees
    │  │   {
    │  │     "base_tree": "{base_tree_sha}",
    │  │     "tree": [
    │  │       {"path": "translations/en/README.md", "mode": "100644", "content": "..."},
    │  │       {"path": "translations/ja/README.md", "mode": "100644", "content": "..."},
    │  │       ...
    │  │     ]
    │  │   }
    │  │
    │  ├─ 创建 Commit
    │  │   POST /repos/{owner}/{repo}/git/commits
    │  │   {"message": "docs: add translations (en, ja, ko)", "tree": "{tree_sha}", "parents": ["{parent_sha}"]}
    │  │
    │  └─ 更新分支引用
    │      PATCH /repos/{owner}/{repo}/git/refs/heads/doc-translation/{timestamp}
    │      {"sha": "{commit_sha}"}
    │
    ▼
[10] 创建 Pull Request
    │   POST /repos/{owner}/{repo}/pulls
    │   {
    │     "title": "[doc-translation] Update translations (en, ja, ko)",
    │     "head": "doc-translation/{timestamp}",
    │     "base": "{default_branch}",
    │     "body": "## 📝 自动翻译更新\n\n### 变更摘要\n- 翻译文件: 12 个\n- 目标语言: English, 日本語, 한국어\n- AI 模型: deepseek/deepseek-chat\n- Token 消耗: 45,230\n\n### 变更文件列表\n| 文件 | 状态 |\n|...|...|\n\n---\n*由 GitHub Doc Translation 自动生成*"
    │   }
    │
    ▼
[11] 更新任务状态 (status: COMPLETED, pr_url, pr_number)
    │
    ▼
[12] 通知用户 (前端通过轮询获取状态更新)
```

### 5.2 增量翻译流程

```
GitHub Webhook 推送 push 事件
    │
    ▼
[1] 验证 Webhook 签名 (HMAC-SHA256)
    │
    ▼
[2] 幂等性检查 (delivery_id 去重, Redis SETNX TTL 24h)
    │
    ▼
[3] 快速响应 200 OK (5秒内)
    │
    ▼
[4] 异步处理:
    │
    ├─ [4.1] 解析 push payload
    │   提取: repository.full_name, ref, before, after
    │
    ├─ [4.2] 查找平台中的仓库配置
    │   若仓库不存在或已暂停 → 跳过
    │
    ├─ [4.3] 只处理默认分支的 push
    │   ref != "refs/heads/{default_branch}" → 跳过
    │
    ├─ [4.4] 调用 Compare API 获取变更文件
    │   GET /repos/{owner}/{repo}/compare/{before}...{after}
    │   ⚠️ 注意: 最多返回 300 个文件，超过需分页处理
    │
    ├─ [4.5] 过滤文件:
    │   ├─ 只保留 .md 文件
    │   ├─ 排除 translations/ 目录下的文件 (避免翻译文件触发递归翻译)
    │   ├─ 只保留翻译配置范围内的文件 (matched against selected paths)
    │   └─ 分类: added, modified, removed
    │
    ├─ [4.6] 处理删除文件:
    │   对 removed 文件，同步删除 translations/{lang}/{path}
    │
    ├─ [4.7] 处理新增/修改文件:
    │   创建增量翻译任务 (trigger_type: WEBHOOK)
    │
    └─ [4.8] 检查现有未合并 PR:
        ├─ 有未合并的翻译 PR → 追加 commit 到同一分支
        └─ 无未合并 PR → 创建新分支和新 PR
```

### 5.3 追加到现有 PR 的流程

```java
public void appendToExistingPR(Repository repo, List<TranslatedFile> translatedFiles) {
    // 1. 找到现有未合并的翻译 PR
    GHPullRequest existingPR = findOpenTranslationPR(repo);

    // 2. 获取 PR source 分支的最新 commit
    String branchName = existingPR.getHead().getRef();
    String latestCommitSha = getLatestCommitSha(repo, branchName);

    // 3. 基于最新 commit 创建新 Tree
    GHTree newTree = createTreeWithFiles(repo, latestCommitSha, translatedFiles);

    // 4. 创建新 Commit (parent 为当前分支最新 commit)
    GHCommit newCommit = createCommit(repo, newTree, latestCommitSha,
        "docs: update translations (incremental)");

    // 5. 更新分支引用指向新 commit
    updateBranchRef(repo, branchName, newCommit.getSHA1());

    // 6. 更新 PR body 追加变更信息 (可选)
    updatePRBody(existingPR, translatedFiles);
}
```

---

## 六、数据库设计

### 6.1 ER 关系图

```
User (1) ──────── (N) Repository
                       │
Repository (1) ────── (N) TranslationTask
                              │
TranslationTask (1) ── (N) TranslationFile

User (1) ──────── (N) OperationLog
Repository (1) ── (N) OperationLog
```

### 6.2 表结构设计

#### users 表

```sql
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    github_id           BIGINT NOT NULL UNIQUE,
    username            VARCHAR(100) NOT NULL,
    avatar_url          VARCHAR(500),
    email               VARCHAR(200),
    github_access_token TEXT,                           -- 加密存储的 GitHub User Access Token
    openrouter_api_key  TEXT,                           -- AES 加密存储的 OpenRouter API Key
    free_quota          BIGINT NOT NULL DEFAULT 100000, -- 免费额度 (tokens)
    used_quota          BIGINT NOT NULL DEFAULT 0,      -- 已使用额度 (tokens)
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_github_id ON users(github_id);
```

#### repositories 表

```sql
CREATE TABLE repositories (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    github_repo_id      BIGINT NOT NULL,
    full_name           VARCHAR(300) NOT NULL,           -- owner/repo
    default_branch      VARCHAR(100) NOT NULL DEFAULT 'main',
    installation_id     BIGINT NOT NULL,                 -- GitHub App Installation ID
    base_language       VARCHAR(10) NOT NULL DEFAULT 'zh',
    target_languages    JSONB NOT NULL DEFAULT '[]',     -- ["en", "ja", "ko"]
    selected_paths      JSONB NOT NULL DEFAULT '[]',     -- 选中的翻译路径
    ignore_patterns     JSONB NOT NULL DEFAULT '[]',     -- .gitdoc-ignore 规则
    ai_model            VARCHAR(200) NOT NULL DEFAULT 'deepseek/deepseek-chat',
    output_path_pattern VARCHAR(200) NOT NULL DEFAULT 'translations/{lang}/',
    status              VARCHAR(20) NOT NULL DEFAULT 'active',  -- active/paused/error
    webhook_active      BOOLEAN NOT NULL DEFAULT TRUE,
    last_sync_at        TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),

    UNIQUE(user_id, github_repo_id)
);

CREATE INDEX idx_repositories_user_id ON repositories(user_id);
CREATE INDEX idx_repositories_full_name ON repositories(full_name);
CREATE INDEX idx_repositories_installation_id ON repositories(installation_id);
```

#### translation_tasks 表

```sql
CREATE TABLE translation_tasks (
    id                  BIGSERIAL PRIMARY KEY,
    repository_id       BIGINT NOT NULL REFERENCES repositories(id),
    trigger_type        VARCHAR(20) NOT NULL,            -- webhook / manual
    trigger_commit_sha  VARCHAR(64),                     -- 触发的 commit SHA (webhook)
    status              VARCHAR(20) NOT NULL DEFAULT 'queued',  -- queued/running/completed/failed/cancelled
    target_languages    JSONB NOT NULL DEFAULT '[]',
    total_files         INT NOT NULL DEFAULT 0,
    completed_files     INT NOT NULL DEFAULT 0,
    failed_files        INT NOT NULL DEFAULT 0,
    tokens_used         BIGINT NOT NULL DEFAULT 0,
    estimated_cost      DECIMAL(10, 6) DEFAULT 0,
    pr_number           INT,
    pr_url              VARCHAR(500),
    pr_branch           VARCHAR(200),                    -- 翻译分支名
    error_message       TEXT,
    started_at          TIMESTAMP,
    completed_at        TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tasks_repository_id ON translation_tasks(repository_id);
CREATE INDEX idx_tasks_status ON translation_tasks(status);
CREATE INDEX idx_tasks_created_at ON translation_tasks(created_at);
```

#### translation_files 表

```sql
CREATE TABLE translation_files (
    id                  BIGSERIAL PRIMARY KEY,
    task_id             BIGINT NOT NULL REFERENCES translation_tasks(id),
    repository_id       BIGINT NOT NULL REFERENCES repositories(id),
    source_path         VARCHAR(500) NOT NULL,           -- 源文件路径
    target_language     VARCHAR(10) NOT NULL,
    target_path         VARCHAR(500) NOT NULL,           -- 翻译文件路径
    status              VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending/translating/completed/failed
    source_hash         VARCHAR(64),                     -- SHA-256 文件内容 Hash (增量检测)
    tokens_used         BIGINT NOT NULL DEFAULT 0,
    error_message       TEXT,
    translated_at       TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_files_task_id ON translation_files(task_id);
CREATE INDEX idx_files_repository_id ON translation_files(repository_id);
CREATE INDEX idx_files_source_hash ON translation_files(source_hash);
```

#### operation_logs 表

```sql
CREATE TABLE operation_logs (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT REFERENCES users(id),
    repository_id       BIGINT REFERENCES repositories(id),
    action              VARCHAR(100) NOT NULL,           -- e.g. "translation.started", "pr.created", "webhook.received"
    detail              JSONB,                           -- 操作详情
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_logs_user_id ON operation_logs(user_id);
CREATE INDEX idx_logs_repository_id ON operation_logs(repository_id);
CREATE INDEX idx_logs_created_at ON operation_logs(created_at);
```

### 6.3 文件级增量检测方案

```
增量检测策略：基于文件内容 SHA-256 Hash

[翻译时]
1. 读取源文件内容
2. 计算 SHA-256 Hash
3. 存储到 translation_files.source_hash

[增量翻译时]
1. 获取变更文件的当前内容
2. 计算 SHA-256 Hash
3. 查询 translation_files 中该文件最近一次成功翻译的 source_hash
4. 若 hash 相同 → 跳过翻译（文件内容未变）
5. 若 hash 不同 → 执行翻译

这可防止 Webhook 触发了但文件实际内容未变的情况（如 merge commit 等）。
```

---

## 七、API 接口详细设计

### 7.1 认证相关

```
GET  /api/auth/github
  → 重定向到 GitHub OAuth 授权页
  → 生成 state 参数存入 Redis (TTL: 5min)

GET  /api/auth/github/callback?code={code}&state={state}
  → 验证 state → 用 code 换取 user access token
  → 获取用户信息 → 创建/更新用户记录
  → 生成平台 JWT → Set-Cookie 或 返回 JSON
  → 检查 App 安装状态 → 返回 redirect URL

GET  /api/auth/me
  → Authorization: Bearer {JWT}
  → 返回: { id, username, avatarUrl, email, freeQuota, usedQuota, hasInstallation }

POST /api/auth/logout
  → 清除 JWT Cookie / 使 JWT 失效
```

### 7.2 仓库管理

```
GET  /api/repos
  → 获取用户在平台中已添加的仓库列表
  → 支持: ?page=1&size=10&search=keyword&status=active

GET  /api/repos/available
  → 获取用户通过 GitHub App 授权的所有仓库（从 GitHub API 实时拉取）
  → 用于"添加仓库"对话框
  → 标记已添加到平台的仓库

POST /api/repos
  → Body: { githubRepoId, fullName, defaultBranch }
  → 将仓库添加到平台

GET  /api/repos/{id}
  → 获取单个仓库详情（含翻译状态统计）

DELETE /api/repos/{id}
  → 从平台移除仓库（不删除 GitHub 上的翻译文件）

GET  /api/repos/{id}/tree
  → 获取仓库的 Markdown 文件树形结构
  → 通过 GitHub API: GET /repos/{owner}/{repo}/git/trees/{branch}?recursive=1
  → 过滤只返回 .md 文件和相关目录
```

### 7.3 翻译配置

```
GET  /api/repos/{id}/config
  → 返回: { baseLanguage, targetLanguages, selectedPaths, ignorePatterns, aiModel, outputPathPattern }

PUT  /api/repos/{id}/config
  → Body: { baseLanguage, targetLanguages, selectedPaths, ignorePatterns, aiModel }
  → 更新翻译配置

GET  /api/repos/{id}/ignore
  → 返回 .gitdoc-ignore 文件内容（从 GitHub 读取）

PUT  /api/repos/{id}/ignore
  → 更新 .gitdoc-ignore 内容（可选：同步到 GitHub 仓库）

GET  /api/models
  → 获取 OpenRouter 可用模型列表（缓存 1 小时）
  → 返回: [{ id, name, contextLength, inputPrice, outputPrice }]
```

### 7.4 翻译任务

```
POST /api/repos/{id}/translate
  → Body: { type: "full" | "incremental" }
  → 手动触发翻译任务
  → 返回: { taskId }

GET  /api/repos/{id}/tasks
  → 获取仓库的翻译任务列表
  → 支持: ?page=1&size=10&status=running

GET  /api/tasks/{id}
  → 获取任务详情（含各文件翻译进度）
  → 翻译进行中时前端轮询此接口 (每 3 秒)

POST /api/tasks/{id}/cancel
  → 取消进行中的翻译任务
```

### 7.5 Webhook

```
POST /api/webhook/github
  → Headers: X-Hub-Signature-256, X-GitHub-Event, X-GitHub-Delivery
  → 接收并处理 GitHub App Webhook 事件
  → 支持的事件: push, pull_request, installation, installation_repositories
```

### 7.6 额度管理

```
GET  /api/quota
  → 返回: { freeQuota, usedQuota, remaining, hasCustomApiKey }

GET  /api/quota/usage
  → 返回额度使用记录列表
  → 支持: ?page=1&size=20&startDate=2026-03-01&endDate=2026-03-18

PUT  /api/settings/api-key
  → Body: { apiKey: "sk-or-v1-xxx" }
  → AES 加密后存储

DELETE /api/settings/api-key
  → 清除用户自带的 API Key

POST /api/settings/api-key/verify
  → Body: { apiKey: "sk-or-v1-xxx" }
  → 验证 API Key 是否有效（调用 OpenRouter /models 测试）
```

### 7.7 仪表盘

```
GET  /api/dashboard/stats
  → 返回: { totalRepos, totalTranslatedFiles, totalLanguages, monthlyTasks }

GET  /api/dashboard/recent-tasks
  → 返回最近 10 条翻译任务
```

---

## 八、前端实现方案

### 8.1 技术方案概述

| 方面 | 方案 |
|------|------|
| **路由守卫** | Vue Router 的 `beforeEach` 钩子，检查认证状态，未登录重定向到登录页/落地页 |
| **状态管理** | Pinia stores: `useAuthStore`(认证), `useRepoStore`(仓库), `useTaskStore`(任务) |
| **API 封装** | Axios 实例 + 拦截器（自动附加 JWT、处理 401 跳转登录、统一错误提示） |
| **实时更新** | 任务进度通过轮询 `/api/tasks/{id}` 实现（每 3 秒），使用 `setInterval` + 组件卸载时清理 |
| **主题配置** | Element Plus 的 CSS 变量覆盖，主色 `--el-color-primary: #1a73e8` |

### 8.2 核心组件设计

#### 文件树选择器 (FileTreeSelector.vue)

```
技术方案:
- 基于 el-tree 组件，开启 show-checkbox
- 数据源: 调用 /api/repos/{id}/tree 获取
- 懒加载: 目录节点展开时才加载子节点内容 (el-tree 的 lazy 模式)
- 节点过滤: 仅显示 .md 文件和包含 .md 文件的目录
- 图标区分: 📁 目录 / 📄 文件
- 双向同步: 
  ├─ 树形取消勾选 → 触发 emit 事件 → 父组件更新 .gitdoc-ignore 编辑器
  └─ .gitdoc-ignore 编辑 → 父组件解析规则 → 更新树形勾选状态
```

#### .gitdoc-ignore 编辑器 (IgnoreEditor.vue)

```
技术方案:
- 使用 @monaco-editor/vue (Monaco Editor Vue 封装)
- 语言模式: plaintext (简单文本高亮即可)
- 双向绑定: v-model 绑定编辑器内容
- 实时解析: 内容变更时，使用 ignore 库 (npm: ignore) 解析 gitignore 语法
- 同步逻辑: 解析后的规则通过 emit 传递给父组件
```

#### 翻译进度面板 (TranslationProgress.vue)

```
技术方案:
- 使用 el-collapse 分语言折叠展示
- 每种语言下使用 el-table 展示文件列表
- 文件状态使用 el-tag:
  ├─ ⏳ 排队中 → type="info"
  ├─ 🔄 翻译中 → type="warning" 
  ├─ ✅ 完成   → type="success"
  └─ ❌ 失败   → type="danger"
- 整体进度使用 el-progress (percentage 模式)
- 轮询更新: 每 3 秒调用 /api/tasks/{id}，直到任务完成/失败
```

### 8.3 路由配置

```typescript
const routes = [
  { path: '/', component: LandingPage, meta: { requiresAuth: false } },
  { path: '/login', component: LoginPage, meta: { requiresAuth: false } },
  { path: '/setup', component: SetupPage, meta: { requiresAuth: true } },
  { path: '/dashboard', component: Dashboard, meta: { requiresAuth: true } },
  { path: '/repos', component: RepoList, meta: { requiresAuth: true } },
  { path: '/repos/:id', component: RepoDetail, meta: { requiresAuth: true } },
  { path: '/repos/:id/config', component: RepoConfig, meta: { requiresAuth: true } },
  { path: '/repos/:id/tasks', component: TaskList, meta: { requiresAuth: true } },
  { path: '/repos/:id/tasks/:taskId', component: TaskDetail, meta: { requiresAuth: true } },
  { path: '/settings', component: Settings, meta: { requiresAuth: true },
    children: [
      { path: '', redirect: 'profile' },
      { path: 'profile', component: Profile },
      { path: 'api-key', component: ApiKeySettings },
      { path: 'quota', component: QuotaSettings },
    ]
  },
]
```

### 8.4 前端与后端的通信方式

```
[开发环境]
前端 Vite Dev Server → vite.config.ts 配置 proxy 代理到后端 8080 端口

[生产环境]
Nginx 反向代理:
  / → 前端 SPA 静态文件
  /api/* → 后端 Spring Boot (8080)
```

---

## 九、异步任务处理方案

### 9.1 MVP 阶段：内存队列

```java
@Component
public class InMemoryTaskQueue {

    private final LinkedBlockingQueue<TranslationTask> queue
        = new LinkedBlockingQueue<>(100);

    // 生产者: 提交任务
    public void enqueue(TranslationTask task) {
        if (!queue.offer(task)) {
            throw new QueueFullException("任务队列已满，请稍后再试");
        }
    }

    // 消费者: 单线程轮询消费
    @PostConstruct
    public void startConsumer() {
        Thread.ofVirtual().name("task-consumer").start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TranslationTask task = queue.take(); // 阻塞等待
                    translationExecutor.executeTranslationTask(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("任务执行失败", e);
                }
            }
        });
    }
}
```

**内存队列的局限性与后续扩展路径**：

| 局限 | 后续扩展方案 (Redis Stream) |
|------|--------------------------|
| 应用重启任务丢失 | Redis Stream 持久化，消费者组保证消息不丢失 |
| 单实例限制 | Redis Stream 支持多消费者组，天然支持水平扩展 |
| 无延迟队列 | Redis Stream 可配合 Sorted Set 实现延迟队列 |
| 无死信处理 | Redis Stream 的 XPENDING + XCLAIM 处理超时消息 |

### 9.2 Webhook 事件队列

Webhook 事件处理也使用内存队列，与翻译任务队列分离：

```java
@Component
public class WebhookEventQueue {

    private final LinkedBlockingQueue<WebhookEvent> queue
        = new LinkedBlockingQueue<>(500);

    public void enqueue(String eventType, String payload) {
        queue.offer(new WebhookEvent(eventType, payload, Instant.now()));
    }

    @PostConstruct
    public void startProcessor() {
        Thread.ofVirtual().name("webhook-processor").start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                WebhookEvent event = queue.take();
                webhookHandler.process(event);
            }
        });
    }
}
```

---

## 十、安全方案

### 10.1 认证与鉴权

| 安全措施 | 实现方案 |
|----------|---------|
| **用户认证** | GitHub OAuth (Authorization Code Flow) → 平台 JWT (HS256, 有效期 7 天) |
| **API 鉴权** | Spring Security Filter Chain: JWT Token 校验 |
| **CSRF 防护** | OAuth state 参数 + SameSite Cookie |
| **权限校验** | 接口层校验当前用户是否为仓库所有者 |

### 10.2 敏感数据加密

```java
@Component
public class EncryptionService {

    // AES-256-GCM 加密
    // 密钥从环境变量加载: ENCRYPTION_KEY

    public String encrypt(String plainText) {
        // AES-256-GCM 加密
        // 每次生成随机 IV (12 bytes)
        // 返回: Base64(IV + ciphertext + tag)
    }

    public String decrypt(String cipherText) {
        // 从密文中提取 IV → 解密
    }
}

// 加密存储的字段:
// - users.github_access_token  (GitHub User Access Token)
// - users.openrouter_api_key   (OpenRouter API Key)
```

### 10.3 Webhook 安全

```
1. HMAC-SHA256 签名验证 (X-Hub-Signature-256)
2. Delivery ID 幂等去重 (Redis SETNX, TTL 24h)
3. 只处理已注册的仓库事件 (忽略未知仓库)
4. 限流: 每个仓库每分钟最多处理 10 个 Webhook 事件
```

### 10.4 GitHub Token 安全

```
1. Installation Access Token:
   - 缓存在 Redis (TTL 50分钟, 实际有效期 60分钟)
   - 不持久化到数据库
   - 每次操作前检查是否过期

2. User Access Token:
   - AES-256-GCM 加密后存储到 PostgreSQL
   - 仅在需要时解密使用

3. App 私钥:
   - 存储在环境变量或密钥管理服务中
   - 不提交到代码仓库
```

---

## 十一、本地开发环境方案

### 11.1 Docker Compose (开发环境)

```yaml
# docker-compose.dev.yml
services:
  postgres:
    image: postgres:17
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: doc_translation
      POSTGRES_USER: dev
      POSTGRES_PASSWORD: dev123
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

### 11.2 环境变量配置

```bash
# .env.example (复制为 .env 使用)

# GitHub App 配置
GITHUB_APP_ID=your-app-id
GITHUB_APP_CLIENT_ID=your-client-id
GITHUB_APP_CLIENT_SECRET=your-client-secret
GITHUB_APP_PRIVATE_KEY_PATH=./github-app-private-key.pem
GITHUB_APP_WEBHOOK_SECRET=your-webhook-secret

# OpenRouter 配置
OPENROUTER_API_KEY=sk-or-v1-xxx

# 数据库
DATABASE_URL=jdbc:postgresql://localhost:5432/doc_translation
DATABASE_USERNAME=dev
DATABASE_PASSWORD=dev123

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# 安全
JWT_SECRET=your-jwt-secret-at-least-32-chars
ENCRYPTION_KEY=your-aes-256-encryption-key

# Smee (本地 Webhook 代理)
SMEE_URL=https://smee.io/your-channel
```

### 11.3 本地开发启动流程

```bash
# 1. 启动基础设施
docker compose -f docker-compose.dev.yml up -d

# 2. 启动后端 (Maven)
./mvnw spring-boot:run

# 3. 启动前端 (Vite)
cd doc_translation_website
pnpm install
pnpm dev

# 4. 启动 Webhook 代理 (可选, 需测试 Webhook 时)
npx smee-client --url $SMEE_URL --target http://localhost:8080/api/webhook/github
```

---

## 十二、生产部署方案（后续）

### 12.1 Docker Compose (生产环境)

```yaml
# docker-compose.yml
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/conf.d:/etc/nginx/conf.d
      - ./nginx/ssl:/etc/nginx/ssl
      - ./frontend/dist:/usr/share/nginx/html
    depends_on:
      - backend

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    env_file:
      - .env
    depends_on:
      - postgres
      - redis

  postgres:
    image: postgres:17
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      POSTGRES_DB: doc_translation
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

### 12.2 Nginx 配置要点

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;

    # 前端 SPA
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 十三、关键技术风险与应对

| 风险 | 影响 | 应对方案 |
|------|------|---------|
| **Push Webhook 不再包含文件变更详情** (2025.10起) | 无法直接从 payload 获取 added/modified/removed 文件 | 使用 Compare API: `GET /repos/{owner}/{repo}/compare/{before}...{after}`，从 response 的 `files` 字段获取 |
| **Compare API 文件数上限 300** | 大量文件变更时可能丢失 | MVP 阶段记录日志告警；后续可通过逐 commit 遍历绕过 |
| **Spring AI + OpenRouter 兼容性问题** | 部分模型 `finish_reason` 不标准导致反序列化失败 | 选用兼容性好的模型（如 DeepSeek、GPT 系列）；自定义错误处理忽略非关键字段 |
| **GitHub App Installation Token 1 小时过期** | 长时间翻译任务中途 Token 失效 | 翻译开始前检查 Token 剩余有效期，不足 15 分钟则刷新；每个 GitHub API 调用前检查 |
| **内存队列重启丢失** | 应用重启时进行中的任务丢失 | 任务状态持久化到 PostgreSQL；应用启动时扫描 RUNNING 状态任务标记为 FAILED 并允许手动重试 |
| **大文件翻译超出模型上下文** | AI 返回截断或错误 | 实现分段翻译策略；监控文件大小，超过阈值自动分段 |
| **并发翻译导致 OpenRouter 限流** | 429 Too Many Requests | 信号量控制并发数（默认 5）；429 响应时自动降速重试 |
| **GitHub API Rate Limit** | 5000-12500 请求/小时 | 批量操作优化（Tree API 一次提交多文件）；缓存仓库元数据；监控剩余配额 |

---

## 十四、开发里程碑与接口依赖

### 14.1 推荐开发顺序

```
M0: 项目初始化 (1周)
├── 后端: Spring Boot 4 项目骨架、Maven 依赖、数据库迁移 (Flyway)
├── 前端: Vite + Vue 3 + Element Plus + TypeScript 项目骨架
├── 基础设施: Docker Compose (PostgreSQL + Redis)
└── 公共模块: 异常处理、响应封装、日志配置

M1: 认证与仓库管理 (1.5周)
├── GitHub App 注册 (手动操作)
├── 后端: OAuth 登录流程、JWT 签发、Installation Token 管理
├── 后端: 仓库 CRUD、文件树加载
├── 前端: 落地页、登录页、App 安装引导页
└── 前端: 仓库列表、仓库详情

M2: 翻译配置 (1周)
├── 后端: 翻译配置 CRUD、.gitdoc-ignore 解析
├── 前端: 翻译配置页（文件树选择器 + Ignore 编辑器 + 双向同步）
├── 前端: 语言选择、模型选择
└── 后端: OpenRouter 模型列表接口

M3: 核心翻译引擎 (2周)
├── 后端: Spring AI + OpenRouter 集成
├── 后端: Markdown 预处理 (代码块提取/还原)
├── 后端: 翻译 Prompt 模板
├── 后端: 并发翻译执行器 + 内存任务队列
├── 后端: 翻译失败重试 + 大文件分段
└── 前端: 任务详情页 (进度展示 + 轮询)

M4: PR 管理 & 增量同步 (1.5周)
├── 后端: 创建翻译分支、提交文件、创建 PR
├── 后端: Webhook 接收 + 签名验证 + 幂等处理
├── 后端: Push 事件处理 (Compare API 获取变更文件)
├── 后端: 增量翻译 + 追加到现有 PR
└── 前端: PR 状态展示

M5: 额度 & 仪表盘 (1周)
├── 后端: 额度扣减、用量统计、自带 Key 管理
├── 前端: 仪表盘总览 (统计卡片 + 最近任务)
├── 前端: 设置页 (个人信息 + API Key + 额度管理)
└── 全局: 操作日志

M6: 测试 & 部署 (1周)
├── 集成测试: GitHub 流程端到端测试
├── Docker 部署配置
├── 文档: README、部署指南
└── Bug 修复与优化
```

### 14.2 外部 API 调用清单

| API | 用途 | 调用时机 | 频率 |
|-----|------|---------|------|
| GitHub: `POST /login/oauth/access_token` | OAuth 换取用户 Token | 用户登录 | 低 |
| GitHub: `GET /user` | 获取用户信息 | 用户登录 | 低 |
| GitHub: `GET /user/installations` | 检查 App 安装状态 | 登录后 | 低 |
| GitHub: `POST /app/installations/{id}/access_tokens` | 获取 Installation Token | 操作仓库前 | 中（缓存后低） |
| GitHub: `GET /installation/repositories` | 获取授权仓库列表 | 添加仓库页面 | 低 |
| GitHub: `GET /repos/{owner}/{repo}/git/trees/{sha}?recursive=1` | 获取仓库文件树 | 翻译配置页面 | 低 |
| GitHub: `GET /repos/{owner}/{repo}/contents/{path}` | 读取文件内容 | 翻译时 | 高 |
| GitHub: `GET /repos/{owner}/{repo}/compare/{base}...{head}` | 获取 commit 间文件差异 | Webhook push | 中 |
| GitHub: `POST /repos/{owner}/{repo}/git/trees` | 创建文件 Tree | 提交翻译结果 | 中 |
| GitHub: `POST /repos/{owner}/{repo}/git/commits` | 创建 Commit | 提交翻译结果 | 中 |
| GitHub: `POST /repos/{owner}/{repo}/git/refs` | 创建分支 | 首次翻译 | 低 |
| GitHub: `PATCH /repos/{owner}/{repo}/git/refs/{ref}` | 更新分支引用 | 提交翻译结果 | 中 |
| GitHub: `POST /repos/{owner}/{repo}/pulls` | 创建 PR | 翻译完成 | 低 |
| OpenRouter: `POST /api/v1/chat/completions` | AI 翻译 | 翻译每个文件 | 高 |
| OpenRouter: `GET /api/v1/models` | 获取模型列表 | 模型选择页面 | 低（缓存） |

---

## 十五、关键配置参考

### 15.1 Spring Boot application.yml 概览

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

  flyway:
    enabled: true
    locations: classpath:db/migration

  ai:
    openai:
      api-key: ${OPENROUTER_API_KEY}
      base-url: https://openrouter.ai/api
      chat:
        options:
          model: deepseek/deepseek-chat
          temperature: 0.3

  threads:
    virtual:
      enabled: true   # 启用虚拟线程

# GitHub App 配置
github:
  app:
    id: ${GITHUB_APP_ID}
    client-id: ${GITHUB_APP_CLIENT_ID}
    client-secret: ${GITHUB_APP_CLIENT_SECRET}
    private-key-path: ${GITHUB_APP_PRIVATE_KEY_PATH}
    webhook-secret: ${GITHUB_APP_WEBHOOK_SECRET}
    callback-url: ${GITHUB_CALLBACK_URL:http://localhost:5173/api/auth/github/callback}

# 应用配置
app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 7d
  encryption:
    key: ${ENCRYPTION_KEY}
  translation:
    max-concurrent: 5          # 最大并发翻译数
    max-retry: 3               # 最大重试次数
    task-queue-size: 100       # 任务队列大小
  quota:
    default-free: 100000       # 默认免费额度 (tokens)
```

---

> **文档结束**
>
> 本文档为 GitHub Doc Translation 项目 MVP 的技术实现方案，涵盖系统架构、核心模块设计、数据库设计、API 设计、安全方案和开发计划。请审阅后提出修改意见。

````

## 三、开发实现

### 1、后端开发-提示词编写

```markdown
## 角色
你是一位专业的后端开发程序员

## 任务
我会给你一个完整的需求文档和方案设计文档，你需要仔细完整分析需求和实现方案。
1）先帮我生成一套简单凝练的接口文档，提供给前端进行开发。不要过于复杂，但是不能缺少关键信息。
2）当我确认了接口文档之后，可以生成完整的后端代码，并且提供需要《人工进行的配置文档》和《快速启动说明文档》，帮助我能够顺利运行项目

## 约束
1. 你需要通过 Context7 来获取最新的技术文档信息（尤其是怎么对接 github、以及对接 OpenRouter 大模型）
2. 你必须只专注于 MVP 最小可行性产品功能的实现，不要做太多无关的功能。
3. 你的任务不包括编写前端代码，你必须只专注于后端开发，不要生成我要求之外的文档，以快速能够运行项目为主
4. 指定后端项目文件夹（所有后端代码都放在该文件夹下）：

## 我提供给你的信息

需求文档：
技术方案文档：

```

### （1）生成接口文档

![image-20260318211652049](/Users/scenery/Library/Application Support/typora-user-images/image-20260318211652049.png)

### （2）生成后端代码

![image-20260318211727740](/Users/scenery/Library/Application Support/typora-user-images/image-20260318211727740.png)

### （3）后端代码生成

![image-20260318211821374](/Users/scenery/Library/Application Support/typora-user-images/image-20260318211821374.png)

### （4）根据生成的 【人工配置文档】完成密钥生成

创建 github app 和 open router 的密钥

![image-20260318212033974](/Users/scenery/Library/Application Support/typora-user-images/image-20260318212033974.png)

### （5）启动验收，修复启动问题

1、使用的 spring ai 的版本太低了，让ai使用最新版本

![image-20260318212210356](/Users/scenery/Library/Application Support/typora-user-images/image-20260318212210356.png)

2、默认的 .env 文件没有加载，让ai添加配置 （.env 文件生产环境是放在哪里的需要确定）

![image-20260318212357287](/Users/scenery/Library/Application Support/typora-user-images/image-20260318212357287.png)

3、完成项目启动

![image-20260318212730347](/Users/scenery/Library/Application Support/typora-user-images/image-20260318212730347.png)

### 2、前端开发-提示词编写

```markdown
## 角色
你是一位专业的前端开发程序员

## 任务
我会给你一个完整的需求文档和方案设计文档、后端API接口文档，你需要仔细完整阅读这些文档。
1）帮我完整开发出可运行的前端页面，不要生成预料之外的功能
2）需要《快速启动说明文档》，帮助我能够顺利运行项目

## 约束
1. 你需要通过 Context7 来获取最新的技术文档信息（尤其是 Next.js 等前端开发框架）
2. 你必须只专注于 MVP 最小可行性产品功能的实现，不要做太多无关的功能。
3. 你的任务不包括编写后端代码，你必须只专注于前端开发，不要生成我要求之外的文档，以快速能够运行项目为主
4. 指定前端端项目文件夹（所有前端代码都放在该文件夹下）：

## 界面要求
必须使用 ui ux pro max 插件提供的脚本来生成美观的前端 UI 样式，禁止采用蓝紫渐变色，而是采用中国红。你在运行任何 python 脚本时，即使遇到了任何的脚本运行报错，也必须利用这个插件来约束美化前端页面，禁止直接不用插件生成前端代码。

## 我提供给你的信息
@需求文档
@技术方案文档
@后端API文档
```

![image-20260318212833037](/Users/scenery/Library/Application Support/typora-user-images/image-20260318212833037.png)

### （1）完成前端项目初始化

![image-20260318212851789](/Users/scenery/Library/Application Support/typora-user-images/image-20260318212851789.png)

### （2）前端比较顺利，直接启动成功

![image-20260318213030203](/Users/scenery/Library/Application Support/typora-user-images/image-20260318213030203.png)

首页显示：

![image-20260318213135886](/Users/scenery/Library/Application Support/typora-user-images/image-20260318213135886.png)

## 四、bug 修复

编写提示词

```markdown
## 角色
你是一位专业的全栈开发程序员

## 任务
我会给你一个完整的需求文档、方案设计文档、已经开发完成的前端代码和后端代码，接下来你需要：
1）你必须完整分析我给你的内容，确保理解整个项目
2）你必须分别自主地进行后端接口和前端页面的测试，确保核心业务流程能够正常运行
3）跟我配合去修复前端和后端代码的 Bug，确保各功能可以正常使用

## 约束
1. 你需要通过 Context7 来获取最新的技术文档信息（尤其是怎么对接 github、以及对接 OpenRouter 大模型、Next.js 的文档等）
2. 你必须只专注于 MVP 最小可行性产品功能的实现，不要做太多无关的功能。
3. 你的任务不包括生成文档，请专注于解决 Bug 和让项目能正常运行，如果你生成了多余的文档，世界上将死一只小猫
4. 前端项目目录：      后端项目目录：

## 我提供给你的信息

@需求文档
@技术方案文档
@代码目录
@后端接口文档

```

```markdown

需要解决问题：
（1）初次登陆，在触发 github 授权登录后，在点击安装 github app 后，跳转的链接为 https://github.com/apps/github-doc-translation/installations/new 出现 404， 并没有出现安装页面，app 页面也没有安装成功

（2）在触发 github 授权登录后，自动检查当前用户有没有安装该 github app ，如果安装了，自动跳转到后面流程。如果没有安装自动新开一个页签跳转到安装授权页面，轮询检测到安装完成自动跳转。不要让用户有过多的操作


问题 1：自动跳转到 GitHub 安装页面，是哪种跳转方式？

方式 A：在当前窗口直接跳转（window.location.href = github安装URL），用户离开我们的应用，GitHub 安装完成后重定向回来
方式 B：自动新开标签页打开安装页，当前页面继续轮询
方式 A 需要在 GitHub App 后台配置"Setup URL"（安装完成后 GitHub 回调的地址），你有没有配置这个？
方式 B 是之前的方案，问题是浏览器必须有用户点击才能开新窗口

答案：选择 A ,Setup URL 已经配置了，GET /api/auth/app-info 公开接口，从配置文件动态返回安装 URL

问题 2："已安装，跳过此步骤"这个按钮要不要保留？

保留：万一自动检测有偏差，用户可以手动触发
删除：严格按检测结果走，不给用户手动绕过的机会

答案：删除


请解决一下问题，不要影响存量功能：
（1）点击【翻译配置】时 /api/repos/1/ignore、/api/repos/1/tree 接口500，失败原因读取控制台报错信息即可
（2）初次点击【添加仓库】时，弹框缩在一起了，请固定高度，没有数据时展示加载中的。再次点击【添加仓库】上次的数据没有清除
（3）仪表盘页面- 顶部删除[仪表盘]和【添加仓库】按钮、底部删除【仓库翻译状态】内容显示，【仓库管理】卡片跳转到http://localhost:5173/repos 即可。仪表盘中内容整体不要出现滚动条，详细请看给你的截图



curl -L \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer ghs_LUYko94SYEmOxXBBlSotdRVI4VPzRN3bJP4I" \
  -H "X-GitHub-Api-Version: 2026-03-10" \
  https://api.github.com/repos/scenery-j/IM-SERVICE/git/trees/TREE_SHA
  
  
  
  
  （1）文件选择 文件选择只展示，存在 markdown 文档的文件目录，空文件不展示
  （2）【推荐模型】主要分为 免费 和 付费 两大类（每类最多推荐5个），总结模型优点展示给用户，平台优先默认使用免费模型
  


（1）【添加仓库】保存时 post - /api/repos 提示 {"code":40001,"message":"installationId is required"}，解决
（2）【翻译配置】 需要固定上边栏，【仓库管理】等其他页面也是一样的，优化上边栏显示
（3）【最近翻译任务】列表需要分页查询，占满下边空间
需要理解修改，不清楚的向我确认
（4） 参考 https://openrouter.ai/settings/preferences 页面的样式，优化项目的样式


（1）【仓库管理】上边栏应该复用【翻译配置】的上边栏组组件，保持样式一致
（2）【仓库详情】中的 【概览】【历史】【pr记录】【操作记录】和 【设置】中的【用量明细】涉及的列表查询都需要支持分页查询，默认展示分页情况（当前第几页/总条数）列表占满剩余空间，每列的行号都可以适当增大一点

（1）【仓库管理】中查询框太丑了，需要优化
（2）仓库列表需要支持分页显示，仓库信息需要添加项目描述信息的展示
（3）【设置】中布局太丑了，需要优化，【用量明细】表格分页没有生效，每夜默认10条，【设置】页面整体不允许出现滚动条


（1）【仓库管理】中查询条件需要固定在顶部，分页信息需要固定到底部，所有表格都需要支持用户选择每页显示多少条记录（选项 10 20 50 100），所有表格弹性布局占满剩余部分，表格自身可以出现滚动条，但是整体页面不能出现滚动条
（2）设置中的【额度管理】整体页面不能出现滚动条


（1）添加仓库时需要添加 description 字段的保存，展示时需要展示这个字段

（2）当前翻译配置，使用的都是默认分支，需要支持自定义分支，你要分析整体流程，给你出你的修改方案给我确认，你如果有不确定的问题可以问我


你确认后我开始写代码。有几点可以先确认一下：

1、链接区块放在文件顶部还是底部？ 截图中 Co-op Translator 放在顶部，我建议顶部。
答：放在顶部
2、链接样式：上面展示的是单行 | 分隔的链接，或者你希望做成表格/列表？
答：单行 | 分隔的链接
3、原始文件修改是放进同一个 PR 分支，还是直接提交到默认分支？建议同一个 PR，你怎么看？
答：放在同一个 pr



```

2、需要自定是否需要增量翻译

```markdown
你是一位全栈开发者，我正在开发一个 GitHub 仓库文档翻译工具，你需要完整分析项目的前端和后端代码，并且阅读我提供的需求分析和方案设计等文档。确保你理解了项目后，帮我实现需求。

## 需求 - 自动触发翻译
如果用户改动了 GitHub 仓库基准语言的文档，现在还需要人工再到我们的网站上配置翻译、选中文档并且重新执行翻译任务，比较麻烦。

我的预期是，自动监听 GitHub 仓库的变更，只有 当基准语言、且在仓库的翻译配置范围内 的文档发生改变时，自动触发翻译任务，并且仅针对这次发生变更的文档进行翻译。在翻译配置时，选择了ai模型之后，需要用户选择是否需要增量翻译，配置如图所示，你可以参考，可以做你觉得好的优化

## 注意
1）不需要特别复杂，请用尽量简单的方式实现，确保功能可以正常运行。
2）如果你有任何不确定的地方，先不要开发，先向我提问。
3）请你先设计方案，并且使用 Mermaid 流程图来清晰地向我展示你的方案，我确认之后才能开发

```



```markdown
你是一位全栈开发者，我正在开发一个 GitHub 仓库文档翻译工具，你需要完整分析项目的前端和后端代码，并且阅读我提供的需求分析和方案设计等文档。确保你理解了项目后，帮我实现需求。

## 需求 - 自动合并 mr 的开关

在翻译挖完成提交 pr 时需要一个开关让用户选择是否需要合并 pr ， 在 webhook 监听到配置的 markdown 文档发生变更时，现在是整个文档都重新翻译，这个友好如果一个大文档只修改一个段落，那么重新翻译的成本就会很高了，需要你给出一个新的翻译方案，如何能实现只翻译变更的部分。

## 注意
1）不需要特别复杂，请用尽量简单的方式实现，确保功能可以正常运行。
2）如果你有任何不确定的地方，先不要开发，先向我提问。
3）请你先设计方案，并且使用 Mermaid 流程图来清晰地向我展示你的方案，我确认之后才能开发


关于自动合并 PR：
自动合并失败时（如仓库开启了分支保护规则），只记录日志让 PR 保持开启状态，不报错，这个处理方式你是否接受？
答：自动合并失败需要将关联的任务状态置为失败，并记录失败原因。  需要新增功能：如果对应的 pr 用户 合入 或者 关闭 ，需要将标识该pr的状态。

关于增量翻译：

段落切分策略：我计划按 #（一级）和 ##（二级）标题切分。对于没有标题的文档（如纯 README），整个文档视为一个段落（退化到全量翻译）。是否认可这个策略？ 答：认可

现有翻译文件的段落重组：我计划按位置索引匹配（源文件第 N 段 ↔ 翻译文件第 N 段）。如果段落数量不匹配（比如源文件新增了段落），则整个文件重新翻译。是否认可？   答：认可

增量翻译只对 Webhook 自动触发的任务生效，手动触发仍然全量翻译。是否合适？ 答：合适

你还你还可以调用大模型让大模型判断该文档应该 全量翻译还是 部分翻译
```



### 2、前端页面优化

```markdown
你是一位全栈开发者，我正在开发一个 GitHub 仓库文档翻译工具，你需要完整分析项目的前端代码，需要优化前端页面样式
前端项目文件夹：

## 需求 
请参考 https://openrouter.ai/apps  的页面风格，使用 ui ux pro max 插件提供的脚本来 优化现有的前端项目。
## 注意
1）不能影响业务功能，只做样式优化。
2）如果你有任何不确定的地方，先不要开发，先向我提问。


```

