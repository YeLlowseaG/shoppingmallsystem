<template>
  <div class="banner">
    <el-carousel height="450px" :interval="4000" arrow="always">
      <el-carousel-item v-for="item in banners" :key="item.id">
        <img
          :src="item.imageUrl"
          :alt="item.title"
          class="banner-image"
          @click="handleBannerClick(item)"
        />
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getActiveBanners, type Banner } from '@/api/buyer/website'

const router = useRouter()

// 轮播图数据（从API获取）
const banners = ref<Banner[]>([])

// 加载轮播图
const loadBanners = async () => {
  try {
    banners.value = await getActiveBanners()
  } catch (error) {
    console.error('加载轮播图失败:', error)
  }
}

const handleBannerClick = (banner: Banner) => {
  if (banner.linkType === 0 || !banner.linkValue) return

  // 根据链接类型跳转
  switch (banner.linkType) {
    case 1: // 商品分类
      router.push(`/products?categoryId=${banner.linkValue}`)
      break
    case 2: // 商品详情
      router.push(`/products/${banner.linkValue}`)
      break
    case 3: // 促销活动
      router.push(`/products?type=${banner.linkValue}`)
      break
    case 4: // 外部链接
      window.open(banner.linkValue, '_blank')
      break
  }
}

onMounted(() => {
  loadBanners()
})
</script>

<style scoped lang="scss">
.banner {
  width: 100%;

  .banner-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    cursor: pointer;
    transition: opacity 0.3s;

    &:hover {
      opacity: 0.95;
    }
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
