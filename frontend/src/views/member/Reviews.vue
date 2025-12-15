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
          <MemberSidebar active-menu="messages/reviews" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <div class="reviews-wrapper">
              <!-- 页面标题 -->
              <div class="page-title">我的评价</div>

              <!-- 评价列表 -->
              <div v-loading="loading" class="review-list">
                <div v-if="reviewList.length === 0 && !loading" class="empty-data">
                  <el-empty description="暂无评价记录" />
                </div>
                <div v-else>
                  <div
                    v-for="review in reviewList"
                    :key="review.id"
                    class="review-item"
                  >
                    <!-- 评价头部信息 -->
                    <div class="review-header">
                      <div class="product-info">
                        <img 
                          v-if="review.productImage" 
                          :src="review.productImage" 
                          :alt="review.productName"
                          class="product-image"
                        />
                        <div class="product-details">
                          <h4 class="product-name">{{ review.productName }}</h4>
                          <div class="review-meta">
                            <span class="order-info">订单号：{{ review.orderNumber }}</span>
                            <span class="review-time">{{ formatTime(review.createdTime) }}</span>
                            <el-tag 
                              :type="getStatusTagType(review.status)" 
                              size="small"
                            >
                              {{ review.statusText }}
                            </el-tag>
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- 评价内容 -->
                    <div class="review-content">
                      <div v-if="review.reviewContent" class="content-section">
                        <div class="section-label">评价内容：</div>
                        <div class="content-text">{{ review.reviewContent }}</div>
                      </div>

                      <!-- 评价图片 -->
                      <div v-if="review.reviewImages && review.reviewImages.length > 0" class="images-section">
                        <div class="section-label">评价图片：</div>
                        <div class="review-images">
                          <el-image
                            v-for="(img, index) in review.reviewImages"
                            :key="index"
                            :src="img"
                            class="review-image"
                            fit="cover"
                            :preview-src-list="review.reviewImages"
                            :initial-index="index"
                          />
                        </div>
                      </div>

                      <!-- 商家回复 -->
                      <div v-if="review.adminReply" class="reply-section">
                        <div class="section-label">商家回复：</div>
                        <div class="content-text">{{ review.adminReply }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 分页 -->
              <div v-if="pagination.total > 0" class="pagination-wrapper">
                <el-pagination
                  v-model:current-page="pagination.current"
                  v-model:page-size="pagination.size"
                  :total="pagination.total"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  @current-change="loadReviewList"
                  @size-change="loadReviewList"
                />
              </div>
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
import { ElMessage } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { getMyReviews, type ProductReviewVO } from '@/api/buyer/review'

const unreadMessageCount = ref(0)
const loading = ref(false)

// 评价列表
const reviewList = ref<ProductReviewVO[]>([])

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  switch (status) {
    case 0: return 'warning'  // 待审核
    case 1: return 'success'  // 已通过
    case 2: return 'danger'   // 已拒绝
    default: return ''
  }
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

// 加载评价列表
const loadReviewList = async () => {
  loading.value = true
  try {
    const response = await getMyReviews(pagination.value.current, pagination.value.size)
    reviewList.value = response.records
    pagination.value.total = response.total
  } catch (error: any) {
    console.error('加载评价列表失败:', error)
    ElMessage.error(error.response?.data?.message || '加载评价列表失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadReviewList()
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

      .reviews-wrapper {
        .page-title {
          font-size: 18px;
          font-weight: bold;
          color: #333;
          margin-bottom: 20px;
          padding-bottom: 10px;
          border-bottom: 2px solid #e4393c;
        }

        .review-list {
          min-height: 300px;

          .empty-data {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 300px;
          }

          .review-item {
            border: 1px solid #e5e5e5;
            border-radius: 8px;
            margin-bottom: 20px;
            background: #fff;
            overflow: hidden;
            transition: box-shadow 0.3s;

            &:hover {
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            }

            .review-header {
              background: #f9f9f9;
              padding: 15px 20px;
              border-bottom: 1px solid #e5e5e5;

              .product-info {
                display: flex;
                align-items: center;
                gap: 15px;

                .product-image {
                  width: 80px;
                  height: 80px;
                  object-fit: cover;
                  border-radius: 4px;
                  border: 1px solid #e5e5e5;
                }

                .product-details {
                  flex: 1;

                  .product-name {
                    font-size: 16px;
                    font-weight: bold;
                    color: #333;
                    margin: 0 0 8px 0;
                    line-height: 1.4;
                  }

                  .review-meta {
                    display: flex;
                    align-items: center;
                    gap: 15px;
                    font-size: 14px;

                    .order-info,
                    .review-time {
                      color: #999;
                    }
                  }
                }
              }
            }

            .review-content {
              padding: 20px;

              .rating-section,
              .content-section,
              .images-section,
              .reply-section {
                margin-bottom: 20px;

                &:last-child {
                  margin-bottom: 0;
                }

                .section-label {
                  font-weight: bold;
                  color: #333;
                  margin-bottom: 10px;
                  font-size: 14px;
                }
              }

              .rating-section {
                display: flex;
                align-items: center;
                gap: 15px;

                .section-label {
                  margin-bottom: 0;
                }
              }

              .content-section .content-text {
                background: #f8f9fa;
                padding: 12px 15px;
                border-radius: 4px;
                line-height: 1.6;
                color: #333;
                white-space: pre-wrap;
                word-break: break-word;
              }

              .images-section {
                .review-images {
                  display: flex;
                  gap: 10px;
                  flex-wrap: wrap;

                  .review-image {
                    width: 100px;
                    height: 100px;
                    border-radius: 4px;
                    cursor: pointer;
                    border: 1px solid #e5e5e5;
                  }
                }
              }

              .reply-section {
                .section-label {
                  color: #e4393c;
                }

                .content-text {
                  background: #fff5f5;
                  border-left: 3px solid #e4393c;
                  padding: 12px 15px;
                  border-radius: 4px;
                  line-height: 1.6;
                  color: #333;
                  white-space: pre-wrap;
                  word-break: break-word;
                }
              }
            }
          }
        }

        .pagination-wrapper {
          margin-top: 30px;
          display: flex;
          justify-content: center;
        }
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
        .reviews-wrapper {
          .review-list {
            .review-item {
              .review-header {
                .product-info {
                  flex-direction: column;
                  align-items: flex-start;
                  text-align: center;

                  .product-image {
                    align-self: center;
                  }

                  .product-details {
                    width: 100%;
                    text-align: left;

                    .review-meta {
                      flex-direction: column;
                      align-items: flex-start;
                      gap: 5px;
                    }
                  }
                }
              }

              .review-content {
                padding: 15px;

                .rating-section {
                  flex-direction: column;
                  align-items: flex-start;
                  gap: 10px;

                  .section-label {
                    margin-bottom: 0;
                  }
                }

                .images-section {
                  .review-images {
                    .review-image {
                      width: 80px;
                      height: 80px;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>