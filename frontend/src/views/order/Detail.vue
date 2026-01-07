<template>
  <div class="order-detail-page">
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
            <div class="order-detail-wrapper">
              <!-- 订单详情标题和订单历史 -->
              <div class="order-detail-header">
                <div class="order-detail-title">
                  <span>订单详情</span>
                  <a href="#" class="toggle-history" @click.prevent="toggleOrderHistory">
                    {{ showOrderHistory ? '隐藏' : '展开' }}
                  </a>
                </div>
                
                <!-- 订单历史记录 -->
                <div v-show="showOrderHistory" class="order-history">
                  <div
                    v-for="(history, index) in orderHistory"
                    :key="index"
                    class="history-item"
                  >
                    {{ index + 1 }}. {{ history.date }} {{ history.action }}
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div class="order-actions">
                  <el-button type="warning" class="action-btn" @click="handleMarkAsPaid">我已付款</el-button>
                  <el-button type="warning" class="action-btn" @click="handleHaveQuestion">我有问题</el-button>
                </div>
              </div>

              <!-- 订单信息横幅 -->
              <div class="order-header-banner">
                <div class="order-info-row">
                  <div class="order-info-item">
                    <span class="order-label">订单编号:</span>
                    <span class="order-value">{{ orderNumber }}</span>
                  </div>
                </div>
                <div class="order-info-row">
                  <div class="order-info-item">
                    <span class="order-label">下单日期:</span>
                    <span class="order-value">{{ orderDate }}</span>
                  </div>
                  <div class="order-info-item">
                    <span class="order-label">状态:</span>
                    <span :class="['order-status-text', getStatusClass(orderStatusValue)]">
                      {{ getStatusDisplayText(orderStatusValue) }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- 未付款状态提示 -->
              <div v-if="showPaymentNotice" class="payment-notice">
                <el-button type="warning" size="large" class="pay-notice-btn" @click="handlePayNow">
                  您尚未完成订单 按此为订单付款>>
                </el-button>
              </div>

              <!-- 购买的商品 -->
              <div class="order-section">
                <div class="section-title">购买的商品</div>
                <div class="products-table-wrapper">
                  <table class="products-table">
                    <thead>
                      <tr>
                        <th class="col-image">图片</th>
                        <th class="col-code">货号</th>
                        <th class="col-name">商品名称</th>
                        <th class="col-price">价格</th>
                        <th class="col-quantity">数量</th>
                        <th class="col-subtotal">小计</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="item in orderItems" :key="item.id" class="product-row">
                        <td class="col-image">
                          <img :src="item.image" :alt="item.name" class="product-image" />
                        </td>
                        <td class="col-code">{{ item.skuCode || item.productCode }}</td>
                        <td class="col-name">
                          <div class="product-name">
                            <router-link
                              :to="`/products/${item.productId}`"
                              class="product-name-link"
                              title="点击查看商品详情"
                            >
                              {{ item.name }}
                            </router-link>
                          </div>
                          <div v-if="formatSpecText(item.specCombination)" class="sku-spec-text">
                            规格：{{ formatSpecText(item.specCombination) }}
                          </div>
                        </td>
                        <td class="col-price">¥{{ item.price.toFixed(2) }}</td>
                        <td class="col-quantity">{{ item.quantity }}</td>
                        <td class="col-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</td>
                      </tr>
                    </tbody>
                    <tfoot>
                      <tr>
                        <td colspan="2" class="summary-label">商品数量</td>
                        <td colspan="4" class="summary-value">{{ totalQuantity }}</td>
                      </tr>
                      <tr>
                        <td colspan="2" class="summary-label">商品总金额</td>
                        <td colspan="4" class="summary-value">¥{{ totalProductAmount.toFixed(2) }}</td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>

              <!-- 收货人信息 -->
              <div class="order-section">
                <div class="section-title">收货人信息</div>
                <div class="recipient-info">
                  <div class="info-left">
                    <div class="info-item">
                      <span class="info-label">收货人姓名:</span>
                      <span class="info-value">{{ recipientInfo.name }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">配送地区:</span>
                      <span class="info-value">{{ recipientInfo.region }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">收货人邮编:</span>
                      <span class="info-value">{{ recipientInfo.zipCode || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">商品重量:</span>
                      <span class="info-value">{{ formatWeight(recipientInfo.weight) }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">收货人地址:</span>
                      <span class="info-value">{{ recipientInfo.address }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">订单附言:</span>
                      <span class="info-value">{{ orderNotes || '-' }}</span>
                    </div>
                  </div>
                  <div class="info-right">
                    <div class="info-item">
                      <span class="info-label">联系手机:</span>
                      <span class="info-value">{{ recipientInfo.mobile || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">联系电话:</span>
                      <span class="info-value">{{ recipientInfo.phone || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">付款方式:</span>
                      <span class="info-value">{{ recipientInfo.paymentMethod }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">支付币别:</span>
                      <span class="info-value">{{ recipientInfo.paymentCurrency }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 订单汇总 -->
              <div class="order-summary">
                <div class="summary-item">
                  <span class="summary-label">商品总金额</span>
                  <span class="summary-value">¥{{ totalProductAmount.toFixed(2) }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">配送费用:</span>
                  <span class="summary-value">¥{{ shippingFee.toFixed(2) }}</span>
                </div>
                <div class="summary-item total-item">
                  <span class="summary-label">订单总金额:</span>
                  <span class="summary-value">¥{{ totalAmount.toFixed(2) }}</span>
                </div>
              </div>

              <!-- 退款记录 -->
              <div v-if="refundList.length > 0" class="order-section">
                <div class="section-title">退款记录</div>
                <div class="refund-list">
                  <div v-for="refund in refundList" :key="refund.id" class="refund-item">
                    <div class="refund-header">
                      <div class="refund-info-row">
                        <div class="refund-info-item">
                          <span class="refund-label">退款单号:</span>
                          <span class="refund-value">{{ refund.refundNo }}</span>
                        </div>
                        <div class="refund-info-item">
                          <span class="refund-label">退款金额:</span>
                          <span class="refund-amount">¥{{ refund.refundAmount.toFixed(2) }}</span>
                        </div>
                        <div class="refund-info-item">
                          <span class="refund-label">退款类型:</span>
                          <span class="refund-value">{{ refund.refundTypeText }}</span>
                        </div>
                        <div class="refund-info-item">
                          <span class="refund-label">退款状态:</span>
                          <span :class="['refund-status', getRefundStatusClass(refund.refundStatus)]">
                            {{ refund.refundStatusText }}
                          </span>
                        </div>
                      </div>
                      <div class="refund-info-row">
                        <div class="refund-info-item">
                          <span class="refund-label">退款时间:</span>
                          <span class="refund-value">{{ refund.refundTime ? formatDateTime(refund.refundTime) : '-' }}</span>
                        </div>
                        <div class="refund-info-item">
                          <span class="refund-label">退款原因:</span>
                          <span class="refund-value">{{ refund.refundReason || '-' }}</span>
                        </div>
                      </div>
                    </div>
                    <div v-if="refund.refundItems && refund.refundItems.length > 0" class="refund-items">
                      <div class="refund-items-title">退款明细:</div>
                      <table class="refund-items-table">
                        <thead>
                          <tr>
                            <th>商品编码</th>
                            <th>商品名称</th>
                            <th>退款数量</th>
                            <th>退款单价</th>
                            <th>退款小计</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="item in refund.refundItems" :key="item.id">
                            <td>{{ item.productCode }}</td>
                            <td class="product-name-cell">
                              <div>{{ item.productName }}</div>
                              <div v-if="formatSpecText(item.specCombination)" class="sku-spec-text">
                                规格：{{ formatSpecText(item.specCombination) }}
                              </div>
                            </td>
                            <td>{{ item.refundQuantity }}</td>
                            <td>¥{{ item.refundPrice.toFixed(2) }}</td>
                            <td class="refund-subtotal">¥{{ item.refundSubtotal.toFixed(2) }}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 支付操作（仅未付款状态显示） -->
              <div v-if="showPaymentAction" class="payment-action">
                <el-button type="warning" size="large" class="pay-now-btn" @click="handlePayNow">
                  您尚未完成订单 按此为订单付款>>
                </el-button>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { getOrderDetail, cancelOrder, confirmReceipt, getOrderRefundList } from '@/api/buyer/order'
import type { OrderDetailVO, OrderRefundVO } from '@/api/buyer/order'

const router = useRouter()
const route = useRoute()

const unreadMessageCount = ref(0)
const loading = ref(false)

// 订单编号
const orderNumber = ref('')
// 原订单编号
const originalOrderNo = ref('')
// 下单日期
const orderDate = ref('')
// 订单状态值（用于判断）
const orderStatusValue = ref('pending_payment')

// 订单历史记录展开状态
const showOrderHistory = ref(false)

// 订单详情数据
const orderDetail = ref<OrderDetailVO | null>(null)

// 退款记录列表
const refundList = ref<OrderRefundVO[]>([])

// 订单历史记录
const orderHistory = computed(() => {
  return orderDetail.value?.orderHistory || []
})

// 订单商品列表
const orderItems = computed(() => {
  return orderDetail.value?.items || []
})

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

// 收货人信息
const recipientInfo = computed(() => {
  return orderDetail.value?.recipientInfo || {
    name: '',
    region: '',
    zipCode: '',
    shippingMethod: '',
    weight: 0,
    address: '',
    email: '',
    phone: '',
    mobile: '',
    deliveryTime: '',
    paymentMethod: '',
    paymentCurrency: ''
  }
})

// 订单附言
const orderNotes = computed(() => {
  return orderDetail.value?.orderNotes || ''
})

// 计算总数量
const totalQuantity = computed(() => {
  return orderDetail.value?.totalQuantity || 0
})

// 计算商品总金额
const totalProductAmount = computed(() => {
  return orderDetail.value?.totalProductAmount || 0
})

// 配送费用
const shippingFee = computed(() => {
  return orderDetail.value?.shippingFee || 0
})

// 订单总金额
const totalAmount = computed(() => {
  return orderDetail.value?.totalAmount || 0
})

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

// 获取状态显示文本
const getStatusDisplayText = (statusValue: string) => {
  if (orderDetail.value?.statusText) {
    return orderDetail.value.statusText
  }
  const statusMap: Record<string, string> = {
    pending_payment: '未付款[未发货]',
    paid_not_shipped: '已付款[未发货]',
    shipped: '已付款[已发货]',
    completed: '已完成',
    refunded: '已退款(未发货)',
    returned: '已退货',
    cancelled: '已作废'
  }
  return statusMap[statusValue] || statusValue
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
  return statusMap[statusNum] || 'pending_payment'
}

// 是否显示付款提示（仅待付款状态）
const showPaymentNotice = computed(() => {
  return orderStatusValue.value === 'pending_payment'
})

// 是否显示付款操作按钮（仅待付款状态）
const showPaymentAction = computed(() => {
  return orderStatusValue.value === 'pending_payment'
})

// 切换订单历史记录
const toggleOrderHistory = () => {
  showOrderHistory.value = !showOrderHistory.value
}

// 我已付款
const handleMarkAsPaid = () => {
  // 确保订单号存在
  const currentOrderNumber = orderNumber.value || route.query.orderNumber as string
  if (!currentOrderNumber) {
    ElMessage.warning('订单号不存在')
    return
  }
  router.push({
    path: '/order/message',
    query: {
      orderNumber: currentOrderNumber,
      type: 'paid'
    }
  })
}

// 我有问题
const handleHaveQuestion = () => {
  // 确保订单号存在
  const currentOrderNumber = orderNumber.value || route.query.orderNumber as string
  if (!currentOrderNumber) {
    ElMessage.warning('订单号不存在')
    return
  }
  router.push({
    path: '/order/message',
    query: {
      orderNumber: currentOrderNumber,
      type: 'question'
    }
  })
}

// 依照此订单再次下单
const handleReorder = () => {
  ElMessage.info('依照此订单再次下单功能待实现')
  // TODO: 实现再次下单逻辑
}

// 立刻付款
const handlePayNow = () => {
  router.push({
    path: '/order/payment',
    query: {
      orderNumber: orderNumber.value,
      amount: totalAmount.value.toFixed(2)
    }
  })
}

// 加载订单详情（根据订单编号）
const loadOrderDetail = async (orderNo: string) => {
  loading.value = true
  try {
    const data = await getOrderDetail(orderNo)
    orderDetail.value = data
    
    // 设置订单基本信息
    orderNumber.value = data.orderNo
    originalOrderNo.value = data.originalOrderNo || ''
    orderDate.value = formatDateTime(data.orderDate)
    orderStatusValue.value = convertStatusNumberToString(data.status)
    
    // 检查URL参数中是否有支付成功标识
    const paymentStatus = route.query.paymentStatus as string
    if (paymentStatus === 'success') {
      ElMessage.success('支付成功！订单已确认，等待商家发货')
      // 清除URL参数中的paymentStatus，避免刷新时重复提示
      router.replace({
        path: '/order/detail',
        query: {
          orderNumber: orderNo
        }
      })
    }
    
    // 加载退款记录
    await loadRefundList(orderNo)
  } catch (error: any) {
    // 如果全局拦截器已经显示过错误提示，这里就不再显示
    if (!error.__messageShown) {
      ElMessage.error(error.message || '加载订单详情失败')
    }
    router.push('/member/transaction/orders')
  } finally {
    loading.value = false
  }
}

// 加载退款记录列表
const loadRefundList = async (orderNo: string) => {
  try {
    const refunds = await getOrderRefundList(orderNo)
    refundList.value = refunds || []
  } catch (error: any) {
    // 如果获取退款记录失败，不影响订单详情显示，只记录错误
    console.error('加载退款记录失败:', error)
    refundList.value = []
  }
}

// 获取退款状态样式类
const getRefundStatusClass = (status: number) => {
  switch (status) {
    case 3:
      return 'status-refunding' // 退款中
    case 4:
      return 'status-success' // 退款成功
    case 5:
      return 'status-failed' // 退款失败
    default:
      return ''
  }
}

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

// 格式化重量（统一显示为克）
const formatWeight = (weight: number | string | null | undefined) => {
  if (!weight) return '-'
  const weightNum = typeof weight === 'string' ? parseFloat(weight) : weight
  if (isNaN(weightNum) || weightNum <= 0) return '-'
  // 统一显示为克（g）
  return `${weightNum.toFixed(0)} g`
}

// 旧代码（保留作为备用，但不再使用）
const loadOrderDetailOld = (orderNo: string) => {
  // 已废弃，使用loadOrderDetail替代
  if (orderNo === '20190609205425') {
    // 已退款订单
    orderStatusValue.value = 'refunded'
    orderDate.value = '2019-06-10 13:37'
    originalOrderNo.value = ''
    
    // 设置订单历史记录
    orderHistory.value = [
      { date: '2019-06-09 20:07', action: '订单创建' },
      { date: '2019-06-09 20:09', action: `订单${orderNo}付款681.000` },
      { date: '2019-06-10 13:40', action: '订单退款681.000' },
      { date: '2019-06-10 13:40', action: '支付的状态更改为全额退款' },
      { date: '2019-06-10 13:40', action: '订单状态改为作废' }
    ]
    
    // 设置商品列表（示例数据）
    orderItems.value = [
      {
        id: 1,
        productCode: '505098',
        name: '第六感颗粒3只装',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 3.5,
        quantity: 4
      }
      // 可以添加更多商品...
    ]
    
    // 设置收货人信息
    recipientInfo.value = {
      name: '黄连本',
      region: '辽宁省-沈阳市-大东区',
      zipCode: '',
      shippingMethod: '上门自提',
      weight: '18860.000',
      address: '辽宁省沈阳市大东区辽宁,沈阳市,大东区',
      email: '5464351354@qq.com',
      phone: '13940119189',
      deliveryTime: '',
      paymentMethod: '微信支付',
      paymentCurrency: '人民币'
    }
  } else if (orderNo === '20250903163733') {
    // 已发货订单（与订单列表保持一致）
    orderStatusValue.value = 'shipped'
    orderDate.value = '2025-09-03 16:20'
    originalOrderNo.value = ''
    
    orderHistory.value = [
      { date: '2025-09-03 16:20', action: '订单创建' },
      { date: '2025-09-03 16:21', action: `订单${orderNo}付款2235.78` },
      { date: '2025-09-04 10:30', action: '订单已发货' }
    ]
    
    orderItems.value = [
      {
        id: 1,
        productCode: '505365',
        name: '【避孕润滑】润滑剂8ml ANGUS/爱神',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 12.7,
        quantity: 176
      }
    ]
    
    recipientInfo.value = {
      name: '叶丽丽',
      region: '浙江省-杭州市-西湖区',
      zipCode: '310000',
      shippingMethod: '包邮订单',
      weight: '5000.000',
      address: '浙江省杭州市西湖区文三路259号',
      email: 'yeli@example.com',
      phone: '13800138000',
      deliveryTime: '任意日期 任意时间段',
      paymentMethod: '支付宝',
      paymentCurrency: '人民币'
    }
  } else if (orderNo === '20250903163734') {
    // 已发货订单
    orderStatusValue.value = 'shipped'
    orderDate.value = '2025-09-03 16:37'
    originalOrderNo.value = ''
    
    orderHistory.value = [
      { date: '2025-09-03 16:37', action: '订单创建' },
      { date: '2025-09-03 16:38', action: `订单${orderNo}付款100.00` },
      { date: '2025-09-04 10:00', action: '订单已发货' }
    ]
    
    orderItems.value = [
      {
        id: 1,
        productCode: '505365',
        name: '【避孕润滑】玻尿酸润滑液200g ANGUS/爱神(规格)',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 100.0,
        quantity: 1
      }
    ]
    
    recipientInfo.value = {
      name: '测试用户',
      region: '北京市-北京市-朝阳区',
      zipCode: '100000',
      shippingMethod: '包邮订单',
      weight: '250.000',
      address: '北京市朝阳区测试地址123号',
      email: 'test@example.com',
      phone: '13800138000',
      deliveryTime: '任意日期 任意时间段',
      paymentMethod: '支付宝',
      paymentCurrency: '人民币'
    }
  } else if (orderNo === '20250903163735') {
    // 已完成订单
    orderStatusValue.value = 'completed'
    orderDate.value = '2025-09-03 16:37'
    originalOrderNo.value = ''
    
    orderHistory.value = [
      { date: '2025-09-03 16:37', action: '订单创建' },
      { date: '2025-09-03 16:38', action: `订单${orderNo}付款100.00` },
      { date: '2025-09-04 10:00', action: '订单已发货' },
      { date: '2025-09-05 14:00', action: '订单已完成' }
    ]
    
    orderItems.value = [
      {
        id: 1,
        productCode: '505365',
        name: '【避孕润滑】玻尿酸润滑液200g ANGUS/爱神(规格)',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 100.0,
        quantity: 1
      }
    ]
    
    recipientInfo.value = {
      name: '测试用户',
      region: '北京市-北京市-朝阳区',
      zipCode: '100000',
      shippingMethod: '包邮订单',
      weight: '250.000',
      address: '北京市朝阳区测试地址123号',
      email: 'test@example.com',
      phone: '13800138000',
      deliveryTime: '任意日期 任意时间段',
      paymentMethod: '支付宝',
      paymentCurrency: '人民币'
    }
  } else if (orderNo === '20250903163736') {
    // 已取消订单
    orderStatusValue.value = 'cancelled'
    orderDate.value = '2025-09-03 16:37'
    originalOrderNo.value = ''
    
    orderHistory.value = [
      { date: '2025-09-03 16:37', action: '订单创建' },
      { date: '2025-09-03 17:00', action: '订单已取消' }
    ]
    
    orderItems.value = [
      {
        id: 1,
        productCode: '505365',
        name: '【避孕润滑】玻尿酸润滑液200g ANGUS/爱神(规格)',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 100.0,
        quantity: 1
      }
    ]
    
    recipientInfo.value = {
      name: '测试用户',
      region: '北京市-北京市-朝阳区',
      zipCode: '100000',
      shippingMethod: '包邮订单',
      weight: '250.000',
      address: '北京市朝阳区测试地址123号',
      email: 'test@example.com',
      phone: '13800138000',
      deliveryTime: '任意日期 任意时间段',
      paymentMethod: '支付宝',
      paymentCurrency: '人民币'
    }
  } else if (orderNo === '20250903163737') {
    // 已退货订单
    orderStatusValue.value = 'returned'
    orderDate.value = '2025-09-03 16:37'
    originalOrderNo.value = ''
    
    orderHistory.value = [
      { date: '2025-09-03 16:37', action: '订单创建' },
      { date: '2025-09-03 16:38', action: `订单${orderNo}付款100.00` },
      { date: '2025-09-04 10:00', action: '订单已发货' },
      { date: '2025-09-06 15:00', action: '订单已退货' }
    ]
    
    orderItems.value = [
      {
        id: 1,
        productCode: '505365',
        name: '【避孕润滑】玻尿酸润滑液200g ANGUS/爱神(规格)',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 100.0,
        quantity: 1
      }
    ]
    
    recipientInfo.value = {
      name: '测试用户',
      region: '北京市-北京市-朝阳区',
      zipCode: '100000',
      shippingMethod: '包邮订单',
      weight: '250.000',
      address: '北京市朝阳区测试地址123号',
      email: 'test@example.com',
      phone: '13800138000',
      deliveryTime: '任意日期 任意时间段',
      paymentMethod: '支付宝',
      paymentCurrency: '人民币'
    }
  } else if (orderNo === '20250815123456') {
    // 已付款未发货订单（与订单列表保持一致）
    orderStatusValue.value = 'paid_not_shipped'
    orderDate.value = '2025-08-15 14:30'
    originalOrderNo.value = ''
    
    orderHistory.value = [
      { date: '2025-08-15 14:30', action: '订单创建' },
      { date: '2025-08-15 14:31', action: `订单${orderNo}付款1500.00` }
    ]
    
    orderItems.value = [
      {
        id: 1,
        productCode: 'MA80001',
        name: '【男用器具】飞机杯 经典款',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 298.0,
        quantity: 2
      },
      {
        id: 2,
        productCode: 'MA80002',
        name: '【男用器具】飞机杯 升级款',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 398.0,
        quantity: 1
      },
      {
        id: 3,
        productCode: 'MA80003',
        name: '【男用器具】飞机杯 豪华款',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 498.0,
        quantity: 1
      },
      {
        id: 4,
        productCode: 'MA80004',
        name: '【男用器具】飞机杯 旗舰款',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 306.0,
        quantity: 1
      }
    ]
    
    recipientInfo.value = {
      name: '张三',
      region: '广东省-广州市-天河区',
      zipCode: '510000',
      shippingMethod: '包邮订单',
      weight: '2000.000',
      address: '广东省广州市天河区天河路123号',
      email: 'zhangsan@example.com',
      phone: '13800138001',
      deliveryTime: '任意日期 任意时间段',
      paymentMethod: '支付宝',
      paymentCurrency: '人民币'
    }
  } else if (orderNo === '20250810111213') {
    // 已完成订单（与订单列表保持一致）
    orderStatusValue.value = 'completed'
    orderDate.value = '2025-08-10 10:20'
    originalOrderNo.value = ''
    
    orderHistory.value = [
      { date: '2025-08-10 10:20', action: '订单创建' },
      { date: '2025-08-10 10:21', action: `订单${orderNo}付款800.00` },
      { date: '2025-08-11 09:00', action: '订单已发货' },
      { date: '2025-08-13 16:30', action: '订单已完成' }
    ]
    
    orderItems.value = [
      {
        id: 1,
        productCode: 'LY001',
        name: '【情趣内衣】蕾丝套装 黑色',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 268.0,
        quantity: 1
      },
      {
        id: 2,
        productCode: 'LY002',
        name: '【情趣内衣】蕾丝套装 红色',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 268.0,
        quantity: 1
      },
      {
        id: 3,
        productCode: 'LY003',
        name: '【情趣内衣】蕾丝套装 紫色',
        image: 'https://via.placeholder.com/80x80?text=Product',
        price: 264.0,
        quantity: 1
      }
    ]
    
    recipientInfo.value = {
      name: '李四',
      region: '江苏省-南京市-鼓楼区',
      zipCode: '210000',
      shippingMethod: '包邮订单',
      weight: '500.000',
      address: '江苏省南京市鼓楼区中山路50号',
      email: 'lisi@example.com',
      phone: '13800138002',
      deliveryTime: '任意日期 任意时间段',
      paymentMethod: '支付宝',
      paymentCurrency: '人民币'
    }
  } else {
    // 默认未付款订单
    orderStatusValue.value = 'pending_payment'
    orderDate.value = '2025-12-08 11:48'
    originalOrderNo.value = ''
    
    orderHistory.value = [
      { date: '2025-12-08 11:48', action: '订单创建' }
    ]
  }
}

onMounted(() => {
  // 从路由参数获取订单编号
  if (route.query.orderNumber) {
    orderNumber.value = route.query.orderNumber as string
    loadOrderDetail(orderNumber.value)
  } else {
    // 默认加载示例订单
    loadOrderDetail(orderNumber.value)
  }
})
</script>

<style scoped lang="scss">
.order-detail-page {
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

      .order-detail-wrapper {
        // 订单详情标题和订单历史
        .order-detail-header {
          margin-bottom: 20px;
          padding-bottom: 15px;
          border-bottom: 1px solid #e5e5e5;

          .order-detail-title {
            font-size: 16px;
            font-weight: bold;
            color: #333;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
            gap: 10px;

            .toggle-history {
              font-size: 14px;
              color: #666;
              text-decoration: none;
              font-weight: normal;

              &:hover {
                color: #e4393c;
              }
            }
          }

          .order-history {
            background: #f9f9f9;
            padding: 15px;
            margin-bottom: 15px;
            border: 1px solid #e5e5e5;
            font-size: 14px;
            color: #666;
            line-height: 2;

            .history-item {
              margin-bottom: 5px;

              &:last-child {
                margin-bottom: 0;
              }
            }
          }

          .order-actions {
            display: flex;
            gap: 10px;
            margin-top: 15px;

            .action-btn {
              padding: 8px 20px;
              font-size: 14px;
            }
          }
        }

        // 订单信息横幅
        .order-header-banner {
          background: #e6f3ff;
          padding: 15px 20px;
          margin-bottom: 20px;
          border: 1px solid #d0e8ff;

          .order-info-row {
            display: flex;
            align-items: center;
            gap: 30px;
            margin-bottom: 10px;

            &:last-child {
              margin-bottom: 0;
            }

            .order-info-item {
              display: flex;
              align-items: center;
              gap: 8px;
              font-size: 14px;

              .order-label {
                color: #333;
              }

              .order-value {
                color: #333;
                font-weight: 500;
              }

              .order-status-text {
                font-weight: 500;

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
            }

            .reorder-btn {
              background: #e4393c;
              border-color: #e4393c;
              color: #fff;
              padding: 6px 15px;
              font-size: 14px;
              margin-left: auto;

              &:hover {
                background: #c9302c;
                border-color: #c9302c;
              }
            }
          }
        }

        // 未付款状态提示
        .payment-notice {
          text-align: center;
          margin-bottom: 20px;
          padding: 20px;
          background: #fff5e6;
          border: 1px solid #ffe5b3;

          .pay-notice-btn {
            background: #ff8c00;
            border-color: #ff8c00;
            color: #fff;
            padding: 12px 40px;
            font-size: 16px;
            font-weight: bold;

            &:hover {
              background: #ff7a00;
              border-color: #ff7a00;
            }
          }
        }

        // 订单区块
        .order-section {
          margin-bottom: 25px;
          padding-bottom: 20px;
          border-bottom: 1px solid #e5e5e5;

          &:last-child {
            border-bottom: none;
            margin-bottom: 0;
            padding-bottom: 0;
          }

          .section-title {
            font-size: 16px;
            font-weight: bold;
            color: #333;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 2px solid #e5e5e5;
          }
        }

        // 商品表格
        .products-table-wrapper {
          overflow-x: auto;
          border: 1px solid #e5e5e5;
        }

        .products-table {
          width: 100%;
          border-collapse: collapse;
          font-size: 14px;

          thead {
            background: #f5f5f5;

            th {
              padding: 12px 8px;
              text-align: center;
              font-weight: bold;
              color: #333;
              border: 1px solid #e5e5e5;
              border-bottom: 2px solid #e5e5e5;
            }
          }

          tbody {
            .product-row {
              border-bottom: 1px solid #e5e5e5;

              &:last-child {
                border-bottom: none;
              }

              td {
                padding: 15px 8px;
                text-align: center;
                border-right: 1px solid #e5e5e5;
                vertical-align: middle;

                &:last-child {
                  border-right: none;
                }
              }

              .col-image {
                width: 100px;

                .product-image {
                  width: 80px;
                  height: 80px;
                  object-fit: cover;
                  border: 1px solid #e5e5e5;
                }
              }

              .col-code {
                width: 100px;
                color: #666;
              }

              .col-name {
                width: 400px;
                text-align: left;
                color: #333;
                padding-left: 15px;

                .product-name {
                  line-height: 20px;

                  .product-name-link {
                    color: #e4393c;
                    text-decoration: none;
                    font-weight: 500;
                    transition: color 0.3s ease;

                    &:hover {
                      color: #c9302c;
                      text-decoration: underline;
                    }
                  }
                }

                .sku-spec-text {
                  margin-top: 6px;
                  font-size: 12px;
                  color: #666;
                }
              }

              .col-price {
                width: 100px;
                color: #e4393c;
                font-weight: bold;
              }

              .col-quantity {
                width: 80px;
              }

              .col-subtotal {
                width: 120px;
                color: #e4393c;
                font-weight: bold;
              }
            }
          }

          tfoot {
            tr {
              td {
                padding: 12px 8px;
                text-align: center;
                border-top: 1px solid #e5e5e5;
                border-right: 1px solid #e5e5e5;

                &:last-child {
                  border-right: none;
                }
              }

              .summary-label {
                text-align: right;
                font-weight: 500;
                color: #333;
              }

              .summary-value {
                text-align: left;
                padding-left: 15px;
                color: #e4393c;
                font-weight: bold;
              }
            }
          }
        }

        // 收货人信息
        .recipient-info {
          display: flex;
          gap: 40px;
          padding: 20px 0;

          .info-left,
          .info-right {
            flex: 1;
            display: flex;
            flex-direction: column;
            gap: 15px;
          }

          .info-item {
            display: flex;
            align-items: flex-start;
            gap: 10px;
            font-size: 14px;
            line-height: 1.8;

            .info-label {
              color: #333;
              min-width: 100px;
              flex-shrink: 0;
            }

            .info-value {
              color: #666;
              flex: 1;
            }
          }
        }

        // 订单汇总
        .order-summary {
          display: flex;
          justify-content: flex-end;
          gap: 30px;
          margin: 30px 0;
          padding: 20px;
          background: #f9f9f9;
          border: 1px solid #e5e5e5;

          .summary-item {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            gap: 5px;
            font-size: 14px;

            .summary-label {
              color: #666;
            }

            .summary-value {
              color: #333;
              font-weight: 500;
            }

            &.total-item {
              .summary-value {
                color: #e4393c;
                font-weight: bold;
                font-size: 16px;
              }
            }
          }
        }

        // 支付操作
        .payment-action {
          text-align: center;
          padding-top: 20px;
          border-top: 1px solid #e5e5e5;

          .pay-now-btn {
            background: #ff8c00;
            border-color: #ff8c00;
            color: #fff;
            padding: 15px 60px;
            font-size: 18px;
            font-weight: bold;
            border-radius: 4px;

            &:hover {
              background: #ff7a00;
              border-color: #ff7a00;
              color: #fff;
            }

            &:active {
              background: #ff6b00;
              border-color: #ff6b00;
            }
          }
        }

        // 退款记录
        .refund-list {
          .refund-item {
            margin-bottom: 20px;
            padding: 15px;
            background: #f9f9f9;
            border: 1px solid #e5e5e5;
            border-radius: 4px;

            &:last-child {
              margin-bottom: 0;
            }

            .refund-header {
              margin-bottom: 15px;

              .refund-info-row {
                display: flex;
                flex-wrap: wrap;
                gap: 20px;
                margin-bottom: 10px;
                font-size: 14px;

                &:last-child {
                  margin-bottom: 0;
                }

                .refund-info-item {
                  display: flex;
                  align-items: center;
                  gap: 8px;

                  .refund-label {
                    color: #666;
                  }

                  .refund-value {
                    color: #333;
                  }

                  .refund-amount {
                    color: #e4393c;
                    font-weight: bold;
                    font-size: 16px;
                  }

                  .refund-status {
                    padding: 2px 8px;
                    border-radius: 3px;
                    font-size: 12px;

                    &.status-refunding {
                      background: #fff7e6;
                      color: #ff8c00;
                    }

                    &.status-success {
                      background: #f6ffed;
                      color: #52c41a;
                    }

                    &.status-failed {
                      background: #fff1f0;
                      color: #ff4d4f;
                    }
                  }
                }
              }
            }

            .refund-items {
              margin-top: 15px;
              padding-top: 15px;
              border-top: 1px solid #e5e5e5;

              .refund-items-title {
                font-size: 14px;
                font-weight: bold;
                color: #333;
                margin-bottom: 10px;
              }

              .refund-items-table {
                width: 100%;
                border-collapse: collapse;
                font-size: 14px;
                background: #fff;

                thead {
                  background: #f5f5f5;

                  th {
                    padding: 10px 8px;
                    text-align: center;
                    font-weight: bold;
                    color: #333;
                    border: 1px solid #e5e5e5;
                  }
                }

                tbody {
                  td {
                    padding: 10px 8px;
                    text-align: center;
                    border: 1px solid #e5e5e5;
                    color: #666;

                    &.product-name-cell {
                      text-align: left;
                      padding-left: 15px;

                      .sku-spec-text {
                        margin-top: 5px;
                        font-size: 12px;
                        color: #999;
                      }
                    }

                    &.refund-subtotal {
                      color: #e4393c;
                      font-weight: bold;
                    }
                  }
                }
              }
            }
          }
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
        .order-detail-wrapper {
          .recipient-info {
            flex-direction: column;
            gap: 20px;
          }

          .order-summary {
            flex-direction: column;
            align-items: flex-start;
          }
        }
      }
    }
  }
}
</style>
