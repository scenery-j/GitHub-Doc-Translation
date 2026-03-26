<template>
  <div class="app-layout">
    <AppHeader @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed"/>
    <div class="layout-body">
      <AppSidebar :collapsed="sidebarCollapsed"/>
      <main class="layout-main">
        <slot/>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'

const sidebarCollapsed = ref(false)
</script>

<style scoped>
.app-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-body {
  display: flex;
  flex: 1;
  overflow: hidden;
  padding-top: var(--header-height);
}

.layout-main {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-page);
}
</style>
