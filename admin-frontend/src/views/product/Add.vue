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

        <el-form-item label="条码" prop="barcode">
          <el-input v-model="productForm.barcode" placeholder="请输入条码（可选）" />
        </el-form-item>

        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="productForm.unit" placeholder="请输入计量单位，如：个、件、盒" />
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

        <el-form-item label="运费模板" prop="shippingTemplateId">
          <el-select
            v-model="productForm.shippingTemplateId"
            placeholder="请选择运费模板（可选，不选则包邮）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="template in shippingTemplates"
              :key="template.id"
              :label="template.templateName"
              :value="template.id"
            />
          </el-select>
          <div class="form-tip" style="color: #f56c6c;">不选择运费模板则该商品包邮</div>
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
              :disabled="productForm.enableSpec"
              controls-position="right"
              style="width: 100%"
            />
            <div v-if="productForm.enableSpec" class="form-tip" style="margin-top: 5px;">
              启用规格后，总库存由SKU库存自动计算：{{ totalSkuStock }}
            </div>
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

        <!-- 会员价设置 -->
        <div class="member-price-row">
          <el-form-item label="启用会员价">
            <el-switch v-model="productForm.enableMemberPrice" :active-value="1" :inactive-value="0" @change="handleProductEnableMemberPriceChange" />
            <span class="form-tip" style="margin-left: 10px; color: #f56c6c;">启用后可为不同会员等级设置不同的会员价</span>
          </el-form-item>
          <el-form-item v-if="productForm.enableMemberPrice === 1" label="会员价设置">
            <el-table :data="productMemberPriceTable" border style="width: 100%; margin-top: 10px;" max-height="300">
              <el-table-column prop="memberLevelName" label="会员等级" width="150" align="center" />
              <el-table-column label="会员价" min-width="200">
                <template #default="{ row }">
                  <el-input-number
                    v-model="row.memberPrice"
                    :min="0"
                    :precision="2"
                    :step="0.01"
                    controls-position="right"
                    style="width: 100%"
                    placeholder="请输入会员价"
                  />
                </template>
              </el-table-column>
            </el-table>
            <div class="form-tip" style="margin-top: 5px; color: #909399;">
              提示：为空表示该等级不享受会员价，将显示基础价格
            </div>
          </el-form-item>
        </div>

        <!-- 商品规格配置 -->
        <el-divider content-position="left">商品规格配置</el-divider>
        
        <el-form-item label="是否启用规格" prop="enableSpec">
          <el-switch v-model="productForm.enableSpec" @change="handleEnableSpecChange" />
          <div class="form-tip" style="color: #f56c6c;">启用后可为商品配置不同规格的SKU（如颜色、尺寸等）</div>
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
                style="width: 100%; min-width: 1350px;"
                :scroll="{ x: 1350 }"
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
                <el-table-column label="会员价状态" width="120" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.enableMemberPrice === 1 && row.memberPrices?.length > 0" type="success" size="small">
                      已设置
                    </el-tag>
                    <el-tag v-else-if="row.enableMemberPrice === 1" type="warning" size="small">
                      未设置
                    </el-tag>
                    <el-tag v-else type="info" size="small">
                      未启用
                    </el-tag>
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
                <el-table-column label="重量(g)" min-width="120">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.weight"
                      :min="0"
                      :precision="2"
                      :step="0.01"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="警戒库存" width="110">
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
                <el-table-column label="操作" width="130" fixed="right">
                  <template #default="{ row, $index }">
                    <el-button
                      type="primary"
                      size="small"
                      @click="openSkuMemberPriceDialog(row, $index)"
                      style="margin-right: 3px; font-size: 12px; padding: 4px 6px;"
                    >
                      设置会员价
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      :icon="Delete"
                      @click="removeSku($index)"
                      style="padding: 4px 8px;"
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
            <el-radio label="草稿">草稿</el-radio>
            <el-radio label="上架">上架</el-radio>
            <el-radio label="下架">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- SKU会员价设置弹窗 -->
    <el-dialog
      v-model="skuMemberPriceDialogVisible"
      :title="`设置会员价 - ${currentSkuForMemberPrice ? currentSkuForMemberPrice.specCombinationText : ''}`"
      width="600px"
    >
      <div style="margin-bottom: 15px;" v-if="currentSkuForMemberPrice">
        <el-switch
          v-model="currentSkuForMemberPrice.enableMemberPrice"
          :active-value="1"
          :inactive-value="0"
          @change="handleSkuEnableMemberPriceChange"
        />
        <span style="margin-left: 10px; color: #909399;">
          启用会员价后可为不同会员等级设置不同的会员价
        </span>
      </div>
      
      <el-table
        v-if="currentSkuForMemberPrice && currentSkuForMemberPrice.enableMemberPrice === 1"
        :data="skuMemberPriceTable"
        border
        style="width: 100%"
        max-height="400"
      >
        <el-table-column prop="memberLevelName" label="会员等级" width="150" align="center" />
        <el-table-column label="会员价" min-width="200">
          <template #default="{ row }">
            <el-input-number
              v-model="row.memberPrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
              placeholder="请输入会员价"
            />
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="currentSkuForMemberPrice && currentSkuForMemberPrice.enableMemberPrice === 1" class="form-tip" style="margin-top: 10px; color: #909399;">
        提示：为空表示该等级不享受会员价，将显示基础价格
      </div>
      
      <template #footer>
        <el-button @click="skuMemberPriceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSkuMemberPrice">确定</el-button>
      </template>
    </el-dialog>

    <!-- 固定底部操作栏 -->
    <div class="fixed-footer">
      <el-button type="primary" @click="handleSubmit">提交</el-button>
      <el-button type="info" @click="handleSaveAsDraft">保存为草稿</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </div>

    <!-- ERP同步进度弹窗 -->
    <el-dialog
      v-model="syncProgressVisible"
      title="ERP同步进度"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="!syncProgressLoading"
    >
      <div class="sync-progress-content">
        <el-steps :active="syncProgressStep" direction="vertical" finish-status="success">
          <el-step title="同步商品资料" :status="getStepStatus(0)">
            <template #description>
              <div v-if="syncProgressStep === 0 && syncProgressLoading" class="step-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>正在同步商品资料到ERP...</span>
              </div>
              <div v-else-if="syncProgressStep > 0" class="step-success">
                <el-icon><CircleCheck /></el-icon>
                <span>商品资料同步成功</span>
              </div>
              <div v-else-if="syncProgressError && syncProgressStep === 0" class="step-error">
                <el-icon><CircleClose /></el-icon>
                <span>{{ syncProgressError }}</span>
              </div>
            </template>
          </el-step>
          
          <el-step title="等待ERP处理" :status="getStepStatus(1)">
            <template #description>
              <div v-if="syncProgressStep === 1 && syncProgressLoading" class="step-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>等待ERP系统处理商品资料（2秒）...</span>
              </div>
              <div v-else-if="syncProgressStep > 1" class="step-success">
                <el-icon><CircleCheck /></el-icon>
                <span>等待完成</span>
              </div>
            </template>
          </el-step>
          
          <el-step title="同步库存" :status="getStepStatus(2)">
            <template #description>
              <div v-if="syncProgressStep === 2 && syncProgressLoading" class="step-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>正在同步库存到ERP（尝试 {{ syncProgressRetryCount + 1 }}/4）...</span>
              </div>
              <div v-else-if="syncProgressStep > 2" class="step-success">
                <el-icon><CircleCheck /></el-icon>
                <span>库存同步成功</span>
              </div>
              <div v-else-if="syncProgressError && syncProgressStep === 2" class="step-error">
                <el-icon><CircleClose /></el-icon>
                <span>{{ syncProgressError }}</span>
              </div>
            </template>
          </el-step>
        </el-steps>
      </div>
      
      <template #footer>
        <el-button 
          v-if="!syncProgressLoading" 
          type="primary" 
          @click="syncProgressVisible = false"
        >
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { Plus, Delete, Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile, UploadUserFile } from 'element-plus'
import { createProduct, type ProductMemberPriceDTO } from '@/api/admin/product'
import { getCategoryTree } from '@/api/admin/productCategory'
import { getBrandOptions } from '@/api/admin/brand'
import { batchCreateSkus, type ProductSkuDTO, type ProductSkuMemberPriceDTO } from '@/api/admin/sku'
import { getAllEnabledMemberLevels, type MemberLevelVO } from '@/api/admin/memberLevel'
import request from '@/utils/request'
import RichTextEditor from '@/components/common/RichTextEditor.vue'

const router = useRouter()
const formRef = ref<FormInstance>()

const productForm = ref({
  productName: '',
  categoryId: null as any,
  productCode: '',
  barcode: '',
  unit: '',
  brandId: null as any,
  shippingTemplateId: null as any,
  basePrice: 0,
  suggestedRetailPrice: 0,
  marketRetailPrice: 0,
  memberPrice: 0,
  enableMemberPrice: 0,
  memberPrices: [] as ProductMemberPriceDTO[],
  stock: 0,
  warningStock: 10,
  weight: 0,
  description: '',
  mainImage: '',
  status: '草稿',
  enableSpec: false as boolean
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

// 会员等级列表
const memberLevels = ref<MemberLevelVO[]>([])

// 商品会员价表格数据（按会员等级）
const productMemberPriceTable = ref<Array<{
  memberLevelId: number
  memberLevelName: string
  memberPrice: number | null
}>>([])

// SKU会员价设置相关
const skuMemberPriceDialogVisible = ref(false)
const currentSkuForMemberPrice = ref<any>(null)
const currentSkuIndex = ref<number>(-1)
const skuMemberPriceTable = ref<Array<{
  memberLevelId: number
  memberLevelName: string
  memberPrice: number | null
}>>([])

// 计算属性：是否可以生成SKU
const canGenerateSkus = computed(() => {
  if (!productForm.value.enableSpec) return false
  return specKeys.value.every(key =>
    key.specName.trim() &&
    key.values.length > 0 &&
    key.values.every(value => value.specValue.trim())
  )
})

// 计算属性：SKU总库存
const totalSkuStock = computed(() => {
  if (!productForm.value.enableSpec || skuList.value.length === 0) {
    return 0
  }
  return skuList.value.reduce((total, sku) => total + (sku.stock || 0), 0)
})

// 监听SKU库存变化，自动更新商品总库存
watch(totalSkuStock, (newTotal) => {
  if (productForm.value.enableSpec) {
    productForm.value.stock = newTotal
  }
})

// 监听enableSpec变化
watch(() => productForm.value.enableSpec, (newValue) => {
  if (newValue) {
    // 启用规格时，设置总库存为SKU总和
    productForm.value.stock = totalSkuStock.value
  }
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
const shippingTemplates = ref<any[]>([])

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

// 加载运费模板列表
const loadShippingTemplates = async () => {
  try {
    const response = await request.get('/api/admin/shipping/template/all')
    shippingTemplates.value = response || []
  } catch (error) {
    // 静默处理，不显示错误
    shippingTemplates.value = []
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

// ERP同步进度相关
const syncProgressVisible = ref(false)
const syncProgressLoading = ref(false)
const syncProgressStep = ref(0) // 0-商品资料, 1-等待, 2-库存
const syncProgressRetryCount = ref(0)
const syncProgressError = ref('')
let syncProgressInstance: ReturnType<typeof ElLoading.service> | null = null

// 获取步骤状态
const getStepStatus = (step: number) => {
  if (syncProgressError.value && syncProgressStep.value === step) {
    return 'error'
  }
  if (syncProgressStep.value > step) {
    return 'success'
  }
  if (syncProgressStep.value === step && syncProgressLoading.value) {
    return 'process'
  }
  return 'wait'
}

// 同步商品到ERP（完整流程：商品资料 + 库存）
const syncProductToErp = async (productId: number) => {
  // 重置进度状态
  syncProgressVisible.value = true
  syncProgressLoading.value = true
  syncProgressStep.value = 0
  syncProgressRetryCount.value = 0
  syncProgressError.value = ''
  
  try {
    // 显示全屏loading
    syncProgressInstance = ElLoading.service({
      lock: true,
      text: '正在同步到ERP，请稍候...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    // 更新进度：步骤1 - 同步商品资料
    syncProgressStep.value = 0
    
    // 使用定时器模拟进度更新（因为后端是同步接口，无法实时获取进度）
    // 步骤1：同步商品资料（预计2-3秒）
    let stepTimer1: ReturnType<typeof setTimeout> | null = null
    let stepTimer2: ReturnType<typeof setTimeout> | null = null
    
    // 2秒后进入等待步骤（模拟商品资料同步完成）
    stepTimer1 = setTimeout(() => {
      if (syncProgressLoading.value && syncProgressStep.value === 0) {
        syncProgressStep.value = 1 // 进入等待步骤
      }
    }, 2000)
    
    // 4秒后进入库存同步步骤（2秒商品资料 + 2秒等待）
    stepTimer2 = setTimeout(() => {
      if (syncProgressLoading.value && syncProgressStep.value === 1) {
        syncProgressStep.value = 2 // 进入库存同步步骤
      }
    }, 4000)
    
    // 开始调用接口（使用完整同步接口）
    const startTime = Date.now()
    const response = await request.post(`/api/admin/inventory/sync/${productId}/full`)
    const elapsedTime = Date.now() - startTime
    
    // 清除定时器
    if (stepTimer1) clearTimeout(stepTimer1)
    if (stepTimer2) clearTimeout(stepTimer2)
    
    // 根据实际耗时更新进度
    // 如果接口返回时还在步骤0，说明商品资料同步很快，直接跳到步骤1
    if (syncProgressStep.value === 0 && elapsedTime < 2000) {
      syncProgressStep.value = 1
    }
    // 如果接口返回时还在步骤1，说明等待时间还没到，直接跳到步骤2
    if (syncProgressStep.value === 1 && elapsedTime < 4000) {
      syncProgressStep.value = 2
    }
    
    // 关闭全屏loading
    if (syncProgressInstance) {
      syncProgressInstance.close()
      syncProgressInstance = null
    }
    
    // 更新进度
    syncProgressLoading.value = false
    
    // 检查同步结果
    if (response && response.success) {
      syncProgressStep.value = 3 // 全部完成
      ElMessage.success('商品资料和库存同步成功')
      // 2秒后自动关闭进度弹窗
      setTimeout(() => {
        syncProgressVisible.value = false
      }, 2000)
    } else {
      // 根据失败步骤设置错误信息
      const errorMsg = response?.message || '同步失败'
      syncProgressError.value = errorMsg
      
      if (response?.step === 'ITEM_SYNC') {
        syncProgressStep.value = 0
      } else if (response?.step === 'INVENTORY_SYNC') {
        syncProgressStep.value = 2
        syncProgressRetryCount.value = response?.inventoryRetryCount || 0
      }
      
      ElMessage.error({
        message: errorMsg,
        duration: 5000,
        showClose: true
      })
    }
  } catch (error: any) {
    console.error('同步到ERP失败:', error)
    
    // 关闭全屏loading
    if (syncProgressInstance) {
      syncProgressInstance.close()
      syncProgressInstance = null
    }
    
    syncProgressLoading.value = false
    syncProgressError.value = error?.message || '同步失败'
    
    const errorMsg = error?.message || '同步失败'
    if (!errorMsg.includes('请求失败')) {
      ElMessage.error(errorMsg)
    }
  }
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

        // 转换会员价表格数据为memberPrices数组
        if (productForm.value.enableMemberPrice === 1) {
          productForm.value.memberPrices = productMemberPriceTable.value
            .filter(row => row.memberPrice != null && row.memberPrice > 0)
            .map(row => ({
              memberLevelId: row.memberLevelId,
              memberPrice: row.memberPrice!
            }))
        } else {
          productForm.value.memberPrices = []
        }

        const productId = await createProduct({
          productName: productForm.value.productName,
          categoryId: categoryId,
          productCode: productForm.value.productCode,
          barcode: productForm.value.barcode,
          unit: productForm.value.unit,
          brandId: productForm.value.brandId,
          shippingTemplateId: productForm.value.shippingTemplateId,
          basePrice: productForm.value.basePrice,
          suggestedRetailPrice: productForm.value.suggestedRetailPrice,
          marketRetailPrice: productForm.value.marketRetailPrice,
          enableMemberPrice: productForm.value.enableMemberPrice,
          memberPrices: productForm.value.memberPrices,
          stock: productForm.value.stock,
          warningStock: productForm.value.warningStock,
          weight: productForm.value.weight,
          description: productForm.value.description,
          mainImage: productForm.value.mainImage,
          images: detailImages.length > 0 ? JSON.stringify(detailImages) : undefined, // JSON数组格式
          status: productForm.value.status,
          enableSpec: productForm.value.enableSpec ? 1 : 0
        })
        
        // 如果启用了规格，保存SKU数据
        if (productForm.value.enableSpec && skuList.value.length > 0) {
          const skuDTOs: ProductSkuDTO[] = skuList.value.map(sku => ({
            productId: productId,
            skuCode: sku.skuCode,
            specCombination: sku.specCombination,
            price: sku.price,
            suggestedRetailPrice: sku.suggestedRetailPrice || 0,
            marketRetailPrice: sku.marketRetailPrice || 0,
            enableMemberPrice: sku.enableMemberPrice || 0,
            memberPrices: sku.memberPrices || [],
            stock: sku.stock || 0,
            warningStock: sku.warningStock || 0,
            weight: sku.weight || 0,
            status: sku.status || 1
          }))
          await batchCreateSkus(skuDTOs)
        }
        
        ElMessage.success('商品发布成功！')
        
        // 同步到ERP（所有商品状态都同步）
        // 注意：后端事件监听器也会自动同步，但前端需要显示进度条，所以前端主动调用同步接口
        // 显示进度条并等待同步完成
        try {
          await syncProductToErp(productId)
        } catch (syncError) {
          // 同步失败不影响商品发布成功，只记录错误
          console.error('ERP同步失败:', syncError)
        }
        
        // 等待同步完成后再跳转，确保进度条显示完整
        router.push('/admin/product/list')
      } catch (error) {
        console.error('商品发布失败:', error)
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

const handleSaveAsDraft = async () => {
  try {
    // 暂存原状态
    const originalStatus = productForm.value.status
    // 设置为草稿状态
    productForm.value.status = '草稿'

    // 处理级联选择器的值（如果是数组，取最后一个值）
    const categoryId = Array.isArray(productForm.value.categoryId)
      ? productForm.value.categoryId[productForm.value.categoryId.length - 1]
      : productForm.value.categoryId

    // 获取详情图URL列表
    const detailImages = detailImageList.value
      .map(file => file.url || (file.response as any)?.data?.url)
      .filter(url => url)

    // 转换会员价表格数据为memberPrices数组
    if (productForm.value.enableMemberPrice === 1) {
      productForm.value.memberPrices = productMemberPriceTable.value
        .filter(row => row.memberPrice != null && row.memberPrice > 0)
        .map(row => ({
          memberLevelId: row.memberLevelId,
          memberPrice: row.memberPrice!
        }))
    } else {
      productForm.value.memberPrices = []
    }

    const productId = await createProduct({
      productName: productForm.value.productName,
      categoryId: categoryId,
      productCode: productForm.value.productCode,
      barcode: productForm.value.barcode,
      unit: productForm.value.unit,
      brandId: productForm.value.brandId,
      shippingTemplateId: productForm.value.shippingTemplateId,
      basePrice: productForm.value.basePrice,
      suggestedRetailPrice: productForm.value.suggestedRetailPrice,
      marketRetailPrice: productForm.value.marketRetailPrice,
      enableMemberPrice: productForm.value.enableMemberPrice,
      memberPrices: productForm.value.memberPrices,
      stock: productForm.value.stock,
      warningStock: productForm.value.warningStock,
      weight: productForm.value.weight,
      description: productForm.value.description,
      mainImage: productForm.value.mainImage,
      images: detailImages.length > 0 ? JSON.stringify(detailImages) : undefined, // JSON数组格式
      status: '草稿',
      enableSpec: productForm.value.enableSpec ? 1 : 0
    })

    // 如果启用了规格，保存SKU数据
    if (productForm.value.enableSpec && skuList.value.length > 0) {
      const skuDTOs: ProductSkuDTO[] = skuList.value.map(sku => ({
        productId: productId,
        skuCode: sku.skuCode,
        specCombination: sku.specCombination,
        price: sku.price,
        suggestedRetailPrice: sku.suggestedRetailPrice || 0,
        marketRetailPrice: sku.marketRetailPrice || 0,
        enableMemberPrice: sku.enableMemberPrice || 0,
        memberPrices: sku.memberPrices || [],
        stock: sku.stock || 0,
        warningStock: sku.warningStock || 0,
        weight: sku.weight || 0,
        status: sku.status || 1
      }))
      await batchCreateSkus(skuDTOs)
    }

    ElMessage.success('草稿保存成功！')
    
    // 同步到ERP（所有商品状态都同步，包括草稿）
    try {
      await syncProductToErp(productId)
    } catch (syncError) {
      // 同步失败不影响草稿保存成功，只记录错误
      console.error('ERP同步失败:', syncError)
    }
    
    router.push('/admin/product/list')
  } catch (error) {
    console.error('草稿保存失败:', error)
    ElMessage.error('草稿保存失败')
  }
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
      enableMemberPrice: 0,
      memberPrices: [] as ProductSkuMemberPriceDTO[],
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

// 加载会员等级列表
const loadMemberLevels = async () => {
  try {
    memberLevels.value = await getAllEnabledMemberLevels()
    // 初始化商品会员价表格
    initProductMemberPriceTable()
  } catch (error) {
    console.error('加载会员等级失败:', error)
    memberLevels.value = []
  }
}

// 初始化商品会员价表格
const initProductMemberPriceTable = () => {
  productMemberPriceTable.value = memberLevels.value.map(level => ({
    memberLevelId: level.id,
    memberLevelName: level.levelName,
    memberPrice: null
  }))
}

// 处理启用会员价切换
const handleProductEnableMemberPriceChange = (value: number) => {
  if (value === 1 && productMemberPriceTable.value.length === 0) {
    initProductMemberPriceTable()
  }
}

// 打开SKU会员价设置弹窗
const openSkuMemberPriceDialog = (sku: any, index: number) => {
  currentSkuForMemberPrice.value = {
    ...sku,
    enableMemberPrice: sku.enableMemberPrice ?? 0,
    memberPrices: sku.memberPrices || []
  }
  currentSkuIndex.value = index
  skuMemberPriceDialogVisible.value = true
  // 初始化会员价表格
  initSkuMemberPriceTable()
}

// 初始化SKU会员价表格
const initSkuMemberPriceTable = () => {
  skuMemberPriceTable.value = memberLevels.value.map(level => {
    // 如果SKU已有会员价配置，查找对应的价格
    const existingPrice = currentSkuForMemberPrice.value.memberPrices?.find(
      (mp: ProductSkuMemberPriceDTO) => mp.memberLevelId === level.id
    )
    return {
      memberLevelId: level.id,
      memberLevelName: level.levelName,
      memberPrice: existingPrice ? existingPrice.memberPrice : null
    }
  })
}

// 处理SKU启用会员价切换
const handleSkuEnableMemberPriceChange = (value: number) => {
  if (value === 0) {
    // 禁用会员价时，清空会员价配置
    if (currentSkuForMemberPrice.value) {
      currentSkuForMemberPrice.value.memberPrices = []
    }
  }
}

// 保存SKU会员价设置
const saveSkuMemberPrice = () => {
  if (!currentSkuForMemberPrice.value) return
  
  // 转换会员价表格数据为memberPrices数组
  if (currentSkuForMemberPrice.value.enableMemberPrice === 1) {
    currentSkuForMemberPrice.value.memberPrices = skuMemberPriceTable.value
      .filter(row => row.memberPrice != null && row.memberPrice > 0)
      .map(row => ({
        memberLevelId: row.memberLevelId,
        memberPrice: row.memberPrice!
      }))
  } else {
    currentSkuForMemberPrice.value.memberPrices = []
  }
  
  // 更新skuList中对应的SKU（保留原有字段）
  const originalSku = skuList.value[currentSkuIndex.value]
  skuList.value[currentSkuIndex.value] = {
    ...originalSku,
    enableMemberPrice: currentSkuForMemberPrice.value.enableMemberPrice,
    memberPrices: currentSkuForMemberPrice.value.memberPrices
  }
  
  ElMessage.success('会员价设置已保存')
  skuMemberPriceDialogVisible.value = false
}

onMounted(() => {
  loadCategories()
  loadBrands()
  loadShippingTemplates()
  loadMemberLevels()
})
</script>

<style scoped lang="scss">
.product-add-container {
  padding: 20px;
  padding-bottom: 80px; // 为固定底部栏留出空间
}

.card-header {
  font-size: 18px;
  font-weight: 500;
}

// 固定底部操作栏
.fixed-footer {
  position: fixed;
  bottom: 0;
  left: 200px;
  right: 0;
  height: 60px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  z-index: 999;
  padding: 0 20px;
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

.sync-progress-content {
  padding: 20px 0;
  
  .step-loading {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #409eff;
    
    .el-icon {
      animation: rotating 2s linear infinite;
    }
  }
  
  .step-success {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #67c23a;
  }
  
  .step-error {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #f56c6c;
  }
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
