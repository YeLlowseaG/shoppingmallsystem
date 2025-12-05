<template>
  <div class="register-container">
    <div class="register-box">
      <h2>用户注册</h2>
      <p class="welcome-text">尊敬的用户，欢迎您注册成为本网站用户</p>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
        label-width="120px"
        class="register-form"
      >
        <!-- 必填字段 -->
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（3-50个字符）"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="电子邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20个字符）"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="姓名" prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
          />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="registerForm.gender">
            <el-radio :label="0">女</el-radio>
            <el-radio :label="1">男</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="地区" required>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="registerForm.province" placeholder="省份" @change="handleProvinceChange">
                <el-option
                  v-for="province in provinces"
                  :key="province.value"
                  :label="province.label"
                  :value="province.value"
                />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="registerForm.city" placeholder="城市" :disabled="!registerForm.province" @change="handleCityChange">
                <el-option
                  v-for="city in cities"
                  :key="city.value"
                  :label="city.label"
                  :value="city.value"
                />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="registerForm.district" placeholder="区县" :disabled="!registerForm.city">
                <el-option
                  v-for="district in districts"
                  :key="district.value"
                  :label="district.label"
                  :value="district.value"
                />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="联系地址" prop="address">
          <el-input
            v-model="registerForm.address"
            placeholder="请输入详细地址"
            type="textarea"
            :rows="2"
          />
        </el-form-item>

        <el-form-item label="移动电话" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号码"
            prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item label="运营人员" prop="operator">
          <el-input
            v-model="registerForm.operator"
            placeholder="请输入运营人员信息"
          />
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-input
                v-model="registerForm.captcha"
                placeholder="请输入验证码"
                @keyup.enter="handleRegister"
              />
            </el-col>
            <el-col :span="12">
              <div class="captcha-image-container" @click="refreshCaptcha">
                <img
                  v-if="captchaImage"
                  :src="captchaImage"
                  alt="验证码"
                  class="captcha-image"
                />
                <el-button
                  v-else
                  @click="refreshCaptcha"
                  :loading="captchaLoading"
                  style="width: 100%"
                >
                  获取验证码
                </el-button>
              </div>
            </el-col>
          </el-row>
        </el-form-item>

        <!-- 可选字段 -->
        <el-divider>可选信息</el-divider>

        <el-form-item label="出生日期">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="registerForm.birthYear" placeholder="年">
                <el-option
                  v-for="year in years"
                  :key="year"
                  :label="year"
                  :value="year"
                />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="registerForm.birthMonth" placeholder="月">
                <el-option
                  v-for="month in months"
                  :key="month"
                  :label="month"
                  :value="month"
                />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="registerForm.birthDay" placeholder="日">
                <el-option
                  v-for="day in days"
                  :key="day"
                  :label="day"
                  :value="day"
                />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="邮编">
          <el-input v-model="registerForm.zipCode" placeholder="请输入邮编" />
        </el-form-item>

        <el-form-item label="固定电话">
          <el-input v-model="registerForm.fixedPhone" placeholder="请输入固定电话" />
        </el-form-item>

        <el-form-item label="安全问题">
          <el-input v-model="registerForm.securityQuestion" placeholder="用于找回密码" />
        </el-form-item>

        <el-form-item label="回答">
          <el-input v-model="registerForm.securityAnswer" placeholder="安全问题答案" />
        </el-form-item>

        <el-form-item label="旺旺">
          <el-input v-model="registerForm.wangwang" placeholder="请输入旺旺账号" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister" style="width: 100%">
            立即注册
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="login-link">
            已有账号？<el-link type="primary" @click="goToLogin">立即登录</el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { register as registerApi, type RegisterDTO } from '@/api/buyer/user'
import { generateCaptcha } from '@/api/common/captcha'

const router = useRouter()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaLoading = ref(false)
const captchaImage = ref('')

const registerForm = reactive<RegisterDTO>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  realName: '',
  gender: 1,
  province: '',
  city: '',
  district: '',
  address: '',
  phone: '',
  operator: '',
  captchaId: '',
  captcha: '',
  birthYear: undefined,
  birthMonth: undefined,
  birthDay: undefined,
  zipCode: '',
  fixedPhone: '',
  securityQuestion: '',
  securityAnswer: '',
  wangwang: ''
})

// 验证规则
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3-50个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入联系地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  operator: [
    { required: true, message: '请输入运营人员', trigger: 'blur' }
  ],
  captchaId: [
    { required: true, message: '请获取验证码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 地区数据（简化版，实际应该从后端获取）
const provinces = ref([
  { label: '北京市', value: '北京' },
  { label: '上海市', value: '上海' },
  { label: '广东省', value: '广东' },
  { label: '浙江省', value: '浙江' },
  { label: '江苏省', value: '江苏' }
])

const cities = ref<Array<{ label: string; value: string }>>([])
const districts = ref<Array<{ label: string; value: string }>>([])

// 简化版地区数据
const regionData: Record<string, Record<string, string[]>> = {
  '北京': {
    '北京市': ['东城区', '西城区', '朝阳区', '海淀区']
  },
  '上海': {
    '上海市': ['黄浦区', '徐汇区', '长宁区', '静安区']
  },
  '广东': {
    '广州市': ['越秀区', '海珠区', '天河区', '白云区'],
    '深圳市': ['罗湖区', '福田区', '南山区', '宝安区']
  }
}

const handleProvinceChange = () => {
  registerForm.city = ''
  registerForm.district = ''
  const provinceData = regionData[registerForm.province] || {}
  cities.value = Object.keys(provinceData).map(city => ({ label: city, value: city }))
  districts.value = []
}

const handleCityChange = () => {
  registerForm.district = ''
  const provinceData = regionData[registerForm.province] || {}
  const cityData = provinceData[registerForm.city] || []
  districts.value = cityData.map(district => ({ label: district, value: district }))
}

// 出生日期选项
const currentYear = new Date().getFullYear()
const years = ref(Array.from({ length: 100 }, (_, i) => currentYear - i))
const months = ref(Array.from({ length: 12 }, (_, i) => i + 1))
const days = computed(() => {
  if (!registerForm.birthYear || !registerForm.birthMonth) {
    return Array.from({ length: 31 }, (_, i) => i + 1)
  }
  const daysInMonth = new Date(registerForm.birthYear, registerForm.birthMonth, 0).getDate()
  return Array.from({ length: daysInMonth }, (_, i) => i + 1)
})

// 刷新验证码
const refreshCaptcha = async () => {
  captchaLoading.value = true
  try {
    const response = await generateCaptcha()
    captchaImage.value = response.captchaImage
    registerForm.captchaId = response.captchaId
    registerForm.captcha = '' // 清空验证码输入
  } catch (error: any) {
    ElMessage.error(error.message || '获取验证码失败')
  } finally {
    captchaLoading.value = false
  }
}

// 页面加载时获取验证码
onMounted(() => {
  refreshCaptcha()
})

// 注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate((valid) => {
    if (valid) {
      // 验证必填的地区字段
      if (!registerForm.province || !registerForm.city || !registerForm.district) {
        ElMessage.error('请选择完整的地区信息')
        return
      }

      // 验证验证码ID
      if (!registerForm.captchaId) {
        ElMessage.error('请先获取验证码')
        return
      }

      loading.value = true
      registerApi(registerForm)
        .then(() => {
          ElMessage.success('注册成功，请等待管理员审核')
          router.push('/login')
        })
        .catch((error) => {
          ElMessage.error(error.message || '注册失败')
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
.register-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 40px 20px;
}

.register-box {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 10px;
  color: #333;
  font-size: 28px;
}

.welcome-text {
  text-align: center;
  color: #666;
  margin-bottom: 30px;
}

.register-form {
  margin-top: 20px;
}

.login-link {
  text-align: center;
  width: 100%;
}

.captcha-image-container {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #f5f7fa;
  transition: all 0.3s;
}

.captcha-image-container:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.captcha-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 4px;
}
</style>

