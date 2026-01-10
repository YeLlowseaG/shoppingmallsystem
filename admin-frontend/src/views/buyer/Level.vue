<template>
  <div class="member-level-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>等级管理</span>
          <el-button type="primary" @click="handleAdd">新增等级</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="等级名称">
          <el-input v-model="searchForm.levelName" placeholder="请输入等级名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="全部" :value="undefined" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 会员等级列表 -->
      <el-table :data="levelList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="levelName" label="等级名称" width="150" />
        <!-- 积分区间列已屏蔽 -->
        <!-- <el-table-column prop="pointsRangeText" label="积分区间" width="180" /> -->
        <!-- 折扣率列已屏蔽：不再使用折扣率计算会员价 -->
        <!-- <el-table-column prop="discountRateText" label="折扣率" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getDiscountTagType(row.discountRate)">
              {{ row.discountRateText }}
            </el-tag>
          </template>
        </el-table-column> -->
        <el-table-column prop="sortOrder" label="排序号" width="100" align="center" />
        <el-table-column prop="statusName" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="form.levelName" placeholder="请输入等级名称" maxlength="50" show-word-limit />
        </el-form-item>
        <!-- 积分相关字段已屏蔽 -->
        <!-- <el-form-item label="最低积分" prop="minPoints">
          <el-input-number
            v-model="form.minPoints"
            :min="0"
            :precision="0"
            placeholder="请输入最低积分"
            style="width: 100%"
          />
          <div class="form-item-tip">包含此积分值</div>
        </el-form-item>
        <el-form-item label="最高积分" prop="maxPoints">
          <el-input-number
            v-model="form.maxPoints"
            :min="0"
            :precision="0"
            placeholder="请输入最高积分（留空表示无上限）"
            style="width: 100%"
          />
          <div class="form-item-tip">不包含此积分值，留空表示无上限</div>
        </el-form-item> -->
        <!-- 折扣率字段已屏蔽：不再使用折扣率计算会员价 -->
        <!-- <el-form-item label="折扣率" prop="discountRate">
          <el-input-number
            v-model="form.discountRate"
            :min="0.01"
            :max="100.00"
            :precision="2"
            :step="0.01"
            placeholder="请输入折扣率"
            style="width: 100%"
          />
          <div class="form-item-tip">例如：95.00 表示 95折，100.00 表示无折扣</div>
        </el-form-item> -->
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number
            v-model="form.sortOrder"
            :min="0"
            :precision="0"
            placeholder="请输入排序号"
            style="width: 100%"
          />
          <div class="form-item-tip">数字越小越靠前</div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="等级描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入等级描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getMemberLevelPage,
  getMemberLevelById,
  createMemberLevel,
  updateMemberLevel,
  deleteMemberLevel,
  updateMemberLevelStatus
} from '@/api/admin/memberLevel'
import type { MemberLevelVO, MemberLevelDTO } from '@/api/admin/memberLevel'
import { formatDateTime } from '@/utils'

const loading = ref(false)
const submitLoading = ref(false)
const levelList = ref<MemberLevelVO[]>([])

const searchForm = reactive({
  levelName: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增等级')
const formRef = ref<FormInstance>()

const form = reactive<MemberLevelDTO>({
  id: undefined,
  levelName: '',
  // 积分相关字段已屏蔽，但保留在DTO中以避免类型错误
  minPoints: 0,
  maxPoints: undefined,
  discountRate: 100.00,
  sortOrder: 0,
  status: 1,
  description: ''
})

const rules: FormRules = {
  levelName: [
    { required: true, message: '请输入等级名称', trigger: 'blur' },
    { max: 50, message: '等级名称长度不能超过50个字符', trigger: 'blur' }
  ],
  // 积分相关验证已屏蔽
  // minPoints: [
  //   { required: true, message: '请输入最低积分', trigger: 'blur' },
  //   { type: 'number', min: 0, message: '最低积分不能小于0', trigger: 'blur' }
  // ],
  // 折扣率验证已屏蔽：不再使用折扣率计算会员价
  // discountRate: [
  //   { required: true, message: '请输入折扣率', trigger: 'blur' },
  //   { type: 'number', min: 0.01, max: 100.00, message: '折扣率必须在0.01-100.00之间', trigger: 'blur' }
  // ],
  sortOrder: [
    { required: true, message: '请输入排序号', trigger: 'blur' },
    { type: 'number', min: 0, message: '排序号不能小于0', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 加载会员等级列表
const loadLevelList = async () => {
  loading.value = true
  try {
    const response = await getMemberLevelPage({
      page: pagination.page,
      pageSize: pagination.pageSize,
      levelName: searchForm.levelName || undefined,
      status: searchForm.status
    })
    levelList.value = response.records || []
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
  loadLevelList()
}

// 重置
const handleReset = () => {
  searchForm.levelName = ''
  searchForm.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增等级'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row: MemberLevelVO) => {
  dialogTitle.value = '编辑等级'
  try {
    const level = await getMemberLevelById(row.id)
    form.id = level.id
    form.levelName = level.levelName
    // 积分相关字段已屏蔽
    // form.minPoints = level.minPoints
    // form.maxPoints = level.maxPoints
    form.discountRate = level.discountRate
    form.sortOrder = level.sortOrder
    form.status = level.status
    form.description = level.description || ''
    dialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 删除
const handleDelete = async (row: MemberLevelVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除等级"${row.levelName}"吗？`, '提示', {
      type: 'warning'
    })
    await deleteMemberLevel(row.id)
    ElMessage.success('删除成功')
    loadLevelList()
  } catch (error: any) {
    if (error !== 'cancel') {
      // 如果错误信息包含"无法删除"，说明有用户使用了该等级
      const errorMessage = error.message || error.response?.data?.message || '删除失败'
      if (errorMessage.includes('无法删除') || errorMessage.includes('已被') || errorMessage.includes('使用')) {
        // 后端错误信息已包含完整提示，直接显示警告
        ElMessage.warning(errorMessage)
      } else {
        ElMessage.error(errorMessage)
      }
    }
  }
}

// 启用/禁用
const handleStatusChange = async (row: MemberLevelVO) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const statusText = newStatus === 1 ? '启用' : '禁用'
    await ElMessageBox.confirm(`确定要${statusText}等级"${row.levelName}"吗？`, '提示', {
      type: 'warning'
    })
    await updateMemberLevelStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    loadLevelList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (form.id) {
      // 更新
      await updateMemberLevel(form)
      ElMessage.success('更新成功')
    } else {
      // 新增
      await createMemberLevel(form)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadLevelList()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.id = undefined
  form.levelName = ''
  // 积分相关字段已屏蔽
  // form.minPoints = 0
  // form.maxPoints = undefined
  form.discountRate = 100.00
  form.sortOrder = 0
  form.status = 1
  form.description = ''
  formRef.value?.clearValidate()
}

// 关闭对话框
const handleDialogClose = () => {
  resetForm()
}

// 分页
const handleSizeChange = () => {
  loadLevelList()
}

const handlePageChange = () => {
  loadLevelList()
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  return status === 1 ? 'success' : 'info'
}

// 获取折扣率标签类型
const getDiscountTagType = (discountRate: number) => {
  if (discountRate >= 100) return 'info'
  if (discountRate >= 95) return 'success'
  if (discountRate >= 90) return 'warning'
  return 'danger'
}

// 初始化
onMounted(() => {
  loadLevelList()
})
</script>

<style scoped lang="scss">
.member-level-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .form-item-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}
</style>
