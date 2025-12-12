<template>
  <div class="member-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 会员中心内容区域 -->
    <div class="member-content">
      <div class="container">
        <!-- 会员中心标题栏 -->
        <MemberHeaderBar />

        <!-- 会员中心主体 -->
        <div class="member-main">
          <!-- 左侧导航菜单 -->
          <MemberSidebar active-menu="favorites/products" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <!-- 页面标题 -->
            <div class="page-title">
              <h2>我的收藏</h2>
            </div>

            <!-- 商品收藏列表 -->
            <div class="favorites-list" v-if="favoritesList.length > 0">
              <div
                v-for="item in favoritesList"
                :key="item.id"
                class="favorite-item"
              >
                <!-- 商品图片 -->
                <div class="product-image">
                  <img :src="item.mainImage" :alt="item.productName" />
                </div>

                <!-- 商品信息 -->
                <div class="product-info">
                  <div class="product-name">{{ item.productName }}</div>
                  <div class="product-note">
                    <span v-if="item.userLevelPrice">会员价:{{ item.userLevelPrice }}元。</span>
                    <span v-if="item.stock">库存:{{ item.stock }}</span>
                  </div>
                </div>

                <!-- 商品价格 -->
                <div class="product-price">
                  <span class="price">¥{{ item.basePrice }}</span>
                </div>

                <!-- 操作按钮 -->
                <div class="product-actions">
                  <el-button
                    link
                    type="danger"
                    class="action-btn"
                    @click="handleDelete(item)"
                  >
                    删除
                  </el-button>
                  <el-button
                    type="danger"
                    class="add-cart-btn"
                    @click="handleAddToCart(item)"
                  >
                    加入购物车
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div class="empty-state" v-else>
              <el-empty description="暂无收藏商品" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { getFavoritePage, removeFavorite, type FavoriteVO } from '@/api/buyer/favorite'

const unreadMessageCount = ref(0)
const loading = ref(false)

// 收藏商品列表
const favoritesList = ref<FavoriteVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 加载收藏列表
const loadFavorites = async () => {
  loading.value = true
  try {
    const response = await getFavoritePage(currentPage.value, pageSize.value)
    favoritesList.value = response.records
    total.value = response.total
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    ElMessage.error('加载收藏列表失败')
    favoritesList.value = []
  } finally {
    loading.value = false
  }
}

// 删除收藏
const handleDelete = async (item: FavoriteVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品"${item.productName}"的收藏吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await removeFavorite(item.productId)
    ElMessage.success('删除成功')
    loadFavorites() // 重新加载列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除收藏失败:', error)
      ElMessage.error('删除收藏失败')
    }
  }
}

// 加入购物车
const handleAddToCart = (item: FavoriteVO) => {
  // TODO: 调用加入购物车API
  ElMessage.success('已加入购物车')
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped lang="scss">
.member-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.member-content {
  background: #fff;
  padding: 20px 0 40px;
  min-height: 600px;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }

  // 会员中心主体
  .member-main {
    display: flex;
    gap: 20px;
    align-items: flex-start;

    // 右侧主内容区
    .member-main-content {
      flex: 1;
      background: #fff;
      min-height: 500px;
      padding: 20px;

      // 页面标题
      .page-title {
        margin-bottom: 20px;
        padding-bottom: 10px;
        border-bottom: 1px solid #e5e5e5;

        h2 {
          font-size: 18px;
          font-weight: 500;
          color: #333;
          margin: 0;
        }
      }

      // 收藏列表
      .favorites-list {
        .favorite-item {
          display: flex;
          align-items: flex-start;
          padding: 15px 0;
          border-bottom: 1px solid #f0f0f0;
          gap: 15px;

          &:last-child {
            border-bottom: none;
          }

          &:hover {
            background: #fafafa;
            margin: 0 -20px;
            padding-left: 20px;
            padding-right: 20px;
          }

          // 商品图片
          .product-image {
            width: 150px;
            height: 150px;
            flex-shrink: 0;
            border: 1px solid #e5e5e5;
            border-radius: 4px;
            overflow: hidden;
            background: #f5f5f5;
            cursor: pointer;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
              transition: transform 0.3s;
            }

            &:hover img {
              transform: scale(1.05);
            }
          }

          // 商品信息
          .product-info {
            flex: 1;
            min-width: 0;
            padding-top: 5px;

            .product-name {
              font-size: 14px;
              color: #333;
              line-height: 1.6;
              margin-bottom: 10px;
              word-break: break-all;
              cursor: pointer;

              &:hover {
                color: #e4393c;
              }
            }

            .product-note {
              font-size: 12px;
              color: #999;
              line-height: 1.5;
            }
          }

          // 商品价格和操作区域
          .product-price {
            width: 150px;
            text-align: right;
            flex-shrink: 0;
            padding-top: 5px;

            .price {
              font-size: 20px;
              font-weight: bold;
              color: #e4393c;
              display: block;
              margin-bottom: 15px;
            }
          }

          // 操作按钮
          .product-actions {
            width: 180px;
            display: flex;
            flex-direction: column;
            gap: 8px;
            flex-shrink: 0;
            padding-top: 5px;

            .action-btn {
              padding: 0;
              height: auto;
              font-size: 13px;
              text-align: left;
              justify-content: flex-start;
              color: #666;

              &:hover {
                color: #e4393c;
                text-decoration: underline;
              }
            }

            .add-cart-btn {
              width: 100%;
              height: 36px;
              background: #e4393c;
              border-color: #e4393c;
              color: #fff;
              font-size: 14px;
              margin-top: 5px;

              &:hover {
                background: #c9302c;
                border-color: #c9302c;
              }
            }
          }
        }
      }

      // 空状态
      .empty-state {
        padding: 60px 0;
        text-align: center;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .member-content {
    .member-main {
      flex-direction: column;

      .member-main-content {
        .favorites-list {
          .favorite-item {
            flex-wrap: wrap;

            .product-image {
              width: 100px;
              height: 100px;
            }

            .product-info {
              min-width: 200px;
            }

            .product-price {
              width: 100px;
            }

            .product-actions {
              width: 100%;
              flex-direction: row;
              justify-content: flex-start;
              flex-wrap: wrap;

              .add-cart-btn {
                width: auto;
                min-width: 120px;
              }
            }
          }
        }
      }
    }
  }
}
</style>

