<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { getPublicConfigs } from '@/api/buyer/systemConfig'

// 设置SEO meta标签
const setSEOMetaTags = async () => {
  try {
    const configs = await getPublicConfigs()
    
    // 设置keywords
    if (configs['site.keywords']) {
      let keywordsMeta = document.querySelector('meta[name="keywords"]')
      if (!keywordsMeta) {
        keywordsMeta = document.createElement('meta')
        keywordsMeta.setAttribute('name', 'keywords')
        document.head.appendChild(keywordsMeta)
      }
      keywordsMeta.setAttribute('content', configs['site.keywords'])
    }
    
    // 设置description
    if (configs['site.description']) {
      let descriptionMeta = document.querySelector('meta[name="description"]')
      if (!descriptionMeta) {
        descriptionMeta = document.createElement('meta')
        descriptionMeta.setAttribute('name', 'description')
        document.head.appendChild(descriptionMeta)
      }
      descriptionMeta.setAttribute('content', configs['site.description'])
    }
  } catch (error) {
    console.error('加载SEO配置失败:', error)
  }
}

onMounted(() => {
  setSEOMetaTags()
})
</script>

<style>
#app {
  width: 100%;
  height: 100%;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol',
    'Noto Color Emoji';
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>














































































