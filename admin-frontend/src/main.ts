import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/index.scss'

// 创建应用实例
const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用 Pinia 状态管理
app.use(createPinia())

// 使用 Element Plus
app.use(ElementPlus)

// 使用路由
app.use(router)

// 挂载到 DOM
app.mount('#app')

console.log('✅ B2B成人用品采购平台 - 管理后台已启动')
console.log('📍 访问地址: http://localhost:3003/admin/')
