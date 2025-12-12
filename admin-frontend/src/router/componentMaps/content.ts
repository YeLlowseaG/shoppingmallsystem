// 内容管理模块组件映射
export const contentComponentMap: Record<string, () => Promise<any>> = {
  // 咨询管理
  'content/Consultation': () => import('@/views/content/ConsultationManage.vue'),
  
  // 评价管理
  'content/Review': () => import('@/views/content/ReviewManage.vue'),
}