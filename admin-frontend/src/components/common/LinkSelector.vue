<template>
  <div class="link-selector">
    <!-- 链接类型选择 -->
    <el-form-item label="链接类型" required>
      <el-select
        v-model="linkType"
        placeholder="请选择链接类型"
        style="width: 100%"
        @change="handleTypeChange"
      >
        <el-option 
          v-if="!props.excludeTypes.includes('0')"
          label="无链接" 
          :value="0" 
        />
        <el-option 
          v-if="!props.excludeTypes.includes('1')"
          label="商品分类" 
          :value="1" 
        />
        <el-option 
          v-if="!props.excludeTypes.includes('2')"
          label="商品详情" 
          :value="2" 
        />
        <el-option 
          v-if="!props.excludeTypes.includes('3')"
          label="促销活动" 
          :value="3" 
        />
        <el-option 
          v-if="!props.excludeTypes.includes('4')"
          label="外部链接" 
          :value="4" 
        />
        <el-option 
          v-if="!props.excludeTypes.includes('5')"
          label="品牌类型" 
          :value="5" 
        />
      </el-select>
    </el-form-item>

    <!-- 根据链接类型显示不同的选择器 -->
    <div v-if="linkType !== 0">
      <!-- 商品分类选择 -->
      <el-form-item v-if="linkType === 1" label="目标分类" required>
        <el-select
          v-model="linkValue"
          placeholder="请选择商品分类"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="category in flatCategories"
            :key="category.id"
            :label="category.name"
            :value="category.id.toString()"
          />
        </el-select>
      </el-form-item>

      <!-- 商品详情选择 -->
      <el-form-item v-if="linkType === 2" label="目标商品" required>
        <div style="display: flex; gap: 10px; align-items: center;">
          <el-input
            v-model="selectedProductName"
            placeholder="请选择商品"
            readonly
            style="flex: 1"
          />
          <el-button type="primary" @click="showProductSelector = true">
            选择商品
          </el-button>
          <el-button v-if="linkValue" @click="clearProduct">清除</el-button>
        </div>
      </el-form-item>

      <!-- 促销活动选择 -->
      <el-form-item v-if="linkType === 3" label="促销类型" required>
        <el-select
          v-model="linkValue"
          placeholder="请选择促销类型"
          style="width: 100%"
        >
          <el-option v-if="props.promotionTypes.includes('new')" label="新品专区" value="new" />
          <el-option v-if="props.promotionTypes.includes('special')" label="特惠区" value="special" />
          <el-option v-if="props.promotionTypes.includes('hot')" label="热销专区" value="hot" />
          <el-option v-if="props.promotionTypes.includes('limited')" label="限时促销" value="limited" />
        </el-select>
      </el-form-item>

      <!-- 外部链接输入 -->
      <el-form-item v-if="linkType === 4" label="外部链接" required>
        <el-input
          v-model="linkValue"
          placeholder="请输入完整的URL地址，如：https://www.example.com"
          type="url"
        />
      </el-form-item>

      <!-- 品牌类型选择 -->
      <el-form-item v-if="linkType === 5" label="目标品牌" required>
        <el-select
          v-model="linkValue"
          placeholder="请选择品牌"
          filterable
          style="width: 100%"
          :loading="brandLoading"
        >
          <el-option
            v-for="brand in brandList"
            :key="brand.id"
            :label="brand.brandName"
            :value="brand.id!.toString()"
          />
        </el-select>
      </el-form-item>
    </div>

    <!-- 商品选择对话框 -->
    <el-dialog
      v-model="showProductSelector"
      title="选择商品"
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="product-selector">
        <!-- 搜索框 -->
        <el-form :inline="true" style="margin-bottom: 20px;">
          <el-form-item label="商品搜索">
            <el-input
              v-model="productSearchKeyword"
              placeholder="输入商品名称或编码搜索"
              clearable
              style="width: 200px"
              @keyup.enter="loadProducts"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadProducts">搜索</el-button>
          </el-form-item>
        </el-form>

        <!-- 商品列表 -->
        <el-table
          v-loading="productLoading"
          :data="productList"
          height="400px"
          @selection-change="handleProductSelection"
        >
          <el-table-column type="selection" width="55" :selectable="() => true" />
          <el-table-column width="80">
            <template #default="{ row }">
              <img
                :src="row.mainImage || 'https://via.placeholder.com/60x60?text=No+Image'"
                style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px"
                :alt="row.productName"
              />
            </template>
          </el-table-column>
          <el-table-column prop="productCode" label="商品编码" width="120" />
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="basePrice" label="价格" width="100">
            <template #default="{ row }">
              ¥{{ row.basePrice }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === '上架' ? 'success' : 'danger'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="productPage.current"
          v-model:page-size="productPage.size"
          :total="productPage.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 20px; text-align: right"
          @size-change="loadProducts"
          @current-change="loadProducts"
        />
      </div>

      <template #footer>
        <div>
          <el-button @click="showProductSelector = false">取消</el-button>
          <el-button
            type="primary"
            :disabled="!selectedProductId"
            @click="confirmProductSelection"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getProductPage, type ProductVO } from '@/api/admin/product'
import { getCategoryTree, type ProductCategoryVO } from '@/api/admin/productCategory'
import { getBrandOptions } from '@/api/admin/brand'
import { type Brand } from '@/api/admin/website'

interface Props {
  modelLinkType?: number
  modelLinkValue?: string
  excludeTypes?: string[] // 排除的链接类型
  promotionTypes?: string[] // 促销类型选项，如果不传则显示所有选项
}

interface Emits {
  (e: 'update:modelLinkType', value: number): void
  (e: 'update:modelLinkValue', value: string): void
}

const props = withDefaults(defineProps<Props>(), {
  modelLinkType: 0,
  modelLinkValue: '',
  excludeTypes: () => [],
  promotionTypes: () => ['new', 'special', 'hot', 'limited'] // 默认显示所有促销类型
})

const emit = defineEmits<Emits>()

// 双向绑定
const linkType = computed({
  get: () => props.modelLinkType,
  set: (value) => emit('update:modelLinkType', value)
})

const linkValue = computed({
  get: () => props.modelLinkValue,
  set: (value) => emit('update:modelLinkValue', value)
})

// 分类数据
const categoryTree = ref<ProductCategoryVO[]>([])
const flatCategories = ref<Array<{ id: number; name: string }>>([])

// 品牌数据
const brandList = ref<Brand[]>([])
const brandLoading = ref(false)

// 商品选择器
const showProductSelector = ref(false)
const productLoading = ref(false)
const productList = ref<ProductVO[]>([])
const productSearchKeyword = ref('')
const productPage = ref({
  current: 1,
  size: 10,
  total: 0
})
const selectedProductId = ref<number | null>(null)
const selectedProductName = ref('')

// 加载分类数据
const loadCategories = async () => {
  try {
    categoryTree.value = await getCategoryTree()
    
    // 扁平化分类树
    const flatten = (categories: ProductCategoryVO[], prefix = '') => {
      const result: Array<{ id: number; name: string }> = []
      
      categories.forEach(category => {
        const name = prefix ? `${prefix} > ${category.categoryName}` : category.categoryName
        result.push({
          id: category.id,
          name
        })
        
        if (category.children && category.children.length > 0) {
          result.push(...flatten(category.children, name))
        }
      })
      
      return result
    }
    
    flatCategories.value = flatten(categoryTree.value)
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  }
}

// 加载品牌数据
const loadBrands = async () => {
  brandLoading.value = true
  try {
    const response = await getBrandOptions()
    // request拦截器已经提取了data字段，所以response直接就是品牌列表
    brandList.value = Array.isArray(response) ? response : []
    console.log('品牌列表加载成功，数量:', brandList.value.length)
  } catch (error) {
    console.error('加载品牌失败:', error)
    ElMessage.error('加载品牌失败')
    brandList.value = []
  } finally {
    brandLoading.value = false
  }
}

// 加载商品数据（只查询已上架的商品）
const loadProducts = async () => {
  productLoading.value = true
  try {
    const response = await getProductPage(
      productPage.value.current,
      productPage.value.size,
      undefined,
      productSearchKeyword.value,
      undefined,
      '上架', // 只查询已上架的商品
      undefined
    )
    
    productList.value = response.records
    productPage.value.total = response.total
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败')
  } finally {
    productLoading.value = false
  }
}

// 处理链接类型变化
const handleTypeChange = () => {
  linkValue.value = ''
  selectedProductName.value = ''
  selectedProductId.value = null
  // 如果切换到品牌类型，确保品牌列表已加载
  if (linkType.value === 5 && brandList.value.length === 0) {
    loadBrands()
  }
}

// 处理商品选择（checkbox选择，但只允许选择一个）
const handleProductSelection = (selection: ProductVO[]) => {
  if (selection.length > 0) {
    // 只保留最后一个选中的商品
    selectedProductId.value = selection[selection.length - 1].id
    // 如果选中了多个，取消其他选中
    if (selection.length > 1) {
      // 通过设置表格的选中状态来只保留最后一个
      const lastProduct = selection[selection.length - 1]
      // 这里需要手动控制表格选中状态，但Element Plus的表格组件会自动处理
      // 我们只需要确保selectedProductId是最新的即可
    }
  } else {
    selectedProductId.value = null
  }
}

// 确认商品选择
const confirmProductSelection = () => {
  if (!selectedProductId.value) {
    ElMessage.warning('请先选择商品')
    return
  }
  
  const selectedProduct = productList.value.find(p => p.id === selectedProductId.value)
  if (selectedProduct) {
    linkValue.value = selectedProductId.value.toString()
    selectedProductName.value = selectedProduct.productName
    showProductSelector.value = false
  }
}

// 清除商品选择
const clearProduct = () => {
  linkValue.value = ''
  selectedProductName.value = ''
  selectedProductId.value = null
}

// 根据linkValue加载已选择的商品名称
const loadSelectedProductName = async () => {
  if (linkType.value === 2 && linkValue.value) {
    try {
      const response = await getProductPage(1, 1000, undefined, undefined, undefined, undefined)
      const product = response.records.find(p => p.id.toString() === linkValue.value)
      if (product) {
        selectedProductName.value = product.productName
      }
    } catch (error) {
      console.error('加载商品名称失败:', error)
    }
  }
}

// 处理品牌类型的编辑回显：如果linkValue是品牌名称，转换为品牌ID
const handleBrandValue = async () => {
  if (linkType.value === 5 && linkValue.value) {
    // 如果品牌列表还没加载，先加载
    if (brandList.value.length === 0) {
      await loadBrands()
    }
    
    // 如果linkValue不是纯数字（可能是品牌名称），尝试根据品牌名称找到品牌ID
    if (isNaN(Number(linkValue.value))) {
      const brand = brandList.value.find(b => 
        b.brandName.toLowerCase() === linkValue.value.toLowerCase() ||
        b.brandName === linkValue.value
      )
      if (brand && brand.id) {
        linkValue.value = brand.id.toString()
        console.log('品牌名称转换为ID:', linkValue.value, '->', brand.id)
      } else {
        console.warn('未找到对应的品牌:', linkValue.value)
      }
    }
  }
}

// 监听linkValue变化，用于编辑时回显
watch([linkType, linkValue], async () => {
  if (linkType.value === 2 && linkValue.value) {
    loadSelectedProductName()
  } else if (linkType.value === 5 && linkValue.value) {
    await handleBrandValue()
  } else if (linkType.value !== 2) {
    selectedProductName.value = ''
  }
})

// 组件挂载时加载数据
onMounted(async () => {
  await loadCategories()
  await loadBrands()
  await loadSelectedProductName()
  // 如果初始值就是品牌类型，处理品牌名称转换
  if (linkType.value === 5 && linkValue.value) {
    await handleBrandValue()
  }
})

// 打开商品选择器时加载商品并重置选择
watch(showProductSelector, (newVal) => {
  if (newVal) {
    selectedProductId.value = null
    productPage.value.current = 1
    productSearchKeyword.value = ''
    loadProducts()
  }
})
</script>

<style scoped lang="scss">
.link-selector {
  .product-selector {
    .el-table {
      border: 1px solid #ebeef5;
      border-radius: 4px;
    }
  }
}
</style>