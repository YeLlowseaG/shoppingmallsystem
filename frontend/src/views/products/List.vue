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
          v-for="product in displayProducts"
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
import { getProductPage, type ProductVO } from '@/api/buyer/product'
import { getCategoryTree, type ProductCategoryVO } from '@/api/buyer/productCategory'

const route = useRoute()
const router = useRouter()

// 分类数据
const categoryTree = ref<ProductCategoryVO[]>([])
const categoryMap = ref<Record<number, string>>({})

// 扁平化分类树并生成映射表
const loadCategories = async () => {
  try {
    categoryTree.value = await getCategoryTree()
    // 递归扁平化分类树
    const flattenCategories = (categories: ProductCategoryVO[]) => {
      categories.forEach(category => {
        categoryMap.value[category.id] = category.categoryName
        if (category.children && category.children.length > 0) {
          flattenCategories(category.children)
        }
      })
    }
    flattenCategories(categoryTree.value)
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载状态
const loading = ref(false)

// 商品列表数据
const products = ref<ProductVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

// 转换商品数据格式给 ProductCard 使用
const displayProducts = computed(() => {
  return products.value.map(product => ({
    id: product.id,
    name: product.productName,
    image: product.mainImage,
    price: product.basePrice,
    originalPrice: product.basePrice * 1.5, // 原价设置为基础价的1.5倍
    sales: product.salesCount,
    category: product.categoryName,
    tags: '', // 暂不使用标签
    brand: '' // 暂不使用品牌
  }))
})

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
    const categoryName = categoryMap.value[categoryId]
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
    // 调用后端 API
    const categoryId = filters.value.categoryId ? Number(filters.value.categoryId) : undefined
    const keyword = filters.value.keyword || undefined
    const brand = filters.value.brand || undefined

    const response = await getProductPage(
      currentPage.value,
      pageSize.value,
      categoryId,
      keyword,
      brand
    )

    products.value = response.records
    total.value = response.total
  } catch (error) {
    console.error('加载商品失败:', error)
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
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
onMounted(async () => {
  await loadCategories()
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
