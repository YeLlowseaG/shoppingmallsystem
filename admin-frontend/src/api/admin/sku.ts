import request from '@/utils/request'

// SKU会员价DTO
export interface ProductSkuMemberPriceDTO {
  memberLevelId: number
  memberPrice: number
}

// SKU会员价VO（用于接收后端数据）
export interface ProductSkuMemberPriceVO {
  memberLevelId: number
  memberPrice: number
}

// SKU相关接口类型定义
export interface ProductSkuDTO {
  productId: number
  skuCode: string
  specCombination: string
  price: number
  suggestedRetailPrice?: number
  marketRetailPrice?: number
  memberPrice?: number
  enableMemberPrice?: number
  memberPrices?: ProductSkuMemberPriceDTO[]
  stock: number
  warningStock?: number
  weight?: number
  skuImage?: string
  skuImages?: string
  status?: number
}

export interface ProductSkuVO {
  id: number
  productId: number
  skuCode: string
  specCombination: string
  price: number
  suggestedRetailPrice?: number
  marketRetailPrice?: number
  memberPrice?: number
  enableMemberPrice?: number
  stock: number
  warningStock: number
  salesCount: number
  weight?: number
  skuImage?: string
  skuImages?: string
  status: number
  createTime: string
  updateTime: string
  memberPrices?: ProductSkuMemberPriceVO[]
}

export interface ProductSpecKeyDTO {
  productId: number
  specName: string
  sortOrder?: number
}

export interface ProductSpecValueVO {
  id: number
  specKeyId: number
  specValue: string
  specImage?: string
  sortOrder: number
  createTime: string
  updateTime: string
}

export interface ProductSpecKeyVO {
  id: number
  productId: number
  specName: string
  sortOrder: number
  specValues: ProductSpecValueVO[]
  createTime: string
  updateTime: string
}

/**
 * 创建SKU
 */
export const createSku = (data: ProductSkuDTO): Promise<number> => {
  return request.post('/api/admin/product-sku', data)
}

/**
 * 批量创建SKU
 */
export const batchCreateSkus = (data: ProductSkuDTO[]): Promise<number> => {
  return request.post('/api/admin/product-sku/batch', data)
}

/**
 * 更新SKU
 */
export const updateSku = (id: number, data: ProductSkuDTO): Promise<string> => {
  return request.put(`/api/admin/product-sku/${id}`, data)
}

/**
 * 删除SKU
 */
export const deleteSku = (id: number): Promise<string> => {
  return request.delete(`/api/admin/product-sku/${id}`)
}

/**
 * 根据商品ID获取SKU列表
 */
export const getSkusByProductId = (productId: number): Promise<ProductSkuVO[]> => {
  return request.get(`/api/admin/product-sku/product/${productId}`)
}

/**
 * 根据ID获取SKU详情
 */
export const getSkuById = (id: number): Promise<ProductSkuVO> => {
  return request.get(`/api/admin/product-sku/${id}`)
}

/**
 * 根据SKU编码获取SKU详情
 */
export const getSkuByCode = (skuCode: string): Promise<ProductSkuVO> => {
  return request.get(`/api/admin/product-sku/code/${skuCode}`)
}

/**
 * 更新SKU库存
 */
export const updateSkuStock = (id: number, stock: number): Promise<string> => {
  return request.put(`/api/admin/product-sku/${id}/stock?stock=${stock}`)
}

/**
 * 获取库存警戒的SKU列表
 */
export const getLowStockSkus = (): Promise<ProductSkuVO[]> => {
  return request.get('/api/admin/product-sku/low-stock')
}

/**
 * 检查SKU编码是否存在
 */
export const checkSkuCode = (skuCode: string): Promise<boolean> => {
  return request.get(`/api/admin/product-sku/check-code?skuCode=${skuCode}`)
}

/**
 * 创建规格属性
 */
export const createSpecKey = (data: ProductSpecKeyDTO): Promise<number> => {
  return request.post('/api/admin/product-spec', data)
}

/**
 * 更新规格属性
 */
export const updateSpecKey = (id: number, data: ProductSpecKeyDTO): Promise<string> => {
  return request.put(`/api/admin/product-spec/${id}`, data)
}

/**
 * 删除规格属性
 */
export const deleteSpecKey = (id: number): Promise<string> => {
  return request.delete(`/api/admin/product-spec/${id}`)
}

/**
 * 根据商品ID获取规格属性列表
 */
export const getSpecKeysByProductId = (productId: number): Promise<ProductSpecKeyVO[]> => {
  return request.get(`/api/admin/product-spec/product/${productId}`)
}

/**
 * 根据ID获取规格属性详情
 */
export const getSpecKeyById = (id: number): Promise<ProductSpecKeyVO> => {
  return request.get(`/api/admin/product-spec/${id}`)
}

/**
 * 根据商品ID删除所有规格属性和规格值
 */
export const deleteSpecsByProductId = (productId: number): Promise<number> => {
  return request.delete(`/api/admin/product-spec/product/${productId}`)
}