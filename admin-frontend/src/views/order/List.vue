<template>
  <div class="order-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="收货人">
          <el-input v-model="searchForm.recipientName" placeholder="请输入收货人姓名" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.orderStatus" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="全部" :value="undefined" />
            <el-option label="待付款" :value="0" />
            <el-option label="已付款未发货" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
            <el-option label="已退款" :value="5" />
            <el-option label="已退货" :value="6" />
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

      <!-- 订单状态标签页 -->
      <div class="order-tabs">
        <div
          v-for="tab in orderTabs"
          :key="tab.value === undefined ? 'all' : tab.value"
          :class="['tab-item', { active: activeTab === tab.value }]"
          @click="handleTabChange(tab.value)"
        >
          {{ tab.label }}
        </div>
      </div>

      <!-- 订单列表 -->
      <el-table :data="orderList" v-loading="loading" border>
        <el-table-column prop="orderNo" label="订单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="handleView(row)" style="cursor: pointer;">
              {{ row.orderNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="recipientName" label="收货人" width="100" />
        <el-table-column prop="description" label="订单描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="orderDate" label="下单日期" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.orderDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewLogistics(row)">物流信息</el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消订单
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              @click="handleShip(row)"
            >
              发货
            </el-button>
            <el-button type="info" size="small" @click="handleRemark(row)">备注</el-button>
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
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="900px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="下单日期">{{ formatDateTime(currentOrder.orderDate) }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusTagType(currentOrder.status)">
            {{ currentOrder.statusText }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="商品总金额">¥{{ currentOrder.totalProductAmount.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="运费">¥{{ currentOrder.shippingFee.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="商品数量">{{ currentOrder.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="订单备注" :span="2">{{ currentOrder.orderNotes || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 商品列表 -->
      <el-divider>商品信息</el-divider>
      <el-table :data="currentOrder?.items" border style="margin-top: 20px">
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="getImageUrl(row.image)"
              :alt="row.name"
              fit="cover"
              style="width: 60px; height: 60px;"
              :preview-src-list="[getImageUrl(row.image)]"
              :initial-index="0"
              preview-teleported
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column label="商品名称" width="300">
          <template #default="{ row }">
            <div>{{ row.name }}</div>
            <div v-if="formatSpecText(row.specCombination)" class="sku-spec-text">
              {{ formatSpecText(row.specCombination) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="subtotal" label="小计" width="120">
          <template #default="{ row }">
            ¥{{ row.subtotal.toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 收货人信息 -->
      <el-divider>收货人信息</el-divider>
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="收货人姓名">{{ currentOrder.recipientInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.recipientInfo.phone }}</el-descriptions-item>
        <el-descriptions-item label="配送地区">{{ currentOrder.recipientInfo.region }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.recipientInfo.address }}</el-descriptions-item>
        <el-descriptions-item label="配送方式">{{ currentOrder.recipientInfo.shippingMethod }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentOrder.recipientInfo.paymentMethod }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog v-model="shipDialogVisible" title="订单发货" width="500px">
      <el-form :model="shipForm" :rules="shipRules" ref="shipFormRef" label-width="100px">
        <el-form-item label="订单号">
          <el-input :value="currentOrder?.orderNo" disabled />
        </el-form-item>
        <el-form-item label="物流公司" prop="logisticsCompany">
          <el-input v-model="shipForm.logisticsCompany" placeholder="请输入物流公司" />
        </el-form-item>
        <el-form-item label="物流单号" prop="logisticsNo">
          <el-input v-model="shipForm.logisticsNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleShipSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 备注对话框 -->
    <el-dialog v-model="remarkDialogVisible" title="订单备注" width="500px">
      <el-form :model="remarkForm" ref="remarkFormRef" label-width="100px">
        <el-form-item label="订单号">
          <el-input :value="currentOrder?.orderNo" disabled />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="remarkForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="remarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRemarkSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 物流信息对话框 -->
    <el-dialog v-model="logisticsDialogVisible" title="物流信息" width="600px">
      <div v-if="currentLogisticsOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ currentLogisticsOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentLogisticsOrder.recipientName }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <div v-if="currentLogisticsOrder.logistics">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="承运公司">{{ currentLogisticsOrder.logistics.carrier }}</el-descriptions-item>
            <el-descriptions-item label="发货日期">{{ currentLogisticsOrder.logistics.shipDate }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ currentLogisticsOrder.logistics.shipTime }}</el-descriptions-item>
            <el-descriptions-item label="物流单号">
              <span>{{ currentLogisticsOrder.logistics.trackingNo }}</span>
              <el-button
                type="text"
                size="small"
                style="margin-left: 10px;"
                @click="handleCopyTrackingNo(currentLogisticsOrder.logistics!.trackingNo)"
              >
                复制
              </el-button>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else style="text-align: center; padding: 40px; color: #999;">
          暂无物流信息
        </div>
      </div>
      <template #footer>
        <el-button @click="logisticsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils'
import {
  getOrderList,
  getOrderDetail,
  cancelOrder,
  shipOrder,
  addOrderRemark
} from '@/api/admin/order'
import type { OrderListVO, OrderDetailVO } from '@/api/admin/order'

const loading = ref(false)
const orderList = ref<OrderListVO[]>([])

const searchForm = reactive({
  orderNo: '',
  recipientName: '',
  orderStatus: undefined as number | undefined,
  startDate: '',
  endDate: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const shipDialogVisible = ref(false)
const remarkDialogVisible = ref(false)
const logisticsDialogVisible = ref(false)
const currentOrder = ref<OrderDetailVO | null>(null)
const currentLogisticsOrder = ref<OrderListVO | null>(null)

const shipForm = reactive({
  logisticsCompany: '',
  logisticsNo: ''
})

const remarkForm = reactive({
  remark: ''
})

const shipFormRef = ref<FormInstance>()
const remarkFormRef = ref<FormInstance>()

const shipRules: FormRules = {
  logisticsCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
  logisticsNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
}

// 订单状态标签页配置
const orderTabs = [
  { label: '全部订单', value: undefined },
  { label: '待付款', value: 0 },
  { label: '已付款未发货', value: 1 },
  { label: '已发货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
  { label: '已退款', value: 5 },
  { label: '已退货', value: 6 }
]

const activeTab = ref<number | undefined>(undefined)


// 加载订单列表
const loadOrderList = async () => {
  loading.value = true
  try {
    const response = await getOrderList({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      orderNo: searchForm.orderNo || undefined,
      recipientName: searchForm.recipientName || undefined,
      orderStatus: searchForm.orderStatus,
      startDate: searchForm.startDate || undefined,
      endDate: searchForm.endDate || undefined
    })
    orderList.value = response.records || []
    pagination.total = response.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 标签页切换
const handleTabChange = (value: number | undefined) => {
  if (activeTab.value === value) {
    return // 如果点击的是当前标签，不执行任何操作
  }
  activeTab.value = value
  searchForm.orderStatus = value
  pagination.page = 1
  loadOrderList()
}

// 搜索
const handleSearch = () => {
  // 如果搜索时没有指定状态，使用当前tab的状态
  if (searchForm.orderStatus === undefined && activeTab.value !== undefined) {
    searchForm.orderStatus = activeTab.value
  }
  // 同步tab状态
  activeTab.value = searchForm.orderStatus
  pagination.page = 1
  loadOrderList()
}

// 重置
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.recipientName = ''
  searchForm.orderStatus = undefined
  searchForm.startDate = ''
  searchForm.endDate = ''
  activeTab.value = undefined
  handleSearch()
}

// 查看详情
const handleView = async (row: OrderListVO) => {
  try {
    const order = await getOrderDetail(row.orderNo)
    currentOrder.value = order
    detailDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 查看物流信息
const handleViewLogistics = (row: OrderListVO) => {
  currentLogisticsOrder.value = row
  logisticsDialogVisible.value = true
}

// 复制物流单号
const handleCopyTrackingNo = (trackingNo: string) => {
  navigator.clipboard.writeText(trackingNo).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 取消订单
const handleCancel = async (row: OrderListVO) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？取消后库存将自动恢复。', '提示', {
      type: 'warning'
    })
    await cancelOrder(row.orderNo)
    ElMessage.success('取消成功')
    loadOrderList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 发货
const handleShip = (row: OrderListVO) => {
  currentOrder.value = { orderNo: row.orderNo } as any
  shipForm.logisticsCompany = ''
  shipForm.logisticsNo = ''
  shipDialogVisible.value = true
}

// 提交发货
const handleShipSubmit = async () => {
  if (!shipFormRef.value || !currentOrder.value) return

  await shipFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await shipOrder(
          currentOrder.value!.orderNo,
          shipForm.logisticsCompany,
          shipForm.logisticsNo
        )
        ElMessage.success('发货成功')
        shipDialogVisible.value = false
        loadOrderList()
      } catch (error: any) {
        ElMessage.error(error.message || '发货失败')
      }
    }
  })
}

// 备注
const handleRemark = async (row: OrderListVO) => {
  try {
    const order = await getOrderDetail(row.orderNo)
    currentOrder.value = order
    remarkForm.remark = order.orderNotes || ''
    remarkDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 提交备注
const handleRemarkSubmit = async () => {
  if (!currentOrder.value) return

  try {
    await addOrderRemark(currentOrder.value.orderNo, remarkForm.remark)
    ElMessage.success('备注添加成功')
    remarkDialogVisible.value = false
    loadOrderList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 分页
const handleSizeChange = () => {
  loadOrderList()
}

const handlePageChange = () => {
  loadOrderList()
}

// 获取图片URL（处理相对路径）
const getImageUrl = (url: string | undefined): string => {
  if (!url) return ''
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 如果是相对路径，添加基础URL
  if (url.startsWith('/')) {
    const baseURL = import.meta.env.VITE_API_BASE_URL || ''
    return baseURL + url
  }
  return url
}

// 获取状态标签类型
const getStatusTagType = (status: number): string => {
  const typeMap: Record<number, string> = {
    0: 'danger', // 待付款
    1: 'warning', // 已付款未发货
    2: 'primary', // 已发货
    3: 'success', // 已完成
    4: 'info', // 已取消
    5: 'info', // 已退款
    6: 'info' // 已退货
  }
  return typeMap[status] || 'info'
}

// 将规格组合JSON转换为可读文本
const formatSpecText = (specCombination: string | undefined): string => {
  if (!specCombination) return ''
  try {
    const specs = JSON.parse(specCombination)
    return Object.entries(specs)
      .map(([key, value]) => `${key}:${value}`)
      .join(' / ')
  } catch (e) {
    return ''
  }
}

// 监听搜索表单中的订单状态变化，同步到tab
watch(() => searchForm.orderStatus, (newStatus) => {
  activeTab.value = newStatus
})

// 初始化
onMounted(() => {
  loadOrderList()
})
</script>

<style scoped lang="scss">
.order-management {
  .search-form {
    margin-bottom: 20px;
  }

  // 订单状态标签页样式
  .order-tabs {
    display: flex;
    gap: 0;
    border-bottom: 2px solid #e5e5e5;
    margin-bottom: 20px;

    .tab-item {
      padding: 12px 20px;
      font-size: 14px;
      color: #666;
      cursor: pointer;
      border-bottom: 2px solid transparent;
      margin-bottom: -2px;
      transition: all 0.3s;

      &:hover {
        color: #409eff;
      }

      &.active {
        color: #409eff;
        font-weight: bold;
        border-bottom-color: #409eff;
      }
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

// 图片占位符样式
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}

// SKU规格文本样式
.sku-spec-text {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
</style>

