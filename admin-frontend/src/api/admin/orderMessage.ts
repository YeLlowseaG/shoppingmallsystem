/**
 * 订单问题/消息管理API
 */

import request from '@/utils/request'

/**
 * 订单问题/消息VO
 */
export interface OrderMessageVO {
  id: number
  orderNo: string
  userId: number
  username?: string
  realName?: string
  messageType: number
  messageTypeText: string
  title?: string
  content?: string
  paymentAmount?: number
  paymentMethod?: string
  paymentDate?: string
  paymentTime?: string
  remarks?: string
  status: number
  statusText: string
  handlerId?: number
  handlerName?: string
  handleTime?: string
  handleRemark?: string
  createTime: string
  updateTime: string
}

/**
 * 订单问题查询DTO
 */
export interface OrderMessageQueryDTO {
  orderNo?: string
  userId?: number
  messageType?: number
  status?: number
  startDate?: string
  endDate?: string
  current?: number
  size?: number
}

/**
 * 订单问题处理DTO
 */
export interface OrderMessageHandleDTO {
  id: number
  status: number
  handleRemark?: string
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 分页查询订单问题列表
 */
export const getOrderMessagePage = (
  params: OrderMessageQueryDTO
): Promise<PageResponse<OrderMessageVO>> => {
  return request.get('/api/admin/order-messages/page', { params })
}

/**
 * 根据ID获取订单问题详情
 */
export const getOrderMessageById = (id: number): Promise<OrderMessageVO> => {
  return request.get(`/api/admin/order-messages/${id}`)
}

/**
 * 处理订单问题
 */
export const handleOrderMessage = (data: OrderMessageHandleDTO): Promise<void> => {
  return request.put('/api/admin/order-messages/handle', data)
}











































