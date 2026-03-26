import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api/auth'
import type { User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
    const user = ref<User | null>(null)
    const token = ref<string | null>(localStorage.getItem('token'))
    const loading = ref(false)

    const isLoggedIn = computed(() => !!token.value && !!user.value)
    const quotaPercent = computed(() => {
        if (!user.value) return 0
        return Math.round((user.value.usedQuota / user.value.freeQuota) * 100)
    })

    function setToken(newToken: string) {
        token.value = newToken
        localStorage.setItem('token', newToken)
    }

    function clearToken() {
        token.value = null
        user.value = null
        localStorage.removeItem('token')
    }

    async function fetchUser() {
        if (!token.value) return null
        loading.value = true
        try {
            user.value = await authApi.getMe()
            return user.value
        } catch {
            clearToken()
            return null
        } finally {
            loading.value = false
        }
    }

    async function logout() {
        try {
            await authApi.logout()
        } finally {
            clearToken()
        }
    }

    return {
        user,
        token,
        loading,
        isLoggedIn,
        quotaPercent,
        setToken,
        clearToken,
        fetchUser,
        logout,
    }
})
