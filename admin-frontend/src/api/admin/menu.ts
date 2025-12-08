/**
 * 菜单管理API
 */

import request from '@/utils/request'

// 菜单VO
export interface MenuVO {
  id?: number
  parentId?: number
  menuName: string
  menuType: string
  path?: string
  component?: string
  icon?: string
  permission?: string
  sortOrder?: number
  status: number
  children?: MenuVO[]
}

/**
 * 获取菜单树
 */
export const getMenuTree = (): Promise<MenuVO[]> => {
  return request.get('/api/admin/menu/tree')
}

/**
 * 获取菜单详情
 */
export const getMenuById = (id: number): Promise<MenuVO> => {
  return request.get(`/api/admin/menu/${id}`)
}

/**
 * 新增菜单
 */
export const addMenu = (data: MenuVO): Promise<void> => {
  return request.post('/api/admin/menu', data)
}

/**
 * 更新菜单
 */
export const updateMenu = (id: number, data: MenuVO): Promise<void> => {
  return request.put(`/api/admin/menu/${id}`, data)
}

/**
 * 删除菜单
 */
export const deleteMenu = (id: number): Promise<void> => {
  return request.delete(`/api/admin/menu/${id}`)
}

/**
 * 启用/禁用菜单
 */
export const updateMenuStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/menu/${id}/status`, null, {
    params: { status }
  })
}


