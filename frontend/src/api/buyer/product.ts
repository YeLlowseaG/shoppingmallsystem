/**
 * 买家端商品API
 */

import request from '@/utils/request'

// 商品VO
export interface ProductVO {
  id: number
  productCode: string
  productName: string
  categoryId: number
  categoryName: string
  brandName?: string
  mainImage: string
  imageList: string[]
  description: string
  basePrice: number
  salePrice?: number
  marketPrice?: number
  costPrice?: number
  userLevelPrice: number
  memberPrice?: number
  stock: number
  salesCount: number
  status: string
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
 * 分页查询商品列表
 */
export const getProductPage = (
  current: number,
  size: number,
  categoryId?: number,
  keyword?: string,
  brand?: string,
  sortBy?: string
): Promise<PageResponse<ProductVO>> => {
  return request.get('/api/buyer/product/page', {
    params: { current, size, categoryId, keyword, brand, sortBy }
  })
}

/**
 * 获取热门商品
 */
export const getHotProducts = (limit: number = 4): Promise<ProductVO[]> => {
  return request.get('/api/buyer/product/hot', {
    params: { limit }
  })
}

/**
 * 根据分类获取推荐商品
 */
export const getRecommendProducts = (
  categoryId: number,
  limit: number = 6
): Promise<ProductVO[]> => {
  return request.get(`/api/buyer/product/recommend/${categoryId}`, {
    params: { limit }
  })
}

/**
 * 根据ID获取商品详情
 */
export const getProductById = (id: number): Promise<ProductVO> => {
  return request.get(`/api/buyer/product/${id}`)
}
