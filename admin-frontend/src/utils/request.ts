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

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000,
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
      // 401未登录，特殊处理
      if (res.code === 401) {
        console.warn('未登录或登录已过期')
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('admin_token')
        return Promise.reject(new Error('未登录'))
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
          ElMessage.error('未授权，请重新登录')
          localStorage.removeItem('admin_token')
          router.push('/admin/login')
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


