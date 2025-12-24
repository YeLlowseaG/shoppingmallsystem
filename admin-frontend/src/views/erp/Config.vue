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
        <el-form-item label="API地址" prop="apiUrl">
          <el-input v-model="form.apiUrl" placeholder="请输入聚水潭API地址" />
          <div class="form-tip">例如：https://api.jushuitan.com/api/open/query.aspx</div>
        </el-form-item>

        <el-form-item label="App Key" prop="appKey">
          <el-input v-model="form.appKey" placeholder="请输入App Key" />
        </el-form-item>

        <el-form-item label="App Secret" prop="appSecret">
          <el-input v-model="form.appSecret" type="password" placeholder="请输入App Secret" show-password />
        </el-form-item>

        <el-form-item label="店铺ID" prop="shopId">
          <el-input v-model="form.shopId" placeholder="请输入店铺ID（选填）" />
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
        <h4>1. 获取聚水潭API凭证</h4>
        <p>登录聚水潭ERP后台，进入「系统设置」-「开放平台」，创建应用获取App Key和App Secret。</p>

        <h4>2. API地址</h4>
        <p>聚水潭正式环境API地址：https://api.jushuitan.com/api/open/query.aspx</p>

        <h4>3. 功能说明</h4>
        <ul>
          <li><strong>自动推送订单：</strong>订单支付成功后自动推送到聚水潭，商家可在聚水潭后台处理发货</li>
          <li><strong>自动拉取物流：</strong>定时从聚水潭拉取物流信息，自动更新订单发货状态</li>
          <li><strong>手动操作：</strong>在订单列表页面可以手动推送订单或拉取物流信息</li>
        </ul>

        <h4>4. 注意事项</h4>
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
  apiUrl: 'https://api.jushuitan.com/api/open/query.aspx',
  appKey: '',
  appSecret: '',
  shopId: '',
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
      await saveJushuitanConfig(form)
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
