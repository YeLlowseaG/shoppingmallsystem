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
          <MemberSidebar active-menu="settings/address" :unread-message-count="unreadMessageCount" />

          <!-- 右侧主内容区 -->
          <div class="member-main-content">
            <!-- 收货地址列表 -->
            <div class="address-list-wrapper">
              <div class="table-header">
                <h3 class="section-title">收货地址</h3>
                <el-button type="danger" @click="handleAddAddress">
                  新增收货地址
                </el-button>
              </div>

              <el-table
                :data="addressList"
                border
                style="width: 100%"
                class="address-table"
              >
                <el-table-column prop="recipient" label="收货人" width="120" align="center" />
                <el-table-column prop="address" label="地址" min-width="300">
                  <template #default="scope">
                    {{ formatFullAddress(scope.row) }}
                  </template>
                </el-table-column>
                <el-table-column prop="phone" label="电话" width="120" align="center">
                  <template #default="scope">
                    {{ scope.row.phone || '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="mobile" label="手机" width="130" align="center">
                  <template #default="scope">
                    {{ scope.row.mobile || '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="zipCode" label="邮编" width="100" align="center">
                  <template #default="scope">
                    {{ scope.row.zipCode || '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="isDefault" label="默认" width="80" align="center">
                  <template #default="scope">
                    {{ scope.row.isDefault ? '是' : '否' }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" align="center" fixed="right">
                  <template #default="scope">
                    <el-button type="text" size="small" @click="handleEditAddress(scope.row)">
                      修改
                    </el-button>
                    <el-button type="text" size="small" @click="handleDeleteAddress(scope.row)">
                      删除
                    </el-button>
                    <el-button
                      v-if="!scope.row.isDefault"
                      type="text"
                      size="small"
                      @click="handleSetDefault(scope.row)"
                    >
                      设为默认
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import TopBar from '@/components/home/TopBar.vue'
import Header from '@/components/home/Header.vue'
import Navbar from '@/components/home/Navbar.vue'
import Footer from '@/components/home/Footer.vue'
import MemberHeaderBar from '@/components/member/MemberHeaderBar.vue'
import MemberSidebar from '@/components/member/MemberSidebar.vue'
import { getAddressList, deleteAddress, setDefaultAddress, type AddressVO } from '@/api/buyer/address'

const router = useRouter()

// 未读消息数量
const unreadMessageCount = ref(0)

// 收货地址列表
const addressList = ref<AddressVO[]>([])

// 加载收货地址列表
const loadAddressList = async () => {
  try {
    const data = await getAddressList()
    addressList.value = data
  } catch (error: any) {
    console.error('加载收货地址列表失败:', error)
    // 如果全局拦截器已经显示过错误提示，这里就不再显示
    if (!error.__messageShown) {
      ElMessage.error(error.message || '加载收货地址列表失败')
    }
  }
}

// 格式化完整地址（省市区 + 详细地址）
const formatFullAddress = (address: AddressVO): string => {
  const parts: string[] = []
  
  if (address.province) {
    parts.push(address.province)
  }
  if (address.city) {
    parts.push(address.city)
  }
  if (address.district) {
    parts.push(address.district)
  }
  if (address.address) {
    parts.push(address.address)
  }
  
  return parts.join(' ')
}

// 菜单选择逻辑已移至 MemberSidebar 组件中

// 新增收货地址
const handleAddAddress = () => {
  router.push('/member/settings/address/edit')
}

// 修改收货地址
const handleEditAddress = (row: AddressVO) => {
  router.push(`/member/settings/address/edit?id=${row.id}`)
}

// 删除收货地址
const handleDeleteAddress = async (row: AddressVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该收货地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAddress(row.id)
    ElMessage.success('删除成功')
    // 重新加载列表
    loadAddressList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除收货地址失败:', error)
      // 如果全局拦截器已经显示过错误提示，这里就不再显示
      if (!error.__messageShown) {
        ElMessage.error(error.message || '删除失败')
      }
    }
  }
}

// 设为默认
const handleSetDefault = async (row: AddressVO) => {
  try {
    await setDefaultAddress(row.id)
    ElMessage.success('设置成功')
    // 重新加载列表
    loadAddressList()
  } catch (error: any) {
    console.error('设置默认地址失败:', error)
    // 如果全局拦截器已经显示过错误提示，这里就不再显示
    if (!error.__messageShown) {
      ElMessage.error(error.message || '设置失败')
    }
  }
}

// 初始化加载数据
onMounted(() => {
  loadAddressList()
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
      border: 1px solid #e5e5e5;
      padding: 20px;

      .address-list-wrapper {
        .table-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;

          .section-title {
            font-size: 16px;
            font-weight: bold;
            color: #333;
            margin: 0;
          }
        }

        .address-table {
          :deep(.el-table__header) {
            th {
              background: #f5f5f5;
              color: #333;
              font-weight: bold;
            }
          }

          :deep(.el-button--text) {
            color: #e4393c;
            padding: 0 5px;

            &:hover {
              text-decoration: underline;
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .member-content {
    .member-main {
      flex-direction: column;
    }
  }
}
</style>

