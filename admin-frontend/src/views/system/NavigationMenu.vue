<template>
  <div class="navigation-menu">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>导航菜单管理</span>
          <el-button type="primary" @click="handleAdd">添加菜单</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="菜单名称">
          <el-input
            v-model="searchForm.menuName"
            placeholder="请输入菜单名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 菜单列表 -->
      <el-table :data="menuList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="menuName" label="菜单名称" width="150" />
        <el-table-column prop="menuUrl" label="菜单链接" min-width="200" show-overflow-tooltip />
        <el-table-column prop="menuType" label="菜单类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.menuType)" size="small">
              {{ getTypeText(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="menuParams" label="菜单参数" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatMenuParams(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="target" label="打开方式" width="100">
          <template #default="{ row }">
            {{ row.target === '_blank' ? '新窗口' : '当前窗口' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadMenuList"
        @current-change="loadMenuList"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formData.id ? '编辑菜单' : '添加菜单'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="formData.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <!-- 使用通用链接选择器组件（排除商品详情） -->
        <LinkSelector
          v-model:model-link-type="linkType"
          v-model:model-link-value="linkValue"
          :exclude-types="['2']"
        />
        <el-form-item label="菜单图标">
          <el-input v-model="formData.icon" placeholder="请输入图标名称（可选）" />
        </el-form-item>
        <el-form-item label="打开方式" prop="target">
          <el-radio-group v-model="formData.target">
            <el-radio label="_self">当前窗口</el-radio>
            <el-radio label="_blank">新窗口</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="请输入菜单描述（可选）"
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
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getNavigationMenuPage,
  createNavigationMenu,
  updateNavigationMenu,
  deleteNavigationMenu,
  updateNavigationMenuStatus,
  type NavigationMenu
} from '@/api/admin/navigationMenu'
import LinkSelector from '@/components/common/LinkSelector.vue'
import { getBrandOptions } from '@/api/admin/brand'
import { type Brand } from '@/api/admin/website'

// 搜索表单
const searchForm = ref({
  menuName: ''
})

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 菜单列表
const menuList = ref<NavigationMenu[]>([])

// 品牌列表（用于显示品牌名称）
const brandList = ref<Brand[]>([])

// 对话框
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = ref<NavigationMenu>({
  menuName: '',
  menuUrl: '',
  menuType: 'link',
  menuParams: '',
  icon: '',
  sortOrder: 0,
  status: 1,
  target: '_self',
  description: ''
})

// LinkSelector双向绑定
const linkType = ref(0)
const linkValue = ref('')

// 监听LinkSelector的变化，同步到formData
watch([linkType, linkValue], () => {
  formData.value.menuType = getLinkTypeString(linkType.value)
  formData.value.menuUrl = linkType.value === 4 ? linkValue.value : '/products'
  formData.value.menuParams = generateMenuParams(linkType.value, linkValue.value)
})

// 将数字类型转换为字符串类型
const getLinkTypeString = (type: number): string => {
  const typeMap: Record<number, string> = {
    0: 'link',
    1: 'category', 
    3: 'type',
    4: 'link',
    5: 'brand'
  }
  return typeMap[type] || 'link'
}

// 生成菜单参数
const generateMenuParams = (type: number, value: string): string => {
  if (type === 1) { // 商品分类
    return JSON.stringify({ categoryId: value })
  } else if (type === 3) { // 促销活动
    return JSON.stringify({ type: value })
  } else if (type === 5) { // 品牌类型
    return JSON.stringify({ brand: value })
  }
  return ''
}

// 表单验证规则
const formRules: FormRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  target: [{ required: true, message: '请选择打开方式', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

// 获取类型标签类型
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    link: '',
    category: 'success',
    brand: 'warning',
    type: 'info'
  }
  return typeMap[type] || ''
}

// 获取类型文本
const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    link: '直接链接',
    category: '商品分类',
    brand: '品牌筛选',
    type: '类型筛选'
  }
  return typeMap[type] || '未知'
}

// 将字符串类型转换为数字类型（用于回显）
const getNumberLinkType = (typeString: string): number => {
  const typeMap: Record<string, number> = {
    'link': 0,
    'category': 1,
    'type': 3,
    'brand': 5
  }
  return typeMap[typeString] || 0
}

// 解析参数用于编辑回显
const parseMenuData = (menu: NavigationMenu) => {
  // 设置链接类型
  linkType.value = getNumberLinkType(menu.menuType || 'link')
  
  // 解析链接值
  if (menu.menuType === 'category' || menu.menuType === 'type' || menu.menuType === 'brand') {
    try {
      const params = menu.menuParams ? JSON.parse(menu.menuParams) : {}
      if (params.categoryId) {
        linkValue.value = params.categoryId
      } else if (params.type) {
        linkValue.value = params.type
      } else if (params.brand) {
        linkValue.value = params.brand
      }
    } catch (error) {
      console.error('解析菜单参数失败:', error)
      linkValue.value = ''
    }
  } else if (menu.menuType === 'link' && menu.menuUrl !== '/products') {
    linkType.value = 4 // 外部链接
    linkValue.value = menu.menuUrl
  } else {
    linkValue.value = ''
  }
}

// 加载品牌列表
const loadBrandList = async () => {
  try {
    const response = await getBrandOptions()
    brandList.value = Array.isArray(response) ? response : []
  } catch (error) {
    console.error('加载品牌列表失败:', error)
    brandList.value = []
  }
}

// 格式化菜单参数显示
const formatMenuParams = (row: NavigationMenu): string => {
  if (!row.menuParams) return ''
  
  try {
    const params = JSON.parse(row.menuParams)
    
    // 如果是品牌类型，将品牌ID转换为品牌名称
    if (row.menuType === 'brand' && params.brand) {
      const brandId = params.brand
      const brand = brandList.value.find(b => b.id?.toString() === brandId.toString())
      if (brand) {
        return JSON.stringify({ brand: brand.brandName })
      }
    }
    
    // 其他类型直接返回原始JSON
    return row.menuParams
  } catch (error) {
    // 如果解析失败，返回原始值
    return row.menuParams
  }
}

// 加载菜单列表
const loadMenuList = async () => {
  try {
    const res = await getNavigationMenuPage(
      pagination.value.current,
      pagination.value.size,
      searchForm.value.menuName || undefined
    )
    menuList.value = res.records
    pagination.value.total = res.total
  } catch (error) {
    ElMessage.error('加载菜单列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadMenuList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    menuName: ''
  }
  handleSearch()
}

// 添加菜单
const handleAdd = () => {
  formData.value = {
    menuName: '',
    menuUrl: '/products',
    menuType: 'link',
    menuParams: '',
    icon: '',
    sortOrder: 0,
    status: 1,
    target: '_self',
    description: ''
  }
  linkType.value = 0
  linkValue.value = ''
  dialogVisible.value = true
}

// 编辑菜单
const handleEdit = (row: NavigationMenu) => {
  formData.value = { ...row }
  parseMenuData(row)
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (formData.value.id) {
        await updateNavigationMenu(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createNavigationMenu(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadMenuList()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

// 切换状态
const handleToggleStatus = async (row: NavigationMenu) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateNavigationMenuStatus(row.id!, newStatus)
    ElMessage.success('状态更新成功')
    loadMenuList()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 删除菜单
const handleDelete = async (row: NavigationMenu) => {
  try {
    await ElMessageBox.confirm('确定要删除该菜单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteNavigationMenu(row.id!)
    ElMessage.success('删除成功')
    loadMenuList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(async () => {
  await loadBrandList()
  await loadMenuList()
})
</script>

<style scoped lang="scss">
.navigation-menu {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .param-input {
    small {
      display: block;
      margin-top: 4px;
    }
  }
}
</style>