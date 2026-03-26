<template>
  <div class="setup-page">
    <header class="setup-header">
      <div class="setup-logo">
        <svg width="28" height="28" viewBox="0 0 32 32" fill="none">
          <rect width="32" height="32" rx="8" fill="#DE2910"/>
          <path d="M8 10h16M8 16h10M8 22h12" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
          <circle cx="23" cy="20" r="5" fill="white"/>
          <path d="M21 20l1.5 1.5L25 18" stroke="#DE2910" stroke-width="1.5" stroke-linecap="round"
                stroke-linejoin="round"/>
        </svg>
        <span>GitHub Doc Translation</span>
      </div>
      <div class="setup-user" v-if="authStore.user">
        <img :src="authStore.user.avatarUrl" class="user-avatar"/>
        <span>{{ authStore.user.username }}</span>
      </div>
    </header>

    <div class="setup-body">
      <div class="setup-card">
        <div class="spinner-wrap">
          <div class="spinner"></div>
        </div>
        <h2>{{ statusTitle }}</h2>
        <p>{{ statusDesc }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

type Phase = 'checking' | 'redirecting_to_github' | 'verifying'
const phase = ref<Phase>('checking')

const statusTitle = computed(() => {
  if (phase.value === 'checking') return '正在检查安装状态...'
  if (phase.value === 'redirecting_to_github') return '正在跳转到 GitHub...'
  return '正在验证安装...'
})

const statusDesc = computed(() => {
  if (phase.value === 'checking') return '请稍候'
  if (phase.value === 'redirecting_to_github') return '即将跳转到 GitHub App 安装页面，完成后将自动返回'
  return '正在验证 GitHub App 安装状态，请稍候...'
})

onMounted(async () => {
  const urlParams = new URLSearchParams(window.location.search)
  const installationId = urlParams.get('installation_id')

  // Returning from GitHub installation callback
  if (installationId) {
    phase.value = 'verifying'
    // Clean URL
    window.history.replaceState({}, '', '/setup')
    // Fetch latest user state (with retry)
    for (let i = 0; i < 3; i++) {
      await authStore.fetchUser()
      if (authStore.user?.hasInstallation) break
      if (i < 2) await new Promise(r => setTimeout(r, 1500))
    }
    router.replace('/dashboard')
    return
  }

  // No token / not logged in
  if (!authStore.user) {
    await authStore.fetchUser()
    if (!authStore.user) {
      router.replace('/')
      return
    }
  }

  // Already installed
  if (authStore.user.hasInstallation) {
    router.replace('/dashboard')
    return
  }

  // Not installed → fetch install URL and redirect in current window
  phase.value = 'redirecting_to_github'
  let installUrl = 'https://github.com/apps/github-doc-translation/installations/new'
  try {
    const info = await authApi.getAppInfo()
    installUrl = info.installUrl
  } catch {
    // use fallback
  }

  // Small delay so user sees the message, then redirect current window
  await new Promise(r => setTimeout(r, 800))
  window.location.href = installUrl
})
</script>

<style scoped>
.setup-page {
  min-height: 100vh;
  background: var(--bg-page);
  display: flex;
  flex-direction: column;
}

.setup-header {
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  padding: 16px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.setup-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.setup-user {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
}

.setup-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
}

.setup-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid var(--border-color);
  padding: 48px 40px;
  width: 100%;
  max-width: 420px;
  text-align: center;
  box-shadow: var(--shadow-md);
}

.setup-card h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.setup-card p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
}

.spinner-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #E2E8F0;
  border-top-color: var(--chinese-red, #DE2910);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
