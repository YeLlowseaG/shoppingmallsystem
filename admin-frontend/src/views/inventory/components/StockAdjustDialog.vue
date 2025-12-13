<template>
  <el-dialog
    v-model="visible"
    title="库存调整"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="adjustForm"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="商品信息">
        <div class="product-info">
          <el-image
            :src="stockItem?.productImage"
            style="width: 60px; height: 60px; margin-right: 12px"
            fit="cover"
          />
          <div>
            <div class="product-name">{{ stockItem?.productName }}</div>
            <div class="product-code">编码：{{ stockItem?.productCode }}</div>
            <div v-if="stockItem?.skuSpecs" class="product-specs">{{ stockItem?.skuSpecs }}</div>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="当前库存">
        <span class="current-stock">{{ stockItem?.currentStock || 0 }} 件</span>
      </el-form-item>

      <el-form-item label="调整类型" prop="adjustType">
        <el-radio-group v-model="adjustForm.adjustType">
          <el-radio label="add">增加库存</el-radio>
          <el-radio label="reduce">减少库存</el-radio>
          <el-radio label="set">设置库存</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="调整数量" prop="quantity">
        <el-input-number
          v-model="adjustForm.quantity"
          :min="adjustForm.adjustType === 'set' ? 0 : 1"
          :max="adjustForm.adjustType === 'reduce' ? stockItem?.currentStock : 999999"
          style="width: 100%"
        />
        <div class="adjust-hint">
          <span v-if="adjustForm.adjustType === 'add'">
            调整后库存：{{ (stockItem?.currentStock || 0) + (adjustForm.quantity || 0) }} 件
          </span>
          <span v-else-if="adjustForm.adjustType === 'reduce'">
            调整后库存：{{ Math.max(0, (stockItem?.currentStock || 0) - (adjustForm.quantity || 0)) }} 件
          </span>
          <span v-else-if="adjustForm.adjustType === 'set'">
            调整后库存：{{ adjustForm.quantity || 0 }} 件
          </span>
        </div>
      </el-form-item>

      <el-form-item label="调整原因" prop="reason">
        <el-select
          v-model="adjustForm.reason"
          placeholder="请选择调整原因"
          style="width: 100%"
        >
          <el-option label="进货入库" value="purchase" />
          <el-option label="销售出库" value="sale" />
          <el-option label="损耗报废" value="damage" />
          <el-option label="盘点调整" value="inventory" />
          <el-option label="系统纠错" value="correction" />
          <el-option label="其他原因" value="other" />
        </el-select>
      </el-form-item>

      <el-form-item label="备注说明" prop="remark">
        <el-input
          v-model="adjustForm.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入调整说明（可选）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '调整中...' : '确认调整' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

interface Props {
  modelValue: boolean
  stockItem: any
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const adjustForm = ref({
  adjustType: 'add',
  quantity: 1,
  reason: '',
  remark: ''
})

const formRules: FormRules = {
  adjustType: [
    { required: true, message: '请选择调整类型', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入调整数量', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value || value <= 0) {
          callback(new Error('调整数量必须大于0'))
        } else if (adjustForm.value.adjustType === 'reduce' && value > (props.stockItem?.currentStock || 0)) {
          callback(new Error('减少数量不能超过当前库存'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  reason: [
    { required: true, message: '请选择调整原因', trigger: 'change' }
  ]
}

// 监听props变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    resetForm()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})

// 重置表单
const resetForm = () => {
  adjustForm.value = {
    adjustType: 'add',
    quantity: 1,
    reason: '',
    remark: ''
  }
  formRef.value?.clearValidate()
}

// 提交调整
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      // 这里应该调用实际的库存调整API
      console.log('调整库存:', {
        stockId: props.stockItem?.id,
        skuId: props.stockItem?.skuId,
        productId: props.stockItem?.productId,
        ...adjustForm.value
      })
      
      // 模拟API请求
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      ElMessage.success('库存调整成功')
      emit('success')
      handleClose()
    } catch (error) {
      console.error('库存调整失败:', error)
      ElMessage.error('库存调整失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}

// 关闭弹框
const handleClose = () => {
  visible.value = false
}
</script>

<style scoped lang="scss">
.product-info {
  display: flex;
  align-items: flex-start;
  
  .product-name {
    font-weight: 500;
    color: #303133;
    margin-bottom: 4px;
  }
  
  .product-code {
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
  }
  
  .product-specs {
    font-size: 12px;
    color: #67c23a;
    background: #f0f9ff;
    padding: 2px 6px;
    border-radius: 4px;
    display: inline-block;
  }
}

.current-stock {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
}

.adjust-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>