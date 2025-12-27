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
          <MemberSidebar active-menu="deposit/recharge" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <!-- 充值表单 -->
            <div class="recharge-form-wrapper">
              <h2 class="recharge-title">充值到预存款</h2>
              
              <!-- 线上充值按钮 -->
              <div class="online-recharge-btn-wrapper">
                <el-button type="danger" class="online-recharge-btn">线上充值</el-button>
              </div>

              <el-form
                ref="rechargeFormRef"
                :model="rechargeForm"
                :rules="rules"
                label-width="0"
                class="recharge-form"
              >
                <!-- 充值金额 -->
                <el-form-item prop="amount">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        输入充值金额:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="rechargeForm.amount"
                          placeholder="请输入充值金额"
                          class="form-input"
                          clearable
                          type="number"
                          :min="0.01"
                          step="0.01"
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 支付币别 -->
                <el-form-item prop="currency">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        选择支付币别:
                      </td>
                      <td class="input-cell">
                        <el-select v-model="rechargeForm.currency" class="form-select" style="width: 200px;">
                          <el-option label="人民币" value="CNY" />
                        </el-select>
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 支付方式 -->
                <el-form-item prop="paymentMethod">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        选择支付方式:
                      </td>
                      <td class="input-cell">
                        <div class="payment-methods">
                          <div
                            v-for="method in paymentMethods"
                            :key="method.id"
                            class="payment-method-item"
                            :class="{ 'is-selected': rechargeForm.paymentMethod === method.id }"
                            @click="rechargeForm.paymentMethod = method.id"
                          >
                            <span class="payment-radio">{{ rechargeForm.paymentMethod === method.id ? '●' : '○' }}</span>
                            <span class="payment-name">{{ method.name }}</span>
                            <span class="payment-desc">{{ method.description }}</span>
                          </div>
                        </div>
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 立即付款按钮 -->
                <el-form-item>
                  <table class="form-table">
                    <tr>
                      <td class="label-cell"></td>
                      <td class="input-cell">
                        <el-button
                          type="danger"
                          :loading="loading"
                          @click="handlePayNow"
                          class="pay-now-btn"
                        >
                          点击立刻付款
                        </el-button>
                      </td>
                    </tr>
                  </table>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <Footer />

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
          <div class="status-desc">充值金额已到账，正在跳转到余额页面...</div>
        </div>

        <!-- 付款有问题 -->
        <div v-if="paymentStatus === 'problem'" class="status-problem">
          <el-icon class="status-icon error-icon"><CircleClose /></el-icon>
          <div class="status-title">支付遇到问题</div>
          <div class="status-desc">如果您已完成支付但未到账，请联系客服处理</div>
          <div class="status-actions">
            <el-button type="primary" @click="handleContactService">联系客服</el-button>
            <el-button @click="handleRetryPayment">重新支付</el-button>
            <el-button @click="closePaymentStatusDialog">关闭</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { rechargeDeposit, type PaymentResponseVO } from '@/api/buyer/deposit'

const router = useRouter()
const route = useRoute()
const rechargeFormRef = ref<FormInstance>()
const loading = ref(false)
const unreadMessageCount = ref(0)

// 支付状态弹窗
const showPaymentStatusDialog = ref(false)
const paymentStatus = ref<'paying' | 'paid' | 'problem' | ''>('')
const currentPaymentOrderNo = ref<string>('')

// 表单数据
const rechargeForm = reactive({
  amount: '0.01',
  currency: 'CNY',
  paymentMethod: 'wechat' // 默认选择微信支付
})

// 支付方式列表
const paymentMethods = [
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
]

// 验证规则
const rules: FormRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        const amount = parseFloat(value)
        if (isNaN(amount) || amount <= 0) {
          callback(new Error('充值金额必须大于0'))
        } else if (amount < 0.01) {
          callback(new Error('充值金额不能小于0.01元'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  currency: [
    { required: true, message: '请选择支付币别', trigger: 'change' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

// 立即付款
const handlePayNow = async () => {
  if (!rechargeFormRef.value) return

  await rechargeFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const amount = parseFloat(rechargeForm.amount)
        
        // 调用真实充值接口
        const paymentResponse: PaymentResponseVO = await rechargeDeposit({
          amount,
          currency: rechargeForm.currency,
          paymentMethod: rechargeForm.paymentMethod
        })
        
        // 保存订单号（如果有）
        if (paymentResponse.internalOrderNo) {
          currentPaymentOrderNo.value = paymentResponse.internalOrderNo
        }
        
        // 如果是模拟支付，直接显示成功
        if (paymentResponse.isMock) {
          paymentStatus.value = 'paid'
          showPaymentStatusDialog.value = true
          
          // 重置表单
          rechargeForm.amount = '0.01'
          rechargeForm.paymentMethod = 'wechat'
          
          // 延迟关闭弹窗并跳转
          setTimeout(() => {
            showPaymentStatusDialog.value = false
            router.push('/member/deposit/balance')
          }, 2000)
        } else {
          // 真实支付，显示支付中弹窗
          paymentStatus.value = 'paying'
          showPaymentStatusDialog.value = true
          
          // 跳转到支付页面或展示二维码
          if (paymentResponse.paymentParams) {
            // 服务端返回 HTML 表单（auto-submit），在新窗口打开以触发支付宝页面跳转
            try {
              const win = window.open('', '_blank')
              if (win) {
                win.document.open()
                win.document.write(paymentResponse.paymentParams)
                win.document.close()
                // 保持支付中状态，等待用户完成支付
              } else {
                ElMessage.error('弹窗被拦截，请允许弹窗或改用非弹窗方式支付')
                paymentStatus.value = 'problem'
              }
            } catch (e) {
              console.error('打开支付页面失败', e)
              ElMessage.error('打开支付页面失败，请重试')
              paymentStatus.value = 'problem'
            }
          } else if (paymentResponse.paymentUrl) {
            // 跳转到支付URL
            window.location.href = paymentResponse.paymentUrl
          } else if (paymentResponse.qrCodeUrl) {
            // 显示二维码支付
            ElMessage.info('请使用手机扫描二维码完成支付')
            // TODO: 可以打开二维码弹窗显示二维码
          } else {
            ElMessage.warning('支付订单创建成功，但未返回支付URL')
            paymentStatus.value = 'problem'
          }
        }
      } catch (error: any) {
        console.error('支付失败:', error)
        ElMessage.error(error.message || '支付失败，请重试')
        paymentStatus.value = 'problem'
        showPaymentStatusDialog.value = true
      } finally {
        loading.value = false
      }
    }
  })
}

// 我已付款
const handleMarkAsPaid = () => {
  ElMessage.info('正在验证支付状态...')
  // 如果支付成功（通过回调），会自动跳转
  // 这里可以添加轮询逻辑检查支付状态
  // 暂时直接跳转到余额页面，让用户查看充值记录
  showPaymentStatusDialog.value = false
  router.push('/member/deposit/balance')
}

// 付款有问题
const handlePaymentProblem = () => {
  paymentStatus.value = 'problem'
}

// 联系客服
const handleContactService = () => {
  ElMessage.info('请联系客服处理支付问题')
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

onMounted(() => {
  // 检查URL参数中是否有支付成功标识
  const urlPaymentStatus = route.query.paymentStatus as string
  if (urlPaymentStatus === 'success') {
    // 显示支付成功弹窗
    paymentStatus.value = 'paid'
    showPaymentStatusDialog.value = true
    ElMessage.success('充值成功！金额已到账')
    
    // 延迟关闭弹窗并清除URL参数
    setTimeout(() => {
      showPaymentStatusDialog.value = false
      router.replace({
        path: '/member/deposit/recharge'
      })
      // 跳转到余额页面查看
      setTimeout(() => {
        router.push('/member/deposit/balance')
      }, 500)
    }, 2000)
  }
  // 可以在这里加载用户信息、预存款余额等
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

      .recharge-form-wrapper {
        .recharge-title {
          font-size: 18px;
          font-weight: bold;
          color: #333;
          margin: 0 0 20px 0;
          padding-bottom: 15px;
          border-bottom: 1px solid #e5e5e5;
        }

        .online-recharge-btn-wrapper {
          margin-bottom: 30px;

          .online-recharge-btn {
            background: #e4393c;
            border-color: #e4393c;
            padding: 10px 30px;
            font-size: 14px;
            font-weight: bold;

            &:hover {
              background: #c9302c;
              border-color: #c9302c;
            }
          }
        }

        .recharge-form {
          :deep(.el-form-item) {
            margin-bottom: 20px;
          }

          .form-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 0;

            .label-cell {
              width: 150px;
              padding: 8px 0;
              vertical-align: top;
              font-size: 14px;
              color: #333;
              text-align: right;
              padding-right: 15px;
            }

            .input-cell {
              padding: 8px 0;
              vertical-align: top;

              .form-input {
                width: 300px;
              }

              .form-select {
                width: 200px;
              }

              .payment-methods {
                display: flex;
                flex-direction: column;
                gap: 15px;

                .payment-method-item {
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

                  .payment-radio {
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
                    min-width: 80px;
                  }

                  .payment-desc {
                    font-size: 12px;
                    color: #999;
                    margin-left: 5px;
                  }

                  // 选中状态
                  &.is-selected {
                    .payment-radio {
                      color: #e4393c;
                    }

                    .payment-name {
                      color: #e4393c;
                    }
                  }
                }
              }

              .pay-now-btn {
                background: #e4393c;
                border-color: #e4393c;
                padding: 12px 40px;
                font-size: 16px;
                font-weight: bold;
                margin-top: 10px;

                &:hover {
                  background: #c9302c;
                  border-color: #c9302c;
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
        .recharge-form-wrapper {
          .recharge-form {
            .form-table {
              .input-cell {
                .form-input,
                .form-select {
                  width: 100%;
                }

                .payment-methods {
                  .payment-method-item {
                    flex-wrap: wrap;
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
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 10px;
    color: #333;
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
    flex-wrap: wrap;
  }

  .status-paying,
  .status-paid,
  .status-problem {
    min-height: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
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


