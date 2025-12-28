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
  if (adminStore.isLoggedIn()) {
    router.replace('/dashboard')
  } else {
    router.replace('/admin/login')
  }
})
</script>
