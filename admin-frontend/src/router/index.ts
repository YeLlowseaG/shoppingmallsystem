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
            title: '数据概览',
            permission: 'admin:dashboard:view'
          }
        },
        {
          path: 'permission/user',
          name: 'admin-permission-user',
          component: () => import('@/views/permission/User.vue'),
          meta: {
            title: '用户管理',
            permission: 'admin:permission:user:list'
          }
        },
        {
          path: 'permission/role',
          name: 'admin-permission-role',
          component: () => import('@/views/permission/Role.vue'),
          meta: {
            title: '角色管理',
            permission: 'admin:permission:role:list'
          }
        },
        {
          path: 'permission/menu',
          name: 'admin-permission-menu',
          component: () => import('@/views/permission/Menu.vue'),
          meta: {
            title: '菜单管理',
            permission: 'admin:permission:menu:list'
          }
        }
      ]
    }
  ]
})

// 动态添加路由
export const addRoutes = (menus: MenuVO[]) => {
  const buildRoutes = (menuList: MenuVO[], parentPath = '/admin') => {
    menuList.forEach(menu => {
      if (menu.menuType === '菜单' && menu.path && menu.component) {
        const route = {
          path: menu.path.startsWith('/') ? menu.path : `${parentPath}/${menu.path}`,
          name: `admin-${menu.permission?.replace(/:/g, '-')}`,
          component: () => import(`@/views/${menu.component}.vue`),
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

  // 检查权限
  if (to.meta.permission && adminStore.permissions) {
    const hasPermission = adminStore.permissions.includes(to.meta.permission as string)
    if (!hasPermission) {
      ElMessage.error('无权限访问')
      next('/admin/dashboard')
      return
    }
  }

  next()
})

export default router

