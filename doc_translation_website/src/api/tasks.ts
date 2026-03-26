import request from './index'
import type { PageResponse, TranslationTask, TranslationTaskDetail } from '@/types'

export const tasksApi = {
    trigger: (repoId: number, data: { type: 'full' | 'incremental'; branches?: string[] }) =>
        request.post<never, { taskIds: number[] }>(`/repos/${repoId}/translate`, data),

    list: (repoId: number, params?: { page?: number; size?: number; status?: string }) =>
        request.get<never, PageResponse<TranslationTask>>(`/repos/${repoId}/tasks`, { params }),

    get: (id: number) =>
        request.get<never, TranslationTaskDetail>(`/tasks/${id}`),

    cancel: (id: number) =>
        request.post<never, void>(`/tasks/${id}/cancel`),
}
