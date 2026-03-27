# GitHub Doc Translation

<p align="center">
  <strong>AI-driven GitHub documentation multilingual translation platform</strong>
</p>

<p align="center">
  Let your open-source project be seen by the world
</p>

<p align="center">
  <a href="#功能特性">Features</a> •
  <a href="#技术栈">Tech Stack</a> •
  <a href="#快速开始">Getting Started</a> •
  <a href="#部署">Deployment</a> •
  <a href="#项目结构">Project Structure</a> •
  <a href="#页面操作指导">Page Operation Guide</a> •
</p>

---

## IntroductionGitHub Doc Translation is a **SaaS platform** that provides AI-driven automatic multilingual documentation translation service for GitHub open-source repositories.

After users log in via GitHub OAuth and install the GitHub App to authorize repositories, the platform automatically translates Markdown documents in the repository into multiple target languages and submits them as pull requests to the original repository. When the documentation in the base language changes, the platform automatically detects and performs incremental translation synchronization.

### Why is this project needed?

Many Chinese open-source projects are of high quality but have only Chinese documentation, making it difficult for international users to use them, limiting the project's impact and traffic growth. Manual translation is costly and hard to maintain continuously. Existing tools are mostly CLI or GitHub Actions, with complex configuration and lacking visual management capabilities.

This project aims to provide a **one-stop, zero-configuration SaaS translation service**, lowering the barrier to internationalization for open-source projects.

---

## Project Documentation

| Document                                    | Description                    |
|---------------------------------------------|--------------------------------|
| [需求规格文档](docs/需求规格文档.md)              | Product requirements, functional modules, user personas, etc. |
| [技术实现方案](docs/技术实现方案.md)              | System architecture, database design, API design, etc. |
| [API 接口文档](docs/API接口文档.md)           | Detailed description of backend API |
| [快速启动指南](docs/快速启动指南.md)              | Steps to set up local development environment |
| [人工配置文档](docs/人工配置文档.md)              | GitHub App creation, environment variable configuration, etc. |
| [Vibe Coding 开发过程提示词](docs/VibeCoding过程提示词.md) | Vibe Coding development process |

---

## Features

- **🔌 Zero-configuration integration** -