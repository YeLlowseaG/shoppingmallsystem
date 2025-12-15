import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 消息VO
 */
export interface MessageVO {
  id: number
  senderId?: number
  senderName?: string
  receiverId: number
  title: string
  content: string
  messageType: number // 0-普通，1-系统，2-订单，3-其他
  isRead: boolean
  orderNo?: string
  createTime: string
}

/**
 * 消息查询DTO
 */
export interface MessageQueryDTO {
  pageNum?: number
  pageSize?: number
  messageType?: number
  isRead?: boolean
}

/**
 * 消息分页响应VO
 */
export interface MessagePageVO {
  records: MessageVO[]
  total: number
  unreadCount: number
}

/**
 * 获取收件箱消息列表
 */
export const getInboxMessages = (params?: MessageQueryDTO): Promise<MessagePageVO> => {
  return request.get('/api/buyer/messages/inbox', { params })
}

/**
 * 标记消息为已读
 */
export const markMessageAsRead = (messageId: number): Promise<ApiResponse<void>> => {
  return request.put(`/api/buyer/messages/${messageId}/read`)
}

/**
 * 全部标记为已读
 */
export const markAllAsRead = (): Promise<ApiResponse<void>> => {
  return request.put('/api/buyer/messages/read-all')
}

/**
 * 删除消息
 */
export const deleteMessage = (messageId: number): Promise<ApiResponse<void>> => {
  return request.delete(`/api/buyer/messages/${messageId}`)
}

/**
 * 获取未读消息数量
 */
export const getUnreadCount = (): Promise<ApiResponse<number>> => {
  return request.get('/api/buyer/messages/unread-count')
}


