<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>库存预警</span>
      </template>
      <el-empty description="库存预警功能开发中，敬请期待！" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

onMounted(() => {
  ElMessage.info('库存预警页面已加载')
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
