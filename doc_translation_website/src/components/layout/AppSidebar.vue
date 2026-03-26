<template>
  <aside :class="['app-sidebar', { collapsed }]">
    <nav class="sidebar-nav">
      <router-link to="/dashboard" custom v-slot="{ isActive, navigate }">
        <div :class="['nav-item', { active: isActive }]" @click="navigate" title="仪表盘">
          <span class="nav-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"/>
              <rect x="14" y="3" width="7" height="7"/>
              <rect x="14" y="14" width="7" height="7"/>
              <rect x="3" y="14" width="7" height="7"/>
            </svg>
          </span>
          <span class="nav-label">仪表盘</span>
        </div>
      </router-link>

      <router-link to="/repos" custom v-slot="{ isActive, navigate }">
        <div :class="['nav-item', { active: isActive || isReposActive }]" @click="navigate" title="仓库管理">
          <span class="nav-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
          </span>
          <span class="nav-label">仓库管理</span>
        </div>
      </router-link>

      <router-link to="/settings" custom v-slot="{ isActive, navigate }">
        <div :class="['nav-item', { active: isActive }]" @click="navigate" title="设置">
          <span class="nav-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3"/>
              <path
                  d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
            </svg>
          </span>
          <span class="nav-label">设置</span>
        </div>
      </router-link>

    </nav>

    <div class="sidebar-bottom">
      <div class="nav-divider"></div>
      <a href="https://github.com/scenery-j/GitHub-Doc-Translation" target="_blank" class="nav-item external" title="使用文档">
        <span class="nav-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
            <polyline points="10 9 9 9 8 9"/>
          </svg>
        </span>
        <span class="nav-label">使用文档</span>
      </a>
      <a href="https://github.com/scenery-j/GitHub-Doc-Translation/issues" target="_blank" class="nav-item external" title="反馈建议">
        <span class="nav-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
        </span>
        <span class="nav-label">反馈建议</span>
      </a>
      <div class="sidebar-footer" v-if="!collapsed">
        <div class="version-tag">v1.0 MVP</div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

defineProps<{ collapsed: boolean }>()

const route = useRoute()
const isReposActive = computed(() => route.path.startsWith('/repos'))
</script>

<style scoped>
.app-sidebar {
  width: var(--sidebar-width);
  background: var(--bg-card);
  border-right: 1px solid var(--border-color);
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.app-sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

.sidebar-nav {
  flex: 1;
  padding: 10px 8px;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--border-radius-md);
  cursor: pointer;
  color: var(--text-secondary);
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
  overflow: hidden;
  text-decoration: none;
  font-size: 13.5px;
  font-weight: 500;
  position: relative;
  letter-spacing: -0.01em;
}

.nav-item:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.nav-item.active {
  background: var(--chinese-red-light);
  color: var(--chinese-red);
  font-weight: 600;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: var(--chinese-red);
  border-radius: 0 2px 2px 0;
}

.nav-item.external {
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 400;
}

.nav-item.external:hover {
  color: var(--text-secondary);
  background: var(--bg-hover);
}

.nav-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  opacity: 0.85;
}

.nav-item.active .nav-icon {
  opacity: 1;
}

.nav-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-sidebar.collapsed .nav-label {
  display: none;
}

.app-sidebar.collapsed .nav-item::before {
  left: 0;
  height: 14px;
}

.nav-divider {
  height: 1px;
  background: var(--border-color);
  margin: 6px 4px;
}

.sidebar-bottom {
  flex-shrink: 0;
  padding: 0 8px 8px;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.sidebar-bottom .nav-divider {
  margin: 0 4px 6px;
}

.sidebar-footer {
  padding: 8px 6px 2px;
}

.version-tag {
  font-size: 11px;
  color: var(--text-placeholder);
  font-family: var(--font-mono);
  letter-spacing: 0.02em;
}
</style>
