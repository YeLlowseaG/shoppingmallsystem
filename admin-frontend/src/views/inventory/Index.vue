<template>
  <div class="inventory-manage">
    <!-- 库存报告 -->
    <el-row :gutter="20" class="inventory-stats">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon size="24"><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(inventoryStats.totalValue) }}</div>
              <div class="stat-label">总库存价值</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon low">
              <el-icon size="24"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ inventoryStats.lowStockCount }}</div>
              <div class="stat-label">低库存商品</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon out">
              <el-icon size="24"><Close /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ inventoryStats.outOfStockCount }}</div>
              <div class="stat-label">缺货商品</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon normal">
              <el-icon size="24"><Select /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ inventoryStats.avgDays }}</div>
              <div class="stat-label">平均库存天数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 库存状态分布图 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>库存状态分布</span>
          <div class="header-actions">
            <el-button-group size="small">
              <el-button :type="chartType === 'chart' ? 'primary' : ''" @click="chartType = 'chart'">
                <el-icon><TrendCharts /></el-icon>
                图表
              </el-button>
              <el-button :type="chartType === 'normal' ? 'primary' : ''" @click="chartType = 'normal'">
                <el-icon><List /></el-icon>
                普通
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>
      
      <!-- 图表视图 -->
      <div v-if="chartType === 'chart'" class="chart-container">
        <div ref="pieChartRef" style="width: 100%; height: 300px;"></div>
      </div>
      
      <!-- 普通视图 -->
      <div v-else class="status-overview">
        <div class="status-item">
          <div class="status-color normal-color"></div>
          <span class="status-label">正常库存</span>
          <span class="status-count">{{ inventoryStats.normalCount }} 件</span>
        </div>
        <div class="status-item">
          <div class="status-color low-color"></div>
          <span class="status-label">低库存</span>
          <span class="status-count">{{ inventoryStats.lowStockCount }} 件</span>
        </div>
        <div class="status-item">
          <div class="status-color out-color"></div>
          <span class="status-label">缺货</span>
          <span class="status-count">{{ inventoryStats.outOfStockCount }} 件</span>
        </div>
      </div>
    </el-card>

    <!-- 库存列表 -->
    <el-card class="inventory-list">
      <template #header>
        <div class="card-header">
          <span>库存明细</span>
          <div class="header-actions">
            <!-- 已屏蔽：导出数据和批量调整功能 -->
            <!-- <el-button type="primary" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
            <el-button type="success" @click="handleBatchUpdate">
              <el-icon><Edit /></el-icon>
              批量调整
            </el-button> -->
          </div>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <div class="filter-section">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-input
              v-model="searchForm.keyword"
              placeholder="商品名称/编码"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.categoryId" placeholder="商品分类" clearable>
              <el-option 
                v-for="category in flatCategories" 
                :key="category.id" 
                :label="category.categoryName" 
                :value="category.id" 
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.stockStatus" placeholder="库存状态" clearable>
              <el-option label="全部状态" value="" />
              <el-option label="正常" value="normal" />
              <el-option label="低库存" value="low" />
              <el-option label="缺货" value="out" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.sortBy" placeholder="排序方式">
              <el-option label="按库存数量" value="stock" />
              <el-option label="按库存价值" value="value" />
              <el-option label="按更新时间" value="time" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 数据表格 -->
      <el-table 
        v-loading="loading"
        :data="inventoryList" 
        border 
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="productImage" label="商品图片" width="80">
          <template #default="{ row }">
            <el-image
              :src="row.productImage"
              style="width: 60px; height: 60px"
              fit="cover"
              :preview-src-list="[row.productImage]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-name">{{ row.productName }}</div>
              <div class="product-code">编码：{{ row.productCode }}</div>
              <div v-if="row.skuSpecs" class="product-specs">
                {{ row.skuSpecs }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="currentStock" label="当前库存" width="100" align="center">
          <template #default="{ row }">
            <span :class="getStockStatusClass(row)">{{ row.currentStock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningStock" label="预警库存" width="100" align="center" />
        <el-table-column prop="unitPrice" label="库存价格" width="120" align="right">
          <template #default="{ row }">
            ¥{{ parseFloat(row.unitPrice).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stockValue" label="库存价值" width="120" align="right">
          <template #default="{ row }">
            ¥{{ parseFloat(row.stockValue).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stockStatus" label="库存状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStockTagType(row.stockStatus)">
              {{ getStockStatusText(row.stockStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.lastUpdateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleStockAdjust(row)"
            >
              调整库存
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[20, 50, 100, 200]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadInventoryList"
          @current-change="loadInventoryList"
        />
      </div>
    </el-card>

    <!-- 库存调整弹框 -->
    <StockAdjustDialog
      v-model="adjustDialogVisible"
      :stock-item="currentStockItem"
      @success="handleAdjustSuccess"
    />

    <!-- 批量库存调整弹框 -->
    <BatchStockAdjustDialog
      v-model="batchAdjustDialogVisible"
      :selected-items="selectedItems"
      @success="handleBatchAdjustSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Box,
  Warning,
  Close,
  Select,
  TrendCharts,
  List,
  Download,
  Edit,
  Search
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { getCategoryTree, type ProductCategoryVO } from '@/api/admin/productCategory'
import StockAdjustDialog from './components/StockAdjustDialog.vue'
import BatchStockAdjustDialog from './components/BatchStockAdjustDialog.vue'

// 组件状态
const loading = ref(false)
const chartType = ref('chart')
const pieChartRef = ref<HTMLElement>()
let pieChart: echarts.EChartsInstance | null = null

// 搜索表单
const searchForm = ref({
  keyword: '',
  categoryId: '',
  stockStatus: '',
  sortBy: 'stock'
})

// 分页
const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

// 库存统计
const inventoryStats = ref({
  totalValue: 807856.1,
  lowStockCount: 23,
  outOfStockCount: 0,
  avgDays: 23,
  normalCount: 1000
})

// 数据列表
const categoryTree = ref<ProductCategoryVO[]>([])
const inventoryList = ref<any[]>([])
const selectedItems = ref<any[]>([])

// 扁平化分类列表（用于下拉选择）
const flatCategories = computed(() => {
  const flatten = (categories: ProductCategoryVO[], level = 0): ProductCategoryVO[] => {
    let result: ProductCategoryVO[] = []
    categories.forEach(category => {
      result.push(category)
      if (category.children && category.children.length > 0) {
        result = result.concat(flatten(category.children, level + 1))
      }
    })
    return result
  }
  return flatten(categoryTree.value)
})

// 弹框控制
const adjustDialogVisible = ref(false)
const batchAdjustDialogVisible = ref(false)
const currentStockItem = ref<any>(null)

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return `¥${(num / 10000).toFixed(1)}万`
  }
  return `¥${num.toFixed(2)}`
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 获取库存状态样式类
const getStockStatusClass = (row: any) => {
  if (row.currentStock <= 0) return 'stock-out'
  if (row.currentStock <= row.warningStock) return 'stock-low'
  return 'stock-normal'
}

// 获取库存标签类型
const getStockTagType = (status: string) => {
  switch (status) {
    case 'normal': return 'success'
    case 'low': return 'warning'
    case 'out': return 'danger'
    default: return ''
  }
}

// 获取库存状态文本
const getStockStatusText = (status: string) => {
  switch (status) {
    case 'normal': return '正常'
    case 'low': return '低库存'
    case 'out': return '缺货'
    default: return '-'
  }
}

// 初始化图表
const initPieChart = () => {
  if (!pieChartRef.value) return
  
  pieChart = echarts.init(pieChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      data: ['正常库存', '低库存', '缺货']
    },
    series: [
      {
        name: '库存状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        data: [
          { value: inventoryStats.value.normalCount, name: '正常库存', itemStyle: { color: '#67c23a' } },
          { value: inventoryStats.value.lowStockCount, name: '低库存', itemStyle: { color: '#e6a23c' } },
          { value: inventoryStats.value.outOfStockCount, name: '缺货', itemStyle: { color: '#f56c6c' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  pieChart.setOption(option)
}

// 加载分类数据
const loadCategories = async () => {
  try {
    categoryTree.value = await getCategoryTree()
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  }
}

// 加载库存列表 - 获取所有商品的库存信息
const loadInventoryList = async () => {
  loading.value = true
  try {
    // 调用商品列表API获取所有商品
    const productParams = {
      current: pagination.value.current,
      size: pagination.value.size,
      keyword: searchForm.value.keyword,
      categoryId: searchForm.value.categoryId
    }
    
    const response = await request.get('/api/admin/product/page', { params: productParams })
    
    console.log('商品API响应:', response)
    
    // 检查响应数据结构
    let products, total
    if (response.code === 200) {
      products = response.data.records || []
      total = response.data.total || 0
    } else if (response.records) {
      // 直接返回分页对象的情况
      products = response.records || []
      total = response.total || 0
    } else {
      products = []
      total = 0
    }
    
    console.log('获取到商品数量:', products.length)
      
      // 并行获取每个商品的SKU信息
      const inventoryPromises = products.map(async (product: any) => {
        try {
          // 获取商品的SKU列表
          const skuResponse = await request.get(`/api/admin/product-sku/product/${product.id}`)
          // API 直接返回数组，不是 {code, data} 格式
          const skuList = Array.isArray(skuResponse) ? skuResponse : (skuResponse.data || skuResponse || [])

          if (skuList.length > 0) {
            // 有SKU的商品，汇总所有SKU的库存（列表只显示一行）
            const totalStock = skuList.reduce((sum: number, sku: any) => sum + (sku.stock || 0), 0)
            const avgPrice = skuList.reduce((sum: number, sku: any) => sum + (sku.price || 0), 0) / skuList.length
            const minWarningStock = Math.min(...skuList.map((sku: any) => sku.warningStock || 20))
            const latestUpdateTime = skuList.reduce((latest: string, sku: any) => {
              return sku.updateTime > latest ? sku.updateTime : latest
            }, product.updateTime || '')

            return {
              id: product.id,
              productId: product.id,
              skuId: null, // 列表显示商品级别，弹框里显示SKU
              productName: product.productName,
              productCode: product.productCode,
              productImage: product.mainImage || 'https://via.placeholder.com/60',
              categoryName: product.categoryName,
              skuSpecs: `${skuList.length}个SKU`,
              currentStock: totalStock,
              warningStock: minWarningStock,
              unitPrice: avgPrice || product.basePrice,
              stockValue: totalStock * (avgPrice || product.basePrice),
              stockStatus: getStockStatus(totalStock, minWarningStock),
              lastUpdateTime: latestUpdateTime,
              enableSpec: true // 标记有SKU
            }
          } else {
            // 没有SKU的商品，使用基础库存
            return {
              id: product.id,
              productId: product.id,
              skuId: null,
              productName: product.productName,
              productCode: product.productCode,
              productImage: product.mainImage || 'https://via.placeholder.com/60',
              categoryName: product.categoryName,
              skuSpecs: '默认规格',
              currentStock: product.stock || 0,
              warningStock: product.warningStock || 20,
              unitPrice: product.basePrice,
              stockValue: (product.stock || 0) * product.basePrice,
              stockStatus: getStockStatus(product.stock || 0, product.warningStock || 20),
              lastUpdateTime: product.updateTime,
              enableSpec: false
            }
          }
        } catch (error) {
          console.error(`获取商品 ${product.id} 的SKU信息失败:`, error)
          // 如果SKU获取失败，使用基础库存信息
          return {
            id: product.id,
            productId: product.id,
            skuId: null,
            productName: product.productName,
            productCode: product.productCode,
            productImage: product.mainImage || 'https://via.placeholder.com/60',
            categoryName: product.categoryName,
            skuSpecs: '默认规格',
            currentStock: product.stock || 0,
            warningStock: product.warningStock || 20,
            unitPrice: product.basePrice,
            stockValue: (product.stock || 0) * product.basePrice,
            stockStatus: getStockStatus(product.stock || 0, product.warningStock || 20),
            lastUpdateTime: product.updateTime,
            enableSpec: false
          }
        }
      })

      const inventoryResults = await Promise.all(inventoryPromises)
      inventoryList.value = inventoryResults
      
      // 应用筛选条件
      if (searchForm.value.stockStatus) {
        inventoryList.value = inventoryList.value.filter(item => 
          item.stockStatus === searchForm.value.stockStatus
        )
      }
      
      // 应用排序
      if (searchForm.value.sortBy === 'stock') {
        inventoryList.value.sort((a, b) => a.currentStock - b.currentStock)
      } else if (searchForm.value.sortBy === 'value') {
        inventoryList.value.sort((a, b) => a.stockValue - b.stockValue)
      } else if (searchForm.value.sortBy === 'time') {
        inventoryList.value.sort((a, b) => 
          new Date(b.lastUpdateTime).getTime() - new Date(a.lastUpdateTime).getTime()
        )
      }
      
      pagination.value.total = total
      
      // 更新统计数据
      updateInventoryStats()
      
      console.log('库存列表处理完成，共', inventoryList.value.length, '条记录')
  } catch (error) {
    console.error('加载库存列表失败:', error)
    ElMessage.error('加载库存列表失败')
  } finally {
    loading.value = false
  }
}

// 解析SKU规格组合
const parseSkuSpecs = (specCombination: string) => {
  try {
    const specs = JSON.parse(specCombination)
    return Object.entries(specs).map(([key, value]) => `${key}: ${value}`).join(', ')
  } catch {
    return specCombination || '默认规格'
  }
}

// 判断库存状态
const getStockStatus = (currentStock: number, warningStock: number) => {
  if (currentStock <= 0) return 'out'
  if (currentStock <= warningStock) return 'low'
  return 'normal'
}

// 更新库存统计数据
const updateInventoryStats = () => {
  const totalValue = inventoryList.value.reduce((sum, item) => sum + item.stockValue, 0)
  const lowStockCount = inventoryList.value.filter(item => item.stockStatus === 'low').length
  const outOfStockCount = inventoryList.value.filter(item => item.stockStatus === 'out').length
  const normalCount = inventoryList.value.filter(item => item.stockStatus === 'normal').length
  
  inventoryStats.value = {
    totalValue,
    lowStockCount,
    outOfStockCount,
    normalCount,
    avgDays: Math.round(totalValue / 1000) // 简单计算平均库存天数
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadInventoryList()
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    keyword: '',
    categoryId: '',
    stockStatus: '',
    sortBy: 'stock'
  }
  handleSearch()
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedItems.value = selection
}

// 库存调整
const handleStockAdjust = (row: any) => {
  currentStockItem.value = row
  adjustDialogVisible.value = true
}

// 批量库存调整
const handleBatchUpdate = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要调整的库存项')
    return
  }
  batchAdjustDialogVisible.value = true
}

// 导出数据
const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

// 调整成功回调
const handleAdjustSuccess = () => {
  loadInventoryList()
}

// 批量调整成功回调
const handleBatchAdjustSuccess = () => {
  selectedItems.value = []
  loadInventoryList()
}

// 监听图表类型变化
const handleChartTypeChange = () => {
  if (chartType.value === 'chart') {
    nextTick(() => {
      initPieChart()
    })
  }
}

onMounted(async () => {
  await loadCategories()
  await loadInventoryList()
  
  if (chartType.value === 'chart') {
    await nextTick()
    initPieChart()
  }
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (pieChart) {
      pieChart.resize()
    }
  })
})
</script>

<style scoped lang="scss">
.inventory-manage {
  .inventory-stats {
    margin-bottom: 20px;
    
    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        
        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          
          &.total {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
          }
          
          &.low {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
          }
          
          &.out {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            color: white;
          }
          
          &.normal {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
            color: white;
          }
        }
        
        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
          }
          
          .stat-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }
  }
  
  .chart-card, .inventory-list {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-actions {
        display: flex;
        gap: 12px;
      }
    }
  }
  
  .status-overview {
    display: flex;
    justify-content: space-around;
    align-items: center;
    padding: 40px 0;
    
    .status-item {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .status-color {
        width: 16px;
        height: 16px;
        border-radius: 50%;
        
        &.normal-color {
          background-color: #67c23a;
        }
        
        &.low-color {
          background-color: #e6a23c;
        }
        
        &.out-color {
          background-color: #f56c6c;
        }
      }
      
      .status-label {
        font-size: 14px;
        color: #606266;
      }
      
      .status-count {
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }
    }
  }
  
  .filter-section {
    margin-bottom: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 6px;
  }
  
  .product-info {
    .product-name {
      font-weight: 500;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .product-code {
      font-size: 12px;
      color: #909399;
      margin-bottom: 4px;
    }
    
    .product-specs {
      font-size: 12px;
      color: #67c23a;
      background: #f0f9ff;
      padding: 2px 6px;
      border-radius: 4px;
      display: inline-block;
    }
  }
  
  .stock-normal {
    color: #67c23a;
    font-weight: bold;
  }
  
  .stock-low {
    color: #e6a23c;
    font-weight: bold;
  }
  
  .stock-out {
    color: #f56c6c;
    font-weight: bold;
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>