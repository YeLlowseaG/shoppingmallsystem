<template>
  <div class="member-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 会员中心内容区域 -->
    <div class="member-content">
      <div class="container">
        <!-- 会员中心标题栏 -->
        <MemberHeaderBar />

        <!-- 会员中心主体 -->
        <div class="member-main">
          <!-- 左侧导航菜单 -->
          <MemberSidebar active-menu="transaction/orders" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <div class="orders-wrapper">
              <!-- 页面标题 -->
              <div class="page-title">我的订单</div>

              <!-- 搜索筛选区域 -->
              <div class="search-section">
                <div class="search-form">
                  <!-- 基础搜索行 -->
                  <div class="search-row">
                    <label class="search-label">订单号:</label>
                    <el-input
                      v-model="searchForm.orderNo"
                      placeholder="请输入订单号"
                      class="search-input"
                      clearable
                    />
                    <label class="search-label">收货人姓名:</label>
                    <el-input
                      v-model="searchForm.recipientName"
                      placeholder="请输入收货人姓名"
                      class="search-input"
                      clearable
                    />
                    <label class="search-label">交易状态:</label>
                    <el-select
                      v-model="searchForm.status"
                      placeholder="全部订单"
                      class="search-select"
                      style="width: 150px;"
                    >
                      <el-option label="全部订单" value="" />
                      <el-option label="等待付款" value="pending_payment" />
                      <el-option label="已付款未发货" value="paid_not_shipped" />
                      <el-option label="已发货" value="shipped" />
                      <el-option label="已完成" value="completed" />
                      <el-option label="已退款" value="refunded" />
                      <el-option label="已作废" value="cancelled" />
                    </el-select>
                  </div>
                  
                  <!-- 操作按钮行 -->
                  <div class="search-row">
                    <a href="#" class="advanced-search" @click.prevent="toggleAdvancedSearch">
                      {{ showAdvancedSearch ? '收起' : '高级搜索' }}
                    </a>
                    <el-button type="primary" @click="handleSearch" class="search-btn">查询订单</el-button>
                  </div>
                  
                  <!-- 高级搜索区域 -->
                  <div v-show="showAdvancedSearch" class="advanced-search-row">
                    <div class="search-row">
                      <label class="search-label">起始时间:</label>
                      <el-date-picker
                        v-model="searchForm.startDate"
                        type="date"
                        placeholder="选择开始日期"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        class="search-date-picker"
                        style="width: 150px;"
                      />
                      <span class="date-separator">至</span>
                      <el-date-picker
                        v-model="searchForm.endDate"
                        type="date"
                        placeholder="选择结束日期"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        class="search-date-picker"
                        style="width: 150px;"
                      />
                    </div>
                    <div class="search-row">
                      <label class="search-label">联系电话:</label>
                      <el-input
                        v-model="searchForm.contactPhone"
                        placeholder="请输入联系电话"
                        class="search-input"
                        clearable
                      />
                      <label class="search-label">联系手机:</label>
                      <el-input
                        v-model="searchForm.contactMobile"
                        placeholder="请输入联系手机"
                        class="search-input"
                        clearable
                      />
                    </div>
                    <div class="search-row">
                      <label class="search-label">收货人地址:</label>
                      <el-input
                        v-model="searchForm.recipientAddress"
                        placeholder="请输入收货人地址"
                        class="search-input-address"
                        clearable
                      />
                    </div>
                  </div>
                </div>
              </div>

              <!-- 订单管理操作 -->
              <div class="order-actions">
                <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
                <el-button @click="handleMergePayment" :disabled="selectedOrders.length === 0">合并付款</el-button>
                <el-button @click="handleExportOrders" :disabled="selectedOrders.length === 0">导出订单</el-button>
              </div>

              <!-- 订单状态标签页 -->
              <div class="order-tabs">
                <div
                  v-for="tab in orderTabs"
                  :key="tab.value"
                  :class="['tab-item', { active: activeTab === tab.value }]"
                  @click="handleTabChange(tab.value)"
                >
                  {{ tab.label }}
                </div>
              </div>

              <!-- 订单列表表格 -->
              <div class="order-table-wrapper">
                <table class="order-table">
                  <thead>
                    <tr>
                      <th width="50">
                        <el-checkbox v-model="selectAll" @change="handleSelectAll" />
                      </th>
                      <th width="150">订单号</th>
                      <th width="120">收货人信息</th>
                      <th>订单描述</th>
                      <th width="130">下单日期</th>
                      <th width="90">总金额</th>
                      <th width="200">状态</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="order in displayedOrders" :key="order.id">
                      <td>
                        <el-checkbox
                          :model-value="selectedOrders.includes(order.id)"
                          @change="(val: boolean) => handleSelectOrder(order.id, val)"
                        />
                      </td>
                      <td>
                        <a href="#" class="order-link" @click.prevent="handleViewOrder(order.orderNo)">
                          {{ order.orderNo }}
                        </a>
                      </td>
                      <td class="recipient-info">
                        <el-tooltip
                          :content="getRecipientFullInfo(order)"
                          placement="top"
                          :disabled="!shouldShowTooltip(order)"
                        >
                          <div class="recipient-content">
                            {{ getRecipientDisplayText(order) }}
                          </div>
                        </el-tooltip>
                      </td>
                      <td class="order-desc">
                        <a href="#" class="order-link" @click.prevent="handleViewOrder(order.orderNo)">
                          {{ order.description }}
                        </a>
                      </td>
                      <td>{{ formatDateTime(order.orderDate) }}</td>
                      <td class="order-amount">¥{{ formatAmount(order.totalAmount) }}</td>
                      <td>
                        <div class="order-status">
                          <span :class="['status-text', getStatusClass(order.status)]">
                            {{ order.statusText || getStatusText(order) }}
                          </span>
                          <div v-if="order.status === 2 && order.logistics" class="logistics-info">
                            <a href="#" class="logistics-link" @click.prevent="handleToggleLogistics(order.id)">
                              {{ expandedLogistics.has(order.id) ? '收起' : '物流信息' }}
                            </a>
                            <div v-show="expandedLogistics.has(order.id)" class="logistics-details">
                              <div>
                                1){{ order.logistics.shipDate }} {{ order.logistics.shipTime }}已通过{{ order.logistics.carrier }}发货
                              </div>
                              <div class="tracking-no">
                                发货单号:{{ order.logistics.trackingNo }}
                                <el-button
                                  type="text"
                                  size="small"
                                  class="copy-btn"
                                  @click.stop="handleCopyTrackingNo(order.logistics.trackingNo)"
                                >
                                  复制
                                </el-button>
                                <a href="#" class="view-link" @click.prevent.stop="handleViewOrder(order.orderNo)">
                                  查看
                                </a>
                              </div>
                            </div>
                          </div>
                        </div>
                      </td>
                    </tr>
                    <tr v-if="displayedOrders.length === 0">
                      <td colspan="7" class="empty-data">暂无订单数据</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- 底部操作栏 -->
              <div class="order-actions bottom-actions">
                <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
                <el-button @click="handleMergePayment" :disabled="selectedOrders.length === 0">合并付款</el-button>
                <el-button @click="handleExportOrders" :disabled="selectedOrders.length === 0">导出订单</el-button>
              </div>

              <!-- 分页 -->
              <div class="pagination-wrapper">
                <el-pagination
                  v-model:current-page="pagination.currentPage"
                  v-model:page-size="pagination.pageSize"
                  :total="pagination.total"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="prev, pager, next, jumper, total"
                  @current-change="handlePageChange"
                  @size-change="handleSizeChange"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElTooltip } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { getOrderList, cancelOrder, confirmReceipt } from '@/api/buyer/order'
import type { OrderListVO, OrderPageResponse } from '@/api/buyer/order'
import { formatDateTime } from '@/utils'

const router = useRouter()
const unreadMessageCount = ref(0)
const loading = ref(false)

// 高级搜索展开状态
const showAdvancedSearch = ref(false)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  recipientName: '',
  status: '',
  startDate: '',
  endDate: '',
  contactPhone: '',
  contactMobile: '',
  recipientAddress: ''
})

// 订单状态标签页
const orderTabs = [
  { label: '全部订单', value: '' },
  { label: '等待付款', value: 'pending_payment' },
  { label: '已付款未发货', value: 'paid_not_shipped' },
  { label: '已发货', value: 'shipped' },
  { label: '已完成', value: 'completed' },
  { label: '已退款', value: 'refunded' },
  { label: '已退货', value: 'returned' },
  { label: '已作废', value: 'cancelled' }
]

const activeTab = ref('')

// 订单列表数据
const orderList = ref<OrderListVO[]>([])

// 选中订单
const selectAll = ref(false)
const selectedOrders = ref<number[]>([])

// 展开的物流信息（使用Set存储已展开的订单ID）
const expandedLogistics = ref<Set<number>>(new Set())

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 获取状态文本
const getStatusText = (order: OrderListVO) => {
  const statusStr = convertStatusNumberToString(order.status)
  const statusMap: Record<string, string> = {
    pending_payment: '等待付款',
    paid_not_shipped: '已付款未发货',
    shipped: '已发货',
    completed: '已完成',
    refunded: '已退款',
    returned: '已退货',
    cancelled: '已作废'
  }
  
  let statusText = statusMap[statusStr] || '未知'
  
  // 已发货订单显示"已付款[已发货]"
  if (order.status === 2) {
    statusText = `已付款[${statusText}]`
  }
  
  return statusText
}

// 获取状态样式类
const getStatusClass = (status: number) => {
  const statusStr = convertStatusNumberToString(status)
  const classMap: Record<string, string> = {
    pending_payment: 'status-pending',
    paid_not_shipped: 'status-paid',
    shipped: 'status-shipped',
    completed: 'status-completed',
    refunded: 'status-refunded',
    returned: 'status-returned',
    cancelled: 'status-cancelled'
  }
  return classMap[statusStr] || ''
}

// 切换高级搜索
const toggleAdvancedSearch = () => {
  showAdvancedSearch.value = !showAdvancedSearch.value
}

// 搜索订单
const handleSearch = async () => {
  pagination.currentPage = 1
  await loadOrderList()
}

// 加载订单列表
const loadOrderList = async () => {
  loading.value = true
  try {
    // 将前端状态字符串转换为后端需要的格式
    let statusStr = searchForm.status || activeTab.value
    if (statusStr === '') {
      statusStr = undefined
    }
    
    const params: any = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      orderNo: searchForm.orderNo || undefined,
      recipientName: searchForm.recipientName || undefined,
      status: statusStr,
      startDate: searchForm.startDate || undefined,
      endDate: searchForm.endDate || undefined,
      contactPhone: searchForm.contactPhone || undefined,
      contactMobile: searchForm.contactMobile || undefined,
      recipientAddress: searchForm.recipientAddress || undefined
    }
    
    const response: OrderPageResponse = await getOrderList(params)
    orderList.value = response.records || []
    pagination.total = response.total || 0
    
    // 重置选中状态
    selectedOrders.value = []
    selectAll.value = false
    
    // 自动展开所有已发货订单的物流信息
    autoExpandLogistics()
  } catch (error: any) {
    ElMessage.error(error.message || '加载订单失败')
    orderList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 筛选订单列表（标签页切换）
const filterOrders = () => {
  searchForm.status = activeTab.value
  pagination.currentPage = 1
  loadOrderList()
}

// 自动展开所有已发货订单的物流信息
const autoExpandLogistics = () => {
  const shippedOrders = orderList.value.filter(
    order => {
      // 后端返回的status是数字，需要转换
      const statusNum = convertStatusStringToNumber('shipped')
      return order.status === statusNum && order.logistics
    }
  )
  shippedOrders.forEach(order => {
    expandedLogistics.value.add(order.id)
  })
  // 创建新的Set以触发响应式更新
  expandedLogistics.value = new Set(expandedLogistics.value)
}

// 将状态字符串转换为数字
const convertStatusStringToNumber = (statusStr: string): number => {
  const statusMap: Record<string, number> = {
    'pending_payment': 0,
    'paid_not_shipped': 1,
    'shipped': 2,
    'completed': 3,
    'cancelled': 4,
    'refunded': 5,
    'returned': 6
  }
  return statusMap[statusStr] ?? -1
}

// 将状态数字转换为字符串
const convertStatusNumberToString = (statusNum: number): string => {
  const statusMap: Record<number, string> = {
    0: 'pending_payment',
    1: 'paid_not_shipped',
    2: 'shipped',
    3: 'completed',
    4: 'cancelled',
    5: 'refunded',
    6: 'returned'
  }
  return statusMap[statusNum] || ''
}

// 标签页切换
const handleTabChange = (value: string) => {
  activeTab.value = value
  searchForm.status = value
  filterOrders()
}

// 全选/取消全选
const handleSelectAll = (val: boolean) => {
  if (val) {
    selectedOrders.value = displayedOrders.value.map(order => order.id)
  } else {
    selectedOrders.value = []
  }
}

// 选择单个订单
const handleSelectOrder = (orderId: number, selected: boolean) => {
  if (selected) {
    if (!selectedOrders.value.includes(orderId)) {
      selectedOrders.value.push(orderId)
    }
  } else {
    selectedOrders.value = selectedOrders.value.filter(id => id !== orderId)
  }
  // 更新全选状态
  selectAll.value = selectedOrders.value.length === displayedOrders.value.length && displayedOrders.value.length > 0
}

// 合并付款
const handleMergePayment = () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请选择要合并付款的订单')
    return
  }
  // TODO: 实现合并付款逻辑
  ElMessage.info('合并付款功能待实现')
}

// 导出订单
const handleExportOrders = () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请选择要导出的订单')
    return
  }
  // TODO: 实现导出订单逻辑
  ElMessage.info('导出订单功能待实现')
}

// 查看订单详情
const handleViewOrder = (orderNo: string) => {
  router.push({
    path: '/order/detail',
    query: {
      orderNumber: orderNo
    }
  })
}

// 获取收货人显示文本（最多10个字符）
const getRecipientDisplayText = (order: any) => {
  const name = order.recipientName || ''
  const address = order.recipientAddress || '-'
  const fullText = `${name} ${address}`
  // 最多显示10个字符，超过则用省略号
  if (fullText.length > 14) {
    return fullText.substring(0, 14) + '...'
  }
  return fullText
}

// 获取收货人完整信息（用于tooltip显示）
const getRecipientFullInfo = (order: any) => {
  const name = order.recipientName || ''
  const address = order.recipientAddress || '-'
  return `${name} ${address}`
}

// 判断是否需要显示tooltip（如果内容超过10个字符则显示）
const shouldShowTooltip = (order: any) => {
  const name = order.recipientName || ''
  const address = order.recipientAddress || ''
  const fullText = `${name} ${address}`
  return fullText.length > 10
}

// 切换物流信息展开/收起
const handleToggleLogistics = (orderId: number) => {
  if (expandedLogistics.value.has(orderId)) {
    expandedLogistics.value.delete(orderId)
  } else {
    expandedLogistics.value.add(orderId)
  }
  // 创建新的Set以触发响应式更新
  expandedLogistics.value = new Set(expandedLogistics.value)
}

// 复制物流单号
const handleCopyTrackingNo = (trackingNo: string) => {
  navigator.clipboard.writeText(trackingNo).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 查看物流跟踪（保留用于后续功能扩展）
// const handleViewTracking = (trackingNo: string) => {
//   // TODO: 打开物流跟踪页面
//   console.log('查看物流跟踪:', trackingNo)
// }

// 分页变化
const handlePageChange = (page: number) => {
  pagination.currentPage = page
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 计算当前页显示的订单列表
const displayedOrders = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return orderList.value.slice(start, end)
})

// 格式化日期时间
const formatDateTime = (dateTime: string | Date) => {
  if (!dateTime) return '-'
  const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 格式化金额
const formatAmount = (amount: number | string) => {
  if (typeof amount === 'number') {
    return amount.toFixed(2)
  }
  return amount || '0.00'
}

// 初始化
onMounted(() => {
  // 初始化时加载全部订单
  loadOrderList()
})
</script>

<style scoped lang="scss">
.member-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.member-content {
  background: #fff;
  padding: 20px 0 40px;
  min-height: 600px;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }

  // 会员中心主体
  .member-main {
    display: flex;
    gap: 20px;
    align-items: flex-start;

    // 右侧主内容区
    .member-main-content {
      flex: 1;
      background: #fff;
      min-height: 500px;
      padding: 20px;

      .orders-wrapper {
        .page-title {
          font-size: 18px;
          font-weight: bold;
          color: #333;
          margin-bottom: 20px;
          padding-bottom: 10px;
          border-bottom: 2px solid #e4393c;
        }

        // 搜索筛选区域
        .search-section {
          margin-bottom: 20px;

          .search-form {
            background: #f9f9f9;
            padding: 15px;
            border: 1px solid #e5e5e5;

            .search-row {
              display: flex;
              align-items: center;
              gap: 10px;
              flex-wrap: wrap;
              margin-bottom: 10px;

              .search-label {
                font-size: 14px;
                color: #333;
                white-space: nowrap;
              }

              .search-select-type {
                width: 100px;
              }

              .search-input {
                width: 180px;
              }

              .search-input-address {
                width: 500px;
              }

              .search-select {
                width: 150px;
              }

              .search-date-picker {
                width: 150px;
              }

              .date-separator {
                font-size: 14px;
                color: #666;
                margin: 0 5px;
              }

              .advanced-search {
                color: #666;
                font-size: 14px;
                text-decoration: none;
                margin-left: 10px;
                cursor: pointer;

                &:hover {
                  color: #e4393c;
                }
              }

              .search-btn {
                background: #e4393c;
                border-color: #e4393c;
                margin-left: 10px;

                &:hover {
                  background: #c9302c;
                  border-color: #c9302c;
                }
              }
            }

            // 高级搜索区域
            .advanced-search-row {
              margin-top: 15px;
              padding-top: 15px;
              border-top: 1px solid #e5e5e5;
            }
          }
        }

        // 订单管理操作
        .order-actions {
          display: flex;
          align-items: center;
          gap: 15px;
          padding: 15px 0;
          border-bottom: 1px solid #e5e5e5;

          &.bottom-actions {
            border-top: 1px solid #e5e5e5;
            border-bottom: none;
            margin-top: 20px;
          }

          :deep(.el-checkbox) {
            .el-checkbox__label {
              font-size: 14px;
              color: #333;
            }
          }

          :deep(.el-button) {
            padding: 8px 20px;
            font-size: 14px;
          }
        }

        // 订单状态标签页
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
              color: #e4393c;
            }

            &.active {
              color: #e4393c;
              font-weight: bold;
              border-bottom-color: #e4393c;
            }
          }
        }

        // 订单列表表格
        .order-table-wrapper {
          overflow-x: auto;

          .order-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;

            thead {
              background: #f5f5f5;

              th {
                padding: 12px 8px;
                text-align: left;
                font-weight: normal;
                color: #333;
                border: 1px solid #e5e5e5;
              }
            }

            tbody {
              tr {
                border-bottom: 1px solid #e5e5e5;

                &:hover {
                  background: #f9f9f9;
                }

                td {
                  padding: 15px 8px;
                  vertical-align: top;
                  border: 1px solid #e5e5e5;

                  .order-link {
                    color: #0066cc;
                    text-decoration: none;

                    &:hover {
                      text-decoration: underline;
                    }
                  }

                  .recipient-info {
                    .recipient-content {
                      max-width: 120px;
                      overflow: hidden;
                      text-overflow: ellipsis;
                      white-space: nowrap;
                      font-size: 14px;
                      color: #333;
                      line-height: 1.5;
                      cursor: default;
                    }
                  }

                  .order-desc {
                    max-width: 300px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                  }

                  .order-amount {
                    color: #e4393c;
                    font-weight: bold;
                  }

                  .order-status {
                    .status-text {
                      display: block;
                      margin-bottom: 5px;

                      &.status-pending {
                        color: #e4393c;
                      }

                      &.status-paid {
                        color: #ff9900;
                      }

                      &.status-shipped {
                        color: #0066cc;
                      }

                      &.status-completed {
                        color: #52c41a;
                      }

                      &.status-refunded {
                        color: #999;
                      }

                      &.status-returned {
                        color: #999;
                      }

                      &.status-cancelled {
                        color: #999;
                      }
                    }

                    .logistics-info {
                      margin-top: 8px;
                      font-size: 12px;
                      color: #666;

                      .logistics-link {
                        color: #0066cc;
                        text-decoration: none;
                        margin-bottom: 5px;
                        display: inline-block;

                        &:hover {
                          text-decoration: underline;
                        }
                      }

                      .logistics-details {
                        margin-top: 5px;
                        padding-left: 0;

                        div {
                          margin-bottom: 5px;
                          line-height: 1.5;
                        }

                        .tracking-no {
                          display: inline;
                          margin-left: 0;

                          .copy-btn {
                            padding: 0;
                            font-size: 12px;
                            color: #0066cc;
                            height: auto;
                            min-height: auto;
                            margin-left: 5px;

                            &:hover {
                              color: #e4393c;
                            }
                          }

                          .view-link {
                            color: #0066cc;
                            text-decoration: none;
                            font-size: 12px;
                            margin-left: 10px;

                            &:hover {
                              text-decoration: underline;
                            }
                          }
                        }
                      }
                    }
                  }

                  &.empty-data {
                    text-align: center;
                    color: #999;
                    padding: 40px;
                  }
                }
              }
            }
          }
        }

        // 分页
        .pagination-wrapper {
          margin-top: 20px;
          display: flex;
          justify-content: center;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .member-content {
    .member-main {
      flex-direction: column;

      .member-main-content {
        .orders-wrapper {
          .search-section {
            .search-form {
              .search-row {
                flex-direction: column;
                align-items: flex-start;

                .search-input,
                .search-select {
                  width: 100%;
                }
              }
            }
          }

          .order-tabs {
            overflow-x: auto;
            white-space: nowrap;
          }

          .order-table-wrapper {
            .order-table {
              font-size: 12px;

              thead th,
              tbody td {
                padding: 8px 4px;
              }
            }
          }
        }
      }
    }
  }
}
</style>

