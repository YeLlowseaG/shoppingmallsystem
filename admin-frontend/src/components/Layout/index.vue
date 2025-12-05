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
      <el-aside width="200px">
        <el-menu
          :default-active="activeMenu"
          router
          class="sidebar-menu"
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
                :index="getMenuPath(child)"
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
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
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
  return adminStore.menus || []
})

// 获取菜单路径
const getMenuPath = (menu: MenuVO): string => {
  if (menu.path) {
    return menu.path.startsWith('/') ? menu.path : `/admin/${menu.path}`
  }
  return `/admin/menu-${menu.id}`
}

// 加载管理员信息和菜单
const loadAdminInfo = async () => {
  try {
    const info = await getAdminInfo()
    adminStore.setAdminInfo(info)
    // 如果菜单为空，重新加载菜单
    if (!adminStore.menus || adminStore.menus.length === 0) {
      // 从登录响应中获取菜单，这里需要重新获取
      // 实际应该从登录接口返回的menus中获取
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

const handleLogout = () => {
  adminStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/admin/login')
}

onMounted(() => {
  adminStore.init()
  if (adminStore.isLoggedIn() && (!adminStore.menus || adminStore.menus.length === 0)) {
    loadAdminInfo()
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
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
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
}

.sidebar-menu {
  height: 100%;
}
</style>

