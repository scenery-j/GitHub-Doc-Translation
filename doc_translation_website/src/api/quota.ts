import request from './index'
import type { PageResponse, QuotaInfo, QuotaUsageRecord } from '@/types'

export const quotaApi = {
    get: () =>
        request.get<never, QuotaInfo>('/quota'),

    getUsage: (params?: { page?: number; size?: number; startDate?: string; endDate?: string }) =>
        request.get<never, PageResponse<QuotaUsageRecord>>('/quota/usage', { params }),

    saveApiKey: (apiKey: string) =>
        request.put<never, void>('/settings/api-key', { apiKey }),

    verifyApiKey: (apiKey: string) =>
        request.post<never, { valid: boolean; message: string }>('/settings/api-key/verify', { apiKey }),

    deleteApiKey: () =>
        request.delete<never, void>('/settings/api-key'),
}
