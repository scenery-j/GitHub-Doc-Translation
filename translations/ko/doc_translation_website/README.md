# GitHub Doc Translation — 프론트엔드 빠른 시작 가이드

> **프론트엔드 기술 스택**: Vue 3 + Vite + Element Plus + TypeScript + Pinia + Vue Router

---

## 목차

1. [환경 요구](#환경要求)
2. [빠른 시작](#快速启动)
3. [프로젝트 구조 설명](#项目结构说明)
4. [페이지 라우팅](#页面路由)
5. [개발 프록시 구성](#开发代理配置)
6. [테마 색상 설명](#主题颜色说明)
7. [빌드 및 배포](#构建与部署)
8. [자주 묻는 질문](#常见问题)

---

## 환경 요구

| 도구 | 최소 버전 | 설명 |
|------|---------|------|
| Node.js | ≥ 20.x | LTS 버전 사용 권장 |
| npm | ≥ 9.x | pnpm 또는 yarn 사용 |

검증：

%%CODEBLOCK_0%%

---

## 빠른 시작

### 1. 의존성 설치

%%CODEBLOCK_1%%

### 2. 개발 서버 시작

%%CODEBLOCK_2%%> 방문 [http://localhost:5173](http://localhost:5173)

### 3. 전제 조건: 백엔드 서비스

프론트엔드 개발 서버는 기본적으로 `/api/*` 요청을 `http://localhost:8080`(백엔드 Spring Boot 서비스)로 프록시합니다.
백엔드 서비스가 실행 중인지 확인하세요：

%%CODEBLOCK_3%%

백엔드 주소를 수정해야 하는 경우, `vite.config.ts`의 proxy 설정을 편집하세요：

%%CODEBLOCK_4%%

---

## 프로젝트 구조 설명

%%CODEBLOCK_5%%

---

## 페이지 라우팅| 라우트 | 페이지 | 인증 요구 |
|------|------|---------|
| `/` | 랜딩 페이지 | 없음 |
| `/login` | 로그인 페이지 | 없음 |
| `/setup` | GitHub App 설치 가이드 | 로그인 필요 |
| `/dashboard` | 대시보드 개요 | 로그인 필요 |
| `/repos` | 저장소 관리 목록 | 로그인 필요 |
| `/repos/:id` | 저장소 세부 정보(개요/작업/PR/로그) | 로그인 필요 |
| `/repos/:id/config` | 번역 설정(파일 선택/언어/모델) | 로그인 필요 |
| `/repos/:id/tasks/:taskId` | 번역 작업 실시간 진행 상황 | 로그인 필요 |
| `/settings/profile` | 개인 정보 | 로그인 필요 |
| `/settings/api-key` | OpenRouter API 키 관리 | 로그인 필요 |
| `/settings/quota` | 번역 할당량 및 사용량 세부 정보 | 로그인 필요 |

---

## 개발 프록시 구성

프론트엔드 개발 시, 모든 `/api/*` 요청이 자동으로 백엔드로 프록시됩니다(크로스オリ진 문제를 방지하기 위해):

%%CODEBLOCK_6%%

**GitHub OAuth 로그인 흐름**：

1. 사용자가 'GitHub로 로그인'을 클릭하면 → 프론트엔드가 바로 `/api/auth/github`로 이동합니다(백엔드가 GitHub로 리다이렉트).
2. GitHub 인증 후 콜백 `/api/auth/github/callback`
3. 백엔드 처리가 완료되면 프론트엔드 `/dashboard?token=xxx` 또는 `/setup?token=xxx`로 리다이렉트됩니다.
4. `App.vue`에서 URL의 `token` 매개변수를 자동으로 추출하여 `localStorage`에 저장합니다.
5. 모든 후속 API 요청에 `Authorization: Bearer {token}` 헤더가 자동으로 추가됩니다.

---

## 테마 색상 설명

이 프로젝트는 **중국 빨강**(`#DE2910`)을 테마 색상으로 사용하며, Element Plus CSS 변수를覆盖하여 구현합니다：

%%CODEBLOCK_7%%

글꼴은 **IBM Plex Sans**(본문)와 **JetBrains Mono**(코드/숫자)를 사용하며, Google Fonts를 통해 가져옵니다。

---

## 빌드 및 배포

### 개발 빌드%%CODEBLOCK_8%%

### 생산 배포(Nginx와 함께)

%%CODEBLOCK_9%%

---

## 자주 묻는 질문

### Q: 페이지가 흰색/빈 화면으로 표시됨- npm install이 완료되었는지 확인하세요
- Node.js 버전이 20 이상인지 확인하세요
- 브라우저 콘솔 오류를 확인하세요

### Q: API 요청 404 / 네트워크 오류

- 백엔드 Spring Boot 서비스가 8080 포트에서 실행 중인지 확인하세요
- vite.config.ts의 프록시 설정을 확인하세요

### Q: 로그인 후 /setup으로 리다이렉트되고 /dashboard로 가지 않음

- GitHub App이 아직 설치되지 않았음을 의미하며, 설치 가이드 단계를 따라 설치하면 됩니다.

### Q: 번역 설정 페이지의 파일 트리가 비어 있음

- 저장소가 플랫폼에 추가되었는지 확인하세요
- GitHub App이 해당 저장소에 대한 접근 권한을 가지고 있는지 확인하세요
- 백엔드 인터페이스 GET /api/repos/{id}/tree가 정상적으로 데이터를 반환해야 합니다

### Q: 백엔드 API 주소를 어떻게 변경하나요?

`vite.config.ts`의 proxy target을 수정하거나, 생산 환경에서 Nginx 역방향 프록시 설정을 통해 변경할 수 있습니다.

---

> 문제가 있으면 [GitHub 이슈](https://github.com/issues)를 제출해 주세요.