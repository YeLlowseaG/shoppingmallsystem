<template>
  <el-container class="layout-container">
    <el-header>
      <div class="header-content">
        <h1>B2B成人用品采购平台 - 管理后台</h1>
        <div class="header-right">
          <el-dropdown>
            <span class="admin-info">
              <el-icon><User /></el-icon>
              {{ adminStore.adminInfo?.username || '未登录' }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" class="sidebar-aside">
        <el-menu
          v-if="menuList && menuList.length > 0"
          :default-active="activeMenu"
          router
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <template v-for="menu in menuList" :key="menu.id">
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="getMenuPath(menu)">
              <template #title>
                <el-icon v-if="menu.icon">
                  <component :is="menu.icon" />
                </el-icon>
                <span>{{ menu.menuName }}</span>
              </template>
              <el-menu-item
                v-for="child in menu.children"
                :key="child.id"
                :index="getMenuPath(child, menu)"
              >
                <el-icon v-if="child.icon">
                  <component :is="child.icon" />
                </el-icon>
                <span>{{ child.menuName }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="getMenuPath(menu)">
              <el-icon v-if="menu.icon">
                <component :is="menu.icon" />
              </el-icon>
              <span>{{ menu.menuName }}</span>
            </el-menu-item>
          </template>
        </el-menu>
        <div v-else class="no-menu-tip">
          <el-empty description="您还没有分配角色，请联系管理员" :image-size="80" />
        </div>
      </el-aside>
      <el-main>
        <router-view v-slot="{ Component, route }">
          <component v-if="Component" :is="Component" :key="route.path" />
          <div v-else class="no-route-matched">
            <el-empty description="页面加载中，请稍候...">
              <el-button type="primary" @click="handleRetry">重试</el-button>
            </el-empty>
          </div>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin/user'
import { ElMessage } from 'element-plus'
import { User, HomeFilled } from '@element-plus/icons-vue'
import { getAdminInfo } from '@/api/admin/user'
import { addRoutes } from '@/router'
import type { MenuVO } from '@/api/admin/user'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const activeMenu = computed(() => route.path)

const menuList = computed(() => {
  const menus = adminStore.menus || []
  if (menus.length === 0) {
    console.warn('侧边栏菜单数据为空，请检查：')
    console.warn('1. localStorage 中是否有 admin_menus 数据')
    console.warn('2. adminStore.menus 的值:', adminStore.menus)
    const savedMenus = localStorage.getItem('admin_menus')
    console.warn('3. localStorage 中的原始数据:', savedMenus)
  } else {
    console.log('✅ 侧边栏菜单数据已加载，数量:', menus.length)
  }
  return menus
})

// 获取菜单路径（支持父菜单路径）
const getMenuPath = (menu: MenuVO, parentMenu?: MenuVO): string => {
  if (!menu.path) {
    return `/admin/menu-${menu.id}`
  }
  
  // 如果路径以 / 开头，说明是绝对路径，直接返回
  if (menu.path.startsWith('/')) {
    return menu.path
  }
  
  // 如果有父菜单，构建完整路径
  if (parentMenu?.path) {
    // 父菜单路径可能是 /order 或 order，需要处理
    const parentPath = parentMenu.path.startsWith('/') 
      ? parentMenu.path.replace(/^\//, '') 
      : parentMenu.path
    
    // 特殊处理：如果子菜单路径是 'index'，且父菜单路径是 'dashboard'，则返回 /admin/dashboard
    // 因为静态路由中 dashboard 的路径就是 /admin/dashboard，不是 /admin/dashboard/index
    if (menu.path === 'index' && parentPath === 'dashboard') {
      return `/admin/dashboard`
    }
    
    return `/admin/${parentPath}/${menu.path}`
  }
  
  // 没有父菜单，直接拼接 /admin
  return `/admin/${menu.path}`
}

// 加载管理员信息和菜单
const loadAdminInfo = async () => {
  try {
    const info = await getAdminInfo()
    adminStore.setAdminInfo(info)
    // 注意：菜单和权限应该从登录接口获取，这里只更新用户信息
    // 如果菜单为空，说明用户没有分配角色，这是正常的
    if (!adminStore.menus || adminStore.menus.length === 0) {
      console.warn('用户没有分配角色，菜单列表为空')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
    // 如果获取用户信息失败，可能是token过期，跳转到登录页
    if (error.message?.includes('未授权') || error.message?.includes('401')) {
      adminStore.logout()
      router.push('/admin/login')
    }
  }
}

const handleLogout = () => {
  adminStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/admin/login')
}

const handleRetry = () => {
  // 重新加载菜单数据并添加路由
  if (adminStore.menus && adminStore.menus.length > 0) {
    addRoutes(adminStore.menus)
    // 重新导航到当前路径
    router.replace(route.path)
  } else {
    // 如果没有菜单数据，重新加载用户信息
    loadAdminInfo()
  }
}

// 监听菜单数据变化，确保路由正确添加
watch(
  () => adminStore.menus,
  (newMenus) => {
    if (newMenus && newMenus.length > 0) {
      console.log('菜单数据变化，添加动态路由，数量:', newMenus.length)
      addRoutes(newMenus)
      // 路由添加后，如果当前路径未匹配，尝试重新导航
      nextTick(() => {
        const currentPath = route.path
        const matched = router.resolve(currentPath).matched.length > 0
        if (!matched && currentPath.startsWith('/admin') && currentPath !== '/admin/login') {
          console.log('当前路径未匹配，尝试重新导航:', currentPath)
          router.replace(currentPath)
        }
      })
    }
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  adminStore.init()
  
  // 使用 nextTick 确保 store 初始化完成后再检查菜单数据
  nextTick(() => {
    // 如果菜单数据已存在（从 localStorage 恢复），watch 会自动处理路由添加
    if (adminStore.menus && adminStore.menus.length > 0) {
      console.log('onMounted: 从 localStorage 恢复菜单数据，数量:', adminStore.menus.length)
    } else if (adminStore.isLoggedIn()) {
      console.log('onMounted: 菜单数据为空，尝试从服务器获取')
      loadAdminInfo()
    } else {
      console.log('onMounted: 用户未登录或菜单数据为空')
    }
  })
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* 顶部栏样式 */
:deep(.el-header) {
  background-color: #304156;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

h1 {
  margin: 0;
  font-size: 20px;
  color: #fff;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #bfcbd9;
  gap: 8px;
  transition: color 0.3s;
}

.admin-info:hover {
  color: #fff;
}

/* 下拉菜单样式调整 */
:deep(.el-dropdown-menu) {
  background-color: #fff;
}

.sidebar-aside {
  background-color: #304156;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

/* 菜单项悬停效果 */
.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background-color: #263445 !important;
}

/* 激活的菜单项 */
.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
  color: #fff !important;
}

/* 子菜单项激活状态 */
.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
}

.no-menu-tip {
  padding: 20px;
  text-align: center;
  color: #bfcbd9;
}

.no-route-matched {
  padding: 40px;
  text-align: center;
}
</style>

