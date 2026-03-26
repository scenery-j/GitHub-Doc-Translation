<template>
  <header class="app-header">
    <div class="header-left">
      <button class="sidebar-toggle" @click="$emit('toggle-sidebar')" title="折叠/展开菜单">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="3" y1="6" x2="21" y2="6"/>
          <line x1="3" y1="12" x2="21" y2="12"/>
          <line x1="3" y1="18" x2="21" y2="18"/>
        </svg>
      </button>
      <router-link to="/" class="logo-link">
        <div class="logo">
          <svg width="28" height="28" viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="8" fill="#DE2910"/>
            <path d="M8 10h16M8 16h10M8 22h12" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="23" cy="20" r="5" fill="white"/>
            <path d="M21 20l1.5 1.5L25 18" stroke="#DE2910" stroke-width="1.5" stroke-linecap="round"
                  stroke-linejoin="round"/>
          </svg>
          <span class="logo-text">GitHub Doc Translation</span>
        </div>
      </router-link>
    </div>

    <div class="header-right">
      <div class="quota-badge" v-if="authStore.user">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <path d="M12 6v6l4 2"/>
        </svg>
        <span>{{ formatQuota(authStore.user.freeQuota - authStore.user.usedQuota) }} tokens</span>
      </div>

      <el-dropdown trigger="click" @command="handleCommand">
        <button class="user-btn" v-if="authStore.user">
          <img :src="authStore.user.avatarUrl" :alt="authStore.user.username" class="user-avatar"/>
          <span class="user-name">{{ authStore.user.username }}</span>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   style="margin-right:8px">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              个人设置
            </el-dropdown-item>
            <el-dropdown-item command="apikey">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   style="margin-right:8px">
                <path
                    d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0 3 3L22 7l-3-3m-3.5 3.5L19 4"/>
              </svg>
              API Key 管理
            </el-dropdown-item>
            <el-dropdown-item command="quota">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   style="margin-right:8px">
                <rect x="2" y="7" width="20" height="14" rx="2" ry="2"/>
                <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
              </svg>
              额度管理
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   style="margin-right:8px">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

defineEmits(['toggle-sidebar'])

const router = useRouter()
const authStore = useAuthStore()

function formatQuota(n: number): string {
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

async function handleCommand(cmd: string) {
  if (cmd === 'profile') router.push('/settings/profile')
  else if (cmd === 'apikey') router.push('/settings/api-key')
  else if (cmd === 'quota') router.push('/settings/quota')
  else if (cmd === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '退出登录', {
      confirmButtonText: '确认退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  }
}
</script>

<style scoped>
.app-header {
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  box-shadow: var(--shadow-xs);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sidebar-toggle {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 7px;
  border-radius: var(--border-radius-md);
  display: flex;
  align-items: center;
  transition: background 0.15s, color 0.15s;
}

.sidebar-toggle:hover {
  background: var(--bg-hover);
  color: var(--text-secondary);
}

.logo-link {
  text-decoration: none;
}

.logo {
  display: flex;
  align-items: center;
  gap: 9px;
}

.logo-text {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
  letter-spacing: -0.02em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quota-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-page);
  padding: 4px 11px;
  border-radius: var(--border-radius-full);
  border: 1px solid var(--border-color);
  font-family: var(--font-mono);
  font-weight: 500;
  transition: border-color 0.15s;
}

.quota-badge:hover {
  border-color: var(--border-color-hover);
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-md);
  padding: 5px 10px 5px 6px;
  cursor: pointer;
  color: var(--text-primary);
  transition: background 0.15s, border-color 0.15s, box-shadow 0.15s;
}

.user-btn:hover {
  background: var(--bg-hover);
  border-color: var(--border-color-hover);
  box-shadow: var(--shadow-xs);
}

.user-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  object-fit: cover;
  border: 1.5px solid var(--border-color);
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.01em;
}
</style>
