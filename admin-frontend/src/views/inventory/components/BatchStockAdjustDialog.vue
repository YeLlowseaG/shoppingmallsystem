<template>
  <el-dialog
    v-model="visible"
    title="批量库存调整"
    width="700px"
    @close="handleClose"
  >
    <div class="batch-adjust">
      <!-- 选中商品列表 -->
      <div class="selected-items">
        <h4>已选择 {{ selectedItems.length }} 个商品</h4>
        <div class="items-list">
          <div 
            v-for="item in selectedItems" 
            :key="item.id"
            class="item-row"
          >
            <el-image
              :src="item.productImage"
              style="width: 40px; height: 40px; margin-right: 8px"
              fit="cover"
            />
            <div class="item-info">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-code">{{ item.productCode }}</div>
              <div v-if="item.skuSpecs" class="item-specs">{{ item.skuSpecs }}</div>
            </div>
            <div class="item-stock">当前：{{ item.currentStock }}</div>
          </div>
        </div>
      </div>

      <!-- 调整表单 -->
      <el-form
        ref="formRef"
        :model="batchForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="调整类型" prop="adjustType">
          <el-radio-group v-model="batchForm.adjustType">
            <el-radio label="add">统一增加</el-radio>
            <el-radio label="reduce">统一减少</el-radio>
            <el-radio label="set">设置为</el-radio>
            <el-radio label="percentage">按比例调整</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item 
          :label="getQuantityLabel()" 
          prop="quantity"
        >
          <el-input-number
            v-model="batchForm.quantity"
            :min="getMinValue()"
            :max="getMaxValue()"
            :precision="batchForm.adjustType === 'percentage' ? 2 : 0"
            style="width: 200px"
          />
          <span class="quantity-unit">{{ getQuantityUnit() }}</span>
        </el-form-item>

        <el-form-item label="调整原因" prop="reason">
          <el-select
            v-model="batchForm.reason"
            placeholder="请选择调整原因"
            style="width: 100%"
          >
            <el-option label="批量进货" value="bulk_purchase" />
            <el-option label="批量销售" value="bulk_sale" />
            <el-option label="批量损耗" value="bulk_damage" />
            <el-option label="盘点调整" value="inventory_check" />
            <el-option label="促销活动" value="promotion" />
            <el-option label="其他原因" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注说明" prop="remark">
          <el-input
            v-model="batchForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入批量调整说明（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 预览调整结果 -->
        <el-form-item label="调整预览">
          <el-table 
            :data="previewData" 
            border 
            size="small"
            max-height="300"
          >
            <el-table-column prop="productName" label="商品" min-width="200" />
            <el-table-column prop="currentStock" label="当前库存" width="100" align="center" />
            <el-table-column prop="adjustAmount" label="调整量" width="100" align="center">
              <template #default="{ row }">
                <span :style="{ color: row.adjustAmount >= 0 ? '#67c23a' : '#f56c6c' }">
                  {{ row.adjustAmount > 0 ? '+' : '' }}{{ row.adjustAmount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="newStock" label="调整后" width="100" align="center">
              <template #default="{ row }">
                <span :style="{ color: row.newStock <= row.warningStock ? '#e6a23c' : '#67c23a' }">
                  {{ row.newStock }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '调整中...' : '确认批量调整' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'

interface Props {
  modelValue: boolean
  selectedItems: any[]
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

const batchForm = ref({
  adjustType: 'add',
  quantity: 1,
  reason: '',
  remark: ''
})

// 计算调整预览数据
const previewData = computed(() => {
  return props.selectedItems.map(item => {
    let adjustAmount = 0
    let newStock = item.currentStock

    switch (batchForm.value.adjustType) {
      case 'add':
        adjustAmount = batchForm.value.quantity || 0
        newStock = item.currentStock + adjustAmount
        break
      case 'reduce':
        adjustAmount = -(batchForm.value.quantity || 0)
        newStock = Math.max(0, item.currentStock + adjustAmount)
        break
      case 'set':
        adjustAmount = (batchForm.value.quantity || 0) - item.currentStock
        newStock = batchForm.value.quantity || 0
        break
      case 'percentage':
        const percentage = (batchForm.value.quantity || 0) / 100
        adjustAmount = Math.floor(item.currentStock * percentage)
        newStock = item.currentStock + adjustAmount
        break
    }

    return {
      ...item,
      adjustAmount,
      newStock: Math.max(0, newStock)
    }
  })
})

const formRules: FormRules = {
  adjustType: [
    { required: true, message: '请选择调整类型', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入调整数量', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value === null || value === undefined) {
          callback(new Error('请输入调整数量'))
        } else if (value <= 0 && batchForm.value.adjustType !== 'set') {
          callback(new Error('调整数量必须大于0'))
        } else if (batchForm.value.adjustType === 'set' && value < 0) {
          callback(new Error('设置库存不能为负数'))
        } else if (batchForm.value.adjustType === 'percentage' && (value < -100 || value > 1000)) {
          callback(new Error('调整比例应在-100%到1000%之间'))
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

// 获取数量标签
const getQuantityLabel = () => {
  switch (batchForm.value.adjustType) {
    case 'add': return '增加数量'
    case 'reduce': return '减少数量'
    case 'set': return '设置库存'
    case 'percentage': return '调整比例'
    default: return '数量'
  }
}

// 获取最小值
const getMinValue = () => {
  if (batchForm.value.adjustType === 'set') return 0
  if (batchForm.value.adjustType === 'percentage') return -100
  return 1
}

// 获取最大值
const getMaxValue = () => {
  if (batchForm.value.adjustType === 'percentage') return 1000
  return 999999
}

// 获取单位
const getQuantityUnit = () => {
  return batchForm.value.adjustType === 'percentage' ? '%' : '件'
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
  batchForm.value = {
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

    // 确认对话框
    const totalItems = props.selectedItems.length
    const confirmText = `确定要对 ${totalItems} 个商品进行批量库存调整吗？此操作不可撤销！`
    
    try {
      await ElMessageBox.confirm(confirmText, '确认批量调整', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }

    submitting.value = true
    try {
      // 这里应该调用实际的批量库存调整API
      console.log('批量调整库存:', {
        items: props.selectedItems.map(item => ({
          stockId: item.id,
          skuId: item.skuId,
          productId: item.productId
        })),
        ...batchForm.value,
        previewData: previewData.value
      })
      
      // 模拟API请求
      await new Promise(resolve => setTimeout(resolve, 2000))
      
      ElMessage.success(`成功调整 ${totalItems} 个商品的库存`)
      emit('success')
      handleClose()
    } catch (error) {
      console.error('批量库存调整失败:', error)
      ElMessage.error('批量库存调整失败，请重试')
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
.batch-adjust {
  .selected-items {
    margin-bottom: 20px;
    
    h4 {
      color: #303133;
      margin-bottom: 12px;
      font-size: 14px;
    }
    
    .items-list {
      max-height: 200px;
      overflow-y: auto;
      border: 1px solid #ebeef5;
      border-radius: 4px;
      
      .item-row {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        border-bottom: 1px solid #f5f7fa;
        
        &:last-child {
          border-bottom: none;
        }
        
        .item-info {
          flex: 1;
          
          .item-name {
            font-size: 13px;
            color: #303133;
            margin-bottom: 2px;
          }
          
          .item-code {
            font-size: 11px;
            color: #909399;
            margin-bottom: 2px;
          }
          
          .item-specs {
            font-size: 11px;
            color: #67c23a;
            background: #f0f9ff;
            padding: 1px 4px;
            border-radius: 2px;
            display: inline-block;
          }
        }
        
        .item-stock {
          font-size: 12px;
          color: #409eff;
          font-weight: bold;
          min-width: 80px;
          text-align: right;
        }
      }
    }
  }
  
  .quantity-unit {
    margin-left: 8px;
    color: #909399;
    font-size: 14px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>