<template>
  <div class="empty-state">
    <div class="empty-icon">
      <slot name="icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
      </slot>
    </div>
    <p class="empty-title">{{ title }}</p>
    <p class="empty-desc" v-if="description">{{ description }}</p>
    <div class="empty-action" v-if="$slots.action">
      <slot name="action"/>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 24px;
  text-align: center;
}

.empty-icon {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-title {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--text-secondary);
  margin: 0 0 7px;
  letter-spacing: -0.01em;
}

.empty-desc {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0 0 22px;
  max-width: 300px;
  line-height: 1.6;
}

.empty-action {
  margin-top: 4px;
}
</style>
