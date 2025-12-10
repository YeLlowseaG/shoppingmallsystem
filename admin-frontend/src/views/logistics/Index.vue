<template>
  <div class="logistics-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>物流管理</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 物流公司管理 -->
        <el-tab-pane label="物流公司" name="company">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="handleAddCompany">
                <el-icon><Plus /></el-icon>
                新增物流公司
              </el-button>
            </div>

            <!-- 搜索栏 -->
            <el-form :inline="true" :model="companySearchForm" class="search-form">
              <el-form-item label="关键词">
                <el-input v-model="companySearchForm.keyword" placeholder="公司名称/编码" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="companySearchForm.status" placeholder="请选择状态" clearable style="width: 180px">
                  <el-option label="全部" :value="undefined" />
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadCompanyList">查询</el-button>
                <el-button @click="resetCompanySearch">重置</el-button>
              </el-form-item>
            </el-form>

            <!-- 物流公司列表 -->
            <el-table :data="companyList" v-loading="companyLoading" border>
              <el-table-column prop="companyCode" label="公司编码" width="150" />
              <el-table-column prop="companyName" label="公司名称" width="200" />
              <el-table-column prop="companyShortName" label="简称" width="120" />
              <el-table-column prop="contactPhone" label="联系电话" width="150" />
              <el-table-column prop="website" label="官网" />
              <el-table-column prop="sortOrder" label="排序" width="80" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleEditCompany(row)">编辑</el-button>
                  <el-button type="success" link @click="handleCompanyStatusChange(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteCompany(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="companyPagination.page"
                v-model:page-size="companyPagination.pageSize"
                :total="companyPagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleCompanySizeChange"
                @current-change="handleCompanyPageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 配送方式管理 -->
        <el-tab-pane label="配送方式" name="method">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="handleAddMethod">
                <el-icon><Plus /></el-icon>
                新增配送方式
              </el-button>
            </div>

            <!-- 搜索栏 -->
            <el-form :inline="true" :model="methodSearchForm" class="search-form">
              <el-form-item label="关键词">
                <el-input v-model="methodSearchForm.keyword" placeholder="配送方式名称/编码" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="methodSearchForm.status" placeholder="请选择状态" clearable style="width: 180px">
                  <el-option label="全部" :value="undefined" />
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadMethodList">查询</el-button>
                <el-button @click="resetMethodSearch">重置</el-button>
              </el-form-item>
            </el-form>

            <!-- 配送方式列表 -->
            <el-table :data="methodList" v-loading="methodLoading" border>
              <el-table-column prop="methodCode" label="编码" width="150" />
              <el-table-column prop="methodName" label="配送方式名称" width="200" />
              <el-table-column prop="logisticsCompanyName" label="物流公司" width="150" />
              <el-table-column prop="basePrice" label="基础运费" width="120">
                <template #default="{ row }">
                  ¥{{ row.basePrice?.toFixed(2) || '0.00' }}
                </template>
              </el-table-column>
              <el-table-column prop="calculationType" label="计算方式" width="120">
                <template #default="{ row }">
                  {{ getCalculationTypeName(row.calculationType) }}
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
                  <el-button type="primary" link @click="handleEditMethod(row)">编辑</el-button>
                  <el-button type="success" link @click="handleMethodStatusChange(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteMethod(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="methodPagination.page"
                v-model:page-size="methodPagination.pageSize"
                :total="methodPagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleMethodSizeChange"
                @current-change="handleMethodPageChange"
              />
            </div>
          </div>
        </el-tab-pane>

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
              <el-table-column prop="templateName" label="模板名称" width="200" />
              <el-table-column prop="calculationType" label="计算方式" width="120">
                <template #default="{ row }">
                  {{ getTemplateCalculationTypeName(row.calculationType) }}
                </template>
              </el-table-column>
              <el-table-column prop="freeShippingAmount" label="包邮金额" width="120">
                <template #default="{ row }">
                  {{ row.freeShippingAmount ? `¥${row.freeShippingAmount.toFixed(2)}` : '-' }}
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
                  <el-button type="primary" link @click="handleEditTemplate(row)">编辑</el-button>
                  <el-button type="info" link @click="handleViewTemplateRules(row)">查看规则</el-button>
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
      </el-tabs>
    </el-card>

    <!-- 物流公司对话框 -->
    <el-dialog
      v-model="companyDialogVisible"
      :title="companyDialogTitle"
      width="600px"
      @close="handleCompanyDialogClose"
    >
      <el-form :model="companyForm" :rules="companyRules" ref="companyFormRef" label-width="120px">
        <el-form-item label="公司编码" prop="companyCode">
          <el-input v-model="companyForm.companyCode" :disabled="!!companyForm.id" placeholder="请输入公司编码" />
        </el-form-item>
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="companyForm.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="公司简称">
          <el-input v-model="companyForm.companyShortName" placeholder="请输入公司简称" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="companyForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="官网地址">
          <el-input v-model="companyForm.website" placeholder="请输入官网地址" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="companyForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="companyForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="companyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCompanySubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 配送方式对话框 -->
    <el-dialog
      v-model="methodDialogVisible"
      :title="methodDialogTitle"
      width="700px"
      @close="handleMethodDialogClose"
    >
      <el-form :model="methodForm" :rules="methodRules" ref="methodFormRef" label-width="120px">
        <el-form-item label="配送方式编码" prop="methodCode">
          <el-input v-model="methodForm.methodCode" :disabled="!!methodForm.id" placeholder="请输入配送方式编码" />
        </el-form-item>
        <el-form-item label="配送方式名称" prop="methodName">
          <el-input v-model="methodForm.methodName" placeholder="请输入配送方式名称" />
        </el-form-item>
        <el-form-item label="物流公司">
          <el-select v-model="methodForm.logisticsCompanyId" placeholder="请选择物流公司" clearable style="width: 100%">
            <el-option
              v-for="company in enabledCompanies"
              :key="company.id"
              :label="company.companyName"
              :value="company.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="配送方式描述">
          <el-input v-model="methodForm.description" type="textarea" :rows="3" placeholder="请输入配送方式描述" />
        </el-form-item>
        <el-form-item label="运费模板">
          <el-select v-model="methodForm.shippingTemplateId" placeholder="请选择运费模板" clearable style="width: 100%">
            <el-option
              v-for="template in enabledTemplates"
              :key="template.id"
              :label="template.templateName"
              :value="template.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="基础运费">
          <el-input-number v-model="methodForm.basePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计算方式" prop="calculationType">
          <el-select v-model="methodForm.calculationType" style="width: 100%">
            <el-option label="固定运费" :value="1" />
            <el-option label="按重量" :value="2" />
            <el-option label="按件数" :value="3" />
            <el-option label="按金额" :value="4" />
            <el-option label="运费模板" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="methodForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="methodForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="methodDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleMethodSubmit">确定</el-button>
      </template>
    </el-dialog>

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
          <el-select v-model="templateForm.calculationType" style="width: 100%">
            <el-option label="按重量" :value="1" />
            <el-option label="按件数" :value="2" />
            <el-option label="按金额" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="包邮金额">
          <el-input-number v-model="templateForm.freeShippingAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="包邮重量(kg)">
          <el-input-number v-model="templateForm.freeShippingWeight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="包邮件数">
          <el-input-number v-model="templateForm.freeShippingQuantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-divider>默认运费规则</el-divider>
        <el-form-item label="默认首重(kg)">
          <el-input-number v-model="templateForm.defaultFirstWeight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="默认首重价格">
          <el-input-number v-model="templateForm.defaultFirstPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="默认续重(kg)">
          <el-input-number v-model="templateForm.defaultContinueWeight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="默认续重价格">
          <el-input-number v-model="templateForm.defaultContinuePrice" :min="0" :precision="2" style="width: 100%" />
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

    <!-- 运费规则查看对话框 -->
    <el-dialog v-model="rulesDialogVisible" title="运费规则" width="1000px">
      <el-table :data="currentTemplateRules" border>
        <el-table-column prop="regionName" label="地区" width="200" />
        <el-table-column prop="firstWeight" label="首重(kg)" width="100" />
        <el-table-column prop="firstPrice" label="首重价格" width="120" />
        <el-table-column prop="continueWeight" label="续重(kg)" width="100" />
        <el-table-column prop="continuePrice" label="续重价格" width="120" />
        <el-table-column prop="freeShippingAmount" label="包邮金额" width="120" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getLogisticsCompanyList,
  getAllEnabledLogisticsCompanies,
  getLogisticsCompanyById,
  addLogisticsCompany,
  updateLogisticsCompany,
  deleteLogisticsCompany,
  updateLogisticsCompanyStatus,
  getShippingMethodList,
  getShippingMethodById,
  addShippingMethod,
  updateShippingMethod,
  deleteShippingMethod,
  updateShippingMethodStatus,
  getShippingTemplateList,
  getShippingTemplateById,
  addShippingTemplate,
  updateShippingTemplate,
  deleteShippingTemplate,
  updateShippingTemplateStatus,
  getAllEnabledShippingTemplates,
  type LogisticsCompanyVO,
  type ShippingMethodVO,
  type ShippingTemplateVO,
  type ShippingRuleVO
} from '@/api/admin/logistics'

// 当前标签页
const activeTab = ref('company')

// ==================== 物流公司管理 ====================
const companyList = ref<LogisticsCompanyVO[]>([])
const companyLoading = ref(false)
const companySearchForm = reactive({
  keyword: '',
  status: undefined as number | undefined
})
const companyPagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const companyDialogVisible = ref(false)
const companyDialogTitle = ref('新增物流公司')
const companyFormRef = ref()
const companyForm = reactive<LogisticsCompanyVO>({
  companyCode: '',
  companyName: '',
  companyShortName: '',
  contactPhone: '',
  website: '',
  sortOrder: 0,
  status: 1
})

const companyRules = {
  companyCode: [{ required: true, message: '请输入公司编码', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// ==================== 配送方式管理 ====================
const methodList = ref<ShippingMethodVO[]>([])
const methodLoading = ref(false)
const methodSearchForm = reactive({
  keyword: '',
  status: undefined as number | undefined
})
const methodPagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const methodDialogVisible = ref(false)
const methodDialogTitle = ref('新增配送方式')
const methodFormRef = ref()
const methodForm = reactive<ShippingMethodVO>({
  methodCode: '',
  methodName: '',
  logisticsCompanyId: undefined,
  description: '',
  shippingTemplateId: undefined,
  basePrice: 0,
  calculationType: 1,
  sortOrder: 0,
  status: 1
})

const methodRules = {
  methodCode: [{ required: true, message: '请输入配送方式编码', trigger: 'blur' }],
  methodName: [{ required: true, message: '请输入配送方式名称', trigger: 'blur' }],
  calculationType: [{ required: true, message: '请选择计算方式', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

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
  freeShippingAmount: 0,
  freeShippingWeight: 0,
  freeShippingQuantity: 0,
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
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 启用的物流公司和运费模板（用于下拉选择）
const enabledCompanies = ref<LogisticsCompanyVO[]>([])
const enabledTemplates = ref<ShippingTemplateVO[]>([])

// 运费规则查看
const rulesDialogVisible = ref(false)
const currentTemplateRules = ref<ShippingRuleVO[]>([])

// ==================== 通用方法 ====================
const handleTabChange = (name: string) => {
  if (name === 'company') {
    loadCompanyList()
  } else if (name === 'method') {
    loadMethodList()
    loadEnabledCompanies()
    loadEnabledTemplates()
  } else if (name === 'template') {
    loadTemplateList()
  }
}

// ==================== 物流公司管理方法 ====================
const loadCompanyList = async () => {
  companyLoading.value = true
  try {
    const response = await getLogisticsCompanyList({
      page: companyPagination.page,
      pageSize: companyPagination.pageSize,
      keyword: companySearchForm.keyword || undefined,
      status: companySearchForm.status
    })
    companyList.value = response.records || []
    companyPagination.total = response.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    companyLoading.value = false
  }
}

const resetCompanySearch = () => {
  companySearchForm.keyword = ''
  companySearchForm.status = undefined
  companyPagination.page = 1
  loadCompanyList()
}

const handleCompanySizeChange = (size: number) => {
  companyPagination.pageSize = size
  companyPagination.page = 1
  loadCompanyList()
}

const handleCompanyPageChange = (page: number) => {
  companyPagination.page = page
  loadCompanyList()
}

const handleAddCompany = () => {
  companyDialogTitle.value = '新增物流公司'
  Object.assign(companyForm, {
    id: undefined,
    companyCode: '',
    companyName: '',
    companyShortName: '',
    contactPhone: '',
    website: '',
    sortOrder: 0,
    status: 1
  })
  companyDialogVisible.value = true
}

const handleEditCompany = async (row: LogisticsCompanyVO) => {
  companyDialogTitle.value = '编辑物流公司'
  try {
    const company = await getLogisticsCompanyById(row.id!)
    Object.assign(companyForm, company)
    companyDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

const handleCompanySubmit = async () => {
  if (!companyFormRef.value) return
  await companyFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (companyForm.id) {
          await updateLogisticsCompany(companyForm.id, companyForm)
          ElMessage.success('更新成功')
        } else {
          await addLogisticsCompany(companyForm)
          ElMessage.success('新增成功')
        }
        companyDialogVisible.value = false
        loadCompanyList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const handleCompanyDialogClose = () => {
  companyFormRef.value?.resetFields()
}

const handleCompanyStatusChange = async (row: LogisticsCompanyVO) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateLogisticsCompanyStatus(row.id!, newStatus)
    ElMessage.success('操作成功')
    loadCompanyList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDeleteCompany = async (row: LogisticsCompanyVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该物流公司吗？', '提示', {
      type: 'warning'
    })
    await deleteLogisticsCompany(row.id!)
    ElMessage.success('删除成功')
    loadCompanyList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// ==================== 配送方式管理方法 ====================
const loadMethodList = async () => {
  methodLoading.value = true
  try {
    const response = await getShippingMethodList({
      page: methodPagination.page,
      pageSize: methodPagination.pageSize,
      keyword: methodSearchForm.keyword || undefined,
      status: methodSearchForm.status
    })
    methodList.value = response.records || []
    methodPagination.total = response.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    methodLoading.value = false
  }
}

const loadEnabledCompanies = async () => {
  try {
    enabledCompanies.value = await getAllEnabledLogisticsCompanies()
  } catch (error: any) {
    console.error('加载物流公司失败', error)
  }
}

const loadEnabledTemplates = async () => {
  try {
    enabledTemplates.value = await getAllEnabledShippingTemplates()
  } catch (error: any) {
    console.error('加载运费模板失败', error)
  }
}

const resetMethodSearch = () => {
  methodSearchForm.keyword = ''
  methodSearchForm.status = undefined
  methodPagination.page = 1
  loadMethodList()
}

const handleMethodSizeChange = (size: number) => {
  methodPagination.pageSize = size
  methodPagination.page = 1
  loadMethodList()
}

const handleMethodPageChange = (page: number) => {
  methodPagination.page = page
  loadMethodList()
}

const handleAddMethod = () => {
  methodDialogTitle.value = '新增配送方式'
  Object.assign(methodForm, {
    id: undefined,
    methodCode: '',
    methodName: '',
    logisticsCompanyId: undefined,
    description: '',
    shippingTemplateId: undefined,
    basePrice: 0,
    calculationType: 1,
    sortOrder: 0,
    status: 1
  })
  methodDialogVisible.value = true
}

const handleEditMethod = async (row: ShippingMethodVO) => {
  methodDialogTitle.value = '编辑配送方式'
  try {
    const method = await getShippingMethodById(row.id!)
    Object.assign(methodForm, method)
    methodDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

const handleMethodSubmit = async () => {
  if (!methodFormRef.value) return
  await methodFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (methodForm.id) {
          await updateShippingMethod(methodForm.id, methodForm)
          ElMessage.success('更新成功')
        } else {
          await addShippingMethod(methodForm)
          ElMessage.success('新增成功')
        }
        methodDialogVisible.value = false
        loadMethodList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const handleMethodDialogClose = () => {
  methodFormRef.value?.resetFields()
}

const handleMethodStatusChange = async (row: ShippingMethodVO) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateShippingMethodStatus(row.id!, newStatus)
    ElMessage.success('操作成功')
    loadMethodList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDeleteMethod = async (row: ShippingMethodVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该配送方式吗？', '提示', {
      type: 'warning'
    })
    await deleteShippingMethod(row.id!)
    ElMessage.success('删除成功')
    loadMethodList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const getCalculationTypeName = (type: number) => {
  const map: Record<number, string> = {
    1: '固定运费',
    2: '按重量',
    3: '按件数',
    4: '按金额',
    5: '运费模板'
  }
  return map[type] || '未知'
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
    calculationType: 1,
    freeShippingAmount: 0,
    freeShippingWeight: 0,
    freeShippingQuantity: 0,
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

const handleViewTemplateRules = async (row: ShippingTemplateVO) => {
  try {
    const template = await getShippingTemplateById(row.id!)
    currentTemplateRules.value = template.rules || []
    rulesDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

const getTemplateCalculationTypeName = (type: number) => {
  const map: Record<number, string> = {
    1: '按重量',
    2: '按件数',
    3: '按金额'
  }
  return map[type] || '未知'
}

onMounted(() => {
  loadCompanyList()
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
}
</style>

