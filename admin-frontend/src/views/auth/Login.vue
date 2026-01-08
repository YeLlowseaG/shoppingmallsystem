<template>
  <div class="login-container">
    <div class="login-box">
      <h2>管理员登录</h2>
      <p class="description">平台管理员登录系统</p>
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
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin/user'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { addRoutes } from '@/router'

const router = useRouter()
const adminStore = useAdminStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
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

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { adminLogin } = await import('@/api/admin/user')
        const response = await adminLogin({
          username: loginForm.username,
          password: loginForm.password
        })
        
        // 调试日志：检查返回的数据结构
        console.log('登录响应数据:', response)
        
        adminStore.setToken(response.token)
        adminStore.setAdminInfo(response.adminInfo)
        
        // 设置菜单和权限（确保是数组，即使是空数组）
        const menus = response.menus || []
        const permissions = response.permissions || []
        adminStore.setMenus(menus)
        adminStore.setPermissions(permissions)
        
        // 调试日志：检查菜单和权限
        console.log('登录成功 - 菜单数量:', menus.length, '权限数量:', permissions.length)
        
        // 等待一下确保 store 状态已更新
        await new Promise(resolve => setTimeout(resolve, 50))
        
        // 检查用户是否有角色和权限
        if (!menus || menus.length === 0 || !permissions || permissions.length === 0) {
          // 没有权限时，只显示一个警告消息，不显示"登录成功"
          ElMessage.warning('您还没有分配角色，请联系管理员分配角色和权限')
          // 跳转到dashboard，但dashboard会检查权限，如果没有权限会显示友好提示
          router.push('/admin/dashboard')
        } else {
          // 有权限时，显示成功消息并跳转
          ElMessage.success('登录成功')
          // 动态添加路由
          console.log('登录成功 - 开始添加路由...')
          addRoutes(menus)
          // 等待路由添加完成后再跳转
          await new Promise(resolve => setTimeout(resolve, 300))
          console.log('登录成功 - 路由已添加，准备跳转到 dashboard')
          // 使用 replace 而不是 push，避免在历史记录中留下登录页
          router.replace('/admin/dashboard').catch((err) => {
            console.error('跳转失败:', err)
            // 如果跳转失败，尝试使用 push
            router.push('/admin/dashboard')
          })
        }
      } catch (error: any) {
        // 错误提示已在响应拦截器中处理，这里不需要重复显示
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #304156;
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
</style>

