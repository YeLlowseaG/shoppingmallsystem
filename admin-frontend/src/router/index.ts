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
          path: 'list',
          name: 'admin-buyer-list',
          component: () => import('@/views/buyer/List.vue'),
          meta: {
            title: '采购者列表',
            permission: 'admin:buyer:list'
          }
        },
        {
          path: 'audit',
          name: 'admin-buyer-audit',
          component: () => import('@/views/buyer/Audit.vue'),
          meta: {
            title: '采购者审核',
            permission: 'admin:buyer:audit'
          }
        },
        {
          path: 'logistics',
          name: 'admin-logistics',
          component: () => import('@/views/logistics/Index.vue'),
          meta: {
            title: '物流管理',
            permission: 'admin:logistics:list'
          }
        },
        {
          path: 'deposit',
          name: 'admin-deposit-record',
          component: () => import('@/views/deposit/Record.vue'),
          meta: {
            title: '预存款交易记录',
            permission: 'admin:deposit:list'
          }
        },
        {
          path: 'order/list',
          name: 'admin-order-list',
          component: () => import('@/views/order/List.vue'),
          meta: {
            title: '订单管理',
            permission: 'admin:order:list'
          }
        }
      ]
    },
    // 添加404路由，捕获所有未匹配的admin路径
    {
      path: '/admin/:pathMatch(.*)*',
      name: 'admin-404',
      component: () => import('@/components/Layout/index.vue'),
      meta: {
        requiresAuth: true
      },
      children: [
        {
          path: '',
          name: 'not-found',
          component: {
            render() {
              return null
            },
            mounted() {
              // 在组件挂载时重定向
              const adminStore = useAdminStore()
              if (adminStore.isLoggedIn()) {
                this.$router.replace('/admin/dashboard')
              } else {
                this.$router.replace('/admin/login')
              }
            }
          }
        }
      ]
    },
    // 处理根路径的404
    {
      path: '/:pathMatch(.*)*',
      name: '404',
      redirect: (to) => {
        // 如果是admin路径，重定向到admin登录页
        if (to.path.startsWith('/admin')) {
          return '/admin/login'
        }
        // 其他路径重定向到首页
        return '/'
      }
    }
  ]
})

// 组件映射表 - 将所有可能的组件路径预先定义（解决 Vite 动态导入问题）
// 注意：只包含实际存在的组件文件，不存在的组件会在运行时输出警告
const componentMap: Record<string, () => Promise<any>> = {
  // 仪表盘
  'dashboard/Index': () => import('@/views/dashboard/Index.vue'),
  
  // 商品管理
  'product/List': () => import('@/views/product/ProductManage.vue'),
  'product/Add': () => import('@/views/product/Add.vue'),
  'product/Category': () => import('@/views/product/CategoryManage.vue'),
  
  // 订单管理
  'order/List': () => import('@/views/order/List.vue'),
  
  // 采购者管理
  'buyer/List': () => import('@/views/buyer/List.vue'),
  'buyer/Audit': () => import('@/views/buyer/Audit.vue'),
  
  // 预存款管理
  'deposit/Record': () => import('@/views/deposit/Record.vue'),
  
  // 权限管理
  'permission/User': () => import('@/views/permission/User.vue'),
  'permission/Role': () => import('@/views/permission/Role.vue'),
  'permission/Menu': () => import('@/views/permission/Menu.vue'),
  
  // 物流管理
  'logistics/Index': () => import('@/views/logistics/Index.vue'),
  
  // 帮助中心管理
  'help/Index': () => import('@/views/help/Index.vue'),
  'announcement/Index': () => import('@/views/announcement/Index.vue'),
  
  // Website模块
  'website/Banner': () => import('@/views/website/Banner.vue'),
  'website/Brand': () => import('@/views/website/Brand.vue'),
  'website/Advertisement': () => import('@/views/website/Advertisement.vue'),
  
  // 以下组件文件尚未创建，待开发时添加：
  // - stock/List, stock/Warning, stock/Adjust, stock/Statistics
  // - buyer/Level
  // - marketing/Promotion, marketing/Price
  // - statistics/Sales, statistics/Order, statistics/Product, statistics/Buyer
  // - system/Basic, system/Payment, system/Logistics, system/Notification
}

// 动态添加路由
export const addRoutes = (menus: MenuVO[]) => {
  const buildRoutes = (menuList: MenuVO[], parentPath = '') => {
    menuList.forEach(menu => {
      if (menu.menuType === 1 && menu.path && menu.component) {
        // 从组件映射表中获取组件，如果不存在则使用默认组件或报错
        const componentLoader = componentMap[menu.component]
        
        if (!componentLoader) {
          console.warn(`组件 ${menu.component} 未在 componentMap 中定义，跳过路由添加`)
          return
        }
        
        // 构建相对路径（相对于 /admin）
        let routePath = menu.path
        // 如果路径是绝对路径（以 /admin 开头），需要转换为相对路径
        if (routePath.startsWith('/admin/')) {
          routePath = routePath.replace(/^\/admin\//, '')
        } else if (routePath.startsWith('/')) {
          // 如果路径以 / 开头但不是 /admin，去掉开头的 /
          routePath = routePath.replace(/^\//, '')
        }
        
        // 如果有父路径且当前路径不是绝对路径，则拼接父路径
        if (parentPath && !menu.path.startsWith('/admin/')) {
          // 如果父路径以 / 开头，去掉开头的 /
          const cleanParentPath = parentPath.startsWith('/') ? parentPath.replace(/^\//, '') : parentPath
          // 如果父路径以 /admin 开头，去掉 /admin 前缀
          const finalParentPath = cleanParentPath.startsWith('admin/') ? cleanParentPath.replace(/^admin\//, '') : cleanParentPath
          routePath = finalParentPath ? `${finalParentPath}/${routePath}` : routePath
        }
        
        // 特殊处理：如果路径是 'index' 且父路径是 'dashboard'，则路径应该是 'dashboard'
        // 因为静态路由中已经定义了 dashboard，避免重复添加
        if (menu.path === 'index' && parentPath === '/dashboard') {
          routePath = 'dashboard'
          // 检查路由是否已存在，避免重复添加
          const existingRoute = router.getRoutes().find(r => {
            const fullPath = r.path.startsWith('/') ? r.path : `/admin/${r.path}`
            return fullPath === '/admin/dashboard' && r.name === 'admin-dashboard'
          })
          if (existingRoute) {
            console.log(`路由 ${routePath} 已存在，跳过添加`)
            return
          }
        }
        
        // 检查路由是否已存在，避免重复添加
        const targetFullPath = routePath.startsWith('/') ? routePath : `/admin/${routePath}`
        const existingRoute = router.getRoutes().find(r => {
          const fullPath = r.path.startsWith('/') ? r.path : `/admin/${r.path}`
          return fullPath === targetFullPath
        })
        if (existingRoute) {
          console.log(`路由 ${routePath} 已存在，跳过添加`)
          return
        }
        
        const route = {
          path: routePath,
          name: `admin-${menu.permission?.replace(/:/g, '-')}`,
          component: componentLoader, // 使用映射表中的组件加载器
          meta: {
            title: menu.menuName,
            permission: menu.permission
          }
        }
        router.addRoute('admin', route)
      }
      if (menu.children && menu.children.length > 0) {
        // 构建父路径（用于子路由）
        let currentPath = parentPath
        if (menu.path) {
          if (menu.path.startsWith('/')) {
            currentPath = menu.path
          } else {
            currentPath = parentPath ? `${parentPath}/${menu.path}` : menu.path
          }
        }
        buildRoutes(menu.children, currentPath)
      }
    })
  }
  buildRoutes(menus)
}

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const adminStore = useAdminStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - B2B成人用品采购平台管理后台`
  }

  // 检查路由是否存在（排除404路由本身）
  const matched = to.matched.length > 0
  if (!matched && to.path.startsWith('/admin') && to.name !== 'admin-404' && to.name !== 'not-found') {
    // 路由不存在，根据登录状态重定向
    if (adminStore.isLoggedIn()) {
      ElMessage.warning('页面不存在，已跳转到首页')
      next('/admin/dashboard')
      return
    } else {
      next('/admin/login')
      return
    }
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

// 添加路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)
  const adminStore = useAdminStore()
  
  // 如果是组件加载失败，重定向到首页或登录页
  if (error.message && (
    error.message.includes('Failed to fetch dynamically imported module') ||
    error.message.includes('Loading chunk') ||
    error.message.includes('Failed to fetch')
  )) {
    ElMessage.error('页面加载失败，正在跳转...')
    if (adminStore.isLoggedIn()) {
      router.replace('/admin/dashboard')
    } else {
      router.replace('/admin/login')
    }
  }
})

export default router

