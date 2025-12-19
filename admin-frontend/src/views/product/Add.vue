<template>
  <div class="product-add-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品发布</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="productForm"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="productForm.productName" placeholder="请输入商品名称" />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader
            v-model="productForm.categoryId"
            :options="categoryOptions"
            :props="cascaderProps"
            placeholder="请选择商品分类"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="商品编码" prop="productCode">
          <el-input v-model="productForm.productCode" placeholder="请输入商品编码" />
        </el-form-item>

        <el-form-item label="商品品牌" prop="brandId">
          <el-select
            v-model="productForm.brandId"
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
          <el-form-item label="基础价" prop="basePrice" required>
            <el-input-number
              v-model="productForm.basePrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="建议零售价" prop="suggestedRetailPrice">
            <el-input-number
              v-model="productForm.suggestedRetailPrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="市场零售价" prop="marketRetailPrice">
            <el-input-number
              v-model="productForm.marketRetailPrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="商品库存" prop="stock" required>
            <el-input-number
              v-model="productForm.stock"
              :min="0"
              :step="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="警戒库存" prop="warningStock">
            <el-input-number
              v-model="productForm.warningStock"
              :min="0"
              :step="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="商品重量(g)" prop="weight">
            <el-input-number
              v-model="productForm.weight"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <!-- 会员价设置（单独一行） -->
        <div class="member-price-row">
          <el-form-item label="启用会员价">
            <el-switch v-model="productForm.enableMemberPrice" :active-value="1" :inactive-value="0" />
            <span class="form-tip" style="margin-left: 10px;">启用后以会员价作为售价，否则以基础价作为售价</span>
          </el-form-item>
          <el-form-item label="会员价" prop="memberPrice" v-if="productForm.enableMemberPrice === 1">
            <el-input-number
              v-model="productForm.memberPrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 200px"
            />
          </el-form-item>
        </div>

        <!-- 商品规格配置 -->
        <el-divider content-position="left">商品规格配置</el-divider>
        
        <el-form-item label="是否启用规格" prop="enableSpec">
          <el-switch v-model="productForm.enableSpec" @change="handleEnableSpecChange" />
          <div class="form-tip">启用后可为商品配置不同规格的SKU（如颜色、尺寸等）</div>
        </el-form-item>

        <!-- 规格配置区域 -->
        <div v-if="productForm.enableSpec" class="spec-config-area">
          <!-- 规格属性配置 -->
          <div class="spec-section">
            <div class="spec-section-title"><span class="required-star">*</span> 规格属性</div>
            <div class="spec-keys-wrapper">
              <div 
                v-for="(specKey, keyIndex) in specKeys" 
                :key="keyIndex"
                class="spec-key-item"
              >
                <div class="spec-key-header">
                  <el-input 
                    v-model="specKey.specName" 
                    placeholder="请输入规格名称（如：颜色、尺寸）"
                    style="width: 200px"
                  />
                  <el-button 
                    type="danger" 
                    size="small" 
                    :icon="Delete" 
                    @click="removeSpecKey(keyIndex)"
                    :disabled="specKeys.length <= 1"
                  >
                    删除规格
                  </el-button>
                </div>
                
                <div class="spec-values-wrapper">
                  <div class="spec-values-header">规格值：</div>
                  <div class="spec-values-list">
                    <div 
                      v-for="(specValue, valueIndex) in specKey.values" 
                      :key="valueIndex"
                      class="spec-value-item"
                    >
                      <el-input 
                        v-model="specValue.specValue" 
                        placeholder="规格值"
                        style="width: 150px"
                      />
                      <el-button 
                        type="danger" 
                        size="small" 
                        :icon="Delete" 
                        @click="removeSpecValue(keyIndex, valueIndex)"
                        :disabled="specKey.values.length <= 1"
                      />
                    </div>
                    <el-button 
                      type="primary" 
                      size="small" 
                      :icon="Plus" 
                      @click="addSpecValue(keyIndex)"
                    >
                      添加规格值
                    </el-button>
                  </div>
                </div>
              </div>
              
              <el-button
                type="primary"
                :icon="Plus"
                @click="addSpecKey"
                style="margin-top: 20px;"
              >
                添加规格属性
              </el-button>
            </div>
          </div>

          <!-- SKU列表 -->
          <div class="spec-section">
            <div class="spec-section-title"><span class="required-star">*</span> SKU列表</div>
            <div class="sku-list-wrapper">
              <div class="sku-list-header">
                <el-button 
                  type="primary" 
                  @click="generateSkuList"
                  :disabled="!canGenerateSkus"
                >
                  生成SKU
                </el-button>
                <span class="tip">根据规格属性自动生成SKU组合</span>
              </div>
              
              <el-table
                v-if="skuList.length > 0"
                :data="skuList"
                border
                class="sku-table"
                style="width: 100%; min-width: 1200px;"
              >
                <el-table-column prop="specCombinationText" label="规格组合" min-width="120" align="left" />
                <el-table-column label="SKU编码" min-width="150">
                  <template #default="{ row, $index }">
                    <el-input
                      v-model="row.skuCode"
                      placeholder="SKU编码"
                      size="small"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="基础价" min-width="130">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.price"
                      :min="0"
                      :precision="2"
                      :step="0.01"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="建议零售价" min-width="130">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.suggestedRetailPrice"
                      :min="0"
                      :precision="2"
                      :step="0.01"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="市场零售价" min-width="130">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.marketRetailPrice"
                      :min="0"
                      :precision="2"
                      :step="0.01"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="启用会员价" min-width="100" align="center">
                  <template #default="{ row, $index }">
                    <el-switch
                      v-model="row.enableMemberPrice"
                      :active-value="1"
                      :inactive-value="0"
                      size="small"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="会员价" min-width="130">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.memberPrice"
                      :min="0"
                      :precision="2"
                      :step="0.01"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                      :disabled="row.enableMemberPrice !== 1"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="库存" min-width="120">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.stock"
                      :min="0"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="警戒库存" min-width="120">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.warningStock"
                      :min="0"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="重量(g)" min-width="120">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.weight"
                      :min="0"
                      :precision="2"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="70" fixed="right">
                  <template #default="{ row, $index }">
                    <el-button
                      type="danger"
                      size="small"
                      :icon="Delete"
                      @click="removeSku($index)"
                    />
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>

        <el-form-item label="主图" prop="mainImage">
          <div class="upload-wrapper">
            <!-- 主图预览 -->
            <div v-if="productForm.mainImage" class="image-preview">
              <el-image
                :src="productForm.mainImage"
                fit="contain"
                style="width: 150px; height: 150px"
                :preview-src-list="[productForm.mainImage]"
              />
              <el-button
                type="danger"
                size="small"
                circle
                :icon="Delete"
                class="delete-btn"
                @click="productForm.mainImage = ''"
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
                v-model="productForm.mainImage"
                placeholder="或直接输入主图URL"
                clearable
              />
            </div>
          </div>
        </el-form-item>

        <el-form-item label="详情轮播图" prop="detailImages">
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
            v-model="productForm.description"
            placeholder="请输入商品详细描述"
            height="500px"
          />
        </el-form-item>

        <el-form-item label="商品状态" prop="status">
          <el-radio-group v-model="productForm.status">
            <el-radio label="上架">上架</el-radio>
            <el-radio label="下架">下架</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile, UploadUserFile } from 'element-plus'
import { createProduct } from '@/api/admin/product'
import { getCategoryTree } from '@/api/admin/productCategory'
import { getBrandOptions } from '@/api/admin/brand'
import RichTextEditor from '@/components/common/RichTextEditor.vue'

const router = useRouter()
const formRef = ref<FormInstance>()

const productForm = ref({
  productName: '',
  categoryId: null as any,
  productCode: '',
  brandId: null as any,
  basePrice: 0,
  suggestedRetailPrice: 0,
  marketRetailPrice: 0,
  memberPrice: 0,
  enableMemberPrice: 0,
  stock: 0,
  warningStock: 10,
  weight: 0,
  description: '',
  mainImage: '',
  status: '下架',
  enableSpec: false
})

// 详情图列表
const detailImageList = ref<UploadUserFile[]>([])

// SKU规格相关数据
const specKeys = ref([
  {
    specName: '',
    values: [{ specValue: '' }]
  }
])

const skuList = ref<any[]>([])

// 计算属性：是否可以生成SKU
const canGenerateSkus = computed(() => {
  if (!productForm.value.enableSpec) return false
  return specKeys.value.every(key => 
    key.specName.trim() && 
    key.values.length > 0 && 
    key.values.every(value => value.specValue.trim())
  )
})

const formRules: FormRules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  productCode: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入基础价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

const categoryOptions = ref<any[]>([])
const brandOptions = ref<any[]>([])

const cascaderProps = {
  value: 'id',
  label: 'categoryName',
  children: 'children',
  checkStrictly: true
}

// 加载商品分类数据
const loadCategories = async () => {
  try {
    categoryOptions.value = await getCategoryTree()
  } catch (error) {
    ElMessage.error('加载分类数据失败')
  }
}

// 加载品牌数据
const loadBrands = async () => {
  try {
    const response = await getBrandOptions()
    brandOptions.value = response || []
  } catch (error) {
    // 静默处理，不显示错误
    brandOptions.value = []
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

// 主图上传成功回调
const handleMainImageSuccess = (response: any) => {
  if (response.code === 200 && response.data) {
    productForm.value.mainImage = response.data.url
    ElMessage.success('主图上传成功')
  } else {
    ElMessage.error(response.message || '主图上传失败')
  }
}

// 详情图上传成功回调
const handleDetailImageSuccess = (response: any, file: UploadFile) => {
  if (response.code === 200 && response.data) {
    // 更新file对象的url
    file.url = response.data.url
    ElMessage.success('详情图上传成功')
  } else {
    ElMessage.error(response.message || '详情图上传失败')
  }
}

// 详情图删除回调
const handleDetailImageRemove = (file: UploadFile) => {
  console.log('删除详情图:', file)
}

// 上传失败回调
const handleUploadError = (error: any) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 处理级联选择器的值（如果是数组，取最后一个值）
        const categoryId = Array.isArray(productForm.value.categoryId)
          ? productForm.value.categoryId[productForm.value.categoryId.length - 1]
          : productForm.value.categoryId

        // 获取详情图URL列表
        const detailImages = detailImageList.value
          .map(file => file.url || (file.response as any)?.data?.url)
          .filter(url => url)

        await createProduct({
          productName: productForm.value.productName,
          categoryId: categoryId,
          productCode: productForm.value.productCode,
          brandId: productForm.value.brandId,
          basePrice: productForm.value.basePrice,
          suggestedRetailPrice: productForm.value.suggestedRetailPrice,
          marketRetailPrice: productForm.value.marketRetailPrice,
          memberPrice: productForm.value.memberPrice,
          enableMemberPrice: productForm.value.enableMemberPrice,
          stock: productForm.value.stock,
          warningStock: productForm.value.warningStock,
          weight: productForm.value.weight,
          description: productForm.value.description,
          mainImage: productForm.value.mainImage,
          detailImages: detailImages.join(','), // 多张图片用逗号分隔
          status: productForm.value.status
        })
        ElMessage.success('商品发布成功！')
        router.push('/admin/product/list')
      } catch (error) {
        ElMessage.error('商品发布失败')
      }
    } else {
      ElMessage.error('请填写完整的商品信息')
    }
  })
}

const handleReset = () => {
  formRef.value?.resetFields()
  detailImageList.value = []
}

const handleCancel = () => {
  router.push('/admin/product/list')
}

// SKU规格管理方法
const handleEnableSpecChange = (value: boolean) => {
  if (!value) {
    // 禁用规格时清空数据
    skuList.value = []
    specKeys.value = [{ specName: '', values: [{ specValue: '' }] }]
  }
}

const addSpecKey = () => {
  specKeys.value.push({
    specName: '',
    values: [{ specValue: '' }]
  })
}

const removeSpecKey = (index: number) => {
  if (specKeys.value.length > 1) {
    specKeys.value.splice(index, 1)
    // 重新生成SKU
    if (skuList.value.length > 0) {
      generateSkuList()
    }
  }
}

const addSpecValue = (keyIndex: number) => {
  specKeys.value[keyIndex].values.push({ specValue: '' })
}

const removeSpecValue = (keyIndex: number, valueIndex: number) => {
  const specKey = specKeys.value[keyIndex]
  if (specKey.values.length > 1) {
    specKey.values.splice(valueIndex, 1)
    // 重新生成SKU
    if (skuList.value.length > 0) {
      generateSkuList()
    }
  }
}

const generateSkuList = () => {
  if (!canGenerateSkus.value) {
    ElMessage.warning('请先完善规格属性配置')
    return
  }

  // 生成笛卡尔积
  const combinations = generateCartesianProduct(specKeys.value)
  
  skuList.value = combinations.map((combination, index) => {
    const specCombination: Record<string, string> = {}
    const specCombinationTextArray: string[] = []

    combination.forEach((value, keyIndex) => {
      const specName = specKeys.value[keyIndex].specName
      specCombination[specName] = value
      specCombinationTextArray.push(`${specName}:${value}`)
    })

    return {
      specCombination: JSON.stringify(specCombination),
      specCombinationText: specCombinationTextArray.join(', '),
      skuCode: `${productForm.value.productCode || 'SKU'}-${index + 1}`,
      price: productForm.value.basePrice,
      suggestedRetailPrice: productForm.value.suggestedRetailPrice,
      marketRetailPrice: productForm.value.marketRetailPrice,
      memberPrice: productForm.value.memberPrice,
      enableMemberPrice: productForm.value.enableMemberPrice,
      stock: productForm.value.stock,
      warningStock: productForm.value.warningStock,
      weight: productForm.value.weight,
      status: 1
    }
  })
  
  ElMessage.success(`已生成 ${skuList.value.length} 个SKU`)
}

// 生成笛卡尔积
const generateCartesianProduct = (specKeys: any[]): string[][] => {
  const values = specKeys.map(key => key.values.map((v: any) => v.specValue))
  
  function cartesian(arr: string[][]): string[][] {
    return arr.reduce((a, b) => {
      return a.flatMap((x: string[]) => b.map(y => [...x, y]))
    }, [[]] as string[][])
  }
  
  return cartesian(values)
}

const removeSku = (index: number) => {
  skuList.value.splice(index, 1)
}

onMounted(() => {
  loadCategories()
  loadBrands()
})
</script>

<style scoped lang="scss">
.product-add-container {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: 500;
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

.member-price-row {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
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

    .upload-placeholder {
      width: 150px;
      height: 150px;
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;

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
        color: #8c939d;
      }
    }
  }

  .url-input {
    margin-top: 10px;
    max-width: 500px;
  }
}

.detail-images-wrapper {
  :deep(.el-upload-list--picture-card) {
    display: inline-flex;
    flex-wrap: wrap;
  }

  :deep(.el-upload--picture-card) {
    width: 148px;
    height: 148px;
  }

  :deep(.el-upload-list__item) {
    width: 148px;
    height: 148px;
  }

  .upload-tip {
    margin-top: 10px;
    font-size: 12px;
    color: #999;
  }
}

// SKU配置样式
.form-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #666;
}

.spec-config-area {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  background-color: #fafbfc;
}

.spec-section {
  margin-bottom: 20px;
}

.spec-section-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 15px;

  .required-star {
    color: #f56c6c;
    margin-right: 4px;
  }
}

.spec-keys-wrapper {
  .spec-key-item {
    margin-bottom: 20px;
    padding: 15px;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    background-color: #fff;

    .spec-key-header {
      display: flex;
      align-items: center;
      gap: 15px;
      margin-bottom: 15px;
    }

    .spec-values-wrapper {
      .spec-values-header {
        margin-bottom: 10px;
        font-weight: bold;
        color: #333;
      }

      .spec-values-list {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        align-items: center;

        .spec-value-item {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }
    }
  }
}

.sku-list-wrapper {
  .sku-list-header {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 15px;

    .tip {
      color: #666;
      font-size: 12px;
    }
  }

  .sku-table {
    margin-top: 15px;
  }

  overflow-x: auto;
}
</style>
