import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 预存款交易记录查询DTO
 */
export interface DepositQueryDTO {
  pageNum?: number
  pageSize?: number
  userId?: number
  username?: string
  event?: string
  type?: number
  status?: number
  orderNo?: string
  externalTradeNo?: string
  internalOrderNo?: string
  startDate?: string
  endDate?: string
}

/**
 * 预存款交易记录VO
 */
export interface DepositRecordVO {
  id: number
  userId: number
  username: string
  event: string
  type: number
  typeName: string
  status: number
  statusName: string
  depositAmount: number
  expenseAmount: number
  frozenAmount: number
  unfrozenAmount: number
  currentBalance: number
  availableBalance: number
  paymentMethod?: string
  orderId?: number
  orderNo?: string
  externalTradeNo?: string
  internalOrderNo?: string
  remark?: string
  createTime: string
  auditTime?: string
}

/**
 * 分页查询预存款交易记录
 */
export const getDepositRecordList = (params?: DepositQueryDTO): Promise<{
  records: DepositRecordVO[]
  total: number
  size: number
  current: number
  pages: number
}> => {
  return request.get('/api/admin/deposit/records', { params })
}

/**
 * 根据ID获取预存款交易记录详情
 */
export const getDepositRecordById = (id: number): Promise<DepositRecordVO> => {
  return request.get(`/api/admin/deposit/record/${id}`)
}

/**
 * 退款请求DTO
 */
export interface RefundRequestDTO {
  depositDetailId: number
  refundAmount: number
  refundReason: string
}

/**
 * 预存款充值退款
 */
export const refundDepositRecharge = (data: RefundRequestDTO): Promise<void> => {
  return request.post('/api/admin/deposit/refund', data)
}

