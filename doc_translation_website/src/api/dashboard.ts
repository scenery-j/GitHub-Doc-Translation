import request from './index'
import type { DashboardStats, PageResult, RecentTask } from '@/types'

export const dashboardApi = {
    getStats: () =>
        request.get<never, DashboardStats>('/dashboard/stats'),

    getRecentTasks: (page = 1, size = 10) =>
        request.get<never, PageResult<RecentTask>>('/dashboard/recent-tasks', { params: { page, size } }),
}
