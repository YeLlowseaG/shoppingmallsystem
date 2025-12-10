<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>支付配置</span>
      </template>
      <el-empty description="支付配置功能开发中，敬请期待！" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

onMounted(() => {
  ElMessage.info('支付配置页面已加载')
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
