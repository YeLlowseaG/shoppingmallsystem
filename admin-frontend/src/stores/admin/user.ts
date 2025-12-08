import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AdminInfoVO, MenuVO } from '@/api/admin/user'

export const useAdminStore = defineStore('admin', () => {
  const token = ref<string>(localStorage.getItem('admin_token') || '')
  const adminInfo = ref<AdminInfoVO | null>(null)
  const menus = ref<MenuVO[]>([])
  const permissions = ref<string[]>([])

  // 设置token
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('admin_token', newToken)
  }

  // 清除token
  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('admin_token')
  }

  // 设置管理员信息
  const setAdminInfo = (info: AdminInfoVO) => {
    adminInfo.value = info
    localStorage.setItem('admin_info', JSON.stringify(info))
  }

  // 清除管理员信息
  const clearAdminInfo = () => {
    adminInfo.value = null
    localStorage.removeItem('admin_info')
  }

  // 设置菜单
  const setMenus = (menuList: MenuVO[]) => {
    menus.value = menuList
    localStorage.setItem('admin_menus', JSON.stringify(menuList))
  }

  // 设置权限
  const setPermissions = (permissionList: string[]) => {
    permissions.value = permissionList
    localStorage.setItem('admin_permissions', JSON.stringify(permissionList))
  }

  // 检查权限
  const hasPermission = (permission: string): boolean => {
    return permissions.value.includes(permission)
  }

  // 退出登录
  const logout = () => {
    clearToken()
    clearAdminInfo()
    menus.value = []
    permissions.value = []
    localStorage.removeItem('admin_menus')
    localStorage.removeItem('admin_permissions')
  }

  // 是否已登录
  const isLoggedIn = () => {
    return !!token.value
  }

  // 初始化（从localStorage恢复）
  const init = () => {
    const savedInfo = localStorage.getItem('admin_info')
    if (savedInfo) {
      adminInfo.value = JSON.parse(savedInfo)
    }
    const savedMenus = localStorage.getItem('admin_menus')
    if (savedMenus) {
      menus.value = JSON.parse(savedMenus)
    }
    const savedPermissions = localStorage.getItem('admin_permissions')
    if (savedPermissions) {
      permissions.value = JSON.parse(savedPermissions)
    }
  }

  return {
    token,
    adminInfo,
    menus,
    permissions,
    setToken,
    clearToken,
    setAdminInfo,
    clearAdminInfo,
    setMenus,
    setPermissions,
    hasPermission,
    logout,
    isLoggedIn,
    init
  }
})

