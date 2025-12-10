<template>
  <div class="announcement-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
        </div>
      </template>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增公告
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="全部" :value="undefined" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 公告列表 -->
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="publishDate" label="发布日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.publishDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleStatusChange(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadList"
        @current-change="loadList"
      />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1000px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <div class="editor-wrapper">
            <QuillEditor
              :key="editorKey"
              v-model:content="formData.content"
              contentType="html"
              :options="editorOptions"
              style="height: 400px"
            />
          </div>
          <div class="form-tip">提示：支持富文本编辑，可以直接插入图片、设置文本格式等。图片可以通过工具栏的图片按钮插入。</div>
        </el-form-item>
        <el-form-item label="发布日期" prop="publishDate">
          <el-date-picker
            v-model="formData.publishDate"
            type="date"
            placeholder="请选择发布日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" style="width: 100%" />
          <div class="form-tip">提示：数字越小越靠前，相同发布日期时按排序字段升序排列</div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { formatDateTime, formatDate } from '@/utils'
import {
  getAnnouncementPage,
  getAnnouncementById,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  updateAnnouncementStatus,
  type AnnouncementVO,
  type AnnouncementDTO
} from '@/api/admin/announcement'

const loading = ref(false)
const list = ref<AnnouncementVO[]>([])
const searchForm = ref({
  title: '',
  status: undefined as number | undefined
})
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增公告')
const formRef = ref()
const formData = ref<AnnouncementDTO>({
  title: '',
  content: '',
  images: [],
  publishDate: '',
  sort: 0,
  status: 1
})
const formRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  publishDate: [{ required: true, message: '请选择发布日期', trigger: 'change' }]
}

// 编辑器 key，用于强制重新渲染以清空内容
const editorKey = ref(0)

// 富文本编辑器配置
const editorOptions = {
  theme: 'snow',
  modules: {
    toolbar: [
      [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
      ['bold', 'italic', 'underline', 'strike'],
      [{ 'color': [] }, { 'background': [] }],
      [{ 'list': 'ordered'}, { 'list': 'bullet' }],
      [{ 'align': [] }],
      ['link', 'image'],
      ['clean']
    ]
  },
  placeholder: ''
}

// 加载列表
const loadList = async () => {
  loading.value = true
  try {
    const response = await getAnnouncementPage(
      pagination.value.pageNum,
      pagination.value.pageSize,
      searchForm.value.title || undefined,
      searchForm.value.status
    )
    list.value = response.records || []
    pagination.value.total = response.total || 0
  } catch (error) {
    ElMessage.error('加载公告列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.value.title = ''
  searchForm.value.status = undefined
  pagination.value.pageNum = 1
  loadList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增公告'
  dialogVisible.value = true
  resetForm()
}

// 编辑
const handleEdit = async (row: AnnouncementVO) => {
  dialogTitle.value = '编辑公告'
  dialogVisible.value = true
  
  try {
    const announcement = await getAnnouncementById(row.id)
    formData.value = {
      id: announcement.id,
      title: announcement.title,
      content: announcement.content,
      images: announcement.images || [],
      publishDate: announcement.publishDate,
      sort: announcement.sort,
      status: announcement.status
    }
    // 强制重新渲染编辑器以加载内容
    editorKey.value++
  } catch (error) {
    ElMessage.error('加载公告详情失败')
  }
}

// 删除
const handleDelete = async (row: AnnouncementVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      type: 'warning'
    })
    await deleteAnnouncement(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 状态切换
const handleStatusChange = async (row: AnnouncementVO) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateAnnouncementStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    loadList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (formData.value.id) {
      // 更新
      await updateAnnouncement(formData.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      // 新增
      await createAnnouncement(formData.value)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    loadList()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    title: '',
    content: '',
    images: [],
    publishDate: '',
    sort: 0,
    status: 1
  }
  editorKey.value++
  formRef.value?.clearValidate()
}

// 初始化
onMounted(() => {
  loadList()
})
</script>

<style scoped lang="scss">
.announcement-management {
  .card-header {
    font-size: 18px;
    font-weight: bold;
  }

  .toolbar {
    margin-bottom: 20px;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }

  .editor-wrapper {
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;

    :deep(.ql-container) {
      min-height: 350px;
    }
  }

  .form-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }
}
</style>

