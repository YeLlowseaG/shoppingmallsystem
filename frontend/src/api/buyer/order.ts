import request from '@/utils/request'
import type { ApiResponse } from '@/types'

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
 * 创建订单DTO
 */
export interface CreateOrderDTO {
  addressId: number
  cartIds?: number[]
  items?: Array<{
    productId: number
    quantity: number
    skuId?: number
  }>
  shippingMethod?: string
  deliveryDate?: string
  deliveryTime?: string
  paymentMethod: string
  orderRemark?: string
}

/**
 * 订单查询DTO
 */
export interface OrderQueryDTO {
  orderNo?: string
  recipientName?: string
  orderStatus?: string
  startDate?: string
  endDate?: string
  contactPhone?: string
  contactMobile?: string
  recipientAddress?: string
  pageNum?: number
  pageSize?: number
}

/**
 * 订单列表VO
 */
export interface OrderListVO {
  id: number
  orderNo: string
  recipientName: string
  recipientAddress: string
  description: string
  orderDate: string
  totalAmount: string
  status: string
  logistics?: {
    shipDate: string
    shipTime: string
    carrier: string
    trackingNo: string
  }
}

/**
 * 订单详情VO
 */
export interface OrderDetailVO {
  id: number
  orderNo: string
  originalOrderNo?: string
  orderDate: string
  status: string
  statusText: string
  items: Array<{
    id: number
    productId: number
    productCode: string
    name: string
    specCombination?: string
    image: string
    price: number
    quantity: number
    subtotal: number
  }>
  recipientInfo: {
    name: string
    region: string
    zipCode?: string
    shippingMethod: string
    weight: string
    address: string
    email: string
    phone: string
    mobile?: string
    deliveryTime?: string
    paymentMethod: string
    paymentCurrency: string
  }
  orderNotes?: string
  totalQuantity: number
  totalProductAmount: number
  shippingFee: number
  totalAmount: number
  orderHistory?: Array<{
    date: string
    action: string
  }>
}

/**
 * 订单分页响应
 */
export interface OrderPageResponse {
  records: OrderListVO[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 创建订单
 */
export const createOrder = (data: CreateOrderDTO): Promise<string> => {
  return request.post('/api/buyer/orders', data)
}

/**
 * 获取订单列表
 */
export const getOrderList = (params: OrderQueryDTO): Promise<OrderPageResponse> => {
  return request.get('/api/buyer/orders', { params })
}

/**
 * 获取订单详情
 */
export const getOrderDetail = (orderNo: string): Promise<OrderDetailVO> => {
  return request.get(`/api/buyer/orders/${orderNo}`)
}

/**
 * 取消订单
 */
export const cancelOrder = (orderNo: string): Promise<ApiResponse> => {
  return request.put(`/api/buyer/orders/${orderNo}/cancel`)
}

/**
 * 确认收货
 */
export const confirmReceipt = (orderNo: string): Promise<ApiResponse> => {
  return request.put(`/api/buyer/orders/${orderNo}/confirm`)
}

/**
 * 订单支付DTO
 */
export interface OrderPaymentDTO {
  orderNo: string
  paymentMethod: string
  paymentPassword?: string
}

/**
 * 订单支付
 */
export const payOrder = (orderNo: string, data: OrderPaymentDTO): Promise<PaymentResponseVO> => {
  return request.post(`/api/buyer/orders/${orderNo}/pay`, data)
}

/**
 * 订单统计VO
 */
export interface OrderStatisticsVO {
  unpaidOrderCount: number
  shippedOrderCount: number
  cancelledOrderCount: number
}

/**
 * 获取订单统计信息
 */
export const getOrderStatistics = (): Promise<OrderStatisticsVO> => {
  return request.get('/api/buyer/orders/statistics')
}

/**
 * 订单退款明细VO
 */
export interface OrderRefundItemVO {
  id: number
  orderItemId: number
  productId: number
  productName: string
  productCode: string
  skuId?: number
  specCombination?: string
  refundQuantity: number
  refundPrice: number
  refundSubtotal: number
}

/**
 * 订单退款VO
 */
export interface OrderRefundVO {
  id: number
  refundNo: string
  orderId: number
  orderNo: string
  userId: number
  refundAmount: number
  refundReason?: string
  refundStatus: number
  refundStatusText: string
  refundType: number
  refundTypeText: string
  operatorId?: number
  operatorName?: string
  operatorTime?: string
  operatorRemark?: string
  refundTime?: string
  refundPaymentMethod?: string
  refundPaymentNo?: string
  createTime: string
  refundItems: OrderRefundItemVO[]
}

/**
 * 获取订单的退款列表
 */
export const getOrderRefundList = (orderNo: string): Promise<OrderRefundVO[]> => {
  return request.get(`/api/buyer/orders/${orderNo}/refunds`)
}

