<template>
  <div class="order-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单编号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单编号"
            clearable
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待支付" value="待支付" />
            <el-option label="待发货" value="待发货" />
            <el-option label="已发货" value="已发货" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已取消" value="已取消" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 订单列表 -->
      <el-table :data="orderList" border style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="buyerName" label="采购商" width="120" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            <span>¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              effect="dark"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === '待发货'"
              size="small"
              type="primary"
              @click="handleShip(row)"
            >
              发货
            </el-button>
            <el-button
              v-if="row.status === '待支付'"
              size="small"
              type="danger"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
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
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = ref({
  orderNo: '',
  status: ''
})

const orderList = ref<any[]>([])

const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

const getStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    '待支付': 'warning',
    '待发货': 'info',
    '已发货': 'primary',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return typeMap[status] || 'info'
}

const handleSearch = () => {
  pagination.value.current = 1
  loadOrderList()
}

const handleReset = () => {
  searchForm.value = {
    orderNo: '',
    status: ''
  }
  loadOrderList()
}

const handleView = (row: any) => {
  ElMessage.info(`查看订单详情：${row.orderNo}`)
}

const handleShip = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('发货成功')
    loadOrderList()
  } catch {
    // 取消操作
  }
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认取消订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('订单已取消')
    loadOrderList()
  } catch {
    // 取消操作
  }
}

const handleSizeChange = () => {
  loadOrderList()
}

const handleCurrentChange = () => {
  loadOrderList()
}

const loadOrderList = async () => {
  // TODO: 调用后端API加载订单列表
  ElMessage.info('订单列表功能开发中，敬请期待！')
  orderList.value = []
  pagination.value.total = 0
}

onMounted(() => {
  loadOrderList()
})
</script>

<style scoped>
.order-list-container {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: 500;
}

.search-form {
  margin-bottom: 20px;
}
</style>
