<template>
  <div class="stock-statistics">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存统计</span>
          <el-button type="primary" @click="loadStatistics">刷新</el-button>
        </div>
      </template>

      <div v-loading="loading" class="statistics-content">
        <!-- 统计卡片 -->
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-label">商品总数</div>
                <div class="stat-value">{{ statistics.totalProducts || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-label">总库存数量</div>
                <div class="stat-value primary">{{ statistics.totalStock || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-label">可用库存总数</div>
                <div class="stat-value success">{{ statistics.totalAvailableStock || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-label">锁定库存总数</div>
                <div class="stat-value warning">{{ statistics.totalLockedStock || 0 }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="12">
            <el-card class="stat-card danger">
              <div class="stat-item">
                <div class="stat-label">预警商品数量</div>
                <div class="stat-value danger">{{ statistics.warningProductCount || 0 }}</div>
                <div class="stat-desc">可用库存 ≤ 预警阈值</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="stat-card danger">
              <div class="stat-item">
                <div class="stat-label">缺货商品数量</div>
                <div class="stat-value danger">{{ statistics.outOfStockCount || 0 }}</div>
                <div class="stat-desc">可用库存 = 0</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 统计说明 -->
        <el-card style="margin-top: 20px">
          <template #header>
            <span>统计说明</span>
          </template>
          <div class="stat-description">
            <ul>
              <li><strong>商品总数</strong>：有库存记录的商品数量</li>
              <li><strong>总库存数量</strong>：所有商品的总库存之和</li>
              <li><strong>可用库存总数</strong>：总库存减去锁定库存后的可用库存总和</li>
              <li><strong>锁定库存总数</strong>：已下单但未发货的库存数量</li>
              <li><strong>预警商品数量</strong>：可用库存小于等于预警阈值的商品数量</li>
              <li><strong>缺货商品数量</strong>：可用库存为0的商品数量</li>
            </ul>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStockStatistics, type StockStatisticsVO } from '@/api/admin/stock'

const loading = ref(false)
const statistics = ref<StockStatisticsVO>({
  totalProducts: 0,
  totalStock: 0,
  totalAvailableStock: 0,
  totalLockedStock: 0,
  warningProductCount: 0,
  outOfStockCount: 0
})

// 加载统计信息
const loadStatistics = async () => {
  loading.value = true
  try {
    const data = await getStockStatistics()
    statistics.value = data
  } catch (error) {
    ElMessage.error('加载统计信息失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.stock-statistics {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 18px;
    font-weight: bold;
  }

  .statistics-content {
    min-height: 400px;
  }

  .stat-card {
    text-align: center;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }

    &.danger {
      border-left: 4px solid #f56c6c;
    }
  }

  .stat-item {
    padding: 20px 0;
  }

  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-bottom: 10px;
  }

  .stat-value {
    font-size: 32px;
    font-weight: bold;
    color: #303133;

    &.primary {
      color: #409eff;
    }

    &.success {
      color: #67c23a;
    }

    &.warning {
      color: #e6a23c;
    }

    &.danger {
      color: #f56c6c;
    }
  }

  .stat-desc {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
  }

  .stat-description {
    ul {
      margin: 0;
      padding-left: 20px;

      li {
        margin-bottom: 10px;
        line-height: 1.8;
      }
    }
  }
}
</style>
