<template>
  <div class="announcement-detail-page">
    <!-- 复用首页头部组件 -->
    <TopBar />
    <Header />
    <Navbar />

    <div class="container">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator=">" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/news' }">最新公告</el-breadcrumb-item>
        <el-breadcrumb-item>{{ announcement?.title || '公告详情' }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 公告详情 -->
      <div class="announcement-detail">
        <div v-if="loading" class="loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          加载中...
        </div>
        <div v-else-if="!announcement" class="empty">
          <el-empty description="公告不存在" />
        </div>
        <div v-else class="detail-content">
          <!-- 标题 -->
          <h1 class="title">{{ announcement.title }}</h1>

          <!-- 发布日期和更新时间 -->
          <div class="meta-info">
            <div class="meta-item">
              <span class="label">发布日期：</span>
              <span class="value">{{ formatDate(announcement.publishDate) }}</span>
            </div>
            <div v-if="announcement.updateTime" class="meta-item">
              <span class="label">最后更新时间：</span>
              <span class="value">{{ formatDate(announcement.updateTime) }}</span>
            </div>
          </div>

          <!-- 正文内容 -->
          <div class="content" v-html="announcement.content"></div>

          <!-- 返回公告列表 -->
          <div class="back-to-list">
            <router-link to="/news" class="back-link">
              <el-icon><ArrowLeft /></el-icon>
              返回公告列表
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Loading, ArrowLeft } from '@element-plus/icons-vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import {
  getAnnouncementById
} from '@/api/common/announcement'
import type { Announcement } from '@/api/common/announcement'
import { formatDate } from '@/utils'

const route = useRoute()

// 加载状态
const loading = ref(false)

// 公告详情数据
const announcement = ref<Announcement | null>(null)

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 加载公告详情
const loadAnnouncementDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  // 清空之前的数据
  announcement.value = null

  try {
    // 加载公告详情
    const detail = await getAnnouncementById(id)
    announcement.value = detail
  } catch (error) {
    console.error('加载公告详情失败:', error)
    announcement.value = null
  } finally {
    loading.value = false
  }
}

// 监听路由参数变化，重新加载数据
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadAnnouncementDetail()
    }
  },
  { immediate: false }
)

// 组件挂载时加载数据
onMounted(() => {
  loadAnnouncementDetail()
})
</script>

<style scoped lang="scss">
.announcement-detail-page {
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

  .announcement-detail {
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

    .detail-content {
      .title {
        font-size: 20px;
        font-weight: bold;
        color: #333;
        margin-bottom: 20px;
        padding-bottom: 15px;
        border-bottom: 2px solid #e4393c;
      }

      .meta-info {
        display: flex;
        gap: 30px;
        margin-bottom: 30px;
        padding-bottom: 15px;
        border-bottom: 1px solid #eee;
        font-size: 14px;
        color: #666;

        .meta-item {
          .label {
            color: #999;
          }

          .value {
            color: #333;
          }
        }
      }

      .content {
        line-height: 1.8;
        color: #333;
        font-size: 14px;
        margin-bottom: 40px;
        min-height: 200px;

        :deep(p) {
          margin-bottom: 15px;
        }

        :deep(h3) {
          font-size: 16px;
          font-weight: bold;
          margin: 20px 0 10px;
          color: #333;
        }

        :deep(ul),
        :deep(ol) {
          margin: 15px 0;
          padding-left: 30px;

          li {
            margin-bottom: 8px;
          }
        }
      }

      .back-to-list {
        margin-top: 40px;
        padding-top: 20px;
        border-top: 1px solid #eee;
        text-align: center;

        .back-link {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          color: #e4393c;
          text-decoration: none;
          font-size: 14px;
          transition: color 0.3s;

          &:hover {
            color: #c9302c;
            text-decoration: underline;
          }

          .el-icon {
            font-size: 16px;
          }
        }
      }

    }
  }
}
</style>

