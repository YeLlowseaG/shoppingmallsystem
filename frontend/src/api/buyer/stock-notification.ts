/**
 * 缺货登记API
 */

import request from '@/utils/request'

// 缺货登记DTO
export interface StockNotificationDTO {
  productId: number
  contactPhone?: string
  contactEmail?: string
  notifyType?: string
  remark?: string
}

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
  notifiedAt: string
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

/**
 * 创建缺货登记
 */
export const createStockNotification = (data: StockNotificationDTO): Promise<number> => {
  return request.post('/api/buyer/stock-notification', data)
}

/**
 * 取消缺货登记
 */
export const cancelStockNotification = (id: number): Promise<void> => {
  return request.delete(`/api/buyer/stock-notification/${id}`)
}

/**
 * 获取用户的缺货登记列表
 */
export const getStockNotificationList = (
  current: number,
  size: number
): Promise<PageResponse<StockNotificationVO>> => {
  return request.get('/api/buyer/stock-notification/list', {
    params: { current, size }
  })
}

/**
 * 检查是否已登记某商品
 */
export const checkStockNotificationRegistered = (productId: number): Promise<boolean> => {
  return request.get(`/api/buyer/stock-notification/check/${productId}`)
}