<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">
        <svg width="48" height="48" viewBox="0 0 32 32" fill="none">
          <rect width="32" height="32" rx="8" fill="#DE2910"/>
          <path d="M8 10h16M8 16h10M8 22h12" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
          <circle cx="23" cy="20" r="5" fill="white"/>
          <path d="M21 20l1.5 1.5L25 18" stroke="#DE2910" stroke-width="1.5" stroke-linecap="round"
                stroke-linejoin="round"/>
        </svg>
      </div>
      <h1 class="login-title">GitHub Doc Translation</h1>
      <p class="login-desc">AI 驱动的 GitHub 文档多语言翻译平台</p>

      <a :href="loginUrl" class="github-btn">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
          <path
              d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
        </svg>
        使用 GitHub 登录
      </a>

      <p class="login-agreement">
        登录即表示同意
        <a href="#">服务条款</a>
        和
        <a href="#">隐私政策</a>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
const loginUrl = '/api/auth/github'
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #FAFBFC;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.login-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(0, 0, 0, 0.045) 1px, transparent 1px);
  background-size: 22px 22px;
  pointer-events: none;
}

.login-page::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -65%);
  width: 600px;
  height: 400px;
  background: radial-gradient(ellipse at center, rgba(222, 41, 16, 0.07) 0%, transparent 65%);
  pointer-events: none;
}

.login-card {
  background: var(--bg-card);
  border-radius: var(--border-radius-xl);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-xl);
  padding: 52px 44px;
  width: 100%;
  max-width: 400px;
  text-align: center;
  position: relative;
  z-index: 1;
}

.login-logo {
  margin-bottom: 20px;
}

.login-title {
  font-size: 21px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 8px;
  letter-spacing: -0.025em;
}

.login-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 36px;
  line-height: 1.65;
}

.github-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  background: #18181B;
  color: #FAFAFA;
  padding: 13px 24px;
  border-radius: var(--border-radius-md);
  text-decoration: none;
  font-size: 14.5px;
  font-weight: 600;
  letter-spacing: -0.01em;
  transition: background 0.15s, transform 0.15s, box-shadow 0.15s;
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
}

.github-btn:hover {
  background: #27272A;
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.login-agreement {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0;
  line-height: 1.6;
}

.login-agreement a {
  color: var(--chinese-red);
  text-decoration: none;
  font-weight: 500;
}

.login-agreement a:hover {
  text-decoration: underline;
}
</style>
