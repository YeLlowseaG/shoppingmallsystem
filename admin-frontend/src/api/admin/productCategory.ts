/**
 * 商品分类管理API
 */

import request from '@/utils/request'

// 商品分类DTO
export interface ProductCategoryDTO {
  id?: number
  parentId: number
  categoryName: string
  level: number
  sortOrder?: number
  status?: number
}

// 商品分类VO
export interface ProductCategoryVO {
  id: number
  parentId: number
  categoryName: string
  level: number
  sortOrder: number
  status: number
  createTime: string
  updateTime: string
  children?: ProductCategoryVO[]
}

/**
 * 获取分类树
 */
export const getCategoryTree = (): Promise<ProductCategoryVO[]> => {
  return request.get('/api/admin/product-category/tree')
}

/**
 * 根据父分类ID获取子分类
 */
export const getChildCategories = (parentId: number): Promise<ProductCategoryVO[]> => {
  return request.get(`/api/admin/product-category/children/${parentId}`)
}

/**
 * 根据ID获取分类详情
 */
export const getCategoryById = (id: number): Promise<ProductCategoryVO> => {
  return request.get(`/api/admin/product-category/${id}`)
}

/**
 * 创建分类
 */
export const createCategory = (data: ProductCategoryDTO): Promise<number> => {
  return request.post('/api/admin/product-category', data)
}

/**
 * 更新分类
 */
export const updateCategory = (data: ProductCategoryDTO): Promise<void> => {
  return request.put('/api/admin/product-category', data)
}

/**
 * 删除分类
 */
export const deleteCategory = (id: number): Promise<void> => {
  return request.delete(`/api/admin/product-category/${id}`)
}

/**
 * 更新分类状态
 */
export const updateCategoryStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/product-category/${id}/status`, null, { params: { status } })
}
