import { defineStore } from 'pinia'
import { ref } from 'vue'
import { reposApi } from '@/api/repos'
import type { Repository, RepositoryDetail } from '@/types'

export const useReposStore = defineStore('repos', () => {
    const repos = ref<Repository[]>([])
    const currentRepo = ref<RepositoryDetail | null>(null)
    const loading = ref(false)
    const total = ref(0)

    async function fetchRepos(params?: { page?: number; size?: number; search?: string; status?: string }) {
        loading.value = true
        try {
            const result = await reposApi.list(params)
            repos.value = result.records
            total.value = result.total
            return result
        } finally {
            loading.value = false
        }
    }

    async function fetchRepo(id: number) {
        loading.value = true
        try {
            currentRepo.value = await reposApi.get(id)
            return currentRepo.value
        } finally {
            loading.value = false
        }
    }

    return { repos, currentRepo, loading, total, fetchRepos, fetchRepo }
})
