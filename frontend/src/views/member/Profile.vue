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
          <MemberSidebar active-menu="settings/profile" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <div class="profile-form-wrapper">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="rules"
                label-width="0"
                class="profile-form"
              >
                <!-- 货币 -->
                <el-form-item>
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        货币:
                      </td>
                      <td class="input-cell">
                        <el-select v-model="profileForm.currency" class="form-select" style="width: 200px;">
                          <el-option label="人民币" value="CNY" />
                        </el-select>
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 电子邮箱 -->
                <el-form-item prop="email">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        <span class="required">*</span>电子邮箱:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="profileForm.email"
                          placeholder="请输入电子邮箱"
                          class="form-input"
                          clearable
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 姓名 -->
                <el-form-item prop="realName">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        <span class="required">*</span>姓名:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="profileForm.realName"
                          placeholder="请输入姓名"
                          class="form-input"
                          clearable
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 性别 -->
                <el-form-item prop="gender">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        <span class="required">*</span>性别:
                      </td>
                      <td class="input-cell">
                        <el-radio-group v-model="profileForm.gender">
                          <el-radio :label="1">男</el-radio>
                          <el-radio :label="2">女</el-radio>
                        </el-radio-group>
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 出生日期 -->
                <el-form-item>
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        出生日期:
                      </td>
                      <td class="input-cell">
                        <el-date-picker
                          v-model="profileForm.birthday"
                          type="date"
                          placeholder="请选择出生日期"
                          format="YYYY-MM-DD"
                          value-format="YYYY-MM-DD"
                          class="form-input"
                          style="width: 200px;"
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 地区 -->
                <el-form-item prop="region">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        <span class="required">*</span>地区:
                      </td>
                      <td class="input-cell">
                        <RegionSelector
                          v-model="regionData"
                          @change="handleRegionChange"
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 联系地址 -->
                <el-form-item prop="address">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        <span class="required">*</span>联系地址:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="profileForm.address"
                          placeholder="请输入联系地址"
                          class="form-input"
                          clearable
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 邮编 -->
                <el-form-item>
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        邮编:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="profileForm.zipCode"
                          placeholder="请输入邮编"
                          class="form-input"
                          clearable
                          maxlength="6"
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 移动电话 -->
                <el-form-item prop="phone">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        <span class="required">*</span>移动电话:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="profileForm.phone"
                          placeholder="请输入移动电话"
                          class="form-input"
                          clearable
                          maxlength="11"
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 固定电话 -->
                <el-form-item>
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        固定电话:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="profileForm.fixedPhone"
                          placeholder="请输入固定电话"
                          class="form-input"
                          clearable
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 安全问题 -->
                <el-form-item>
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        安全问题:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="profileForm.securityQuestion"
                          placeholder="请输入安全问题"
                          class="form-input"
                          clearable
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 回答 -->
                <el-form-item>
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        回答:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="profileForm.securityAnswer"
                          placeholder="请输入安全问题的回答"
                          class="form-input"
                          clearable
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 旺旺 -->
                <el-form-item>
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        旺旺:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="profileForm.wangwang"
                          placeholder="请输入旺旺账号"
                          class="form-input"
                          clearable
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 运营人员 -->
                <el-form-item prop="operator">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        <span class="required">*</span>运营人员:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="profileForm.operator"
                          placeholder="请输入运营人员"
                          class="form-input"
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
                          type="primary"
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { getUserInfo, updateUserInfo, type UserInfoVO, type UserInfoDTO } from '@/api/buyer/user'
import { useUserStore } from '@/stores/user'
import { getProvinces, getChildrenByParentId } from '@/api/common/region'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import RegionSelector from '@/components/common/RegionSelector.vue'

const userStore = useUserStore()
const profileFormRef = ref<FormInstance>()
const loading = ref(false)
const unreadMessageCount = ref(0)

// 表单数据
const profileForm = reactive({
  currency: 'CNY',
  email: '',
  realName: '',
  gender: 1,
  birthday: '',
  province: '',
  city: '',
  district: '',
  address: '',
  zipCode: '',
  phone: '',
  fixedPhone: '',
  securityQuestion: '',
  securityAnswer: '',
  wangwang: '',
  operator: ''
})

// 验证规则
const rules: FormRules = {
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  // 地区验证在保存时手动检查
  address: [
    { required: true, message: '请输入联系地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入移动电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  operator: [
    { required: true, message: '请输入运营人员', trigger: 'blur' }
  ]
}

// 地区选择器数据
const regionData = ref<{
  provinceId?: number;
  cityId?: number;
  districtId?: number;
}>({})

// 地区选择器change事件处理
const handleRegionChange = (value: {
  provinceId?: number;
  cityId?: number;
  districtId?: number;
  provinceName?: string;
  cityName?: string;
  districtName?: string;
}) => {
  profileForm.province = value.provinceName || ''
  profileForm.city = value.cityName || ''
  profileForm.district = value.districtName || ''
}

// 根据名称查找地区ID
const loadRegionIdsByName = async (provinceName: string, cityName: string, districtName: string) => {
  try {
    // 1. 查找省份ID
    const provinces = await getProvinces()
    const province = provinces.find(p => p.name === provinceName)
    if (!province) {
      console.warn('未找到省份:', provinceName)
      return
    }
    
    // 2. 查找城市ID
    const cities = await getChildrenByParentId(province.id)
    const city = cities.find(c => c.name === cityName)
    if (!city) {
      console.warn('未找到城市:', cityName)
      return
    }
    
    // 3. 查找区县ID
    const districts = await getChildrenByParentId(city.id)
    const district = districts.find(d => d.name === districtName)
    if (!district) {
      console.warn('未找到区县:', districtName)
      return
    }
    
    // 4. 设置regionData
    regionData.value = {
      provinceId: province.id,
      cityId: city.id,
      districtId: district.id
    }
  } catch (error) {
    console.error('加载地区ID失败:', error)
    // 失败时不影响表单数据，用户仍可以重新选择
  }
}

// 菜单选择逻辑已移至 MemberSidebar 组件中

// 保存个人信息
const handleSave = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      // 验证地区是否完整
      if (!profileForm.province || !profileForm.city || !profileForm.district) {
        ElMessage.warning('请完整选择地区')
        return
      }

      loading.value = true
      try {
        // 构建提交数据
        const updateData: UserInfoDTO = {
          email: profileForm.email,
          realName: profileForm.realName,
          gender: profileForm.gender,
          phone: profileForm.phone,
          province: profileForm.province,
          city: profileForm.city,
          district: profileForm.district,
          address: profileForm.address,
          birthday: profileForm.birthday || undefined,
          zipCode: profileForm.zipCode || undefined,
          fixedPhone: profileForm.fixedPhone || undefined,
          securityQuestion: profileForm.securityQuestion || undefined,
          securityAnswer: profileForm.securityAnswer || undefined,
          wangwang: profileForm.wangwang || undefined,
          operator: profileForm.operator || undefined
        }

        await updateUserInfo(updateData)
        ElMessage.success('个人信息保存成功')
        
        // 更新本地用户信息
        if (userStore.userInfo) {
          userStore.setUserInfo({
            ...userStore.userInfo,
            email: profileForm.email,
            realName: profileForm.realName,
            gender: profileForm.gender,
            phone: profileForm.phone
          })
        }

        // 重新加载用户信息
        await loadUserInfo()
      } catch (error: any) {
        console.error('保存个人信息失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const userInfo: UserInfoVO = await getUserInfo()
    
    // 填充表单数据
    profileForm.email = userInfo.email || ''
    profileForm.realName = userInfo.realName || ''
    profileForm.gender = userInfo.gender || 1
    profileForm.phone = userInfo.phone || ''
    // 处理日期格式（后端返回的可能是LocalDate格式的字符串）
    if (userInfo.birthday) {
      profileForm.birthday = String(userInfo.birthday)
    }
    profileForm.address = userInfo.address || ''
    profileForm.zipCode = userInfo.zipCode || ''
    profileForm.fixedPhone = userInfo.fixedPhone || ''
    profileForm.securityQuestion = userInfo.securityQuestion || ''
    profileForm.securityAnswer = userInfo.securityAnswer || ''
    profileForm.wangwang = userInfo.wangwang || ''
    profileForm.operator = userInfo.operator || ''
    
    // 设置地区信息
    if (userInfo.province && userInfo.city && userInfo.district) {
      profileForm.province = userInfo.province
      profileForm.city = userInfo.city
      profileForm.district = userInfo.district
      // 根据名称查找地区ID，用于初始化RegionSelector组件
      await loadRegionIdsByName(userInfo.province, userInfo.city, userInfo.district)
    }
  } catch (error: any) {
    console.error('加载用户信息失败:', error)
    // 如果加载失败，使用store中的信息作为fallback
    const userInfo = userStore.userInfo
    if (userInfo) {
      profileForm.email = userInfo.email || ''
      profileForm.realName = userInfo.realName || ''
      profileForm.gender = userInfo.gender || 1
      profileForm.phone = userInfo.phone || ''
    }
  }
}

// 初始化表单数据
onMounted(() => {
  loadUserInfo()
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

      .profile-form-wrapper {
        .profile-form {
          :deep(.el-form-item) {
            margin-bottom: 4px;
          }

          .form-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 0;

            .label-cell {
              width: 120px;
              padding: 2px 0;
              vertical-align: middle;
              font-size: 14px;
              color: #333;
              text-align: right;
              padding-right: 15px;

              .required {
                color: #e4393c;
                margin-right: 4px;
              }
            }

            .input-cell {
              padding: 2px 0;
              vertical-align: middle;

              .form-input {
                width: 300px;
              }

              .form-select {
                width: 200px;
              }


              .save-button {
                width: 120px;
                height: 40px;
                background: #e4393c;
                border-color: #e4393c;
                font-size: 16px;
                font-weight: bold;

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
        .profile-form-wrapper {
          .profile-form {
            .form-table {
              .input-cell {
                .form-input,
                .form-select {
                  width: 100%;
                }

                .region-selectors {
                  flex-direction: column;

                  .region-select {
                    width: 100%;
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
</style>

