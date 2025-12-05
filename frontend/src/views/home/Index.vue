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
      floor-number="1F"
      category-name="男用器具"
      title-color="linear-gradient(to right, #FF6B9D, #E4393C)"
      :big-ad-image="floorData[0].bigAd"
      :side-products="floorData[0].sideProducts"
      :bottom-products="floorData[0].bottomProducts"
    />

    <!-- 2F 女用器具 -->
    <CategoryFloor
      floor-number="2F"
      category-name="女用器具"
      title-color="linear-gradient(to right, #9D50BB, #6C5CE7)"
      :big-ad-image="floorData[1].bigAd"
      :side-products="floorData[1].sideProducts"
      :bottom-products="floorData[1].bottomProducts"
    />

    <!-- 3F 润滑清洁 -->
    <CategoryFloor
      floor-number="3F"
      category-name="润滑清洁"
      title-color="linear-gradient(to right, #74B9FF, #0984E3)"
      :big-ad-image="floorData[2].bigAd"
      :side-products="floorData[2].sideProducts"
      :bottom-products="floorData[2].bottomProducts"
    />

    <!-- 4F 情趣内衣 -->
    <CategoryFloor
      floor-number="4F"
      category-name="情趣内衣"
      title-color="linear-gradient(to right, #FFD93D, #FFA500)"
      :big-ad-image="floorData[3].bigAd"
      :side-products="floorData[3].sideProducts"
      :bottom-products="floorData[3].bottomProducts"
    />

    <!-- 5F 延时保健 -->
    <CategoryFloor
      floor-number="5F"
      category-name="延时保健"
      title-color="linear-gradient(to right, #FD79A8, #E84393)"
      :big-ad-image="floorData[4].bigAd"
      :side-products="floorData[4].sideProducts"
      :bottom-products="floorData[4].bottomProducts"
    />

    <!-- 6F 喷剂助情 -->
    <CategoryFloor
      floor-number="6F"
      category-name="喷剂助情"
      title-color="linear-gradient(to right, #55EFC4, #00B894)"
      :big-ad-image="floorData[5].bigAd"
      :side-products="floorData[5].sideProducts"
      :bottom-products="floorData[5].bottomProducts"
    />

    <!-- 7F 其他情趣 -->
    <CategoryFloor
      floor-number="7F"
      category-name="其他情趣"
      title-color="linear-gradient(to right, #A29BFE, #6C5CE7)"
      :big-ad-image="floorData[6].bigAd"
      :side-products="floorData[6].sideProducts"
      :bottom-products="floorData[6].bottomProducts"
    />

    <!-- 底部 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Banner from '@/components/home/Banner.vue'
import HotProducts from '@/components/home/HotProducts.vue'
import BrandSection from '@/components/home/BrandSection.vue'
import CategoryFloor from '@/components/home/CategoryFloor.vue'
import Footer from '@/components/home/Footer.vue'

// 模拟楼层数据（后续从接口获取）
const floorData = ref([
  {
    // 1F 男用器具
    bigAd: 'https://via.placeholder.com/800x400/FF6B9D/ffffff?text=新品上市+好物抢先购',
    sideProducts: [
      {
        id: 1,
        name: '双穴情欲小翘臀',
        tag: '丰满翘臀',
        image: 'https://via.placeholder.com/195x195?text=Product',
        price: 134.0,
        originalPrice: 201.0
      },
      {
        id: 2,
        name: '极上爆乳美模名器',
        tag: '酥胸把位',
        image: 'https://via.placeholder.com/195x195?text=Product',
        price: 134.0,
        originalPrice: 201.0
      }
    ],
    bottomProducts: [
      {
        id: 3,
        category: '男用器具',
        name: 'AV女优三穴仿道路 [虞姬]',
        image: 'https://via.placeholder.com/280x280?text=Product',
        price: 134.0,
        originalPrice: 201.0
      },
      {
        id: 4,
        category: '男用器具',
        name: '娇嫩酥胸小萌妹 [虞姬]',
        image: 'https://via.placeholder.com/280x280?text=Product',
        price: 134.0,
        originalPrice: 201.0
      },
      {
        id: 5,
        category: '男用器具',
        name: '好色人妻巨乳 [虞姬]',
        image: 'https://via.placeholder.com/280x280?text=Product',
        price: 239.0,
        originalPrice: 358.0
      },
      {
        id: 6,
        category: '男用器具',
        name: '深喉洁音震动双穴飞机杯 [虞姬]',
        image: 'https://via.placeholder.com/280x280?text=Product',
        price: 134.0,
        originalPrice: 201.0
      }
    ]
  },
  {
    // 2F 女用器具
    bigAd: 'https://via.placeholder.com/800x400/9D50BB/ffffff?text=女用器具专区',
    sideProducts: [
      {
        id: 7,
        name: '粉红小猪震动棒',
        tag: '会阴古尼发声',
        image: 'https://via.placeholder.com/195x195?text=Product',
        price: 99.0,
        originalPrice: 150.0
      },
      {
        id: 8,
        name: '玩趣甜心小象震',
        tag: '软萌小狗狗',
        image: 'https://via.placeholder.com/195x195?text=Product',
        price: 89.0,
        originalPrice: 130.0
      }
    ],
    bottomProducts: [
      {
        id: 9,
        category: '女用器具',
        name: '日本NPG 真人倒模大名器 [虞姬]',
        image: 'https://via.placeholder.com/280x280?text=Product',
        price: 134.0,
        originalPrice: 201.0
      },
      {
        id: 10,
        category: '女用器具',
        name: '真实阴道倒模震动器',
        image: 'https://via.placeholder.com/280x280?text=Product',
        price: 134.0,
        originalPrice: 201.0
      },
      {
        id: 11,
        category: '女用器具',
        name: '丝滑少女唇 [虞姬]',
        image: 'https://via.placeholder.com/280x280?text=Product',
        price: 239.0,
        originalPrice: 358.0
      },
      {
        id: 12,
        category: '女用器具',
        name: '粉嫩小玫瑰',
        image: 'https://via.placeholder.com/280x280?text=Product',
        price: 134.0,
        originalPrice: 201.0
      }
    ]
  },
  // ... 其他楼层数据类似，这里简化处理
  {
    bigAd: 'https://via.placeholder.com/800x400/74B9FF/ffffff?text=润滑清洁',
    sideProducts: [
      { id: 13, name: '润滑液', tag: '清爽不黏', image: 'https://via.placeholder.com/195x195', price: 19.9, originalPrice: 39.0 },
      { id: 14, name: '清洁液', tag: '温和抑菌', image: 'https://via.placeholder.com/195x195', price: 29.9, originalPrice: 59.0 }
    ],
    bottomProducts: [
      { id: 15, category: '润滑清洁', name: '水溶性润滑液', image: 'https://via.placeholder.com/280x280', price: 19.9, originalPrice: 39.0 },
      { id: 16, category: '润滑清洁', name: '硅基润滑液', image: 'https://via.placeholder.com/280x280', price: 39.9, originalPrice: 79.0 },
      { id: 17, category: '润滑清洁', name: '女性护理液', image: 'https://via.placeholder.com/280x280', price: 29.9, originalPrice: 59.0 },
      { id: 18, category: '润滑清洁', name: '男性清洁液', image: 'https://via.placeholder.com/280x280', price: 29.9, originalPrice: 59.0 }
    ]
  },
  {
    bigAd: 'https://via.placeholder.com/800x400/FFD93D/ffffff?text=情趣内衣',
    sideProducts: [
      { id: 19, name: '蕾丝连体衣', tag: '性感诱惑', image: 'https://via.placeholder.com/195x195', price: 79.0, originalPrice: 150.0 },
      { id: 20, name: '情趣护士服', tag: '角色扮演', image: 'https://via.placeholder.com/195x195', price: 89.0, originalPrice: 160.0 }
    ],
    bottomProducts: [
      { id: 21, category: '情趣内衣', name: '黑色蕾丝套装', image: 'https://via.placeholder.com/280x280', price: 79.0, originalPrice: 150.0 },
      { id: 22, category: '情趣内衣', name: '红色激情套装', image: 'https://via.placeholder.com/280x280', price: 79.0, originalPrice: 150.0 },
      { id: 23, category: '情趣内衣', name: '学生制服', image: 'https://via.placeholder.com/280x280', price: 89.0, originalPrice: 160.0 },
      { id: 24, category: '情趣内衣', name: 'OL套装', image: 'https://via.placeholder.com/280x280', price: 89.0, originalPrice: 160.0 }
    ]
  },
  {
    bigAd: 'https://via.placeholder.com/800x400/FD79A8/ffffff?text=延时保健',
    sideProducts: [
      { id: 25, name: '延时喷剂', tag: '持久耐用', image: 'https://via.placeholder.com/195x195', price: 59.0, originalPrice: 120.0 },
      { id: 26, name: '保健按摩油', tag: '温和滋养', image: 'https://via.placeholder.com/280x280', price: 49.0, originalPrice: 100.0 }
    ],
    bottomProducts: [
      { id: 27, category: '延时保健', name: '男士延时喷雾', image: 'https://via.placeholder.com/280x280', price: 59.0, originalPrice: 120.0 },
      { id: 28, category: '延时保健', name: '保健精油', image: 'https://via.placeholder.com/280x280', price: 49.0, originalPrice: 100.0 },
      { id: 29, category: '延时保健', name: '延时湿巾', image: 'https://via.placeholder.com/280x280', price: 39.0, originalPrice: 80.0 },
      { id: 30, category: '延时保健', name: '增大精油', image: 'https://via.placeholder.com/280x280', price: 69.0, originalPrice: 140.0 }
    ]
  },
  {
    bigAd: 'https://via.placeholder.com/800x400/55EFC4/ffffff?text=喷剂助情',
    sideProducts: [
      { id: 31, name: '女性助情喷剂', tag: '愉悦体验', image: 'https://via.placeholder.com/195x195', price: 79.0, originalPrice: 150.0 },
      { id: 32, name: '男性助情喷剂', tag: '持久耐力', image: 'https://via.placeholder.com/195x195', price: 79.0, originalPrice: 150.0 }
    ],
    bottomProducts: [
      { id: 33, category: '喷剂助情', name: '女士情欲提升液', image: 'https://via.placeholder.com/280x280', price: 79.0, originalPrice: 150.0 },
      { id: 34, category: '喷剂助情', name: '男士助勃喷雾', image: 'https://via.placeholder.com/280x280', price: 79.0, originalPrice: 150.0 },
      { id: 35, category: '喷剂助情', name: '情趣香水', image: 'https://via.placeholder.com/280x280', price: 59.0, originalPrice: 120.0 },
      { id: 36, category: '喷剂助情', name: '费洛蒙香水', image: 'https://via.placeholder.com/280x280', price: 89.0, originalPrice: 180.0 }
    ]
  },
  {
    bigAd: 'https://via.placeholder.com/800x400/A29BFE/ffffff?text=其他情趣',
    sideProducts: [
      { id: 37, name: 'SM套装', tag: '刺激体验', image: 'https://via.placeholder.com/195x195', price: 129.0, originalPrice: 250.0 },
      { id: 38, name: '情趣道具', tag: '多样玩法', image: 'https://via.placeholder.com/195x195', price: 99.0, originalPrice: 200.0 }
    ],
    bottomProducts: [
      { id: 39, category: '其他情趣', name: 'SM束缚套装', image: 'https://via.placeholder.com/280x280', price: 129.0, originalPrice: 250.0 },
      { id: 40, category: '其他情趣', name: '情趣眼罩手铐', image: 'https://via.placeholder.com/280x280', price: 59.0, originalPrice: 120.0 },
      { id: 41, category: '其他情趣', name: '后庭玩具', image: 'https://via.placeholder.com/280x280', price: 99.0, originalPrice: 200.0 },
      { id: 42, category: '其他情趣', name: '情趣骰子', image: 'https://via.placeholder.com/280x280', price: 29.0, originalPrice: 60.0 }
    ]
  }
])
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  background: #f5f5f5;
}
</style>
