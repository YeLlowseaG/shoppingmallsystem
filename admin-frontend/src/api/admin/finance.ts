import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 支付记录查询DTO
 */
export interface PaymentRecordQueryDTO {
  pageNum?: number
  pageSize?: number
  orderNo?: string
  paymentNo?: string
  paymentMethod?: string
  paymentStatus?: number
  startTime?: string
  endTime?: string
}

/**
 * 支付记录VO
 */
export interface PaymentRecordVO {
  id: number
  orderId: number
  orderNo: string
  userId: number
  username: string
  paymentNo: string
  paymentMethod: string
  paymentMethodName: string
  amount: number
  refundedAmount: number
  refundableAmount: number
  paymentStatus: number
  paymentStatusName: string
  paymentTime: string
  refundTime?: string
  refundReason?: string
  refundOperatorName?: string
  createTime: string
}

/**
 * 退款请求DTO
 */
export interface RefundRequestDTO {
  paymentRecordId?: number
  depositDetailId?: number
  refundAmount: number
  refundReason: string
}

/**
 * 分页查询支付记录
 */
export const getPaymentRecordList = (params?: PaymentRecordQueryDTO): Promise<{
  records: PaymentRecordVO[]
  total: number
  size: number
  current: number
  pages: number
}> => {
  return request.get('/api/admin/finance/payment-records', { params })
}

/**
 * 根据ID获取支付记录详情
 */
export const getPaymentRecordById = (id: number): Promise<PaymentRecordVO> => {
  return request.get(`/api/admin/finance/payment-record/${id}`)
}

/**
 * 支付记录退款
 */
export const refundPaymentRecord = (data: RefundRequestDTO): Promise<void> => {
  return request.post('/api/admin/finance/payment-record/refund', data)
}



















