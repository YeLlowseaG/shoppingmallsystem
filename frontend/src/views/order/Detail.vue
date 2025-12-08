<template>
  <div class="order-detail-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 订单详情内容区 -->
    <div class="order-detail-content">
      <div class="container">
        <!-- 面包屑导航 -->
        <div class="breadcrumb">
          <span>您当前的位置:</span>
          <router-link to="/">首页</router-link>
        </div>

        <!-- 订单详情主内容 -->
        <div class="order-detail-main">
          <!-- 订单信息横幅 -->
          <div class="order-header-banner">
            <div class="order-number-section">
              <span class="order-label">订单编号:</span>
              <span class="order-value">{{ orderNumber }}</span>
            </div>
            <div class="order-info-section">
              <span class="order-date">下单日期:{{ orderDate }}</span>
              <span class="order-status">状态:{{ orderStatus }}</span>
            </div>
            <el-button type="primary" class="reorder-btn" @click="handleReorder">依照此订单再次下单</el-button>
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
                    <th class="col-points">商品积分</th>
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
                    <td class="col-code">{{ item.productCode }}</td>
                    <td class="col-name">{{ item.name }}</td>
                    <td class="col-points">{{ item.points }}</td>
                    <td class="col-price">¥{{ item.price.toFixed(2) }}</td>
                    <td class="col-quantity">{{ item.quantity }}</td>
                    <td class="col-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</td>
                  </tr>
                </tbody>
                <tfoot>
                  <tr>
                    <td colspan="3" class="summary-label">商品数量</td>
                    <td colspan="4" class="summary-value">{{ totalQuantity }}</td>
                  </tr>
                  <tr>
                    <td colspan="3" class="summary-label">商品总金额</td>
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
                  <span class="info-value">{{ recipientInfo.zipCode }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">配送方式:</span>
                  <span class="info-value">{{ recipientInfo.shippingMethod }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">商品重量:</span>
                  <span class="info-value">{{ recipientInfo.weight }} g</span>
                </div>
                <div class="info-item">
                  <span class="info-label">收货人地址:</span>
                  <span class="info-value">{{ recipientInfo.address }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">订单附言:</span>
                  <el-input
                    v-model="orderNotes"
                    type="textarea"
                    :rows="2"
                    placeholder=""
                    class="order-notes-input"
                  />
                </div>
              </div>
              <div class="info-right">
                <div class="info-item">
                  <span class="info-label">收货人Mail:</span>
                  <span class="info-value">{{ recipientInfo.email }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">联系电话:</span>
                  <span class="info-value">{{ recipientInfo.phone }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">送货时间:</span>
                  <span class="info-value">{{ recipientInfo.deliveryTime }}</span>
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

          <!-- 支付汇总 -->
          <div class="payment-summary">
            <div class="payment-amount-box">
              <div class="amount-label">共需支付</div>
              <div class="amount-value">¥{{ totalAmount.toFixed(2) }}</div>
            </div>
            <div class="payment-details-box">
              <div class="detail-item">
                <span class="detail-label">商品总金额:</span>
                <span class="detail-value">¥{{ totalProductAmount.toFixed(2) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">配送费用:</span>
                <span class="detail-value">¥{{ shippingFee.toFixed(2) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">您可获得积分:</span>
                <span class="detail-value">{{ points }}</span>
              </div>
              <div class="detail-item total-amount-item">
                <span class="detail-label">订单总金额:</span>
                <span class="detail-value">¥{{ totalAmount.toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <!-- 支付操作 -->
          <div class="payment-action">
            <el-button type="warning" size="large" class="pay-now-btn" @click="handlePayNow">立刻付款</el-button>
            <div class="other-payment-link">
              <router-link
                :to="{
                  path: '/order/payment',
                  query: { orderNumber: orderNumber, amount: totalAmount.toFixed(2) }
                }"
              >
                选择其他支付方式》
              </router-link>
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

const router = useRouter()
const route = useRoute()

// 订单编号
const orderNumber = ref('20251208115856')
// 下单日期
const orderDate = ref('2025-12-08 11:48')
// 订单状态
const orderStatus = ref('未付款[未发货]')

// 订单商品列表（模拟数据，后续从API获取）
const orderItems = ref([
  {
    id: 1,
    productCode: '505365',
    name: '【避孕润滑】玻尿酸润滑液200g ANGUS/爱神(规格)',
    image: 'https://via.placeholder.com/80x80?text=Product',
    points: 0,
    price: 6.5,
    quantity: 1
  }
])

// 收货人信息（模拟数据，后续从API获取）
const recipientInfo = ref({
  name: '刘朋辉',
  region: '陕西省西安市-雁塔区',
  zipCode: '100000',
  shippingMethod: '包邮订单',
  weight: '250.000',
  address: '陕西省西安市雁塔区科技路徐家庄西南口148号',
  email: '5464351354@qq.com',
  phone: '18829634981',
  deliveryTime: '任意日期 任意时间段',
  paymentMethod: '支付宝',
  paymentCurrency: '人民币'
})

// 订单附言
const orderNotes = ref('')

// 计算总数量
const totalQuantity = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

// 计算商品总金额
const totalProductAmount = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 配送费用
const shippingFee = ref(0)

// 积分
const points = ref(0)

// 订单总金额
const totalAmount = computed(() => {
  return totalProductAmount.value + shippingFee.value
})

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

onMounted(() => {
  // 从路由参数获取订单编号
  if (route.query.orderNumber) {
    orderNumber.value = route.query.orderNumber as string
  }
  // TODO: 根据订单编号从API获取订单详情
})
</script>

<style scoped lang="scss">
.order-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.order-detail-content {
  padding: 20px 0 40px;
}

// 面包屑导航
.breadcrumb {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  padding: 10px 0;

  a {
    color: #e4393c;
    text-decoration: none;
    margin-left: 5px;

    &:hover {
      text-decoration: underline;
    }
  }
}

// 订单详情主内容
.order-detail-main {
  background: #fff;
  border: 1px solid #e5e5e5;
  padding: 20px;
}

// 订单信息横幅
.order-header-banner {
  background: #e6f3ff;
  padding: 15px 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 15px;

  .order-number-section {
    display: flex;
    align-items: center;
    gap: 10px;

    .order-label {
      font-size: 14px;
      color: #333;
    }

    .order-value {
      font-size: 14px;
      color: #333;
      font-weight: 500;
    }
  }

  .order-info-section {
    display: flex;
    align-items: center;
    gap: 20px;
    font-size: 14px;
    color: #333;

    .order-date {
      margin-right: 15px;
    }
  }

  .reorder-btn {
    background: #e4393c;
    border-color: #e4393c;
    color: #fff;
    padding: 8px 20px;
    font-size: 14px;

    &:hover {
      background: #c9302c;
      border-color: #c9302c;
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
      }

      .col-points {
        width: 100px;
        color: #666;
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

    .order-notes-input {
      flex: 1;

      :deep(.el-textarea__inner) {
        border: 1px solid #e5e5e5;
        font-size: 14px;
      }
    }
  }
}

// 支付汇总
.payment-summary {
  display: flex;
  gap: 30px;
  margin: 30px 0;
  padding: 20px;
  background: #f9f9f9;
  border: 1px solid #e5e5e5;

  .payment-amount-box {
    background: #fff;
    border: 2px solid #e4393c;
    padding: 20px 30px;
    text-align: center;
    min-width: 200px;

    .amount-label {
      font-size: 14px;
      color: #666;
      margin-bottom: 10px;
    }

    .amount-value {
      font-size: 36px;
      font-weight: bold;
      color: #e4393c;
      line-height: 1.2;
    }
  }

  .payment-details-box {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 12px;

    .detail-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;

      .detail-label {
        color: #666;
      }

      .detail-value {
        color: #333;
        font-weight: 500;
      }

      &.total-amount-item {
        .detail-value {
          color: #e4393c;
          font-weight: bold;
        }
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

  .other-payment-link {
    margin-top: 15px;

    a {
      color: #e4393c;
      font-size: 14px;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>

