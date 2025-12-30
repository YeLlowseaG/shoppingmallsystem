/**
 * API响应接口
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 分页请求参数
 */
export interface PageParams {
  page: number
  pageSize: number
}

/**
 * 分页响应数据
 */
export interface PageResult<T = any> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
































































