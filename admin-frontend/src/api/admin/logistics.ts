/**
 * 物流管理API
 */

import request from '@/utils/request'

// 物流公司VO
export interface LogisticsCompanyVO {
  id?: number
  companyCode: string
  companyName: string
  companyShortName?: string
  contactPhone?: string
  website?: string
  sortOrder?: number
  status: number
}

// 配送方式VO
export interface ShippingMethodVO {
  id?: number
  methodCode: string
  methodName: string
  logisticsCompanyId?: number
  logisticsCompanyName?: string
  description?: string
  shippingTemplateId?: number
  basePrice?: number
  calculationType: number
  sortOrder?: number
  status: number
}

// 运费规则VO
export interface ShippingRuleVO {
  id?: number
  templateId?: number
  regionCode?: string
  regionName?: string
  firstWeight?: number
  firstPrice?: number
  continueWeight?: number
  continuePrice?: number
  freeShippingAmount?: number
  freeShippingWeight?: number
  freeShippingQuantity?: number
  sortOrder?: number
}

// 运费模板VO
export interface ShippingTemplateVO {
  id?: number
  templateName: string
  calculationType: number
  freeShippingAmount?: number
  freeShippingWeight?: number
  freeShippingQuantity?: number
  defaultFirstWeight?: number
  defaultFirstPrice?: number
  defaultContinueWeight?: number
  defaultContinuePrice?: number
  description?: string
  status: number
  rules?: ShippingRuleVO[]
}

// ==================== 物流公司管理 ====================

/**
 * 获取物流公司列表
 */
export const getLogisticsCompanyList = (params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
}): Promise<any> => {
  return request.get('/api/admin/logistics/company/list', { params })
}

/**
 * 获取所有启用的物流公司
 */
export const getAllEnabledLogisticsCompanies = (): Promise<LogisticsCompanyVO[]> => {
  return request.get('/api/admin/logistics/company/all')
}

/**
 * 获取物流公司详情
 */
export const getLogisticsCompanyById = (id: number): Promise<LogisticsCompanyVO> => {
  return request.get(`/api/admin/logistics/company/${id}`)
}

/**
 * 新增物流公司
 */
export const addLogisticsCompany = (data: LogisticsCompanyVO): Promise<void> => {
  return request.post('/api/admin/logistics/company', data)
}

/**
 * 更新物流公司
 */
export const updateLogisticsCompany = (id: number, data: LogisticsCompanyVO): Promise<void> => {
  return request.put(`/api/admin/logistics/company/${id}`, data)
}

/**
 * 删除物流公司
 */
export const deleteLogisticsCompany = (id: number): Promise<void> => {
  return request.delete(`/api/admin/logistics/company/${id}`)
}

/**
 * 启用/禁用物流公司
 */
export const updateLogisticsCompanyStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/logistics/company/${id}/status`, null, {
    params: { status }
  })
}

// ==================== 配送方式管理 ====================

/**
 * 获取配送方式列表
 */
export const getShippingMethodList = (params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
}): Promise<any> => {
  return request.get('/api/admin/shipping/method/list', { params })
}

/**
 * 获取配送方式详情
 */
export const getShippingMethodById = (id: number): Promise<ShippingMethodVO> => {
  return request.get(`/api/admin/shipping/method/${id}`)
}

/**
 * 新增配送方式
 */
export const addShippingMethod = (data: ShippingMethodVO): Promise<void> => {
  return request.post('/api/admin/shipping/method', data)
}

/**
 * 更新配送方式
 */
export const updateShippingMethod = (id: number, data: ShippingMethodVO): Promise<void> => {
  return request.put(`/api/admin/shipping/method/${id}`, data)
}

/**
 * 删除配送方式
 */
export const deleteShippingMethod = (id: number): Promise<void> => {
  return request.delete(`/api/admin/shipping/method/${id}`)
}

/**
 * 启用/禁用配送方式
 */
export const updateShippingMethodStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/shipping/method/${id}/status`, null, {
    params: { status }
  })
}

// ==================== 运费模板管理 ====================

/**
 * 获取运费模板列表
 */
export const getShippingTemplateList = (params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
}): Promise<any> => {
  return request.get('/api/admin/shipping/template/list', { params })
}

/**
 * 获取所有启用的运费模板
 */
export const getAllEnabledShippingTemplates = (): Promise<ShippingTemplateVO[]> => {
  return request.get('/api/admin/shipping/template/all')
}

/**
 * 获取运费模板详情
 */
export const getShippingTemplateById = (id: number): Promise<ShippingTemplateVO> => {
  return request.get(`/api/admin/shipping/template/${id}`)
}

/**
 * 新增运费模板
 */
export const addShippingTemplate = (data: ShippingTemplateVO): Promise<void> => {
  return request.post('/api/admin/shipping/template', data)
}

/**
 * 更新运费模板
 */
export const updateShippingTemplate = (id: number, data: ShippingTemplateVO): Promise<void> => {
  return request.put(`/api/admin/shipping/template/${id}`, data)
}

/**
 * 删除运费模板
 */
export const deleteShippingTemplate = (id: number): Promise<void> => {
  return request.delete(`/api/admin/shipping/template/${id}`)
}

/**
 * 启用/禁用运费模板
 */
export const updateShippingTemplateStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/shipping/template/${id}/status`, null, {
    params: { status }
  })
}











































