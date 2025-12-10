<template>
  <div class="deposit-record-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预存款交易记录</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input-number v-model="searchForm.userId" placeholder="用户ID" :min="1" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select v-model="searchForm.event" placeholder="请选择事件类型" clearable style="width: 150px">
            <el-option label="全部" :value="undefined" />
            <el-option label="在线充值" value="在线充值" />
            <el-option label="预存款支付" value="预存款支付" />
            <el-option label="预存款退款" value="预存款退款" />
            <el-option label="代充值" value="代充值" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易类型">
          <el-select v-model="searchForm.type" placeholder="请选择交易类型" clearable style="width: 120px">
            <el-option label="全部" :value="undefined" />
            <el-option label="充值" :value="1" />
            <el-option label="消费" :value="2" />
            <el-option label="退款" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" :value="undefined" />
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="支付中" :value="3" />
            <el-option label="已超时" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="外部交易号">
          <el-input v-model="searchForm.externalTradeNo" placeholder="请输入外部交易号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="内部订单号">
          <el-input v-model="searchForm.internalOrderNo" placeholder="请输入内部订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="searchForm.startDate"
            type="date"
            placeholder="选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="searchForm.endDate"
            type="date"
            placeholder="选择结束日期"
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

      <!-- 交易记录列表 -->
      <el-table :data="recordList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="event" label="事件" width="120" />
        <el-table-column prop="typeName" label="类型" width="80" />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="存入金额" width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.depositAmount > 0" style="color: #67c23a">+¥{{ row.depositAmount.toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="支出金额" width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.expenseAmount > 0" style="color: #f56c6c">-¥{{ row.expenseAmount.toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="冻结金额" width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.frozenAmount > 0" style="color: #e6a23c">¥{{ row.frozenAmount.toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="解冻金额" width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.unfrozenAmount > 0" style="color: #409eff">¥{{ row.unfrozenAmount.toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="当前余额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.currentBalance.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="可用余额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.availableBalance.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">
            {{ getPaymentMethodName(row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link v-if="row.orderNo" type="primary" :underline="false" @click="handleViewOrder(row.orderNo)">
              {{ row.orderNo }}
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="externalTradeNo" label="外部交易号" width="180" show-overflow-tooltip />
        <el-table-column prop="internalOrderNo" label="内部订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
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
    <el-dialog v-model="detailDialogVisible" title="预存款交易记录详情" width="800px">
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="记录ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentRecord.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentRecord.username }}</el-descriptions-item>
        <el-descriptions-item label="事件">{{ currentRecord.event }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag>{{ currentRecord.typeName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentRecord.status)">
            {{ currentRecord.statusName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="存入金额">
          <span v-if="currentRecord.depositAmount > 0" style="color: #67c23a; font-weight: bold">
            +¥{{ currentRecord.depositAmount.toFixed(2) }}
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="支出金额">
          <span v-if="currentRecord.expenseAmount > 0" style="color: #f56c6c; font-weight: bold">
            -¥{{ currentRecord.expenseAmount.toFixed(2) }}
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="冻结金额">
          <span v-if="currentRecord.frozenAmount > 0" style="color: #e6a23c">
            ¥{{ currentRecord.frozenAmount.toFixed(2) }}
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="解冻金额">
          <span v-if="currentRecord.unfrozenAmount > 0" style="color: #409eff">
            ¥{{ currentRecord.unfrozenAmount.toFixed(2) }}
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="当前余额">
          <span style="font-weight: bold">¥{{ currentRecord.currentBalance.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="可用余额">
          <span style="font-weight: bold">¥{{ currentRecord.availableBalance.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ getPaymentMethodName(currentRecord.paymentMethod) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ currentRecord.orderId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单号" :span="2">
          <el-link v-if="currentRecord.orderNo" type="primary" :underline="false" @click="handleViewOrder(currentRecord.orderNo)">
            {{ currentRecord.orderNo }}
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="外部交易号" :span="2">{{ currentRecord.externalTradeNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="内部订单号" :span="2">{{ currentRecord.internalOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRecord.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentRecord.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentRecord.auditTime ? formatDateTime(currentRecord.auditTime) : '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getDepositRecordList, getDepositRecordById, type DepositRecordVO, type DepositQueryDTO } from '@/api/admin/deposit'
import { formatDateTime } from '@/utils'

const router = useRouter()

const loading = ref(false)
const recordList = ref<DepositRecordVO[]>([])

const searchForm = reactive<DepositQueryDTO>({
  username: '',
  userId: undefined,
  event: undefined,
  type: undefined,
  status: undefined,
  orderNo: '',
  externalTradeNo: '',
  internalOrderNo: '',
  startDate: '',
  endDate: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const currentRecord = ref<DepositRecordVO | null>(null)

// 加载交易记录列表
const loadRecordList = async () => {
  loading.value = true
  try {
    const params: DepositQueryDTO = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    
    // 清理空值
    Object.keys(params).forEach(key => {
      if (params[key as keyof DepositQueryDTO] === '' || params[key as keyof DepositQueryDTO] === undefined) {
        delete params[key as keyof DepositQueryDTO]
      }
    })

    const response = await getDepositRecordList(params)
    recordList.value = response.records || []
    pagination.total = response.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadRecordList()
}

// 重置
const handleReset = () => {
  searchForm.username = ''
  searchForm.userId = undefined
  searchForm.event = undefined
  searchForm.type = undefined
  searchForm.status = undefined
  searchForm.orderNo = ''
  searchForm.externalTradeNo = ''
  searchForm.internalOrderNo = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  handleSearch()
}

// 查看详情
const handleView = async (row: DepositRecordVO) => {
  try {
    const response = await getDepositRecordById(row.id)
    currentRecord.value = response
    detailDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 查看订单
const handleViewOrder = (orderNo: string) => {
  // 跳转到订单详情页面（如果存在）
  router.push({
    path: '/admin/order/list',
    query: { orderNo }
  })
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.page = 1
  loadRecordList()
}

// 页码改变
const handlePageChange = (page: number) => {
  pagination.page = page
  loadRecordList()
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  switch (status) {
    case 0:
      return 'warning' // 待审核
    case 1:
      return 'success' // 已通过
    case 2:
      return 'danger' // 已拒绝
    case 3:
      return 'info' // 支付中
    case 4:
      return 'info' // 已超时
    default:
      return ''
  }
}

// 获取支付方式中文名称
const getPaymentMethodName = (paymentMethod?: string) => {
  if (!paymentMethod) {
    return '-'
  }
  switch (paymentMethod.toLowerCase()) {
    case 'alipay':
      return '支付宝'
    case 'wechat':
      return '微信'
    case 'wechatpay':
      return '微信'
    default:
      return paymentMethod
  }
}

onMounted(() => {
  loadRecordList()
})
</script>

<style scoped lang="scss">
.deposit-record-management {
  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>

