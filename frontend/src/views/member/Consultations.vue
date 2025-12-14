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
          <MemberSidebar active-menu="messages/consultations" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <div class="consultations-wrapper">
              <!-- 页面标题 -->
              <div class="page-title">我的咨询</div>

              <!-- 咨询列表 -->
              <div v-loading="loading" class="consultation-list">
                <div v-if="consultationList.length === 0 && !loading" class="empty-data">
                  <el-empty description="暂无咨询记录" />
                </div>
                <div v-else>
                  <div
                    v-for="consultation in consultationList"
                    :key="consultation.id"
                    class="consultation-item"
                  >
                    <!-- 咨询头部信息 -->
                    <div class="consultation-header">
                      <div class="product-info">
                        <img 
                          v-if="consultation.productImage" 
                          :src="consultation.productImage" 
                          :alt="consultation.productName"
                          class="product-image"
                        />
                        <div class="product-details">
                          <h4 class="product-name">{{ consultation.productName }}</h4>
                          <div class="consultation-meta">
                            <span class="consultation-time">{{ formatTime(consultation.createdTime) }}</span>
                            <el-tag 
                              :type="getStatusTagType(consultation.status)" 
                              size="small"
                            >
                              {{ consultation.statusText }}
                            </el-tag>
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- 咨询内容 -->
                    <div class="consultation-content">
                      <div class="question-section">
                        <div class="section-label">我的咨询：</div>
                        <div class="content-text">{{ consultation.consultationContent }}</div>
                        <div class="contact-info">
                          联系人：{{ consultation.contactName }}
                          <span v-if="consultation.contactPhone"> | 电话：{{ consultation.contactPhone }}</span>
                          <span v-if="consultation.contactEmail"> | 邮箱：{{ consultation.contactEmail }}</span>
                        </div>
                      </div>

                      <!-- 商家回复 -->
                      <div v-if="consultation.replyContent" class="reply-section">
                        <div class="section-label">商家回复：</div>
                        <div class="content-text">{{ consultation.replyContent }}</div>
                        <div class="reply-info">
                          <span v-if="consultation.replyAdminName">回复人：{{ consultation.replyAdminName }}</span>
                          <span class="reply-time">{{ formatTime(consultation.replyTime) }}</span>
                        </div>
                      </div>

                      <!-- 等待回复提示 -->
                      <div v-else-if="consultation.status === 0" class="pending-reply">
                        <el-icon><Clock /></el-icon>
                        <span>等待商家回复中...</span>
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
                  @current-change="loadConsultationList"
                  @size-change="loadConsultationList"
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
import { Clock } from '@element-plus/icons-vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { getMyConsultations, type ConsultationVO } from '@/api/buyer/consultation'

const unreadMessageCount = ref(0)
const loading = ref(false)

// 咨询列表
const consultationList = ref<ConsultationVO[]>([])

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  switch (status) {
    case 0: return 'warning'  // 待回复
    case 1: return 'success'  // 已回复
    case 2: return 'info'     // 已关闭
    default: return ''
  }
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

// 加载咨询列表
const loadConsultationList = async () => {
  loading.value = true
  try {
    const response = await getMyConsultations(pagination.value.current, pagination.value.size)
    consultationList.value = response.records
    pagination.value.total = response.total
  } catch (error: any) {
    console.error('加载咨询列表失败:', error)
    ElMessage.error(error.response?.data?.message || '加载咨询列表失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadConsultationList()
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

      .consultations-wrapper {
        .page-title {
          font-size: 18px;
          font-weight: bold;
          color: #333;
          margin-bottom: 20px;
          padding-bottom: 10px;
          border-bottom: 2px solid #e4393c;
        }

        .consultation-list {
          min-height: 300px;

          .empty-data {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 300px;
          }

          .consultation-item {
            border: 1px solid #e5e5e5;
            border-radius: 8px;
            margin-bottom: 20px;
            background: #fff;
            overflow: hidden;
            transition: box-shadow 0.3s;

            &:hover {
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            }

            .consultation-header {
              background: #f9f9f9;
              padding: 15px 20px;
              border-bottom: 1px solid #e5e5e5;

              .product-info {
                display: flex;
                align-items: center;
                gap: 15px;

                .product-image {
                  width: 60px;
                  height: 60px;
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

                  .consultation-meta {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    font-size: 14px;

                    .consultation-time {
                      color: #999;
                    }
                  }
                }
              }
            }

            .consultation-content {
              padding: 20px;

              .question-section,
              .reply-section {
                margin-bottom: 20px;

                &:last-child {
                  margin-bottom: 0;
                }

                .section-label {
                  font-weight: bold;
                  color: #333;
                  margin-bottom: 8px;
                  font-size: 14px;
                }

                .content-text {
                  background: #f8f9fa;
                  padding: 12px 15px;
                  border-radius: 4px;
                  line-height: 1.6;
                  color: #333;
                  margin-bottom: 8px;
                  white-space: pre-wrap;
                  word-break: break-word;
                }

                .contact-info,
                .reply-info {
                  font-size: 12px;
                  color: #999;
                  display: flex;
                  align-items: center;
                  gap: 10px;

                  .reply-time {
                    margin-left: auto;
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
                }
              }

              .pending-reply {
                display: flex;
                align-items: center;
                gap: 8px;
                color: #999;
                font-size: 14px;
                padding: 15px;
                background: #f8f9fa;
                border-radius: 4px;
                text-align: center;
                justify-content: center;

                .el-icon {
                  animation: pulse 1.5s ease-in-out infinite;
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

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .member-content {
    .member-main {
      flex-direction: column;

      .member-main-content {
        .consultations-wrapper {
          .consultation-list {
            .consultation-item {
              .consultation-header {
                .product-info {
                  flex-direction: column;
                  align-items: flex-start;
                  text-align: center;

                  .product-image {
                    align-self: center;
                  }
                }
              }

              .consultation-content {
                padding: 15px;

                .question-section,
                .reply-section {
                  .contact-info,
                  .reply-info {
                    flex-direction: column;
                    align-items: flex-start;
                    gap: 5px;

                    .reply-time {
                      margin-left: 0;
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