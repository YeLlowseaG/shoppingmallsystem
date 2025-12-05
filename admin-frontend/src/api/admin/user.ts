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
  email: string
  role: string
}

// 登录响应VO
export interface AdminLoginVO {
  token: string
  adminInfo: AdminInfoVO
}

/**
 * 管理员登录
 */
export const adminLogin = (data: AdminLoginDTO): Promise<AdminLoginVO> => {
  return request.post('/admin/user/login', data)
}

/**
 * 获取当前管理员信息
 */
export const getAdminInfo = (): Promise<AdminInfoVO> => {
  return request.get('/admin/user/info')
}

