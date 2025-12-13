/**
 * 商品收藏API
 */

import request from '@/utils/request'

// 收藏VO
export interface FavoriteVO {
  id: number
  productId: number
  productName: string
  mainImage: string
  basePrice: number
  userLevelPrice: number
  stock: number
  salesCount: number
  status: string
  createTime: string
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
 * 添加收藏
 */
export const addFavorite = (productId: number): Promise<string> => {
  return request.post('/api/buyer/favorites', { productId })
}

/**
 * 取消收藏
 */
export const removeFavorite = (productId: number): Promise<string> => {
  return request.delete(`/api/buyer/favorites/${productId}`)
}

/**
 * 检查是否已收藏
 */
export const checkFavorite = (productId: number): Promise<boolean> => {
  return request.get(`/api/buyer/favorites/check/${productId}`)
}

/**
 * 分页查询用户收藏
 */
export const getFavoritePage = (
  current: number = 1,
  size: number = 10
): Promise<PageResponse<FavoriteVO>> => {
  return request.get('/api/buyer/favorites/page', {
    params: { current, size }
  })
}