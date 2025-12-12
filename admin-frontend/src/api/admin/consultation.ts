/**
 * 管理端咨询API
 */

import request from '@/utils/request'

// 咨询接口
export interface ConsultationVO {
  id: number
  productId: number
  productName: string
  productImage: string
  userId?: number
  userName?: string
  contactName: string
  contactPhone?: string
  contactEmail?: string
  consultationContent: string
  replyContent?: string
  status: number
  statusText: string
  replyTime?: string
  replyAdminId?: number
  replyAdminName?: string
  createdTime: string
  updatedTime: string
}

export interface ConsultationReplyDTO {
  replyContent: string
}

/**
 * 分页获取咨询列表
 */
export const getConsultationPage = (
  current: number,
  size: number,
  productName?: string,
  contactName?: string,
  status?: number
): Promise<{ records: ConsultationVO[]; total: number }> => {
  return request.get('/api/admin/consultation/page', {
    params: {
      current,
      size,
      productName,
      contactName,
      status
    }
  })
}

/**
 * 获取咨询详情
 */
export const getConsultationById = (consultationId: number): Promise<ConsultationVO> => {
  return request.get(`/api/admin/consultation/${consultationId}`)
}

/**
 * 回复咨询
 */
export const replyConsultation = (
  consultationId: number,
  data: ConsultationReplyDTO
): Promise<void> => {
  return request.put(`/api/admin/consultation/${consultationId}/reply`, data)
}

/**
 * 更新咨询状态
 */
export const updateConsultationStatus = (
  consultationId: number,
  status: number
): Promise<void> => {
  return request.put(`/api/admin/consultation/${consultationId}/status`, null, {
    params: { status }
  })
}

/**
 * 删除咨询
 */
export const deleteConsultation = (consultationId: number): Promise<void> => {
  return request.delete(`/api/admin/consultation/${consultationId}`)
}