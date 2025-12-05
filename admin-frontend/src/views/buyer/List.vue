<template>
  <div class="buyer-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>采购者列表</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" :value="undefined" />
            <el-option label="待审核" :value="0" />
            <el-option label="已激活" :value="1" />
            <el-option label="已禁用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="searchForm.userLevel" placeholder="请选择等级" clearable>
            <el-option label="全部" :value="undefined" />
            <el-option label="普通" :value="0" />
            <el-option label="VIP" :value="1" />
            <el-option label="金牌" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 采购者列表 -->
      <el-table :data="buyerList" v-loading="loading" border>
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="地区" width="200">
          <template #default="{ row }">
            {{ getRegionText(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="userLevelName" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.userLevel)">
              {{ row.userLevelName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatusName" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusTagType(row.auditStatus)">
              {{ row.auditStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="success" link @click="handleStatusChange(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="warning" link @click="handleLevelChange(row)">修改等级</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="采购者详情" width="800px">
      <el-descriptions :column="2" border v-if="currentBuyer">
        <el-descriptions-item label="用户名">{{ currentBuyer.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentBuyer.realName }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentBuyer.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentBuyer.phone }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ currentBuyer.gender === 1 ? '男' : currentBuyer.gender === 0 ? '女' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="等级">
          <el-tag :type="getLevelTagType(currentBuyer.userLevel)">
            {{ currentBuyer.userLevelName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentBuyer.status)">
            {{ currentBuyer.statusName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusTagType(currentBuyer.auditStatus)">
            {{ currentBuyer.auditStatusName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地区" :span="2">
          {{ getRegionText(currentBuyer) }}
        </el-descriptions-item>
        <el-descriptions-item label="详细地址" :span="2">{{ currentBuyer.address }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentBuyer.auditComment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentBuyer.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentBuyer.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 修改等级对话框 -->
    <el-dialog v-model="levelDialogVisible" title="修改等级" width="400px">
      <el-form :model="levelForm" label-width="100px">
        <el-form-item label="当前等级">
          <el-tag :type="getLevelTagType(currentBuyer?.userLevel)">
            {{ currentBuyer?.userLevelName }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新等级">
          <el-select v-model="levelForm.userLevel" placeholder="请选择等级" style="width: 100%">
            <el-option label="普通" :value="0" />
            <el-option label="VIP" :value="1" />
            <el-option label="金牌" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="levelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleLevelSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBuyerList, getBuyerById, updateBuyerStatus, updateBuyerLevel } from '@/api/admin/buyer'
import type { BuyerVO } from '@/api/admin/buyer'

const loading = ref(false)
const buyerList = ref<BuyerVO[]>([])

const searchForm = reactive({
  username: '',
  status: undefined as number | undefined,
  userLevel: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const levelDialogVisible = ref(false)
const currentBuyer = ref<BuyerVO | null>(null)

const levelForm = reactive({
  userLevel: 0
})

// 加载采购者列表
const loadBuyerList = async () => {
  loading.value = true
  try {
    const response = await getBuyerList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      status: searchForm.status,
      userLevel: searchForm.userLevel
    })
    buyerList.value = response.records || []
    pagination.total = response.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadBuyerList()
}

// 重置
const handleReset = () => {
  searchForm.username = ''
  searchForm.status = undefined
  searchForm.userLevel = undefined
  handleSearch()
}

// 查看详情
const handleView = async (row: BuyerVO) => {
  try {
    const buyer = await getBuyerById(row.id)
    currentBuyer.value = buyer
    detailDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 启用/禁用
const handleStatusChange = async (row: BuyerVO) => {
  try {
    const newStatus = row.status === 1 ? 2 : 1
    const statusText = newStatus === 1 ? '启用' : '禁用'
    await ElMessageBox.confirm(`确定要${statusText}该采购者吗？`, '提示', {
      type: 'warning'
    })
    await updateBuyerStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    loadBuyerList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 修改等级
const handleLevelChange = (row: BuyerVO) => {
  currentBuyer.value = row
  levelForm.userLevel = row.userLevel
  levelDialogVisible.value = true
}

// 提交等级修改
const handleLevelSubmit = async () => {
  if (!currentBuyer.value) return
  try {
    await updateBuyerLevel(currentBuyer.value.id, levelForm.userLevel)
    ElMessage.success('修改成功')
    levelDialogVisible.value = false
    loadBuyerList()
  } catch (error: any) {
    ElMessage.error(error.message || '修改失败')
  }
}

// 分页
const handleSizeChange = () => {
  loadBuyerList()
}

const handlePageChange = () => {
  loadBuyerList()
}

// 获取地区文本
const getRegionText = (row: BuyerVO): string => {
  const parts = []
  if (row.province) parts.push(row.province)
  if (row.city) parts.push(row.city)
  if (row.district) parts.push(row.district)
  return parts.length > 0 ? parts.join(' ') : '-'
}

// 获取等级标签类型
const getLevelTagType = (level?: number): string => {
  switch (level) {
    case 0:
      return 'info'
    case 1:
      return 'warning'
    case 2:
      return 'success'
    default:
      return ''
  }
}

// 获取状态标签类型
const getStatusTagType = (status?: number): string => {
  switch (status) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'danger'
    default:
      return ''
  }
}

// 获取审核状态标签类型
const getAuditStatusTagType = (status?: number): string => {
  switch (status) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'danger'
    default:
      return 'info'
  }
}

onMounted(() => {
  loadBuyerList()
})
</script>

<style scoped>
.buyer-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

