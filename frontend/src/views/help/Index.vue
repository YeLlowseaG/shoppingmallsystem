<template>
  <div class="help-center-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 帮助中心内容区域 -->
    <div class="help-content">
      <div class="container">
        <!-- 面包屑导航 -->
        <div class="breadcrumb">
          <span>您当前的位置：</span>
          <router-link to="/">首页</router-link>
          <span class="separator">»</span>
          <span class="current">帮助中心</span>
          <span v-if="currentCategory" class="separator">»</span>
          <span v-if="currentCategory" class="current">{{ currentCategory }}</span>
          <span v-if="currentArticle" class="separator">»</span>
          <span v-if="currentArticle" class="current">{{ currentArticle }}</span>
        </div>

        <!-- 主体内容 -->
        <div class="help-main">
          <!-- 左侧导航栏 -->
          <div class="help-sidebar">
            <div class="sidebar-title">帮助中心</div>
            <div class="sidebar-menu">
              <div
                v-for="category in helpCategories"
                :key="category.id"
                class="category-group"
              >
                <div
                  class="category-title"
                  :class="{ active: activeCategoryId === category.id }"
                  @click="handleCategoryClick(category)"
                >
                  {{ category.name }}
                </div>
                <div
                  v-if="category.children && category.children.length > 0"
                  class="sub-categories"
                  :class="{ expanded: expandedCategories.includes(category.id) }"
                >
                  <div
                    v-for="subCategory in category.children"
                    :key="subCategory.id"
                    class="sub-category-wrapper"
                  >
                    <div
                      class="sub-category-item"
                      :class="{ active: activeCategoryId === subCategory.id }"
                      @click="handleSubCategoryClick(subCategory, category)"
                    >
                      {{ subCategory.name }}
                    </div>
                    <!-- 显示该子分类下的文章列表 -->
                    <div
                      v-if="categoryArticlesMap.has(subCategory.id) && categoryArticlesMap.get(subCategory.id)!.length > 0"
                      class="article-items"
                    >
                      <div
                        v-for="article in categoryArticlesMap.get(subCategory.id)"
                        :key="article.id"
                        class="article-item-sidebar"
                        :class="{ active: activeArticleId === article.id }"
                        @click="handleArticleClickFromSidebar(article, subCategory, category)"
                      >
                        {{ article.title }}
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 如果该分类没有子分类，直接显示该分类下的文章 -->
                <div
                  v-else-if="categoryArticlesMap.has(category.id) && categoryArticlesMap.get(category.id)!.length > 0"
                  class="article-items"
                  :class="{ expanded: expandedCategories.includes(category.id) }"
                >
                  <div
                    v-for="article in categoryArticlesMap.get(category.id)"
                    :key="article.id"
                    class="article-item-sidebar"
                    :class="{ active: activeArticleId === article.id }"
                    @click="handleArticleClickFromSidebar(article, category, null)"
                  >
                    {{ article.title }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧主内容区 -->
          <div class="help-content-area">
            <div v-if="loading" class="loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
            <!-- 文章列表 -->
            <div v-else-if="articleList.length > 0 && !currentArticleData" class="article-list">
              <div
                v-for="article in articleList"
                :key="article.id"
                class="article-item"
                :class="{ active: activeArticleId === article.id }"
                @click="handleArticleClick(article)"
              >
                <div class="article-item-title">{{ article.title }}</div>
              </div>
            </div>
            <!-- 文章详情 -->
            <div v-else-if="currentArticleData" class="article-content">
              <div class="article-header">
                <h1 class="article-title">{{ currentArticleData.title }}</h1>
                <el-button 
                  v-if="articleList.length > 0" 
                  type="text" 
                  class="back-to-list-btn"
                  @click="backToList"
                >
                  <el-icon><ArrowLeft /></el-icon>
                  返回列表
                </el-button>
              </div>
              <div class="article-body">
                <!-- 图片展示 -->
                <div v-if="currentArticleData.images && currentArticleData.images.length > 0" class="article-images">
                  <img
                    v-for="(image, index) in currentArticleData.images"
                    :key="index"
                    :src="image"
                    :alt="`${currentArticleData.title} - 图片${index + 1}`"
                    class="article-image"
                  />
                </div>
                <!-- 文字内容 -->
                <div
                  class="article-text"
                  v-html="currentArticleData.content"
                ></div>
              </div>
            </div>
            <div v-else class="empty-content">
              <el-empty description="请从左侧选择帮助内容" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 页尾 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loading, ArrowLeft } from '@element-plus/icons-vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import { getHelpCategories, getHelpArticleById, getHelpArticlesByCategory, type HelpCategory, type HelpArticle } from '@/api/common/help'

const route = useRoute()
const router = useRouter()

const helpCategories = ref<HelpCategory[]>([])
const activeCategoryId = ref<number | null>(null)
const activeArticleId = ref<number | null>(null)
const expandedCategories = ref<number[]>([])
const currentArticleData = ref<HelpArticle | null>(null)
const articleList = ref<HelpArticle[]>([]) // 当前分类下的文章列表
const categoryArticlesMap = ref<Map<number, HelpArticle[]>>(new Map()) // 存储每个分类的文章列表
const loading = ref(false)

const currentCategory = computed(() => {
  if (!activeCategoryId.value) return ''
  const category = findCategoryById(activeCategoryId.value)
  return category?.name || ''
})

const currentArticle = computed(() => {
  if (!activeArticleId.value) return ''
  return currentArticleData.value?.title || ''
})

// 查找分类
const findCategoryById = (id: number): HelpCategory | null => {
  for (const category of helpCategories.value) {
    if (category.id === id) return category
    if (category.children) {
      for (const subCategory of category.children) {
        if (subCategory.id === id) return subCategory
      }
    }
  }
  return null
}

// 处理分类点击（父分类）
const handleCategoryClick = async (category: HelpCategory) => {
  if (expandedCategories.value.includes(category.id)) {
    expandedCategories.value = expandedCategories.value.filter(id => id !== category.id)
  } else {
    expandedCategories.value.push(category.id)
  }
  activeCategoryId.value = category.id
  
  // 如果该分类没有子分类，则直接加载该分类下的文章列表
  if (!category.children || category.children.length === 0) {
    await loadCategoryArticles(category.id)
    // 如果有文章，默认加载第一篇文章
    const articles = categoryArticlesMap.value.get(category.id)
    if (articles && articles.length > 0) {
      await handleArticleClick(articles[0])
    }
  } else {
    // 如果有子分类，加载所有子分类的文章列表
    for (const subCategory of category.children) {
      await loadCategoryArticles(subCategory.id)
    }
    // 默认加载第一个子分类的第一篇文章
    if (category.children.length > 0) {
      const firstSubCategory = category.children[0]
      const articles = categoryArticlesMap.value.get(firstSubCategory.id)
      if (articles && articles.length > 0) {
        await handleSubCategoryClick(firstSubCategory, category)
      }
    }
  }
}

// 加载分类的文章列表（用于左侧栏显示）
const loadCategoryArticles = async (categoryId: number) => {
  // 如果已经加载过，直接返回
  if (categoryArticlesMap.value.has(categoryId)) {
    return
  }
  
  try {
    const articles = await getHelpArticlesByCategory(categoryId)
    categoryArticlesMap.value.set(categoryId, articles)
  } catch (error) {
    console.error('获取分类文章列表失败:', error)
    categoryArticlesMap.value.set(categoryId, [])
  }
}

// 处理子分类点击
const handleSubCategoryClick = async (subCategory: HelpCategory, parentCategory: HelpCategory) => {
  activeCategoryId.value = subCategory.id
  activeArticleId.value = null
  currentArticleData.value = null
  
  // 确保父分类展开
  if (!expandedCategories.value.includes(parentCategory.id)) {
    expandedCategories.value.push(parentCategory.id)
  }
  
  // 加载该子分类下的文章列表（用于右侧显示）
  await loadArticlesByCategory(subCategory.id)
  
  // 如果还没有加载过该子分类的文章（用于左侧栏显示），则加载
  if (!categoryArticlesMap.value.has(subCategory.id)) {
    await loadCategoryArticles(subCategory.id)
  }
  
  // 如果有文章，默认加载第一篇文章
  const articles = categoryArticlesMap.value.get(subCategory.id)
  if (articles && articles.length > 0) {
    await handleArticleClick(articles[0])
  }
}

// 从左侧栏点击文章
const handleArticleClickFromSidebar = async (article: HelpArticle, category: HelpCategory, parentCategory: HelpCategory | null) => {
  // 确保分类展开
  if (parentCategory && !expandedCategories.value.includes(parentCategory.id)) {
    expandedCategories.value.push(parentCategory.id)
  }
  
  // 设置活动分类
  activeCategoryId.value = category.id
  
  // 加载文章详情
  await handleArticleClick(article)
}

// 根据分类ID加载文章列表
const loadArticlesByCategory = async (categoryId: number, autoLoadFirst = true) => {
  loading.value = true
  try {
    articleList.value = await getHelpArticlesByCategory(categoryId)
    
    // 如果有文章且需要自动加载第一篇文章
    if (articleList.value.length > 0 && autoLoadFirst) {
      await handleArticleClick(articleList.value[0])
    } else {
      currentArticleData.value = null
    }
  } catch (error) {
    console.error('获取帮助文章列表失败:', error)
    articleList.value = []
    currentArticleData.value = null
  } finally {
    loading.value = false
  }
}

// 返回文章列表
const backToList = () => {
  currentArticleData.value = null
  activeArticleId.value = null
}

// 处理文章点击
const handleArticleClick = async (article: HelpArticle) => {
  activeArticleId.value = article.id
  loading.value = true
  
  try {
    // 获取文章内容
    const articleData = await getHelpArticleById(article.id)
    currentArticleData.value = articleData
    
    // 更新URL
    router.push({
      path: '/help',
      query: { articleId: article.id }
    })
  } catch (error) {
    console.error('获取帮助文章失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化数据
onMounted(async () => {
  try {
    // 加载分类列表
    helpCategories.value = await getHelpCategories()
    
    // 默认展开所有分类
    for (const category of helpCategories.value) {
      expandedCategories.value.push(category.id)
      
      // 加载所有分类的文章列表（用于左侧栏显示）
      if (category.children && category.children.length > 0) {
        // 如果有子分类，加载所有子分类的文章
        for (const subCategory of category.children) {
          await loadCategoryArticles(subCategory.id)
        }
      } else {
        // 如果没有子分类，加载该分类的文章
        await loadCategoryArticles(category.id)
      }
    }
    
    // 如果URL中有articleId参数，加载对应文章
    const articleId = route.query.articleId
    if (articleId) {
      const id = Number(articleId)
      if (!isNaN(id)) {
        // 直接加载文章详情
        loading.value = true
        try {
          const articleData = await getHelpArticleById(id)
          currentArticleData.value = articleData
          activeArticleId.value = id
          
          // 找到文章所属的分类并展开
          let found = false
          for (const category of helpCategories.value) {
            // 先检查是否是父分类本身
            if (category.id === articleData.categoryId) {
              activeCategoryId.value = category.id
              // 加载该分类下的文章列表（用于右侧显示）
              articleList.value = await getHelpArticlesByCategory(category.id)
              found = true
              break
            }
            // 再检查是否是子分类
            if (category.children) {
              for (const subCategory of category.children) {
                if (subCategory.id === articleData.categoryId) {
                  activeCategoryId.value = subCategory.id
                  // 加载该分类下的文章列表（用于右侧显示）
                  articleList.value = await getHelpArticlesByCategory(subCategory.id)
                  found = true
                  break
                }
              }
              if (found) break
            }
          }
        } catch (error) {
          console.error('加载文章失败:', error)
        } finally {
          loading.value = false
        }
      }
    } else {
      // 默认加载第一个分类的第一篇文章
      if (helpCategories.value.length > 0) {
        const firstCategory = helpCategories.value[0]
        // 如果有子分类，加载第一个子分类的第一篇文章
        if (firstCategory.children && firstCategory.children.length > 0) {
          const firstSubCategory = firstCategory.children[0]
          const articles = categoryArticlesMap.value.get(firstSubCategory.id)
          if (articles && articles.length > 0) {
            await handleSubCategoryClick(firstSubCategory, firstCategory)
          }
        } else {
          // 如果没有子分类，加载该分类的第一篇文章
          const articles = categoryArticlesMap.value.get(firstCategory.id)
          if (articles && articles.length > 0) {
            activeCategoryId.value = firstCategory.id
            await handleArticleClick(articles[0])
          }
        }
      }
    }
  } catch (error) {
    console.error('加载帮助中心数据失败:', error)
  }
})
</script>

<style scoped lang="scss">
.help-center-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.help-content {
  background: #fff;
  padding: 20px 0 40px;
  min-height: 600px;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }

  .breadcrumb {
    padding: 15px 0;
    font-size: 12px;
    color: #666;
    border-bottom: 1px solid #eee;
    margin-bottom: 20px;

    a {
      color: #666;
      text-decoration: none;
      transition: color 0.3s;

      &:hover {
        color: #e4393c;
      }
    }

    .separator {
      margin: 0 8px;
      color: #999;
    }

    .current {
      color: #e4393c;
    }
  }

  .help-main {
    display: flex;
    gap: 20px;
    align-items: flex-start;
  }

  .help-sidebar {
    width: 220px;
    flex-shrink: 0;
    background: #fff;
    border: 1px solid #eee;

    .sidebar-title {
      background: #e4393c;
      color: #fff;
      padding: 15px 20px;
      font-size: 16px;
      font-weight: bold;
      text-align: center;
    }

    .sidebar-menu {
      .category-group {
        border-bottom: 1px solid #eee;

        &:last-child {
          border-bottom: none;
        }

        .category-title {
          padding: 12px 20px;
          font-size: 14px;
          font-weight: 500;
          color: #333;
          cursor: pointer;
          transition: all 0.3s;
          position: relative;

          &:hover {
            background: #f5f5f5;
            color: #e4393c;
          }

          &.active {
            background: #fff5f5;
            color: #e4393c;
            font-weight: bold;
          }

          &::after {
            content: '';
            position: absolute;
            right: 15px;
            top: 50%;
            transform: translateY(-50%);
            width: 0;
            height: 0;
            border-left: 5px solid transparent;
            border-right: 5px solid transparent;
            border-top: 5px solid #999;
            transition: transform 0.3s;
          }
        }

        .sub-categories {
          display: none;
          background: #fafafa;

          &.expanded {
            display: block;
          }

          .sub-category-item {
            padding: 10px 20px 10px 40px;
            font-size: 13px;
            color: #666;
            cursor: pointer;
            transition: all 0.3s;
            border-left: 3px solid transparent;

            &:hover {
              background: #f0f0f0;
              color: #e4393c;
            }

            &.active {
              background: #fff5f5;
              color: #e4393c;
              font-weight: bold;
              border-left-color: #e4393c;
            }
          }

          .sub-category-wrapper {
            .article-items {
              background: #f9f9f9;
              
              .article-item-sidebar {
                padding: 8px 20px 8px 60px;
                font-size: 12px;
                color: #666;
                cursor: pointer;
                transition: all 0.3s;
                border-left: 3px solid transparent;

                &:hover {
                  background: #f0f0f0;
                  color: #e4393c;
                }

                &.active {
                  background: #fff5f5;
                  color: #e4393c;
                  font-weight: bold;
                  border-left-color: #e4393c;
                }
              }
            }
          }
        }

        // 父分类直接显示文章的情况
        .article-items {
          display: none;
          background: #fafafa;

          &.expanded {
            display: block;
          }

          .article-item-sidebar {
            padding: 10px 20px 10px 40px;
            font-size: 13px;
            color: #666;
            cursor: pointer;
            transition: all 0.3s;
            border-left: 3px solid transparent;

            &:hover {
              background: #f0f0f0;
              color: #e4393c;
            }

            &.active {
              background: #fff5f5;
              color: #e4393c;
              font-weight: bold;
              border-left-color: #e4393c;
            }
          }
        }
      }
    }
  }

  .help-content-area {
    flex: 1;
    background: #fff;
    padding: 30px;
    min-height: 500px;
    border: 1px solid #eee;

    .loading {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      padding: 100px 0;
      color: #999;
      font-size: 14px;
    }

    .empty-content {
      padding: 100px 0;
    }

    .article-list {
      .article-item {
        padding: 15px 20px;
        border-bottom: 1px solid #eee;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background: #f5f5f5;
        }

        &.active {
          background: #fff5f5;
          border-left: 3px solid #e4393c;
          padding-left: 17px;
        }

        .article-item-title {
          font-size: 14px;
          color: #333;
          font-weight: 500;

          &:hover {
            color: #e4393c;
          }
        }

        &.active .article-item-title {
          color: #e4393c;
          font-weight: bold;
        }
      }
    }

      .article-content {
      .article-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 25px;
        padding-bottom: 15px;
        border-bottom: 2px solid #e4393c;

        .article-title {
          font-size: 24px;
          font-weight: bold;
          color: #333;
          margin: 0;
          padding: 0;
          border: none;
        }

        .back-to-list-btn {
          color: #666;
          font-size: 14px;

          &:hover {
            color: #e4393c;
          }
        }
      }

      .article-title {
        font-size: 24px;
        font-weight: bold;
        color: #333;
        margin-bottom: 25px;
        padding-bottom: 15px;
        border-bottom: 2px solid #e4393c;
      }

      .article-body {
        .article-images {
          margin-bottom: 25px;

          .article-image {
            max-width: 100%;
            height: auto;
            margin-bottom: 15px;
            border: 1px solid #eee;
            border-radius: 4px;

            &:last-child {
              margin-bottom: 0;
            }
          }
        }

        .article-text {
          font-size: 14px;
          line-height: 1.8;
          color: #666;

          :deep(p) {
            margin-bottom: 15px;
          }

          :deep(ul),
          :deep(ol) {
            margin-bottom: 15px;
            padding-left: 30px;

            li {
              margin-bottom: 8px;
            }
          }

          :deep(h2),
          :deep(h3) {
            font-size: 18px;
            font-weight: bold;
            color: #333;
            margin: 20px 0 15px;
          }

          :deep(img) {
            max-width: 100%;
            height: auto;
            margin: 15px 0;
            border: 1px solid #eee;
            border-radius: 4px;
          }
        }
      }
    }
  }
}
</style>

