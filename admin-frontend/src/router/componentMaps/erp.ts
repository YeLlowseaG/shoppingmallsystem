// ERP管理模块组件映射
export const erpComponentMap = {
  // 聚水潭配置
  'erp/Config': () => import('@/views/erp/Config.vue'),

  // 订单同步日志
  'erp/OrderSync': () => import('@/views/erp/OrderSync.vue'),

  // 商品同步日志
  'erp/ProductSync': () => import('@/views/erp/ProductSync.vue')
}
