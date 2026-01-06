<template>
  <footer class="footer">
    <!-- 服务保障 - 已屏蔽 -->
    <!-- <div class="service-bar">
      <div class="container">
        <div class="service-item">
          <div class="icon-circle">正</div>
          <span>正品货源</span>
        </div>
        <div class="service-item">
          <span class="icon-text">✈</span>
          <span>海外直邮</span>
        </div>
        <div class="service-item">
          <span class="icon-text">👤</span>
          <span>本土客服</span>
        </div>
        <div class="service-item">
          <span class="icon-text">💰</span>
          <span>全球同价</span>
        </div>
        <el-button type="danger" class="contact-btn">查看详情</el-button>
      </div>
    </div> -->

    <!-- 底部信息 -->
    <div class="footer-info">
      <div class="container">
        <div class="footer-content">
          <!-- 动态加载的帮助中心分类 -->
          <div class="links">
            <div 
              v-for="category in footerCategories" 
              :key="category.id"
              class="link-group"
            >
              <h4>{{ category.name }}</h4>
              <a 
                v-for="article in category.articles" 
                :key="article.id"
                href="#"
                @click.prevent="goToArticle(article.id)"
              >
                {{ article.title }}
              </a>
            </div>
            
            <!-- 备案号（静态内容） -->
            <div class="link-group">
              <h4>备案号</h4>
              <a href="#">粤ICP备11098444号</a>
              <a href="#">粤深械网备202005070014</a>
              <a href="#">粤深食药监械经营备20151274号</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHelpCategories, getHelpArticlesByCategory, type HelpCategory, type HelpArticle } from '@/api/common/help'

const router = useRouter()

interface FooterCategory {
  id: number
  name: string
  articles: HelpArticle[]
}

const footerCategories = ref<FooterCategory[]>([])

// 加载页脚数据
const loadFooterData = async () => {
  try {
    // 获取所有分类
    const allCategories = await getHelpCategories()
    
    // 只取前4个顶级分类（parentId为0或没有parentId的分类）
    const topCategories = allCategories.slice(0, 4)
    
    // 为每个分类加载文章
    const categoriesWithArticles = await Promise.all(
      topCategories.map(async (category) => {
        try {
          let allArticles: HelpArticle[] = []
          
          if (category.children && category.children.length > 0) {
            // 如果分类有子分类，聚合所有子分类的文章
            const subCategoryArticles = await Promise.all(
              category.children.map(subCategory => 
                getHelpArticlesByCategory(subCategory.id).catch(() => [])
              )
            )
            // 合并所有子分类的文章
            allArticles = subCategoryArticles.flat()
          } else {
            // 如果没有子分类，直接获取分类本身的文章
            allArticles = await getHelpArticlesByCategory(category.id)
          }
          
          // 按sort排序，然后取前4条
          allArticles.sort((a, b) => (a.sort || 0) - (b.sort || 0))
          
          return {
            id: category.id,
            name: category.name,
            articles: allArticles.slice(0, 4) // 最多显示4条
          }
        } catch (error) {
          console.error(`加载分类 ${category.name} 的文章失败:`, error)
          return {
            id: category.id,
            name: category.name,
            articles: []
          }
        }
      })
    )
    
    footerCategories.value = categoriesWithArticles
  } catch (error) {
    console.error('加载页脚数据失败:', error)
    // 失败时使用空数组，不显示内容
    footerCategories.value = []
  }
}

// 跳转到文章详情
const goToArticle = (articleId: number) => {
  router.push({
    path: '/help',
    query: { articleId }
  })
}

onMounted(() => {
  loadFooterData()
})
</script>

<style scoped lang="scss">
.footer {
  .service-bar {
    background: #e4393c;
    padding: 20px 0;

    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 15px;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .service-item {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #fff;

        .icon-circle {
          width: 30px;
          height: 30px;
          border-radius: 50%;
          background: #fff;
          color: #e4393c;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 16px;
          font-weight: bold;
        }

        .icon {
          font-size: 24px;
          color: #fff;
        }

        .icon-text {
          font-size: 20px;
          color: #fff;
        }

        span {
          font-size: 14px;
          color: #fff;
        }
      }

      .contact-btn {
        padding: 12px 30px;
      }
    }
  }

  .footer-info {
    background: #333;
    color: #fff;
    padding: 40px 0 20px;

    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 15px;
    }

    .footer-content {
      display: flex;
      justify-content: center;
      margin-bottom: 30px;

      .links {
        display: flex;
        gap: 40px;
        justify-content: center;

        .link-group {
          h4 {
            font-size: 14px;
            font-weight: bold;
            margin-bottom: 15px;
            color: #fff;
          }

          a {
            display: block;
            color: #999;
            font-size: 12px;
            margin-bottom: 8px;
            text-decoration: none;
            transition: color 0.3s;

            &:hover {
              color: #e4393c;
            }
          }
        }
      }

    }
  }
}
</style>
