// 财务模块组件映射
export const financeComponentMap: Record<string, () => Promise<any>> = {
  // 支付记录管理
  'finance/PaymentRecord': () => import('@/views/finance/PaymentRecord.vue'),
}
