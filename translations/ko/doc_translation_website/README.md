<!--TRANSLATION_LINKS_START-->
#### Supported by [GitHub Doc Translation](https://github.com/scenery-j/GitHub-Doc-Translation)
> 📖 **다른 언어 버전**：[English (en)](../translations/en/doc_translation_website/README.md) | [日本語 (ja)](../translations/ja/doc_translation_website/README.md) | [한국어 (ko)](../translations/ko/doc_translation_website/README.md)
<!--TRANSLATION_LINKS_END-->

# GitHub Doc Translation — 프론트엔드 빠른 시작 가이드

> **프론트엔드 기술 스택**: Vue 3 + Vite + Element Plus + TypeScript + Pinia + Vue Router

---

## 목차

1. [환경 요구사항](#환경 요구사항)
2. [빠른 시작](#빠른 시작)
3. [프로젝트 구조 설명](#프로젝트 구조 설명)
4. [페이지 라우팅](#페이지 라우팅)
5. [개발 프록시 설정](#개발 프록시 설정)
6. [테마 색상 설명](#테마 색상 설명)
7. [빌드 및 배포](#빌드 및 배포)
8. [자주 묻는 질문](#자주 묻는 질문)

---

## 환경 요구사항

| 도구 | 최소 버전 | 설명 |
|------|---------|------|
| Node.js | ≥ 20.x | LTS 버전 사용 권장 |
| npm | ≥ 9.x | pnpm 또는 yarn 사용 |

검증:

%%CODEBLOCK_0%%

---

## 빠른 시작

### 1. 종속성 설치

%%CODEBLOCK_1%%

### 2. 개발 서버 시작

%%CODEBLOCK_2%%> 방문 [http://localhost:5173](http://localhost:5173)

### 3. 사전 조건: 백엔드 서비스

프론트엔드 개발 서버는 기본적으로 `/api/*` 요청을 `http://localhost:8080` 로 프록시합니다（백엔드 Spring Boot 서비스）。

백엔드 서비스가 실행 중인지 확인하세요：

%%CODEBLOCK_3%%

백엔드 주소를 수정해야 하는 경우, `vite.config.ts` 의 proxy 설정을 편집하세요：

%%CODEBLOCK_4%%

---

## 프로젝트 구조 설명

%%CODEBLOCK_5%%

---

## 페이지 라우팅

| 라우트 | 페이지 | 인증 요구 |
|------|------|---------|
| `/` | 랜딩 페이지 | 없음 |
| `/login` | 로그인 페이지 | 없음 |
| `/setup` | GitHub App 설치 안내 | 로그인 필요 |
| `/dashboard` | 대시보드 개요 | 로그인 필요 |
| `/repos` | 저장소 관리 목록 | 로그인 필요 |
| `/repos/:id` | 저장소 세부 정보 (개요/작업/PR/로그) | 로그인 필요 |
| `/repos/:id/config` | 번역 설정 (파일 선택/언어/모델) | 로그인 필요 |
| `/repos/:id/tasks/:taskId` | 번역 작업 실시간 진행 상황 | 로그인 필요 |
| `/settings/profile` | 개인 정보 | 로그인 필요 |
| `/settings/api-key` | OpenRouter API 키 관리 | 로그인 필요 |
| `/settings/quota` | 번역 할당량 및 사용 내역 | 로그인 필요 |

---

## 개발 프록시 설정

프론트엔드 개발 시, 모든 `/api/*` 요청이 자동으로 백엔드로 프록시됩니다（CORS 방지）：

%%CODEBLOCK_6%%**GitHub OAuth 로그인 흐름**：

1. 사용자가 "GitHub로 로그인"을 클릭 → 프론트엔드가 바로 `/api/auth/github` 로 이동합니다（백엔드가 GitHub로 리다이렉트）
2. GitHub 인증 후 콜백 `/api/auth/github/callback`
3. 백엔드 처리 후 프론트엔드 `/dashboard?token=xxx` 또는 `/setup?token=xxx` 로 리다이렉트
4. `App.vue` 에서 URL 의 `token` 매개변수를 자동으로 추출하여 `localStorage` 에 저장
5. 모든 후속 API 요청에 `Authorization: Bearer {token}` 헤더가 자동으로 추가됩니다---

## 테마 색상