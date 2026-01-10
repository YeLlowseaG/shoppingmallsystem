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

    <!-- 动态楼层：根据广告数据生成 -->
    <CategoryFloor
      v-for="floor in floorData"
      :key="floor.floorNumber"
      :floor-number="floor.floorNumber"
      :category-name="floor.categoryName"
      :title-color="floor.titleColor"
      :category-id="floor.categoryId"
      :big-ad-image="floor.bigAd"
      :ad-link-type="floor.adLinkType"
      :ad-link-value="floor.adLinkValue"
      :side-products="floor.sideProducts"
      :bottom-products="floor.bottomProducts"
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
import { getAllFloorAdvertisements } from '@/api/buyer/website'

// 广告位置与分类ID的映射配置（根据实际数据库分类ID）
// 格式：广告位置 -> { 分类ID, 标题颜色 }
const floorConfig: Record<string, { categoryId: number; titleColor: string }> = {
  floor_1: { categoryId: 5, titleColor: 'linear-gradient(to right, #FF6B9D, #E4393C)' },  // 男用器具
  floor_2: { categoryId: 6, titleColor: 'linear-gradient(to right, #9D50BB, #6C5CE7)' },  // 女用器具
  floor_3: { categoryId: 7, titleColor: 'linear-gradient(to right, #74B9FF, #0984E3)' },  // 润滑清洁
  floor_4: { categoryId: 8, titleColor: 'linear-gradient(to right, #FFD93D, #FFA500)' },  // 情趣内衣
  floor_5: { categoryId: 9, titleColor: 'linear-gradient(to right, #FD79A8, #E84393)' },  // 延时保健
  floor_6: { categoryId: 11, titleColor: 'linear-gradient(to right, #55EFC4, #00B894)' },  // 喷剂助情
  floor_7: { categoryId: 12, titleColor: 'linear-gradient(to right, #A29BFE, #6C5CE7)' }   // 其他情趣
}

// 楼层数据
const floorData = ref<Array<{
  floorNumber: string
  categoryName: string
  titleColor: string
  categoryId: number
  bigAd: string
  adLinkType?: number
  adLinkValue?: string
  sideProducts: any[]
  bottomProducts: any[]
}>>([])

// 转换商品数据格式
const convertProduct = (product: ProductVO) => {
  // 如果启用了SKU且有SKU数据，优先使用第一个SKU的价格
  const hasSku = product.enableSpec === 1 && product.skus && product.skus.length > 0
  const firstSku = hasSku && product.skus ? product.skus[0] : null

  return {
    id: product.id,
    name: product.productName,
    image: product.mainImage,
    // 价格：优先使用SKU价格，否则使用商品价格
    price: firstSku?.price ?? product.basePrice,
    // 会员价：优先使用SKU会员价，否则使用商品会员价
    memberPrice: firstSku?.memberPrice ?? product.memberPrice ?? product.basePrice,
    // 原价：优先使用SKU市场零售价，否则使用商品市场零售价，最后使用基础价的1.5倍
    originalPrice: firstSku?.marketRetailPrice ?? product.marketRetailPrice ?? product.basePrice * 1.5,
    category: product.categoryName,
    tag: '',
    salesCount: product.salesCount
  }
}

// 从广告位置提取楼层编号（如 floor_1 -> 1F）
const getFloorNumber = (adPosition: string): string => {
  const match = adPosition.match(/floor_(\d+)/)
  if (match) {
    return `${match[1]}F`
  }
  return ''
}

// 加载楼层数据（根据广告数据动态生成）
const loadFloorData = async () => {
  try {
    // 先加载楼层广告（只返回启用状态的广告）
    const ads = await getAllFloorAdvertisements()
    
    // 根据广告数据动态生成楼层
    const floors = await Promise.all(
      ads.map(async (ad) => {
        const config = floorConfig[ad.adPosition]
        if (!config) {
          // 如果广告位置没有配置，跳过
          return null
        }

        // 确定使用的分类ID：优先使用广告配置中的分类ID，否则使用默认配置
        let categoryId = config.categoryId
        if (ad.linkType === 1 && ad.linkValue) {
          // linkType=1 表示商品分类，linkValue 是分类ID
          const adCategoryId = Number(ad.linkValue)
          if (!isNaN(adCategoryId)) {
            categoryId = adCategoryId
          }
        }

        // 获取该分类的推荐商品（6个）
        let products: ProductVO[] = []
        try {
          products = await getRecommendProducts(categoryId, 6)
        } catch (error) {
          console.error(`加载分类 ${categoryId} 的商品失败:`, error)
        }
        
        const convertedProducts = products.map(convertProduct)

        return {
          floorNumber: getFloorNumber(ad.adPosition),
          categoryName: ad.adName,  // 使用广告名称作为标题
          titleColor: config.titleColor,
          categoryId: categoryId,  // 使用实际使用的分类ID
          bigAd: ad.imageUrl || '',  // 使用广告图片
          adLinkType: ad.linkType,  // 广告链接类型
          adLinkValue: ad.linkValue,  // 广告链接值
          sideProducts: convertedProducts.slice(0, 2),  // 前2个作为侧边商品
          bottomProducts: convertedProducts.slice(2, 6)  // 后4个作为底部商品
        }
      })
    )

    // 过滤掉 null 值（没有配置的广告位置）
    floorData.value = floors.filter((floor): floor is NonNullable<typeof floor> => floor !== null)
  } catch (error) {
    console.error('加载楼层数据失败:', error)
    floorData.value = []
  }
}

onMounted(async () => {
  // 加载楼层数据（内部会先加载广告数据）
  await loadFloorData()
})
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  background: #f5f5f5;
}
</style>
