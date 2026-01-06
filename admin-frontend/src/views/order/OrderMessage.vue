<template>
  <div class="order-message-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单问题</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="问题类型">
          <el-select v-model="searchForm.messageType" placeholder="请选择类型" clearable style="width: 150px">
            <el-option label="全部" :value="undefined" />
            <el-option label="我已付款" :value="1" />
            <el-option label="我有问题" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="全部" :value="undefined" />
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已处理" :value="2" />
            <el-option label="已关闭" :value="3" />
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
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 订单问题列表 -->
      <el-table
        v-loading="loading"
        :data="messageList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="realName" label="用户" width="120">
          <template #default="{ row }">
            {{ row.realName || row.username || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="messageTypeText" label="问题类型" width="120" align="center" />
        <el-table-column label="问题内容" min-width="200">
          <template #default="{ row }">
            <div v-if="row.messageType === 1">
              <div>付款金额：¥{{ row.paymentAmount }}</div>
              <div>付款方式：{{ row.paymentMethod }}</div>
              <div v-if="row.paymentDate">
                付款时间：{{ row.paymentDate }} {{ row.paymentTime || '' }}
              </div>
            </div>
            <div v-else>
              <div><strong>{{ row.title }}</strong></div>
              <div style="color: #666; margin-top: 5px;">{{ row.content }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusTagType(row.status)"
              size="small"
            >
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handlerName" label="处理人" width="120" />
        <el-table-column prop="handleTime" label="处理时间" width="180">
          <template #default="{ row }">
            {{ row.handleTime ? formatDateTime(row.handleTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="warning"
              link
              size="small"
              @click="handleProcess(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadMessageList"
          @current-change="loadMessageList"
        />
      </div>
    </el-card>

    <!-- 详情/处理对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        v-if="currentMessage"
        :model="currentMessage"
        label-width="120px"
      >
        <el-form-item label="订单号">
          <span>{{ currentMessage.orderNo }}</span>
        </el-form-item>
        <el-form-item label="用户">
          <span>{{ currentMessage.realName || currentMessage.username || '-' }}</span>
        </el-form-item>
        <el-form-item label="问题类型">
          <span>{{ currentMessage.messageTypeText }}</span>
        </el-form-item>
        <el-form-item v-if="currentMessage.messageType === 1" label="付款信息">
          <div>付款金额：¥{{ currentMessage.paymentAmount }}</div>
          <div>付款方式：{{ currentMessage.paymentMethod }}</div>
          <div v-if="currentMessage.paymentDate">
            付款时间：{{ currentMessage.paymentDate }} {{ currentMessage.paymentTime || '' }}
          </div>
        </el-form-item>
        <el-form-item v-else label="问题内容">
          <div><strong>{{ currentMessage.title }}</strong></div>
          <div style="color: #666; margin-top: 5px;">{{ currentMessage.content }}</div>
        </el-form-item>
        <el-form-item v-if="currentMessage.remarks" label="备注">
          <span>{{ currentMessage.remarks }}</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(currentMessage.status)" size="small">
            {{ currentMessage.statusText }}
          </el-tag>
        </el-form-item>
        <el-form-item v-if="currentMessage.handlerName" label="处理人">
          <span>{{ currentMessage.handlerName }}</span>
        </el-form-item>
        <el-form-item v-if="currentMessage.handleTime" label="处理时间">
          <span>{{ formatDateTime(currentMessage.handleTime) }}</span>
        </el-form-item>
        <el-form-item v-if="currentMessage.handleRemark" label="处理备注">
          <span>{{ currentMessage.handleRemark }}</span>
        </el-form-item>

        <!-- 处理表单 -->
        <template v-if="isProcessing && (currentMessage.status === 0 || currentMessage.status === 1)">
          <el-divider />
          <el-form-item label="处理状态" required>
            <el-radio-group v-model="handleForm.status">
              <el-radio :label="1">处理中</el-radio>
              <el-radio :label="2">已处理</el-radio>
              <el-radio :label="3">已关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="处理备注">
            <el-input
              v-model="handleForm.handleRemark"
              type="textarea"
              :rows="4"
              placeholder="请输入处理备注"
            />
          </el-form-item>
        </template>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button
            v-if="isProcessing && (currentMessage?.status === 0 || currentMessage?.status === 1)"
            type="primary"
            @click="handleSubmitProcess"
          >
            提交处理
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getOrderMessagePage, getOrderMessageById, handleOrderMessage } from '@/api/admin/orderMessage'
import type { OrderMessageVO, OrderMessageQueryDTO, OrderMessageHandleDTO } from '@/api/admin/orderMessage'
import { formatDateTime } from '@/utils'

const loading = ref(false)
const messageList = ref<OrderMessageVO[]>([])
const currentMessage = ref<OrderMessageVO | null>(null)

// 搜索表单
const searchForm = reactive<OrderMessageQueryDTO>({
  orderNo: '',
  messageType: undefined,
  status: undefined,
  startDate: '',
  endDate: '',
  current: 1,
  size: 10
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('订单问题详情')
const isProcessing = ref(false)

// 处理表单
const handleForm = reactive<OrderMessageHandleDTO>({
  id: 0,
  status: 1,
  handleRemark: ''
})

// 加载订单问题列表
const loadMessageList = async () => {
  loading.value = true
  try {
    const params: OrderMessageQueryDTO = {
      ...searchForm,
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    const response = await getOrderMessagePage(params)
    messageList.value = response.records
    pagination.total = response.total
  } catch (error) {
    ElMessage.error('加载订单问题列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadMessageList()
}

// 重置
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.messageType = undefined
  searchForm.status = undefined
  searchForm.startDate = ''
  searchForm.endDate = ''
  pagination.currentPage = 1
  loadMessageList()
}

// 查看详情
const handleView = async (row: OrderMessageVO) => {
  try {
    const message = await getOrderMessageById(row.id)
    currentMessage.value = message
    dialogTitle.value = '订单问题详情'
    isProcessing.value = false
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载订单问题详情失败')
  }
}

// 处理
const handleProcess = async (row: OrderMessageVO) => {
  try {
    const message = await getOrderMessageById(row.id)
    currentMessage.value = message
    dialogTitle.value = '处理订单问题'
    isProcessing.value = true
    handleForm.id = message.id
    handleForm.status = message.status === 0 ? 1 : message.status
    handleForm.handleRemark = ''
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载订单问题详情失败')
  }
}

// 提交处理
const handleSubmitProcess = async () => {
  if (!handleForm.status) {
    ElMessage.warning('请选择处理状态')
    return
  }

  try {
    await ElMessageBox.confirm('确定要提交处理吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await handleOrderMessage(handleForm)
    ElMessage.success('处理成功')
    dialogVisible.value = false
    loadMessageList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '处理失败')
    }
  }
}

// 对话框关闭
const handleDialogClose = () => {
  currentMessage.value = null
  isProcessing.value = false
  handleForm.id = 0
  handleForm.status = 1
  handleForm.handleRemark = ''
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  switch (status) {
    case 0:
      return 'warning'
    case 1:
      return 'info'
    case 2:
      return 'success'
    case 3:
      return 'info'
    default:
      return ''
  }
}

// 初始化
onMounted(() => {
  loadMessageList()
})
</script>

<style scoped lang="scss">
.order-message-management {
  .search-form {
    margin-bottom: 20px;
  }

  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>









































