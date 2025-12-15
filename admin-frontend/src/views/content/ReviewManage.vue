<template>
  <div class="review-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评价管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input 
            v-model="searchForm.productName" 
            placeholder="请输入商品名称" 
            clearable 
            style="width: 200px" 
          />
        </el-form-item>
        <el-form-item label="用户名称">
          <el-input
            v-model="searchForm.userName"
            placeholder="请输入用户名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
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

      <!-- 评价列表 -->
      <el-table :data="reviewList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productImage" label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.productImage"
              :src="row.productImage"
              style="width: 60px; height: 60px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" width="200" show-overflow-tooltip />
        <el-table-column prop="userName" label="用户名称" width="120" />
        <el-table-column prop="reviewContent" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              size="small" 
              @click="handleAudit(row, 1)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 0" 
              type="danger" 
              size="small" 
              @click="handleAudit(row, 2)"
            >
              拒绝
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleReply(row)"
            >
              回复
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 评价详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="评价详情"
      width="800px"
    >
      <div v-if="selectedReview" class="review-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="评价ID">{{ selectedReview.id }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ selectedReview.productName }}</el-descriptions-item>
          <el-descriptions-item label="用户名称">{{ selectedReview.userName }}</el-descriptions-item>
          <el-descriptions-item label="订单编号">{{ selectedReview.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedReview.status)" size="small">
              {{ selectedReview.statusText }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="评价时间" :span="2">
            {{ formatTime(selectedReview.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="评价内容" :span="2">
            <div style="white-space: pre-wrap;">{{ selectedReview.reviewContent }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedReview.reviewImages?.length" label="评价图片" :span="2">
            <div class="review-images">
              <el-image
                v-for="(img, index) in selectedReview.reviewImages"
                :key="index"
                :src="img"
                style="width: 100px; height: 100px; margin-right: 10px"
                fit="cover"
                :preview-src-list="selectedReview.reviewImages"
              />
            </div>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedReview.adminReply" label="管理员回复" :span="2">
            <div style="white-space: pre-wrap;">{{ selectedReview.adminReply }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复评价"
      width="600px"
    >
      <el-form
        ref="replyFormRef"
        :model="replyForm"
        :rules="replyRules"
        label-width="120px"
      >
        <el-form-item label="评价内容">
          <div style="background: #f5f5f5; padding: 10px; border-radius: 4px; white-space: pre-wrap;">
            {{ selectedReview?.reviewContent }}
          </div>
        </el-form-item>
        <el-form-item label="回复内容" prop="adminReply">
          <el-input
            v-model="replyForm.adminReply"
            type="textarea"
            :rows="6"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReply">确定回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getReviewPage,
  getReviewById,
  auditReview,
  replyReview,
  deleteReview,
  type ProductReviewVO
} from '@/api/admin/review'

// 搜索表单
const searchForm = ref({
  productName: '',
  userName: '',
  status: undefined as number | undefined
})

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 评价列表
const reviewList = ref<ProductReviewVO[]>([])

// 对话框
const viewDialogVisible = ref(false)
const replyDialogVisible = ref(false)
const replyFormRef = ref<FormInstance>()

// 选中的评价
const selectedReview = ref<ProductReviewVO | null>(null)

// 回复表单
const replyForm = ref({
  adminReply: ''
})

// 回复表单验证规则
const replyRules: FormRules = {
  adminReply: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return ''
  }
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

// 加载评价列表
const loadReviewList = async () => {
  try {
    const res = await getReviewPage(
      pagination.value.current,
      pagination.value.size,
      searchForm.value.productName || undefined,
      searchForm.value.userName || undefined,
      undefined,
      searchForm.value.status
    )
    reviewList.value = res.records
    pagination.value.total = res.total
  } catch (error) {
    ElMessage.error('加载评价列表失败')
  }
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
    userName: '',
    status: undefined
  }
  handleSearch()
}

// 查看详情
const handleView = async (row: ProductReviewVO) => {
  try {
    selectedReview.value = await getReviewById(row.id)
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取评价详情失败')
  }
}

// 审核评价
const handleAudit = async (row: ProductReviewVO, status: number) => {
  const statusText = status === 1 ? '通过' : '拒绝'
  
  try {
    await ElMessageBox.confirm(`确定要${statusText}该评价吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await auditReview(row.id, { status })
    ElMessage.success(`${statusText}成功`)
    loadReviewList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${statusText}失败`)
    }
  }
}

// 回复评价
const handleReply = (row: ProductReviewVO) => {
  selectedReview.value = row
  replyForm.value.adminReply = row.adminReply || ''
  replyDialogVisible.value = true
}

// 提交回复
const handleSubmitReply = async () => {
  if (!replyFormRef.value || !selectedReview.value) return

  await replyFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await replyReview(selectedReview.value!.id, replyForm.value)
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      loadReviewList()
    } catch (error) {
      ElMessage.error('回复失败')
    }
  })
}

// 删除评价
const handleDelete = async (row: ProductReviewVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteReview(row.id)
    ElMessage.success('删除成功')
    loadReviewList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadReviewList()
})
</script>

<style scoped lang="scss">
.review-manage {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .review-detail {
    .el-descriptions {
      margin-bottom: 20px;
    }

    .review-images {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }
  }
}
</style>