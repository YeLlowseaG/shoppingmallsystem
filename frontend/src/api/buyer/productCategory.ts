/**
 * 商品分类API（买家端）
 */

import request from '@/utils/request'

// 商品分类接口
export interface ProductCategoryVO {
  id: number
  categoryName: string
  parentId: number
  level: number
  sortOrder: number
  children?: ProductCategoryVO[]
}

/**
 * 获取分类树
 */
export const getCategoryTree = (): Promise<ProductCategoryVO[]> => {
  return request.get('/api/buyer/product-category/tree')
}

/**
 * 根据父分类ID获取子分类
 */
export const getChildCategories = (parentId: number): Promise<ProductCategoryVO[]> => {
  return request.get(`/api/buyer/product-category/children/${parentId}`)
}

/**
 * 根据ID获取分类详情
 */
export const getCategoryById = (id: number): Promise<ProductCategoryVO> => {
  return request.get(`/api/buyer/product-category/${id}`)
}
