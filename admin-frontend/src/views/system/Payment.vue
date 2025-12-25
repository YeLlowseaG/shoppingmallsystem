<template>
  <div class="payment-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>支付配置管理</span>
          <div>
            <el-button type="info" @click="handleRefreshCache">刷新缓存</el-button>
            <el-button type="primary" @click="handleSave" :loading="saving">保存配置</el-button>
          </div>
        </div>
      </template>

      <!-- 微信支付配置 -->
      <el-card shadow="never" class="config-card" style="margin-bottom: 20px">
        <template #header>
          <div class="config-header">
            <span>微信支付配置</span>
            <el-switch
              v-model="wechatConfig.enabled"
              active-text="启用"
              inactive-text="禁用"
              @change="handleWechatEnabledChange"
            />
          </div>
        </template>

        <el-form :model="wechatConfig" label-width="140px" :disabled="!wechatConfig.enabled">
          <!-- 环境选择 -->
          <el-form-item label="支付环境">
            <el-radio-group v-model="wechatConfig.env">
              <el-radio label="sandbox">沙箱环境</el-radio>
              <el-radio label="production">生产环境</el-radio>
            </el-radio-group>
            <el-button
              type="primary"
              link
              size="small"
              style="margin-left: 20px"
              @click="handleTestConnection('wechat', wechatConfig.env)"
              :loading="testing.wechat"
            >
              测试连接
            </el-button>
          </el-form-item>

          <!-- 回调地址 -->
          <el-form-item label="回调地址">
            <el-input
              v-model="wechatConfig.notifyUrl"
              placeholder="请输入回调地址，如：https://your-domain.com/api/buyer/payment/wechat/notify"
              style="width: 600px"
            />
          </el-form-item>

          <!-- 沙箱环境配置 -->
          <el-divider content-position="left">沙箱环境配置</el-divider>
          <el-form-item label="AppID">
            <el-input
              v-model="wechatConfig.sandbox.appid"
              placeholder="请输入微信支付沙箱AppID"
              style="width: 400px"
            />
          </el-form-item>
          <el-form-item label="商户号">
            <el-input
              v-model="wechatConfig.sandbox.mchid"
              placeholder="请输入微信支付沙箱商户号"
              style="width: 400px"
            />
          </el-form-item>
          <el-form-item label="API密钥">
            <el-input
              v-model="wechatConfig.sandbox.key"
              type="textarea"
              :rows="3"
              placeholder="请输入微信支付沙箱API密钥"
              style="width: 600px"
              show-password
            />
          </el-form-item>
          <el-form-item label="证书路径">
            <el-input
              v-model="wechatConfig.sandbox.certPath"
              placeholder="请输入证书文件路径，如：/cert/wechat/sandbox/apiclient_cert.p12"
              style="width: 600px"
            />
          </el-form-item>

          <!-- 生产环境配置 -->
          <el-divider content-position="left">生产环境配置</el-divider>
          <el-form-item label="AppID">
            <el-input
              v-model="wechatConfig.production.appid"
              placeholder="请输入微信支付生产AppID"
              style="width: 400px"
            />
          </el-form-item>
          <el-form-item label="商户号">
            <el-input
              v-model="wechatConfig.production.mchid"
              placeholder="请输入微信支付生产商户号"
              style="width: 400px"
            />
          </el-form-item>
          <el-form-item label="API密钥">
            <el-input
              v-model="wechatConfig.production.key"
              type="textarea"
              :rows="3"
              placeholder="请输入微信支付生产API密钥"
              style="width: 600px"
              show-password
            />
          </el-form-item>
          <el-form-item label="证书路径">
            <el-input
              v-model="wechatConfig.production.certPath"
              placeholder="请输入证书文件路径，如：/cert/wechat/production/apiclient_cert.p12"
              style="width: 600px"
            />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 支付宝配置 -->
      <el-card shadow="never" class="config-card">
        <template #header>
          <div class="config-header">
            <span>支付宝配置</span>
            <el-switch
              v-model="alipayConfig.enabled"
              active-text="启用"
              inactive-text="禁用"
              @change="handleAlipayEnabledChange"
            />
          </div>
        </template>

        <el-form :model="alipayConfig" label-width="140px" :disabled="!alipayConfig.enabled">
          <!-- 环境选择 -->
          <el-form-item label="支付环境">
            <el-radio-group v-model="alipayConfig.env">
              <el-radio label="sandbox">沙箱环境</el-radio>
              <el-radio label="production">生产环境</el-radio>
            </el-radio-group>
            <el-button
              type="primary"
              link
              size="small"
              style="margin-left: 20px"
              @click="handleTestConnection('alipay', alipayConfig.env)"
              :loading="testing.alipay"
            >
              测试连接
            </el-button>
          </el-form-item>

          <!-- 回调地址 -->
          <el-form-item label="回调地址">
            <el-input
              v-model="alipayConfig.notifyUrl"
              placeholder="请输入回调地址，如：https://your-domain.com/api/buyer/payment/alipay/notify"
              style="width: 600px"
            />
          </el-form-item>

          <!-- 沙箱环境配置 -->
          <el-divider content-position="left">沙箱环境配置</el-divider>
          <el-form-item label="AppID">
            <el-input
              v-model="alipayConfig.sandbox.appid"
              placeholder="请输入支付宝沙箱AppID"
              style="width: 400px"
            />
          </el-form-item>
          <el-form-item label="应用私钥">
            <el-input
              v-model="alipayConfig.sandbox.privateKey"
              type="textarea"
              :rows="5"
              placeholder="请输入支付宝沙箱应用私钥"
              style="width: 600px"
              show-password
            />
          </el-form-item>
          <el-form-item label="支付宝公钥">
            <el-input
              v-model="alipayConfig.sandbox.publicKey"
              type="textarea"
              :rows="5"
              placeholder="请输入支付宝沙箱公钥"
              style="width: 600px"
            />
          </el-form-item>

          <!-- 生产环境配置 -->
          <el-divider content-position="left">生产环境配置</el-divider>
          <el-form-item label="AppID">
            <el-input
              v-model="alipayConfig.production.appid"
              placeholder="请输入支付宝生产AppID"
              style="width: 400px"
            />
          </el-form-item>
          <el-form-item label="应用私钥">
            <el-input
              v-model="alipayConfig.production.privateKey"
              type="textarea"
              :rows="5"
              placeholder="请输入支付宝生产应用私钥"
              style="width: 600px"
              show-password
            />
          </el-form-item>
          <el-form-item label="支付宝公钥">
            <el-input
              v-model="alipayConfig.production.publicKey"
              type="textarea"
              :rows="5"
              placeholder="请输入支付宝生产公钥"
              style="width: 600px"
            />
          </el-form-item>
        </el-form>
      </el-card>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPaymentConfig,
  updatePaymentConfig,
  refreshPaymentConfig,
  testPaymentConnection,
  type WeChatPayConfig,
  type AlipayConfig
} from '@/api/admin/payment'

// 配置数据
const wechatConfig = ref<WeChatPayConfig>({
  enabled: false,
  env: 'sandbox',
  notifyUrl: '',
  sandbox: {
    appid: '',
    mchid: '',
    key: '',
    certPath: ''
  },
  production: {
    appid: '',
    mchid: '',
    key: '',
    certPath: ''
  }
})

const alipayConfig = ref<AlipayConfig>({
  enabled: false,
  env: 'sandbox',
  notifyUrl: '',
  sandbox: {
    appid: '',
    privateKey: '',
    publicKey: ''
  },
  production: {
    appid: '',
    privateKey: '',
    publicKey: ''
  }
})

// 状态
const saving = ref(false)
const testing = ref({
  wechat: false,
  alipay: false
})

// 加载配置
const loadConfig = async () => {
  try {
    const res = await getPaymentConfig()
    if (res.wechat) {
      wechatConfig.value = res.wechat
    }
    if (res.alipay) {
      alipayConfig.value = res.alipay
    }
  } catch (error: any) {
    ElMessage.error('加载配置失败：' + (error.message || '未知错误'))
  }
}

// 保存配置
const handleSave = async () => {
  try {
    saving.value = true
    await updatePaymentConfig({
      wechat: wechatConfig.value,
      alipay: alipayConfig.value
    })
    ElMessage.success('配置保存成功')
  } catch (error: any) {
    ElMessage.error('保存配置失败：' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 刷新缓存
const handleRefreshCache = async () => {
  try {
    await refreshPaymentConfig()
    ElMessage.success('缓存刷新成功')
    // 重新加载配置
    await loadConfig()
  } catch (error: any) {
    ElMessage.error('刷新缓存失败：' + (error.message || '未知错误'))
  }
}

// 测试连接
const handleTestConnection = async (paymentMethod: 'wechat' | 'alipay', env?: string) => {
  try {
    testing.value[paymentMethod] = true
    const result = await testPaymentConnection({
      paymentMethod,
      env: env as 'sandbox' | 'production' | undefined
    })
    ElMessage.success(result || '连接测试成功')
  } catch (error: any) {
    ElMessage.error('连接测试失败：' + (error.message || '未知错误'))
  } finally {
    testing.value[paymentMethod] = false
  }
}

// 微信支付启用状态变化
const handleWechatEnabledChange = (value: boolean) => {
  if (value) {
    ElMessage.info('已启用微信支付')
  } else {
    ElMessage.warning('已禁用微信支付')
  }
}

// 支付宝启用状态变化
const handleAlipayEnabledChange = (value: boolean) => {
  if (value) {
    ElMessage.info('已启用支付宝')
  } else {
    ElMessage.warning('已禁用支付宝')
  }
}

// 初始化
onMounted(() => {
  loadConfig()
})
</script>

<style scoped lang="scss">
.payment-config {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .config-card {
    .config-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  :deep(.el-divider__text) {
    font-weight: 600;
    color: #409eff;
  }
}
</style>
