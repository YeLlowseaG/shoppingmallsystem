/**
 * 角色管理API
 */

import request from '@/utils/request'

// 角色VO
export interface RoleVO {
  id?: number
  roleCode: string
  roleName: string
  description?: string
  status: number
  sortOrder?: number
  menuIds?: number[]
}

/**
 * 获取角色列表
 */
export const getRoleList = (params: {
  page?: number
  pageSize?: number
  roleCode?: string
  status?: number
}): Promise<any> => {
  return request.get('/api/admin/role/list', { params })
}

/**
 * 获取所有角色
 */
export const getAllRoles = (): Promise<RoleVO[]> => {
  return request.get('/api/admin/role/all')
}

/**
 * 获取角色详情
 */
export const getRoleById = (id: number): Promise<RoleVO> => {
  return request.get(`/api/admin/role/${id}`)
}

/**
 * 新增角色
 */
export const addRole = (data: RoleVO, menuIds?: number[]): Promise<void> => {
  // 确保 menuIds 参数正确传递，即使是空数组也要传递
  const params: any = {}
  if (menuIds !== undefined) {
    params.menuIds = menuIds
  }
  return request.post('/api/admin/role', data, {
    params
  })
}

/**
 * 更新角色
 */
export const updateRole = (id: number, data: RoleVO, menuIds?: number[]): Promise<void> => {
  // 确保 menuIds 参数正确传递，即使是空数组也要传递
  const params: any = {}
  if (menuIds !== undefined) {
    params.menuIds = menuIds
  }
  return request.put(`/api/admin/role/${id}`, data, {
    params
  })
}

/**
 * 删除角色
 */
export const deleteRole = (id: number): Promise<void> => {
  return request.delete(`/api/admin/role/${id}`)
}

/**
 * 启用/禁用角色
 */
export const updateRoleStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/role/${id}/status`, null, {
    params: { status }
  })
}

