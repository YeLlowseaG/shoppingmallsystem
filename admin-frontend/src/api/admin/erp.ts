import request from '@/utils/request'

// ==================== 聚水潭配置管理 ====================

/**
 * 获取聚水潭配置
 */
export function getJushuitanConfig() {
  return request({
    url: '/api/admin/erp/config',
    method: 'get'
  })
}

/**
 * 保存或更新聚水潭配置
 */
export function saveJushuitanConfig(data: any) {
  return request({
    url: '/api/admin/erp/config',
    method: 'post',
    data
  })
}

/**
 * 测试聚水潭连接
 */
export function testJushuitanConnection() {
  return request({
    url: '/api/admin/erp/config/test',
    method: 'post'
  })
}

// ==================== 订单同步操作 ====================

/**
 * 手动推送订单到聚水潭
 */
export function pushOrderToErp(orderId: number) {
  return request({
    url: `/api/admin/erp/order/push/${orderId}`,
    method: 'post'
  })
}

/**
 * 批量推送订单
 */
export function batchPushOrders(orderIds: number[]) {
  return request({
    url: '/api/admin/erp/order/push/batch',
    method: 'post',
    data: orderIds
  })
}

/**
 * 重试失败的订单推送
 */
export function retryPushOrder(orderId: number) {
  return request({
    url: `/api/admin/erp/order/push/retry/${orderId}`,
    method: 'post'
  })
}

/**
 * 重试失败的订单推送（返回详细日志）
 */
export function retryPushOrderWithDetail(orderId: number) {
  return request({
    url: `/api/admin/erp/order/push/retry-with-detail/${orderId}`,
    method: 'post'
  })
}

/**
 * 查询订单推送状态
 */
export function queryPushStatus(orderId: number) {
  return request({
    url: `/api/admin/erp/order/push/status/${orderId}`,
    method: 'get'
  })
}

// ==================== 物流信息拉取 ====================

/**
 * 手动拉取订单物流信息
 */
export function pullOrderLogistics(orderId: number) {
  return request({
    url: `/api/admin/erp/order/logistics/pull/${orderId}`,
    method: 'post'
  })
}

/**
 * 批量拉取物流信息
 */
export function batchPullLogistics(orderIds: number[]) {
  return request({
    url: '/api/admin/erp/order/logistics/pull/batch',
    method: 'post',
    data: orderIds
  })
}

/**
 * 拉取所有待发货订单的物流信息
 */
export function pullPendingLogistics() {
  return request({
    url: '/api/admin/erp/order/logistics/pull/pending',
    method: 'post'
  })
}

/**
 * 查询订单物流状态
 */
export function queryLogisticsStatus(orderId: number) {
  return request({
    url: `/api/admin/erp/order/logistics/status/${orderId}`,
    method: 'get'
  })
}

// ==================== 同步日志查询 ====================

/**
 * 分页查询订单同步日志
 */
export function getSyncLogs(params: {
  pageNum?: number
  pageSize?: number
  orderId?: number
  syncType?: string
  syncStatus?: number
}) {
  return request({
    url: '/api/admin/erp/order/sync-log',
    method: 'get',
    params
  })
}

/**
 * 查询指定订单的同步日志
 */
export function getOrderSyncLogs(orderId: number) {
  return request({
    url: `/api/admin/erp/order/sync-log/${orderId}`,
    method: 'get'
  })
}

// ==================== 商品同步操作 ====================

/**
 * 同步单个商品到聚水潭
 */
export function syncProductToErp(productId: number) {
  return request({
    url: `/api/admin/erp/product/sync/${productId}`,
    method: 'post'
  })
}

/**
 * 批量同步商品到聚水潭
 */
export function batchSyncProducts(productIds: number[]) {
  return request({
    url: '/api/admin/erp/product/sync/batch',
    method: 'post',
    data: productIds
  })
}

/**
 * 分页查询商品同步日志
 */
export function getProductSyncLogs(params: {
  pageNum?: number
  pageSize?: number
  productId?: number
  syncType?: string
  syncStatus?: number
  envType?: string
}) {
  return request({
    url: '/api/admin/erp/product/sync-log',
    method: 'get',
    params
  })
}

/**
 * 查询指定商品的同步日志
 */
export function getProductSyncLogsByProductId(productId: number) {
  return request({
    url: `/api/admin/erp/product/sync-log/${productId}`,
    method: 'get'
  })
}
