<template>
  <div class="register-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 主内容区域 -->
    <div class="register-content">
      <div class="container">
        <h2 class="page-title">用户注册</h2>
        <p class="welcome-text">欢迎来到我们网站,如果您是新用户,请填写下面的表单进行注册</p>

        <!-- 协议同意步骤 -->
        <div v-if="!agreed" class="agreement-box">
          <h3 class="agreement-title">会员注册协议</h3>
          <div class="agreement-content">
            <p>欢迎您光临，如果您准备注册成我们的会员，请认真阅读此协议。注册后，则视为您同意我们的协议，协议内容如下：</p>
            
            <div class="agreement-item">
              <strong>一、</strong>
              <p>只有注册会员才可以在商店上进行购物、订单查询等。请记住您的会员用户名。它是您在的唯一识别，您的任何投诉、问题、购买记录，均采用这个会员用户名处理。而且，您再次光临本站购物会有很大的方便，许多信息不必重新输入。</p>
            </div>

            <div class="agreement-item">
              <strong>二、</strong>
              <p>会员用户名可以是您便于记忆的任何代号比如网名。不过，在注册用户的时候，我们建议您务必输入您的真实信息，这样有利于准确发货、提供各项服务、必要时迅速与您联系。拥有强大的后台保密程序，我们绝对保证您的注册信息的安全和保密性。</p>
            </div>

            <div class="agreement-item">
              <strong>三、</strong>
              <p>我们保证所售商品的质量，不售假冒伪劣产品。如有质量问题，请联系客服。</p>
            </div>

            <div class="agreement-item">
              <strong>四、</strong>
              <p>我们在收到您的汇款后，会及时发货。售出商品若有质量问题，除人为损坏外，未经穿着、洗涤无污损的商品，包换。</p>
            </div>

            <div class="agreement-item">
              <strong>五、</strong>
              <p>未经许可不得擅自转载、使用本站商品资料、图片，发现必究。</p>
            </div>
          </div>
          <div class="agreement-actions">
            <el-button class="agree-button" size="large" @click="handleAgree">同意</el-button>
          </div>
        </div>

        <!-- 注册表单 -->
        <div v-else class="register-form-box">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="rules"
            label-width="0"
            class="register-form"
          >
            <!-- 用户名 -->
            <el-form-item prop="username">
              <div class="form-row">
                <div class="form-label">
                  <span class="required">*</span>用户名:
                </div>
                <div class="form-input-wrapper">
                  <el-input
                    v-model="registerForm.username"
                    placeholder="请输入用户名"
                    class="form-input"
                  />
                </div>
              </div>
            </el-form-item>

            <!-- 电子邮箱 -->
            <el-form-item prop="email">
              <div class="form-row">
                <div class="form-label">
                  <span class="required">*</span>电子邮箱:
                </div>
                <div class="form-input-wrapper">
                  <el-input
                    v-model="registerForm.email"
                    placeholder="请输入邮箱"
                    class="form-input"
                  />
                </div>
              </div>
            </el-form-item>

            <!-- 密码 -->
            <el-form-item prop="password">
              <div class="form-row">
                <div class="form-label">
                  <span class="required">*</span>密码:
                </div>
                <div class="form-input-wrapper">
                  <el-input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="请输入密码"
                    class="form-input"
                    show-password
                  />
                </div>
              </div>
            </el-form-item>

            <!-- 确认密码 -->
            <el-form-item prop="confirmPassword">
              <div class="form-row">
                <div class="form-label">
                  <span class="required">*</span>确认密码:
                </div>
                <div class="form-input-wrapper">
                  <el-input
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入密码"
                    class="form-input"
                    show-password
                  />
                </div>
              </div>
            </el-form-item>

            <!-- 姓名 -->
            <el-form-item prop="realName">
              <div class="form-row">
                <div class="form-label">
                  <span class="required">*</span>姓名:
                </div>
                <div class="form-input-wrapper">
                  <el-input
                    v-model="registerForm.realName"
                    placeholder="请输入真实姓名"
                    class="form-input"
                  />
                </div>
              </div>
            </el-form-item>

            <!-- 性别 -->
            <el-form-item prop="gender">
              <div class="form-row">
                <div class="form-label">
                  <span class="required">*</span>性别:
                </div>
                <div class="form-input-wrapper">
                  <el-radio-group v-model="registerForm.gender">
                    <el-radio :label="1">男</el-radio>
                    <el-radio :label="0">女</el-radio>
                  </el-radio-group>
                </div>
              </div>
            </el-form-item>

            <!-- 出生日期 -->
            <div class="form-row">
              <div class="form-label">出生日期:</div>
              <div class="form-input-wrapper">
                <div class="date-selectors">
                  <el-select v-model="registerForm.birthYear" placeholder="请选择" class="date-select">
                    <el-option
                      v-for="year in years"
                      :key="year"
                      :label="year"
                      :value="year"
                    />
                  </el-select>
                  <el-select v-model="registerForm.birthMonth" placeholder="请选择" class="date-select">
                    <el-option
                      v-for="month in months"
                      :key="month"
                      :label="month"
                      :value="month"
                    />
                  </el-select>
                  <el-select v-model="registerForm.birthDay" placeholder="请选择" class="date-select">
                    <el-option
                      v-for="day in days"
                      :key="day"
                      :label="day"
                      :value="day"
                    />
                  </el-select>
                </div>
              </div>
            </div>

            <!-- 地区 -->
            <div class="form-row">
              <div class="form-label">
                <span class="required">*</span>地区:
              </div>
              <div class="form-input-wrapper">
                <div class="region-selectors">
                  <el-select v-model="registerForm.province" placeholder="请选择..." class="region-select" @change="handleProvinceChange">
                    <el-option
                      v-for="province in provinces"
                      :key="province.value"
                      :label="province.label"
                      :value="province.value"
                    />
                  </el-select>
                  <el-select v-model="registerForm.city" placeholder="请选择..." class="region-select" :disabled="!registerForm.province" @change="handleCityChange">
                    <el-option
                      v-for="city in cities"
                      :key="city.value"
                      :label="city.label"
                      :value="city.value"
                    />
                  </el-select>
                  <el-select v-model="registerForm.district" placeholder="请选择..." class="region-select" :disabled="!registerForm.city">
                    <el-option
                      v-for="district in districts"
                      :key="district.value"
                      :label="district.label"
                      :value="district.value"
                    />
                  </el-select>
                </div>
              </div>
            </div>

            <!-- 联系地址 -->
            <el-form-item prop="address">
              <div class="form-row">
                <div class="form-label">
                  <span class="required">*</span>联系地址:
                </div>
                <div class="form-input-wrapper">
                  <el-input
                    v-model="registerForm.address"
                    placeholder="请输入详细地址"
                    type="textarea"
                    :rows="2"
                    class="form-textarea"
                  />
                </div>
              </div>
            </el-form-item>

            <!-- 邮编 -->
            <div class="form-row">
              <div class="form-label">邮编:</div>
              <div class="form-input-wrapper">
                <el-input v-model="registerForm.zipCode" placeholder="请输入邮编" class="form-input" />
              </div>
            </div>

            <!-- 移动电话 -->
            <el-form-item prop="phone">
              <div class="form-row">
                <div class="form-label">
                  <span class="required">*</span>移动电话:
                </div>
                <div class="form-input-wrapper">
                  <el-input
                    v-model="registerForm.phone"
                    placeholder="请输入手机号码"
                    class="form-input"
                  />
                </div>
              </div>
            </el-form-item>

            <!-- 固定电话 -->
            <div class="form-row">
              <div class="form-label">固定电话:</div>
              <div class="form-input-wrapper">
                <el-input v-model="registerForm.fixedPhone" placeholder="请输入固定电话" class="form-input" />
              </div>
            </div>

            <!-- 安全问题 -->
            <div class="form-row">
              <div class="form-label">安全问题:</div>
              <div class="form-input-wrapper">
                <el-input v-model="registerForm.securityQuestion" placeholder="用于找回密码" class="form-input" />
              </div>
            </div>

            <!-- 回答 -->
            <div class="form-row">
              <div class="form-label">回答:</div>
              <div class="form-input-wrapper">
                <el-input v-model="registerForm.securityAnswer" placeholder="安全问题答案" class="form-input" />
              </div>
            </div>

            <!-- 旺旺 -->
            <div class="form-row">
              <div class="form-label">旺旺:</div>
              <div class="form-input-wrapper">
                <el-input v-model="registerForm.wangwang" placeholder="请输入旺旺账号" class="form-input" />
              </div>
            </div>

            <!-- 运营人员 -->
            <el-form-item prop="operator">
              <div class="form-row">
                <div class="form-label">
                  <span class="required">*</span>运营人员:
                </div>
                <div class="form-input-wrapper">
                  <el-input
                    v-model="registerForm.operator"
                    placeholder="请输入运营人员信息"
                    class="form-input"
                  />
                </div>
              </div>
            </el-form-item>

            <!-- 验证码 -->
            <el-form-item prop="captcha">
              <div class="form-row">
                <div class="form-label">
                  <span class="required">*</span>验证码:
                </div>
                <div class="form-input-wrapper">
                  <div class="captcha-container">
                    <div class="captcha-image-box" @click="refreshCaptcha">
                      <img
                        v-if="captchaImage"
                        :src="captchaImage"
                        alt="验证码"
                        class="captcha-image"
                      />
                      <div v-else class="captcha-placeholder">
                        <el-button
                          @click="refreshCaptcha"
                          :loading="captchaLoading"
                          size="small"
                        >
                          获取验证码
                        </el-button>
                      </div>
                    </div>
                    <el-input
                      v-model="registerForm.captcha"
                      placeholder="请输入验证码"
                      class="captcha-input"
                      @keyup.enter="handleRegister"
                    />
                  </div>
                </div>
              </div>
            </el-form-item>

            <!-- 提交按钮 -->
            <div class="form-row">
              <div class="form-label"></div>
              <div class="form-input-wrapper">
                <el-button
                  :loading="loading"
                  @click="handleRegister"
                  class="register-button"
                >
                  立即注册
                </el-button>
              </div>
            </div>

            <!-- 登录链接 -->
            <div class="form-row">
              <div class="form-label"></div>
              <div class="form-input-wrapper">
                <div class="login-link">
                  已有账号？<el-link type="primary" @click="goToLogin">立即登录</el-link>
                </div>
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
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { register as registerApi, type RegisterDTO } from '@/api/buyer/user'
import { generateCaptcha } from '@/api/common/captcha'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'

const router = useRouter()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaLoading = ref(false)
const captchaImage = ref('')
const agreed = ref(false)

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
const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
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

// 同意协议
const handleAgree = () => {
  agreed.value = true
  // 同意后获取验证码
  refreshCaptcha()
}

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

// 注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

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

  await registerFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      registerApi(registerForm)
        .then(() => {
          ElMessage.success('注册成功，请等待管理员审核')
          router.push('/login')
        })
        .catch((error) => {
          ElMessage.error(error.message || '注册失败')
          // 验证码错误时刷新验证码
          if (error.message && (error.message.includes('验证码') || error.message.includes('captcha'))) {
            refreshCaptcha()
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

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.register-content {
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

  .welcome-text {
    text-align: center;
    color: #666;
    margin-bottom: 30px;
    font-size: 14px;
  }
}

// 协议同意框
.agreement-box {
  max-width: 900px;
  margin: 0 auto;
  background: white;
  padding: 40px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

  .agreement-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin-bottom: 20px;
    text-align: center;
  }

  .agreement-content {
    max-height: 500px;
    overflow-y: auto;
    padding: 20px;
    background: #fafafa;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    margin-bottom: 30px;
    line-height: 1.8;
    color: #666;
    font-size: 14px;

    > p {
      margin-bottom: 15px;
      text-indent: 2em;
    }

    .agreement-item {
      margin-bottom: 20px;

      strong {
        color: #333;
        font-size: 15px;
      }

      p {
        margin-top: 8px;
        text-indent: 2em;
        line-height: 1.8;
      }
    }
  }

  .agreement-actions {
    text-align: center;

    .agree-button {
      width: 200px;
      height: 45px;
      font-size: 16px;
      background: linear-gradient(to right, #ffa500, #ff8c00);
      border: none;
      border-radius: 4px;
      color: #fff;
      font-weight: bold;
      transition: all 0.3s;

      &:hover {
        background: linear-gradient(to right, #ff8c00, #ff7f00);
        transform: translateY(-1px);
        box-shadow: 0 4px 8px rgba(255, 140, 0, 0.3);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }
}

// 注册表单框
.register-form-box {
  max-width: 900px;
  margin: 0 auto;
  background: white;
  padding: 40px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.register-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  :deep(.el-form-item__error) {
    padding-left: 135px;
    margin-top: 4px;
  }

  .form-row {
    display: flex;
    align-items: flex-start;

    .form-label {
      width: 120px;
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
        width: 500px;

        :deep(.el-input__wrapper) {
          border-radius: 4px;
        }
      }

      .form-textarea {
        width: 500px;

        :deep(.el-textarea__inner) {
          border-radius: 4px;
          min-height: 80px;
        }
      }

      .date-selectors {
        display: flex;
        gap: 10px;
        width: 500px;

        .date-select {
          flex: 1;

          :deep(.el-input__wrapper) {
            border-radius: 4px;
          }
        }
      }

      .region-selectors {
        display: flex;
        gap: 10px;
        width: 500px;

        .region-select {
          flex: 1;

          :deep(.el-input__wrapper) {
            border-radius: 4px;
          }
        }
      }

      .captcha-container {
        display: flex;
        gap: 10px;
        align-items: center;
        width: 500px;

        .captcha-image-box {
          width: 120px;
          height: 40px;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          background: #f5f7fa;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: all 0.3s;
          flex-shrink: 0;

          &:hover {
            border-color: #ff8c00;
            background: #fff5e6;
          }

          .captcha-image {
            width: 100%;
            height: 100%;
            object-fit: contain;
            border-radius: 4px;
          }

          .captcha-placeholder {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100%;
          }
        }

        .captcha-input {
          flex: 1;
          min-width: 200px;

          :deep(.el-input__wrapper) {
            border-radius: 4px;
          }
        }
      }

      .register-button {
        width: 200px;
        height: 45px;
        font-size: 16px;
        background: linear-gradient(to right, #ffa500, #ff8c00);
        border: none;
        border-radius: 4px;
        color: #fff;
        font-weight: bold;
        transition: all 0.3s;

        &:hover {
          background: linear-gradient(to right, #ff8c00, #ff7f00);
          transform: translateY(-1px);
          box-shadow: 0 4px 8px rgba(255, 140, 0, 0.3);
        }

        &:active {
          transform: translateY(0);
        }
      }

      .login-link {
        font-size: 14px;
        color: #666;

        :deep(.el-link) {
          color: #e4393c;
        }
      }
    }
  }
}
</style>
