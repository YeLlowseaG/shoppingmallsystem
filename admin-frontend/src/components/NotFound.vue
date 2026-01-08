<template>
  <div></div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin/user'
import { onMounted } from 'vue'

const router = useRouter()
const adminStore = useAdminStore()

// 在组件挂载时重定向
onMounted(() => {
  // 避免循环重定向（支持两种路径格式）
  const currentPath = router.currentRoute.value.path
  if (currentPath === '/admin/dashboard' || currentPath === '/admin/login' || 
      currentPath === '/dashboard' || currentPath === '/login' || currentPath === '/') {
    return
  }
  
  if (adminStore.isLoggedIn()) {
    router.replace('/admin/dashboard')
  } else {
    router.replace('/admin/login')
  }
})
</script>
