<template>
  <div class="product-add-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品发布</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="productForm"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="productForm.productName" placeholder="请输入商品名称" />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader
            v-model="productForm.categoryId"
            :options="categoryOptions"
            :props="cascaderProps"
            placeholder="请选择商品分类"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="商品编码" prop="productCode">
          <el-input v-model="productForm.productCode" placeholder="请输入商品编码" />
        </el-form-item>

        <el-form-item label="基础价格" prop="basePrice">
          <el-input-number
            v-model="productForm.basePrice"
            :min="0"
            :precision="2"
            :step="0.01"
          />
        </el-form-item>

        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" />
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>

        <el-form-item label="主图" prop="mainImage">
          <el-input v-model="productForm.mainImage" placeholder="请输入主图URL" />
        </el-form-item>

        <el-form-item label="商品状态" prop="status">
          <el-radio-group v-model="productForm.status">
            <el-radio label="上架">上架</el-radio>
            <el-radio label="下架">下架</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()

const productForm = ref({
  productName: '',
  categoryId: null as any,
  productCode: '',
  basePrice: 0,
  stock: 0,
  description: '',
  mainImage: '',
  status: '下架'
})

const formRules: FormRules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  productCode: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入基础价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

const categoryOptions = ref<any[]>([])

const cascaderProps = {
  value: 'id',
  label: 'categoryName',
  children: 'children',
  checkStrictly: true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success('商品发布功能开发中，敬请期待！')
      console.log('提交的表单数据：', productForm.value)
    } else {
      ElMessage.error('请填写完整的商品信息')
    }
  })
}

const handleReset = () => {
  formRef.value?.resetFields()
}

const handleCancel = () => {
  router.push('/admin/product/list')
}

onMounted(() => {
  // TODO: 从后端API加载商品分类数据
  ElMessage.info('商品发布页面加载成功')
})
</script>

<style scoped>
.product-add-container {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: 500;
}
</style>
