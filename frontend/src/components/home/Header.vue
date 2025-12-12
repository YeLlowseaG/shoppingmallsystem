<template>
  <div class="header">
    <div class="container">
      <!-- Logo -->
      <div class="logo">
        <router-link to="/">
          <img :src="siteConfig.logo" :alt="siteConfig.name" />
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
          <div class="number">{{ siteConfig.servicePhone }}</div>
        </div>
        <div class="phone">
          <div class="label">咨询热线：</div>
          <div class="number">{{ siteConfig.consultPhone }}</div>
        </div>
        <div class="qrcode">
          <img :src="siteConfig.qrcode" alt="二维码" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublicConfigs } from '@/api/buyer/systemConfig'

const router = useRouter()
const searchKeyword = ref('')

// 网站配置
const siteConfig = reactive({
  logo: 'https://via.placeholder.com/150x60/E4393C/ffffff?text=JINGVO',
  name: 'JINGVO 净果',
  servicePhone: '400-166-1683',
  consultPhone: '13049338552',
  qrcode: 'https://via.placeholder.com/60x60/666666/ffffff?text=QR'
})

// 热门关键词
const hotKeywords = ref<string[]>(['飞机杯', '跳蛋名器', '情趣跳蛋', '情趣内衣', '安全套', '延时喷雾'])

// 加载系统配置
const loadSiteConfig = async () => {
  try {
    const configs = await getPublicConfigs()
    
    // 更新网站配置
    if (configs['site.logo']) siteConfig.logo = configs['site.logo']
    if (configs['site.name']) siteConfig.name = configs['site.name']
    if (configs['site.service_phone']) siteConfig.servicePhone = configs['site.service_phone']
    if (configs['site.consult_phone']) siteConfig.consultPhone = configs['site.consult_phone']
    if (configs['site.qrcode']) siteConfig.qrcode = configs['site.qrcode']
    
    // 更新热门关键词
    if (configs['search.hot_keywords']) {
      hotKeywords.value = configs['search.hot_keywords'].split(',').map(k => k.trim()).filter(k => k)
    }
  } catch (error) {
    console.error('加载系统配置失败:', error)
    // 保持默认配置
  }
}

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

// 组件挂载时加载配置
onMounted(() => {
  loadSiteConfig()
})
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
