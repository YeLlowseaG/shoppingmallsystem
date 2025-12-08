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
    },
    {
      path: '/member',
      name: 'member',
      component: () => import('@/views/member/Index.vue'),
      meta: {
        title: '会员中心',
        requiresAuth: true
      }
    },
    {
      path: '/member/settings/password',
      name: 'member-password',
      component: () => import('@/views/member/Password.vue'),
      meta: {
        title: '修改密码',
        requiresAuth: true
      }
    },
    {
      path: '/member/settings/profile',
      name: 'member-profile',
      component: () => import('@/views/member/Profile.vue'),
      meta: {
        title: '个人信息',
        requiresAuth: true
      }
    },
    {
      path: '/member/settings/address',
      name: 'member-address',
      component: () => import('@/views/member/Address.vue'),
      meta: {
        title: '收货地址',
        requiresAuth: true
      }
    },
    {
      path: '/member/settings/address/edit',
      name: 'member-address-edit',
      component: () => import('@/views/member/AddressEdit.vue'),
      meta: {
        title: '收货地址',
        requiresAuth: true
      }
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('@/views/cart/Index.vue'),
      meta: {
        title: '购物车',
        requiresAuth: false
      }
    },
    {
      path: '/cart/checkout',
      name: 'checkout',
      component: () => import('@/views/cart/Checkout.vue'),
      meta: {
        title: '填写购物信息',
        requiresAuth: true
      }
    },
    {
      path: '/order/payment',
      name: 'order-payment',
      component: () => import('@/views/order/Payment.vue'),
      meta: {
        title: '订单支付',
        requiresAuth: true
      }
    },
    {
      path: '/order/detail',
      name: 'order-detail',
      component: () => import('@/views/order/Detail.vue'),
      meta: {
        title: '订单详情',
        requiresAuth: true
      }
    }
  ]
})

// 路由守卫
router.beforeEach((to, _from, next) => {
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
