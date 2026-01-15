<template>
  <div class="product-sync-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品和库存同步日志</span>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="商品ID">
          <el-input v-model="queryForm.productId" placeholder="请输入商品ID" clearable style="width: 200px" />
        </el-form-item>

        <el-form-item label="商品编码">
          <el-input v-model="queryForm.productCode" placeholder="请输入商品编码" clearable style="width: 200px" />
        </el-form-item>

        <el-form-item label="同步类型">
          <el-select v-model="queryForm.syncType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="普通商品资料上传" value="UPLOAD_ITEM" />
            <el-option label="上传店铺商品资料" value="UPLOAD_SHOP_ITEM" />
            <el-option label="库存同步" value="INVENTORY_SYNC" />
          </el-select>
        </el-form-item>

        <el-form-item label="同步状态">
          <el-select v-model="queryForm.syncStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
            <el-option label="处理中" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="环境类型">
          <el-select v-model="queryForm.envType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="测试环境" value="test" />
            <el-option label="生产环境" value="production" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 同步日志列表 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="日志ID" width="80" />
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column prop="productCode" label="商品编码" width="150" />
        <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="envType" label="环境" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.envType === 'test'" type="info">测试</el-tag>
            <el-tag v-else-if="row.envType === 'production'" type="success">生产</el-tag>
            <el-tag v-else type="warning">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="syncTypeDesc" label="同步类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getSyncTypeTagType(row.syncType)">
              {{ row.syncTypeDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="syncStatusDesc" label="同步状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getSyncStatusTagType(row.syncStatus)">
              {{ row.syncStatusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试次数" width="100" />
        <el-table-column prop="errorCode" label="错误码" width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="同步日志详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="商品ID">{{ detailData.productId }}</el-descriptions-item>
        <el-descriptions-item label="商品编码">{{ detailData.productCode }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ detailData.productName }}</el-descriptions-item>
        <el-descriptions-item label="环境类型">
          <el-tag v-if="detailData.envType === 'test'" type="info">测试</el-tag>
          <el-tag v-else-if="detailData.envType === 'production'" type="success">生产</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="同步类型">
          <el-tag :type="getSyncTypeTagType(detailData.syncType)">
            {{ detailData.syncTypeDesc }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="同步状态">
          <el-tag :type="getSyncStatusTagType(detailData.syncStatus)">
            {{ detailData.syncStatusDesc }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ detailData.retryCount }}</el-descriptions-item>
        <el-descriptions-item label="错误代码" :span="2">{{ detailData.errorCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2">{{ detailData.errorMessage || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求数据" :span="2">
          <el-input
            type="textarea"
            :value="formatJson(detailData.requestData)"
            :rows="8"
            readonly
          />
        </el-descriptions-item>
        <el-descriptions-item label="响应数据" :span="2">
          <el-input
            type="textarea"
            :value="formatJson(detailData.responseData)"
            :rows="8"
            readonly
          />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProductSyncLogs } from '@/api/admin/erp'

// 查询表单
const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  productId: undefined as number | undefined,
  productCode: '',
  syncType: '',
  syncStatus: undefined as number | undefined,
  envType: ''
})

// 表格数据
const tableData = ref<any[]>([])
const total = ref(0)
const loading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const detailData = ref<any>({})

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      productId: queryForm.productId || undefined,
      productCode: queryForm.productCode || undefined,
      syncType: queryForm.syncType || undefined,
      syncStatus: queryForm.syncStatus,
      envType: queryForm.envType || undefined
    }

    const res = await getProductSyncLogs(params)
    if (res) {
      tableData.value = res.records || []
      total.value = res.total || 0
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  queryForm.pageNum = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryForm.productId = undefined
  queryForm.productCode = ''
  queryForm.syncType = ''
  queryForm.syncStatus = undefined
  queryForm.envType = ''
  queryForm.pageNum = 1
  loadData()
}

// 查看详情
const handleViewDetail = (row: any) => {
  detailData.value = row
  detailDialogVisible.value = true
}

// 获取同步类型标签类型
const getSyncTypeTagType = (syncType: string) => {
  switch (syncType) {
    case 'UPLOAD_ITEM':
      return 'primary'
    case 'UPDATE_ITEM':
      return 'warning'
    case 'UPLOAD_SHOP_ITEM':
      return 'success'
    case 'UPDATE_SHOP_ITEM':
      return 'info'
    default:
      return ''
  }
}

// 获取同步状态标签类型
const getSyncStatusTagType = (syncStatus: number) => {
  switch (syncStatus) {
    case 0:
      return 'danger'
    case 1:
      return 'success'
    case 2:
      return 'warning'
    default:
      return 'info'
  }
}

// 格式化JSON
const formatJson = (jsonStr: string) => {
  if (!jsonStr) return ''
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.product-sync-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
