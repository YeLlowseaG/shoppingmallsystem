<template>
  <div class="banner-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>轮播图管理</span>
          <el-button type="primary" @click="handleAdd">添加轮播图</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 轮播图列表 -->
      <el-table :data="bannerList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="imageUrl" label="图片" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              style="width: 80px; height: 40px"
              fit="cover"
              :preview-src-list="[row.imageUrl]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="linkType" label="链接类型" width="120">
          <template #default="{ row }">
            {{ getLinkTypeText(row.linkType) }}
          </template>
        </el-table-column>
        <el-table-column prop="linkValue" label="链接值" width="150" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
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
        @size-change="loadBannerList"
        @current-change="loadBannerList"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formData.id ? '编辑轮播图' : '添加轮播图'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入轮播图标题" />
        </el-form-item>
        <el-form-item label="图片URL" prop="imageUrl">
          <el-input v-model="formData.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="链接类型" prop="linkType">
          <el-select v-model="formData.linkType" placeholder="请选择链接类型" style="width: 100%">
            <el-option label="无链接" :value="0" />
            <el-option label="商品分类" :value="1" />
            <el-option label="商品详情" :value="2" />
            <el-option label="促销活动" :value="3" />
            <el-option label="外部链接" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="链接值" prop="linkValue" v-if="formData.linkType > 0">
          <el-input v-model="formData.linkValue" placeholder="根据链接类型填写对应的值" />
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
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getBannerPage,
  createBanner,
  updateBanner,
  deleteBanner,
  updateBannerStatus,
  type Banner
} from '@/api/admin/website'

// 搜索表单
const searchForm = ref({
  status: undefined as number | undefined
})

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 轮播图列表
const bannerList = ref<Banner[]>([])

// 对话框
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = ref<Banner>({
  title: '',
  imageUrl: '',
  linkType: 0,
  linkValue: '',
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const formRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请输入图片URL', trigger: 'blur' }],
  linkType: [{ required: true, message: '请选择链接类型', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

// 获取链接类型文本
const getLinkTypeText = (type: number) => {
  const typeMap: Record<number, string> = {
    0: '无链接',
    1: '商品分类',
    2: '商品详情',
    3: '促销活动',
    4: '外部链接'
  }
  return typeMap[type] || '未知'
}

// 加载轮播图列表
const loadBannerList = async () => {
  try {
    const res = await getBannerPage(
      pagination.value.current,
      pagination.value.size,
      searchForm.value.status
    )
    bannerList.value = res.records
    pagination.value.total = res.total
  } catch (error) {
    ElMessage.error('加载轮播图列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadBannerList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    status: undefined
  }
  handleSearch()
}

// 添加轮播图
const handleAdd = () => {
  formData.value = {
    title: '',
    imageUrl: '',
    linkType: 0,
    linkValue: '',
    sortOrder: 0,
    status: 1
  }
  dialogVisible.value = true
}

// 编辑轮播图
const handleEdit = (row: Banner) => {
  formData.value = { ...row }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (formData.value.id) {
        await updateBanner(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createBanner(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadBannerList()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

// 切换状态
const handleToggleStatus = async (row: Banner) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateBannerStatus(row.id!, newStatus)
    ElMessage.success('状态更新成功')
    loadBannerList()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 删除轮播图
const handleDelete = async (row: Banner) => {
  try {
    await ElMessageBox.confirm('确定要删除该轮播图吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteBanner(row.id!)
    ElMessage.success('删除成功')
    loadBannerList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadBannerList()
})
</script>

<style scoped lang="scss">
.banner-manage {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }
}
</style>
