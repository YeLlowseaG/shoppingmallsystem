/**
 * 管理端 - 缺货登记API
 */

import request from '@/utils/request'

// 缺货登记VO
export interface StockNotificationVO {
  id: number
  userId: number
  productId: number
  productName: string
  productCode: string
  mainImage: string
  basePrice: number
  contactPhone: string
  contactEmail: string
  status: number
  statusDesc: string
  notifyType: string
  notifiedAt: string | null
  expiredAt: string
  remark: string
  createTime: string
  updateTime: string
}

// 分页响应
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 查询参数
export interface StockNotificationQuery {
  current: number
  size: number
  productName?: string
  status?: number
}

/**
 * 分页查询所有缺货登记
 */
export const getStockNotificationPage = (params: StockNotificationQuery): Promise<PageResponse<StockNotificationVO>> => {
  return request.get('/api/admin/stock-notification/page', { params })
}

/**
 * 商品补货后通知用户
 */
export const notifyStockUsers = (productId: number): Promise<void> => {
  return request.post(`/api/admin/stock-notification/notify/${productId}`)
}
