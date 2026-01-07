import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 购物车商品VO
 */
export interface CartVO {
  id: number
  productId: number
  productCode: string
  name: string
  image: string
  salesPrice: number
  memberPrice: number
  quantity: number
  weight: number
  specText?: string
  selected?: boolean
  isMember?: number // 用户是否是会员（0-普通用户，1-会员）
  shippingTemplateId?: number // 运费模板ID（为空表示包邮）
  stock?: number // 商品库存（可选，如果后端返回则使用）
  skuId?: number // SKU ID（可选，如果商品有规格）
}

/**
 * 添加商品到购物车DTO
 */
export interface AddCartDTO {
  productId?: number
  productCode?: string
  quantity: number
  skuId?: number
  specCombination?: string
}

/**
 * 获取购物车列表
 */
export const getCartList = (): Promise<CartVO[]> => {
  return request.get('/api/buyer/cart')
}

/**
 * 添加商品到购物车
 */
export const addToCart = (data: AddCartDTO): Promise<ApiResponse<number>> => {
  return request.post('/api/buyer/cart', data)
}

/**
 * 通过货号添加商品到购物车
 */
export const addToCartByCode = (productCode: string, quantity: number): Promise<ApiResponse<number>> => {
  return request.post('/api/buyer/cart/add-by-code', null, {
    params: { productCode, quantity }
  })
}

/**
 * 更新购物车商品数量
 */
export const updateCartQuantity = (id: number, quantity: number): Promise<ApiResponse> => {
  return request.put(`/api/buyer/cart/${id}`, null, {
    params: { quantity }
  })
}

/**
 * 删除购物车商品
 */
export const deleteCartItem = (id: number): Promise<ApiResponse> => {
  return request.delete(`/api/buyer/cart/${id}`)
}

/**
 * 批量删除购物车商品
 */
export const batchDeleteCartItems = (ids: number[]): Promise<ApiResponse> => {
  return request.delete('/api/buyer/cart/batch', { data: ids })
}

/**
 * 清空购物车
 */
export const clearCart = (): Promise<ApiResponse> => {
  return request.delete('/api/buyer/cart/clear')
}

/**
 * 获取购物车商品数量统计
 */
export const getCartItemCount = (): Promise<number> => {
  return request.get('/api/buyer/cart/count')
}



