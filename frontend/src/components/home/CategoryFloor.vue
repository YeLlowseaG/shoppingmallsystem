<template>
  <div class="category-floor">
    <div class="container">
      <!-- 分类标题栏 -->
      <div class="floor-title" :style="{ background: titleColor }" @click="goToCategory">
        {{ floorNumber }} {{ categoryName }}
      </div>

      <!-- 第二行：大图广告 + 2个商品 -->
      <div class="top-row">
        <!-- 左侧大图广告 (占2/3宽度) -->
        <div class="big-ad">
          <img :src="bigAdImage" alt="广告" />
        </div>

        <!-- 右侧2个商品 (占1/3宽度，纵向排列) -->
        <div class="side-products">
          <div
            v-for="product in sideProducts"
            :key="product.id"
            class="product-item"
            @click="goToProduct(product.id)"
          >
            <img :src="product.image" :alt="product.name" class="product-image" />
            <div class="product-info">
              <div class="product-tag">{{ product.tag }}</div>
              <div class="product-name">{{ product.name }}</div>
              <div class="product-price">
                <span class="original-price">市场零售价: ¥{{ parseFloat(product.originalPrice).toFixed(2) }}</span>
                <span class="current-price">¥ {{ parseFloat(product.price).toFixed(2) }}</span>
              </div>
              <div class="product-sales">已售 {{ product.salesCount }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 第三行：4个商品横向排列 -->
      <div class="bottom-row">
        <div
          v-for="product in bottomProducts"
          :key="product.id"
          class="product-card"
          @click="goToProduct(product.id)"
        >
          <div class="product-image-wrapper">
            <img :src="product.image" :alt="product.name" />
          </div>
          <div class="product-details">
            <div class="product-category">[{{ product.category }}]</div>
            <div class="product-title">{{ product.name }}</div>
            <div class="product-prices">
              <span class="old-price">市场零售价: ¥{{ parseFloat(product.originalPrice).toFixed(2) }}</span>
              <span class="new-price">¥ {{ parseFloat(product.price).toFixed(2) }}</span>
            </div>
            <div class="product-sales-bottom">已售 {{ product.salesCount }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

interface Product {
  id: number
  name: string
  image: string
  price: number
  originalPrice: number
  category?: string
  tag?: string
  salesCount: number
}

interface Props {
  floorNumber: string
  categoryName: string
  titleColor: string
  bigAdImage: string
  sideProducts: Product[]
  bottomProducts: Product[]
  categoryId?: number
}

const props = defineProps<Props>()

// 跳转到分类列表页
const goToCategory = () => {
  if (props.categoryId) {
    router.push(`/products?categoryId=${props.categoryId}`)
  }
}

// 跳转到商品详情页
const goToProduct = (id: number) => {
  router.push(`/products/${id}`)
}
</script>

<style scoped lang="scss">
.category-floor {
  background: #fff;
  margin-bottom: 20px;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
  }

  .floor-title {
    height: 50px;
    line-height: 50px;
    padding: 0 20px;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 2px;
    cursor: pointer;
    transition: opacity 0.3s;

    &:hover {
      opacity: 0.9;
    }
  }

  .top-row {
    display: flex;
    gap: 10px;
    margin-bottom: 10px;

    .big-ad {
      flex: 2;
      cursor: pointer;

      img {
        width: 100%;
        height: 400px;
        object-fit: cover;
        display: block;
      }
    }

    .side-products {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 10px;

      .product-item {
        flex: 1;
        display: flex;
        background: #fff;
        border: 1px solid #eee;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: #e4393c;
          box-shadow: 0 2px 8px rgba(228, 57, 60, 0.2);
        }

        .product-image {
          width: 195px;
          height: 195px;
          object-fit: cover;
        }

        .product-info {
          flex: 1;
          padding: 15px;
          display: flex;
          flex-direction: column;
          justify-content: center;

          .product-tag {
            background: #e4393c;
            color: #fff;
            display: inline-block;
            padding: 2px 8px;
            border-radius: 3px;
            font-size: 12px;
            margin-bottom: 10px;
            align-self: flex-start;
          }

          .product-name {
            font-size: 14px;
            font-weight: bold;
            color: #333;
            margin-bottom: 10px;
          }

          .product-price {
            .original-price {
              display: block;
              font-size: 12px;
              color: #999;
              text-decoration: line-through;
              margin-bottom: 5px;
            }

            .current-price {
              font-size: 20px;
              color: #e4393c;
              font-weight: bold;
            }
          }

          .product-sales {
            font-size: 12px;
            color: #999;
            margin-top: 8px;
          }
        }
      }
    }
  }

  .bottom-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;

    .product-card {
      background: #fff;
      border: 1px solid #eee;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: #e4393c;
        box-shadow: 0 2px 8px rgba(228, 57, 60, 0.2);
      }

      .product-image-wrapper {
        width: 100%;
        height: 280px;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s;
        }

        &:hover img {
          transform: scale(1.05);
        }
      }

      .product-details {
        padding: 15px;

        .product-category {
          font-size: 12px;
          color: #999;
          margin-bottom: 5px;
        }

        .product-title {
          font-size: 14px;
          color: #333;
          margin-bottom: 10px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .product-prices {
          .old-price {
            display: block;
            font-size: 12px;
            color: #999;
            text-decoration: line-through;
            margin-bottom: 5px;
          }

          .new-price {
            font-size: 18px;
            color: #e4393c;
            font-weight: bold;
          }
        }

        .product-sales-bottom {
          font-size: 12px;
          color: #999;
          margin-top: 8px;
        }
      }
    }
  }
}
</style>
