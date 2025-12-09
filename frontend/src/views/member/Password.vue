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
          <MemberSidebar active-menu="settings/password" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <!-- 标签页 -->
            <el-tabs v-model="activeTab" class="security-tabs">
              <!-- 密码修改标签页 -->
              <el-tab-pane label="密码修改" name="password">
                <div class="form-container">
                  <el-form
                    ref="passwordFormRef"
                    :model="passwordForm"
                    :rules="passwordRules"
                    label-width="120px"
                    class="password-form"
                  >
                    <el-form-item label="旧密码:" prop="oldPassword">
                      <el-input
                        v-model="passwordForm.oldPassword"
                        type="password"
                        placeholder="请输入旧密码"
                        show-password
                        style="width: 300px"
                      />
                    </el-form-item>

                    <el-form-item label="新密码:" prop="newPassword">
                      <el-input
                        v-model="passwordForm.newPassword"
                        type="password"
                        placeholder="请输入新密码"
                        show-password
                        style="width: 300px"
                      />
                    </el-form-item>

                    <el-form-item label="确认新密码:" prop="confirmPassword">
                      <el-input
                        v-model="passwordForm.confirmPassword"
                        type="password"
                        placeholder="请再次输入新密码"
                        show-password
                        style="width: 300px"
                      />
                    </el-form-item>

                    <el-form-item>
                      <el-button
                        type="danger"
                        :loading="passwordLoading"
                        @click="handlePasswordSubmit"
                      >
                        保存
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </el-tab-pane>

              <!-- 安全问题标签页 -->
              <el-tab-pane label="安全问题" name="security">
                <div class="form-container">
                  <el-form
                    ref="securityFormRef"
                    :model="securityForm"
                    :rules="securityRules"
                    label-width="120px"
                    class="security-form"
                  >
                    <el-form-item label="安全问题:" prop="securityQuestion">
                      <el-input
                        v-model="securityForm.securityQuestion"
                        placeholder="请输入安全问题"
                        style="width: 300px"
                      />
                    </el-form-item>

                    <el-form-item label="回答:" prop="securityAnswer">
                      <el-input
                        v-model="securityForm.securityAnswer"
                        placeholder="请输入回答"
                        style="width: 300px"
                      />
                    </el-form-item>

                    <el-form-item>
                      <el-button
                        type="danger"
                        :loading="securityLoading"
                        @click="handleSecuritySubmit"
                      >
                        保存
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { changePassword, getUserInfo, updateUserInfo, type UserInfoDTO } from '@/api/buyer/user'

// 当前激活的标签页
const activeTab = ref('password')

// 未读消息数量
const unreadMessageCount = ref(0)

// 密码修改表单
const passwordFormRef = ref<InstanceType<typeof ElForm>>()
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordLoading = ref(false)

// 密码修改验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 安全问题表单
const securityFormRef = ref<InstanceType<typeof ElForm>>()
const securityForm = ref({
  securityQuestion: '',
  securityAnswer: ''
})

const securityLoading = ref(false)

// 安全问题验证规则
const securityRules = {
  securityQuestion: [
    { required: true, message: '请输入安全问题', trigger: 'blur' }
  ],
  securityAnswer: [
    { required: true, message: '请输入回答', trigger: 'blur' }
  ]
}

// 菜单选择逻辑已移至 MemberSidebar 组件中

// 提交密码修改
const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    passwordLoading.value = true
    try {
      await changePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword)
      ElMessage.success('密码修改成功')
      // 清空表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      passwordFormRef.value?.clearValidate()
    } catch (error) {
      console.error('密码修改失败:', error)
    } finally {
      passwordLoading.value = false
    }
  })
}

// 提交安全问题
const handleSecuritySubmit = async () => {
  if (!securityFormRef.value) return

  await securityFormRef.value.validate(async (valid) => {
    if (!valid) return

    securityLoading.value = true
    try {
      // 调用更新用户信息API保存安全问题
      const updateData: UserInfoDTO = {
        securityQuestion: securityForm.value.securityQuestion,
        securityAnswer: securityForm.value.securityAnswer
      }
      await updateUserInfo(updateData)
      ElMessage.success('安全问题保存成功')
      // 不清空表单，保留已保存的数据
      securityFormRef.value?.clearValidate()
    } catch (error) {
      console.error('安全问题保存失败:', error)
    } finally {
      securityLoading.value = false
    }
  })
}

// 加载安全问题数据
const loadSecurityInfo = async () => {
  try {
    const userInfo = await getUserInfo()
    if (userInfo) {
      securityForm.value.securityQuestion = userInfo.securityQuestion || ''
      securityForm.value.securityAnswer = userInfo.securityAnswer || ''
    }
  } catch (error) {
    console.error('加载安全问题失败:', error)
  }
}

// 页面加载时获取安全问题数据
onMounted(() => {
  loadSecurityInfo()
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
      border: 1px solid #e5e5e5;
      padding: 20px;

      // 标签页样式
      .security-tabs {
        :deep(.el-tabs__header) {
          margin-bottom: 30px;
        }

        :deep(.el-tabs__item) {
          font-size: 14px;
          padding: 0 20px;
          height: 40px;
          line-height: 40px;

          &.is-active {
            color: #e4393c;
          }
        }

        :deep(.el-tabs__active-bar) {
          background-color: #e4393c;
        }

        :deep(.el-tabs__item:hover) {
          color: #e4393c;
        }
      }

      // 表单容器
      .form-container {
        padding: 20px 0;

        .password-form,
        .security-form {
          :deep(.el-form-item) {
            margin-bottom: 25px;
          }

          :deep(.el-form-item__label) {
            font-size: 14px;
            color: #333;
            font-weight: normal;
          }

          :deep(.el-input__wrapper) {
            border-radius: 4px;
          }

          :deep(.el-button) {
            padding: 10px 30px;
            font-size: 14px;
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

