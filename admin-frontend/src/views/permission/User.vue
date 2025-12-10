<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 180px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 用户列表 -->
      <el-table :data="userList" v-loading="loading" border>
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="200">
          <template #default="{ row }">
            <template v-if="row.roles && row.roles.length > 0">
              <el-tag v-for="role in row.roles" :key="role.id" style="margin-right: 5px;">
                {{ role.roleName }}
              </el-tag>
            </template>
            <span v-else style="color: #999;">未分配角色</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleStatusChange(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in allRoles"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils'
import {
  getAdminUserList,
  getAdminUserById,
  addAdminUser,
  updateAdminUser,
  deleteAdminUser,
  updateAdminUserStatus,
  resetPassword
} from '@/api/admin/user'
import { getAllRoles } from '@/api/admin/role'
import type { AdminInfoVO } from '@/api/admin/user'
import type { RoleVO } from '@/api/admin/role'

const loading = ref(false)
const userList = ref<AdminInfoVO[]>([])
const allRoles = ref<RoleVO[]>([])

const searchForm = reactive({
  username: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref<FormInstance>()

const form = reactive({
  id: undefined as number | undefined,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  roleIds: [] as number[],
  status: 1
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    const response = await getAdminUserList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      status: searchForm.status
    })
    userList.value = response.records || []
    pagination.total = response.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 加载所有角色
const loadAllRoles = async () => {
  try {
    allRoles.value = await getAllRoles()
  } catch (error: any) {
    ElMessage.error(error.message || '加载角色失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadUserList()
}

// 重置
const handleReset = () => {
  searchForm.username = ''
  searchForm.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(form, {
    id: undefined,
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    roleIds: [],
    status: 1
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row: AdminInfoVO) => {
  dialogTitle.value = '编辑用户'
  try {
    const user = await getAdminUserById(row.id)
    Object.assign(form, {
      id: user.id,
      username: user.username,
      password: '',
      realName: user.realName || '',
      email: user.email,
      phone: user.phone || '',
      roleIds: user.roles?.map(r => r.id) || [],
      status: user.status
    })
    dialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await updateAdminUser(form.id, {
            realName: form.realName,
            email: form.email,
            phone: form.phone,
            password: form.password || undefined,
            status: form.status
          }, form.roleIds)
          ElMessage.success('更新成功')
        } else {
          await addAdminUser({
            username: form.username,
            password: form.password,
            realName: form.realName,
            email: form.email,
            phone: form.phone,
            status: form.status
          }, form.roleIds)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadUserList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

// 删除
const handleDelete = async (row: AdminInfoVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      type: 'warning'
    })
    await deleteAdminUser(row.id)
    ElMessage.success('删除成功')
    loadUserList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 启用/禁用
const handleStatusChange = async (row: AdminInfoVO) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateAdminUserStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    loadUserList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 重置密码
const handleResetPassword = async (row: AdminInfoVO) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
      inputType: 'password',
      inputPlaceholder: '请输入新密码',
      inputValidator: (value) => {
        if (!value || value.length < 6) {
          return '密码长度不能少于6位'
        }
        return true
      }
    })
    await resetPassword(row.id, value)
    ElMessage.success('重置密码成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '重置失败')
    }
  }
}

// 分页
const handleSizeChange = () => {
  loadUserList()
}

const handlePageChange = () => {
  loadUserList()
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadUserList()
  loadAllRoles()
})
</script>

<style scoped>
.user-management {
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


