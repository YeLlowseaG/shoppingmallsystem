<template>
  <div class="product-detail">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <div class="container">
        <span>您当前的位置：</span>
        <router-link to="/">首页</router-link>
        <template v-for="(category, index) in breadcrumbPath" :key="category.id">
          <span> > </span>
          <router-link :to="`/products?categoryId=${category.id}`">
            {{ category.name }}
          </router-link>
        </template>
        <span> > </span>
        <span class="current">{{ product.name }}</span>
      </div>
    </div>

    <!-- 商品主体信息 -->
    <div class="product-main">
      <div class="container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          <p>加载中...</p>
        </div>

        <!-- 商品不存在提示 -->
        <div v-else-if="productNotFound" class="product-not-found">
          <div class="not-found-content">
            <el-icon :size="80" class="not-found-icon"><Warning /></el-icon>
            <h2 class="not-found-title">商品不存在</h2>
            <p class="not-found-message">抱歉，您访问的商品不存在或已被删除</p>
            <div class="not-found-actions">
              <el-button type="primary" @click="router.push('/')">返回首页</el-button>
              <el-button @click="router.push('/products')">浏览商品</el-button>
            </div>
          </div>
        </div>

        <!-- 商品状态错误提示（下架、草稿等） -->
        <div v-else-if="productStatusError" class="product-not-found">
          <div class="not-found-content">
            <el-icon :size="80" class="not-found-icon"><Warning /></el-icon>
            <h2 class="not-found-title">{{ productStatusError.message }}</h2>
            <p class="not-found-message" v-if="productStatusError.type === 'offline'">
              抱歉，该商品已下架，暂时无法购买
            </p>
            <p class="not-found-message" v-else>
              抱歉，您访问的商品不存在或已被删除
            </p>
            <div class="not-found-actions">
              <el-button type="primary" @click="router.push('/')">返回首页</el-button>
              <el-button @click="router.push('/products')">浏览商品</el-button>
            </div>
          </div>
        </div>

        <!-- 商品内容 -->
        <template v-else>
          <!-- 左侧：商品图片 -->
          <div class="product-gallery">
          <div class="main-image">
            <img :src="currentImage" :alt="product.name" />
          </div>
          <div class="thumbnail-list">
            <div
              v-for="(img, index) in product.images"
              :key="index"
              class="thumbnail-item"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            >
              <img :src="img" :alt="`${product.name} ${index + 1}`" />
            </div>
          </div>
        </div>

        <!-- 右侧：商品信息 -->
        <div class="product-info">
          <!-- 商品标题 -->
          <h1 class="product-title">{{ product.name }}</h1>

          <!-- 促销信息 -->
          <div class="promo-info" v-if="product.promoText">
            <span class="promo-label">箱限：</span>
            <span class="promo-text">{{ product.promoText }}</span>
          </div>

          <!-- 商品基本信息 -->
          <div class="product-meta">
            <div class="meta-row">
              <span class="meta-label">商品编号：</span>
              <span class="meta-value">{{ product.productNo }}</span>
            </div>
            <div class="meta-row" v-if="product.barcode">
              <span class="meta-label">条码：</span>
              <span class="meta-value">{{ product.barcode }}</span>
            </div>
            <div class="meta-row" v-if="product.unit">
              <span class="meta-label">计量单位：</span>
              <span class="meta-value">{{ product.unit }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">商品重量：</span>
              <span class="meta-value">{{ product.weight }} 克(g)</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">货号：</span>
              <span class="meta-value">{{ getProductCode() }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">品牌：</span>
              <span class="meta-value">{{ product.brand }}</span>
            </div>
          </div>

          <!-- 价格信息 -->
          <div class="price-info">
            <div class="price-row">
              <span class="price-label">市场零售价：</span>
              <span class="market-price">¥{{ currentSku ? parseFloat(currentSku.marketRetailPrice || 0).toFixed(2) : parseFloat(product.marketRetailPrice || 0).toFixed(2) }}</span>
            </div>
            <div class="price-row">
              <span class="price-label">建议零售价：</span>
              <span class="suggest-price">
                ¥{{ currentSku ? parseFloat(currentSku.suggestedRetailPrice || 0).toFixed(2) : parseFloat(product.suggestedRetailPrice || 0).toFixed(2) }}
              </span>
            </div>
            <div class="price-row">
              <span class="price-label">{{ getPriceLabel() }}</span>
              <span class="member-price">
                <!-- 根据isMember显示价格：会员显示会员价，普通用户和未登录用户显示基础价格 -->
                ¥{{ getDisplayPrice().toFixed(2) }}
              </span>
            </div>
          </div>

          <!-- 规格选择器（只有当商品启用了规格时才显示） -->
          <div class="spec-selection" v-if="product.enableSpec === 1 && productSpecKeys.length > 0">
            <SpecSelector
              :spec-keys="productSpecKeys"
              :sku-list="productSkuList"
              :default-specs="defaultSpecs"
              @spec-change="handleSpecChange"
            />
          </div>

          <!-- 基础库存信息（未启用规格的商品） -->
          <div class="base-stock-info" v-else>
            <div class="stock-label">库存：</div>
            <div class="stock-value" :class="{ 'low-stock': product.stock <= 10, 'out-stock': product.stock <= 0 }">
              {{ product.stock > 0 ? `${product.stock} ${product.unit}` : '暂无库存' }}
            </div>
          </div>

          <!-- 库存不足提示 -->
          <div v-if="isOutOfStock()" class="out-of-stock-alert">
            <el-alert
              title="商品暂时缺货"
              type="warning"
              :closable="false"
              show-icon
            >
              <template #default>
                <div class="alert-content">
                  <p>该商品目前库存不足，暂时无法购买。</p>
                  <p>您可以进行缺货登记，商品补货后我们会第一时间通知您。</p>
                </div>
              </template>
            </el-alert>
          </div>

          <!-- 购买数量 -->
          <div class="quantity-row">
            <span class="quantity-label">购买数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="getAvailableStock() || 1"
              :disabled="isOutOfStock()"
              size="large"
            />
            <span class="stock-status" :class="getStockStatusClass()">
              {{ getStockStatusText() }}
            </span>
          </div>


          <!-- 操作按钮 -->
          <div class="action-buttons">
            <!-- 有库存时显示正常按钮 -->
            <template v-if="!isOutOfStock()">
              <el-button type="danger" size="large" class="buy-now-btn" @click="buyNow">
                立即购买
              </el-button>
              <el-button 
                size="large" 
                class="add-cart-btn" 
                :loading="addingToCart"
                :disabled="addingToCart"
                @click="addToCart"
              >
                <el-icon v-if="!addingToCart"><ShoppingCart /></el-icon>
                {{ addingToCart ? '加入中...' : '加入购物车' }}
              </el-button>
            </template>
            
            <!-- 缺货时显示缺货登记按钮 -->
            <template v-else>
              <el-button 
                v-if="!hasRegisteredStock"
                type="warning" 
                size="large" 
                class="stock-register-btn-full"
                :loading="registeringStock"
                @click="showStockRegisterDialog"
              >
                <el-icon><Bell /></el-icon>
                缺货登记
              </el-button>
              <el-button 
                v-else
                size="large" 
                class="registered-btn-full"
                disabled
              >
                <el-icon><Check /></el-icon>
                已登记缺货通知
              </el-button>
            </template>
          </div>

          <!-- 收藏 -->
          <div class="favorite-link" @click="toggleFavorite">
            <el-icon v-if="favoritLoading"><Loading /></el-icon>
            <el-icon v-else>
              <Star :class="{ favorited: isFavorited }" />
            </el-icon>
            {{ isFavorited ? '已收藏' : '加入收藏' }}
          </div>
        </div>
        </template>
      </div>
    </div>

    <!-- 商品详情标签页 -->
    <div class="product-tabs">
      <div class="container">
        <el-tabs v-model="activeTab">
          <!-- 商品详情 -->
          <el-tab-pane label="商品详情" name="detail">
            <div class="detail-content" v-html="product.detailHtml"></div>
          </el-tab-pane>

          <!-- 购买咨询 -->
          <el-tab-pane name="consultation">
            <template #label>
              购买咨询({{ consultationCount }})
            </template>
            <div class="consultation-section">
              <div class="tips">
                如果您对本商品有什么问题，请提问咨询吧！
              </div>
              <el-form 
                ref="consultationFormRef"
                :model="consultationForm" 
                :rules="consultationRules"
                label-width="100px"
              >
                <el-form-item label="联系人姓名：" prop="contactName" required>
                  <el-input 
                    v-model="consultationForm.contactName" 
                    placeholder="请输入您的姓名"
                  />
                </el-form-item>
                <el-form-item label="联系电话：" prop="contactPhone">
                  <el-input 
                    v-model="consultationForm.contactPhone" 
                    placeholder="请输入您的联系电话（可选）"
                  />
                </el-form-item>
                <el-form-item label="联系邮箱：" prop="contactEmail">
                  <el-input 
                    v-model="consultationForm.contactEmail" 
                    placeholder="请输入您的邮箱（可选）"
                  />
                </el-form-item>
                <el-form-item label="咨询内容：" prop="consultationContent" required>
                  <el-input
                    v-model="consultationForm.consultationContent"
                    type="textarea"
                    :rows="6"
                    placeholder="请详细描述您的问题..."
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    :loading="submittingConsultation"
                    @click="submitConsultation"
                  >
                    {{ submittingConsultation ? '提交中...' : '提交咨询' }}
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 商品评论 -->
          <el-tab-pane name="review">
            <template #label>
              商品评论 ({{ reviewCount }})
            </template>
            <div class="review-section">
              <div class="tips">
                如果您对本商品有什么使用心得或建议，欢迎分享！
              </div>
              <el-form :model="reviewForm" label-width="100px">
                <el-form-item label="评论标题：" required>
                  <el-input v-model="reviewForm.title" />
                </el-form-item>
                <el-form-item label="联系方式：" required>
                  <el-input v-model="reviewForm.contact" placeholder="(可以是电话、email、qq等)" />
                </el-form-item>
                <el-form-item label="评论内容：" required>
                  <el-input
                    v-model="reviewForm.content"
                    type="textarea"
                    :rows="6"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitReview">提交评论</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 缺货登记对话框 -->
    <el-dialog
      v-model="stockRegisterDialogVisible"
      title="缺货登记"
      width="500px"
      center
    >
      <div class="stock-register-dialog">
        <div class="dialog-tips">
          <el-icon><InfoFilled /></el-icon>
          <span>商品补货后我们将第一时间通知您</span>
        </div>
        
        <el-form 
          ref="stockRegisterFormRef"
          :model="stockRegisterForm" 
          :rules="stockRegisterRules"
          label-width="100px"
        >
          <el-form-item label="商品信息：">
            <div class="product-info-mini">
              <img :src="product.images[0]" alt="" class="mini-image" />
              <div class="mini-details">
                <div class="mini-name">{{ product.name }}</div>
                <div class="mini-price">¥{{ parseFloat(product.price).toFixed(2) }}</div>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item label="联系电话：" prop="contactPhone">
            <el-input 
              v-model="stockRegisterForm.contactPhone" 
              placeholder="请输入您的手机号码"
            />
          </el-form-item>
          
          <el-form-item label="联系邮箱：" prop="contactEmail">
            <el-input 
              v-model="stockRegisterForm.contactEmail" 
              placeholder="请输入您的邮箱地址（可选）"
            />
          </el-form-item>
          
          <el-form-item label="通知方式：">
            <el-radio-group v-model="stockRegisterForm.notifyType">
              <el-radio value="email">邮箱通知</el-radio>
              <el-radio value="sms">短信通知</el-radio>
              <el-radio value="both">邮箱+短信</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="备注说明：">
            <el-input
              v-model="stockRegisterForm.remark"
              type="textarea"
              :rows="3"
              placeholder="您可以留下一些备注信息（可选）"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="stockRegisterDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="registeringStock"
          @click="submitStockRegister"
        >
          确认登记
        </el-button>
      </template>
    </el-dialog>

    <!-- 底部 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Document,
  Warning,
  ShoppingCart,
  Star,
  Loading,
  Bell,
  Check,
  Close,
  InfoFilled
} from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import { getProductById, type ProductVO } from '@/api/buyer/product'
import { addToCart as addToCartAPI, type AddCartDTO } from '@/api/buyer/cart'
import { submitConsultation as submitConsultationAPI, type ConsultationDTO } from '@/api/buyer/consultation'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/buyer/favorite'
import { getSkusByProductId, getSpecKeysByProductId, type ProductSkuVO, type ProductSpecKeyVO } from '@/api/buyer/sku'
import { createStockNotification, checkStockNotificationRegistered, type StockNotificationDTO } from '@/api/buyer/stock-notification'
import { submitReview as submitReviewAPI, type ProductReviewDTO } from '@/api/buyer/review'
import { getCategoryById, type ProductCategoryVO } from '@/api/buyer/productCategory'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import SpecSelector from '@/components/product/SpecSelector.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 当前选中的图片
const currentImage = ref('')

// SKU规格相关数据
const productSpecKeys = ref<ProductSpecKeyVO[]>([])
const productSkuList = ref<ProductSkuVO[]>([])
const selectedSpecs = ref<Record<string, string>>({})
const currentSku = ref<ProductSkuVO | null>(null)
const defaultSpecs = ref<Record<string, string>>({})

// 购买数量
const quantity = ref(1)

// 当前标签页
const activeTab = ref('detail')

// 面包屑路径
const breadcrumbPath = ref<Array<{ id: number; name: string }>>([])

// 咨询和评论数量
const consultationCount = ref(0)
const reviewCount = ref(0)

// 咨询表单
const consultationFormRef = ref<FormInstance>()
const consultationForm = ref({
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  consultationContent: ''
})

// 咨询表单验证规则
const consultationRules: FormRules = {
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  consultationContent: [
    { required: true, message: '请输入咨询内容', trigger: 'blur' },
    { min: 10, max: 1000, message: '咨询内容长度在 10 到 1000 个字符', trigger: 'blur' }
  ]
}

// 提交咨询状态
const submittingConsultation = ref(false)

// 收藏相关状态
const isFavorited = ref(false)
const favoritLoading = ref(false)

// 评论表单
const reviewForm = ref({
  title: '',
  contact: '',
  content: ''
})

// 缺货登记相关状态
const stockRegisterDialogVisible = ref(false)
const registeringStock = ref(false)
const hasRegisteredStock = ref(false)

// 缺货登记表单
const stockRegisterFormRef = ref<FormInstance>()
const stockRegisterForm = ref({
  contactPhone: '',
  contactEmail: '',
  notifyType: 'email',
  remark: ''
})

// 缺货登记表单验证规则
const stockRegisterRules: FormRules = {
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 模拟商品数据
const product = ref({
  id: 0,
  name: '',
  productNo: '',
  weight: 0,
  sku: '',
  barcode: '',
  brand: '',
  unit: '盒',
  marketRetailPrice: 0,
  suggestedRetailPrice: 0,  // 建议零售价
  basePrice: 0,  // 基础价
  price: 0,
  memberPrice: 0,  // 会员价
  stock: 0,  // 添加库存字段
  promoText: '',
  images: [] as string[],
  detailHtml: '',
  isMember: 0 as number | undefined,  // 用户是否是会员（0-普通用户，1-会员）
  enableSpec: 0 as number | undefined  // 是否启用规格（0-否，1-是）
})

// 加载状态
const loading = ref(true)

// 商品不存在状态
const productNotFound = ref(false)

// 商品状态错误信息（下架、草稿、已删除）
const productStatusError = ref<{
  type: 'offline' | 'notfound' // offline-下架, notfound-不存在（草稿或已删除）
  message: string
} | null>(null)

// 加入购物车按钮加载状态
const addingToCart = ref(false)

// 根据分类ID构建面包屑路径（递归向上查找父分类）
const buildCategoryPath = async (categoryId: number) => {
  const path: Array<{ id: number; name: string }> = []
  
  try {
    let currentCategoryId = categoryId
    while (currentCategoryId) {
      const category = await getCategoryById(currentCategoryId)
      path.unshift({ id: category.id, name: category.categoryName })
      
      // 如果有父分类，继续向上查找
      if (category.parentId && category.parentId !== 0) {
        currentCategoryId = category.parentId
      } else {
        break
      }
    }
  } catch (error) {
    console.error('获取分类路径失败:', error)
  }
  
  return path
}

// 加载商品详情
const loadProductDetail = async (productId: number) => {
  loading.value = true
  productStatusError.value = null
  productNotFound.value = false
  try {
    const productData = await getProductById(productId)

    // 检查商品状态：只有上架状态的商品才能访问
    if (productData.status !== '上架') {
      if (productData.status === '下架') {
        // 下架状态：提示商品已下架
        productStatusError.value = {
          type: 'offline',
          message: '商品已下架'
        }
      } else {
        // 草稿或其他状态：提示商品不存在
        productStatusError.value = {
          type: 'notfound',
          message: '商品不存在'
        }
      }
      loading.value = false
      return
    }

    // 将后端返回的 ProductVO 数据映射到页面需要的格式
    // 字段对应关系（根据管理后台）：
    // - marketRetailPrice -> 市场零售价
    // - suggestedRetailPrice -> 建议零售价
    // - basePrice -> 初始会员价（基础价格）
    product.value = {
      id: productData.id,
      name: productData.productName,
      productNo: productData.productCode,
      productCode: productData.productCode,
      weight: productData.weight || 0, // 从API获取重量字段
      barcode: productData.barcode || '', // 条码
      brand: productData.brandName || '暂无', // 从API获取品牌字段
      unit: productData.unit || '', // 计量单位
      marketRetailPrice: productData.marketRetailPrice || 0, // 市场零售价
      suggestedRetailPrice: productData.suggestedRetailPrice || 0, // 建议零售价
      basePrice: productData.basePrice || 0, // 基础价
      price: productData.suggestedRetailPrice || productData.basePrice || 0,
      memberPrice: productData.memberPrice || productData.basePrice || 0, // 会员价（后端已根据用户等级计算）
      stock: productData.stock || 0, // 添加库存字段映射
      promoText: '',
      images: productData.imageList.length > 0 ? productData.imageList : [productData.mainImage],
      detailHtml: `
        <div style="padding: 20px; line-height: 1.8;">
          <h3 style="color: #333; margin-bottom: 20px;">商品描述</h3>
          <p style="color: #666; white-space: pre-wrap;">${productData.description || '暂无详细描述'}</p>
          ${productData.mainImage ? `<img src="${productData.mainImage}" style="max-width: 100%; margin: 20px 0;" />` : ''}
        </div>
      `,
      isMember: productData.isMember, // 从后端获取isMember字段（后端已根据用户ID判断）
      enableSpec: productData.enableSpec, // 是否启用规格（0-否，1-是）
      enableMemberPrice: productData.enableMemberPrice // 是否启用会员价（0-否，1-是）
    }
    
    // 调试日志：检查后端返回的数据（开发环境）
    if (import.meta.env.DEV) {
      console.log('商品详情数据:', {
        isMember: productData.isMember,
        enableSpec: productData.enableSpec,
        memberPrice: productData.memberPrice,
        basePrice: productData.basePrice,
        skus: productData.skus?.map((sku: any) => ({
          id: sku.id,
          price: sku.price,
          memberPrice: sku.memberPrice,
          enableMemberPrice: sku.enableMemberPrice
        }))
      })
    }

    // 设置默认图片
    if (product.value.images.length > 0) {
      currentImage.value = product.value.images[0]
    }

    // 构建面包屑路径
    if (productData.categoryId) {
      breadcrumbPath.value = await buildCategoryPath(productData.categoryId)
    }

    // 只有当商品启用了规格（enableSpec === 1）且有SKU数据时，才加载规格选择器
    if (productData.enableSpec === 1 && productData.skus && productData.skus.length > 0) {
      productSkuList.value = productData.skus
      // 加载规格属性
      const specKeys = await getSpecKeysByProductId(Number(productId))
      productSpecKeys.value = specKeys

      // 设置默认选中的SKU
      if (specKeys.length > 0) {
        let firstSku = productData.skus.find((sku: any) => sku.status === 1 && sku.stock > 0)
        if (!firstSku) {
          firstSku = productData.skus.find((sku: any) => sku.status === 1)
        }
        if (!firstSku) {
          firstSku = productData.skus[0]
        }
        if (firstSku) {
          try {
            const specCombination = JSON.parse(firstSku.specCombination)
            defaultSpecs.value = specCombination
            selectedSpecs.value = { ...specCombination }
            currentSku.value = firstSku
            console.log('默认选中规格:', specCombination, 'SKU:', firstSku)
          } catch (error) {
            console.error('解析默认SKU规格失败:', error)
          }
        }
      }
    } else if (productData.enableSpec === 1) {
      // 启用了规格但没有SKU数据时，单独加载
      await loadProductSkuData(Number(productId))
    } else {
      // 未启用规格，清空规格相关数据
      productSpecKeys.value = []
      productSkuList.value = []
      currentSku.value = null
      selectedSpecs.value = {}
      defaultSpecs.value = {}
    }
  } catch (error: any) {
    console.error('加载商品详情失败:', error)
    // 检查是否是404错误（商品不存在）
    if (error.response?.status === 404 || error.message?.includes('商品不存在')) {
      productNotFound.value = true
    } else {
      ElMessage.error('加载商品详情失败')
    }
  } finally {
    loading.value = false
  }
}

// 加载商品SKU数据
const loadProductSkuData = async (productId: number) => {
  try {
    // 并行加载规格属性和SKU列表
    const [specKeys, skuList] = await Promise.all([
      getSpecKeysByProductId(productId),
      getSkusByProductId(productId)
    ])
    
    productSpecKeys.value = specKeys
    productSkuList.value = skuList

    // 如果有SKU数据，设置默认选中第一个SKU的规格（无论库存多少，都显示所有规格）
    if (skuList.length > 0 && specKeys.length > 0) {
      // 优先选择启用且有库存的SKU，如果没有则选择第一个启用的SKU
      let firstSku = skuList.find(sku => sku.status === 1 && sku.stock > 0)
      if (!firstSku) {
        firstSku = skuList.find(sku => sku.status === 1)
      }
      // 如果连启用的都没有，就选第一个
      if (!firstSku) {
        firstSku = skuList[0]
      }

      if (firstSku) {
        try {
          const specCombination = JSON.parse(firstSku.specCombination)
          defaultSpecs.value = specCombination
          selectedSpecs.value = { ...specCombination }
          currentSku.value = firstSku
          console.log('默认选中规格:', specCombination, 'SKU ID:', firstSku.id)
        } catch (error) {
          console.error('解析默认SKU规格失败:', error)
        }
      }
    }
  } catch (error) {
    console.error('加载商品SKU数据失败:', error)
    // 如果SKU数据加载失败，不影响商品基本信息显示
  }
}

// 处理规格选择变化
const handleSpecChange = (newSelectedSpecs: Record<string, string>, newCurrentSku: ProductSkuVO | null) => {
  // 如果商品启用了SKU，确保至少有一个SKU被选中
  if (product.value.enableSpec === 1 && productSkuList.value.length > 0) {
    // 如果所有规格都被取消选择，恢复默认选中的第一个SKU
    const selectedCount = Object.keys(newSelectedSpecs).length
    if (selectedCount === 0) {
      // 恢复默认选中的第一个SKU
      let firstSku = productSkuList.value.find(sku => sku.status === 1 && sku.stock > 0)
      if (!firstSku) {
        firstSku = productSkuList.value.find(sku => sku.status === 1)
      }
      if (!firstSku) {
        firstSku = productSkuList.value[0]
      }
      
      if (firstSku) {
        try {
          const specCombination = JSON.parse(firstSku.specCombination)
          selectedSpecs.value = { ...specCombination }
          currentSku.value = firstSku
          // 重置购买数量为1
          quantity.value = 1
          return
        } catch (error) {
          console.error('解析默认SKU规格失败:', error)
        }
      }
    }
  }
  
  selectedSpecs.value = newSelectedSpecs
  currentSku.value = newCurrentSku
  
  // 重置购买数量为1
  quantity.value = 1
}

// 获取库存状态文本
const getStockStatusText = () => {
  if (currentSku.value) {
    const stock = currentSku.value.stock
    const warningStock = currentSku.value.warningStock
    
    if (stock <= 0) {
      return '库存-缺货'
    } else if (stock <= warningStock) {
      return `库存-紧张 (剩余${stock}件)`
    } else {
      return '库存-充足'
    }
  }
  // 没有SKU时显示商品基础库存状态
  const baseStock = product.value.stock
  return baseStock > 0 ? '库存-充足' : '库存-缺货'
}

// 获取库存状态样式类
const getStockStatusClass = () => {
  if (currentSku.value) {
    const stock = currentSku.value.stock
    const warningStock = currentSku.value.warningStock
    
    if (stock <= 0) {
      return 'out-of-stock'
    } else if (stock <= warningStock) {
      return 'low-stock'
    }
  } else {
    // 没有SKU时检查商品基础库存
    const baseStock = product.value.stock
    if (baseStock <= 0) {
      return 'out-of-stock'
    }
  }
  return ''
}

// 获取可用库存数量
const getAvailableStock = () => {
  if (currentSku.value) {
    return currentSku.value.stock || 0
  }
  // 没有SKU时使用商品基础库存
  return product.value.stock || 0
}

// 初始化
onMounted(async () => {
  // 根据路由参数加载商品数据
  const productId = route.params.id
  if (productId) {
    await loadProductDetail(Number(productId))
    // 加载商品后检查收藏状态和缺货登记状态
    await checkFavoriteStatus()
    await checkStockRegisterStatus()
  } else {
    ElMessage.error('商品ID不存在')
    router.push('/')
  }
})

// 立即购买
const buyNow = async () => {
  // 检查登录状态
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // 检查商品信息是否存在
  if (!product.value.id) {
    ElMessage.error('商品信息不存在')
    return
  }

  // 检查规格选择（仅当商品启用了规格且有规格数据时）
  if (productSpecKeys.value.length > 0 && !currentSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }

  // 检查库存（如果有SKU用SKU库存，否则用商品基础库存）
  const availableStock = currentSku.value ? currentSku.value.stock : product.value.stock
  if (availableStock <= 0) {
    ElMessage.error('商品库存不足')
    return
  }

  if (quantity.value > availableStock) {
    ElMessage.error(`购买数量不能超过库存数量 ${availableStock}`)
    return
  }

  try {
    // 静默加入购物车（用户无感知）
    const cartData: AddCartDTO = {
      productId: product.value.id,
      quantity: quantity.value,
      ...(currentSku.value?.id && { skuId: currentSku.value.id })
    }

    const response = await addToCartAPI(cartData)
    const cartId = response.data || response // 兼容不同的返回格式
    
    // 直接跳转到结算页面，用户无感知购物车过程
    router.push(`/cart/checkout?cartIds=${cartId}`)
  } catch (error: any) {
    console.error('立即购买失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('请先登录')
      router.push('/login')
    } else {
      ElMessage.error(error.response?.data?.message || '立即购买失败，请重试')
    }
  }
}


// 加入购物车
const addToCart = async () => {
  // 检查登录状态
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (addingToCart.value) return
  
  try {
    if (!product.value.id) {
      ElMessage.error('商品信息不存在')
      return
    }

    // 检查规格选择（仅当商品启用了规格且有规格数据时）
    if (productSpecKeys.value.length > 0 && !currentSku.value) {
      ElMessage.warning('请选择商品规格')
      return
    }

    // 检查库存（如果有SKU用SKU库存，否则用商品基础库存）
    const availableStock = getAvailableStock()
    if (availableStock <= 0) {
      ElMessage.error('商品库存不足')
      return
    }

    if (quantity.value > availableStock) {
      ElMessage.error(`购买数量不能超过库存数量 ${availableStock}`)
      // 自动调整数量为最大库存
      quantity.value = availableStock
      return
    }

    addingToCart.value = true

    // 构建规格组合JSON字符串
    let specCombination: string | undefined = undefined
    if (currentSku.value && currentSku.value.specCombination) {
      specCombination = currentSku.value.specCombination
    } else if (selectedSpecs.value && Object.keys(selectedSpecs.value).length > 0) {
      // 如果没有SKU但有选中的规格，手动构建JSON
      specCombination = JSON.stringify(selectedSpecs.value)
    }

    const cartData: AddCartDTO = {
      productId: product.value.id,
      quantity: quantity.value,
      ...(currentSku.value?.id && { skuId: currentSku.value.id }),
      ...(specCombination && { specCombination })
    }

    await addToCartAPI(cartData)
    
    // 更新购物车数量
    await cartStore.updateCartCount()
    
    ElMessage.success('已成功加入购物车！')
  } catch (error: any) {
    console.error('加入购物车失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('请先登录')
      router.push('/login')
    } else {
      ElMessage.error(error.response?.data?.message || '加入购物车失败，请重试')
    }
  } finally {
    addingToCart.value = false
  }
}

// 提交咨询
const submitConsultation = async () => {
  // 检查登录状态
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!consultationFormRef.value) return

  await consultationFormRef.value.validate(async (valid) => {
    if (!valid) return

    if (!product.value.id) {
      ElMessage.error('商品信息不存在')
      return
    }

    try {
      submittingConsultation.value = true

      const consultationData: ConsultationDTO = {
        productId: product.value.id,
        contactName: consultationForm.value.contactName,
        contactPhone: consultationForm.value.contactPhone || undefined,
        contactEmail: consultationForm.value.contactEmail || undefined,
        consultationContent: consultationForm.value.consultationContent
      }

      await submitConsultationAPI(consultationData)

      ElMessage.success('咨询提交成功！我们会尽快回复您')

      // 重置表单
      consultationForm.value = {
        contactName: '',
        contactPhone: '',
        contactEmail: '',
        consultationContent: ''
      }
      consultationFormRef.value?.resetFields()

    } catch (error: any) {
      console.error('提交咨询失败:', error)
      ElMessage.error(error.response?.data?.message || '提交咨询失败，请重试')
    } finally {
      submittingConsultation.value = false
    }
  })
}

// 提交评论
const submitReview = async () => {
  // 检查登录状态
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!reviewForm.value.content || !reviewForm.value.content.trim()) {
    ElMessage.warning('请填写评论内容')
    return
  }

  try {
    const reviewData: ProductReviewDTO = {
      productId: product.value.id!,
      orderId: 1, // 临时使用固定orderId，实际应该从已完成订单中选择
      rating: 0, // 无评分
      reviewContent: `${reviewForm.value.title ? reviewForm.value.title + '\n' : ''}${reviewForm.value.content}`
    }

    await submitReviewAPI(reviewData)
    ElMessage.success('评论提交成功，等待审核！')

    // 清空表单
    reviewForm.value.title = ''
    reviewForm.value.contact = ''
    reviewForm.value.content = ''
  } catch (error: any) {
    console.error('提交评论失败:', error)
    ElMessage.error(error.response?.data?.message || '提交评论失败，请重试')
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  if (!product.value.id) return
  // 只在用户已登录时检查收藏状态
  if (!userStore.userInfo) {
    isFavorited.value = false
    return
  }
  try {
    isFavorited.value = await checkFavorite(product.value.id)
  } catch (error) {
    console.error('检查收藏状态失败:', error)
    // 发生错误时默认为未收藏
    isFavorited.value = false
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  // 检查登录状态
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!product.value.id) {
    ElMessage.error('商品信息不存在')
    return
  }

  favoritLoading.value = true
  try {
    if (isFavorited.value) {
      await removeFavorite(product.value.id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(product.value.id)
      isFavorited.value = true
      ElMessage.success('已加入收藏')
    }
  } catch (error) {
    console.error('操作收藏失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    favoritLoading.value = false
  }
}

// 判断商品是否缺货
const isOutOfStock = () => {
  if (currentSku.value) {
    return currentSku.value.stock <= 0
  }
  // 没有SKU时检查商品基础库存
  const baseStock = product.value.stock
  return baseStock <= 0
}

// 检查缺货登记状态
const checkStockRegisterStatus = async () => {
  if (!product.value.id) return
  // 只在用户已登录时检查缺货登记状态
  if (!userStore.userInfo) {
    hasRegisteredStock.value = false
    return
  }
  try {
    hasRegisteredStock.value = await checkStockNotificationRegistered(product.value.id)
  } catch (error: any) {
    console.error('检查缺货登记状态失败:', error)
    // 如果是网络错误或服务未启动，默认为未登记状态，不影响页面使用
    hasRegisteredStock.value = false
    
    // 只有在开发环境下才显示错误提示
    if (import.meta.env.DEV) {
      console.warn('缺货登记功能可能需要后端服务支持，当前将忽略此功能')
    }
  }
}

// 显示缺货登记对话框
const showStockRegisterDialog = () => {
  stockRegisterDialogVisible.value = true
}

// 获取价格标签（优先判断用户是否是会员，再判断商品或SKU是否启用会员价）
const getPriceLabel = () => {
  // 1. 优先判断：如果不是会员或未登录，统一显示"商品价格"
  const isMember = product.value.isMember === 1
  if (!isMember) {
    return '商品价格：'
  }
  
  // 2. 如果是会员，再判断商品或SKU是否启用会员价
  // 如果商品启用了SKU且有当前选中的SKU，优先检查SKU是否启用会员价
  if (product.value.enableSpec === 1 && currentSku.value && currentSku.value.enableMemberPrice === 1) {
    return '会员价：'
  }
  // 如果没有SKU或SKU未启用会员价，检查商品是否启用会员价
  if (product.value.enableMemberPrice === 1) {
    return '会员价：'
  }
  // 会员但商品/SKU都没有启用会员价，显示"商品价格"
  return '商品价格：'
}

// 获取显示价格（根据用户是否是会员）
// 完全依赖后端返回的isMember字段和memberPrice（后端已根据用户ID判断）
const getDisplayPrice = () => {
  // 后端返回的isMember字段：1-会员，0-普通用户，undefined-未登录
  const isMember = product.value.isMember === 1
  
  // 未登录用户：直接显示基础价格
  if (product.value.isMember === undefined || product.value.isMember === null) {
    // SKU的price字段就是基础价格
    if (currentSku.value && currentSku.value.price != null) {
      return parseFloat(currentSku.value.price)
    }
    return parseFloat(product.value.basePrice ?? 0)
  }
  
  if (isMember) {
    // 会员用户显示会员价（后端已计算好）
    if (currentSku.value && currentSku.value.memberPrice != null) {
      return parseFloat(currentSku.value.memberPrice)
    }
    // 如果没有SKU或SKU没有会员价，使用商品的会员价
    if (product.value.memberPrice != null) {
      return parseFloat(product.value.memberPrice)
    }
    // 如果都没有，返回原价（不应该发生，但作为兜底）
    return parseFloat(product.value.basePrice ?? 0)
  } else {
    // 普通用户显示原价（基础价格）
    // SKU的price字段就是基础价格
    if (currentSku.value && currentSku.value.price != null) {
      return parseFloat(currentSku.value.price)
    }
    return parseFloat(product.value.basePrice ?? 0)
  }
}

// 获取货号：如果启用SKU且有当前SKU，显示SKU编码，否则显示商品编码
const getProductCode = () => {
  // 如果商品启用了SKU且有当前选中的SKU，显示SKU编码
  if (product.value.enableSpec === 1 && currentSku.value && currentSku.value.skuCode) {
    return currentSku.value.skuCode
  }
  // 否则显示商品编码
  return product.value.productNo || product.value.productCode || ''
}

// 提交缺货登记
const submitStockRegister = async () => {
  if (!stockRegisterFormRef.value) return

  await stockRegisterFormRef.value.validate(async (valid) => {
    if (!valid) return

    if (!product.value.id) {
      ElMessage.error('商品信息不存在')
      return
    }

    try {
      registeringStock.value = true

      const registerData: StockNotificationDTO = {
        productId: product.value.id,
        contactPhone: stockRegisterForm.value.contactPhone,
        contactEmail: stockRegisterForm.value.contactEmail || undefined,
        notifyType: stockRegisterForm.value.notifyType,
        remark: stockRegisterForm.value.remark || undefined
      }

      await createStockNotification(registerData)
      
      ElMessage.success('缺货登记成功！商品补货时我们会及时通知您')
      stockRegisterDialogVisible.value = false
      hasRegisteredStock.value = true
      
      // 重置表单
      stockRegisterForm.value = {
        contactPhone: '',
        contactEmail: '',
        notifyType: 'email',
        remark: ''
      }
      stockRegisterFormRef.value?.resetFields()
      
    } catch (error: any) {
      console.error('缺货登记失败:', error)
      ElMessage.error(error.response?.data?.message || '登记失败，请重试')
    } finally {
      registeringStock.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.product-detail {
  background: #f5f5f5;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }

  // 面包屑导航
  .breadcrumb {
    background: #fff;
    padding: 12px 0;
    font-size: 12px;
    color: #666;

    a {
      color: #666;
      text-decoration: none;
      transition: color 0.3s;

      &:hover {
        color: #e4393c;
      }
    }

    .current {
      color: #e4393c;
    }
  }

  // 商品主体信息
  .product-main {
    background: #fff;
    padding: 30px 0;
    min-height: 500px;

    .container {
      display: flex;
      gap: 40px;
    }

    .loading-container {
      width: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 100px 0;
      color: #999;

      .el-icon {
        margin-bottom: 10px;
      }
    }

    .product-not-found {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 100px 0;
      min-height: 500px;

      .not-found-content {
        text-align: center;
        max-width: 500px;

        .not-found-icon {
          color: #f56c6c;
          margin-bottom: 20px;
        }

        .not-found-title {
          font-size: 24px;
          color: #333;
          margin: 0 0 15px 0;
          font-weight: bold;
        }

        .not-found-message {
          font-size: 16px;
          color: #666;
          margin: 0 0 30px 0;
          line-height: 1.6;
        }

        .not-found-actions {
          display: flex;
          gap: 15px;
          justify-content: center;
        }
      }
    }
  }

  // 商品图片区
  .product-gallery {
    flex: 0 0 400px;

    .main-image {
      width: 400px;
      height: 400px;
      border: 1px solid #eee;
      margin-bottom: 15px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .thumbnail-list {
      display: flex;
      gap: 10px;
      margin-bottom: 15px;

      .thumbnail-item {
        width: 90px;
        height: 90px;
        border: 2px solid transparent;
        cursor: pointer;
        transition: border-color 0.3s;

        &.active,
        &:hover {
          border-color: #e4393c;
        }

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
    }

    .view-detail-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 10px;
      background: #f5f5f5;
      border: 1px solid #ddd;
      border-radius: 4px;
      cursor: pointer;
      transition: background 0.3s;

      &:hover {
        background: #e8e8e8;
      }
    }
  }

  // 商品信息区
  .product-info {
    flex: 1;

    .product-title {
      font-size: 20px;
      font-weight: bold;
      color: #333;
      margin: 0 0 15px 0;
      line-height: 1.5;
    }

    .promo-info {
      background: #fff5f5;
      border: 1px solid #ffe6e6;
      padding: 12px;
      margin-bottom: 20px;
      font-size: 13px;
      line-height: 1.6;

      .promo-label {
        color: #e4393c;
        font-weight: bold;
      }

      .promo-text {
        color: #666;
      }
    }

    .product-meta {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
      padding: 20px 0;
      border-top: 1px solid #eee;
      border-bottom: 1px solid #eee;
      margin-bottom: 20px;

      .meta-row {
        font-size: 14px;

        .meta-label {
          color: #999;
        }

        .meta-value {
          color: #333;
        }
      }
    }

    .price-info {
      padding: 20px 0;
      border-bottom: 1px solid #eee;
      margin-bottom: 20px;

      .price-row {
        display: flex;
        align-items: baseline;
        margin-bottom: 10px;
        font-size: 14px;

        .price-label {
          color: #999;
          width: 100px;
        }

        .market-price {
          color: #999;
          text-decoration: line-through;
        }

        .suggest-price {
          color: #e4393c;
          font-size: 24px;
          font-weight: bold;
        }

        .member-price {
          color: #ff6600;
          font-size: 20px;
          font-weight: bold;
        }
      }
    }

    .spec-selection {
      padding: 20px 0;
      border-bottom: 1px solid #eee;
      margin-bottom: 20px;

      .spec-row {
        display: flex;
        align-items: center;
        margin-bottom: 15px;
        font-size: 14px;

        &:last-child {
          margin-bottom: 0;
        }

        .spec-label {
          color: #333;
          width: 100px;
        }

        .spec-value {
          color: #e4393c;
          font-weight: bold;
        }

        .spec-options {
          display: flex;
          gap: 10px;

          .spec-option {
            padding: 8px 20px;
            border: 1px solid #ddd;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s;

            &.active,
            &:hover {
              border-color: #e4393c;
              color: #e4393c;
              background: #fff5f5;
            }
          }
        }
      }
    }

    .base-stock-info {
      padding: 20px 0;
      border-bottom: 1px solid #eee;
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      gap: 15px;

      .stock-label {
        color: #333;
        font-size: 14px;
        font-weight: bold;
      }

      .stock-value {
        color: #52c41a;
        font-size: 16px;
        font-weight: bold;

        &.low-stock {
          color: #faad14;
        }

        &.out-stock {
          color: #cf1322;
        }
      }
    }

    .out-of-stock-alert {
      padding: 20px 0;
      margin-bottom: 20px;

      .alert-content {
        p {
          margin: 5px 0;
          line-height: 1.6;
          color: #666;
        }
      }
    }

    .quantity-row {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 20px 0;
      border-bottom: 1px solid #eee;
      margin-bottom: 20px;

      .quantity-label {
        color: #333;
        font-size: 14px;
      }

      .stock-status {
        color: #52c41a;
        font-size: 14px;
        
        &.low-stock {
          color: #faad14;
        }
        
        &.out-of-stock {
          color: #ff4d4f;
        }
      }
    }

    .buy-mode-tip {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #f56c6c;
      font-size: 14px;
      margin-bottom: 20px;
    }

    .action-buttons {
      display: flex;
      gap: 15px;
      margin-bottom: 20px;

      .buy-now-btn {
        flex: 1;
        height: 50px;
        font-size: 16px;
        font-weight: bold;
      }

      .add-cart-btn {
        flex: 1;
        height: 50px;
        font-size: 16px;
        color: #fff;
        background: #4285f4;
        border-color: #4285f4;

        &:hover {
          background: #3367d6;
          border-color: #3367d6;
        }
      }

      .stock-register-btn {
        flex: 1;
        height: 50px;
        font-size: 16px;
        font-weight: bold;
      }

      .stock-register-btn-full {
        width: 100%;
        height: 50px;
        font-size: 16px;
        font-weight: bold;
      }

      .registered-btn {
        flex: 1;
        height: 50px;
        font-size: 16px;
        color: #52c41a;
        border-color: #52c41a;
        cursor: not-allowed;
      }

      .registered-btn-full {
        width: 100%;
        height: 50px;
        font-size: 16px;
        color: #52c41a;
        border-color: #52c41a;
        cursor: not-allowed;
      }

      .out-of-stock-btn {
        flex: 1;
        height: 50px;
        font-size: 16px;
        color: #999;
        border-color: #d9d9d9;
        cursor: not-allowed;
      }
    }

    .favorite-link {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 5px;
      color: #666;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: #e4393c;
      }

      .favorited {
        color: #e4393c;
      }
    }
  }

  // 商品详情标签页
  .product-tabs {
    background: #fff;
    margin-top: 20px;
    padding: 30px 0;

    .detail-content {
      padding: 20px;
      line-height: 1.8;
    }

    .consultation-section,
    .review-section {
      padding: 20px;

      .tips {
        background: #f0f9ff;
        border: 1px solid #d0ebff;
        padding: 12px;
        margin-bottom: 20px;
        color: #666;
        font-size: 14px;
      }

      // 确保表单标签不换行
      :deep(.el-form-item__label) {
        white-space: nowrap;
        word-break: keep-all;
      }
    }
  }

  // 缺货登记对话框样式
  .stock-register-dialog {
    .dialog-tips {
      display: flex;
      align-items: center;
      gap: 8px;
      background: #f0f9ff;
      border: 1px solid #d0ebff;
      padding: 12px;
      margin-bottom: 20px;
      border-radius: 4px;
      color: #1890ff;
      font-size: 14px;
    }

    .product-info-mini {
      display: flex;
      align-items: center;
      gap: 12px;

      .mini-image {
        width: 60px;
        height: 60px;
        object-fit: cover;
        border: 1px solid #eee;
        border-radius: 4px;
      }

      .mini-details {
        flex: 1;

        .mini-name {
          font-size: 14px;
          color: #333;
          margin-bottom: 4px;
          font-weight: 500;
        }

        .mini-price {
          font-size: 16px;
          color: #e4393c;
          font-weight: bold;
        }
      }
    }
  }
}
</style>
