/**
 * 订单管理API
 */

import request from '@/utils/request'

// 订单列表VO
export interface OrderListVO {
  id: number
  orderNo: string
  buyerName?: string
  buyerUsername?: string
  recipientName: string
  recipientAddress: string
  description: string
  orderDate: string
  totalAmount: number
  status: number
  statusText: string
  logistics?: {
    shipDate: string
    shipTime: string
    carrier: string
    trackingNo: string
  }
}

// 订单详情VO
export interface OrderDetailVO {
  id: number
  orderNo: string
  originalOrderNo?: string
  buyerName?: string
  buyerUsername?: string
  orderDate: string
  status: number
  statusText: string
  items: Array<{
    id: number
    productCode: string
    name: string
    image: string
    price: number
    quantity: number
    subtotal: number
    specCombination?: string
    refundedQuantity?: number
    availableRefundQuantity?: number
  }>
  recipientInfo: {
    name: string
    region: string
    zipCode?: string
    shippingMethod: string
    weight: number
    address: string
    email: string
    phone: string
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

// 订单查询DTO
export interface OrderQueryDTO {
  orderNo?: string
  recipientName?: string
  buyerName?: string
  buyerUsername?: string
  orderStatus?: number
  startDate?: string
  endDate?: string
  contactPhone?: string
  contactMobile?: string
  recipientAddress?: string
  pageNum?: number
  pageSize?: number
}

/**
 * 获取订单列表
 */
export const getOrderList = (params: OrderQueryDTO): Promise<any> => {
  return request.get('/api/admin/orders', { params })
}

/**
 * 获取订单详情
 */
export const getOrderDetail = (orderNo: string): Promise<OrderDetailVO> => {
  return request.get(`/api/admin/orders/${orderNo}`)
}

/**
 * 确认订单
 */
export const confirmOrder = (orderNo: string): Promise<void> => {
  return request.put(`/api/admin/orders/${orderNo}/confirm`)
}

/**
 * 发货
 */
export const shipOrder = (orderNo: string, logisticsCompany: string, logisticsNo: string): Promise<void> => {
  return request.put(`/api/admin/orders/${orderNo}/ship`, null, {
    params: { logisticsCompany, logisticsNo }
  })
}

/**
 * 添加订单备注
 */
export const addOrderRemark = (orderNo: string, remark: string): Promise<void> => {
  return request.put(`/api/admin/orders/${orderNo}/remark`, null, {
    params: { remark }
  })
}

/**
 * 取消订单（管理员）
 */
export const cancelOrder = (orderNo: string): Promise<void> => {
  return request.put(`/api/admin/orders/${orderNo}/cancel`)
}

/**
 * 订单退款申请DTO
 */
export interface OrderRefundRequestDTO {
  orderNo: string
  refundReason: string
  refundItems: Array<{
    orderItemId: number
    refundQuantity: number
  }>
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
  refundReason: string
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
  refundItems: Array<{
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
  }>
}

/**
 * 订单退款（管理员）
 */
export const refundOrder = (orderNo: string, refundDTO: OrderRefundRequestDTO): Promise<string> => {
  return request.post(`/api/admin/orders/${orderNo}/refund`, refundDTO)
}

/**
 * 获取订单的退款列表
 */
export const getOrderRefundList = (orderNo: string): Promise<OrderRefundVO[]> => {
  return request.get(`/api/admin/orders/${orderNo}/refunds`)
}

/**
 * 退款记录查询DTO
 */
export interface OrderRefundQueryDTO {
  refundNo?: string
  orderNo?: string
  userId?: number
  refundStatus?: number
  refundType?: number
  operatorId?: number
  startDate?: string
  endDate?: string
  pageNum?: number
  pageSize?: number
}

/**
 * 查询退款记录列表（分页）
 */
export const getRefundList = (params: OrderRefundQueryDTO): Promise<any> => {
  return request.get('/api/admin/orders/refunds', { params })
}

/**
 * 获取退款记录详情
 */
export const getRefundDetail = (refundId: number): Promise<OrderRefundVO> => {
  return request.get(`/api/admin/orders/refunds/${refundId}`)
}



