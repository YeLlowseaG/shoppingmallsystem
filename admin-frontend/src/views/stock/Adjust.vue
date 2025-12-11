<template>
  <div class="stock-adjust">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存调整</span>
        </div>
      </template>

      <!-- 搜索商品 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品编码">
          <el-input v-model="searchForm.productCode" placeholder="请输入商品编码" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索商品</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 商品列表 -->
      <el-table :data="productList" v-loading="loading" border style="margin-bottom: 20px">
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
        <el-table-column prop="totalStock" label="当前总库存" width="120" align="right" />
        <el-table-column prop="availableStock" label="可用库存" width="100" align="right" />
        <el-table-column prop="lockedStock" label="锁定库存" width="100" align="right" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAdjust(row)">调整库存</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 提示信息 -->
      <el-alert
        title="操作说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #default>
          <ul style="margin: 0; padding-left: 20px">
            <li>可以通过商品编码或商品名称搜索商品</li>
            <li>点击"调整库存"按钮可以对商品库存进行调整</li>
            <li>调整数量为正数表示增加库存，负数表示减少库存</li>
            <li>调整库存时可以选择是否更新预警阈值</li>
          </ul>
        </template>
      </el-alert>
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
        <el-form-item label="商品编码">
          <el-input v-model="adjustFormData.productCode" disabled />
        </el-form-item>
        <el-form-item label="当前总库存">
          <el-input v-model="adjustFormData.currentStock" disabled />
        </el-form-item>
        <el-form-item label="当前可用库存">
          <el-input v-model="adjustFormData.currentAvailableStock" disabled />
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
        <el-form-item label="调整后总库存">
          <el-input :value="adjustFormData.currentStock + (adjustFormData.adjustQuantity || 0)" disabled />
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStockPage, adjustStock, type StockVO, type StockDTO } from '@/api/admin/stock'

const loading = ref(false)
const productList = ref<StockVO[]>([])
const searchForm = ref({
  productCode: '',
  productName: ''
})

// 库存调整对话框
const adjustDialogVisible = ref(false)
const adjustFormRef = ref()
const adjustFormData = ref<StockDTO & { 
  productName: string
  productCode: string
  currentStock: number
  currentAvailableStock: number
}>({
  productId: 0,
  productName: '',
  productCode: '',
  currentStock: 0,
  currentAvailableStock: 0,
  adjustQuantity: 0,
  reason: '',
  warningThreshold: undefined
})
const adjustFormRules = {
  adjustQuantity: [{ required: true, message: '请输入调整数量', trigger: 'blur' }]
}

// 搜索商品
const handleSearch = async () => {
  loading.value = true
  try {
    const response = await getStockPage(
      1,
      100, // 搜索时显示更多结果
      undefined,
      searchForm.value.productCode || undefined,
      searchForm.value.productName || undefined
    )
    productList.value = response.records || []
    
    if (productList.value.length === 0) {
      ElMessage.warning('未找到匹配的商品')
    }
  } catch (error) {
    ElMessage.error('搜索商品失败')
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
  productList.value = []
}

// 调整库存
const handleAdjust = (row: StockVO) => {
  adjustFormData.value = {
    productId: row.productId,
    productName: row.productName,
    productCode: row.productCode,
    currentStock: row.totalStock,
    currentAvailableStock: row.availableStock,
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
    
    // 检查调整后库存是否为负数
    const newStock = adjustFormData.value.currentStock + adjustFormData.value.adjustQuantity
    if (newStock < 0) {
      ElMessage.error('调整后库存不能为负数')
      return
    }
    
    const stockDTO: StockDTO = {
      productId: adjustFormData.value.productId,
      adjustQuantity: adjustFormData.value.adjustQuantity,
      reason: adjustFormData.value.reason || undefined,
      warningThreshold: adjustFormData.value.warningThreshold || undefined
    }
    
    await adjustStock(stockDTO)
    ElMessage.success('库存调整成功')
    adjustDialogVisible.value = false
    // 重新搜索以刷新列表
    handleSearch()
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
    productCode: '',
    currentStock: 0,
    currentAvailableStock: 0,
    adjustQuantity: 0,
    reason: '',
    warningThreshold: undefined
  }
  adjustFormRef.value?.clearValidate()
}

// 初始化
onMounted(() => {
  // 页面加载时不自动搜索，等待用户输入
})
</script>

<style scoped lang="scss">
.stock-adjust {
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

  .form-tip {
    margin-top: 5px;
    font-size: 12px;
    color: #909399;
  }
}
</style>
