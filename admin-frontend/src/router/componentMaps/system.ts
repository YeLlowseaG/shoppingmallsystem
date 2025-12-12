// 系统模块组件映射
export const systemComponentMap: Record<string, () => Promise<any>> = {
  // 基础配置（网站Logo、电话、二维码等基础信息）
  'system/Basic': () => import('@/views/system/Basic.vue'),
  // 导航菜单管理
  'system/NavigationMenu': () => import('@/views/system/NavigationMenu.vue'),
  
  // 预留其他系统模块组件  
  // 'system/Payment': () => import('@/views/system/Payment.vue'), 
  // 'system/Logistics': () => import('@/views/system/Logistics.vue'),
  // 'system/Notification': () => import('@/views/system/Notification.vue'),
}