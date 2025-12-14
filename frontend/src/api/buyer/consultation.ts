/**
 * 买家端咨询API
 */

import request from '@/utils/request'

// 咨询接口
export interface ConsultationDTO {
  productId: number
  contactName: string
  contactPhone?: string
  contactEmail?: string
  consultationContent: string
}

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
  createTime: string
  updateTime: string
}

/**
 * 提交咨询
 */
export const submitConsultation = (data: ConsultationDTO): Promise<void> => {
  return request.post('/api/buyer/consultation', data)
}

/**
 * 获取我的咨询列表
 */
export const getMyConsultations = (
  current: number,
  size: number
): Promise<{ records: ConsultationVO[]; total: number }> => {
  return request.get('/api/buyer/consultation/my', {
    params: { current, size }
  })
}

/**
 * 获取咨询详情
 */
export const getConsultationById = (consultationId: number): Promise<ConsultationVO> => {
  return request.get(`/api/buyer/consultation/${consultationId}`)
}