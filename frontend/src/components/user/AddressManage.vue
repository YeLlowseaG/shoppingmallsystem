<template>
  <el-dialog
    v-model="visible"
    title="地址管理"
    width="700px"
    @close="handleClose"
  >
    <div class="address-manage">
      <!-- 地址列表 -->
      <div class="address-list">
        <div
          v-for="address in addresses"
          :key="address.id"
          class="address-item"
        >
          <div class="address-info">
            <div class="recipient-info">
              <span class="recipient">{{ address.recipient }}</span>
              <span class="phone">{{ address.mobile || address.phone }}</span>
              <el-tag v-if="address.isDefault" type="primary" size="small">默认</el-tag>
            </div>
            <div class="address-detail">
              {{ address.fullAddress || `${address.province}${address.city}${address.district}${address.address}` }}
            </div>
          </div>
          <div class="address-actions">
            <el-button size="small" @click="handleEdit(address)">编辑</el-button>
            <el-button
              v-if="!address.isDefault"
              size="small"
              type="primary"
              @click="handleSetDefault(address.id)"
            >
              设为默认
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(address.id)"
              :disabled="address.isDefault"
            >
              删除
            </el-button>
          </div>
        </div>

        <div v-if="addresses.length === 0" class="no-address">
          <el-icon><Location /></el-icon>
          <p>暂无收货地址</p>
        </div>
      </div>

      <!-- 添加地址按钮 -->
      <div class="add-button">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加新地址
        </el-button>
      </div>
    </div>

    <!-- 地址编辑表单 -->
    <el-dialog
      v-model="formVisible"
      :title="editingAddress ? '编辑地址' : '添加地址'"
      width="500px"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="addressForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="收件人" prop="recipient">
          <el-input v-model="addressForm.recipient" placeholder="请输入收件人姓名" />
        </el-form-item>

        <el-form-item label="手机号码" prop="mobile">
          <el-input v-model="addressForm.mobile" placeholder="请输入手机号码" />
        </el-form-item>

        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="selectedRegion"
            :options="regionOptions"
            :props="cascaderProps"
            placeholder="请选择省市区"
            style="width: 100%"
            @change="handleRegionChange"
          />
        </el-form-item>

        <el-form-item label="详细地址" prop="address">
          <el-input
            v-model="addressForm.address"
            type="textarea"
            :rows="2"
            placeholder="请输入详细地址"
          />
        </el-form-item>

        <el-form-item label="邮政编码" prop="zipCode">
          <el-input v-model="addressForm.zipCode" placeholder="请输入邮政编码（选填）" />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          {{ saving ? '保存中...' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Location, Plus } from '@element-plus/icons-vue'
import {
  getAddressList,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  type AddressVO,
  type AddressDTO
} from '@/api/buyer/address'

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'address-added'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(false)
const formVisible = ref(false)
const addresses = ref<AddressVO[]>([])
const editingAddress = ref<AddressVO | null>(null)
const saving = ref(false)

const formRef = ref<FormInstance>()
const addressForm = ref<AddressDTO>({
  recipient: '',
  mobile: '',
  province: '',
  city: '',
  district: '',
  address: '',
  zipCode: '',
  isDefault: false
})

const selectedRegion = ref<string[]>([])

// 简化的地区数据（实际项目中应该从API获取）
const regionOptions = ref([
  {
    label: '北京市',
    value: '北京市',
    children: [
      {
        label: '市辖区',
        value: '市辖区',
        children: [
          { label: '东城区', value: '东城区' },
          { label: '西城区', value: '西城区' },
          { label: '朝阳区', value: '朝阳区' },
          { label: '丰台区', value: '丰台区' },
          { label: '石景山区', value: '石景山区' },
          { label: '海淀区', value: '海淀区' }
        ]
      }
    ]
  },
  {
    label: '上海市',
    value: '上海市',
    children: [
      {
        label: '市辖区',
        value: '市辖区',
        children: [
          { label: '黄浦区', value: '黄浦区' },
          { label: '徐汇区', value: '徐汇区' },
          { label: '长宁区', value: '长宁区' },
          { label: '静安区', value: '静安区' },
          { label: '普陀区', value: '普陀区' },
          { label: '虹口区', value: '虹口区' }
        ]
      }
    ]
  },
  {
    label: '广东省',
    value: '广东省',
    children: [
      {
        label: '广州市',
        value: '广州市',
        children: [
          { label: '越秀区', value: '越秀区' },
          { label: '海珠区', value: '海珠区' },
          { label: '荔湾区', value: '荔湾区' },
          { label: '天河区', value: '天河区' }
        ]
      },
      {
        label: '深圳市',
        value: '深圳市',
        children: [
          { label: '罗湖区', value: '罗湖区' },
          { label: '福田区', value: '福田区' },
          { label: '南山区', value: '南山区' },
          { label: '宝安区', value: '宝安区' }
        ]
      }
    ]
  }
])

const cascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children'
}

const formRules: FormRules = {
  recipient: [
    { required: true, message: '请输入收件人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  mobile: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 100, message: '详细地址长度在 5 到 100 个字符', trigger: 'blur' }
  ]
}

// 监听props变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadAddresses()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})

// 加载地址列表
const loadAddresses = async () => {
  try {
    addresses.value = await getAddressList()
  } catch (error) {
    console.error('加载地址列表失败:', error)
    ElMessage.error('加载地址列表失败')
  }
}

// 处理地区选择变化
const handleRegionChange = (value: string[]) => {
  if (value && value.length === 3) {
    addressForm.value.province = value[0]
    addressForm.value.city = value[1]
    addressForm.value.district = value[2]
  }
}

// 添加地址
const handleAdd = () => {
  editingAddress.value = null
  addressForm.value = {
    recipient: '',
    mobile: '',
    province: '',
    city: '',
    district: '',
    address: '',
    zipCode: '',
    isDefault: false
  }
  selectedRegion.value = []
  formVisible.value = true
}

// 编辑地址
const handleEdit = (address: AddressVO) => {
  editingAddress.value = address
  addressForm.value = {
    recipient: address.recipient,
    mobile: address.mobile || '',
    province: address.province,
    city: address.city,
    district: address.district,
    address: address.address,
    zipCode: address.zipCode || '',
    isDefault: address.isDefault
  }
  selectedRegion.value = [address.province, address.city, address.district]
  formVisible.value = true
}

// 保存地址
const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      if (editingAddress.value) {
        await updateAddress(editingAddress.value.id, addressForm.value)
        ElMessage.success('地址更新成功')
      } else {
        await addAddress(addressForm.value)
        ElMessage.success('地址添加成功')
        emit('address-added')
      }
      
      formVisible.value = false
      loadAddresses()
    } catch (error: any) {
      console.error('保存地址失败:', error)
      ElMessage.error(error.response?.data?.message || '保存地址失败')
    } finally {
      saving.value = false
    }
  })
}

// 设为默认地址
const handleSetDefault = async (id: number) => {
  try {
    await setDefaultAddress(id)
    ElMessage.success('默认地址设置成功')
    loadAddresses()
  } catch (error: any) {
    console.error('设置默认地址失败:', error)
    ElMessage.error(error.response?.data?.message || '设置默认地址失败')
  }
}

// 删除地址
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteAddress(id)
    ElMessage.success('地址删除成功')
    loadAddresses()
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('删除地址失败:', error)
    ElMessage.error(error.response?.data?.message || '删除地址失败')
  }
}

// 关闭弹框
const handleClose = () => {
  visible.value = false
}

onMounted(() => {
  if (props.modelValue) {
    loadAddresses()
  }
})
</script>

<style scoped lang="scss">
.address-manage {
  .address-list {
    max-height: 400px;
    overflow-y: auto;
    margin-bottom: 20px;

    .address-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 16px;
      border: 1px solid #eee;
      border-radius: 8px;
      margin-bottom: 12px;
      background: #fafafa;

      .address-info {
        flex: 1;

        .recipient-info {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 8px;

          .recipient {
            font-weight: 500;
            color: #333;
          }

          .phone {
            color: #666;
          }
        }

        .address-detail {
          color: #999;
          line-height: 1.5;
        }
      }

      .address-actions {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }

    .no-address {
      text-align: center;
      padding: 40px;
      color: #999;

      .el-icon {
        font-size: 48px;
        margin-bottom: 16px;
        color: #ddd;
      }

      p {
        margin: 0;
        font-size: 14px;
      }
    }
  }

  .add-button {
    text-align: center;
    padding: 16px;
    border: 2px dashed #ddd;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      background: #f0f9ff;
    }
  }
}
</style>