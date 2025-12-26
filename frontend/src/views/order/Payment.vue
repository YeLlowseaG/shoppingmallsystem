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
            <el-button type="warning" size="large" class="pay-now-btn" @click="handlePayNow" :loading="paying">
              立刻付款
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 支付密码输入对话框 -->
    <el-dialog
      v-model="showPaymentPasswordDialog"
      title="请输入支付密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <!-- 提示信息 -->
      <div class="payment-password-tip">
        (如未设置过支付密码,默认支付密码为您的账号登陆密码!)
      </div>
      <el-form>
        <el-form-item label="支付密码">
          <el-input
            v-model="paymentPassword"
            type="password"
            placeholder="请输入支付密码"
            show-password
            @keyup.enter="confirmPayment"
            autofocus
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPaymentPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmPayment" :loading="paying">确认支付</el-button>
      </template>
    </el-dialog>

    <!-- 支付状态弹窗 -->
    <el-dialog
      v-model="showPaymentStatusDialog"
      title="支付状态"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="paymentStatus !== 'paying'"
    >
      <div class="payment-status-content">
        <!-- 付款中 -->
        <div v-if="paymentStatus === 'paying'" class="status-paying">
          <el-icon class="status-icon paying-icon"><Loading /></el-icon>
          <div class="status-title">正在处理支付...</div>
          <div class="status-desc">请在新打开的支付页面完成支付，完成后请点击下方按钮</div>
          <div class="status-actions">
            <el-button type="primary" @click="handleMarkAsPaid">我已付款</el-button>
            <el-button @click="handlePaymentProblem">付款有问题</el-button>
          </div>
        </div>

        <!-- 已付款 -->
        <div v-if="paymentStatus === 'paid'" class="status-paid">
          <el-icon class="status-icon success-icon"><CircleCheck /></el-icon>
          <div class="status-title">支付成功！</div>
          <div class="status-desc">订单支付成功，正在跳转到订单详情页面...</div>
        </div>

        <!-- 付款有问题 -->
        <div v-if="paymentStatus === 'problem'" class="status-problem">
          <el-icon class="status-icon error-icon"><CircleClose /></el-icon>
          <div class="status-title">支付遇到问题</div>
          <div class="status-desc">如果您已完成支付但订单状态未更新，请联系客服处理</div>
          <div class="status-actions">
            <el-button type="primary" @click="handleContactService">联系客服</el-button>
            <el-button @click="handleRetryPayment">重新支付</el-button>
            <el-button @click="closePaymentStatusDialog">关闭</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 底部 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import { payOrder, type OrderPaymentDTO, type PaymentResponseVO } from '@/api/buyer/order'
import { getDepositBalance } from '@/api/buyer/deposit'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()

// 订单编号（从路由参数或订单信息获取）
const orderNumber = ref('')

// 支付金额（从订单信息获取）
const totalAmount = ref(0)

// 支付方式相关
const showPaymentOptions = ref(false)
const selectedPaymentMethodId = ref('alipay')

// 预存款余额
const depositBalance = ref(0.00)

// 支付密码相关
const showPaymentPasswordDialog = ref(false)
const paymentPassword = ref('')
const paying = ref(false)

// 支付状态弹窗
const showPaymentStatusDialog = ref(false)
const paymentStatus = ref<'paying' | 'paid' | 'problem' | ''>('')
const currentPaymentOrderNo = ref<string>('')

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
const handlePayNow = async () => {
  if (!selectedPaymentMethodId.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  // 预存款支付需要输入密码
  if (selectedPaymentMethodId.value === 'pre_deposit') {
    // 检查余额
    if (depositBalance.value < totalAmount.value) {
      ElMessage.error('预存款余额不足，请选择其他支付方式或充值')
      return
    }
    
    // 显示支付密码输入对话框
    paymentPassword.value = ''
    showPaymentPasswordDialog.value = true
    return
  }

  // 支付宝/微信支付
  await processPayment()
}

// 确认支付（预存款支付）
const confirmPayment = async () => {
  if (!paymentPassword.value) {
    ElMessage.warning('请输入支付密码')
    return
  }
  
  showPaymentPasswordDialog.value = false
  await processPayment()
}

// 处理支付
const processPayment = async () => {
  if (paying.value) {
    return
  }

  try {
    paying.value = true

    // 转换支付方式
    const paymentMethodMap: Record<string, string> = {
      'pre_deposit': 'PRE_DEPOSIT',
      'alipay': 'ALIPAY',
      'wechat': 'WECHAT'
    }
    const backendPaymentMethod = paymentMethodMap[selectedPaymentMethodId.value] || 'ALIPAY'

    // 构建支付请求
    const paymentDTO: OrderPaymentDTO = {
      orderNo: orderNumber.value,
      paymentMethod: backendPaymentMethod
    }

    // 预存款支付需要支付密码
    if (selectedPaymentMethodId.value === 'pre_deposit') {
      paymentDTO.paymentPassword = paymentPassword.value
    }

    // 调用支付接口
    const response: PaymentResponseVO = await payOrder(orderNumber.value, paymentDTO)

    // 保存订单号
    currentPaymentOrderNo.value = orderNumber.value

    // 根据支付方式处理
    if (selectedPaymentMethodId.value === 'pre_deposit') {
      // 预存款支付直接成功
      paymentStatus.value = 'paid'
      showPaymentStatusDialog.value = true
      ElMessage.success('支付成功！')
      
      // 延迟关闭弹窗并跳转
      setTimeout(() => {
        showPaymentStatusDialog.value = false
        router.push({
          path: '/order/detail',
          query: {
            orderNumber: orderNumber.value,
            paymentStatus: 'success'
          }
        })
      }, 2000)
    } else {
      // 支付宝/微信支付
      if (response.isMock) {
        // 模拟支付，调用模拟支付成功接口
        paymentStatus.value = 'paying'
        showPaymentStatusDialog.value = true
        ElMessage.info('正在处理支付...')
        
        // 调用模拟支付成功接口
        try {
          await request.post(`/api/buyer/payment/mock/success?orderNo=${orderNumber.value}&paymentMethod=${backendPaymentMethod}`);
          
          paymentStatus.value = 'paid'
          ElMessage.success('支付成功！')
          
          // 延迟关闭弹窗并跳转
          setTimeout(() => {
            showPaymentStatusDialog.value = false
            router.push({
              path: '/order/detail',
              query: {
                orderNumber: orderNumber.value,
                paymentStatus: 'success'
              }
            })
          }, 2000)
        } catch (error: any) {
          paymentStatus.value = 'problem'
          console.error('支付处理失败:', error);
        }
      } else {
        // 真实支付，显示支付中弹窗
        paymentStatus.value = 'paying'
        showPaymentStatusDialog.value = true
        
        // 处理支付表单或支付URL
        if (response.paymentParams) {
          // 服务端返回HTML表单，使用动态表单方式提交以确保跳转成功
          try {
            // 移除HTML内容中的反引号
            const cleanHtml = response.paymentParams.replace(/`/g, '');
            
            // 解析表单HTML以获取action和参数
            const parser = new DOMParser();
            const doc = parser.parseFromString(cleanHtml, 'text/html');
            const form = doc.querySelector('form#alipayForm');
            
            if (form && form.action) {
              // 获取表单action和所有input参数
              const formAction = form.action;
              const formData = new FormData();
              
              // 收集所有隐藏输入字段
              form.querySelectorAll('input[type="hidden"]').forEach(input => {
                if (input.name && input.value) {
                  formData.append(input.name, input.value);
                }
              });
              
              // 创建form元素并提交
              const tempForm = document.createElement('form');
              tempForm.method = 'POST';
              tempForm.action = formAction;
              tempForm.target = '_blank';
              tempForm.style.display = 'none';
              
              // 添加所有参数
              formData.forEach((value, key) => {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = key;
                input.value = value;
                tempForm.appendChild(input);
              });
              
              document.body.appendChild(tempForm);
              tempForm.submit();
              document.body.removeChild(tempForm);
              // 保持支付中状态，等待用户完成支付
            } else {
              // 如果解析失败，回退到原始方法
              const win = window.open('', '_blank');
              if (win) {
                win.document.open();
                win.document.write(cleanHtml);
                win.document.close();
                // 保持支付中状态，等待用户完成支付
              } else {
                ElMessage.error('弹窗被拦截，请允许弹窗或改用非弹窗方式支付');
                paymentStatus.value = 'problem'
              }
            }
          } catch (e) {
            console.error('打开支付页面失败', e);
            ElMessage.error('打开支付页面失败，请重试');
            paymentStatus.value = 'problem'
          }
        } else if (response.paymentUrl) {
          // 跳转到支付URL
          window.open(response.paymentUrl, '_blank');
          // 保持支付中状态，等待用户完成支付
        } else if (response.qrCodeUrl) {
          // 显示二维码支付（微信支付使用）
          ElMessage.info('请扫描二维码支付');
          // TODO: 可以打开二维码弹窗显示二维码
        } else {
          ElMessage.warning('支付信息未生成');
          paymentStatus.value = 'problem'
        }
      }
    }
  } catch (error: any) {
    // request拦截器已经显示了错误消息，这里不需要再显示
    // 如果是支付密码错误，重新打开密码输入对话框
    if (selectedPaymentMethodId.value === 'pre_deposit') {
      const errorMessage = error.message || error.response?.data?.message || ''
      if (errorMessage.includes('支付密码错误')) {
        paymentPassword.value = ''
        showPaymentPasswordDialog.value = true
        return
      }
    }
    
    // 其他错误，显示支付问题弹窗
    paymentStatus.value = 'problem'
    showPaymentStatusDialog.value = true
    console.error('支付失败:', error)
  } finally {
    paying.value = false
    // 只有在非预存款支付或支付成功时才清空密码
    // 预存款支付失败时不清空，让用户重新输入
    if (selectedPaymentMethodId.value !== 'pre_deposit') {
      paymentPassword.value = ''
    }
  }
}

// 我已付款
const handleMarkAsPaid = () => {
  ElMessage.info('正在验证支付状态...')
  // 跳转到订单详情页面，让用户查看订单状态
  showPaymentStatusDialog.value = false
  router.push({
    path: '/order/detail',
    query: {
      orderNumber: orderNumber.value
    }
  })
}

// 付款有问题
const handlePaymentProblem = () => {
  paymentStatus.value = 'problem'
}

// 联系客服
const handleContactService = () => {
  ElMessage.info('请联系客服处理支付问题，客服电话：400-xxx-xxxx')
  // TODO: 可以跳转到客服页面或打开客服对话框
}

// 重新支付
const handleRetryPayment = () => {
  showPaymentStatusDialog.value = false
  paymentStatus.value = ''
  currentPaymentOrderNo.value = ''
  // 可以重新触发支付流程
  // 这里不自动触发，让用户重新点击付款按钮
}

// 关闭支付状态弹窗
const closePaymentStatusDialog = () => {
  showPaymentStatusDialog.value = false
  paymentStatus.value = ''
  currentPaymentOrderNo.value = ''
}

// 加载预存款余额
const loadDepositBalance = async () => {
  try {
    const data = await getDepositBalance()
    depositBalance.value = data.availableBalance || 0
  } catch (error: any) {
    console.error('加载预存款余额失败:', error)
    depositBalance.value = 0
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
  // 从路由参数获取支付方式，如果存在则设置为选中
  if (route.query.paymentMethod) {
    const paymentMethod = route.query.paymentMethod as string
    if (paymentMethods.value.some(m => m.id === paymentMethod)) {
      selectedPaymentMethodId.value = paymentMethod
    }
  }
  
  // 检查URL参数中是否有支付成功标识
  const urlPaymentStatus = route.query.paymentStatus as string
  if (urlPaymentStatus === 'success') {
    // 显示支付成功弹窗
    paymentStatus.value = 'paid'
    showPaymentStatusDialog.value = true
    ElMessage.success('支付成功！订单已确认，等待商家发货')
    
    // 延迟关闭弹窗并清除URL参数
    setTimeout(() => {
      showPaymentStatusDialog.value = false
      router.replace({
        path: '/order/payment',
        query: {
          orderNumber: orderNumber.value,
          amount: totalAmount.value.toFixed(2),
          paymentMethod: selectedPaymentMethodId.value
        }
      })
      // 跳转到订单详情页面
      setTimeout(() => {
        router.push({
          path: '/order/detail',
          query: {
            orderNumber: orderNumber.value
          }
        })
      }, 500)
    }, 2000)
  }
  
  // 加载预存款余额
  loadDepositBalance()
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

// 支付密码对话框提示样式
:deep(.el-dialog__body) {
  .payment-password-tip {
    background: #fffbe6;
    border: 1px solid #ffe58f;
    padding: 10px 15px;
    margin-bottom: 20px;
    color: #666;
    font-size: 13px;
    line-height: 1.5;
    border-radius: 4px;
  }
}

// 支付状态弹窗样式
.payment-status-content {
  text-align: center;
  padding: 20px;

  .status-icon {
    font-size: 64px;
    margin-bottom: 20px;

    &.paying-icon {
      color: #409eff;
      animation: rotate 1s linear infinite;
    }

    &.success-icon {
      color: #67c23a;
    }

    &.error-icon {
      color: #f56c6c;
    }
  }

  .status-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin-bottom: 10px;
  }

  .status-desc {
    font-size: 14px;
    color: #666;
    margin-bottom: 20px;
    line-height: 1.6;
  }

  .status-actions {
    display: flex;
    justify-content: center;
    gap: 10px;
    margin-top: 20px;
  }

  .status-paying {
    .status-desc {
      margin-bottom: 30px;
    }
  }

  .status-paid {
    .status-desc {
      color: #67c23a;
    }
  }

  .status-problem {
    .status-desc {
      color: #f56c6c;
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>


