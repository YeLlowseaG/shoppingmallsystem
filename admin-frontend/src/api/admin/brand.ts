import request from '@/utils/request'

/**
 * 获取品牌选项列表（用于下拉框）
 */
export function getBrandOptions() {
  return request.get('/api/admin/website/brand/options')
}

/**
 * 获取所有启用的品牌（分页方式）
 */
export function getAllBrands() {
  return request({
    url: '/admin/website/brand/page',
    method: 'get',
    params: {
      status: 1,
      current: 1,
      size: 999
    }
  })
}