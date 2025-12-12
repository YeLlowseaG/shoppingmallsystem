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
        <span> > </span>
        <router-link to="/products?categoryId=3">避孕润滑</router-link>
        <span> > </span>
        <router-link to="/products?categoryId=31">安全套</router-link>
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
          <div class="view-detail-btn">
            <el-icon><Document /></el-icon>
            查看商品详情
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
            <div class="meta-row">
              <span class="meta-label">商品重量：</span>
              <span class="meta-value">{{ product.weight }} 克(g)</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">货号：</span>
              <span class="meta-value">{{ product.sku }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">条码：</span>
              <span class="meta-value">{{ product.barcode }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">品牌：</span>
              <span class="meta-value">{{ product.brand }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">计量单位：</span>
              <span class="meta-value">{{ product.unit }}</span>
            </div>
          </div>

          <!-- 价格信息 -->
          <div class="price-info">
            <div class="price-row">
              <span class="price-label">市场零售价：</span>
              <span class="market-price">¥{{ product.marketPrice }}</span>
            </div>
            <div class="price-row">
              <span class="price-label">建议零售价：</span>
              <span class="suggest-price">¥ {{ product.price }}</span>
            </div>
          </div>

          <!-- 规格选择 -->
          <div class="spec-selection">
            <div class="spec-row">
              <span class="spec-label">您已选择：</span>
              <span class="spec-value">"{{ selectedSpec }}"</span>
            </div>
            <div class="spec-row">
              <span class="spec-label">规格：</span>
              <div class="spec-options">
                <div
                  v-for="spec in product.specs"
                  :key="spec"
                  class="spec-option"
                  :class="{ active: selectedSpec === spec }"
                  @click="selectedSpec = spec"
                >
                  {{ spec }}
                </div>
              </div>
            </div>
          </div>

          <!-- 购买数量 -->
          <div class="quantity-row">
            <span class="quantity-label">购买数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="999"
              size="large"
            />
            <span class="stock-status">库存-充足</span>
          </div>

          <!-- 显示购买模式提示 -->
          <div class="buy-mode-tip">
            <el-icon color="#f56c6c"><Warning /></el-icon>
            <span>显示批发购买模式</span>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
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
          </div>

          <!-- 收藏 -->
          <div class="favorite-link">
            <el-icon><Star /></el-icon>
            加入收藏
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
                <el-form-item label="*联系人姓名：" prop="contactName">
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
                <el-form-item label="*咨询内容：" prop="consultationContent">
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
                <el-form-item label="*评论标题：">
                  <el-input v-model="reviewForm.title" />
                </el-form-item>
                <el-form-item label="*联系方式：">
                  <el-input v-model="reviewForm.contact" placeholder="(可以是电话、email、qq等)" />
                </el-form-item>
                <el-form-item label="*评论内容：">
                  <el-input
                    v-model="reviewForm.content"
                    type="textarea"
                    :rows="6"
                  />
                </el-form-item>
                <el-form-item label="*验证码：">
                  <el-input v-model="reviewForm.captcha" style="width: 120px" />
                  <img src="https://via.placeholder.com/100x40?text=1547" class="captcha-img" />
                  <span class="captcha-tip">看不清楚?换个图片</span>
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
  Loading
} from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import { getProductById, type ProductVO } from '@/api/buyer/product'
import { addToCart as addToCartAPI, type AddCartDTO } from '@/api/buyer/cart'
import { submitConsultation as submitConsultationAPI, type ConsultationDTO } from '@/api/buyer/consultation'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

// 当前选中的图片
const currentImage = ref('')

// 选中的规格
const selectedSpec = ref('2只装')

// 购买数量
const quantity = ref(1)

// 当前标签页
const activeTab = ref('detail')

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

// 评论表单
const reviewForm = ref({
  title: '',
  contact: '',
  content: '',
  captcha: ''
})

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
  marketPrice: 0,
  price: 0,
  specs: ['标准'],
  promoText: '',
  images: [] as string[],
  detailHtml: ''
})

// 加载状态
const loading = ref(true)

// 加入购物车按钮加载状态
const addingToCart = ref(false)

// 加载商品详情
const loadProductDetail = async (productId: number) => {
  loading.value = true
  try {
    const productData = await getProductById(productId)

    // 将后端返回的 ProductVO 数据映射到页面需要的格式
    product.value = {
      id: productData.id,
      name: productData.productName,
      productNo: productData.productCode,
      weight: 0, // API 暂无重量字段
      sku: productData.productCode,
      barcode: '', // API 暂无条码字段
      brand: '', // API 暂无品牌字段
      unit: '盒',
      marketPrice: productData.basePrice * 1.5, // 原价设为基础价的1.5倍
      price: productData.basePrice,
      specs: ['标准'],
      promoText: '',
      images: productData.imageList.length > 0 ? productData.imageList : [productData.mainImage],
      detailHtml: `
        <div style="padding: 20px; line-height: 1.8;">
          <h3 style="color: #333; margin-bottom: 20px;">商品描述</h3>
          <p style="color: #666; white-space: pre-wrap;">${productData.description || '暂无详细描述'}</p>
          ${productData.mainImage ? `<img src="${productData.mainImage}" style="max-width: 100%; margin: 20px 0;" />` : ''}
        </div>
      `
    }

    // 设置默认图片
    if (product.value.images.length > 0) {
      currentImage.value = product.value.images[0]
    }
  } catch (error) {
    console.error('加载商品详情失败:', error)
    ElMessage.error('加载商品详情失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  // 根据路由参数加载商品数据
  const productId = route.params.id
  if (productId) {
    loadProductDetail(Number(productId))
  } else {
    ElMessage.error('商品ID不存在')
    router.push('/')
  }
})

// 立即购买
const buyNow = () => {
  ElMessage.warning('立即购买功能开发中...')
}

// 加入购物车
const addToCart = async () => {
  if (addingToCart.value) return
  
  try {
    if (!product.value.id) {
      ElMessage.error('商品信息不存在')
      return
    }

    addingToCart.value = true

    const cartData: AddCartDTO = {
      productId: product.value.id,
      quantity: quantity.value
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
const submitReview = () => {
  ElMessage.success('评论提交成功！')
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

      .captcha-img {
        margin-left: 10px;
        vertical-align: middle;
        cursor: pointer;
      }

      .captcha-tip {
        margin-left: 10px;
        color: #999;
        font-size: 12px;
      }
    }
  }
}
</style>
