<template>
  <div class="stock-warning">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存预警</span>
          <el-tag type="danger" size="large">预警商品：{{ pagination.total }} 个</el-tag>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品编码">
          <el-input v-model="searchForm.productCode" placeholder="请输入商品编码" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadWarningList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 预警商品列表 -->
      <el-table :data="warningList" v-loading="loading" border>
        <el-table-column prop="productCode" label="商品编码" width="150" />
        <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="mainImage" label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.mainImage"
              :src="row.mainImage"
              style="width: 60px; height: 60px"
              fit="cover"
            />
            <span v-else>无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalStock" label="总库存" width="100" align="right" />
        <el-table-column prop="availableStock" label="可用库存" width="100" align="right">
          <template #default="{ row }">
            <span class="warning-text">{{ row.availableStock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lockedStock" label="锁定库存" width="100" align="right" />
        <el-table-column prop="warningThreshold" label="预警阈值" width="100" align="right" />
        <el-table-column label="预警状态" width="100">
          <template #default="{ row }">
            <el-tag type="danger">预警</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAdjust(row)">调整库存</el-button>
            <el-button type="success" link @click="handleSetThreshold(row)">设置预警</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadWarningList"
        @current-change="loadWarningList"
      />
    </el-card>

    <!-- 库存调整对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="调整库存"
      width="500px"
      @close="resetAdjustForm"
    >
      <el-form
        ref="adjustFormRef"
        :model="adjustFormData"
        :rules="adjustFormRules"
        label-width="120px"
      >
        <el-form-item label="商品名称">
          <el-input v-model="adjustFormData.productName" disabled />
        </el-form-item>
        <el-form-item label="当前总库存">
          <el-input v-model="adjustFormData.currentStock" disabled />
        </el-form-item>
        <el-form-item label="调整数量" prop="adjustQuantity">
          <el-input-number
            v-model="adjustFormData.adjustQuantity"
            :min="-999999"
            :max="999999"
            style="width: 100%"
            placeholder="正数为增加，负数为减少"
          />
          <div class="form-tip">提示：正数表示增加库存，负数表示减少库存</div>
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="adjustFormData.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因（可选）"
          />
        </el-form-item>
        <el-form-item label="预警阈值" prop="warningThreshold">
          <el-input-number
            v-model="adjustFormData.warningThreshold"
            :min="0"
            style="width: 100%"
            placeholder="可选，用于更新预警阈值"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjustSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 设置预警阈值对话框 -->
    <el-dialog
      v-model="thresholdDialogVisible"
      title="设置预警阈值"
      width="400px"
    >
      <el-form
        ref="thresholdFormRef"
        :model="thresholdFormData"
        :rules="thresholdFormRules"
        label-width="120px"
      >
        <el-form-item label="商品名称">
          <el-input v-model="thresholdFormData.productName" disabled />
        </el-form-item>
        <el-form-item label="当前可用库存">
          <el-input v-model="thresholdFormData.currentAvailableStock" disabled />
        </el-form-item>
        <el-form-item label="预警阈值" prop="warningThreshold">
          <el-input-number
            v-model="thresholdFormData.warningThreshold"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="thresholdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleThresholdSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils'
import {
  getWarningStockPage,
  adjustStock,
  updateWarningThreshold,
  type StockVO,
  type StockDTO
} from '@/api/admin/stock'

const loading = ref(false)
const warningList = ref<StockVO[]>([])
const searchForm = ref({
  productCode: '',
  productName: ''
})
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 库存调整对话框
const adjustDialogVisible = ref(false)
const adjustFormRef = ref()
const adjustFormData = ref<StockDTO & { productName: string; currentStock: number }>({
  productId: 0,
  productName: '',
  currentStock: 0,
  adjustQuantity: 0,
  reason: '',
  warningThreshold: undefined
})
const adjustFormRules = {
  adjustQuantity: [{ required: true, message: '请输入调整数量', trigger: 'blur' }]
}

// 设置预警阈值对话框
const thresholdDialogVisible = ref(false)
const thresholdFormRef = ref()
const thresholdFormData = ref<{
  productId: number
  productName: string
  currentAvailableStock: number
  warningThreshold: number
}>({
  productId: 0,
  productName: '',
  currentAvailableStock: 0,
  warningThreshold: 10
})
const thresholdFormRules = {
  warningThreshold: [{ required: true, message: '请输入预警阈值', trigger: 'blur' }]
}

// 加载预警列表
const loadWarningList = async () => {
  loading.value = true
  try {
    const response = await getWarningStockPage(
      pagination.value.current,
      pagination.value.size
    )
    
    // 在内存中过滤商品编码和名称
    let filteredList = response.records || []
    if (searchForm.value.productCode) {
      filteredList = filteredList.filter(item => 
        item.productCode?.includes(searchForm.value.productCode)
      )
    }
    if (searchForm.value.productName) {
      filteredList = filteredList.filter(item => 
        item.productName?.includes(searchForm.value.productName)
      )
    }
    
    warningList.value = filteredList
    pagination.value.total = filteredList.length
  } catch (error) {
    ElMessage.error('加载预警列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    productCode: '',
    productName: ''
  }
  pagination.value.current = 1
  loadWarningList()
}

// 调整库存
const handleAdjust = (row: StockVO) => {
  adjustFormData.value = {
    productId: row.productId,
    productName: row.productName,
    currentStock: row.totalStock,
    adjustQuantity: 0,
    reason: '',
    warningThreshold: row.warningThreshold
  }
  adjustDialogVisible.value = true
}

// 提交库存调整
const handleAdjustSubmit = async () => {
  if (!adjustFormRef.value) return
  
  try {
    await adjustFormRef.value.validate()
    
    const stockDTO: StockDTO = {
      productId: adjustFormData.value.productId,
      adjustQuantity: adjustFormData.value.adjustQuantity,
      reason: adjustFormData.value.reason || undefined,
      warningThreshold: adjustFormData.value.warningThreshold || undefined
    }
    
    await adjustStock(stockDTO)
    ElMessage.success('库存调整成功')
    adjustDialogVisible.value = false
    loadWarningList()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '库存调整失败')
    }
  }
}

// 重置调整表单
const resetAdjustForm = () => {
  adjustFormData.value = {
    productId: 0,
    productName: '',
    currentStock: 0,
    adjustQuantity: 0,
    reason: '',
    warningThreshold: undefined
  }
  adjustFormRef.value?.clearValidate()
}

// 设置预警阈值
const handleSetThreshold = (row: StockVO) => {
  thresholdFormData.value = {
    productId: row.productId,
    productName: row.productName,
    currentAvailableStock: row.availableStock,
    warningThreshold: row.warningThreshold
  }
  thresholdDialogVisible.value = true
}

// 提交预警阈值设置
const handleThresholdSubmit = async () => {
  if (!thresholdFormRef.value) return
  
  try {
    await thresholdFormRef.value.validate()
    
    await updateWarningThreshold(
      thresholdFormData.value.productId,
      thresholdFormData.value.warningThreshold
    )
    ElMessage.success('预警阈值设置成功')
    thresholdDialogVisible.value = false
    loadWarningList()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '预警阈值设置失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadWarningList()
})
</script>

<style scoped lang="scss">
.stock-warning {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 18px;
    font-weight: bold;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }

  .warning-text {
    color: #f56c6c;
    font-weight: bold;
  }

  .form-tip {
    margin-top: 5px;
    font-size: 12px;
    color: #909399;
  }
}
</style>
