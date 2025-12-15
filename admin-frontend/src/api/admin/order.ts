/**
 * 订单管理API
 */

import request from '@/utils/request'

// 订单列表VO
export interface OrderListVO {
  id: number
  orderNo: string
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



