<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>基础配置</span>
      </template>
      <el-empty description="基础配置功能开发中，敬请期待！" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

onMounted(() => {
  ElMessage.info('基础配置页面已加载')
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
