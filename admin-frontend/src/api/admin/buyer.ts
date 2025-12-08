/**
 * 采购者管理API
 */

import request from '@/utils/request'

// 采购者VO
export interface BuyerVO {
  id: number
  username: string
  email: string
  realName?: string
  gender?: number
  birthday?: string
  phone?: string
  fixedPhone?: string
  operator?: string
  province?: string
  city?: string
  district?: string
  address?: string
  zipCode?: string
  securityQuestion?: string
  securityAnswer?: string
  wangwang?: string
  userLevel: number
  userLevelName?: string
  status: number
  statusName?: string
  auditStatus?: number
  auditStatusName?: string
  auditComment?: string
  auditorId?: number
  auditTime?: string
  createTime?: string
  updateTime?: string
}

// 采购者DTO
export interface BuyerDTO {
  id?: number
  userLevel?: number
  status?: number
  auditStatus: number
  auditComment?: string
}

/**
 * 获取采购者列表
 */
export const getBuyerList = (params: {
  page?: number
  pageSize?: number
  username?: string
  phone?: string
  status?: number
  userLevel?: number
}): Promise<any> => {
  return request.get('/api/admin/buyer/list', { params })
}

/**
 * 获取采购者详情
 */
export const getBuyerById = (id: number): Promise<BuyerVO> => {
  return request.get(`/api/admin/buyer/${id}`)
}

/**
 * 更新采购者信息
 */
export const updateBuyer = (id: number, data: BuyerDTO): Promise<void> => {
  return request.put(`/api/admin/buyer/${id}`, data)
}

/**
 * 更新采购者状态
 */
export const updateBuyerStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/buyer/${id}/status`, null, {
    params: { status }
  })
}

/**
 * 更新采购者等级
 */
export const updateBuyerLevel = (id: number, userLevel: number): Promise<void> => {
  return request.put(`/api/admin/buyer/${id}/level`, null, {
    params: { userLevel }
  })
}

/**
 * 审核采购者
 */
export const auditBuyer = (id: number, data: BuyerDTO): Promise<void> => {
  return request.post(`/api/admin/buyer/${id}/audit`, data)
}

/**
 * 获取待审核采购者列表
 */
export const getPendingAuditList = (params: {
  page?: number
  pageSize?: number
  username?: string
  realName?: string
  phone?: string
}): Promise<any> => {
  return request.get('/api/admin/buyer/audit/list', { params })
}


