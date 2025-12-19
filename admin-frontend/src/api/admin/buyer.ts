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
  isMember?: number
  memberLevelId?: number
  memberLevelName?: string
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
  isMember?: number
  memberLevelId?: number
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
  isMember?: number
  memberLevelId?: number
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
 * 更新采购者会员信息
 */
export const updateBuyerMemberInfo = (id: number, isMember?: number, memberLevelId?: number): Promise<void> => {
  return request.put(`/api/admin/buyer/${id}/member`, null, {
    params: { isMember, memberLevelId }
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

/**
 * 重置会员密码
 */
export const resetBuyerPassword = (id: number, password: string): Promise<void> => {
  return request.put(`/api/admin/buyer/${id}/reset-password`, null, {
    params: { password }
  })
}
