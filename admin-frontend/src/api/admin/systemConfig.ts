/**
 * 系统配置管理API（管理端）
 */

import request from '@/utils/request'

// 系统配置接口
export interface SystemConfig {
  id?: number
  configKey: string
  configValue?: string
  configName: string
  configDesc?: string
  configType: string // text/textarea/image/number
  category?: string // site-网站基础, payment-支付配置, app-应用配置, mail-邮件配置, order-订单配置, wechat.work-企业微信
  sortOrder: number
  status: number // 0-禁用，1-启用
  createTime?: string
  updateTime?: string
}

export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 分页查询系统配置列表
 */
export const getSystemConfigPage = (
  current: number,
  size: number,
  configKey?: string,
  category?: string
): Promise<PageResponse<SystemConfig>> => {
  return request.get('/api/admin/system/config/page', {
    params: { current, size, configKey, category }
  })
}

/**
 * 获取所有配置的键值对映射
 */
export const getAllConfigs = (): Promise<Record<string, string>> => {
  return request.get('/api/admin/system/config/all')
}

/**
 * 获取指定前缀的配置
 */
export const getConfigsByPrefix = (prefix: string): Promise<SystemConfig[]> => {
  return request.get(`/api/admin/system/config/prefix/${prefix}`)
}

/**
 * 根据ID获取系统配置详情
 */
export const getSystemConfigById = (id: number): Promise<SystemConfig> => {
  return request.get(`/api/admin/system/config/${id}`)
}

/**
 * 创建系统配置
 */
export const createSystemConfig = (config: SystemConfig): Promise<number> => {
  return request.post('/api/admin/system/config', config)
}

/**
 * 更新系统配置
 */
export const updateSystemConfig = (config: SystemConfig): Promise<void> => {
  return request.put('/api/admin/system/config', config)
}

/**
 * 批量更新系统配置
 */
export const batchUpdateConfigs = (configs: SystemConfig[]): Promise<void> => {
  return request.put('/api/admin/system/config/batch', configs)
}

/**
 * 删除系统配置
 */
export const deleteSystemConfig = (id: number): Promise<void> => {
  return request.delete(`/api/admin/system/config/${id}`)
}

/**
 * 更新系统配置状态
 */
export const updateSystemConfigStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/system/config/${id}/status`, null, {
    params: { status }
  })
}