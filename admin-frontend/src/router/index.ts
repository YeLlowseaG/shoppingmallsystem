import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin/user'
import { ElMessage } from 'element-plus'
import type { MenuVO } from '@/api/admin/user'
import { componentMap } from './componentMap'

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
        // 所有路由都通过动态路由机制自动添加
        // 新增页面时，只需在数据库中插入菜单数据，并在 componentMaps/xxx.ts 中添加组件映射即可
        // 无需在此处手动添加静态路由
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

// 组件映射表已移至 componentMap.ts，按模块拆分以避免多人开发冲突
// 新增组件映射时，请在 router/componentMaps/ 目录下对应的模块文件中添加

// 动态添加路由
export const addRoutes = (menus: MenuVO[]) => {
  console.log('开始添加路由，菜单数量:', menus.length)
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
          // 如果父路径是 /dashboard，去掉开头的 /
          const normalizedParentPath = finalParentPath === 'dashboard' ? 'dashboard' : finalParentPath
          routePath = normalizedParentPath ? `${normalizedParentPath}/${routePath}` : routePath
        }
        
        // 特殊处理：如果路径是 'index' 且父路径是 'dashboard'，则路径应该是 'dashboard'
        if (menu.path === 'index' && (parentPath === '/dashboard' || parentPath === 'dashboard')) {
          routePath = 'dashboard'
        }
        
        // 检查路由是否已存在，避免重复添加
        const targetFullPath = routePath.startsWith('/') ? routePath : `/admin/${routePath}`
        // 获取父路由 'admin' 的所有子路由
        const adminRoute = router.getRoutes().find(r => r.name === 'admin')
        const existingRoute = adminRoute?.children?.find(r => {
          const fullPath = r.path.startsWith('/') ? r.path : `/admin/${r.path}`
          return fullPath === targetFullPath
        })
        if (existingRoute) {
          console.log(`路由 ${routePath} (${targetFullPath}) 已存在，跳过添加`)
          return
        }
        
        const route = {
          path: routePath,
          name: `admin-${menu.permission?.replace(/:/g, '-') || menu.id}`,
          component: componentLoader, // 使用映射表中的组件加载器
          meta: {
            title: menu.menuName,
            permission: menu.permission
          }
        }
        console.log('添加路由:', routePath, '->', targetFullPath, '组件:', menu.component)
        router.addRoute('admin', route)
      }
      if (menu.children && menu.children.length > 0) {
        // 构建父路径（用于子路由）
        let currentPath = parentPath
        if (menu.path) {
          if (menu.path.startsWith('/')) {
            currentPath = menu.path
          } else {
            // 特殊处理：如果父路径是 '/dashboard'，子路径是 'index'，则父路径应该是 'dashboard'
            if (menu.path === 'index' && parentPath === '/dashboard') {
              currentPath = 'dashboard'
            } else {
              currentPath = parentPath ? `${parentPath}/${menu.path}` : menu.path
            }
          }
        }
        buildRoutes(menu.children, currentPath)
      }
    })
  }
  buildRoutes(menus)
  console.log('路由添加完成，当前所有路由:', router.getRoutes().filter(r => r.path.startsWith('/admin') || (r.name && r.name.startsWith('admin-'))).map(r => ({ path: r.path, name: r.name })))
}

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const adminStore = useAdminStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - B2B成人用品采购平台管理后台`
  }

  // 调试日志：检查路由匹配情况
  console.log('路由守卫 - 当前路径:', to.path, '匹配的路由:', to.matched.map(r => r.path), '路由名称:', to.name)

  // 检查路由是否存在（排除404路由本身）
  const matched = to.matched.length > 0
  if (!matched && to.path.startsWith('/admin') && to.name !== 'admin-404' && to.name !== 'not-found') {
    // 路由不存在，尝试等待路由添加完成
    console.warn('路由未匹配，当前路径:', to.path, '已注册的路由:', router.getRoutes().filter(r => r.path.startsWith('/admin')).map(r => r.path))
    
    // 如果已登录且有菜单数据，可能是路由还未添加，等待一下
    if (adminStore.isLoggedIn() && adminStore.menus && adminStore.menus.length > 0) {
      // 检查是否需要添加路由
      const hasRoutes = router.getRoutes().some(r => {
        const routePath = r.path.startsWith('/') ? r.path : `/admin/${r.path}`
        return routePath === to.path || routePath === to.path + '/'
      })
      if (!hasRoutes) {
        console.log('路由未找到，尝试重新添加路由')
        // 重新添加路由（直接调用，避免循环依赖）
        addRoutes(adminStore.menus)
        // 等待路由添加完成后再检查
        await new Promise(resolve => setTimeout(resolve, 100))
        // 重新匹配路由
        const retryMatched = router.resolve(to.path).matched.length > 0
        if (retryMatched) {
          console.log('路由添加成功，继续导航')
          next(to.path)
          return
        }
      }
    }
    
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
