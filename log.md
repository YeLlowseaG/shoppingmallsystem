# 修改日志

## 2026-01-10 - 生成新模板批量导入测试数据

### 需求说明
根据修改后的批量导入模板（包含会员价列和运费模板ID字段），生成多份组合场景的测试数据，用于验证导入功能。

### 生成内容

生成了6份测试数据文件（CSV格式），位于 `docs/test/` 目录：

1. **商品导入测试数据-09-新模板基础商品.csv**
   - 基础商品测试（无规格、无会员价）
   - 包含5个商品，涵盖不同分类、品牌、运费模板ID场景
   - 验证基础字段导入功能

2. **商品导入测试数据-10-新模板商品会员价.csv**
   - 启用商品会员价的商品测试
   - 包含5个商品，涵盖不同会员价配置场景：
     - 所有会员等级都有价格
     - 部分会员等级有价格
     - 仅普通会员有价格
     - 无运费模板ID的商品
     - 无品牌的商品

3. **商品导入测试数据-11-新模板规格商品.csv**
   - 启用规格的商品测试（无会员价）
   - 包含3个商品，涵盖：
     - 单规格商品（颜色）
     - 多规格组合商品（颜色+尺寸）
     - 三规格组合商品（颜色+尺寸+其他）

4. **商品导入测试数据-12-新模板规格+SKU会员价.csv**
   - 启用规格+SKU会员价的商品测试
   - 包含3个商品，涵盖：
     - 所有SKU都启用会员价
     - 部分SKU启用会员价
     - SKU会员价部分等级有价格

5. **商品导入测试数据-13-新模板综合场景.csv**
   - 综合场景测试，涵盖各种组合：
     - 基础商品+运费模板
     - 规格+SKU会员价
     - 草稿+规格
     - 无运费模板+商品会员价
     - 无品牌+运费模板
     - 无预警库存+规格+SKU会员价
     - 商品会员价+规格+SKU会员价
     - 商品会员价（部分等级）
     - 规格+部分SKU会员价

6. **商品导入测试数据-14-新模板边界值测试.csv**
   - 边界值测试，验证系统对极端值的处理：
     - 最小价格和库存（0.01, 0）
     - 最大价格和库存（99999.99, 99999）
     - 空运费模板ID
     - 空品牌名称
     - 空预警库存
     - 会员价边界值（最小和最大）
     - 长描述文本测试

### 测试数据特点

- **完整的列结构**：包含所有新模板字段：
  - 基础列（15列）：商品编码、条码、计量单位、商品名称、分类名称、品牌名称、运费模板ID、基础价、建议零售价、市场零售价、预警库存、重量(g)、商品描述、状态、启用会员价
  - 商品会员价列（动态）：商品会员价-普通会员、商品会员价-银卡会员、商品会员价-金卡会员、商品会员价-钻石会员
  - SKU相关列（6列）：启用规格、SKU编码、规格组合、SKU价格、SKU库存、启用SKU会员价
  - SKU会员价列（动态）：SKU会员价-普通会员、SKU会员价-银卡会员、SKU会员价-金卡会员、SKU会员价-钻石会员

- **会员等级假设**：测试数据中假设系统有4个会员等级：
  - 普通会员
  - 银卡会员
  - 金卡会员
  - 钻石会员
  - **注意**：实际使用时，需要根据系统中实际的会员等级名称调整列名

- **场景覆盖**：
  - ✅ 基础商品导入
  - ✅ 商品会员价导入
  - ✅ 规格商品导入
  - ✅ SKU会员价导入
  - ✅ 综合场景导入
  - ✅ 边界值测试
  - ✅ 可选字段测试（运费模板ID、品牌名称、预警库存）

### 使用说明

1. **会员等级名称调整**：
   - 如果系统中的会员等级名称与测试数据中的不同，需要修改CSV文件的列名
   - 例如：如果系统只有"普通会员"和"VIP会员"，需要删除"商品会员价-银卡会员"等列，并添加"商品会员价-VIP会员"列

2. **导入测试步骤**：
   - 先使用"商品导入测试数据-09-新模板基础商品.csv"测试基础功能
   - 然后逐步测试其他场景
   - 最后使用"商品导入测试数据-13-新模板综合场景.csv"进行综合测试
   - 使用"商品导入测试数据-14-新模板边界值测试.csv"验证边界值处理

3. **验证要点**：
   - 验证会员价是否正确保存到数据库
   - 验证运费模板ID是否正确关联
   - 验证规格和SKU是否正确创建
   - 验证会员等级名称验证是否生效
   - 验证边界值是否正确处理

### 相关文件
- `docs/test/商品导入测试数据-09-新模板基础商品.csv` (新建)
- `docs/test/商品导入测试数据-10-新模板商品会员价.csv` (新建)
- `docs/test/商品导入测试数据-11-新模板规格商品.csv` (新建)
- `docs/test/商品导入测试数据-12-新模板规格+SKU会员价.csv` (新建)
- `docs/test/商品导入测试数据-13-新模板综合场景.csv` (新建)
- `docs/test/商品导入测试数据-14-新模板边界值测试.csv` (新建)

---

## 2026-01-10 - 批量导入功能优化：移除CSV模板支持并添加运费模板ID字段

### 需求说明
1. 屏蔽CSV模板的入口，只保留Excel模板下载
2. 在导入模板中添加"运费模板ID"字段，解决导入时提示"运费模板ID不存在"的问题

### 修改内容

1. **前端移除CSV模板入口** (`admin-frontend/src/views/product/ProductManage.vue`)
   - 移除"下载CSV模板"按钮
   - 移除文件上传的CSV格式支持（`.csv`）
   - 更新导入说明，只保留Excel格式
   - 简化 `downloadTemplate` 函数，移除CSV相关逻辑
   - 移除CSV文件行数检查逻辑

2. **模板添加运费模板ID字段** (`backend/src/main/java/com/shoppingmall/controller/admin/ProductController.java`)
   - 在"品牌名称"之后添加"运费模板ID"字段
   - 更新列索引计算，基础列数从14改为15
   - 更新示例数据，添加运费模板ID列（留空，表示可选）
   - 在 `getFieldComment()` 方法中添加运费模板ID的批注说明

3. **修复解析代码列索引** (`backend/src/main/java/com/shoppingmall/service/product/impl/ProductImportServiceImpl.java`)
   - 更新 `baseColumnCount` 从14改为15（包含运费模板ID）
   - 更新兼容旧模板的列索引计算

### 功能说明
- **只支持Excel格式**：批量导入现在只支持Excel格式（.xlsx/.xls），不再支持CSV格式
- **运费模板ID字段**：模板中包含"运费模板ID"字段，位于"品牌名称"之后
- **字段说明**：运费模板ID字段有批注说明，说明其为可选字段，如果填写则必须存在且已启用

### 相关文件
- `admin-frontend/src/views/product/ProductManage.vue`
- `backend/src/main/java/com/shoppingmall/controller/admin/ProductController.java`
- `backend/src/main/java/com/shoppingmall/service/product/impl/ProductImportServiceImpl.java`

---

## 2026-01-10 - 批量导入模板添加填写说明注释

### 需求说明
在批量导入Excel模板中添加填写说明注释，方便用户了解如何填写各个字段的规则。通过在表头单元格添加批注，鼠标悬停时显示详细说明。

### 修改内容

1. **添加批注功能** (`backend/src/main/java/com/shoppingmall/controller/admin/ProductController.java`)
   - 为每个表头单元格添加批注说明
   - 鼠标悬停在表头单元格上时显示该字段的详细填写规则
   - 使用 `XSSFClientAnchor` 和 `Comment` 实现批注功能
   - 批注包含：必填项、格式要求、示例、注意事项等

2. **添加字段说明方法** (`backend/src/main/java/com/shoppingmall/controller/admin/ProductController.java`)
   - 添加 `getFieldComment()` 方法，为每个字段返回详细的填写说明
   - 支持基础字段、会员价字段、SKU字段的说明
   - 会员价字段说明会根据会员等级名称动态生成

### 功能说明
- **批注说明**：鼠标悬停在表头单元格上，会显示该字段的详细填写规则
- **字段说明覆盖**：
  - 基础字段：商品编码、商品名称、分类名称、价格等
  - 会员价字段：根据会员等级动态生成说明（如"商品会员价-银卡会员"）
  - SKU字段：规格组合格式、SKU编码等
- **说明内容**：包含必填项标识、格式要求、示例、注意事项等

### 相关文件
- `backend/src/main/java/com/shoppingmall/controller/admin/ProductController.java`

---

## 2026-01-10 - 批量导入商品功能支持按会员等级设置会员价

### 需求说明
批量导入商品功能需要支持不同会员等级的会员价格，与界面功能保持一致。导入时需要判断会员等级名称是否存在。

### 修改内容

1. **修改 ProductImportDTO** (`backend/src/main/java/com/shoppingmall/dto/ProductImportDTO.java`)
   - 添加 `enableMemberPrice` 字段：是否启用商品会员价
   - 添加 `productMemberPrices` 字段：商品会员价Map（key: 会员等级名称, value: 会员价）
   - 添加 `enableSkuMemberPrice` 字段：是否启用SKU会员价
   - 添加 `skuMemberPrices` 字段：SKU会员价Map（key: 会员等级名称, value: 会员价）
   - 保留 `skuMemberPrice` 字段（标记为废弃），用于兼容旧模板

2. **修改 ProductImportServiceImpl** (`backend/src/main/java/com/shoppingmall/service/product/impl/ProductImportServiceImpl.java`)
   - 添加 `MemberLevelService` 依赖
   - 添加 `memberLevelNameToIdMap` 缓存：会员等级名称到ID的映射
   - 添加 `initMemberLevelMap()` 方法：初始化会员等级映射
   - 添加 `findMemberLevelIdByName()` 方法：根据会员等级名称查找ID
   - 添加 `validateMemberLevelNames()` 方法：验证会员等级名称是否存在
   - 修改 `importProducts()` 方法：在导入前验证会员等级名称
   - 修改 `parseExcel()` 方法：解析会员价列（动态列，根据会员等级数量）
   - 修改 `parseCSV()` 方法：解析会员价列（动态列，根据会员等级数量）
   - 修改 `importSingleProduct()` 方法：保存商品和SKU的会员价配置

3. **修改 ProductController** (`backend/src/main/java/com/shoppingmall/controller/admin/ProductController.java`)
   - 添加 `MemberLevelService` 依赖
   - 修改 `downloadExcelTemplate()` 方法：
     - 动态获取所有启用的会员等级
     - 为每个会员等级添加"商品会员价-{等级名称}"列
     - 为每个会员等级添加"SKU会员价-{等级名称}"列
     - 更新示例数据，展示会员价填写方式

### 功能说明
- **会员价列格式**：使用会员等级名称作为列名，例如"商品会员价-普通会员"、"SKU会员价-VIP会员"
- **会员等级名称验证**：导入时会验证会员等级名称是否存在，如果不存在会抛出明确的错误提示
- **兼容性**：保留对旧模板的支持（单个SKU会员价列），如果新格式没有数据，会尝试使用旧格式
- **动态适配**：模板列数会根据系统中启用的会员等级数量动态调整

### 技术要点
- 使用会员等级名称而不是ID，提升用户体验
- 通过名称到ID的映射缓存，提高解析效率
- 在导入前统一验证会员等级名称，避免部分数据导入成功部分失败的情况
- 支持CSV和Excel两种格式

### 相关文件
- `backend/src/main/java/com/shoppingmall/dto/ProductImportDTO.java`
- `backend/src/main/java/com/shoppingmall/service/product/impl/ProductImportServiceImpl.java`
- `backend/src/main/java/com/shoppingmall/controller/admin/ProductController.java`

---

## 2026-01-10 - 修复用户端商品详情页库存为0时不显示商品信息的问题

### 问题描述
用户端商品详情页面（`http://localhost:3002/products/311`），当商品的库存为0时，目前只显示了商品的图片，没有显示商品的基本字段信息（如商品名称、价格、规格等），这是不合理的。应该也要显示字段信息，只是需要提示库存不足，支持缺货登记。

### 修改内容

1. **添加库存不足提示** (`frontend/src/views/products/Detail.vue`)
   - 在购买数量输入框之前添加库存不足提示框（`el-alert`）
   - 当库存为0时，显示醒目的警告提示："商品暂时缺货"
   - 提示内容说明商品暂时无法购买，可以进行缺货登记

2. **优化购买数量输入框** (`frontend/src/views/products/Detail.vue`)
   - 当库存为0时，禁用购买数量输入框（`:disabled="isOutOfStock()"`）
   - 修改 `:max` 属性，当库存为0时使用 `1` 作为最大值，避免输入框无法使用

3. **添加样式支持** (`frontend/src/views/products/Detail.vue`)
   - 添加 `.out-of-stock-alert` 样式类
   - 美化库存不足提示框的显示效果

### 功能说明
- **库存为0时**：
  - ✅ 商品信息正常显示（商品名称、价格、规格、描述等）
  - ✅ 显示醒目的库存不足提示
  - ✅ 购买数量输入框被禁用
  - ✅ 显示缺货登记按钮，用户可以登记缺货通知
  - ✅ 已登记的用户显示"已登记缺货通知"按钮

### 相关文件
- `frontend/src/views/products/Detail.vue`

---

## 2026-01-10 - 库存列表优化

### 需求说明
优化库存列表页面（`http://localhost:3003/admin/inventory`）：
1. 列表默认显示10条一页（之前是20条）
2. 查询字段增加商品状态，支持按照商品状态查询，默认值是已上架
3. 列表表格中增加商品状态字段显示
4. 调整列宽度，金额字段（库存价格、库存价值）宽度调小

### 修改内容

1. **分页设置** (`admin-frontend/src/views/inventory/Index.vue`)
   - 将分页默认值从 `size: 20` 改为 `size: 10`
   - 将分页选项从 `[20, 50, 100, 200]` 改为 `[10, 20, 50, 100]`

2. **查询表单** (`admin-frontend/src/views/inventory/Index.vue`)
   - 在 `searchForm` 中添加 `productStatus` 字段，默认值为 `'上架'`
   - 在查询区域添加商品状态下拉选择框，选项包括：全部、已上架、已下架、草稿
   - 在 `loadInventoryList` 函数中，将 `productStatus` 作为 `status` 参数传递给API

3. **列表表格** (`admin-frontend/src/views/inventory/Index.vue`)
   - 在表格中增加"商品状态"列，显示位置在"分类"列之后
   - 商品状态使用标签显示，已上架显示为绿色（success），已下架显示为灰色（info），草稿显示为默认样式
   - 在数据处理时，为每个库存项添加 `productStatus` 字段，从商品数据中获取 `status` 字段

4. **列宽度调整** (`admin-frontend/src/views/inventory/Index.vue`)
   - 库存价格列宽度从 `120` 调整为 `100`
   - 库存价值列宽度从 `120` 调整为 `100`

5. **重置功能** (`admin-frontend/src/views/inventory/Index.vue`)
   - 重置搜索时，`productStatus` 保持默认值 `'上架'`

### 功能说明
- 列表默认每页显示10条记录，用户可以通过分页器选择其他每页显示数量
- 支持按商品状态筛选，默认只显示已上架的商品
- 列表中可以直观看到每个商品的上下架状态
- 金额字段宽度优化，表格布局更紧凑

### 相关文件
- `admin-frontend/src/views/inventory/Index.vue`

---

## 2026-01-10 - 修改价格显示文案逻辑：根据商品/SKU是否启用会员价判断

### 需求说明
修改商品详情页、购物车页、结算页的价格显示文案逻辑：
- **之前的逻辑**：根据用户是否是会员来判断显示"会员价"还是"商品价格"
- **新的逻辑**：根据商品或SKU是否启用会员价来判断
  - 如果商品或SKU启用了会员价（enableMemberPrice === 1）→ 显示"会员价"
  - 如果商品或SKU没有启用会员价（enableMemberPrice !== 1）→ 显示"商品价格"
- **特别注意**：如果商品启用了SKU，需要根据SKU维度是否启用会员价来判断，而不是商品维度

### 后端修改

1. **backend/src/main/java/com/shoppingmall/vo/CartVO.java**
   - 添加 `enableMemberPrice` 字段（Integer类型）
   - 注释说明：如果商品有SKU，则使用SKU的enableMemberPrice；否则使用商品的enableMemberPrice

2. **backend/src/main/java/com/shoppingmall/service/buyer/impl/CartServiceImpl.java**
   - 在 `convertToVO` 方法中，设置 `enableMemberPrice` 字段
   - 逻辑：优先使用SKU的 `enableMemberPrice`，如果没有SKU则使用商品的 `enableMemberPrice`
   - 位置：在设置 `memberPrice` 之后，处理规格信息之前

### 前端修改

1. **frontend/src/api/buyer/cart.ts**
   - 在 `CartVO` 接口中添加 `enableMemberPrice?: number` 字段
   - 注释说明：如果商品有SKU则使用SKU的，否则使用商品的

2. **frontend/src/views/products/Detail.vue**
   - 修改 `getPriceLabel()` 函数：
     - 如果商品启用了SKU且有当前选中的SKU，优先检查SKU的 `enableMemberPrice`
     - 如果没有SKU或SKU未启用会员价，检查商品的 `enableMemberPrice`
     - 如果都没有启用会员价，显示"商品价格"
   - 在 `product.value` 对象中添加 `enableMemberPrice` 字段映射

3. **frontend/src/views/cart/Index.vue**
   - 修改 `getPriceColumnTitle()` 函数：
     - 从判断 `isMember` 改为判断购物车中是否有启用会员价的商品
     - 如果至少有一个商品的 `enableMemberPrice === 1`，显示"会员价"
     - 否则显示"商品价格"

4. **frontend/src/views/cart/Checkout.vue**
   - 修改 `getPriceColumnTitle()` 函数：
     - 从判断 `isMember` 改为判断订单商品中是否有启用会员价的商品
     - 如果至少有一个商品的 `enableMemberPrice === 1`，显示"会员价"
     - 否则显示"商品价格"

### 功能说明
- **判断逻辑（优先级从高到低）**：
  1. **优先判断用户是否是会员**：如果不是会员或未登录，统一显示"商品价格"（因为普通用户不能享有会员价）
  2. **如果是会员，再判断商品或SKU是否启用会员价**：
     - 如果商品启用了SKU且有选中的SKU，优先检查SKU的 `enableMemberPrice`
     - 如果没有SKU或SKU未启用会员价，检查商品的 `enableMemberPrice`
     - 如果启用了会员价，显示"会员价"；否则显示"商品价格"
- **商品详情页**：根据用户会员状态和当前选中的SKU或商品本身的 `enableMemberPrice` 来判断
- **购物车页**：如果用户是会员且购物车中有任何商品启用了会员价，价格列标题显示"会员价"，否则显示"商品价格"
- **结算页**：如果用户是会员且订单中有任何商品启用了会员价，价格列标题显示"会员价"，否则显示"商品价格"
- 后端在返回购物车数据时，会根据是否有SKU来设置 `enableMemberPrice` 字段（优先使用SKU的）

### 相关文件
- `backend/src/main/java/com/shoppingmall/vo/CartVO.java`
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/CartServiceImpl.java`
- `frontend/src/api/buyer/cart.ts`
- `frontend/src/views/products/Detail.vue`
- `frontend/src/views/cart/Index.vue`
- `frontend/src/views/cart/Checkout.vue`

---

## 2026-01-10 - 修复编辑商品页面会员价不显示问题

### 问题描述
编辑商品页面（`http://localhost:3003/admin/product/list`）时，界面的会员价没有显示，但数据库中有保存会员价数据。

### 问题原因
1. **前端未加载会员价数据**：`loadProductMemberPrices` 函数中的代码被注释掉了，没有实际从后端返回的数据中加载会员价
2. **数据未传递**：`handleEdit` 函数中，虽然后端返回了 `row.memberPrices`，但没有传递给 `loadProductMemberPrices` 函数

### 修复内容

1. **修改loadProductMemberPrices函数** (`admin-frontend/src/views/product/ProductManage.vue`)
   - 添加 `memberPrices` 参数，接收后端返回的会员价数据
   - 从 `memberPrices` 中加载会员价配置，填充到表格中
   - 添加日志输出，便于调试

2. **修改handleEdit函数** (`admin-frontend/src/views/product/ProductManage.vue`)
   - 调用 `loadProductMemberPrices` 时，传入 `row.memberPrices` 参数

3. **添加类型导入** (`admin-frontend/src/views/product/ProductManage.vue`)
   - 导入 `ProductMemberPriceVO` 类型，用于类型检查

### 修改后的效果
- ✅ 编辑商品时，会员价配置能够正确显示
- ✅ 从后端返回的 `memberPrices` 数据中加载会员价
- ✅ 会员价表格能够正确填充已有的会员价数据

### 相关文件
- `admin-frontend/src/views/product/ProductManage.vue`

---

## 2026-01-10 - 修复商品详情页未登录用户价格显示问题

### 问题描述
用户未登录状态下访问商品详情页（`http://localhost:3002/products/22`）时，商品价格行不显示。未登录用户应该和普通用户一样，能够看到商品的基础价格（basePrice）。

### 问题原因
商品详情页的价格行使用了 `v-if="userStore.isLoggedIn()"` 条件，导致只有登录用户才能看到价格信息。未登录用户无法看到商品价格。

### 修复内容

1. **移除登录条件** (`frontend/src/views/products/Detail.vue`)
   - 移除价格行的 `v-if="userStore.isLoggedIn()"` 条件
   - 让未登录用户也能看到价格信息

2. **优化价格显示逻辑** (`frontend/src/views/products/Detail.vue`)
   - 修改 `getDisplayPrice()` 函数，添加未登录用户的处理逻辑
   - 未登录用户：显示基础价格（basePrice）
     - 如果有SKU，显示SKU的 `price` 字段（SKU的基础价格）
     - 如果没有SKU，显示商品的 `basePrice` 字段
   - 普通用户：显示基础价格（basePrice）
   - 会员用户：显示会员价（memberPrice）

### 修改后的效果
- ✅ 未登录用户可以看到商品价格，显示基础价格（basePrice）
- ✅ 普通用户显示基础价格
- ✅ 会员用户显示会员价
- ✅ 价格标签根据用户类型显示："商品价格："（未登录/普通用户）或"会员价："（会员用户）

### 相关文件
- `frontend/src/views/products/Detail.vue`

---

## 2026-01-08 - 修复收藏和缺货登记check接口未登录访问问题

### 问题描述
用户未登录状态下访问商品详情页面时，以下两个接口返回401错误：
- `/api/buyer/favorites/check/{productId}` - 检查是否已收藏
- `/api/buyer/stock-notification/check/{productId}` - 检查是否已登记缺货

这两个接口在未登录状态下应该允许访问，并返回 `false`，而不是返回401错误。

### 问题原因
1. **JWT拦截器要求登录**：`JwtAuthenticationInterceptor` 拦截器将这两个接口视为需要登录的接口，未登录时会抛出401异常
2. **Controller抛出异常**：`StockNotificationController.checkRegistered` 方法在未登录时会调用 `getUserIdFromRequest`，该方法会抛出401异常

### 修复内容

1. **修改JWT拦截器** (`backend/src/main/java/com/shoppingmall/common/security/JwtAuthenticationInterceptor.java`)
   - 在可选认证列表中添加 `/favorites/check/` 和 `/stock-notification/check/` 路径
   - 这两个接口现在支持可选认证：有token就验证并设置userId，没有token就允许通过（作为游客）

2. **修改收藏检查接口** (`backend/src/main/java/com/shoppingmall/controller/buyer/FavoriteController.java`)
   - 修改 `checkFavorite` 方法，添加 `HttpServletRequest` 参数
   - 从request中获取userId，如果为null（未登录），直接返回 `false`
   - 如果已登录，正常检查收藏状态

3. **修改缺货登记检查接口** (`backend/src/main/java/com/shoppingmall/controller/user/StockNotificationController.java`)
   - 修改 `checkRegistered` 方法，不再调用 `getUserIdFromRequest`（该方法会抛出异常）
   - 直接从request中获取userId，如果为null（未登录），直接返回 `false`
   - 如果已登录，正常检查登记状态

### 修改后的效果
- ✅ 未登录用户访问商品详情页时，这两个接口不再返回401错误
- ✅ 未登录时返回 `false`，表示未收藏/未登记
- ✅ 已登录用户正常检查收藏/登记状态
- ✅ 用户体验更好：未登录状态下可以正常浏览商品详情页

### 相关文件
- `backend/src/main/java/com/shoppingmall/common/security/JwtAuthenticationInterceptor.java`
- `backend/src/main/java/com/shoppingmall/controller/buyer/FavoriteController.java`
- `backend/src/main/java/com/shoppingmall/controller/user/StockNotificationController.java`

---

## 2026-01-08 - 完成SKU会员价弹窗设置功能

### 前端修改

1. **admin-frontend/src/views/product/ProductManage.vue**
   - 修改SKU表格，将"启用会员价"和"会员价"列替换为"会员价状态"列，显示会员价设置状态
   - 在SKU操作列添加"设置会员价"按钮
   - 添加SKU会员价设置弹窗，支持按会员等级设置不同的会员价
   - 添加相关响应式数据：`skuMemberPriceDialogVisible`、`currentSkuForMemberPrice`、`currentSkuIndex`、`skuMemberPriceTable`
   - 添加函数：
     - `openSkuMemberPriceDialog`: 打开SKU会员价设置弹窗
     - `initSkuMemberPriceTable`: 初始化SKU会员价表格
     - `handleSkuEnableMemberPriceChange`: 处理SKU启用会员价切换
     - `saveSkuMemberPrice`: 保存SKU会员价设置
   - 修改`handleEdit`函数，在加载SKU数据时加载`memberPrices`字段
   - 修改`generateEditSkuList`函数，初始化SKU时添加`memberPrices`字段
   - 修改保存逻辑，在提交SKU数据时包含`memberPrices`字段

2. **admin-frontend/src/api/admin/product.ts**
   - 添加`ProductMemberPriceVO`接口（用于接收后端数据）
   - 在`ProductDTO`和`ProductVO`中添加`memberPrices`字段
   - 添加`getProductById`导入

3. **admin-frontend/src/api/admin/sku.ts**
   - 添加`ProductSkuMemberPriceVO`接口（用于接收后端数据）
   - 在`ProductSkuDTO`和`ProductSkuVO`中添加`memberPrices`字段

### 后端修改

1. **backend/src/main/java/com/shoppingmall/vo/ProductVO.java**
   - 添加`memberPrices`字段（`List<ProductMemberPriceVO>`）
   - 添加`ProductMemberPriceVO`导入

2. **backend/src/main/java/com/shoppingmall/vo/ProductSkuVO.java**
   - 添加`memberPrices`字段（`List<ProductSkuMemberPriceVO>`）
   - 添加`List`导入

3. **backend/src/main/java/com/shoppingmall/vo/ProductMemberPriceVO.java**（新建）
   - 创建商品会员价VO类，包含`memberLevelId`和`memberPrice`字段

4. **backend/src/main/java/com/shoppingmall/vo/ProductSkuMemberPriceVO.java**（新建）
   - 创建SKU会员价VO类，包含`memberLevelId`和`memberPrice`字段

5. **backend/src/main/java/com/shoppingmall/service/product/impl/ProductServiceImpl.java**
   - 修改`convertToVO`方法，在管理后台查询时（`userId == null`）加载商品会员价配置
   - 查询`product_member_price`表，将数据转换为`ProductMemberPriceVO`列表并设置到`vo.memberPrices`

6. **backend/src/main/java/com/shoppingmall/service/sku/impl/ProductSkuServiceImpl.java**
   - 修改`getSkusByProductId`方法，在管理后台查询时（`userId == null`）加载SKU会员价配置
   - 查询`product_sku_member_price`表，将数据转换为`ProductSkuMemberPriceVO`列表并设置到`vo.memberPrices`
   - 添加`ProductSkuMemberPriceVO`导入

### 功能说明

- 商品管理页面现在支持为每个SKU单独设置会员价
- 点击SKU表格中的"设置会员价"按钮，会弹出会员价设置对话框
- 在对话框中可以启用/禁用会员价，并为每个会员等级设置不同的会员价
- 编辑商品时，会自动加载已有的会员价配置
- 保存商品时，会将SKU的会员价配置一并提交到后端

## 2026-01-08 - 创建系统操作手册文档

### 功能说明
根据系统功能创建了两份操作手册文档（用户端和管理后台），用于给客户使用的操作使用文档。

### 创建内容

**用户端操作手册**：
1. **第一部分**：`docs/用户端操作手册-第一部分.md`
   - 系统简介
   - 登录与注册
   - 商品浏览
   - 购物车管理

2. **第二部分**：`docs/用户端操作手册-第二部分.md`
   - 订单管理
   - 支付功能
   - 会员中心（个人信息、收货地址、预存款、收藏、缺货登记、站内消息等）

**管理后台操作手册**：
1. **第一部分**：`docs/管理后台操作手册-第一部分.md`
   - 系统简介
   - 登录系统
   - 首页概览
   - 商品管理（商品列表、商品发布、商品分类、批量导入）

2. **第二部分**：`docs/管理后台操作手册-第二部分.md`
   - 订单管理（订单列表、订单详情、订单发货、订单统计）
   - 库存管理（库存列表、库存调整、库存预警、库存统计）
   - 采购者管理（采购者列表、会员等级管理）

3. **第三部分**：`docs/管理后台操作手册-第三部分.md`
   - 营销管理（促销活动、价格策略）
   - 数据统计（销售统计、订单统计、商品统计、采购者统计）
   - 系统设置（基础配置、支付配置、物流配置、通知设置）
   - 权限管理（用户管理、角色管理、菜单管理）

### 文档特点
- 操作手册级别，适合客户使用
- 包含详细的操作步骤说明
- 图片位置已预留，供人工粘贴
- 分批次输出，便于管理和维护

### 相关文件
- `docs/用户端操作手册-第一部分.md` (新建)
- `docs/用户端操作手册-第二部分.md` (新建)
- `docs/管理后台操作手册-第一部分.md` (新建)
- `docs/管理后台操作手册-第二部分.md` (新建)
- `docs/管理后台操作手册-第三部分.md` (新建)

---

## 2026-01-08 - 导航菜单配置添加品牌类型选择功能

### 功能说明
在导航菜单配置中，当用户选择"品牌类型"时，自动加载品牌列表供用户选择。

### 修改内容

**前端修改：**

1. **文件：** `admin-frontend/src/components/common/LinkSelector.vue`
   - 在链接类型选择器中添加"品牌类型"选项（类型值：5）
   - 添加品牌下拉选择器（类似商品分类选择器）
   - 导入 `getBrandOptions` API 和 `Brand` 类型
   - 添加品牌列表状态和加载函数 `loadBrands()`
   - 在组件挂载时自动加载品牌列表
   - 当切换到品牌类型时，确保品牌列表已加载

2. **文件：** `admin-frontend/src/views/system/NavigationMenu.vue`
   - 更新 `getLinkTypeString()` 函数：添加 `5 -> 'brand'` 的映射
   - 更新 `getNumberLinkType()` 函数：添加 `'brand' -> 5` 的映射
   - 更新 `generateMenuParams()` 函数：当类型为5时，生成 `{"brand": value}` 格式的JSON参数
   - 更新 `parseMenuData()` 函数：解析品牌类型的参数，支持编辑回显

### 菜单参数格式
- 品牌类型的 `menuParams` 格式：`{"brand": "1"}`（品牌ID转字符串）
- 与数据库现有格式保持一致，使用 `brand` 字段名

### 使用说明
1. 在导航菜单配置页面，点击"添加菜单"或"编辑菜单"
2. 在"链接类型"下拉框中选择"品牌类型"
3. 系统自动加载启用的品牌列表
4. 在"目标品牌"下拉框中选择要关联的品牌
5. 保存后，菜单参数会自动生成 `{"brand": "品牌ID"}` 格式

---

## 2026-01-08 - 执行数据库更新脚本（公告性能优化）

### 执行内容
执行了以下数据库更新脚本：

1. **update-20260108-optimize-announcement-index.sql**
   - 优化公告列表查询性能，添加复合索引
   - 添加 `idx_deleted_publish_sort` 复合索引（deleted, publish_date DESC, sort ASC）
   - 添加 `idx_deleted_status_publish_sort` 复合索引（deleted, status, publish_date DESC, sort ASC）
   - 状态：✅ 执行成功

### 验证结果
- ✅ `announcement` 表的 `idx_deleted_publish_sort` 索引已创建
- ✅ `announcement` 表的 `idx_deleted_status_publish_sort` 索引已创建

---

## 2026-01-07 - 执行数据库更新脚本

### 执行内容
按时间顺序执行了以下数据库更新脚本：

1. **update-20260102-create-product-sync-log.sql**
   - 创建商品同步日志表 `product_sync_log`
   - 状态：✅ 执行成功

2. **update-20260103-add-test-shop-id.sql**
   - 为 `jushuitan_config` 表添加测试环境店铺ID字段 `test_shop_id`
   - 状态：✅ 字段已存在（之前已执行）

3. **update-20260105-add-logistics-callback-url.sql**
   - 为 `jushuitan_config` 表添加物流同步回调地址字段 `test_callback_url` 和 `callback_url`
   - 状态：✅ 字段已存在（之前已执行）

4. **update-20260105-add-system-config-category.sql**
   - 为 `system_config` 表添加分类字段 `category`
   - 状态：✅ 字段已存在（之前已执行）

### 验证结果
- ✅ `product_sync_log` 表已创建
- ✅ `jushuitan_config` 表相关字段已存在
- ✅ `system_config` 表 `category` 字段已存在

### 说明
部分脚本出现"字段已存在"的错误提示，属于正常情况，说明这些脚本之前已经执行过。数据库结构已是最新状态。

---

## 2026-01-08 - 修复首页广告位跳转问题：标题和图片统一使用广告配置的链接

### 问题描述
首页楼层广告位（如 floor_1）配置了商品分类跳转（link_type=1, link_value=1），但点击后跳转的地址不正确。例如配置的是分类ID=1，但实际跳转到了 categoryId=5。

### 原因分析
1. 在 `Index.vue` 中，楼层数据使用了硬编码的 `floorConfig` 映射（如 floor_1 对应 categoryId: 5），而不是使用广告数据中的 `linkValue`
2. 在 `CategoryFloor.vue` 中，标题栏点击使用的是传入的 `categoryId`（硬编码值），而不是广告的 `linkValue`
3. 大图广告没有点击事件处理

### 解决方案
1. **修改 `frontend/src/views/home/Index.vue`**：
   - 在楼层数据中添加 `adLinkType` 和 `adLinkValue` 字段，从广告数据中获取
   - 将广告的 `linkType` 和 `linkValue` 传递给 `CategoryFloor` 组件

2. **修改 `frontend/src/components/home/CategoryFloor.vue`**：
   - 添加 `adLinkType` 和 `adLinkValue` 属性接收广告链接配置
   - 创建统一的 `goToAdTarget()` 方法处理跳转逻辑，优先使用广告配置的 `linkValue`
   - 标题栏点击调用 `goToAdTarget()` 方法
   - 大图广告添加点击事件，也调用 `goToAdTarget()` 方法
   - 如果广告没有配置链接或链接类型为0（无链接），则回退使用 `categoryId`

### 修改内容

1. ✅ `frontend/src/views/home/Index.vue`
   - 更新 `floorData` 类型定义，添加 `adLinkType?: number` 和 `adLinkValue?: string` 字段
   - 在生成楼层数据时，从广告对象中获取 `linkType` 和 `linkValue` 并赋值给 `adLinkType` 和 `adLinkValue`
   - 在模板中将 `adLinkType` 和 `adLinkValue` 传递给 `CategoryFloor` 组件

2. ✅ `frontend/src/components/home/CategoryFloor.vue`
   - 在 `Props` 接口中添加 `adLinkType?: number` 和 `adLinkValue?: string` 属性
   - 创建 `goToAdTarget()` 方法，根据 `adLinkType` 处理不同类型的跳转：
     - `linkType=1`：商品分类 → `/products?categoryId=${adLinkValue}`
     - `linkType=2`：商品详情 → `/products/${adLinkValue}`
     - `linkType=3`：促销活动 → `/products?type=${adLinkValue}`
     - `linkType=4`：外部链接 → 新窗口打开
   - 修改 `goToCategory()` 方法，调用 `goToAdTarget()` 方法
   - 为大图广告添加 `@click="goToAdTarget"` 事件

### 修改后的效果
- ✅ 标题栏点击优先使用广告配置的 `linkValue`，而不是硬编码的 `categoryId`
- ✅ 大图广告点击也使用广告配置的 `linkValue` 进行跳转
- ✅ 标题和图片点击跳转到相同的位置（都使用广告配置的链接）
- ✅ 如果广告没有配置链接，则回退使用默认的 `categoryId`
- ✅ 支持所有广告链接类型：商品分类、商品详情、促销活动、外部链接

---

## 2025-12-27 - 商品列表和库存列表分类选择改为级联选择器

### 修改内容

1. **商品列表页面 (`admin-frontend/src/views/product/ProductManage.vue`)**
   - 将搜索栏的分类下拉选择从 `el-select` 改为 `el-cascader`，支持多级树展开
   - 将编辑对话框中的分类选择从 `el-select` 改为 `el-cascader`
   - 添加 `cascaderProps` 配置，与商品发布页面保持一致
   - 修改 `loadProductList` 函数，处理级联选择器返回的数组值（取最后一个值）
   - 修改 `handleSubmit` 函数，处理编辑时的分类ID（级联选择器返回数组）
   - 更新 `searchForm.categoryId` 的类型定义，支持数组类型

2. **库存列表页面 (`admin-frontend/src/views/inventory/Index.vue`)**
   - 将搜索栏的分类下拉选择从 `el-select` 改为 `el-cascader`，支持多级树展开
   - 添加 `cascaderProps` 配置，与商品发布页面保持一致
   - 修改 `loadInventoryList` 函数，处理级联选择器返回的数组值（取最后一个值）
   - 修改 `handleReset` 函数，重置时使用 `undefined` 而不是空字符串
   - 更新 `searchForm.categoryId` 的类型定义，支持数组类型

### 技术细节

- 级联选择器配置：
  ```typescript
  const cascaderProps = {
    value: 'id',
    label: 'categoryName',
    children: 'children',
    checkStrictly: true
  }
  ```
- 级联选择器返回数组时，取最后一个值作为分类ID：
  ```typescript
  const categoryId = Array.isArray(searchForm.value.categoryId)
    ? searchForm.value.categoryId[searchForm.value.categoryId.length - 1]
    : searchForm.value.categoryId
  ```

### 变更原因

统一商品列表、库存列表和商品发布页面的分类选择方式，使用多级树展开的级联选择器，提升用户体验。

---

## 2025-12-27 - 更新部署文档为子域名架构（移除路径方式）

### 修改内容

1. **更新服务器部署检查清单.md**
   - 移除测试环境路径方式配置
   - 简化目录结构说明（只保留正式环境）
   - 移除测试环境后端项目配置
   - 简化前端构建说明（只保留正式环境）
   - 更新访问地址说明（移除路径方式测试环境）

2. **更新完全隔离环境部署方案.md**
   - 更新方案概述，说明测试环境也使用子域名（test前缀）
   - 移除测试环境路径方式配置
   - 更新前端环境变量说明（移除test环境路径配置）
   - 更新Vite配置说明（移除路径相关配置）
   - 更新部署步骤说明（简化配置）

3. **更新前端Vite配置**
   - `frontend/vite.config.ts`：移除测试环境路径相关proxy配置，简化build配置
   - `admin-frontend/vite.config.ts`：移除测试环境路径相关proxy配置，简化build配置，修复语法错误

4. **更新后端配置文件**
   - `application-prod.yml`：添加HTTPS代理支持配置
   - `application-test.yml`：更新测试环境前端URL为test前缀子域名，添加HTTPS代理支持

### 架构变更

**正式环境架构（子域名方式）**：
- 用户端：https://www.shop.quaichao.com/
- 管理后台：https://admin.quaichao.com
- API服务：https://api.quaichao.com

**测试环境架构（如需，使用test前缀子域名）**：
- 用户端：https://test-www.shop.quaichao.com
- 管理后台：https://test-admin.quaichao.com
- API服务：https://test-api.quaichao.com

**移除的路径方式**：
- ~~测试环境用户端：www.shop.quaichao.com/test~~
- ~~测试环境管理后台：www.shop.quaichao.com/test/admin~~

### 变更原因

1. **统一架构**：所有环境均使用子域名方式，架构更清晰
2. **避免混淆**：路径方式容易与前端路由冲突
3. **便于扩展**：子域名方式更便于独立配置和管理
4. **符合规范**：符合现代Web应用部署最佳实践

### 代码修改清单

1. ✅ `frontend/vite.config.ts` - 移除路径相关配置
2. ✅ `admin-frontend/vite.config.ts` - 移除路径相关配置，修复语法
3. ✅ `backend/src/main/resources/application-prod.yml` - 添加HTTPS代理支持
4. ✅ `backend/src/main/resources/application-test.yml` - 更新为test前缀子域名

### 需要执行的配置

1. 创建前端环境变量文件（.env.production），配置VITE_API_BASE_URL=https://api.quaichao.com
2. 在宝塔面板创建三个独立站点（正式环境）
3. 为三个域名分别申请SSL证书
4. 在管理后台配置支付回调地址为https://api.quaichao.com/api/buyer/payment/{wechat|alipay}/notify

## 2025-01-08 - 管理后台路由支持根路径访问（方案一：兼容模式）

### 修改内容

1. **更新路由配置 (admin-frontend/src/router/index.ts)**
   - 添加根路径 `/login` 重定向到 `/admin/login`（支持独立子域名访问）
   - 添加根路径 `/` 重定向到 `/admin/dashboard`（支持独立子域名访问）
   - 保留所有现有的 `/admin/*` 路径（兼容开发环境）
   - 更新路由守卫，同时支持根路径和 `/admin` 路径两种格式
   - 更新404路由处理，根路径和 `/dashboard` 路径重定向到 `/admin/dashboard`

2. **更新 NotFound 组件 (admin-frontend/src/components/NotFound.vue)**
   - 支持根路径格式的检查（`/dashboard`, `/login`, `/`）

3. **更新主入口文件 (admin-frontend/src/main.ts)**
   - 更新访问地址日志，说明支持两种访问方式

### 访问方式

**独立子域名访问（生产环境）**：
- `https://admin.quaichao.com/` → 自动重定向到 `/admin/dashboard`
- `https://admin.quaichao.com/login` → 自动重定向到 `/admin/login`

**开发环境访问（兼容模式）**：
- `http://localhost:3003/admin/` → 直接访问 `/admin/dashboard`
- `http://localhost:3003/admin/login` → 直接访问登录页

### 变更原因

1. **支持独立子域名**：管理后台部署在 `admin.quaichao.com` 时，用户可以直接通过根路径访问，无需 `/admin` 前缀
2. **保持向后兼容**：保留 `/admin/*` 路径，确保开发环境和现有代码不受影响
3. **用户体验优化**：独立子域名访问时，URL更简洁（`/login` 而不是 `/admin/login`）

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 添加根路径重定向，更新路由守卫
2. ✅ `admin-frontend/src/components/NotFound.vue` - 支持根路径格式检查
3. ✅ `admin-frontend/src/main.ts` - 更新访问地址日志

### 注意事项

- 所有动态路由仍然基于 `/admin` 路径添加，确保兼容性
- 组件中的路径跳转代码（如 `router.push('/admin/login')`）仍然有效
- 路由守卫会自动处理两种路径格式的转换

## 2025-01-08 - 修复开发环境登录页访问问题

### 问题描述

开发环境访问 `http://localhost:3003/admin/login` 时，直接跳转到了 `http://localhost:3003/admin/dashboard`，显示空白页。

### 问题原因

1. 路由守卫中，如果用户已登录，访问登录页会跳转到 dashboard
2. 但是 dashboard 路由可能还没有被添加（因为需要菜单数据）
3. 路由匹配逻辑有问题，导致登录页无法正常访问

### 修复内容

1. **优化路由守卫逻辑顺序** (`admin-frontend/src/router/index.ts`)
   - 优先处理登录页：如果是登录页，直接允许访问（无论是否登录）
   - 如果已登录访问登录页，会检查 dashboard 路由是否存在后再跳转
   - 如果 dashboard 路由不存在，会尝试加载菜单数据或允许访问登录页
   - 调整登录检查顺序：在检查路由匹配之前先检查是否需要登录
   - 修复未登录用户访问 dashboard 时的处理逻辑

### 修复后的行为

**未登录用户**：
- 访问 `http://localhost:3003/admin/login` → 正常显示登录页 ✅
- 访问 `http://localhost:3003/admin/dashboard` → 自动跳转到登录页 ✅

**已登录用户**：
- 访问 `http://localhost:3003/admin/login` → 自动跳转到 dashboard（如果路由存在）✅
- 访问 `http://localhost:3003/admin/dashboard` → 正常显示 dashboard ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 优化路由守卫逻辑顺序，修复登录页访问问题

## 2025-01-08 - 修复已登录用户访问登录页时的跳转问题

### 问题描述

已登录用户访问 `http://localhost:3003/admin/login` 时，没有自动跳转到 dashboard 页面。

### 问题原因

1. 路由守卫中检查 dashboard 路由是否存在的逻辑不够准确
2. 即使有菜单数据，dashboard 路由可能还没有被添加
3. 路由查找方式不够全面，可能找不到已添加的 dashboard 路由

### 修复内容

1. **优化登录页跳转逻辑** (`admin-frontend/src/router/index.ts`)
   - 改进 dashboard 路由查找逻辑，支持多种路径格式（`dashboard`、`/dashboard`、`/admin/dashboard`）
   - 如果找不到路由，会通过路由名称查找（包含 `dashboard` 的路由）
   - 如果路由不存在，会强制添加路由并等待更长时间（200ms）
   - 添加详细的日志输出，便于调试
   - 无论路由是否存在，都会跳转到 dashboard（Layout 组件会处理路由不存在的情况）

### 修复后的行为

**已登录用户访问登录页**：
- 访问 `http://localhost:3003/admin/login` → 自动跳转到 `/admin/dashboard` ✅
- 如果 dashboard 路由不存在，会自动添加路由后再跳转 ✅
- 添加了详细的日志输出，便于排查问题 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 优化登录页跳转逻辑，改进路由查找方式

## 2025-01-08 - 修复 dashboard 页面空白问题

### 问题描述

登录后访问 `http://localhost:3003/admin/dashboard`，页面显示空白。

### 问题原因

1. 当访问 dashboard 时，如果路由未匹配，代码会直接 `next()` 允许访问
2. Layout 组件会渲染，但 router-view 中没有对应的子路由，导致空白
3. dashboard 路由可能还没有被添加，或者路由查找逻辑有问题

### 修复内容

1. **优化路由守卫逻辑** (`admin-frontend/src/router/index.ts`)
   - 在检查路由匹配之前，先特殊处理 dashboard 路径
   - 如果访问 dashboard 但路由未匹配，会先尝试添加路由
   - 如果路由添加成功，会重新导航到 dashboard
   - 如果路由添加失败，也会允许访问，但 Layout 会显示提示
   - 增加等待时间（200ms）确保路由添加完成

2. **优化 Layout 组件** (`admin-frontend/src/components/Layout/index.vue`)
   - 使用 router-view 的 slot，当没有组件时显示提示和重试按钮
   - 添加 `handleRetry` 函数，可以重新加载菜单数据并添加路由
   - 添加样式支持，美化提示界面

### 修复后的行为

**已登录用户访问 dashboard**：
- 访问 `http://localhost:3003/admin/dashboard` → 如果路由未匹配，会自动添加路由并重新导航 ✅
- 如果路由添加失败，会显示友好的提示和重试按钮 ✅
- 添加了详细的日志输出，便于排查问题 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 优化 dashboard 路由处理逻辑
2. ✅ `admin-frontend/src/components/Layout/index.vue` - 添加 router-view slot 支持和重试功能

## 2025-01-08 - 修复登录成功后未跳转问题

### 问题描述

登录成功后，接口返回成功，但没有跳转到系统首页（dashboard）。

### 问题原因

1. 登录成功后，路由添加和跳转的时序问题
2. 路由添加后没有等待足够的时间，导致跳转时路由还未完全添加
3. 使用 `router.push` 可能在历史记录中留下登录页

### 修复内容

1. **优化登录成功后的跳转逻辑** (`admin-frontend/src/views/auth/Login.vue`)
   - 在设置 store 状态后，等待 50ms 确保状态已更新
   - 添加路由后，等待 300ms 确保路由已完全添加
   - 使用 `router.replace` 而不是 `router.push`，避免在历史记录中留下登录页
   - 添加错误处理，如果 replace 失败，会尝试使用 push
   - 添加详细的日志输出，便于调试

### 修复后的行为

**登录成功**：
- 设置 token、adminInfo、menus、permissions ✅
- 等待 store 状态更新完成 ✅
- 添加路由并等待路由添加完成 ✅
- 跳转到 `/admin/dashboard` ✅
- 添加了详细的日志输出，便于排查问题 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/views/auth/Login.vue` - 优化登录成功后的跳转逻辑，增加等待时间和错误处理

## 2025-01-08 - 修复路由重复添加问题

### 问题描述

控制台一直输出路由添加信息，路由被重复添加，导致性能问题和日志混乱。

### 问题原因

1. 路由守卫在路由添加后会重新执行（`next({ ...to, replace: true })`）
2. 路由守卫再次检查时，可能路由还没有完全注册好，导致重复添加
3. 没有防止重复添加的机制
4. 日志输出过多，影响调试

### 修复内容

1. **添加防止重复添加的机制** (`admin-frontend/src/router/index.ts`)
   - 添加 `isAddingRoutes` 标志，防止重复添加路由
   - 在路由添加过程中，如果再次调用 `addRoutes`，直接返回
   - 在路由守卫中，如果正在添加路由，直接允许访问，避免重复添加

2. **优化路由查找逻辑**
   - 改进 dashboard 路由查找方式，更准确地判断路由是否存在
   - 如果路由已匹配，直接允许访问，不再重复检查

3. **优化日志输出**
   - 路由添加的详细日志只在开发环境输出
   - 减少不必要的日志输出

### 修复后的行为

**路由添加**：
- 路由只会添加一次，不会重复添加 ✅
- 路由添加过程中，如果再次调用，会跳过 ✅
- 路由守卫不会导致重复添加 ✅
- 日志输出更清晰，只在开发环境输出详细日志 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 添加防止重复添加机制，优化路由查找逻辑和日志输出

## 2025-01-08 - 修复刷新页面后路由丢失问题

### 问题描述

登录成功后可以正常访问 dashboard，但是刷新页面后，页面显示空白，路由匹配到了 404 路由。

### 问题原因

1. 刷新页面后，路由守卫执行时，路由还没有被添加（因为菜单数据是从 localStorage 恢复的，但路由需要重新添加）
2. 路由守卫检查路由匹配时，发现路由未匹配，匹配到了 404 路由
3. 路由添加逻辑在路由匹配检查之后，导致刷新时路由无法正确恢复

### 修复内容

1. **优化路由守卫逻辑** (`admin-frontend/src/router/index.ts`)
   - 在路由守卫的最开始，检查是否已登录且有菜单数据
   - 如果 admin 路由没有子路由，或者当前路径未匹配且不是登录页，先添加路由
   - 等待路由添加完成后，重新匹配路由并导航
   - 添加详细的日志输出，便于调试

### 修复后的行为

**刷新页面**：
- 刷新页面后，路由守卫会先检查路由是否已添加 ✅
- 如果路由未添加，会先添加路由，然后再检查匹配 ✅
- 路由添加完成后，会重新导航到当前路径 ✅
- 添加了详细的日志输出，便于排查问题 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 在路由守卫开始时就检查并添加路由，确保刷新时路由能正确恢复

## 2025-01-08 - 修复刷新页面后匹配到404路由的问题

### 问题描述

刷新页面后，路由匹配到了 404 路由，导致页面空白。修复后出现死循环问题。

### 问题原因

1. 刷新页面时，路由还没有被添加
2. `/admin/dashboard` 路径匹配到了 404 路由 `/admin/:pathMatch(.*)*`
3. 之前的逻辑中，如果路由已匹配（即使是404），就不会添加路由
4. 导致刷新时无法正确恢复路由

### 修复内容

1. **优化路由守卫逻辑** (`admin-frontend/src/router/index.ts`)
   - 在路由守卫的最开始，优先处理404路由的情况
   - 如果匹配到404路由，且已登录，检查是否需要添加路由
   - 如果没有子路由，添加路由并重新导航
   - 避免死循环：如果路由已匹配且不是404，直接允许访问

### 修复后的行为

**刷新页面**：
- 如果匹配到404路由，会先检查并添加路由 ✅
- 路由添加成功后，会重新导航到正确路径 ✅
- 避免了死循环问题 ✅
- 刷新后页面能正常显示 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 优化路由守卫逻辑，优先处理404路由情况，确保刷新时路由能正确恢复

## 2025-01-08 - 修改根路径访问逻辑，根据登录状态跳转

### 修改内容

1. **修改根路径处理逻辑** (`admin-frontend/src/router/index.ts`)
   - 移除根路径 `/` 的静态重定向
   - 在路由守卫中添加根路径处理逻辑
   - 根据登录状态决定跳转：
     - 已登录 → 跳转到 `/admin/dashboard`
     - 未登录 → 跳转到 `/admin/login`

### 修改后的行为

**访问根路径 `http://localhost:3003/`**：
- 如果已登录 → 自动跳转到 `http://localhost:3003/admin/dashboard` ✅
- 如果未登录 → 自动跳转到 `http://localhost:3003/admin/login` ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 移除根路径静态重定向，在路由守卫中根据登录状态动态跳转

## 2025-01-08 - 彻底修复刷新页面后路由丢失问题

### 问题描述

登录成功后，刷新首页出现空白，路由匹配到了404路由，说明路由没有正确恢复。

### 问题原因

1. 刷新页面时，路由守卫执行时，store 可能还没有从 localStorage 恢复数据
2. 路由守卫中的路由添加逻辑执行顺序有问题
3. 路由添加后等待时间不够，导致路由还没有完全注册就继续执行
4. 路由匹配检查不够准确，没有排除404路由

### 修复内容

1. **优化路由守卫逻辑** (`admin-frontend/src/router/index.ts`)
   - 在路由守卫开始时，确保 store 已从 localStorage 恢复数据
   - 如果菜单数据为空，尝试从 localStorage 恢复
   - 优化路由添加和匹配的逻辑：
     - 优先检查并添加路由（在所有其他检查之前）
     - 如果没有子路由，或者匹配到了404路由，或者路由未匹配，都需要添加路由
     - 增加等待时间到 500ms，确保路由已完全添加
     - 改进路由匹配检查，确保不是404路由才认为匹配成功
     - 添加详细的日志输出，便于调试

### 修复后的行为

**刷新页面**：
- 路由守卫开始时，会先确保 store 已初始化 ✅
- 如果菜单数据为空，会从 localStorage 恢复 ✅
- 优先检查并添加路由，确保路由已添加 ✅
- 路由添加后，会正确匹配并导航 ✅
- 避免了匹配到404路由的问题 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 优化路由守卫逻辑，确保 store 初始化，优先添加路由，改进匹配检查

## 2025-01-XX - 修复路由添加后空白页面问题

### 问题
- 刷新 `http://localhost:3003/admin/dashboard` 后出现空白页面
- 路由添加成功后，检查 `hasChildrenAfter` 时发现仍然没有子路由
- 日志显示："刷新页面：路由添加后仍然没有子路由，允许访问，Layout 会处理"

### 修复
- 添加了 `nextTick` 导入，确保 Vue Router 路由表更新后再检查
- 优化了路由添加后的检查逻辑：
  - 使用 `nextTick()` 和 `setTimeout` 等待路由表更新
  - 无论是否有子路由，都尝试重新匹配路由
  - 如果路由匹配成功，重新导航到目标路径
  - 如果路由未匹配但目标是 dashboard，允许访问让 Layout 处理
- 在 `addRoutes` 函数中添加了路由添加验证日志，输出子路由数量

### 修改文件
- `admin-frontend/src/router/index.ts`
  - 添加 `nextTick` 导入
  - 优化路由添加后的检查逻辑
  - 添加路由添加验证日志

## 2025-01-XX - 修复刷新页面时跳转到 dashboard 的问题

### 问题
- 访问 `http://localhost:3003/admin/product/list` 后刷新页面，会跳转到 `http://localhost:3003/admin/dashboard`
- 原因是路由守卫中，当路由未匹配时，如果用户已登录，会强制跳转到 dashboard

### 修复
- 优化了路由未匹配时的处理逻辑：
  - 如果路由添加后仍然未匹配，允许访问让 Layout 处理，而不是跳转到 dashboard
  - 如果路由已存在但未匹配，也允许访问让 Layout 处理
  - 移除了强制跳转到 dashboard 的逻辑（第 507-509 行）
  - 只有在真正找不到路由且未登录时才跳转到登录页
  - 已登录但路由不存在时，允许访问让 Layout 处理（Layout 会显示404或重定向）
- 改进了路由匹配检查，使用 `router.resolve(to.path)` 并排除 404 路由
- 添加了 `nextTick()` 等待路由表更新

### 修改文件
- `admin-frontend/src/router/index.ts`
  - 优化路由未匹配时的处理逻辑（第 452-515 行）
  - 改进路由匹配检查，排除 404 路由
  - 修复 TypeScript 类型检查错误（第 197 行）

## 2025-01-XX - 修复刷新 dashboard 页面空白问题

### 问题
- 刷新 `http://localhost:3003/admin/dashboard` 后出现空白页面
- 路由添加成功后，`adminRoute.children` 仍然是空的（子路由数量: 0）
- 重新导航时匹配到了路由，但再次进入路由守卫时，又匹配到了 `admin-404`
- 日志显示："路由添加验证：admin 路由的子路由数量: 0"

### 问题原因
- `router.addRoute('admin', route)` 添加路由后，`router.getRoutes()` 返回的路由对象的 `children` 属性可能没有立即更新
- Vue Router 的路由表更新是异步的，需要更多时间
- `router.resolve(to.path)` 可以正确匹配到路由，但 `adminRoute.children` 可能还是空的
- 使用 `next({ ...to, replace: true })` 会重新触发路由守卫，但此时路由可能还没有完全注册好，导致又匹配到了404

### 修复
- 优化路由添加后的检查逻辑：
  - 不再依赖 `adminRoute.children` 来检查路由是否已添加
  - 直接使用 `router.resolve(to.path)` 来检查路由是否匹配
  - 增加等待时间到 400ms，确保路由表已更新
  - 如果路由匹配成功，使用 `next()` 而不是 `next({ ...to, replace: true })`，避免重新触发路由守卫
  - 在重新导航前，再次检查路由是否匹配，避免重新触发路由守卫时匹配到404
- 优化404路由处理逻辑：
  - 如果有子路由但匹配到了404，等待一下后重新匹配
  - 如果路由存在但匹配到了404，可能是路由表更新延迟，等待后重新匹配

### 修改文件
- `admin-frontend/src/router/index.ts`
  - 优化路由添加后的检查逻辑（第 274-314 行）
  - 改进404路由处理逻辑（第 315-350 行）
  - 增加等待时间，确保路由表已更新
  - 使用 `next()` 而不是 `next({ ...to, replace: true })`，避免重新触发路由守卫

## 2025-01-08 - 彻底修复刷新页面后匹配到404路由导致空白页的问题

### 问题描述
- 刷新页面后，路由匹配到了 404 路由 `/admin/:pathMatch(.*)*`，导致页面显示空白
- 路由添加成功后，虽然路由已添加到路由表，但当前导航仍然匹配到404路由
- 日志显示："路由守卫 - 当前路径: /admin/dashboard 匹配的路由: ['/admin/:pathMatch(.*)*'] 路由名称: admin-404"
- 路由添加验证显示："admin 路由的子路由数量: 0"，说明路由添加后 children 属性没有立即更新

### 问题根本原因
1. **404路由在动态路由之前定义**：404路由 `/admin/:pathMatch(.*)*` 在路由配置中预先定义（第59-67行），导致在动态路由添加之前，所有 `/admin/*` 路径都会匹配到404路由
2. **路由匹配顺序问题**：Vue Router 按照路由定义的顺序进行匹配，通配符路由会优先匹配，导致动态路由无法正确匹配
3. **路由守卫逻辑过于复杂**：之前的修复尝试通过复杂的逻辑来处理，但无法从根本上解决问题

### 修复内容（基于网上最佳实践）

1. **移除预先定义的404路由** (`admin-frontend/src/router/index.ts`)
   - 移除了路由配置中预先定义的 `/admin/:pathMatch(.*)*` 404路由（第59-67行）
   - 这样可以确保动态路由能够优先匹配

2. **在动态路由添加之后再添加404路由** (`admin-frontend/src/router/index.ts`)
   - 在 `addRoutes` 函数中，在所有动态路由添加完成之后，再添加404路由
   - 使用 `router.addRoute('admin', { path: ':pathMatch(.*)*', ... })` 将404路由作为 admin 路由的子路由添加
   - 检查404路由是否已存在，避免重复添加

3. **简化路由守卫逻辑** (`admin-frontend/src/router/index.ts`)
   - 简化了路由守卫中的路由添加和匹配逻辑
   - 如果检测到路由未添加或匹配到404路由，直接添加路由并使用 `next({ ...to, replace: true })` 重新导航
   - 移除了复杂的等待和重试逻辑，因为现在404路由会在动态路由之后添加，不会出现匹配问题

### 修复后的行为
**刷新页面**：
- 刷新页面后，如果路由未添加，会先添加路由（包括动态路由和404路由）✅
- 路由添加完成后，使用 `next({ ...to, replace: true })` 重新导航 ✅
- 由于404路由在动态路由之后添加，动态路由会优先匹配 ✅
- 避免了页面空白的问题 ✅

### 代码修改清单
1. ✅ `admin-frontend/src/router/index.ts` - 移除预先定义的404路由，在动态路由添加之后再添加404路由，简化路由守卫逻辑

### 参考来源
- Vue Router 动态路由最佳实践：404路由应该在动态路由添加之后再添加
- 网上相关问题的解决方案：延迟添加404路由，确保动态路由优先匹配

## 2025-01-08 - 修复直接访问URL时跳转到dashboard的问题

### 问题描述
- 直接在浏览器输入 `http://localhost:3003/admin/buyer/list` 等URL时，会自动跳转到 `http://localhost:3003/admin/dashboard`
- 刷新页面可以正常显示，但直接访问URL会跳转

### 问题原因
1. 路由守卫在检查路由是否添加时，没有排除登录页
2. 当访问 `/admin/login` 时，如果路由未添加，会触发路由添加逻辑并重新导航到登录页
3. 由于用户已登录，登录页的逻辑会跳转到 dashboard
4. 这导致即使访问其他URL（如 `/admin/buyer/list`），如果路由未添加，也会先导航到登录页，然后跳转到 dashboard

### 修复内容
1. **排除登录页的路由添加逻辑** (`admin-frontend/src/router/index.ts`)
   - 在路由守卫的早期检查中，添加 `isLoginPage` 判断
   - 排除登录页（`/admin/login` 和 `/login`），避免在登录页触发路由添加和重新导航
   - 确保只有访问非登录页的 admin 路径时，才会触发路由添加逻辑

### 修复后的行为
**直接访问URL**：
- 访问 `http://localhost:3003/admin/buyer/list` → 如果路由未添加，会添加路由并正确导航到该页面 ✅
- 不会因为访问登录页而跳转到 dashboard ✅
- 刷新页面和直接访问URL都能正常工作 ✅

### 代码修改清单
1. ✅ `admin-frontend/src/router/index.ts` - 在路由守卫早期检查中排除登录页，避免在登录页触发路由添加逻辑

## 2025-01-08 - 修复登录页路由检查逻辑，使用 router.resolve 检查路由是否存在

### 问题描述
- 直接访问 `/admin/buyer/list` 等URL时，仍然会跳转到 `/admin/dashboard`
- 日志显示：登录页的逻辑在检查 dashboard 路由是否存在时，使用 `adminRoute?.children?.find()` 检查，但路由添加后 `children` 属性不会立即更新
- 导致即使路由已添加，检查结果仍然是 `false`，然后跳转到 dashboard

### 问题原因
1. 登录页的逻辑使用 `adminRoute?.children?.find()` 来检查路由是否存在
2. `router.addRoute()` 后，`router.getRoutes()` 返回的路由对象的 `children` 属性可能不会立即更新
3. 即使路由已添加，检查结果仍然是 `false`，导致不必要的跳转

### 修复内容
1. **优化登录页的路由检查逻辑** (`admin-frontend/src/router/index.ts`)
   - 使用 `router.resolve('/admin/dashboard')` 来检查路由是否存在，而不是检查 `children` 属性
   - `router.resolve()` 可以正确匹配到已添加的路由，即使 `children` 属性还没有更新
   - 简化登录页的逻辑：如果路由未添加，先添加路由，然后直接跳转（路由守卫会处理路由匹配）

### 修复后的行为
**直接访问URL**：
- 访问 `http://localhost:3003/admin/buyer/list` → 如果路由未添加，会添加路由并正确导航到该页面 ✅
- 登录页的逻辑使用 `router.resolve()` 检查路由，可以正确识别已添加的路由 ✅
- 避免了因为 `children` 属性未更新而导致的错误跳转 ✅

### 代码修改清单
1. ✅ `admin-frontend/src/router/index.ts` - 优化登录页的路由检查逻辑，使用 `router.resolve()` 而不是检查 `children` 属性

## 2025-01-08 - 在 dashboard 页面添加刷新数据按钮

### 修改内容
1. **添加刷新按钮** (`admin-frontend/src/views/dashboard/Index.vue`)
   - 在页面顶部标题旁边添加"刷新数据"按钮
   - 按钮带有刷新图标，点击时显示加载状态
   - 只有在有权限时才显示刷新按钮

2. **实现刷新功能**
   - 添加 `handleRefresh` 函数，实现数据刷新逻辑
   - 刷新时先销毁现有图表实例，然后重新加载数据和初始化图表
   - 刷新成功后显示成功提示

3. **优化页面布局**
   - 使用 flex 布局，让标题和按钮在同一行显示
   - 标题和按钮之间自动对齐

### 功能说明
- 点击"刷新数据"按钮后，会重新加载所有统计数据
- 同时会重新初始化所有图表（销售趋势、订单趋势、订单状态分布）
- 刷新过程中按钮显示加载状态，防止重复点击
- 刷新成功后显示成功提示

### 代码修改清单
1. ✅ `admin-frontend/src/views/dashboard/Index.vue` - 添加刷新按钮和刷新功能，优化页面布局

---

## 2025-01-XX 修复正式环境API请求失败问题

### 问题描述
正式环境（https://www.shop.quaichao.com）出现以下错误：
1. **API请求全部失败**：所有API请求返回"请求失败"错误
   - 加载系统配置失败
   - 加载导航菜单失败
   - 加载商品分类失败
   - 加载页脚数据失败
   - 获取验证码失败
2. **CSS文件404错误**：layer.css、laydate.css、code.css文件找不到
3. **Chrome扩展错误**：浏览器扩展导致的语法错误（不影响功能）

### 问题原因
1. **API地址配置错误**：`frontend/.env.production`文件中`VITE_API_BASE_URL`设置为相对路径`/api`，导致构建后的代码无法正确请求API
2. **构建脚本覆盖环境变量**：`scripts/build-user-prod.bat`中设置了空的环境变量，覆盖了`.env.production`文件的配置

### 解决方案
1. **修改环境变量文件**：`frontend/.env.production`
   - 将`VITE_API_BASE_URL`从`/api`改为`https://api.quaichao.com`
   - 确保正式环境使用完整的HTTPS URL进行API请求

2. **修改构建脚本**：`scripts/build-user-prod.bat`
   - 移除覆盖环境变量的代码（`set VITE_API_BASE_URL=`）
   - 让Vite自动读取`.env.production`文件中的配置

### 代码修改清单
1. ✅ `frontend/.env.production` - 修改API地址为`https://api.quaichao.com`
2. ✅ `scripts/build-user-prod.bat` - 移除环境变量覆盖代码，使用.env.production配置

### 后续操作
1. 重新构建前端项目：`npm run build -- --mode production`
2. 重新部署构建产物到服务器
3. 验证API请求是否正常

---

## 2025-01-XX 修复地区API请求返回HTML而非JSON数据问题

### 问题描述
正式环境（https://www.shop.quaichao.com/register）访问地区接口时：
- API请求 `https://www.shop.quaichao.com/api/regions/provinces` 返回200状态码
- 但返回的是HTML页面内容（前端index.html），而不是JSON数据
- 本地开发环境正常

### 问题原因
`frontend/src/api/common/region.ts` 文件直接使用 `axios` 而不是统一的 `request` 工具，导致：
1. **未使用环境变量**：没有使用 `VITE_API_BASE_URL`（`https://api.quaichao.com`）
2. **请求路径错误**：使用相对路径 `/api/regions`，请求发送到 `https://www.shop.quaichao.com/api/regions/provinces`
3. **被前端路由捕获**：该路径被前端路由规则捕获，返回HTML页面而不是代理到后端API

### 解决方案
修改 `frontend/src/api/common/region.ts` 文件：
1. 将 `import axios from 'axios'` 改为 `import request from '@/utils/request'`
2. 将所有 `axios.get()` 调用改为 `request.get()`
3. 移除手动解析响应数据的代码（`request` 工具会自动处理响应拦截器，直接返回 `data`）

### 代码修改清单
1. ✅ `frontend/src/api/common/region.ts` - 修改为使用统一的request工具
   - 修改导入语句：`axios` → `request`
   - 修改 `getProvinces()` 函数
   - 修改 `getChildrenByParentId()` 函数
   - 修改 `getRegionByCode()` 函数
   - 修改 `getFullPathByCode()` 函数

### 修改后的效果
- ✅ 请求会正确发送到：`https://api.quaichao.com/api/regions/provinces`
- ✅ 使用统一的请求拦截器和错误处理
- ✅ 与其他API文件保持一致的使用方式

### 后续操作
1. 重新构建前端项目：`npm run build -- --mode production`
2. 重新部署构建产物到服务器
3. 验证地区选择功能是否正常

---

## 2026-01-08 - 修复正式环境图片上传和访问问题

### 问题描述
1. **图片上传失败**：`https://admin.quaichao.com/api/common/upload/image` 返回 405 Not Allowed
2. **图片访问失败**：上传成功后，图片无法正常访问，返回 404 Not Found

### 问题原因
1. **405错误**：`admin.quaichao.com` 和 `www.shop.quaichao.com` 的Nginx配置中缺少 `/api/` 的location块，无法将API请求代理到后端
2. **404错误**：两个站点的Nginx配置中缺少 `/uploads/` 的location块，无法提供静态文件服务

### 解决方案

#### 1. 修复Nginx配置 - 添加API反向代理
在 `admin.quaichao.com` 和 `www.shop.quaichao.com` 的Nginx配置中，在 `location /` 之前添加：

```nginx
# API反向代理（后端服务）
location /api/ {
    proxy_pass http://127.0.0.1:8081;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    
    proxy_connect_timeout 60s;
    proxy_send_timeout 60s;
    proxy_read_timeout 60s;
    
    proxy_buffering off;
    proxy_request_buffering off;
}
```

**关键点**：
- `proxy_pass` 必须**不带尾部斜杠**（`http://127.0.0.1:8081`），这样才能保留完整路径 `/api/common/upload/image`
- 如果使用 `proxy_pass http://127.0.0.1:8081/;`（带斜杠），Nginx会去掉 `/api/` 前缀，导致路径错误

#### 2. 修复Nginx配置 - 添加文件上传目录静态服务
在 `admin.quaichao.com` 和 `www.shop.quaichao.com` 的Nginx配置中，在 `location /api/` 之后、`location /` 之前添加：

```nginx
# 文件上传目录（静态文件服务）
location /uploads/ {
    alias /www/wwwroot/shopping-mall-backend-prod/uploads/;
    expires 30d;
    add_header Cache-Control "public, immutable";
    
    # 允许访问图片和文档文件
    location ~* \.(jpg|jpeg|png|gif|pdf|doc|docx|xls|xlsx)$ {
        access_log off;
    }
    
    # 禁止访问可执行文件
    location ~* \.(php|jsp|asp|aspx|sh|bat|exe)$ {
        deny all;
    }
}
```

**关键点**：
- `alias` 路径必须以 `/` 结尾
- `location /uploads/` 必须在 `location /` 之前，确保优先匹配
- 路径指向实际的上传文件存储目录：`/www/wwwroot/shopping-mall-backend-prod/uploads/`

#### 3. 配置顺序
正确的配置顺序（从上到下）：
1. `location /api/` - API反向代理
2. `location /uploads/` - 文件上传目录静态服务
3. `location /` - 前端静态文件

### 修改的文件
- `docs/完全隔离环境部署方案.md`
  - 更新了 `4.3.1 用户端站点配置（www.shop.quaichao.com）`
  - 更新了 `4.3.2 管理后台站点配置（admin.quaichao.com）`
  - 添加了 `/api/` 和 `/uploads/` 的location配置块

### 修改后的效果
- ✅ 图片上传功能正常：`POST https://admin.quaichao.com/api/common/upload/image` 返回200
- ✅ 管理后台图片可以正常访问：`https://admin.quaichao.com/uploads/images/2026/01/xxx.jpg`
- ✅ 用户端图片可以正常访问：`https://www.shop.quaichao.com/uploads/images/2026/01/xxx.jpg`
- ✅ API请求正常：两个站点都可以通过 `/api/` 访问后端服务

### 注意事项
1. **配置顺序很重要**：`location /api/` 和 `location /uploads/` 必须在 `location /` 之前
2. **proxy_pass路径**：必须不带尾部斜杠，否则会去掉location匹配的部分
3. **alias路径**：必须以 `/` 结尾，且指向实际的文件存储目录
4. **配置后需要重载Nginx**：
   ```bash
   nginx -t  # 测试配置
   nginx -s reload  # 重载配置
   ```

<<<<<<< HEAD
<<<<<<< HEAD
## 2026-01-08 - 修复正式环境订单详情页图片不显示问题

### 问题描述
正式环境（https://admin.quaichao.com）订单详情页的商品图片无法显示，但商品列表页的图片可以正常显示，用户端也可以正常显示。

### 问题原因
1. **图片URL拼接错误**：订单详情页使用了 `getImageUrl` 函数处理图片路径
2. **使用了错误的baseURL**：`getImageUrl` 函数使用 `import.meta.env.VITE_API_BASE_URL`（`https://api.quaichao.com`）来拼接图片路径
3. **路径错误**：图片路径 `/uploads/images/...` 被拼接成 `https://api.quaichao.com/uploads/images/...`，但图片实际应该通过当前域名（`https://admin.quaichao.com`）访问
4. **对比**：商品列表页直接使用 `row.mainImage`，没有调用 `getImageUrl`，所以能正常显示

### 解决方案
修改 `admin-frontend/src/views/order/List.vue` 中的 `getImageUrl` 函数：
- 将 `import.meta.env.VITE_API_BASE_URL` 改为 `window.location.origin`
- 对于以 `/` 开头的相对路径，使用当前域名而不是API服务器域名
- 确保图片资源通过当前域名访问，而不是API服务器

### 代码修改清单
1. ✅ `admin-frontend/src/views/order/List.vue` - 修改 `getImageUrl` 函数，使用 `window.location.origin` 而不是 `VITE_API_BASE_URL`

### 修改后的效果
- ✅ 订单详情页的图片可以正常显示：`https://admin.quaichao.com/uploads/images/2026/01/xxx.jpg`
- ✅ 与商品列表页和用户端的图片显示方式保持一致
- ✅ 图片路径正确：使用当前域名而不是API服务器域名

### 技术说明
- 图片资源（`/uploads/...`）应该通过当前域名访问，因为Nginx配置中 `/uploads/` 路径会提供静态文件服务
- API请求（`/api/...`）才需要通过 `VITE_API_BASE_URL` 访问后端服务器
- 使用 `window.location.origin` 可以动态获取当前域名，适配不同环境（开发/测试/生产）

## 2026-01-08 - 用户端首页广告位模块优化：动态读取广告名称并隐藏禁用/删除的广告位

### 问题描述
用户端首页的广告位模块标题（如"7F 其他情趣"）是硬编码的，需要从管理后台读取广告名称。如果管理后台禁用或删除了某个广告位，前端仍然会显示该模块。

### 需求
1. 广告位模块的标题需要从管理后台读取广告名称（`adName`）
2. 如果管理后台禁用或删除了某个广告位，前端不应该显示该模块
3. 已删除、禁用或未添加的广告位整个模块应该隐藏

### 解决方案
修改 `frontend/src/views/home/Index.vue`：
1. **动态生成楼层**：根据广告数据动态生成楼层，而不是固定7个楼层
2. **使用广告名称**：从广告数据中读取 `adName` 作为楼层标题
3. **自动过滤**：后端API `getAllFloorAdvertisements()` 已经只返回启用状态的广告，前端只需要根据返回的广告数据生成楼层
4. **配置映射**：创建广告位置与分类ID的映射配置，将广告位置（如 `floor_1`）映射到对应的分类ID和标题颜色

### 代码修改清单
1. ✅ `frontend/src/views/home/Index.vue` - 修改楼层生成逻辑
   - 移除硬编码的7个楼层组件
   - 改为使用 `v-for` 动态生成楼层
   - 根据广告数据动态生成楼层数据
   - 使用广告的 `adName` 作为楼层标题
   - 从广告位置（`adPosition`）中提取楼层编号（如 `floor_1` -> `1F`）
   - 创建 `floorConfig` 配置对象，映射广告位置到分类ID和标题颜色

### 修改后的效果
- ✅ 广告位模块标题从管理后台读取：使用广告的 `adName` 字段
- ✅ 禁用或删除的广告位自动隐藏：后端API只返回启用状态的广告，前端只显示有广告的楼层
- ✅ 动态楼层生成：根据实际启用的广告数量动态生成楼层，不再固定7个
- ✅ 配置化管理：通过 `floorConfig` 配置广告位置与分类ID的映射关系

### 技术说明
- 后端API `getAllFloorAdvertisements()` 已经实现了状态过滤（只返回 `status=1` 的广告）和时间过滤
- 前端只需要根据返回的广告数据生成楼层，如果某个广告位被禁用或删除，就不会出现在返回列表中
- 广告位置格式：`floor_1`, `floor_2`, ..., `floor_7`
- 楼层编号格式：从 `floor_1` 提取为 `1F`，从 `floor_2` 提取为 `2F`，以此类推

## 2026-01-08 - 修复广告位标题显示和管理后台广告位置选项

### 问题描述
1. 用户端首页广告位标题显示为"2F 2F楼层广告"，前面的"2F"不需要，应该只显示广告名称
2. 管理后台广告位置下拉选项缺少了楼层广告5、楼层广告6、楼层广告7

### 修复内容

#### 1. 修改CategoryFloor组件标题显示
- **文件**：`frontend/src/components/home/CategoryFloor.vue`
- **修改**：移除楼层编号（`floorNumber`），标题栏只显示广告名称（`categoryName`）
- **效果**：标题从"2F 2F楼层广告"改为"2F楼层广告"（只显示管理后台配置的广告名称）

#### 2. 管理后台添加楼层广告5、6、7选项
- **文件**：`admin-frontend/src/views/website/Advertisement.vue`
- **修改位置**：
  1. 搜索表单的广告位置下拉选项（第17-20行）
  2. 编辑表单的广告位置下拉选项（第116-119行）
  3. `getPositionText` 函数的映射（第262-265行）
- **添加内容**：
  - `<el-option label="楼层广告5" value="floor_5" />`
  - `<el-option label="楼层广告6" value="floor_6" />`
  - `<el-option label="楼层广告7" value="floor_7" />`
  - 在 `positionMap` 中添加对应的映射

### 代码修改清单
1. ✅ `frontend/src/components/home/CategoryFloor.vue` - 移除楼层编号显示，只显示广告名称
2. ✅ `admin-frontend/src/views/website/Advertisement.vue` - 添加楼层广告5、6、7选项

### 修改后的效果
- ✅ 广告位标题只显示广告名称：不再显示"2F"前缀，直接显示管理后台配置的广告名称
- ✅ 管理后台可以配置楼层广告5、6、7：下拉选项中包含所有7个楼层广告位置
- ✅ 广告位置文本显示正确：列表和详情中正确显示楼层广告5、6、7的文本
=======
=======
>>>>>>> b74a7f175bbbada1ad9299cfe8e1b196cc68ab32
---

## 2025-01-08 - 修改管理后台标题，从系统配置读取网站名称

### 修改内容
1. **创建网站配置工具函数** (`admin-frontend/src/utils/siteConfig.ts`)
   - 新增 `getSiteName()` 函数：从系统配置获取网站名称，支持缓存
   - 新增 `getPageTitle()` 函数：生成完整页面标题（页面名称 + 网站名称 + 管理后台）
   - 新增 `clearSiteNameCache()` 函数：清除缓存

2. **修改Layout组件** (`admin-frontend/src/components/Layout/index.vue`)
   - 添加 `siteName` 响应式变量，从系统配置读取 `site.name`
   - 添加 `pageTitle` 计算属性，格式为：`${siteName}管理后台`
   - 添加 `loadSiteConfig()` 函数，在组件挂载时加载系统配置
   - 修改页面标题显示，从硬编码改为动态显示

3. **修改路由守卫** (`admin-frontend/src/router/index.ts`)
   - 导入 `getPageTitle` 工具函数
   - 修改路由守卫中的标题设置逻辑，使用动态获取的网站名称
   - 支持有页面标题和无页面标题两种情况

4. **修改HTML默认标题** (`admin-frontend/index.html`)
   - 将默认标题从 "B2B成人用品采购平台 - 管理后台" 改为 "管理后台"
   - 实际标题会在应用启动后从系统配置动态更新

### 功能说明
- 管理后台标题格式：`网站名称管理后台`（例如："趣爱巢商城管理后台"）
- 带页面标题时格式：`页面标题 - 网站名称管理后台`
- 网站名称从系统配置表 `system_config` 的 `site.name` 配置项读取
- 支持缓存机制，避免重复请求

### 相关文件
- `admin-frontend/src/utils/siteConfig.ts` (新建)
- `admin-frontend/src/components/Layout/index.vue`
- `admin-frontend/src/router/index.ts`
- `admin-frontend/index.html`

---

## 2025-01-08 - 批量导入商品增加品牌字段校验

### 修改内容
**修改批量导入服务** (`backend/src/main/java/com/shoppingmall/service/product/impl/ProductImportServiceImpl.java`)
- 在 `importSingleProduct` 方法中，增加品牌名称存在性校验
- 如果导入数据中填写了品牌名称，则必须验证该品牌在系统中存在
- 如果品牌不存在，抛出异常："品牌名称不存在: {品牌名称}"
- 如果品牌存在但已禁用，抛出异常："品牌已禁用: {品牌名称}"
- 如果品牌名称为空，则不进行校验（品牌为可选字段）

### 功能说明
- 品牌字段为可选字段，但如果填写了品牌名称，则必须存在且启用
- 校验失败时，会在导入结果中显示错误信息，包含行号和商品编码
- 确保导入的商品数据中品牌信息的准确性

### 相关文件
- `backend/src/main/java/com/shoppingmall/service/product/impl/ProductImportServiceImpl.java`

---

## 2025-01-08 - 批量导入商品增加200条数量限制

### 修改内容
1. **后端添加数量限制校验** (`backend/src/main/java/com/shoppingmall/service/product/impl/ProductImportServiceImpl.java`)
   - 添加常量 `MAX_IMPORT_COUNT = 200`，定义单次最大导入数量
   - 在解析文件后、导入前进行数量校验
   - 如果超过200条，抛出异常："单次导入商品数量不能超过 200 条，当前数量: {数量} 条，请分批导入"

2. **前端导入界面优化** (`admin-frontend/src/views/product/ProductManage.vue`)
   - 在导入对话框顶部添加信息提示框（el-alert），显示导入说明：
     - 单次最多导入 **200条** 商品数据
     - 如果数据超过200条，请分批导入
     - 支持 CSV 或 Excel (.xlsx/.xls) 格式
   - 在文件选择时（handleCsvChange）添加前端预校验：
     - 对于CSV文件，读取文件内容并计算行数
     - 如果数据行数超过200条，显示警告并移除文件
     - Excel文件由后端校验（因为前端解析Excel较复杂）

### 功能说明
- **数量限制**：单次最多导入200条商品数据
- **双重校验**：前端预校验（CSV文件）+ 后端严格校验（所有文件）
- **用户提示**：
  - 导入界面顶部显示醒目的引导语
  - 超过限制时，前端和后端都会给出明确的错误提示
  - 提示用户分批导入

### 相关文件
- `backend/src/main/java/com/shoppingmall/service/product/impl/ProductImportServiceImpl.java`
- `admin-frontend/src/views/product/ProductManage.vue`

---

## 2025-01-08 - 修复批量导入商品预警库存字段映射问题

### 修改内容
**修复商品创建服务** (`backend/src/main/java/com/shoppingmall/service/product/impl/ProductServiceImpl.java`)
- 在 `BeanUtils.copyProperties` 的排除字段列表中添加 `"warningStock"`
- 确保 `warningStock`（预警库存/警戒库存）字段通过手动设置逻辑正确映射
- 修复原因：`BeanUtils.copyProperties` 可能已经复制了该字段，但后续手动设置逻辑需要确保正确映射

### 功能说明
- 批量导入时，"预警库存"字段的数据现在会正确映射到商品的"警戒库存"字段
- 如果导入数据中预警库存为空，则设置为默认值0
- 确保导入的商品数据中预警库存信息能够正确保存

### 相关文件
- `backend/src/main/java/com/shoppingmall/service/product/impl/ProductServiceImpl.java`

---

## 2025-01-08 - 修复批量导入商品警戒库存默认值问题

### 问题描述
批量导入商品后，所有商品的警戒库存都显示为10，而不是导入数据中的值。

### 问题原因
在 `StockServiceImpl.updateProductTotalStock` 方法中，创建库存记录时硬编码了默认预警阈值为10，没有从商品表读取 `warningStock` 字段的值。

### 修改内容
**修改库存服务** (`backend/src/main/java/com/shoppingmall/service/admin/impl/StockServiceImpl.java`)
- 在 `updateProductTotalStock` 方法中，添加从商品表读取 `warningStock` 的逻辑
- 创建库存记录时，优先使用商品表的 `warningStock` 值
- 如果商品表中没有设置警戒库存，才使用默认值10
- 更新库存记录时，如果商品表的警戒库存有更新，同步更新库存表的预警阈值
- 移除了不必要的反向同步逻辑（因为已经从商品表读取了值）

### 功能说明
- 批量导入商品时，导入的"预警库存"字段数据现在会正确映射到：
  1. 商品表的 `warning_stock` 字段（警戒库存）
  2. 库存表的 `warning_threshold` 字段（预警阈值）
- 确保导入的商品数据中预警库存信息能够正确保存和使用

### 相关文件
- `backend/src/main/java/com/shoppingmall/service/admin/impl/StockServiceImpl.java`

---

## 2025-01-08 - 商品列表增加状态标签tab页

### 修改内容
1. **后端支持查询已删除商品** (`backend/src/main/java/com/shoppingmall/service/product/`)
   - 修改 `ProductService` 接口，添加 `includeDeleted` 参数
   - 修改 `ProductServiceImpl.getProductPage` 方法：
     - 添加 `includeDeleted` 参数处理逻辑
     - 当 `includeDeleted=true` 时，手动添加 `deleted=1` 条件，绕过 MyBatis-Plus 的 `@TableLogic` 自动过滤
     - 已删除的商品不进行状态筛选
   - 修改 `ProductController.getProductPage` 方法，添加 `includeDeleted` 参数

2. **前端添加状态标签tab页** (`admin-frontend/src/views/product/ProductManage.vue`)
   - 添加商品状态标签页配置：全部、已上架、已下架、草稿、已删除（5个tab）
   - 添加 `activeTab` 响应式变量，管理当前激活的tab
   - 添加 `handleTabChange` 函数，处理tab切换逻辑
   - 修改 `loadProductList` 函数，根据当前tab传递相应的查询参数
   - 修改 `handleSearch` 和 `handleReset` 函数，同步tab状态
   - 添加 `watch` 监听器，监听搜索表单状态变化，同步到tab
   - 在搜索栏下方添加tab页UI组件
   - 添加tab页样式（参考订单列表的实现）
   - 修改状态列显示，已删除tab页显示"已删除"标签

3. **前端API修改** (`admin-frontend/src/api/admin/product.ts`)
   - 修改 `getProductPage` 函数，添加 `includeDeleted` 参数

### 功能说明
- **5个状态标签页**：
  - 全部：显示所有未删除的商品
  - 已上架：显示状态为"上架"的商品
  - 已下架：显示状态为"下架"的商品
  - 草稿：显示状态为"草稿"的商品
  - 已删除：显示已逻辑删除的商品（deleted=1）
- **Tab与搜索表单同步**：
  - Tab切换时，自动更新搜索表单的状态字段
  - 搜索表单状态变化时，自动同步到对应的tab
  - 重置时，重置tab为"全部"
- **已删除商品特殊处理**：
  - 查询已删除商品时，使用 `includeDeleted=true` 参数
  - 已删除商品不进行状态筛选
  - 已删除商品在列表中显示"已删除"标签

### 技术要点
- 使用 MyBatis-Plus 的 `@TableLogic` 注解实现逻辑删除
- 查询已删除商品时，需要手动添加 `deleted=1` 条件，绕过自动过滤
- Tab页UI样式参考订单列表的实现，保持一致的用户体验

### 相关文件
- `backend/src/main/java/com/shoppingmall/service/product/ProductService.java`
- `backend/src/main/java/com/shoppingmall/service/product/impl/ProductServiceImpl.java`
- `backend/src/main/java/com/shoppingmall/controller/admin/ProductController.java`
- `backend/src/main/java/com/shoppingmall/controller/buyer/ProductController.java`
- `admin-frontend/src/api/admin/product.ts`
- `admin-frontend/src/views/product/ProductManage.vue`

---

## 2025-01-08 - 修复买家端ProductController编译错误

### 问题描述
修改 `ProductService.getProductPage` 方法添加 `includeDeleted` 参数后，买家端 `ProductController` 调用该方法时参数不匹配，导致编译错误。

### 修改内容
**修复买家端Controller** (`backend/src/main/java/com/shoppingmall/controller/buyer/ProductController.java`)
- 在调用 `getProductPage` 方法时添加 `includeDeleted` 参数，传入 `false`
- 买家端不应该看到已删除的商品，所以传入 `false`

### 相关文件
- `backend/src/main/java/com/shoppingmall/controller/buyer/ProductController.java`

---

## 2025-01-08 - 移除商品列表"已删除"标签页

### 问题描述
由于 MyBatis-Plus 的 `@TableLogic` 注解会自动在查询时添加 `deleted=0` 条件，要查询已删除的商品（`deleted=1`）需要绕过这个自动过滤机制，实现较为复杂。为了简化实现，决定移除"已删除"标签页。

### 修改内容
1. **前端移除"已删除"标签页** (`admin-frontend/src/views/product/ProductManage.vue`)
   - 从 `productTabs` 配置中移除"已删除"选项
   - 简化 `handleTabChange` 函数，移除已删除相关的逻辑
   - 简化 `loadProductList` 函数，移除 `includeDeleted` 参数处理
   - 简化 `handleSearch` 和 `watch` 监听器，移除已删除相关逻辑
   - 移除状态列中已删除标签的特殊显示

2. **前端API简化** (`admin-frontend/src/api/admin/product.ts`)
   - 移除 `getProductPage` 函数的 `includeDeleted` 参数

3. **后端简化** (`backend/src/main/java/com/shoppingmall/controller/admin/ProductController.java`)
   - 移除 `getProductPage` 方法的 `includeDeleted` 参数
   - 固定传入 `includeDeleted=false`

4. **后端Service简化** (`backend/src/main/java/com/shoppingmall/service/product/impl/ProductServiceImpl.java`)
   - 移除 `includeDeleted` 参数的处理逻辑
   - 简化查询条件，依赖 MyBatis-Plus 的 `@TableLogic` 自动过滤

### 功能说明
- 商品列表现在只显示 4 个状态标签页：全部、已上架、已下架、草稿
- 已删除的商品不会在列表中显示（由 MyBatis-Plus 的 `@TableLogic` 自动过滤）
- 代码更简洁，维护更容易

### 相关文件
- `admin-frontend/src/views/product/ProductManage.vue`
- `admin-frontend/src/api/admin/product.ts`
- `backend/src/main/java/com/shoppingmall/controller/admin/ProductController.java`
- `backend/src/main/java/com/shoppingmall/service/product/impl/ProductServiceImpl.java`

---

## 2025-01-08 - 用户端商品详情页添加商品状态验证

### 需求
用户通过修改产品ID的方式访问产品详情页时，需要判断商品状态：
- 如果商品是下架状态，提示"商品已下架"
- 如果商品是草稿或已删除状态，提示"商品不存在"
- 只有上架状态的商品才能正常访问

### 修改内容

1. **前端商品详情页** (`frontend/src/views/products/Detail.vue`)
   - 添加 `productStatusError` 响应式变量，用于存储商品状态错误信息
   - 在 `loadProductDetail` 函数中添加商品状态检查逻辑
   - 获取商品详情后，检查 `productData.status` 字段
   - 如果状态不是"上架"：
     - 状态为"下架"时，设置错误类型为 `offline`，提示"商品已下架"
     - 状态为"草稿"或其他时，设置错误类型为 `notfound`，提示"商品不存在"
   - 添加商品状态错误提示UI组件，显示相应的错误信息和操作按钮

### 功能说明
- **状态验证**：用户访问商品详情页时，自动检查商品状态
- **错误提示**：
  - 下架商品：显示"商品已下架"提示，说明商品暂时无法购买
  - 草稿/已删除商品：显示"商品不存在"提示
- **用户体验**：提供返回首页和浏览商品的操作按钮，方便用户导航

### 技术要点
- 商品状态定义：
  - `0` = 下架
  - `1` = 上架
  - `2` = 草稿
  - `deleted = 1` = 已删除（MyBatis-Plus 自动过滤，不会返回）
- 前端通过检查 `ProductVO.status` 字段判断商品状态
- 复用现有的 `product-not-found` 样式，保持UI一致性

### 相关文件
- `frontend/src/views/products/Detail.vue`
>>>>>>> cc8e7415c71a5bce6695505e0488a5a650c027ef

---

## 2026-01-08 22:17:11 - 统一系统文案：将"B2B成人用品采购平台"改为"趣爱巢商城"

### 修改内容
统一替换系统中所有"B2B成人用品采购平台"相关文案为"趣爱巢商城"

### 修改文件列表

#### 前端代码文件
- `frontend/src/components/Layout/index.vue` - 页面标题
- `frontend/index.html` - HTML页面标题
- `frontend/src/router/index.ts` - 路由页面标题后缀
- `frontend/package.json` - 项目描述
- `frontend/README.md` - README文档标题

#### 管理后台代码文件
- `admin-frontend/src/main.ts` - 启动日志
- `admin-frontend/package.json` - 项目描述
- `admin-frontend/README.md` - README文档标题

#### 后端代码文件
- `backend/pom.xml` - Maven项目名称和描述
- `backend/src/main/java/com/shoppingmall/ShoppingMallApplication.java` - 启动类注释
- `backend/src/main/java/com/shoppingmall/common/config/SwaggerConfig.java` - Swagger API文档标题和描述

#### 配置文件
- `config/nginx/nginx.conf` - Nginx配置注释

#### 文档文件
- `docs/第三方支付对接技术方案.md` - 系统名称
- `docs/Requirements AnalysisV1.0.md` - 文档标题和系统类型
- `docs/Development Task List.md` - 文档标题和项目名称
- `docs/Development TODO List(backup).md` - 文档标题和项目名称
- `docs/Technical Architecture Design.md` - 文档标题、系统名称和总结部分

### 修改说明
- 所有用户可见的文案统一改为"趣爱巢商城"
- 保持代码注释和文档的一致性
- 数据库SQL备份文件中的历史数据保持不变（仅修改代码和文档）

### 相关文件
- 所有包含"B2B成人用品采购平台"的代码和文档文件

---

## 2026-01-08 22:20:00 - 修改首页欢迎文案：将"云起分销王商城"改为"趣爱巢商城"

### 修改内容
将首页顶部欢迎栏中的默认网站名称从"云起分销王商城"改为"趣爱巢商城"

### 修改文件列表
- `frontend/src/components/home/TopBar.vue` - 修改默认网站名称常量

### 修改说明
- 修改了 `siteName` 的默认值从 `'云起分销王商城'` 改为 `'趣爱巢商城'`
- 页面显示的欢迎语会从"亲，欢迎光临云起分销王商城！"变为"亲，欢迎光临趣爱巢商城！"
- 如果系统配置中有 `site.name` 配置，会优先使用配置值（可通过后台系统配置管理修改）

### 相关文件
- `frontend/src/components/home/TopBar.vue`
<<<<<<< HEAD
=======
=======
## 2026-01-08 - 修复正式环境订单详情页图片不显示问题

### 问题描述
正式环境（https://admin.quaichao.com）订单详情页的商品图片无法显示，但商品列表页的图片可以正常显示，用户端也可以正常显示。

### 问题原因
1. **图片URL拼接错误**：订单详情页使用了 `getImageUrl` 函数处理图片路径
2. **使用了错误的baseURL**：`getImageUrl` 函数使用 `import.meta.env.VITE_API_BASE_URL`（`https://api.quaichao.com`）来拼接图片路径
3. **路径错误**：图片路径 `/uploads/images/...` 被拼接成 `https://api.quaichao.com/uploads/images/...`，但图片实际应该通过当前域名（`https://admin.quaichao.com`）访问
4. **对比**：商品列表页直接使用 `row.mainImage`，没有调用 `getImageUrl`，所以能正常显示

### 解决方案
修改 `admin-frontend/src/views/order/List.vue` 中的 `getImageUrl` 函数：
- 将 `import.meta.env.VITE_API_BASE_URL` 改为 `window.location.origin`
- 对于以 `/` 开头的相对路径，使用当前域名而不是API服务器域名
- 确保图片资源通过当前域名访问，而不是API服务器

### 代码修改清单
1. ✅ `admin-frontend/src/views/order/List.vue` - 修改 `getImageUrl` 函数，使用 `window.location.origin` 而不是 `VITE_API_BASE_URL`

### 修改后的效果
- ✅ 订单详情页的图片可以正常显示：`https://admin.quaichao.com/uploads/images/2026/01/xxx.jpg`
- ✅ 与商品列表页和用户端的图片显示方式保持一致
- ✅ 图片路径正确：使用当前域名而不是API服务器域名

### 技术说明
- 图片资源（`/uploads/...`）应该通过当前域名访问，因为Nginx配置中 `/uploads/` 路径会提供静态文件服务
- API请求（`/api/...`）才需要通过 `VITE_API_BASE_URL` 访问后端服务器
- 使用 `window.location.origin` 可以动态获取当前域名，适配不同环境（开发/测试/生产）

## 2026-01-08 - 用户端首页广告位模块优化：动态读取广告名称并隐藏禁用/删除的广告位

### 问题描述
用户端首页的广告位模块标题（如"7F 其他情趣"）是硬编码的，需要从管理后台读取广告名称。如果管理后台禁用或删除了某个广告位，前端仍然会显示该模块。

### 需求
1. 广告位模块的标题需要从管理后台读取广告名称（`adName`）
2. 如果管理后台禁用或删除了某个广告位，前端不应该显示该模块
3. 已删除、禁用或未添加的广告位整个模块应该隐藏

### 解决方案
修改 `frontend/src/views/home/Index.vue`：
1. **动态生成楼层**：根据广告数据动态生成楼层，而不是固定7个楼层
2. **使用广告名称**：从广告数据中读取 `adName` 作为楼层标题
3. **自动过滤**：后端API `getAllFloorAdvertisements()` 已经只返回启用状态的广告，前端只需要根据返回的广告数据生成楼层
4. **配置映射**：创建广告位置与分类ID的映射配置，将广告位置（如 `floor_1`）映射到对应的分类ID和标题颜色

### 代码修改清单
1. ✅ `frontend/src/views/home/Index.vue` - 修改楼层生成逻辑
   - 移除硬编码的7个楼层组件
   - 改为使用 `v-for` 动态生成楼层
   - 根据广告数据动态生成楼层数据
   - 使用广告的 `adName` 作为楼层标题
   - 从广告位置（`adPosition`）中提取楼层编号（如 `floor_1` -> `1F`）
   - 创建 `floorConfig` 配置对象，映射广告位置到分类ID和标题颜色

### 修改后的效果
- ✅ 广告位模块标题从管理后台读取：使用广告的 `adName` 字段
- ✅ 禁用或删除的广告位自动隐藏：后端API只返回启用状态的广告，前端只显示有广告的楼层
- ✅ 动态楼层生成：根据实际启用的广告数量动态生成楼层，不再固定7个
- ✅ 配置化管理：通过 `floorConfig` 配置广告位置与分类ID的映射关系

### 技术说明
- 后端API `getAllFloorAdvertisements()` 已经实现了状态过滤（只返回 `status=1` 的广告）和时间过滤
- 前端只需要根据返回的广告数据生成楼层，如果某个广告位被禁用或删除，就不会出现在返回列表中
- 广告位置格式：`floor_1`, `floor_2`, ..., `floor_7`
- 楼层编号格式：从 `floor_1` 提取为 `1F`，从 `floor_2` 提取为 `2F`，以此类推

## 2026-01-08 - 修复广告位标题显示和管理后台广告位置选项

### 问题描述
1. 用户端首页广告位标题显示为"2F 2F楼层广告"，前面的"2F"不需要，应该只显示广告名称
2. 管理后台广告位置下拉选项缺少了楼层广告5、楼层广告6、楼层广告7

### 修复内容

#### 1. 修改CategoryFloor组件标题显示
- **文件**：`frontend/src/components/home/CategoryFloor.vue`
- **修改**：移除楼层编号（`floorNumber`），标题栏只显示广告名称（`categoryName`）
- **效果**：标题从"2F 2F楼层广告"改为"2F楼层广告"（只显示管理后台配置的广告名称）

#### 2. 管理后台添加楼层广告5、6、7选项
- **文件**：`admin-frontend/src/views/website/Advertisement.vue`
- **修改位置**：
  1. 搜索表单的广告位置下拉选项（第17-20行）
  2. 编辑表单的广告位置下拉选项（第116-119行）
  3. `getPositionText` 函数的映射（第262-265行）
- **添加内容**：
  - `<el-option label="楼层广告5" value="floor_5" />`
  - `<el-option label="楼层广告6" value="floor_6" />`
  - `<el-option label="楼层广告7" value="floor_7" />`
  - 在 `positionMap` 中添加对应的映射

### 代码修改清单
1. ✅ `frontend/src/components/home/CategoryFloor.vue` - 移除楼层编号显示，只显示广告名称
2. ✅ `admin-frontend/src/views/website/Advertisement.vue` - 添加楼层广告5、6、7选项

### 修改后的效果
- ✅ 广告位标题只显示广告名称：不再显示"2F"前缀，直接显示管理后台配置的广告名称
- ✅ 管理后台可以配置楼层广告5、6、7：下拉选项中包含所有7个楼层广告位置
- ✅ 广告位置文本显示正确：列表和详情中正确显示楼层广告5、6、7的文本
>>>>>>> 745ac937db43a34c8711f309399dc6e7f2b2bd98
>>>>>>> b74a7f175bbbada1ad9299cfe8e1b196cc68ab32

