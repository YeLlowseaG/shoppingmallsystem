<template>
  <div class="logistics-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>物流配置</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 运费模板管理 -->
        <el-tab-pane label="运费模板" name="template">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="handleAddTemplate">
                <el-icon><Plus /></el-icon>
                新增运费模板
              </el-button>
            </div>

            <!-- 搜索栏 -->
            <el-form :inline="true" :model="templateSearchForm" class="search-form">
              <el-form-item label="关键词">
                <el-input v-model="templateSearchForm.keyword" placeholder="模板名称" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="templateSearchForm.status" placeholder="请选择状态" clearable style="width: 180px">
                  <el-option label="全部" :value="undefined" />
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadTemplateList">查询</el-button>
                <el-button @click="resetTemplateSearch">重置</el-button>
              </el-form-item>
            </el-form>

            <!-- 运费模板列表 -->
            <el-table :data="templateList" v-loading="templateLoading" border>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="templateName" label="模板名称" width="200" />
              <el-table-column prop="calculationType" label="计算方式" width="120">
                <template #default="{ row }">
                  {{ getTemplateCalculationTypeName(row.calculationType) }}
                </template>
              </el-table-column>
              <el-table-column prop="defaultFirstWeight" label="默认首重(kg)" width="120">
                <template #default="{ row }">
                  {{ row.defaultFirstWeight || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="defaultFirstPrice" label="默认首费(元)" width="120">
                <template #default="{ row }">
                  {{ row.defaultFirstPrice ? `¥${row.defaultFirstPrice.toFixed(2)}` : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleEditTemplate(row)">编辑</el-button>
                  <el-button type="success" link @click="handleTemplateStatusChange(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteTemplate(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="templatePagination.page"
                v-model:page-size="templatePagination.pageSize"
                :total="templatePagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleTemplateSizeChange"
                @current-change="handleTemplatePageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 发货地址库管理 -->
        <el-tab-pane label="发货地址库" name="warehouse">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="handleAddWarehouse">
                <el-icon><Plus /></el-icon>
                新增发货地址
              </el-button>
            </div>

            <!-- 搜索栏 -->
            <el-form :inline="true" :model="warehouseSearchForm" class="search-form">
              <el-form-item label="关键词">
                <el-input v-model="warehouseSearchForm.keyword" placeholder="仓库名称/联系人" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="warehouseSearchForm.status" placeholder="请选择状态" clearable style="width: 180px">
                  <el-option label="全部" :value="undefined" />
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadWarehouseList">查询</el-button>
                <el-button @click="resetWarehouseSearch">重置</el-button>
              </el-form-item>
            </el-form>

            <!-- 发货地址列表 -->
            <el-table :data="warehouseList" v-loading="warehouseLoading" border>
              <el-table-column prop="warehouseName" label="仓库名称" width="150" />
              <el-table-column prop="contactName" label="联系人" width="120" />
              <el-table-column prop="contactPhone" label="联系电话" width="150" />
              <el-table-column label="地址" min-width="300">
                <template #default="{ row }">
                  {{ row.fullAddress || `${row.province}${row.city}${row.district || ''}${row.detailAddress}` }}
                </template>
              </el-table-column>
              <el-table-column prop="isDefault" label="默认" width="80">
                <template #default="{ row }">
                  <el-tag v-if="row.isDefault === 1" type="success">默认</el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="250" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleEditWarehouse(row)">编辑</el-button>
                  <el-button v-if="row.isDefault !== 1" type="warning" link @click="handleSetDefaultWarehouse(row)">
                    设为默认
                  </el-button>
                  <el-button type="success" link @click="handleWarehouseStatusChange(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteWarehouse(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="warehousePagination.page"
                v-model:page-size="warehousePagination.pageSize"
                :total="warehousePagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleWarehouseSizeChange"
                @current-change="handleWarehousePageChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 运费模板对话框 -->
    <el-dialog
      v-model="templateDialogVisible"
      :title="templateDialogTitle"
      width="900px"
      @close="handleTemplateDialogClose"
    >
      <el-form :model="templateForm" :rules="templateRules" ref="templateFormRef" label-width="140px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="templateForm.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="计算方式" prop="calculationType">
          <el-select v-model="templateForm.calculationType" style="width: 100%" disabled>
            <el-option label="按重量" :value="1" />
            <!-- 暂时屏蔽按件数和按金额 -->
            <!-- <el-option label="按件数" :value="2" /> -->
            <!-- <el-option label="按金额" :value="3" /> -->
          </el-select>
          <div style="color: #909399; font-size: 12px; margin-top: 5px">当前仅支持按重量计算方式</div>
        </el-form-item>
        <el-divider>默认运费规则</el-divider>
        <el-form-item label="默认首重(kg)" prop="defaultFirstWeight">
          <el-input-number v-model="templateForm.defaultFirstWeight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="默认首费(元)" prop="defaultFirstPrice">
          <el-input-number v-model="templateForm.defaultFirstPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="默认续重(kg)" prop="defaultContinueWeight">
          <el-input-number v-model="templateForm.defaultContinueWeight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="默认续费(元)" prop="defaultContinuePrice">
          <el-input-number v-model="templateForm.defaultContinuePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-divider>指定地区规则（可选）</el-divider>
        <el-form-item label="地区规则">
          <el-button type="primary" size="small" @click="handleAddRule">添加地区规则</el-button>
          <div v-if="templateForm.rules && templateForm.rules.length > 0" style="margin-top: 10px">
            <el-table :data="templateForm.rules" border>
              <el-table-column label="地区" min-width="200">
                <template #default="{ row }">
                  <span v-if="row.regionName">
                    <template v-if="isJsonString(row.regionName)">
                      <el-tag
                        v-for="(region, index) in parseRegions(row.regionName)"
                        :key="index"
                        size="small"
                        style="margin-right: 4px; margin-bottom: 4px"
                      >
                        {{ region.provinceName }}{{ region.cityName ? '-' + region.cityName : '' }}{{ region.districtName ? '-' + region.districtName : '' }}
                      </el-tag>
                    </template>
                    <span v-else>{{ row.regionName }}</span>
                  </span>
                  <span v-else class="text-muted">默认规则</span>
                </template>
              </el-table-column>
              <el-table-column label="首重(kg)" width="100">
                <template #default="{ row }">
                  {{ row.firstWeight || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="首费(元)" width="100">
                <template #default="{ row }">
                  {{ row.firstPrice ? `¥${row.firstPrice.toFixed(2)}` : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="续重(kg)" width="100">
                <template #default="{ row }">
                  {{ row.continueWeight || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="续费(元)" width="100">
                <template #default="{ row }">
                  {{ row.continuePrice ? `¥${row.continuePrice.toFixed(2)}` : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ $index }">
                  <el-button type="danger" link @click="handleRemoveRule($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input v-model="templateForm.description" type="textarea" :rows="3" placeholder="请输入模板描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="templateForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTemplateSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 地区规则编辑对话框 -->
    <el-dialog
      v-model="ruleDialogVisible"
      title="编辑地区规则"
      width="800px"
      @close="handleRuleDialogClose"
    >
      <el-form :model="ruleForm" :rules="ruleRules" ref="ruleFormRef" label-width="140px">
        <el-form-item label="地区选择" prop="regions">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 10px"
          >
            <template #default>
              可以选择多个地区，指定地区规则优先于默认规则生效。如果不选择地区，则为默认规则。
            </template>
          </el-alert>
          <MultiRegionSelector
            v-model="ruleForm.regions"
            @update:modelValue="handleRegionsChange"
          />
          <div v-if="ruleForm.regions && ruleForm.regions.length > 0" style="margin-top: 10px">
            <el-tag
              v-for="(region, index) in ruleForm.regions"
              :key="index"
              style="margin-right: 8px; margin-bottom: 8px"
            >
              {{ region.provinceName }}{{ region.cityName ? '-' + region.cityName : '' }}{{ region.districtName ? '-' + region.districtName : '' }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="首重(kg)" prop="firstWeight">
          <el-input-number v-model="ruleForm.firstWeight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="首费(元)" prop="firstPrice">
          <el-input-number v-model="ruleForm.firstPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="续重(kg)" prop="continueWeight">
          <el-input-number v-model="ruleForm.continueWeight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="续费(元)" prop="continuePrice">
          <el-input-number v-model="ruleForm.continuePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRuleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 发货地址对话框 -->
    <el-dialog
      v-model="warehouseDialogVisible"
      :title="warehouseDialogTitle"
      width="700px"
      @close="handleWarehouseDialogClose"
    >
      <el-form :model="warehouseForm" :rules="warehouseRules" ref="warehouseFormRef" label-width="120px">
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="warehouseForm.warehouseName" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="联系人姓名" prop="contactName">
          <el-input v-model="warehouseForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="warehouseForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="warehouseForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="warehouseForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区县">
          <el-input v-model="warehouseForm.district" placeholder="请输入区县" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="warehouseForm.detailAddress" type="textarea" :rows="3" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="邮编">
          <el-input v-model="warehouseForm.zipCode" placeholder="请输入邮编" />
        </el-form-item>
        <el-form-item label="是否默认">
          <el-radio-group v-model="warehouseForm.isDefault">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="warehouseForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="warehouseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleWarehouseSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 运费规则查看对话框 -->
    <el-dialog v-model="rulesDialogVisible" title="运费规则" width="1000px">
      <el-table :data="currentTemplateRules" border>
        <el-table-column prop="regionName" label="地区" width="200">
          <template #default="{ row }">
            {{ row.regionName || '默认规则' }}
          </template>
        </el-table-column>
        <el-table-column prop="firstWeight" label="首重(kg)" width="100" />
        <el-table-column prop="firstPrice" label="首重价格" width="120" />
        <el-table-column prop="continueWeight" label="续重(kg)" width="100" />
        <el-table-column prop="continuePrice" label="续重价格" width="120" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import MultiRegionSelector from '@/components/common/MultiRegionSelector.vue'
import {
  getShippingTemplateList,
  getShippingTemplateById,
  addShippingTemplate,
  updateShippingTemplate,
  deleteShippingTemplate,
  updateShippingTemplateStatus,
  getWarehouseAddressList,
  getWarehouseAddressById,
  addWarehouseAddress,
  updateWarehouseAddress,
  deleteWarehouseAddress,
  updateWarehouseAddressStatus,
  setDefaultWarehouseAddress,
  type ShippingTemplateVO,
  type ShippingRuleVO,
  type WarehouseAddressVO
} from '@/api/admin/logistics'

// 当前标签页
const activeTab = ref('template')

// ==================== 运费模板管理 ====================
const templateList = ref<ShippingTemplateVO[]>([])
const templateLoading = ref(false)
const templateSearchForm = reactive({
  keyword: '',
  status: undefined as number | undefined
})
const templatePagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const templateDialogVisible = ref(false)
const templateDialogTitle = ref('新增运费模板')
const templateFormRef = ref()
const templateForm = reactive<ShippingTemplateVO>({
  templateName: '',
  calculationType: 1,
  defaultFirstWeight: 1,
  defaultFirstPrice: 0,
  defaultContinueWeight: 1,
  defaultContinuePrice: 0,
  description: '',
  status: 1,
  rules: []
})

const templateRules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  calculationType: [{ required: true, message: '请选择计算方式', trigger: 'change' }],
  defaultFirstWeight: [{ required: true, message: '请输入默认首重', trigger: 'blur' }],
  defaultFirstPrice: [{ required: true, message: '请输入默认首费', trigger: 'blur' }],
  defaultContinueWeight: [{ required: true, message: '请输入默认续重', trigger: 'blur' }],
  defaultContinuePrice: [{ required: true, message: '请输入默认续费', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// ==================== 地区规则编辑 ====================
const ruleDialogVisible = ref(false)
const ruleFormRef = ref()
const ruleForm = reactive({
  regions: [] as Array<{
    provinceCode?: string
    provinceName?: string
    cityCode?: string
    cityName?: string
    districtCode?: string
    districtName?: string
  }>,
  firstWeight: 1,
  firstPrice: 0,
  continueWeight: 1,
  continuePrice: 0
})

const ruleRules = {
  firstWeight: [{ required: true, message: '请输入首重', trigger: 'blur' }],
  firstPrice: [{ required: true, message: '请输入首费', trigger: 'blur' }],
  continueWeight: [{ required: true, message: '请输入续重', trigger: 'blur' }],
  continuePrice: [{ required: true, message: '请输入续费', trigger: 'blur' }]
}

// ==================== 发货地址库管理 ====================
const warehouseList = ref<WarehouseAddressVO[]>([])
const warehouseLoading = ref(false)
const warehouseSearchForm = reactive({
  keyword: '',
  status: undefined as number | undefined
})
const warehousePagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const warehouseDialogVisible = ref(false)
const warehouseDialogTitle = ref('新增发货地址')
const warehouseFormRef = ref()
const warehouseForm = reactive<WarehouseAddressVO>({
  warehouseName: '',
  contactName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  zipCode: '',
  isDefault: 0,
  status: 1
})

const warehouseRules = {
  warehouseName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 运费规则查看
const rulesDialogVisible = ref(false)
const currentTemplateRules = ref<ShippingRuleVO[]>([])

// ==================== 通用方法 ====================
const handleTabChange = (name: string) => {
  if (name === 'template') {
    loadTemplateList()
  } else if (name === 'warehouse') {
    loadWarehouseList()
  }
}

// ==================== 运费模板管理方法 ====================
const loadTemplateList = async () => {
  templateLoading.value = true
  try {
    const response = await getShippingTemplateList({
      page: templatePagination.page,
      pageSize: templatePagination.pageSize,
      keyword: templateSearchForm.keyword || undefined,
      status: templateSearchForm.status
    })
    templateList.value = response.records || []
    templatePagination.total = response.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    templateLoading.value = false
  }
}

const resetTemplateSearch = () => {
  templateSearchForm.keyword = ''
  templateSearchForm.status = undefined
  templatePagination.page = 1
  loadTemplateList()
}

const handleTemplateSizeChange = (size: number) => {
  templatePagination.pageSize = size
  templatePagination.page = 1
  loadTemplateList()
}

const handleTemplatePageChange = (page: number) => {
  templatePagination.page = page
  loadTemplateList()
}

const handleAddTemplate = () => {
  templateDialogTitle.value = '新增运费模板'
  Object.assign(templateForm, {
    id: undefined,
    templateName: '',
    calculationType: 1, // 固定为按重量
    defaultFirstWeight: 1,
    defaultFirstPrice: 0,
    defaultContinueWeight: 1,
    defaultContinuePrice: 0,
    description: '',
    status: 1,
    rules: []
  })
  templateDialogVisible.value = true
}

const handleEditTemplate = async (row: ShippingTemplateVO) => {
  templateDialogTitle.value = '编辑运费模板'
  try {
    const template = await getShippingTemplateById(row.id!)
    Object.assign(templateForm, template)
    // 强制设置为按重量计算方式
    templateForm.calculationType = 1
    templateDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

const handleTemplateSubmit = async () => {
  if (!templateFormRef.value) return
  await templateFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (templateForm.id) {
          await updateShippingTemplate(templateForm.id, templateForm)
          ElMessage.success('更新成功')
        } else {
          await addShippingTemplate(templateForm)
          ElMessage.success('新增成功')
        }
        templateDialogVisible.value = false
        loadTemplateList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const handleTemplateDialogClose = () => {
  templateFormRef.value?.resetFields()
}

const handleTemplateStatusChange = async (row: ShippingTemplateVO) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateShippingTemplateStatus(row.id!, newStatus)
    ElMessage.success('操作成功')
    loadTemplateList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDeleteTemplate = async (row: ShippingTemplateVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该运费模板吗？', '提示', {
      type: 'warning'
    })
    await deleteShippingTemplate(row.id!)
    ElMessage.success('删除成功')
    loadTemplateList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 暂时屏蔽查看规则功能
// const handleViewTemplateRules = async (row: ShippingTemplateVO) => {
//   try {
//     const template = await getShippingTemplateById(row.id!)
//     currentTemplateRules.value = template.rules || []
//     rulesDialogVisible.value = true
//   } catch (error: any) {
//     ElMessage.error(error.message || '加载失败')
//   }
// }

const getTemplateCalculationTypeName = (type: number) => {
  const map: Record<number, string> = {
    1: '按重量',
    2: '按件数',
    3: '按金额'
  }
  return map[type] || '未知'
}

// ==================== 地区规则管理方法 ====================
const handleAddRule = () => {
  Object.assign(ruleForm, {
    regions: [],
    regionDisplay: '',
    firstWeight: templateForm.defaultFirstWeight || 1,
    firstPrice: templateForm.defaultFirstPrice || 0,
    continueWeight: templateForm.defaultContinueWeight || 1,
    continuePrice: templateForm.defaultContinuePrice || 0
  })
  ruleDialogVisible.value = true
}

const handleRegionsChange = (regions: Array<{
  provinceCode?: string
  provinceName?: string
  cityCode?: string
  cityName?: string
  districtCode?: string
  districtName?: string
}>) => {
  ruleForm.regions = regions
}

const handleRuleSubmit = async () => {
  if (!ruleFormRef.value) return
  await ruleFormRef.value.validate(async (valid) => {
    if (valid) {
      // 构建地区编码和名称的JSON字符串
      let regionCode = ''
      let regionName = ''
      
      if (ruleForm.regions && ruleForm.regions.length > 0) {
        regionCode = JSON.stringify(ruleForm.regions.map(r => ({
          provinceCode: r.provinceCode || '',
          cityCode: r.cityCode || '',
          districtCode: r.districtCode || ''
        })))
        regionName = JSON.stringify(ruleForm.regions.map(r => ({
          provinceName: r.provinceName || '',
          cityName: r.cityName || '',
          districtName: r.districtName || ''
        })))
      }

      const rule: ShippingRuleVO = {
        regionCode: regionCode || null,
        regionName: regionName || null,
        firstWeight: ruleForm.firstWeight,
        firstPrice: ruleForm.firstPrice,
        continueWeight: ruleForm.continueWeight,
        continuePrice: ruleForm.continuePrice
      }
      
      if (!templateForm.rules) {
        templateForm.rules = []
      }
      templateForm.rules.push(rule)
      ruleDialogVisible.value = false
      ElMessage.success('添加规则成功')
    }
  })
}

const handleRuleDialogClose = () => {
  ruleFormRef.value?.resetFields()
}

const handleRemoveRule = (index: number) => {
  if (templateForm.rules) {
    templateForm.rules.splice(index, 1)
  }
}

// 判断是否为JSON字符串
const isJsonString = (str: string | null | undefined): boolean => {
  if (!str) return false
  try {
    JSON.parse(str)
    return str.trim().startsWith('[') || str.trim().startsWith('{')
  } catch {
    return false
  }
}

// 解析地区JSON字符串
const parseRegions = (regionName: string): Array<{
  provinceName?: string
  cityName?: string
  districtName?: string
}> => {
  try {
    return JSON.parse(regionName)
  } catch {
    return []
  }
}

// ==================== 发货地址库管理方法 ====================
const loadWarehouseList = async () => {
  warehouseLoading.value = true
  try {
    const response = await getWarehouseAddressList({
      page: warehousePagination.page,
      pageSize: warehousePagination.pageSize,
      keyword: warehouseSearchForm.keyword || undefined,
      status: warehouseSearchForm.status
    })
    warehouseList.value = response.records || []
    warehousePagination.total = response.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    warehouseLoading.value = false
  }
}

const resetWarehouseSearch = () => {
  warehouseSearchForm.keyword = ''
  warehouseSearchForm.status = undefined
  warehousePagination.page = 1
  loadWarehouseList()
}

const handleWarehouseSizeChange = (size: number) => {
  warehousePagination.pageSize = size
  warehousePagination.page = 1
  loadWarehouseList()
}

const handleWarehousePageChange = (page: number) => {
  warehousePagination.page = page
  loadWarehouseList()
}

const handleAddWarehouse = () => {
  warehouseDialogTitle.value = '新增发货地址'
  Object.assign(warehouseForm, {
    id: undefined,
    warehouseName: '',
    contactName: '',
    contactPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    zipCode: '',
    isDefault: 0,
    status: 1
  })
  warehouseDialogVisible.value = true
}

const handleEditWarehouse = async (row: WarehouseAddressVO) => {
  warehouseDialogTitle.value = '编辑发货地址'
  try {
    const address = await getWarehouseAddressById(row.id!)
    Object.assign(warehouseForm, address)
    warehouseDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

const handleWarehouseSubmit = async () => {
  if (!warehouseFormRef.value) return
  await warehouseFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (warehouseForm.id) {
          await updateWarehouseAddress(warehouseForm.id, warehouseForm)
          ElMessage.success('更新成功')
        } else {
          await addWarehouseAddress(warehouseForm)
          ElMessage.success('新增成功')
        }
        warehouseDialogVisible.value = false
        loadWarehouseList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const handleWarehouseDialogClose = () => {
  warehouseFormRef.value?.resetFields()
}

const handleWarehouseStatusChange = async (row: WarehouseAddressVO) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateWarehouseAddressStatus(row.id!, newStatus)
    ElMessage.success('操作成功')
    loadWarehouseList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleSetDefaultWarehouse = async (row: WarehouseAddressVO) => {
  try {
    await setDefaultWarehouseAddress(row.id!)
    ElMessage.success('设置默认地址成功')
    loadWarehouseList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDeleteWarehouse = async (row: WarehouseAddressVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该发货地址吗？', '提示', {
      type: 'warning'
    })
    await deleteWarehouseAddress(row.id!)
    ElMessage.success('删除成功')
    loadWarehouseList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadTemplateList()
})
</script>

<style scoped lang="scss">
.logistics-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .tab-content {
    .toolbar {
      margin-bottom: 20px;
    }

    .search-form {
      margin-bottom: 20px;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .text-muted {
    color: #909399;
  }
}
</style>
