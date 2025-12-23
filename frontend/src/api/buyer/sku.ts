import request from '@/utils/request'

// SKU相关接口类型定义
export interface ProductSkuVO {
  id: number
  productId: number
  skuCode: string
  specCombination: string
  price: number
  suggestedRetailPrice?: number  // 建议零售价
  marketRetailPrice?: number      // 市场零售价
  memberPrice?: number            // 会员价（后端根据用户等级折扣计算）
  stock: number
  warningStock: number
  salesCount: number
  weight?: number
  skuImage?: string
  skuImages?: string
  status: number
  createTime: string
  updateTime: string
}

export interface ProductSpecValueVO {
  id: number
  specKeyId: number
  specValue: string
  specImage?: string
  sortOrder: number
}

export interface ProductSpecKeyVO {
  id: number
  productId: number
  specName: string
  sortOrder: number
  specValues: ProductSpecValueVO[]
}

/**
 * 根据商品ID获取SKU列表
 */
export const getSkusByProductId = (productId: number): Promise<ProductSkuVO[]> => {
  return request.get(`/api/buyer/product/${productId}/skus`)
}

/**
 * 根据商品ID获取规格属性列表
 */
export const getSpecKeysByProductId = (productId: number): Promise<ProductSpecKeyVO[]> => {
  return request.get(`/api/buyer/product/${productId}/specs`)
}

/**
 * 根据商品ID和规格组合获取SKU
 */
export const getSkuBySpecCombination = (productId: number, specCombination: string): Promise<ProductSkuVO | null> => {
  return request.get(`/api/buyer/product/${productId}/sku?specCombination=${encodeURIComponent(specCombination)}`)
}