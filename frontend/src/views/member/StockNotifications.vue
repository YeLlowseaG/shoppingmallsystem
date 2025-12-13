<template>
  <div class="stock-notifications">
    <!-- Header -->
    <MemberHeaderBar />
    
    <!-- Main Content -->
    <div class="container">
      <!-- Sidebar -->
      <MemberSidebar active-menu="favorites/out-of-stock" />
      
      <!-- Content Area -->
      <div class="content-area">
        <div class="page-header">
          <h2>缺货登记</h2>
          <p class="page-desc">查看您的缺货登记记录，商品补货时我们会及时通知您</p>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          <p>加载中...</p>
        </div>

        <!-- No Data -->
        <div v-else-if="!loading && notifications.length === 0" class="empty-state">
          <el-icon :size="60"><DocumentRemove /></el-icon>
          <h3>暂无缺货登记记录</h3>
          <p>您还没有登记过缺货商品，当您遇到缺货商品时可以登记获得补货通知</p>
          <el-button type="primary" @click="$router.push('/')">
            去逛逛
          </el-button>
        </div>

        <!-- Notifications List -->
        <div v-else class="notifications-list">
          <div 
            v-for="notification in notifications" 
            :key="notification.id"
            class="notification-item"
          >
            <div class="product-info">
              <img :src="notification.mainImage" :alt="notification.productName" class="product-image" />
              <div class="product-details">
                <h4 class="product-name">{{ notification.productName }}</h4>
                <p class="product-code">商品编码：{{ notification.productCode }}</p>
                <p class="product-price">¥{{ parseFloat(notification.basePrice).toFixed(2) }}</p>
              </div>
            </div>
            
            <div class="notification-info">
              <div class="info-row">
                <span class="label">登记时间：</span>
                <span class="value">{{ formatDate(notification.createTime) }}</span>
              </div>
              <div class="info-row">
                <span class="label">联系方式：</span>
                <span class="value">{{ notification.contactPhone }}</span>
                <span v-if="notification.contactEmail" class="value">{{ notification.contactEmail }}</span>
              </div>
              <div class="info-row">
                <span class="label">通知方式：</span>
                <span class="value">{{ getNotifyTypeText(notification.notifyType) }}</span>
              </div>
              <div v-if="notification.remark" class="info-row">
                <span class="label">备注：</span>
                <span class="value">{{ notification.remark }}</span>
              </div>
              <div v-if="notification.notifiedAt" class="info-row">
                <span class="label">通知时间：</span>
                <span class="value">{{ formatDate(notification.notifiedAt) }}</span>
              </div>
            </div>
            
            <div class="notification-actions">
              <div class="status-tag" :class="getStatusClass(notification.status)">
                {{ notification.statusDesc }}
              </div>
              <div class="action-buttons">
                <el-button 
                  size="small" 
                  @click="viewProduct(notification.productId)"
                >
                  查看商品
                </el-button>
                <el-button 
                  v-if="notification.status === 0"
                  size="small" 
                  type="danger"
                  @click="cancelNotification(notification.id)"
                  :loading="cancelingIds.includes(notification.id)"
                >
                  取消登记
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="pagination.total > 0" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, DocumentRemove } from '@element-plus/icons-vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { 
  getStockNotificationList, 
  cancelStockNotification,
  type StockNotificationVO 
} from '@/api/buyer/stock-notification'

const router = useRouter()

// 数据状态
const loading = ref(true)
const notifications = ref<StockNotificationVO[]>([])
const cancelingIds = ref<number[]>([])

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 加载缺货登记列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const response = await getStockNotificationList(pagination.value.current, pagination.value.size)
    notifications.value = response.records
    pagination.value.total = response.total
  } catch (error) {
    console.error('加载缺货登记列表失败:', error)
    ElMessage.error('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

// 取消缺货登记
const cancelNotification = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要取消此缺货登记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    cancelingIds.value.push(id)
    await cancelStockNotification(id)
    ElMessage.success('取消登记成功')
    
    // 重新加载列表
    await loadNotifications()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消登记失败:', error)
      ElMessage.error('取消失败，请重试')
    }
  } finally {
    cancelingIds.value = cancelingIds.value.filter(cancelId => cancelId !== id)
  }
}

// 查看商品
const viewProduct = (productId: number) => {
  router.push(`/products/${productId}`)
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取通知方式文本
const getNotifyTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'email': '邮箱通知',
    'sms': '短信通知',
    'both': '邮箱+短信'
  }
  return typeMap[type] || type
}

// 获取状态样式类
const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    0: 'status-pending',
    1: 'status-notified',
    2: 'status-cancelled'
  }
  return classMap[status] || ''
}

// 分页大小变化
const handleSizeChange = (newSize: number) => {
  pagination.value.size = newSize
  pagination.value.current = 1
  loadNotifications()
}

// 当前页变化
const handleCurrentChange = (newPage: number) => {
  pagination.value.current = newPage
  loadNotifications()
}

// 初始化
onMounted(() => {
  loadNotifications()
})
</script>

<style scoped lang="scss">
.stock-notifications {
  background: #f5f5f5;
  min-height: 100vh;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    display: flex;
    gap: 20px;
  }

  .content-area {
    flex: 1;
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
  }

  .page-header {
    padding: 24px;
    border-bottom: 1px solid #f0f0f0;

    h2 {
      font-size: 20px;
      color: #333;
      margin: 0 0 8px 0;
    }

    .page-desc {
      color: #666;
      margin: 0;
      font-size: 14px;
    }
  }

  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    color: #999;

    .el-icon {
      margin-bottom: 16px;
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    color: #999;

    .el-icon {
      margin-bottom: 16px;
      color: #ddd;
    }

    h3 {
      margin: 0 0 12px 0;
      color: #666;
      font-size: 16px;
    }

    p {
      margin: 0 0 24px 0;
      font-size: 14px;
      text-align: center;
      line-height: 1.6;
    }
  }

  .notifications-list {
    padding: 0;

    .notification-item {
      display: flex;
      align-items: flex-start;
      gap: 20px;
      padding: 20px 24px;
      border-bottom: 1px solid #f0f0f0;
      transition: background-color 0.2s;

      &:hover {
        background-color: #fafafa;
      }

      &:last-child {
        border-bottom: none;
      }

      .product-info {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        flex: 0 0 280px;

        .product-image {
          width: 80px;
          height: 80px;
          object-fit: cover;
          border: 1px solid #eee;
          border-radius: 4px;
          flex-shrink: 0;
        }

        .product-details {
          flex: 1;

          .product-name {
            font-size: 14px;
            color: #333;
            margin: 0 0 6px 0;
            line-height: 1.4;
            font-weight: 500;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
            overflow: hidden;
          }

          .product-code {
            font-size: 12px;
            color: #999;
            margin: 0 0 6px 0;
          }

          .product-price {
            font-size: 16px;
            color: #e4393c;
            font-weight: bold;
            margin: 0;
          }
        }
      }

      .notification-info {
        flex: 1;
        min-width: 0;

        .info-row {
          display: flex;
          align-items: flex-start;
          margin-bottom: 6px;
          font-size: 13px;
          line-height: 1.4;

          &:last-child {
            margin-bottom: 0;
          }

          .label {
            color: #999;
            flex: 0 0 80px;
          }

          .value {
            color: #333;
            word-break: break-all;
            margin-right: 8px;
          }
        }
      }

      .notification-actions {
        flex: 0 0 120px;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 12px;

        .status-tag {
          padding: 4px 12px;
          border-radius: 4px;
          font-size: 12px;
          font-weight: 500;

          &.status-pending {
            background: #e6f7ff;
            color: #1890ff;
            border: 1px solid #d0ebff;
          }

          &.status-notified {
            background: #f6ffed;
            color: #52c41a;
            border: 1px solid #b7eb8f;
          }

          &.status-cancelled {
            background: #fff2f0;
            color: #ff4d4f;
            border: 1px solid #ffccc7;
          }
        }

        .action-buttons {
          display: flex;
          flex-direction: column;
          gap: 8px;

          .el-button {
            padding: 4px 12px;
            height: auto;
            line-height: 1.4;
            min-width: 80px;
          }
        }
      }
    }
  }

  .pagination-wrapper {
    padding: 24px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: center;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .stock-notifications {
    .container {
      flex-direction: column;
      padding: 10px;
    }

    .notifications-list {
      .notification-item {
        flex-direction: column;
        gap: 16px;

        .product-info {
          flex: 1;
          width: 100%;
        }

        .notification-info {
          width: 100%;
        }

        .notification-actions {
          flex-direction: row;
          justify-content: space-between;
          align-items: center;
          width: 100%;

          .action-buttons {
            flex-direction: row;
          }
        }
      }
    }
  }
}
</style>