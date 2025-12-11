<template>
  <div class="home-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 轮播图 -->
    <Banner />

    <!-- 热门商品卡片 -->
    <HotProducts />

    <!-- 品牌展示 -->
    <BrandSection />

    <!-- 1F 男用器具 -->
    <CategoryFloor
      v-if="floorData[0]"
      floor-number="1F"
      category-name="男用器具"
      title-color="linear-gradient(to right, #FF6B9D, #E4393C)"
      :category-id="1"
      :big-ad-image="floorData[0].bigAd"
      :side-products="floorData[0].sideProducts"
      :bottom-products="floorData[0].bottomProducts"
    />

    <!-- 2F 女用器具 -->
    <CategoryFloor
      v-if="floorData[1]"
      floor-number="2F"
      category-name="女用器具"
      title-color="linear-gradient(to right, #9D50BB, #6C5CE7)"
      :category-id="2"
      :big-ad-image="floorData[1].bigAd"
      :side-products="floorData[1].sideProducts"
      :bottom-products="floorData[1].bottomProducts"
    />

    <!-- 3F 润滑清洁 -->
    <CategoryFloor
      v-if="floorData[2]"
      floor-number="3F"
      category-name="润滑清洁"
      title-color="linear-gradient(to right, #74B9FF, #0984E3)"
      :category-id="3"
      :big-ad-image="floorData[2].bigAd"
      :side-products="floorData[2].sideProducts"
      :bottom-products="floorData[2].bottomProducts"
    />

    <!-- 4F 情趣内衣 -->
    <CategoryFloor
      v-if="floorData[3]"
      floor-number="4F"
      category-name="情趣内衣"
      title-color="linear-gradient(to right, #FFD93D, #FFA500)"
      :category-id="4"
      :big-ad-image="floorData[3].bigAd"
      :side-products="floorData[3].sideProducts"
      :bottom-products="floorData[3].bottomProducts"
    />

    <!-- 5F 延时保健 -->
    <CategoryFloor
      v-if="floorData[4]"
      floor-number="5F"
      category-name="延时保健"
      title-color="linear-gradient(to right, #FD79A8, #E84393)"
      :category-id="5"
      :big-ad-image="floorData[4].bigAd"
      :side-products="floorData[4].sideProducts"
      :bottom-products="floorData[4].bottomProducts"
    />

    <!-- 6F 喷剂助情 -->
    <CategoryFloor
      v-if="floorData[5]"
      floor-number="6F"
      category-name="喷剂助情"
      title-color="linear-gradient(to right, #55EFC4, #00B894)"
      :category-id="6"
      :big-ad-image="floorData[5].bigAd"
      :side-products="floorData[5].sideProducts"
      :bottom-products="floorData[5].bottomProducts"
    />

    <!-- 7F 其他情趣 -->
    <CategoryFloor
      v-if="floorData[6]"
      floor-number="7F"
      category-name="其他情趣"
      title-color="linear-gradient(to right, #A29BFE, #6C5CE7)"
      :category-id="7"
      :big-ad-image="floorData[6].bigAd"
      :side-products="floorData[6].sideProducts"
      :bottom-products="floorData[6].bottomProducts"
    />

    <!-- 底部 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Banner from '@/components/home/Banner.vue'
import HotProducts from '@/components/home/HotProducts.vue'
import BrandSection from '@/components/home/BrandSection.vue'
import CategoryFloor from '@/components/home/CategoryFloor.vue'
import Footer from '@/components/home/Footer.vue'
import { getRecommendProducts, type ProductVO } from '@/api/buyer/product'
import { getAllFloorAdvertisements, type Advertisement } from '@/api/buyer/website'

// 分类ID配置（根据实际数据库分类ID）
const categoryIds = [5, 6, 7, 8, 9, 11, 12]  // 男用器具、女用器具、润滑剂、安全套、护理用品、女士内衣、男士内衣

// 楼层数据
const floorData = ref<any[]>([])

// 楼层广告数据
const floorAds = ref<Advertisement[]>([])

// 转换商品数据格式
const convertProduct = (product: ProductVO) => ({
  id: product.id,
  name: product.productName,
  image: product.mainImage,
  price: product.basePrice,
  originalPrice: product.basePrice * 1.5,  // 原价设置为基础价的1.5倍
  category: product.categoryName,
  tag: ''
})

// 加载楼层广告
const loadFloorAds = async () => {
  try {
    floorAds.value = await getAllFloorAdvertisements()
  } catch (error) {
    console.error('加载楼层广告失败:', error)
  }
}

// 获取楼层广告图片
const getFloorAdImage = (floorIndex: number): string => {
  // 根据楼层索引获取对应位置的广告
  const positionMap = ['floor_1', 'floor_2', 'floor_3', 'floor_4', 'floor_5', 'floor_6', 'floor_7']
  const position = positionMap[floorIndex]

  const ad = floorAds.value.find(item => item.adPosition === position)
  if (ad && ad.imageUrl) {
    return ad.imageUrl
  }

  // 如果没有找到对应广告，返回占位符
  const placeholderColors = ['FF6B9D', '9D50BB', '74B9FF', 'FFD93D', 'FD79A8', '55EFC4', 'A29BFE']
  return `https://via.placeholder.com/800x400/${placeholderColors[floorIndex]}/ffffff?text=Floor+${floorIndex + 1}`
}

// 加载楼层数据
const loadFloorData = async () => {
  try {
    const floors = await Promise.all(
      categoryIds.map(async (categoryId, index) => {
        // 获取该分类的推荐商品（6个）
        const products = await getRecommendProducts(categoryId, 6)
        const convertedProducts = products.map(convertProduct)

        return {
          bigAd: getFloorAdImage(index),
          sideProducts: convertedProducts.slice(0, 2),  // 前2个作为侧边商品
          bottomProducts: convertedProducts.slice(2, 6)  // 后4个作为底部商品
        }
      })
    )
    floorData.value = floors
  } catch (error) {
    console.error('加载楼层数据失败:', error)
    // 失败时使用空数据
    floorData.value = categoryIds.map((_, index) => ({
      bigAd: getFloorAdImage(index),
      sideProducts: [],
      bottomProducts: []
    }))
  }
}

onMounted(async () => {
  // 先加载楼层广告，再加载楼层数据
  await loadFloorAds()
  await loadFloorData()
})
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  background: #f5f5f5;
}
</style>
