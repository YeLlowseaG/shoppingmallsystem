import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import { useAdminStore } from './stores/admin/user'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})

// 初始化store
const adminStore = useAdminStore()
adminStore.init()

app.mount('#app')

// 添加全局错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('全局错误:', err, info)
  // 如果是路由相关错误，尝试重定向
  if (err && typeof err === 'object' && 'message' in err) {
    const errorMessage = String(err.message)
    if (errorMessage.includes('Failed to fetch') || 
        errorMessage.includes('Loading chunk') ||
        errorMessage.includes('dynamically imported module')) {
      if (adminStore.isLoggedIn()) {
        router.replace('/admin/dashboard')
      } else {
        router.replace('/admin/login')
      }
    }
  }
}

// 添加未捕获的Promise错误处理
window.addEventListener('unhandledrejection', (event) => {
  console.error('未处理的Promise错误:', event.reason)
  // 如果是组件加载失败，重定向
  if (event.reason && event.reason.message && 
      (event.reason.message.includes('Failed to fetch') || 
       event.reason.message.includes('Loading chunk') ||
       event.reason.message.includes('dynamically imported module'))) {
    if (adminStore.isLoggedIn()) {
      router.replace('/admin/dashboard')
    } else {
      router.replace('/admin/login')
    }
  }
})

