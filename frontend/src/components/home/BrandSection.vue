<template>
  <div class="brand-section">
    <div class="container">
      <!-- 左侧品牌专区 -->
      <div class="brand-area">
        <div class="title">品牌专区</div>
        <div class="brand-grid">
          <div v-for="brand in brands" :key="brand.id" class="brand-item">
            <img :src="brand.logo" :alt="brand.name" />
          </div>
        </div>
      </div>

      <!-- 右侧精品推荐广告 -->
      <div class="recommend-ads">
        <img
          v-for="ad in ads"
          :key="ad.id"
          :src="ad.image"
          :alt="ad.title"
          class="ad-image"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const brands = ref([
  { id: 1, name: '虚姬', logo: 'https://via.placeholder.com/120x60/FF6B9D/ffffff?text=虚姬' },
  { id: 2, name: 'Angus', logo: 'https://via.placeholder.com/120x60/9D50BB/ffffff?text=Angus' },
  { id: 3, name: '杜蕾斯', logo: 'https://via.placeholder.com/120x60/6C5CE7/ffffff?text=Durex' },
  { id: 4, name: '冈本', logo: 'https://via.placeholder.com/120x60/FFD93D/ffffff?text=Okamoto' },
  { id: 5, name: '欧姿丹', logo: 'https://via.placeholder.com/120x60/FD79A8/ffffff?text=欧姿丹' },
  { id: 6, name: '杰士邦', logo: 'https://via.placeholder.com/120x60/74B9FF/ffffff?text=杰士邦' },
  { id: 7, name: 'DESIRE', logo: 'https://via.placeholder.com/120x60/A29BFE/ffffff?text=DESIRE' },
  { id: 8, name: 'IC KISTOY', logo: 'https://via.placeholder.com/120x60/FD79A8/ffffff?text=IC' },
  { id: 9, name: 'Beten', logo: 'https://via.placeholder.com/120x60/FF6B9D/ffffff?text=Beten' },
  { id: 10, name: 'NO17', logo: 'https://via.placeholder.com/120x60/6C5CE7/ffffff?text=NO17' },
  { id: 11, name: 'W Dibe', logo: 'https://via.placeholder.com/120x60/FFD93D/ffffff?text=Dibe' },
  { id: 12, name: 'LE', logo: 'https://via.placeholder.com/120x60/74B9FF/ffffff?text=LE' },
  { id: 13, name: '久慕之', logo: 'https://via.placeholder.com/120x60/A29BFE/ffffff?text=久慕之' },
  { id: 14, name: 'NU smile', logo: 'https://via.placeholder.com/120x60/FD79A8/ffffff?text=NU' },
  { id: 15, name: '爱巢', logo: 'https://via.placeholder.com/120x60/FF6B9D/ffffff?text=爱巢' },
  { id: 16, name: '百乐', logo: 'https://via.placeholder.com/120x60/9D50BB/ffffff?text=百乐' }
])

const ads = ref([
  { id: 1, title: '好货来袭', image: 'https://via.placeholder.com/280x220/6C5CE7/ffffff?text=好货来袭' },
  { id: 2, title: '敬请期待', image: 'https://via.placeholder.com/280x220/FF6B9D/ffffff?text=敬请期待' }
])
</script>

<style scoped lang="scss">
.brand-section {
  background: #fff;
  padding: 30px 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
    display: flex;
    gap: 20px;
  }

  .brand-area {
    flex: 1;

    .title {
      background: linear-gradient(to right, #FFD93D, #FFA500);
      color: #fff;
      font-size: 16px;
      font-weight: bold;
      padding: 12px 20px;
      border-radius: 4px 4px 0 0;
    }

    .brand-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1px;
      background: #eee;
      border: 1px solid #eee;

      .brand-item {
        background: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 15px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background: #f9f9f9;
          transform: scale(1.05);
        }

        img {
          max-width: 100%;
          height: auto;
        }
      }
    }
  }

  .recommend-ads {
    display: flex;
    flex-direction: column;
    gap: 15px;

    .ad-image {
      width: 280px;
      height: 220px;
      object-fit: cover;
      border-radius: 4px;
      cursor: pointer;
      transition: transform 0.3s;

      &:hover {
        transform: scale(1.02);
      }
    }
  }
}
</style>
