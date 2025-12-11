/**
 * 网站内容管理API（管理端）
 */

import request from '@/utils/request'

// ==================== 轮播图管理 ====================

export interface Banner {
  id?: number
  title: string
  imageUrl: string
  linkType: number // 0-无链接，1-商品分类，2-商品详情，3-促销活动，4-外部链接
  linkValue?: string
  sortOrder: number
  status: number // 0-禁用，1-启用
  startTime?: string
  endTime?: string
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
 * 分页查询轮播图列表
 */
export const getBannerPage = (
  current: number,
  size: number,
  status?: number
): Promise<PageResponse<Banner>> => {
  return request.get('/api/admin/website/banner/page', {
    params: { current, size, status }
  })
}

/**
 * 根据ID获取轮播图详情
 */
export const getBannerById = (id: number): Promise<Banner> => {
  return request.get(`/api/admin/website/banner/${id}`)
}

/**
 * 创建轮播图
 */
export const createBanner = (banner: Banner): Promise<number> => {
  return request.post('/api/admin/website/banner', banner)
}

/**
 * 更新轮播图
 */
export const updateBanner = (banner: Banner): Promise<void> => {
  return request.put('/api/admin/website/banner', banner)
}

/**
 * 删除轮播图
 */
export const deleteBanner = (id: number): Promise<void> => {
  return request.delete(`/api/admin/website/banner/${id}`)
}

/**
 * 更新轮播图状态
 */
export const updateBannerStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/website/banner/${id}/status`, null, {
    params: { status }
  })
}

// ==================== 品牌管理 ====================

export interface Brand {
  id?: number
  brandName: string
  logoUrl: string
  description?: string
  sortOrder: number
  status: number // 0-禁用，1-启用
  createTime?: string
  updateTime?: string
}

/**
 * 分页查询品牌列表
 */
export const getBrandPage = (
  current: number,
  size: number,
  status?: number
): Promise<PageResponse<Brand>> => {
  return request.get('/api/admin/website/brand/page', {
    params: { current, size, status }
  })
}

/**
 * 根据ID获取品牌详情
 */
export const getBrandById = (id: number): Promise<Brand> => {
  return request.get(`/api/admin/website/brand/${id}`)
}

/**
 * 创建品牌
 */
export const createBrand = (brand: Brand): Promise<number> => {
  return request.post('/api/admin/website/brand', brand)
}

/**
 * 更新品牌
 */
export const updateBrand = (brand: Brand): Promise<void> => {
  return request.put('/api/admin/website/brand', brand)
}

/**
 * 删除品牌
 */
export const deleteBrand = (id: number): Promise<void> => {
  return request.delete(`/api/admin/website/brand/${id}`)
}

/**
 * 更新品牌状态
 */
export const updateBrandStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/website/brand/${id}/status`, null, {
    params: { status }
  })
}

// ==================== 广告位管理 ====================

export interface Advertisement {
  id?: number
  adName: string
  adPosition: string // floor_1/floor_2.../brand_side_1/brand_side_2
  imageUrl: string
  linkType: number // 0-无链接，1-商品分类，2-商品详情，3-促销活动，4-外部链接
  linkValue?: string
  sortOrder: number
  status: number // 0-禁用，1-启用
  startTime?: string
  endTime?: string
  createTime?: string
  updateTime?: string
}

/**
 * 分页查询广告位列表
 */
export const getAdvertisementPage = (
  current: number,
  size: number,
  position?: string,
  status?: number
): Promise<PageResponse<Advertisement>> => {
  return request.get('/api/admin/website/advertisement/page', {
    params: { current, size, position, status }
  })
}

/**
 * 根据ID获取广告位详情
 */
export const getAdvertisementById = (id: number): Promise<Advertisement> => {
  return request.get(`/api/admin/website/advertisement/${id}`)
}

/**
 * 创建广告位
 */
export const createAdvertisement = (advertisement: Advertisement): Promise<number> => {
  return request.post('/api/admin/website/advertisement', advertisement)
}

/**
 * 更新广告位
 */
export const updateAdvertisement = (advertisement: Advertisement): Promise<void> => {
  return request.put('/api/admin/website/advertisement', advertisement)
}

/**
 * 删除广告位
 */
export const deleteAdvertisement = (id: number): Promise<void> => {
  return request.delete(`/api/admin/website/advertisement/${id}`)
}

/**
 * 更新广告位状态
 */
export const updateAdvertisementStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/website/advertisement/${id}/status`, null, {
    params: { status }
  })
}
