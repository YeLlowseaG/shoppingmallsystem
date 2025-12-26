import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 预存款充值DTO
 */
export interface DepositRechargeDTO {
  amount: number
  currency: string
  paymentMethod: string
}

/**
 * 预存款查询DTO
 */
export interface DepositQueryDTO {
  pageNum?: number
  pageSize?: number
  operationType?: string
  startDate?: string
  endDate?: string
  status?: number // 状态（0-待审核，1-已通过，2-已拒绝/支付失败，3-支付中，4-已超时）
}

/**
 * 预存款交易记录VO
 */
export interface DepositRecordVO {
  id: number
  event: string
  depositAmount: number
  expenseAmount: number
  frozenAmount: number
  unfrozenAmount: number
  currentBalance: number
  availableBalance: number
  createTime: string
  remark: string
  orderNo?: string
  status?: number // 状态（0-待审核，1-已通过，2-已拒绝/支付失败，3-支付中，4-已超时）
  statusName?: string // 状态名称
  paymentMethod?: string // 支付方式
}

/**
 * 预存款余额VO
 */
export interface DepositBalanceVO {
  depositBalance: number
  availableBalance: number
  records: DepositRecordVO[]
  total: number
}

/**
 * 支付响应VO
 */
export interface PaymentResponseVO {
  internalOrderNo: string
  paymentUrl?: string
  paymentParams?: string
  qrCodeUrl?: string
  isMock?: boolean
  mockExternalTradeNo?: string
}

/**
 * 预存款充值
 */
export const rechargeDeposit = (data: DepositRechargeDTO): Promise<PaymentResponseVO> => {
  return request.post('/api/buyer/member/deposit/recharge', data)
}

/**
 * 获取预存款余额和交易记录
 */
export const getDepositBalance = (params?: DepositQueryDTO): Promise<DepositBalanceVO> => {
  return request.get('/api/buyer/member/deposit/balance', { params })
}

