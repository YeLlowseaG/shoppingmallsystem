<template>
  <div class="announcement-list-page">
    <!-- 复用首页头部组件 -->
    <TopBar />
    <Header />
    <Navbar />

    <div class="container">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator=">" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>最新公告</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 公告列表 -->
      <div class="announcement-list">
        <div v-if="loading" class="loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          加载中...
        </div>
        <div v-else-if="announcements.length === 0" class="empty">
          <el-empty description="暂无公告" />
        </div>
        <div v-else class="list-content">
          <div
            v-for="announcement in announcements"
            :key="announcement.id"
            class="announcement-item"
            @click="handleItemClick(announcement.id)"
          >
            <div class="item-left">
              <div class="title">{{ announcement.title }}</div>
            </div>
            <div class="item-right">
              <span class="publish-date">[{{ formatDateShort(announcement.publishDate) }}]</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :total="total"
          :page-size="pageSize"
          layout="prev, pager, next, jumper"
          class="pagination"
          @current-change="handlePageChange"
        />
        <div class="page-input">
          <span>到第</span>
          <el-input-number
            v-model="jumpPage"
            :min="1"
            :max="totalPages"
            :controls="false"
            class="page-input-number"
            @keyup.enter="handleJumpPage"
          />
          <span>页</span>
          <el-button type="primary" size="small" @click="handleJumpPage">确定</el-button>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import { getAnnouncementList } from '@/api/common/announcement'
import type { Announcement } from '@/api/common/announcement'
import { formatDate } from '@/utils'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 公告列表数据
const announcements = ref<Announcement[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const jumpPage = ref(1)

// 计算总页数
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

import { formatDate } from '@/utils'

// 格式化日期（显示年月日，带点号）
const formatDateWithDot = (dateStr: string) => {
  if (!dateStr) return ''
  const formatted = formatDate(dateStr, 'YYYY.MM.DD')
  return formatted
}

// 格式化日期（短格式，用于右侧显示）
const formatDateShort = (dateStr: string) => {
  if (!dateStr) return ''
  return formatDate(dateStr, 'YYYY-MM-DD')
}

// 加载公告列表
const loadAnnouncements = async () => {
  loading.value = true
  try {
    const response = await getAnnouncementList({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    announcements.value = response.records || []
    total.value = response.total || 0
    jumpPage.value = currentPage.value
  } catch (error) {
    console.error('加载公告列表失败:', error)
    announcements.value = []
    total.value = 0
    // 如果API调用失败，使用模拟数据
    generateMockData()
  } finally {
    loading.value = false
  }
}

// 生成模拟数据（用于开发测试）
const generateMockData = () => {
  const mockAnnouncements: Announcement[] = []
  const dates = [
    '2025-12-10',
    '2025-12-08',
    '2025-12-05',
    '2025-12-01',
    '2025-11-28',
    '2025-11-25',
    '2025-11-20',
    '2025-11-15',
    '2025-11-10',
    '2025-11-05',
    '2025-10-30',
    '2025-10-25',
    '2025-10-20',
    '2025-10-15',
    '2025-10-10',
    '2025-10-05',
    '2025-09-30',
    '2025-09-25',
    '2025-09-20',
    '2025-09-15'
  ]
  const titles = [
    '安全套税率调整及价格变动的通知',
    '冈本品牌价格调整通知',
    '杜蕾斯部分产品下架说明',
    '拼多多平台价格限制通知',
    '临时暂停发货通知',
    '新品牌入驻公告',
    '双十一活动预告',
    '会员积分规则调整',
    '配送时间调整通知',
    '支付方式更新',
    '商品质量保证声明',
    '售后服务升级公告',
    '新用户注册优惠',
    '会员等级权益说明',
    '退换货政策更新',
    '物流配送优化通知',
    '商品库存更新',
    '促销活动规则说明',
    '系统维护通知',
    '客服服务时间调整'
  ]

  for (let i = 0; i < pageSize.value; i++) {
    const index = (currentPage.value - 1) * pageSize.value + i
    if (index >= dates.length) break
    mockAnnouncements.push({
      id: index + 1,
      title: titles[index % titles.length],
      content: '',
      publishDate: dates[index % dates.length],
      sort: 0,
      status: 1
    })
  }

  announcements.value = mockAnnouncements
  total.value = 341 // 模拟总数
}

// 分页切换
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadAnnouncements()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 跳转到指定页
const handleJumpPage = () => {
  if (jumpPage.value >= 1 && jumpPage.value <= totalPages.value) {
    currentPage.value = jumpPage.value
    loadAnnouncements()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// 点击公告项
const handleItemClick = (id: number) => {
  router.push(`/news/${id}`)
}

// 组件挂载时加载数据
onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped lang="scss">
.announcement-list-page {
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
    font-size: 14px;
    color: #666;
  }

  .announcement-list {
    min-height: 400px;

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

    .list-content {
      .announcement-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 0;
        border-bottom: 1px solid #eee;
        cursor: pointer;
        transition: background-color 0.3s;

        &:hover {
          background-color: #f9f9f9;
        }

        .item-left {
          display: flex;
          align-items: center;
          flex: 1;

          .title {
            color: #333;
            font-size: 14px;
            flex: 1;

            &:hover {
              color: #e4393c;
            }
          }
        }

        .item-right {
          .publish-date {
            color: #999;
            font-size: 12px;
          }
        }
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #eee;

    .pagination {
      flex: 1;
    }

    .page-input {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-left: 20px;
      font-size: 14px;
      color: #666;

      .page-input-number {
        width: 60px;

        :deep(.el-input__inner) {
          text-align: center;
        }
      }
    }
  }
}
</style>

