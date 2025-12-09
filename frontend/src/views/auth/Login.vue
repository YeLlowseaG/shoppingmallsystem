<template>
  <div class="login-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 登录内容区域 -->
    <div class="login-content">
      <div class="container">
        <div class="login-wrapper">
          <!-- 登录表单 -->
          <div class="login-form-section">
            <h2 class="section-title">已注册用户,请登录</h2>
            <p class="section-subtitle">如果您已是本站会员,请登录</p>
            
            <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="rules"
              label-width="0"
              class="login-form"
            >
              <el-form-item prop="username">
                <table class="form-table">
                  <tr>
                    <td class="label-cell">
                      <span class="required">*</span>用户名：
                    </td>
                    <td class="input-cell">
                      <el-input
                        v-model="loginForm.username"
                        placeholder="请输入用户名"
                        class="form-input"
                        clearable
                      />
                    </td>
                    <td class="link-cell">
                      <router-link to="/register" class="register-link">立即注册</router-link>
                    </td>
                  </tr>
                </table>
              </el-form-item>

              <el-form-item prop="password">
                <table class="form-table">
                  <tr>
                    <td class="label-cell">
                      <span class="required">*</span>密码：
                    </td>
                    <td class="input-cell">
                      <el-input
                        v-model="loginForm.password"
                        type="password"
                        placeholder="请输入密码"
                        class="form-input"
                        show-password
                        @keyup.enter="handleLogin"
                      />
                    </td>
                    <td class="link-cell">
                      <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
                    </td>
                  </tr>
                </table>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  :loading="loading"
                  @click="handleLogin"
                  class="login-button"
                  size="large"
                >
                  立即登录
                </el-button>
              </el-form-item>
            </el-form>
          </div>
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
import { login as loginApi, type LoginDTO } from '@/api/buyer/user'
import { useUserStore } from '@/stores/user'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive<LoginDTO>({
  username: '',
  password: ''
})

// 验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
}

// 登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const result = await loginApi(loginForm)
        
        // 保存token和用户信息
        userStore.setToken(result.token)
        userStore.setUserInfo(result.userInfo)
        
        ElMessage.success('登录成功')
        
        // 跳转到原目标页面或首页
        const redirect = (route.query.redirect as string) || '/'
        router.push(redirect)
      } catch (error: any) {
        // 错误提示已在响应拦截器中处理，这里不需要重复显示
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 如果已登录，跳转到首页
onMounted(() => {
  if (userStore.isLoggedIn()) {
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  }
})
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.login-content {
  background: #fff;
  padding: 40px 0 60px;
  min-height: 500px;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }

  .login-wrapper {
    display: flex;
    justify-content: center;
    align-items: flex-start;
  }

  // 登录表单
  .login-form-section {
    width: 100%;
    max-width: 500px;

    .section-title {
      font-size: 20px;
      font-weight: bold;
      color: #333;
      margin-bottom: 8px;
    }

    .section-subtitle {
      font-size: 14px;
      color: #666;
      margin-bottom: 30px;
    }

    .login-form {
      .form-table {
        width: 100%;
        border-collapse: collapse;
        margin-bottom: 20px;

        .label-cell {
          width: 100px;
          padding: 8px 0;
          vertical-align: middle;
          font-size: 14px;
          color: #333;
          text-align: right;
          padding-right: 10px;

          .required {
            color: #e4393c;
            margin-right: 4px;
          }
        }

        .input-cell {
          padding: 8px 0;
          vertical-align: middle;

          .form-input {
            width: 280px;
          }
        }

        .link-cell {
          padding: 8px 0;
          padding-left: 10px;
          vertical-align: middle;

          .register-link,
          .forgot-link {
            font-size: 12px;
            color: #666;
            text-decoration: none;

            &:hover {
              color: #e4393c;
              text-decoration: underline;
            }
          }
        }
      }

      .login-button {
        width: 100%;
        height: 50px;
        background: linear-gradient(135deg, #ff8c00 0%, #ff6b00 100%);
        border: none;
        font-size: 18px;
        font-weight: bold;
        color: #fff;
        border-radius: 4px;
        transition: all 0.3s;
        margin-top: 10px;

        &:hover {
          background: linear-gradient(135deg, #ff7a00 0%, #ff5a00 100%);
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(255, 140, 0, 0.4);
        }
      }
    }
  }

}

// 响应式设计
@media (max-width: 768px) {
  .login-content {
    .login-form-section {
      max-width: 100%;
    }
  }
}
</style>
