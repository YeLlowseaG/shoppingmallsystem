<template>
  <div class="checkout-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 结算内容区 -->
    <div class="checkout-content">
      <div class="container">
        <!-- 购物流程进度条 -->
        <div class="checkout-progress">
          <div class="progress-steps">
            <div class="step">
              <div class="step-number">1</div>
              <div class="step-text">查看购物车</div>
            </div>
            <div class="step-line"></div>
            <div class="step">
              <div class="step-number">2</div>
              <div class="step-text">用户登录或注册</div>
            </div>
            <div class="step-line"></div>
            <div class="step active">
              <div class="step-number">3</div>
              <div class="step-text">填写购物信息</div>
            </div>
            <div class="step-line"></div>
            <div class="step">
              <div class="step-number">4</div>
              <div class="step-text">完成订单</div>
            </div>
          </div>
          <div class="cart-icon">
            <el-icon class="icon"><ShoppingCart /></el-icon>
            <div class="icon-text">我的购物车</div>
            <div class="icon-text-en">my shopping cart</div>
          </div>
        </div>

        <!-- 主内容区域 -->
        <div class="checkout-main">
          <!-- 填写收货人信息 -->
          <div class="checkout-section">
            <div class="section-header">
              <h3 class="section-title">填写收货人信息</h3>
            </div>

            <!-- 确认收货地址 -->
            <div class="address-selection">
              <div class="address-label">确认收货地址：</div>
              <div class="address-list">
                <div class="address-radio-group">
                  <label
                    v-for="address in addressList"
                    :key="address.id"
                    class="address-radio-item"
                  >
                    <input
                      type="radio"
                      :value="address.id"
                      v-model="selectedAddressId"
                      class="address-radio-input"
                    />
                    <span class="address-radio-label">
                      <div class="address-content">
                        <span class="address-region">{{ address.province }} {{ address.city }} {{ address.district }}</span>
                        <span class="address-detail">{{ address.address }}</span>
                        <span class="address-recipient">
                          (收货人:{{ address.recipient }} 手机:{{ address.mobile || address.phone }} 邮编:{{ address.zipCode || '-' }})
                        </span>
                        <el-button type="text" class="edit-link" @click="handleEditAddress(address)">
                          编辑
                        </el-button>
                      </div>
                    </span>
                  </label>
                  <label class="address-radio-item">
                    <input
                      type="radio"
                      value="other"
                      v-model="selectedAddressId"
                      class="address-radio-input"
                    />
                    <span class="address-radio-label">
                      <span class="other-address-text">其他收货地址</span>
                    </span>
                  </label>
                </div>
              </div>
            </div>

            <!-- 收货地址表单 -->
            <div v-if="selectedAddressId === 'other' || showAddressForm || addressList.length === 0" class="address-form">
              <table class="address-form-table">
                <tr>
                  <td class="form-label">
                    <span class="required-mark">*</span>收货地区:
                  </td>
                  <td class="form-input">
                    <RegionSelector
                      v-model="regionData"
                      @change="handleRegionChange"
                    />
                  </td>
                </tr>
                <tr>
                  <td class="form-label">
                    <span class="required-mark">*</span>街道地址:
                  </td>
                  <td class="form-input">
                    <el-input
                      v-model="addressForm.detailAddress"
                      placeholder="请输入详细地址"
                      style="width: 500px"
                    />
                    <div class="form-tip">不允许输入特殊字符</div>
                  </td>
                </tr>
                <tr>
                  <td class="form-label">邮编:</td>
                  <td class="form-input">
                    <el-input
                      v-model="addressForm.zipCode"
                      placeholder="请输入邮编"
                      style="width: 300px"
                    />
                  </td>
                </tr>
                <tr>
                  <td class="form-label">
                    <span class="required-mark">*</span>收货人姓名:
                  </td>
                  <td class="form-input">
                    <el-input
                      v-model="addressForm.receiverName"
                      placeholder="请输入收货人姓名"
                      style="width: 300px"
                    />
                    <span class="form-tip-inline">请填写真实姓名,以免延误收货</span>
                  </td>
                </tr>
                <tr>
                  <td class="form-label">
                    <span class="required-mark">*</span>联系电话:
                  </td>
                  <td class="form-input">
                    <el-input
                      v-model="addressForm.receiverPhone"
                      placeholder="请输入联系电话"
                      style="width: 300px"
                    />
                    <span class="form-tip-inline">手机和电话填写一项即可</span>
                  </td>
                </tr>
                <tr>
                  <td class="form-label">
                    <span class="required-mark">*</span>联系手机:
                  </td>
                  <td class="form-input">
                    <el-input
                      v-model="addressForm.receiverMobile"
                      placeholder="请输入联系手机"
                      style="width: 300px"
                    />
                    <span class="form-tip-inline">手机和电话填写一项即可</span>
                  </td>
                </tr>
                <tr>
                  <td class="form-label">是否保存地址:</td>
                  <td class="form-input">
                    <el-checkbox v-model="addressForm.saveAddress">保存本次收货地址</el-checkbox>
                  </td>
                </tr>
              </table>
            </div>

            <!-- 送货日期和时间（已屏蔽） -->
            <!-- <div class="delivery-time">
              <div class="time-item">
                <span class="time-label">送货日期：</span>
                <el-select v-model="deliveryDate" placeholder="任意日期" style="width: 200px">
                  <el-option label="任意日期" value="any" />
                  <el-option label="仅工作日" value="weekdays" />
                  <el-option label="仅休息日" value="weekends" />
                  <el-option label="指定日期" value="specific" />
                </el-select>
              </div>
              <div class="time-item">
                <span class="time-label">时间：</span>
                <el-select v-model="deliveryTime" placeholder="任意时间段" style="width: 200px">
                  <el-option label="任意时间段" value="any" />
                  <el-option label="上午" value="morning" />
                  <el-option label="下午" value="afternoon" />
                  <el-option label="晚上" value="evening" />
                </el-select>
              </div>
            </div> -->

            <!-- 订单附言 -->
            <div class="order-remarks">
              <div class="remarks-label">订单附言：</div>
              <el-input
                v-model="orderRemarks"
                type="textarea"
                :rows="4"
                placeholder="请输入订单附言（选填）"
                style="width: 100%"
              />
            </div>
          </div>

          <!-- 选择配送方式（已屏蔽） -->
          <!-- <div class="checkout-section">
            <div class="section-header">
              <h3 class="section-title">
                选择配送方式
                <el-button type="text" class="modify-link" @click="showShippingDialog = true">修改</el-button>
              </h3>
            </div>
            <div class="shipping-info">
              <div class="info-item">
                <span class="info-label">配送方式：</span>
                <span class="info-value">{{ selectedShippingMethod.name }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">运费：</span>
                <span class="info-value shipping-fee">+¥{{ selectedShippingMethod.price.toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <el-dialog
            v-model="showShippingDialog"
            title="选择配送方式"
            width="800px"
            class="shipping-dialog"
          >
            <div class="shipping-methods-list">
              <div class="shipping-radio-group">
                <label
                  v-for="method in shippingMethods"
                  :key="method.id"
                  class="shipping-radio-item"
                >
                  <input
                    type="radio"
                    :value="method.id"
                    v-model="selectedShippingMethodId"
                    class="shipping-radio-input"
                  />
                  <span class="shipping-radio-label">
                    <div class="shipping-method-content">
                      <div class="method-header">
                        <span class="method-name">{{ method.name }}</span>
                        <span class="method-price">+¥{{ method.price.toFixed(2) }}</span>
                      </div>
                      <div v-if="method.description" class="method-description">
                        {{ method.description }}
                      </div>
                    </div>
                  </span>
                </label>
              </div>
            </div>
            <template #footer>
              <el-button @click="showShippingDialog = false">取消</el-button>
              <el-button type="danger" @click="handleConfirmShipping">确定</el-button>
            </template>
          </el-dialog> -->

          <!-- 选择支付方式 -->
          <div class="checkout-section">
            <div class="section-header">
              <h3 class="section-title">
                选择支付方式
                <el-button type="text" class="modify-link" @click="showPaymentOptions = !showPaymentOptions">
                  {{ showPaymentOptions ? '收起' : '修改' }}
                </el-button>
              </h3>
            </div>
            <div class="payment-info">
              <div class="info-item">
                <span class="info-label">选择支付币别：</span>
                <el-select v-model="paymentCurrency" style="width: 150px">
                  <el-option label="人民币" value="CNY" />
                </el-select>
              </div>
              <div v-if="!showPaymentOptions" class="info-item">
                <span class="info-label">支付方式：</span>
                <span class="info-value">{{ selectedPaymentMethod.name }}</span>
              </div>
            </div>

            <!-- 支付方式选择列表（展开显示） -->
            <div v-if="showPaymentOptions" class="payment-methods-list">
              <div class="payment-radio-group">
                <div
                  v-for="method in paymentMethods"
                  :key="method.id"
                  class="payment-radio"
                  :class="{ 'is-checked': paymentMethod === method.id }"
                  @click="paymentMethod = method.id"
                >
                  <span class="payment-icon">{{ paymentMethod === method.id ? '●' : '○' }}</span>
                  <span class="payment-name">{{ method.name }}</span>
                  <span class="payment-desc">{{ method.description }}</span>
                  <span v-if="method.id === 'pre_deposit' && paymentMethod === 'pre_deposit'" class="deposit-balance">
                    预存款余额：¥{{ depositBalance.toFixed(2) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 购买的商品 -->
          <div class="checkout-section">
            <div class="section-header">
              <h3 class="section-title">购买的商品</h3>
            </div>
            <div class="products-table-wrapper">
              <table class="products-table">
                <thead>
                  <tr>
                    <th class="col-code">货号</th>
                    <th class="col-name">商品名称</th>
                    <th class="col-price">{{ getPriceColumnTitle() }}</th>
                    <th v-if="isMember" class="col-price">销售价格</th>
                    <th class="col-quantity">数量</th>
                    <th class="col-subtotal">小计</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in orderItems" :key="item.id" class="product-row">
                    <td class="col-code">{{ item.productCode }}</td>
                  <td class="col-name">
                    <div class="product-name">{{ item.name }}</div>
                    <div v-if="item.specText" class="sku-spec-text">规格：{{ item.specText }}</div>
                  </td>
                    <td class="col-price">
                      <span class="member-price">¥{{ (item.memberPrice || 0).toFixed(2) }}</span>
                    </td>
                    <td v-if="isMember" class="col-price">
                      <span class="sales-price">¥{{ (item.salesPrice || 0).toFixed(2) }}</span>
                    </td>
                    <td class="col-quantity">{{ item.quantity }}</td>
                    <td class="col-subtotal">
                      <div class="subtotal-price">¥{{ ((item.memberPrice || 0) * item.quantity).toFixed(2) }}</div>
                      <div class="weight-text">({{ (item.weight || 0) }}克)</div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 订单金额明细 -->
          <div class="checkout-section order-summary">
            <div class="summary-row">
              <span class="summary-label">商品总价格：</span>
              <span class="summary-value">{{ totalProductPrice.toFixed(2) }}</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">商品重量：</span>
              <span class="summary-value">{{ totalWeight }}克</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">配送费用：</span>
              <span class="summary-value">
                <span v-if="calculatingShippingFee">计算中...</span>
                <span v-else>¥{{ shippingFee.toFixed(2) }}</span>
              </span>
            </div>
            <!-- 税金和发票抬头（已屏蔽） -->
            <!-- <div class="summary-row">
              <span class="summary-label">税金(0%)：</span>
              <span class="summary-value">+¥{{ tax.toFixed(2) }}</span>
              <span class="invoice-label">发票抬头：</span>
              <el-checkbox v-model="needInvoice" class="invoice-checkbox"></el-checkbox>
            </div> -->
            <div class="summary-row total-row">
              <span class="summary-label">订单总金额：</span>
              <span class="summary-value total-amount">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div class="action-buttons">
              <el-button class="back-btn" @click="handleBackToCart">返回购物车</el-button>
              <el-button type="warning" size="large" class="submit-btn" @click="handlePlaceOrder">
                确认无误,下订单
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import RegionSelector from '@/components/common/RegionSelector.vue'
import { getCartList } from '@/api/buyer/cart'
import { getAddressList, addAddress, updateAddress } from '@/api/buyer/address'
import { createOrder } from '@/api/buyer/order'
import { getDepositBalance } from '@/api/buyer/deposit'
import { calculateShippingFeeByTemplate } from '@/api/buyer/shipping'
import { useUserStore } from '@/stores/user'
import { getProvinces, getChildrenByParentId } from '@/api/common/region'
import type { CartVO } from '@/api/buyer/cart'
import type { AddressVO, AddressDTO } from '@/api/buyer/address'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 选中的地址ID
const selectedAddressId = ref<number | string>('')
const showAddressForm = ref(false)
const loading = ref(false)
// 正在编辑的地址ID（用于区分编辑和新增）
const editingAddressId = ref<number | null>(null)

// 收货地址列表
const addressList = ref<AddressVO[]>([])

// 地址表单
const addressFormRef = ref<InstanceType<typeof ElForm>>()
const addressForm = ref({
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  zipCode: '',
  receiverName: '',
  receiverPhone: '',
  receiverMobile: '',
  saveAddress: false
})

// 地区选择器数据
const regionData = ref<{
  provinceId?: number;
  cityId?: number;
  districtId?: number;
}>({})

// 地址验证规则（用于表单验证）
const addressRules = {
  detailAddress: [{ required: true, message: '请输入街道地址', trigger: 'blur' }],
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [{ required: false, message: '请输入联系电话', trigger: 'blur' }],
  receiverMobile: [
    {
      validator: (rule: any, value: string, callback: Function) => {
        // 手机和电话至少填一个
        if (!value && !addressForm.value.receiverPhone) {
          callback(new Error('请至少填写手机或电话中的一项'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 地区选择器change事件处理
const handleRegionChange = (value: {
  provinceId?: number;
  cityId?: number;
  districtId?: number;
  provinceName?: string;
  cityName?: string;
  districtName?: string;
}) => {
  addressForm.value.province = value.provinceName || ''
  addressForm.value.city = value.cityName || ''
  addressForm.value.district = value.districtName || ''
}

// 监听地址选择变化，选择"其他收货地址"时清空表单
watch(selectedAddressId, async (newVal, oldVal) => {
  // 只有当从非'other'变为'other'时才清空表单
  // 避免编辑地址时触发清空
  if (newVal === 'other' && oldVal !== 'other') {
    // 清空所有收货人信息字段
    addressForm.value = {
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      zipCode: '',
      receiverName: '',
      receiverPhone: '',
      receiverMobile: '',
      saveAddress: false
    }
    // 清空地区选择器数据
    regionData.value = {}
    // 清空编辑地址ID（选择其他收货地址时，是新增操作）
    editingAddressId.value = null
    // 显示地址表单
    showAddressForm.value = true
    // 清空运费
    shippingFee.value = 0
  } else if (newVal !== 'other' && newVal !== oldVal) {
    // 地址变化时，重新计算运费
    await calculateShippingFee()
  }
})

// 监听地区选择变化，重新计算运费
watch([() => addressForm.value.province, () => addressForm.value.city, () => addressForm.value.district], async () => {
  if (selectedAddressId.value === 'other' && addressForm.value.province && addressForm.value.city && addressForm.value.district) {
    // 如果选择了其他收货地址，且已填写完整地址信息，计算运费
    await calculateShippingFee()
  }
})

// 送货日期和时间
const deliveryDate = ref('any')
const deliveryTime = ref('any')

// 订单附言
const orderRemarks = ref('')

// 支付币别
const paymentCurrency = ref('CNY')

// 支付方式相关
const showPaymentOptions = ref(false)
const paymentMethod = ref('alipay')
// 预存款余额
const depositBalance = ref(0.00)

// 支付方式列表
const paymentMethods = ref([
  {
    id: 'pre_deposit',
    name: '预存款支付',
    description: '商店预存款支付'
  },
  {
    id: 'wechat',
    name: '微信支付',
    description: '微信支付'
  },
  {
    id: 'alipay',
    name: '支付宝',
    description: '电脑端支付宝支付'
  }
])

// 当前选中的支付方式
const selectedPaymentMethod = computed(() => {
  return paymentMethods.value.find(m => m.id === paymentMethod.value) || paymentMethods.value[2]
})

// 配送方式相关
const showShippingDialog = ref(false)
const selectedShippingMethodId = ref('free_shipping')

// 配送方式列表（模拟数据，后续从API获取）
const shippingMethods = ref([
  {
    id: 'pickup',
    name: '上门自提',
    price: 0.00,
    description: '需要到仓库自提,不发货'
  },
  {
    id: 'anneng',
    name: '安能物流',
    price: 70.00,
    description: '安能物流主要适合货物重量在10KG以上非包邮订单,广东省内最低40元/票,省外最低70元/票,紧急订单或偏远地区订单请询问后选择。'
  },
  {
    id: 'yto_cainiao',
    name: '圆通菜鸟',
    price: 3.40,
    description: ''
  },
  {
    id: 'yto_pdd',
    name: '圆通拼多多',
    price: 3.40,
    description: ''
  },
  {
    id: 'yto_jd',
    name: '圆通京东',
    price: 3.40,
    description: ''
  },
  {
    id: 'sto_pdd',
    name: '申通拼多多',
    price: 3.40,
    description: ''
  },
  {
    id: 'sto_cainiao',
    name: '申通菜鸟',
    price: 3.40,
    description: ''
  },
  {
    id: 'sto_jd',
    name: '申通京东',
    price: 3.40,
    description: ''
  },
  {
    id: 'debon',
    name: '德邦快递',
    price: 13.00,
    description: '适用于3KG以上订单.'
  },
  {
    id: 'sf',
    name: '顺丰快递',
    price: 18.00,
    description: '下单前请自行去顺丰http://www.sf-express.com/cn/sc/查询该地是否到达!超区快递不派送需要转其他快递的费用,自行承担'
  },
  {
    id: 'freight_collect',
    name: '运费到付',
    price: 0.00,
    description: '运费到付仅支持顺丰到付,安能物流到付'
  },
  {
    id: 'free_shipping',
    name: '包邮订单',
    price: 0.00,
    description: '2025年10月份货物订单金额符合相应区域包邮政策要求的方可选择"包邮订单",具体如下: 北追远地区客户订单满2000元的向当地物済安白白坦'
  }
])

// 当前选中的配送方式
const selectedShippingMethod = computed(() => {
  return shippingMethods.value.find(m => m.id === selectedShippingMethodId.value) || shippingMethods.value[11]
})

// 订单商品列表（从购物车获取）
const orderItems = ref<CartVO[]>([])

// 判断用户是否是会员
const isMember = computed(() => {
  // 从订单商品列表中获取第一个商品的isMember字段（所有商品的isMember应该相同）
  return orderItems.value.length > 0 && orderItems.value[0].isMember === 1
})

// 获取价格列标题（根据用户是否是会员）
const getPriceColumnTitle = () => {
  if (isMember.value) {
    return '会员价格'
  }
  return '商品价格'
}

// 发票
const needInvoice = ref(false)

// 计算总价
const totalProductPrice = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + (item.memberPrice || 0) * item.quantity, 0)
})

// 计算总重量
const totalWeight = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + (item.weight || 0) * item.quantity, 0)
})

// 配送费用（根据运费模板计算）
const shippingFee = ref(0)
const calculatingShippingFee = ref(false)

// 计算运费（必须在orderItems定义之后）
const calculateShippingFee = async () => {
  // 如果没有商品，运费为0
  if (orderItems.value.length === 0) {
    console.log('计算运费：商品列表为空')
    shippingFee.value = 0
    return
  }

  // 获取地址信息
  let addressInfo: { province: string; city: string; district: string } | null = null
  
  if (selectedAddressId.value === 'other') {
    // 如果选择了"其他地址"，使用表单中的地址信息
    if (addressForm.value.province && addressForm.value.city && addressForm.value.district) {
      addressInfo = {
        province: addressForm.value.province,
        city: addressForm.value.city,
        district: addressForm.value.district
      }
      console.log('计算运费：使用表单地址', addressInfo)
    } else {
      console.log('计算运费：表单地址信息不完整', addressForm.value)
      shippingFee.value = 0
      return
    }
  } else if (selectedAddressId.value) {
    // 如果选择了已保存的地址，使用地址列表中的地址信息
    const selectedAddress = addressList.value.find(addr => addr.id === selectedAddressId.value)
    if (!selectedAddress) {
      console.log('计算运费：未找到选中的地址', selectedAddressId.value, addressList.value)
      shippingFee.value = 0
      return
    }
    addressInfo = {
      province: selectedAddress.province,
      city: selectedAddress.city,
      district: selectedAddress.district
    }
    console.log('计算运费：使用已保存地址', addressInfo)
  } else {
    // 没有选中地址
    console.log('计算运费：未选中地址', selectedAddressId.value)
    shippingFee.value = 0
    return
  }

  if (!addressInfo) {
    shippingFee.value = 0
    return
  }

  console.log('开始计算运费：', {
    addressId: selectedAddressId.value,
    address: `${addressInfo.province} ${addressInfo.city} ${addressInfo.district}`,
    itemsCount: orderItems.value.length,
    items: orderItems.value.map(item => ({
      id: item.id,
      name: item.name,
      shippingTemplateId: item.shippingTemplateId,
      weight: item.weight,
      quantity: item.quantity,
      memberPrice: item.memberPrice
    }))
  })

  // 按运费模板分组商品
  const templateGroups = new Map<number | null, CartVO[]>()
  orderItems.value.forEach(item => {
    const templateId = item.shippingTemplateId || null
    if (!templateGroups.has(templateId)) {
      templateGroups.set(templateId, [])
    }
    templateGroups.get(templateId)!.push(item)
  })

  // 如果没有运费模板的商品（包邮），只计算有运费模板的商品
  let totalFee = 0
  calculatingShippingFee.value = true

  try {
    // 遍历每个运费模板组，计算运费
    for (const [templateId, items] of templateGroups.entries()) {
      if (templateId === null) {
        // 包邮商品，跳过
        continue
      }

      // 计算该模板组的总重量（转换为kg）、总金额、总件数
      const totalWeight = items.reduce((sum, item) => sum + (item.weight || 0) * item.quantity, 0) / 1000 // 转换为kg
      const totalAmount = items.reduce((sum, item) => sum + (item.memberPrice || 0) * item.quantity, 0)
      const totalQuantity = items.reduce((sum, item) => sum + item.quantity, 0)

      // 调用API计算运费
      console.log(`计算模板ID=${templateId}的运费：`, {
        province: addressInfo.province || '',
        city: addressInfo.city || '',
        district: addressInfo.district || '',
        totalWeight: totalWeight,
        totalAmount: totalAmount,
        totalQuantity: totalQuantity
      })

      const response = await calculateShippingFeeByTemplate(templateId, {
        province: addressInfo.province || '',
        city: addressInfo.city || '',
        district: addressInfo.district || '',
        totalWeight: totalWeight,
        totalAmount: totalAmount,
        totalQuantity: totalQuantity
      })

      console.log(`模板ID=${templateId}的运费计算结果：`, response)

      // 注意：request.ts的响应拦截器已经提取了data字段，所以response就是data的值（数字）
      if (response !== undefined && response !== null) {
        totalFee += Number(response)
      }
    }
  } catch (error: any) {
    console.error('计算运费失败:', error)
    ElMessage.warning(error.message || '计算运费失败，请稍后重试')
    totalFee = 0
  } finally {
    calculatingShippingFee.value = false
  }

  console.log('运费计算完成，总运费：', totalFee)
  shippingFee.value = totalFee
}

// 监听商品变化，重新计算运费（必须在orderItems和calculateShippingFee定义之后）
watch(orderItems, async () => {
  if (selectedAddressId.value && selectedAddressId.value !== 'other') {
    await calculateShippingFee()
  }
}, { deep: true })

// 税金
const tax = ref(0)

// 订单总金额
const totalAmount = computed(() => {
  return totalProductPrice.value + shippingFee.value + tax.value
})

// 根据名称查找地区ID
const loadRegionIdsByName = async (provinceName: string, cityName: string, districtName: string) => {
  try {
    // 1. 查找省份ID
    const provinces = await getProvinces()
    const province = provinces.find(p => p.name === provinceName)
    if (!province) {
      console.warn('未找到省份:', provinceName)
      return
    }
    
    // 2. 查找城市ID
    const cities = await getChildrenByParentId(province.id)
    const city = cities.find(c => c.name === cityName)
    if (!city) {
      console.warn('未找到城市:', cityName)
      return
    }
    
    // 3. 查找区县ID
    const districts = await getChildrenByParentId(city.id)
    const district = districts.find(d => d.name === districtName)
    if (!district) {
      console.warn('未找到区县:', districtName)
      return
    }
    
    // 4. 设置regionData
    regionData.value = {
      provinceId: province.id,
      cityId: city.id,
      districtId: district.id
    }
  } catch (error) {
    console.error('加载地区ID失败:', error)
    // 失败时不影响表单数据，用户仍可以重新选择
  }
}

// 编辑地址
const handleEditAddress = async (address: AddressVO) => {
  // 不改变selectedAddressId，保持当前选中的地址ID，只显示编辑表单
  showAddressForm.value = true
  // 保存正在编辑的地址ID
  editingAddressId.value = address.id
  
  // 填充表单数据，编辑时默认勾选保存
  addressForm.value = {
    province: address.province || '',
    city: address.city || '',
    district: address.district || '',
    detailAddress: address.address || '',
    zipCode: address.zipCode || '',
    receiverName: address.recipient || '',
    receiverPhone: address.phone || '',
    receiverMobile: address.mobile || '',
    saveAddress: true  // 编辑时默认勾选保存
  }
  
  // 根据名称查找ID，设置到regionData中
  if (address.province && address.city && address.district) {
    await loadRegionIdsByName(address.province, address.city, address.district)
  }
}

// 确认配送方式
const handleConfirmShipping = () => {
  showShippingDialog.value = false
  // 配送费用会自动更新（通过computed）
}

// 返回购物车
const handleBackToCart = () => {
  router.push('/cart')
}

// 提交订单
const handlePlaceOrder = async () => {
  // 验证地址
  let addressId: number | null = null
  
  if (selectedAddressId.value === 'other' || showAddressForm.value || addressList.value.length === 0) {
    // 使用新地址，需要验证表单
    // 先验证收货人信息必填项
    if (!addressForm.value.receiverName || addressForm.value.receiverName.trim() === '') {
      ElMessage.warning('请输入收货人姓名')
      return
    }
    
    // 验证手机或电话至少填一个
    if ((!addressForm.value.receiverMobile || addressForm.value.receiverMobile.trim() === '') &&
        (!addressForm.value.receiverPhone || addressForm.value.receiverPhone.trim() === '')) {
      ElMessage.warning('请至少填写手机或电话中的一项')
      return
    }
    
    // 验证其他必填项
    if (!addressForm.value.province || !addressForm.value.city || !addressForm.value.district) {
      ElMessage.warning('请完整选择收货地区')
      return
    }
    
    if (!addressForm.value.detailAddress || addressForm.value.detailAddress.trim() === '') {
      ElMessage.warning('请输入街道地址')
      return
    }
    
    // 如果选择保存地址，根据是编辑还是新增进行操作
    if (addressForm.value.saveAddress) {
      try {
        const addressDTO: AddressDTO = {
          recipient: addressForm.value.receiverName,
          phone: addressForm.value.receiverPhone || undefined,
          mobile: addressForm.value.receiverMobile || undefined,
          province: addressForm.value.province,
          city: addressForm.value.city,
          district: addressForm.value.district,
          address: addressForm.value.detailAddress,
          zipCode: addressForm.value.zipCode || undefined,
          isDefault: false
        }
        
        // 如果是编辑操作，更新地址；否则新增地址
        if (editingAddressId.value !== null) {
          // 编辑：更新已有地址
          await updateAddress(editingAddressId.value, addressDTO)
          addressId = editingAddressId.value
        } else {
          // 新增：创建新地址
          const newAddressId = await addAddress(addressDTO)
          addressId = newAddressId
        }
      } catch (error: any) {
        ElMessage.error(error.message || '保存地址失败')
        return
      }
    } else {
      // 不保存地址，需要临时创建或使用已有地址
      // 这里简化处理，要求用户先保存地址
      ElMessage.warning('请先保存收货地址')
      return
    }
  } else {
    // 使用已有地址
    addressId = Number(selectedAddressId.value)
  }
  
  if (!addressId) {
    ElMessage.warning('请选择或填写收货地址')
    return
  }
  
  // 获取选中的购物车ID列表
  const cartIds = route.query.cartIds ? (route.query.cartIds as string).split(',').map(id => Number(id)) : []
  
  // 转换支付方式
  const paymentMethodMap: Record<string, string> = {
    'alipay': 'ALIPAY',
    'wechat': 'WECHAT',
    'pre_deposit': 'PRE_DEPOSIT',
    'offline': 'OFFLINE'
  }
  const backendPaymentMethod = paymentMethodMap[paymentMethod.value] || 'ALIPAY'
  
  // 转换配送日期和时间（已屏蔽，传undefined）
  // let deliveryDateValue: string | undefined = undefined
  // if (deliveryDate.value !== 'any') {
  //   deliveryDateValue = deliveryDate.value
  // }
  
  // let deliveryTimeValue: string | undefined = undefined
  // if (deliveryTime.value !== 'any') {
  //   deliveryTimeValue = deliveryTime.value
  // }
  
  try {
    loading.value = true
    const orderNo = await createOrder({
      addressId: addressId,
      cartIds: cartIds.length > 0 ? cartIds : undefined,
      // shippingMethod: selectedShippingMethod.value.name, // 已屏蔽配送方式
      // deliveryDate: deliveryDateValue, // 已屏蔽
      // deliveryTime: deliveryTimeValue, // 已屏蔽
      paymentMethod: backendPaymentMethod,
      orderRemark: orderRemarks.value || undefined
    })
    
    ElMessage.success('订单创建成功')
    
    // 跳转到支付页面
    router.push({
      path: '/order/payment',
      query: {
        orderNumber: orderNo,
        amount: totalAmount.value.toFixed(2),
        paymentMethod: paymentMethod.value
      }
    })
  } catch (error: any) {
    ElMessage.error(error.message || '创建订单失败')
  } finally {
    loading.value = false
  }
}

// 加载收货地址列表
const loadAddressList = async () => {
  try {
    const data = await getAddressList()
    addressList.value = data
    // 设置默认地址
    const defaultAddress = data.find(addr => addr.isDefault)
    if (defaultAddress) {
      selectedAddressId.value = defaultAddress.id
    } else if (data.length > 0) {
      selectedAddressId.value = data[0].id
    } else {
      // 如果没有收货地址，清空表单默认值，显示地址表单
      selectedAddressId.value = 'other'
      showAddressForm.value = true
      addressForm.value = {
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        zipCode: '',
        receiverName: '',
        receiverPhone: '',
        receiverMobile: '',
        saveAddress: false
      }
      regionData.value = {}
      shippingFee.value = 0
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载收货地址失败')
    // 加载失败时也清空表单默认值
    addressForm.value = {
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      zipCode: '',
      receiverName: '',
      receiverPhone: '',
      receiverMobile: '',
      saveAddress: false
    }
    regionData.value = {}
    shippingFee.value = 0
  }
}

// 加载购物车商品（从路由参数获取购物车ID）
const loadCartItems = async () => {
  try {
    const cartIds = route.query.cartIds ? (route.query.cartIds as string).split(',').map(id => Number(id)) : []
    if (cartIds.length === 0) {
      ElMessage.warning('请先选择要结算的商品')
      router.push('/cart')
      return
    }
    
    const allCartItems = await getCartList()
    orderItems.value = allCartItems.filter(item => cartIds.includes(item.id))
    
    if (orderItems.value.length === 0) {
      ElMessage.warning('购物车商品不存在')
      router.push('/cart')
      return
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载购物车商品失败')
    router.push('/cart')
  }
}

// 加载预存款余额
const loadDepositBalance = async () => {
  try {
    const response = await getDepositBalance()
    if (response) {
      depositBalance.value = response.availableBalance || 0
    }
  } catch (error: any) {
    console.error('获取预存款余额失败:', error)
    // 如果用户未登录或其他错误，不显示错误提示，保持默认值0
    if (error?.response?.status !== 401) {
      // 静默失败，不影响页面正常使用
    }
  }
}

onMounted(async () => {
  // 并行加载地址和商品，等待两者都完成后再计算运费
  await Promise.all([
    loadAddressList(),
    loadCartItems()
  ])
  
  // 如果地址和商品都加载完成，计算运费
  if (selectedAddressId.value && selectedAddressId.value !== 'other' && orderItems.value.length > 0) {
    await calculateShippingFee()
  }
  
  // 如果用户已登录，加载预存款余额
  if (userStore.isLoggedIn()) {
    loadDepositBalance()
  }
})
</script>

<style scoped lang="scss">
.checkout-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.checkout-content {
  padding: 20px 0 40px;
}

// 购物流程进度条
.checkout-progress {
  background: #f5f5f5;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .progress-steps {
    display: flex;
    align-items: center;
    flex: 1;

    .step {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;

      .step-number {
        width: 30px;
        height: 30px;
        border-radius: 50%;
        background: #ccc;
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: bold;
        font-size: 14px;
      }

      .step-text {
        font-size: 12px;
        color: #666;
      }

      &.active {
        .step-number {
          background: #e4393c;
        }

        .step-text {
          color: #e4393c;
          font-weight: bold;
        }
      }
    }

    .step-line {
      width: 100px;
      height: 2px;
      background: #eee;
      margin: 0 20px;
    }
  }

  .cart-icon {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
    margin-left: 40px;

    .icon {
      font-size: 32px;
      color: #e4393c;
    }

    .icon-text {
      font-size: 14px;
      color: #333;
      font-weight: bold;
    }

    .icon-text-en {
      font-size: 12px;
      color: #999;
    }
  }
}

// 主内容区域
.checkout-main {
  background: #fff;
  border: 1px solid #e5e5e5;
  padding: 20px;
}

// 结算区块
.checkout-section {
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e5e5;

  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }

  .section-header {
    margin-bottom: 15px;

    .section-title {
      font-size: 16px;
      font-weight: bold;
      color: #333;
      margin: 0;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .modify-link {
        color: #e4393c;
        font-size: 14px;
        padding: 0;
        font-weight: normal;
      }
    }
  }
}

// 地址选择
.address-selection {
  margin-bottom: 20px;

  .address-label {
    font-size: 14px;
    color: #333;
    margin-bottom: 15px;
    font-weight: 500;
  }

  .address-list {
    .address-radio-group {
      display: flex;
      flex-direction: column;
      gap: 10px;

      .address-radio-item {
        display: flex;
        align-items: flex-start;
        padding: 10px 0;
        margin: 0;
        height: auto;
        line-height: 1.5;
        cursor: pointer;

        .address-radio-input {
          margin: 0;
          margin-right: 8px;
          margin-top: 2px;
          flex-shrink: 0;
          width: 16px;
          height: 16px;
          cursor: pointer;
        }

        .address-radio-label {
          flex: 1;
          display: block;
          text-align: left;
          width: 100%;

          .address-content {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 5px;
            flex: 1;
            line-height: 1.6;
            text-align: left;

            .address-region {
              font-size: 14px;
              color: #333;
              font-weight: 500;
            }

            .address-detail {
              font-size: 14px;
              color: #333;
            }

            .address-recipient {
              font-size: 14px;
              color: #666;
            }

            .edit-link {
              color: #e4393c;
              font-size: 14px;
              padding: 0;
              margin-left: 10px;
            }
          }

          .other-address-text {
            font-size: 14px;
            color: #333;
            text-align: left;
            display: block;
            width: 100%;
          }
        }
      }
    }
  }
}

// 地址表单
.address-form {
  margin-top: 20px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 4px;

  .address-form-table {
    width: 100%;
    border-collapse: collapse;

    tr {
      td {
        padding: 8px 0;
        vertical-align: top;
      }

      .form-label {
        width: 120px;
        font-size: 14px;
        color: #333;
        text-align: right;
        padding-right: 15px;
        white-space: nowrap;

        .required-mark {
          color: #e4393c;
          margin-right: 2px;
        }
      }

      .form-input {
        position: relative;

        .form-tip {
          font-size: 12px;
          color: #999;
          margin-top: 5px;
          display: block;
        }

        .form-tip-inline {
          font-size: 12px;
          color: #999;
          margin-left: 10px;
          white-space: nowrap;
        }
      }
    }
  }
}

// 送货日期和时间
.delivery-time {
  display: flex;
  gap: 30px;
  margin: 20px 0;

  .time-item {
    display: flex;
    align-items: center;
    gap: 10px;

    .time-label {
      font-size: 14px;
      color: #333;
      min-width: 90px;
    }
  }
}

// 订单附言
.order-remarks {
  margin-top: 20px;

  .remarks-label {
    font-size: 14px;
    color: #333;
    margin-bottom: 10px;
    display: block;
  }

  :deep(.el-textarea__inner) {
    border: 1px solid #e5e5e5;
  }
}

// 配送和支付信息
.shipping-info {
  .info-item {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;

    .info-label {
      font-size: 14px;
      color: #333;
      min-width: 100px;
    }

    .info-value {
      font-size: 14px;
      color: #666;

      &.shipping-fee {
        color: #e4393c;
      }
    }
  }
}

// 配送方式选择对话框
.shipping-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    background: #f5f5f5;
  }

  .shipping-methods-list {
    padding: 0;
    margin: 0;
    overflow: visible;

    .shipping-radio-group {
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding: 0;
      margin: 0;

      .shipping-radio-item {
        display: flex;
        align-items: flex-start;
        padding: 8px 0;
        margin: 0;
        height: auto;
        line-height: 1.5;
        cursor: pointer;

        .shipping-radio-input {
          margin: 0;
          margin-right: 8px;
          margin-top: 2px;
          flex-shrink: 0;
          width: 16px;
          height: 16px;
          cursor: pointer;
        }

        .shipping-radio-label {
          flex: 1;
          display: block;
          text-align: left;
          width: 100%;

          .shipping-method-content {
            width: 100%;
            max-width: 100%;
            text-align: left;
            padding-right: 0;
            padding-left: 0;
            margin: 0;
            box-sizing: border-box;

            .method-header {
              display: flex;
              align-items: flex-start;
              justify-content: flex-start;
              margin-bottom: 4px;
              gap: 8px;
              padding: 0;
              margin: 0;
              line-height: 1.5;

              .method-name {
                font-size: 14px;
                color: #333;
                font-weight: normal;
                margin: 0;
                padding: 0;
              }

              .method-price {
                font-size: 14px;
                color: #e4393c;
                font-weight: normal;
                margin: 0;
                padding: 0;
              }
            }

            .method-description {
              font-size: 12px;
              color: #e4393c;
              line-height: 1.6;
              margin-top: 2px;
              margin-left: 0;
              margin-bottom: 0;
              padding: 0;
              word-break: normal !important;
              word-wrap: break-word !important;
              overflow-wrap: break-word !important;
              white-space: normal !important;
              text-align: left;
              display: block;
              width: 100%;
              box-sizing: border-box;
            }
          }
        }
      }
    }
  }
}

.payment-info {
  .info-item {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;

    .info-label {
      font-size: 14px;
      color: #333;
      min-width: 120px;
    }

    .info-value {
      font-size: 14px;
      color: #666;
    }
  }
}

// 支付方式选择列表（独立样式，不在 .payment-info 内）
.payment-methods-list {
  margin-top: 15px;
  padding: 0;
  overflow: visible;

  .payment-radio-group {
    display: flex;
    flex-direction: column;
    gap: 15px;
    padding: 0;
    margin: 0;
    width: 100%;

    .payment-radio {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 0;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        .payment-name {
          color: #e4393c;
        }
      }

      .payment-icon {
        font-size: 18px;
        color: #999;
        width: 20px;
        text-align: center;
        display: inline-block;
        transition: color 0.3s;
      }

      .payment-name {
        font-size: 14px;
        color: #333;
        font-weight: 500;
        transition: color 0.3s;
      }

      .payment-desc {
        font-size: 12px;
        color: #999;
        margin-left: 5px;
      }

      .deposit-balance {
        font-size: 14px;
        color: #ff8c00;
        margin-left: auto;
        padding: 4px 8px;
        background-color: #fffacd;
        border-radius: 2px;
      }

      // 选中状态
      &.is-checked {
        .payment-icon {
          color: #e4393c;
        }

        .payment-name {
          color: #e4393c;
        }
      }
    }
  }
}

// 商品表格
.products-table-wrapper {
  overflow-x: auto;
  border: 1px solid #e5e5e5;
}

.products-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;

  thead {
    background: #f5f5f5;

    th {
      padding: 12px 8px;
      text-align: center;
      font-weight: bold;
      color: #333;
      border: 1px solid #e5e5e5;
      border-bottom: 2px solid #e5e5e5;
    }
  }

  tbody {
    .product-row {
      border-bottom: 1px solid #e5e5e5;

      &:last-child {
        border-bottom: none;
      }

      td {
        padding: 15px 8px;
        text-align: center;
        border-right: 1px solid #e5e5e5;
        vertical-align: middle;

        &:last-child {
          border-right: none;
        }
      }

      .col-code {
        width: 100px;
        color: #666;
      }

      .col-name {
        width: 400px;
        text-align: left;
        color: #333;
        padding-left: 15px;

        .product-name {
          margin-bottom: 5px;
        }

        .sku-spec-text {
          font-size: 12px;
          color: #999;
        }
      }

      .col-price {
        width: 120px;

        .member-price {
          color: #e4393c;
          font-weight: bold;
        }

        .sales-price {
          color: #999;
          text-decoration: line-through;
        }
      }

      .col-quantity {
        width: 80px;
      }

      .col-subtotal {
        width: 150px;

        .subtotal-price {
          color: #e4393c;
          font-weight: bold;
          font-size: 16px;
          margin-bottom: 3px;
        }

        .weight-text {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

// 订单金额明细
.order-summary {
  .summary-row {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    margin-bottom: 12px;
    font-size: 14px;
    line-height: 1.8;

    .summary-label {
      color: #666;
      margin-right: 10px;
    }

    .summary-value {
      color: #333;
      min-width: 120px;
      text-align: right;
    }

    &.total-row {
      margin-top: 15px;
      padding-top: 15px;
      border-top: 2px solid #eee;
      margin-bottom: 20px;

      .summary-label {
        font-size: 16px;
        font-weight: bold;
        color: #333;
      }

      .total-amount {
        font-size: 24px;
        font-weight: bold;
        color: #e4393c;
      }
    }

    .invoice-label {
      margin-left: 15px;
      font-size: 14px;
      color: #333;
    }

    .invoice-checkbox {
      margin-left: 5px;
    }
  }

  .action-buttons {
    display: flex;
    justify-content: space-between;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #eee;

    .back-btn {
      background: #999;
      border-color: #999;
      color: #fff;
      padding: 12px 30px;
      font-size: 14px;
      border-radius: 4px;

      &:hover {
        background: #888;
        border-color: #888;
        color: #fff;
      }
    }

    .submit-btn {
      background: linear-gradient(to right, #ff8c00, #ff6b00);
      border: none;
      color: #fff;
      padding: 15px 50px;
      font-size: 16px;
      font-weight: bold;
      border-radius: 4px;

      &:hover {
        background: linear-gradient(to right, #ff7a00, #ff5a00);
        color: #fff;
      }
    }
  }
}
</style>

