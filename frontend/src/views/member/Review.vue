<template>
  <div class="member-page">
    <!-- 顶部提示条 -->
    <TopBar />

    <!-- Logo + 搜索 + 联系方式 -->
    <Header />

    <!-- 主导航 + 全部分类 -->
    <Navbar />

    <!-- 会员中心内容区域 -->
    <div class="member-content">
      <div class="container">
        <!-- 会员中心标题栏 -->
        <MemberHeaderBar />

        <!-- 会员中心主体 -->
        <div class="member-main">
          <!-- 左侧导航菜单 -->
          <MemberSidebar active-menu="transaction/orders" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <div class="review-wrapper">
              <!-- 页面标题 -->
              <div class="page-title">
                商品评价
                <span v-if="orderNo" class="order-info">（订单号：{{ orderNo }}）</span>
              </div>

              <!-- 加载状态 -->
              <div v-if="loading" class="loading-wrapper">
                <el-skeleton :rows="5" animated />
              </div>

              <!-- 评价表单 -->
              <div v-else-if="orderItems.length > 0" class="review-form-wrapper">
                <div
                  v-for="item in orderItems"
                  :key="item.productId"
                  class="product-review-item"
                >
                  <!-- 商品信息 -->
                  <div class="product-info">
                    <img 
                      :src="item.productImage || 'https://via.placeholder.com/80x80'" 
                      :alt="item.productName"
                      class="product-image"
                    />
                    <div class="product-details">
                      <h4 class="product-name">{{ item.productName }}</h4>
                      <div class="product-specs">
                        数量：{{ item.quantity }} | 单价：¥{{ item.price }}
                      </div>
                    </div>
                  </div>

                  <!-- 评价表单 -->
                  <el-form
                    :ref="(el: any) => setFormRef(el, item.productId)"
                    :model="item.reviewForm"
                    :rules="reviewRules"
                    class="review-form"
                    label-width="100px"
                  >
                    <el-form-item label="商品评分" prop="rating">
                      <div class="rating-wrapper">
                        <el-rate 
                          v-model="item.reviewForm.rating" 
                          size="large"
                          show-text
                        />
                        <span class="rating-text">{{ getRatingText(item.reviewForm.rating) }}</span>
                      </div>
                    </el-form-item>

                    <el-form-item label="评价内容">
                      <el-input
                        v-model="item.reviewForm.reviewContent"
                        type="textarea"
                        :rows="4"
                        placeholder="分享您对这个商品的使用感受..."
                        maxlength="500"
                        show-word-limit
                      />
                    </el-form-item>

                    <el-form-item label="上传图片">
                      <div class="upload-wrapper">
                        <el-upload
                          v-model:file-list="item.reviewForm.fileList"
                          action="/api/common/upload/image"
                          list-type="picture-card"
                          :limit="5"
                          accept="image/*"
                          :on-success="(response: any, file: any) => handleUploadSuccess(response, file, item)"
                          :on-error="(error: any) => handleUploadError(error)"
                          :on-remove="(file: any) => handleRemoveImage(file, item)"
                          :before-upload="beforeUpload"
                        >
                          <el-icon><Plus /></el-icon>
                        </el-upload>
                        <div class="upload-tips">
                          最多上传5张图片，支持JPG、PNG格式，每张图片不超过2MB
                        </div>
                      </div>
                    </el-form-item>
                  </el-form>
                </div>

                <!-- 提交按钮 -->
                <div class="submit-wrapper">
                  <el-button @click="handleCancel">取消</el-button>
                  <el-button 
                    type="primary" 
                    :loading="submitting"
                    @click="handleSubmitAll"
                  >
                    {{ submitting ? '提交中...' : '提交评价' }}
                  </el-button>
                </div>
              </div>

              <!-- 空状态 -->
              <div v-else class="empty-state">
                <el-empty description="没有找到可评价的商品">
                  <el-button @click="$router.push('/member/orders')">返回订单列表</el-button>
                </el-empty>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { getOrderDetail, type OrderItemVO } from '@/api/buyer/order'
import { submitReview, canReviewProduct, type ProductReviewDTO } from '@/api/buyer/review'

const route = useRoute()
const router = useRouter()
const unreadMessageCount = ref(0)
const loading = ref(false)
const submitting = ref(false)

// 路由参数
const orderId = ref<number>(Number(route.query.orderId))
const orderNo = ref<string>(String(route.query.orderNo))

// 订单商品列表
interface OrderItemWithReview extends OrderItemVO {
  reviewForm: {
    rating: number
    reviewContent: string
    reviewImages: string[]
    fileList: any[]
  }
}

const orderItems = ref<OrderItemWithReview[]>([])

// 表单引用
const formRefs = ref<Map<number, FormInstance>>(new Map())

// 设置表单引用
const setFormRef = (el: any, productId: number) => {
  if (el) {
    formRefs.value.set(productId, el)
  }
}

// 表单验证规则
const reviewRules: FormRules = {
  rating: [
    { required: true, message: '请为商品评分', trigger: 'change' },
    { type: 'number', min: 1, max: 5, message: '评分必须在1-5星之间', trigger: 'change' }
  ]
}

// 获取评分文本
const getRatingText = (rating: number) => {
  const texts = ['', '非常差', '差', '一般', '好', '非常好']
  return texts[rating] || ''
}

// 上传前验证
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  return true
}

// 上传成功处理
const handleUploadSuccess = (response: any, file: any, item: OrderItemWithReview) => {
  if (response.code === 200 && response.data) {
    item.reviewForm.reviewImages.push(response.data.url)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// 上传失败处理
const handleUploadError = (error: any) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
}

// 移除图片
const handleRemoveImage = (file: any, item: OrderItemWithReview) => {
  // 从reviewImages中移除对应的URL
  if (file.response && file.response.data) {
    const urlIndex = item.reviewForm.reviewImages.indexOf(file.response.data.url)
    if (urlIndex > -1) {
      item.reviewForm.reviewImages.splice(urlIndex, 1)
    }
  }
}

// 加载订单详情
const loadOrderDetail = async () => {
  if (!orderId.value) {
    ElMessage.error('订单ID不存在')
    router.push('/member/orders')
    return
  }

  loading.value = true
  try {
    // 这里需要实现获取订单详情的API，暂时模拟数据
    // const orderDetail = await getOrderDetail(orderId.value)
    
    // 模拟订单商品数据
    const mockItems: OrderItemWithReview[] = [
      {
        id: 1,
        productId: 1,
        productName: '测试商品1',
        productImage: 'https://via.placeholder.com/80x80',
        quantity: 2,
        price: 99.00,
        reviewForm: {
          rating: 0,
          reviewContent: '',
          reviewImages: [],
          fileList: []
        }
      }
    ]

    // 检查每个商品是否可以评价
    for (const item of mockItems) {
      try {
        const canReview = await canReviewProduct(orderId.value, item.productId)
        if (canReview) {
          orderItems.value.push(item)
        }
      } catch (error) {
        console.error(`检查商品${item.productId}评价权限失败:`, error)
      }
    }

    if (orderItems.value.length === 0) {
      ElMessage.warning('该订单的商品已评价或不可评价')
    }
    
  } catch (error: any) {
    console.error('加载订单详情失败:', error)
    ElMessage.error(error.response?.data?.message || '加载订单详情失败')
  } finally {
    loading.value = false
  }
}

// 提交所有评价
const handleSubmitAll = async () => {
  if (orderItems.value.length === 0) return

  // 验证所有表单
  const validationPromises = Array.from(formRefs.value.values()).map(formRef => 
    formRef.validate().catch(() => false)
  )

  try {
    const validationResults = await Promise.all(validationPromises)
    if (!validationResults.every(result => result)) {
      ElMessage.error('请完善评价信息')
      return
    }

    submitting.value = true

    // 提交所有评价
    const submitPromises = orderItems.value.map(async (item) => {
      const reviewData: ProductReviewDTO = {
        productId: item.productId,
        orderId: orderId.value,
        rating: item.reviewForm.rating,
        reviewContent: item.reviewForm.reviewContent || undefined,
        reviewImages: item.reviewForm.reviewImages.length > 0 ? item.reviewForm.reviewImages : undefined
      }

      return submitReview(reviewData)
    })

    await Promise.all(submitPromises)
    
    ElMessage.success('评价提交成功！')
    
    // 跳转回订单列表
    router.push('/member/orders')
    
  } catch (error: any) {
    console.error('提交评价失败:', error)
    ElMessage.error(error.response?.data?.message || '提交评价失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 取消评价
const handleCancel = () => {
  router.back()
}

// 初始化
onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped lang="scss">
.member-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.member-content {
  background: #fff;
  padding: 20px 0 40px;
  min-height: 600px;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }

  // 会员中心主体
  .member-main {
    display: flex;
    gap: 20px;
    align-items: flex-start;

    // 右侧主内容区
    .member-main-content {
      flex: 1;
      background: #fff;
      min-height: 500px;
      padding: 20px;

      .review-wrapper {
        .page-title {
          font-size: 18px;
          font-weight: bold;
          color: #333;
          margin-bottom: 20px;
          padding-bottom: 10px;
          border-bottom: 2px solid #e4393c;

          .order-info {
            font-size: 14px;
            color: #999;
            font-weight: normal;
          }
        }

        .loading-wrapper {
          padding: 20px;
        }

        .review-form-wrapper {
          .product-review-item {
            border: 1px solid #e5e5e5;
            border-radius: 8px;
            margin-bottom: 20px;
            overflow: hidden;

            .product-info {
              background: #f9f9f9;
              padding: 20px;
              display: flex;
              align-items: center;
              gap: 15px;
              border-bottom: 1px solid #e5e5e5;

              .product-image {
                width: 80px;
                height: 80px;
                object-fit: cover;
                border-radius: 4px;
                border: 1px solid #e5e5e5;
              }

              .product-details {
                .product-name {
                  font-size: 16px;
                  font-weight: bold;
                  color: #333;
                  margin: 0 0 8px 0;
                }

                .product-specs {
                  color: #999;
                  font-size: 14px;
                }
              }
            }

            .review-form {
              padding: 20px;

              .rating-wrapper {
                display: flex;
                align-items: center;
                gap: 15px;

                .rating-text {
                  color: #e4393c;
                  font-weight: bold;
                }
              }

              .upload-wrapper {
                .upload-tips {
                  margin-top: 8px;
                  font-size: 12px;
                  color: #999;
                  line-height: 1.4;
                }
              }
            }
          }

          .submit-wrapper {
            text-align: center;
            padding: 20px 0;
            border-top: 1px solid #e5e5e5;
            margin-top: 20px;

            .el-button {
              margin: 0 10px;
              padding: 12px 30px;
            }
          }
        }

        .empty-state {
          display: flex;
          justify-content: center;
          align-items: center;
          min-height: 300px;
        }
      }
    }
  }
}

// 上传组件样式调整
:deep(.el-upload--picture-card) {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

// 响应式设计
@media (max-width: 768px) {
  .member-content {
    .member-main {
      flex-direction: column;

      .member-main-content {
        .review-wrapper {
          .review-form-wrapper {
            .product-review-item {
              .product-info {
                flex-direction: column;
                align-items: flex-start;
                text-align: center;

                .product-image {
                  align-self: center;
                }
              }

              .review-form {
                padding: 15px;
              }
            }
          }
        }
      }
    }
  }
}
</style>