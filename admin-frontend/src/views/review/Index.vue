<template>
  <div class="review-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评论管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="searchForm.rating" placeholder="请选择评分" clearable style="width: 120px">
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 评论列表 -->
      <el-table :data="reviewList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userName" label="评价用户" width="120" />
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <el-rate :model-value="row.rating" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="reviewContent" label="评价内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="status" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="评价时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              size="small" 
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 0" 
              type="danger" 
              size="small" 
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
            <el-button 
              v-if="row.status !== 0" 
              type="warning" 
              size="small" 
              @click="handleReply(row)"
            >
              回复
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadReviewList"
        @current-change="loadReviewList"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 查看评论对话框 -->
    <el-dialog v-model="viewDialogVisible" title="评论详情" width="600px">
      <div v-if="currentReview">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="商品名称">{{ currentReview.productName }}</el-descriptions-item>
          <el-descriptions-item label="评价用户">{{ currentReview.userName }}</el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate :model-value="currentReview.rating" disabled />
          </el-descriptions-item>
          <el-descriptions-item label="评价内容">
            <div style="white-space: pre-wrap;">{{ currentReview.reviewContent }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="评价图片" v-if="currentReview.reviewImages && currentReview.reviewImages.length > 0">
            <div style="display: flex; gap: 10px; flex-wrap: wrap;">
              <el-image 
                v-for="(img, index) in currentReview.reviewImages" 
                :key="index"
                :src="img" 
                style="width: 100px; height: 100px;"
                fit="cover"
                :preview-src-list="currentReview.reviewImages"
                :initial-index="index"
              />
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="管理员回复" v-if="currentReview.adminReply">
            <div style="white-space: pre-wrap;">{{ currentReview.adminReply }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag v-if="currentReview.status === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="currentReview.status === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="currentReview.status === 2" type="danger">已拒绝</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="评价时间">{{ currentReview.createdTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复评论" width="500px">
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.adminReply"
            type="textarea"
            :rows="5"
            placeholder="请输入回复内容..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="replyLoading" @click="handleSubmitReply">提交回复</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单
const searchForm = ref({
  productName: '',
  rating: undefined,
  status: undefined
})

// 分页参数
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 评论列表
const reviewList = ref([])

// 对话框状态
const viewDialogVisible = ref(false)
const replyDialogVisible = ref(false)
const currentReview = ref(null)
const replyLoading = ref(false)

// 回复表单
const replyForm = ref({
  adminReply: ''
})

// 模拟数据
const mockReviews = [
  {
    id: 1,
    productName: '杜蕾斯避孕套经典装',
    userName: 'user001',
    rating: 5,
    reviewContent: '质量很好，包装完好，物流快速，会继续购买',
    reviewImages: [],
    adminReply: '',
    status: 0,
    createdTime: '2025-12-13 10:30:00'
  },
  {
    id: 2,
    productName: '润滑剂水溶性',
    userName: 'user002',
    rating: 4,
    reviewContent: '效果不错，使用感受良好，性价比高',
    reviewImages: [],
    adminReply: '感谢您的评价，我们会继续提供优质的产品和服务',
    status: 1,
    createdTime: '2025-12-12 15:20:00'
  },
  {
    id: 3,
    productName: '成人玩具清洁液',
    userName: 'user003',
    rating: 3,
    reviewContent: '一般般，没有想象中的好用',
    reviewImages: [],
    adminReply: '',
    status: 2,
    createdTime: '2025-12-11 09:15:00'
  }
]

// 加载评论列表
const loadReviewList = () => {
  // 模拟API调用
  setTimeout(() => {
    reviewList.value = mockReviews
    pagination.value.total = mockReviews.length
  }, 300)
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadReviewList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    productName: '',
    rating: undefined,
    status: undefined
  }
  handleSearch()
}

// 查看评论
const handleView = (row: any) => {
  currentReview.value = row
  viewDialogVisible.value = true
}

// 通过审核
const handleApprove = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认通过该评论吗？', '提示', {
      type: 'warning'
    })
    
    // 模拟API调用
    row.status = 1
    ElMessage.success('审核通过成功')
  } catch {
    // 用户取消
  }
}

// 拒绝审核
const handleReject = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认拒绝该评论吗？', '提示', {
      type: 'warning'
    })
    
    // 模拟API调用
    row.status = 2
    ElMessage.success('审核拒绝成功')
  } catch {
    // 用户取消
  }
}

// 回复评论
const handleReply = (row: any) => {
  currentReview.value = row
  replyForm.value.adminReply = row.adminReply || ''
  replyDialogVisible.value = true
}

// 提交回复
const handleSubmitReply = async () => {
  if (!replyForm.value.adminReply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  replyLoading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (currentReview.value) {
      currentReview.value.adminReply = replyForm.value.adminReply
    }
    
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
  } catch (error) {
    ElMessage.error('回复失败，请重试')
  } finally {
    replyLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadReviewList()
})
</script>

<style scoped>
.review-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>