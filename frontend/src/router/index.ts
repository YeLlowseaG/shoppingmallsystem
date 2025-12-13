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
      path: '/reset-password',
      name: 'reset-password',
      component: () => import('@/views/auth/ResetPassword.vue'),
      meta: {
        title: '重置密码',
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
      path: '/products/:id',
      name: 'product-detail',
      component: () => import('@/views/products/Detail.vue'),
      meta: {
        title: '商品详情',
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
      path: '/member/settings/payment-password',
      name: 'member-payment-password',
      component: () => import('@/views/member/PaymentPassword.vue'),
      meta: {
        title: '修改预存款支付密码',
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
      path: '/member/transaction/orders',
      name: 'member-orders',
      component: () => import('@/views/member/Orders.vue'),
      meta: {
        title: '我的订单',
        requiresAuth: true
      }
    },
    {
      path: '/member/favorites/products',
      name: 'member-favorites',
      component: () => import('@/views/member/Favorites.vue'),
      meta: {
        title: '商品收藏',
        requiresAuth: true
      }
    },
    {
      path: '/member/favorites/out-of-stock',
      name: 'member-stock-notifications',
      component: () => import('@/views/member/StockNotifications.vue'),
      meta: {
        title: '缺货登记',
        requiresAuth: true
      }
    },
    {
      path: '/member/deposit/recharge',
      name: 'member-deposit-recharge',
      component: () => import('@/views/member/DepositRecharge.vue'),
      meta: {
        title: '预存款充值',
        requiresAuth: true
      }
    },
    {
      path: '/member/deposit/balance',
      name: 'member-deposit-balance',
      component: () => import('@/views/member/DepositBalance.vue'),
      meta: {
        title: '我的预存款',
        requiresAuth: true
      }
    },
    {
      path: '/member/reviews',
      name: 'member-reviews',
      component: () => import('@/views/member/Reviews.vue'),
      meta: {
        title: '我的评论',
        requiresAuth: true
      }
    },
    {
      path: '/member/consultations',
      name: 'member-consultations',
      component: () => import('@/views/member/Consultations.vue'),
      meta: {
        title: '我的咨询',
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
    },
    {
      path: '/order/message',
      name: 'order-message',
      component: () => import('@/views/order/OrderMessage.vue'),
      meta: {
        title: '订单消息',
        requiresAuth: true
      }
    },
    {
      path: '/help',
      name: 'help',
      component: () => import('@/views/help/Index.vue'),
      meta: {
        title: '帮助中心',
        requiresAuth: false
      }
    },
    {
      path: '/news',
      name: 'news',
      component: () => import('@/views/news/List.vue'),
      meta: {
        title: '最新公告',
        requiresAuth: false
      }
    },
    {
      path: '/news/:id',
      name: 'news-detail',
      component: () => import('@/views/news/Detail.vue'),
      meta: {
        title: '公告详情',
        requiresAuth: false
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
