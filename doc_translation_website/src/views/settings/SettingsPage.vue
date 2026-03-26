<template>
  <AppLayout>
    <div class="settings-wrap">

      <!-- Header -->
      <div class="settings-hero">
        <div class="hero-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"/>
            <path
                d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
          </svg>
        </div>
        <div class="hero-text">
          <h1 class="hero-title">设置</h1>
          <p class="hero-sub">管理账号信息、API 密钥和翻译额度</p>
        </div>
      </div>

      <!-- Body: nav + content -->
      <div class="settings-body">
        <!-- Left nav -->
        <nav class="settings-nav">
          <router-link
              v-for="item in menuItems"
              :key="item.path"
              :to="item.path"
              custom
              v-slot="{ isActive, navigate }"
          >
            <button
                :class="['nav-item', { active: isActive }]"
                @click="navigate"
                type="button"
            >
              <span class="nav-icon">
                <component :is="item.icon" style="width:15px;height:15px"/>
              </span>
              <span class="nav-label">{{ item.label }}</span>
              <span v-if="isActive" class="nav-active-dot"/>
            </button>
          </router-link>
        </nav>

        <!-- Right content -->
        <div class="settings-content">
          <router-view/>
        </div>
      </div>

    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from '@/components/layout/AppLayout.vue'
import { Key, User, Wallet } from '@element-plus/icons-vue'

const menuItems = [
  { path: '/settings/profile', label: '个人信息', icon: User },
  { path: '/settings/api-key', label: 'API Key', icon: Key },
  { path: '/settings/quota', label: '额度管理', icon: Wallet },
]
</script>

<style scoped>
.settings-wrap {
  height: calc(100vh - var(--header-height));
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* ===== Hero header ===== */
.settings-hero {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 32px;
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.hero-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #fdefee 0%, #fdd 100%);
  border: 1px solid #f5c6c2;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--chinese-red);
  flex-shrink: 0;
}

.hero-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.hero-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.2;
}

.hero-sub {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0;
}

/* ===== Body ===== */
.settings-body {
  flex: 1;
  overflow: hidden;
  display: flex;
}

/* ===== Left nav ===== */
.settings-nav {
  width: 196px;
  flex-shrink: 0;
  padding: 16px 12px;
  background: #fff;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 9px;
  width: 100%;
  padding: 9px 10px;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  font-size: 13.5px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: background 0.12s, color 0.12s;
  text-align: left;
  position: relative;
}

.nav-item:hover {
  background: var(--bg-page);
  color: var(--text-primary);
}

.nav-item.active {
  background: #fdefee;
  color: var(--chinese-red);
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 6px;
  background: transparent;
  flex-shrink: 0;
  transition: background 0.12s;
}

.nav-item.active .nav-icon {
  background: #fad5d1;
}

.nav-label {
  flex: 1;
}

.nav-active-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--chinese-red);
}

/* ===== Right content ===== */
.settings-content {
  flex: 1;
  overflow: hidden;
  background: var(--bg-page);
  min-width: 0;
}
</style>
