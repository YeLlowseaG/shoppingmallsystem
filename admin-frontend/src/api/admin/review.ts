/**
 * 管理端评价API
 */

import request from '@/utils/request'

// 评价接口
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
  createdTime: string
  updatedTime: string
}

export interface ReviewAuditDTO {
  status: number
}

export interface ReviewReplyDTO {
  adminReply: string
}

/**
 * 分页获取评价列表
 */
export const getReviewPage = (
  current: number,
  size: number,
  productName?: string,
  userName?: string,
  rating?: number,
  status?: number
): Promise<{ records: ProductReviewVO[]; total: number }> => {
  return request.get('/api/admin/review/page', {
    params: {
      current,
      size,
      productName,
      userName,
      rating,
      status
    }
  })
}

/**
 * 获取评价详情
 */
export const getReviewById = (reviewId: number): Promise<ProductReviewVO> => {
  return request.get(`/api/admin/review/${reviewId}`)
}

/**
 * 审核评价
 */
export const auditReview = (
  reviewId: number,
  data: ReviewAuditDTO
): Promise<void> => {
  return request.put(`/api/admin/review/${reviewId}/audit`, data)
}

/**
 * 回复评价
 */
export const replyReview = (
  reviewId: number,
  data: ReviewReplyDTO
): Promise<void> => {
  return request.put(`/api/admin/review/${reviewId}/reply`, data)
}

/**
 * 删除评价
 */
export const deleteReview = (reviewId: number): Promise<void> => {
  return request.delete(`/api/admin/review/${reviewId}`)
}