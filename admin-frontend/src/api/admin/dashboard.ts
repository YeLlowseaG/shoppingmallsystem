/**
 * 数据看板API
 */

import request from '@/utils/request'

// 销售趋势数据
export interface SalesTrendData {
  date: string
  sales: number
}

// 订单趋势数据
export interface OrderTrendData {
  date: string
  count: number
}

// 订单状态统计
export interface OrderStatusStatistics {
  pendingPayment: number
  paidNotShipped: number
  shipped: number
  completed: number
  cancelled: number
}

// 数据看板VO
export interface DashboardVO {
  todayOrders: number
  todaySales: number
  pendingOrders: number
  stockWarnings: number
  totalOrders: number
  totalSales: number
  totalUsers: number
  totalProducts: number
  salesTrend: SalesTrendData[]
  orderTrend: OrderTrendData[]
  orderStatusStatistics: OrderStatusStatistics
}

/**
 * 获取数据看板统计信息
 */
export const getDashboardStatistics = (): Promise<DashboardVO> => {
  return request.get('/api/admin/dashboard/statistics')
}
















































