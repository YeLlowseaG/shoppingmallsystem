<template>
  <div class="refund-list-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单退款记录</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="退款单号">
          <el-input v-model="searchForm.refundNo" placeholder="请输入退款单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="退款状态">
          <el-select v-model="searchForm.refundStatus" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="全部" :value="undefined" />
            <el-option label="退款中" :value="3" />
            <el-option label="退款成功" :value="4" />
            <el-option label="退款失败" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款类型">
          <el-select v-model="searchForm.refundType" placeholder="请选择类型" clearable style="width: 150px">
            <el-option label="全部" :value="undefined" />
            <el-option label="部分退款" :value="1" />
            <el-option label="全额退款" :value="2" />
          </el-select>
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

      <!-- 退款记录列表 -->
      <el-table :data="refundList" v-loading="loading" border>
        <el-table-column prop="refundNo" label="退款单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="handleView(row)" style="cursor: pointer;">
              {{ row.refundNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="refundAmount" label="退款金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.refundAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="refundTypeText" label="退款类型" width="100" align="center" />
        <el-table-column prop="refundStatusText" label="退款状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.refundStatus)">
              {{ row.refundStatusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operatorTime" label="操作时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.operatorTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="refundTime" label="退款完成时间" width="170">
          <template #default="{ row }">
            {{ row.refundTime ? formatDateTime(row.refundTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="refundReason" label="退款原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看详情</el-button>
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
    <el-dialog v-model="detailDialogVisible" title="退款记录详情" width="900px">
      <div v-if="currentRefund">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="退款单号">{{ currentRefund.refundNo }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentRefund.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">
            <span style="color: #e4393c; font-weight: bold;">¥{{ currentRefund.refundAmount.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="退款类型">
            <el-tag>{{ currentRefund.refundTypeText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="退款状态">
            <el-tag :type="getStatusTagType(currentRefund.refundStatus)">
              {{ currentRefund.refundStatusText }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="退款原因" :span="2">{{ currentRefund.refundReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentRefund.operatorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ formatDateTime(currentRefund.operatorTime) }}</el-descriptions-item>
          <el-descriptions-item label="操作备注" :span="2">{{ currentRefund.operatorRemark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退款完成时间">
            {{ currentRefund.refundTime ? formatDateTime(currentRefund.refundTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="退款支付方式">{{ getPaymentMethodName(currentRefund.refundPaymentMethod) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退款支付单号" :span="2">{{ currentRefund.refundPaymentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentRefund.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 退款明细 -->
        <el-divider>退款明细</el-divider>
        <el-table :data="currentRefund.refundItems" border style="margin-top: 20px">
          <el-table-column prop="productCode" label="商品编码" width="120" />
          <el-table-column label="商品名称" min-width="250">
            <template #default="{ row }">
              <div>{{ row.productName }}</div>
              <div v-if="row.specCombination" class="sku-spec-text">
                {{ row.specCombination }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="refundQuantity" label="退款数量" width="100" align="center" />
          <el-table-column prop="refundPrice" label="退款单价" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.refundPrice.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="refundSubtotal" label="退款小计" width="120" align="right">
            <template #default="{ row }">
              <span style="color: #e4393c; font-weight: bold;">¥{{ row.refundSubtotal.toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils'
import { getRefundList, getRefundDetail } from '@/api/admin/order'
import type { OrderRefundVO, OrderRefundQueryDTO } from '@/api/admin/order'

const loading = ref(false)
const refundList = ref<OrderRefundVO[]>([])

const searchForm = reactive<OrderRefundQueryDTO>({
  refundNo: '',
  orderNo: '',
  refundStatus: undefined,
  refundType: undefined,
  startDate: '',
  endDate: '',
  pageNum: 1,
  pageSize: 10
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const currentRefund = ref<OrderRefundVO | null>(null)

// 加载退款记录列表
const loadRefundList = async () => {
  loading.value = true
  try {
    const response = await getRefundList({
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    })
    refundList.value = response.records || []
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
  loadRefundList()
}

// 重置
const handleReset = () => {
  searchForm.refundNo = ''
  searchForm.orderNo = ''
  searchForm.refundStatus = undefined
  searchForm.refundType = undefined
  searchForm.startDate = ''
  searchForm.endDate = ''
  pagination.page = 1
  handleSearch()
}

// 查看详情
const handleView = async (row: OrderRefundVO) => {
  try {
    const refund = await getRefundDetail(row.id)
    currentRefund.value = refund
    detailDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  switch (status) {
    case 3:
      return 'warning' // 退款中
    case 4:
      return 'success' // 退款成功
    case 5:
      return 'danger' // 退款失败
    default:
      return 'info'
  }
}

// 获取支付方式中文名称
const getPaymentMethodName = (paymentMethod?: string) => {
  if (!paymentMethod) {
    return '-'
  }
  switch (paymentMethod.toUpperCase()) {
    case 'WECHAT':
      return '微信支付'
    case 'ALIPAY':
      return '支付宝'
    case 'PRE_DEPOSIT':
      return '预存款支付'
    case 'OFFLINE':
      return '线下支付'
    default:
      return paymentMethod
  }
}

// 分页
const handleSizeChange = () => {
  loadRefundList()
}

const handlePageChange = () => {
  loadRefundList()
}

onMounted(() => {
  loadRefundList()
})
</script>

<style scoped lang="scss">
.refund-list-management {
  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .sku-spec-text {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}
</style>




















