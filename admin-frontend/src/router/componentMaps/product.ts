// 商品管理模块组件映射（开发者A负责）
export const productComponentMap = {
  'product/List': () => import('@/views/product/ProductManage.vue'),
  'product/Add': () => import('@/views/product/Add.vue'),
  'product/Category': () => import('@/views/product/CategoryManage.vue'),
}

