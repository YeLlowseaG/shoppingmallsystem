<template>
  <div class="top-bar">
    <div class="container">
      <div class="left">
        <span class="welcome">亲，欢迎光临总部旗舰商城！</span>
      </div>
      <div class="right">
        <template v-if="userStore.isLoggedIn">
          <span class="username">您好！{{ userStore.userInfo?.username }}</span>
          <a href="#" class="link" @click="handleLogout">退出</a>
        </template>
        <template v-else>
          <router-link to="/login" class="link highlight">【请登录】</router-link>
          <router-link to="/register" class="link">【免费注册】</router-link>
        </template>
        <span class="divider">|</span>
        <router-link to="/cart" class="link">
          购物车总金额
          <span class="cart-amount">{{ cartStore.totalAmount }}</span>
        </router-link>
        <span class="divider">|</span>
        <router-link to="/help" class="link">帮助中心</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const cartStore = useCartStore()
const router = useRouter()

const handleLogout = (e: Event) => {
  e.preventDefault()
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/')
}
</script>

<style scoped lang="scss">
.top-bar {
  background-color: #f5f5f5;
  height: 30px;
  line-height: 30px;
  font-size: 12px;
  color: #666;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .left {
    .welcome {
      color: #666;
    }
  }

  .right {
    display: flex;
    align-items: center;
    gap: 8px;

    .username {
      color: #333;
      font-weight: 500;
    }

    .link {
      color: #666;
      text-decoration: none;
      transition: color 0.3s;

      &:hover {
        color: #e4393c;
      }

      &.highlight {
        color: #e4393c;
      }
    }

    .cart-amount {
      color: #e4393c;
      font-weight: bold;
      margin-left: 4px;
    }

    .divider {
      color: #ddd;
    }
  }
}
</style>
