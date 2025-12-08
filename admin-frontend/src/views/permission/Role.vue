<template>
  <div class="role-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色编码">
          <el-input v-model="searchForm.roleCode" placeholder="请输入角色编码" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
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

      <!-- 角色列表 -->
      <el-table :data="roleList" v-loading="loading" border>
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleStatusChange(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="!!form.id" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单权限" prop="menuIds">
          <el-tree
            ref="menuTreeRef"
            :data="menuTree"
            :props="{ children: 'children', label: 'menuName' }"
            show-checkbox
            node-key="id"
            :default-checked-keys="form.menuIds"
            @check="handleMenuCheck"
          />
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
import {
  getRoleList,
  getRoleById,
  addRole,
  updateRole,
  deleteRole,
  updateRoleStatus
} from '@/api/admin/role'
import { getMenuTree } from '@/api/admin/menu'
import type { RoleVO } from '@/api/admin/role'
import type { MenuVO } from '@/api/admin/menu'

const loading = ref(false)
const roleList = ref<RoleVO[]>([])
const menuTree = ref<MenuVO[]>([])
const menuTreeRef = ref()

const searchForm = reactive({
  roleCode: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref<FormInstance>()

const form = reactive({
  id: undefined as number | undefined,
  roleCode: '',
  roleName: '',
  description: '',
  status: 1,
  menuIds: [] as number[]
})

const rules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

// 加载角色列表
const loadRoleList = async () => {
  loading.value = true
  try {
    const response = await getRoleList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      roleCode: searchForm.roleCode || undefined,
      status: searchForm.status
    })
    roleList.value = response.records || []
    pagination.total = response.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 加载菜单树
const loadMenuTree = async () => {
  try {
    menuTree.value = await getMenuTree()
  } catch (error: any) {
    ElMessage.error(error.message || '加载菜单失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadRoleList()
}

// 重置
const handleReset = () => {
  searchForm.roleCode = ''
  searchForm.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  Object.assign(form, {
    id: undefined,
    roleCode: '',
    roleName: '',
    description: '',
    status: 1,
    menuIds: []
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row: RoleVO) => {
  dialogTitle.value = '编辑角色'
  try {
    const role = await getRoleById(row.id!)
    Object.assign(form, {
      id: role.id,
      roleCode: role.roleCode,
      roleName: role.roleName,
      description: role.description || '',
      status: role.status,
      menuIds: role.menuIds || []
    })
    dialogVisible.value = true
    // 等待DOM更新后设置选中的菜单
    setTimeout(() => {
      if (menuTreeRef.value && form.menuIds.length > 0) {
        menuTreeRef.value.setCheckedKeys(form.menuIds)
      }
    }, 100)
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 菜单选择
const handleMenuCheck = () => {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
  form.menuIds = [...checkedKeys, ...halfCheckedKeys] as number[]
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 获取选中的菜单ID
      const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
      const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
      const menuIds = [...checkedKeys, ...halfCheckedKeys] as number[]

      try {
        if (form.id) {
          await updateRole(form.id, {
            roleCode: form.roleCode,
            roleName: form.roleName,
            description: form.description,
            status: form.status
          }, menuIds)
          ElMessage.success('更新成功')
        } else {
          await addRole({
            roleCode: form.roleCode,
            roleName: form.roleName,
            description: form.description,
            status: form.status
          }, menuIds)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadRoleList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

// 删除
const handleDelete = async (row: RoleVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
      type: 'warning'
    })
    await deleteRole(row.id!)
    ElMessage.success('删除成功')
    loadRoleList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 启用/禁用
const handleStatusChange = async (row: RoleVO) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateRoleStatus(row.id!, newStatus)
    ElMessage.success('操作成功')
    loadRoleList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 分页
const handleSizeChange = () => {
  loadRoleList()
}

const handlePageChange = () => {
  loadRoleList()
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
  menuTreeRef.value?.setCheckedKeys([])
}

onMounted(() => {
  loadRoleList()
  loadMenuTree()
})
</script>

<style scoped>
.role-management {
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

