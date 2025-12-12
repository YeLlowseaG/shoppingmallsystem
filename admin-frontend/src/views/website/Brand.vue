<template>
  <div class="brand-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>品牌管理</span>
          <el-button type="primary" @click="handleAdd">添加品牌</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="品牌名称">
          <el-input v-model="searchForm.brandName" placeholder="请输入品牌名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 品牌列表 -->
      <el-table :data="brandList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="logoUrl" label="LOGO" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.logoUrl"
              :src="row.logoUrl"
              style="width: 80px; height: 80px"
              fit="contain"
              :preview-src-list="[row.logoUrl]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="brandName" label="品牌名称" width="150" />
        <el-table-column prop="description" label="品牌描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
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
        @size-change="loadBrandList"
        @current-change="loadBrandList"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formData.id ? '编辑品牌' : '添加品牌'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="品牌名称" prop="brandName">
          <el-input v-model="formData.brandName" placeholder="请输入品牌名称" />
        </el-form-item>
        <el-form-item label="LOGO" prop="logoUrl">
          <div class="upload-wrapper">
            <!-- 图片要求说明 -->
            <div class="image-requirements">
              <el-alert
                title="图片要求：建议尺寸200x80像素，格式JPG/PNG，大小不超过2MB"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 10px;"
              />
            </div>
            <!-- 图片预览 -->
            <div v-if="formData.logoUrl" class="image-preview">
              <el-image
                :src="formData.logoUrl"
                fit="contain"
                style="width: 200px; height: 80px; border: 1px solid #eee"
                :preview-src-list="[formData.logoUrl]"
              />
              <el-button
                type="danger"
                size="small"
                circle
                :icon="Delete"
                class="delete-btn"
                @click="formData.logoUrl = ''"
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
              <el-button type="primary" :icon="Upload">点击上传LOGO</el-button>
            </el-upload>
            <!-- URL输入框 -->
            <div class="url-input">
              <el-input
                v-model="formData.logoUrl"
                placeholder="或直接输入LOGO URL"
                clearable
              />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="品牌描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入品牌描述"
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
  getBrandPage,
  createBrand,
  updateBrand,
  deleteBrand,
  updateBrandStatus,
  type Brand
} from '@/api/admin/website'

// 搜索表单
const searchForm = ref({
  brandName: '',
  status: undefined as number | undefined
})

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 品牌列表
const brandList = ref<Brand[]>([])

// 对话框
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = ref<Brand>({
  brandName: '',
  logoUrl: '',
  description: '',
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const formRules: FormRules = {
  brandName: [{ required: true, message: '请输入品牌名称', trigger: 'blur' }],
  logoUrl: [{ required: true, message: '请输入LOGO URL', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

// 加载品牌列表
const loadBrandList = async () => {
  try {
    const res = await getBrandPage(
      pagination.value.current,
      pagination.value.size,
      searchForm.value.brandName || undefined,
      searchForm.value.status
    )
    brandList.value = res.records
    pagination.value.total = res.total
  } catch (error) {
    ElMessage.error('加载品牌列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadBrandList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    brandName: '',
    status: undefined
  }
  handleSearch()
}

// 添加品牌
const handleAdd = () => {
  formData.value = {
    brandName: '',
    logoUrl: '',
    description: '',
    sortOrder: 0,
    status: 1
  }
  dialogVisible.value = true
}

// 编辑品牌
const handleEdit = (row: Brand) => {
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
        await updateBrand(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createBrand(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadBrandList()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

// 切换状态
const handleToggleStatus = async (row: Brand) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateBrandStatus(row.id!, newStatus)
    ElMessage.success('状态更新成功')
    loadBrandList()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 删除品牌
const handleDelete = async (row: Brand) => {
  try {
    await ElMessageBox.confirm('确定要删除该品牌吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteBrand(row.id!)
    ElMessage.success('删除成功')
    loadBrandList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 上传前的验证
const beforeImageUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/jpg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJpgOrPng) {
    ElMessage.error('只能上传JPG或PNG格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  return true
}

// 上传成功回调
const handleUploadSuccess = (response: any) => {
  if (response.code === 200 && response.data) {
    formData.value.logoUrl = response.data.url
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
  loadBrandList()
})
</script>

<style scoped lang="scss">
.brand-manage {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .upload-wrapper {
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

    .image-uploader {
      margin-bottom: 10px;
    }

    .url-input {
      margin-top: 10px;
    }
  }
}
</style>
