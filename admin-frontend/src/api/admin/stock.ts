/**
 * 库存管理API
 */

import request from '@/utils/request'

// 库存VO
export interface StockVO {
  id: number
  productId: number
  productCode: string
  productName: string
  mainImage?: string
  productStatus?: number // 商品状态（0-下架，1-上架）
  availableStock: number
  lockedStock: number
  totalStock: number
  warningThreshold: number
  isWarning: boolean
  updateTime: string
}

// 库存调整DTO
export interface StockDTO {
  productId: number
  adjustQuantity: number
  reason?: string
  warningThreshold?: number
}

// 库存统计VO
export interface StockStatisticsVO {
  totalProducts: number
  totalStock: number
  totalAvailableStock: number
  totalLockedStock: number
  warningProductCount: number
  outOfStockCount: number
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
 * 分页查询库存列表
 */
export const getStockPage = (
  current: number,
  size: number,
  productId?: number,
  productCode?: string,
  productName?: string,
  onlyWarning?: boolean
): Promise<PageResponse<StockVO>> => {
  return request.get('/api/admin/stock/page', {
    params: { current, size, productId, productCode, productName, onlyWarning }
  })
}

/**
 * 根据商品ID获取库存信息
 */
export const getStockByProductId = (productId: number): Promise<StockVO> => {
  return request.get(`/api/admin/stock/product/${productId}`)
}

/**
 * 调整库存
 */
export const adjustStock = (data: StockDTO): Promise<void> => {
  return request.post('/api/admin/stock/adjust', data)
}

/**
 * 更新预警阈值
 */
export const updateWarningThreshold = (
  productId: number,
  warningThreshold: number
): Promise<void> => {
  return request.put('/api/admin/stock/warning-threshold', null, {
    params: { productId, warningThreshold }
  })
}

/**
 * 获取库存预警列表
 */
export const getWarningStockPage = (
  current: number,
  size: number
): Promise<PageResponse<StockVO>> => {
  return request.get('/api/admin/stock/warning/page', {
    params: { current, size }
  })
}

/**
 * 获取库存统计信息
 */
export const getStockStatistics = (): Promise<StockStatisticsVO> => {
  return request.get('/api/admin/stock/statistics')
}

