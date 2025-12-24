<template>
  <div class="order-sync-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单同步日志</span>
          <el-button type="primary" @click="handlePullPending" :loading="pulling">拉取待发货订单物流</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="订单ID">
          <el-input v-model="queryForm.orderId" placeholder="请输入订单ID" clearable style="width: 200px" />
        </el-form-item>

        <el-form-item label="同步类型">
          <el-select v-model="queryForm.syncType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="订单推送" value="PUSH_ORDER" />
            <el-option label="物流拉取" value="PULL_LOGISTICS" />
            <el-option label="订单查询" value="QUERY_ORDER" />
          </el-select>
        </el-form-item>

        <el-form-item label="同步状态">
          <el-select v-model="queryForm.syncStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
            <el-option label="处理中" :value="2" />
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
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看详情</el-button>
            <el-button
              link
              type="warning"
              @click="handleRetry(row)"
              v-if="row.syncStatus === 0 && row.syncType === 'PUSH_ORDER'"
            >
              重试
            </el-button>
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
    <el-dialog v-model="detailVisible" title="同步日志详情" width="60%">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="日志ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ currentRow.orderId }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="同步类型">{{ currentRow.syncTypeDesc }}</el-descriptions-item>
        <el-descriptions-item label="同步状态">
          <el-tag :type="getSyncStatusTagType(currentRow.syncStatus)">
            {{ currentRow.syncStatusDesc }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ currentRow.retryCount }}</el-descriptions-item>
        <el-descriptions-item label="错误码">{{ currentRow.errorCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求数据" :span="2">
          <el-input
            type="textarea"
            :rows="6"
            v-model="currentRow.requestData"
            readonly
            style="font-family: monospace;"
          />
        </el-descriptions-item>
        <el-descriptions-item label="响应数据" :span="2">
          <el-input
            type="textarea"
            :rows="6"
            v-model="currentRow.responseData"
            readonly
            style="font-family: monospace;"
          />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSyncLogs, retryPushOrder, pullPendingLogistics } from '@/api/admin/erp'

const loading = ref(false)
const pulling = ref(false)
const detailVisible = ref(false)
const currentRow = ref<any>(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 20,
  orderId: undefined,
  syncType: undefined,
  syncStatus: undefined
})

const tableData = ref([])
const total = ref(0)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await getSyncLogs(queryForm)
    tableData.value = data?.records || []
    total.value = data?.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryForm.pageNum = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryForm.orderId = undefined
  queryForm.syncType = undefined
  queryForm.syncStatus = undefined
  queryForm.pageNum = 1
  loadData()
}

// 查看详情
const handleViewDetail = (row: any) => {
  currentRow.value = row
  detailVisible.value = true
}

// 重试
const handleRetry = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要重试推送订单 ${row.orderNo} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await retryPushOrder(row.orderId)
    ElMessage.success('重试成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '重试失败')
    }
  }
}

// 拉取待发货订单物流
const handlePullPending = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要拉取所有待发货订单的物流信息吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    pulling.value = true
    const count = await pullPendingLogistics()
    ElMessage.success(`成功拉取 ${count || 0} 个订单的物流信息`)
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '拉取失败')
    }
  } finally {
    pulling.value = false
  }
}

// 获取同步类型标签类型
const getSyncTypeTagType = (syncType: string) => {
  const typeMap: Record<string, any> = {
    'PUSH_ORDER': 'primary',
    'PULL_LOGISTICS': 'success',
    'QUERY_ORDER': 'info'
  }
  return typeMap[syncType] || ''
}

// 获取同步状态标签类型
const getSyncStatusTagType = (syncStatus: number) => {
  const statusMap: Record<number, any> = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return statusMap[syncStatus] || ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.order-sync-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }
}
</style>
