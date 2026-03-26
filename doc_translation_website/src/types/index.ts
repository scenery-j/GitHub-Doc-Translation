// API Response wrapper
export interface ApiResponse<T> {
    code: number
    message: string
    data: T
}

export interface PageResponse<T> {
    records: T[]
    total: number
    page: number
    size: number
    pages: number
}

// Auth
export interface User {
    id: number
    username: string
    avatarUrl: string
    email: string
    freeQuota: number
    usedQuota: number
    hasOpenrouterKey: boolean
    hasInstallation: boolean
    createdAt: string
}

// Repository
export interface Repository {
    id: number
    fullName: string
    description?: string
    defaultBranch: string
    baseLanguage: string
    targetLanguages: string[]
    aiModel: string
    status: 'active' | 'paused' | 'error'
    translatedFileCount: number
    lastSyncAt: string | null
    createdAt: string
}

export interface RepositoryDetail extends Repository {
    stats: {
        totalFiles: number
        translatedFiles: number
        pendingChanges: number
        totalPRs: number
        languageProgress: LanguageProgress[]
    }
}

export interface LanguageProgress {
    language: string
    total: number
    completed: number
    percentage: number
}

export interface AvailableRepository {
    githubRepoId: number
    installationId: number
    fullName: string
    defaultBranch: string
    description: string
    starCount: number
    language: string
    isPrivate: boolean
    alreadyAdded: boolean
}

// File tree
export interface FileTreeNode {
    path: string
    type: 'file' | 'directory'
    size?: number
    children?: FileTreeNode[]
}

// Translation Config (global, per-repo)
export interface TranslationConfig {
    baseLanguage: string
    targetLanguages: string[]
    aiModel: string
}

// Branch Config (per-branch)
export interface BranchConfig {
    branchName: string
    selectedPaths: string[]
    ignorePatterns: string[]
    outputPathPattern: string
    webhookActive: boolean
    autoMergePr: boolean
}

// AI Model
export interface AIModel {
    id: string
    name: string
    contextLength: number
    inputPrice: number
    outputPrice: number
    priceUnit: string
    isFree: boolean
}

export interface RecommendedModel {
    id: string
    name: string
    tag: string
    desc: string
}

export interface ModelsResponse {
    models: AIModel[]
    recommended: {
        free: RecommendedModel[]
        paid: RecommendedModel[]
    }
}

// Translation Task
export interface TranslationTask {
    id: number
    branchName?: string
    triggerType: 'webhook' | 'manual'
    status: 'queued' | 'running' | 'completed' | 'failed' | 'cancelled'
    targetLanguages: string[]
    totalFiles: number
    completedFiles: number
    failedFiles: number
    tokensUsed: number
    estimatedCost: number
    prNumber: number | null
    prUrl: string | null
    /** PR lifecycle: open / merged / closed. Null before a PR is created. */
    prStatus: 'open' | 'merged' | 'closed' | null
    startedAt: string | null
    completedAt: string | null
    createdAt: string
}

export interface TranslationTaskDetail extends TranslationTask {
    repositoryId: number
    repositoryName: string
    errorMessage?: string
    files: TranslationFileRecord[]
}

export interface TranslationFileRecord {
    id: number
    sourcePath: string
    targetLanguage: string
    targetPath: string
    status: 'pending' | 'translating' | 'completed' | 'failed'
    tokensUsed: number
    errorMessage?: string
    translatedAt: string | null
}

// Dashboard
export interface DashboardStats {
    totalRepos: number
    totalTranslatedFiles: number
    totalLanguages: number
    monthlyTasks: number
}

export interface RecentTask {
    id: number
    repositoryId: number
    repositoryName: string
    branchName?: string
    triggerType: 'webhook' | 'manual'
    status: 'queued' | 'running' | 'completed' | 'failed' | 'cancelled'
    totalFiles: number
    completedFiles: number
    createdAt: string
}

export type PageResult<T> = PageResponse<T>

// Quota
export interface QuotaInfo {
    freeQuota: number
    usedQuota: number
    remaining: number
    hasCustomApiKey: boolean
}

export interface QuotaUsageRecord {
    id: number
    repositoryName: string
    taskId: number
    tokensUsed: number
    source: 'platform' | 'custom_key'
    createdAt: string
}

// Operation Log
export interface OperationLog {
    id: number
    action: string
    detail: string
    createdAt: string
}

// Language
export const SUPPORTED_LANGUAGES: { code: string; name: string; nativeName: string }[] = [
    { code: 'en', name: 'English', nativeName: 'English' },
    { code: 'ja', name: 'Japanese', nativeName: '日本語' },
    { code: 'ko', name: 'Korean', nativeName: '한국어' },
    { code: 'es', name: 'Spanish', nativeName: 'Español' },
    { code: 'fr', name: 'French', nativeName: 'Français' },
    { code: 'de', name: 'German', nativeName: 'Deutsch' },
    { code: 'ru', name: 'Russian', nativeName: 'Русский' },
    { code: 'pt', name: 'Portuguese', nativeName: 'Português' },
    { code: 'ar', name: 'Arabic', nativeName: 'العربية' },
    { code: 'hi', name: 'Hindi', nativeName: 'हिन्दी' },
]

export const BASE_LANGUAGES: { code: string; name: string }[] = [
    { code: 'zh', name: '中文 (Chinese)' },
    { code: 'en', name: 'English' },
    { code: 'ja', name: '日本語' },
    { code: 'ko', name: '한국어' },
]

export const LANGUAGE_NAMES: Record<string, string> = {
    zh: '中文', en: 'English', ja: '日本語', ko: '한국어',
    es: 'Español', fr: 'Français', de: 'Deutsch', ru: 'Русский',
    pt: 'Português', ar: 'العربية', hi: 'हिन्दी',
}
