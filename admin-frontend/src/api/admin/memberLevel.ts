/**
 * 会员等级管理API
 */

import request from '@/utils/request'

// 会员等级VO
export interface MemberLevelVO {
  id: number
  levelName: string
  minPoints: number
  maxPoints?: number
  pointsRangeText?: string
  discountRate: number
  discountRateText?: string
  sortOrder: number
  status: number
  statusName?: string
  description?: string
  createTime?: string
  updateTime?: string
}

// 会员等级DTO
export interface MemberLevelDTO {
  id?: number
  levelName: string
  minPoints: number
  maxPoints?: number
  discountRate: number
  sortOrder: number
  status: number
  description?: string
}

/**
 * 获取会员等级列表（分页）
 */
export const getMemberLevelPage = (params: {
  page?: number
  pageSize?: number
  levelName?: string
  status?: number
}): Promise<any> => {
  return request.get('/api/admin/member/level/page', { params })
}

/**
 * 获取所有启用的会员等级列表
 */
export const getAllEnabledMemberLevels = (): Promise<MemberLevelVO[]> => {
  return request.get('/api/admin/member/level/all')
}

/**
 * 获取会员等级详情
 */
export const getMemberLevelById = (id: number): Promise<MemberLevelVO> => {
  return request.get(`/api/admin/member/level/${id}`)
}

/**
 * 根据积分获取对应的会员等级
 */
export const getMemberLevelByPoints = (points: number): Promise<MemberLevelVO> => {
  return request.get('/api/admin/member/level/by-points', {
    params: { points }
  })
}

/**
 * 创建会员等级
 */
export const createMemberLevel = (data: MemberLevelDTO): Promise<number> => {
  return request.post('/api/admin/member/level', data)
}

/**
 * 更新会员等级
 */
export const updateMemberLevel = (data: MemberLevelDTO): Promise<void> => {
  return request.put('/api/admin/member/level', data)
}

/**
 * 删除会员等级
 */
export const deleteMemberLevel = (id: number): Promise<void> => {
  return request.delete(`/api/admin/member/level/${id}`)
}

/**
 * 更新会员等级状态
 */
export const updateMemberLevelStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/member/level/${id}/status`, null, {
    params: { status }
  })
}








