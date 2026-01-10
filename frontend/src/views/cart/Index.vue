<template>
  <div class="cart-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 购物车内容区 -->
    <div class="cart-content">
      <div class="container">
        <!-- 购物流程进度条 -->
        <div class="checkout-progress">
          <div class="progress-steps">
            <div class="step active">
              <div class="step-number">1</div>
              <div class="step-text">查看购物车</div>
            </div>
            <div class="step-line"></div>
            <div class="step">
              <div class="step-number">2</div>
              <div class="step-text">用户登录或注册</div>
            </div>
            <div class="step-line"></div>
            <div class="step">
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

        <!-- 快速添加商品区（已屏蔽） -->
        <!-- <div class="quick-add-section">
          <div class="add-form">
            <span class="label">货号：</span>
            <el-input v-model="quickAdd.productCode" placeholder="请输入货号" class="input-field" />
            <span class="label">数量：</span>
            <el-input-number
              v-model="quickAdd.quantity"
              :min="1"
              :max="999"
              class="input-field"
            />
            <el-button type="danger" @click="handleQuickAdd">添加到购物车</el-button>
          </div>
        </div> -->

        <!-- 购物车商品列表 -->
        <div class="cart-items-section">
          <div class="section-title">
            <span class="title-icon">●</span>
            <span class="title-text">已放入购物车的商品</span>
          </div>
          <div class="section-subtitle">请在此确认你要购买的商品</div>

          <!-- 购物车表格 -->
          <div class="cart-table-wrapper" v-loading="loading">
            <table class="cart-table">
              <thead>
                <tr>
                  <th class="col-checkbox">
                    <el-checkbox
                      v-model="selectAll"
                      @change="handleSelectAll"
                    />
                  </th>
                  <th class="col-image">图片</th>
                  <th class="col-code">货号</th>
                  <th class="col-name">商品名称</th>
                  <th v-if="isMember" class="col-price">销售价格</th>
                  <th class="col-price">{{ getPriceColumnTitle() }}</th>
                  <th class="col-quantity">数量</th>
                  <th class="col-total">合计</th>
                  <th class="col-action">删除</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in cartItems" :key="item.id" class="cart-item-row">
                  <td class="col-checkbox">
                    <el-checkbox
                      v-model="item.selected"
                      @change="handleItemSelect"
                    />
                  </td>
                  <td class="col-image">
                    <img :src="item.image" :alt="item.name" class="product-image" />
                  </td>
                  <td class="col-code">{{ item.skuCode || item.productCode }}</td>
                  <td class="col-name">
                    <router-link :to="`/products/${item.productId}`" class="product-name-link">
                      {{ item.name }}
                    </router-link>
                    <div v-if="item.specText" class="sku-spec-text">规格：{{ item.specText }}</div>
                  </td>
                  <td v-if="isMember" class="col-price">
                    <span class="price-text">¥{{ item.salesPrice.toFixed(2) }}</span>
                  </td>
                  <td class="col-price">
                    <span class="price-text member-price">¥{{ item.memberPrice.toFixed(2) }}</span>
                  </td>
                  <td class="col-quantity">
                    <el-input-number
                      v-model="item.quantity"
                      :min="1"
                      :max="getItemMaxQuantity(item)"
                      size="small"
                      @change="handleQuantityChange(item)"
                    />
                  </td>
                  <td class="col-total">
                    <div class="total-price">¥{{ (item.memberPrice * item.quantity).toFixed(2) }}</div>
                    <div class="weight-text">({{ Number((item.weight || 0).toFixed(2)) }}克)</div>
                  </td>
                  <td class="col-action">
                    <el-button
                      type="danger"
                      :icon="Close"
                      circle
                      size="small"
                      @click="handleDeleteItem(item.id)"
                    />
                  </td>
                </tr>
                <tr v-if="cartItems.length === 0" class="empty-row">
                  <td :colspan="isMember ? 9 : 8" class="empty-cart">
                    <div class="empty-content">
                      <el-icon class="empty-icon"><ShoppingCart /></el-icon>
                      <div class="empty-text">购物车是空的，快去选购吧！</div>
                      <el-button type="danger" @click="$router.push('/products')">去购物</el-button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 购物车操作按钮 -->
          <div class="cart-actions">
            <el-button @click="$router.push('/products')">继续购物</el-button>
            <el-button @click="handleClearCart">清空购物车</el-button>
            <el-button @click="handleRecalculate">重算价格</el-button>
            <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
          </div>

          <!-- 购物车统计区 -->
          <div class="cart-summary">
            <div class="summary-info">
              <span class="selected-count">{{ selectedCount }} 合计:¥{{ selectedTotal.toFixed(2) }}</span>
            </div>
            <div class="summary-total">
              <div class="total-info">
                商品总重:{{ totalWeight.toFixed(2) }}克
                <span class="divider">|</span>
                此笔订单总计(商品数量:{{ totalCount }}): = ¥{{ totalAmount.toFixed(2) }}
              </div>
              <el-button type="danger" size="large" class="checkout-btn" @click="handleCheckout">
                去结账
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Close } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import {
  getCartList,
  addToCartByCode,
  updateCartQuantity,
  deleteCartItem,
  batchDeleteCartItems,
  clearCart
} from '@/api/buyer/cart'
import type { CartVO } from '@/api/buyer/cart'
import { getProductById } from '@/api/buyer/product'

const router = useRouter()

// 快速添加商品
const quickAdd = ref({
  productCode: '',
  quantity: 1
})

// 购物车商品列表
const cartItems = ref<CartVO[]>([])
const loading = ref(false)

// 全选状态
const selectAll = computed({
  get: () => cartItems.value.length > 0 && cartItems.value.every(item => item.selected),
  set: (val) => {
    cartItems.value.forEach(item => {
      item.selected = val
    })
  }
})

// 选中商品数量
const selectedCount = computed(() => {
  return cartItems.value.filter(item => item.selected).reduce((sum, item) => sum + item.quantity, 0)
})

// 选中商品总价
const selectedTotal = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + (item.memberPrice || 0) * item.quantity, 0)
})

// 商品总数量（只计算选中的商品）
const totalCount = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.quantity, 0)
})

// 商品总重量（只计算选中的商品，保留两位小数）
const totalWeight = computed(() => {
  const weight = cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + (item.weight || 0) * item.quantity, 0)
  return Number(weight.toFixed(2))
})

// 订单总金额（只计算选中的商品）
const totalAmount = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + (item.memberPrice || 0) * item.quantity, 0)
})

// 快速添加到购物车
const handleQuickAdd = async () => {
  if (!quickAdd.value.productCode.trim()) {
    ElMessage.warning('请输入货号')
    return
  }
  try {
    await addToCartByCode(quickAdd.value.productCode, quickAdd.value.quantity)
    ElMessage.success('已添加到购物车')
    quickAdd.value.productCode = ''
    quickAdd.value.quantity = 1
    // 重新加载购物车列表
    loadCartList()
  } catch (error: any) {
    // 如果全局拦截器已经显示过错误提示，这里就不再显示
    if (!error.__messageShown) {
      ElMessage.error(error.message || '添加失败')
    }
  }
}

// 全选/取消全选
const handleSelectAll = () => {
  // selectAll computed setter 会自动处理
}

// 单个商品选择
const handleItemSelect = () => {
  // 自动更新全选状态
}

// 获取商品的最大可购买数量
const getItemMaxQuantity = (item: CartVO) => {
  // 如果后端返回了库存信息，使用库存作为最大值
  if (item.stock !== undefined && item.stock !== null) {
    return Math.max(1, item.stock)
  }
  // 否则使用一个较大的默认值，后端会验证
  return 999
}

// 修改数量
const handleQuantityChange = async (item: CartVO) => {
  // 检查库存（如果后端返回了库存信息）
  if (item.stock !== undefined && item.stock !== null) {
    if (item.quantity > item.stock) {
      ElMessage.error(`库存不足，当前库存为 ${item.stock}`)
      // 恢复原数量
      loadCartList()
      return
    }
  }

  try {
    await updateCartQuantity(item.id, item.quantity)
    ElMessage.success('数量已更新')
  } catch (error: any) {
    // 如果全局拦截器已经显示过错误提示，这里就不再显示
    if (!error.__messageShown) {
      // 检查是否是库存不足的错误
      const errorMessage = error.response?.data?.message || error.message || '更新失败'
      if (errorMessage.includes('库存') || errorMessage.includes('不足') || errorMessage.includes('stock')) {
        ElMessage.error('库存不足，无法购买该数量')
      } else {
        ElMessage.error(errorMessage)
      }
    }
    // 重新加载购物车列表以恢复原数量
    loadCartList()
  }
}

// 删除商品
const handleDeleteItem = (id: number) => {
  ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCartItem(id)
      ElMessage.success('删除成功')
      loadCartList()
    } catch (error: any) {
      // 如果全局拦截器已经显示过错误提示，这里就不再显示
      if (!error.__messageShown) {
        ElMessage.error(error.message || '删除失败')
      }
    }
  }).catch(() => {})
}

// 清空购物车
const handleClearCart = () => {
  ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await clearCart()
      ElMessage.success('购物车已清空')
      loadCartList()
    } catch (error: any) {
      // 如果全局拦截器已经显示过错误提示，这里就不再显示
      if (!error.__messageShown) {
        ElMessage.error(error.message || '清空失败')
      }
    }
  }).catch(() => {})
}

// 重算价格
const handleRecalculate = () => {
  // TODO: 调用API重新计算价格
  ElMessage.success('价格已重新计算')
}

// 批量删除
const handleBatchDelete = () => {
  const selectedItems = cartItems.value.filter(item => item.selected)
  if (selectedItems.length === 0) {
    ElMessage.warning('请选择要删除的商品')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedItems.length} 个商品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = selectedItems.map(item => item.id)
      await batchDeleteCartItems(ids)
      ElMessage.success('删除成功')
      loadCartList()
    } catch (error: any) {
      // 如果全局拦截器已经显示过错误提示，这里就不再显示
      if (!error.__messageShown) {
        ElMessage.error(error.message || '删除失败')
      }
    }
  }).catch(() => {})
}

// 去结账
const handleCheckout = () => {
  const selectedItems = cartItems.value.filter(item => item.selected)
  if (selectedItems.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  // 跳转到结算页面，传递选中的购物车ID列表
  const cartIds = selectedItems.map(item => item.id)
  router.push({
    path: '/cart/checkout',
    query: {
      cartIds: cartIds.join(',')
    }
  })
}

// 判断用户是否是会员
const isMember = computed(() => {
  // 从购物车列表中获取第一个商品的isMember字段（所有商品的isMember应该相同）
  return cartItems.value.length > 0 && cartItems.value[0].isMember === 1
})

// 获取价格列标题（优先判断用户是否是会员，再判断商品是否启用会员价）
const getPriceColumnTitle = () => {
  // 1. 优先判断：如果不是会员或未登录，统一显示"商品价格"
  if (!isMember.value) {
    return '商品价格'
  }
  
  // 2. 如果是会员，再检查购物车中是否有启用会员价的商品
  // 如果至少有一个商品启用了会员价，就显示"会员价"
  const hasMemberPrice = cartItems.value.some(item => item.enableMemberPrice === 1)
  return hasMemberPrice ? '会员价' : '商品价格'
}

// 加载购物车列表
const loadCartList = async () => {
  loading.value = true
  try {
    const data = await getCartList()
    
    // 为每个商品获取库存信息
    const cartItemsWithStock = await Promise.all(
      data.map(async (item) => {
        try {
          // 获取商品详情以获取库存信息
          const productDetail = await getProductById(item.productId)
          
          let stock = productDetail.stock || 0
          
          // 如果商品有SKU且购物车项有skuId，需要找到对应SKU的库存
          if (item.skuId && productDetail.skus && productDetail.skus.length > 0) {
            const sku = productDetail.skus.find(s => s.id === item.skuId)
            if (sku) {
              stock = sku.stock || 0
            }
          }
          
          return {
            ...item,
            selected: true, // 默认选中
            stock: stock // 添加库存信息
          }
        } catch (error) {
          console.error(`获取商品 ${item.productId} 库存失败:`, error)
          // 如果获取库存失败，使用默认值，后端会验证
          return {
            ...item,
            selected: true,
            stock: undefined // 库存获取失败，不设置限制
          }
        }
      })
    )
    
    cartItems.value = cartItemsWithStock
  } catch (error: any) {
    // 如果全局拦截器已经显示过错误提示，这里就不再显示
    if (!error.__messageShown) {
      ElMessage.error(error.message || '加载购物车失败')
    }
    cartItems.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCartList()
})
</script>

<style scoped lang="scss">
.cart-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.cart-content {
  padding: 20px 0 40px;
}

// 购物流程进度条
.checkout-progress {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #eee;

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
          background: #409eff;
        }

        .step-text {
          color: #409eff;
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

// 快速添加商品区
.quick-add-section {
  background: #fff;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #eee;

  .add-form {
    display: flex;
    align-items: center;
    gap: 15px;

    .label {
      font-size: 14px;
      color: #333;
    }

    .input-field {
      width: 150px;
    }
  }
}

// 购物车商品列表
.cart-items-section {
  background: #fff;
  padding: 20px;
  border: 1px solid #eee;

  .section-title {
    background: #ffffcc;
    padding: 10px 15px;
    margin-bottom: 10px;
    font-size: 16px;
    font-weight: bold;
    color: #333;

    .title-icon {
      color: #e4393c;
      margin-right: 8px;
    }
  }

  .section-subtitle {
    font-size: 12px;
    color: #666;
    margin-bottom: 20px;
    padding-left: 15px;
  }

  .cart-table-wrapper {
    overflow-x: auto;
    margin-bottom: 20px;
  }

  .cart-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;

    thead {
      background: #f5f5f5;

      th {
        padding: 12px;
        text-align: center;
        font-weight: bold;
        color: #333;
        border: 1px solid #eee;
      }
    }

    tbody {
      .cart-item-row {
        border-bottom: 1px solid #eee;

        &:hover {
          background: #f9f9f9;
        }

        td {
          padding: 15px;
          text-align: center;
          border: 1px solid #eee;
          vertical-align: middle;
        }

        .col-checkbox {
          width: 60px;
        }

        .col-image {
          width: 100px;

          .product-image {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border: 1px solid #eee;
          }
        }

        .col-code {
          width: 100px;
          color: #666;
        }

        .col-name {
          width: 300px;
          text-align: left;
          color: #333;

          .product-name-link {
            color: #333;
            text-decoration: none;
            cursor: pointer;
            transition: color 0.3s;

            &:hover {
              color: #409eff;
              text-decoration: underline;
            }
          }

          .sku-spec-text {
            font-size: 12px;
            color: #999;
            margin-top: 5px;
          }
        }

        .col-price {
          width: 100px;

          .price-text {
            color: #666;

            &.member-price {
              color: #e4393c;
              font-weight: bold;
            }
          }
        }

        .col-quantity {
          width: 120px;
        }

        .col-total {
          width: 120px;

          .total-price {
            color: #e4393c;
            font-weight: bold;
            font-size: 16px;
            margin-bottom: 5px;
          }

          .weight-text {
            font-size: 12px;
            color: #999;
          }
        }

        .col-action {
          width: 80px;
        }
      }

      .empty-row {
        .empty-cart {
          padding: 60px 20px;
          text-align: center;

          .empty-content {
            .empty-icon {
              font-size: 64px;
              color: #ccc;
              margin-bottom: 20px;
            }

            .empty-text {
              font-size: 16px;
              color: #999;
              margin-bottom: 20px;
            }
          }
        }
      }
    }
  }

  // 购物车操作按钮
  .cart-actions {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    padding: 0 15px;
  }

  // 购物车统计区
  .cart-summary {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    background: #f9f9f9;
    border: 1px solid #eee;

    .summary-info {
      font-size: 14px;
      color: #666;

      .selected-count {
        color: #e4393c;
        font-weight: bold;
      }
    }

    .summary-total {
      display: flex;
      align-items: center;
      gap: 20px;

      .total-info {
        font-size: 14px;
        color: #333;

        .divider {
          margin: 0 10px;
          color: #ccc;
        }
      }

      .checkout-btn {
        padding: 15px 50px;
        font-size: 18px;
        font-weight: bold;
      }
    }
  }
}
</style>

