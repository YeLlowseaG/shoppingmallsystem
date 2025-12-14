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
          <MemberSidebar active-menu="site-messages/inbox" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <div class="inbox-wrapper">
              <!-- 页面标题 -->
              <div class="page-title">
                <span class="title-text">收件箱</span>
                <span class="title-count" v-if="unreadCount > 0">({{ unreadCount }}条未读)</span>
              </div>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <el-button @click="handleMarkAllRead" :disabled="unreadCount === 0">全部标记为已读</el-button>
                <el-button @click="handleRefresh">刷新</el-button>
              </div>

              <!-- 消息列表 -->
              <div class="message-list">
                <div v-if="loading" class="loading-wrapper">
                  <el-skeleton :rows="5" animated />
                </div>
                <div v-else-if="messageList.length === 0" class="empty-data">
                  <el-empty description="暂无消息" />
                </div>
                <div v-else>
                  <div
                    v-for="message in messageList"
                    :key="message.id"
                    class="message-item"
                    :class="{ 'unread': !message.isRead }"
                    @click="handleViewMessage(message)"
                  >
                    <div class="message-header">
                      <div class="message-title-row">
                        <span class="message-title">{{ message.title }}</span>
                        <span v-if="!message.isRead" class="unread-badge">未读</span>
                        <span v-if="message.messageType === 1" class="system-badge">系统消息</span>
                        <span v-if="message.messageType === 2" class="order-badge">订单消息</span>
                      </div>
                      <div class="message-time">{{ formatDateTime(message.createTime) }}</div>
                    </div>
                    <div class="message-content">{{ message.content }}</div>
                    <div v-if="message.orderNo" class="message-order">
                      <span>关联订单：</span>
                      <el-link
                        type="primary"
                        :underline="false"
                        @click.stop="handleViewOrder(message.orderNo)"
                      >
                        {{ message.orderNo }}
                      </el-link>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 分页 -->
              <div class="pagination-wrapper" v-if="messageList.length > 0">
                <el-pagination
                  v-model:current-page="pagination.currentPage"
                  v-model:page-size="pagination.pageSize"
                  :total="pagination.total"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="prev, pager, next, jumper, total"
                  @current-change="handlePageChange"
                  @size-change="handleSizeChange"
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { getInboxMessages, markMessageAsRead, markAllAsRead, type MessageVO } from '@/api/buyer/message'
import { formatDateTime } from '@/utils'

const router = useRouter()
const route = useRoute()

const unreadMessageCount = ref(0)
const loading = ref(false)

// 消息列表
const messageList = ref<MessageVO[]>([])
const unreadCount = ref(0)

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载消息列表
const loadMessages = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    const response = await getInboxMessages(params)
    
    messageList.value = response.records || []
    pagination.total = response.total || 0
    unreadCount.value = response.unreadCount || 0
    unreadMessageCount.value = unreadCount.value
  } catch (error: any) {
    ElMessage.error(error.message || '加载消息失败')
    messageList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 查看消息详情
const handleViewMessage = async (message: MessageVO) => {
  if (!message.isRead) {
    try {
      await markMessageAsRead(message.id)
      message.isRead = true
      unreadCount.value--
      unreadMessageCount.value = unreadCount.value
    } catch (error: any) {
      console.error('标记已读失败:', error)
    }
  }
  
  // 如果有订单号，可以跳转到订单详情
  if (message.orderNo) {
    router.push({
      path: '/order/detail',
      query: {
        orderNumber: message.orderNo
      }
    })
  }
}

// 查看订单
const handleViewOrder = (orderNo: string) => {
  router.push({
    path: '/order/detail',
    query: {
      orderNumber: orderNo
    }
  })
}

// 全部标记为已读
const handleMarkAllAsRead = async () => {
  try {
    await markAllAsRead()
    ElMessage.success('全部标记为已读成功')
    await loadMessages()
  } catch (error: any) {
    ElMessage.error(error.message || '标记失败')
  }
}

// 刷新
const handleRefresh = () => {
  loadMessages()
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.currentPage = page
  loadMessages()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadMessages()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 初始化
onMounted(() => {
  loadMessages()
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

  .member-main {
    display: flex;
    gap: 20px;
    align-items: flex-start;

    .member-main-content {
      flex: 1;
      background: #fff;
      min-height: 500px;
      padding: 20px;

      .inbox-wrapper {
        .page-title {
          font-size: 16px;
          margin-bottom: 15px;
          padding-bottom: 10px;
          border-bottom: 1px solid #e5e5e5;
          color: #333;

          .title-text {
            font-weight: 500;
          }

          .title-count {
            color: #e4393c;
            margin-left: 10px;
          }
        }

        .action-buttons {
          margin-bottom: 15px;
          display: flex;
          gap: 10px;
        }

        .message-list {
          .loading-wrapper {
            padding: 20px 0;
          }

          .empty-data {
            padding: 60px 0;
            text-align: center;
          }

          .message-item {
            background: #fff;
            border: 1px solid #e5e5e5;
            border-radius: 4px;
            padding: 15px;
            margin-bottom: 15px;
            cursor: pointer;
            transition: all 0.3s;

            &:hover {
              border-color: #e4393c;
              box-shadow: 0 2px 8px rgba(228, 57, 60, 0.1);
            }

            &.unread {
              background: #fff5f5;
              border-left: 3px solid #e4393c;
            }

            .message-header {
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              margin-bottom: 10px;

              .message-title-row {
                display: flex;
                align-items: center;
                gap: 10px;
                flex: 1;

                .message-title {
                  font-size: 15px;
                  font-weight: 500;
                  color: #333;
                }

                .unread-badge {
                  background: #e4393c;
                  color: #fff;
                  font-size: 12px;
                  padding: 2px 8px;
                  border-radius: 10px;
                }

                .system-badge {
                  background: #409eff;
                  color: #fff;
                  font-size: 12px;
                  padding: 2px 8px;
                  border-radius: 10px;
                }

                .order-badge {
                  background: #67c23a;
                  color: #fff;
                  font-size: 12px;
                  padding: 2px 8px;
                  border-radius: 10px;
                }
              }

              .message-time {
                font-size: 12px;
                color: #999;
              }
            }

            .message-content {
              font-size: 14px;
              color: #666;
              line-height: 1.6;
              margin-bottom: 10px;
              display: -webkit-box;
              -webkit-line-clamp: 3;
              -webkit-box-orient: vertical;
              overflow: hidden;
            }

            .message-order {
              font-size: 12px;
              color: #999;
              padding-top: 10px;
              border-top: 1px solid #f0f0f0;
            }
          }
        }

        .pagination-wrapper {
          margin-top: 20px;
          display: flex;
          justify-content: center;
        }
      }
    }
  }
}
</style>
