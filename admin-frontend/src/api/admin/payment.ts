/**
 * 支付配置管理API（管理端）
 */

import request from '@/utils/request'

// 微信支付环境配置
export interface WeChatPayEnvConfig {
  appid: string
  mchid: string
  key: string
  certPath: string
}

// 微信支付配置
export interface WeChatPayConfig {
  enabled: boolean
  env: 'sandbox' | 'production'
  notifyUrl: string
  sandbox: WeChatPayEnvConfig
  production: WeChatPayEnvConfig
}

// 支付宝环境配置
export interface AlipayEnvConfig {
  appid: string
  privateKey: string
  publicKey: string
}

// 支付宝配置
export interface AlipayConfig {
  enabled: boolean
  env: 'sandbox' | 'production'
  notifyUrl: string
  sandbox: AlipayEnvConfig
  production: AlipayEnvConfig
}

// 支付配置响应
export interface PaymentConfigResponse {
  wechat: WeChatPayConfig
  alipay: AlipayConfig
}

// 支付配置更新请求
export interface PaymentConfigUpdateRequest {
  wechat?: Partial<WeChatPayConfig>
  alipay?: Partial<AlipayConfig>
}

// 测试连接请求
export interface TestConnectionRequest {
  paymentMethod: 'wechat' | 'alipay'
  env?: 'sandbox' | 'production'
}

/**
 * 获取支付配置
 */
export const getPaymentConfig = (): Promise<PaymentConfigResponse> => {
  return request.get('/api/admin/payment/config')
}

/**
 * 更新支付配置
 */
export const updatePaymentConfig = (
  data: PaymentConfigUpdateRequest
): Promise<void> => {
  return request.post('/api/admin/payment/config', data)
}

/**
 * 刷新支付配置缓存
 */
export const refreshPaymentConfig = (): Promise<void> => {
  return request.post('/api/admin/payment/config/refresh')
}

/**
 * 测试支付连接
 */
export const testPaymentConnection = (
  data: TestConnectionRequest
): Promise<string> => {
  return request.post('/api/admin/payment/config/test', data)
}







