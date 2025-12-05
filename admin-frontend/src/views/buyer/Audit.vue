<template>
  <div class="buyer-audit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>采购者审核</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 待审核采购者列表 -->
      <el-table :data="auditList" v-loading="loading" border>
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="地区" width="200">
          <template #default="{ row }">
            {{ getRegionText(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="详细地址" width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看详情</el-button>
            <el-button type="success" link @click="handleAudit(row, 1)">通过</el-button>
            <el-button type="danger" link @click="handleAudit(row, 2)">拒绝</el-button>
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

    <!-- 审核对话框 -->
    <el-dialog v-model="auditDialogVisible" :title="auditTitle" width="500px">
      <el-form :model="auditForm" :rules="auditRules" ref="auditFormRef" label-width="100px">
        <el-form-item label="用户名">
          <span>{{ currentBuyer?.username }}</span>
        </el-form-item>
        <el-form-item label="姓名">
          <span>{{ currentBuyer?.realName }}</span>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-tag :type="auditForm.auditStatus === 1 ? 'success' : 'danger'">
            {{ auditForm.auditStatus === 1 ? '通过' : '拒绝' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditComment">
          <el-input
            v-model="auditForm.auditComment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAuditSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { getPendingAuditList, getBuyerById, auditBuyer } from '@/api/admin/buyer'
import type { BuyerVO, BuyerDTO } from '@/api/admin/buyer'

const loading = ref(false)
const auditList = ref<BuyerVO[]>([])

const searchForm = reactive({
  username: '',
  realName: '',
  phone: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const currentBuyer = ref<BuyerVO | null>(null)
const auditFormRef = ref<FormInstance>()

const auditForm = reactive<BuyerDTO>({
  auditStatus: 1,
  auditComment: ''
})

const auditRules: FormRules = {
  auditComment: [
    { required: true, message: '请输入审核意见', trigger: 'blur' },
    { min: 5, message: '审核意见至少5个字符', trigger: 'blur' }
  ]
}

const auditTitle = computed(() => {
  return auditForm.auditStatus === 1 ? '审核通过' : '审核拒绝'
})

// 加载待审核列表
const loadAuditList = async () => {
  loading.value = true
  try {
    const response = await getPendingAuditList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      realName: searchForm.realName || undefined,
      phone: searchForm.phone || undefined
    })
    auditList.value = response.records || []
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
  loadAuditList()
}

// 重置
const handleReset = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.phone = ''
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

// 审核操作
const handleAudit = (row: BuyerVO, auditStatus: number) => {
  currentBuyer.value = row
  auditForm.auditStatus = auditStatus
  auditForm.auditComment = ''
  auditDialogVisible.value = true
}

// 提交审核
const handleAuditSubmit = async () => {
  if (!auditFormRef.value || !currentBuyer.value) return
  
  await auditFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await auditBuyer(currentBuyer.value.id, auditForm)
        ElMessage.success('审核成功')
        auditDialogVisible.value = false
        loadAuditList()
      } catch (error: any) {
        ElMessage.error(error.message || '审核失败')
      }
    }
  })
}

// 分页
const handleSizeChange = () => {
  loadAuditList()
}

const handlePageChange = () => {
  loadAuditList()
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
  loadAuditList()
})
</script>

<style scoped>
.buyer-audit {
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


