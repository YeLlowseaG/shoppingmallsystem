<template>
  <div class="login-container">
    <div class="login-box">
      <h2>已注册用户, 请登录</h2>
      <p class="description">如果您已是本站会员, 请登录</p>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">
            立即登录
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <div class="register-link">
            <el-link type="primary" @click="goToRegister">立即注册</el-link>
            <el-link type="primary" @click="goToForgotPassword" style="float: right">忘记密码？</el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { login as loginApi, type LoginDTO } from '@/api/buyer/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive<LoginDTO>({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      loginApi(loginForm)
        .then((response) => {
          // 保存Token和用户信息
          userStore.setToken(response.token)
          userStore.setUserInfo(response.userInfo)
          ElMessage.success('登录成功')
          router.push('/')
        })
        .catch((error) => {
          ElMessage.error(error.message || '登录失败')
        })
        .finally(() => {
          loading.value = false
        })
    }
  })
}

const goToRegister = () => {
  router.push('/register-agreement')
}

const goToForgotPassword = () => {
  router.push('/forgot-password')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}

.login-box {
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

.register-link {
  width: 100%;
  text-align: center;
}
</style>

