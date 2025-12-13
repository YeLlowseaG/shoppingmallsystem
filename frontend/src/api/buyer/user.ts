import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 注册DTO
 */
export interface RegisterDTO {
  username: string
  email: string
  password: string
  confirmPassword: string
  realName: string
  gender: number
  province: string
  city: string
  district: string
  address: string
  phone: string
  operator: string
  captchaId: string
  captcha: string
  birthYear?: number
  birthMonth?: number
  birthDay?: number
  zipCode?: string
  fixedPhone?: string
  securityQuestion?: string
  securityAnswer?: string
  wangwang?: string
}

/**
 * 登录DTO
 */
export interface LoginDTO {
  username: string
  password: string
}

/**
 * 忘记密码DTO
 */
export interface ForgotPasswordDTO {
  username: string
  email?: string
  phone?: string
}

/**
 * 重置密码DTO
 */
export interface ResetPasswordDTO {
  code: string
  newPassword: string
  confirmPassword: string
}

/**
 * 用户信息DTO
 */
export interface UserInfoDTO {
  realName?: string
  gender?: number
  phone?: string
  email?: string
  province?: string
  city?: string
  district?: string
  address?: string
  birthday?: string
  zipCode?: string
  fixedPhone?: string
  securityQuestion?: string
  securityAnswer?: string
  wangwang?: string
  operator?: string
}

/**
 * 登录响应VO
 */
export interface LoginVO {
  token: string
  userInfo: UserInfoVO
}

/**
 * 用户信息VO
 */
export interface UserInfoVO {
  id: number
  username: string
  email: string
  realName: string
  gender: number
  phone: string
  userLevel: string
  status: string
  province?: string
  city?: string
  district?: string
  address?: string
  birthday?: string
  zipCode?: string
  fixedPhone?: string
  securityQuestion?: string
  securityAnswer?: string
  wangwang?: string
  operator?: string
}

/**
 * 用户注册
 */
export const register = (data: RegisterDTO): Promise<ApiResponse> => {
  return request.post('/api/buyer/user/register', data)
}

/**
 * 用户登录
 */
export const login = (data: LoginDTO): Promise<LoginVO> => {
  return request.post('/api/buyer/user/login', data)
}

/**
 * 忘记密码
 */
export const forgotPassword = (data: ForgotPasswordDTO): Promise<ApiResponse> => {
  return request.post('/api/buyer/user/forgot-password', data)
}

/**
 * 重置密码（通过验证码）
 */
export const resetPassword = (data: ResetPasswordDTO): Promise<ApiResponse> => {
  return request.post('/api/buyer/user/reset-password', data)
}

/**
 * 获取当前用户信息
 */
export const getUserInfo = (): Promise<UserInfoVO> => {
  return request.get('/api/buyer/user/info')
}

/**
 * 更新用户信息
 */
export const updateUserInfo = (data: UserInfoDTO): Promise<ApiResponse> => {
  return request.put('/api/buyer/user/info', data)
}

/**
 * 修改密码
 */
export const changePassword = (oldPassword: string, newPassword: string): Promise<ApiResponse> => {
  return request.put('/api/buyer/user/password', null, {
    params: { oldPassword, newPassword }
  })
}

/**
 * 修改支付密码
 */
export const changePaymentPassword = (oldPaymentPassword: string, newPaymentPassword: string): Promise<ApiResponse> => {
  return request.put('/api/buyer/user/payment-password', null, {
    params: { oldPaymentPassword, newPaymentPassword }
  })
}

