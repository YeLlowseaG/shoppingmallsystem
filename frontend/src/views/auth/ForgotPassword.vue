<template>
  <div class="forgot-password-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 主内容区域 -->
    <div class="forgot-password-content">
      <div class="container">
        <h2 class="page-title">取回密码</h2>
        <p class="description">说明:当你填写邮箱或手机号并提交后,密码会自动发到您注册的邮箱或手机,请及时查收,取回密码!</p>

        <!-- 忘记密码表单 -->
        <div class="forgot-password-form-box">
          <el-form
            ref="forgotPasswordFormRef"
            :model="forgotPasswordForm"
            :rules="rules"
            class="forgot-password-form"
          >
            <!-- 用户名 -->
            <div class="form-row">
              <div class="form-label">
                <span class="required">*</span>您的用户名:
              </div>
              <div class="form-input-wrapper">
                <el-input
                  v-model="forgotPasswordForm.username"
                  placeholder="请输入用户名"
                  class="form-input"
                />
              </div>
            </div>

            <!-- 邮箱 -->
            <div class="form-row">
              <div class="form-label">
                请输入您的邮箱:
              </div>
              <div class="form-input-wrapper">
                <el-input
                  v-model="forgotPasswordForm.email"
                  placeholder="请输入邮箱"
                  class="form-input"
                />
              </div>
            </div>

            <!-- 手机号 -->
            <div class="form-row">
              <div class="form-label">
                请输入您的手机号:
              </div>
              <div class="form-input-wrapper">
                <el-input
                  v-model="forgotPasswordForm.phone"
                  placeholder="请输入手机号"
                  class="form-input"
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { forgotPassword as forgotPasswordApi, type ForgotPasswordDTO } from '@/api/buyer/user'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'

const router = useRouter()

const forgotPasswordFormRef = ref<FormInstance>()
const loading = ref(false)

const forgotPasswordForm = reactive<ForgotPasswordDTO>({
  username: '',
  email: '',
  phone: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

// 提交表单
const handleSubmit = async () => {
  if (!forgotPasswordFormRef.value) return

  // 验证至少填写邮箱或手机号之一
  if (!forgotPasswordForm.email && !forgotPasswordForm.phone) {
    ElMessage.warning('请至少填写邮箱或手机号之一')
    return
  }

  await forgotPasswordFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      forgotPasswordApi(forgotPasswordForm)
        .then(() => {
          ElMessage.success('密码重置信息已发送到您的邮箱或手机，请查收')
          setTimeout(() => {
            router.push('/login')
          }, 2000)
        })
        .catch((error) => {
          // 错误提示已在响应拦截器中处理，这里不需要重复显示
          // 但需要处理特殊业务逻辑（如跳转）
          if (error.message && error.message.includes('不存在')) {
            // 用户不存在时，响应拦截器已显示错误，这里只处理跳转逻辑
            setTimeout(() => {
              router.push('/login')
            }, 2000)
          }
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
.forgot-password-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.forgot-password-content {
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

.forgot-password-form-box {
  max-width: 600px;
  margin: 0 auto;
  background: white;
  padding: 40px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.forgot-password-form {
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
        background: #999;
        border: none;
        border-radius: 4px;
        color: #fff;
        font-weight: normal;
        transition: all 0.3s;

        &:hover {
          background: #888;
        }

        &:active {
          background: #777;
        }
      }
    }
  }
}
</style>
