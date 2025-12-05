import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/home/Index.vue'),
      meta: {
        title: '首页',
        requiresAuth: false
      }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/Login.vue'),
      meta: {
        title: '登录',
        requiresAuth: false
      }
    },
    {
      path: '/register-agreement',
      name: 'register-agreement',
      component: () => import('@/views/auth/RegisterAgreement.vue'),
      meta: {
        title: '注册协议',
        requiresAuth: false
      }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/Register.vue'),
      meta: {
        title: '用户注册',
        requiresAuth: false
      }
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: () => import('@/views/auth/ForgotPassword.vue'),
      meta: {
        title: '忘记密码',
        requiresAuth: false
      }
    },
    {
      path: '/products',
      name: 'products',
      component: () => import('@/views/products/List.vue'),
      meta: {
        title: '商品列表',
        requiresAuth: false
      }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - B2B成人用品采购平台`
  }

  // 游客模式：默认允许访问，只有明确标记 requiresAuth: true 的页面才需要登录
  // 如果页面需要登录但用户未登录，跳转到登录页并保存原目标路径
  if (to.meta.requiresAuth === true && !userStore.isLoggedIn()) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } 
  // 如果已登录用户访问登录/注册页面，跳转到首页
  else if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn()) {
    next('/')
  } 
  // 其他情况正常访问（支持游客模式）
  else {
    next()
  }
})

export default router
