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
                      <td>{{ order.orderDate }}</td>
                      <td class="order-amount">¥{{ order.totalAmount }}</td>
                      <td>
                        <div class="order-status">
                          <span :class="['status-text', getStatusClass(order.status)]">
                            {{ getStatusText(order) }}
                          </span>
                          <div v-if="order.status === 'shipped' && order.logistics" class="logistics-info">
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

const router = useRouter()
const unreadMessageCount = ref(0)

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

// 所有订单数据（示例数据，后续对接后端API）
const allOrders = ref([
  {
    id: 1,
    orderNo: '20251208115856',
    recipientName: '刘朋辉',
    recipientAddress: '陕西省西安市雁塔区科技路徐家庄西南口148号',
    description: '【避孕润滑】玻尿酸润滑液200g ANGUS/爱神(规格)',
    orderDate: '2025-12-08 11:48',
    totalAmount: '6,500',
    status: 'pending_payment'
  },
  {
    id: 2,
    orderNo: '20251208118741',
    recipientName: '刘朋辉',
    recipientAddress: '陕西省西安市雁塔区科技路徐家庄西南口148号',
    description: '【避孕润滑】玻尿酸润滑液200g ANGUS/爱神(规格)',
    orderDate: '2025-12-08 11:28',
    totalAmount: '6,500',
    status: 'pending_payment'
  },
  {
    id: 3,
    orderNo: '20251208102131',
    recipientName: '刘朋辉',
    recipientAddress: '陕西省西安市雁塔区科技路徐家庄西南口148号',
    description: '美团流量款【男用器具】夹吸健慰依依杯 虞姬等2件商品...',
    orderDate: '2025-12-08 10:52',
    totalAmount: '33,000',
    status: 'pending_payment'
  },
  {
    id: 4,
    orderNo: '20250903163733',
    recipientName: '叶丽丽',
    recipientAddress: '浙江省杭州市西湖区文三路259号',
    description: '【避孕润滑】润滑剂8ml ANGUS/爱神等176件商品...',
    orderDate: '2025-09-03 16:20',
    totalAmount: '2,235.78',
    status: 'shipped',
    logistics: {
      shipDate: '2025-09-04',
      shipTime: '10:30',
      carrier: '安能物流',
      trackingNo: '300615250813'
    }
  },
  {
    id: 5,
    orderNo: '20250902172914',
    recipientName: '田烁',
    recipientAddress: '山东省济南市历下区解放路88号',
    description: '【避孕润滑】润滑剂8ml ANGUS/爱神等177件商品...',
    orderDate: '2025-09-02 17:42',
    totalAmount: '2,241.08',
    status: 'shipped',
    logistics: {
      shipDate: '2025-09-03',
      shipTime: '10:52',
      carrier: '安能物流',
      trackingNo: '300615250800'
    }
  },
  {
    id: 6,
    orderNo: '20250902161172',
    recipientName: '杨晓男',
    recipientAddress: '辽宁省沈阳市和平区中山路100号',
    description: '【避孕润滑】润滑剂8ml ANGUS/爱神等178件商品...',
    orderDate: '2025-09-02 16:46',
    totalAmount: '2,291.90',
    status: 'shipped',
    logistics: {
      shipDate: '2025-09-02',
      shipTime: '18:02',
      carrier: '安能物流',
      trackingNo: '300615250797'
    }
  },
  {
    id: 7,
    orderNo: '20250902130590',
    recipientName: '马磊',
    recipientAddress: '黑龙江省哈尔滨市南岗区西大直街200号',
    description: '【避孕润滑】润滑剂8ml ANGUS/爱神等177件商品...',
    orderDate: '2025-09-02 13:48',
    totalAmount: '2,240.28',
    status: 'shipped',
    logistics: {
      shipDate: '2025-09-02',
      shipTime: '17:10',
      carrier: '安能物流',
      trackingNo: '300615250796'
    }
  },
  {
    id: 8,
    orderNo: '20250815123456',
    recipientName: '张三',
    recipientAddress: '广东省广州市天河区天河路123号',
    description: '【男用器具】飞机杯等5件商品...',
    orderDate: '2025-08-15 14:30',
    totalAmount: '1,500.00',
    status: 'paid_not_shipped'
  },
  {
    id: 9,
    orderNo: '20250810111213',
    recipientName: '李四',
    recipientAddress: '江苏省南京市鼓楼区中山路50号',
    description: '【情趣内衣】蕾丝套装等3件商品...',
    orderDate: '2025-08-10 10:20',
    totalAmount: '800.00',
    status: 'completed'
  },
  {
    id: 10,
    orderNo: '20190609205425',
    recipientName: '黄连丰',
    recipientAddress: '辽宁省大连市沙河口区星海广场1号',
    description: '第六感颗粒3只装等15件商品...',
    orderDate: '2019-06-09 20:07',
    totalAmount: '681.000',
    status: 'refunded',
    refundStatus: 'cancelled' // 已退款[已作废]
  },
  {
    id: 11,
    orderNo: '20250805123456',
    recipientName: '王五',
    recipientAddress: '北京市朝阳区建国路88号',
    description: '【保健食品】等10件商品...',
    orderDate: '2025-08-05 15:30',
    totalAmount: '2,000.00',
    status: 'returned'
  },
  {
    id: 12,
    orderNo: '20250801111213',
    recipientName: '赵六',
    recipientAddress: '上海市黄浦区南京东路100号',
    description: '【安全套】等20件商品...',
    orderDate: '2025-08-01 09:15',
    totalAmount: '500.00',
    status: 'cancelled'
  }
])

// 当前显示的订单列表（根据状态筛选）
const orderList = ref<any[]>([])

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
const getStatusText = (order: any) => {
  const statusMap: Record<string, string> = {
    pending_payment: '等待付款',
    paid_not_shipped: '已付款未发货',
    shipped: '已发货',
    completed: '已完成',
    refunded: '已退款',
    returned: '已退货',
    cancelled: '已作废'
  }
  
  let statusText = statusMap[order.status] || order.status
  
  // 已退款订单可能显示组合状态，如"已退款[已作废]"
  if (order.status === 'refunded' && order.refundStatus) {
    const refundStatusMap: Record<string, string> = {
      cancelled: '已作废'
    }
    const refundStatusText = refundStatusMap[order.refundStatus]
    if (refundStatusText) {
      statusText = `${statusText}[${refundStatusText}]`
    }
  }
  
  // 已发货订单显示"已付款[已发货]"
  if (order.status === 'shipped') {
    statusText = `已付款[${statusText}]`
  }
  
  return statusText
}

// 获取状态样式类
const getStatusClass = (status: string) => {
  const classMap: Record<string, string> = {
    pending_payment: 'status-pending',
    paid_not_shipped: 'status-paid',
    shipped: 'status-shipped',
    completed: 'status-completed',
    refunded: 'status-refunded',
    returned: 'status-returned',
    cancelled: 'status-cancelled'
  }
  return classMap[status] || ''
}

// 切换高级搜索
const toggleAdvancedSearch = () => {
  showAdvancedSearch.value = !showAdvancedSearch.value
}

// 搜索订单
const handleSearch = () => {
  // TODO: 调用后端API搜索订单
  console.log('搜索订单:', searchForm)
  
  // 前端筛选逻辑（临时实现，后续对接后端API）
  let filtered = [...allOrders.value]
  
  // 根据订单号筛选
  if (searchForm.orderNo) {
    filtered = filtered.filter(order => 
      order.orderNo.includes(searchForm.orderNo)
    )
  }
  
  // 根据状态筛选
  if (searchForm.status) {
    filtered = filtered.filter(order => order.status === searchForm.status)
  }
  
  // 根据收货人姓名筛选
  if (searchForm.recipientName) {
    filtered = filtered.filter(order => 
      order.recipientName?.includes(searchForm.recipientName) ||
      order.recipientAddress?.includes(searchForm.recipientName)
    )
  }
  
  // 根据日期范围筛选
  if (searchForm.startDate) {
    filtered = filtered.filter(order => order.orderDate >= searchForm.startDate)
  }
  if (searchForm.endDate) {
    filtered = filtered.filter(order => order.orderDate <= searchForm.endDate + ' 23:59:59')
  }
  
  // 根据联系电话筛选
  if (searchForm.contactPhone) {
    // TODO: 需要订单数据中包含联系电话字段
  }
  
  // 根据联系手机筛选
  if (searchForm.contactMobile) {
    // TODO: 需要订单数据中包含联系手机字段
  }
  
  // 根据收货人地址筛选
  if (searchForm.recipientAddress) {
    // TODO: 需要订单数据中包含收货人地址字段
  }
  
  orderList.value = filtered
  pagination.total = filtered.length
  pagination.currentPage = 1
  
  // 重置选中状态
  selectedOrders.value = []
  selectAll.value = false
  
  // 自动展开所有已发货订单的物流信息
  autoExpandLogistics()
  
  ElMessage.success(`找到 ${filtered.length} 条订单`)
}

// 筛选订单列表
const filterOrders = () => {
  if (activeTab.value === '') {
    // 全部订单
    orderList.value = [...allOrders.value]
  } else {
    // 根据状态筛选
    orderList.value = allOrders.value.filter(order => order.status === activeTab.value)
  }
  // 更新分页总数
  pagination.total = orderList.value.length
  // 重置选中状态
  selectedOrders.value = []
  selectAll.value = false
  // 自动展开所有已发货订单的物流信息
  autoExpandLogistics()
}

// 自动展开所有已发货订单的物流信息
const autoExpandLogistics = () => {
  const shippedOrders = orderList.value.filter(
    order => order.status === 'shipped' && order.logistics
  )
  shippedOrders.forEach(order => {
    expandedLogistics.value.add(order.id)
  })
  // 创建新的Set以触发响应式更新
  expandedLogistics.value = new Set(expandedLogistics.value)
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

// 初始化
onMounted(() => {
  // 初始化时加载全部订单
  filterOrders()
  // filterOrders 中已经调用了 autoExpandLogistics，这里不需要重复调用
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

