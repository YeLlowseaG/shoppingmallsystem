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
          <MemberSidebar active-menu="deposit/balance" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <div class="deposit-wrapper">
              <!-- 页面标题 -->
              <div class="page-title">
                <span class="title-text">预存款交易记录 [预存款余额:</span>
                <span class="title-amount">¥{{ Number(depositBalance || 0).toFixed(2) }}</span>
                <span class="title-text"> (可用余额:</span>
                <span class="title-amount">¥{{ Number(availableBalance || 0).toFixed(2) }}</span>
                <span class="title-text">)]</span>
              </div>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <el-button type="primary" @click="handleDownloadRecord">下载交易记录</el-button>
              </div>

              <!-- 筛选区域 -->
              <div class="filter-section">
                <div class="filter-form">
                  <div class="filter-row">
                    <label class="filter-label">操作类型:</label>
                    <el-select
                      v-model="filterForm.operationType"
                      placeholder="所有"
                      class="filter-select"
                      style="width: 150px;"
                    >
                      <el-option label="所有" value="" />
                      <el-option label="预存款支付" value="deposit_payment" />
                      <el-option label="在线充值" value="online_recharge" />
                      <el-option label="预存款退款" value="deposit_refund" />
                      <el-option label="代充值" value="agent_recharge" />
                    </el-select>
                    <label class="filter-label">支付状态:</label>
                    <el-select
                      v-model="filterForm.status"
                      placeholder="所有"
                      class="filter-select"
                      style="width: 150px;"
                      clearable
                    >
                      <el-option label="所有" :value="undefined" />
                      <el-option label="待审核" :value="0" />
                      <el-option label="已通过" :value="1" />
                      <el-option label="支付失败/已拒绝" :value="2" />
                      <el-option label="支付中" :value="3" />
                      <el-option label="已超时" :value="4" />
                    </el-select>
                    <label class="filter-label">起始时间:</label>
                    <el-date-picker
                      v-model="filterForm.startDate"
                      type="date"
                      placeholder="选择开始日期"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      class="filter-date-picker"
                      style="width: 150px;"
                    />
                    <el-date-picker
                      v-model="filterForm.endDate"
                      type="date"
                      placeholder="选择结束日期"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      class="filter-date-picker"
                      style="width: 150px;"
                    />
                    <el-button type="primary" @click="handleSearch" class="search-btn">查询</el-button>
                  </div>
                </div>
              </div>

              <!-- 交易记录表格 -->
              <div class="table-wrapper">
                <table class="deposit-table">
                  <thead>
                    <tr>
                      <th width="50">
                        <el-checkbox v-model="selectAll" @change="handleSelectAll" />
                      </th>
                      <th width="150">事件</th>
                      <th width="100">支付状态</th>
                      <th width="120">存入金额</th>
                      <th width="120">支出金额</th>
                      <th width="120">冻结金额</th>
                      <th width="120">解冻金额</th>
                      <th width="120">当前余额</th>
                      <th width="120">可用余额</th>
                      <th width="150">时间</th>
                      <th>备注</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="record in displayedRecords" :key="record.id">
                      <td>
                        <el-checkbox
                          :model-value="selectedRecords.includes(record.id)"
                          @change="(val: boolean) => handleSelectRecord(record.id, val)"
                        />
                      </td>
                      <td>
                        <span
                          v-if="isClickableEvent(record.event)"
                          class="event-link"
                          @click="handleEventClick(record)"
                        >
                          {{ record.event }}
                        </span>
                        <span v-else>{{ record.event }}</span>
                      </td>
                      <td>
                        <el-tag
                          v-if="record.status !== undefined && record.statusName"
                          :type="getStatusTagType(record.status)"
                          size="small"
                        >
                          {{ record.statusName }}
                        </el-tag>
                        <span v-else>-</span>
                      </td>
                      <td class="amount-cell deposit-amount">
                        {{ (record.depositAmount && record.depositAmount > 0) ? `¥${Number(record.depositAmount).toFixed(2)}` : '-' }}
                      </td>
                      <td class="amount-cell expense-amount">
                        {{ (record.expenseAmount && record.expenseAmount > 0) ? `¥${Number(record.expenseAmount).toFixed(2)}` : '-' }}
                      </td>
                      <td class="amount-cell frozen-amount">
                        {{ (record.frozenAmount && record.frozenAmount > 0) ? `¥${Number(record.frozenAmount).toFixed(2)}` : '-' }}
                      </td>
                      <td class="amount-cell unfrozen-amount">
                        {{ (record.unfrozenAmount && record.unfrozenAmount > 0) ? `¥${Number(record.unfrozenAmount).toFixed(2)}` : '-' }}
                      </td>
                      <td class="amount-cell current-balance">
                        ¥{{ Number(record.currentBalance || 0).toFixed(2) }}
                      </td>
                      <td class="amount-cell available-balance">
                        ¥{{ Number(record.availableBalance || 0).toFixed(2) }}
                      </td>
                      <td>{{ formatDateTime(record.createTime) }}</td>
                      <td class="remark-cell">{{ record.remark || '-' }}</td>
                    </tr>
                    <tr v-if="displayedRecords.length === 0">
                      <td colspan="11" class="empty-data">暂无交易记录</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- 底部操作栏 -->
              <div class="bottom-actions">
                <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
                <el-button @click="handleExportSelected" :disabled="selectedRecords.length === 0">
                  导出选中记录
                </el-button>
              </div>

              <!-- 分页 -->
              <div class="pagination-wrapper">
                <el-pagination
                  v-model:current-page="pagination.currentPage"
                  v-model:page-size="pagination.pageSize"
                  :total="pagination.total"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="prev, pager, next, jumper"
                  @current-change="handlePageChange"
                  @size-change="handleSizeChange"
                />
                <div class="pagination-info">
                  <span>到第</span>
                  <el-input
                    v-model="pageInput"
                    type="number"
                    :min="1"
                    :max="totalPages"
                    class="page-input"
                    @keyup.enter="handleGoToPage"
                  />
                  <span>页</span>
                  <el-button type="primary" size="small" @click="handleGoToPage" class="go-page-btn">确定</el-button>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { getDepositBalance, type DepositRecordVO } from '@/api/buyer/deposit'
import { formatDateTime } from '@/utils'

const router = useRouter()

const unreadMessageCount = ref(0)
const loading = ref(false)

// 预存款余额
const depositBalance = ref(0)
const availableBalance = ref(0)

// 筛选表单
const filterForm = reactive({
  operationType: '',
  status: undefined as number | undefined,
  startDate: '',
  endDate: ''
})

// 交易记录列表
const recordList = ref<DepositRecordVO[]>([])

// 选中记录
const selectAll = ref(false)
const selectedRecords = ref<number[]>([])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const pageInput = ref('')

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(pagination.total / pagination.pageSize) || 1
})

// 搜索交易记录
const handleSearch = async () => {
  pagination.currentPage = 1
  await loadRecords()
}

// 加载交易记录
const loadRecords = async () => {
  loading.value = true
  try {
    // 调用真实API接口
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      operationType: filterForm.operationType || undefined,
      status: filterForm.status,
      startDate: filterForm.startDate || undefined,
      endDate: filterForm.endDate || undefined
    }
    const response = await getDepositBalance(params)
    
    recordList.value = response.records || []
    pagination.total = response.total || 0
    depositBalance.value = response.depositBalance || 0
    availableBalance.value = response.availableBalance || 0

    // 重置选中状态
    selectedRecords.value = []
    selectAll.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '加载交易记录失败')
    recordList.value = []
    pagination.total = 0
    depositBalance.value = 0
    availableBalance.value = 0
  } finally {
    loading.value = false
  }
}

// 全选/取消全选
const handleSelectAll = (val: boolean) => {
  if (val) {
    selectedRecords.value = displayedRecords.value.map(record => record.id)
  } else {
    selectedRecords.value = []
  }
}

// 选择单个记录
const handleSelectRecord = (recordId: number, selected: boolean) => {
  if (selected) {
    if (!selectedRecords.value.includes(recordId)) {
      selectedRecords.value.push(recordId)
    }
  } else {
    selectedRecords.value = selectedRecords.value.filter(id => id !== recordId)
  }
  // 更新全选状态
  selectAll.value = selectedRecords.value.length === displayedRecords.value.length && displayedRecords.value.length > 0
}

// 下载交易记录
const handleDownloadRecord = () => {
  // TODO: 实现下载交易记录功能
  ElMessage.info('下载交易记录功能待实现')
}

// 导出选中记录
const handleExportSelected = () => {
  if (selectedRecords.value.length === 0) {
    ElMessage.warning('请选择要导出的记录')
    return
  }
  // TODO: 实现导出选中记录功能
  ElMessage.info('导出选中记录功能待实现')
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.currentPage = page
  pageInput.value = String(page)
  loadRecords()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  pageInput.value = '1'
  loadRecords()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 跳转到指定页
const handleGoToPage = () => {
  const page = parseInt(pageInput.value)
  if (isNaN(page) || page < 1 || page > totalPages.value) {
    ElMessage.warning(`请输入1-${totalPages.value}之间的页码`)
    return
  }
  pagination.currentPage = page
  loadRecords()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 当前页显示的记录列表（后端已分页，直接使用）
const displayedRecords = computed(() => {
  return recordList.value
})

// 判断事件是否可点击（预存款支付、预存款退款）
const isClickableEvent = (event: string): boolean => {
  return event === '预存款支付' || event === '预存款退款'
}

// 从备注中提取订单号（备用方案）
const extractOrderNumber = (remark: string): string | null => {
  if (!remark) return null
  // 匹配格式：订单号{20231127161917} 或 订单号(20231127161917)
  const match = remark.match(/订单号[{(](\d+)[})]/)
  return match ? match[1] : null
}

// 处理事件点击
const handleEventClick = (record: DepositRecordVO) => {
  if (!isClickableEvent(record.event)) return
  
  // 优先使用 orderNo 字段，如果没有则从备注中提取
  let orderNumber = record.orderNo
  if (!orderNumber && record.remark) {
    orderNumber = extractOrderNumber(record.remark)
  }
  
  if (!orderNumber) {
    ElMessage.warning('无法获取订单号')
    return
  }
  
  // 跳转到订单详情页面
  router.push({
    path: '/order/detail',
    query: {
      orderNumber: orderNumber
    }
  })
}

import { formatDateTime } from '@/utils'

// 获取状态标签类型
const getStatusTagType = (status?: number) => {
  if (status === undefined) return ''
  switch (status) {
    case 0:
      return 'warning' // 待审核
    case 1:
      return 'success' // 已通过
    case 2:
      return 'danger' // 支付失败/已拒绝
    case 3:
      return 'info' // 支付中
    case 4:
      return 'info' // 已超时
    default:
      return ''
  }
}

// 初始化
onMounted(() => {
  loadRecords()
  pageInput.value = '1'
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

      .deposit-wrapper {
        .page-title {
          font-size: 16px;
          margin-bottom: 15px;
          padding-bottom: 10px;
          border-bottom: 1px solid #e5e5e5;
          color: #999; // 默认灰色

          .title-text {
            color: #999 !important;
            font-weight: normal;
          }

          .title-amount {
            color: #e4393c !important;
            font-weight: bold;
          }
        }

        // 操作按钮
        .action-buttons {
          margin-bottom: 15px;

          :deep(.el-button) {
            background: #e4393c;
            border-color: #e4393c;

            &:hover {
              background: #c9302c;
              border-color: #c9302c;
            }
          }
        }

        // 筛选区域
        .filter-section {
          margin-bottom: 20px;

          .filter-form {
            background: #f9f9f9;
            padding: 15px;
            border: 1px solid #e5e5e5;

            .filter-row {
              display: flex;
              align-items: center;
              gap: 10px;
              flex-wrap: wrap;

              .filter-label {
                font-size: 14px;
                color: #333;
                white-space: nowrap;
              }

              .filter-select {
                width: 150px;
              }

              .filter-date-picker {
                width: 150px;
              }

              .search-btn {
                background: #e4393c;
                border-color: #e4393c;
                margin-left: 10px;

                &:hover {
                  background: #c9302c;
                  border-color: #c9302c;
                }
              }
            }
          }
        }

        // 交易记录表格
        .table-wrapper {
          overflow-x: auto;
          margin-bottom: 15px;

          .deposit-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;

            thead {
              background: #f5f5f5;

              th {
                padding: 12px 8px;
                text-align: left;
                font-weight: normal;
                color: #333;
                border: 1px solid #e5e5e5;
              }
            }

            tbody {
              tr {
                border-bottom: 1px solid #e5e5e5;

                &:hover {
                  background: #f9f9f9;
                }

                td {
                  padding: 12px 8px;
                  vertical-align: middle;
                  border: 1px solid #e5e5e5;

                  .event-link {
                    color: #0066cc;
                    cursor: pointer;
                    text-decoration: none;

                    &:hover {
                      color: #e4393c;
                      text-decoration: underline;
                    }
                  }

                  .amount-cell {
                    text-align: right;
                    font-family: 'Courier New', monospace;

                    &.deposit-amount {
                      color: #52c41a;
                    }

                    &.expense-amount {
                      color: #e4393c;
                    }

                    &.frozen-amount {
                      color: #ff9900;
                    }

                    &.unfrozen-amount {
                      color: #0066cc;
                    }

                    &.current-balance {
                      color: #333;
                      font-weight: bold;
                    }

                    &.available-balance {
                      color: #333;
                      font-weight: bold;
                    }
                  }

                  .remark-cell {
                    max-width: 300px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                  }

                  &.empty-data {
                    text-align: center;
                    color: #999;
                    padding: 40px;
                  }
                }
              }
            }
          }
        }

        // 底部操作栏
        .bottom-actions {
          display: flex;
          align-items: center;
          gap: 15px;
          padding: 15px 0;
          border-top: 1px solid #e5e5e5;
          margin-bottom: 20px;

          :deep(.el-checkbox) {
            .el-checkbox__label {
              font-size: 14px;
              color: #333;
            }
          }

          :deep(.el-button) {
            padding: 8px 20px;
            font-size: 14px;
          }
        }

        // 分页
        .pagination-wrapper {
          display: flex;
          align-items: center;
          justify-content: space-between;
          flex-wrap: wrap;
          gap: 15px;

          :deep(.el-pagination) {
            .el-pagination__total {
              margin-right: 10px;
            }
          }

          .pagination-info {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: #333;

            .page-input {
              width: 60px;

              :deep(.el-input__inner) {
                text-align: center;
                padding: 0 5px;
              }
            }

            .go-page-btn {
              background: #e4393c;
              border-color: #e4393c;
              padding: 5px 15px;

              &:hover {
                background: #c9302c;
                border-color: #c9302c;
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

      .member-main-content {
        .deposit-wrapper {
          .filter-section {
            .filter-form {
              .filter-row {
                flex-direction: column;
                align-items: flex-start;

                .filter-select,
                .filter-date-picker {
                  width: 100%;
                }
              }
            }
          }

          .table-wrapper {
            .deposit-table {
              font-size: 12px;

              thead th,
              tbody td {
                padding: 8px 4px;
              }
            }
          }

          .pagination-wrapper {
            flex-direction: column;
            align-items: flex-start;
          }
        }
      }
    }
  }
}
</style>

