<template>
  <div class="help-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>帮助中心管理</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 分类管理 -->
        <el-tab-pane label="分类管理" name="category">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="handleAddCategory">
                <el-icon><Plus /></el-icon>
                新增分类
              </el-button>
            </div>

            <!-- 分类树 -->
            <el-table :data="categoryList" v-loading="categoryLoading" border row-key="id" :tree-props="{ children: 'children' }">
              <el-table-column prop="name" label="分类名称" min-width="200" />
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
              <el-table-column label="操作" width="250" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleEditCategory(row)">编辑</el-button>
                  <el-button type="success" link @click="handleCategoryStatusChange(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteCategory(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 文章管理 -->
        <el-tab-pane label="文章管理" name="article">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="handleAddArticle">
                <el-icon><Plus /></el-icon>
                新增文章
              </el-button>
            </div>

            <!-- 搜索栏 -->
            <el-form :inline="true" :model="articleSearchForm" class="search-form">
              <el-form-item label="分类">
                <el-select v-model="articleSearchForm.categoryId" placeholder="请选择分类" clearable style="width: 200px">
                  <el-option
                    v-for="category in flatCategories"
                    :key="category.id"
                    :label="category.name"
                    :value="category.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="标题">
                <el-input v-model="articleSearchForm.title" placeholder="请输入标题" clearable style="width: 200px" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="articleSearchForm.status" placeholder="请选择状态" clearable style="width: 150px">
                  <el-option label="全部" :value="undefined" />
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadArticleList">查询</el-button>
                <el-button @click="resetArticleSearch">重置</el-button>
              </el-form-item>
            </el-form>

            <!-- 文章列表 -->
            <el-table :data="articleList" v-loading="articleLoading" border>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="categoryName" label="分类" width="150" />
              <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
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
              <el-table-column label="操作" width="250" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleEditArticle(row)">编辑</el-button>
                  <el-button type="success" link @click="handleArticleStatusChange(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteArticle(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="articlePagination.pageNum"
                v-model:page-size="articlePagination.pageSize"
                :total="articlePagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleArticleSizeChange"
                @current-change="handleArticlePageChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 分类编辑对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="categoryDialogTitle"
      width="600px"
      @close="resetCategoryForm"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryFormData"
        :rules="categoryFormRules"
        label-width="100px"
      >
        <el-form-item label="父分类" prop="parentId">
          <el-tree-select
            v-model="categoryFormData.parentId"
            :data="categoryTreeOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择父分类（不选则为顶级分类）"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryFormData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryFormData.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryFormData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCategorySubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 文章编辑对话框 -->
    <el-dialog
      v-model="articleDialogVisible"
      :title="articleDialogTitle"
      width="1000px"
      @close="resetArticleForm"
    >
      <el-form
        ref="articleFormRef"
        :model="articleFormData"
        :rules="articleFormRules"
        label-width="100px"
      >
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="articleFormData.categoryId" placeholder="请选择分类" clearable style="width: 100%">
            <el-option
              v-for="category in flatCategories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="articleFormData.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <div class="editor-wrapper">
            <QuillEditor
              :key="editorKey"
              v-model:content="articleFormData.content"
              contentType="html"
              :options="editorOptions"
              style="height: 400px"
            />
          </div>
          <div class="form-tip">提示：支持富文本编辑，可以直接插入图片、设置文本格式等。图片可以通过工具栏的图片按钮插入。</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="articleFormData.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="articleFormData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="articleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleArticleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { formatDateTime } from '@/utils'
import {
  getHelpCategoryTree,
  getHelpCategoryById,
  createHelpCategory,
  updateHelpCategory,
  deleteHelpCategory,
  updateHelpCategoryStatus,
  getHelpArticlePage,
  getHelpArticleById,
  createHelpArticle,
  updateHelpArticle,
  deleteHelpArticle,
  updateHelpArticleStatus,
  type HelpCategoryVO,
  type HelpArticleVO,
  type HelpCategoryDTO,
  type HelpArticleDTO
} from '@/api/admin/help'

const activeTab = ref('category')

// 分类管理
const categoryLoading = ref(false)
const categoryList = ref<HelpCategoryVO[]>([])
const categoryDialogVisible = ref(false)
const categoryDialogTitle = ref('新增分类')
const categoryFormRef = ref()
const categoryFormData = ref<HelpCategoryDTO>({
  parentId: 0,
  name: '',
  sort: 0,
  status: 1
})
const categoryFormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择父分类', trigger: 'change' }]
}

// 文章管理
const articleLoading = ref(false)
const articleList = ref<HelpArticleVO[]>([])
const articleSearchForm = ref({
  categoryId: undefined as number | undefined,
  title: '',
  status: undefined as number | undefined
})
const articlePagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const articleDialogVisible = ref(false)
const articleDialogTitle = ref('新增文章')
const articleFormRef = ref()
const articleFormData = ref<HelpArticleDTO>({
  categoryId: undefined,
  title: '',
  content: '',
  images: [],
  sort: 0,
  status: 1
})
const articleFormRules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

// 编辑器 key，用于强制重新渲染以清空内容
const editorKey = ref(0)

// 扁平化分类列表（用于下拉选择）
const flatCategories = computed(() => {
  const flatten = (categories: HelpCategoryVO[]): HelpCategoryVO[] => {
    const result: HelpCategoryVO[] = []
    categories.forEach(category => {
      result.push(category)
      if (category.children && category.children.length > 0) {
        result.push(...flatten(category.children))
      }
    })
    return result
  }
  return flatten(categoryList.value)
})

// 分类树选项（用于树形选择器）
const categoryTreeOptions = computed(() => {
  return categoryList.value
})

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

// Tab切换
const handleTabChange = (tab: string) => {
  if (tab === 'category') {
    loadCategoryList()
  } else if (tab === 'article') {
    loadArticleList()
  }
}

// ==================== 分类管理 ====================

// 加载分类列表
const loadCategoryList = async () => {
  categoryLoading.value = true
  try {
    categoryList.value = await getHelpCategoryTree()
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  } finally {
    categoryLoading.value = false
  }
}

// 新增分类
const handleAddCategory = () => {
  categoryDialogTitle.value = '新增分类'
  categoryFormData.value = {
    parentId: 0,
    name: '',
    sort: 0,
    status: 1
  }
  categoryDialogVisible.value = true
}

// 编辑分类
const handleEditCategory = async (row: HelpCategoryVO) => {
  categoryDialogTitle.value = '编辑分类'
  try {
    const category = await getHelpCategoryById(row.id)
    categoryFormData.value = {
      id: category.id,
      parentId: category.parentId,
      name: category.name,
      sort: category.sort,
      status: category.status
    }
    categoryDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取分类信息失败')
  }
}

// 删除分类
const handleDeleteCategory = async (row: HelpCategoryVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      type: 'warning'
    })
    await deleteHelpCategory(row.id)
    ElMessage.success('删除成功')
    loadCategoryList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 更新分类状态
const handleCategoryStatusChange = async (row: HelpCategoryVO) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateHelpCategoryStatus(row.id, newStatus)
    ElMessage.success('更新成功')
    loadCategoryList()
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  }
}

// 提交分类表单
const handleCategorySubmit = async () => {
  if (!categoryFormRef.value) return
  await categoryFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (categoryFormData.value.id) {
          await updateHelpCategory(categoryFormData.value.id, categoryFormData.value)
          ElMessage.success('更新成功')
        } else {
          await createHelpCategory(categoryFormData.value)
          ElMessage.success('创建成功')
        }
        categoryDialogVisible.value = false
        loadCategoryList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

// 重置分类表单
const resetCategoryForm = () => {
  categoryFormRef.value?.resetFields()
}

// ==================== 文章管理 ====================

// 加载文章列表
const loadArticleList = async () => {
  articleLoading.value = true
  try {
    const response = await getHelpArticlePage(
      articlePagination.value.pageNum,
      articlePagination.value.pageSize,
      articleSearchForm.value.categoryId,
      articleSearchForm.value.title,
      articleSearchForm.value.status
    )
    articleList.value = response.records
    articlePagination.value.total = response.total
  } catch (error) {
    ElMessage.error('加载文章列表失败')
  } finally {
    articleLoading.value = false
  }
}

// 重置搜索
const resetArticleSearch = () => {
  articleSearchForm.value = {
    categoryId: undefined,
    title: '',
    status: undefined
  }
  loadArticleList()
}

// 分页变化
const handleArticlePageChange = () => {
  loadArticleList()
}

const handleArticleSizeChange = () => {
  articlePagination.value.pageNum = 1
  loadArticleList()
}

// 新增文章
const handleAddArticle = () => {
  articleDialogTitle.value = '新增文章'
  articleFormData.value = {
    categoryId: undefined,
    title: '',
    content: '',
    images: [],
    sort: 0,
    status: 1
  }
  // 强制重新渲染编辑器以清空内容
  editorKey.value++
  articleDialogVisible.value = true
}

// 编辑文章
const handleEditArticle = async (row: HelpArticleVO) => {
  articleDialogTitle.value = '编辑文章'
  try {
    const article = await getHelpArticleById(row.id)
    articleFormData.value = {
      id: article.id,
      categoryId: article.categoryId,
      title: article.title,
      content: article.content,
      images: article.images || [],
      sort: article.sort,
      status: article.status
    }
    articleDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取文章信息失败')
  }
}

// 删除文章
const handleDeleteArticle = async (row: HelpArticleVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该文章吗？', '提示', {
      type: 'warning'
    })
    await deleteHelpArticle(row.id)
    ElMessage.success('删除成功')
    loadArticleList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 更新文章状态
const handleArticleStatusChange = async (row: HelpArticleVO) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateHelpArticleStatus(row.id, newStatus)
    ElMessage.success('更新成功')
    loadArticleList()
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  }
}

// 添加图片
const addImage = () => {
  if (!articleFormData.value.images) {
    articleFormData.value.images = []
  }
  articleFormData.value.images.push('')
}

// 删除图片
const removeImage = (index: number) => {
  if (articleFormData.value.images) {
    articleFormData.value.images.splice(index, 1)
  }
}

// 提交文章表单
const handleArticleSubmit = async () => {
  if (!articleFormRef.value) return
  await articleFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 过滤空图片URL
        if (articleFormData.value.images) {
          articleFormData.value.images = articleFormData.value.images.filter(img => img.trim() !== '')
        }
        
        if (articleFormData.value.id) {
          await updateHelpArticle(articleFormData.value.id, articleFormData.value)
          ElMessage.success('更新成功')
        } else {
          await createHelpArticle(articleFormData.value)
          ElMessage.success('创建成功')
        }
        articleDialogVisible.value = false
        loadArticleList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

// 重置文章表单
const resetArticleForm = () => {
  articleFormRef.value?.resetFields()
  // 清空表单数据
  articleFormData.value = {
    categoryId: undefined,
    title: '',
    content: '',
    images: [],
    sort: 0,
    status: 1
  }
  // 强制重新渲染编辑器以清空内容
  editorKey.value++
}

// 初始化
onMounted(() => {
  loadCategoryList()
  loadArticleList()
})
</script>

<style scoped lang="scss">
.help-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .tab-content {
    .toolbar {
      margin-bottom: 20px;
    }

    .search-form {
      margin-bottom: 20px;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
  }

  .image-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
  }

  .editor-wrapper {
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;

    :deep(.ql-container) {
      min-height: 350px;
      font-size: 14px;
    }

    :deep(.ql-editor) {
      min-height: 350px;
    }
  }
}
</style>

