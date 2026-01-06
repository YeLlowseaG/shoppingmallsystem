<template>
  <div class="payment-api-log-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>支付接口日志查询</span>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="支付方式">
          <el-select v-model="queryForm.paymentMethod" placeholder="请选择" clearable style="width: 150px">
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="微信" value="WECHAT" />
          </el-select>
        </el-form-item>

        <el-form-item label="接口类型">
          <el-select v-model="queryForm.apiType" placeholder="请选择" clearable style="width: 180px">
            <el-option label="创建支付" value="CREATE_PAYMENT" />
            <el-option label="退款" value="REFUND" />
            <el-option label="查询订单" value="QUERY_ORDER" />
            <el-option label="回调通知" value="CALLBACK" />
          </el-select>
        </el-form-item>

        <el-form-item label="业务类型">
          <el-select v-model="queryForm.businessType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="订单支付" value="ORDER" />
            <el-option label="预存款充值" value="DEPOSIT" />
          </el-select>
        </el-form-item>

        <el-form-item label="订单号">
          <el-input v-model="queryForm.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
        </el-form-item>

        <el-form-item label="支付流水号">
          <el-input v-model="queryForm.paymentNo" placeholder="请输入支付流水号" clearable style="width: 200px" />
        </el-form-item>

        <el-form-item label="接口状态">
          <el-select v-model="queryForm.apiStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option label="失败" :value="0" />
            <el-option label="成功" :value="1" />
            <el-option label="处理中" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="创建时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 380px"
            @change="handleDateChange"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 日志列表 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="日志ID" width="80" />
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.paymentMethod === 'ALIPAY' ? 'primary' : 'success'">
              {{ row.paymentMethod === 'ALIPAY' ? '支付宝' : '微信' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="apiTypeDesc" label="接口类型" width="120" />
        <el-table-column prop="businessTypeDesc" label="业务类型" width="120" />
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column prop="paymentNo" label="支付流水号" width="180" show-overflow-tooltip />
        <el-table-column prop="externalTradeNo" label="外部交易号" width="180" show-overflow-tooltip />
        <el-table-column prop="apiStatusDesc" label="接口状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getApiStatusTagType(row.apiStatus)">
              {{ row.apiStatusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="httpStatusCode" label="HTTP状态码" width="120" />
        <el-table-column prop="executionTime" label="执行耗时(ms)" width="120" />
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
    <el-dialog v-model="detailVisible" title="支付接口日志详情" width="70%">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="日志ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <el-tag :type="currentRow.paymentMethod === 'ALIPAY' ? 'primary' : 'success'">
            {{ currentRow.paymentMethod === 'ALIPAY' ? '支付宝' : '微信' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="接口类型">{{ currentRow.apiTypeDesc }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ currentRow.businessTypeDesc }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentRow.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付流水号">{{ currentRow.paymentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="外部交易号">{{ currentRow.externalTradeNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="接口状态">
          <el-tag :type="getApiStatusTagType(currentRow.apiStatus)">
            {{ currentRow.apiStatusDesc }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="HTTP状态码">{{ currentRow.httpStatusCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行耗时">{{ currentRow.executionTime ? currentRow.executionTime + 'ms' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ currentRow.retryCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="接口URL" :span="2">{{ currentRow.apiUrl || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentRow.requestMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="错误代码">{{ currentRow.errorCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2">
          <el-input
            type="textarea"
            :rows="3"
            :model-value="currentRow.errorMessage || '-'"
            readonly
            style="font-family: monospace;"
          />
        </el-descriptions-item>
        <el-descriptions-item label="请求数据" :span="2">
          <el-input
            type="textarea"
            :rows="8"
            :model-value="formatJson(currentRow.requestData)"
            readonly
            style="font-family: monospace;"
          />
        </el-descriptions-item>
        <el-descriptions-item label="响应数据" :span="2">
          <el-input
            type="textarea"
            :rows="8"
            :model-value="formatJson(currentRow.responseData)"
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
import { ElMessage } from 'element-plus'
import { getPaymentApiLogs } from '@/api/admin/payment'

const loading = ref(false)
const detailVisible = ref(false)
const currentRow = ref<any>(null)
const dateRange = ref<[string, string] | null>(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 20,
  paymentMethod: undefined,
  apiType: undefined,
  businessType: undefined,
  orderNo: undefined,
  paymentNo: undefined,
  apiStatus: undefined,
  startTime: undefined,
  endTime: undefined
})

const tableData = ref([])
const total = ref(0)

// 格式化JSON
const formatJson = (jsonStr: string | null) => {
  if (!jsonStr) return '-'
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch {
    return jsonStr
  }
}

// 日期范围变化
const handleDateChange = (dates: [string, string] | null) => {
  if (dates && dates.length === 2) {
    queryForm.startTime = dates[0]
    queryForm.endTime = dates[1]
  } else {
    queryForm.startTime = undefined
    queryForm.endTime = undefined
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await getPaymentApiLogs(queryForm)
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
  queryForm.paymentMethod = undefined
  queryForm.apiType = undefined
  queryForm.businessType = undefined
  queryForm.orderNo = undefined
  queryForm.paymentNo = undefined
  queryForm.apiStatus = undefined
  queryForm.startTime = undefined
  queryForm.endTime = undefined
  dateRange.value = null
  queryForm.pageNum = 1
  loadData()
}

// 查看详情
const handleViewDetail = (row: any) => {
  currentRow.value = row
  detailVisible.value = true
}

// 获取接口状态标签类型
const getApiStatusTagType = (apiStatus: number) => {
  const statusMap: Record<number, any> = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return statusMap[apiStatus] || ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.payment-api-log-container {
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












