import request from './index'
import type { User } from '@/types'

export interface AppInfo {
    appSlug: string
    installUrl: string
}

export const authApi = {
    getMe: () => request.get<never, User>('/auth/me'),
    logout: () => request.post<never, void>('/auth/logout'),
    getGitHubLoginUrl: () => '/api/auth/github',
    getAppInfo: () => request.get<never, AppInfo>('/auth/app-info'),
}
