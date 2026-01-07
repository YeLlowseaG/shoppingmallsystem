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
          <MemberSidebar active-menu="settings/payment-password" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <div class="payment-password-form-wrapper">
              <!-- 标题 -->
              <h2 class="form-title">预存款支付密码修改</h2>

              <!-- 提示信息 -->
              <div class="tip-banner">
                (如未设置过支付密码,默认支付密码为您的账号登陆密码!)
              </div>

              <!-- 表单 -->
              <el-form
                ref="paymentPasswordFormRef"
                :model="paymentPasswordForm"
                :rules="rules"
                label-width="0"
                class="payment-password-form"
              >
                <!-- 原支付密码 -->
                <el-form-item prop="oldPassword">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        原支付密码:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="paymentPasswordForm.oldPassword"
                          type="password"
                          placeholder="请输入原支付密码"
                          class="form-input"
                          show-password
                          clearable
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 新支付密码 -->
                <el-form-item prop="newPassword">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        新支付密码:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="paymentPasswordForm.newPassword"
                          type="password"
                          placeholder="请输入新支付密码"
                          class="form-input"
                          show-password
                          clearable
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 确认新支付密码 -->
                <el-form-item prop="confirmPassword">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        确认新支付密码:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="paymentPasswordForm.confirmPassword"
                          type="password"
                          placeholder="请再次输入新支付密码"
                          class="form-input"
                          show-password
                          clearable
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 保存按钮 -->
                <el-form-item>
                  <table class="form-table">
                    <tr>
                      <td class="label-cell"></td>
                      <td class="input-cell">
                        <el-button
                          type="default"
                          :loading="loading"
                          @click="handleSave"
                          class="save-button"
                        >
                          保存
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { changePaymentPassword } from '@/api/buyer/user'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'

const paymentPasswordFormRef = ref<FormInstance>()
const loading = ref(false)
const unreadMessageCount = ref(0)

// 表单数据
const paymentPasswordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证规则
const rules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原支付密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新支付密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新支付密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== paymentPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 保存
const handleSave = async () => {
  if (!paymentPasswordFormRef.value) return

  await paymentPasswordFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await changePaymentPassword(
        paymentPasswordForm.oldPassword,
        paymentPasswordForm.newPassword
      )
      ElMessage.success('支付密码修改成功')
      // 清空表单
      paymentPasswordForm.oldPassword = ''
      paymentPasswordForm.newPassword = ''
      paymentPasswordForm.confirmPassword = ''
      paymentPasswordFormRef.value?.clearValidate()
    } catch (error) {
      console.error('支付密码修改失败:', error)
    } finally {
      loading.value = false
    }
  })
}
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
      border: 1px solid #e5e5e5;
      padding: 20px;

      .payment-password-form-wrapper {
        .form-title {
          font-size: 16px;
          font-weight: 500;
          color: #333;
          margin: 0 0 20px 0;
          padding-bottom: 10px;
          border-bottom: 1px solid #e5e5e5;
        }

        .tip-banner {
          background: #fffbe6;
          border: 1px solid #ffe58f;
          padding: 10px 15px;
          margin-bottom: 20px;
          color: #666;
          font-size: 13px;
          line-height: 1.5;
        }

        .payment-password-form {
          .form-table {
            width: 100%;
            border-collapse: collapse;

            tr {
              td {
                padding: 12px 0;
                vertical-align: middle;
              }

              .label-cell {
                width: 150px;
                text-align: right;
                padding-right: 15px;
                font-size: 14px;
                color: #333;
              }

              .input-cell {
                .form-input {
                  width: 300px;
                }

                .save-button {
                  padding: 10px 30px;
                  font-size: 14px;
                  background: #f5f5f5;
                  border-color: #d9d9d9;
                  color: #333;

                  &:hover {
                    background: #e6e6e6;
                    border-color: #bfbfbf;
                  }
                }
              }
            }
          }

          :deep(.el-form-item) {
            margin-bottom: 0;
          }

          :deep(.el-form-item__error) {
            padding-left: 165px;
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
    }
  }
}
</style>





































































