/**
 * 网站配置工具函数
 * 用于统一管理网站名称等配置信息
 */

import { getAllConfigs } from '@/api/admin/systemConfig'

// 网站名称缓存
let siteNameCache: string | null = null

/**
 * 获取网站名称
 * @param defaultValue 默认值，如果获取失败则返回此值
 * @returns 网站名称
 */
export const getSiteName = async (defaultValue: string = '管理后台'): Promise<string> => {
  // 如果已有缓存，直接返回
  if (siteNameCache) {
    return siteNameCache
  }

  try {
    const configs = await getAllConfigs()
    if (configs['site.name']) {
      siteNameCache = configs['site.name']
      return siteNameCache
    }
  } catch (error) {
    console.error('获取网站名称失败:', error)
  }

  return defaultValue
}

/**
 * 获取页面标题
 * @param pageTitle 页面标题
 * @param defaultValue 默认网站名称
 * @returns 完整页面标题
 */
export const getPageTitle = async (pageTitle?: string, defaultValue: string = '管理后台'): Promise<string> => {
  const siteName = await getSiteName(defaultValue)
  if (pageTitle) {
    return `${pageTitle} - ${siteName}管理后台`
  }
  return `${siteName}管理后台`
}

/**
 * 清除缓存（当配置更新时调用）
 */
export const clearSiteNameCache = () => {
  siteNameCache = null
}

