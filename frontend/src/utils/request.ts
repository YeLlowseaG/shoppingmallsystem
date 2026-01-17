import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 响应数据接口
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 全局标志，防止重复跳转和重复显示错误提示
let isRedirectingToLogin = false

// 判断是否为token相关错误
const isTokenError = (code: number, message?: string): boolean => {
  if (code !== 401) return false
  if (!message) return false
  const tokenErrorKeywords = ['Token已过期', 'Token无效', '未登录', '请先登录', '请重新登录']
  return tokenErrorKeywords.some(keyword => message.includes(keyword))
}

// 处理token错误并跳转登录页（静默处理，不显示错误提示）
const handleTokenError = () => {
  // 防止重复跳转
  if (isRedirectingToLogin) {
    return
  }
  
  isRedirectingToLogin = true
  localStorage.removeItem('token')
  
  // 使用 window.location.href 强制跳转，立即中断当前页面执行
  // 这样可以避免业务代码继续执行并显示错误信息
  window.location.href = '/login'
}

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 如果正在跳转到登录页，取消所有后续请求，避免死循环
    if (isRedirectingToLogin) {
      return Promise.reject(new Error('正在跳转到登录页，请求已取消'))
    }
    
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data

    // 如果返回的状态码为200，说明接口请求成功
    if (res.code === 200) {
      return res.data
    } else {
      // 如果是token相关错误，静默跳转登录页，不显示错误提示
      if (isTokenError(res.code, res.message)) {
        handleTokenError()
        // 创建一个特殊的错误对象，标记为已处理
        const error = new Error(res.message || 'Token已过期')
        ;(error as any).__tokenError = true
        ;(error as any).__messageShown = true
        return Promise.reject(error)
      }
      // 其他状态码，显示错误信息
      ElMessage.error(res.message || '请求失败')
      // 标记错误已经显示过，避免业务代码重复显示
      const error = new Error(res.message || '请求失败')
      ;(error as any).__messageShown = true
      return Promise.reject(error)
    }
  },
  (error) => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          // 检查是否是token相关错误
          if (isTokenError(status, data?.message)) {
            // token相关错误，静默跳转登录页，不显示错误提示
            handleTokenError()
            // 标记为token错误，已处理
            ;(error as any).__tokenError = true
            ;(error as any).__messageShown = true
          } else {
            // 其他401错误，显示错误提示并跳转
            ElMessage.error('未授权，请重新登录')
            localStorage.removeItem('token')
            router.push('/login')
            ;(error as any).__messageShown = true
          }
          break
        case 403:
          ElMessage.error('拒绝访问')
          ;(error as any).__messageShown = true
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          ;(error as any).__messageShown = true
          break
        case 500:
          ElMessage.error('服务器内部错误')
          ;(error as any).__messageShown = true
          break
        default:
          ElMessage.error(data?.message || `请求失败: ${status}`)
          ;(error as any).__messageShown = true
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
      ;(error as any).__messageShown = true
    } else {
      ElMessage.error('请求配置错误')
      ;(error as any).__messageShown = true
    }
    
    return Promise.reject(error)
  }
)

export default service








