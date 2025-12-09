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
          <MemberSidebar :active-menu="activeMenu" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <!-- 欢迎信息 -->
            <div class="welcome-section">
              <h2 class="welcome-title">
                您好,{{ userStore.userInfo?.realName || userStore.userInfo?.username }}先生,欢迎进入用户中心
              </h2>
            </div>

            <!-- 预存款余额 -->
            <div class="info-card">
              <div class="card-header">
                <span class="card-title">预存款余额</span>
              </div>
              <div class="card-content">
                <div class="balance-info">
                  <span class="balance-label">¥</span>
                  <span class="balance-amount">{{ depositBalance }}</span>
                  <span class="balance-unit">元</span>
                  <span class="available-balance">(可用余额¥{{ availableBalance }}元)</span>
                </div>
              </div>
            </div>

            <!-- 未读消息 -->
            <div class="info-card">
              <div class="card-header">
                <span class="card-title">您的未读消息</span>
              </div>
              <div class="card-content">
                <span class="message-count">{{ unreadMessageCount }}条</span>
                <el-button type="text" class="view-link">查看</el-button>
              </div>
            </div>

            <!-- 订单相关 -->
            <div class="info-card">
              <div class="card-header">
                <span class="card-title">订单相关</span>
              </div>
              <div class="card-content order-stats">
                <div class="order-item">
                  <span class="order-label">您的未付款订单总数量:</span>
                  <span class="order-count">{{ unpaidOrderCount }}个</span>
                  <el-button type="text" class="action-link">付款</el-button>
                </div>
                <div class="order-item">
                  <span class="order-label">您的已发货订单总数量:</span>
                  <span class="order-count">{{ shippedOrderCount }}个</span>
                  <el-button type="text" class="action-link">查看</el-button>
                </div>
                <div class="order-item">
                  <span class="order-label">您的已作废订单总数量:</span>
                  <span class="order-count">{{ cancelledOrderCount }}个</span>
                  <el-button type="text" class="action-link">查看</el-button>
                </div>
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
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'

const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = ref('transaction/orders')

// 未读消息数量
const unreadMessageCount = ref(0)

// 预存款余额
const depositBalance = ref(0)
const availableBalance = ref(0)

// 订单统计
const unpaidOrderCount = ref(0)
const shippedOrderCount = ref(17)
const cancelledOrderCount = ref(1)

// 菜单选择逻辑已移至 MemberSidebar 组件中
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

      // 欢迎信息
      .welcome-section {
        padding: 30px 20px;
        border-bottom: 1px solid #e5e5e5;

        .welcome-title {
          font-size: 18px;
          font-weight: normal;
          color: #333;
          margin: 0;
        }
      }

      // 信息卡片
      .info-card {
        border-bottom: 1px solid #e5e5e5;
        padding: 20px;

        .card-header {
          margin-bottom: 15px;

          .card-title {
            font-size: 16px;
            font-weight: bold;
            color: #333;
          }
        }

        .card-content {
          font-size: 14px;
          color: #666;

          // 预存款余额
          .balance-info {
            display: flex;
            align-items: baseline;
            gap: 5px;

            .balance-label {
              font-size: 18px;
              color: #e4393c;
              font-weight: bold;
            }

            .balance-amount {
              font-size: 28px;
              color: #e4393c;
              font-weight: bold;
            }

            .balance-unit {
              font-size: 16px;
              color: #666;
            }

            .available-balance {
              font-size: 14px;
              color: #999;
              margin-left: 10px;
            }
          }

          // 未读消息
          .message-count {
            color: #e4393c;
            font-weight: bold;
            margin-right: 10px;
          }

          .view-link {
            color: #e4393c;
            padding: 0;
            font-size: 14px;

            &:hover {
              text-decoration: underline;
            }
          }

          // 订单统计
          &.order-stats {
            .order-item {
              display: flex;
              align-items: center;
              margin-bottom: 12px;

              &:last-child {
                margin-bottom: 0;
              }

              .order-label {
                color: #666;
                margin-right: 10px;
              }

              .order-count {
                color: #e4393c;
                font-weight: bold;
                margin-right: 10px;
              }

              .action-link {
                color: #e4393c;
                padding: 0;
                font-size: 14px;

                &:hover {
                  text-decoration: underline;
                }
              }
            }
          }
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
    }
  }
}
</style>
