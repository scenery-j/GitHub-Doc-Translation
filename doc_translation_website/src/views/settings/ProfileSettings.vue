<template>
  <div class="profile-settings">
    <!-- Header bar -->
    <div class="section-header">
      <h2 class="section-title">个人信息</h2>
      <p class="section-desc">您的 GitHub 账号信息与平台状态</p>
    </div>

    <div v-if="user" class="profile-body">
      <!-- Avatar card -->
      <div class="avatar-card">
        <div class="avatar-wrap">
          <img :src="user.avatarUrl" :alt="user.username" class="avatar-img"/>
          <div class="avatar-badge">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <path
                  d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
          </div>
        </div>
        <div class="avatar-info">
          <div class="user-name">{{ user.username }}</div>
          <a :href="`https://github.com/${user.username}`" target="_blank" rel="noopener" class="github-link">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor" style="flex-shrink:0">
              <path
                  d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            github.com/{{ user.username }}
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 style="flex-shrink:0">
              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
              <polyline points="15 3 21 3 21 9"/>
              <line x1="10" y1="14" x2="21" y2="3"/>
            </svg>
          </a>
        </div>
      </div>

      <!-- Status cards -->
      <div class="status-grid">
        <div :class="['status-card', user.hasInstallation ? 'ok' : 'warn']">
          <div class="status-card-icon">
            <svg v-if="user.hasInstallation" width="18" height="18" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
          </div>
          <div>
            <div class="status-card-label">GitHub App</div>
            <div class="status-card-value">{{ user.hasInstallation ? '已安装' : '未安装' }}</div>
          </div>
        </div>

        <div :class="['status-card', user.hasOpenrouterKey ? 'ok' : 'neutral']">
          <div class="status-card-icon">
            <svg v-if="user.hasOpenrouterKey" width="18" height="18" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2">
              <path
                  d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0 3 3L22 7l-3-3m-3.5 3.5L19 4"/>
            </svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path
                  d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0 3 3L22 7l-3-3m-3.5 3.5L19 4"/>
            </svg>
          </div>
          <div>
            <div class="status-card-label">OpenRouter Key</div>
            <div class="status-card-value">{{ user.hasOpenrouterKey ? '已配置' : '未配置' }}</div>
          </div>
        </div>
      </div>

      <!-- Account details table -->
      <div class="detail-section">
        <h3 class="detail-title">账号详情</h3>
        <div class="detail-table">
          <div class="detail-row">
            <span class="detail-key">用户名</span>
            <span class="detail-val mono">{{ user.username }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-key">邮箱</span>
            <span class="detail-val">{{ user.email || '未公开' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-key">注册时间</span>
            <span class="detail-val">{{ formatTime(user.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-key">登录方式</span>
            <span class="detail-val">
              <span class="provider-badge">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor">
                  <path
                      d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                </svg>
                GitHub OAuth
              </span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="skeleton-wrap">
      <el-skeleton :rows="6" animated/>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

function formatTime(d: string) {
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}
</script>

<style scoped>
.profile-settings {
  height: 100%;
  overflow-y: auto;
}

.section-header {
  padding: 24px 28px 20px;
  border-bottom: 1px solid var(--border-color);
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.section-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.profile-body {
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Avatar card */
.avatar-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px;
  background: linear-gradient(135deg, #fdefee 0%, #fff5f5 100%);
  border: 1px solid #fad5d1;
  border-radius: 12px;
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.avatar-img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(222, 41, 16, 0.2);
  display: block;
}

.avatar-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 22px;
  height: 22px;
  background: #1a1a1a;
  border-radius: 50%;
  border: 2px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.avatar-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.github-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.15s;
}

.github-link:hover {
  color: var(--chinese-red);
}

/* Status grid */
.status-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.status-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid;
  transition: box-shadow 0.15s;
}

.status-card:hover {
  box-shadow: var(--shadow-sm);
}

.status-card.ok {
  background: #F0FDF4;
  border-color: #BBF7D0;
  color: #15803D;
}

.status-card.warn {
  background: #FFFBEB;
  border-color: #FDE68A;
  color: #92400E;
}

.status-card.neutral {
  background: var(--bg-page);
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.status-card-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.status-card-label {
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  opacity: 0.7;
  margin-bottom: 2px;
}

.status-card-value {
  font-size: 14px;
  font-weight: 600;
}

/* Detail section */
.detail-section {
  border: 1px solid var(--border-color);
  border-radius: 10px;
  overflow: hidden;
}

.detail-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  padding: 12px 16px;
  background: var(--bg-page);
  border-bottom: 1px solid var(--border-color);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin: 0;
}

.detail-table {
  display: flex;
  flex-direction: column;
}

.detail-row {
  display: flex;
  align-items: center;
  padding: 13px 16px;
  border-bottom: 1px solid var(--border-color);
  font-size: 13.5px;
  gap: 12px;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-key {
  min-width: 90px;
  color: var(--text-muted);
  font-size: 13px;
}

.detail-val {
  color: var(--text-primary);
  font-weight: 500;
  flex: 1;
}

.detail-val.mono {
  font-family: var(--font-mono);
  font-size: 12.5px;
}

.provider-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  background: #f3f4f6;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: #374151;
}

.skeleton-wrap {
  padding: 28px;
}
</style>
