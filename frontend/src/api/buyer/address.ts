import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 收货地址DTO
 */
export interface AddressDTO {
  recipient: string
  phone?: string
  mobile?: string
  province: string
  city: string
  district: string
  address: string
  zipCode?: string
  isDefault?: boolean
}

/**
 * 收货地址VO
 */
export interface AddressVO {
  id: number
  userId: number
  recipient: string
  phone?: string
  mobile?: string
  province: string
  city: string
  district: string
  address: string
  fullAddress?: string
  zipCode?: string
  isDefault: boolean
  createTime?: string
  updateTime?: string
}

/**
 * 获取收货地址列表
 */
export const getAddressList = (): Promise<AddressVO[]> => {
  return request.get('/api/buyer/addresses')
}

/**
 * 根据ID获取收货地址详情
 */
export const getAddressById = (id: number): Promise<AddressVO> => {
  return request.get(`/api/buyer/addresses/${id}`)
}

/**
 * 新增收货地址
 */
export const addAddress = (data: AddressDTO): Promise<ApiResponse> => {
  return request.post('/api/buyer/addresses', data)
}

/**
 * 更新收货地址
 */
export const updateAddress = (id: number, data: AddressDTO): Promise<ApiResponse> => {
  return request.put(`/api/buyer/addresses/${id}`, data)
}

/**
 * 删除收货地址
 */
export const deleteAddress = (id: number): Promise<ApiResponse> => {
  return request.delete(`/api/buyer/addresses/${id}`)
}

/**
 * 设置默认收货地址
 */
export const setDefaultAddress = (id: number): Promise<ApiResponse> => {
  return request.put(`/api/buyer/addresses/${id}/default`)
}

