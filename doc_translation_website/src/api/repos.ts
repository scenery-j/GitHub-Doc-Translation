import request from './index'
import type {
    AvailableRepository,
    BranchConfig,
    FileTreeNode,
    ModelsResponse,
    PageResponse,
    Repository,
    RepositoryDetail,
    TranslationConfig
} from '@/types'

export const reposApi = {
    list: (params?: { page?: number; size?: number; search?: string; status?: string }) =>
        request.get<never, PageResponse<Repository>>('/repos', { params }),

    available: (params?: { page?: number; size?: number }) =>
        request.get<never, PageResponse<AvailableRepository>>('/repos/available', { params }),

    add: (data: {
        githubRepoId: number;
        installationId: number;
        fullName: string;
        defaultBranch: string;
        description?: string
    }) =>
        request.post<never, Repository>('/repos', data),

    get: (id: number) =>
        request.get<never, RepositoryDetail>(`/repos/${id}`),

    remove: (id: number) =>
        request.delete<never, void>(`/repos/${id}`),

    // Global config (baseLanguage, targetLanguages, aiModel)
    getConfig: (id: number) =>
        request.get<never, TranslationConfig>(`/repos/${id}/config`),

    updateConfig: (id: number, data: Partial<TranslationConfig>) =>
        request.put<never, TranslationConfig>(`/repos/${id}/config`, data),

    // Branch management
    getBranches: (id: number) =>
        request.get<never, BranchConfig[]>(`/repos/${id}/branches`),

    getAvailableBranches: (id: number) =>
        request.get<never, string[]>(`/repos/${id}/branches/available`),

    addBranch: (id: number, branchName: string) =>
        request.post<never, BranchConfig>(`/repos/${id}/branches`, { branchName }),

    getBranchConfig: (id: number, branch: string) =>
        request.get<never, BranchConfig>(`/repos/${id}/branches/config`, { params: { branch } }),

    updateBranchConfig: (id: number, branch: string, data: Partial<BranchConfig>) =>
        request.put<never, BranchConfig>(`/repos/${id}/branches/config`, data, { params: { branch } }),

    deleteBranch: (id: number, branch: string) =>
        request.delete<never, void>(`/repos/${id}/branches`, { params: { branch } }),

    // File tree (branch-aware)
    getTree: (id: number, branch?: string) =>
        request.get<never, FileTreeNode[]>(`/repos/${id}/tree`, { params: branch ? { branch } : undefined }),

    // Ignore rules (per-branch)
    getIgnore: (id: number, branch: string) =>
        request.get<never, string>(`/repos/${id}/ignore`, { params: { branch } }),

    updateIgnore: (id: number, branch: string, content: string) =>
        request.put<never, void>(`/repos/${id}/ignore`, { content }, { params: { branch } }),

    getLogs: (id: number, params?: { page?: number; size?: number }) =>
        request.get<never, PageResponse<import('@/types').OperationLog>>(`/repos/${id}/logs`, { params }),

    getModels: () =>
        request.get<never, ModelsResponse>('/models'),
}
