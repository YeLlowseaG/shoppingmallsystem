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

          <!-- 支付方式选择 -->
          <div class="payment-section">
            <div class="payment-options">
              <div class="payment-radio-group">
                <div 
                  class="payment-radio" 
                  :class="{ 'is-checked': selectedPaymentMethod === 'pre_deposit' }"
                  @click="selectedPaymentMethod = 'pre_deposit'"
                >
                  <span class="payment-icon">{{ selectedPaymentMethod === 'pre_deposit' ? '●' : '○' }}</span>
                  <span class="payment-name">预存款支付</span>
                  <span class="payment-desc">商店预存款支付</span>
                </div>
                <div 
                  class="payment-radio" 
                  :class="{ 'is-checked': selectedPaymentMethod === 'wechat' }"
                  @click="selectedPaymentMethod = 'wechat'"
                >
                  <span class="payment-icon">{{ selectedPaymentMethod === 'wechat' ? '●' : '○' }}</span>
                  <span class="payment-name">微信支付</span>
                  <span class="payment-desc">微信支付</span>
                </div>
                <div 
                  class="payment-radio" 
                  :class="{ 'is-checked': selectedPaymentMethod === 'alipay' }"
                  @click="selectedPaymentMethod = 'alipay'"
                >
                  <span class="payment-icon">{{ selectedPaymentMethod === 'alipay' ? '●' : '○' }}</span>
                  <span class="payment-name">支付宝</span>
                  <span class="payment-desc">电脑端支付宝支付</span>
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

// 选中的支付方式（默认支付宝）
const selectedPaymentMethod = ref('alipay')

// 支付金额（从订单信息获取）
const totalAmount = ref(6.5)

// 查看订单详情
const handleViewDetail = () => {
  // TODO: 跳转到订单详情页面
  ElMessage.info('查看订单详情功能待实现')
}

// 立即付款
const handlePayNow = () => {
  if (!selectedPaymentMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  // TODO: 调用支付API
  ElMessage.success('正在跳转到支付页面...')
  
  // 根据选择的支付方式跳转到对应的支付网关
  switch (selectedPaymentMethod.value) {
    case 'alipay':
      // TODO: 跳转到支付宝支付页面
      console.log('跳转到支付宝支付')
      break
    case 'wechat':
      // TODO: 跳转到微信支付页面
      console.log('跳转到微信支付')
      break
    case 'pre_deposit':
      // TODO: 使用预存款支付
      console.log('使用预存款支付')
      break
  }
}

onMounted(() => {
  // 从路由参数获取订单信息
  if (route.query.orderNumber) {
    orderNumber.value = route.query.orderNumber as string
  }
  if (route.query.amount) {
    totalAmount.value = parseFloat(route.query.amount as string)
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
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding-bottom: 30px;
  border-bottom: 1px solid #e5e5e5;
}

.payment-options {
  flex: 1;

  .payment-radio-group {
    display: flex;
    flex-direction: column;
    gap: 15px;

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

.payment-amount {
  text-align: right;
  margin-left: 40px;

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

