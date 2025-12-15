<template>
  <div class="stock-notification-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>缺货登记管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.productName"
            placeholder="请输入商品名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待通知" :value="0" />
            <el-option label="已通知" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" min-width="200" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.mainImage"
              :src="row.mainImage"
              :preview-src-list="[row.mainImage]"
              fit="cover"
              style="width: 60px; height: 60px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="basePrice" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.basePrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="contactEmail" label="联系邮箱" width="180" />
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="登记时间" width="160" />
        <el-table-column prop="notifiedAt" label="通知时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              @click="handleNotify(row)"
            >
              发送通知
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="缺货登记详情" width="600px">
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="登记ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ currentRow.productName }}</el-descriptions-item>
        <el-descriptions-item label="商品编码">{{ currentRow.productCode }}</el-descriptions-item>
        <el-descriptions-item label="商品价格">¥{{ currentRow.basePrice?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ currentRow.contactEmail }}</el-descriptions-item>
        <el-descriptions-item label="通知方式">{{ currentRow.notifyType }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRow.remark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow.status)">
            {{ currentRow.statusDesc }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="登记时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="通知时间">{{ currentRow.notifiedAt || '未通知' }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">{{ currentRow.expiredAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getStockNotificationPage, notifyStockUsers } from '@/api/admin/stock-notification'
import type { StockNotificationVO } from '@/api/admin/stock-notification'

// 搜索表单
const searchForm = reactive({
  productName: '',
  status: undefined as number | undefined
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref<StockNotificationVO[]>([])
const loading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const currentRow = ref<StockNotificationVO | null>(null)

// 获取状态标签类型
const getStatusType = (status: number) => {
  switch (status) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'info'
    default:
      return ''
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getStockNotificationPage({
      current: pagination.current,
      size: pagination.size,
      productName: searchForm.productName || undefined,
      status: searchForm.status
    })
    tableData.value = res.records
    pagination.total = res.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.productName = ''
  searchForm.status = undefined
  pagination.current = 1
  loadData()
}

// 分页变化
const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

// 发送通知
const handleNotify = async (row: StockNotificationVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要通知用户"${row.productName}"已补货吗？`,
      '发送通知',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await notifyStockUsers(row.productId)
    ElMessage.success('通知发送成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '通知发送失败')
    }
  }
}

// 查看详情
const handleViewDetail = (row: StockNotificationVO) => {
  currentRow.value = row
  detailDialogVisible.value = true
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.stock-notification-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: bold;
  }

  .search-form {
    margin-bottom: 0;
  }
}
</style>
