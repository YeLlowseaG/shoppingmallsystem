<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>采购者统计</span>
      </template>
      <el-empty description="采购者统计功能开发中，敬请期待！" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

onMounted(() => {
  ElMessage.info('采购者统计页面已加载')
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
