<template>
  <div class="payment-record-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>支付记录管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="支付流水号">
          <el-input v-model="searchForm.paymentNo" placeholder="请输入支付流水号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="请选择支付方式" clearable style="width: 150px">
            <el-option label="全部" :value="undefined" />
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="预存款" value="PRE_DEPOSIT" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.paymentStatus" placeholder="请选择支付状态" clearable style="width: 120px">
            <el-option label="全部" :value="undefined" />
            <el-option label="待支付" :value="0" />
            <el-option label="支付中" :value="1" />
            <el-option label="已支付" :value="2" />
            <el-option label="已关闭" :value="3" />
            <el-option label="已失败" :value="4" />
            <el-option label="已退款" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="searchForm.startTime"
            type="date"
            placeholder="选择开始时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="searchForm.endTime"
            type="date"
            placeholder="选择结束时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 支付记录列表 -->
      <el-table 
        :data="recordList" 
        v-loading="loading" 
        border 
        stripe
        height="600"
        :row-class-name="getRowClassName"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column prop="username" label="用户名" width="100" />
        <el-table-column prop="paymentNo" label="支付流水号" width="190" show-overflow-tooltip />
        <el-table-column prop="paymentMethodName" label="支付方式" width="90">
          <template #default="{ row }">
            <el-tag :type="getPaymentMethodTagType(row.paymentMethod)" size="small">
              {{ row.paymentMethodName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付金额" width="90" align="right">
          <template #default="{ row }">
            <span>¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已退款金额" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.refundedAmount > 0" style="color: #f56c6c">
              ¥{{ row.refundedAmount.toFixed(2) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="可退款金额" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.refundableAmount > 0" style="color: #67c23a">
              ¥{{ row.refundableAmount.toFixed(2) }}
            </span>
            <span v-else style="color: #909399">¥0.00</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatusName" label="支付状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.paymentStatus)" size="small">
              {{ row.paymentStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" width="160">
          <template #default="{ row }">
            {{ row.paymentTime ? formatDateTime(row.paymentTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="支付记录详情" width="700px">
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="记录ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentRecord.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentRecord.username }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentRecord.userId }}</el-descriptions-item>
        <el-descriptions-item label="支付流水号">{{ currentRecord.paymentNo }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentRecord.paymentMethodName }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">¥{{ currentRecord.amount.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="已退款金额">
          ¥{{ (currentRecord.refundedAmount || 0).toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="可退款金额">
          ¥{{ currentRecord.refundableAmount.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="getStatusTagType(currentRecord.paymentStatus)">
            {{ currentRecord.paymentStatusName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付时间">
          {{ currentRecord.paymentTime ? formatDateTime(currentRecord.paymentTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="退款时间" v-if="currentRecord.refundTime">
          {{ formatDateTime(currentRecord.refundTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="退款原因" v-if="currentRecord.refundReason" :span="2">
          {{ currentRecord.refundReason }}
        </el-descriptions-item>
        <el-descriptions-item label="退款操作人" v-if="currentRecord.refundOperatorName">
          {{ currentRecord.refundOperatorName }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentRecord.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getPaymentRecordList,
  getPaymentRecordById,
  type PaymentRecordVO,
  type PaymentRecordQueryDTO
} from '@/api/admin/finance'

// 搜索表单
const searchForm = reactive<PaymentRecordQueryDTO>({
  pageNum: 1,
  pageSize: 10,
  orderNo: undefined,
  paymentNo: undefined,
  paymentMethod: undefined,
  paymentStatus: undefined,
  startTime: undefined,
  endTime: undefined
})

// 数据列表
const recordList = ref<PaymentRecordVO[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 当前记录
const currentRecord = ref<PaymentRecordVO | null>(null)

// 详情对话框
const detailDialogVisible = ref(false)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const response = await getPaymentRecordList(params)
    recordList.value = response.records
    pagination.total = response.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    pageNum: 1,
    pageSize: 10,
    orderNo: undefined,
    paymentNo: undefined,
    paymentMethod: undefined,
    paymentStatus: undefined,
    startTime: undefined,
    endTime: undefined
  })
  handleSearch()
}

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
}

const handlePageChange = (page: number) => {
  pagination.pageNum = page
  loadData()
}

// 查看详情
const handleView = async (row: PaymentRecordVO) => {
  try {
    const record = await getPaymentRecordById(row.id)
    currentRecord.value = record
    detailDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取详情失败')
  }
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  switch (status) {
    case 0:
      return 'info' // 待支付
    case 1:
      return 'warning' // 支付中
    case 2:
      return 'success' // 已支付
    case 3:
      return '' // 已关闭
    case 4:
      return 'danger' // 已失败
    case 5:
      return 'warning' // 已退款
    default:
      return ''
  }
}

// 获取支付方式标签类型
const getPaymentMethodTagType = (paymentMethod: string) => {
  switch (paymentMethod) {
    case 'WECHAT':
      return 'success' // 微信支付 - 绿色
    case 'ALIPAY':
      return 'primary' // 支付宝 - 蓝色
    case 'PRE_DEPOSIT':
      return 'warning' // 预存款 - 橙色
    default:
      return ''
  }
}

// 获取行样式类名（用于设置行背景色）
const getRowClassName = ({ row }: { row: PaymentRecordVO }) => {
  if (!row.paymentMethod) return ''
  
  switch (row.paymentMethod) {
    case 'WECHAT':
      return 'payment-row-wechat'
    case 'ALIPAY':
      return 'payment-row-alipay'
    case 'PRE_DEPOSIT':
      return 'payment-row-deposit'
    default:
      return ''
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.payment-record-management {
  .search-form {
    margin-bottom: 20px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  // 表头固定样式
  :deep(.el-table) {
    .el-table__header-wrapper {
      position: sticky;
      top: 0;
      z-index: 10;
      background: #fff;
    }
  }

  // 不同支付方式的行背景色
  :deep(.el-table__body) {
    .payment-row-wechat {
      background-color: #f0f9ff !important; // 微信支付 - 浅蓝色背景
      
      &:hover {
        background-color: #e0f2fe !important;
      }
    }

    .payment-row-alipay {
      background-color: #fef3f2 !important; // 支付宝 - 浅红色背景
      
      &:hover {
        background-color: #fee4e2 !important;
      }
    }

    .payment-row-deposit {
      background-color: #fff7ed !important; // 预存款 - 浅橙色背景
      
      &:hover {
        background-color: #ffedd5 !important;
      }
    }
  }
}
</style>

