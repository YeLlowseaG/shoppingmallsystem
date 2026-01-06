<template>
  <div class="erp-config-container">
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>聚水潭ERP配置</span>
          <el-button type="primary" @click="handleTest" :loading="testing">测试连接</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="150px">
        <el-form-item label="当前环境">
          <el-radio-group v-model="form.envType">
            <el-radio value="test">测试环境</el-radio>
            <el-radio value="production">生产环境</el-radio>
          </el-radio-group>
          <div class="form-tip">⚠️ 测试环境不会真实发货，生产环境会触发真实订单处理！</div>
        </el-form-item>

        <el-divider content-position="left">{{ form.envType === 'test' ? '测试环境配置' : '生产环境配置' }}</el-divider>

        <el-form-item v-if="form.envType === 'test'" label="API地址" prop="testApiUrl">
          <el-input v-model="form.testApiUrl" placeholder="请输入测试环境API地址" />
          <div class="form-tip">测试环境：https://dev-api.jushuitan.com/api/open/query.aspx</div>
        </el-form-item>

        <el-form-item v-if="form.envType === 'production'" label="API地址" prop="apiUrl">
          <el-input v-model="form.apiUrl" placeholder="请输入生产环境API地址" />
          <div class="form-tip">生产环境：https://api.jushuitan.com/api/open/query.aspx</div>
        </el-form-item>

        <el-form-item v-if="form.envType === 'test'" label="App Key" prop="testAppKey">
          <el-input v-model="form.testAppKey" placeholder="请输入测试环境App Key" />
          <div class="form-tip">公共测试Key：b0b7d1db226d4216a3d58df9ffa2dde5</div>
        </el-form-item>

        <el-form-item v-if="form.envType === 'production'" label="App Key" prop="appKey">
          <el-input v-model="form.appKey" placeholder="请输入生产环境App Key" />
        </el-form-item>

        <el-form-item v-if="form.envType === 'test'" label="App Secret" prop="testAppSecret">
          <el-input v-model="form.testAppSecret" type="password" placeholder="请输入测试环境App Secret" show-password />
          <div class="form-tip">公共测试Secret：99c4cef262f34ca882975a7064de0b87</div>
        </el-form-item>

        <el-form-item v-if="form.envType === 'production'" label="App Secret" prop="appSecret">
          <el-input v-model="form.appSecret" type="password" placeholder="请输入生产环境App Secret" show-password />
        </el-form-item>

        <el-form-item v-if="form.envType === 'test'" label="Access Token" prop="testAccessToken">
          <el-input v-model="form.testAccessToken" type="password" placeholder="请输入测试环境Access Token" show-password />
          <div class="form-tip">公共测试Token：b7e3b1e24e174593af8ca5c397e53dad</div>
        </el-form-item>

        <el-form-item v-if="form.envType === 'production'" label="Access Token" prop="accessToken">
          <el-input v-model="form.accessToken" type="password" placeholder="请输入生产环境Access Token（可选）" show-password />
        </el-form-item>

        <el-form-item v-if="form.envType === 'test'" label="店铺ID" prop="testShopId">
          <el-input v-model="form.testShopId" placeholder="请输入测试环境店铺ID（必填）" />
          <div class="form-tip">测试环境的店铺ID，用于测试环境调试</div>
        </el-form-item>

        <el-form-item v-if="form.envType === 'test'" label="物流同步回调地址" prop="testCallbackUrl">
          <el-input v-model="form.testCallbackUrl" placeholder="请输入测试环境物流同步回调地址" />
          <div class="form-tip">测试环境回调地址，ERP发货后会调用此地址通知系统，例如：https://your-domain.com/api/common/erp/callback/logistics</div>
        </el-form-item>

        <el-form-item v-if="form.envType === 'production'" label="店铺ID" prop="shopId">
          <el-input v-model="form.shopId" placeholder="请输入生产环境店铺ID（必填）" />
          <div class="form-tip">生产环境的店铺ID，正式业务使用</div>
        </el-form-item>

        <el-form-item v-if="form.envType === 'production'" label="物流同步回调地址" prop="callbackUrl">
          <el-input v-model="form.callbackUrl" placeholder="请输入生产环境物流同步回调地址" />
          <div class="form-tip">生产环境回调地址，ERP发货后会调用此地址通知系统，例如：https://your-domain.com/api/common/erp/callback/logistics</div>
        </el-form-item>

        <el-form-item label="启用状态">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
          <span class="ml-2">{{ form.enabled === 1 ? '已启用' : '未启用' }}</span>
        </el-form-item>

        <el-form-item label="自动推送订单">
          <el-switch v-model="form.autoPushOrder" :active-value="1" :inactive-value="0" />
          <div class="form-tip">开启后，订单支付成功将自动推送到聚水潭</div>
        </el-form-item>

        <el-form-item label="自动拉取物流">
          <el-switch v-model="form.autoPullLogistics" :active-value="1" :inactive-value="0" />
          <div class="form-tip">开启后，系统将定时拉取物流信息</div>
        </el-form-item>

        <el-form-item label="拉取间隔(分钟)" prop="pullInterval" v-if="form.autoPullLogistics === 1">
          <el-input-number v-model="form.pullInterval" :min="10" :max="1440" :step="10" />
          <div class="form-tip">定时拉取物流信息的时间间隔，建议30-60分钟</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存配置</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="help-card" style="margin-top: 20px;">
      <template #header>
        <span>配置说明</span>
      </template>
      <div class="help-content">
        <h4>1. 环境选择</h4>
        <ul>
          <li><strong>测试环境：</strong>用于开发调试，不会触发真实订单发货，使用聚水潭沙箱账号</li>
          <li><strong>生产环境：</strong>正式业务使用，会触发真实订单处理和发货，请谨慎操作！</li>
        </ul>

        <h4>2. 测试环境配置</h4>
        <p>API地址：https://dev-api.jushuitan.com/api/open/query.aspx</p>
        <p>公共测试凭证：</p>
        <ul>
          <li>App Key: b0b7d1db226d4216a3d58df9ffa2dde5</li>
          <li>App Secret: 99c4cef262f34ca882975a7064de0b87</li>
          <li>Access Token: b7e3b1e24e174593af8ca5c397e53dad</li>
        </ul>

        <h4>3. 生产环境配置</h4>
        <p>API地址：https://api.jushuitan.com/api/open/query.aspx</p>
        <p>需要登录聚水潭ERP后台，进入「系统设置」-「开放平台」，创建应用获取App Key和App Secret。</p>

        <h4>4. 功能说明</h4>
        <ul>
          <li><strong>自动推送订单：</strong>订单支付成功后自动推送到聚水潭，商家可在聚水潭后台处理发货</li>
          <li><strong>自动拉取物流：</strong>定时从聚水潭拉取物流信息，自动更新订单发货状态</li>
          <li><strong>手动操作：</strong>在订单列表页面可以手动推送订单或拉取物流信息</li>
        </ul>

        <h4>5. 物流同步回调地址</h4>
        <ul>
          <li><strong>回调地址：</strong>ERP发货后会自动调用此地址，通知系统更新订单状态和物流信息</li>
          <li><strong>接口路径：</strong>系统固定为 <code>/api/common/erp/callback/logistics</code></li>
          <li><strong>完整地址：</strong>需要在聚水潭ERP后台配置完整的回调URL，例如：<code>https://your-domain.com/api/common/erp/callback/logistics</code></li>
          <li><strong>注意事项：</strong>回调地址必须是公网可访问的URL，本地开发需要使用内网穿透工具（如ngrok）</li>
        </ul>

        <h4>6. 注意事项</h4>
        <ul>
          <li>保存配置前请先测试连接，确保配置正确</li>
          <li>App Secret请妥善保管，不要泄露给他人</li>
          <li>建议在非营业高峰期进行首次配置和测试</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getJushuitanConfig, saveJushuitanConfig, testJushuitanConnection } from '@/api/admin/erp'

const formRef = ref<FormInstance>()
const saving = ref(false)
const testing = ref(false)

const form = reactive({
  envType: 'test', // 默认测试环境
  apiUrl: 'https://api.jushuitan.com/api/open/query.aspx',
  appKey: '',
  appSecret: '',
  accessToken: '',
  testApiUrl: 'https://dev-api.jushuitan.com/api/open/query.aspx',
  testAppKey: 'b0b7d1db226d4216a3d58df9ffa2dde5',
  testAppSecret: '99c4cef262f34ca882975a7064de0b87',
  testAccessToken: 'b7e3b1e24e174593af8ca5c397e53dad',
  testShopId: '',
  testCallbackUrl: '',
  shopId: '',
  callbackUrl: '',
  enabled: 0,
  autoPushOrder: 1,
  autoPullLogistics: 1,
  pullInterval: 30
})

const rules: FormRules = {
  apiUrl: [
    { required: true, message: '请输入API地址', trigger: 'blur' }
  ],
  appKey: [
    { required: true, message: '请输入App Key', trigger: 'blur' }
  ],
  appSecret: [
    { required: true, message: '请输入App Secret', trigger: 'blur' }
  ],
  testShopId: [
    { required: true, message: '请输入测试环境店铺ID', trigger: 'blur' }
  ],
  shopId: [
    { required: true, message: '请输入生产环境店铺ID', trigger: 'blur' }
  ],
  pullInterval: [
    { required: true, message: '请输入拉取间隔', trigger: 'blur' },
    { type: 'number', min: 10, max: 1440, message: '拉取间隔应在10-1440分钟之间', trigger: 'blur' }
  ]
}

// 加载配置
const loadConfig = async () => {
  try {
    const data = await getJushuitanConfig()
    if (data) {
      Object.assign(form, data)
    }
  } catch (error: any) {
    console.error('加载配置失败:', error)
    // 如果是404错误，说明还没有配置，使用默认值
    if (error.response?.status !== 404) {
      ElMessage.error(error.message || '加载配置失败')
    }
  }
}

// 测试连接
const handleTest = async () => {
  if (!form.apiUrl || !form.appKey || !form.appSecret) {
    ElMessage.warning('请先填写API地址、App Key和App Secret')
    return
  }

  testing.value = true
  try {
    const data = await testJushuitanConnection()
    ElMessage.success(data || '连接成功')
  } catch (error: any) {
    ElMessage.error(error.message || '连接测试失败')
  } finally {
    testing.value = false
  }
}

// 保存配置
const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await ElMessageBox.confirm(
        '确定要保存配置吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      saving.value = true

      // 准备提交数据，处理AppSecret和AccessToken脱敏问题
      const submitData = { ...form }
      // 如果生产环境AppSecret包含****，说明是脱敏值，不提交此字段
      if (submitData.appSecret && submitData.appSecret.includes('****')) {
        delete submitData.appSecret
      }
      // 如果测试环境AppSecret包含****，说明是脱敏值，不提交此字段
      if (submitData.testAppSecret && submitData.testAppSecret.includes('****')) {
        delete submitData.testAppSecret
      }
      // 如果生产环境AccessToken包含****，说明是脱敏值，不提交此字段
      if (submitData.accessToken && submitData.accessToken.includes('****')) {
        delete submitData.accessToken
      }
      // 如果测试环境AccessToken包含****，说明是脱敏值，不提交此字段
      if (submitData.testAccessToken && submitData.testAccessToken.includes('****')) {
        delete submitData.testAccessToken
      }

      await saveJushuitanConfig(submitData)
      ElMessage.success('配置保存成功')
      await loadConfig()
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '配置保存失败')
      }
    } finally {
      saving.value = false
    }
  })
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
  loadConfig()
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped lang="scss">
.erp-config-container {
  padding: 20px;

  .config-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .form-tip {
    color: #909399;
    font-size: 12px;
    margin-top: 5px;
  }

  .ml-2 {
    margin-left: 8px;
  }

  .help-card {
    .help-content {
      h4 {
        margin-top: 16px;
        margin-bottom: 8px;
        color: #303133;
        font-size: 14px;
        font-weight: 600;

        &:first-child {
          margin-top: 0;
        }
      }

      p {
        margin: 8px 0;
        color: #606266;
        font-size: 14px;
        line-height: 1.6;
      }

      ul {
        margin: 8px 0;
        padding-left: 20px;
        color: #606266;
        font-size: 14px;
        line-height: 1.8;

        li {
          margin: 4px 0;

          strong {
            color: #303133;
          }
        }
      }
    }
  }
}
</style>
