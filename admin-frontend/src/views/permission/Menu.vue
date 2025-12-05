<template>
  <div class="menu-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增菜单
          </el-button>
        </div>
      </template>

      <!-- 菜单树 -->
      <el-tree
        :data="menuTree"
        :props="{ children: 'children', label: 'menuName' }"
        default-expand-all
        :expand-on-click-node="false"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <span class="node-label">
              <el-icon v-if="data.icon" style="margin-right: 5px;">
                <component :is="data.icon" />
              </el-icon>
              {{ data.menuName }}
              <el-tag size="small" style="margin-left: 10px;">{{ data.menuType }}</el-tag>
              <el-tag v-if="data.permission" size="small" type="info" style="margin-left: 5px;">
                {{ data.permission }}
              </el-tag>
            </span>
            <span class="node-actions">
              <el-button type="primary" link size="small" @click="handleAddChild(data)">
                新增子菜单
              </el-button>
              <el-button type="primary" link size="small" @click="handleEdit(data)">编辑</el-button>
              <el-button type="success" link size="small" @click="handleStatusChange(data)">
                {{ data.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button type="danger" link size="small" @click="handleDelete(data)">删除</el-button>
            </span>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="父菜单" prop="parentId">
          <el-select
            v-model="form.parentId"
            placeholder="请选择父菜单（不选则为顶级菜单）"
            clearable
            style="width: 100%"
          >
            <el-option label="顶级菜单" :value="0" />
            <template v-for="menu in flatMenuList" :key="menu.id">
              <el-option
                v-if="menu.menuType === '目录' && menu.id !== form.id"
                :label="menu.menuName"
                :value="menu.id"
              />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio label="目录">目录</el-radio>
            <el-radio label="菜单">菜单</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path" v-if="form.menuType === '菜单'">
          <el-input v-model="form.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component" v-if="form.menuType === '菜单'">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permission">
          <el-input v-model="form.permission" placeholder="请输入权限标识，如：admin:user:list" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getMenuTree,
  getMenuById,
  addMenu,
  updateMenu,
  deleteMenu,
  updateMenuStatus
} from '@/api/admin/menu'
import type { MenuVO } from '@/api/admin/menu'

const menuTree = ref<MenuVO[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref<FormInstance>()
const parentMenuId = ref<number | undefined>(undefined)

const form = reactive({
  id: undefined as number | undefined,
  parentId: undefined as number | undefined,
  menuName: '',
  menuType: '目录',
  path: '',
  component: '',
  icon: '',
  permission: '',
  sortOrder: 0,
  status: 1
})

const rules: FormRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  path: [
    { required: true, message: '请输入路由路径', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (form.menuType === '菜单' && !value) {
          callback(new Error('菜单类型必须填写路由路径'))
        } else {
          callback()
        }
      }, trigger: 'blur' }
  ],
  permission: [{ required: true, message: '请输入权限标识', trigger: 'blur' }]
}

// 扁平化菜单列表（用于父菜单选择）
const flatMenuList = computed(() => {
  const flatten = (menus: MenuVO[]): MenuVO[] => {
    const result: MenuVO[] = []
    menus.forEach(menu => {
      result.push(menu)
      if (menu.children && menu.children.length > 0) {
        result.push(...flatten(menu.children))
      }
    })
    return result
  }
  return flatten(menuTree.value)
})

// 加载菜单树
const loadMenuTree = async () => {
  loading.value = true
  try {
    menuTree.value = await getMenuTree()
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增菜单'
  parentMenuId.value = undefined
  Object.assign(form, {
    id: undefined,
    parentId: undefined,
    menuName: '',
    menuType: '目录',
    path: '',
    component: '',
    icon: '',
    permission: '',
    sortOrder: 0,
    status: 1
  })
  dialogVisible.value = true
}

// 新增子菜单
const handleAddChild = (parent: MenuVO) => {
  dialogTitle.value = '新增子菜单'
  parentMenuId.value = parent.id
  Object.assign(form, {
    id: undefined,
    parentId: parent.id,
    menuName: '',
    menuType: '菜单',
    path: '',
    component: '',
    icon: '',
    permission: '',
    sortOrder: 0,
    status: 1
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (menu: MenuVO) => {
  dialogTitle.value = '编辑菜单'
  try {
    const menuData = await getMenuById(menu.id!)
    Object.assign(form, {
      id: menuData.id,
      parentId: menuData.parentId || undefined,
      menuName: menuData.menuName,
      menuType: menuData.menuType,
      path: menuData.path || '',
      component: menuData.component || '',
      icon: menuData.icon || '',
      permission: menuData.permission || '',
      sortOrder: menuData.sortOrder || 0,
      status: menuData.status
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
          await updateMenu(form.id, {
            parentId: form.parentId || 0,
            menuName: form.menuName,
            menuType: form.menuType,
            path: form.path,
            component: form.component,
            icon: form.icon,
            permission: form.permission,
            sortOrder: form.sortOrder,
            status: form.status
          })
          ElMessage.success('更新成功')
        } else {
          await addMenu({
            parentId: form.parentId || 0,
            menuName: form.menuName,
            menuType: form.menuType,
            path: form.path,
            component: form.component,
            icon: form.icon,
            permission: form.permission,
            sortOrder: form.sortOrder,
            status: form.status
          })
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadMenuTree()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

// 删除
const handleDelete = async (menu: MenuVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该菜单吗？', '提示', {
      type: 'warning'
    })
    await deleteMenu(menu.id!)
    ElMessage.success('删除成功')
    loadMenuTree()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 启用/禁用
const handleStatusChange = async (menu: MenuVO) => {
  try {
    const newStatus = menu.status === 1 ? 0 : 1
    await updateMenuStatus(menu.id!, newStatus)
    ElMessage.success('操作成功')
    loadMenuTree()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadMenuTree()
})
</script>

<style scoped>
.menu-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex: 1;
  padding-right: 8px;
}

.node-label {
  flex: 1;
}

.node-actions {
  display: flex;
  gap: 5px;
}
</style>

