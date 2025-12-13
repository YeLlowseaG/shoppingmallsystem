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
                <el-form-item prop="recipient" class="form-item-inline">
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
                <el-form-item prop="phone" class="form-item-inline">
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
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 手机 -->
                <el-form-item prop="mobile" class="form-item-inline">
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
                        <RegionSelector
                          v-model="regionData"
                          @change="handleRegionChange"
                        />
                      </td>
                    </tr>
                  </table>
                </el-form-item>

                <!-- 地址 -->
                <el-form-item prop="address" class="form-item-inline">
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
import RegionSelector from '@/components/common/RegionSelector.vue'
import { getAddressById, addAddress, updateAddress, type AddressDTO } from '@/api/buyer/address'
import { getProvinces, getChildrenByParentId } from '@/api/common/region'

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
  addressForm.province = value.provinceName || ''
  addressForm.city = value.cityName || ''
  addressForm.district = value.districtName || ''
}

// 菜单选择逻辑已移至 MemberSidebar 组件中

// 保存收货地址
const handleSave = async () => {
  if (!addressFormRef.value) return

  await addressFormRef.value.validate(async (valid) => {
    if (valid) {
      // 验证地区是否完整（检查addressForm中的值，因为RegionSelector的change事件会更新这些值）
      // 如果addressForm已经有值（编辑时加载的数据），说明地区已选择
      const hasProvince = addressForm.province && addressForm.province.trim() !== ''
      const hasCity = addressForm.city && addressForm.city.trim() !== ''
      const hasDistrict = addressForm.district && addressForm.district.trim() !== ''
      
      if (!hasProvince || !hasCity || !hasDistrict) {
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
        const addressDTO: AddressDTO = {
          recipient: addressForm.recipient,
          phone: addressForm.phone || undefined,
          mobile: addressForm.mobile || undefined,
          province: addressForm.province,
          city: addressForm.city,
          district: addressForm.district,
          address: addressForm.address,
          zipCode: addressForm.zipCode || undefined,
          isDefault: addressForm.isDefault
        }

        if (isEdit.value) {
          const addressId = Number(route.query.id)
          await updateAddress(addressId, addressDTO)
          ElMessage.success('修改成功')
        } else {
          await addAddress(addressDTO)
          ElMessage.success('新增成功')
        }
        router.push('/member/settings/address')
      } catch (error: any) {
        console.error('保存收货地址失败:', error)
        ElMessage.error(error.message || '保存失败')
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
    const addressId = Number(route.query.id)
    const addressData = await getAddressById(addressId)
    
    // 填充表单数据
    addressForm.recipient = addressData.recipient || ''
    addressForm.phone = addressData.phone || ''
    addressForm.mobile = addressData.mobile || ''
    addressForm.province = addressData.province || ''
    addressForm.city = addressData.city || ''
    addressForm.district = addressData.district || ''
    addressForm.address = addressData.address || ''
    addressForm.zipCode = addressData.zipCode || ''
    addressForm.isDefault = addressData.isDefault || false

    // 根据名称查找ID，设置到regionData中
    if (addressData.province && addressData.city && addressData.district) {
      await loadRegionIdsByName(addressData.province, addressData.city, addressData.district)
    }
  } catch (error: any) {
    console.error('加载收货地址失败:', error)
    ElMessage.error(error.message || '加载收货地址失败')
    router.push('/member/settings/address')
  }
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

          // 内联布局的表单项，错误信息显示在右侧
          :deep(.form-item-inline) {
            .el-form-item__content {
              display: flex;
              align-items: center;
              flex-wrap: wrap;
            }

            .el-form-item__error {
              position: static !important;
              padding-top: 0 !important;
              margin-top: 0 !important;
              margin-left: 10px !important;
              color: #f56c6c;
              font-size: 12px;
              line-height: 1;
              display: inline-block;
              flex: 1;
              min-width: 200px;
            }
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
              display: flex;
              align-items: center;
              gap: 10px;

              .form-input {
                width: 300px;
                flex-shrink: 0;
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

