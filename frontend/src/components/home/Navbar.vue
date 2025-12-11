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
            <div
              v-for="category in categories"
              :key="category.id"
              class="category-item"
              @mouseenter="handleCategoryHover(category)"
            >
              <div class="category-title" @click="goToCategory(category.id)">
                {{ category.name }}
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
              <div class="sub-items">
                <span
                  v-for="sub in category.children?.slice(0, 3)"
                  :key="sub.id"
                  class="sub-name"
                  @click.stop="goToCategory(sub.id)"
                >
                  {{ sub.name }}
                </span>
              </div>

              <!-- 二级和三级分类悬浮展示 -->
              <div
                v-show="hoveredCategory?.id === category.id && category.children"
                class="sub-categories"
                @mouseenter="handleCategoryHover(category)"
                @mouseleave="hoveredCategory = null"
              >
                <div
                  v-for="subCategory in category.children"
                  :key="subCategory.id"
                  class="sub-category-group"
                >
                  <div class="third-level">
                    <router-link
                      v-for="third in subCategory.children"
                      :key="third.id"
                      :to="`/products?categoryId=${third.id}`"
                      class="third-item"
                    >
                      {{ third.name }}
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
        <router-link to="/" class="nav-item active">首页</router-link>
        <router-link to="/products?type=new" class="nav-item">新品专区</router-link>
        <router-link to="/products?brand=angus" class="nav-item">虚姬-Angus</router-link>
        <router-link to="/products?type=special" class="nav-item">特惠区</router-link>
        <router-link to="/training" class="nav-item">两性培训营</router-link>
        <router-link to="/news" class="nav-item">最新公告</router-link>
        <router-link to="/cooperation" class="nav-item">合作开店</router-link>
        <router-link to="/stores" class="nav-item">实体店热销</router-link>
      </nav>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Menu, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const showCategories = ref(false)
const hoveredCategory = ref<any>(null)

// 模拟分类数据（后续从接口获取）
const categories = ref([
  {
    id: 1,
    name: '男用器具',
    children: [
      {
        id: 11,
        name: '女用器具',
        children: [
          { id: 111, name: '飞机杯' },
          { id: 112, name: '助勃锻炼' },
          { id: 113, name: '充气娃娃' }
        ]
      },
      {
        id: 12,
        name: '女用器具',
        children: [
          { id: 121, name: '震动棒' },
          { id: 122, name: '跳蛋' },
          { id: 123, name: '潮吹AV棒' }
        ]
      }
    ]
  },
  {
    id: 2,
    name: '女用器具',
    children: [
      {
        id: 21,
        name: '震动跳蛋',
        children: [
          { id: 211, name: '情趣跳蛋' },
          { id: 212, name: '震动棒' },
          { id: 213, name: '潮吹AV棒' }
        ]
      }
    ]
  },
  {
    id: 3,
    name: '避孕润滑',
    children: [
      {
        id: 31,
        name: '润滑液',
        children: [
          { id: 311, name: '润滑液' },
          { id: 312, name: '安全套' },
          { id: 313, name: '排卵测孕' },
          { id: 314, name: '口杯液' }
        ]
      }
    ]
  },
  {
    id: 4,
    name: '情趣内衣',
    children: [
      {
        id: 41,
        name: '连体衣',
        children: [
          { id: 411, name: '连体衣' },
          { id: 412, name: '激情T裤' },
          { id: 413, name: '三点式' }
        ]
      }
    ]
  },
  {
    id: 5,
    name: '护理保健',
    children: [
      {
        id: 51,
        name: '保健食品',
        children: [
          { id: 511, name: '保健食品' },
          { id: 512, name: '清洗抑菌' },
          { id: 513, name: '按摩精油' }
        ]
      }
    ]
  },
  {
    id: 6,
    name: '喷剂助情',
    children: [
      {
        id: 61,
        name: '男用喷剂',
        children: [
          { id: 611, name: '男用喷剂' },
          { id: 612, name: '情欲提升' },
          { id: 613, name: '香水诱惑' }
        ]
      }
    ]
  },
  {
    id: 7,
    name: '其他情趣',
    children: [
      {
        id: 71,
        name: 'SM系列',
        children: [
          { id: 711, name: 'SM系列' },
          { id: 712, name: '情趣跳逗' },
          { id: 713, name: '后庭刺激' }
        ]
      }
    ]
  }
])

const handleCategoryHover = (category: any) => {
  hoveredCategory.value = category
}

const handleCategoryLeave = () => {
  setTimeout(() => {
    hoveredCategory.value = null
  }, 100)
}

// 跳转到分类列表页
const goToCategory = (categoryId: number) => {
  showCategories.value = false
  router.push({
    path: '/products',
    query: { categoryId: String(categoryId) }
  })
}
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
      max-height: 500px;
      overflow-y: auto;

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
          position: fixed;
          left: 580px;
          top: 130px;
          width: 600px;
          height: 500px;
          background: #fff;
          color: #333;
          border: 1px solid #eee;
          box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.1);
          padding: 20px;
          overflow-y: auto;
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
