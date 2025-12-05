<template>
  <div class="forgot-password-container">
    <div class="forgot-password-box">
      <h2>忘记密码？</h2>
      <p class="description">如果忘记密码，请填写下面表单来重新获取密码</p>
      
      <el-form
        ref="forgotPasswordFormRef"
        :model="forgotPasswordForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="forgotPasswordForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit" style="width: 100%">
            提交
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-link type="primary" @click="goToLogin">返回登录</el-link>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { forgotPassword as forgotPasswordApi, type ForgotPasswordDTO } from '@/api/buyer/user'

const router = useRouter()

const forgotPasswordFormRef = ref<FormInstance>()
const loading = ref(false)

const forgotPasswordForm = reactive<ForgotPasswordDTO>({
  username: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!forgotPasswordFormRef.value) return

  await forgotPasswordFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      forgotPasswordApi(forgotPasswordForm)
        .then(() => {
          ElMessage.success('密码重置信息已发送到您的邮箱，请查收')
          setTimeout(() => {
            router.push('/login')
          }, 2000)
        })
        .catch((error) => {
          if (error.message && error.message.includes('不存在')) {
            // 用户不存在的情况，显示错误提示
            ElMessage.error('该用户不存在！')
            setTimeout(() => {
              router.push('/login')
            }, 2000)
          } else {
            ElMessage.error(error.message || '操作失败')
          }
        })
        .finally(() => {
          loading.value = false
        })
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f5f5;
}

.forgot-password-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 10px;
  color: #333;
}

.description {
  text-align: center;
  color: #666;
  margin-bottom: 30px;
  font-size: 14px;
}
</style>

