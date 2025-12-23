import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface UserInfo {
  id: number
  username: string
  email?: string
  realName?: string
  isMember?: number  // 是否会员（0-普通用户，1-会员）
  memberLevelId?: number  // 会员等级ID
  memberLevelName?: string  // 会员等级名称
  gender?: number
  phone?: string
  status?: string
  [key: string]: any
}

export const useUserStore = defineStore('user', () => {
  // 从localStorage初始化token和用户信息
  const initToken = localStorage.getItem('token') || ''
  const initUserInfo = localStorage.getItem('userInfo')
  
  const token = ref<string>(initToken)
  const userInfo = ref<UserInfo | null>(
    initUserInfo ? JSON.parse(initUserInfo) : null
  )

  // 设置token
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 清除token
  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('token')
  }

  // 设置用户信息
  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  // 清除用户信息
  const clearUserInfo = () => {
    userInfo.value = null
    localStorage.removeItem('userInfo')
  }

  // 退出登录
  const logout = () => {
    clearToken()
    clearUserInfo()
  }

  // 是否已登录（同时检查token和用户信息）
  const isLoggedIn = () => {
    return !!token.value && !!userInfo.value
  }

  // 是否是会员（登录且isMember=1）
  const isMemberUser = () => {
    return isLoggedIn() && userInfo.value?.isMember === 1
  }

  return {
    token,
    userInfo,
    setToken,
    clearToken,
    setUserInfo,
    clearUserInfo,
    logout,
    isLoggedIn,
    isMemberUser
  }
})

