<template>
  <router-view/>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onMounted(async () => {
  // Handle OAuth callback token from URL
  const urlParams = new URLSearchParams(window.location.search)
  const token = urlParams.get('token')
  if (token) {
    authStore.setToken(token)
    // Clean URL
    const cleanUrl = window.location.pathname
    window.history.replaceState({}, '', cleanUrl)
    await authStore.fetchUser()
  }
})
</script>
