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
          <MemberSidebar active-menu="settings/address" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <div class="address-edit-wrapper">
              <h3 class="section-title">{{ isEdit ? '修改收货地址' : '新增收货地址' }}</h3>

              <el-form
                ref="addressFormRef"
                :model="addressForm"
                :rules="rules"
                label-width="0"
                class="address-form"
              >
                <!-- 默认收货地址 -->
                <el-form-item>
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        默认收货地址:
                      </td>
                      <td class="input-cell">
                        <el-radio-group v-model="addressForm.isDefault">
                          <el-radio :label="false">否</el-radio>
                          <el-radio :label="true">是</el-radio>
                        </el-radio-group>
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 姓名 -->
                <el-form-item prop="recipient">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        <span class="required">*</span>姓名:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="addressForm.recipient"
                          placeholder="请输入收货人姓名"
                          class="form-input"
                          clearable
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 电话 -->
                <el-form-item prop="phone">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        电话:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="addressForm.phone"
                          placeholder="请输入联系电话"
                          class="form-input"
                          clearable
                        />
                        <span class="form-hint">其中联系电话和联系手机必须填写一项</span>
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 手机 -->
                <el-form-item prop="mobile">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        手机:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="addressForm.mobile"
                          placeholder="请输入手机号码"
                          class="form-input"
                          clearable
                          maxlength="11"
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
                        <div class="region-selectors">
                          <el-select
                            v-model="addressForm.province"
                            placeholder="请选择..."
                            class="region-select"
                            @change="handleProvinceChange"
                          >
                            <el-option
                              v-for="province in provinces"
                              :key="province.value"
                              :label="province.label"
                              :value="province.value"
                            />
                          </el-select>
                          <el-select
                            v-model="addressForm.city"
                            placeholder="请选择..."
                            class="region-select"
                            :disabled="!addressForm.province"
                            @change="handleCityChange"
                          >
                            <el-option
                              v-for="city in cities"
                              :key="city.value"
                              :label="city.label"
                              :value="city.value"
                            />
                          </el-select>
                          <el-select
                            v-model="addressForm.district"
                            placeholder="请选择..."
                            class="region-select"
                            :disabled="!addressForm.city"
                          >
                            <el-option
                              v-for="district in districts"
                              :key="district.value"
                              :label="district.label"
                              :value="district.value"
                            />
                          </el-select>
                        </div>
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 地址 -->
                <el-form-item prop="address">
                  <table class="form-table">
                    <tr>
                      <td class="label-cell">
                        <span class="required">*</span>地址:
                      </td>
                      <td class="input-cell">
                        <el-input
                          v-model="addressForm.address"
                          placeholder="请输入详细地址"
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
                          v-model="addressForm.zipCode"
                          placeholder="请输入邮编"
                          class="form-input"
                          clearable
                          maxlength="6"
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 按钮 -->
                <el-form-item>
                  <table class="form-table">
                    <tr>
                      <td class="label-cell"></td>
                      <td class="input-cell">
                        <el-button
                          type="danger"
                          :loading="loading"
                          @click="handleSave"
                          class="save-button"
                        >
                          保存
                        </el-button>
                        <el-button @click="handleBack" class="back-button">
                          返回
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'

const router = useRouter()
const route = useRoute()

const addressFormRef = ref<FormInstance>()
const loading = ref(false)
const unreadMessageCount = ref(0)

// 判断是编辑还是新增
const isEdit = computed(() => {
  return !!route.query.id
})

// 表单数据
const addressForm = reactive({
  isDefault: false,
  recipient: '',
  phone: '',
  mobile: '',
  province: '',
  city: '',
  district: '',
  address: '',
  zipCode: ''
})

// 验证规则
const validatePhoneOrMobile = (rule: any, value: any, callback: any) => {
  if (!addressForm.phone && !addressForm.mobile) {
    callback(new Error('联系电话和手机号码必须填写一项'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  recipient: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhoneOrMobile, trigger: 'blur' }
  ],
  mobile: [
    { validator: validatePhoneOrMobile, trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 地区数据（简化版，实际应该从后端获取）
const provinces = ref([
  { label: '北京市', value: '北京' },
  { label: '上海市', value: '上海' },
  { label: '广东省', value: '广东' },
  { label: '浙江省', value: '浙江' },
  { label: '江苏省', value: '江苏' },
  { label: '陕西省', value: '陕西' },
  { label: '山东省', value: '山东' },
  { label: '河南省', value: '河南' },
  { label: '四川省', value: '四川' },
  { label: '湖北省', value: '湖北' }
])

const cities = ref<Array<{ label: string; value: string }>>([])
const districts = ref<Array<{ label: string; value: string }>>([])

// 简化版地区数据
const regionData: Record<string, Record<string, string[]>> = {
  '北京': {
    '北京市': ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '石景山区']
  },
  '上海': {
    '上海市': ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区']
  },
  '广东': {
    '广州市': ['越秀区', '海珠区', '天河区', '白云区', '番禺区', '花都区'],
    '深圳市': ['罗湖区', '福田区', '南山区', '宝安区', '龙岗区', '盐田区']
  },
  '陕西': {
    '西安市': ['雁塔区', '碑林区', '莲湖区', '新城区', '未央区', '灞桥区']
  }
}

const handleProvinceChange = () => {
  addressForm.city = ''
  addressForm.district = ''
  const provinceData = regionData[addressForm.province] || {}
  cities.value = Object.keys(provinceData).map(city => ({ label: city, value: city }))
  districts.value = []
}

const handleCityChange = () => {
  addressForm.district = ''
  const provinceData = regionData[addressForm.province] || {}
  const cityData = provinceData[addressForm.city] || []
  districts.value = cityData.map(district => ({ label: district, value: district }))
}

// 菜单选择逻辑已移至 MemberSidebar 组件中

// 保存收货地址
const handleSave = async () => {
  if (!addressFormRef.value) return

  await addressFormRef.value.validate(async (valid) => {
    if (valid) {
      // 验证地区是否完整
      if (!addressForm.province || !addressForm.city || !addressForm.district) {
        ElMessage.warning('请完整选择地区')
        return
      }

      // 验证电话或手机至少填写一项
      if (!addressForm.phone && !addressForm.mobile) {
        ElMessage.warning('联系电话和手机号码必须填写一项')
        return
      }

      loading.value = true
      try {
        // TODO: 调用后端API保存收货地址
        ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
        router.push('/member/settings/address')
      } catch (error: any) {
        console.error('保存收货地址失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 返回
const handleBack = () => {
  router.push('/member/settings/address')
}

// 加载收货地址数据（编辑时）
const loadAddressData = async () => {
  if (!isEdit.value) return

  try {
    const addressId = route.query.id as string
    // TODO: 调用后端API获取收货地址详情
    // const addressData = await getAddressById(addressId)
    // 填充表单数据
    // addressForm.recipient = addressData.recipient
    // ...
  } catch (error) {
    console.error('加载收货地址失败:', error)
  }
}

// 初始化
onMounted(() => {
  loadAddressData()
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

      .address-edit-wrapper {
        .section-title {
          font-size: 16px;
          font-weight: bold;
          color: #333;
          margin: 0 0 20px 0;
        }

        .address-form {
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

              .form-hint {
                font-size: 12px;
                color: #999;
                margin-left: 10px;
              }

              .region-selectors {
                display: flex;
                gap: 10px;

                .region-select {
                  width: 150px;
                }
              }

              .save-button {
                width: 120px;
                height: 40px;
                background: #e4393c;
                border-color: #e4393c;
                font-size: 16px;
                font-weight: bold;
                margin-right: 10px;

                &:hover {
                  background: #c9302c;
                  border-color: #c9302c;
                }
              }

              .back-button {
                width: 120px;
                height: 40px;
                font-size: 16px;
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
        .address-edit-wrapper {
          .address-form {
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

