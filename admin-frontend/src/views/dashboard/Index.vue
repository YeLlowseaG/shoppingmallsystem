<template>
  <div class="dashboard">
    <h1>数据概览</h1>
    
    <!-- 如果没有权限，显示提示信息 -->
    <el-alert
      v-if="!hasPermissions"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 20px;"
    >
      <template #title>
        <span>您还没有分配角色，请联系管理员分配角色和权限</span>
      </template>
    </el-alert>
    
    <!-- 有权限时显示数据统计 -->
    <template v-if="hasPermissions">
    <!-- 核心指标 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF;">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.todayOrders || 0) }}</div>
              <div class="stat-label">今日订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A;">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatCurrency(stats.todaySales || 0) }}</div>
              <div class="stat-label">今日销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C;">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.pendingOrders || 0) }}</div>
              <div class="stat-label">待处理订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C;">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.stockWarnings || 0) }}</div>
              <div class="stat-label">库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 总体数据 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #909399;">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.totalOrders || 0) }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A;">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatCurrency(stats.totalSales || 0) }}</div>
              <div class="stat-label">总销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF;">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.totalUsers || 0) }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C;">
              <el-icon><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.totalProducts || 0) }}</div>
              <div class="stat-label">总商品数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近7天销售趋势</span>
          </template>
          <div ref="salesChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近7天订单趋势</span>
          </template>
          <div ref="orderChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>订单状态分布</span>
          </template>
          <div ref="orderStatusChartRef" class="chart-container-large"></div>
        </el-card>
      </el-col>
    </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick, onBeforeUnmount } from 'vue'
import { useAdminStore } from '@/stores/admin/user'
import { Document, Money, Clock, Warning, User, Box } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getDashboardStatistics, type DashboardVO } from '@/api/admin/dashboard'

const adminStore = useAdminStore()

// 检查用户是否有权限
const hasPermissions = computed(() => {
  return adminStore.permissions && adminStore.permissions.length > 0
})

// 统计数据
const stats = ref<DashboardVO>({
  todayOrders: 0,
  todaySales: 0,
  pendingOrders: 0,
  stockWarnings: 0,
  totalOrders: 0,
  totalSales: 0,
  totalUsers: 0,
  totalProducts: 0,
  salesTrend: [],
  orderTrend: [],
  orderStatusStatistics: {
    pendingPayment: 0,
    paidNotShipped: 0,
    shipped: 0,
    completed: 0,
    cancelled: 0
  }
})

// 图表引用
const salesChartRef = ref<HTMLElement>()
const orderChartRef = ref<HTMLElement>()
const orderStatusChartRef = ref<HTMLElement>()

// 图表实例
let salesChart: echarts.EChartsInstance | null = null
let orderChart: echarts.EChartsInstance | null = null
let orderStatusChart: echarts.EChartsInstance | null = null

// 加载状态
const loading = ref(false)

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}万`
  }
  return num.toString()
}

// 格式化货币
const formatCurrency = (amount: number): string => {
  if (amount >= 100000000) {
    return `${(amount / 100000000).toFixed(2)}亿`
  }
  if (amount >= 10000) {
    return `${(amount / 10000).toFixed(2)}万`
  }
  return amount.toFixed(2)
}

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    const data = await getDashboardStatistics()
    stats.value = data
    
    // 等待DOM更新后初始化图表
    await nextTick()
    initCharts()
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化销售趋势图表
const initSalesChart = () => {
  if (!salesChartRef.value) return
  
  salesChart = echarts.init(salesChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const param = params[0]
        return `${param.name}<br/>${param.marker}销售额: ¥${formatCurrency(param.value)}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: stats.value.salesTrend.map(item => item.date)
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => {
          if (value >= 10000) {
            return `¥${(value / 10000).toFixed(1)}万`
          }
          return `¥${value}`
        }
      }
    },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: stats.value.salesTrend.map(item => item.sales),
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
              { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
            ]
          }
        },
        itemStyle: {
          color: '#67C23A'
        },
        lineStyle: {
          color: '#67C23A',
          width: 2
        }
      }
    ]
  }
  
  salesChart.setOption(option)
}

// 初始化订单趋势图表
const initOrderChart = () => {
  if (!orderChartRef.value) return
  
  orderChart = echarts.init(orderChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const param = params[0]
        return `${param.name}<br/>${param.marker}订单数: ${param.value}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: stats.value.orderTrend.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: stats.value.orderTrend.map(item => item.count),
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ]
          }
        },
        itemStyle: {
          color: '#409EFF'
        },
        lineStyle: {
          color: '#409EFF',
          width: 2
        }
      }
    ]
  }
  
  orderChart.setOption(option)
}

// 初始化订单状态分布图表
const initOrderStatusChart = () => {
  if (!orderStatusChartRef.value) return
  
  orderStatusChart = echarts.init(orderStatusChartRef.value)
  
  const statusData = stats.value.orderStatusStatistics
  const data = [
    { value: statusData.pendingPayment, name: '待付款' },
    { value: statusData.paidNotShipped, name: '已付款未发货' },
    { value: statusData.shipped, name: '已发货' },
    { value: statusData.completed, name: '已完成' },
    { value: statusData.cancelled, name: '已取消' }
  ]
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 'bottom',
      data: ['待付款', '已付款未发货', '已发货', '已完成', '已取消']
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}\n({d}%)'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: data,
        color: ['#E6A23C', '#409EFF', '#67C23A', '#909399', '#F56C6C']
      }
    ]
  }
  
  orderStatusChart.setOption(option)
}

// 初始化所有图表
const initCharts = () => {
  initSalesChart()
  initOrderChart()
  initOrderStatusChart()
  
  // 监听窗口大小变化，自动调整图表大小
  window.addEventListener('resize', handleResize)
}

// 处理窗口大小变化
const handleResize = () => {
  salesChart?.resize()
  orderChart?.resize()
  orderStatusChart?.resize()
}

onMounted(() => {
  if (hasPermissions.value) {
    loadStatistics()
  }
})

onBeforeUnmount(() => {
  // 清理图表实例
  salesChart?.dispose()
  orderChart?.dispose()
  orderStatusChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

h1 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.charts-row {
  margin-top: 20px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.chart-container-large {
  height: 400px;
  width: 100%;
}
</style>

