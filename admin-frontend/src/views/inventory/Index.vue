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

    <!-- 库存状态分布 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>库存状态分布</span>
        </div>
      </template>
      
      <!-- 普通视图 -->
      <div class="status-overview">
        <div class="status-item">
          <div class="status-color normal-color"></div>
          <span class="status-label">正常库存</span>
          <span class="status-count">{{ inventoryStats.normalCount }} 件</span>
        </div>
        <div class="status-item">
          <div class="status-color low-color"></div>
          <span class="status-label">低库存</span>
          <span class="status-count">{{ inventoryStats.lowStockQuantity }} 件</span>
        </div>
        <div class="status-item">
          <div class="status-color out-color"></div>
          <span class="status-label">缺货</span>
          <!-- 缺货商品显示商品数量，因为缺货商品的库存件数都是0 -->
          <span class="status-count">{{ inventoryStats.outOfStockCount > 0 ? inventoryStats.outOfStockCount + ' 个商品' : inventoryStats.outOfStockQuantity + ' 件' }}</span>
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
            <el-cascader
              v-model="searchForm.categoryId"
              :options="categoryTree"
              :props="cascaderProps"
              placeholder="商品分类"
              clearable
              style="width: 100%"
            />
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
            <el-select v-model="searchForm.productStatus" placeholder="商品状态" clearable>
              <el-option label="全部" value="" />
              <el-option label="已上架" value="上架" />
              <el-option label="已下架" value="下架" />
              <el-option label="草稿" value="草稿" />
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
              <div class="product-name" :title="row.productName">{{ row.productName }}</div>
              <div class="product-code">编码：{{ row.productCode }}</div>
              <div v-if="row.skuSpecs" class="product-specs">
                {{ row.skuSpecs }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="productStatus" label="商品状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.productStatus === '上架' ? 'success' : row.productStatus === '草稿' ? '' : 'info'">
              {{ row.productStatus || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentStock" label="当前库存" width="100" align="center">
          <template #default="{ row }">
            <span :class="getStockStatusClass(row)">{{ row.currentStock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningStock" label="预警库存" width="100" align="center" />
        <el-table-column prop="unitPrice" label="库存价格" width="100" align="right">
          <template #default="{ row }">
            ¥{{ parseFloat(row.unitPrice).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stockValue" label="库存价值" width="100" align="right">
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; gap: 10px; justify-content: flex-start; align-items: center;">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleStockAdjust(row)"
                style="width: 80px;"
              >
                调整库存
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                @click="handleSyncToErp(row)"
                :loading="syncLoading[row.id]"
                style="width: 90px;"
              >
                <el-icon><RefreshRight /></el-icon>
                同步ERP
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
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

    <!-- ERP同步进度弹窗 -->
    <el-dialog
      v-model="syncProgressVisible"
      title="ERP同步进度"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="!syncProgressLoading"
    >
      <div class="sync-progress-content">
        <el-steps :active="syncProgressStep" direction="vertical" finish-status="success">
          <el-step title="同步商品资料" :status="getStepStatus(0)">
            <template #description>
              <div v-if="syncProgressStep === 0 && syncProgressLoading" class="step-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>正在同步商品资料到ERP...</span>
              </div>
              <div v-else-if="syncProgressStep > 0" class="step-success">
                <el-icon><CircleCheck /></el-icon>
                <span>商品资料同步成功</span>
              </div>
              <div v-else-if="syncProgressError && syncProgressStep === 0" class="step-error">
                <el-icon><CircleClose /></el-icon>
                <span>{{ syncProgressError }}</span>
              </div>
            </template>
          </el-step>
          
          <el-step title="等待ERP处理" :status="getStepStatus(1)">
            <template #description>
              <div v-if="syncProgressStep === 1 && syncProgressLoading" class="step-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>等待ERP系统处理商品资料（2秒）...</span>
              </div>
              <div v-else-if="syncProgressStep > 1" class="step-success">
                <el-icon><CircleCheck /></el-icon>
                <span>等待完成</span>
              </div>
            </template>
          </el-step>
          
          <el-step title="同步库存" :status="getStepStatus(2)">
            <template #description>
              <div v-if="syncProgressStep === 2 && syncProgressLoading" class="step-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>正在同步库存到ERP（尝试 {{ syncProgressRetryCount + 1 }}/4）...</span>
              </div>
              <div v-else-if="syncProgressStep > 2" class="step-success">
                <el-icon><CircleCheck /></el-icon>
                <span>库存同步成功</span>
              </div>
              <div v-else-if="syncProgressError && syncProgressStep === 2" class="step-error">
                <el-icon><CircleClose /></el-icon>
                <span>{{ syncProgressError }}</span>
              </div>
            </template>
          </el-step>
        </el-steps>
      </div>
      
      <template #footer>
        <el-button 
          v-if="!syncProgressLoading" 
          type="primary" 
          @click="syncProgressVisible = false"
        >
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import {
  Box,
  Warning,
  Close,
  Select,
  TrendCharts,
  List,
  Download,
  Edit,
  Search,
  RefreshRight,
  Loading,
  CircleCheck,
  CircleClose
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
  categoryId: undefined as number | number[] | undefined,
  stockStatus: '',
  productStatus: '上架', // 商品状态，默认值为已上架
  sortBy: 'stock'
})

// 分页
const pagination = ref({
  current: 1,
  size: 10, // 默认显示10条一页
  total: 0
})

// 库存统计
const inventoryStats = ref({
  totalValue: 807856.1,
  lowStockCount: 23, // 顶部统计：低库存商品数量（个）
  outOfStockCount: 0, // 顶部统计：缺货商品数量（个）
  avgDays: 23,
  normalCount: 1000, // 库存状态分布：正常库存件数（件）
  lowStockQuantity: 0, // 库存状态分布：低库存件数（件）
  outOfStockQuantity: 0 // 库存状态分布：缺货件数（件）
})

// 数据列表
const categoryTree = ref<ProductCategoryVO[]>([])
const inventoryList = ref<any[]>([])
const selectedItems = ref<any[]>([])

// 级联选择器配置
const cascaderProps = {
  value: 'id',
  label: 'categoryName',
  children: 'children',
  checkStrictly: true
}

// 扁平化分类列表（保留用于其他可能需要的地方）
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

// 同步加载状态
const syncLoading = ref<Record<number, boolean>>({})

// ERP同步进度相关
const syncProgressVisible = ref(false)
const syncProgressLoading = ref(false)
const syncProgressStep = ref(0) // 0-商品资料, 1-等待, 2-库存
const syncProgressRetryCount = ref(0)
const syncProgressError = ref('')
let syncProgressInstance: ReturnType<typeof ElLoading.service> | null = null

// 获取步骤状态
const getStepStatus = (step: number) => {
  if (syncProgressError.value && syncProgressStep.value === step) {
    return 'error'
  }
  if (syncProgressStep.value > step) {
    return 'success'
  }
  if (syncProgressStep.value === step && syncProgressLoading.value) {
    return 'process'
  }
  return 'wait'
}

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
          { value: inventoryStats.value.lowStockQuantity, name: '低库存', itemStyle: { color: '#e6a23c' } },
          { value: inventoryStats.value.outOfStockQuantity, name: '缺货', itemStyle: { color: '#f56c6c' } }
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
    // 处理级联选择器的值（如果是数组，取最后一个值）
    const categoryId = Array.isArray(searchForm.value.categoryId)
      ? searchForm.value.categoryId[searchForm.value.categoryId.length - 1]
      : searchForm.value.categoryId
    
    // 使用分页获取商品列表（默认10条，提升性能）
    const productParams = {
      current: pagination.value.current,
      size: pagination.value.size, // 使用分页大小，默认10条
      keyword: searchForm.value.keyword,
      categoryId: categoryId,
      status: searchForm.value.productStatus || undefined // 传递商品状态参数
    }
    
    const response = await request.get('/api/admin/product/page', { params: productParams })
    
    console.log('商品API响应:', response)
    
    // 检查响应数据结构
    let products
    if (response.code === 200) {
      products = response.data.records || []
    } else if (response.records) {
      // 直接返回分页对象的情况
      products = response.records || []
    } else {
      products = []
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
              productStatus: product.status || '草稿', // 添加商品状态
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
              productStatus: product.status || '草稿', // 添加商品状态
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
            productStatus: product.status || '草稿', // 添加商品状态
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

      const allInventoryResults = await Promise.all(inventoryPromises)
      
      // 应用库存状态筛选（如果设置了）
      let filteredItems = allInventoryResults
      if (searchForm.value.stockStatus) {
        filteredItems = allInventoryResults.filter(item => 
          item.stockStatus === searchForm.value.stockStatus
        )
      }
      
      // 应用排序
      if (searchForm.value.sortBy === 'stock') {
        filteredItems.sort((a, b) => a.currentStock - b.currentStock)
      } else if (searchForm.value.sortBy === 'value') {
        filteredItems.sort((a, b) => a.stockValue - b.stockValue)
      } else if (searchForm.value.sortBy === 'time') {
        filteredItems.sort((a, b) => 
          new Date(b.lastUpdateTime).getTime() - new Date(a.lastUpdateTime).getTime()
        )
      }
      
      // 直接使用当前页的数据（后端已分页）
      inventoryList.value = filteredItems
      
      // 更新总数：从后端响应中获取总数
      if (response.code === 200 && response.data && response.data.total !== undefined) {
        pagination.value.total = response.data.total
      } else if (response.total !== undefined) {
        pagination.value.total = response.total
      } else {
        // 如果后端没有返回总数，使用当前页数据量估算
        pagination.value.total = filteredItems.length < pagination.value.size 
          ? (pagination.value.current - 1) * pagination.value.size + filteredItems.length
          : (pagination.value.current * pagination.value.size)
      }
      
      // 更新统计数据（基于筛选条件统计所有符合条件的数据）
      loadInventoryStats()
      
      console.log('库存列表处理完成，筛选后总数:', filteredItems.length, '当前页显示:', inventoryList.value.length, '条记录')
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

// 加载统计数据 - 基于筛选条件获取所有符合条件的数据进行统计
const loadInventoryStats = async () => {
  try {
    // 处理级联选择器的值（如果是数组，取最后一个值）
    const categoryId = Array.isArray(searchForm.value.categoryId)
      ? searchForm.value.categoryId[searchForm.value.categoryId.length - 1]
      : searchForm.value.categoryId
    
    // 获取符合筛选条件的商品用于统计（限制为1000条，避免性能问题）
    // 注意：统计功能需要所有数据，但为了性能考虑，限制为1000条
    const productParams = {
      current: 1,
      size: 1000, // 限制为1000条用于统计，避免性能问题
      keyword: searchForm.value.keyword,
      categoryId: categoryId,
      status: searchForm.value.productStatus || undefined // 如果选择了商品状态，则按该状态查询；如果选择"全部"，则查询所有
    }
    
    const response = await request.get('/api/admin/product/page', { params: productParams })
    
    // 检查响应数据结构
    let products
    if (response.code === 200) {
      products = response.data.records || []
    } else if (response.records) {
      products = response.records || []
    } else {
      products = []
    }
    
    // 并行获取每个商品的SKU信息
    const inventoryPromises = products.map(async (product: any) => {
      try {
        // 获取商品的SKU列表
        const skuResponse = await request.get(`/api/admin/product-sku/product/${product.id}`)
        const skuList = Array.isArray(skuResponse) ? skuResponse : (skuResponse.data || skuResponse || [])

        if (skuList.length > 0) {
          // 有SKU的商品，汇总所有SKU的库存
          const totalStock = skuList.reduce((sum: number, sku: any) => sum + (sku.stock || 0), 0)
          const avgPrice = skuList.reduce((sum: number, sku: any) => sum + (sku.price || 0), 0) / skuList.length
          const minWarningStock = Math.min(...skuList.map((sku: any) => sku.warningStock || 20))

          return {
            id: product.id,
            productId: product.id,
            productStatus: product.status || '草稿',
            currentStock: totalStock,
            warningStock: minWarningStock,
            unitPrice: avgPrice || product.basePrice,
            stockValue: totalStock * (avgPrice || product.basePrice),
            stockStatus: getStockStatus(totalStock, minWarningStock)
          }
        } else {
          // 没有SKU的商品，使用基础库存
          return {
            id: product.id,
            productId: product.id,
            productStatus: product.status || '草稿',
            currentStock: product.stock || 0,
            warningStock: product.warningStock || 20,
            unitPrice: product.basePrice,
            stockValue: (product.stock || 0) * product.basePrice,
            stockStatus: getStockStatus(product.stock || 0, product.warningStock || 20)
          }
        }
      } catch (error) {
        console.error(`获取商品 ${product.id} 的SKU信息失败:`, error)
        // 如果SKU获取失败，使用基础库存信息
        return {
          id: product.id,
          productId: product.id,
          productStatus: product.status || '草稿',
          currentStock: product.stock || 0,
          warningStock: product.warningStock || 20,
          unitPrice: product.basePrice,
          stockValue: (product.stock || 0) * product.basePrice,
          stockStatus: getStockStatus(product.stock || 0, product.warningStock || 20)
        }
      }
    })

    const allInventoryItems = await Promise.all(inventoryPromises)
    
    // 应用库存状态筛选（如果设置了）
    let filteredItems = allInventoryItems
    if (searchForm.value.stockStatus) {
      filteredItems = allInventoryItems.filter(item => item.stockStatus === searchForm.value.stockStatus)
    }
    
    // 根据用户选择的商品状态进行统计
    // 如果用户选择了商品状态，则只统计该状态的商品；如果选择"全部"（空值），则统计所有状态的商品
    let statsItems = filteredItems
    if (searchForm.value.productStatus) {
      statsItems = filteredItems.filter(item => item.productStatus === searchForm.value.productStatus)
    }
    
    const totalValue = statsItems.reduce((sum, item) => sum + item.stockValue, 0)
    
    // 顶部统计：统计商品数量（个）- 基于筛选条件
    const lowStockProductCount = statsItems.filter(item => item.stockStatus === 'low').length
    const outOfStockProductCount = statsItems.filter(item => item.stockStatus === 'out').length
    
    // 库存状态分布：统计库存件数（件）- 累加所有符合筛选条件商品的库存数量
    const normalStockQuantity = statsItems
      .filter(item => item.stockStatus === 'normal')
      .reduce((sum, item) => sum + (item.currentStock || 0), 0)
    const lowStockQuantity = statsItems
      .filter(item => item.stockStatus === 'low')
      .reduce((sum, item) => sum + (item.currentStock || 0), 0)
    const outOfStockQuantity = statsItems
      .filter(item => item.stockStatus === 'out')
      .reduce((sum, item) => sum + (item.currentStock || 0), 0)
    
    // 调试日志：检查统计一致性
    console.log('统计调试信息:', {
      总商品数: statsItems.length,
      低库存商品数: lowStockProductCount,
      缺货商品数: outOfStockProductCount,
      正常库存件数: normalStockQuantity,
      低库存件数: lowStockQuantity,
      缺货件数: outOfStockQuantity,
      缺货商品详情: statsItems.filter(item => item.stockStatus === 'out').slice(0, 5).map(item => ({
        id: item.id,
        currentStock: item.currentStock,
        stockStatus: item.stockStatus
      }))
    })
    
    inventoryStats.value = {
      totalValue,
      lowStockCount: lowStockProductCount, // 顶部统计：低库存商品数量（个）- 基于筛选条件
      outOfStockCount: outOfStockProductCount, // 顶部统计：缺货商品数量（个）- 基于筛选条件
      normalCount: normalStockQuantity, // 库存状态分布：正常库存件数（件）- 基于筛选条件
      lowStockQuantity: lowStockQuantity, // 库存状态分布：低库存件数（件）- 基于筛选条件
      outOfStockQuantity: outOfStockQuantity, // 库存状态分布：缺货件数（件）- 基于筛选条件
      avgDays: Math.round(totalValue / 1000) // 简单计算平均库存天数
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 如果统计失败，不影响列表显示，只记录错误
  }
}

// 更新库存统计数据（基于当前列表数据，已废弃，改用loadInventoryStats）
const updateInventoryStats = () => {
  // 此函数保留用于兼容，但实际统计使用loadInventoryStats
  loadInventoryStats()
}

// 处理搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadInventoryList()
  // 注意：loadInventoryList内部已经调用了loadInventoryStats，这里不需要重复调用
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    keyword: '',
    categoryId: undefined,
    stockStatus: '',
    productStatus: '上架', // 重置时保持默认值为已上架
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

// 同步到ERP（完整流程：商品资料 + 库存）
const handleSyncToErp = async (row: any) => {
  // 重置进度状态
  syncProgressVisible.value = true
  syncProgressLoading.value = true
  syncProgressStep.value = 0
  syncProgressRetryCount.value = 0
  syncProgressError.value = ''
  syncLoading.value[row.id] = true
  
  try {
    // 显示全屏loading
    syncProgressInstance = ElLoading.service({
      lock: true,
      text: '正在同步到ERP，请稍候...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    // 更新进度：步骤1 - 同步商品资料
    syncProgressStep.value = 0
    
    // 使用定时器模拟进度更新（因为后端是同步接口，无法实时获取进度）
    // 步骤1：同步商品资料（预计2-3秒）
    let stepTimer1: ReturnType<typeof setTimeout> | null = null
    let stepTimer2: ReturnType<typeof setTimeout> | null = null
    
    // 2秒后进入等待步骤（模拟商品资料同步完成）
    stepTimer1 = setTimeout(() => {
      if (syncProgressLoading.value && syncProgressStep.value === 0) {
        syncProgressStep.value = 1 // 进入等待步骤
      }
    }, 2000)
    
    // 4秒后进入库存同步步骤（2秒商品资料 + 2秒等待）
    stepTimer2 = setTimeout(() => {
      if (syncProgressLoading.value && syncProgressStep.value === 1) {
        syncProgressStep.value = 2 // 进入库存同步步骤
      }
    }, 4000)
    
    // 开始调用接口
    const startTime = Date.now()
    const response = await request.post(`/api/admin/inventory/sync/${row.productId}/full`)
    const elapsedTime = Date.now() - startTime
    
    // 清除定时器
    if (stepTimer1) clearTimeout(stepTimer1)
    if (stepTimer2) clearTimeout(stepTimer2)
    
    // 根据实际耗时更新进度
    // 如果接口返回时还在步骤0，说明商品资料同步很快，直接跳到步骤1
    if (syncProgressStep.value === 0 && elapsedTime < 2000) {
      syncProgressStep.value = 1
    }
    // 如果接口返回时还在步骤1，说明等待时间还没到，直接跳到步骤2
    if (syncProgressStep.value === 1 && elapsedTime < 4000) {
      syncProgressStep.value = 2
    }
    
    // 关闭全屏loading
    if (syncProgressInstance) {
      syncProgressInstance.close()
      syncProgressInstance = null
    }
    
    // 更新进度
    syncProgressLoading.value = false
    
    // 检查同步结果
    if (response && response.success) {
      syncProgressStep.value = 3 // 全部完成
      ElMessage.success('商品资料和库存同步成功')
      // 刷新库存列表
      await loadInventoryList()
      // 2秒后自动关闭进度弹窗
      setTimeout(() => {
        syncProgressVisible.value = false
      }, 2000)
    } else {
      // 根据失败步骤设置错误信息
      const errorMsg = response?.message || '同步失败'
      syncProgressError.value = errorMsg
      
      if (response?.step === 'ITEM_SYNC') {
        syncProgressStep.value = 0
      } else if (response?.step === 'INVENTORY_SYNC') {
        syncProgressStep.value = 2
        syncProgressRetryCount.value = response?.inventoryRetryCount || 0
      }
      
      ElMessage.error({
        message: errorMsg,
        duration: 5000,
        showClose: true
      })
    }
  } catch (error: any) {
    console.error('同步到ERP失败:', error)
    
    // 清除可能的定时器
    // clearInterval(progressTimer)
    
    // 关闭全屏loading
    if (syncProgressInstance) {
      syncProgressInstance.close()
      syncProgressInstance = null
    }
    
    syncProgressLoading.value = false
    syncProgressError.value = error?.message || '同步失败'
    
    const errorMsg = error?.message || '同步失败'
    if (!errorMsg.includes('请求失败')) {
      ElMessage.error(errorMsg)
    }
  } finally {
    // 取消加载状态
    syncLoading.value[row.id] = false
  }
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
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
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
  
  .sync-progress-content {
    padding: 20px 0;
    
    .step-loading {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #409eff;
      
      .el-icon {
        animation: rotating 2s linear infinite;
      }
    }
    
    .step-success {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #67c23a;
    }
    
    .step-error {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #f56c6c;
    }
  }
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>