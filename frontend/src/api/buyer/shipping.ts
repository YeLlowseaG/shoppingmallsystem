import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 运费计算DTO
 */
export interface ShippingFeeCalculateDTO {
  province: string // 省份
  city: string // 城市
  district: string // 区县
  totalWeight: number // 总重量（kg）
  totalAmount: number // 总金额
  totalQuantity: number // 总件数
}

/**
 * 根据运费模板ID计算运费
 */
export const calculateShippingFeeByTemplate = (
  templateId: number | null,
  calculateDTO: ShippingFeeCalculateDTO
): Promise<ApiResponse<number>> => {
  if (!templateId) {
    // 没有运费模板，返回0（包邮）
    return Promise.resolve({ code: 200, message: 'success', data: 0 })
  }
  return request.post(`/api/buyer/shipping/calculate-by-template/${templateId}`, calculateDTO)
}



