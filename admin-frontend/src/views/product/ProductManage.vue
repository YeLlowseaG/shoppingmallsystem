<template>
  <div class="product-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <el-button type="primary" @click="router.push('/admin/product/add')">添加商品</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable style="width: 200px">
            <el-option
              v-for="category in flatCategories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="商品名称/编码" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="上架" value="上架" />
            <el-option label="下架" value="下架" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 商品列表 -->
      <el-table :data="productList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="mainImage" label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.mainImage"
              :src="row.mainImage"
              style="width: 60px; height: 60px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="basePrice" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ parseFloat(row.basePrice).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="salesCount" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '上架' ? 'success' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === '上架' ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === '上架' ? '下架' : '上架' }}
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
        @size-change="loadProductList"
        @current-change="loadProductList"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑商品"
      width="800px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="商品编码" prop="productCode">
          <el-input v-model="formData.productCode" placeholder="请输入商品编码/SKU" />
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="formData.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in flatCategories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品品牌" prop="brandId">
          <el-select
            v-model="formData.brandId"
            placeholder="请选择商品品牌（可选）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="brand in brandOptions"
              :key="brand.id"
              :label="brand.brandName"
              :value="brand.id"
            />
          </el-select>
        </el-form-item>

        <!-- 价格与库存 -->
        <el-divider content-position="left">价格与库存</el-divider>

        <div class="price-stock-grid">
          <el-form-item label="初始会员价" prop="basePrice" required>
            <el-input-number
              v-model="formData.basePrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="建议零售价" prop="marketPrice">
            <el-input-number
              v-model="formData.marketPrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="市场零售价" prop="costPrice">
            <el-input-number
              v-model="formData.costPrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="商品库存" prop="stock" required>
            <el-input-number
              v-model="formData.stock"
              :min="0"
              :step="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="警戒库存" prop="warningStock">
            <el-input-number
              v-model="formData.warningStock"
              :min="0"
              :step="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="商品重量(g)" prop="weight">
            <el-input-number
              v-model="formData.weight"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <el-form-item label="主图" prop="mainImage">
          <div class="upload-wrapper">
            <!-- 主图预览 -->
            <div v-if="formData.mainImage" class="image-preview">
              <el-image
                :src="formData.mainImage"
                fit="contain"
                style="width: 150px; height: 150px"
                :preview-src-list="[formData.mainImage]"
              />
              <el-button
                type="danger"
                size="small"
                circle
                :icon="Delete"
                class="delete-btn"
                @click="formData.mainImage = ''"
              />
            </div>
            <!-- 上传按钮 -->
            <el-upload
              v-else
              class="image-uploader"
              action="/api/common/upload/image"
              :show-file-list="false"
              :on-success="handleMainImageSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeImageUpload"
              accept="image/*"
            >
              <div class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">上传主图</div>
              </div>
            </el-upload>
            <!-- URL输入框 -->
            <div class="url-input">
              <el-input
                v-model="formData.mainImage"
                placeholder="或直接输入主图URL"
                clearable
              />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="详情轮播图" prop="images">
          <div class="detail-images-wrapper">
            <el-upload
              v-model:file-list="detailImageList"
              action="/api/common/upload/image"
              list-type="picture-card"
              :on-success="handleDetailImageSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeImageUpload"
              :on-remove="handleDetailImageRemove"
              accept="image/*"
              multiple
              :limit="5"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">最多上传5张轮播图，将在详情页顶部轮播展示</div>
          </div>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <RichTextEditor
            v-model="formData.description"
            placeholder="请输入商品详细描述"
            height="500px"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio label="上架">上架</el-radio>
            <el-radio label="下架">下架</el-radio>
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadFile, type UploadUserFile } from 'element-plus'
import { Plus, Delete, Upload } from '@element-plus/icons-vue'
import {
  getProductPage,
  updateProduct,
  deleteProduct,
  updateProductStatus,
  type ProductDTO,
  type ProductVO
} from '@/api/admin/product'
import { getCategoryTree, type ProductCategoryVO } from '@/api/admin/productCategory'
import { getBrandOptions } from '@/api/admin/brand'
import RichTextEditor from '@/components/common/RichTextEditor.vue'

const router = useRouter()

// 搜索表单
const searchForm = ref({
  categoryId: undefined as number | undefined,
  keyword: '',
  status: ''
})

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 商品列表
const productList = ref<ProductVO[]>([])

// 分类列表
const categoryTree = ref<ProductCategoryVO[]>([])

// 品牌列表
const brandOptions = ref<any[]>([])

// 扁平化分类列表（用于下拉选择）
const flatCategories = computed(() => {
  const flatten = (categories: ProductCategoryVO[], level = 0): ProductCategoryVO[] => {
    let result: ProductCategoryVO[] = []
    categories.forEach(category => {
      result.push(category)
      if (category.children && category.children.length > 0) {
        result = result.concat(flatten(category.children, level + 1))
      }
    })
    return result
  }
  return flatten(categoryTree.value)
})

// 对话框
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

// 详情图片列表
const detailImageList = ref<UploadUserFile[]>([])

// 表单数据
const formData = ref<ProductDTO>({
  productCode: '',
  productName: '',
  categoryId: 0,
  brandId: null,
  basePrice: 0,
  marketPrice: 0,
  costPrice: 0,
  stock: 0,
  warningStock: 10,
  weight: 0,
  mainImage: '',
  images: '',
  description: '',
  status: '下架'
})

// 表单验证规则
const formRules: FormRules = {
  productCode: [
    { required: true, message: '请输入商品编码', trigger: 'blur' }
  ],
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  basePrice: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' }
  ]
}

// 加载分类树
const loadCategoryTree = async () => {
  try {
    categoryTree.value = await getCategoryTree()
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

// 加载品牌列表
const loadBrands = async () => {
  try {
    const response = await getBrandOptions()
    brandOptions.value = response || []
  } catch (error) {
    // 静默处理，不显示错误
    brandOptions.value = []
  }
}

// 加载商品列表
const loadProductList = async () => {
  try {
    const res = await getProductPage(
      pagination.value.current,
      pagination.value.size,
      searchForm.value.categoryId,
      searchForm.value.keyword,
      searchForm.value.status
    )
    productList.value = res.records
    pagination.value.total = res.total
  } catch (error) {
    ElMessage.error('加载商品列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadProductList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    categoryId: undefined,
    keyword: '',
    status: ''
  }
  handleSearch()
}

// 图片上传前的校验
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

// 主图上传成功
const handleMainImageSuccess = (response: any) => {
  if (response.code === 200 && response.data) {
    formData.value.mainImage = response.data.url
    ElMessage.success('主图上传成功')
  } else {
    ElMessage.error('主图上传失败')
  }
}

// 详情图上传成功
const handleDetailImageSuccess = (response: any, file: UploadFile) => {
  if (response.code === 200 && response.data) {
    file.url = response.data.url
    ElMessage.success('详情图上传成功')
  } else {
    ElMessage.error('详情图上传失败')
  }
}

// 详情图移除
const handleDetailImageRemove = (file: UploadFile) => {
  const index = detailImageList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    detailImageList.value.splice(index, 1)
  }
}

// 图片上传失败
const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

// 编辑商品
const handleEdit = (row: ProductVO) => {
  formData.value = {
    id: row.id,
    productCode: row.productCode,
    productName: row.productName,
    categoryId: row.categoryId,
    brandId: row.brandId || null,
    basePrice: row.basePrice,
    marketPrice: row.marketPrice || 0,
    costPrice: row.costPrice || 0,
    stock: row.stock,
    warningStock: row.warningStock || 10,
    weight: row.weight || 0,
    mainImage: row.mainImage,
    images: JSON.stringify(row.imageList),
    description: row.description,
    status: row.status
  }

  // 初始化详情图片列表
  if (row.imageList && row.imageList.length > 0) {
    detailImageList.value = row.imageList.map((url, index) => ({
      name: `image-${index}`,
      url: url
    }))
  } else {
    detailImageList.value = []
  }

  dialogVisible.value = true
}

// 提交表单（只用于编辑）
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      // 提取详情图片URL列表
      const detailImages = detailImageList.value
        .map(file => file.url || (file.response as any)?.data?.url)
        .filter(url => url)

      // 更新formData的images字段
      formData.value.images = JSON.stringify(detailImages)

      await updateProduct(formData.value)
      ElMessage.success('更新成功')
      dialogVisible.value = false
      loadProductList()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

// 切换状态
const handleToggleStatus = async (row: ProductVO) => {
  const newStatus = row.status === '上架' ? '下架' : '上架'
  try {
    await updateProductStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    loadProductList()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 删除商品
const handleDelete = async (row: ProductVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    loadProductList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadCategoryTree()
  loadBrands()
  loadProductList()
})
</script>

<style scoped lang="scss">
.product-manage {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }
}

.price-stock-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0 20px;
  margin-bottom: 20px;

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

// 图片上传相关样式
.upload-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .image-preview {
    position: relative;
    display: inline-block;

    .delete-btn {
      position: absolute;
      top: 5px;
      right: 5px;
    }
  }

  .upload-placeholder {
    width: 150px;
    height: 150px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }

    .upload-icon {
      font-size: 28px;
      color: #8c939d;
      margin-bottom: 8px;
    }

    .upload-text {
      font-size: 14px;
      color: #606266;
    }
  }

  .url-input {
    width: 100%;
    max-width: 500px;
  }
}

.detail-images-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .upload-tip {
    font-size: 12px;
    color: #909399;
  }
}
</style>
