import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 响应数据接口
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 自定义参数序列化函数，处理数组参数
const paramsSerializer = (params: any): string => {
  const searchParams = new URLSearchParams()
  
  Object.keys(params || {}).forEach(key => {
    const value = params[key]
    if (value === null || value === undefined) {
      return
    }
    
    if (Array.isArray(value)) {
      // 数组参数：roleIds=1&roleIds=2
      value.forEach(item => {
        if (item !== null && item !== undefined) {
          searchParams.append(key, String(item))
        }
      })
    } else {
      searchParams.append(key, String(value))
    }
  })
  
  return searchParams.toString()
}

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
  localStorage.removeItem('admin_token')
  
  // 使用 window.location.href 强制跳转，立即中断当前页面执行
  // 这样可以避免业务代码继续执行并显示错误信息
  window.location.href = '/admin/login'
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 90000, // 默认90秒超时（支付/退款接口可能需要更长时间）
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  },
  paramsSerializer: {
    serialize: paramsSerializer
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 如果正在跳转到登录页，取消所有后续请求，避免死循环
    if (isRedirectingToLogin) {
      return Promise.reject(new Error('正在跳转到登录页，请求已取消'))
    }
    
    // 从localStorage获取token
    const token = localStorage.getItem('admin_token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 禁用GET请求的浏览器缓存，确保获取最新数据
    if (config.method?.toLowerCase() === 'get') {
      // 添加时间戳参数，防止缓存
      if (!config.params) {
        config.params = {}
      }
      config.params._t = Date.now()
      
      // 设置请求头，禁用缓存
      if (config.headers) {
        config.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
        config.headers['Pragma'] = 'no-cache'
        config.headers['Expires'] = '0'
      }
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
      // 返回data字段，这样API调用时可以直接使用返回的数据
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
      return Promise.reject(new Error(res.message || '请求失败'))
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
            localStorage.removeItem('admin_token')
            router.push('/admin/login')
          }
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败: ${status}`)
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default service


