<template>
  <div class="basic-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>基础配置管理</span>
          <el-button type="primary" @click="handleAdd">添加配置</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="配置键/名称">
          <el-input
            v-model="searchForm.configKey"
            placeholder="请输入配置键或配置名称"
            clearable
            style="width: 250px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 配置列表 -->
      <el-table :data="configList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="configName" label="配置名称" width="150" />
        <el-table-column prop="configKey" label="配置键" width="200" />
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div v-if="row.configType === 'image'" class="image-preview">
              <el-image
                v-if="row.configValue"
                :src="row.configValue"
                style="width: 60px; height: 30px"
                fit="cover"
                :preview-src-list="[row.configValue]"
              />
              <span v-else>-</span>
            </div>
            <span v-else>{{ row.configValue || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="configType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.configType)" size="small">
              {{ getTypeText(row.configType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
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
        @size-change="loadConfigList"
        @current-change="loadConfigList"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formData.id ? '编辑配置' : '添加配置'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="formData.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置键" prop="configKey">
          <el-input 
            v-model="formData.configKey" 
            placeholder="请输入配置键，如：site.logo"
            :disabled="!!formData.id"
          />
        </el-form-item>
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="formData.configType" placeholder="请选择配置类型" style="width: 100%">
            <el-option label="文本" value="text" />
            <el-option label="多行文本" value="textarea" />
            <el-option label="图片" value="image" />
            <el-option label="数字" value="number" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <!-- 文本输入 -->
          <el-input
            v-if="formData.configType === 'text'"
            v-model="formData.configValue"
            placeholder="请输入配置值"
          />
          <!-- 多行文本输入 -->
          <el-input
            v-else-if="formData.configType === 'textarea'"
            v-model="formData.configValue"
            type="textarea"
            :rows="3"
            placeholder="请输入配置值"
          />
          <!-- 数字输入 -->
          <el-input-number
            v-else-if="formData.configType === 'number'"
            v-model="formData.configValue"
            style="width: 100%"
          />
          <!-- 图片上传 -->
          <div v-else-if="formData.configType === 'image'" class="upload-wrapper">
            <!-- 图片预览 -->
            <div v-if="formData.configValue" class="image-preview">
              <el-image
                :src="formData.configValue"
                fit="contain"
                style="width: 200px; height: 100px"
                :preview-src-list="[formData.configValue]"
              />
              <el-button
                type="danger"
                size="small"
                circle
                :icon="Delete"
                class="delete-btn"
                @click="formData.configValue = ''"
              />
            </div>
            <!-- 上传按钮 -->
            <el-upload
              v-else
              class="image-uploader"
              action="/api/common/upload/image"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeImageUpload"
              accept="image/*"
            >
              <el-button type="primary" :icon="Upload">点击上传图片</el-button>
            </el-upload>
            <!-- URL输入框 -->
            <div class="url-input">
              <el-input
                v-model="formData.configValue"
                placeholder="或直接输入图片URL"
                clearable
              />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="配置描述">
          <el-input
            v-model="formData.configDesc"
            type="textarea"
            :rows="2"
            placeholder="请输入配置描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" />
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
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Upload, Delete } from '@element-plus/icons-vue'
import {
  getSystemConfigPage,
  createSystemConfig,
  updateSystemConfig,
  deleteSystemConfig,
  updateSystemConfigStatus,
  type SystemConfig
} from '@/api/admin/systemConfig'

// 搜索表单
const searchForm = ref({
  configKey: ''
})

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 配置列表
const configList = ref<SystemConfig[]>([])

// 对话框
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = ref<SystemConfig>({
  configKey: '',
  configValue: '',
  configName: '',
  configDesc: '',
  configType: 'text',
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const formRules: FormRules = {
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

// 获取类型标签类型
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    text: '',
    textarea: 'success',
    image: 'warning',
    number: 'info'
  }
  return typeMap[type] || ''
}

// 获取类型文本
const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    text: '文本',
    textarea: '多行',
    image: '图片',
    number: '数字'
  }
  return typeMap[type] || '未知'
}

// 加载配置列表
const loadConfigList = async () => {
  try {
    const res = await getSystemConfigPage(
      pagination.value.current,
      pagination.value.size,
      searchForm.value.configKey || undefined
    )
    configList.value = res.records
    pagination.value.total = res.total
  } catch (error) {
    ElMessage.error('加载配置列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadConfigList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    configKey: ''
  }
  handleSearch()
}

// 添加配置
const handleAdd = () => {
  formData.value = {
    configKey: '',
    configValue: '',
    configName: '',
    configDesc: '',
    configType: 'text',
    sortOrder: 0,
    status: 1
  }
  dialogVisible.value = true
}

// 编辑配置
const handleEdit = (row: SystemConfig) => {
  formData.value = { ...row }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (formData.value.id) {
        await updateSystemConfig(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createSystemConfig(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadConfigList()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

// 切换状态
const handleToggleStatus = async (row: SystemConfig) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateSystemConfigStatus(row.id!, newStatus)
    ElMessage.success('状态更新成功')
    loadConfigList()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 删除配置
const handleDelete = async (row: SystemConfig) => {
  try {
    await ElMessageBox.confirm('确定要删除该配置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteSystemConfig(row.id!)
    ElMessage.success('删除成功')
    loadConfigList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 上传前的验证
const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

// 上传成功回调
const handleUploadSuccess = (response: any) => {
  if (response.code === 200 && response.data) {
    formData.value.configValue = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// 上传失败回调
const handleUploadError = (error: any) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
}

// 初始化
onMounted(() => {
  loadConfigList()
})
</script>

<style scoped lang="scss">
.basic-config {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .image-preview {
    position: relative;
    display: inline-block;
    margin-bottom: 10px;

    .delete-btn {
      position: absolute;
      top: 5px;
      right: 5px;
    }
  }

  .upload-wrapper {
    .image-uploader {
      margin-bottom: 10px;
    }

    .url-input {
      margin-top: 10px;
    }
  }
}
</style>