<template>
  <div class="header">
    <div class="container">
      <!-- Logo -->
      <div class="logo">
        <router-link to="/">
          <img src="https://via.placeholder.com/150x60/E4393C/ffffff?text=JINGVO" alt="JINGVO 净果" />
        </router-link>
      </div>

      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="飞机杯"
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button type="danger" @click="handleSearch">
              搜 索
            </el-button>
          </template>
        </el-input>
        <div class="hot-keywords">
          <span
            v-for="keyword in hotKeywords"
            :key="keyword"
            class="keyword"
            @click="handleKeywordClick(keyword)"
          >
            {{ keyword }}
          </span>
        </div>
      </div>

      <!-- 联系方式 -->
      <div class="contact">
        <div class="phone">
          <div class="label">服务热线：</div>
          <div class="number">400-166-1683</div>
        </div>
        <div class="phone">
          <div class="label">咨询热线：</div>
          <div class="number">13049338552</div>
        </div>
        <div class="qrcode">
          <img src="https://via.placeholder.com/60x60/666666/ffffff?text=QR" alt="二维码" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchKeyword = ref('')

// 热门关键词
const hotKeywords = ['飞机杯', '跳蛋名器', '情趣跳蛋', '情趣内衣', '安全套', '延时喷雾']

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/products',
      query: { keyword: searchKeyword.value }
    })
  }
}

const handleKeywordClick = (keyword: string) => {
  searchKeyword.value = keyword
  handleSearch()
}
</script>

<style scoped lang="scss">
.header {
  background: #fff;
  padding: 20px 0;
  border-bottom: 2px solid #e4393c;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
    display: flex;
    align-items: center;
    gap: 40px;
  }

  .logo {
    flex-shrink: 0;

    img {
      height: 60px;
      width: auto;
    }
  }

  .search-box {
    flex: 1;

    .search-input {
      :deep(.el-input__wrapper) {
        border-radius: 4px 0 0 4px;
      }

      :deep(.el-input-group__append) {
        background-color: #e4393c;
        border-color: #e4393c;
        padding: 0 30px;

        .el-button {
          background: transparent;
          border: none;
          color: #fff;
          font-weight: bold;
        }
      }
    }

    .hot-keywords {
      margin-top: 10px;
      display: flex;
      gap: 12px;

      .keyword {
        font-size: 12px;
        color: #999;
        cursor: pointer;
        transition: color 0.3s;

        &:hover {
          color: #e4393c;
        }
      }
    }
  }

  .contact {
    display: flex;
    align-items: center;
    gap: 15px;
    flex-shrink: 0;

    .phone {
      text-align: center;

      .label {
        font-size: 12px;
        color: #666;
      }

      .number {
        font-size: 16px;
        font-weight: bold;
        color: #e4393c;
      }
    }

    .qrcode {
      img {
        width: 60px;
        height: 60px;
        border: 1px solid #eee;
      }
    }
  }
}
</style>
