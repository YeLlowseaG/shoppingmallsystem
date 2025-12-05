<template>
  <div class="product-list-page">
    <!-- 复用首页头部组件 -->
    <TopBar />
    <Header />
    <Navbar />

    <div class="container">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator=">" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 筛选工具栏 -->
      <div class="filter-bar">
        <div class="sort-options">
          <span
            v-for="option in sortOptions"
            :key="option.value"
            :class="['sort-item', { active: currentSort === option.value }]"
            @click="handleSortChange(option.value)"
          >
            {{ option.label }}
            <template v-if="option.value === 'price_asc'">↑</template>
            <template v-if="option.value === 'price_desc'">↓</template>
          </span>
        </div>
        <div class="view-mode">
          <el-icon
            :class="['mode-icon', { active: viewMode === 'grid' }]"
            @click="viewMode = 'grid'"
          >
            <Grid />
          </el-icon>
          <el-icon
            :class="['mode-icon', { active: viewMode === 'list' }]"
            @click="viewMode = 'list'"
          >
            <List />
          </el-icon>
        </div>
      </div>

      <!-- 商品展示区域 -->
      <div v-if="loading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>
      <div v-else-if="products.length === 0" class="empty">
        <el-empty description="暂无商品" />
      </div>
      <div v-else :class="['product-display', viewMode]">
        <ProductCard
          v-for="product in products"
          :key="product.id"
          :product="product"
          :view-mode="viewMode"
        />
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Grid, List, Loading } from '@element-plus/icons-vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import ProductCard from '@/components/products/ProductCard.vue'

const route = useRoute()
const router = useRouter()

// 分类映射表（用于面包屑导航显示分类名称）
const categoryMap: Record<number, string> = {
  // 一级分类
  1: '男用器具',
  2: '女用器具',
  3: '避孕润滑',
  4: '情趣内衣',
  5: '护理保健',
  6: '喷剂助情',
  7: '其他情趣',
  // 二级分类
  11: '女用器具',
  12: '女用器具',
  21: '震动跳蛋',
  31: '润滑液',
  41: '连体衣',
  51: '保健食品',
  61: '男用喷剂',
  71: 'SM系列',
  // 三级分类
  111: '飞机杯',
  112: '助勃锻炼',
  113: '充气娃娃',
  121: '震动棒',
  122: '跳蛋',
  123: '潮吹AV棒',
  211: '情趣跳蛋',
  212: '震动棒',
  213: '潮吹AV棒',
  311: '润滑液',
  312: '安全套',
  313: '排卵测孕',
  314: '口杯液',
  411: '连体衣',
  412: '激情T裤',
  413: '三点式',
  511: '保健食品',
  512: '清洗抑菌',
  513: '按摩精油',
  611: '男用喷剂',
  612: '情欲提升',
  613: '香水诱惑',
  711: 'SM系列',
  712: '情趣跳逗',
  713: '后庭刺激'
}

// 加载状态
const loading = ref(false)

// 商品列表数据
const products = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

// 显示模式
const viewMode = ref<'grid' | 'list'>('grid')

// 排序选项
const sortOptions = [
  { label: '综合', value: 'default' },
  { label: '价格', value: 'price_asc' },
  { label: '价格', value: 'price_desc' },
  { label: '销量', value: 'sales' },
  { label: '最新', value: 'newest' }
]

const currentSort = ref('default')

// 筛选条件
const filters = computed(() => ({
  keyword: route.query.keyword as string || '',
  categoryId: route.query.categoryId as string || '',
  brand: route.query.brand as string || '',
  type: route.query.type as string || '',
  minPrice: route.query.minPrice as string || '',
  maxPrice: route.query.maxPrice as string || '',
  promotionId: route.query.promotionId as string || ''
}))

// 动态页面标题
const pageTitle = computed(() => {
  if (filters.value.keyword) {
    return `搜索 "${filters.value.keyword}"`
  }
  if (filters.value.type === 'new') {
    return '新品专区'
  }
  if (filters.value.type === 'special') {
    return '特惠区'
  }
  if (filters.value.type === 'hot') {
    return '热销专区'
  }
  if (filters.value.categoryId) {
    const categoryId = Number(filters.value.categoryId)
    const categoryName = categoryMap[categoryId]
    return categoryName || '商品分类'
  }
  if (filters.value.brand) {
    return `品牌: ${filters.value.brand}`
  }
  if (filters.value.promotionId) {
    return '促销活动'
  }
  return '商品列表'
})

// 排序切换
const handleSortChange = (sortValue: string) => {
  currentSort.value = sortValue
  currentPage.value = 1
  loadProducts()
}

// 分页切换
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadProducts()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 每页数量切换
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadProducts()
}

// 加载商品数据
const loadProducts = async () => {
  loading.value = true

  try {
    // TODO: 调用后端 API
    // const response = await productApi.getList({
    //   ...filters.value,
    //   sort: currentSort.value,
    //   page: currentPage.value,
    //   pageSize: pageSize.value
    // })

    // 临时使用模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))

    const mockProducts = generateMockProducts()
    products.value = mockProducts
    total.value = 48 // 模拟总数
  } catch (error) {
    console.error('加载商品失败:', error)
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 生成模拟数据
const generateMockProducts = () => {
  const mockData = []
  const categories = ['男用器具', '女用器具', '润滑清洁', '情趣内衣', '延时保健', '喷剂助情', '其他情趣']
  const brands = ['虞姬', 'NPG', 'TENGA', '网易严选', '杜蕾斯', '冈本']

  for (let i = 1; i <= pageSize.value; i++) {
    const id = (currentPage.value - 1) * pageSize.value + i
    mockData.push({
      id,
      name: `精选商品 ${id} - ${categories[Math.floor(Math.random() * categories.length)]}`,
      category: categories[Math.floor(Math.random() * categories.length)],
      brand: brands[Math.floor(Math.random() * brands.length)],
      image: `https://via.placeholder.com/280x280?text=Product+${id}`,
      price: (Math.random() * 200 + 50).toFixed(2),
      originalPrice: (Math.random() * 300 + 100).toFixed(2),
      sales: Math.floor(Math.random() * 10000),
      tags: ['热销', '新品', '特惠'][Math.floor(Math.random() * 3)],
      rating: (Math.random() * 2 + 3).toFixed(1)
    })
  }

  return mockData
}

// 监听路由参数变化
watch(
  () => route.query,
  () => {
    currentPage.value = 1
    loadProducts()
  },
  { deep: true }
)

// 组件挂载时加载数据
onMounted(() => {
  loadProducts()
})
</script>

<style scoped lang="scss">
.product-list-page {
  min-height: 100vh;
  background: #f5f5f5;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px 15px;
    background: #fff;
    min-height: 600px;
  }

  .breadcrumb {
    padding: 15px 0;
    border-bottom: 1px solid #eee;
    margin-bottom: 20px;
  }

  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 0;
    border-bottom: 2px solid #f5f5f5;
    margin-bottom: 20px;

    .sort-options {
      display: flex;
      gap: 20px;

      .sort-item {
        padding: 5px 12px;
        cursor: pointer;
        color: #666;
        font-size: 14px;
        transition: all 0.3s;

        &:hover {
          color: #e4393c;
        }

        &.active {
          color: #e4393c;
          font-weight: bold;
        }
      }
    }

    .view-mode {
      display: flex;
      gap: 10px;

      .mode-icon {
        font-size: 20px;
        color: #999;
        cursor: pointer;
        transition: color 0.3s;

        &:hover {
          color: #e4393c;
        }

        &.active {
          color: #e4393c;
        }
      }
    }
  }

  .loading {
    text-align: center;
    padding: 100px 0;
    color: #999;

    .el-icon {
      font-size: 40px;
      margin-bottom: 10px;
    }
  }

  .empty {
    padding: 100px 0;
  }

  .product-display {
    &.grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20px;
    }

    &.list {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }
  }

  .pagination {
    margin-top: 40px;
    display: flex;
    justify-content: center;
  }
}
</style>
