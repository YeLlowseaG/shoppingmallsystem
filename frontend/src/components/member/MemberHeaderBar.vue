<template>
  <div class="member-header-bar">
    <div class="header-left">
      <el-icon class="member-icon"><User /></el-icon>
      <span class="header-title">会员中心</span>
    </div>
    <div class="header-right">
      <span class="greeting-text">
        您好:{{ userStore.userInfo?.realName || userStore.userInfo?.username }}{{ userLevelText }}
      </span>
      <el-button type="text" class="notification-btn" @click="handleNotificationClick">通知</el-button>
      <!-- <span class="new-feature">NEW 新功能展示</span> -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const userLevelText = computed(() => {
  const level = userStore.userInfo?.userLevel
  if (level === 'VIP' || level === 'vip') {
    return '(战略合作VIP)'
  }
  return ''
})

// 点击通知按钮跳转到收件箱
const handleNotificationClick = () => {
  router.push('/member/site-messages/inbox')
}
</script>

<style scoped lang="scss">
.member-header-bar {
  background: #f5f5f5;
  height: 50px;
  line-height: 50px;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border: 1px solid #e5e5e5;

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .member-icon {
      font-size: 18px;
      color: #666;
    }

    .header-title {
      font-size: 16px;
      font-weight: bold;
      color: #333;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 15px;

    .greeting-text {
      font-size: 14px;
      color: #333;
    }

    .notification-btn {
      color: #666;
      font-size: 14px;
      padding: 0 10px;

      &:hover {
        color: #e4393c;
      }
    }

    .new-feature {
      font-size: 12px;
      color: #e4393c;
      font-weight: bold;
    }
  }
}
</style>


















































