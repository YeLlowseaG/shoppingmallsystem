import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin/user'
import { ElMessage } from 'element-plus'
import { nextTick } from 'vue'
import type { MenuVO } from '@/api/admin/user'
import { componentMap } from './componentMap'

// 防止重复添加路由的标志
let isAddingRoutes = false
// 记录最近添加路由的时间，避免短时间内重复添加
let lastRouteAddTime = 0

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 根路径登录页（支持独立子域名访问）
    {
      path: '/login',
      redirect: '/admin/login'
    },
    // 原有的 /admin/login 路径（兼容开发环境）
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('@/views/auth/Login.vue'),
      meta: {
        title: '管理员登录',
        requiresAuth: false
      }
    },
    // 根路径 - 在路由守卫中根据登录状态处理
    // 定义一个空路由，让路由守卫处理跳转逻辑
    {
      path: '/',
      name: 'root',
      component: () => import('@/components/NotFound.vue'), // 临时组件，实际不会渲染，路由守卫会拦截跳转
      meta: {
        requiresAuth: false
      }
    },
    // 原有的 /admin 路径（兼容开发环境）
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
        // 注意：404路由会在动态路由添加之后自动添加，不要在这里预先定义
      ]
    },
    // 处理根路径的404（支持独立子域名）
    {
      path: '/:pathMatch(.*)*',
      name: '404',
      redirect: (to) => {
        // 如果是admin路径，重定向到admin登录页
        if (to.path.startsWith('/admin')) {
          return '/admin/login'
        }
        // 根路径不在404路由中处理，由路由守卫处理
        // 其他路径重定向到登录页
        return '/admin/login'
      }
    }
  ]
})

// 组件映射表已移至 componentMap.ts，按模块拆分以避免多人开发冲突
// 新增组件映射时，请在 router/componentMaps/ 目录下对应的模块文件中添加

// 动态添加路由
export const addRoutes = (menus: MenuVO[]) => {
  // 如果正在添加路由，直接返回，避免重复添加
  if (isAddingRoutes) {
    console.log('路由正在添加中，跳过重复添加')
    return
  }
  
  isAddingRoutes = true
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
        // 只在开发环境输出详细日志
        if (import.meta.env.DEV) {
          console.log('添加路由:', routePath, '->', targetFullPath, '组件:', menu.component)
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
  
  // ✅ 关键修复：在动态路由添加之后，再添加404路由
  // 这样可以确保所有动态路由都已注册，避免刷新时误跳转到404
  const existing404Route = router.getRoutes().find(r => r.name === 'admin-404')
  if (!existing404Route) {
    router.addRoute('admin', {
      path: ':pathMatch(.*)*',
      name: 'admin-404',
      component: () => import('@/components/NotFound.vue'),
      meta: {
        requiresAuth: false
      }
    })
    if (import.meta.env.DEV) {
      console.log('✅ 404路由已添加（在动态路由之后）')
    }
  }
  
  // 验证路由是否已添加
  const adminRoute = router.getRoutes().find(r => r.name === 'admin')
  const childrenCount = adminRoute?.children?.length || 0
  
  // 只在开发环境输出详细日志
  if (import.meta.env.DEV) {
    console.log('路由添加完成，当前所有路由:', router.getRoutes().filter(r => {
      const path = typeof r.path === 'string' ? r.path : ''
      const name = typeof r.name === 'string' ? r.name : ''
      return path.startsWith('/admin') || name.startsWith('admin-')
    }).map(r => ({ path: r.path, name: r.name })))
    console.log('路由添加验证：admin 路由的子路由数量:', childrenCount)
    if (childrenCount > 0 && adminRoute?.children) {
      console.log('admin 路由的子路由列表:', adminRoute.children.map(r => ({ path: r.path, name: r.name })))
    }
  }
  
  isAddingRoutes = false
}

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const adminStore = useAdminStore()
  
  // 确保 store 已初始化（从 localStorage 恢复数据）
  if (!adminStore.menus || adminStore.menus.length === 0) {
    // 如果菜单数据为空，尝试初始化
    const savedMenus = localStorage.getItem('admin_menus')
    if (savedMenus && savedMenus !== 'null' && savedMenus !== 'undefined') {
      try {
        const menus = JSON.parse(savedMenus)
        adminStore.setMenus(menus)
        console.log('路由守卫：从 localStorage 恢复菜单数据，数量:', menus.length)
      } catch (error) {
        console.error('路由守卫：解析菜单数据失败:', error)
      }
    }
  }
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - B2B成人用品采购平台管理后台`
  }

  // 调试日志：检查路由匹配情况
  console.log('路由守卫 - 当前路径:', to.path, '匹配的路由:', to.matched.map(r => r.path), '路由名称:', to.name)

  // 优先处理根路径：根据登录状态决定跳转
  if (to.path === '/') {
    if (adminStore.isLoggedIn()) {
      // 已登录，跳转到 dashboard
      console.log('根路径访问：已登录，跳转到 dashboard')
      next('/admin/dashboard')
      return
    } else {
      // 未登录，跳转到登录页
      console.log('根路径访问：未登录，跳转到登录页')
      next('/admin/login')
      return
    }
  }

  // ========== 关键修复：刷新页面时，优先检查并添加路由 ==========
  // 如果已登录且有菜单数据，但路由未添加，先添加路由
  // 这必须在所有其他检查之前执行，确保路由已添加
  // 避免短时间内重复添加路由（防止死循环）
  const now = Date.now()
  const timeSinceLastAdd = now - lastRouteAddTime
  // ✅ 排除登录页，避免在登录页触发路由添加和重新导航
  const isLoginPage = to.path === '/admin/login' || to.path === '/login'
  if (adminStore.isLoggedIn() && adminStore.menus && adminStore.menus.length > 0 && !isAddingRoutes && timeSinceLastAdd > 1000 && !isLoginPage) {
    const adminRoute = router.getRoutes().find(r => r.name === 'admin')
    const hasChildren = adminRoute?.children && adminRoute.children.length > 0
    
    // 检查当前路由是否匹配到了404路由
    const is404Route = to.name === 'admin-404' || to.name === '404'
    
    // 如果没有子路由，或者匹配到了404路由，需要添加路由
    if (!hasChildren || (is404Route && to.path.startsWith('/admin') && to.path !== '/admin/login')) {
      console.log('刷新页面：检测到路由未添加或匹配到404，正在添加路由...', {
        hasChildren,
        is404Route,
        path: to.path
      })
      
      lastRouteAddTime = Date.now()
      addRoutes(adminStore.menus)
      
      // 等待路由添加完成
      await nextTick()
      await new Promise(resolve => setTimeout(resolve, 100))
      
      // ✅ 使用 next({ ...to, replace: true }) 重新导航，确保路由正确匹配
      console.log('刷新页面：路由添加完成，重新导航到:', to.path)
      next({ ...to, replace: true })
      return
    }
  }

  // 优先处理登录页：如果是登录页，直接允许访问（无论是否登录）
  // 如果已登录访问登录页，会跳转到 dashboard
  if (to.path === '/admin/login' || to.path === '/login') {
    // 如果已登录，跳转到 dashboard
    // 注意：路由添加逻辑由路由守卫的早期检查处理，这里直接跳转即可
    if (adminStore.isLoggedIn()) {
      // 如果有菜单数据但路由未添加，先添加路由
      if (adminStore.menus && adminStore.menus.length > 0 && !isAddingRoutes) {
        const adminRoute = router.getRoutes().find(r => r.name === 'admin')
        const hasChildren = adminRoute?.children && adminRoute.children.length > 0
        
        // ✅ 使用 router.resolve 检查路由是否存在，而不是检查 children 属性
        const resolved = router.resolve('/admin/dashboard')
        const dashboardExists = resolved.matched.length > 0 && 
                                resolved.matched.some(r => {
                                  const routeName = r.name
                                  return routeName && routeName !== 'admin-404' && routeName !== '404' && routeName !== 'not-found' && routeName !== 'root'
                                })
        
        if (!hasChildren || !dashboardExists) {
          // 路由未添加，先添加路由
          console.log('登录页：路由未添加，正在添加路由...')
          addRoutes(adminStore.menus)
          // 等待路由添加完成
          await nextTick()
          await new Promise(resolve => setTimeout(resolve, 100))
        }
      }
      
      // 直接跳转到 dashboard（路由守卫会处理路由添加和匹配）
      console.log('登录页：已登录用户，跳转到 dashboard')
      next('/admin/dashboard')
      return
    }
    // 未登录，直接允许访问登录页
    next()
    return
  }

  // 检查是否需要登录（在检查路由匹配之前）
  // 如果路由需要认证但用户未登录，直接跳转到登录页
  if (to.meta.requiresAuth !== false && !adminStore.isLoggedIn()) {
    // 排除登录页本身，避免循环
    if (to.path !== '/admin/login' && to.path !== '/login') {
      next('/admin/login')
      return
    }
  }

  // 检查路由是否存在（排除404路由本身）
  const matched = to.matched.length > 0
  
  // 特殊处理：如果是 dashboard 路径且已登录，确保路由已添加
  if ((to.path === '/admin/dashboard' || to.path === '/dashboard' || to.path === '/') && adminStore.isLoggedIn()) {
    // 如果有菜单数据，确保路由已添加
    if (adminStore.menus && adminStore.menus.length > 0) {
      // 如果正在添加路由，直接允许访问，避免重复添加
      if (isAddingRoutes) {
        console.log('路由正在添加中，直接允许访问')
        next()
        return
      }
      
      // 检查 dashboard 路由是否已添加（更准确的查找方式）
      const adminRoute = router.getRoutes().find(r => r.name === 'admin')
      const dashboardRoute = adminRoute?.children?.find(r => {
        // 检查路由路径：相对路径 'dashboard' 或绝对路径
        const routePath = r.path.startsWith('/') ? r.path : `/admin/${r.path}`
        return routePath === '/admin/dashboard' || r.path === 'dashboard'
      })
      
      // 如果路由已匹配，说明路由存在，直接允许访问
      if (matched && to.matched.some(r => r.path === '/admin/dashboard' || r.path === '/admin')) {
        next()
        return
      }
      
      if (!dashboardRoute && !matched) {
        console.log('dashboard 路由未找到，正在添加路由...')
        addRoutes(adminStore.menus)
        // 等待路由添加完成
        await new Promise(resolve => setTimeout(resolve, 300))
        // 重新匹配路由
        const retryMatched = router.resolve('/admin/dashboard').matched.length > 0
        if (retryMatched) {
          console.log('dashboard 路由添加成功，重新导航')
          next({ ...to, replace: true })
          return
        } else {
          console.error('dashboard 路由添加失败，可能菜单数据中没有 dashboard 或组件映射有问题')
          // 即使路由未匹配，也允许访问，Layout 会显示提示
          next()
          return
        }
      }
    }
  }
  
  // 支持根路径和 /admin 路径两种格式
  // 排除登录页和根路径重定向
  const isAdminPath = to.path.startsWith('/admin') && to.path !== '/admin/login'
  if (!matched && isAdminPath && to.name !== 'admin-404' && to.name !== 'not-found' && to.name !== '404') {
    // 路由不存在，尝试等待路由添加完成
    console.warn('路由未匹配，当前路径:', to.path, '已注册的路由:', router.getRoutes().filter(r => r.path.startsWith('/admin')).map(r => r.path))
    
    // 如果已登录且有菜单数据，可能是路由还未添加，等待一下
    if (adminStore.isLoggedIn() && adminStore.menus && adminStore.menus.length > 0) {
      // 如果正在添加路由，直接允许访问，避免重复添加
      if (isAddingRoutes) {
        console.log('路由正在添加中，直接允许访问')
        next()
        return
      }
      
      // 检查是否需要添加路由（支持两种路径格式）
      const normalizedPath = to.path.startsWith('/admin') ? to.path : `/admin${to.path === '/' ? '/dashboard' : to.path}`
      const adminRoute = router.getRoutes().find(r => r.name === 'admin')
      const hasRoutes = adminRoute?.children?.some(r => {
        const routePath = r.path.startsWith('/') ? r.path : `/admin/${r.path}`
        return routePath === normalizedPath || routePath === normalizedPath + '/' || routePath === to.path || routePath === to.path + '/'
      })
      
      if (!hasRoutes) {
        console.log('路由未找到，尝试重新添加路由')
        // 重新添加路由（直接调用，避免循环依赖）
        addRoutes(adminStore.menus)
        // 等待路由添加完成后再检查
        await nextTick()
        await new Promise(resolve => setTimeout(resolve, 300))
        // 重新匹配路由，检查是否匹配到有效路由（不是404）
        const resolved = router.resolve(to.path)
        const retryMatched = resolved.matched.length > 0 && 
                            resolved.matched.some(r => {
                              const routeName = r.name
                              return routeName && routeName !== 'admin-404' && routeName !== '404' && routeName !== 'not-found' && routeName !== 'root'
                            })
        if (retryMatched) {
          console.log('路由添加成功，继续导航')
          next({ ...to, replace: true })
          return
        } else {
          // 路由添加后仍然未匹配，但可能是路由路径问题，允许访问让 Layout 处理
          console.warn('路由添加后仍然未匹配，允许访问，Layout 会处理', {
            path: to.path,
            matchedRoutes: resolved.matched.map(r => ({ path: r.path, name: r.name }))
          })
          next()
          return
        }
      } else {
        // 路由已存在但未匹配，可能是路由路径问题，允许访问让 Layout 处理
        console.warn('路由已存在但未匹配，允许访问，Layout 会处理', {
          path: to.path,
          hasRoutes: true
        })
        next()
        return
      }
    }
    
    // 路由不存在，根据登录状态重定向（避免循环）
    // 支持根路径和 /admin 路径
    if (to.path === '/admin/dashboard' || to.path === '/dashboard' || to.path === '/') {
      // 如果目标路径就是dashboard，且已登录，允许访问（Layout 会处理）
      if (adminStore.isLoggedIn()) {
        console.warn('dashboard 路由未匹配，但允许访问，Layout 会显示提示')
        next()
        return
      } else {
        // 未登录访问 dashboard，跳转到登录页
        next('/admin/login')
        return
      }
    }
    
    // 只有在真正找不到路由且未登录时才跳转
    if (!adminStore.isLoggedIn()) {
      next('/admin/login')
      return
    }
    
    // 已登录但路由不存在，允许访问让 Layout 处理（Layout 会显示404或重定向）
    console.warn('路由不存在，但允许访问，Layout 会处理')
    next()
    return
  }

  // 检查权限（需要权限的页面）
  if (to.meta.permission) {
    // 如果没有权限列表或权限列表为空，说明用户没有分配角色
    if (!adminStore.permissions || adminStore.permissions.length === 0) {
      // 如果用户已登录但没有权限，显示友好提示，但不退出登录
      // 允许访问dashboard页面，在页面上显示提示信息（支持两种路径格式）
      if (to.path === '/admin/dashboard' || to.path === '/dashboard') {
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
