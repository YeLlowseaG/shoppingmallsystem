/**
 * 网站内容API（买家端）
 */

import request from '@/utils/request'

// 轮播图接口
export interface Banner {
  id: number
  title: string
  imageUrl: string
  linkType: number
  linkValue?: string
}

// 品牌接口
export interface Brand {
  id: number
  brandName: string
  logoUrl: string
  description?: string
}

// 广告位接口
export interface Advertisement {
  id: number
  adName: string
  adPosition: string
  imageUrl: string
  linkType: number
  linkValue?: string
}

/**
 * 获取启用的轮播图列表
 */
export const getActiveBanners = (): Promise<Banner[]> => {
  return request.get('/api/buyer/website/banners')
}

/**
 * 获取启用的品牌列表
 */
export const getActiveBrands = (limit?: number): Promise<Brand[]> => {
  return request.get('/api/buyer/website/brands', {
    params: { limit }
  })
}

/**
 * 根据位置获取广告列表
 */
export const getAdvertisementsByPosition = (position: string): Promise<Advertisement[]> => {
  return request.get(`/api/buyer/website/advertisements/${position}`)
}

/**
 * 获取所有楼层广告
 */
export const getAllFloorAdvertisements = (): Promise<Advertisement[]> => {
  return request.get('/api/buyer/website/advertisements/floors')
}
