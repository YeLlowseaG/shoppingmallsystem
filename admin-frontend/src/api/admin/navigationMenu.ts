/**
 * 导航菜单管理API（管理端）
 */

import request from '@/utils/request'

// 导航菜单接口
export interface NavigationMenu {
  id?: number
  menuName: string
  menuUrl: string
  menuType: string // link-直接链接，category-分类，brand-品牌，type-类型
  menuParams?: string // JSON格式存储
  icon?: string
  sortOrder: number
  status: number // 0-禁用，1-启用
  target: string // _self-当前窗口，_blank-新窗口
  description?: string
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
 * 分页查询导航菜单列表
 */
export const getNavigationMenuPage = (
  current: number,
  size: number,
  menuName?: string
): Promise<PageResponse<NavigationMenu>> => {
  return request.get('/api/admin/system/navigation/page', {
    params: { current, size, menuName }
  })
}

/**
 * 获取所有启用的导航菜单
 */
export const getEnabledMenus = (): Promise<NavigationMenu[]> => {
  return request.get('/api/admin/system/navigation/enabled')
}

/**
 * 根据ID获取导航菜单详情
 */
export const getNavigationMenuById = (id: number): Promise<NavigationMenu> => {
  return request.get(`/api/admin/system/navigation/${id}`)
}

/**
 * 创建导航菜单
 */
export const createNavigationMenu = (menu: NavigationMenu): Promise<number> => {
  return request.post('/api/admin/system/navigation', menu)
}

/**
 * 更新导航菜单
 */
export const updateNavigationMenu = (menu: NavigationMenu): Promise<void> => {
  return request.put('/api/admin/system/navigation', menu)
}

/**
 * 删除导航菜单
 */
export const deleteNavigationMenu = (id: number): Promise<void> => {
  return request.delete(`/api/admin/system/navigation/${id}`)
}

/**
 * 更新导航菜单状态
 */
export const updateNavigationMenuStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/system/navigation/${id}/status`, null, {
    params: { status }
  })
}