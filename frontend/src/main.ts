import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import { getPublicConfigs } from '@/api/buyer/systemConfig'

// 在应用创建之前设置SEO meta标签
const setSEOMetaTags = async () => {
  try {
    console.log('开始加载SEO配置...')
    const configs = await getPublicConfigs()
    console.log('获取到的配置:', configs)
    console.log('配置类型:', typeof configs, '是否为对象:', configs instanceof Object)
    
    if (!configs || typeof configs !== 'object') {
      console.error('配置数据格式错误:', configs)
      return
    }
    
    // 设置keywords
    if (configs['site.keywords']) {
      let keywordsMeta = document.querySelector('meta[name="keywords"]')
      if (!keywordsMeta) {
        keywordsMeta = document.createElement('meta')
        keywordsMeta.setAttribute('name', 'keywords')
        document.head.appendChild(keywordsMeta)
      }
      const oldValue = keywordsMeta.getAttribute('content')
      keywordsMeta.setAttribute('content', configs['site.keywords'])
      console.log('已更新keywords:', oldValue, '->', configs['site.keywords'])
    } else {
      console.warn('未找到site.keywords配置，当前配置键:', Object.keys(configs))
    }
    
    // 设置description
    if (configs['site.description']) {
      let descriptionMeta = document.querySelector('meta[name="description"]')
      if (!descriptionMeta) {
        descriptionMeta = document.createElement('meta')
        descriptionMeta.setAttribute('name', 'description')
        document.head.appendChild(descriptionMeta)
      }
      const oldValue = descriptionMeta.getAttribute('content')
      descriptionMeta.setAttribute('content', configs['site.description'])
      console.log('已更新description:', oldValue, '->', configs['site.description'])
    } else {
      console.warn('未找到site.description配置，当前配置键:', Object.keys(configs))
    }
  } catch (error: any) {
    console.error('加载SEO配置失败:', error)
    console.error('错误详情:', error?.message, error?.response?.data)
    // 如果加载失败，使用index.html中的默认值，不进行任何操作
  }
}

// 先设置SEO标签，再创建应用
setSEOMetaTags().then(() => {
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

  app.mount('#app')
})














































































