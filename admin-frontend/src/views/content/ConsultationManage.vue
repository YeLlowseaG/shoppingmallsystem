<template>
  <div class="consultation-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>咨询管理</span>
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
        <el-form-item label="联系人">
          <el-input 
            v-model="searchForm.contactName" 
            placeholder="请输入联系人姓名" 
            clearable 
            style="width: 200px" 
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="待回复" :value="0" />
            <el-option label="已回复" :value="1" />
            <el-option label="已关闭" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 咨询列表 -->
      <el-table :data="consultationList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" width="200" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="consultationContent" label="咨询内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="咨询时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              size="small" 
              @click="handleReply(row)"
            >
              回复
            </el-button>
            <el-button 
              :type="row.status === 2 ? 'warning' : 'info'" 
              size="small" 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 2 ? '重新开启' : '关闭' }}
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
        @size-change="loadConsultationList"
        @current-change="loadConsultationList"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 咨询详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="咨询详情"
      width="800px"
    >
      <div v-if="selectedConsultation" class="consultation-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="咨询ID">{{ selectedConsultation.id }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ selectedConsultation.productName }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ selectedConsultation.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedConsultation.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ selectedConsultation.contactEmail }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedConsultation.status)" size="small">
              {{ selectedConsultation.statusText }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="咨询时间" :span="2">
            {{ formatTime(selectedConsultation.createdTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="咨询内容" :span="2">
            <div style="white-space: pre-wrap;">{{ selectedConsultation.consultationContent }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedConsultation.replyContent" label="回复内容" :span="2">
            <div style="white-space: pre-wrap;">{{ selectedConsultation.replyContent }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedConsultation.replyTime" label="回复时间" :span="2">
            {{ formatTime(selectedConsultation.replyTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复咨询"
      width="600px"
    >
      <el-form
        ref="replyFormRef"
        :model="replyForm"
        :rules="replyRules"
        label-width="120px"
      >
        <el-form-item label="咨询内容">
          <div style="background: #f5f5f5; padding: 10px; border-radius: 4px; white-space: pre-wrap;">
            {{ selectedConsultation?.consultationContent }}
          </div>
        </el-form-item>
        <el-form-item label="回复内容" prop="replyContent">
          <el-input
            v-model="replyForm.replyContent"
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
  getConsultationPage,
  getConsultationById,
  replyConsultation,
  updateConsultationStatus,
  deleteConsultation,
  type ConsultationVO
} from '@/api/admin/consultation'

// 搜索表单
const searchForm = ref({
  productName: '',
  contactName: '',
  status: undefined as number | undefined
})

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 咨询列表
const consultationList = ref<ConsultationVO[]>([])

// 对话框
const viewDialogVisible = ref(false)
const replyDialogVisible = ref(false)
const replyFormRef = ref<FormInstance>()

// 选中的咨询
const selectedConsultation = ref<ConsultationVO | null>(null)

// 回复表单
const replyForm = ref({
  replyContent: ''
})

// 回复表单验证规则
const replyRules: FormRules = {
  replyContent: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'info'
    default: return ''
  }
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

// 加载咨询列表
const loadConsultationList = async () => {
  try {
    const res = await getConsultationPage(
      pagination.value.current,
      pagination.value.size,
      searchForm.value.productName || undefined,
      searchForm.value.contactName || undefined,
      searchForm.value.status
    )
    consultationList.value = res.records
    pagination.value.total = res.total
  } catch (error) {
    ElMessage.error('加载咨询列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadConsultationList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    productName: '',
    contactName: '',
    status: undefined
  }
  handleSearch()
}

// 查看详情
const handleView = async (row: ConsultationVO) => {
  try {
    selectedConsultation.value = await getConsultationById(row.id)
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取咨询详情失败')
  }
}

// 回复咨询
const handleReply = (row: ConsultationVO) => {
  selectedConsultation.value = row
  replyForm.value.replyContent = ''
  replyDialogVisible.value = true
}

// 提交回复
const handleSubmitReply = async () => {
  if (!replyFormRef.value || !selectedConsultation.value) return

  await replyFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await replyConsultation(selectedConsultation.value!.id, replyForm.value)
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      loadConsultationList()
    } catch (error) {
      ElMessage.error('回复失败')
    }
  })
}

// 切换状态
const handleToggleStatus = async (row: ConsultationVO) => {
  const newStatus = row.status === 2 ? 1 : 2
  const statusText = newStatus === 2 ? '关闭' : '重新开启'
  
  try {
    await ElMessageBox.confirm(`确定要${statusText}该咨询吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await updateConsultationStatus(row.id, newStatus)
    ElMessage.success(`${statusText}成功`)
    loadConsultationList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${statusText}失败`)
    }
  }
}

// 删除咨询
const handleDelete = async (row: ConsultationVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该咨询吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteConsultation(row.id)
    ElMessage.success('删除成功')
    loadConsultationList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadConsultationList()
})
</script>

<style scoped lang="scss">
.consultation-manage {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .consultation-detail {
    .el-descriptions {
      margin-bottom: 20px;
    }
  }
}
</style>