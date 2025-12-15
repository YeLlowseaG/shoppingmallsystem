/**
 * 买家端评价API
 */

import request from '@/utils/request'

// 评价接口
export interface ProductReviewDTO {
  productId: number
  orderId: number
  rating: number
  reviewContent?: string
  reviewImages?: string[]
}

export interface ProductReviewVO {
  id: number
  productId: number
  productName: string
  productImage: string
  orderId: number
  orderNumber: string
  userId: number
  userName: string
  rating: number
  reviewContent?: string
  reviewImages?: string[]
  adminReply?: string
  status: number
  statusText: string
  createTime: string
  updateTime: string
}

/**
 * 提交商品评价
 */
export const submitReview = (data: ProductReviewDTO): Promise<void> => {
  return request.post('/api/buyer/review', data)
}

/**
 * 获取我的评价列表
 */
export const getMyReviews = (
  current: number,
  size: number
): Promise<{ records: ProductReviewVO[]; total: number }> => {
  return request.get('/api/buyer/review/my', {
    params: { current, size }
  })
}

/**
 * 获取评价详情
 */
export const getReviewById = (reviewId: number): Promise<ProductReviewVO> => {
  return request.get(`/api/buyer/review/${reviewId}`)
}

/**
 * 检查是否可以评价
 */
export const canReviewProduct = (
  orderId: number,
  productId: number
): Promise<boolean> => {
  return request.get('/api/buyer/review/can-review', {
    params: { orderId, productId }
  })
}