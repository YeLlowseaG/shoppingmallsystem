/**
 * 订单问题/消息API
 */

import request from '@/utils/request'

/**
 * 创建订单问题/消息DTO
 */
export interface OrderMessageDTO {
  orderNumber: string
  messageType: 'paid' | 'question'
  title?: string
  content?: string
  paymentAmount?: string
  paymentMethod?: string
  paymentDate?: string
  paymentHour?: number
  paymentMinute?: number
  remarks?: string
}

/**
 * 创建订单问题/消息
 */
export const createOrderMessage = (data: OrderMessageDTO): Promise<number> => {
  return request.post('/api/buyer/order-messages', data)
}








