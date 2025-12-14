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
            <div class="order-message-wrapper">
              <!-- 页面标题 -->
              <div class="page-title">
                {{ messageType === 'paid' ? '线下支付、银行汇款或其他支付方式:' : '订单问题反馈:' }}
              </div>

              <!-- 表单 -->
              <el-form
                ref="messageFormRef"
                :model="messageForm"
                :rules="rules"
                :validate-on-rule-change="false"
                label-width="0"
                class="message-form"
                @submit.prevent
              >
                <!-- 我已付款 - 显示付款相关字段 -->
                <template v-if="messageType === 'paid'">
                  <!-- 付款金额 -->
                  <el-form-item prop="paymentAmount">
                    <table class="form-table">
                      <tr>
                        <td class="label-cell">
                          <span class="required">*</span>付款金额:
                        </td>
                        <td class="input-cell">
                          <el-input
                            v-model="messageForm.paymentAmount"
                            placeholder="请输入付款金额"
                            class="form-input amount-input"
                            clearable
                          />
                          <span class="unit">元</span>
                        </td>
                      </tr>
                    </table>
                  </el-form-item>

                  <!-- 付款方式 -->
                  <el-form-item prop="paymentMethod">
                    <table class="form-table">
                      <tr>
                        <td class="label-cell">
                          <span class="required">*</span>付款方式:
                        </td>
                        <td class="input-cell">
                          <el-input
                            v-model="messageForm.paymentMethod"
                            placeholder="请输入付款方式，如：银行转账、支付宝、微信等"
                            class="form-input"
                            clearable
                          />
                        </td>
                      </tr>
                    </table>
                  </el-form-item>

                  <!-- 付款时间 -->
                  <el-form-item prop="paymentTime">
                    <table class="form-table">
                      <tr>
                        <td class="label-cell">
                          <span class="required">*</span>付款时间:
                        </td>
                        <td class="input-cell">
                          <el-date-picker
                            v-model="messageForm.paymentDate"
                            type="date"
                            placeholder="选择日期"
                            format="YYYY-MM-DD"
                            value-format="YYYY-MM-DD"
                            class="date-picker"
                            style="width: 150px;"
                          />
                          <el-select
                            v-model="messageForm.paymentHour"
                            placeholder="时"
                            class="time-select"
                            style="width: 80px; margin-left: 10px;"
                          >
                            <el-option
                              v-for="hour in hours"
                              :key="hour"
                              :label="String(hour).padStart(2, '0')"
                              :value="hour"
                            />
                          </el-select>
                          <span class="time-separator">时</span>
                          <el-select
                            v-model="messageForm.paymentMinute"
                            placeholder="分"
                            class="time-select"
                            style="width: 80px; margin-left: 10px;"
                          >
                            <el-option
                              v-for="minute in minutes"
                              :key="minute"
                              :label="String(minute).padStart(2, '0')"
                              :value="minute"
                            />
                          </el-select>
                          <span class="time-separator">分</span>
                        </td>
                      </tr>
                    </table>
                  </el-form-item>
                </template>

                <!-- 我有问题 - 显示标题和内容字段 -->
                <template v-else>
                  <!-- 标题 -->
                  <el-form-item prop="title">
                    <table class="form-table">
                      <tr>
                        <td class="label-cell">
                          <span class="required">*</span>标题:
                        </td>
                        <td class="input-cell">
                          <el-input
                            v-model="messageForm.title"
                            placeholder="请输入消息标题"
                            class="form-input"
                            clearable
                          />
                        </td>
                      </tr>
                    </table>
                  </el-form-item>

                  <!-- 内容 -->
                  <el-form-item prop="content">
                    <table class="form-table">
                      <tr>
                        <td class="label-cell">
                          内容:
                        </td>
                        <td class="input-cell">
                          <el-input
                            v-model="messageForm.content"
                            type="textarea"
                            :rows="8"
                            placeholder="请输入问题描述"
                            class="form-textarea"
                          />
                        </td>
                      </tr>
                    </table>
                  </el-form-item>
                </template>

                <!-- 备注（通用字段） -->
                <el-form-item prop="remarks">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        备注:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="messageForm.remarks"
                          type="textarea"
                          :rows="4"
                          placeholder="请输入备注信息（选填）"
                          class="form-textarea"
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 操作按钮 -->
                <el-form-item>
                  <div class="form-actions">
                    <el-button type="primary" @click="handleSubmit">提交</el-button>
                    <el-button @click="handleCancel">取消</el-button>
                  </div>
                </el-form-item>
              </el-form>
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
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { createOrderMessage } from '@/api/buyer/order-message'

const router = useRouter()
const route = useRoute()

const unreadMessageCount = ref(0)

// 消息类型：paid（我已付款）或 question（我有问题）
const messageType = ref<'paid' | 'question'>('question')

// 订单编号
const orderNumber = ref('')

// 表单引用
const messageFormRef = ref<FormInstance>()

// 表单数据
const messageForm = reactive({
  // 我已付款相关字段
  paymentAmount: '',
  paymentMethod: '',
  paymentDate: '',
  paymentHour: 0,
  paymentMinute: 0,
  // 我有问题相关字段
  title: '',
  content: '',
  // 通用字段
  remarks: ''
})

// 小时选项（0-23）
const hours = Array.from({ length: 24 }, (_, i) => i)

// 分钟选项（0-59，步长为5）
const minutes = Array.from({ length: 12 }, (_, i) => i * 5)

// 表单验证规则
const getRules = () => {
  if (messageType.value === 'paid') {
    return {
      paymentAmount: [
        { required: true, message: '请输入付款金额', trigger: 'blur' },
        { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的金额格式', trigger: 'blur' }
      ],
      paymentMethod: [
        { required: true, message: '请输入付款方式', trigger: 'blur' }
      ]
    }
  } else {
    return {
      title: [
        { required: true, message: '请输入标题', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入内容', trigger: 'blur' }
      ]
    }
  }
}

const rules = computed(() => getRules())

// 提交表单
const handleSubmit = async () => {
  if (!messageFormRef.value) return

  try {
    // 先进行基础字段验证
    await messageFormRef.value.validate()

    // 如果是"我已付款"，额外验证付款时间
    if (messageType.value === 'paid') {
      if (!messageForm.paymentDate) {
        ElMessage.warning('请选择付款日期')
        return
      }
      if (messageForm.paymentHour === null || messageForm.paymentHour === undefined) {
        ElMessage.warning('请选择付款小时')
        return
      }
      if (messageForm.paymentMinute === null || messageForm.paymentMinute === undefined) {
        ElMessage.warning('请选择付款分钟')
        return
      }
    }

    // 根据消息类型构建提交数据
    let submitData: any = {
      orderNumber: orderNumber.value,
      messageType: messageType.value,
      remarks: messageForm.remarks || ''
    }

    if (messageType.value === 'paid') {
      submitData = {
        ...submitData,
        paymentAmount: messageForm.paymentAmount,
        paymentMethod: messageForm.paymentMethod,
        paymentDate: messageForm.paymentDate,
        paymentHour: messageForm.paymentHour,
        paymentMinute: messageForm.paymentMinute
      }
    } else {
      submitData = {
        ...submitData,
        title: messageForm.title,
        content: messageForm.content || ''
      }
    }

    // 调用API提交订单消息
    try {
      await createOrderMessage(submitData)
      ElMessage.success('提交成功')
      
      // 提交成功后返回订单详情页
      setTimeout(() => {
        router.push({
          path: '/order/detail',
          query: { orderNumber: orderNumber.value }
        })
      }, 1500)
    } catch (error: any) {
      console.error('提交订单消息失败:', error)
      ElMessage.error(error?.response?.data?.message || '提交失败，请重试')
    }
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.warning('请完整填写必填项')
  }
}

// 取消操作
const handleCancel = () => {
  ElMessageBox.confirm('确定要取消吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 返回订单详情页
    router.push({
      path: '/order/detail',
      query: { orderNumber: orderNumber.value }
    })
  }).catch(() => {
    // 用户取消操作
  })
}

// 初始化
onMounted(() => {
  // 从路由参数获取订单编号和消息类型
  const orderNo = route.query.orderNumber as string
  const msgType = route.query.type as string
  
  if (!orderNo) {
    ElMessage.error('订单号不能为空')
    router.push('/member/transaction/orders')
    return
  }
  
  orderNumber.value = orderNo
  
  if (msgType && (msgType === 'paid' || msgType === 'question')) {
    messageType.value = msgType as 'paid' | 'question'
  } else {
    // 如果没有指定类型，默认使用 question
    messageType.value = 'question'
  }

  // 如果是"我已付款"，设置默认付款时间为当前时间
  if (messageType.value === 'paid') {
    const now = new Date()
    messageForm.paymentDate = now.toISOString().split('T')[0]
    messageForm.paymentHour = now.getHours()
    messageForm.paymentMinute = Math.floor(now.getMinutes() / 5) * 5 // 向下取整到5的倍数
  }

  // 清除表单验证状态，确保默认不显示必填提示
  setTimeout(() => {
    if (messageFormRef.value) {
      messageFormRef.value.clearValidate()
    }
  }, 100)
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

      .order-message-wrapper {
        .page-title {
          font-size: 16px;
          font-weight: bold;
          color: #333;
          margin-bottom: 20px;
          padding-bottom: 10px;
          border-bottom: 2px solid #e5e5e5;
        }

        .message-form {
          .form-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;

            tr {
              td {
                padding: 12px 0;
                vertical-align: top;
              }

              .label-cell {
                width: 120px;
                text-align: right;
                padding-right: 15px;
                font-size: 14px;
                color: #333;

                .required {
                  color: #e4393c;
                  margin-right: 4px;
                }
              }

              .input-cell {
                text-align: left;

                .form-input {
                  width: 100%;
                  max-width: 400px;
                }

                .amount-input {
                  max-width: 200px;
                }

                .unit {
                  margin-left: 8px;
                  color: #666;
                  font-size: 14px;
                }

                .date-picker {
                  margin-right: 10px;
                }

                .time-select {
                  margin-right: 5px;
                }

                .time-separator {
                  margin: 0 5px;
                  color: #666;
                  font-size: 14px;
                }

                .form-textarea {
                  width: 100%;
                  max-width: 600px;
                }
              }
            }
          }

          .form-actions {
            margin-top: 30px;
            padding-left: 135px;

            .el-button {
              padding: 10px 30px;
              font-size: 14px;
              margin-right: 15px;
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
        .order-message-wrapper {
          .message-form {
            .form-table {
              .label-cell {
                width: 100px;
                font-size: 13px;
              }

              .input-cell {
                .form-input,
                .form-textarea {
                  max-width: 100%;
                }
              }
            }

            .form-actions {
              padding-left: 0;
              text-align: center;
            }
          }
        }
      }
    }
  }
}
</style>

