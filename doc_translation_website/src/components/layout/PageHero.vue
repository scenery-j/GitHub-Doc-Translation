<template>
  <div class="page-hero">
    <div class="hero-left">
      <!-- Back button -->
      <router-link v-if="backTo" :to="backTo" class="hero-back">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </router-link>

      <!-- Icon -->
      <div class="hero-icon">
        <slot name="icon">
          <!-- Default: folder icon -->
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
        </slot>
      </div>

      <!-- Breadcrumb trail + title -->
      <div class="hero-meta">
        <div v-if="trail && trail.length" class="hero-nav-trail">
          <template v-for="(item, i) in trail" :key="i">
            <router-link v-if="item.to" :to="item.to" class="trail-link">{{ item.label }}</router-link>
            <span v-else class="trail-current">{{ item.label }}</span>
            <span v-if="i < trail.length - 1" class="trail-sep">/</span>
          </template>
        </div>
        <h1 class="hero-title">{{ title }}</h1>
      </div>
    </div>

    <!-- Right actions slot -->
    <div class="hero-right" v-if="$slots.right">
      <slot name="right"/>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  backTo?: string
  trail?: { label: string; to?: string }[]
}>()
</script>

<style scoped>
.page-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 28px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 20;
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  text-decoration: none;
  transition: border-color 0.15s, color 0.15s, background 0.15s;
  flex-shrink: 0;
}

.hero-back:hover {
  border-color: var(--chinese-red-border);
  color: var(--chinese-red);
  background: var(--chinese-red-light);
}

.hero-icon {
  width: 38px;
  height: 38px;
  border-radius: var(--border-radius-md);
  background: linear-gradient(135deg, var(--chinese-red-light) 0%, #fde4e0 100%);
  border: 1px solid var(--chinese-red-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--chinese-red);
  flex-shrink: 0;
}

.hero-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.hero-nav-trail {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11.5px;
}

.trail-link {
  color: var(--text-muted);
  text-decoration: none;
  font-weight: 400;
  transition: color 0.12s;
}

.trail-link:hover {
  color: var(--chinese-red);
}

.trail-sep {
  color: var(--border-color-hover);
  font-size: 11px;
}

.trail-current {
  color: var(--text-secondary);
  font-weight: 500;
}

.hero-title {
  font-size: 17px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.hero-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
</style>
