import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin/user'

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
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: () => import('@/components/Layout/index.vue'),
      meta: {
        title: '仪表盘',
        requiresAuth: true
      },
      children: [
        {
          path: '',
          component: () => import('@/views/dashboard/Index.vue'),
          meta: {
            title: '仪表盘'
          }
        },
        {
          path: '/admin/product/category',
          name: 'product-category',
          component: () => import('@/views/product/CategoryManage.vue'),
          meta: {
            title: '商品分类管理',
            requiresAuth: true
          }
        },
        {
          path: '/admin/product/list',
          name: 'product-list',
          component: () => import('@/views/product/ProductManage.vue'),
          meta: {
            title: '商品管理',
            requiresAuth: true
          }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const adminStore = useAdminStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - B2B成人用品采购平台管理后台`
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth !== false && !adminStore.isLoggedIn()) {
    next('/admin/login')
  } else {
    // 如果已登录，访问登录页则跳转到仪表盘
    if (to.path === '/admin/login' && adminStore.isLoggedIn()) {
      next('/admin/dashboard')
    } else {
      next()
    }
  }
})

export default router

