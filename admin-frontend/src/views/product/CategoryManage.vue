<template>
  <div class="category-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品分类管理</span>
          <el-button type="primary" @click="handleAdd(null)">添加一级分类</el-button>
        </div>
      </template>

      <!-- 分类树形表格 -->
      <el-table
        :data="categoryTree"
        row-key="id"
        border
        :tree-props="{ children: 'children' }"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="categoryName" label="分类名称" min-width="200" />
        <el-table-column prop="level" label="级别" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.level === 1" type="primary">一级</el-tag>
            <el-tag v-else-if="row.level === 2" type="success">二级</el-tag>
            <el-tag v-else type="info">三级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.level < 3"
              type="primary"
              size="small"
              @click="handleAdd(row)"
            >
              添加子分类
            </el-button>
            <el-button type="warning" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="formData.categoryName" placeholder="请输入分类名称" />
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
import {
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory,
  updateCategoryStatus,
  type ProductCategoryDTO,
  type ProductCategoryVO
} from '@/api/admin/productCategory'

// 分类树数据
const categoryTree = ref<ProductCategoryVO[]>([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加分类')
const formRef = ref<FormInstance>()

// 表单数据
const formData = ref<ProductCategoryDTO>({
  parentId: 0,
  categoryName: '',
  level: 1,
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const formRules: FormRules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
}

// 加载分类树
const loadCategoryTree = async () => {
  try {
    categoryTree.value = await getCategoryTree()
  } catch (error) {
    ElMessage.error('加载分类数据失败')
  }
}

// 添加分类
const handleAdd = (parent: ProductCategoryVO | null) => {
  dialogTitle.value = parent ? `添加子分类（父分类：${parent.categoryName}）` : '添加一级分类'
  formData.value = {
    parentId: parent ? parent.id : 0,
    categoryName: '',
    level: parent ? parent.level + 1 : 1,
    sortOrder: 0,
    status: 1
  }
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row: ProductCategoryVO) => {
  dialogTitle.value = '编辑分类'
  formData.value = {
    id: row.id,
    parentId: row.parentId,
    categoryName: row.categoryName,
    level: row.level,
    sortOrder: row.sortOrder,
    status: row.status
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (formData.value.id) {
        await updateCategory(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createCategory(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadCategoryTree()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

// 删除分类
const handleDelete = async (row: ProductCategoryVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    loadCategoryTree()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 更新状态
const handleStatusChange = async (row: ProductCategoryVO) => {
  try {
    await updateCategoryStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 初始化
onMounted(() => {
  loadCategoryTree()
})
</script>

<style scoped lang="scss">
.category-manage {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
