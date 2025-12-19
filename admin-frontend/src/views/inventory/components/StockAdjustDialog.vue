<template>
  <el-dialog
    v-model="visible"
    title="库存调整"
    width="700px"
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
          </div>
        </div>
      </el-form-item>

      <!-- 有多个SKU时显示SKU列表 -->
      <el-form-item v-if="skuList.length > 0" label="SKU库存">
        <el-table :data="skuList" border size="small" class="sku-table">
          <el-table-column prop="specText" label="规格" min-width="120" />
          <el-table-column prop="stock" label="当前库存" width="100" align="center">
            <template #default="{ row }">
              <span :class="{ 'warning-stock': row.stock <= row.warningStock }">{{ row.stock }}</span>
            </template>
          </el-table-column>
          <el-table-column label="调整后库存" width="150" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.newStock"
                :min="0"
                :max="999999"
                size="small"
                controls-position="right"
              />
            </template>
          </el-table-column>
          <el-table-column label="变化" width="80" align="center">
            <template #default="{ row }">
              <span :class="getChangeClass(row)">
                {{ getChangeText(row) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-form-item>

      <!-- 无SKU时显示单个库存调整 -->
      <template v-else>
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
      </template>

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
import { updateSkuStock, getSkusByProductId } from '@/api/admin/sku'
import { adjustStock as adjustProductStock } from '@/api/admin/stock'

interface SkuItem {
  skuId: number
  skuCode: string
  specText: string
  stock: number
  warningStock: number
  newStock: number
}

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
const skuList = ref<SkuItem[]>([])

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
    { required: true, message: '请输入调整数量', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请选择调整原因', trigger: 'change' }
  ]
}

// 获取变化样式
const getChangeClass = (row: SkuItem) => {
  const diff = row.newStock - row.stock
  if (diff > 0) return 'change-positive'
  if (diff < 0) return 'change-negative'
  return 'change-zero'
}

// 获取变化文本
const getChangeText = (row: SkuItem) => {
  const diff = row.newStock - row.stock
  if (diff > 0) return `+${diff}`
  if (diff < 0) return `${diff}`
  return '0'
}

// 解析规格文本
const parseSpecText = (specCombination: string) => {
  try {
    const specs = JSON.parse(specCombination)
    return Object.entries(specs).map(([key, value]) => `${key}: ${value}`).join(', ')
  } catch {
    return specCombination || '默认规格'
  }
}

// 加载SKU列表
const loadSkuList = async (productId: number) => {
  try {
    const response = await getSkusByProductId(productId)
    const list = Array.isArray(response) ? response : (response as any)?.data || []

    if (list.length > 0) {
      skuList.value = list.map((sku: any) => ({
        skuId: sku.id,
        skuCode: sku.skuCode,
        specText: parseSpecText(sku.specCombination),
        stock: sku.stock || 0,
        warningStock: sku.warningStock || 10,
        newStock: sku.stock || 0
      }))
    } else {
      skuList.value = []
    }
  } catch (error) {
    console.error('获取SKU列表失败:', error)
    skuList.value = []
  }
}

// 监听props变化
watch(() => props.modelValue, async (val) => {
  visible.value = val
  if (val && props.stockItem) {
    resetForm()
    // 加载该商品的所有SKU
    await loadSkuList(props.stockItem.productId)
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
  skuList.value = []
  formRef.value?.clearValidate()
}

// 提交调整
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (skuList.value.length > 0) {
        // 有SKU，批量调整每个SKU的库存
        const changedSkus = skuList.value.filter(sku => sku.newStock !== sku.stock)

        if (changedSkus.length === 0) {
          ElMessage.warning('没有修改任何SKU的库存')
          submitting.value = false
          return
        }

        // 逐个调整SKU库存
        for (const sku of changedSkus) {
          await updateSkuStock(sku.skuId, sku.newStock)
        }

        ElMessage.success(`成功调整 ${changedSkus.length} 个SKU的库存`)
      } else {
        // 无SKU，调整商品级别库存
        const currentStock = props.stockItem?.currentStock || 0
        let newStock = 0

        if (adjustForm.value.adjustType === 'add') {
          newStock = currentStock + (adjustForm.value.quantity || 0)
        } else if (adjustForm.value.adjustType === 'reduce') {
          newStock = Math.max(0, currentStock - (adjustForm.value.quantity || 0))
        } else if (adjustForm.value.adjustType === 'set') {
          newStock = adjustForm.value.quantity || 0
        }

        const adjustQuantity = newStock - currentStock
        await adjustProductStock({
          productId: props.stockItem.productId,
          adjustQuantity: adjustQuantity,
          reason: `${adjustForm.value.reason}: ${adjustForm.value.remark || ''}`
        })

        ElMessage.success('库存调整成功')
      }

      emit('success')
      handleClose()
    } catch (error: any) {
      console.error('库存调整失败:', error)
      ElMessage.error(error.message || '库存调整失败，请重试')
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
  }
}

.sku-table {
  width: 100%;

  .warning-stock {
    color: #f56c6c;
    font-weight: bold;
  }
}

.change-positive {
  color: #67c23a;
  font-weight: bold;
}

.change-negative {
  color: #f56c6c;
  font-weight: bold;
}

.change-zero {
  color: #909399;
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
