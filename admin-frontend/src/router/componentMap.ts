// 自动合并所有模块的组件映射
// 注意：此文件由各模块文件自动合并生成，不要直接修改此文件
// 新增组件映射时，请在对应的模块文件中添加

import { commonComponentMap } from './componentMaps/common'
import { productComponentMap } from './componentMaps/product'
import { buyerComponentMap } from './componentMaps/buyer'
import { websiteComponentMap } from './componentMaps/website'
import { orderComponentMap } from './componentMaps/order'
import { permissionComponentMap } from './componentMaps/permission'
import { logisticsComponentMap } from './componentMaps/logistics'
import { stockComponentMap } from './componentMaps/stock'
import { systemComponentMap } from './componentMaps/system'
import { contentComponentMap } from './componentMaps/content'
import { financeComponentMap } from './componentMaps/finance'

// 合并所有模块的组件映射
export const componentMap: Record<string, () => Promise<any>> = {
  // 公共组件
  ...commonComponentMap,
  
  // 各模块组件映射（按模块拆分，各自维护）
  ...productComponentMap,
  ...buyerComponentMap,
  ...websiteComponentMap,
  ...orderComponentMap,
  ...permissionComponentMap,
  ...logisticsComponentMap,
  ...stockComponentMap,
  ...systemComponentMap,
  ...contentComponentMap,
  ...financeComponentMap,
  
  // 其他模块（待分配或共同维护）
  'deposit/Record': () => import('@/views/deposit/Record.vue'),
  'help/Index': () => import('@/views/help/Index.vue'),
  'announcement/Index': () => import('@/views/announcement/Index.vue'),
  
  // 以下组件文件尚未创建，待开发时添加：
  // - buyer/Level
  // - marketing/Promotion, marketing/Price
  // - statistics/Sales, statistics/Order, statistics/Product, statistics/Buyer
  // - system/Basic, system/Payment, system/Logistics, system/Notification
}

