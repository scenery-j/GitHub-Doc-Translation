import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
    {
        path: '/',
        component: () => import('@/views/landing/LandingPage.vue'),
        meta: { requiresAuth: false, guestOnly: true },
    },
    {
        path: '/login',
        component: () => import('@/views/login/LoginPage.vue'),
        meta: { requiresAuth: false, guestOnly: true },
    },
    {
        path: '/setup',
        component: () => import('@/views/setup/SetupPage.vue'),
        meta: { requiresAuth: true },
    },
    {
        path: '/dashboard',
        component: () => import('@/views/dashboard/DashboardPage.vue'),
        meta: { requiresAuth: true, layout: 'app' },
    },
    {
        path: '/repos',
        component: () => import('@/views/repos/RepoList.vue'),
        meta: { requiresAuth: true, layout: 'app' },
    },
    {
        path: '/repos/:id',
        component: () => import('@/views/repos/RepoDetail.vue'),
        meta: { requiresAuth: true, layout: 'app' },
    },
    {
        path: '/repos/:id/config',
        component: () => import('@/views/repos/RepoConfig.vue'),
        meta: { requiresAuth: true, layout: 'app' },
    },
    {
        path: '/repos/:id/tasks/:taskId',
        component: () => import('@/views/tasks/TaskDetail.vue'),
        meta: { requiresAuth: true, layout: 'app' },
    },
    {
        path: '/settings',
        component: () => import('@/views/settings/SettingsPage.vue'),
        meta: { requiresAuth: true, layout: 'app' },
        children: [
            { path: '', redirect: '/settings/profile' },
            { path: 'profile', component: () => import('@/views/settings/ProfileSettings.vue') },
            { path: 'api-key', component: () => import('@/views/settings/ApiKeySettings.vue') },
            { path: 'quota', component: () => import('@/views/settings/QuotaSettings.vue') },
        ],
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/',
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { top: 0 }
    },
})
// 路由守卫
router.beforeEach(async (to, _from, next) => {
    const authStore = useAuthStore()
    // 检查本地是否存在 token
    const token = localStorage.getItem('token')
    if (token && !authStore.user) {
        await authStore.fetchUser()
    }

    // Not logged in → landing page
    if (to.meta.requiresAuth && !authStore.isLoggedIn) {
        next('/')
        return
    }

    // Logged-in users on guest-only pages → dashboard (or setup if not installed)
    if (to.meta.guestOnly && authStore.isLoggedIn) {
        next(authStore.user?.hasInstallation ? '/dashboard' : '/setup')
        return
    }

    // Already installed → skip /setup
    if (to.path === '/setup' && authStore.user?.hasInstallation) {
        next('/dashboard')
        return
    }

    // Logged-in but GitHub App not installed → must go through /setup
    if (
        to.meta.requiresAuth &&
        authStore.isLoggedIn &&
        !authStore.user?.hasInstallation &&
        to.path !== '/setup'
    ) {
        next('/setup')
        return
    }

    next()
})

export default router
