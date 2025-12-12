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
          <el-option label="新品专区" value="new" />
          <el-option label="特惠区" value="special" />
          <el-option label="热销专区" value="hot" />
          <el-option label="限时促销" value="limited" />
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
          highlight-current-row
          @current-change="handleProductSelection"
        >
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

interface Props {
  modelLinkType?: number
  modelLinkValue?: string
  excludeTypes?: string[] // 排除的链接类型
}

interface Emits {
  (e: 'update:modelLinkType', value: number): void
  (e: 'update:modelLinkValue', value: string): void
}

const props = withDefaults(defineProps<Props>(), {
  modelLinkType: 0,
  modelLinkValue: '',
  excludeTypes: () => []
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

// 加载商品数据
const loadProducts = async () => {
  productLoading.value = true
  try {
    const response = await getProductPage(
      productPage.value.current,
      productPage.value.size,
      undefined,
      productSearchKeyword.value,
      undefined,
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
}

// 处理商品选择
const handleProductSelection = (product: ProductVO | null) => {
  selectedProductId.value = product ? product.id : null
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

// 监听linkValue变化，用于编辑时回显
watch([linkType, linkValue], () => {
  if (linkType.value === 2 && linkValue.value) {
    loadSelectedProductName()
  } else if (linkType.value !== 2) {
    selectedProductName.value = ''
  }
})

// 组件挂载时加载数据
onMounted(() => {
  loadCategories()
  loadSelectedProductName()
})

// 打开商品选择器时加载商品
watch(showProductSelector, (newVal) => {
  if (newVal) {
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