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
                <el-radio-group v-model="selectedAddressId" class="address-radio-group">
                  <el-radio
                    v-for="address in addressList"
                    :key="address.id"
                    :label="address.id"
                    class="address-radio"
                  >
                    <div class="address-content">
                      <span class="address-region">{{ address.region }}</span>
                      <span class="address-detail">{{ address.detailAddress }}</span>
                      <span class="address-recipient">
                        (收货人:{{ address.receiverName }} 手机:{{ address.receiverPhone }} 邮编:{{ address.zipCode }})
                      </span>
                      <el-button type="text" class="edit-link" @click="handleEditAddress(address)">
                        编辑
                      </el-button>
                    </div>
                  </el-radio>
                  <el-radio label="other" class="address-radio">
                    <span class="other-address-text">其他收货地址</span>
                  </el-radio>
                </el-radio-group>
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
                    <el-cascader
                      v-model="addressForm.region"
                      :options="regionOptions"
                      placeholder="请选择省/市/区"
                      style="width: 500px"
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

            <!-- 送货日期和时间 -->
            <div class="delivery-time">
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
            </div>

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

          <!-- 选择配送方式 -->
          <div class="checkout-section">
            <div class="section-header">
              <h3 class="section-title">
                选择配送方式
                <el-button type="text" class="modify-link" @click="showShippingOptions = !showShippingOptions">
                  {{ showShippingOptions ? '收起' : '修改' }}
                </el-button>
              </h3>
            </div>
            <div class="shipping-info">
              <div class="info-item">
                <span class="info-label">配送方式：</span>
                <span class="info-value">{{ selectedShippingMethod.name }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">运费：</span>
                <span class="info-value">+¥{{ selectedShippingMethod.price.toFixed(2) }}</span>
              </div>
            </div>

            <!-- 配送方式选择列表（展开显示） -->
            <div v-if="showShippingOptions" class="shipping-methods-list">
              <el-radio-group v-model="selectedShippingMethodId" class="shipping-radio-group">
                <el-radio
                  v-for="method in shippingMethods"
                  :key="method.id"
                  :label="method.id"
                  class="shipping-radio"
                >
                  <div class="shipping-method-content">
                    <div class="method-header">
                      <span class="method-name">{{ method.name }}</span>
                      <span class="method-price">+¥{{ method.price.toFixed(2) }}</span>
                    </div>
                    <div v-if="method.description" class="method-description">
                      {{ method.description }}
                    </div>
                  </div>
                </el-radio>
              </el-radio-group>
            </div>
          </div>

          <!-- 选择支付方式 -->
          <div class="checkout-section">
            <div class="section-header">
              <h3 class="section-title">
                选择支付方式
                <el-button type="text" class="modify-link">修改</el-button>
              </h3>
            </div>
            <div class="payment-info">
              <div class="info-item">
                <span class="info-label">选择支付币别：</span>
                <el-select v-model="paymentCurrency" style="width: 150px">
                  <el-option label="人民币" value="CNY" />
                </el-select>
              </div>
              <div class="payment-methods">
                <el-radio-group v-model="paymentMethod" class="payment-radio-group">
                  <el-radio label="pre_deposit" class="payment-radio">
                    <span class="payment-name">预存款支付</span>
                    <span class="payment-desc">商店预存款支付</span>
                  </el-radio>
                  <el-radio label="wechat" class="payment-radio">
                    <span class="payment-name">微信支付</span>
                    <span class="payment-desc">微信支付</span>
                  </el-radio>
                  <el-radio label="alipay" class="payment-radio">
                    <span class="payment-name">支付宝</span>
                    <span class="payment-desc">电脑端支付宝支付</span>
                  </el-radio>
                </el-radio-group>
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
                    <th class="col-price">会员价格</th>
                    <th class="col-price">销售价格</th>
                    <th class="col-quantity">数量</th>
                    <th class="col-subtotal">小计</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in orderItems" :key="item.id" class="product-row">
                    <td class="col-code">{{ item.productCode }}</td>
                    <td class="col-name">{{ item.name }}</td>
                    <td class="col-price">
                      <span class="member-price">¥{{ item.memberPrice.toFixed(2) }}</span>
                    </td>
                    <td class="col-price">
                      <span class="sales-price">¥{{ item.salesPrice.toFixed(2) }}</span>
                    </td>
                    <td class="col-quantity">{{ item.quantity }}</td>
                    <td class="col-subtotal">
                      <div class="subtotal-price">¥{{ (item.memberPrice * item.quantity).toFixed(2) }}</div>
                      <div class="weight-text">({{ item.weight }}克)</div>
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
              <span class="summary-value">¥{{ shippingFee.toFixed(2) }}</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">税金(0%)：</span>
              <span class="summary-value">+¥{{ tax.toFixed(2) }}</span>
              <span class="invoice-label">发票抬头：</span>
              <el-checkbox v-model="needInvoice" class="invoice-checkbox"></el-checkbox>
            </div>
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'

const router = useRouter()

// 选中的地址ID
const selectedAddressId = ref<number | string>(1)
const showAddressForm = ref(false)

// 收货地址列表（模拟数据，后续从API获取）
const addressList = ref([
  {
    id: 1,
    receiverName: '刘朋辉',
    receiverPhone: '18829634981',
    zipCode: '100000',
    region: '陕西省西安市-雁塔区',
    detailAddress: '陕西省西安市雁塔区科技路徐家庄西南口148号'
  }
])

// 地址表单
const addressFormRef = ref<InstanceType<typeof ElForm>>()
const addressForm = ref({
  region: ['shaanxi', 'xian', 'yanta'], // 默认选择陕西省西安市雁塔区
  detailAddress: '陕西省西安市雁塔区科技路徐家庄西南口148号',
  zipCode: '100000',
  receiverName: '刘明辉',
  receiverPhone: '',
  receiverMobile: '18829634981',
  saveAddress: false
})

// 地址验证规则
const addressRules = {
  region: [{ required: true, message: '请选择收货地区', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入街道地址', trigger: 'blur' }],
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [{ required: false, message: '请输入联系电话', trigger: 'blur' }],
  receiverMobile: [{ required: false, message: '请输入联系手机', trigger: 'blur' }]
}

// 地区选项（模拟数据，后续从API获取）
const regionOptions = ref([
  {
    value: 'shaanxi',
    label: '陕西省',
    children: [
      {
        value: 'xian',
        label: '西安市',
        children: [
          { value: 'yanta', label: '雁塔区' }
        ]
      }
    ]
  }
])

// 送货日期和时间
const deliveryDate = ref('any')
const deliveryTime = ref('any')

// 订单附言
const orderRemarks = ref('')

// 支付币别
const paymentCurrency = ref('CNY')

// 支付方式
const paymentMethod = ref('alipay')

// 配送方式相关
const showShippingOptions = ref(false)
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

// 订单商品列表（模拟数据，后续从购物车获取）
const orderItems = ref([
  {
    id: 1,
    productCode: '505365',
    name: '【避孕润滑】玻尿酸润滑液200g ANGUS/爱神(规格)',
    memberPrice: 6.50,
    salesPrice: 54.00,
    quantity: 1,
    weight: 250
  }
])

// 发票
const needInvoice = ref(false)

// 计算总价
const totalProductPrice = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.memberPrice * item.quantity, 0)
})

// 计算总重量
const totalWeight = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.weight * item.quantity, 0)
})

// 配送费用（根据选中的配送方式计算）
const shippingFee = computed(() => {
  return selectedShippingMethod.value.price
})

// 税金
const tax = ref(0)

// 订单总金额
const totalAmount = computed(() => {
  return totalProductPrice.value + shippingFee.value + tax.value
})

// 编辑地址
const handleEditAddress = (address: any) => {
  selectedAddressId.value = 'other'
  showAddressForm.value = true
  // 填充表单数据
  addressForm.value = {
    region: ['shaanxi', 'xian', 'yanta'], // 默认选择陕西省西安市雁塔区
    detailAddress: address.detailAddress || '陕西省西安市雁塔区科技路徐家庄西南口148号',
    zipCode: address.zipCode || '100000',
    receiverName: address.receiverName || '刘明辉',
    receiverPhone: address.receiverPhone || '',
    receiverMobile: address.receiverPhone || '18829634981',
    saveAddress: false
  }
}

// 配送方式选择后自动更新（通过computed）
watch(selectedShippingMethodId, () => {
  // 配送费用会自动更新（通过computed）
})

// 返回购物车
const handleBackToCart = () => {
  router.push('/cart')
}

// 提交订单
const handlePlaceOrder = async () => {
  // 验证地址
  if (selectedAddressId.value === 'other' || showAddressForm.value) {
    if (!addressFormRef.value) return
    await addressFormRef.value.validate(async (valid) => {
      if (!valid) {
        ElMessage.warning('请完善收货地址信息')
        return
      }
    })
  }

  // TODO: 调用提交订单API，获取订单编号
  // 模拟订单编号
  const orderNumber = '20251208115856'
  
  // 跳转到支付页面，传递订单信息
  router.push({
    path: '/order/payment',
    query: {
      orderNumber: orderNumber,
      amount: totalAmount.value.toFixed(2)
    }
  })
}
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

      .address-radio {
        display: flex;
        align-items: center;
        padding: 10px 0;
        margin: 0;
        height: auto;
        line-height: 1.5;

        :deep(.el-radio__input) {
          margin-top: 0;
          flex-shrink: 0;
        }

        :deep(.el-radio__label) {
          padding-left: 8px;
          flex: 1;
          display: flex;
          align-items: center;
          text-align: left;
        }

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

        // 确保"其他收货地址"选项也左对齐
        .other-address-text {
          font-size: 14px;
          color: #333;
          text-align: left;
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
    }
  }
}

// 配送方式选择列表（展开显示）
.shipping-methods-list {
  margin-top: 20px;
  padding: 0;
  overflow: visible;

  .shipping-radio-group {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 0;
    margin: 0;

    .shipping-radio {
      display: block;
      padding: 0;
      margin: 0;
      height: auto;
      border: 1px solid #e5e5e5;
      border-radius: 4px;
      transition: all 0.3s;
      background: #fff;
      min-height: 50px;

      &:hover {
        border-color: #e4393c;
        background: #fff5f5;
      }

      :deep(.el-radio) {
        display: flex;
        align-items: flex-start;
        width: 100%;
        margin: 0;
        padding: 0;
        white-space: normal;
        height: 100%;
      }

      :deep(.el-radio__input) {
        margin-top: 15px;
        margin-left: 12px;
        margin-right: 0;
        flex-shrink: 0;
        width: auto;
      }

      :deep(.el-radio__input.is-checked .el-radio__inner) {
        background-color: #409eff;
        border-color: #409eff;
      }

      :deep(.el-radio__label) {
        padding: 12px 12px 12px 8px !important;
        flex: 1;
        width: auto;
        display: block;
        margin-left: 0 !important;
        padding-left: 8px !important;
      }

      .shipping-method-content {
        width: 100%;
        text-align: left;
        padding-right: 12px;
        padding-left: 0;
        margin: 0;

        .method-header {
          display: flex;
          align-items: center;
          justify-content: flex-start;
          margin-bottom: 5px;
          gap: 10px;
          padding: 0;
          margin-left: 0;

          .method-name {
            font-size: 14px;
            color: #333;
            font-weight: 500;
          }

          .method-price {
            font-size: 14px;
            color: #333;
            font-weight: normal;
          }
        }

        .method-description {
          font-size: 12px;
          color: #666;
          line-height: 1.6;
          margin-top: 3px;
          word-break: break-all;
          text-align: left;
          padding-left: 0;
          margin-left: 0;
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
    margin-bottom: 15px;

    .info-label {
      font-size: 14px;
      color: #333;
      min-width: 120px;
    }
  }

  .payment-methods {
    margin-top: 10px;

    .payment-radio-group {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .payment-radio {
        display: flex;
        align-items: flex-start;
        padding: 8px 0;
        margin: 0;
        height: auto;
        line-height: 1.5;

        :deep(.el-radio__input) {
          margin-top: 3px;
        }

        :deep(.el-radio__label) {
          padding-left: 8px;
          display: flex;
          flex-direction: column;
          gap: 3px;
        }

        .payment-name {
          font-size: 14px;
          color: #333;
          font-weight: 500;
        }

        .payment-desc {
          font-size: 12px;
          color: #999;
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

