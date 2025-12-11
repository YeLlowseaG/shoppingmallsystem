// 权限管理模块组件映射（开发者B负责）
export const permissionComponentMap = {
  'permission/User': () => import('@/views/permission/User.vue'),
  'permission/Role': () => import('@/views/permission/Role.vue'),
  'permission/Menu': () => import('@/views/permission/Menu.vue'),
}

