import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin/user'
import { ElMessage } from 'element-plus'
import type { MenuVO } from '@/api/admin/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('@/views/auth/Login.vue'),
      meta: {
        title: '管理员登录',
        requiresAuth: false
      }
    },
    {
      path: '/admin',
      redirect: '/admin/dashboard'
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/components/Layout/index.vue'),
      meta: {
        requiresAuth: true
      },
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/dashboard/Index.vue'),
          meta: {
            title: '数据概览'
            // dashboard不需要权限，所有已登录用户都可以访问
          }
        },
        {
          path: 'user',
          name: 'admin-permission-user',
          component: () => import('@/views/permission/User.vue'),
          meta: {
            title: '用户管理',
            permission: 'admin:permission:user:list'
          }
        },
        {
          path: 'role',
          name: 'admin-permission-role',
          component: () => import('@/views/permission/Role.vue'),
          meta: {
            title: '角色管理',
            permission: 'admin:permission:role:list'
          }
        },
        {
          path: 'menu',
          name: 'admin-permission-menu',
          component: () => import('@/views/permission/Menu.vue'),
          meta: {
            title: '菜单管理',
            permission: 'admin:permission:menu:list'
          }
        },
        {
          path: 'product/category',
          name: 'admin-product-category',
          component: () => import('@/views/product/CategoryManage.vue'),
          meta: {
            title: '商品分类管理',
            permission: 'admin:product:category:list'
          }
        },
        {
          path: 'product/list',
          name: 'admin-product-list',
          component: () => import('@/views/product/ProductManage.vue'),
          meta: {
            title: '商品管理',
            permission: 'admin:product:list'
          }
        },
        {
          path: 'buyer/list',
          name: 'admin-buyer-list',
          component: () => import('@/views/buyer/List.vue'),
          meta: {
            title: '采购者列表',
            permission: 'admin:buyer:list'
          }
        },
        {
          path: 'buyer/audit',
          name: 'admin-buyer-audit',
          component: () => import('@/views/buyer/Audit.vue'),
          meta: {
            title: '采购者审核',
            permission: 'admin:buyer:audit'
          }
        }
      ]
    }
  ]
})

// 动态添加路由
export const addRoutes = (menus: MenuVO[]) => {
  // 创建组件映射表（避免Vite动态导入限制）
  const componentMap: Record<string, any> = {
    'dashboard/Index': () => import('@/views/dashboard/Index.vue'),
    'product/ProductManage': () => import('@/views/product/ProductManage.vue'),
    'product/CategoryManage': () => import('@/views/product/CategoryManage.vue'),
    'product/Add': () => import('@/views/product/Add.vue'),
    'buyer/List': () => import('@/views/buyer/List.vue'),
    'buyer/Audit': () => import('@/views/buyer/Audit.vue'),
    'buyer/Level': () => import('@/views/buyer/Level.vue'),
    'permission/User': () => import('@/views/permission/User.vue'),
    'permission/Role': () => import('@/views/permission/Role.vue'),
    'permission/Menu': () => import('@/views/permission/Menu.vue'),
    'order/List': () => import('@/views/order/List.vue'),
    'stock/List': () => import('@/views/stock/List.vue'),
    'stock/Warning': () => import('@/views/stock/Warning.vue'),
    'stock/Adjust': () => import('@/views/stock/Adjust.vue'),
    'stock/Statistics': () => import('@/views/stock/Statistics.vue'),
    'marketing/Promotion': () => import('@/views/marketing/Promotion.vue'),
    'marketing/Price': () => import('@/views/marketing/Price.vue'),
    'statistics/Sales': () => import('@/views/statistics/Sales.vue'),
    'statistics/Order': () => import('@/views/statistics/Order.vue'),
    'statistics/Product': () => import('@/views/statistics/Product.vue'),
    'statistics/Buyer': () => import('@/views/statistics/Buyer.vue'),
    'system/Basic': () => import('@/views/system/Basic.vue'),
    'system/Payment': () => import('@/views/system/Payment.vue'),
    'system/Logistics': () => import('@/views/system/Logistics.vue'),
    'system/Notification': () => import('@/views/system/Notification.vue')
  }

  const buildRoutes = (menuList: MenuVO[], parentPath = '/admin') => {
    menuList.forEach(menu => {
      if (menu.menuType === 1 && menu.path && menu.component) {
        const componentLoader = componentMap[menu.component]
        if (!componentLoader) {
          console.warn(`组件 ${menu.component} 未在映射表中找到`)
          return
        }

        const route = {
          path: menu.path.startsWith('/') ? menu.path : `${parentPath}/${menu.path}`,
          name: `admin-${menu.permission?.replace(/:/g, '-')}`,
          component: componentLoader,
          meta: {
            title: menu.menuName,
            permission: menu.permission
          }
        }
        router.addRoute('admin', route)
      }
      if (menu.children && menu.children.length > 0) {
        const currentPath = menu.path ? `${parentPath}/${menu.path}` : parentPath
        buildRoutes(menu.children, currentPath)
      }
    })
  }
  buildRoutes(menus)
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const adminStore = useAdminStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - B2B成人用品采购平台管理后台`
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth !== false && !adminStore.isLoggedIn()) {
    next('/admin/login')
    return
  }

  // 如果已登录，访问登录页则跳转到仪表盘
  if (to.path === '/admin/login' && adminStore.isLoggedIn()) {
    next('/admin/dashboard')
    return
  }

  // 检查权限（需要权限的页面）
  if (to.meta.permission) {
    // 如果没有权限列表或权限列表为空，说明用户没有分配角色
    if (!adminStore.permissions || adminStore.permissions.length === 0) {
      // 如果用户已登录但没有权限，显示友好提示，但不退出登录
      // 允许访问dashboard页面，在页面上显示提示信息
      if (to.path === '/admin/dashboard') {
        // dashboard页面允许访问，在页面上显示提示
        next()
        return
      }
      // 其他需要权限的页面，显示提示并跳转到dashboard
      ElMessage.warning('您还没有分配角色，请联系管理员分配角色和权限')
      next('/admin/dashboard')
      return
    }
    
    // 检查是否有具体权限
    const hasPermission = adminStore.permissions.includes(to.meta.permission as string)
    if (!hasPermission) {
      ElMessage.warning('无权限访问该页面')
      next('/admin/dashboard')
      return
    }
  }

  next()
})

export default router

