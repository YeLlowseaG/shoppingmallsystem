<template>
  <div :class="['product-card', viewMode]" @click="goToDetail">
    <!-- 网格模式 -->
    <template v-if="viewMode === 'grid'">
      <div class="image-wrapper">
        <img :src="product.image" :alt="product.name" />
        <div v-if="product.tags" class="tag">{{ product.tags }}</div>
      </div>
      <div class="info">
        <div class="category">{{ product.category }}</div>
        <div class="name" :title="product.name">{{ product.name }}</div>
        <div class="price-row">
          <span class="price" :class="{ member: showMemberPrice }">¥{{ displayPrice }}</span>
          <span v-if="showMemberPrice" class="original-price">¥{{ formatNumber(product.price) }}</span>
          <span v-else-if="product.originalPrice" class="original-price">¥{{ formatNumber(product.originalPrice) }}</span>
        </div>
        <div class="meta">
          <span class="sales">销量: {{ formatSales(product.sales) }}</span>
          <span v-if="product.rating" class="rating">
            <el-rate
              v-model="product.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
              :max="5"
              size="small"
            />
          </span>
        </div>
      </div>
    </template>

    <!-- 列表模式 -->
    <template v-else>
      <div class="image-wrapper">
        <img :src="product.image" :alt="product.name" />
        <div v-if="product.tags" class="tag">{{ product.tags }}</div>
      </div>
      <div class="info">
        <div class="name" :title="product.name">{{ product.name }}</div>
        <div class="category-brand">
          <span class="category">{{ product.category }}</span>
          <span v-if="product.brand" class="brand">品牌: {{ product.brand }}</span>
        </div>
        <div class="price-row">
          <span class="price" :class="{ member: showMemberPrice }">¥{{ displayPrice }}</span>
          <span v-if="showMemberPrice" class="original-price">¥{{ formatNumber(product.price) }}</span>
          <span v-else-if="product.originalPrice" class="original-price">¥{{ formatNumber(product.originalPrice) }}</span>
        </div>
        <div class="meta">
          <span class="sales">销量: {{ formatSales(product.sales) }}</span>
          <span v-if="product.rating" class="rating">
            评分: {{ product.rating }}
          </span>
        </div>
      </div>
      <div class="actions">
        <el-button 
          type="danger" 
          :loading="addingToCart"
          :disabled="addingToCart"
          @click.stop="addToCart"
        >
          {{ addingToCart ? '加入中...' : '加入购物车' }}
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addToCart as addToCartAPI, type AddCartDTO } from '@/api/buyer/cart'
import { useCartStore } from '@/stores/cart'

interface Product {
  id: number
  name: string
  category: string
  brand?: string
  image: string
  price: number | string
  memberPrice?: number | string
  originalPrice?: number | string
  sales?: number
  tags?: string
  rating?: number | string
  stock?: number
  status?: number
}

interface Props {
  product: Product
  viewMode?: 'grid' | 'list'
}

const props = withDefaults(defineProps<Props>(), {
  viewMode: 'grid'
})

const router = useRouter()
const cartStore = useCartStore()

const addingToCart = ref(false)

// 格式化销量
const formatNumber = (val?: number | string) => {
  const num = parseFloat(String(val ?? 0))
  return Number.isNaN(num) ? '0.00' : num.toFixed(2)
}

const showMemberPrice = computed(() => {
  if (props.product.memberPrice === undefined || props.product.memberPrice === null) return false
  const member = parseFloat(String(props.product.memberPrice))
  const price = parseFloat(String(props.product.price))
  if (Number.isNaN(member) || Number.isNaN(price)) return false
  return member > 0 && Math.abs(member - price) > 0.0001
})

const displayPrice = computed(() => {
  if (showMemberPrice.value) {
    return formatNumber(props.product.memberPrice)
  }
  return formatNumber(props.product.price)
})

const formatSales = (sales?: number) => {
  if (!sales) return '0'
  if (sales >= 10000) {
    return `${(sales / 10000).toFixed(1)}万`
  }
  return sales.toString()
}

// 跳转到商品详情
const goToDetail = () => {
  router.push({
    path: `/products/${props.product.id}`
  })
}

// 加入购物车
const addToCart = async (e?: Event) => {
  // 阻止事件冒泡，避免触发跳转到详情页
  if (e) {
    e.stopPropagation()
  }

  if (addingToCart.value) return

  try {
    // 检查商品ID
    if (!props.product.id) {
      ElMessage.error('商品信息不存在')
      return
    }

    // 检查商品状态（如果商品已下架）
    if (props.product.status !== undefined && props.product.status !== 1) {
      ElMessage.warning('商品已下架，无法添加到购物车')
      return
    }

    // 检查库存（如果有库存信息且库存为0）
    if (props.product.stock !== undefined && props.product.stock <= 0) {
      ElMessage.warning('商品库存不足，无法添加到购物车')
      return
    }

    addingToCart.value = true

    const cartData: AddCartDTO = {
      productId: props.product.id,
      quantity: 1
    }

    await addToCartAPI(cartData)

    // 更新购物车数量
    await cartStore.updateCartCount()

    ElMessage.success('已成功加入购物车！')
  } catch (error: any) {
    console.error('加入购物车失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('请先登录')
      router.push('/login')
    } else {
      ElMessage.error(error.response?.data?.message || '加入购物车失败，请重试')
    }
  } finally {
    addingToCart.value = false
  }
}
</script>

<style scoped lang="scss">
.product-card {
  background: #fff;
  cursor: pointer;
  transition: all 0.3s;

  &.grid {
    border: 1px solid #eee;
    border-radius: 4px;
    overflow: hidden;

    &:hover {
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }

    .image-wrapper {
      position: relative;
      width: 100%;
      padding-top: 100%; // 1:1 比例
      overflow: hidden;
      background: #f5f5f5;

      img {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s;
      }

      .tag {
        position: absolute;
        top: 10px;
        left: 10px;
        background: #e4393c;
        color: #fff;
        padding: 4px 8px;
        font-size: 12px;
        border-radius: 2px;
      }
    }

    &:hover .image-wrapper img {
      transform: scale(1.05);
    }

    .info {
      padding: 12px;

      .category {
        font-size: 12px;
        color: #999;
        margin-bottom: 5px;
      }

      .name {
        font-size: 14px;
        color: #333;
        margin-bottom: 8px;
        height: 40px;
        line-height: 20px;
        overflow: hidden;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }

      .price-row {
        margin-bottom: 8px;

        .price {
          color: #e4393c;
          font-size: 20px;
          font-weight: bold;
          margin-right: 8px;

          &.member {
            color: #e4393c;
          }
        }

        .original-price {
          color: #999;
          font-size: 14px;
          text-decoration: line-through;
        }
      }

      .meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 12px;
        color: #666;

        .sales {
          color: #999;
        }

        .rating {
          :deep(.el-rate) {
            height: 16px;
          }
        }
      }
    }
  }

  &.list {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 15px;
    border: 1px solid #eee;
    border-radius: 4px;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }

    .image-wrapper {
      position: relative;
      width: 180px;
      height: 180px;
      flex-shrink: 0;
      overflow: hidden;
      background: #f5f5f5;
      border-radius: 4px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .tag {
        position: absolute;
        top: 10px;
        left: 10px;
        background: #e4393c;
        color: #fff;
        padding: 4px 8px;
        font-size: 12px;
        border-radius: 2px;
      }
    }

    .info {
      flex: 1;

      .name {
        font-size: 16px;
        color: #333;
        font-weight: 500;
        margin-bottom: 10px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .category-brand {
        display: flex;
        gap: 15px;
        margin-bottom: 10px;
        font-size: 13px;

        .category {
          color: #999;
        }

        .brand {
          color: #666;
        }
      }

      .price-row {
        margin-bottom: 10px;

        .price {
          color: #e4393c;
          font-size: 24px;
          font-weight: bold;
          margin-right: 10px;
        }

        .original-price {
          color: #999;
          font-size: 16px;
          text-decoration: line-through;
        }
      }

      .meta {
        display: flex;
        gap: 20px;
        font-size: 13px;
        color: #666;

        .sales {
          color: #999;
        }
      }
    }

    .actions {
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      gap: 10px;

      .el-button {
        width: 120px;
      }
    }
  }
}
</style>
