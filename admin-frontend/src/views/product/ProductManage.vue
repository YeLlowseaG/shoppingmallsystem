<template>
  <div class="product-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-section">
        <!-- 筛选条件 -->
        <div class="filters-row">
          <div class="filter-item">
            <label>分类</label>
            <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
              <el-option
                v-for="category in flatCategories"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </div>
          
          <div class="filter-item">
            <label>关键词</label>
            <el-input 
              v-model="searchForm.keyword" 
              placeholder="商品名称/编码" 
              clearable 
              @keyup.enter="handleSearch"
            />
          </div>
          
          <div class="filter-item">
            <label>状态</label>
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="草稿" value="草稿" />
              <el-option label="上架" value="上架" />
              <el-option label="下架" value="下架" />
            </el-select>
          </div>
          
          <div class="action-buttons">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </div>
        </div>
        
        <!-- 排序和操作栏 -->
        <div class="toolbar-row">
          <div class="sort-section">
            <span class="sort-label">排序:</span>
            <div class="sort-buttons">
              <el-button 
                :class="{ 'is-active': searchForm.sortBy === 'create_time_desc' }"
                class="sort-btn"
                @click="handleQuickSort('create_time_desc')"
              >
                添加时间 <el-icon class="sort-icon"><ArrowDown /></el-icon>
              </el-button>
              <el-button 
                :class="{ 'is-active': searchForm.sortBy === 'create_time_asc' }"
                class="sort-btn"
                @click="handleQuickSort('create_time_asc')"
              >
                添加时间 <el-icon class="sort-icon"><ArrowUp /></el-icon>
              </el-button>
              <el-button 
                :class="{ 'is-active': searchForm.sortBy === 'price_desc' }"
                class="sort-btn"
                @click="handleQuickSort('price_desc')"
              >
                价格 <el-icon class="sort-icon"><ArrowDown /></el-icon>
              </el-button>
              <el-button 
                :class="{ 'is-active': searchForm.sortBy === 'price_asc' }"
                class="sort-btn"
                @click="handleQuickSort('price_asc')"
              >
                价格 <el-icon class="sort-icon"><ArrowUp /></el-icon>
              </el-button>
              <el-button 
                :class="{ 'is-active': searchForm.sortBy === 'sales_desc' }"
                class="sort-btn"
                @click="handleQuickSort('sales_desc')"
              >
                销量 <el-icon class="sort-icon"><ArrowDown /></el-icon>
              </el-button>
              <el-button 
                :class="{ 'is-active': searchForm.sortBy === 'stock_desc' }"
                class="sort-btn"
                @click="handleQuickSort('stock_desc')"
              >
                库存 <el-icon class="sort-icon"><ArrowDown /></el-icon>
              </el-button>
            </div>
          </div>
          
          <div class="page-actions">
            <el-button type="primary" @click="router.push('/admin/product/add')">
              <el-icon><Plus /></el-icon>
              添加商品
            </el-button>
            <el-button type="success" @click="importDialogVisible = true">
              <el-icon><Upload /></el-icon>
              批量导入
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品列表 -->
      <el-table 
        :data="productList" 
        border 
        style="width: 100%"
        @sort-change="handleTableSortChange"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="mainImage" label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.mainImage"
              :src="row.mainImage"
              style="width: 60px; height: 60px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="basePrice" label="价格" width="100" sortable="custom">
          <template #default="{ row }">
            ¥{{ parseFloat(row.basePrice).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" sortable="custom" />
        <el-table-column prop="salesCount" label="销量" width="80" sortable="custom" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '上架' ? 'success' : row.status === '草稿' ? '' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === '上架' ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === '上架' ? '下架' : '上架' }}
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleSyncToErp(row)"
              :loading="row.syncing"
            >
              同步到ERP
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
        @size-change="loadProductList"
        @current-change="loadProductList"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 批量导入对话框 -->
    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑商品"
      width="980px"
      class="product-edit-dialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="商品编码" prop="productCode">
          <el-input v-model="formData.productCode" placeholder="请输入商品编码/SKU" />
        </el-form-item>
        <el-form-item label="条码" prop="barcode">
          <el-input v-model="formData.barcode" placeholder="请输入条码（可选）" />
        </el-form-item>
        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="formData.unit" placeholder="请输入计量单位，如：个、件、盒" />
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="formData.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in flatCategories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品品牌" prop="brandId">
          <el-select
            v-model="formData.brandId"
            placeholder="请选择商品品牌（可选）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="brand in brandOptions"
              :key="brand.id"
              :label="brand.brandName"
              :value="brand.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="运费模板" prop="shippingTemplateId">
          <el-select
            v-model="formData.shippingTemplateId"
            placeholder="请选择运费模板（可选，不选则包邮）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="template in shippingTemplates"
              :key="template.id"
              :label="template.templateName"
              :value="template.id"
            />
          </el-select>
          <div class="form-tip">不选择运费模板则该商品包邮</div>
        </el-form-item>

        <!-- 价格与库存 -->
        <el-divider content-position="left">价格与库存</el-divider>

        <div class="price-stock-grid">
          <el-form-item label="基础价" prop="basePrice" required>
            <el-input-number
              v-model="formData.basePrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="建议零售价" prop="suggestedRetailPrice">
            <el-input-number
              v-model="formData.suggestedRetailPrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="市场零售价" prop="marketRetailPrice">
            <el-input-number
              v-model="formData.marketRetailPrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="商品库存" prop="stock" required>
            <el-input-number
              v-model="formData.stock"
              :min="0"
              :step="1"
              :disabled="formData.enableSpec"
              controls-position="right"
              style="width: 100%"
            />
            <div v-if="formData.enableSpec" class="form-tip" style="margin-top: 5px;">
              启用规格后，总库存由SKU库存自动计算：{{ totalEditSkuStock }}
            </div>
          </el-form-item>

          <el-form-item label="警戒库存" prop="warningStock">
            <el-input-number
              v-model="formData.warningStock"
              :min="0"
              :step="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="商品重量(g)" prop="weight">
            <el-input-number
              v-model="formData.weight"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <!-- 会员价设置（单独一行） -->
        <div class="member-price-row">
          <el-form-item label="启用会员价">
            <el-switch v-model="formData.enableMemberPrice" :active-value="1" :inactive-value="0" />
            <span class="form-tip" style="margin-left: 10px;">启用后以会员价作为售价，否则以基础价作为售价</span>
          </el-form-item>
          <el-form-item label="会员价" prop="memberPrice" v-if="formData.enableMemberPrice === 1">
            <el-input-number
              v-model="formData.memberPrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 200px"
            />
          </el-form-item>
        </div>

        <!-- 商品规格配置 -->
        <el-divider content-position="left">商品规格配置</el-divider>
        
        <el-form-item label="是否启用规格" prop="enableSpec">
          <el-switch v-model="formData.enableSpec" @change="handleEnableSpecChange" />
          <div class="form-tip">启用后可为商品配置不同规格的SKU（如颜色、尺寸等）</div>
        </el-form-item>

        <!-- 规格配置区域 -->
        <div v-if="formData.enableSpec" class="spec-config-area">
          <!-- 规格属性配置 -->
          <div class="spec-section">
            <div class="spec-section-title"><span class="required-star">*</span> 规格属性</div>
            <div class="spec-keys-wrapper">
              <div 
                v-for="(specKey, keyIndex) in editSpecKeys" 
                :key="keyIndex"
                class="spec-key-item"
              >
                <div class="spec-key-header">
                  <el-input 
                    v-model="specKey.specName" 
                    placeholder="请输入规格名称（如：颜色、尺寸）"
                    style="width: 200px"
                  />
                  <el-button 
                    type="danger" 
                    size="small" 
                    :icon="Delete" 
                    @click="removeEditSpecKey(keyIndex)"
                    :disabled="editSpecKeys.length <= 1"
                  >
                    删除规格
                  </el-button>
                </div>
                
                <div class="spec-values-wrapper">
                  <div class="spec-values-header">规格值：</div>
                  <div class="spec-values-list">
                    <div 
                      v-for="(specValue, valueIndex) in specKey.values" 
                      :key="valueIndex"
                      class="spec-value-item"
                    >
                      <el-input 
                        v-model="specValue.specValue" 
                        placeholder="规格值"
                        style="width: 150px"
                      />
                      <el-button 
                        type="danger" 
                        size="small" 
                        :icon="Delete" 
                        @click="removeEditSpecValue(keyIndex, valueIndex)"
                        :disabled="specKey.values.length <= 1"
                      />
                    </div>
                    <el-button 
                      type="primary" 
                      size="small" 
                      :icon="Plus" 
                      @click="addEditSpecValue(keyIndex)"
                    >
                      添加规格值
                    </el-button>
                  </div>
                </div>
              </div>
              
              <el-button
                type="primary"
                :icon="Plus"
                @click="addEditSpecKey"
                style="margin-top: 20px;"
              >
                添加规格属性
              </el-button>
            </div>
          </div>

          <!-- SKU列表 -->
          <div class="spec-section">
            <div class="spec-section-title"><span class="required-star">*</span> SKU列表</div>
            <div class="sku-list-wrapper">
              <div class="sku-list-header">
                <el-button 
                  type="primary" 
                  @click="generateEditSkuList"
                  :disabled="!canGenerateEditSkus"
                >
                  生成SKU
                </el-button>
                <span class="tip">根据规格属性自动生成SKU组合</span>
              </div>
              
              <el-table
                v-if="editSkuList.length > 0"
                :data="editSkuList"
                border
                class="sku-table"
                size="small"
                style="width: 100%; min-width: 1200px;"
              >
                <el-table-column prop="specCombinationText" label="规格组合" min-width="120" align="left" />
                <el-table-column label="SKU编码" min-width="150">
                  <template #default="{ row, $index }">
                    <el-input
                      v-model="row.skuCode"
                      placeholder="SKU编码"
                      size="small"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="基础价" min-width="130">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.price"
                      :min="0"
                      :precision="2"
                      :step="0.01"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="建议零售价" min-width="130">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.suggestedRetailPrice"
                      :min="0"
                      :precision="2"
                      :step="0.01"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="市场零售价" min-width="130">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.marketRetailPrice"
                      :min="0"
                      :precision="2"
                      :step="0.01"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="启用会员价" min-width="100" align="center">
                  <template #default="{ row, $index }">
                    <el-switch
                      v-model="row.enableMemberPrice"
                      :active-value="1"
                      :inactive-value="0"
                      size="small"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="会员价" min-width="130">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.memberPrice"
                      :min="0"
                      :precision="2"
                      :step="0.01"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                      :disabled="row.enableMemberPrice !== 1"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="库存" min-width="120">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.stock"
                      :min="0"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="重量(g)" min-width="120">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.weight"
                      :min="0"
                      :precision="2"
                      :step="0.01"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="警戒库存" min-width="120">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.warningStock"
                      :min="0"
                      size="small"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="70" fixed="right">
                  <template #default="{ row, $index }">
                    <el-button
                      type="danger"
                      size="small"
                      :icon="Delete"
                      @click="removeEditSku($index)"
                    />
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>

        <el-form-item label="主图" prop="mainImage">
          <div class="upload-wrapper">
            <!-- 主图预览 -->
            <div v-if="formData.mainImage" class="image-preview">
              <el-image
                :src="formData.mainImage"
                fit="contain"
                style="width: 150px; height: 150px"
                :preview-src-list="[formData.mainImage]"
              />
              <el-button
                type="danger"
                size="small"
                circle
                :icon="Delete"
                class="delete-btn"
                @click="formData.mainImage = ''"
              />
            </div>
            <!-- 上传按钮 -->
            <el-upload
              v-else
              class="image-uploader"
              action="/api/common/upload/image"
              :show-file-list="false"
              :on-success="handleMainImageSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeImageUpload"
              accept="image/*"
            >
              <div class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">上传主图</div>
              </div>
            </el-upload>
            <!-- URL输入框 -->
            <div class="url-input">
              <el-input
                v-model="formData.mainImage"
                placeholder="或直接输入主图URL"
                clearable
              />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="详情轮播图" prop="images">
          <div class="detail-images-wrapper">
            <el-upload
              v-model:file-list="detailImageList"
              action="/api/common/upload/image"
              list-type="picture-card"
              :on-success="handleDetailImageSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeImageUpload"
              :on-remove="handleDetailImageRemove"
              accept="image/*"
              multiple
              :limit="5"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">最多上传5张轮播图，将在详情页顶部轮播展示</div>
          </div>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <RichTextEditor
            v-model="formData.description"
            placeholder="请输入商品详细描述"
            height="500px"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio label="草稿">草稿</el-radio>
            <el-radio label="上架">上架</el-radio>
            <el-radio label="下架">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- SKU规格管理对话框 -->
    <el-dialog
      v-model="skuDialogVisible"
      title="SKU规格管理"
      width="1200px"
      :before-close="handleSkuDialogClose"
      :close-on-click-modal="false"
      class="sku-management-dialog"
      top="3vh"
      :fullscreen="false"
      :modal="true"
      :destroy-on-close="false"
    >
      <div class="sku-manager">
        <!-- 规格属性配置 -->
        <el-card class="spec-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>规格属性配置</span>
              <el-button type="primary" size="small" @click="addSpecKey">添加规格</el-button>
            </div>
          </template>
          
          <div class="spec-keys-list">
            <div 
              v-for="(specKey, keyIndex) in skuSpecKeys" 
              :key="keyIndex"
              class="spec-key-item"
            >
              <div class="spec-key-header">
                <el-input 
                  v-model="specKey.specName" 
                  placeholder="规格名称（如：颜色、尺寸）"
                  style="width: 200px"
                />
                <el-button 
                  type="danger" 
                  size="small" 
                  :icon="Delete" 
                  @click="removeSpecKey(keyIndex)"
                  :disabled="skuSpecKeys.length <= 1"
                >
                  删除
                </el-button>
              </div>
              
              <div class="spec-values-wrapper">
                <div class="spec-values-header">规格值：</div>
                <div class="spec-values-list">
                  <div 
                    v-for="(specValue, valueIndex) in specKey.values" 
                    :key="valueIndex"
                    class="spec-value-item"
                  >
                    <el-input 
                      v-model="specValue.specValue" 
                      placeholder="规格值"
                      style="width: 150px"
                    />
                    <el-button 
                      type="danger" 
                      size="small" 
                      :icon="Delete" 
                      @click="removeSpecValue(keyIndex, valueIndex)"
                      :disabled="specKey.values.length <= 1"
                    />
                  </div>
                  <el-button 
                    type="primary" 
                    size="small" 
                    :icon="Plus" 
                    @click="addSpecValue(keyIndex)"
                  >
                    添加规格值
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          
          <div class="spec-actions">
            <el-button 
              type="primary" 
              @click="generateSkuList"
              :disabled="!canGenerateSkus"
            >
              生成SKU
            </el-button>
            <span class="tip">根据规格属性自动生成SKU组合</span>
          </div>
        </el-card>

        <!-- SKU列表配置 -->
        <el-card class="sku-card" shadow="never" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>SKU列表配置</span>
              <div class="header-actions">
                <el-button size="small" @click="batchSetPrice">批量设价格</el-button>
                <el-button size="small" @click="batchSetStock">批量设库存</el-button>
              </div>
            </div>
          </template>
          
          <div class="sku-table-container">
            <el-table 
              :data="skuManageList" 
              border 
              class="sku-manage-table"
              height="200"
            >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="specCombinationText" label="规格组合" width="80" align="left" />
            <el-table-column label="SKU编码" width="160">
              <template #default="{ row, $index }">
                <el-input 
                  v-model="row.skuCode" 
                  placeholder="SKU编码"
                  size="small"
                />
              </template>
            </el-table-column>
            <el-table-column label="价格" width="150">
              <template #default="{ row }">
                <el-input-number 
                  v-model="row.price" 
                  :min="0"
                  :precision="2"
                  :step="0.01"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="库存" width="150">
              <template #default="{ row }">
                <el-input-number 
                  v-model="row.stock" 
                  :min="0"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="警戒库存" width="150">
              <template #default="{ row }">
                <el-input-number 
                  v-model="row.warningStock" 
                  :min="0"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="重量(g)" width="150">
              <template #default="{ row }">
                <el-input-number 
                  v-model="row.weight" 
                  :min="0"
                  :precision="2"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-switch 
                  v-model="row.status" 
                  :active-value="1"
                  :inactive-value="0"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row, $index }">
                <el-button 
                  type="danger" 
                  size="small" 
                  :icon="Delete" 
                  @click="removeSku($index)"
                />
              </template>
            </el-table-column>
          </el-table>
          </div>
        </el-card>
      </div>
      
      <template #footer>
        <el-button @click="skuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSkuChanges">保存SKU配置</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="importDialogVisible"
      title="批量导入商品"
      width="600px"
      :before-close="handleImportDialogClose"
    >
      <el-form label-width="120px">
        <el-form-item label="数据文件" required>
          <el-upload
            ref="csvUploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".csv,.xlsx,.xls"
            :on-change="handleCsvChange"
            :on-remove="handleCsvRemove"
            :file-list="csvFileList"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
          <div class="form-tip">支持 CSV 或 Excel (.xlsx/.xls) 格式</div>
        </el-form-item>

        <el-form-item label="图片压缩包">
          <el-upload
            ref="zipUploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".zip"
            :on-change="handleZipChange"
            :on-remove="handleZipRemove"
            :file-list="zipFileList"
          >
            <el-button>选择ZIP文件（可选）</el-button>
          </el-upload>
          <div class="form-tip">图片命名规则：商品编码.jpg（主图）、商品编码_1.jpg（详情图）</div>
        </el-form-item>

        <el-form-item label="下载模板">
          <el-button type="success" @click="downloadTemplate('csv')" style="margin-right: 10px">
            <el-icon><Download /></el-icon>
            下载CSV模板
          </el-button>
          <el-button type="success" @click="downloadTemplate('excel')">
            <el-icon><Download /></el-icon>
            下载Excel模板
          </el-button>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleImportSubmit"
          :loading="importLoading"
          :disabled="!importForm.csvFile"
        >
          开始导入
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="resultDialogVisible"
      title="导入结果"
      width="700px"
    >
      <el-result
        :icon="importResult.failCount > 0 ? 'warning' : 'success'"
        :title="`导入完成`"
      >
        <template #sub-title>
          <div class="import-stats">
            <div>总计: {{ importResult.totalCount }} 个商品</div>
            <div style="color: #67c23a">成功: {{ importResult.successCount }} 个</div>
            <div v-if="importResult.failCount > 0" style="color: #f56c6c">失败: {{ importResult.failCount }} 个</div>
          </div>
        </template>
        <template #extra>
          <div v-if="importResult.warnings.length > 0" class="import-warnings">
            <el-alert type="warning" :closable="false">
              <template #title>
                <div style="font-weight: bold; margin-bottom: 8px">警告信息</div>
                <div v-for="(warning, index) in importResult.warnings" :key="index" style="font-size: 13px">
                  {{ warning }}
                </div>
              </template>
            </el-alert>
          </div>

          <div v-if="importResult.errors.length > 0" class="import-errors">
            <el-alert type="error" :closable="false">
              <template #title>
                <div style="font-weight: bold; margin-bottom: 8px">错误详情</div>
              </template>
            </el-alert>
            <el-table
              :data="importResult.errors"
              border
              max-height="300"
              style="margin-top: 10px"
            >
              <el-table-column prop="row" label="行号" width="80" />
              <el-table-column prop="productCode" label="商品编码" width="150" />
              <el-table-column prop="error" label="错误信息" />
            </el-table>
          </div>
        </template>
      </el-result>

      <template #footer>
        <el-button type="primary" @click="handleResultClose">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadFile, type UploadUserFile } from 'element-plus'
import { Plus, Delete, Upload, Search, ArrowUp, ArrowDown, Download } from '@element-plus/icons-vue'
import {
  getProductPage,
  updateProduct,
  deleteProduct,
  updateProductStatus,
  importProducts,
  type ProductDTO,
  type ProductVO,
  type ProductImportResult
} from '@/api/admin/product'
import { getCategoryTree, type ProductCategoryVO } from '@/api/admin/productCategory'
import { getBrandOptions } from '@/api/admin/brand'
import {
  getSkusByProductId,
  batchCreateSkus,
  updateSku,
  deleteSku,
  getSpecKeysByProductId,
  deleteSpecsByProductId,
  type ProductSkuVO,
  type ProductSkuDTO,
  type ProductSpecKeyVO
} from '@/api/admin/sku'
import { syncProductToErp } from '@/api/admin/erp'
import RichTextEditor from '@/components/common/RichTextEditor.vue'
import request from '@/utils/request'

const router = useRouter()

// 搜索表单
const searchForm = ref({
  categoryId: undefined as number | undefined,
  keyword: '',
  status: '',
  sortBy: 'create_time_desc'
})

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 商品列表
const productList = ref<ProductVO[]>([])

// 分类列表
const categoryTree = ref<ProductCategoryVO[]>([])

// 品牌列表
const brandOptions = ref<any[]>([])

// 运费模板列表
const shippingTemplates = ref<any[]>([])

// 监控运费模板数据变化
watch(shippingTemplates, (newVal) => {
  console.log('📦 shippingTemplates 数据变化:', newVal)
  console.log('📦 运费模板数量:', newVal ? newVal.length : 0)
}, { deep: true })

// 扁平化分类列表（用于下拉选择）
const flatCategories = computed(() => {
  const flatten = (categories: ProductCategoryVO[], level = 0): ProductCategoryVO[] => {
    let result: ProductCategoryVO[] = []
    categories.forEach(category => {
      result.push(category)
      if (category.children && category.children.length > 0) {
        result = result.concat(flatten(category.children, level + 1))
      }
    })
    return result
  }
  return flatten(categoryTree.value)
})

// 对话框
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

// 详情图片列表
const detailImageList = ref<UploadUserFile[]>([])

// 表单数据
const formData = ref<ProductDTO>({
  productCode: '',
  barcode: '',
  unit: '',
  productName: '',
  categoryId: 0,
  brandId: null,
  shippingTemplateId: null,
  basePrice: 0,
  suggestedRetailPrice: 0,
  marketRetailPrice: 0,
  memberPrice: 0,
  enableMemberPrice: 0,
  stock: 0,
  warningStock: 10,
  weight: 0,
  mainImage: '',
  images: '',
  description: '',
  status: '下架',
  enableSpec: false
})

// SKU管理相关状态
const skuDialogVisible = ref(false)
const currentSkuList = ref<ProductSkuVO[]>([])
const skuSpecKeys = ref([
  {
    specName: '',
    values: [{ specValue: '' }]
  }
])
const skuManageList = ref<any[]>([])

// 编辑弹框的SKU规格数据
const editSpecKeys = ref([
  {
    specName: '',
    values: [{ specValue: '' }]
  }
])
const editSkuList = ref<any[]>([])

// 批量导入相关状态
const importDialogVisible = ref(false)
const importForm = ref({
  csvFile: null as File | null,
  imageZip: null as File | null
})
const importLoading = ref(false)
const importResult = ref<any>(null)

// 计算属性：是否可以生成SKU
const canGenerateSkus = computed(() => {
  return skuSpecKeys.value.every(key => 
    key.specName.trim() && 
    key.values.length > 0 && 
    key.values.every(value => value.specValue.trim())
  )
})

// 计算属性：编辑弹框是否可以生成SKU
const canGenerateEditSkus = computed(() => {
  if (!formData.value.enableSpec) return false
  return editSpecKeys.value.every(key =>
    key.specName.trim() &&
    key.values.length > 0 &&
    key.values.every(value => value.specValue.trim())
  )
})

// 计算属性：编辑SKU总库存
const totalEditSkuStock = computed(() => {
  if (!formData.value.enableSpec || editSkuList.value.length === 0) {
    return 0
  }
  return editSkuList.value.reduce((total, sku) => total + (sku.stock || 0), 0)
})

// 监听编辑SKU库存变化，自动更新商品总库存
watch(totalEditSkuStock, (newTotal) => {
  if (formData.value.enableSpec) {
    formData.value.stock = newTotal
  }
}, { deep: true })

// 监听enableSpec变化
watch(() => formData.value.enableSpec, (newValue) => {
  if (newValue) {
    // 启用规格时，设置总库存为SKU总和
    formData.value.stock = totalEditSkuStock.value
  }
})

// 表单验证规则
const formRules: FormRules = {
  productCode: [
    { required: true, message: '请输入商品编码', trigger: 'blur' }
  ],
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  basePrice: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' }
  ]
}

// 加载分类树
const loadCategoryTree = async () => {
  try {
    categoryTree.value = await getCategoryTree()
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

// 加载品牌列表
const loadBrands = async () => {
  try {
    const response = await getBrandOptions()
    brandOptions.value = response || []
  } catch (error) {
    // 静默处理，不显示错误
    brandOptions.value = []
  }
}

// 加载运费模板列表
const loadShippingTemplates = async () => {
  console.log('🚀 开始加载运费模板列表...')
  try {
    const response = await request.get('/api/admin/shipping/template/all')
    console.log('✅ 运费模板API返回数据:', response)
    console.log('✅ 运费模板数量:', response ? response.length : 0)
    shippingTemplates.value = response || []
    console.log('✅ shippingTemplates.value 已设置:', shippingTemplates.value)
  } catch (error) {
    console.error('❌ 加载运费模板失败:', error)
    shippingTemplates.value = []
  }
}

// 加载商品列表
const loadProductList = async () => {
  try {
    console.log('排序参数:', searchForm.value.sortBy)
    const res = await getProductPage(
      pagination.value.current,
      pagination.value.size,
      searchForm.value.categoryId,
      searchForm.value.keyword,
      undefined, // brand 参数
      searchForm.value.status,
      searchForm.value.sortBy
    )
    console.log('API返回数据:', res)
    productList.value = res.records
    pagination.value.total = res.total
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadProductList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    categoryId: undefined,
    keyword: '',
    status: '',
    sortBy: 'create_time_desc'
  }
  handleSearch()
}

// 排序变化处理
const handleSortChange = () => {
  pagination.value.current = 1
  loadProductList()
}

// 快速排序处理
const handleQuickSort = (sortBy: string) => {
  searchForm.value.sortBy = sortBy
  pagination.value.current = 1
  loadProductList()
}

// 表格排序处理
const handleTableSortChange = (sortInfo: any) => {
  const { prop, order } = sortInfo
  if (!prop || !order) {
    searchForm.value.sortBy = 'create_time_desc'
  } else {
    const direction = order === 'ascending' ? 'asc' : 'desc'
    switch (prop) {
      case 'basePrice':
        searchForm.value.sortBy = `price_${direction}`
        break
      case 'stock':
        searchForm.value.sortBy = `stock_${direction}`
        break
      case 'salesCount':
        searchForm.value.sortBy = `sales_${direction}`
        break
      default:
        searchForm.value.sortBy = 'create_time_desc'
    }
  }
  pagination.value.current = 1
  loadProductList()
}

// 图片上传前的校验
const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

// 主图上传成功
const handleMainImageSuccess = (response: any) => {
  if (response.code === 200 && response.data) {
    formData.value.mainImage = response.data.url
    ElMessage.success('主图上传成功')
  } else {
    ElMessage.error('主图上传失败')
  }
}

// 详情图上传成功
const handleDetailImageSuccess = (response: any, file: UploadFile) => {
  if (response.code === 200 && response.data) {
    file.url = response.data.url
    ElMessage.success('详情图上传成功')
  } else {
    ElMessage.error('详情图上传失败')
  }
}

// 详情图移除
const handleDetailImageRemove = (file: UploadFile) => {
  const index = detailImageList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    detailImageList.value.splice(index, 1)
  }
}

// 图片上传失败
const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

// 编辑商品
const handleEdit = async (row: ProductVO) => {
  console.log('=== 开始编辑商品 ID:', row.id, '===')

  // 完全重置所有规格和SKU相关数据，防止数据残留
  editSpecKeys.value = []
  editSkuList.value = []
  currentSkuList.value = []

  console.log('已重置：editSpecKeys, editSkuList, currentSkuList')

  formData.value = {
    id: row.id,
    productCode: row.productCode,
    barcode: row.barcode || '',
    unit: row.unit || '',
    productName: row.productName,
    categoryId: row.categoryId,
    brandId: row.brandId || null,
    shippingTemplateId: row.shippingTemplateId || null,
    basePrice: row.basePrice,
    suggestedRetailPrice: row.suggestedRetailPrice || 0,
    marketRetailPrice: row.marketRetailPrice || 0,
    memberPrice: row.memberPrice || 0,
    enableMemberPrice: row.enableMemberPrice || 0,
    stock: row.stock,
    warningStock: row.warningStock || 10,
    weight: row.weight || 0,
    mainImage: row.mainImage,
    images: JSON.stringify(row.imageList),
    description: row.description,
    status: row.status,
    enableSpec: false  // 默认关闭，稍后根据SKU数据设置
  }

  // 初始化详情图片列表
  if (row.imageList && row.imageList.length > 0) {
    detailImageList.value = row.imageList.map((url, index) => ({
      name: `image-${index}`,
      url: url
    }))
  } else {
    detailImageList.value = []
  }

  // 加载SKU数据
  try {
    const skus = await getSkusByProductId(row.id)
    console.log('从API获取的SKU数据:', skus)
    console.log('SKU库存详情（从API）:', skus?.map(s => ({ 
      id: s.id, 
      skuCode: s.skuCode, 
      spec: s.specCombination, 
      stock: s.stock,
      stockType: typeof s.stock
    })))
    
    // 保存当前SKU列表，用于后续删除
    currentSkuList.value = skus || []

    if (skus && skus.length > 0) {
      // 有SKU数据，设置启用规格
      formData.value.enableSpec = true

      // 从SKU数据重建规格属性和SKU列表
      const specMap = new Map<string, Set<string>>()

      skus.forEach(sku => {
        // 解析规格组合，格式如: {"颜色":"白色","尺码":"M"}
        try {
          const specs = JSON.parse(sku.specCombination)
          Object.entries(specs).forEach(([key, value]) => {
            if (!specMap.has(key)) {
              specMap.set(key, new Set())
            }
            specMap.get(key)!.add(value as string)
          })
        } catch (e) {
          console.error('解析SKU规格组合失败:', e)
        }

        // 添加到editSkuList
        editSkuList.value.push({
          skuCode: sku.skuCode,
          specCombination: sku.specCombination,
          specCombinationText: Object.values(JSON.parse(sku.specCombination)).join('/'),
          price: sku.price,
          suggestedRetailPrice: sku.suggestedRetailPrice ?? 0,
          marketRetailPrice: sku.marketRetailPrice ?? 0,
          memberPrice: sku.memberPrice ?? 0,
          enableMemberPrice: sku.enableMemberPrice ?? 0,
          stock: sku.stock ?? 0,
          warningStock: sku.warningStock ?? 0,
          weight: sku.weight ?? 0,
          status: sku.status ?? 1
        })
      })

      // 重建规格属性
      specMap.forEach((values, key) => {
        editSpecKeys.value.push({
          specName: key,
          values: Array.from(values).map(v => ({ specValue: v }))
        })
      })
    }
  } catch (error) {
    console.error('加载SKU数据失败:', error)
    currentSkuList.value = []
  }

  dialogVisible.value = true
}

// 提交表单（只用于编辑）
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      console.log('=== 开始保存商品数据 ===')
      console.log('当前正在编辑的商品ID:', formData.value.id)
      console.log('当前商品编码:', formData.value.productCode)
      console.log('当前商品名称:', formData.value.productName)
      console.log('当前editSkuList:', editSkuList.value)
      console.log('当前editSkuList库存详情:', editSkuList.value.map(s => ({ 
        spec: s.specCombination, 
        stock: s.stock, 
        stockType: typeof s.stock,
        stockIsNull: s.stock === null,
        stockIsUndefined: s.stock === undefined
      })))

      // 数据一致性校验
      if (editSkuList.value.length > 0) {
        // 检查是否有重复的规格组合
        const specCombinations = editSkuList.value.map(s => s.specCombination)
        const uniqueSpecs = new Set(specCombinations)
        if (specCombinations.length !== uniqueSpecs.size) {
          ElMessage.error('SKU列表中存在重复的规格组合，请检查后重新生成SKU')
          return
        }

        // 检查所有SKU是否都有完整的数据
        const invalidSkus = editSkuList.value.filter(sku =>
          !sku.skuCode || !sku.specCombination || sku.price === undefined || sku.stock === undefined
        )
        if (invalidSkus.length > 0) {
          ElMessage.error('存在数据不完整的SKU，请检查后重试')
          console.error('不完整的SKU:', invalidSkus)
          return
        }
        console.log('✓ SKU数据校验通过')
      }

      // 先保存SKU数据
      if (editSkuList.value.length > 0) {
        console.log('检测到SKU数据,开始保存SKU')

        // 重新从数据库查询该商品的所有SKU，确保删除的是最新数据
        console.log('重新查询商品ID:', formData.value.id, '的所有SKU')
        const latestSkus = await getSkusByProductId(formData.value.id!)

        if (latestSkus && latestSkus.length > 0) {
          console.log('查询到最新SKU:', latestSkus.length, '个')
          console.log('要删除的SKU列表:', latestSkus.map(s => ({id: s.id, productId: s.productId, spec: s.specCombination})))
          for (const sku of latestSkus) {
            console.log('正在删除SKU - ID:', sku.id, 'ProductID:', sku.productId, '规格:', sku.specCombination)
            await deleteSku(sku.id)
          }
          console.log('所有旧SKU删除完成')
        } else {
          console.log('该商品没有旧SKU，直接创建新SKU')
        }

        // 批量创建新SKU
        // 确保stock值正确：如果stock是null、undefined或NaN，则使用0
        const skuDTOs: ProductSkuDTO[] = editSkuList.value.map(sku => {
          const stock = (sku.stock !== null && sku.stock !== undefined && !isNaN(Number(sku.stock)))
            ? Number(sku.stock)
            : 0;
          return {
            productId: formData.value.id!,
            skuCode: sku.skuCode,
            specCombination: sku.specCombination,
            price: sku.price,
            suggestedRetailPrice: sku.suggestedRetailPrice || 0,
            marketRetailPrice: sku.marketRetailPrice || 0,
            memberPrice: sku.memberPrice || 0,
            enableMemberPrice: sku.enableMemberPrice || 0,
            stock: stock,
            warningStock: sku.warningStock || 0,
            weight: sku.weight || 0,
            status: sku.status || 1
          };
        })

        console.log('!!! CRITICAL: 即将创建的SKU数据，商品ID为:', formData.value.id)
        console.log('准备批量创建SKU:', skuDTOs)
        console.log('SKU库存详情:', skuDTOs.map(s => ({ spec: s.specCombination, stock: s.stock })))
        await batchCreateSkus(skuDTOs)
        console.log('SKU保存成功')

        // 更新商品的启用规格状态
        formData.value.enableSpec = true
      } else {
        console.log('没有SKU数据,设置enableSpec为false')

        // 重新从数据库查询该商品的所有SKU，确保删除的是最新数据
        console.log('重新查询商品ID:', formData.value.id, '的所有SKU并删除')
        const latestSkus = await getSkusByProductId(formData.value.id!)

        if (latestSkus && latestSkus.length > 0) {
          console.log('删除所有旧SKU:', latestSkus.length, '个')
          for (const sku of latestSkus) {
            console.log('正在删除SKU - ID:', sku.id, '规格:', sku.specCombination)
            await deleteSku(sku.id)
          }
          console.log('所有SKU删除完成')
        } else {
          console.log('该商品没有SKU，无需删除')
        }

        formData.value.enableSpec = false
      }

      // 提取详情图片URL列表
      const detailImages = detailImageList.value
        .map(file => file.url || (file.response as any)?.data?.url)
        .filter(url => url)

      // 更新formData的images字段
      formData.value.images = JSON.stringify(detailImages)

      console.log('保存商品基本信息, enableSpec:', formData.value.enableSpec)

      // 转换 enableSpec 为 0 或 1
      const submitData = {
        ...formData.value,
        enableSpec: formData.value.enableSpec ? 1 : 0
      }

      await updateProduct(submitData)
      console.log('=== 商品保存完成 ===')

      ElMessage.success('更新成功')
      dialogVisible.value = false
      loadProductList()
    } catch (error) {
      console.error('保存失败:', error)
      ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message))
    }
  })
}

// 切换状态
const handleToggleStatus = async (row: ProductVO) => {
  const newStatus = row.status === '上架' ? '下架' : '上架'
  try {
    await updateProductStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    loadProductList()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 删除商品
const handleDelete = async (row: ProductVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    loadProductList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 同步商品到ERP
const handleSyncToErp = async (row: any) => {
  try {
    // 设置同步状态为加载中
    row.syncing = true

    await syncProductToErp(row.id)
    ElMessage.success('商品同步成功')
  } catch (error: any) {
    console.error('同步失败:', error)
    ElMessage.error(error.message || '商品同步失败，请查看日志')
  } finally {
    // 恢复同步状态
    row.syncing = false
  }
}

// SKU管理相关方法
const handleEnableSpecChange = async (value: boolean) => {
  if (!value) {
    // 禁用规格时清空数据
    currentSkuList.value = []
    skuSpecKeys.value = [{ specName: '', values: [{ specValue: '' }] }]
    skuManageList.value = []
    // 清空编辑弹框的规格数据
    editSpecKeys.value = [{ specName: '', values: [{ specValue: '' }] }]
    editSkuList.value = []

    // 如果是编辑商品（有ID），则从数据库删除规格数据
    if (formData.value.id) {
      try {
        console.log('关闭规格开关，删除商品ID:', formData.value.id, '的规格数据')
        const deletedCount = await deleteSpecsByProductId(formData.value.id)
        console.log('已删除规格数量:', deletedCount)
      } catch (error) {
        console.error('删除规格数据失败:', error)
        // 删除失败不影响继续操作，只记录日志
      }
    }
  } else {
    // 启用规格时初始化默认数据
    if (editSpecKeys.value.length === 0 || (editSpecKeys.value.length === 1 && !editSpecKeys.value[0].specName)) {
      editSpecKeys.value = [{ specName: '', values: [{ specValue: '' }] }]
    }
  }
}

// 编辑弹框规格管理方法
const addEditSpecKey = () => {
  editSpecKeys.value.push({
    specName: '',
    values: [{ specValue: '' }]
  })
}

const removeEditSpecKey = (index: number) => {
  if (editSpecKeys.value.length > 1) {
    editSpecKeys.value.splice(index, 1)
    // 重新生成SKU
    if (editSkuList.value.length > 0) {
      generateEditSkuList()
    }
  }
}

const addEditSpecValue = (keyIndex: number) => {
  editSpecKeys.value[keyIndex].values.push({ specValue: '' })
}

const removeEditSpecValue = (keyIndex: number, valueIndex: number) => {
  const specKey = editSpecKeys.value[keyIndex]
  if (specKey.values.length > 1) {
    specKey.values.splice(valueIndex, 1)
    // 重新生成SKU
    if (editSkuList.value.length > 0) {
      generateEditSkuList()
    }
  }
}

const generateEditSkuList = () => {
  if (!canGenerateEditSkus.value) {
    ElMessage.warning('请先完善规格属性配置')
    return
  }

  console.log('=== 生成SKU开始 ===')
  console.log('生成前 currentSkuList:', currentSkuList.value.map(s => s.specCombination))
  console.log('生成前 editSkuList:', editSkuList.value.map(s => s.specCombination))

  // 生成笛卡尔积
  const combinations = generateCartesianProduct(editSpecKeys.value)

  // 调试：检查formData中的价格值
  console.log('生成SKU时formData的值:', {
    basePrice: formData.value.basePrice,
    suggestedRetailPrice: formData.value.suggestedRetailPrice,
    marketRetailPrice: formData.value.marketRetailPrice
  })

  editSkuList.value = combinations.map((combination, index) => {
    const specCombination: Record<string, string> = {}
    const specCombinationTextArray: string[] = []

    combination.forEach((value, keyIndex) => {
      const specName = editSpecKeys.value[keyIndex].specName
      specCombination[specName] = value
      specCombinationTextArray.push(`${specName}:${value}`)
    })

    return {
      specCombination: JSON.stringify(specCombination),
      specCombinationText: specCombinationTextArray.join(', '),
      skuCode: `${formData.value.productCode || 'SKU'}-${index + 1}`,
      price: formData.value.basePrice,
      suggestedRetailPrice: formData.value.suggestedRetailPrice || 0,
      marketRetailPrice: formData.value.marketRetailPrice || 0,
      memberPrice: formData.value.memberPrice,
      enableMemberPrice: formData.value.enableMemberPrice,
      stock: formData.value.stock,
      warningStock: formData.value.warningStock,
      weight: formData.value.weight,
      status: 1
    }
  })

  console.log('生成后 editSkuList:', editSkuList.value.map(s => s.specCombination))
  console.log('注意: currentSkuList仍然是旧数据，保存时会删除所有旧SKU并创建新SKU')
  console.log('=== 生成SKU完成 ===')
  
  ElMessage.success(`已生成 ${editSkuList.value.length} 个SKU`)
}

// 生成笛卡尔积
const generateCartesianProduct = (specKeys: any[]): string[][] => {
  const values = specKeys.map(key => key.values.map((v: any) => v.specValue))
  
  function cartesian(arr: string[][]): string[][] {
    return arr.reduce((a, b) => {
      return a.flatMap((x: string[]) => b.map(y => [...x, y]))
    }, [[]] as string[][])
  }
  
  return cartesian(values)
}

const removeEditSku = (index: number) => {
  editSkuList.value.splice(index, 1)
}

const openSkuManager = () => {
  if (!formData.value.id) {
    ElMessage.warning('请先保存商品基本信息后再管理SKU')
    return
  }
  skuDialogVisible.value = true
  loadCurrentSkuData()
}

const loadCurrentSkuData = async () => {
  try {
    // 加载当前商品的SKU数据
    currentSkuList.value = await getSkusByProductId(formData.value.id!)
    
    // 加载规格属性数据
    const specKeys = await getSpecKeysByProductId(formData.value.id!)
    if (specKeys.length > 0) {
      skuSpecKeys.value = specKeys.map(key => ({
        id: key.id,
        specName: key.specName,
        values: key.specValues.map(value => ({
          id: value.id,
          specValue: value.specValue
        }))
      }))
    }
    
    // 转换SKU数据为管理格式
    skuManageList.value = currentSkuList.value.map(sku => {
      // 解析规格组合文本
      const specCombination = JSON.parse(sku.specCombination)
      const specTexts = Object.entries(specCombination).map(([key, value]) => `${key}:${value}`)
      
      return {
        ...sku,
        specCombinationText: specTexts.join(', '),
        stock: sku.stock ?? 0,
        warningStock: sku.warningStock ?? 0,
        weight: sku.weight ?? 0,
        status: sku.status || 1
      }
    })
  } catch (error) {
    console.error('加载SKU数据失败:', error)
    ElMessage.error('加载SKU数据失败')
  }
}

const addSpecKey = () => {
  skuSpecKeys.value.push({
    specName: '',
    values: [{ specValue: '' }]
  })
}

const removeSpecKey = (index: number) => {
  if (skuSpecKeys.value.length > 1) {
    skuSpecKeys.value.splice(index, 1)
  }
}

const addSpecValue = (keyIndex: number) => {
  skuSpecKeys.value[keyIndex].values.push({ specValue: '' })
}

const removeSpecValue = (keyIndex: number, valueIndex: number) => {
  const specKey = skuSpecKeys.value[keyIndex]
  if (specKey.values.length > 1) {
    specKey.values.splice(valueIndex, 1)
  }
}

const generateSkuList = () => {
  if (!canGenerateSkus.value) {
    ElMessage.warning('请先完善规格属性配置')
    return
  }

  // 生成笛卡尔积
  const combinations = generateCartesianProduct(skuSpecKeys.value)
  
  skuManageList.value = combinations.map((combination, index) => {
    const specCombination: Record<string, string> = {}
    const specCombinationTextArray: string[] = []
    
    combination.forEach((value, keyIndex) => {
      const specName = skuSpecKeys.value[keyIndex].specName
      specCombination[specName] = value
      specCombinationTextArray.push(`${specName}:${value}`)
    })
    
    return {
      specCombination: JSON.stringify(specCombination),
      specCombinationText: specCombinationTextArray.join(', '),
      skuCode: `${formData.value.productCode || 'SKU'}-${index + 1}`,
      price: formData.value.basePrice,
      stock: formData.value.stock,
      warningStock: formData.value.warningStock,
      weight: formData.value.weight,
      status: 1
    }
  })
  
  ElMessage.success(`已生成 ${skuManageList.value.length} 个SKU`)
}

// 生成笛卡尔积
const removeSku = (index: number) => {
  skuManageList.value.splice(index, 1)
}

const batchSetPrice = () => {
  ElMessageBox.prompt('请输入价格', '批量设置价格', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValidator: (value) => {
      const price = parseFloat(value)
      if (isNaN(price) || price < 0) {
        return '请输入有效的价格'
      }
      return true
    }
  }).then(({ value }) => {
    const price = parseFloat(value)
    skuManageList.value.forEach(sku => {
      sku.price = price
    })
    ElMessage.success('批量设置价格成功')
  }).catch(() => {})
}

const batchSetStock = () => {
  ElMessageBox.prompt('请输入库存数量', '批量设置库存', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValidator: (value) => {
      const stock = parseInt(value)
      if (isNaN(stock) || stock < 0) {
        return '请输入有效的库存数量'
      }
      return true
    }
  }).then(({ value }) => {
    const stock = parseInt(value)
    skuManageList.value.forEach(sku => {
      sku.stock = stock
    })
    ElMessage.success('批量设置库存成功')
  }).catch(() => {})
}

const handleCsvChange = (file: UploadFile) => {
  importForm.value.csvFile = file.raw || null
}

const handleCsvRemove = () => {
  importForm.value.csvFile = null
}

const handleZipChange = (file: UploadFile) => {
  importForm.value.imageZip = file.raw || null
}

const handleZipRemove = () => {
  importForm.value.imageZip = null
}

const downloadTemplate = (type: 'csv' | 'excel') => {
  if (type === 'csv') {
    const link = document.createElement('a')
    link.href = '/商品批量导入模板.csv'
    link.download = '商品批量导入模板.csv'
    link.click()
  } else {
    // 下载Excel模板
    window.open('/api/admin/product/template/excel', '_blank')
  }
}

const handleImportSubmit = async () => {
  if (!importForm.value.csvFile) {
    ElMessage.warning('请选择数据文件')
    return
  }

  importLoading.value = true
  importResult.value = null

  try {
    const result = await importProducts(importForm.value.csvFile, importForm.value.imageZip)
    console.log('导入结果:', result)
    console.log('错误列表:', result.errors)
    console.log('警告列表:', result.warnings)
    importResult.value = result

    // 显示导入结果弹框
    resultDialogVisible.value = true

    if (result.failCount === 0) {
      ElMessage.success('导入成功！')
      loadProductList()
    } else {
      ElMessage.warning(`导入完成，但有 ${result.failCount} 条失败`)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '导入失败')
  } finally {
    importLoading.value = false
  }
}

const handleImportCancel = () => {
  importDialogVisible.value = false
  importForm.value.csvFile = null
  importForm.value.imageZip = null
  importResult.value = null
}

const saveSkuChanges = async () => {
  try {
    // 删除原有SKU
    for (const sku of currentSkuList.value) {
      await deleteSku(sku.id)
    }
    
    // 批量创建新SKU
    const skuDTOs: ProductSkuDTO[] = skuManageList.value.map(sku => ({
      productId: formData.value.id!,
      skuCode: sku.skuCode,
      specCombination: sku.specCombination,
      price: sku.price,
      stock: sku.stock ?? 0,
      warningStock: sku.warningStock || 0,
      weight: sku.weight || 0,
      status: sku.status
    }))
    
    console.log('准备保存SKU:', skuDTOs)
    const result = await batchCreateSkus(skuDTOs)
    console.log('SKU保存结果:', result)
    
    // 同步更新商品的启用规格状态
    const hasSkus = skuDTOs.length > 0
    await updateProduct({
      ...formData.value,
      enableSpec: hasSkus
    })
    console.log('更新商品启用规格状态为:', hasSkus)
    
    // 更新本地数据
    formData.value.enableSpec = hasSkus
    
    // 重新加载SKU数据
    await loadCurrentSkuData()
    
    // 重新加载商品列表
    await loadProductList()
    
    ElMessage.success('SKU配置保存成功')
    skuDialogVisible.value = false
  } catch (error) {
    console.error('保存SKU配置失败:', error)
    ElMessage.error('保存SKU配置失败: ' + (error.response?.data?.message || error.message))
  }
}

const handleSkuDialogClose = () => {
  skuDialogVisible.value = false
}

const resultDialogVisible = ref(false)
const csvFile = ref<File | null>(null)
const zipFile = ref<File | null>(null)
const csvFileList = ref<any[]>([])
const zipFileList = ref<any[]>([])

const handleImportDialogClose = () => {
  csvFile.value = null
  zipFile.value = null
  csvFileList.value = []
  zipFileList.value = []
  importDialogVisible.value = false
}

const handleResultClose = () => {
  resultDialogVisible.value = false
  csvFile.value = null
  zipFile.value = null
  csvFileList.value = []
  zipFileList.value = []
}

// 初始化
onMounted(() => {
  loadCategoryTree()
  loadBrands()
  loadShippingTemplates()
  loadProductList()
})
</script>

<style scoped lang="scss">
.product-manage {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-section {
    margin-bottom: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    overflow: hidden;
    
    .filters-row {
      display: flex;
      align-items: center;
      gap: 20px;
      padding: 20px 24px;
      border-bottom: 1px solid #f0f0f0;
      flex-wrap: wrap;
      
      .filter-item {
        display: flex;
        flex-direction: column;
        gap: 8px;
        min-width: 160px;
        
        label {
          font-size: 14px;
          color: #606266;
          font-weight: 500;
        }
        
        .el-select,
        .el-input {
          width: 100%;
        }
      }
      
      .action-buttons {
        margin-left: auto;
        display: flex;
        gap: 12px;
        
        .el-button {
          border-radius: 6px;
        }
      }
    }
    
    .toolbar-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 24px;
      background: #fafafa;
      
      .sort-section {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .sort-label {
          font-size: 14px;
          color: #606266;
          font-weight: 500;
        }
        
        .sort-buttons {
          display: flex;
          gap: 8px;
          
          .sort-btn {
            height: 32px;
            padding: 0 12px;
            border-radius: 6px;
            font-size: 13px;
            border: 1px solid #d9d9d9;
            background: #fff;
            color: #606266;
            transition: all 0.2s;
            
            &:hover {
              border-color: #409eff;
              color: #409eff;
              background: #ecf5ff;
            }
            
            &.is-active {
              border-color: #409eff;
              background: #409eff;
              color: #fff;
              
              &:hover {
                background: #337ecc;
                border-color: #337ecc;
              }
            }
            
            .sort-icon {
              margin-left: 4px;
              font-size: 12px;
            }
          }
        }
      }
      
      .page-actions {
        .el-button {
          border-radius: 6px;
          height: 36px;
          padding: 0 16px;
        }
      }
    }
  }
}

.price-stock-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0 20px;
  margin-bottom: 20px;

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

.member-price-row {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

// 图片上传相关样式
.upload-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .image-preview {
    position: relative;
    display: inline-block;

    .delete-btn {
      position: absolute;
      top: 5px;
      right: 5px;
    }
  }

  .upload-placeholder {
    width: 150px;
    height: 150px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }

    .upload-icon {
      font-size: 28px;
      color: #8c939d;
      margin-bottom: 8px;
    }

    .upload-text {
      font-size: 14px;
      color: #606266;
    }
  }

  .url-input {
    width: 100%;
    max-width: 500px;
  }
}

.detail-images-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .upload-tip {
    font-size: 12px;
    color: #909399;
  }
}

// SKU管理样式
.form-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #666;
}

.spec-config-area {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
  background-color: #fafbfc;
}

.spec-section {
  margin-bottom: 20px;
}

.spec-section-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 15px;

  .required-star {
    color: #f56c6c;
    margin-right: 4px;
  }
}

.sku-list-wrapper {
  overflow-x: auto;
}

.sku-overview-table {
  margin-top: 15px;
}

.no-sku-tip {
  padding: 20px;
  text-align: center;
  color: #999;
  background-color: #f9f9f9;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
}

.sku-manager {
  .spec-card, .sku-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .spec-keys-list {
    .spec-key-item {
      margin-bottom: 20px;
      padding: 15px;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      background-color: #fff;

      .spec-key-header {
        display: flex;
        align-items: center;
        gap: 15px;
        margin-bottom: 15px;
      }

      .spec-values-wrapper {
        .spec-values-header {
          margin-bottom: 10px;
          font-weight: bold;
          color: #333;
        }

        .spec-values-list {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          align-items: center;

          .spec-value-item {
            display: flex;
            align-items: center;
            gap: 8px;
          }
        }
      }
    }
  }

  .spec-actions {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid #e4e7ed;

    .tip {
      color: #666;
      font-size: 12px;
    }
  }

  .sku-manage-table {
    margin-top: 15px;
  }
}

// SKU管理对话框样式
:deep(.sku-management-dialog) {
  .el-dialog {
    max-height: 85vh !important;
    height: 85vh !important;
    margin: 3vh auto !important;
    overflow: hidden !important;
  }
  
  .el-dialog__header {
    padding: 20px 20px 10px 20px;
    flex-shrink: 0;
  }
  
  .el-dialog__body {
    padding: 10px 20px !important;
    flex: 1 !important;
    overflow-y: auto !important;
    height: calc(85vh - 120px) !important;
  }
  
  .el-dialog__footer {
    padding: 10px 20px 20px 20px;
    flex-shrink: 0;
  }
}

.sku-manager {
  height: 100%;
}

.sku-card {
  margin-bottom: 20px;
  
  .el-card__body {
    padding: 15px !important;
  }
}

.sku-table-container {
  margin-top: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.import-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
  margin-top: 10px;
}

.import-warnings,
.import-errors {
  margin-top: 16px;
  text-align: left;
}

// SKU表格样式优化
.sku-manage-table {
  width: 100%;
  
  :deep(.el-table__header-wrapper) {
    th {
      background-color: #fafafa;
      font-weight: 600;
    }
  }
  
  :deep(.el-table__body-wrapper) {
    &::-webkit-scrollbar {
      width: 8px;
    }
    
    &::-webkit-scrollbar-thumb {
      background-color: #c1c1c1;
      border-radius: 4px;
      
      &:hover {
        background-color: #a8a8a8;
      }
    }
    
    &::-webkit-scrollbar-track {
      background-color: #f1f1f1;
      border-radius: 4px;
    }
  }
}

// 商品编辑对话框样式 - 固定底部按钮
:deep(.product-edit-dialog) {
  .el-dialog__footer {
    position: sticky;
    bottom: 0;
    background: #fff;
    border-top: 1px solid #e4e7ed;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
    z-index: 10;
    padding: 15px 20px;
  }

  .el-dialog__body {
    max-height: 60vh;
    overflow-y: auto;
  }
}
</style>
