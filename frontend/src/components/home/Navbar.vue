<template>
  <div class="navbar">
    <div class="container">
      <!-- 全部分类按钮 -->
      <div
        class="all-categories"
        @mouseenter="showCategories = true"
        @mouseleave="showCategories = false"
      >
        <el-icon class="menu-icon"><Menu /></el-icon>
        <span>全部分类与品牌</span>

        <!-- 分类下拉菜单 -->
        <transition name="slide">
          <div v-show="showCategories" class="categories-dropdown">
            <!-- 加载状态 -->
            <div v-if="loadingCategories" class="loading-state">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
            <!-- 空状态 -->
            <div v-else-if="!loadingCategories && categories.length === 0" class="empty-state">
              <span>暂无分类数据</span>
            </div>
            <!-- 分类列表 -->
            <div
              v-for="category in categories"
              v-else
              :key="category.id"
              class="category-item"
              @mouseenter="handleCategoryHover(category)"
            >
              <div class="category-title" @click="goToCategory(category.id)">
                {{ category.categoryName }}
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
              <div class="sub-items">
                <span
                  v-for="sub in category.children?.slice(0, 3)"
                  :key="sub.id"
                  class="sub-name"
                  @click.stop="goToCategory(sub.id)"
                >
                  {{ sub.categoryName }}
                </span>
              </div>

              <!-- 二级和三级分类悬浮展示 -->
              <div
                v-show="hoveredCategory?.id === category.id && category.children && category.children.length > 0"
                class="sub-categories"
                @mouseenter="handleCategoryHover(category)"
                @mouseleave="hoveredCategory = null"
              >
                <div
                  v-for="subCategory in category.children"
                  :key="subCategory.id"
                  class="sub-category-group"
                >
                  <!-- 二级分类标题（如果有三级分类则显示标题，否则直接可点击） -->
                  <div 
                    v-if="subCategory.children && subCategory.children.length > 0"
                    class="sub-category-title"
                    @click="goToCategory(subCategory.id)"
                  >
                    {{ subCategory.categoryName }}
                  </div>
                  <!-- 如果二级分类没有子分类，则直接显示为可点击项 -->
                  <router-link
                    v-else
                    :to="`/products?categoryId=${subCategory.id}`"
                    class="sub-category-title-link"
                  >
                    {{ subCategory.categoryName }}
                  </router-link>
                  <!-- 三级分类列表 -->
                  <div 
                    v-if="subCategory.children && subCategory.children.length > 0"
                    class="third-level"
                  >
                    <router-link
                      v-for="third in subCategory.children"
                      :key="third.id"
                      :to="`/products?categoryId=${third.id}`"
                      class="third-item"
                    >
                      {{ third.categoryName }}
                    </router-link>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- 主导航 -->
      <nav class="main-nav">
        <router-link 
          v-for="menu in navigationMenus" 
          :key="menu.id"
          :to="generateMenuUrl(menu)" 
          :target="menu.target"
          class="nav-item"
          :class="{ active: isActiveMenu(menu) }"
        >
          {{ menu.menuName }}
        </router-link>
      </nav>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Menu, ArrowRight, Loading } from '@element-plus/icons-vue'
import { getNavigationMenus, type NavigationMenu } from '@/api/buyer/navigationMenu'
import { getCategoryTree, type ProductCategoryVO } from '@/api/buyer/productCategory'
import { getActiveBrands, type Brand } from '@/api/buyer/website'

const router = useRouter()
const route = useRoute()
const showCategories = ref(false)
const hoveredCategory = ref<ProductCategoryVO | null>(null)

// 导航菜单
const navigationMenus = ref<NavigationMenu[]>([])

// 商品分类数据
const categories = ref<ProductCategoryVO[]>([])
const loadingCategories = ref(false)

// 品牌列表（用于将品牌ID转换为品牌名称）
const brands = ref<Brand[]>([])

const handleCategoryHover = (category: ProductCategoryVO) => {
  hoveredCategory.value = category
}

// 加载商品分类数据
const loadCategories = async () => {
  loadingCategories.value = true
  try {
    const categoryTree = await getCategoryTree()
    // 只显示一级分类（level=1）作为主分类
    categories.value = categoryTree.filter(category => category.level === 1)
  } catch (error) {
    console.error('加载商品分类失败:', error)
    // 失败时使用空数组，不显示分类菜单
    categories.value = []
  } finally {
    loadingCategories.value = false
  }
}

// 加载导航菜单
const loadNavigationMenus = async () => {
  try {
    navigationMenus.value = await getNavigationMenus()
  } catch (error) {
    console.error('加载导航菜单失败:', error)
    // 使用默认菜单作为兜底
    navigationMenus.value = [
      { id: 1, menuName: '首页', menuUrl: '/', menuType: 'link', sortOrder: 1, status: 1, target: '_self' },
      { id: 2, menuName: '新品专区', menuUrl: '/products', menuType: 'type', menuParams: '{"type": "new"}', sortOrder: 2, status: 1, target: '_self' }
    ]
  }
}

// 生成菜单URL
const generateMenuUrl = (menu: NavigationMenu) => {
  if (menu.menuType === 'link') {
    return menu.menuUrl
  }
  
  // 解析菜单参数
  let params = {}
  try {
    params = menu.menuParams ? JSON.parse(menu.menuParams) : {}
    
    // 如果是品牌类型，确保使用品牌名称而不是ID
    if (menu.menuType === 'brand' && params.brand) {
      const brandValue = params.brand
      // 如果brand是数字（旧数据可能是ID），需要转换为品牌名称
      if (!isNaN(Number(brandValue)) && Number(brandValue).toString() === brandValue) {
        const brandId = Number(brandValue)
        const brand = brands.value.find(b => b.id === brandId)
        if (brand) {
          params.brand = brand.brandName
        } else {
          console.warn('未找到品牌ID对应的品牌名称:', brandId)
        }
      }
    }
  } catch (error) {
    console.error('解析菜单参数失败:', error)
  }
  
  return {
    path: menu.menuUrl,
    query: params
  }
}

// 判断菜单是否激活
const isActiveMenu = (menu: NavigationMenu) => {
  if (menu.menuType === 'link') {
    return route.path === menu.menuUrl
  }
  
  // 对于带参数的菜单，检查路径和参数是否匹配
  if (route.path === menu.menuUrl && menu.menuParams) {
    try {
      const params = JSON.parse(menu.menuParams)
      return Object.keys(params).every(key => route.query[key] === params[key])
    } catch (error) {
      return false
    }
  }
  
  return false
}

// 跳转到分类列表页
const goToCategory = (categoryId: number) => {
  showCategories.value = false
  router.push({
    path: '/products',
    query: { categoryId: String(categoryId) }
  })
}

// 加载品牌列表（用于将品牌ID转换为品牌名称）
const loadBrands = async () => {
  try {
    brands.value = await getActiveBrands()
  } catch (error) {
    console.error('加载品牌列表失败:', error)
  }
}

// 组件挂载时加载导航菜单、商品分类和品牌列表
onMounted(() => {
  loadNavigationMenus()
  loadCategories()
  loadBrands() // 加载品牌列表，用于处理旧数据（品牌ID）
})
</script>

<style scoped lang="scss">
.navbar {
  background: #fff;
  height: 50px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
    display: flex;
    height: 100%;
  }

  .all-categories {
    position: relative;
    width: 220px;
    background: #e4393c;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-size: 14px;
    font-weight: bold;
    cursor: pointer;

    .menu-icon {
      font-size: 18px;
    }

      .categories-dropdown {
      position: absolute;
      top: 50px;
      left: 0;
      width: 220px;
      background: rgba(0, 0, 0, 0.8);
      color: #fff;
      z-index: 1000;
      overflow: visible;

      .loading-state,
      .empty-state {
        padding: 20px;
        text-align: center;
        color: rgba(255, 255, 255, 0.7);
        font-size: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
      }

      .category-item {
        position: relative;
        padding: 12px 15px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        cursor: pointer;
        transition: background 0.3s;
        z-index: 1;

        &:hover {
          background: rgba(255, 255, 255, 0.1);
          z-index: 1002;
        }

        .category-title {
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-size: 14px;
          font-weight: bold;
          margin-bottom: 5px;

          .arrow {
            font-size: 12px;
          }
        }

        .sub-items {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;

          .sub-name {
            font-size: 12px;
            color: rgba(255, 255, 255, 0.7);
            cursor: pointer;
            transition: color 0.3s;

            &:hover {
              color: rgba(255, 255, 255, 1);
            }
          }
        }

        .sub-categories {
          position: absolute;
          left: 100%;
          top: 0;
          margin-left: 0;
          width: 600px;
          min-height: 100%;
          background: #fff;
          color: #333;
          border: 1px solid #eee;
          box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.1);
          padding: 20px;
          overflow: visible;
          z-index: 1001;
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 20px 40px;
          align-content: start;

          .sub-category-group {
            margin-bottom: 10px;

            .sub-category-title {
              font-size: 14px;
              font-weight: bold;
              color: #333;
              margin-bottom: 10px;
              cursor: pointer;
              transition: color 0.3s;
              padding-bottom: 8px;
              border-bottom: 1px solid #f0f0f0;

              &:hover {
                color: #e4393c;
              }
            }

            .sub-category-title-link {
              font-size: 14px;
              font-weight: bold;
              color: #333;
              margin-bottom: 10px;
              text-decoration: none;
              display: block;
              transition: color 0.3s;
              padding-bottom: 8px;
              border-bottom: 1px solid #f0f0f0;

              &:hover {
                color: #e4393c;
              }
            }

            .third-level {
              display: flex;
              flex-direction: column;
              gap: 8px;

              .third-item {
                font-size: 13px;
                color: #666;
                text-decoration: none;
                transition: color 0.3s;
                padding: 2px 0;

                &:hover {
                  color: #e4393c;
                }
              }
            }
          }
        }
      }
    }
  }

  .main-nav {
    flex: 1;
    display: flex;
    align-items: center;
    padding-left: 40px;
    gap: 30px;

    .nav-item {
      font-size: 14px;
      color: #333;
      text-decoration: none;
      font-weight: 500;
      transition: color 0.3s;

      &:hover,
      &.active {
        color: #e4393c;
      }

      &.active {
        font-weight: bold;
      }
    }
  }
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
