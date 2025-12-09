<template>
  <div class="payment-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 支付内容区 -->
    <div class="payment-content">
      <div class="container">
        <!-- 面包屑导航 -->
        <div class="breadcrumb">
          <span>您当前的位置:</span>
          <router-link to="/">首页</router-link>
        </div>

        <!-- 支付主内容 -->
        <div class="payment-main">
          <!-- 订单信息 -->
          <div class="order-info">
            <div class="order-number">
              <span class="label">订单编号:</span>
              <span class="value">{{ orderNumber }}</span>
              <el-button type="text" class="view-detail-link" @click="handleViewDetail">查看详细</el-button>
            </div>
          </div>

          <!-- 选择支付方式 -->
          <div class="payment-section">
            <div class="section-header">
              <h3 class="section-title">
                选择支付方式
                <el-button type="text" class="modify-link" @click="showPaymentOptions = !showPaymentOptions">
                  {{ showPaymentOptions ? '收起' : '修改' }}
                </el-button>
              </h3>
            </div>
            <div class="payment-info">
              <div v-if="!showPaymentOptions" class="info-item">
                <span class="info-label">支付方式：</span>
                <span class="info-value">{{ selectedPaymentMethod.name }}</span>
              </div>
            </div>

            <!-- 支付方式选择列表（展开显示） -->
            <div v-if="showPaymentOptions" class="payment-methods-list">
              <div class="payment-radio-group">
                <div
                  v-for="method in paymentMethods"
                  :key="method.id"
                  class="payment-radio"
                  :class="{ 'is-checked': selectedPaymentMethodId === method.id }"
                  @click="selectedPaymentMethodId = method.id"
                >
                  <span class="payment-icon">{{ selectedPaymentMethodId === method.id ? '●' : '○' }}</span>
                  <span class="payment-name">{{ method.name }}</span>
                  <span class="payment-desc">{{ method.description }}</span>
                  <span v-if="method.id === 'pre_deposit'" class="deposit-balance">
                    预存款余额：¥{{ depositBalance.toFixed(2) }}
                  </span>
                </div>
              </div>
            </div>
            <div class="payment-amount">
              <div class="amount-label">共需支付</div>
              <div class="amount-value">¥{{ totalAmount.toFixed(2) }}</div>
            </div>
          </div>

          <!-- 警告提示 -->
          <div class="warning-box">
            <div class="warning-text">
              点击付款进入支付网关页面后,请勿再重复点击"立刻付款"按钮,否则将会导致重复生成几张相同的支付单据!
            </div>
          </div>

          <!-- 立即付款按钮 -->
          <div class="payment-action">
            <el-button type="warning" size="large" class="pay-now-btn" @click="handlePayNow">
              立刻付款
            </el-button>
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

// 订单编号（从路由参数或订单信息获取）
const orderNumber = ref('20251208115856')

// 支付金额（从订单信息获取）
const totalAmount = ref(6.5)

// 支付方式相关
const showPaymentOptions = ref(false)
const selectedPaymentMethodId = ref('alipay')

// 预存款余额（模拟数据，后续从后端获取）
const depositBalance = ref(0.00)

// 支付方式列表
const paymentMethods = ref([
  {
    id: 'pre_deposit',
    name: '预存款支付',
    description: '商店预存款支付'
  },
  {
    id: 'wechat',
    name: '微信支付',
    description: '微信支付'
  },
  {
    id: 'alipay',
    name: '支付宝',
    description: '电脑端支付宝支付'
  }
])

// 当前选中的支付方式
const selectedPaymentMethod = computed(() => {
  return paymentMethods.value.find(m => m.id === selectedPaymentMethodId.value) || paymentMethods.value[2]
})

// 查看订单详情
const handleViewDetail = () => {
  router.push({
    path: '/order/detail',
    query: {
      orderNumber: orderNumber.value,
      amount: totalAmount.value.toFixed(2)
    }
  })
}

// 立即付款
const handlePayNow = () => {
  if (!selectedPaymentMethodId.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  // 模拟支付过程
  ElMessage.info('正在处理支付...')
  
  // 模拟支付API调用，延迟1秒后返回支付成功
  setTimeout(() => {
    // 模拟支付成功
    ElMessage.success('支付成功！')
    
    // 延迟跳转到订单详情页面
    setTimeout(() => {
      router.push({
        path: '/order/detail',
        query: {
          orderNumber: orderNumber.value,
          amount: totalAmount.value.toFixed(2),
          paymentStatus: 'success'
        }
      })
    }, 1000)
  }, 1000)
}

onMounted(() => {
  // 从路由参数获取订单信息
  if (route.query.orderNumber) {
    orderNumber.value = route.query.orderNumber as string
  }
  if (route.query.amount) {
    totalAmount.value = parseFloat(route.query.amount as string)
  }
  // 从路由参数获取支付方式，如果存在则设置为选中
  if (route.query.paymentMethod) {
    const paymentMethod = route.query.paymentMethod as string
    if (paymentMethods.value.some(m => m.id === paymentMethod)) {
      selectedPaymentMethodId.value = paymentMethod
    }
  }
})
</script>

<style scoped lang="scss">
.payment-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.payment-content {
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

// 支付主内容
.payment-main {
  background: #fff;
  border: 1px solid #e5e5e5;
  padding: 30px;
}

// 订单信息
.order-info {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e5e5;

  .order-number {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;

    .label {
      color: #666;
    }

    .value {
      color: #333;
      font-weight: 500;
    }

    .view-detail-link {
      color: #e4393c;
      font-size: 14px;
      padding: 0;
      margin-left: 10px;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

// 支付方式选择
.payment-section {
  margin-bottom: 30px;
  padding-bottom: 30px;
  border-bottom: 1px solid #e5e5e5;

  .section-header {
    margin-bottom: 15px;

    .section-title {
      font-size: 16px;
      font-weight: bold;
      color: #333;
      margin: 0;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .modify-link {
        color: #e4393c;
        font-size: 14px;
        padding: 0;
        font-weight: normal;
      }
    }
  }

  .payment-info {
    .info-item {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;

      .info-label {
        font-size: 14px;
        color: #333;
        min-width: 120px;
      }

      .info-value {
        font-size: 14px;
        color: #666;
      }
    }
  }

  .payment-methods-list {
    margin-top: 15px;
    padding: 0;
    overflow: visible;

    .payment-radio-group {
      display: flex;
      flex-direction: column;
      gap: 15px;
      padding: 0;
      margin: 0;
      width: 100%;

      .payment-radio {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 0;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          .payment-name {
            color: #e4393c;
          }
        }

        .payment-icon {
          font-size: 18px;
          color: #999;
          width: 20px;
          text-align: center;
          display: inline-block;
          transition: color 0.3s;
        }

        .payment-name {
          font-size: 14px;
          color: #333;
          font-weight: 500;
          transition: color 0.3s;
        }

        .payment-desc {
          font-size: 12px;
          color: #999;
          margin-left: 5px;
        }

        .deposit-balance {
          font-size: 14px;
          color: #ff8c00;
          margin-left: auto;
          padding: 4px 8px;
          background-color: #fffacd;
          border-radius: 2px;
        }

        // 选中状态
        &.is-checked {
          .payment-icon {
            color: #e4393c;
          }

          .payment-name {
            color: #e4393c;
          }
        }
      }
    }
  }
}

.payment-amount {
  text-align: right;
  margin-top: 20px;

  .amount-label {
    font-size: 14px;
    color: #666;
    margin-bottom: 10px;
  }

  .amount-value {
    font-size: 32px;
    font-weight: bold;
    color: #e4393c;
    line-height: 1.2;
  }
}

// 警告提示框
.warning-box {
  background: #fffacd;
  border: 1px solid #ffd700;
  padding: 15px 20px;
  margin-bottom: 30px;
  border-radius: 4px;

  .warning-text {
    font-size: 14px;
    color: #d2691e;
    line-height: 1.6;
  }
}

// 支付操作按钮
.payment-action {
  text-align: center;
  padding-top: 20px;

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
</style>

