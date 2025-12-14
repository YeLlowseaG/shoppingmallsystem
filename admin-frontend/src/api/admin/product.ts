/**
 * 商品管理API
 */

import request from '@/utils/request'

// 商品DTO
export interface ProductDTO {
  id?: number
  productCode: string
  productName: string
  categoryId: number
  brandId?: number | null
  mainImage?: string
  images?: string
  description?: string
  basePrice: number
  marketPrice?: number
  costPrice?: number
  stock: number
  warningStock?: number
  weight?: number
  status?: string
  enableSpec?: boolean
}

// 商品VO
export interface ProductVO {
  id: number
  productCode: string
  productName: string
  categoryId: number
  categoryName: string
  brandId?: number | null
  brandName?: string
  mainImage: string
  imageList: string[]
  description: string
  basePrice: number
  marketPrice?: number
  costPrice?: number
  userLevelPrice: number
  stock: number
  warningStock?: number
  weight?: number
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
  status?: string,
  sortBy?: string
): Promise<PageResponse<ProductVO>> => {
  return request.get('/api/admin/product/page', {
    params: { current, size, categoryId, keyword, brand, status, sortBy }
  })
}

/**
 * 根据ID获取商品详情
 */
export const getProductById = (id: number): Promise<ProductVO> => {
  return request.get(`/api/admin/product/${id}`)
}

/**
 * 创建商品
 */
export const createProduct = (data: ProductDTO): Promise<number> => {
  return request.post('/api/admin/product', data)
}

/**
 * 更新商品
 */
export const updateProduct = (data: ProductDTO): Promise<void> => {
  return request.put('/api/admin/product', data)
}

/**
 * 删除商品
 */
export const deleteProduct = (id: number): Promise<void> => {
  return request.delete(`/api/admin/product/${id}`)
}

/**
 * 更新商品状态
 */
export const updateProductStatus = (id: number, status: string): Promise<void> => {
  return request.put(`/api/admin/product/${id}/status`, null, { params: { status } })
}
