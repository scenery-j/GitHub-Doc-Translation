import type { AxiosResponse } from 'axios'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'

// Guard to avoid triggering multiple redirects
let redirectingToInstall = false

async function redirectToInstallApp() {
    if (redirectingToInstall) return
    redirectingToInstall = true
    ElMessage.warning('GitHub App 授权已失效，正在跳转到安装页面...')
    try {
        const res = await fetch('/api/auth/app-info', {
            headers: { Authorization: `Bearer ${localStorage.getItem('token') ?? ''}` },
        })
        const json = await res.json()
        const installUrl: string = json?.data?.installUrl ?? 'https://github.com/apps'
        setTimeout(() => {
            window.location.href = installUrl
        }, 1500)
    } catch {
        redirectingToInstall = false
    }
}

const request = axios.create({
    baseURL: '/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
    },
})

/**
 * 请求拦截器, 添加 token
 */
request.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

/**
 * 响应拦截器, 处理错误
 */
request.interceptors.response.use(
    (response: AxiosResponse<ApiResponse<unknown>>) => {
        const { data } = response
        if (data.code === 200) {
            return data.data as any
        }
        if (data.code === 40101) {
            localStorage.removeItem('token')
            window.location.href = '/login'
            return Promise.reject(new Error('未登录或登录已过期'))
        }
        if (data.code === 40010) {
            redirectToInstallApp()
            return Promise.reject(new Error(data.message || 'GitHub App 授权失效'))
        }
        ElMessage.error(data.message || '请求失败')
        return Promise.reject(new Error(data.message || '请求失败'))
    },
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            window.location.href = '/login'
        } else if (error.response?.status === 403) {
            ElMessage.error('无权操作')
        } else if (error.response?.status >= 500) {
            ElMessage.error('服务器错误，请稍后重试')
        } else if (error.code === 'ECONNABORTED') {
            ElMessage.error('请求超时，请稍后重试')
        } else if (!error.response) {
            ElMessage.error('网络连接失败，请检查网络')
        }
        return Promise.reject(error)
    }
)

export default request
