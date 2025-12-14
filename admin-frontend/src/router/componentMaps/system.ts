// 系统模块组件映射
export const systemComponentMap: Record<string, () => Promise<any>> = {
  // 基础配置（网站Logo、电话、二维码等基础信息）
  'system/Basic': () => import('@/views/system/Basic.vue'),
  // 导航菜单管理
  'system/NavigationMenu': () => import('@/views/system/NavigationMenu.vue'),
  
  // 系统配置组件  
  'system/Payment': () => import('@/views/system/Payment.vue'), 
  'system/Notification': () => import('@/views/system/Notification.vue'),
  
  // 营销管理组件
  'marketing/Promotion': () => import('@/views/marketing/Promotion.vue'),
  'marketing/Price': () => import('@/views/marketing/Price.vue'),
  
  // 统计分析组件
  'statistics/Sales': () => import('@/views/statistics/Sales.vue'),
  'statistics/Order': () => import('@/views/statistics/Order.vue'),
  'statistics/Product': () => import('@/views/statistics/Product.vue'),
  'statistics/Buyer': () => import('@/views/statistics/Buyer.vue'),
}