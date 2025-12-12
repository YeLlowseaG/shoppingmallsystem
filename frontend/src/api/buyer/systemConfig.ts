/**
 * 买家端系统配置API
 */

import request from '@/utils/request'

/**
 * 获取所有公开配置的键值对映射
 */
export const getPublicConfigs = (): Promise<Record<string, string>> => {
  return request.get('/api/buyer/system/config/public')
}

/**
 * 根据配置键获取配置值
 */
export const getConfigValue = (configKey: string): Promise<string> => {
  return request.get(`/api/buyer/system/config/${configKey}`)
}