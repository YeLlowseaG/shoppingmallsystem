<template>
  <div class="banner">
    <el-carousel height="450px" :interval="4000" arrow="always">
      <el-carousel-item v-for="(item, index) in banners" :key="index">
        <img :src="item.image" :alt="item.title" class="banner-image" />
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 模拟轮播图数据（后续从接口获取）
const banners = ref([
  {
    id: 1,
    title: '新品上市',
    image: 'https://via.placeholder.com/1200x450/FF6B9D/ffffff?text=新品上市'
  },
  {
    id: 2,
    title: '热销商品',
    image: 'https://via.placeholder.com/1200x450/9D50BB/ffffff?text=热销商品'
  },
  {
    id: 3,
    title: '特价活动',
    image: 'https://via.placeholder.com/1200x450/6C5CE7/ffffff?text=特价活动'
  }
])
</script>

<style scoped lang="scss">
.banner {
  width: 100%;

  .banner-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  :deep(.el-carousel__indicator) {
    .el-carousel__button {
      background-color: rgba(255, 255, 255, 0.5);
    }

    &.is-active .el-carousel__button {
      background-color: #e4393c;
    }
  }
}
</style>
