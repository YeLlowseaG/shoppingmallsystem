import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface AdminInfo {
  id: number
  username: string
  email: string
  role?: string
  [key: string]: any
}

export const useAdminStore = defineStore('admin', () => {
  const token = ref<string>(localStorage.getItem('admin_token') || '')
  const adminInfo = ref<AdminInfo | null>(null)

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
  const setAdminInfo = (info: AdminInfo) => {
    adminInfo.value = info
  }

  // 清除管理员信息
  const clearAdminInfo = () => {
    adminInfo.value = null
  }

  // 退出登录
  const logout = () => {
    clearToken()
    clearAdminInfo()
  }

  // 是否已登录
  const isLoggedIn = () => {
    return !!token.value
  }

  return {
    token,
    adminInfo,
    setToken,
    clearToken,
    setAdminInfo,
    clearAdminInfo,
    logout,
    isLoggedIn
  }
})

