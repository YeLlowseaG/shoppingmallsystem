<template>
  <div class="stock-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存列表</span>
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
          <el-button type="primary" @click="loadStockList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 库存列表 -->
      <el-table :data="stockList" v-loading="loading" border>
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
        <el-table-column prop="availableStock" label="可用库存" width="100" align="right" />
        <el-table-column prop="lockedStock" label="锁定库存" width="100" align="right" />
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAdjust(row)">调整库存</el-button>
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
        @size-change="loadStockList"
        @current-change="loadStockList"
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
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjustSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils'
import {
  getStockPage,
  adjustStock,
  type StockVO,
  type StockDTO
} from '@/api/admin/stock'

const loading = ref(false)
const stockList = ref<StockVO[]>([])
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
  reason: ''
})
const adjustFormRules = {
  adjustQuantity: [{ required: true, message: '请输入调整数量', trigger: 'blur' }]
}

// 加载库存列表
const loadStockList = async () => {
  loading.value = true
  try {
    const response = await getStockPage(
      pagination.value.current,
      pagination.value.size,
      undefined,
      searchForm.value.productCode || undefined,
      searchForm.value.productName || undefined
    )
    stockList.value = response.records || []
    pagination.value.total = response.total || 0
  } catch (error) {
    ElMessage.error('加载库存列表失败')
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
  loadStockList()
}

// 调整库存
const handleAdjust = (row: StockVO) => {
  adjustFormData.value = {
    productId: row.productId,
    productName: row.productName,
    currentStock: row.totalStock,
    adjustQuantity: 0,
    reason: ''
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
      reason: adjustFormData.value.reason || undefined
    }
    
    await adjustStock(stockDTO)
    ElMessage.success('库存调整成功')
    adjustDialogVisible.value = false
    loadStockList()
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
    reason: ''
  }
  adjustFormRef.value?.clearValidate()
}

// 初始化
onMounted(() => {
  loadStockList()
})
</script>

<style scoped lang="scss">
.stock-list {
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

  .form-tip {
    margin-top: 5px;
    font-size: 12px;
    color: #909399;
  }
}
</style>
