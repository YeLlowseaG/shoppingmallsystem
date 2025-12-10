<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>销售统计</span>
      </template>
      <el-empty description="销售统计功能开发中，敬请期待！" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

onMounted(() => {
  ElMessage.info('销售统计页面已加载')
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
