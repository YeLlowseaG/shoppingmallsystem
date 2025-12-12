<template>
  <div class="hot-products">
    <div class="container">
      <div
        class="product-card"
        v-for="product in products"
        :key="product.id"
        @click="goToDetail(product.id)"
      >
        <div class="brand-tag">热销</div>
        <img :src="product.mainImage" :alt="product.productName" class="product-image" />
        <div class="product-info">
          <div class="product-name">{{ product.productName }}</div>
          <div class="product-price">¥{{ parseFloat(product.basePrice).toFixed(2) }}</div>
          <div class="product-sales">已售 {{ product.salesCount }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHotProducts, type ProductVO } from '@/api/buyer/product'

const router = useRouter()
const products = ref<ProductVO[]>([])

// 加载热门商品
const loadHotProducts = async () => {
  try {
    products.value = await getHotProducts(4)
  } catch (error) {
    console.error('加载热门商品失败:', error)
  }
}

// 跳转到商品详情
const goToDetail = (id: number) => {
  router.push(`/products/${id}`)
}

onMounted(() => {
  loadHotProducts()
})
</script>

<style scoped lang="scss">
.hot-products {
  background: #fff;
  padding: 20px 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 15px;
  }

  .product-card {
    position: relative;
    border: 2px solid transparent;
    border-radius: 4px;
    overflow: hidden;
    transition: all 0.3s;
    cursor: pointer;

    &:hover {
      border-color: #e4393c;
      box-shadow: 0 4px 12px rgba(228, 57, 60, 0.2);
    }

    .brand-tag {
      position: absolute;
      top: 10px;
      left: 10px;
      background: #e4393c;
      color: #fff;
      padding: 4px 12px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: bold;
      z-index: 1;
    }

    .product-image {
      width: 100%;
      height: 280px;
      object-fit: cover;
    }

    .product-info {
      padding: 15px;
      background: #fff;

      .product-name {
        font-size: 14px;
        font-weight: bold;
        color: #333;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .product-price {
        font-size: 18px;
        font-weight: bold;
        color: #e4393c;
        margin-bottom: 5px;
      }

      .product-sales {
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>
