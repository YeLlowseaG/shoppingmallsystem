<template>
  <div class="reset-password-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 主内容区域 -->
    <div class="reset-password-content">
      <div class="container">
        <h2 class="page-title">重置密码</h2>
        <p class="description">请输入您的新密码，完成密码重置</p>

        <!-- 重置密码表单 -->
        <div class="reset-password-form-box">
          <el-form
            ref="resetPasswordFormRef"
            :model="resetPasswordForm"
            :rules="rules"
            class="reset-password-form"
          >
            <!-- 新密码 -->
            <div class="form-row">
              <div class="form-label">
                <span class="required">*</span>新密码:
              </div>
              <div class="form-input-wrapper">
                <el-input
                  v-model="resetPasswordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  class="form-input"
                  show-password
                />
              </div>
            </div>

            <!-- 确认密码 -->
            <div class="form-row">
              <div class="form-label">
                <span class="required">*</span>确认密码:
              </div>
              <div class="form-input-wrapper">
                <el-input
                  v-model="resetPasswordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  class="form-input"
                  show-password
                  @keyup.enter="handleSubmit"
                />
              </div>
            </div>

            <!-- 提交按钮 -->
            <div class="form-row">
              <div class="form-label"></div>
              <div class="form-input-wrapper">
                <el-button
                  :loading="loading"
                  @click="handleSubmit"
                  class="submit-button"
                >
                  提交
                </el-button>
              </div>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { resetPassword as resetPasswordApi, type ResetPasswordDTO } from '@/api/buyer/user'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'

const router = useRouter()
const route = useRoute()

const resetPasswordFormRef = ref<FormInstance>()
const loading = ref(false)

const resetPasswordForm = reactive<ResetPasswordDTO>({
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证规则
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== resetPasswordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 页面加载时获取验证码
onMounted(() => {
  const code = route.query.code as string
  if (!code) {
    ElMessage.error('验证码参数缺失，请重新申请密码重置')
    setTimeout(() => {
      router.push('/forgot-password')
    }, 2000)
    return
  }
  resetPasswordForm.code = code
})

// 提交表单
const handleSubmit = async () => {
  if (!resetPasswordFormRef.value) return

  await resetPasswordFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      resetPasswordApi(resetPasswordForm)
        .then(() => {
          ElMessage.success('密码重置成功，请使用新密码登录')
          setTimeout(() => {
            router.push('/login')
          }, 2000)
        })
        .catch((error) => {
          // 错误提示已在响应拦截器中处理
          console.error('密码重置失败:', error)
        })
        .finally(() => {
          loading.value = false
        })
    }
  })
}
</script>

<style scoped lang="scss">
.reset-password-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.reset-password-content {
  padding: 30px 0 60px;
  background: #f5f5f5;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }

  .page-title {
    text-align: center;
    font-size: 24px;
    color: #333;
    margin-bottom: 15px;
    font-weight: bold;
  }

  .description {
    text-align: center;
    color: #666;
    margin-bottom: 30px;
    font-size: 14px;
    line-height: 1.6;
  }
}

.reset-password-form-box {
  max-width: 600px;
  margin: 0 auto;
  background: white;
  padding: 40px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.reset-password-form {
  .form-row {
    display: flex;
    align-items: flex-start;
    margin-bottom: 20px;

    .form-label {
      width: 140px;
      padding-top: 8px;
      text-align: right;
      padding-right: 15px;
      color: #333;
      font-size: 14px;
      flex-shrink: 0;

      .required {
        color: #e4393c;
        margin-right: 4px;
      }
    }

    .form-input-wrapper {
      flex: 1;

      .form-input {
        width: 100%;
        max-width: 300px;

        :deep(.el-input__wrapper) {
          border-radius: 4px;
        }
      }

      .submit-button {
        width: 200px;
        height: 40px;
        font-size: 16px;
        background: #ff6600;
        border: none;
        border-radius: 4px;
        color: #fff;
        font-weight: normal;
        transition: all 0.3s;

        &:hover {
          background: #ff8533;
        }

        &:active {
          background: #e55a00;
        }
      }
    }
  }
}
</style>
