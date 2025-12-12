<template>
  <div class="member-sidebar">
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      @select="handleMenuSelect"
    >
      <!-- 交易记录 -->
      <el-sub-menu index="transaction">
        <template #title>
          <span>交易记录</span>
        </template>
        <el-menu-item index="transaction/orders">我的订单</el-menu-item>
      </el-sub-menu>

      <!-- 收藏夹 -->
      <el-sub-menu index="favorites">
        <template #title>
          <span>收藏夹</span>
        </template>
        <el-menu-item index="favorites/products">商品收藏</el-menu-item>
        <el-menu-item index="favorites/out-of-stock">缺货登记</el-menu-item>
      </el-sub-menu>

      <!-- 商品留言 -->
      <el-sub-menu index="messages">
        <template #title>
          <span>商品留言</span>
        </template>
        <el-menu-item index="messages/comments">评论与咨询</el-menu-item>
      </el-sub-menu>

      <!-- 个人设置 -->
      <el-sub-menu index="settings">
        <template #title>
          <span>个人设置</span>
        </template>
        <el-menu-item index="settings/profile">个人信息</el-menu-item>
        <el-menu-item index="settings/password">修改密码</el-menu-item>
        <el-menu-item index="settings/payment-password">修改预存款支付密码</el-menu-item>
        <el-menu-item index="settings/address">收货地址</el-menu-item>
      </el-sub-menu>

      <!-- 预存款 -->
      <el-sub-menu index="deposit">
        <template #title>
          <span>预存款</span>
        </template>
        <el-menu-item index="deposit/balance">我的预存款</el-menu-item>
        <el-menu-item index="deposit/recharge">预存款充值</el-menu-item>
        <!-- 预存款充值审核功能已屏蔽 -->
        <!-- <el-menu-item index="deposit/review">预存款充值审核</el-menu-item> -->
      </el-sub-menu>

      <!-- 站内消息 -->
      <el-sub-menu index="site-messages">
        <template #title>
          <span>站内消息({{ unreadCount }})</span>
        </template>
        <el-menu-item index="site-messages/send">发送消息</el-menu-item>
        <el-menu-item index="site-messages/inbox">收件箱</el-menu-item>
        <el-menu-item index="site-messages/drafts">草稿箱</el-menu-item>
        <el-menu-item index="site-messages/outbox">发件箱</el-menu-item>
        <el-menu-item index="site-messages/admin">给管理员发消息</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const props = withDefaults(defineProps<{
  activeMenu?: string
  unreadMessageCount?: number
}>(), {
  unreadMessageCount: 0
})

const route = useRoute()
const router = useRouter()

// 如果没有传入activeMenu，则根据当前路由自动判断
const activeMenu = computed(() => {
  if (props.activeMenu) {
    return props.activeMenu
  }
  // 根据路由自动判断
  const path = route.path
  if (path.includes('/settings/profile')) return 'settings/profile'
  if (path.includes('/settings/password')) return 'settings/password'
  if (path.includes('/settings/payment-password')) return 'settings/payment-password'
  if (path.includes('/settings/address')) return 'settings/address'
  if (path.includes('/favorites/products')) return 'favorites/products'
  if (path.includes('/transaction/orders')) return 'transaction/orders'
  if (path.includes('/deposit/recharge')) return 'deposit/recharge'
  if (path.includes('/deposit/balance')) return 'deposit/balance'
  return ''
})

const unreadCount = computed(() => props.unreadMessageCount)

const handleMenuSelect = (index: string) => {
  const routeMap: Record<string, string> = {
    'settings/profile': '/member/settings/profile',
    'settings/password': '/member/settings/password',
    'settings/payment-password': '/member/settings/payment-password',
    'settings/address': '/member/settings/address',
    'transaction/orders': '/member/transaction/orders',
    'favorites/products': '/member/favorites/products',
    'favorites/out-of-stock': '/member',
    'messages/comments': '/member',
    'deposit/balance': '/member/deposit/balance',
    'deposit/recharge': '/member/deposit/recharge',
    'deposit/review': '/member',
    'site-messages/send': '/member',
    'site-messages/inbox': '/member',
    'site-messages/drafts': '/member',
    'site-messages/outbox': '/member',
    'site-messages/admin': '/member'
  }
  
  const targetRoute = routeMap[index]
  if (targetRoute && route.path !== targetRoute) {
    router.push(targetRoute)
  }
}
</script>

<style scoped lang="scss">
.member-sidebar {
  width: 220px;
  background: #fff;
  border: 1px solid #e5e5e5;
  flex-shrink: 0;

  .sidebar-menu {
    border: none;
    width: 100%;

    :deep(.el-sub-menu__title),
    :deep(.el-menu-item) {
      height: 45px;
      line-height: 45px;
      font-size: 14px;
      color: #333;
      padding-left: 20px !important;

      &:hover {
        background: #f5f5f5;
        color: #e4393c;
      }
    }

    :deep(.el-menu-item.is-active) {
      background: #fff5f5;
      color: #e4393c;
      border-right: 2px solid #e4393c;
    }

    :deep(.el-sub-menu__title) {
      font-weight: 500;
    }

    :deep(.el-menu-item) {
      padding-left: 40px !important;
      font-size: 13px;
    }
  }
}
</style>

