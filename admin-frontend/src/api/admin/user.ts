/**
 * 管理员用户API
 * TODO: 待后端接口实现后补充
 */

import request from '@/utils/request'

// 管理员登录DTO
export interface AdminLoginDTO {
  username: string
  password: string
}

// 管理员信息VO
export interface AdminInfoVO {
  id: number
  username: string
  realName?: string
  email: string
  phone?: string
  status: number
  lastLoginTime?: string
  lastLoginIp?: string
  roles?: RoleVO[]
}

// 角色VO
export interface RoleVO {
  id: number
  roleCode: string
  roleName: string
  description?: string
  status: number
}

// 菜单VO
export interface MenuVO {
  id: number
  parentId?: number
  menuName: string
  menuType: number // 0-目录，1-菜单，2-按钮
  path?: string
  component?: string
  icon?: string
  permission?: string
  sortOrder?: number
  status: number
  children?: MenuVO[]
}

// 登录响应VO
export interface AdminLoginVO {
  token: string
  adminInfo: AdminInfoVO
  menus: MenuVO[]
  permissions: string[]
}

/**
 * 管理员登录
 */
export const adminLogin = (data: AdminLoginDTO): Promise<AdminLoginVO> => {
  return request.post('/api/admin/user/login', data)
}

/**
 * 获取当前管理员信息
 */
export const getAdminInfo = (): Promise<AdminInfoVO> => {
  return request.get('/api/admin/user/info')
}

/**
 * 获取管理员列表
 */
export const getAdminUserList = (params: {
  page?: number
  pageSize?: number
  username?: string
  status?: number
}): Promise<any> => {
  return request.get('/api/admin/user/list', { params })
}

/**
 * 获取管理员详情
 */
export const getAdminUserById = (id: number): Promise<AdminInfoVO> => {
  return request.get(`/api/admin/user/${id}`)
}

/**
 * 新增管理员
 */
export const addAdminUser = (data: {
  username: string
  password: string
  realName: string
  email: string
  phone?: string
  status: number
}, roleIds?: number[]): Promise<void> => {
  return request.post('/api/admin/user', data, {
    params: { roleIds }
  })
}

/**
 * 更新管理员
 */
export const updateAdminUser = (id: number, data: {
  realName?: string
  email?: string
  phone?: string
  password?: string
  status?: number
}, roleIds?: number[]): Promise<void> => {
  return request.put(`/api/admin/user/${id}`, data, {
    params: { roleIds }
  })
}

/**
 * 删除管理员
 */
export const deleteAdminUser = (id: number): Promise<void> => {
  return request.delete(`/api/admin/user/${id}`)
}

/**
 * 启用/禁用管理员
 */
export const updateAdminUserStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/user/${id}/status`, null, {
    params: { status }
  })
}

/**
 * 重置密码
 */
export const resetPassword = (id: number, password: string): Promise<void> => {
  return request.put(`/api/admin/user/${id}/reset-password`, null, {
    params: { password }
  })
}

