<template>
  <div class="hot-products">
    <div class="container">
      <div class="product-card" v-for="product in products" :key="product.id">
        <div class="brand-tag">{{ product.brand }}</div>
        <img :src="product.image" :alt="product.name" class="product-image" />
        <div class="product-info">
          <div class="product-name">{{ product.name }}</div>
          <div class="product-desc">{{ product.desc }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const products = ref([
  {
    id: 1,
    brand: 'ANGUS',
    name: '极尿酸润滑液',
    desc: '温和润滑 全新体验',
    image: 'https://via.placeholder.com/280x280/FF6B9D/ffffff?text=Product+1'
  },
  {
    id: 2,
    brand: '虚姬',
    name: '魅动遥控跳蛋',
    desc: 'USB充电 十段强震',
    image: 'https://via.placeholder.com/280x280/9D50BB/ffffff?text=Product+2'
  },
  {
    id: 3,
    brand: '虚姬',
    name: '依依酱飞机杯',
    desc: '健硅飞机杯 仿真吮吸',
    image: 'https://via.placeholder.com/280x280/FFD93D/ffffff?text=Product+3'
  },
  {
    id: 4,
    brand: '名姬',
    name: '果冻软胸',
    desc: '好色人妻 三交巨乳',
    image: 'https://via.placeholder.com/280x280/6C5CE7/ffffff?text=Product+4'
  }
])
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
        margin-bottom: 5px;
      }

      .product-desc {
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>
