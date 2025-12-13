<template>
  <el-dialog
    v-model="visible"
    title="确认订单"
    width="800px"
    @close="handleClose"
  >
    <div class="order-confirm">
      <!-- 商品信息 -->
      <div class="product-section">
        <h4>商品信息</h4>
        <div class="product-item">
          <el-image
            :src="productInfo.mainImage"
            fit="cover"
            style="width: 80px; height: 80px"
            class="product-image"
          />
          <div class="product-details">
            <div class="product-name">{{ productInfo.name }}</div>
            <div class="product-specs" v-if="productInfo.specs">
              <span v-for="(value, key) in productInfo.specs" :key="key" class="spec-item">
                {{ key }}: {{ value }}
              </span>
            </div>
            <div class="product-price">
              ¥{{ parseFloat(productInfo.price).toFixed(2) }} × {{ quantity }}
            </div>
          </div>
          <div class="product-total">
            ¥{{ parseFloat(productInfo.price * quantity).toFixed(2) }}
          </div>
        </div>
      </div>

      <!-- 收货地址 -->
      <div class="address-section">
        <h4>收货地址</h4>
        <div v-if="addresses.length === 0" class="no-address">
          <el-icon><Warning /></el-icon>
          <span>暂无收货地址，请先添加收货地址</span>
          <el-button type="primary" size="small" @click="showAddressDialog = true">
            添加地址
          </el-button>
        </div>
        <el-radio-group v-else v-model="selectedAddressId" class="address-list">
          <div
            v-for="address in addresses"
            :key="address.id"
            class="address-item"
            :class="{ active: selectedAddressId === address.id }"
            @click="selectedAddressId = address.id"
          >
            <el-radio :label="address.id">
              <div class="address-content">
                <div class="recipient-info">
                  <span class="recipient">{{ address.recipient }}</span>
                  <span class="phone">{{ address.mobile || address.phone }}</span>
                  <el-tag v-if="address.isDefault" type="primary" size="small">默认</el-tag>
                </div>
                <div class="address-detail">
                  {{ address.fullAddress || `${address.province}${address.city}${address.district}${address.address}` }}
                </div>
              </div>
            </el-radio>
          </div>
        </el-radio-group>
      </div>

      <!-- 支付方式 -->
      <div class="payment-section">
        <h4>支付方式</h4>
        <el-radio-group v-model="paymentMethod">
          <el-radio label="微信支付" class="payment-option">
            <el-icon><CreditCard /></el-icon>
            微信支付
          </el-radio>
          <el-radio label="支付宝" class="payment-option">
            <el-icon><CreditCard /></el-icon>
            支付宝
          </el-radio>
          <el-radio label="银行卡" class="payment-option">
            <el-icon><CreditCard /></el-icon>
            银行卡支付
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 订单备注 -->
      <div class="remark-section">
        <h4>订单备注</h4>
        <el-input
          v-model="orderRemark"
          type="textarea"
          :rows="3"
          placeholder="请填写订单备注信息（选填）"
          maxlength="200"
          show-word-limit
        />
      </div>

      <!-- 费用明细 -->
      <div class="cost-section">
        <div class="cost-item">
          <span>商品总价：</span>
          <span>¥{{ parseFloat(productInfo.price * quantity).toFixed(2) }}</span>
        </div>
        <div class="cost-item">
          <span>运费：</span>
          <span>¥0.00</span>
        </div>
        <div class="cost-item total">
          <span>应付总额：</span>
          <span class="total-amount">¥{{ parseFloat(productInfo.price * quantity).toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting"
          :disabled="!canSubmit"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '确认支付' }}
        </el-button>
      </div>
    </template>

    <!-- 地址管理弹框 -->
    <AddressManage
      v-model="showAddressDialog"
      @address-added="handleAddressAdded"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, CreditCard } from '@element-plus/icons-vue'
import { getAddressList, type AddressVO } from '@/api/buyer/address'
import { createOrder, type CreateOrderDTO } from '@/api/buyer/order'
import AddressManage from '@/components/user/AddressManage.vue'

interface ProductInfo {
  id: number
  name: string
  mainImage: string
  price: number
  specs?: Record<string, string>
  skuId?: number
}

interface Props {
  modelValue: boolean
  productInfo: ProductInfo
  quantity: number
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'order-created', orderNo: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(false)
const addresses = ref<AddressVO[]>([])
const selectedAddressId = ref<number>()
const paymentMethod = ref('微信支付')
const orderRemark = ref('')
const submitting = ref(false)
const showAddressDialog = ref(false)

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

// 是否可以提交订单
const canSubmit = computed(() => {
  return selectedAddressId.value && paymentMethod.value && addresses.value.length > 0
})

// 加载收货地址
const loadAddresses = async () => {
  try {
    addresses.value = await getAddressList()
    // 自动选择默认地址
    const defaultAddress = addresses.value.find(addr => addr.isDefault)
    if (defaultAddress) {
      selectedAddressId.value = defaultAddress.id
    } else if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id
    }
  } catch (error) {
    console.error('加载收货地址失败:', error)
    ElMessage.error('加载收货地址失败')
  }
}

// 处理地址添加成功
const handleAddressAdded = () => {
  loadAddresses()
}

// 提交订单
const handleSubmit = async () => {
  if (!canSubmit.value) {
    ElMessage.warning('请完善订单信息')
    return
  }

  submitting.value = true
  try {
    const orderData: CreateOrderDTO = {
      addressId: selectedAddressId.value!,
      items: [{
        productId: props.productInfo.id,
        quantity: props.quantity,
        ...(props.productInfo.skuId && { skuId: props.productInfo.skuId })
      }],
      paymentMethod: paymentMethod.value,
      orderRemark: orderRemark.value || undefined
    }

    const orderNo = await createOrder(orderData)
    ElMessage.success('订单创建成功！')
    
    emit('order-created', orderNo)
    handleClose()
  } catch (error: any) {
    console.error('创建订单失败:', error)
    ElMessage.error(error.response?.data?.message || '创建订单失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 关闭弹框
const handleClose = () => {
  visible.value = false
  // 重置表单
  selectedAddressId.value = undefined
  paymentMethod.value = '微信支付'
  orderRemark.value = ''
  submitting.value = false
}

onMounted(() => {
  if (props.modelValue) {
    loadAddresses()
  }
})
</script>

<style scoped lang="scss">
.order-confirm {
  .product-section,
  .address-section,
  .payment-section,
  .remark-section,
  .cost-section {
    margin-bottom: 24px;
    
    h4 {
      margin: 0 0 16px 0;
      font-size: 16px;
      color: #333;
      border-bottom: 1px solid #eee;
      padding-bottom: 8px;
    }
  }

  .product-item {
    display: flex;
    gap: 15px;
    padding: 15px;
    border: 1px solid #eee;
    border-radius: 8px;
    background: #fafafa;

    .product-image {
      border-radius: 4px;
    }

    .product-details {
      flex: 1;

      .product-name {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 8px;
        color: #333;
      }

      .product-specs {
        margin-bottom: 8px;

        .spec-item {
          display: inline-block;
          background: #f0f0f0;
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 12px;
          margin-right: 8px;
          color: #666;
        }
      }

      .product-price {
        color: #999;
        font-size: 14px;
      }
    }

    .product-total {
      font-size: 18px;
      font-weight: 600;
      color: #e4393c;
    }
  }

  .no-address {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 20px;
    background: #fff7f0;
    border: 1px solid #ffe6cc;
    border-radius: 8px;
    color: #d46b08;
  }

  .address-list {
    .address-item {
      border: 1px solid #eee;
      border-radius: 8px;
      margin-bottom: 12px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover,
      &.active {
        border-color: #409eff;
        box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
      }

      :deep(.el-radio) {
        width: 100%;
        padding: 16px;
        margin: 0;

        .el-radio__label {
          width: 100%;
          padding-left: 12px;
        }
      }

      .address-content {
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
    }
  }

  .payment-section {
    .payment-option {
      display: block;
      margin-bottom: 12px;
      padding: 12px;
      border: 1px solid #eee;
      border-radius: 8px;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
      }

      :deep(.el-radio__label) {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
  }

  .cost-section {
    background: #fafafa;
    padding: 16px;
    border-radius: 8px;
    border: 1px solid #eee;

    .cost-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 14px;

      &:last-child {
        margin-bottom: 0;
      }

      &.total {
        border-top: 1px solid #ddd;
        padding-top: 8px;
        font-size: 16px;
        font-weight: 600;

        .total-amount {
          color: #e4393c;
          font-size: 20px;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>