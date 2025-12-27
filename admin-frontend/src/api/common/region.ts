/**
 * 地区API
 */
import request from '@/utils/request'

export interface RegionVO {
  id: number
  code: string
  name: string
  parentId?: number
  level: number
  sortOrder: number
  children?: RegionVO[]
}

/**
 * 获取所有省份
 */
export const getProvinces = (): Promise<RegionVO[]> => {
  return request.get('/api/regions/provinces')
}

/**
 * 根据父级ID获取子级地区
 */
export const getChildrenByParentId = (parentId: number): Promise<RegionVO[]> => {
  return request.get(`/api/regions/children/${parentId}`)
}

/**
 * 根据编码获取地区信息
 */
export const getRegionByCode = (code: string): Promise<RegionVO> => {
  return request.get(`/api/regions/code/${code}`)
}

