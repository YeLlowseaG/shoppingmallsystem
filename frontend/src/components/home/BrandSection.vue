<template>
  <div class="brand-section">
    <div class="container">
      <!-- 左侧品牌专区 -->
      <div class="brand-area">
        <div class="title">品牌专区</div>
        <div class="brand-grid">
          <div
            v-for="brand in brands"
            :key="brand.id"
            class="brand-item"
            @click="goToBrand(brand.brandName)"
          >
            <img :src="brand.logoUrl" :alt="brand.brandName" />
          </div>
        </div>
      </div>

      <!-- 右侧精品推荐广告 -->
      <div class="recommend-ads">
        <img
          v-for="ad in ads"
          :key="ad.id"
          :src="ad.imageUrl"
          :alt="ad.adName"
          class="ad-image"
          @click="goToAdTarget(ad)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getActiveBrands, getAdvertisementsByPosition, type Brand, type Advertisement } from '@/api/buyer/website'

const router = useRouter()

// 品牌数据
const brands = ref<Brand[]>([])

// 侧边广告数据
const ads = ref<Advertisement[]>([])

// 加载品牌列表
const loadBrands = async () => {
  try {
    brands.value = await getActiveBrands(16) // 限制16个品牌
  } catch (error) {
    console.error('加载品牌失败:', error)
  }
}

// 加载侧边广告
const loadAds = async () => {
  try {
    const ad1 = await getAdvertisementsByPosition('brand_side_1')
    const ad2 = await getAdvertisementsByPosition('brand_side_2')
    ads.value = [...ad1, ...ad2]
  } catch (error) {
    console.error('加载广告失败:', error)
  }
}

// 跳转到品牌商品列表页
const goToBrand = (brandName: string) => {
  router.push({
    path: '/products',
    query: { brand: brandName }
  })
}

// 跳转到广告目标
const goToAdTarget = (ad: Advertisement) => {
  if (ad.linkType === 0 || !ad.linkValue) return

  switch (ad.linkType) {
    case 1: // 商品分类
      router.push(`/products?categoryId=${ad.linkValue}`)
      break
    case 2: // 商品详情
      router.push(`/products/${ad.linkValue}`)
      break
    case 3: // 促销活动
      router.push(`/products?type=${ad.linkValue}`)
      break
    case 4: // 外部链接
      window.open(ad.linkValue, '_blank')
      break
  }
}

onMounted(() => {
  loadBrands()
  loadAds()
})
</script>

<style scoped lang="scss">
.brand-section {
  background: #fff;
  padding: 30px 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
    display: flex;
    gap: 20px;
  }

  .brand-area {
    flex: 1;

    .title {
      background: linear-gradient(to right, #FFD93D, #FFA500);
      color: #fff;
      font-size: 16px;
      font-weight: bold;
      padding: 12px 20px;
      border-radius: 4px 4px 0 0;
    }

    .brand-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1px;
      background: #eee;
      border: 1px solid #eee;

      .brand-item {
        background: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 15px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background: #f9f9f9;
          transform: scale(1.05);
        }

        img {
          max-width: 100%;
          height: auto;
        }
      }
    }
  }

  .recommend-ads {
    display: flex;
    flex-direction: column;
    gap: 15px;

    .ad-image {
      width: 280px;
      height: 220px;
      object-fit: cover;
      border-radius: 4px;
      cursor: pointer;
      transition: transform 0.3s;

      &:hover {
        transform: scale(1.02);
      }
    }
  }
}
</style>
