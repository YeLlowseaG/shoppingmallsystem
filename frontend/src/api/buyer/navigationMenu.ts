/**
 * 买家端导航菜单API
 */

import request from '@/utils/request'

// 导航菜单接口
export interface NavigationMenu {
  id: number
  menuName: string
  menuUrl: string
  menuType: string // link-直接链接，category-分类，brand-品牌，type-类型
  menuParams?: string // JSON格式存储
  icon?: string
  sortOrder: number
  status: number // 0-禁用，1-启用
  target: string // _self-当前窗口，_blank-新窗口
  description?: string
}

/**
 * 获取所有启用的导航菜单
 */
export const getNavigationMenus = (): Promise<NavigationMenu[]> => {
  return request.get('/api/buyer/navigation/menus')
}