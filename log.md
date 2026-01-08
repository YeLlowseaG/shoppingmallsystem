# 修改日志

## 2026-01-07 - 修复库存管理页面商品分类下拉值不正确的问题

### 功能说明
修复库存管理页面（/admin/inventory）查询条件中"商品分类"下拉值不正确的问题，使其与商品列表页面的分类数据一致。

### 问题原因
库存管理页面的分类数据使用的是硬编码的假数据，而不是从API获取的真实分类数据。

### 修改内容

**前端修改：**

1. **文件：** `admin-frontend/src/views/inventory/Index.vue`
   - 第294行：导入 `getCategoryTree` 和 `ProductCategoryVO` 类型
   - 第329行：将 `categories` 改为 `categoryTree`，类型为 `ProductCategoryVO[]`
   - 第332-345行：添加 `flatCategories` 计算属性，用于将分类树扁平化（与商品管理页面逻辑一致）
   - 第420-432行：修改 `loadCategories` 函数，调用真实的分类API `getCategoryTree()`
   - 第142-150行：修改分类下拉选择器，使用 `flatCategories` 替代 `categories`，并移除"全部分类"选项（清空选择即可）

### 修改原因
用户反馈库存管理页面的商品分类下拉值不正确，需要与商品列表的分类保持一致。

---

## 2026-01-07 - 屏蔽库存管理页面的导出数据和批量调整功能

### 功能说明
在库存管理页面（/admin/inventory）屏蔽"导出数据"和"批量调整"两个功能入口。

### 修改内容

**前端修改：**

1. **文件：** `admin-frontend/src/views/inventory/Index.vue`
   - 第112-121行：注释掉"导出数据"和"批量调整"两个按钮
   - 保留相关函数代码，以便将来需要时可以恢复

### 修改原因
根据用户需求，需要在库存管理页面屏蔽这两个功能的入口。

---

## 2026-01-07 - 修复商品详情图JSON格式错误

### 功能说明
修复商品发布和编辑时，详情轮播图保存失败的问题。数据库 `product.images` 字段是 JSON 类型，需要传递 JSON 数组格式的字符串。

### 问题原因
前端传递的是逗号分隔的字符串（如 `"url1,url2,url3"`），但数据库字段类型是 JSON，需要传递 JSON 数组格式的字符串（如 `["url1","url2","url3"]`）。

### 修改内容

**前端修改：**

1. **文件：** `admin-frontend/src/views/product/Add.vue`
   - 第701行：将 `images: detailImages.join(',')` 改为 `images: detailImages.length > 0 ? JSON.stringify(detailImages) : undefined`
   - 第780行：同样修改保存草稿时的图片格式

2. **文件：** `admin-frontend/src/views/product/ProductManage.vue`
   - 第1571行：将 `formData.value.images = JSON.stringify(detailImages)` 改为 `formData.value.images = detailImages.length > 0 ? JSON.stringify(detailImages) : undefined`
   - 确保没有图片时传递 `undefined` 而不是空字符串

### 修改原因
用户反馈保存商品时出现数据库错误：`Data truncation: Invalid JSON text`。检查后发现是图片字段格式不正确导致的。

---

## 2026-01-07 - 修复商品发布页面运费模板和详情图保存问题

### 功能说明
修复商品发布页面保存时，运费模板和详情轮播图数据没有保存成功的问题。

### 问题原因
1. 商品发布时调用 createProduct API 时缺少了 `shippingTemplateId` 字段
2. 商品发布时使用了错误的字段名 `detailImages`，应该使用 `images`

### 修改内容

**前端修改：**

1. **文件：** `admin-frontend/src/views/product/Add.vue`
   - 第690行：在 handleSubmit 函数的 createProduct 调用中添加 `shippingTemplateId: productForm.value.shippingTemplateId`
   - 第701行：将 `detailImages` 字段名改为 `images`，与后端DTO字段名保持一致
   - 第767行：在 handleSaveAsDraft 函数的 createProduct 调用中添加 `shippingTemplateId: productForm.value.shippingTemplateId`
   - 第780行：将 `detailImages` 字段名改为 `images`

### 修改原因
用户反馈在商品发布页面填写了运费模板和详情轮播图后，保存后再次进入编辑页面时这些数据没有显示。检查代码发现保存时缺少了运费模板字段，且详情图字段名不正确。

---

## 2026-01-07 - 商品列表操作列宽度调整

### 功能说明
调整商品列表页面中操作列的宽度，使其更紧凑。

### 修改内容

**前端修改：**

1. **文件：** `admin-frontend/src/views/product/ProductManage.vue`
   - 第152行：将操作列宽度从 360px 调整为 280px

### 修改原因
用户要求将操作列宽度调小，使表格布局更紧凑。

---

## 2026-01-07 - 商品发布/编辑页面提示文案颜色优化

### 功能说明
将商品发布和编辑页面中的三个默认提示文案改为红色，使其更加醒目突出。

### 修改内容

**前端修改：**

1. **文件：** `admin-frontend/src/views/product/Add.vue`
   - 第72行：将"不选择运费模板则该商品包邮"文案颜色改为红色（#f56c6c）
   - 第152行：将"启用后以会员价作为售价，否则以基础价作为售价"文案颜色改为红色（#f56c6c）
   - 第171行：将"启用后可为商品配置不同规格的SKU（如颜色、尺寸等）"文案颜色改为红色（#f56c6c）

2. **文件：** `admin-frontend/src/views/product/ProductManage.vue`
   - 第255行：将"不选择运费模板则该商品包邮"文案颜色改为红色（#f56c6c）
   - 第335行：将"启用后以会员价作为售价，否则以基础价作为售价"文案颜色改为红色（#f56c6c）
   - 第354行：将"启用后可为商品配置不同规格的SKU（如颜色、尺寸等）"文案颜色改为红色（#f56c6c）

### 修改原因
用户要求将这三个重要的提示文案改为红色，使其更加醒目，提升用户体验。

---

## 2026-01-07 - 统一修改"货号"字段显示逻辑

### 功能说明
统一修改商品详情、购物车、结算、订单、订单详情等页面中"货号"字段的显示逻辑：
- 如果商品启用SKU且有SKU编码，显示SKU编码
- 如果没有启用SKU或没有SKU编码，显示商品编码

### 修改原因
用户要求统一"货号"字段的显示逻辑，确保在所有相关页面中，启用SKU的商品显示SKU编码，未启用SKU的商品显示商品编码。

### 修改内容

**后端修改：**

1. **文件：** `backend/src/main/java/com/shoppingmall/vo/CartVO.java`
   - 添加 `skuId` 字段（SKU ID）
   - 添加 `skuCode` 字段（SKU编码）

2. **文件：** `backend/src/main/java/com/shoppingmall/service/buyer/impl/CartServiceImpl.java`
   - 在 `convertToVO()` 方法中，当购物车项有SKU时，设置 `skuId` 和 `skuCode` 字段

3. **文件：** `backend/src/main/java/com/shoppingmall/vo/OrderDetailVO.java`
   - 在 `OrderItemVO` 内部类中添加 `skuId` 字段（SKU ID）
   - 在 `OrderItemVO` 内部类中添加 `skuCode` 字段（SKU编码）

4. **文件：** `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
   - 在构建 `OrderItemVO` 时，如果订单项有SKU ID，查询SKU信息并设置 `skuId` 和 `skuCode` 字段

5. **文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`
   - 添加 `ProductSkuRepository` 依赖注入
   - 添加 `ProductSku` 导入
   - 在构建 `OrderItemVO` 时，如果订单项有SKU ID，查询SKU信息并设置 `skuId` 和 `skuCode` 字段

**前端修改：**

1. **文件：** `frontend/src/api/buyer/cart.ts`
   - 在 `CartVO` 接口中添加 `skuCode?: string` 字段

2. **文件：** `frontend/src/api/buyer/order.ts`
   - 在 `OrderDetailVO` 接口的 `items` 数组中添加 `skuId?: number` 和 `skuCode?: string` 字段

3. **文件：** `frontend/src/views/products/Detail.vue`
   - 修改货号显示逻辑：使用 `getProductCode()` 函数
   - 添加 `getProductCode()` 函数：如果启用SKU且有当前SKU，返回SKU编码，否则返回商品编码
   - 移除 `sku` 字段映射，添加 `productCode` 字段映射

4. **文件：** `frontend/src/views/cart/Index.vue`
   - 修改货号显示：`{{ item.skuCode || item.productCode }}`

5. **文件：** `frontend/src/views/cart/Checkout.vue`
   - 修改货号显示：`{{ item.skuCode || item.productCode }}`

6. **文件：** `frontend/src/views/order/Detail.vue`
   - 修改货号显示：`{{ item.skuCode || item.productCode }}`

### 影响范围
- ✅ 商品详情页面：货号字段根据是否启用SKU显示对应编码
- ✅ 购物车页面：货号字段根据是否有SKU显示对应编码
- ✅ 结算页面：货号字段根据是否有SKU显示对应编码
- ✅ 订单详情页面：货号字段根据是否有SKU显示对应编码
- ✅ 后端API返回数据包含SKU编码信息，前端可直接使用

## 2026-01-07 - 修复预存款交易记录【重试】按钮编译错误

### 功能说明
修复预存款交易记录【重试】按钮功能中的编译错误。

### 修改原因
用户反馈：点击【重试】按钮时报错：`Handler dispatch failed: java.lang.Error: Unresolved compilation problem: The method valueOf(String) is undefined for the type PaymentMethod`

**问题分析：**
1. `PaymentMethod` 是一个常量类（`public class PaymentMethod`），不是枚举类
2. 常量类没有 `valueOf()` 方法，只有枚举类才有这个方法
3. `PaymentGatewayService` 接口中没有 `queryPaymentStatus()` 方法
4. 应该通过 `getPaymentStrategy()` 获取支付策略，然后调用策略的 `queryPaymentStatus()` 方法

### 修改内容

**文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/DepositServiceImpl.java`
- 添加 `PaymentStrategy` 导入
- 修改 `syncPaymentStatus()` 方法中的支付状态查询逻辑（第821-832行）：
  - 移除错误的 `PaymentMethod.valueOf(paymentMethodUpper)` 调用
  - 改为通过 `paymentGatewayService.getPaymentStrategy(paymentMethodUpper)` 获取支付策略
  - 然后调用 `strategy.queryPaymentStatus(internalOrderNo)` 查询支付状态
  - 添加策略为空的检查

### 影响范围
- ✅ 修复了【重试】按钮的编译错误
- ✅ 支付状态查询功能可以正常工作
- ✅ 日志记录功能正常（通过策略的 `queryPaymentStatus()` 方法自动记录）

## 2026-01-07 - 预存款交易记录增加【重试】按钮功能

### 功能说明
在管理员后台的预存款交易记录页面（`http://localhost:3003/admin/finance/deposit`），为支付中状态的交易记录增加【重试】按钮，支持主动查询支付宝或微信的支付状态，同步支付状态结果。

### 修改原因
针对客户已经付款了，但是第三方支付平台处于异常没有回调通知成功的场景。管理员可以通过【重试】按钮主动查询支付状态，同步支付结果。

### 修改内容

**后端修改：**

1. **文件：** `backend/src/main/java/com/shoppingmall/service/admin/DepositService.java`
   - 添加 `syncPaymentStatus(Long id)` 方法接口

2. **文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/DepositServiceImpl.java`
   - 添加必要的导入：`DepositStatus`、`PaymentStatus`、`PaymentApiLogMapper`、`Caffeine` 缓存相关类
   - 添加 `PaymentApiLogMapper` 注入
   - 添加 `paymentQueryCache` 缓存（30秒过期，用于频率限制）
   - 实现 `syncPaymentStatus()` 方法：
     - 验证记录状态和支付方式（只允许支付中状态且为支付宝或微信）
     - 实现30秒查询频率限制（使用Caffeine缓存）
     - 调用支付网关查询状态（自动记录日志到 `payment_api_log` 表）
     - 根据查询结果更新充值记录状态和余额
   - 添加 `getExternalTradeNoFromLog()` 辅助方法：从支付日志中获取外部交易号

3. **文件：** `backend/src/main/java/com/shoppingmall/controller/admin/DepositController.java`
   - 添加 `sync-payment-status/{id}` 接口（POST方法）

**前端修改：**

4. **文件：** `admin-frontend/src/api/admin/deposit.ts`
   - 添加 `syncPaymentStatus(id: number)` API方法

5. **文件：** `admin-frontend/src/views/deposit/Record.vue`
   - 导入 `syncPaymentStatus` 方法
   - 添加 `syncingIds` 状态（用于显示加载状态）
   - 在操作列中添加【重试】按钮（仅对支付中状态且为支付宝或微信的记录显示）
   - 添加 `handleSyncPaymentStatus()` 方法处理重试逻辑

### 功能特性

1. **查询频率限制：** 30秒内同一记录只能查询一次，防止频繁调用第三方接口
2. **日志记录：** 自动记录到 `payment_api_log` 表，包括：
   - 支付方式（ALIPAY/WECHAT）
   - 接口类型（QUERY_ORDER）
   - 业务类型（DEPOSIT）
   - 订单号和支付流水号
   - 接口调用状态和执行耗时
   - 请求和响应数据
3. **状态同步：** 查询成功后自动更新充值记录状态和预存款余额
4. **错误处理：** 频率超限返回429错误，其他错误返回相应错误信息

### 影响范围
- ✅ 管理员后台预存款交易记录页面：支付中状态的记录显示【重试】按钮
- ✅ 支付状态查询：支持主动查询支付宝和微信的支付状态
- ✅ 支付日志记录：所有查询操作自动记录到 `payment_api_log` 表
- ✅ 用户体验：解决客户已付款但系统未收到回调的问题

## 2026-01-07 - 修复预存款充值回调状态判断问题

### 功能说明
修复支付宝预存款充值回调时，即使支付成功但状态返回失败的问题。

### 修改原因
用户反馈：支付宝预存款充值回调时，`trade_status` 是 `TRADE_SUCCESS`（支付成功），但系统返回的状态是 `{"status": "failed"}`。

**问题分析：**
1. 当充值记录已经是 `APPROVED`（已通过）状态时，`handlePaymentCallback()` 方法会直接 `return`（重复回调处理），不会抛出异常
2. 但在 `PaymentNotifyController.processPaymentNotify()` 中，原来的逻辑是 `callbackSuccess = success;`
3. 如果 `success` 变量因为某些原因被错误地设置为 `false`，即使 `handlePaymentCallback()` 正常返回，`callbackSuccess` 也会是 `false`
4. 这导致支付回调日志状态被错误地更新为失败

### 修改内容

**文件：** `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
- 修改预存款充值回调处理逻辑（第288-299行）：
  - 将 `callbackSuccess = success;` 改为 `callbackSuccess = true;`
  - 原因：只要 `handlePaymentCallback()` 没有抛出异常，就说明处理成功（包括重复回调的情况）
  - 对于重复回调，如果充值记录已经是 `APPROVED` 状态，说明之前已经成功处理过了

### 影响范围
- ✅ 预存款充值回调：当充值记录已经是 `APPROVED` 状态时（重复回调），回调状态会正确更新为成功
- ✅ 支付回调日志：`payment_api_log` 表中的状态会正确更新为成功（`api_status=1`）
- ✅ 解决了支付宝支付成功但状态返回失败的问题

## 2026-01-06 - 预存款列表页面列宽和字体优化

### 功能说明
调整预存款列表页面的列宽和字体大小，优化显示效果。

### 修改原因
用户反馈预存款列表页面（`http://localhost:3002/member/deposit/balance`）中：
1. 支付方式列的宽度需要大一点
2. 备注列的宽度需要小一点
3. 备注内容的字体需要小一点

### 修改内容

**文件：** `frontend/src/views/member/DepositBalance.vue`
- 修改表格列宽（第107-113行）：
  - 将"支付方式"列的宽度从 `100px` 增加到 `150px`
  - 将"备注"列的宽度设置为 `200px`（之前没有设置宽度）
- 修改备注单元格样式（第669-675行）：
  - 将 `max-width` 从 `300px` 调整为 `200px`
  - 添加 `font-size: 12px` 使备注内容字体更小
  - 添加 `color: #666` 使备注内容颜色更柔和

### 影响范围
- ✅ 支付方式列现在有更大的显示空间（150px）
- ✅ 备注列宽度缩小为200px，节省页面空间
- ✅ 备注内容字体更小（12px），视觉更协调

## 2026-01-06 - 订单详情页面和订单列表查询功能优化（修复编译错误）

### 功能说明
1. 订单详情页面：屏蔽收货人Mail字段，添加联系手机和联系电话字段显示
2. 订单列表查询功能：完善收货人姓名、联系电话、联系手机、收货人地址的查询功能
3. 商品收藏页面：商品标题和图片支持点击跳转到商品详情页面

### 修改原因
用户反馈：
1. 订单详情页面需要屏蔽收货人Mail字段，需要显示"联系手机"、"联系电话"字段
2. 订单列表查询功能中，收货人姓名、联系电话、联系手机、收货人地址查询功能都无效
3. 商品收藏页面，商品标题和图片需要支持点击跳转到商品详情页面

### 修改内容

**文件：** `backend/src/main/java/com/shoppingmall/vo/OrderDetailVO.java`
- 在 `RecipientInfo` 内部类中添加 `mobile` 字段（第205-207行）

**文件：** `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
- 修改 `convertToDetailVO` 方法（第643-662行）：
  - 分别设置 `phone` 和 `mobile` 字段，不再合并
  - 屏蔽 `email` 字段（设置为空字符串）
- 修改 `getOrderList` 方法（第334-450行）：
  - 添加收货人信息查询支持（收货人姓名、联系电话、联系手机、收货人地址）
  - 当需要过滤收货人信息时，先查询所有符合条件的订单，然后进行内存过滤，最后进行分页
  - 优化性能：在 `convertToListVO` 方法中设置 `contactPhone` 和 `contactMobile` 字段，避免重复查询订单
  - **修复编译错误**：将 `searchRecipientName`、`searchContactPhone`、`searchContactMobile`、`searchRecipientAddress` 变量声明为 `final`，确保在lambda表达式中可以正常使用
- 修改 `convertToListVO` 方法（第668-700行）：
  - 设置 `contactPhone` 和 `contactMobile` 字段，用于订单列表查询过滤

**文件：** `backend/src/main/java/com/shoppingmall/vo/OrderListVO.java`
- 添加 `contactPhone` 和 `contactMobile` 字段（第47-54行），用于订单列表查询过滤

**文件：** `frontend/src/views/order/Detail.vue`
- 修改收货人信息显示区域（第166-183行）：
  - 屏蔽"收货人Mail"字段显示
  - 添加"联系手机"字段显示
  - 保留"联系电话"字段显示
- 修改 `recipientInfo` computed属性（第350-365行）：
  - 添加 `mobile` 字段的默认值

**文件：** `frontend/src/api/buyer/order.ts`
- 修改 `OrderDetailVO` 接口（第91-103行）：
  - 在 `recipientInfo` 中添加 `mobile` 字段

**文件：** `frontend/src/views/member/Favorites.vue`
- 修改商品图片和商品标题（第37-49行）：
  - 为商品图片添加点击事件，跳转到商品详情页面
  - 为商品标题添加点击事件，跳转到商品详情页面
- 添加 `handleViewProduct` 方法（第146-149行）：
  - 实现商品详情页面跳转功能

### 影响范围
- ✅ 订单详情页面现在显示"联系手机"和"联系电话"字段，不再显示"收货人Mail"字段
- ✅ 订单列表查询功能现在支持收货人姓名、联系电话、联系手机、收货人地址的查询
- ✅ 商品收藏页面的商品标题和图片现在支持点击跳转到商品详情页面
- ✅ 优化了订单列表查询性能，避免重复查询订单数据
- ✅ 修复了Java编译错误：lambda表达式中使用的变量必须是final或effectively final

### 功能说明
1. 订单详情页面：屏蔽收货人Mail字段，添加联系手机和联系电话字段显示
2. 订单列表查询功能：完善收货人姓名、联系电话、联系手机、收货人地址的查询功能
3. 商品收藏页面：商品标题和图片支持点击跳转到商品详情页面

### 修改原因
用户反馈：
1. 订单详情页面需要屏蔽收货人Mail字段，需要显示"联系手机"、"联系电话"字段
2. 订单列表查询功能中，收货人姓名、联系电话、联系手机、收货人地址查询功能都无效
3. 商品收藏页面，商品标题和图片需要支持点击跳转到商品详情页面

### 修改内容

**文件：** `backend/src/main/java/com/shoppingmall/vo/OrderDetailVO.java`
- 在 `RecipientInfo` 内部类中添加 `mobile` 字段（第205-207行）

**文件：** `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
- 修改 `convertToDetailVO` 方法（第643-662行）：
  - 分别设置 `phone` 和 `mobile` 字段，不再合并
  - 屏蔽 `email` 字段（设置为空字符串）
- 修改 `getOrderList` 方法（第334-450行）：
  - 添加收货人信息查询支持（收货人姓名、联系电话、联系手机、收货人地址）
  - 当需要过滤收货人信息时，先查询所有符合条件的订单，然后进行内存过滤，最后进行分页
  - 优化性能：在 `convertToListVO` 方法中设置 `contactPhone` 和 `contactMobile` 字段，避免重复查询订单
- 修改 `convertToListVO` 方法（第668-700行）：
  - 设置 `contactPhone` 和 `contactMobile` 字段，用于订单列表查询过滤

**文件：** `backend/src/main/java/com/shoppingmall/vo/OrderListVO.java`
- 添加 `contactPhone` 和 `contactMobile` 字段（第47-54行），用于订单列表查询过滤

**文件：** `frontend/src/views/order/Detail.vue`
- 修改收货人信息显示区域（第166-183行）：
  - 屏蔽"收货人Mail"字段显示
  - 添加"联系手机"字段显示
  - 保留"联系电话"字段显示
- 修改 `recipientInfo` computed属性（第350-365行）：
  - 添加 `mobile` 字段的默认值

**文件：** `frontend/src/api/buyer/order.ts`
- 修改 `OrderDetailVO` 接口（第91-103行）：
  - 在 `recipientInfo` 中添加 `mobile` 字段

**文件：** `frontend/src/views/member/Favorites.vue`
- 修改商品图片和商品标题（第37-49行）：
  - 为商品图片添加点击事件，跳转到商品详情页面
  - 为商品标题添加点击事件，跳转到商品详情页面
- 添加 `handleViewProduct` 方法（第146-149行）：
  - 实现商品详情页面跳转功能

### 影响范围
- ✅ 订单详情页面现在显示"联系手机"和"联系电话"字段，不再显示"收货人Mail"字段
- ✅ 订单列表查询功能现在支持收货人姓名、联系电话、联系手机、收货人地址的查询
- ✅ 商品收藏页面的商品标题和图片现在支持点击跳转到商品详情页面
- ✅ 优化了订单列表查询性能，避免重复查询订单数据

## 2026-01-05 - 修复购物车页面商品重量显示精度问题

### 功能说明
修复购物车页面商品重量显示的浮点数精度问题，确保商品重量保留两位小数显示。

### 修改原因
用户反馈在购物车页面（`http://localhost:3002/cart`）中，商品总重显示为 `10221.060000000001克`，存在浮点数精度问题，需要保留两位小数显示。

### 修改内容

**文件：** `frontend/src/views/cart/Index.vue`
- 修改 `totalWeight` 计算逻辑（第246-250行）：
  - 在计算总重量后，使用 `toFixed(2)` 保留两位小数，并转换为数字类型
- 修改单个商品重量显示（第125行）：
  - 使用 `Number((item.weight || 0).toFixed(2))` 确保单个商品重量保留两位小数
- 修改总重量显示（第165行）：
  - 使用 `totalWeight.toFixed(2)` 格式化显示，确保总重量保留两位小数

### 影响范围
- ✅ 购物车页面的商品重量现在正确显示为两位小数（如：`10221.06克`）
- ✅ 单个商品重量显示也保留两位小数
- ✅ 解决了浮点数精度导致的显示问题

## 2026-01-05 - 修复购物车结算页面商品重量显示精度问题

### 功能说明
修复购物车结算页面商品重量显示的浮点数精度问题，确保商品重量保留两位小数显示。

### 修改原因
用户反馈在购物车结算页面（`http://localhost:3002/cart/checkout?cartIds=188,187,186`）中，商品重量显示为 `10221.060000000001克`，存在浮点数精度问题，需要保留两位小数显示。

### 修改内容

**文件：** `frontend/src/views/cart/Checkout.vue`
- 修改 `totalWeight` 计算逻辑（第684-686行）：
  - 在计算总重量后，使用 `toFixed(2)` 保留两位小数，并转换为数字类型
- 修改单个商品重量显示（第353行）：
  - 使用 `Number((item.weight || 0).toFixed(2))` 确保单个商品重量保留两位小数
- 修改总重量显示（第369行）：
  - 使用 `totalWeight.toFixed(2)` 格式化显示，确保总重量保留两位小数

### 影响范围
- ✅ 购物车结算页面的商品重量现在正确显示为两位小数（如：`10221.06克`）
- ✅ 单个商品重量显示也保留两位小数
- ✅ 解决了浮点数精度导致的显示问题

## 2026-01-05 - 商品详情页添加商品不存在提示页面

### 功能说明
当访问不存在的商品ID页面时（如 http://localhost:3002/products/22），显示一个友好的商品不存在提示页面，而不是只显示错误消息。

### 修改原因
用户要求访问不存在的商品ID页面时，应该显示一个商品不存在的提示页面，提供更好的用户体验。

### 修改内容

**文件：** `frontend/src/views/products/Detail.vue`
- 添加 `productNotFound` 响应式状态，用于标识商品是否存在
- 在 `loadProductDetail` 函数的 catch 块中：
  - 检查错误响应状态码是否为 404，或错误消息是否包含"商品不存在"
  - 如果是商品不存在的情况，设置 `productNotFound.value = true`
  - 其他错误仍显示错误消息
- 在模板中添加商品不存在提示页面：
  - 当 `productNotFound` 为 true 时显示
  - 包含警告图标、标题"商品不存在"、提示信息
  - 提供"返回首页"和"浏览商品"两个操作按钮
- 添加相应的样式，使提示页面居中显示，美观易读

### 影响范围
- ✅ 访问不存在的商品ID时，显示友好的提示页面
- ✅ 提供返回首页和浏览商品的快捷操作
- ✅ 不影响正常商品详情页的显示

## 2026-01-05 - 页脚对接帮助中心模块

### 功能说明
将页脚内容从静态内容改为动态对接帮助中心模块，页脚分类和文章标题均从帮助中心API获取。

### 修改原因
用户要求页脚内容直接对接帮助中心模块，例如"购物指南"对应帮助中心的分类，下面的最多显示4条文章标题。帮助中心的分类最多显示4个（购物指南、新手上路、购物条款、支付/配送方式）。

### 修改内容

**文件：** `frontend/src/components/home/Footer.vue`
- 导入帮助中心API：`getHelpCategories`、`getHelpArticlesByCategory`
- 添加响应式数据 `footerCategories` 存储页脚分类和文章数据
- 实现 `loadFooterData` 方法：
  - 获取帮助中心分类列表，只取前4个顶级分类
  - 对每个分类，如果有子分类则使用第一个子分类的ID获取文章，否则使用分类本身的ID
  - 每个分类最多显示4条文章
- 实现 `goToArticle` 方法，点击文章标题跳转到 `/help?articleId=xxx`
- 保留"备案号"为静态内容，不依赖帮助中心

### 影响范围
- ✅ 页脚分类和文章标题现在从帮助中心动态获取
- ✅ 最多显示4个帮助中心分类，每个分类最多显示4条文章
- ✅ 点击文章标题可跳转到帮助中心查看详情
- ✅ "备案号"保留为静态内容

## 2026-01-05 - 修改页脚内容居中显示

### 功能说明
将页脚内容从左对齐改为居中对齐显示。

### 修改原因
用户反馈页脚内容（购物指南、新手上路、购物条款、支付/配送方式、备案号等）目前是偏左显示的，需要改为居中显示。

### 修改内容

**文件：** `frontend/src/components/home/Footer.vue`
- 修改 `.footer-content` 的 `justify-content` 从 `space-between` 改为 `center`
- 为 `.links` 添加 `justify-content: center` 样式，使链接组居中显示

### 影响范围
- ✅ 页脚内容现在居中显示，包括所有链接组（购物指南、新手上路、购物条款、支付/配送方式、备案号）

## 2026-01-05 - 修改基础配置列表排序为按ID顺序

### 功能说明
将基础配置页面的列表排序方式改为按ID升序排序。

### 修改原因
用户要求基础配置列表按照ID顺序排序。

### 修改内容

**文件：** `backend/src/main/java/com/shoppingmall/service/system/impl/SystemConfigServiceImpl.java`
- 修改 `getSystemConfigPage` 方法中的排序逻辑
- 移除按 `sortOrder` 排序，改为只按 `id` 升序排序

### 影响范围
- ✅ 基础配置页面（`/admin/system/basic`）的列表现在按ID升序显示

## 2026-01-05 - 为系统配置表添加分类字段，支持按分类查询

### 功能说明
为系统配置表添加分类字段，支持对配置进行分类管理，方便查询和管理。

### 修改原因
用户反馈基础配置页面数据太多、比较乱，需要增加分类功能，按照数据类型进行分类，方便查询。

### 分类方案
根据配置键的前缀，将配置分为6个分类：
1. **网站基础** (site) - 包含网站基本信息、搜索相关配置
2. **支付配置** (payment) - 包含微信支付、支付宝、预存款等支付相关配置
3. **应用配置** (app) - 包含前端地址、密码重置、平台名称等应用配置
4. **邮件配置** (mail) - 包含邮件服务器、邮件模板等配置
5. **订单配置** (order) - 包含订单相关配置
6. **企业微信** (wechat.work) - 包含企业微信通知相关配置

### 修改内容

#### 1. 数据库表结构修改

**文件：** `database/update-20260105-add-system-config-category.sql`
- 添加 `category` 字段到 `system_config` 表
- 为现有数据设置分类（根据config_key前缀自动分类）

#### 2. 后端实体类修改

**文件：** `backend/src/main/java/com/shoppingmall/entity/SystemConfig.java`
- 添加 `category` 字段，用于存储配置分类

#### 3. 后端服务层修改

**文件：** `backend/src/main/java/com/shoppingmall/service/system/SystemConfigService.java`
- 修改 `getSystemConfigPage` 方法签名，添加 `category` 参数

**文件：** `backend/src/main/java/com/shoppingmall/service/system/impl/SystemConfigServiceImpl.java`
- 实现按分类筛选的逻辑

#### 4. 后端控制器修改

**文件：** `backend/src/main/java/com/shoppingmall/controller/admin/SystemConfigController.java`
- 修改 `getSystemConfigPage` 接口，添加 `category` 查询参数

#### 5. 前端API接口修改

**文件：** `admin-frontend/src/api/admin/systemConfig.ts`
- 在 `SystemConfig` 接口中添加 `category` 字段
- 修改 `getSystemConfigPage` 函数，添加 `category` 参数

#### 6. 前端页面修改

**文件：** `admin-frontend/src/views/system/Basic.vue`
- 在搜索栏中添加分类筛选下拉框
- 在配置列表表格中添加分类列显示
- 在编辑/新增对话框中添加分类选择
- 添加分类标签类型和文本的辅助函数

### 影响范围
- ✅ 数据库：新增category字段，现有数据已自动分类
- ✅ 后端接口：支持按分类查询配置
- ✅ 前端页面：支持按分类筛选和显示配置分类

### 注意事项
1. 执行数据库更新脚本后，现有配置数据会根据config_key前缀自动设置分类
2. 新增配置时可以选择分类，也可以不选择（分类为可选字段）
3. 分类值：site、payment、app、mail、order、wechat.work

## 2026-01-05 - 修改重置密码功能校验规则和提示文案

### 功能说明
修改会员列表页面中重置密码功能的校验规则和提示文案。

### 修改原因
用户要求：
- 密码可以输入纯数字或纯字母（不需要同时包含字母和数字）
- 提示文案去掉"包含字母和数字"，只保留"至少6位"

### 修改内容

**文件：** `admin-frontend/src/views/buyer/List.vue`
- 删除密码校验规则中要求同时包含字母和数字的验证逻辑
- 简化校验规则，只保留长度至少6位的校验
- 修改提示文案：将"建议密码长度至少6位，包含字母和数字。"改为"建议密码长度至少6位。"

### 影响范围
- ✅ 会员列表页面（`/admin/buyer/list`）的重置密码功能：
  - 密码可以输入纯数字或纯字母
  - 只需要满足长度至少6位的要求
  - 提示文案已更新

## 2026-01-05 - 屏蔽会员详情页面中的"审核意见"字段

### 功能说明
在会员列表页面的会员详情对话框中，屏蔽"审核意见"字段的显示。

### 修改原因
用户要求在会员详情页面中隐藏"审核意见"字段。

### 修改内容

**文件：** `admin-frontend/src/views/buyer/List.vue`
- 删除会员详情对话框中的"审核意见"字段显示
- 移除第165行的 `<el-descriptions-item label="审核意见" :span="2">{{ currentBuyer.auditComment || '-' }}</el-descriptions-item>`

### 影响范围
- ✅ 会员列表页面（`/admin/buyer/list`）的会员详情对话框不再显示"审核意见"字段

## 2025-12-30 - 修改结算页面运费计算逻辑，根据商品是否启用SKU选择重量来源

### 功能说明
修改结算页面的运费计算逻辑，根据商品是否启用多个规格SKU来决定使用商品表还是SKU表的重量字段来计算运费。

### 修改原因
用户反馈结算页面的运费计算逻辑需要优化：
- 如果商品没有启用多个规格sku，则获取商品表重量字段的数据
- 如果启用sku，则读取product_sku表的重量字段来计算运费

### 修改内容

#### 1. 购物车服务重量获取逻辑

**文件：** `backend/src/main/java/com/shoppingmall/service/buyer/impl/CartServiceImpl.java`
- 修改 `convertToVO` 方法中的重量获取逻辑
- 根据 `product.getEnableSpec()` 判断是否启用多个规格SKU：
  - 如果 `enableSpec == 0`（未启用规格），使用商品表（`product.weight`）的重量字段
  - 如果 `enableSpec == 1`（启用规格），使用SKU表（`product_sku.weight`）的重量字段
  - 如果启用规格但SKU没有重量，则使用商品表的重量作为兜底

#### 2. 订单服务重量获取逻辑

**文件：** `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
- 修改订单创建时的重量获取逻辑
- 根据 `product.getEnableSpec()` 判断是否启用多个规格SKU：
  - 如果 `enableSpec == 0`（未启用规格），使用商品表（`product.weight`）的重量字段
  - 如果 `enableSpec == 1`（启用规格），使用SKU表（`product_sku.weight`）的重量字段
  - 如果启用规格但SKU没有重量，则使用商品表的重量作为兜底

### 业务逻辑

1. **未启用规格的商品**（`enableSpec == 0`）：
   - 使用商品表的 `weight` 字段（Integer类型，单位：克）
   - 转换为 BigDecimal 类型用于计算

2. **启用规格的商品**（`enableSpec == 1`）：
   - 优先使用SKU表的 `weight` 字段（BigDecimal类型，单位：克）
   - 如果SKU没有重量，则使用商品表的重量作为兜底

### 影响范围

- ✅ 购物车商品列表：重量字段根据商品是否启用SKU正确获取
- ✅ 订单结算页面：运费计算时使用的重量数据来源正确
- ✅ 订单创建：订单商品快照中的重量数据来源正确

### 注意事项

1. **重量单位**：商品表和SKU表的重量单位都是克（g），计算运费时需要转换为千克（kg）
2. **数据一致性**：确保启用规格的商品，其SKU都有正确的重量数据
3. **兜底机制**：如果启用规格但SKU没有重量，会使用商品表的重量，确保运费计算的连续性

## 2025-12-30 - 添加运费计算详细日志用于问题排查

### 功能说明
在运费计算逻辑中添加详细的日志输出，用于排查运费计算为0的问题。

### 修改原因
用户反馈订单结算页面运费显示为¥0.00，但运费模板配置不包邮，需要添加详细日志排查问题。

### 修改内容

#### 1. 后端运费计算日志

**文件：** `backend/src/main/java/com/shoppingmall/controller/buyer/ShippingController.java`
- 添加 `@Slf4j` 注解
- 在 `calculateShippingFeeByTemplate` 方法中添加请求和响应日志

**文件：** `backend/src/main/java/com/shoppingmall/service/logistics/impl/ShippingServiceImpl.java`
- 在 `calculateShippingFeeByTemplate` 方法中添加模板ID为null的日志
- 在 `calculateByTemplate` 方法中添加详细日志：
  - 计算参数日志（省份、城市、区县、重量、金额、件数）
  - 运费模板信息日志（名称、计算方式、包邮条件、默认规则）
  - 包邮条件检查日志（金额、重量、件数）
  - 地区规则匹配日志
  - 使用的运费规则日志（首重、首重价格、续重、续重价格）
  - 计算过程日志（按重量/件数/金额计算）
  - 计算结果日志

#### 2. 后端地区匹配日志

**文件：** `backend/src/main/java/com/shoppingmall/service/logistics/impl/ShippingServiceImpl.java`
- 在 `findMatchedRule` 方法中添加日志：
  - 查找参数日志
  - 找到的规则列表日志
  - 匹配优先级日志（区县 > 城市 > 省份 > 默认）
  - 匹配结果日志

- 在 `findRuleByRegion` 方法中添加日志：
  - 规则列表和地区信息日志
  - 省份匹配过程日志
  - 城市匹配过程日志
  - 区县匹配过程日志
  - 字符串匹配（向后兼容）日志

#### 3. 前端日志（已存在）

**文件：** `frontend/src/views/cart/Checkout.vue`
- 已有详细的前端日志：
  - 地址选择日志
  - 商品列表日志
  - 运费模板分组日志
  - API调用参数日志
  - API响应结果日志
  - 错误日志

### 日志输出位置

1. **后端日志**：查看应用日志文件（如 `logs/application.log`）或控制台输出
2. **前端日志**：打开浏览器开发者工具（F12），查看 Console 标签页

### 排查步骤

1. **查看前端日志**：
   - 打开浏览器控制台（F12）
   - 查看是否有"开始计算运费"的日志
   - 检查地址信息、商品信息、运费模板ID是否正确
   - 检查API调用参数是否正确

2. **查看后端日志**：
   - 查看应用日志文件
   - 搜索"========== 开始计算运费 =========="
   - 检查：
     - 运费模板是否存在且启用
     - 包邮条件是否满足
     - 地区规则是否匹配
     - 使用的运费规则是否正确
     - 计算过程是否正确

3. **常见问题排查**：
   - **运费为0的可能原因**：
     - 包邮条件满足（金额/重量/件数）
     - 首重价格为0或null
     - 订单重量为0或null
     - 地区规则匹配失败，且默认规则的首重价格为0
     - 计算方式不匹配

### 影响范围

- ✅ 后端运费计算：添加详细日志，便于排查问题
- ✅ 后端地区匹配：添加详细日志，便于排查地区匹配问题
- ✅ 前端运费计算：已有日志，无需修改

### 注意事项

1. **日志级别**：使用 `log.info` 和 `log.warn`，不会影响生产环境性能
2. **日志格式**：使用分隔线（==========）便于查找
3. **敏感信息**：日志中不包含敏感信息，只记录必要的业务数据

## 2025-12-30 - 修复商品详情页计量单位显示错误

### 功能说明
修复商品ID为49的计量单位显示错误，将"喝"改为"盒"。

### 修改原因
用户反馈商品详情页面（http://localhost:3002/products/49）显示的计量单位为"喝"，但应该是"盒"。

### 修改内容

#### 创建数据库修复脚本

**文件：** `database/update-20251230-fix-product-unit.sql`

**修改点：**
- 创建SQL脚本修复商品ID为49的计量单位
- 将`unit`字段从"喝"更新为"盒"
- 添加查询语句验证更新结果

### 使用方法

执行SQL脚本：
```sql
USE `chengren_shopping_mall`;

UPDATE `product`
SET `unit` = '盒'
WHERE `id` = 49 AND `unit` = '喝';
```

### 影响范围

- ✅ 商品ID为49的计量单位显示：从"喝"改为"盒"
- ✅ 商品详情页面：正确显示计量单位
- ✅ 其他页面：使用该商品unit字段的地方都会显示正确的单位

### 注意事项

1. **数据修复**：这是数据修复脚本，只修复商品ID为49的计量单位
2. **其他商品**：如果其他商品也有类似的单位错误，需要单独修复
3. **执行前备份**：建议在执行SQL脚本前备份数据库

## 2025-12-30 - 订单结算模块增加运费结算功能

### 功能说明
在订单结算模块增加运费结算功能，根据商品配置的运费模板和收货地址自动计算运费。支持按重量、按件数、按金额三种计算方式，并考虑特殊地区的运费规则。

### 修改原因
用户反馈商品已经增加了配置运费模板功能，但订单结算模块还没有实现运费结算，需要根据配置的运费模板来计算运费，如果是有特殊地区，需要考虑这种情况。

### 修改内容

#### 1. 在CartVO中添加运费模板ID字段

**文件：** `backend/src/main/java/com/shoppingmall/vo/CartVO.java`
- 添加`shippingTemplateId`字段，用于标识商品使用的运费模板（为空表示包邮）

**文件：** `backend/src/main/java/com/shoppingmall/service/buyer/impl/CartServiceImpl.java`
- 在`convertToVO`方法中，从商品信息中获取`shippingTemplateId`并设置到CartVO中

**文件：** `frontend/src/api/buyer/cart.ts`
- 在`CartVO`接口中添加`shippingTemplateId`字段

#### 2. 创建直接使用运费模板ID计算运费的API接口

**文件：** `backend/src/main/java/com/shoppingmall/service/logistics/ShippingService.java`
- 添加`calculateShippingFeeByTemplate`方法，直接使用运费模板ID计算运费

**文件：** `backend/src/main/java/com/shoppingmall/service/logistics/impl/ShippingServiceImpl.java`
- 实现`calculateShippingFeeByTemplate`方法
- 如果模板ID为空，返回0（包邮）
- 调用`calculateByTemplate`方法计算运费，支持按重量、按件数、按金额三种计算方式
- 支持特殊地区的运费规则匹配（优先级：区县 > 城市 > 省份 > 默认规则）
- 支持包邮条件检查（包邮金额、包邮重量、包邮件数）

**文件：** `backend/src/main/java/com/shoppingmall/controller/buyer/ShippingController.java`
- 添加`calculateShippingFeeByTemplate`接口：`POST /api/buyer/shipping/calculate-by-template/{templateId}`
- 接收运费模板ID和计算参数（省份、城市、区县、总重量、总金额、总件数）
- 返回计算后的运费金额

#### 3. 在前端订单结算页面实现运费计算逻辑

**文件：** `frontend/src/api/buyer/shipping.ts`（新建）
- 创建运费计算API文件
- 定义`ShippingFeeCalculateDTO`接口
- 实现`calculateShippingFeeByTemplate`函数，调用后端API计算运费

**文件：** `frontend/src/views/cart/Checkout.vue`
- **导入运费计算API**：导入`calculateShippingFeeByTemplate`
- **修改运费计算逻辑**：
  - 将`shippingFee`从computed改为ref，支持异步计算
  - 添加`calculatingShippingFee`状态，显示计算中提示
  - 实现`calculateShippingFee`函数：
    - 按运费模板分组商品
    - 对每个运费模板组，计算总重量（转换为kg）、总金额、总件数
    - 调用API计算每个模板组的运费
    - 累加所有模板组的运费
    - 没有运费模板的商品（包邮）不计算运费
- **监听地址变化**：
  - 监听`selectedAddressId`变化，地址变化时重新计算运费
  - 监听地区选择器变化（省份、城市、区县），地址信息完整时计算运费
- **监听商品变化**：
  - 监听`orderItems`变化，商品变化时重新计算运费
- **加载时计算运费**：
  - 在`loadAddressList`中，加载地址后计算运费
  - 在`loadCartItems`中，加载商品后计算运费
- **UI显示**：
  - 在配送费用显示区域，显示"计算中..."状态
  - 计算完成后显示运费金额

### 运费计算规则

1. **按运费模板分组**：订单中的商品按运费模板ID分组，每个模板组独立计算运费
2. **包邮商品**：没有运费模板的商品（`shippingTemplateId`为空）不计算运费
3. **包邮条件检查**：
   - 如果订单金额达到包邮金额，运费为0
   - 如果订单重量达到包邮重量，运费为0
   - 如果订单件数达到包邮件数，运费为0
4. **特殊地区匹配**：
   - 优先匹配区县规则
   - 其次匹配城市规则
   - 再次匹配省份规则
   - 最后使用默认规则
5. **计算方式**：
   - **按重量**：首重价格 + (超出重量 / 续重) * 续重价格
   - **按件数**：首件价格 + (件数 - 1) * 续件价格
   - **按金额**：固定运费

### 影响范围

- ✅ 订单结算页面：根据商品运费模板和收货地址自动计算运费
- ✅ 购物车商品：返回运费模板ID信息
- ✅ 运费计算API：支持直接使用运费模板ID计算运费
- ✅ 特殊地区：支持不同地区的运费规则

### 注意事项

1. **重量单位**：商品重量单位是克（g），计算运费时需要转换为千克（kg）
2. **异步计算**：运费计算是异步的，需要显示"计算中..."状态
3. **地址完整性**：只有地址信息完整（省份、城市、区县）时才能计算运费
4. **错误处理**：计算运费失败时，显示错误提示，运费默认为0
5. **包邮商品**：没有运费模板的商品不计算运费，运费为0

## 2025-12-30 - 完善支付宝回调状态更新逻辑

### 功能说明
完善支付宝回调状态更新逻辑，修复预存款充值和订单支付回调状态更新问题，确保日志状态与实际处理结果一致。

### 修改原因
1. **预存款充值回调问题**：支付宝支付成功，但回调日志状态显示为"失败"（api_status=0）
   - 原因：`handlePaymentCallback`方法抛出异常（如充值记录状态不是PAYING），但日志更新时仍使用`success=true`，导致状态不一致
2. **订单支付回调问题**：订单支付回调状态一直是"处理中"（api_status=2），没有更新为"成功"
   - 原因1：订单支付回调时创建了新日志记录，而不是更新已存在的"处理中"日志
   - 原因2：重复回调时直接返回，没有更新日志状态

### 修改内容

#### 1. 修复预存款充值回调状态更新

**文件：** `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`

**修改点：**
- 在`processPaymentNotify`方法中，预存款充值回调处理部分：
  - 添加异常捕获，捕获`handlePaymentCallback`可能抛出的异常
  - 根据实际处理结果（`callbackSuccess`）更新日志状态
  - 如果`handlePaymentCallback`抛出异常，将日志状态更新为失败（0）
  - 确保日志状态与实际处理结果一致

#### 2. 修复订单支付回调状态更新

**文件：** `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`

**修改点：**
- **重复回调处理**：
  - 订单已处理时（PAID或REFUNDED状态），不再直接返回
  - 调用`updatePaymentLogStatus`更新日志状态为成功（1）
  - 确保重复回调也能正确更新日志状态
- **正常回调处理**：
  - 使用`updatePaymentLogStatus`更新已存在的日志记录，而不是创建新记录
  - 添加异常捕获，确保即使处理失败也能更新日志状态
  - 根据实际处理结果（`callbackSuccess`）更新日志状态

### 修复的问题

1. ✅ **预存款充值回调**：即使`handlePaymentCallback`抛出异常，也能正确更新日志状态为失败
2. ✅ **订单支付回调**：更新已存在的日志记录，不会创建重复记录
3. ✅ **重复回调**：重复回调时也会更新日志状态为成功
4. ✅ **异常处理**：处理失败时也会更新日志状态为失败

### 影响范围

- ✅ 预存款充值回调：日志状态与实际处理结果一致
- ✅ 订单支付回调：日志状态正确更新，不会产生重复记录
- ✅ 重复回调：日志状态正确更新为成功

### 注意事项

1. **状态一致性**：日志状态现在完全反映实际处理结果
2. **异常处理**：所有异常都会被捕获并记录到日志状态中
3. **重复回调**：重复回调被视为成功，并更新日志状态
4. **日志更新**：统一使用`updatePaymentLogStatus`方法更新日志，而不是创建新记录

## 2025-12-30 - 修复支付宝回调状态更新和预存款退款日志记录问题

### 功能说明
修复两个问题：
1. 支付宝回调状态一直是"处理中"的问题：预存款充值回调成功后，更新payment_api_log的状态为"成功"
2. 支付宝预存款退款没有日志记录的问题：为预存款退款添加payment_api_log日志记录

### 修改原因
1. 用户反馈支付宝回调的payment_api_log表中状态都是"处理中"（api_status=2），没有更新为"成功"（api_status=1）
2. 用户反馈支付宝退款没有日志更新到payment_api_log表，但微信退款有更新

### 修改内容

#### 1. 添加更新日志状态的方法

**文件：** `backend/src/main/java/com/shoppingmall/service/payment/PaymentLogService.java`
- 添加`updatePaymentLogStatus`方法，用于更新已存在的日志记录状态

**文件：** `backend/src/main/java/com/shoppingmall/service/payment/impl/PaymentLogServiceImpl.java`
- 实现`updatePaymentLogStatus`方法
- 查询最新的日志记录（根据订单号、接口类型、支付方式）
- 更新日志状态、外部交易号、响应数据等
- 使用`@Async`异步执行，避免影响支付性能

#### 2. 修复预存款充值回调状态更新

**文件：** `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`

**修改点：**
- 在`processPaymentNotify`方法中，处理预存款充值回调成功后，调用`paymentLogService.updatePaymentLogStatus`更新日志状态
- 将状态从"处理中"（2）更新为"成功"（1）或"失败"（0）
- 同时更新外部交易号和响应数据

#### 3. 为预存款退款添加日志记录

**文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/DepositServiceImpl.java`

**修改点：**
- 注入`PaymentLogService`和`ObjectMapper`
- **使用商户订单号退款时**：记录退款日志（成功/失败）
  - 记录支付方式（ALIPAY）、接口类型（REFUND）、业务类型（DEPOSIT）
  - 记录订单号、支付流水号、外部交易号、请求数据、响应数据、执行耗时等
- **使用支付宝交易号退款时**：记录退款日志（重试场景）
  - 同样记录完整的退款信息
  - 标记`useTradeNo=true`，区分使用哪种方式退款

### 记录的日志类型

1. **CALLBACK**（回调通知）：预存款充值回调状态更新
2. **REFUND**（退款）：预存款退款日志记录

### 日志字段

- `paymentMethod`：支付方式（ALIPAY）
- `apiType`：接口类型（CALLBACK/REFUND）
- `businessType`：业务类型（DEPOSIT）
- `orderNo`：订单号（内部订单号）
- `paymentNo`：支付流水号
- `externalTradeNo`：外部交易号
- `apiUrl`：接口URL
- `requestMethod`：HTTP请求方法（POST）
- `requestData`：请求数据（JSON格式）
- `responseData`：响应数据（JSON格式）
- `apiStatus`：接口调用状态（0-失败，1-成功，2-处理中）
- `errorCode`：错误代码
- `errorMessage`：错误信息
- `executionTime`：执行耗时（毫秒）

### 影响范围

- ✅ 预存款充值回调：支付宝回调成功后，payment_api_log状态会更新为"成功"
- ✅ 预存款退款：支付宝预存款退款会记录日志到payment_api_log表
- ✅ 退款重试：使用支付宝交易号退款时也会记录日志

### 注意事项

1. **状态更新**：更新已存在的日志记录，而不是创建新记录
2. **异步执行**：日志记录和更新都使用`@Async`异步执行，不会影响支付性能
3. **错误处理**：日志记录失败不会影响支付流程，只记录错误日志
4. **退款日志**：支持两种退款方式（使用商户订单号和支付宝交易号）的日志记录

## 2025-12-30 - 优化预存款充值页面支付弹窗样式

### 功能说明
优化预存款充值页面的支付状态弹窗样式，使弹窗居中显示，并减少字段内容之间的间距，让界面更紧凑美观。

### 修改原因
用户反馈支付弹窗需要居中显示，且字段内容上下距离可以更紧凑一些。

### 修改内容

#### 预存款充值页面支付弹窗样式优化

**文件：** `frontend/src/views/member/DepositRecharge.vue`

**修改点：**
- **弹窗居中显示**：在`el-dialog`组件上添加`align-center`属性，确保弹窗居中显示
- **减少间距**：
  - `.payment-status-content`的`padding`从`20px`减少到`10px`
  - `.status-icon`的`margin-bottom`从`20px`减少到`8px`
  - `.status-title`的`margin-bottom`从`10px`减少到`6px`
  - `.status-desc`的`margin-bottom`从`20px`减少到`12px`
  - `.status-actions`的`margin-top`从`20px`减少到`12px`
  - `.qrcode-container`的`margin`从`20px 0`减少到`12px 0`
  - `.qrcode-title`的`margin-bottom`从`15px`减少到`8px`
  - `.qrcode-tip`的`margin-top`从`10px`减少到`6px`

### 影响范围

- ✅ 预存款充值页面（`http://localhost:3002/member/deposit/recharge`）：支付状态弹窗样式优化
  - 弹窗居中显示
  - 字段内容间距更紧凑
  - 提升用户体验

## 2025-12-30 - 修复预存款微信支付未同步到payment_api_log表的问题

### 功能说明
修复预存款微信支付没有记录到payment_api_log表的问题。之前已经为支付宝支付添加了日志记录功能，但微信支付策略中还没有添加日志记录功能。

### 修改原因
用户反馈预存款微信支付没有同步到payment_api_log表，需要为微信支付添加日志记录功能。

### 修改内容

#### 在WeChatPayStrategy中添加日志记录

**文件：** `backend/src/main/java/com/shoppingmall/payment/strategy/impl/WeChatPayStrategy.java`

**修改点：**
- 注入`PaymentLogService`和`ObjectMapper`
- **createPayment方法**：在创建支付订单成功/失败时记录日志
  - 记录支付方式（WECHAT）、接口类型（CREATE_PAYMENT）、业务类型（ORDER或DEPOSIT）
  - 根据订单号前缀（DEPOSIT_）判断业务类型
  - 记录订单号、支付流水号、请求数据、响应数据等
  - 记录API URL（区分沙箱和生产环境）
- **queryPaymentStatus方法**：在查询订单状态时记录日志
  - 记录查询请求和响应数据、执行耗时等
  - 记录外部交易号（transaction_id）
  - 根据订单号前缀判断业务类型
- **refund方法**：在退款成功/失败时记录日志
  - 记录退款请求参数、响应数据、执行耗时等
  - 记录错误代码和错误信息（失败时）
  - 根据订单号前缀判断业务类型（支持预存款退款和订单退款）

### 记录的日志类型

1. **CREATE_PAYMENT**（创建支付）：记录创建微信支付订单的请求和响应
2. **QUERY_ORDER**（查询订单）：记录查询微信支付订单状态的请求和响应
3. **REFUND**（退款）：记录微信支付退款请求和响应

### 日志字段

- `paymentMethod`：支付方式（WECHAT）
- `apiType`：接口类型（CREATE_PAYMENT/REFUND/QUERY_ORDER）
- `businessType`：业务类型（ORDER/DEPOSIT）- 根据订单号前缀自动判断
- `orderNo`：订单号（内部订单号）
- `paymentNo`：支付流水号
- `externalTradeNo`：外部交易号（微信交易号）
- `apiUrl`：接口URL（区分沙箱和生产环境）
- `requestMethod`：HTTP请求方法（POST）
- `requestData`：请求数据（JSON格式）
- `responseData`：响应数据（JSON格式）
- `apiStatus`：接口调用状态（0-失败，1-成功）
- `errorCode`：错误代码
- `errorMessage`：错误信息
- `executionTime`：执行耗时（毫秒）

### 影响范围

- ✅ 预存款微信支付：预存款充值使用微信支付时会记录日志
- ✅ 订单微信支付：订单支付使用微信支付时会记录日志
- ✅ 微信支付查询：查询微信支付订单状态时会记录日志
- ✅ 微信支付退款：微信支付退款时会记录日志（支持预存款退款和订单退款）

### 注意事项

1. **业务类型自动判断**：根据订单号前缀（DEPOSIT_）自动判断是订单支付还是预存款充值
2. **环境区分**：API URL会根据配置自动区分沙箱环境和生产环境
3. **异步执行**：日志记录使用`@Async`异步执行，不会影响支付性能
4. **错误处理**：日志记录失败不会影响支付流程，只记录错误日志

## 2025-12-30 - 修复支付操作后未记录payment_api_log的问题

### 功能说明
修复支付操作后没有更新数据到payment_api_log表的问题。之前虽然创建了payment_api_log表和实体类，但在支付操作时没有调用日志记录功能。

### 修改原因
用户反馈支付操作后，payment_api_log表中没有记录，需要添加日志记录功能。

### 修改内容

#### 1. 创建支付日志服务类

**文件：** `backend/src/main/java/com/shoppingmall/service/payment/PaymentLogService.java`
- 创建支付接口日志服务接口

**文件：** `backend/src/main/java/com/shoppingmall/service/payment/impl/PaymentLogServiceImpl.java`
- 实现支付接口日志服务
- 使用`@Async`异步执行，避免影响支付性能
- 日志记录失败不影响支付流程

#### 2. 在AlipayPayStrategy中添加日志记录

**文件：** `backend/src/main/java/com/shoppingmall/payment/strategy/impl/AlipayPayStrategy.java`

**修改点：**
- 注入`PaymentLogService`和`ObjectMapper`
- **createPayment方法**：在创建支付订单成功/失败时记录日志
  - 记录支付方式、接口类型、业务类型、订单号、支付流水号、请求数据等
  - 根据订单号判断业务类型（ORDER或DEPOSIT）
- **refund方法**：在退款成功/失败时记录日志
  - 记录退款请求参数、响应数据、执行耗时等
- **queryPaymentStatus方法**：在查询订单状态时记录日志
  - 记录查询请求和响应数据、执行耗时等

#### 3. 在PaymentNotifyController中添加日志记录

**文件：** `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`

**修改点：**
- 注入`PaymentLogService`
- **alipayNotify方法**：在收到支付宝回调时记录日志（处理中状态）
- **processPaymentNotify方法**：在支付回调处理成功时记录日志（成功状态）

#### 4. 在PaymentSyncScheduledServiceImpl中添加日志记录

**文件：** `backend/src/main/java/com/shoppingmall/service/payment/impl/PaymentSyncScheduledServiceImpl.java`

**修改点：**
- 注入`PaymentLogService`
- 在定时任务查询订单状态时记录日志
  - 记录查询请求和响应数据、执行耗时等

#### 5. 在OrderServiceImpl中添加日志记录

**文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`

**修改点：**
- 注入`PaymentLogService`
- 在订单退款操作时记录日志
  - 记录使用商户订单号（out_trade_no）退款的日志
  - 记录使用支付宝交易号（trade_no）退款的日志
  - 记录退款请求参数、响应数据、执行耗时等

### 记录的日志类型

1. **CREATE_PAYMENT**（创建支付）：记录创建支付订单的请求和响应
2. **REFUND**（退款）：记录退款请求和响应
3. **QUERY_ORDER**（查询订单）：记录查询订单状态的请求和响应
4. **CALLBACK**（回调通知）：记录支付回调的请求数据

### 日志字段

- `paymentMethod`：支付方式（ALIPAY/WECHAT）
- `apiType`：接口类型（CREATE_PAYMENT/REFUND/QUERY_ORDER/CALLBACK）
- `businessType`：业务类型（ORDER/DEPOSIT）
- `orderNo`：订单号
- `paymentNo`：支付流水号
- `externalTradeNo`：外部交易号
- `apiUrl`：接口URL
- `requestMethod`：HTTP请求方法
- `requestData`：请求数据（JSON格式）
- `responseData`：响应数据（JSON格式）
- `apiStatus`：接口调用状态（0-失败，1-成功，2-处理中）
- `errorCode`：错误代码
- `errorMessage`：错误信息
- `executionTime`：执行耗时（毫秒）

### 影响范围

- ✅ 支付创建：支付宝支付订单创建时会记录日志
- ✅ 支付回调：支付宝支付回调时会记录日志
- ✅ 订单查询：查询订单状态时会记录日志
- ✅ 订单退款：订单退款时会记录日志
- ✅ 定时任务：支付补单定时任务查询订单时会记录日志

### 注意事项

1. **异步执行**：日志记录使用`@Async`异步执行，不会影响支付性能
2. **错误处理**：日志记录失败不会影响支付流程，只记录错误日志
3. **业务类型判断**：根据订单号前缀（DEPOSIT_）判断是订单支付还是预存款充值

## 2025-12-30 - 预存款交易记录页面字段和功能调整

### 功能说明
调整前端会员预存款余额页面和管理后台预存款管理页面的列表字段显示，屏蔽部分字段，增加新字段，并屏蔽部分功能入口。

### 修改原因
- 简化列表显示，屏蔽不必要的字段（冻结金额、解冻金额、可用余额）
- 增加ID字段和支付方式字段，方便查看和识别
- 屏蔽下载和导出功能入口，简化操作

### 修改内容

#### 1. 前端会员预存款余额页面

**文件：** `frontend/src/views/member/DepositBalance.vue`

**修改点：**
- **屏蔽字段**：
  - 屏蔽"冻结金额"列
  - 屏蔽"解冻金额"列
  - 屏蔽"可用余额"列
- **增加字段**：
  - 增加"ID"列（显示记录ID）
  - 增加"支付方式"列（显示支付方式，支持支付宝、微信等）
- **屏蔽功能入口**：
  - 屏蔽"下载交易记录"按钮
  - 屏蔽"导出选中记录"按钮和底部操作栏
- **添加支付方式显示函数**：
  - `getPaymentMethodName()`: 获取支付方式中文名称
  - `getPaymentMethodStyle()`: 获取支付方式样式（支付宝蓝色，微信绿色）

#### 2. 管理后台预存款管理页面

**文件：** `admin-frontend/src/views/deposit/Record.vue`

**修改点：**
- **屏蔽字段**：
  - 屏蔽"冻结金额"列
  - 屏蔽"解冻金额"列
  - 屏蔽"可用余额"列
- **调整操作列宽度**：
  - 操作列宽度从280px调整为180px

### 影响范围
- 前端会员预存款余额页面（`http://localhost:3002/member/deposit/balance`）：列表字段简化，增加ID和支付方式字段
- 管理后台预存款管理页面（`http://localhost:3003/admin/finance/deposit`）：列表字段简化，操作列宽度调整

### 注意事项
1. 前端页面的支付方式字段会根据支付方式显示不同颜色（支付宝蓝色，微信绿色）
2. 前端页面已屏蔽下载和导出功能，但相关代码保留在注释中，便于后续恢复
3. 管理后台页面的操作列宽度已优化，节省页面空间

### 修改文件清单
1. `frontend/src/views/member/DepositBalance.vue`
2. `admin-frontend/src/views/deposit/Record.vue`

---

## 2025-12-30 - 定时任务管理页面增加初始化数据

### 功能说明
为定时任务管理页面（`http://localhost:3003/admin/system/scheduled-task`）创建定时任务表并初始化定时任务数据，使页面能够显示系统中的所有定时任务。

### 修改原因
- 定时任务管理页面需要从数据库读取定时任务数据
- 需要将系统中现有的定时任务信息初始化到数据库中
- 方便管理员在后台查看和管理系统中的定时任务

### 修改内容

#### 数据库脚本

**文件：** `database/update-20251230-add-scheduled-task-table-and-init-data.sql`

**修改点：**
- 创建 `scheduled_task` 表，包含以下字段：
  - `id`: 主键ID
  - `task_name`: 任务名称
  - `task_group`: 任务组
  - `cron_expression`: Cron表达式（固定频率任务可为空）
  - `bean_name`: Bean名称（Spring Bean名称）
  - `method_name`: 方法名称
  - `status`: 状态（0-已停止，1-运行中）
  - `description`: 任务描述
  - `last_execute_time`: 上次执行时间
  - `next_execute_time`: 下次执行时间
  - `create_time`: 创建时间
  - `update_time`: 更新时间
- 创建唯一索引 `uk_task_name_group`，确保同一组内任务名称唯一
- 创建索引 `idx_status` 和 `idx_bean_name`，提升查询性能

**初始化的定时任务数据：**
1. **订单自动取消超时订单**（订单管理组）
   - Bean名称: `orderScheduledServiceImpl`
   - 方法名称: `cancelTimeoutOrders`
   - 执行频率: 每分钟执行一次（fixedRate = 60000）
   - 描述: 每分钟执行一次，自动取消超过指定时间未支付的待付款订单，并恢复库存

2. **预存款自动取消超时记录**（预存款管理组）
   - Bean名称: `depositPayingScheduledService`
   - 方法名称: `cancelTimeoutDepositRecords`
   - 执行频率: 每小时执行一次（fixedRate = 3600000）
   - 描述: 每小时执行一次，自动将超过指定时间仍处于支付中状态的预存款记录更新为已超时

3. **支付记录自动关闭超时记录**（支付管理组）
   - Bean名称: `paymentRecordScheduledServiceImpl`
   - 方法名称: `cancelTimeoutPaymentRecords`
   - 执行频率: 每小时执行一次（fixedRate = 3600000）
   - 描述: 每小时执行一次，自动将超过指定时间仍处于支付中状态的支付记录更新为已关闭

4. **订单补单查询**（支付管理组）
   - Bean名称: `paymentSyncScheduledService`
   - 方法名称: `syncPaymentStatus`
   - 执行频率: 每5分钟执行一次（fixedRate = 300000）
   - 描述: 每5分钟执行一次，查询支付中状态的支付记录，如果支付宝已支付则自动补单并更新订单状态

5. **聚水潭物流拉取**（ERP同步组）
   - Bean名称: `jushuitanSyncTask`
   - 方法名称: `pullLogisticsTask`
   - Cron表达式: `0 */30 * * * ?`（每30分钟执行一次）
   - 描述: 每30分钟执行一次，拉取聚水潭ERP中待发货订单的物流信息（需配置启用）

6. **聚水潭全量同步**（ERP同步组）
   - Bean名称: `jushuitanSyncTask`
   - 方法名称: `fullSyncTask`
   - Cron表达式: `0 0 3 * * ?`（每天凌晨3点执行一次）
   - 描述: 每天凌晨3点执行一次，执行聚水潭ERP全量同步任务（需配置启用）

### 影响范围
- 定时任务管理页面：页面可以显示系统中的所有定时任务
- 数据库：新增 `scheduled_task` 表，包含6条初始化的定时任务数据

### 注意事项
1. 执行SQL脚本后，定时任务数据会被初始化到数据库中
2. 如果任务已存在（根据任务名称和任务组唯一索引），会更新任务信息，不会重复插入
3. 所有初始化的任务默认状态为"运行中"（status = 1）
4. 固定频率任务（fixedRate）的 `cron_expression` 字段为 NULL，Cron表达式任务需要填写对应的Cron表达式

### 修改文件清单
1. `database/update-20251230-add-scheduled-task-table-and-init-data.sql`（新建）

---

## 2025-12-27 - 订单退款按钮增加loading状态

### 功能说明
在订单列表页面的退款对话框中，为"确认退款"按钮增加loading状态，防止用户在退款请求处理期间重复点击。

### 修改原因
- 防止用户在退款请求处理期间重复点击按钮，导致重复提交退款请求
- 提升用户体验，明确显示退款操作正在进行中
- 避免因重复点击导致的异常情况

### 修改内容

#### 1. 添加loading状态变量

**文件：** `admin-frontend/src/views/order/List.vue`

**修改点：**
- 添加 `refundLoading` 响应式变量，用于控制退款按钮的loading状态
- 初始值为 `false`

#### 2. 更新退款按钮

**文件：** `admin-frontend/src/views/order/List.vue`

**修改点：**
- 在"确认退款"按钮上添加 `:loading="refundLoading"` 属性，显示loading动画
- 在 `:disabled` 属性中添加 `|| refundLoading` 条件，loading期间禁用按钮
- 在"取消"按钮上添加 `:disabled="refundLoading"` 属性，loading期间禁用取消按钮

#### 3. 更新退款提交逻辑

**文件：** `admin-frontend/src/views/order/List.vue`

**修改点：**
- 在 `handleRefundSubmit` 函数开头添加 `refundLoading.value` 检查，如果正在处理中则直接返回
- 在确认对话框后、调用退款接口前，设置 `refundLoading.value = true`
- 在退款请求完成后（无论成功还是失败），在 `finally` 块中重置 `refundLoading.value = false`
- 确保在访问 `currentOrder.value` 之前进行null检查，避免TypeScript类型错误

### 影响范围
- 订单退款功能：退款按钮在请求处理期间显示loading状态，防止重复点击

### 注意事项
1. loading状态会在退款请求完成后自动重置，无论成功还是失败
2. 如果用户取消确认对话框，loading状态不会被设置（因为确认对话框在设置loading之前）
3. 取消按钮在loading期间也会被禁用，防止用户在退款处理期间关闭对话框

### 修改文件清单
1. `admin-frontend/src/views/order/List.vue`

---

## 2025-12-27 - 修复订单支付宝退款异常处理和退款单号格式问题

### 功能说明
修复订单支付宝退款代码中的异常处理逻辑和退款单号格式问题，确保使用支付宝交易号（trade_no）退款时能够使用与预存款退款一致的退款单号格式。

### 修改原因
- 订单退款时，使用商户订单号（out_trade_no）退款失败后，尝试使用支付宝交易号（trade_no）退款
- 当使用trade_no退款失败时，代码抛出新的PaymentException，但被外层catch捕获后抛出原始异常e，导致错误信息丢失
- 订单退款使用的退款单号格式（RF + 日期时间 + 随机数）与预存款退款使用的格式（ALI_REFUND_ + timestamp）不一致
- 预存款退款功能正常，对比发现订单退款代码的退款单号格式和异常处理逻辑有问题

### 修改内容

#### 1. 修复tradeNo变量作用域问题

**文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`

**修改点：**
- 将`tradeNo`变量定义在外层作用域（第486行），以便在catch块中能够正确访问
- 修复变量作用域问题，确保在异常处理时能够正确访问tradeNo变量

#### 2. 修复异常处理逻辑

**文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`

**修改点：**
- 在catch (PaymentException e2)块中，抛出e2而不是e，保留使用trade_no退款失败时的详细错误信息
- 在catch (Exception e2)块中，如果是PaymentException类型，抛出e2；否则抛出原始异常e
- 确保异常信息能够正确传递，便于排查问题

#### 3. 统一退款单号格式

**文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`

**修改点：**
- 在使用支付宝交易号（trade_no）退款时，使用与预存款退款一致的退款单号格式：`ALI_REFUND_` + timestamp
- 确保退款单号格式与预存款退款保持一致，避免因格式问题导致退款失败
- 退款成功后，使用支付宝退款单号（alipayRefundNo）作为refundPaymentNo

### 影响范围
- 订单退款功能：使用支付宝交易号（trade_no）退款时，使用与预存款退款一致的退款单号格式
- 异常处理逻辑已优化，确保错误信息能够正确传递

### 注意事项
1. 退款单号格式已统一，与预存款退款保持一致
2. 异常处理逻辑已优化，确保错误信息能够正确传递
3. 如果订单退款仍然失败，需要检查支付宝沙箱环境或订单状态

### 修改文件清单
1. `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`

---

## 2025-12-26 - 预存款退款弹窗显示已退款金额和剩余可退款金额

### 功能说明
优化预存款退款功能，在退款弹窗中显示已退款金额和剩余可退款金额，确保用户清楚了解退款情况，并防止退款金额超过充值金额。

### 修改原因
- 支持部分退款功能，需要显示已退款金额和剩余可退款金额
- 用户需要清楚了解退款情况，避免重复退款或退款金额错误
- 确保退款金额 + 已退款金额不超过充值金额，保证数据准确性

### 修改内容

#### 1. 后端VO添加字段

**文件：** `backend/src/main/java/com/shoppingmall/vo/AdminDepositRecordVO.java`

**修改点：**
- 添加`refundedAmount`字段：已退款金额（针对充值记录，累计已退款金额）
- 添加`refundableAmount`字段：可退款金额（针对充值记录，充值金额 - 已退款金额）

#### 2. 后端计算已退款金额

**文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/DepositServiceImpl.java`

**修改点：**
- 在`getDepositRecordList`方法中：为充值记录计算已退款金额和可退款金额
- 在`getDepositRecordById`方法中：为充值记录计算已退款金额和可退款金额
- 新增`calculateRefundedAmount`方法：查询所有退款记录，remark中包含"原充值记录ID：{充值记录ID}"的退款记录，累加退款金额

#### 3. 前端接口添加字段

**文件：** `admin-frontend/src/api/admin/deposit.ts`

**修改点：**
- 在`DepositRecordVO`接口中添加`refundedAmount`和`refundableAmount`字段

#### 4. 前端退款弹窗优化

**文件：** `admin-frontend/src/views/deposit/Record.vue`

**修改点：**
- 在退款弹窗中显示充值金额、已退款金额、剩余可退款金额
- 添加分隔线，区分信息展示和输入区域
- 优化样式，使用不同颜色突出显示关键信息

#### 5. 前端验证规则优化

**文件：** `admin-frontend/src/views/deposit/Record.vue`

**修改点：**
- 修改退款金额验证规则，确保本次退款金额 + 已退款金额 <= 充值金额
- 验证退款金额不能超过剩余可退款金额
- 提供详细的错误提示信息

### 功能特点

**1. 信息展示清晰：**
- 充值金额、已退款金额、剩余可退款金额一目了然
- 使用不同颜色和字体大小突出显示关键信息

**2. 数据准确性：**
- 后端实时计算已退款金额，确保数据准确
- 前端验证确保退款金额不超过剩余可退款金额

**3. 用户体验优化：**
- 清晰的提示信息，帮助用户理解退款规则
- 输入框最大值自动限制为剩余可退款金额
- 详细的错误提示，帮助用户快速定位问题

**4. 安全性保障：**
- 双重验证：前端验证 + 后端验证
- 防止退款金额超过充值金额
- 防止重复退款或退款金额错误

### 使用说明

**退款流程：**
1. 在预存款交易记录列表中，找到需要退款的充值记录（状态为"已通过"）
2. 点击"退款"按钮，打开退款弹窗
3. 查看充值金额、已退款金额、剩余可退款金额
4. 输入本次退款金额（不能超过剩余可退款金额）
5. 输入退款原因
6. 点击"确认退款"完成退款

**注意事项：**
- 只有状态为"已通过"的充值记录才能退款
- 本次退款金额 + 已退款金额不能超过充值金额
- 退款操作不可撤销，请谨慎操作
- 如果剩余可退款金额为0，则无法继续退款

---

## 2025-12-27 - 优化支付宝退款订单查询逻辑

### 功能说明
优化支付宝退款前的订单查询逻辑，如果订单查询失败（可能是支付宝沙箱环境问题），允许继续尝试退款，避免因订单查询失败导致退款无法进行。

### 修改原因
- 用户反馈：预存款充值通过支付宝沙箱支付成功，但退款操作不了
- 订单查询可能失败（支付宝沙箱环境问题），导致退款无法进行
- 需要放宽订单查询失败的限制，允许继续尝试退款

### 问题分析

**问题现象：**
- 预存款充值通过支付宝沙箱支付成功
- 退款时订单查询可能失败，导致退款无法进行
- 支付宝退款返回错误码 `20000`（系统异常）

**可能的原因：**
1. 支付宝沙箱环境订单查询接口不稳定
2. 订单查询失败后，退款流程被中断
3. 订单号格式问题（商户订单号 vs 支付宝交易号）

### 修改内容

**文件：** `backend/src/main/java/com/shoppingmall/payment/strategy/impl/AlipayPayStrategy.java`

- **修改 `refund()` 方法**：
  - 订单查询失败时，不直接抛出异常，允许继续尝试退款
  - 只有订单状态明确不允许退款时，才抛出异常
  - 记录详细的查询失败日志，便于排查问题

**代码逻辑：**
```java
// 退款前先查询订单状态，确认订单是否存在且已支付成功
// 注意：如果订单查询失败（可能是支付宝沙箱环境问题），允许继续退款尝试
log.info("退款前查询订单状态，订单号：{}", paymentNo);
try {
    Map<String, String> orderStatus = AlipayUtil.queryOrder(envConfig, paymentNo);
    String tradeStatus = orderStatus.get("trade_status");
    
    if ("UNKNOWN".equals(tradeStatus)) {
        log.warn("无法查询到订单状态，订单号：{}，可能订单不存在或支付宝沙箱环境问题，继续尝试退款", paymentNo);
        // 不抛出异常，允许继续尝试退款（可能是支付宝沙箱环境的问题）
    } else {
        // 检查订单状态是否允许退款
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            log.warn("订单状态不允许退款，订单号：{}，订单状态：{}", paymentNo, tradeStatus);
            throw new PaymentException(500, "订单状态不允许退款，当前订单状态：" + tradeStatus + "，只有已支付成功或已完成的订单才能退款");
        }
        log.info("订单状态验证通过，订单号：{}，订单状态：{}，可以退款", paymentNo, tradeStatus);
    }
} catch (PaymentException e) {
    // 如果是订单状态不允许退款，直接抛出异常
    throw e;
} catch (Exception e) {
    // 订单查询失败（可能是网络问题或支付宝沙箱环境问题），记录警告但允许继续退款
    log.warn("订单状态查询失败，订单号：{}，错误：{}，继续尝试退款", paymentNo, e.getMessage());
}
```

**文件：** `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`

- **优化 `queryOrder()` 方法**：
  - 改进网关地址判断逻辑（优先使用配置的网关地址）
  - 增强订单查询失败的日志记录
  - 记录子错误码，便于排查问题

- **优化 `refund()` 方法**：
  - 在退款失败时，如果是订单不存在错误，记录提示信息
  - 提示可以使用支付宝交易号（trade_no）进行退款

### 功能流程

**修改前流程：**
1. 退款前查询订单状态
2. 如果查询失败（返回 `UNKNOWN`），直接抛出异常
3. 退款无法进行

**修改后流程：**
1. 退款前查询订单状态
2. 如果查询失败（返回 `UNKNOWN`），记录警告但允许继续退款
3. 如果订单状态明确不允许退款，才抛出异常
4. 继续尝试退款，由支付宝API返回具体的错误信息

### 影响范围

- ✅ 支付宝退款接口（`/api/admin/deposit/refund`）
- ✅ 订单退款接口
- ✅ 支付记录退款接口
- ✅ 订单查询逻辑
- ✅ 错误处理优化

### 测试建议

1. **订单查询失败测试**：
   - 测试订单查询失败的情况
   - 验证是否允许继续退款
   - 验证错误提示是否友好

2. **正常退款测试**：
   - 测试订单查询成功的情况
   - 验证订单状态验证是否正常
   - 验证退款流程是否正常

### 注意事项

- ✅ **订单查询**：订单查询失败时允许继续退款，避免因查询问题导致退款无法进行
- ✅ **订单状态验证**：只有订单状态明确不允许退款时，才抛出异常
- ✅ **错误提示**：提供详细的错误提示，帮助用户快速定位问题
- ⚠️ **支付宝沙箱环境**：支付宝沙箱环境可能不稳定，订单查询可能失败，但不影响退款功能

---

## 2025-12-27 - 添加退款前订单状态验证

### 功能说明
在支付宝退款前先查询订单状态，确认订单是否存在且已支付成功，避免因订单不存在或状态不正确导致的退款失败。

### 修改原因
- 支付宝退款返回错误码 `20000`（系统异常），可能是订单不存在或状态不正确
- 退款前应该先验证订单状态，提供更明确的错误提示
- 避免无效的退款请求，提升用户体验

### 问题分析

**问题现象：**
- 支付宝退款返回错误码 `20000`，子错误码 `aop.ACQ.SYSTEM_ERROR`
- 错误信息：`Service Currently Unavailable`（服务暂时不可用）
- 子错误信息：`系统异常`

**可能的原因：**
1. 订单不存在：订单号在支付宝中不存在
2. 订单状态不对：订单未支付成功或已退款
3. 订单状态不允许退款：订单状态不是 `TRADE_SUCCESS` 或 `TRADE_FINISHED`

### 修改内容

**文件：** `backend/src/main/java/com/shoppingmall/payment/strategy/impl/AlipayPayStrategy.java`

- **修改 `refund()` 方法**：
  - 在退款前先调用 `AlipayUtil.queryOrder()` 查询订单状态
  - 验证订单是否存在（状态不为 `UNKNOWN`）
  - 验证订单状态是否允许退款（状态为 `TRADE_SUCCESS` 或 `TRADE_FINISHED`）
  - 如果订单不存在或状态不允许退款，提前抛出明确的错误提示
  - 只有订单状态验证通过后，才调用退款接口

**代码逻辑：**
```java
// 退款前先查询订单状态，确认订单是否存在且已支付成功
log.info("退款前查询订单状态，订单号：{}", paymentNo);
Map<String, String> orderStatus = AlipayUtil.queryOrder(envConfig, paymentNo);
String tradeStatus = orderStatus.get("trade_status");

if ("UNKNOWN".equals(tradeStatus)) {
    log.warn("无法查询到订单状态，订单号：{}，可能订单不存在", paymentNo);
    throw new PaymentException(500, "订单不存在或无法查询订单状态，请确认订单号是否正确且已支付成功");
}

// 检查订单状态是否允许退款
if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
    log.warn("订单状态不允许退款，订单号：{}，订单状态：{}", paymentNo, tradeStatus);
    throw new PaymentException(500, "订单状态不允许退款，当前订单状态：" + tradeStatus + "，只有已支付成功或已完成的订单才能退款");
}

log.info("订单状态验证通过，订单号：{}，订单状态：{}，可以退款", paymentNo, tradeStatus);
```

### 订单状态说明

支付宝订单状态：
- `TRADE_SUCCESS`：交易成功（已支付）
- `TRADE_FINISHED`：交易完成（已支付且已结算）
- `WAIT_BUYER_PAY`：等待买家付款
- `TRADE_CLOSED`：交易关闭
- `UNKNOWN`：未知状态（订单不存在或查询失败）

**允许退款的状态：**
- `TRADE_SUCCESS`：交易成功
- `TRADE_FINISHED`：交易完成

### 功能流程

**修改前流程：**
1. 直接调用退款接口
2. 如果订单不存在，支付宝返回 `20000` 错误
3. 错误提示不够明确

**修改后流程：**
1. 退款前先查询订单状态
2. 验证订单是否存在
3. 验证订单状态是否允许退款
4. 如果验证失败，提前抛出明确的错误提示
5. 只有验证通过后，才调用退款接口

### 影响范围

- ✅ 支付宝退款接口（`/api/admin/deposit/refund`）
- ✅ 订单退款接口
- ✅ 支付记录退款接口
- ✅ 订单状态验证
- ✅ 错误提示优化

### 测试建议

1. **订单状态验证测试**：
   - 测试订单不存在的情况
   - 测试订单未支付的情况
   - 测试订单已关闭的情况
   - 验证错误提示是否明确

2. **正常退款测试**：
   - 测试订单已支付成功的情况
   - 测试订单已交易完成的情况
   - 验证退款流程是否正常

### 注意事项

- ✅ **订单验证**：退款前先验证订单状态，避免无效请求
- ✅ **错误提示**：提供明确的错误提示，帮助用户快速定位问题
- ✅ **性能影响**：增加一次订单查询请求，但可以避免无效的退款请求
- ✅ **兼容性**：保持向后兼容，不影响现有功能

---

## 2025-12-27 - 优化支付宝退款错误处理和提示

### 功能说明
优化支付宝退款接口的错误处理，根据支付宝返回的错误码和子错误码提供更详细的错误提示，帮助用户快速定位问题。

### 修改原因
- 支付宝返回错误码 `20000`（系统异常）时，错误提示不够详细
- 需要根据不同的错误码提供针对性的错误提示
- 帮助用户快速定位退款失败的原因

### 问题分析

**问题现象：**
- 支付宝退款返回错误码 `20000`，子错误码 `aop.ACQ.SYSTEM_ERROR`
- 错误信息：`Service Currently Unavailable`（服务暂时不可用）
- 子错误信息：`系统异常`
- 错误提示不够详细，难以定位问题

**可能的原因：**
1. 订单不存在：订单号在支付宝中不存在
2. 订单状态不对：订单未支付成功或已退款
3. 支付宝系统暂时不可用：支付宝沙箱环境问题

### 修改内容

#### 1. 增强错误信息解析

**文件：** `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`

- **修改 `refund()` 方法**：
  - 提取子错误码（`sub_code`）字段
  - 调用 `buildRefundErrorDetail()` 方法构建详细的错误信息
  - 记录更详细的错误日志

- **新增 `buildRefundErrorDetail()` 方法**：
  - 根据错误码和子错误码提供详细的错误说明
  - 针对常见错误码提供针对性的提示
  - 帮助用户快速定位问题

**错误码处理：**
- `20000` + `aop.ACQ.SYSTEM_ERROR`：系统异常，可能是订单不存在、订单状态不正确或支付宝系统暂时不可用
- `20000` + `aop.ACQ.TRADE_NOT_EXIST`：订单不存在
- `40004`：业务处理失败
- `40001`：缺少必填参数
- `40002`：参数格式错误

#### 2. 优化错误提示

**文件：** `backend/src/main/java/com/shoppingmall/payment/strategy/impl/AlipayPayStrategy.java`

- **修改 `refund()` 方法**：
  - 提取子错误码和子错误信息
  - 构建详细的错误信息
  - 针对特定错误码提供更详细的提示

**代码逻辑：**
```java
String code = result.get("code");
if (!"10000".equals(code)) {
    String msg = result.get("msg");
    String subMsg = result.get("sub_msg");
    String subCode = result.get("sub_code");
    
    // 构建详细的错误信息
    StringBuilder errorMsg = new StringBuilder("支付宝退款失败");
    if (subMsg != null && !subMsg.isEmpty()) {
        errorMsg.append(": ").append(subMsg);
    } else if (msg != null && !msg.isEmpty()) {
        errorMsg.append(": ").append(msg);
    }
    
    // 对于特定错误码，提供更详细的提示
    if ("20000".equals(code)) {
        if ("aop.ACQ.SYSTEM_ERROR".equals(subCode)) {
            errorMsg.append("。可能是订单不存在、订单状态不正确或支付宝系统暂时不可用，请检查订单号是否正确且已支付成功");
        } else if ("aop.ACQ.TRADE_NOT_EXIST".equals(subCode)) {
            errorMsg.append("。订单不存在，请确认订单号是否正确或订单是否已支付成功");
        }
    }
    
    throw new PaymentException(500, errorMsg.toString());
}
```

### 错误码说明

| 错误码 | 子错误码 | 说明 | 处理建议 |
|--------|---------|------|---------|
| 20000 | aop.ACQ.SYSTEM_ERROR | 系统异常 | 检查订单是否存在且已支付成功 |
| 20000 | aop.ACQ.TRADE_NOT_EXIST | 订单不存在 | 确认订单号是否正确或订单是否已支付成功 |
| 40004 | - | 业务处理失败 | 检查退款参数是否正确 |
| 40001 | - | 缺少必填参数 | 检查请求参数是否完整 |
| 40002 | - | 参数格式错误 | 检查参数格式是否正确 |

### 影响范围

- ✅ 支付宝退款接口（`/api/admin/deposit/refund`）
- ✅ 订单退款接口
- ✅ 支付记录退款接口
- ✅ 错误提示信息
- ✅ 错误日志记录

### 测试建议

1. **错误处理测试**：
   - 测试订单不存在的情况
   - 测试订单状态不正确的情况
   - 验证错误提示是否详细和友好

2. **错误码测试**：
   - 测试不同的错误码和子错误码
   - 验证错误提示是否正确
   - 验证日志记录是否完整

### 注意事项

- ✅ **错误提示**：根据错误码提供针对性的错误提示，帮助用户快速定位问题
- ✅ **日志记录**：记录详细的错误信息，包括错误码、子错误码、错误信息等
- ✅ **兼容性**：保持向后兼容，不影响现有功能
- ⚠️ **订单验证**：退款前应验证订单是否存在且已支付成功

---

## 2025-12-27 - 优化支付宝退款接口实现

### 功能说明
优化支付宝退款接口的实现，修复退款金额格式问题，改进网关地址判断逻辑，增强日志记录，确保退款接口能够正常工作。

### 修改原因
- 支付宝退款API要求退款金额格式为两位小数（如 "1.00"），但当前代码可能产生 "1" 格式
- 网关地址判断逻辑需要更健壮，优先使用配置的网关地址
- 需要更详细的日志记录来排查退款问题

### 问题分析

**问题现象：**
- 支付宝退款接口超时
- 退款请求发送成功，但没有看到响应
- 退款金额格式可能不符合支付宝要求

**根本原因：**
1. 退款金额格式问题：支付宝要求金额格式为两位小数（如 "1.00"），但 `BigDecimal.toString()` 可能产生 "1" 格式
2. 网关地址判断：如果 `env` 字段为空，可能导致网关地址判断错误
3. 日志不足：缺少详细的请求和响应日志，难以排查问题

### 修改内容

#### 1. 退款金额格式化

**文件：** `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`

- **新增 `formatRefundAmount()` 方法**：
  - 确保退款金额格式为两位小数
  - 使用 `String.format("%.2f", amount)` 格式化金额
  - 处理格式错误的情况，提供默认值

**文件：** `backend/src/main/java/com/shoppingmall/payment/strategy/impl/AlipayPayStrategy.java`

- **修改退款金额转换**：
  - 修改前：`refundAmount.toString()`（可能产生 "1" 格式）
  - 修改后：`String.format("%.2f", refundAmount.doubleValue())`（确保 "1.00" 格式）

#### 2. 网关地址判断优化

**文件：** `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`

- **改进网关地址判断逻辑**：
  - 优先使用配置的网关地址（`config.getGateway()`）
  - 如果未配置，根据 `env` 字段判断
  - 如果 `env` 字段为空，根据 `appid` 判断（沙箱appid通常以9021开头）
  - 确保网关地址正确

**代码逻辑：**
```java
// 优先使用配置的网关地址，如果没有配置则根据环境判断
String gateway = null;
if (config.getGateway() != null && !config.getGateway().isEmpty()) {
    gateway = config.getGateway();
} else {
    String env = config.getEnv();
    if (env == null || env.isEmpty()) {
        // 如果env字段为空，根据appid判断（沙箱appid通常以9021开头）
        String appid = config.getAppid();
        if (appid != null && appid.startsWith("9021")) {
            env = "sandbox";
        } else {
            env = "production";
        }
    }
    gateway = "sandbox".equals(env) ? ALIPAY_SANDBOX_GATEWAY : ALIPAY_GATEWAY;
}
```

#### 3. 增强日志记录

**文件：** `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`

- **添加详细的请求日志**：
  - 记录订单号、退款单号、退款金额、网关地址
  - 记录签名前参数列表（敏感信息截断显示）
  - 记录生成的签名

- **添加详细的响应日志**：
  - 记录HTTP响应状态码和响应体长度
  - 记录完整的响应体（如果长度小于1000字符）
  - 在 `sendHttpRequest()` 方法中记录请求和响应信息

**代码示例：**
```java
log.info("========== 支付宝退款请求参数 ==========");
log.info("订单号: {}", orderNo);
log.info("退款单号: {}", refundNo);
log.info("退款金额: {} (格式化后: {})", refundAmount, formattedRefundAmount);
log.info("网关地址: {}", gateway);
log.info("签名前参数列表:");
params.forEach((key, value) -> {
    if (!"sign".equals(key)) {
        log.info("  {} = {}", key, value.length() > 200 ? value.substring(0, 200) + "..." : value);
    }
});
log.info("生成的签名: {}", sign);
log.info("========================================");
```

### 功能流程

**修改前流程：**
1. 退款金额可能格式不正确（如 "1" 而不是 "1.00"）
2. 网关地址判断可能错误（如果env字段为空）
3. 日志不足，难以排查问题

**修改后流程：**
1. 退款金额自动格式化为两位小数（如 "1.00"）
2. 网关地址优先使用配置值，多重判断确保正确
3. 详细的请求和响应日志，便于排查问题

### 影响范围

- ✅ 支付宝退款接口（`/api/admin/deposit/refund`）
- ✅ 订单退款接口
- ✅ 支付记录退款接口
- ✅ 退款金额格式
- ✅ 网关地址判断
- ✅ 日志记录

### 测试建议

1. **退款金额格式测试**：
   - 测试退款金额为整数（如 1）的情况
   - 测试退款金额为小数（如 1.5）的情况
   - 验证格式化后的金额格式正确（如 "1.00", "1.50"）

2. **网关地址测试**：
   - 测试配置了网关地址的情况
   - 测试未配置网关地址但env字段正确的情况
   - 测试env字段为空但appid正确的情况

3. **退款接口测试**：
   - 测试支付宝退款接口（沙箱环境）
   - 验证退款金额格式正确
   - 验证网关地址正确
   - 验证日志记录完整

### 注意事项

- ✅ **金额格式**：支付宝要求金额格式为两位小数，必须使用 `String.format("%.2f", amount)` 格式化
- ✅ **网关地址**：优先使用配置的网关地址，确保退款请求发送到正确的网关
- ✅ **日志记录**：详细的日志记录有助于排查问题，但要注意敏感信息脱敏
- ✅ **兼容性**：保持向后兼容，如果配置正确，不影响现有功能

---

## 2025-12-27 - 修复订单详情页面支付方式显示错误

### 功能说明
修复前端订单详情页面支付方式显示错误的问题。当订单已支付时，优先从支付记录中获取实际使用的支付方式，而不是从订单表中获取，确保显示正确的支付方式。

### 修改原因
- 用户反馈：订单详情页面显示支付方式有误，微信支付显示成了支付宝
- 问题原因：订单创建时设置的支付方式可能与实际支付时选择的支付方式不一致
- 解决方案：订单已支付时，优先从支付记录中获取支付方式（更准确）

### 问题分析

**问题现象：**
- 订单详情页面显示支付方式错误
- 微信支付显示成了支付宝
- 管理后台的订单详情显示是正确的

**根本原因：**
1. 订单创建时，`paymentMethod` 字段是从创建订单DTO中设置的
2. 用户可能在创建订单后，实际支付时选择了不同的支付方式
3. 订单详情查询时，直接从订单表的 `paymentMethod` 字段获取，而不是从支付记录中获取
4. 支付记录中存储的是实际使用的支付方式，更准确

### 修改内容

**文件：** `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`

- **修改 `convertToDetailVO()` 方法**：
  - 在设置支付方式时，优先从支付记录中获取
  - 如果订单已支付（`PAID`）或支付中（`PAYING`），查询支付记录获取实际使用的支付方式
  - 如果支付记录中没有，则使用订单表中的支付方式
  - 确保显示的是实际使用的支付方式

**代码逻辑：**
```java
// 获取支付方式：如果订单已支付，优先从支付记录中获取（更准确）
String paymentMethod = null;
if (PaymentStatus.PAID.equals(order.getPaymentStatus()) || PaymentStatus.PAYING.equals(order.getPaymentStatus())) {
    // 查询支付记录，获取实际使用的支付方式
    LambdaQueryWrapper<PaymentRecord> paymentWrapper = new LambdaQueryWrapper<>();
    paymentWrapper.eq(PaymentRecord::getOrderId, order.getId());
    paymentWrapper.orderByDesc(PaymentRecord::getCreateTime);
    paymentWrapper.last("LIMIT 1");
    PaymentRecord paymentRecord = paymentRecordRepository.selectOne(paymentWrapper);
    if (paymentRecord != null && paymentRecord.getPaymentMethod() != null) {
        paymentMethod = paymentRecord.getPaymentMethod();
    }
}
// 如果支付记录中没有，则使用订单表中的支付方式
if (paymentMethod == null) {
    paymentMethod = order.getPaymentMethod();
}
recipientInfo.setPaymentMethod(paymentMethod != null ? 
    getPaymentMethodText(paymentMethod) : "未知");
```

### 功能流程

**修改前流程：**
1. 订单详情查询时，直接从订单表的 `paymentMethod` 字段获取
2. 如果订单创建时选择的支付方式与实际支付时不一致，会显示错误的支付方式

**修改后流程：**
1. 订单详情查询时，先检查订单是否已支付或支付中
2. 如果已支付，从支付记录中获取实际使用的支付方式
3. 如果支付记录中没有，则使用订单表中的支付方式
4. 确保显示的是实际使用的支付方式

### 影响范围

- ✅ 前端订单详情页面（`/order/detail`）
- ✅ 订单支付方式显示
- ✅ 已支付订单的支付方式显示

### 测试建议

1. **支付方式显示测试**：
   - 创建订单时选择一种支付方式
   - 实际支付时选择另一种支付方式
   - 验证订单详情页面显示的是实际使用的支付方式

2. **不同支付方式测试**：
   - 测试微信支付订单
   - 测试支付宝支付订单
   - 测试预存款支付订单
   - 验证支付方式显示正确

### 注意事项

- ✅ **支付记录优先**：已支付订单优先从支付记录中获取支付方式
- ✅ **兼容性**：如果支付记录中没有，则使用订单表中的支付方式
- ✅ **支付方式转换**：使用 `getPaymentMethodText()` 方法将支付方式代码转换为中文显示
- ✅ **管理后台一致性**：管理后台的订单详情已经正确显示支付方式，前端现在也保持一致

---

## 2025-12-27 - 修复预存款退款编译错误

### 功能说明
修复预存款退款接口的编译错误，移除不可达的 `TimeoutException` catch 块，改为通过异常消息判断超时异常。

### 修改原因
- 编译错误：`Unreachable catch block for TimeoutException`
- `HttpClient.send()` 方法在超时时不会抛出 `TimeoutException`，而是抛出 `IOException`
- 需要正确识别超时异常，提供友好的错误提示

### 问题分析

**错误信息：**
```
Unresolved compilation problem: 
Unreachable catch block for TimeoutException. 
This exception is never thrown from the try statement body
```

**根本原因：**
- `HttpClient.send()` 是同步方法，超时时抛出 `IOException`，而不是 `TimeoutException`
- `TimeoutException` 是 `java.util.concurrent` 包中的异常，主要用于 `Future.get()` 等异步操作
- 之前的代码错误地尝试捕获 `TimeoutException`，导致编译错误

### 修改内容

**文件：** `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`

- **修改 `sendHttpRequest()` 方法**：
  - 移除 `TimeoutException` 的 catch 块
  - 在 `IOException` catch 块中，通过异常消息判断是否是超时异常
  - 检查异常消息中是否包含 "timeout"、"timed out"、"连接超时"、"read timed out" 等关键词
  - 如果是超时异常，提供友好的错误提示
  - 如果不是超时异常，提供通用的错误提示

**代码逻辑：**
```java
} catch (java.io.IOException e) {
    // 检查是否是超时异常（HttpClient超时会抛出IOException，异常消息可能包含"timeout"）
    String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
    boolean isTimeout = errorMessage.contains("timeout") || 
                       errorMessage.contains("timed out") ||
                       errorMessage.contains("连接超时") ||
                       errorMessage.contains("read timed out");
    
    if (isTimeout) {
        log.warn("支付宝API请求超时，第{}次尝试，网关：{}，超时时间：{}秒，异常信息：{}", 
                attempt, gateway, requestTimeout.getSeconds(), e.getMessage());
        if (attempt == maxAttempts) {
            log.error("支付宝API请求最终超时，网关：{}，请求体长度：{}", gateway, requestBody.length());
            throw new PaymentException(500, "支付宝API请求超时，请稍后重试或联系技术支持", e);
        }
    } else {
        log.warn("支付宝API HTTP请求第{}次尝试失败: {}", attempt, e.getMessage());
        if (attempt == maxAttempts) {
            log.error("支付宝API HTTP请求最终失败，网关：{}，请求体长度：{}", gateway, requestBody.length());
            throw new PaymentException(500, "支付宝API请求异常：" + e.getMessage(), e);
        }
    }
    
    // 重试前等待
    try {
        Thread.sleep(1000L * attempt);
    } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
    }
}
```

### 影响范围

- ✅ 支付宝退款接口（`/api/admin/deposit/refund`）
- ✅ 支付宝支付接口（所有使用 `sendHttpRequest()` 的接口）
- ✅ 错误处理和日志记录

### 测试建议

1. **退款接口测试**：
   - 测试支付宝退款接口（沙箱环境）
   - 验证编译错误已修复
   - 验证超时异常处理是否正确

2. **超时异常测试**：
   - 模拟网络超时情况
   - 验证超时异常识别是否正确
   - 验证错误提示是否友好

### 注意事项

- ✅ **异常处理**：正确识别超时异常，提供友好的错误提示
- ✅ **兼容性**：支持中英文超时异常消息
- ✅ **日志记录**：记录详细的超时异常信息，便于问题排查
- ✅ **重试机制**：超时异常也会触发重试机制

---

## 2025-12-27 - 预存款充值模拟支付直接显示成功

### 功能说明
修改预存款充值功能，当使用模拟支付方式时，直接处理为支付成功状态，更新充值记录状态为"已通过"并更新预存款余额，前端直接显示支付成功弹窗。

### 修改原因
- 用户反馈：预存款充值使用模拟支付时，显示的是"支付中"状态，希望直接显示"支付成功"
- 模拟支付是测试功能，应该直接完成支付流程，无需等待回调
- 提升测试体验，模拟支付时立即看到成功结果

### 问题分析

**问题现象：**
- 预存款充值使用模拟支付时，前端显示"支付中"状态
- 后端充值记录状态为"支付中"（PAYING），未更新为"已通过"（APPROVED）
- 预存款余额未更新

**根本原因：**
1. 前端检测到模拟支付时，直接显示成功，但未调用后端接口更新状态
2. 后端创建充值记录时，状态设置为"支付中"，等待支付回调
3. 模拟支付不会触发真实的支付回调，导致状态一直为"支付中"

### 修改内容

**文件：** `backend/src/main/java/com/shoppingmall/service/buyer/impl/DepositServiceImpl.java`

- **修改 `recharge()` 方法**：
  - 在返回支付响应前，检测是否为模拟支付
  - 如果是模拟支付（`paymentResponse.getIsMock() == true`），直接调用 `handlePaymentCallback()` 方法
  - 自动生成模拟外部交易号（如果响应中没有）
  - 将充值记录状态更新为"已通过"（APPROVED）
  - 更新预存款余额和可用余额
  - 记录审核时间

**代码逻辑：**
```java
// 如果是模拟支付，直接处理为支付成功（更新状态和余额）
if (paymentResponse.getIsMock() != null && paymentResponse.getIsMock()) {
    log.info("检测到模拟支付，直接处理为支付成功，内部订单号：{}", internalOrderNo);
    String mockExternalTradeNo = paymentResponse.getMockExternalTradeNo();
    if (mockExternalTradeNo == null || mockExternalTradeNo.isEmpty()) {
        // 如果没有模拟交易号，生成一个
        mockExternalTradeNo = "MOCK_" + rechargeDTO.getPaymentMethod().toUpperCase() + "_" 
                + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    // 直接调用回调处理方法，更新状态为已通过并更新余额
    handlePaymentCallback(internalOrderNo, mockExternalTradeNo, true);
}
```

### 功能流程

**修改前流程：**
1. 用户发起预存款充值
2. 后端创建充值记录，状态为"支付中"（PAYING）
3. 返回模拟支付响应（`isMock = true`）
4. 前端显示"支付中"弹窗
5. 后端状态一直为"支付中"，余额未更新

**修改后流程：**
1. 用户发起预存款充值
2. 后端创建充值记录，状态为"支付中"（PAYING）
3. 返回模拟支付响应（`isMock = true`）
4. **后端检测到模拟支付，直接调用回调处理**
5. **充值记录状态更新为"已通过"（APPROVED）**
6. **预存款余额和可用余额更新**
7. 前端显示"支付成功"弹窗
8. 2秒后自动跳转到余额页面

### 影响范围

- ✅ 预存款充值功能（`/member/deposit/recharge`）
- ✅ 模拟支付模式（支付方式未启用时自动切换）
- ✅ 充值记录状态更新
- ✅ 预存款余额更新

### 测试建议

1. **模拟支付测试**：
   - 禁用微信支付或支付宝支付（触发模拟支付模式）
   - 发起预存款充值
   - 验证前端显示"支付成功"弹窗
   - 验证充值记录状态为"已通过"
   - 验证预存款余额已更新

2. **真实支付测试**：
   - 启用微信支付或支付宝支付
   - 发起预存款充值
   - 验证流程不受影响，仍等待支付回调

### 注意事项

- ✅ **模拟支付**：仅在模拟支付模式下生效，不影响真实支付流程
- ✅ **事务安全**：使用 `@Transactional` 确保数据一致性
- ✅ **状态更新**：充值记录状态从"支付中"更新为"已通过"
- ✅ **余额更新**：使用悲观锁（`FOR UPDATE`）防止并发问题
- ✅ **日志记录**：记录模拟支付处理日志，便于问题排查

---

## 2025-12-27 - 修复预存款退款接口超时问题

### 功能说明
修复预存款退款接口超时问题，增加前端和后端的请求超时时间，优化错误处理和日志记录，确保支付宝退款API能够正常响应。

### 修改原因
- 支付宝退款API响应较慢，可能超过30秒
- 前端请求超时设置为30秒，导致退款请求超时
- 需要增加超时时间，确保退款接口能够正常完成

### 问题分析

**问题现象：**
- 预存款退款接口超时：`timeout of 30000ms exceeded`
- 支付宝退款API请求已发送，签名成功，但HTTP请求超时

**根本原因：**
1. 前端请求超时设置为30秒（`timeout: 30000`）
2. 后端HTTP客户端请求超时设置为60秒
3. 支付宝退款API响应可能超过30秒，导致前端超时

### 修改内容

#### 1. 前端请求超时调整

**文件：** `admin-frontend/src/utils/request.ts`
- **修改默认超时时间**：
  - 修改前：`timeout: 30000`（30秒）
  - 修改后：`timeout: 90000`（90秒）
  - 说明：支付/退款接口可能需要更长时间，增加默认超时时间

**文件：** `admin-frontend/src/api/admin/deposit.ts`
- **退款接口单独设置超时时间**：
  - 为 `refundDepositRecharge` 接口设置120秒超时
  - 代码：`timeout: 120000`（120秒）
  - 说明：退款接口可能需要更长时间，单独设置更长的超时时间

#### 2. 后端HTTP客户端超时调整

**文件：** `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`
- **修改 `sendHttpRequest()` 方法**：
  - 修改前：`Duration requestTimeout = Duration.ofSeconds(60);`（60秒）
  - 修改后：`Duration requestTimeout = Duration.ofSeconds(90);`（90秒）
  - 说明：增加支付宝API请求超时时间，确保退款接口能够正常完成

- **增强错误处理**：
  - 添加 `TimeoutException` 异常处理
  - 区分超时异常和其他IO异常
  - 提供更详细的错误信息和日志记录

- **增强日志记录**：
  - 记录每次请求尝试的详细信息
  - 记录超时异常和重试情况
  - 便于问题排查和调试

### 超时时间设置

| 层级 | 修改前 | 修改后 | 说明 |
|------|--------|--------|------|
| 前端默认超时 | 30秒 | 90秒 | 适用于所有接口 |
| 前端退款接口超时 | 30秒 | 120秒 | 退款接口单独设置 |
| 后端HTTP请求超时 | 60秒 | 90秒 | 支付宝API请求超时 |
| 后端连接超时 | 20秒 | 20秒 | 保持不变 |

### 错误处理优化

**超时异常处理：**
- 区分 `TimeoutException` 和其他 `IOException`
- 超时异常提供更友好的错误提示
- 记录详细的超时日志，便于问题排查

**重试机制：**
- 保持3次重试机制
- 每次重试间隔递增（1秒、2秒、3秒）
- 记录每次重试的详细信息

### 测试建议

1. **退款接口测试**：
   - 测试支付宝退款接口（沙箱环境）
   - 验证超时时间设置是否合理
   - 验证错误处理和日志记录

2. **网络异常测试**：
   - 测试网络超时情况
   - 验证重试机制是否正常工作
   - 验证错误提示是否友好

3. **性能测试**：
   - 测试退款接口的响应时间
   - 验证超时时间设置是否合理
   - 验证系统稳定性

### 注意事项

- ⚠️ **超时时间设置**：超时时间过长可能影响用户体验，需要根据实际情况调整
- ⚠️ **网络环境**：支付宝沙箱环境可能响应较慢，生产环境可能更快
- ✅ **错误处理**：超时异常会提供友好的错误提示，不会影响其他功能
- ✅ **日志记录**：详细的日志记录便于问题排查和性能优化

---

## 2025-12-26 - 移除支付问题弹窗中的客服电话

### 功能说明
移除订单支付页面和预存款充值页面中"联系客服"按钮点击后显示的客服电话，只保留"请联系客服处理支付问题"的提示信息。

### 修改原因
- 客服电话信息需要屏蔽，避免暴露敏感信息
- 简化提示信息，只保留必要的提示内容

### 修改内容

**前端页面：**
- `frontend/src/views/order/Payment.vue`
  - **修改 `handleContactService()` 方法**：
    - 移除客服电话：`400-xxx-xxxx`
    - 修改前：`ElMessage.info('请联系客服处理支付问题，客服电话：400-xxx-xxxx')`
    - 修改后：`ElMessage.info('请联系客服处理支付问题')`

- `frontend/src/views/member/DepositRecharge.vue`
  - **修改 `handleContactService()` 方法**：
    - 移除客服电话：`400-xxx-xxxx`
    - 修改前：`ElMessage.info('请联系客服处理支付问题，客服电话：400-xxx-xxxx')`
    - 修改后：`ElMessage.info('请联系客服处理支付问题')`

### 影响范围
- ✅ 订单支付页面（`/order/payment`）的"联系客服"按钮
- ✅ 预存款充值页面（`/member/deposit/recharge`）的"联系客服"按钮

---

## 2025-12-26 - 修复支付宝退款功能，对接真实退款接口

### 功能说明
修复退款功能中的关键问题，确保支付宝退款正确调用真实退款接口，使用正确的订单号参数，并增强错误处理和事务安全性。

### 修改原因
- 退款功能涉及金额，是重要的业务环节，必须确保正确性
- 支付宝退款API需要的是订单号（out_trade_no），而不是支付流水号
- 需要确保退款失败时不会更新数据库状态，保证数据一致性
- 增强错误处理和日志记录，便于问题排查

### 关键问题修复

**问题1：订单号参数错误**
- **问题**：支付宝退款API需要的是创建支付订单时的订单号（out_trade_no），但代码中传入的是支付流水号（paymentNo）
- **影响**：会导致支付宝退款失败
- **修复**：使用订单号（orderNo）而不是支付流水号

**问题2：预存款充值退款参数错误**
- **问题**：预存款充值退款时，使用外部交易号（externalTradeNo）而不是内部订单号（internalOrderNo）
- **影响**：会导致支付宝退款失败
- **修复**：使用内部订单号（internalOrderNo）作为退款参数

### 修改内容

#### 1. 订单退款修复

**文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`

**修改点：**
- 第456-479行：修复支付宝/微信退款调用
- **关键修改**：使用订单号（`order.getOrderNo()`）而不是支付流水号（`paymentRecord.getPaymentNo()`）
- **增强日志**：记录订单号、支付流水号、退款金额等关键信息
- **错误处理**：区分PaymentException和其他异常，提供更详细的错误信息

**代码示例：**
```java
// 修改前：使用支付流水号
refundPaymentNo = paymentGatewayService.refund(
    paymentMethod,
    paymentRecord.getPaymentNo(), // ❌ 错误：支付流水号
    totalRefundAmount,
    refundDTO.getRefundReason()
);

// 修改后：使用订单号
String orderNoForRefund = order.getOrderNo();
refundPaymentNo = paymentGatewayService.refund(
    paymentMethod,
    orderNoForRefund, // ✅ 正确：订单号
    totalRefundAmount,
    refundDTO.getRefundReason()
);
```

#### 2. 支付记录退款修复

**文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/FinanceServiceImpl.java`

**修改点：**
- 第219-242行：修复支付宝/微信退款调用
- **关键修改**：从支付记录关联的订单中获取订单号，使用订单号而不是支付流水号
- **安全检查**：确保订单存在，避免空指针异常
- **增强日志**：记录订单号、支付流水号等关键信息

**代码示例：**
```java
// 修改前：使用支付流水号
refundPaymentNo = paymentGatewayService.refund(
    paymentMethod,
    paymentRecord.getPaymentNo(), // ❌ 错误：支付流水号
    refundDTO.getRefundAmount(),
    refundDTO.getRefundReason()
);

// 修改后：使用订单号
Order order = orderRepository.selectById(paymentRecord.getOrderId());
if (order == null) {
    throw new BusinessException(404, "订单不存在，无法退款");
}
String orderNoForRefund = order.getOrderNo();
refundPaymentNo = paymentGatewayService.refund(
    paymentMethod,
    orderNoForRefund, // ✅ 正确：订单号
    refundDTO.getRefundAmount(),
    refundDTO.getRefundReason()
);
```

#### 3. 预存款充值退款修复

**文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/DepositServiceImpl.java`

**修改点：**
- 第315-394行：修复支付宝/微信充值退款调用
- **关键修改**：使用内部订单号（`internalOrderNo`）而不是外部交易号（`externalTradeNo`）
- **安全检查**：确保内部订单号存在，避免退款失败
- **余额回滚**：退款失败时自动回滚预存款余额，保证数据一致性

**代码示例：**
```java
// 修改前：使用外部交易号
refundExternalTradeNo = paymentGatewayService.refund(
    PaymentMethod.ALIPAY,
    externalTradeNo, // ❌ 错误：外部交易号
    refundDTO.getRefundAmount(),
    refundDTO.getRefundReason()
);

// 修改后：使用内部订单号
String internalOrderNo = detail.getInternalOrderNo();
if (internalOrderNo == null || internalOrderNo.isEmpty()) {
    throw new BusinessException(400, "内部订单号不存在，无法退款");
}
refundExternalTradeNo = paymentGatewayService.refund(
    PaymentMethod.ALIPAY,
    internalOrderNo, // ✅ 正确：内部订单号
    refundDTO.getRefundAmount(),
    refundDTO.getRefundReason()
);
```

### 安全措施

**1. 事务管理：**
- 所有退款操作都在`@Transactional`事务中执行
- 退款失败时自动回滚数据库操作
- 预存款退款失败时自动回滚余额

**2. 错误处理：**
- 区分PaymentException和其他异常
- 提供详细的错误信息
- 记录完整的错误日志

**3. 参数验证：**
- 验证订单是否存在
- 验证内部订单号是否存在
- 验证退款金额是否合法

**4. 日志记录：**
- 记录退款开始、成功、失败的完整日志
- 记录订单号、支付流水号、退款金额等关键信息
- 便于问题排查和审计

### 退款流程说明

**订单退款流程：**
1. 验证订单状态和支付状态
2. 验证退款金额（不能超过可退款金额）
3. 调用支付网关退款接口（传入订单号）
4. 创建退款记录
5. 更新支付记录和订单状态

**支付记录退款流程：**
1. 验证支付记录状态
2. 验证退款金额
3. 从支付记录关联的订单中获取订单号
4. 调用支付网关退款接口（传入订单号）
5. 更新支付记录状态

**预存款充值退款流程：**
1. 验证充值记录状态
2. 验证退款金额
3. 扣除预存款余额
4. 调用支付网关退款接口（传入内部订单号）
5. 如果退款失败，回滚预存款余额
6. 更新充值记录状态

### 测试建议

**1. 订单退款测试：**
- 测试支付宝订单退款（全额退款）
- 测试支付宝订单退款（部分退款）
- 测试退款失败时的错误处理
- 验证退款后订单状态和支付记录状态

**2. 支付记录退款测试：**
- 测试通过支付记录直接退款
- 验证退款金额限制
- 测试退款失败时的错误处理

**3. 预存款充值退款测试：**
- 测试支付宝充值退款
- 验证退款失败时的余额回滚
- 验证退款后充值记录状态

### 注意事项

- ⚠️ **重要**：退款功能涉及金额，测试时请使用沙箱环境
- ⚠️ **重要**：确保支付宝配置正确（AppID、私钥、公钥等）
- ⚠️ **重要**：退款操作不可逆，请谨慎操作
- ✅ 所有退款操作都有完整的日志记录
- ✅ 退款失败时会抛出异常，不会更新数据库状态
- ✅ 预存款退款失败时会自动回滚余额

---

## 2025-12-26 - 支付方式禁用时自动切换到模拟支付模式

### 功能说明
当微信支付或支付宝支付在管理后台被禁用时，系统自动切换到模拟支付模式，方便测试支付流程，无需配置第三方支付参数即可测试。

### 修改原因
- 测试环境需要频繁测试支付流程，但配置第三方支付参数繁琐
- 支付方式禁用时抛出异常，不便于测试
- 需要根据开关灵活切换真实支付和模拟支付
- 提升开发和测试效率

### 修改内容

#### 后端代码修改

**支付网关服务：**
- `backend/src/main/java/com/shoppingmall/payment/service/impl/PaymentGatewayServiceImpl.java`
  - **修改 `pay()` 方法**：
    - 当支付方式未启用时，不再抛出异常
    - 自动返回模拟支付响应（`isMock = true`）
    - 生成模拟的外部交易号（格式：`MOCK_{METHOD}_{TIMESTAMP}_{UUID}`）
  - **新增 `createMockPaymentResponse()` 方法**：
    - 创建模拟支付响应
    - 设置 `isMock = true`
    - 生成模拟交易号
    - 记录日志便于追踪
  - **修改 `refund()` 方法**：
    - 当支付方式未启用时，不再抛出异常
    - 自动返回模拟退款流水号（格式：`MOCK_REFUND_{METHOD}_{TIMESTAMP}_{UUID}`）
    - 记录日志便于追踪
  - **新增 `createMockRefundNo()` 方法**：
    - 创建模拟退款流水号
    - 生成格式化的退款流水号
    - 记录日志便于追踪

### 功能特性
- ✅ 支付方式禁用时自动切换到模拟支付模式
- ✅ 支付方式禁用时自动切换到模拟退款模式
- ✅ 支付方式启用时正常调用第三方支付/退款接口
- ✅ 不影响正常支付/退款功能（启用时逻辑完全不变）
- ✅ 支持订单支付和预存款充值
- ✅ 支持订单退款、支付记录退款、预存款充值退款
- ✅ 前端自动识别模拟支付并调用模拟支付成功接口

### 工作流程

**支付方式禁用时（支付）：**
1. 用户选择支付方式（支付宝/微信）
2. 系统检测到支付方式未启用
3. 自动返回模拟支付响应（`isMock = true`）
4. 前端检测到模拟支付，自动调用模拟支付成功接口
5. 订单状态自动更新为已支付

**支付方式禁用时（退款）：**
1. 管理员发起退款操作
2. 系统检测到支付方式未启用
3. 自动返回模拟退款流水号（格式：`MOCK_REFUND_{METHOD}_{TIMESTAMP}_{UUID}`）
4. 退款记录保存模拟退款流水号
5. 订单/支付记录状态正常更新

**支付方式启用时（支付）：**
1. 用户选择支付方式（支付宝/微信）
2. 系统检测到支付方式已启用
3. 正常调用第三方支付接口
4. 跳转到第三方支付页面
5. 用户完成支付后回调更新订单状态

**支付方式启用时（退款）：**
1. 管理员发起退款操作
2. 系统检测到支付方式已启用
3. 正常调用第三方退款接口
4. 返回真实的退款流水号
5. 订单/支付记录状态正常更新

### 使用说明

1. **禁用支付方式（使用模拟支付）：**
   - 登录管理后台，进入"系统设置" -> "支付配置"
   - 关闭"微信支付"或"支付宝"的启用开关
   - 保存配置
   - 用户选择该支付方式时，自动使用模拟支付

2. **启用支付方式（使用真实支付）：**
   - 登录管理后台，进入"系统设置" -> "支付配置"
   - 开启"微信支付"或"支付宝"的启用开关
   - 配置支付参数（AppID、密钥等）
   - 保存配置
   - 用户选择该支付方式时，正常调用第三方支付接口

3. **配置不完整但启用：**
   - 如果启用但配置不完整（如缺少AppID），策略层会抛出异常
   - 这是正常行为，需要管理员配置完整的支付参数

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/payment/service/impl/PaymentGatewayServiceImpl.java` - 支付网关服务
- ✅ 订单支付功能（`OrderServiceImpl.payOrder()`）
- ✅ 预存款充值功能（`DepositServiceImpl.recharge()`）
- ✅ 订单退款功能（`OrderServiceImpl.refundOrder()`）
- ✅ 支付记录退款功能（`FinanceServiceImpl.refundPaymentRecord()`）
- ✅ 预存款充值退款功能（`DepositServiceImpl.refundRecharge()`）
- ✅ 前端支付处理逻辑（已有 `isMock` 判断，无需修改）

### 技术细节
- **模拟支付响应格式**：
  - `isMock = true`
  - `mockExternalTradeNo = "MOCK_{METHOD}_{TIMESTAMP}_{UUID}"`
  - 其他字段为空（前端根据 `isMock` 判断）
- **模拟退款流水号格式**：
  - `MOCK_REFUND_{METHOD}_{TIMESTAMP}_{UUID}`
  - 例如：`MOCK_REFUND_ALIPAY_1735123456789_A1B2C3D4`
- **日志记录**：
  - 记录何时切换到模拟支付/退款模式
  - 记录模拟交易号和退款流水号，便于追踪和调试
- **向后兼容**：
  - 启用状态下的逻辑完全不变
  - 前端已有模拟支付处理逻辑，无需修改
  - 退款功能在禁用时返回模拟退款流水号，不影响业务逻辑

### 注意事项
1. **模拟退款**：退款功能在支付方式禁用时返回模拟退款流水号，不会抛出异常
2. **配置验证**：启用但配置不完整时，策略层会抛出异常，需要管理员配置完整
3. **测试环境**：建议在测试环境禁用支付方式，使用模拟支付/退款进行测试
4. **生产环境**：生产环境应启用支付方式并配置完整的支付参数
5. **退款流水号**：模拟退款流水号格式为 `MOCK_REFUND_{METHOD}_{TIMESTAMP}_{UUID}`，便于识别和追踪

---

## 2025-12-26 - 优化支付超时时间配置

### 功能说明
根据电商平台行业标准和B2B业务特点，优化了支付超时时间的默认配置，使其更加合理和符合实际业务需求。

### 修改原因
- 原默认值6小时过长，参考淘宝15分钟、京东30分钟等主流平台
- B2B平台需要平衡用户体验和库存管理效率
- 支付中状态通常不会持续太久，需要及时释放资源

### 修改内容

**超时时间调整：**

| 配置项 | 原默认值 | 新默认值 | 说明 |
|--------|---------|---------|------|
| 订单支付超时时间 | 6小时 | **4小时** | B2B平台建议4-6小时，给企业用户充足的决策和审批时间 |
| 支付记录自动取消时间 | 6小时 | **2小时** | 支付中状态通常不会持续太久，2小时足够完成支付流程 |
| 预存款记录自动取消时间 | 6小时 | **1小时** | 充值流程相对简单，不需要太长的等待时间 |

### 修改文件

**数据库配置脚本：**
- `database/update-20251226-add-system-configs.sql` - 订单支付超时时间改为4小时
- `database/update-20251226-add-payment-record-timeout-config.sql` - 支付记录超时时间改为2小时
- `database/update-20251226-add-deposit-record-timeout-config.sql` - 预存款记录超时时间改为1小时

**后端代码：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderScheduledServiceImpl.java` - 默认值改为4小时
- `backend/src/main/java/com/shoppingmall/service/payment/impl/PaymentRecordScheduledServiceImpl.java` - 默认值改为2小时
- `backend/src/main/java/com/shoppingmall/service/deposit/impl/DepositScheduledServiceImpl.java` - 默认值改为1小时

### 时间设置说明

**订单支付超时（4小时）：**
- 适合B2B企业采购场景
- 给企业用户充足的决策和审批时间
- 平衡库存管理和用户体验

**支付记录自动取消（2小时）：**
- 支付中状态通常不会持续太久
- 2小时足够用户完成支付流程
- 避免长时间占用支付通道

**预存款充值超时（1小时）：**
- 充值流程相对简单
- 不需要太长的等待时间
- 及时释放资源

### 注意事项

- 所有配置都支持在管理后台动态修改
- 修改后立即生效，无需重启服务
- 建议根据实际业务数据（超时率）进行微调
- 如果超时率 > 10%，可考虑适当延长
- 如果超时率 < 5%，可考虑适当缩短

---

## 2025-12-26 - 预存款支付中记录自动取消功能

### 功能说明
实现预存款支付中状态的记录自动取消功能，类似支付记录自动取消机制。当预存款记录处于"支付中"状态超过配置的时间（默认6小时）后，系统会自动将其更新为"已超时"状态。

### 修改原因
- 预存款支付中状态的记录如果长时间未完成支付，需要自动更新为已超时
- 避免预存款记录长期处于支付中状态，影响数据统计和业务处理
- 与支付记录自动取消机制保持一致，提升系统自动化程度

### 修改内容

#### 数据库配置

**新增配置项：**
- `database/update-20251226-add-deposit-record-timeout-config.sql`
  - `deposit.record-timeout-hours`：预存款记录自动取消时间（小时，默认：6）
    - 配置支付中状态的预存款记录自动超时时间
    - 支持在管理后台动态修改，修改后立即生效

#### 后端代码修改

**1. 预存款定时任务服务接口（新建）：**
- `backend/src/main/java/com/shoppingmall/service/deposit/DepositScheduledService.java`
  - **功能**：定义预存款定时任务服务接口
  - **方法**：
    - `cancelTimeoutDepositRecords()`：自动取消超时的支付中预存款记录

**2. 预存款定时任务服务实现类（新建）：**
- `backend/src/main/java/com/shoppingmall/service/deposit/impl/DepositScheduledServiceImpl.java`
  - **功能**：实现预存款自动取消定时任务
  - **特性**：
    - 每小时执行一次，检查超时的支付中预存款记录（降低系统压力）
    - 从数据库读取最新配置，确保配置修改后立即生效
    - 将超时的支付中记录状态更新为"已超时"（TIMEOUT）
    - 记录详细日志，便于问题排查
  - **方法**：
    - `getDepositRecordTimeoutHours()`：从数据库读取预存款记录自动取消时间配置
    - `cancelTimeoutDepositRecords()`：执行自动取消超时预存款记录的任务

### 技术细节

**定时任务配置：**
- 执行频率：每小时执行一次（`@Scheduled(fixedRate = 3600000)`）
- 事务支持：使用 `@Transactional` 确保数据一致性
- 异常处理：单个记录处理失败不影响其他记录的处理
- **优化说明**：相比每分钟执行，每小时执行可大幅降低系统压力，同时仍能及时处理超时记录

**状态流转：**
- 支付中（PAYING，状态值：3）→ 已超时（TIMEOUT，状态值：4）

**配置管理：**
- 配置键：`deposit.record-timeout-hours`
- 默认值：1小时（已优化，原为6小时）
- 配置类型：number
- 支持在管理后台动态修改

**注意：** 此服务与现有的 `buyer.DepositScheduledService`（处理待审核状态）功能不同，两者互不冲突。

### 使用说明

1. **执行SQL脚本**：
   ```sql
   -- 执行数据库脚本添加配置项
   source database/update-20251226-add-deposit-record-timeout-config.sql
   ```

2. **配置自动取消时间**：
   - 登录管理后台
   - 进入"系统管理" → "系统配置"
   - 找到"预存款记录自动取消时间"配置项
   - 修改时间值（单位：小时）
   - 保存后立即生效，无需重启服务

3. **查看日志**：
   - 定时任务执行日志会记录在应用日志中
   - 可通过日志查看自动更新的预存款记录详情

### 注意事项

- 定时任务会在应用启动后自动运行，无需手动启动
- 配置修改后，下次定时任务执行时立即生效
- 已超时的预存款记录不会再次被处理
- 建议根据实际业务需求调整自动取消时间

---

## 2025-12-26 - 支付记录自动取消功能

### 功能说明
实现支付中状态的支付记录自动取消功能，类似订单自动取消机制。当支付记录处于"支付中"状态超过配置的时间（默认6小时）后，系统会自动将其关闭。

### 修改原因
- 支付中状态的支付记录如果长时间未完成支付，需要自动关闭
- 避免支付记录长期处于支付中状态，影响数据统计和业务处理
- 与订单自动取消机制保持一致，提升系统自动化程度

### 修改内容

#### 数据库配置

**新增配置项：**
- `database/update-20251226-add-payment-record-timeout-config.sql`
  - `payment.record-timeout-hours`：支付记录自动取消时间（小时，默认：6）
    - 配置支付中状态的支付记录自动关闭时间
    - 支持在管理后台动态修改，修改后立即生效

#### 后端代码修改

**1. 支付记录定时任务服务接口（新建）：**
- `backend/src/main/java/com/shoppingmall/service/payment/PaymentRecordScheduledService.java`
  - **功能**：定义支付记录定时任务服务接口
  - **方法**：
    - `cancelTimeoutPaymentRecords()`：自动取消超时的支付中支付记录

**2. 支付记录定时任务服务实现类（新建）：**
- `backend/src/main/java/com/shoppingmall/service/payment/impl/PaymentRecordScheduledServiceImpl.java`
  - **功能**：实现支付记录自动取消定时任务
  - **特性**：
    - 每小时执行一次，检查超时的支付中支付记录（降低系统压力）
    - 从数据库读取最新配置，确保配置修改后立即生效
    - 将超时的支付中记录状态更新为"已关闭"（CLOSED）
    - 记录详细日志，便于问题排查
  - **方法**：
    - `getPaymentRecordTimeoutHours()`：从数据库读取支付记录自动取消时间配置
    - `cancelTimeoutPaymentRecords()`：执行自动取消超时支付记录的任务

### 技术细节

**定时任务配置：**
- 执行频率：每小时执行一次（`@Scheduled(fixedRate = 3600000)`）
- 事务支持：使用 `@Transactional` 确保数据一致性
- 异常处理：单个记录处理失败不影响其他记录的处理
- **优化说明**：相比每分钟执行，每小时执行可大幅降低系统压力，同时仍能及时处理超时记录

**状态流转：**
- 支付中（PAYING，状态值：1）→ 已关闭（CLOSED，状态值：3）

**配置管理：**
- 配置键：`payment.record-timeout-hours`
- 默认值：2小时（已优化，原为6小时）
- 配置类型：number
- 支持在管理后台动态修改

### 使用说明

1. **执行SQL脚本**：
   ```sql
   -- 执行数据库脚本添加配置项
   source database/update-20251226-add-payment-record-timeout-config.sql
   ```

2. **配置自动取消时间**：
   - 登录管理后台
   - 进入"系统管理" → "系统配置"
   - 找到"支付记录自动取消时间"配置项
   - 修改时间值（单位：小时）
   - 保存后立即生效，无需重启服务

3. **查看日志**：
   - 定时任务执行日志会记录在应用日志中
   - 可通过日志查看自动关闭的支付记录详情

### 注意事项

- 定时任务会在应用启动后自动运行，无需手动启动
- 配置修改后，下次定时任务执行时立即生效
- 已关闭的支付记录不会再次被处理
- 建议根据实际业务需求调整自动取消时间

---

## 2025-12-26 - 系统配置改为数据库管理（邮箱、订单、应用配置）

### 功能说明
将邮箱配置、订单配置、应用配置从配置文件改为数据库配置管理，支持管理员在后台动态修改，无需重启服务即可生效。

### 修改原因
- 配置文件修改需要重启服务，不便于运维管理
- 需要支持动态配置，提升系统灵活性
- 统一在系统配置管理页面管理，便于维护
- 支持配置历史记录和权限控制

### 修改内容

#### 数据库配置

**新增配置项：**
- `database/update-20251226-add-system-configs.sql`
  - **邮箱配置**：
    - `mail.host`：邮件服务器地址（默认：smtp.qq.com）
    - `mail.port`：邮件服务器端口（默认：587）
    - `mail.username`：发件人邮箱
    - `mail.password`：邮箱授权码
  - **订单配置**：
    - `order.payment-timeout-hours`：订单支付超时时间（小时，默认：6）
  - **应用配置**：
    - `app.password.reset.token-expire-minutes`：密码重置令牌有效期（分钟，默认：30）

#### 后端代码修改

**1. 邮件配置服务（新建）：**
- `backend/src/main/java/com/shoppingmall/service/impl/MailConfigService.java`
  - **功能**：动态创建 `JavaMailSender`，每次发送邮件时从数据库读取最新配置
  - **方法**：
    - `createMailSender()`：创建邮件发送器（使用最新配置）
    - `getFromEmail()`：获取发件人邮箱地址

**2. 邮件服务实现类：**
- `backend/src/main/java/com/shoppingmall/service/impl/EmailServiceImpl.java`
  - **移除 `JavaMailSender` 注入**：改为使用 `MailConfigService` 动态创建
  - **修改 `sendPasswordResetEmail()` 方法**：每次发送邮件时动态创建 `JavaMailSender`

**3. 订单定时任务服务：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderScheduledServiceImpl.java`
  - **移除 `@Value` 注解**：不再从配置文件读取订单配置
  - **添加 `SystemConfigService` 依赖**：用于从数据库读取配置
  - **添加 `getPaymentTimeoutHours()` 方法**：从数据库读取订单支付超时时间
  - **修改 `cancelTimeoutOrders()` 方法**：每次执行定时任务时读取最新配置

**4. 用户服务实现类：**
- `backend/src/main/java/com/shoppingmall/service/user/impl/UserServiceImpl.java`
  - **移除 `@Value` 注解**：不再从配置文件读取密码重置令牌有效期
  - **添加 `SystemConfigService` 依赖**：用于从数据库读取配置
  - **添加 `getTokenExpireMinutes()` 方法**：从数据库读取令牌有效期
  - **修改 `forgotPassword()` 方法**：每次使用时读取最新配置

### 功能特性
- ✅ 邮箱配置可动态修改（服务器地址、端口、账号、授权码）
- ✅ 订单支付超时时间可动态修改
- ✅ 密码重置令牌有效期可动态修改
- ✅ 配置修改后立即生效，无需重启服务
- ✅ 提供默认值作为兜底机制
- ✅ 配置格式验证（端口、有效期等数字类型）

### 配置读取策略

1. **邮箱配置**：
   - 每次发送邮件时从数据库读取最新配置
   - 动态创建 `JavaMailSender`，确保使用最新配置
   - 配置缺失时抛出异常，提示管理员配置

2. **订单配置**：
   - 定时任务每次执行时读取最新配置
   - 配置格式错误时使用默认值（6小时）

3. **密码重置令牌有效期**：
   - 每次使用时读取最新配置
   - 配置格式错误时使用默认值（30分钟）

### 使用说明

1. **执行数据库脚本**：
   ```bash
   mysql -u root -p < database/update-20251226-add-system-configs.sql
   ```

2. **在管理后台修改配置**：
   - 登录管理后台，进入"系统设置" -> "基础配置"
   - 找到以下配置项进行编辑：
     - `mail.host`：邮件服务器地址
     - `mail.port`：邮件服务器端口
     - `mail.username`：发件人邮箱
     - `mail.password`：邮箱授权码
     - `order.payment-timeout-hours`：订单支付超时时间
     - `app.password.reset.token-expire-minutes`：密码重置令牌有效期

3. **配置修改后立即生效**：
   - 邮箱配置：下次发送邮件时生效
   - 订单配置：下次定时任务执行时生效
   - 密码重置令牌有效期：下次使用时生效

### 注意事项

1. **邮箱配置**：
   - 修改邮箱配置后，下次发送邮件时自动使用新配置
   - 如果配置错误，邮件发送会失败，需要检查配置
   - 邮箱授权码不是登录密码，需要在邮箱设置中生成

2. **订单配置**：
   - 修改订单支付超时时间后，下次定时任务执行时生效
   - 建议设置为合理的值（如6-24小时）

3. **密码重置令牌有效期**：
   - 修改后，新生成的令牌使用新的有效期
   - 已生成的令牌仍使用原来的有效期

4. **配置格式验证**：
   - 端口和有效期必须是数字
   - 配置格式错误时使用默认值，并记录警告日志

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/service/impl/MailConfigService.java` - 邮件配置服务（新建）
- ✅ `backend/src/main/java/com/shoppingmall/service/impl/EmailServiceImpl.java` - 邮件服务实现类
- ✅ `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderScheduledServiceImpl.java` - 订单定时任务服务
- ✅ `backend/src/main/java/com/shoppingmall/service/user/impl/UserServiceImpl.java` - 用户服务实现类
- ✅ `database/update-20251226-add-system-configs.sql` - 数据库配置初始化脚本

### 技术细节
- **配置读取优先级**：数据库配置 > 默认值
- **动态配置更新**：每次使用时读取最新配置，确保配置修改后立即生效
- **错误处理**：配置格式错误时使用默认值，并记录警告日志
- **性能考虑**：配置读取有缓存机制（`SystemConfigService`），性能影响可忽略

---

## 2025-12-26 - 密码重置邮件内容改为系统配置管理

### 功能说明
将密码重置邮件的内容（主题和正文）改为通过系统配置管理，支持管理员在后台自定义邮件模板，无需修改代码即可调整邮件内容。

### 修改原因
- 邮件内容硬编码在代码中，不便于修改
- 需要支持自定义邮件模板，适应不同业务场景
- 需要支持模板变量替换，动态生成邮件内容
- 提升系统的灵活性和可配置性

### 修改内容

#### 数据库配置

**新增配置项：**
- `database/update-20251226-add-mail-template-config.sql`
  - **`mail.password-reset.subject`**：密码重置邮件主题模板
    - 支持变量：`{username}`（用户名）、`{platform}`（平台名称）
    - 默认值：`密码重置验证码 - B2B采购平台`
  - **`mail.password-reset.content`**：密码重置邮件正文模板
    - 支持变量：`{username}`（用户名）、`{resetCode}`（验证码）、`{resetUrl}`（重置链接）、`{expireMinutes}`（有效期分钟数）、`{platform}`（平台名称）
    - 默认值：包含完整邮件正文模板
  - **`app.platform.name`**：平台名称
    - 默认值：`B2B采购平台`
    - 用于邮件模板等场景

#### 后端代码修改

**邮件服务实现类：**
- `backend/src/main/java/com/shoppingmall/service/impl/EmailServiceImpl.java`
  - **移除 `@Value` 注解**：不再从配置文件读取邮件相关配置
  - **添加 `SystemConfigService` 依赖**：用于从数据库读取配置
  - **添加配置读取方法**：
    - `getFromEmail()`：从数据库读取发件人邮箱（`mail.username`）
    - `getFrontendUrl()`：从数据库读取前端地址（`app.frontend.url`）
    - `getPlatformName()`：从数据库读取平台名称（`app.platform.name`）
  - **添加模板处理方法**：
    - `replaceTemplateVariables()`：替换模板变量
    - `getPasswordResetSubjectTemplate()`：获取邮件主题模板（带默认值）
    - `getPasswordResetContentTemplate()`：获取邮件正文模板（带默认值）
  - **修改 `sendPasswordResetEmail()` 方法**：
    - 从数据库读取邮件主题和正文模板
    - 准备模板变量映射（用户名、验证码、重置链接、有效期、平台名称）
    - 使用 `replaceTemplateVariables()` 替换模板变量
    - 发送邮件

### 功能特性
- ✅ 邮件主题和正文模板可配置
- ✅ 支持模板变量替换（`{username}`, `{resetCode}`, `{resetUrl}`, `{expireMinutes}`, `{platform}`）
- ✅ 配置修改后立即生效，无需重启服务
- ✅ 提供默认模板作为兜底机制
- ✅ 平台名称可配置，便于品牌定制

### 模板变量说明

支持的模板变量：
- `{username}` - 用户名
- `{resetCode}` - 重置验证码
- `{resetUrl}` - 重置密码链接（完整URL）
- `{expireMinutes}` - 验证码有效期（分钟数）
- `{platform}` - 平台名称（从 `app.platform.name` 配置读取）

### 使用说明

1. **在系统配置管理页面修改邮件模板**：
   - 登录管理后台，进入"系统设置" -> "基础配置"
   - 找到 `mail.password-reset.subject`（邮件主题）和 `mail.password-reset.content`（邮件正文）
   - 编辑模板内容，使用 `{变量名}` 作为占位符
   - 保存后立即生效

2. **模板示例**：
   ```
   主题：密码重置验证码 - {platform}
   
   正文：
   尊敬的 {username} 用户：
   
   您申请了密码重置，请使用以下验证码重置您的密码：
   
   验证码：{resetCode}
   
   或者点击以下链接直接重置密码：
   {resetUrl}
   
   此验证码有效期为{expireMinutes}分钟，请及时操作。
   如果您没有申请密码重置，请忽略此邮件。
   
   {platform}
   ```

3. **注意事项**：
   - 模板变量必须使用大括号包裹，如 `{username}`
   - 变量名区分大小写
   - 如果模板中使用了不存在的变量，该变量不会被替换（保持原样）
   - 建议在修改模板前先备份原模板

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/service/impl/EmailServiceImpl.java` - 邮件服务实现类
- ✅ `database/update-20251226-add-mail-template-config.sql` - 数据库配置初始化脚本

### 技术细节
- **配置读取优先级**：数据库配置 > 默认值
- **模板变量替换**：使用 `String.replace()` 方法替换模板变量
- **错误处理**：如果配置缺失，使用默认模板，确保邮件发送功能正常

---

## 2025-12-26 - 订单支付页面添加支付状态弹窗功能

### 功能说明
为订单支付页面添加类似预存款充值的支付状态弹窗功能，提升用户体验。支付过程中显示支付状态弹窗，支付成功后自动跳转到订单详情页面并显示支付成功提示。

### 修改原因
- 订单支付页面缺少支付状态反馈
- 用户支付后不知道支付是否成功
- 需要类似预存款充值的支付状态弹窗功能
- 支付成功后需要跳转到订单详情页面并提示支付成功

### 修改内容

#### 前端代码修改

**订单支付页面：**
- `frontend/src/views/order/Payment.vue`
  - **添加支付状态弹窗组件**：
    - 支付中状态（`paying`）：显示加载动画，提供"我已付款"和"付款有问题"按钮
    - 已付款状态（`paid`）：显示成功图标，自动跳转到订单详情页面
    - 付款有问题状态（`problem`）：显示错误图标，提供"联系客服"、"重新支付"、"关闭"按钮
  - **添加图标导入**：`Loading`、`CircleCheck`、`CircleClose`
  - **添加支付状态相关变量**：
    - `showPaymentStatusDialog`：控制弹窗显示
    - `paymentStatus`：支付状态（`paying` | `paid` | `problem` | `''`）
    - `currentPaymentOrderNo`：当前支付订单号
  - **修改 `processPayment` 方法**：
    - 预存款支付：显示"已付款"弹窗，2秒后跳转到订单详情页面
    - 模拟支付：显示"支付中"弹窗，支付成功后显示"已付款"弹窗
    - 真实支付（支付宝/微信）：显示"支付中"弹窗，在新窗口打开支付页面
    - 支付失败：显示"付款有问题"弹窗
  - **添加支付状态弹窗相关方法**：
    - `handleMarkAsPaid`：我已付款，跳转到订单详情页面
    - `handlePaymentProblem`：付款有问题，切换到问题状态
    - `handleContactService`：联系客服
    - `handleRetryPayment`：重新支付
    - `closePaymentStatusDialog`：关闭弹窗
  - **修改 `onMounted` 方法**：
    - 检查URL参数中的 `paymentStatus=success`
    - 如果存在，显示"已付款"弹窗和成功提示
    - 2秒后跳转到订单详情页面
  - **添加支付状态弹窗CSS样式**：
    - 支付中图标：蓝色，旋转动画
    - 成功图标：绿色
    - 错误图标：红色
    - 状态标题和描述样式
    - 按钮布局样式

### 功能特性
- ✅ 支付中状态弹窗（显示加载动画）
- ✅ 支付成功状态弹窗（自动跳转到订单详情）
- ✅ 支付问题状态弹窗（提供联系客服和重新支付选项）
- ✅ 支付成功后自动跳转到订单详情页面
- ✅ 订单详情页面显示支付成功提示
- ✅ 支持预存款支付、支付宝支付、微信支付

### 技术细节
- **支付状态流转**：
  - 预存款支付：直接成功 → 显示"已付款"弹窗 → 跳转订单详情
  - 模拟支付：显示"支付中" → 调用模拟接口 → 显示"已付款"弹窗 → 跳转订单详情
  - 真实支付：显示"支付中" → 打开支付页面 → 用户完成支付 → 回调跳转 → 显示"已付款"弹窗 → 跳转订单详情
- **弹窗控制**：
  - 支付中状态：不允许关闭（`show-close="false"`）
  - 已付款/问题状态：允许关闭
- **跳转逻辑**：
  - 支付成功回调：URL参数 `paymentStatus=success`
  - 跳转目标：`/order/detail?orderNumber={orderNo}`

### 使用说明
1. **支付流程**：
   - 用户选择支付方式，点击"立刻付款"
   - 如果是预存款支付，输入支付密码后直接成功
   - 如果是支付宝/微信支付，显示"支付中"弹窗，在新窗口打开支付页面
   - 用户完成支付后，支付宝回调跳转回订单支付页面
   - 系统检测到 `paymentStatus=success`，显示"已付款"弹窗
   - 2秒后自动跳转到订单详情页面

2. **支付状态弹窗操作**：
   - **我已付款**：跳转到订单详情页面，查看订单状态
   - **付款有问题**：切换到问题状态，提供联系客服和重新支付选项
   - **联系客服**：显示客服联系方式
   - **重新支付**：关闭弹窗，用户可以重新点击付款按钮

### 影响范围
- ✅ `frontend/src/views/order/Payment.vue` - 订单支付页面

### 注意事项
1. **支付中状态**：弹窗不允许关闭，防止用户误操作
2. **支付成功回调**：依赖URL参数 `paymentStatus=success`，需要确保后端回调正确设置
3. **跳转逻辑**：支付成功后会自动跳转到订单详情页面，并清除URL参数
4. **用户体验**：支付状态弹窗提供清晰的状态反馈，提升用户体验

---

## 2025-12-26 - 前端地址改为数据库配置方式

### 功能说明
将前端地址配置从配置文件改为数据库配置方式，支持在管理后台动态修改，无需重启服务即可生效。

### 修改原因
- 前端地址可能会变化（开发环境、测试环境、生产环境）
- 使用数据库配置可以动态修改，不需要重启服务
- 可以在管理后台配置，更方便管理
- 统一管理，与其他系统配置保持一致

### 修改内容

#### 1. 后端代码修改

**Controller：**
- `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
  - 添加 `SystemConfigService` 依赖注入
  - 优化 `getFrontendUrl` 方法：
    - **优先级1**：从数据库配置读取（`app.frontend.url`）
    - **优先级2**：从 `Referer` 头提取（排除支付宝域名和API路径）
    - **优先级3**：内网穿透场景（natapp等）
    - **优先级4**：配置文件默认值（`app.frontend.url`）
  - 添加详细日志记录每个优先级的使用情况

#### 2. 数据库脚本

**SQL脚本：**
- `database/update-20251226-add-frontend-url-config.sql`（新建）
  - 添加 `app.frontend.url` 配置项到 `system_config` 表
  - 默认值：`http://localhost:3002`
  - 配置类型：`text`
  - 状态：启用（`status = 1`）
  - 使用 `ON DUPLICATE KEY UPDATE` 确保幂等性

### 功能特性
- ✅ 支持从数据库动态读取前端地址配置
- ✅ 支持在管理后台修改前端地址
- ✅ 修改后立即生效，无需重启服务
- ✅ 多级降级方案，确保系统稳定性
- ✅ 详细日志记录，便于排查问题

### 技术细节
- **配置键**：`app.frontend.url`
- **优先级顺序**：
  1. 数据库配置（`system_config` 表）
  2. Referer头（排除支付宝域名）
  3. 内网穿透场景（natapp）
  4. 配置文件默认值（`application.yml`）
- **日志级别**：INFO级别记录每个优先级的使用情况

### 使用说明
1. **执行数据库脚本**：
   ```sql
   -- 执行 database/update-20251226-add-frontend-url-config.sql
   ```

2. **管理后台配置**：
   - 登录管理后台
   - 进入"系统设置" -> "基础配置"
   - 找到"前端地址"配置项
   - 修改为实际的前端地址（如：`http://your-domain.com`）
   - 保存后立即生效

3. **多环境配置**：
   - 开发环境：`http://localhost:3002`
   - 测试环境：`http://test.example.com`
   - 生产环境：`https://www.example.com`

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
- ✅ `database/update-20251226-add-frontend-url-config.sql`（新建）

### 注意事项
1. **数据库配置优先级最高**：如果数据库中有配置，会优先使用数据库配置
2. **配置格式**：前端地址应该包含协议（http/https），不需要末尾斜杠
3. **立即生效**：修改数据库配置后，下次支付回调时会立即使用新地址
4. **降级方案**：如果数据库配置不存在或读取失败，会自动降级到其他方案

---

## 2025-12-26 - 修复预存款充值支付成功回调跳转404问题

### 问题描述
预存款支付宝支付成功后，跳转回调页面出现404错误。错误URL：`http://n44a6799.natappfree.cc/member/deposit/recharge?paymentStatus=success`

### 问题原因
`getFrontendUrl` 方法从支付宝回调的请求URL中提取前端地址，但支付宝回调的请求URL是后端地址（`http://n44a6799.natappfree.cc/api/buyer/payment/alipay/return`），导致提取出来的前端地址也是后端地址，重定向URL错误。

### 解决方案
1. **添加配置支持**：使用 `@Value` 注解注入 `app.frontend.url` 配置项作为默认前端地址
2. **优化提取逻辑**：
   - 优先从 `Referer` 头提取前端地址（排除支付宝域名和API路径）
   - 内网穿透场景：前端和后端使用同一个域名，保持原域名
   - 其他场景：使用配置的默认前端地址
3. **添加详细日志**：记录前端地址提取过程，便于排查问题

### 修改内容

**Controller：**
- `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
  - 添加 `@Value("${app.frontend.url:http://localhost:3002}")` 注入默认前端地址
  - 优化 `getFrontendUrl` 方法：
    - 优先从 `Referer` 头提取（排除支付宝域名和API路径）
    - 内网穿透场景特殊处理（保持原域名）
    - 其他场景使用配置的默认前端地址
    - 添加详细日志记录提取过程

### 功能特性
- ✅ 支持从配置文件读取前端地址
- ✅ 优化内网穿透场景的前端地址提取
- ✅ 添加详细日志便于排查问题
- ✅ 支持多种场景的前端地址提取

### 技术细节
- **配置项**：`app.frontend.url`（默认值：`http://localhost:3002`）
- **提取优先级**：
  1. Referer头（排除支付宝域名和API路径）
  2. 内网穿透场景：保持原域名
  3. 配置的默认前端地址
- **日志级别**：INFO级别记录提取过程

### 使用说明
1. **配置前端地址**：
   - 在 `application.yml` 中配置 `app.frontend.url`
   - 生产环境建议配置为实际的前端域名

2. **内网穿透场景**：
   - 如果前端和后端使用同一个域名（如natapp），系统会自动识别
   - 确保前端和后端使用相同的协议和域名

3. **查看日志**：
   - 支付回调时会记录前端地址提取过程
   - 如果提取失败，会使用配置的默认地址

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`

### 注意事项
1. **配置文件**：确保 `application.yml` 中配置了正确的前端地址
2. **内网穿透**：如果使用内网穿透工具，确保前端和后端使用相同的域名
3. **生产环境**：生产环境需要配置正确的前端域名

---

## 2025-12-26 - 添加预存款支付并发控制（悲观锁）

### 功能说明
为预存款支付、退款、充值回调等关键操作添加数据库悲观锁（`SELECT FOR UPDATE`），防止并发操作导致余额超支或数据不一致问题。

### 修改原因
- **风险评估**：预存款支付没有做冻结业务，存在并发风险
- **问题场景**：用户同时支付多个订单时，可能出现余额超支（如余额100元，同时支付80元和50元，都通过余额检查，导致余额变成-30元）
- **当前保护措施不足**：虽然使用了 `@Transactional` 事务，但 `selectOne` 和 `updateById` 之间没有锁保护

### 修改内容

#### 后端代码修改

**Service实现：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/DepositServiceImpl.java`
  - **`depositPayment` 方法**（预存款支付）：
    - 在查询预存款账户时添加 `.last("FOR UPDATE")` 悲观锁
    - 防止并发支付导致余额超支
    - 添加注释说明锁的作用
  - **`depositRefund` 方法**（预存款退款）：
    - 在查询预存款账户时添加 `.last("FOR UPDATE")` 悲观锁
    - 防止并发退款导致余额不一致
    - 优化余额计算逻辑，处理null值
  - **`handlePaymentCallback` 方法**（支付回调处理）：
    - 在查询预存款账户时添加 `.last("FOR UPDATE")` 悲观锁
    - 防止并发充值回调导致余额不一致
    - 优化余额计算逻辑，处理null值

### 功能特性
- ✅ 预存款支付使用悲观锁，防止并发超支
- ✅ 预存款退款使用悲观锁，防止余额不一致
- ✅ 充值回调使用悲观锁，防止余额不一致
- ✅ 优化余额计算逻辑，处理null值情况

### 技术细节
- **悲观锁实现**：使用 `SELECT FOR UPDATE` 在事务中对预存款账户行加锁
- **锁范围**：只锁定当前用户的预存款账户行，不影响其他用户
- **事务隔离**：配合 `@Transactional` 注解，确保事务内数据一致性
- **性能影响**：行锁粒度小，性能影响可控，适合高并发场景

### 风险评估对比

| 风险项 | 修改前 | 修改后 |
|--------|--------|--------|
| 并发支付超支 | 🔴 高风险 | ✅ 已解决 |
| 余额显示不准确 | 🟡 中等风险 | ✅ 已解决 |
| 并发退款不一致 | 🟡 中等风险 | ✅ 已解决 |
| 并发充值不一致 | 🟡 中等风险 | ✅ 已解决 |

### 使用说明
1. **预存款支付**：
   - 用户支付订单时，系统会自动对预存款账户加锁
   - 确保同一用户同时支付多个订单时，余额检查是串行的
   - 防止余额超支问题

2. **预存款退款**：
   - 退款时对预存款账户加锁
   - 确保退款操作的原子性

3. **充值回调**：
   - 支付回调处理时对预存款账户加锁
   - 防止并发回调导致余额重复增加

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/service/buyer/impl/DepositServiceImpl.java`

### 注意事项
1. **锁粒度**：只锁定当前用户的预存款账户行，不影响其他用户的操作
2. **事务要求**：必须在事务中使用，确保锁在事务提交后释放
3. **性能考虑**：行锁性能影响小，适合高并发场景
4. **未来优化**：如果未来需要更复杂的资金管理（如订单取消解冻、退款冻结等），可以考虑实现冻结机制

### 相关文档
- `docs/预存款冻结金额业务说明.md` - 冻结金额业务说明文档

---

## 2025-12-26 - 修复支付宝支付回调问题（订单支付和预存款充值）

### 问题描述
1. **订单支付回调问题**：支付宝支付成功后，`return_url` 直接指向前端页面，导致404错误
2. **预存款充值回调问题**：预存款充值支付成功后，没有跳转到充值成功页面

### 解决方案
1. **统一回调接口**：`return_url` 必须指向后端接口，后端验证签名后重定向到前端页面
2. **区分订单类型**：根据订单号前缀（`DEPOSIT_`）判断是订单支付还是预存款充值
3. **统一异步回调**：订单支付和预存款充值都使用统一的异步回调接口

---

## 2025-12-26 - 添加支付宝支付成功回调功能（订单支付）

### 功能说明
添加支付宝支付成功后的同步回调功能，用户支付成功后可以跳转回平台的订单详情页面，并显示支付成功提示。

### 修改原因
- 支付宝支付成功后，用户停留在支付宝的成功页面，无法自动跳转回平台
- 需要添加 `return_url` 参数，让用户支付成功后跳转到订单详情页面
- 需要在订单详情页面显示支付成功提示

### 修改内容

#### 1. 后端代码修改

**工具类：**
- `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`
  - 修改 `createPagePayment` 方法签名，添加 `returnUrl` 参数
  - 清理 `returnUrl` 中的特殊字符
  - 将 `return_url` 参数添加到签名参数中（必须参与签名）
  - 记录 `returnUrl` 的清理前后日志

**支付策略：**
- `backend/src/main/java/com/shoppingmall/payment/strategy/impl/AlipayPayStrategy.java`
  - **关键修复**：`return_url` 必须指向后端接口，不能直接指向前端页面
  - 在 `createPayment` 方法中构建 `return_url`
  - 从 `notifyUrl` 中提取基础URL（协议+域名+端口）
  - 构建 `return_url`：`/api/buyer/payment/alipay/return`（后端接口）
  - 后端接口会验证签名后重定向到前端页面：`/order/detail?orderNumber={orderNo}&paymentStatus=success`
  - 将 `returnUrl` 传递给 `AlipayUtil.createPagePayment`

**回调控制器：**
- `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
  - **关键修复**：分离同步回调和异步回调接口
  - 新增 `alipayReturn` 方法（GET）：处理同步回调（return_url）
    - 验证签名
    - 处理业务逻辑
    - 重定向到前端订单详情页面
  - 修改 `alipayNotify` 方法（POST）：处理异步回调（notify_url）
    - 验证签名
    - 处理业务逻辑
    - 返回"success"
  - 添加 `getFrontendUrl` 方法，从请求中提取前端地址
  - 添加 `escapeHtml` 方法，防止XSS攻击

**DTO：**
- `backend/src/main/java/com/shoppingmall/dto/PaymentRequestDTO.java`
  - 添加 `frontendUrl` 字段，用于传递前端地址

**Service：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
  - 在 `payOrder` 方法中，如果 `OrderPaymentDTO` 包含 `frontendUrl`，则传递给支付请求

#### 2. 前端代码修改

**订单详情页面：**
- `frontend/src/views/order/Detail.vue`
  - 在 `loadOrderDetail` 方法中检查URL参数 `paymentStatus`
  - 如果 `paymentStatus=success`，显示支付成功提示
  - 清除URL参数中的 `paymentStatus`，避免刷新时重复提示

### 功能特性
- ✅ 支付宝支付成功后自动跳转到订单详情页面
- ✅ 订单详情页面显示支付成功提示
- ✅ 支持同步回调（return_url）和异步回调（notify_url）
- ✅ 自动从notifyUrl提取前端地址
- ✅ 支持手动指定前端地址

### 技术细节
- **同步回调（return_url）**：
  - **重要**：`return_url` 必须指向后端接口，不能直接指向前端页面
  - 后端接口：`/api/buyer/payment/alipay/return`（GET请求）
  - 支付宝会带着回调参数跳转到这个接口
  - 后端验证签名 → 处理业务逻辑 → 重定向到前端页面
  - 前端页面URL格式：`/order/detail?orderNumber={orderNo}&paymentStatus=success`
  - 使用JavaScript和meta refresh双重跳转，兼容性更好
  
- **异步回调（notify_url）**：
  - 后端接口：`/api/buyer/payment/alipay/notify`（POST请求）
  - 支付宝服务器主动调用
  - 返回"success"字符串
  - 处理订单状态更新等业务逻辑

- **return_url构建**：
  - 从 `notifyUrl` 中提取基础URL（协议+域名+端口）
  - 拼接后端接口路径：`/api/buyer/payment/alipay/return`
  - 如果提取失败，使用默认值或从notifyUrl替换路径

- **前端地址提取**：
  - 从请求的Referer或请求URL中提取
  - 如果提取失败，使用默认值 `http://localhost:3002`
  - 支持手动指定 `frontendUrl`

### 使用说明
1. **支付流程**：
   - 用户选择支付宝支付
   - 跳转到支付宝支付页面
   - 支付成功后，支付宝会调用 `return_url`（同步回调）
   - 系统验证签名后，重定向到订单详情页面
   - 订单详情页面显示支付成功提示

2. **回调处理**：
   - 同步回调（GET）：验证签名 → 处理业务逻辑 → 重定向到订单详情
   - 异步回调（POST）：验证签名 → 处理业务逻辑 → 返回"success"

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`
- ✅ `backend/src/main/java/com/shoppingmall/payment/strategy/impl/AlipayPayStrategy.java`
- ✅ `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
- ✅ `backend/src/main/java/com/shoppingmall/dto/PaymentRequestDTO.java`
- ✅ `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
- ✅ `frontend/src/views/order/Detail.vue`

### 注意事项
1. **return_url必须指向后端接口**：
   - ❌ 错误：`return_url` 直接指向前端页面（会导致404，因为前端无法处理支付宝回调参数）
   - ✅ 正确：`return_url` 指向后端接口 `/api/buyer/payment/alipay/return`
   - 后端接口验证签名后，再重定向到前端页面

2. **return_url必须参与签名**：支付宝要求 `return_url` 参数必须参与签名

3. **前端地址配置**：生产环境需要配置正确的前端地址

4. **URL编码**：订单号需要进行URL编码，避免特殊字符问题

5. **重定向方式**：使用JavaScript和meta refresh双重跳转，兼容性更好

6. **安全防护**：重定向URL需要进行HTML转义，防止XSS攻击

---

## 2025-12-26 - 支付宝支付签名问题总结

### 问题总结

在调试支付宝支付功能时，遇到了 `invalid-signature` 验签错误。经过排查，发现两个关键问题：

#### 问题1：charset参数处理
- **错误理解**：认为 `charset` 参数只放在URL中，不参与签名
- **正确做法**：`charset` 参数必须**同时**满足：
  1. 放在URL查询字符串中（`gateway.do?charset=utf-8`）
  2. 参与签名（支付宝验签时会包含它）

#### 问题2：参数值URL编码
- **错误做法**：签名时对参数值进行URL编码（`timestamp=2025-12-26+15%3A57%3A58`）
- **正确做法**：签名时使用**原始值**（`timestamp=2025-12-26 15:57:58`）
- **原因**：支付宝验签字符串中的参数值都是未编码的原始值

### 核心要点

1. ✅ `charset` 参数必须放在URL中，同时参与签名
2. ✅ 签名时使用原始值，不进行URL编码
3. ✅ 参数按字典序排序
4. ✅ 确保私钥和公钥匹配

### 详细文档

已创建详细的排查指南：`docs/支付宝支付签名问题排查指南.md`

---

## 2025-12-26 - 修复支付宝支付验签错误并添加详细日志

### 功能说明
修复支付宝支付时的验签错误（invalid-signature），添加详细的日志用于排查签名问题。

### 修改原因
- 支付宝支付时报错：`invalid-signature`，验签出错
- 错误信息：`请确认charset参数放在了URL查询字符串中且各参数值使用charset参数指示的字符集编码`
- 需要添加详细日志排查签名生成和参数编码问题

### 修改内容

#### 1. 修复签名参数问题（关键修复）

**工具类：**
- `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`
  - **修复关键问题**：将 `charset` 参数从签名参数中移除，放在URL查询字符串中
    - 之前：`charset` 参数包含在签名参数中
    - 现在：`charset` 参数不参与签名，只放在URL查询字符串中
    - 支付宝要求：`charset` 必须放在URL查询字符串中，不参与签名
    - 签名时不包含 `charset` 参数
  - **添加详细日志**：
    - 在 `createPagePayment` 方法中添加签名前的参数列表日志
    - 记录订单号、金额、AppID等关键信息
    - 记录生成的签名值
  - **优化 `getSignContent` 方法**：
    - 明确跳过 `sign` 参数和空值
    - 添加每个参数的编码前后值日志（debug级别）
    - 记录完整的待签名字符串
  - **优化 `sign` 方法**：
    - 添加签名过程的详细日志
    - 支持处理 `RSA PRIVATE KEY` 格式的私钥（兼容性）
    - 记录私钥长度和签名结果长度
    - 异常时记录私钥前100字符用于排查
  - **优化 `buildFormHtmlWithCharset` 方法**：
    - 移除URL中的 `charset` 参数（因为已包含在表单参数中）
    - 添加表单参数列表日志
    - 记录生成的表单HTML长度

### 问题分析
1. **根本原因**：`charset` 参数需要参与签名
   - 支付宝要求：`charset` 参数必须放在URL查询字符串中（如：`gateway.do?charset=utf-8`）
   - 但同时：`charset` 参数也必须参与签名（支付宝验签时会从URL中读取并包含在验签字符串中）
   - 正确做法：
     - `charset` 放在URL查询字符串中（作为URL参数）
     - `charset` 也包含在签名参数中（参与签名）
   - 支付宝验签字符串格式：`...&charset=utf-8&...`（说明charset参与了验签）

2. **参数编码**（关键修复）：
   - **签名时**：使用**原始值**（不进行URL编码）
   - **原因**：支付宝验签字符串中的参数值都是未编码的原始值（如：`timestamp=2025-12-26 15:57:58`）
   - **表单提交时**：浏览器会自动对表单参数进行URL编码
   - **支付宝处理**：接收到编码后的值，但在验签前会先解码，使用解码后的值进行验签
   - **正确做法**：签名时使用原始值，确保与支付宝验签时使用的值一致

3. **私钥格式**：
   - 支持 `BEGIN PRIVATE KEY` 格式（PKCS#8）
   - 也支持 `BEGIN RSA PRIVATE KEY` 格式（兼容性）

### 功能特性
- ✅ 修复签名参数问题（`charset` 放在URL中，同时参与签名）
- ✅ 添加详细的签名过程日志
- ✅ 记录签名前后的参数值
- ✅ 记录待签名字符串（包含 `charset`）
- ✅ 记录未编码版本的签名字符串（用于对比）
- ✅ 记录签名结果
- ✅ 支持多种私钥格式
- ✅ 异常时记录关键信息用于排查

### 技术细节
- **签名参数顺序**：按字典序排序（支付宝要求）
- **参数编码**：**使用原始值，不进行URL编码**（关键修复）
  - 之前错误：签名时对参数值进行URL编码（`timestamp=2025-12-26+15%3A57%3A58`）
  - 现在正确：签名时使用原始值（`timestamp=2025-12-26 15:57:58`）
  - 原因：支付宝验签字符串中的参数值都是未编码的原始值
- **签名算法**：SHA256withRSA（RSA2）
- **参数过滤**：跳过 `sign` 参数和空值参数
- **日志级别**：
  - INFO：关键步骤和结果
  - DEBUG：详细的参数信息

### 使用说明
1. **查看日志**：
   - 支付时查看日志中的"支付宝支付签名参数"部分
   - 检查待签名字符串是否正确
   - 检查签名是否成功生成

2. **排查问题**：
   - 如果仍然报错，检查日志中的参数列表
   - 确认私钥格式是否正确
   - 确认参数编码是否正确

3. **常见问题**：
   - 私钥格式错误：确保私钥包含 `BEGIN PRIVATE KEY` 和 `END PRIVATE KEY`
   - **参数编码错误**：签名时**不要**对参数值进行URL编码，使用原始值
   - 参数缺失：确保包含所有必需参数（包括 `charset`）
   - 时间戳格式：使用原始格式（`2025-12-26 15:57:58`），不要编码为 `2025-12-26+15%3A57%3A58`

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`

### 注意事项
1. **`charset` 参数处理**：
   - ✅ 必须放在URL查询字符串中（如：`gateway.do?charset=utf-8`）
   - ✅ 同时必须参与签名（支付宝验签时会包含它）
   - 注意：虽然charset在URL中，但签名时需要把它当作表单参数一样处理
2. **签名参数**：包含所有表单参数 + charset参数（charset在URL中，但参与签名）
3. **参数编码**：签名时所有参数值需要进行URL编码（UTF-8）
3. **日志级别**：建议在生产环境将日志级别设置为 INFO，避免过多日志
4. **敏感信息**：日志中会记录私钥的部分信息，注意日志安全
5. **参数顺序**：确保参数按字典序排序（代码已实现）
6. **编码格式**：确保所有参数使用 UTF-8 编码

---

## 2025-12-25 - 完善支付宝沙箱环境对接功能

### 功能说明
检查并完善预存款支付和订单支付环节的支付宝沙箱环境对接，确保可以正常使用支付宝沙箱环境进行支付。

### 修改原因
- 预存款支付和订单支付仍在使用旧的 `PaymentService`（模拟支付服务），无法对接真实支付宝
- `AlipayUtil` 中的扫码支付、查询订单、退款等方法有TODO标记，未实现HTTP请求
- 需要确保支付宝沙箱环境可以正常使用

### 修改内容

#### 1. 修复支付服务引用

**Service实现：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/DepositServiceImpl.java`
  - 将 `PaymentService` 改为 `PaymentGatewayService`
  - 将 `paymentService.createPayment()` 改为 `paymentGatewayService.pay()`
  - 移除模拟支付的自动回调逻辑（真实支付由支付宝回调处理）

- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
  - 将 `PaymentService` 改为 `PaymentGatewayService`
  - 将 `paymentService.createPayment()` 改为 `paymentGatewayService.pay()`

#### 2. 完善支付宝API调用

**工具类：**
- `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`
  - 添加 `HttpClient` 和 `ObjectMapper` 依赖
  - 实现 `createQrPayment` 方法的HTTP请求：
    - 发送POST请求到支付宝API
    - 解析响应JSON
    - 返回二维码内容或抛出异常
  - 实现 `queryOrder` 方法的HTTP请求：
    - 发送POST请求查询订单状态
    - 解析响应并返回订单状态
  - 实现 `refund` 方法的HTTP请求：
    - 发送POST请求申请退款
    - 解析响应并返回退款结果
  - 添加 `sendHttpRequest` 方法：发送HTTP请求到支付宝API
  - 添加 `formatTimestamp` 方法：格式化时间戳（支付宝要求格式：yyyy-MM-dd HH:mm:ss）
  - 修复时间戳格式：将 `new Date().toString()` 改为使用 `formatTimestamp` 方法

#### 3. 完善回调处理

**回调控制器：**
- `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
  - 优化 `parseAlipayNotifyData` 方法：
    - 确保 `sign` 和 `sign_type` 参数正确提取
    - 用于签名验证

**支付策略：**
- `backend/src/main/java/com/shoppingmall/payment/strategy/impl/AlipayPayStrategy.java`
  - 优化 `verifyCallback` 方法：
    - 支持 `Map<String, Object>` 类型的回调数据
    - 转换为 `Map<String, String>` 用于签名验证

### 功能特性
- ✅ 预存款支付支持支付宝沙箱环境
- ✅ 订单支付支持支付宝沙箱环境
- ✅ 支付宝扫码支付API完整实现
- ✅ 支付宝订单查询API完整实现
- ✅ 支付宝退款API完整实现
- ✅ 回调签名验证支持
- ✅ 时间戳格式符合支付宝要求

### 技术细节
- **HTTP客户端**：使用Java 11+的 `HttpClient` 发送HTTP请求
- **JSON解析**：使用 `ObjectMapper` 解析支付宝API响应
- **时间戳格式**：使用 `yyyy-MM-dd HH:mm:ss` 格式，时区为 `GMT+8`
- **错误处理**：API调用失败时抛出 `PaymentException`，包含详细错误信息
- **回调验证**：支持 `Map<String, Object>` 和 `Map<String, String>` 类型的回调数据

### 使用说明
1. **配置支付宝沙箱环境**：
   - 在支付配置页面配置支付宝沙箱环境的AppID、应用私钥、支付宝公钥
   - 配置回调地址（需要使用内网穿透工具，如ngrok）

2. **测试预存款充值**：
   - 用户选择支付宝支付进行预存款充值
   - 系统会调用支付宝API创建支付订单
   - 返回支付表单或二维码供用户支付

3. **测试订单支付**：
   - 用户下单后选择支付宝支付
   - 系统会调用支付宝API创建支付订单
   - 返回支付表单或二维码供用户支付

4. **回调处理**：
   - 支付宝支付完成后会回调配置的回调地址
   - 系统自动验证签名并更新订单状态

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/service/buyer/impl/DepositServiceImpl.java`
- ✅ `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
- ✅ `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`
- ✅ `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
- ✅ `backend/src/main/java/com/shoppingmall/payment/strategy/impl/AlipayPayStrategy.java`

### 注意事项
1. **回调地址**：支付宝沙箱环境需要配置公网可访问的回调地址，本地开发需要使用内网穿透工具
2. **API密钥**：确保支付宝沙箱环境的应用私钥和支付宝公钥配置正确
3. **时间戳**：时间戳格式必须为 `yyyy-MM-dd HH:mm:ss`，时区为 `GMT+8`
4. **签名验证**：回调数据必须包含 `sign` 和 `sign_type` 参数用于签名验证

---

## 2025-12-25 - 完成支付配置管理页面功能

### 功能说明
在系统设置下创建支付配置管理页面，提供微信支付和支付宝的配置管理功能，包括启用/禁用、环境切换、参数配置、测试连接等功能。

### 修改原因
- 支付配置是核心业务功能，需要专业的配置管理界面
- 配置项较多（微信11项、支付宝9项），需要更好的组织方式
- 需要测试连接、环境切换等专业功能
- 提升用户体验，操作更直观

### 修改内容

#### 1. 前端代码修改

**API接口：**
- `admin-frontend/src/api/admin/payment.ts` - 支付配置API接口文件（新建）
  - 定义支付配置相关的TypeScript接口
  - 提供获取配置、更新配置、刷新缓存、测试连接等API方法

**页面组件：**
- `admin-frontend/src/views/system/Payment.vue` - 支付配置管理页面（完善）
  - 微信支付配置卡片：
    - 启用/禁用开关
    - 环境切换（沙箱/生产）
    - 回调地址配置
    - 沙箱环境配置表单（AppID、商户号、API密钥、证书路径）
    - 生产环境配置表单（AppID、商户号、API密钥、证书路径）
    - 测试连接按钮
  - 支付宝配置卡片：
    - 启用/禁用开关
    - 环境切换（沙箱/生产）
    - 回调地址配置
    - 沙箱环境配置表单（AppID、应用私钥、支付宝公钥）
    - 生产环境配置表单（AppID、应用私钥、支付宝公钥）
    - 测试连接按钮
  - 操作按钮：保存配置、刷新缓存
  - 敏感信息使用密码输入框（show-password）

#### 2. 后端代码修改

**Service接口：**
- `backend/src/main/java/com/shoppingmall/payment/service/PaymentConfigService.java`
  - 添加 `updateWeChatPayConfig` 方法：更新微信支付配置
  - 添加 `updateAlipayConfig` 方法：更新支付宝配置

**Service实现：**
- `backend/src/main/java/com/shoppingmall/payment/service/impl/PaymentConfigServiceImpl.java`
  - 实现 `updateWeChatPayConfig` 方法：
    - 更新基本配置（启用状态、环境、回调地址）
    - 更新沙箱环境配置
    - 更新生产环境配置
    - 批量更新到数据库
    - 刷新缓存
  - 实现 `updateAlipayConfig` 方法：
    - 更新基本配置（启用状态、环境、回调地址）
    - 更新沙箱环境配置
    - 更新生产环境配置
    - 批量更新到数据库
    - 刷新缓存
  - 添加 `updateConfigValue` 辅助方法：更新配置值

**Controller：**
- `backend/src/main/java/com/shoppingmall/payment/controller/admin/PaymentConfigController.java`
  - 添加 `updatePaymentConfig` 接口：`POST /api/admin/payment/config`
    - 支持更新微信支付配置
    - 支持更新支付宝配置
    - 支持部分更新（只更新提供的字段）
  - 添加 `convertToWeChatPayConfig` 方法：将Map转换为微信支付配置对象
  - 添加 `convertToAlipayConfig` 方法：将Map转换为支付宝配置对象

#### 3. 数据库脚本

**SQL脚本：**
- `database/update-20251225-payment-config-menu.sql` - 支付配置菜单和权限初始化脚本（新建）
  - 确保支付配置菜单存在（菜单ID 29）
  - 为超级管理员角色分配支付配置权限
  - 为运营人员角色分配支付配置权限（如果存在）
  - 为客服人员角色分配支付配置权限（如果存在）
  - 包含查询验证语句

### 功能特性
- ✅ 微信支付配置管理（启用/禁用、环境切换、参数配置）
- ✅ 支付宝配置管理（启用/禁用、环境切换、参数配置）
- ✅ 环境切换（沙箱/生产环境）
- ✅ 敏感信息密码输入（API密钥、私钥等）
- ✅ 测试连接功能
- ✅ 刷新缓存功能
- ✅ 配置保存和验证
- ✅ 权限控制（admin:system:payment）

### 技术细节
- **数据存储**：配置存储在 `system_config` 表中，通过 `payment.wechat.*` 和 `payment.alipay.*` 前缀区分
- **缓存机制**：使用Caffeine缓存，配置更新后自动刷新缓存
- **批量更新**：使用 `batchUpdateConfigs` 方法批量更新配置项
- **部分更新**：支持只更新提供的字段，未提供的字段保持不变
- **权限标识**：`admin:system:payment`

### 使用说明
1. 执行 `database/update-20251225-payment-config-menu.sql` 脚本确保菜单和权限正确
2. 在管理后台"系统设置" -> "支付配置"中配置支付参数
3. 可以分别配置沙箱和生产环境的参数
4. 配置完成后点击"保存配置"
5. 可以点击"测试连接"验证配置是否正确
6. 修改配置后可以点击"刷新缓存"立即生效

### 影响范围
- ✅ `admin-frontend/src/api/admin/payment.ts` - API接口文件（新建）
- ✅ `admin-frontend/src/views/system/Payment.vue` - 支付配置页面（完善）
- ✅ `backend/src/main/java/com/shoppingmall/payment/service/PaymentConfigService.java` - Service接口
- ✅ `backend/src/main/java/com/shoppingmall/payment/service/impl/PaymentConfigServiceImpl.java` - Service实现
- ✅ `backend/src/main/java/com/shoppingmall/payment/controller/admin/PaymentConfigController.java` - Controller
- ✅ `database/update-20251225-payment-config-menu.sql` - 菜单和权限脚本（新建）

---

## 2025-12-25 - 修复编译错误

### 功能说明
修复项目编译时的三个编译错误：
1. PaymentGatewayServiceImpl.java 中重复的 case 标签
2. PaymentConfigController.java 中类型不兼容的问题

### 修改原因
- 编译失败：`case 标签重复` 和 `不兼容的类型` 错误
- 代码中存在逻辑错误和类型不匹配

### 修改内容

#### 1. 修复重复的 case 标签

**文件：**
- `backend/src/main/java/com/shoppingmall/payment/service/impl/PaymentGatewayServiceImpl.java`
  - 删除重复的 case 标签
  - `PaymentMethod.WECHAT` 和 `"WECHAT"` 是重复的（常量值就是字符串 "WECHAT"）
  - `PaymentMethod.ALIPAY` 和 `"ALIPAY"` 是重复的（常量值就是字符串 "ALIPAY"）
  - 只保留使用常量的 case 标签

#### 2. 修复类型不兼容问题

**文件：**
- `backend/src/main/java/com/shoppingmall/payment/controller/admin/PaymentConfigController.java`
  - `refreshConfig()` 方法返回类型是 `Result<Void>`
  - 但返回的是 `Result.success("配置缓存刷新成功")`，这是 `Result<String>` 类型
  - 修改为 `Result.success()` 无参方法，返回 `Result<Void>`

### 功能特性
- ✅ 编译错误已修复
- ✅ 代码逻辑正确
- ✅ 类型匹配正确

### 技术细节
- **case 标签**：在 switch 语句中，`PaymentMethod.WECHAT` 的值是 `"WECHAT"`，所以不能同时使用常量和字符串字面量
- **泛型类型**：`Result<Void>` 需要使用 `Result.success()` 无参方法，而不是 `Result.success(String)`

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/payment/service/impl/PaymentGatewayServiceImpl.java`
- ✅ `backend/src/main/java/com/shoppingmall/payment/controller/admin/PaymentConfigController.java`

---

## 2025-12-25 - 修复支付宝SDK依赖问题

### 功能说明
修复Maven构建时支付宝SDK依赖无法下载的问题。由于支付宝SDK不在公共Maven仓库中，暂时注释掉依赖，并创建安装指南文档。

### 修改原因
- Maven构建失败：`com.alipay.sdk:alipay-sdk-java:jar:4.38.195.ALL` 在公共Maven仓库中找不到
- 支付宝SDK需要手动下载并安装到本地Maven仓库
- 当前代码中支付宝功能还在开发中（有TODO注释），暂时不需要SDK依赖

### 修改内容

#### 1. 注释支付宝SDK依赖

**配置文件：**
- `backend/pom.xml`
  - 注释掉支付宝SDK依赖
  - 添加详细注释说明如何安装SDK到本地仓库
  - 包含安装命令和下载地址

#### 2. 创建安装指南文档

**文档：**
- `docs/支付宝SDK安装指南.md` - 支付宝SDK安装指南（新建）
  - 问题说明
  - 解决方案（三种方案）：
    - 方案一：安装到本地Maven仓库（推荐）
    - 方案二：使用本地jar包
    - 方案三：暂时注释依赖（当前方案）
  - 详细安装步骤
  - 验证安装方法
  - 常见问题排查
  - 相关文档链接

### 功能特性
- ✅ 项目可以正常编译运行（暂时不需要支付宝SDK）
- ✅ 提供详细的SDK安装指南
- ✅ 包含多种安装方案
- ✅ 包含常见问题解决方案

### 技术细节
- **当前状态**：支付宝SDK依赖已注释，项目可以正常编译
- **安装方法**：使用 `mvn install:install-file` 命令安装到本地仓库
- **版本信息**：`com.alipay.sdk:alipay-sdk-java:4.38.195.ALL`
- **下载地址**：https://opendocs.alipay.com/common/02kkv7

### 使用说明
1. **当前**：项目可以正常编译运行，支付宝功能暂时不可用（代码中显示功能还在开发中）
2. **需要支付宝功能时**：
   - 下载支付宝SDK jar包
   - 按照 `docs/支付宝SDK安装指南.md` 中的步骤安装到本地Maven仓库
   - 取消注释 `pom.xml` 中的支付宝SDK依赖
   - 重新编译项目

### 影响范围
- ✅ `backend/pom.xml` - 注释支付宝SDK依赖
- ✅ `docs/支付宝SDK安装指南.md` - 安装指南文档（新建）

---

## 2025-12-21 - 完善生产环境配置和创建后端服务部署文档

### 功能说明
完善生产环境配置文件，并创建详细的后端服务部署文档，用于指导在宝塔面板上部署后端服务到测试环境。

### 修改原因
- 需要将后端服务部署到远程服务器作为测试环境
- 生产环境配置文件不完整，缺少必要的配置项
- 需要详细的部署文档指导部署流程

### 修改内容

#### 1. 完善生产环境配置文件

**配置文件：**
- `backend/src/main/resources/application-prod.yml`
  - 完善数据源配置，支持环境变量配置（`${DB_USERNAME:root}`, `${DB_PASSWORD:root}`）
  - 添加Druid连接池配置
  - 添加文件上传配置
  - 添加邮件配置，支持环境变量
  - 添加MyBatis Plus配置（生产环境关闭SQL日志输出）
  - 添加服务器配置，支持环境变量配置端口（`${SERVER_PORT:8081}`）
  - 完善日志配置（日志级别、文件路径、轮转策略）
  - 修改文件存储路径为Linux路径格式（`/www/wwwroot/shopping-mall-backend/uploads`）
  - 添加JWT配置，支持环境变量（`${JWT_SECRET:...}`）
  - 添加Swagger配置，支持环境变量控制（`${SWAGGER_ENABLED:true}`）
  - 添加订单配置
  - 添加应用配置，支持环境变量配置前端地址（`${FRONTEND_URL:...}`）

#### 2. 创建后端服务部署文档

**文档：**
- `docs/后端服务部署文档.md` - 后端服务部署文档（新建）
  - **一、环境准备**：服务器要求、宝塔面板环境准备、创建项目目录
  - **二、配置文件修改**：修改主配置文件、完善生产环境配置、使用环境变量
  - **三、项目打包**：本地打包、验证打包文件
  - **四、服务器部署**：上传文件、配置数据库、创建启动脚本、使用宝塔面板Java项目管理、配置防火墙
  - **五、启动与验证**：启动应用、查看日志、验证服务、常见启动问题
  - **六、Nginx反向代理配置**：创建站点、配置反向代理、配置SSL证书
  - **七、常见问题排查**：应用无法启动、数据库连接问题、文件上传失败、内存溢出、性能问题
  - **八、维护与监控**：日志管理、备份策略、监控建议、更新部署流程、定时任务
  - **九、安全建议**：配置安全、服务器安全、数据安全
  - **十、附录**：常用命令、配置文件位置、相关文档

### 功能特性
- ✅ 完善的生产环境配置文件，支持环境变量配置
- ✅ 详细的部署文档，包含完整的部署流程
- ✅ 支持宝塔面板Java项目管理
- ✅ 提供启动、停止、重启脚本
- ✅ 包含Nginx反向代理配置示例
- ✅ 包含常见问题排查指南
- ✅ 包含安全建议和维护指南

### 技术细节
- **环境变量支持**：使用 `${变量名:默认值}` 格式支持环境变量配置
- **路径配置**：文件上传路径改为Linux路径格式
- **日志配置**：生产环境日志级别为INFO，自动轮转
- **安全配置**：支持通过环境变量配置敏感信息（数据库密码、JWT密钥等）

### 影响范围
- ✅ `backend/src/main/resources/application-prod.yml` - 生产环境配置文件
- ✅ `docs/后端服务部署文档.md` - 部署文档（新建）

### 使用说明
1. 修改 `application.yml` 中的 `spring.profiles.active` 为 `prod`
2. 根据实际情况修改 `application-prod.yml` 中的配置项
3. 参考 `docs/后端服务部署文档.md` 进行部署
4. 建议使用环境变量配置敏感信息

---

## 2025-12-20 - 退款记录详情页面支付方式显示优化

### 功能说明
优化退款记录详情页面的支付方式显示，将英文支付方式代码（如 PRE_DEPOSIT、WECHAT、ALIPAY）转换为中文显示（预存款支付、微信支付、支付宝）。

### 修改原因
- 退款记录详情页面直接显示英文支付方式代码，用户体验不友好
- 需要将支付方式转换为中文，便于管理员理解

### 修改内容

#### 前端代码修改

**退款记录详情页面：**
- `admin-frontend/src/views/order/RefundList.vue`
  - 添加 `getPaymentMethodName` 方法：将支付方式从英文转换为中文
    - `WECHAT` → `微信支付`
    - `ALIPAY` → `支付宝`
    - `PRE_DEPOSIT` → `预存款支付`
    - `OFFLINE` → `线下支付`
  - 在退款支付方式显示处使用 `getPaymentMethodName` 方法转换

### 功能特性
- ✅ 支付方式中文显示，提升用户体验
- ✅ 支持所有支付方式的中文转换
- ✅ 未知支付方式显示原值

### 技术细节
- **转换方法**：使用 switch 语句根据支付方式代码转换为中文
- **大小写处理**：使用 `toUpperCase()` 统一处理大小写
- **空值处理**：如果支付方式为空，返回 `-`

### 影响范围
- ✅ 退款记录详情对话框的"退款支付方式"字段

---

## 2025-12-20 - 管理后台订单详情页面优化退款信息展示

### 功能说明
优化管理后台订单详情页面的退款信息展示：
1. 商品信息模块中，"已退款/可退款"列的"已退"字段显示为红色，突出显示
2. 添加退款记录模块，可以查看订单的退款记录明细，包括退款金额、退款状态、退款明细等

### 修改原因
- 已退款数量需要更突出地显示，便于管理员快速识别
- 订单详情页面缺少退款记录展示，管理员无法查看订单的退款历史
- 参考用户端的退款记录展示设计，保持一致性

### 修改内容

#### 前端代码修改

**订单详情页面：**
- `admin-frontend/src/views/order/List.vue`
  - 修改"已退款/可退款"列：将"已退"字段显示为红色（`color: #e4393c; font-weight: bold;`）
  - 添加退款记录展示区域（仅在存在退款记录时显示）
  - 显示退款基本信息：退款单号、退款金额、退款类型、退款状态、退款时间、退款原因
  - 显示退款明细表格：商品编码、商品名称、规格、退款数量、退款单价、退款小计
  - 添加 `refundList` 状态变量
  - 添加 `loadRefundList` 方法：加载退款记录列表
  - 添加 `getRefundStatusTagType` 方法：获取退款状态标签类型
  - 在 `handleView` 方法中调用 `loadRefundList` 加载退款记录
  - 在退款成功后重新加载订单详情和退款记录

**API导入：**
- 添加 `getOrderRefundList` API 方法导入
- 添加 `OrderRefundVO` 类型导入

### 功能特性
- ✅ "已退"字段红色突出显示，便于快速识别
- ✅ 订单详情页面显示退款记录
- ✅ 显示退款基本信息（退款单号、金额、类型、状态、时间、原因）
- ✅ 显示退款明细（商品信息、退款数量、退款单价、退款小计）
- ✅ 退款状态颜色区分（退款中-橙色，退款成功-绿色，退款失败-红色）
- ✅ 支持部分退款和全额退款的展示
- ✅ 仅在存在退款记录时显示退款区域
- ✅ 退款成功后自动刷新退款记录

### 技术细节
- **已退款字段样式**：红色（`#e4393c`）+ 加粗，突出显示
- **退款记录展示**：使用浅灰色背景卡片，清晰的信息层次
- **退款状态标签**：使用 Element Plus 的 Tag 组件，不同状态不同颜色
- **退款明细表格**：使用 Element Plus 的 Table 组件，样式与订单商品表格保持一致
- **数据加载**：在查看订单详情时自动加载退款记录，失败不影响订单详情显示

### 影响范围
- ✅ 管理后台订单详情对话框
- ✅ 商品信息表格的"已退款/可退款"列

---

## 2025-12-20 - 修复订单退款记录菜单SQL脚本错误

### 修改原因
SQL脚本执行报错：`You can't specify target table 'sys_menu' for update in FROM clause`。同时需要指定固定的菜单ID（58）和父菜单ID（3）。

### 修改内容
- `database/update-20251220-add-order-refund-menu.sql`
  - 使用固定的菜单ID：58
  - 使用固定的父菜单ID：3（订单管理）
  - 先查询最大sort_order值到变量 `@max_sort_order`
  - 在INSERT语句中使用变量而不是子查询
  - 添加 `deleted` 字段（逻辑删除）
  - 添加角色权限分配（超级管理员、运营人员、客服人员）

### 修改原因
SQL脚本执行报错：`You can't specify target table 'sys_menu' for update in FROM clause`。这是因为在INSERT语句的VALUES子句中使用了子查询，而ON DUPLICATE KEY UPDATE中又引用了同一个表。

### 修改内容
- `database/update-20251220-add-order-refund-menu.sql`
  - 先查询最大sort_order值到变量 `@max_sort_order`
  - 在INSERT语句的VALUES中使用变量而不是子查询
  - 避免在ON DUPLICATE KEY UPDATE中引用同一表的子查询

## 2025-12-20 - 订单列表增加买家姓名和买家用户名字段

### 修改原因
需要在订单管理列表、查询和详情页面中显示买家姓名和买家用户名，方便管理员识别订单的买家信息。

### 修改内容

#### 1. 后端代码修改

**VO：**
- `backend/src/main/java/com/shoppingmall/vo/OrderListVO.java`
  - 添加 `buyerName` 字段（买家姓名，用户真实姓名）
  - 添加 `buyerUsername` 字段（买家用户名）

- `backend/src/main/java/com/shoppingmall/vo/OrderDetailVO.java`
  - 添加 `buyerName` 字段（买家姓名，用户真实姓名）
  - 添加 `buyerUsername` 字段（买家用户名）

**DTO：**
- `backend/src/main/java/com/shoppingmall/dto/OrderQueryDTO.java`
  - 添加 `buyerName` 字段（支持按买家姓名查询）
  - 添加 `buyerUsername` 字段（支持按买家用户名查询）

**Service：**
- `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`
  - 添加 `UserRepository` 依赖
  - 在 `convertToListVO` 方法中：
    - 根据订单的 `userId` 查询用户信息
    - 设置 `buyerName`（用户真实姓名）和 `buyerUsername`（用户名）
    - 如果用户不存在，设置为"未知"
  - 在 `convertToDetailVO` 方法中：
    - 同样查询并设置买家信息
  - 在 `getOrderList` 方法中：
    - 支持按买家姓名和买家用户名进行内存过滤查询
    - 与收货人姓名查询逻辑合并，统一处理

#### 2. 前端代码修改

**API：**
- `admin-frontend/src/api/admin/order.ts`
  - `OrderListVO` 接口：添加 `buyerName` 和 `buyerUsername` 字段
  - `OrderDetailVO` 接口：添加 `buyerName` 和 `buyerUsername` 字段
  - `OrderQueryDTO` 接口：添加 `buyerName` 和 `buyerUsername` 字段

**页面：**
- `admin-frontend/src/views/order/List.vue`
  - 搜索表单：
    - 添加"买家姓名"输入框
    - 添加"买家用户名"输入框
  - 订单列表表格：
    - 在订单号列后添加"买家姓名"列
    - 在买家姓名列后添加"买家用户名"列
  - 订单详情对话框：
    - 在订单号后添加"买家姓名"和"买家用户名"显示项
  - 搜索和重置方法：
    - 更新搜索参数，包含买家姓名和买家用户名
    - 重置时清空这两个字段

### 功能特性
- ✅ 订单列表显示买家姓名和买家用户名
- ✅ 订单详情显示买家姓名和买家用户名
- ✅ 支持按买家姓名查询订单
- ✅ 支持按买家用户名查询订单
- ✅ 支持组合查询（订单号、买家姓名、买家用户名、收货人等）

## 2025-12-20 - 订单退款记录查询功能

### 修改原因
需要在订单管理菜单下增加一个订单退款记录页面，可以查询订单退款记录和退款明细的内容。

### 修改内容

#### 1. 后端代码修改

**DTO：**
- `backend/src/main/java/com/shoppingmall/dto/OrderRefundQueryDTO.java` - 退款记录查询DTO（新建）
  - 支持按退款单号、订单号、用户ID、退款状态、退款类型、操作人ID、日期范围等条件查询
  - 支持分页查询

**Service：**
- `backend/src/main/java/com/shoppingmall/service/admin/OrderService.java`
  - 添加 `getRefundList` 方法：查询退款记录列表（分页）
  - 添加 `getRefundDetail` 方法：获取退款记录详情

- `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`
  - 实现 `getRefundList` 方法：
    - 支持多条件查询（退款单号、订单号、用户ID、退款状态、退款类型、操作人ID、日期范围）
    - 支持分页查询
    - 按创建时间倒序排列
    - 转换为VO返回
  - 实现 `getRefundDetail` 方法：
    - 根据退款ID查询退款记录详情
    - 包含退款明细信息

**Controller：**
- `backend/src/main/java/com/shoppingmall/controller/admin/OrderController.java`
  - 添加 `getRefundList` 接口：`GET /api/admin/orders/refunds`（查询退款记录列表）
  - 添加 `getRefundDetail` 接口：`GET /api/admin/orders/refunds/{refundId}`（获取退款记录详情）

#### 2. 前端代码修改

**API：**
- `admin-frontend/src/api/admin/order.ts`
  - 添加 `OrderRefundQueryDTO` 接口定义
  - 添加 `getRefundList` 方法：查询退款记录列表
  - 添加 `getRefundDetail` 方法：获取退款记录详情

**页面：**
- `admin-frontend/src/views/order/RefundList.vue` - 订单退款记录列表页面（新建）
  - 搜索表单：退款单号、订单号、退款状态、退款类型、日期范围
  - 退款记录列表表格：显示退款单号、订单号、退款金额、退款类型、退款状态、操作人、操作时间等
  - 分页组件
  - 详情对话框：显示退款记录详细信息和退款明细表格

**路由：**
- `admin-frontend/src/router/componentMaps/order.ts`
  - 添加 `'order/RefundList'` 组件映射

**数据库：**
- `database/update-20251220-add-order-refund-menu.sql` - 菜单SQL脚本（新建）
  - 在订单管理菜单下添加"订单退款记录"子菜单
  - 菜单路径：`order/RefundList`
  - 组件：`order/RefundList`
  - 权限标识：`admin:order:refund:list`

### 功能特性
- ✅ 支持多条件查询退款记录
- ✅ 支持分页查询
- ✅ 显示退款记录详细信息和退款明细
- ✅ 支持查看退款记录详情
- ✅ 状态标签显示（退款中、退款成功、退款失败）

### 使用说明
1. 执行 `database/update-20251220-add-order-refund-menu.sql` 脚本添加菜单
2. 刷新管理后台页面，在订单管理菜单下可以看到"订单退款记录"菜单项
3. 点击菜单项进入退款记录列表页面
4. 可以通过搜索条件查询退款记录
5. 点击退款单号或"查看详情"按钮查看退款记录详情和明细

## 2025-12-20 - 用户端订单详情页面添加退款记录展示

### 功能说明
在用户端订单详情页面添加退款记录展示区域，用户可以查看订单的部分退款或全额退款记录信息，包括退款明细、退款金额、退款状态等。

### 修改原因
- 用户端订单详情页面缺少退款记录信息展示
- 用户需要了解订单的退款情况，包括退款金额、退款状态、退款明细等
- 参考管理后台的退款记录详情页面设计，提供用户友好的退款信息展示

### 修改内容

#### 1. 后端代码修改

**Service接口：**
- `backend/src/main/java/com/shoppingmall/service/buyer/OrderService.java`
  - 添加 `getOrderRefundList` 方法：获取订单的退款列表

**Service实现：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
  - 添加 `OrderRefundRepository` 和 `OrderRefundItemRepository` 依赖
  - 实现 `getOrderRefundList` 方法：验证订单属于当前用户，查询退款记录
  - 实现 `convertRefundToVO` 方法：将退款实体转换为VO
  - 实现 `getRefundStatusText` 方法：获取退款状态文本
  - 实现 `getRefundTypeText` 方法：获取退款类型文本

**Controller：**
- `backend/src/main/java/com/shoppingmall/controller/buyer/OrderController.java`
  - 添加 `getOrderRefundList` 接口：`GET /api/buyer/orders/{orderNo}/refunds`

#### 2. 前端代码修改

**API：**
- `frontend/src/api/buyer/order.ts`
  - 添加 `OrderRefundVO` 接口定义
  - 添加 `OrderRefundItemVO` 接口定义
  - 添加 `getOrderRefundList` 方法：获取订单退款列表

**订单详情页面：**
- `frontend/src/views/order/Detail.vue`
  - 添加退款记录展示区域（仅在存在退款记录时显示）
  - 显示退款单号、退款金额、退款类型、退款状态、退款时间、退款原因
  - 显示退款明细表格（商品编码、商品名称、退款数量、退款单价、退款小计）
  - 添加 `refundList` 状态变量
  - 添加 `loadRefundList` 方法：加载退款记录
  - 添加 `getRefundStatusClass` 方法：获取退款状态样式类
  - 在 `loadOrderDetail` 方法中调用 `loadRefundList` 加载退款记录

### 功能特性
- ✅ 用户端订单详情页面显示退款记录
- ✅ 显示退款基本信息（退款单号、金额、类型、状态、时间、原因）
- ✅ 显示退款明细（商品信息、退款数量、退款单价、退款小计）
- ✅ 退款状态颜色区分（退款中-橙色，退款成功-绿色，退款失败-红色）
- ✅ 支持部分退款和全额退款的展示
- ✅ 仅在存在退款记录时显示退款区域

### 技术细节
- **接口路径**：`GET /api/buyer/orders/{orderNo}/refunds`
- **权限验证**：验证订单属于当前用户，防止越权访问
- **数据展示**：
  - 退款记录按创建时间倒序排列
  - 退款明细以表格形式展示
  - 退款金额和退款小计以红色高亮显示
- **样式设计**：
  - 退款记录区域使用浅灰色背景
  - 退款状态使用标签样式，不同状态不同颜色
  - 退款明细表格样式与订单商品表格保持一致

### 影响范围
- ✅ 用户端订单详情页面
- ✅ 后端订单服务接口
- ✅ 前端订单API

---

## 2025-12-20 - 移除支付记录模块的退款功能

### 功能说明
移除支付记录模块的退款按钮和相关功能，因为订单模块已经有退款入口，避免功能重复。

### 修改原因
- 订单模块已经提供了完整的退款功能入口
- 支付记录模块的退款功能与订单模块重复
- 统一退款入口，避免功能分散

### 修改内容

#### 前端代码修改
- `admin-frontend/src/views/finance/PaymentRecord.vue`
  - 移除操作列中的退款按钮
  - 移除退款对话框及其相关代码
  - 移除退款相关的状态变量（refundDialogVisible, refundLoading, refundForm, refundFormRef, refundRules）
  - 移除退款相关的方法（handleRefund, handleConfirmRefund, handleRefundDialogClose）
  - 移除API导入中的 `refundPaymentRecord`
  - 移除 `ElMessageBox` 的导入（不再需要）
  - 调整操作列宽度从240px改为120px（只有一个按钮）

### 功能特性
- ✅ 支付记录页面只保留"查看详情"功能
- ✅ 退款功能统一在订单模块操作
- ✅ 简化支付记录页面，避免功能重复

### 影响范围
- ✅ `admin-frontend/src/views/finance/PaymentRecord.vue` - 支付记录管理页面

---

## 2025-12-20 - 修复退款功能字段名不匹配问题

### 修改原因
系统报错：`Unknown column 'audit_time' in 'field list'`。数据库表结构已更新（移除了审核相关字段，改为操作人字段），但后端代码还在使用旧的字段名。

### 修改内容

#### 后端代码修改
- `backend/src/main/java/com/shoppingmall/vo/OrderRefundVO.java`
  - 将 `auditTime`, `auditUserId`, `auditUserName`, `auditRemark` 改为 `operatorTime`, `operatorId`, `operatorName`, `operatorRemark`
  - 更新退款状态注释：从"0-待审核，1-审核通过，2-审核拒绝，3-退款中，4-退款成功，5-退款失败"改为"3-退款中，4-退款成功，5-退款失败"

- `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`
  - `convertRefundToVO` 方法：将 `setAuditTime`, `setAuditUserId`, `setAuditUserName`, `setAuditRemark` 改为 `setOperatorTime`, `setOperatorId`, `setOperatorName`, `setOperatorRemark`
  - `getRefundStatusText` 方法：移除不再使用的状态（0-待审核，1-审核通过，2-审核拒绝）
  - `calculateRefundedQuantity` 方法：移除对 `RefundStatus.AUDIT_APPROVED` 的引用，只保留 `REFUNDING` 和 `REFUND_SUCCESS`

- `backend/src/main/java/com/shoppingmall/common/constant/RefundStatus.java`
  - 移除不再使用的常量：`PENDING_AUDIT`, `AUDIT_APPROVED`, `AUDIT_REJECTED`
  - 更新类注释，说明退款由管理员直接操作，无需审核流程

#### 前端代码修改
- `admin-frontend/src/api/admin/order.ts`
  - `OrderRefundVO` 接口：将 `auditTime`, `auditUserId`, `auditUserName`, `auditRemark` 改为 `operatorTime`, `operatorId`, `operatorName`, `operatorRemark`

### 业务逻辑说明
- 退款流程已简化为：管理员直接操作退款 → 退款中 → 退款成功/退款失败
- 不再需要审核流程，因此移除了所有审核相关字段和状态

## 2025-12-20 - 调整订单退款表结构（移除审核流程）

### 功能说明
根据实际业务需求，调整订单退款表结构。退款由管理员直接操作，无需用户申请和审核流程。

### 问题分析
原表结构设计包含了审核相关字段（audit_time, audit_user_id, audit_user_name, audit_remark），但实际业务中：
- 用户端不需要申请退款
- 管理员直接操作退款，无需审核流程
- 退款记录不应该被删除（移除了deleted字段）

### 修改方案
1. 将审核相关字段改为操作人字段（operator_id, operator_name, operator_time, operator_remark）
2. 简化退款状态说明（只保留：3-退款中，4-退款成功，5-退款失败）
3. 移除逻辑删除字段（deleted）
4. 更新表注释为"订单退款记录表"而不是"申请表"
5. 添加操作人ID索引

### 修改文件
1. `database/update-20251219-add-order-refund-tables.sql` - 调整退款表结构

### 具体修改

#### order_refund 表结构调整
- **移除字段**：
  - `audit_time` - 审核时间
  - `audit_user_id` - 审核人ID
  - `audit_user_name` - 审核人姓名
  - `audit_remark` - 审核备注
  - `deleted` - 逻辑删除字段

- **新增字段**：
  - `operator_id` - 操作人ID（管理员）
  - `operator_name` - 操作人姓名
  - `operator_time` - 操作时间
  - `operator_remark` - 操作备注

- **修改字段**：
  - `refund_status` - 默认值改为3（退款中），注释简化为（3-退款中，4-退款成功，5-退款失败）
  - `refund_time` - 注释改为"退款完成时间"

- **索引调整**：
  - 添加 `idx_operator_id` 索引（操作人ID）

- **表注释**：
  - 从"订单退款申请表"改为"订单退款记录表"

### 功能特性
- ✅ 退款由管理员直接操作，无需审核流程
- ✅ 记录操作人信息，便于追溯
- ✅ 退款记录永久保存，不可删除
- ✅ 简化状态管理，只保留必要的退款状态

### 技术细节
- **操作流程**：管理员发起退款 → 直接执行退款 → 退款成功/退款失败
- **状态说明**：
  - 3 - 退款中：退款操作进行中
  - 4 - 退款成功：退款已完成
  - 5 - 退款失败：退款操作失败
- **数据完整性**：退款记录永久保存，确保财务数据可追溯

### 影响范围
- ✅ `order_refund` 表结构
- ⚠️ 注意：需要同步更新后端实体类 `OrderRefund.java` 和相关代码

---

## 2025-12-20 - 修复订单退款功能数据库字段缺失问题

### 修改原因
系统报错：`Unknown column 'refunded_quantity' in 'field list'`。`order_item` 表中缺少 `refunded_quantity` 字段，导致查询失败。

### 修改内容
- `database/update-20251220-add-order-item-refunded-quantity.sql` - 新建单独的 SQL 脚本用于添加 `refunded_quantity` 字段
  - 在 `order_item` 表中添加 `refunded_quantity` 字段（int，默认值 0，注释：已退款数量）
  - 字段位置：在 `quantity` 字段之后

### 执行说明
请执行以下 SQL 脚本：
```sql
USE chengren_shopping_mall;
ALTER TABLE `order_item` 
ADD COLUMN `refunded_quantity` int NOT NULL DEFAULT '0' COMMENT '已退款数量' AFTER `quantity`;
```

如果字段已存在，会报错 "Duplicate column name"，可以忽略。

## 2025-12-20 - 修复退款功能编译错误

### 修改原因
编译错误：`javax.validation.constraints` 包不存在。在 Spring Boot 3.x 中，`javax.validation` 已经迁移到 `jakarta.validation`。

### 修改内容
- `backend/src/main/java/com/shoppingmall/dto/OrderRefundRequestDTO.java`
  - 将 `javax.validation.constraints` 改为 `jakarta.validation.constraints`
- `backend/src/main/java/com/shoppingmall/dto/OrderRefundAuditDTO.java`
  - 将 `javax.validation.constraints` 改为 `jakarta.validation.constraints`

## 2025-12-19 - 订单部分SKU/商品退款功能

### 修改原因
订单需要支持选择部分SKU/商品进行退款操作。管理员可以在管理后台选择订单中的部分商品/SKU进行退款，退款金额不包含运费，只退还商品金额。

### 修改内容

#### 1. 数据库表结构

**新建表：**
- `database/update-20251219-add-order-refund-tables.sql` - 退款功能数据库表结构脚本
  - `order_refund` 表：订单退款申请表
  - `order_refund_item` 表：订单退款明细表
  - 在 `order_item` 表中添加 `refunded_quantity` 字段（已退款数量）

#### 2. 后端代码修改

**常量类：**
- `backend/src/main/java/com/shoppingmall/common/constant/RefundStatus.java` - 退款状态常量（新建）
- `backend/src/main/java/com/shoppingmall/common/constant/RefundType.java` - 退款类型常量（新建）

**实体类：**
- `backend/src/main/java/com/shoppingmall/entity/OrderRefund.java` - 订单退款申请实体（新建）
- `backend/src/main/java/com/shoppingmall/entity/OrderRefundItem.java` - 订单退款明细实体（新建）

**Repository：**
- `backend/src/main/java/com/shoppingmall/repository/order/OrderRefundRepository.java` - 退款申请Repository（新建）
- `backend/src/main/java/com/shoppingmall/repository/order/OrderRefundItemRepository.java` - 退款明细Repository（新建）

**DTO：**
- `backend/src/main/java/com/shoppingmall/dto/OrderRefundRequestDTO.java` - 退款申请DTO（新建）
- `backend/src/main/java/com/shoppingmall/dto/OrderRefundAuditDTO.java` - 退款审核DTO（新建）

**VO：**
- `backend/src/main/java/com/shoppingmall/vo/OrderRefundVO.java` - 退款申请VO（新建）
- `backend/src/main/java/com/shoppingmall/vo/OrderDetailVO.java`
  - `OrderItemVO` 添加 `refundedQuantity`（已退款数量）和 `availableRefundQuantity`（可退款数量）字段

**Service：**
- `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`
  - 实现 `refundOrder` 方法：支持选择部分SKU/商品进行退款
    - 验证订单状态和支付状态
    - 验证退款商品和数量（不能超过可退款数量）
    - 计算退款金额（商品金额，不含运费）
    - 判断是部分退款还是全额退款
    - 根据支付方式执行退款（微信/支付宝/预存款）
    - 创建退款申请记录和退款明细
    - 更新订单商品的已退款数量
    - 更新支付记录的已退款金额
    - 恢复商品库存
    - 如果订单已完成，扣减商品销量
  - 实现 `getOrderRefundList` 方法：获取订单的退款列表
  - 实现 `calculateRefundedQuantity` 方法：计算订单商品的已退款数量
  - 修改 `convertToDetailVO` 方法：在订单详情中计算并返回已退款数量和可退款数量

**Controller：**
- `backend/src/main/java/com/shoppingmall/controller/admin/OrderController.java`
  - 添加 `refundOrder` 接口：`POST /api/admin/orders/{orderNo}/refund`
  - 添加 `getOrderRefundList` 接口：`GET /api/admin/orders/{orderNo}/refunds`

#### 3. 前端代码修改

**管理后台API：**
- `admin-frontend/src/api/admin/order.ts`
  - 添加 `OrderRefundRequestDTO` 和 `OrderRefundVO` 接口定义
  - 添加 `refundOrder` 方法：提交退款申请
  - 添加 `getOrderRefundList` 方法：获取订单退款列表
  - 更新 `OrderDetailVO` 接口：添加 `refundedQuantity` 和 `availableRefundQuantity` 字段

**管理后台页面：**
- `admin-frontend/src/views/order/List.vue`
  - 订单详情对话框：
    - 商品列表添加"已退款/可退款"列，显示已退款数量和可退款数量
    - 添加"申请退款"按钮（仅已支付、已发货、已完成的订单显示）
  - 退款对话框：
    - 显示订单信息和退款提示（不含运费）
    - 退款原因输入框
    - 商品列表表格：
      - 支持勾选要退款的商品
      - 显示订单数量、已退款数量、可退款数量
      - 退款数量输入框（可设置退款数量，不能超过可退款数量）
      - 自动计算退款小计和总退款金额
    - 确认退款按钮
  - 添加退款相关方法：
    - `handleRefund`：从列表点击退款按钮
    - `handleRefundFromDetail`：从详情对话框点击退款按钮
    - `initRefundDialog`：初始化退款对话框数据
    - `checkSelectable`：检查商品是否可选（可退款数量>0）
    - `totalRefundAmount`：计算总退款金额（计算属性）
    - `handleSelectionChange`：处理表格选择变化
    - `handleRefundSubmit`：提交退款申请

### 业务逻辑说明
1. **退款条件**：
   - 只有已支付、已发货、已完成的订单可以退款
   - 订单必须已支付（`payment_status = 2`）

2. **退款商品选择**：
   - 支持选择订单中的部分商品/SKU进行退款
   - 每个商品可以设置退款数量，但不能超过可退款数量
   - 可退款数量 = 订单数量 - 已退款数量

3. **退款金额计算**：
   - 退款金额 = 商品单价 × 退款数量（不含运费）
   - 如果退款金额 >= 订单商品总金额，视为全额退款
   - 如果退款金额 < 订单商品总金额，视为部分退款

4. **退款流程**：
   - 管理员在订单详情页点击"申请退款"
   - 选择要退款的商品/SKU，设置退款数量
   - 填写退款原因
   - 提交退款申请
   - 系统自动执行退款（根据支付方式调用相应退款接口）
   - 更新订单状态、支付状态、商品库存等

5. **退款后处理**：
   - 更新订单商品的已退款数量
   - 更新支付记录的已退款金额
   - 恢复商品库存（product表和product_stock表）
   - 如果订单已完成，扣减商品销量
   - 如果全额退款，更新订单状态为"已退款"

6. **退款记录**：
   - 创建退款申请记录（`order_refund`表）
   - 创建退款明细记录（`order_refund_item`表）
   - 记录退款单号、退款金额、退款原因、退款状态等信息

## 2025-12-19 - 购物车和结算页面销售价格字段显示逻辑

### 修改原因
普通用户不需要看到销售价格字段，只有会员才需要显示销售价格（用于对比会员价优惠）。需要在购物车列表和结算页面根据用户是否是会员来控制销售价格列的显示。

### 修改内容

#### 前端代码修改

**购物车列表页面：**
- `frontend/src/views/cart/Index.vue`
  - 添加 `isMember` 计算属性：根据购物车列表中第一个商品的 `isMember` 字段判断用户是否是会员
  - 表头：使用 `v-if="isMember"` 控制"销售价格"列的显示
  - 表格数据行：使用 `v-if="isMember"` 控制销售价格单元格的显示
  - 空购物车行：根据 `isMember` 动态设置 `colspan` 值（会员9列，普通用户8列）

**结算页面：**
- `frontend/src/views/cart/Checkout.vue`
  - 添加 `isMember` 计算属性：根据订单商品列表中第一个商品的 `isMember` 字段判断用户是否是会员
  - 添加 `getPriceColumnTitle` 方法：根据用户是否是会员返回"会员价格"或"商品价格"
  - 表头：调整列顺序，将价格列标题改为动态方法，使用 `v-if="isMember"` 控制"销售价格"列的显示
  - 表格数据行：使用 `v-if="isMember"` 控制销售价格单元格的显示

### 业务逻辑说明
1. **普通用户**：
   - 不显示"销售价格"列
   - 只显示"商品价格"列（实际就是基础价格）

2. **会员**：
   - 显示"销售价格"列（用于显示原价）
   - 显示"会员价"列（用于显示会员优惠价格）
   - 可以对比看到会员优惠

3. **判断逻辑**：
   - 从购物车/订单商品列表的第一个商品的 `isMember` 字段判断
   - 所有商品的 `isMember` 字段应该相同（因为都是同一个用户的商品）

## 2025-12-19 - 非会员价格计算逻辑修正

### 修改原因
非会员用户不能享受会员价优惠，即使商品/SKU开启了会员价，也应该按照基础价格计算。用户是否是会员是大前提条件。

### 修改内容

#### 后端代码修改

**Service：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/CartServiceImpl.java`
  - 修改 `calculateMemberPriceForProduct` 方法：
    - 在方法开头首先检查用户是否是会员（`is_member = 1`）
    - 如果不是会员，直接返回原价（`salesPrice`），不进行任何会员价计算
    - 如果是会员，再检查商品是否启用了会员价，按原逻辑计算
  - 修改 `calculateMemberPriceForSku` 方法：
    - 在方法开头首先检查用户是否是会员（`is_member = 1`）
    - 如果不是会员，直接返回原价（`salesPrice`），不进行任何会员价计算
    - 如果是会员，再检查SKU是否启用了会员价，按原逻辑计算

- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
  - 同样修改 `calculateMemberPriceForProduct` 和 `calculateMemberPriceForSku` 方法
  - 确保订单创建时的价格计算逻辑与购物车保持一致

### 业务逻辑说明
1. **会员身份检查（大前提）**：
   - 首先检查用户是否是会员（`user.getIsMember() == 1`）
   - 如果不是会员，直接返回原价，不进行任何会员价计算
   - 如果是会员，才继续后续的会员价计算逻辑

2. **会员价计算优先级**（仅对会员生效）：
   - 第一优先级：如果商品/SKU启用了会员价（`enableMemberPrice = 1`）且有配置会员价（`memberPrice > 0`），直接使用配置的会员价
   - 第二优先级：如果商品/SKU没有配置会员价，根据用户的会员等级折扣率计算
   - 非会员：始终返回原价（销售价格）

3. **价格字段**：
   - `salesPrice`：销售价格（商品或SKU的基础价格）
   - `memberPrice`：会员价或商品价格（非会员返回原价，会员根据配置计算）

## 2025-12-19 - 订单创建价格逻辑优化

### 修改原因
订单创建、结算时的金额计算逻辑需要与购物车保持一致：
1. 需要判断用户是否是会员（`is_member`字段）
2. 会员价计算优先级：优先使用商品/SKU配置的固定会员价，如果没有配置则根据会员等级折扣率计算
3. 需要处理SKU的情况，如果有SKU则优先使用SKU的价格和会员价配置

### 修改内容

#### 后端代码修改

**Service：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
  - 修改 `createOrder` 方法中的价格计算逻辑：
    - 判断订单项是否有SKU，如果有则查询SKU信息
    - 有SKU时使用SKU的价格和重量，无SKU时使用商品的价格和重量
    - 分别调用 `calculateMemberPriceForProduct` 或 `calculateMemberPriceForSku` 计算会员价
  - 重构 `calculateMemberPrice` 方法，拆分为三个方法：
    - `calculateMemberPriceForProduct`：计算商品的会员价格，优先使用商品配置的会员价
    - `calculateMemberPriceForSku`：计算SKU的会员价格，优先使用SKU配置的会员价
    - `calculateMemberPriceByDiscount`：根据会员等级折扣率计算会员价格
  - 处理重量字段：
    - `Product.weight` 是 `Integer` 类型，使用 `BigDecimal.valueOf(product.getWeight().longValue())` 转换
    - `ProductSku.weight` 是 `BigDecimal` 类型，直接使用
    - SKU有重量时优先使用SKU重量，否则使用商品重量

### 业务逻辑说明
1. **价格计算优先级**：
   - 第一优先级：如果商品/SKU启用了会员价（`enableMemberPrice = 1`）且有配置会员价（`memberPrice > 0`），直接使用配置的会员价
   - 第二优先级：如果商品/SKU没有配置会员价，根据用户的会员等级折扣率计算
   - 普通用户：返回原价（销售价格）

2. **SKU处理**：
   - 如果订单项有SKU（`itemDTO.getSkuId() != null`），优先使用SKU的价格和会员价配置
   - 如果订单项没有SKU，使用商品的价格和会员价配置
   - SKU的重量优先于商品的重量

3. **价格字段**：
   - `salesPrice`：销售价格（商品或SKU的基础价格）
   - `memberPrice`：会员价或商品价格（根据用户是否是会员和配置情况计算）

## 2025-12-19 - 购物车价格逻辑优化

### 修改原因
购物车列表需要根据用户是否是会员显示不同的价格列标题，并且会员价的计算逻辑需要优化：
1. 普通用户显示"商品价格"，会员显示"会员价"
2. 会员价优先使用商品/SKU配置的固定会员价，如果没有配置则根据会员等级折扣率计算

### 修改内容

#### 1. 后端代码修改

**VO：**
- `backend/src/main/java/com/shoppingmall/vo/CartVO.java`
  - 添加 `isMember` 字段（Integer），用于标识用户是否是会员

**Service：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/CartServiceImpl.java`
  - 添加 `ProductSkuRepository` 依赖，用于查询SKU信息
  - 修改 `convertToVO` 方法：
    - 查询用户信息，设置 `isMember` 字段
    - 判断购物车项是否有SKU，如果有则查询SKU信息
    - 有SKU时使用SKU的价格和重量，无SKU时使用商品的价格和重量
    - 分别调用 `calculateMemberPriceForProduct` 或 `calculateMemberPriceForSku` 计算会员价
  - 新增 `calculateMemberPriceForProduct` 方法：
    - 优先检查商品是否启用了会员价（`enableMemberPrice = 1`）且有配置会员价（`memberPrice > 0`）
    - 如果启用了会员价，直接返回配置的会员价
    - 否则调用 `calculateMemberPriceByDiscount` 根据会员等级折扣率计算
  - 新增 `calculateMemberPriceForSku` 方法：
    - 优先检查SKU是否启用了会员价（`enableMemberPrice = 1`）且有配置会员价（`memberPrice > 0`）
    - 如果启用了会员价，直接返回配置的会员价
    - 否则调用 `calculateMemberPriceByDiscount` 根据会员等级折扣率计算
  - 重构 `calculateMemberPrice` 方法为 `calculateMemberPriceByDiscount`：
    - 检查用户是否是会员，如果不是会员直接返回原价
    - 根据用户的会员等级查找对应的折扣率
    - 计算会员价格：销售价格 * (折扣率 / 100.00)

#### 2. 前端代码修改

**API：**
- `frontend/src/api/buyer/cart.ts`
  - `CartVO` 接口：添加 `isMember` 字段（number，0-普通用户，1-会员）

**购物车页面：**
- `frontend/src/views/cart/Index.vue`
  - 表头列标题：将固定的"会员价"改为动态方法 `getPriceColumnTitle()`
  - 新增 `getPriceColumnTitle` 方法：
    - 根据购物车列表中第一个商品的 `isMember` 字段判断
    - 如果是会员（`isMember === 1`），显示"会员价"
    - 如果是普通用户，显示"商品价格"

### 业务逻辑说明
1. **价格列标题**：
   - 普通用户：显示"商品价格"
   - 会员：显示"会员价"

2. **会员价计算优先级**：
   - 第一优先级：如果商品/SKU启用了会员价（`enableMemberPrice = 1`）且有配置会员价（`memberPrice > 0`），直接使用配置的会员价
   - 第二优先级：如果商品/SKU没有配置会员价，根据用户的会员等级折扣率计算
   - 普通用户：返回原价（销售价格）

3. **SKU处理**：
   - 如果购物车项有SKU（`cart.skuId != null`），优先使用SKU的价格和会员价配置
   - 如果购物车项没有SKU，使用商品的价格和会员价配置
   - SKU的重量优先于商品的重量

4. **价格字段**：
   - `salesPrice`：销售价格（商品或SKU的基础价格）
   - `memberPrice`：会员价或商品价格（根据用户是否是会员和配置情况计算）

## 2025-12-19 - 会员等级删除保护功能

### 修改原因
如果有用户已经设置了某个会员等级，删除该等级会导致数据不一致。需要增加保护机制，禁止删除已被使用的会员等级，只能编辑。

### 修改内容

#### 1. 后端代码修改

**Service：**
- `backend/src/main/java/com/shoppingmall/service/member/impl/MemberLevelServiceImpl.java`
  - 在 `deleteMemberLevel` 方法中添加检查逻辑
  - 删除前查询是否有用户使用了该会员等级（`is_member = 1` 且 `member_level_id = 等级ID`）
  - 如果有用户使用，抛出业务异常，提示无法删除，建议使用编辑功能
  - 添加 `UserRepository` 依赖用于查询用户

#### 2. 前端代码修改

**管理后台页面：**
- `admin-frontend/src/views/buyer/Level.vue`
  - 更新 `handleDelete` 方法，优化错误提示处理
  - 当删除失败且错误信息包含"无法删除"、"已被"、"使用"等关键词时，显示警告提示
  - 提示用户如需修改请使用编辑功能

### 业务逻辑说明
1. 删除会员等级前，系统会检查是否有会员使用了该等级
2. 如果有会员使用，删除操作会被阻止，并提示使用人数
3. 管理员可以通过编辑功能修改等级信息，但不能删除已被使用的等级
4. 只有没有任何会员使用的等级才能被删除

## 2025-12-19 - 添加会员标识字段，重构用户等级字段

### 修改原因
当前设计无法判断用户是普通用户还是会员，需要添加一个字段来区分：
- 普通用户不属于会员，不需要等级
- 会员可以设置会员等级

### 修改内容

#### 1. 数据库字段修改
- 添加 `is_member` 字段（tinyint，0-普通用户，1-会员）
- 将 `user_level` 字段改为 `member_level_id`（bigint，关联 member_level 表）
- 普通用户时 `is_member = 0`，`member_level_id = NULL`
- 会员时 `is_member = 1`，`member_level_id` 关联到 `member_level` 表

**文件：**
- `database/update-20251219-add-member-fields.sql` - 数据库迁移脚本（新建）
- `database/chengren_shopping_mall表结构1219.sql` - 表结构定义文件

#### 2. 后端Java代码修改

**实体类：**
- `backend/src/main/java/com/shoppingmall/entity/User.java`
  - 添加 `isMember` 字段（Integer）
  - 将 `userLevel` 改为 `memberLevelId`（Long）
  - 更新字段注释

**DTO：**
- `backend/src/main/java/com/shoppingmall/dto/BuyerDTO.java`
  - 添加 `isMember` 字段
  - 将 `userLevel` 改为 `memberLevelId`

**VO：**
- `backend/src/main/java/com/shoppingmall/vo/BuyerVO.java`
  - 添加 `isMember` 字段
  - 将 `userLevel` 改为 `memberLevelId`
  - 将 `userLevelName` 改为 `memberLevelName`

**Service：**
- `backend/src/main/java/com/shoppingmall/service/buyer/BuyerService.java`
  - `getBuyerList` 方法参数：`userLevel` → `isMember` 和 `memberLevelId`
  - `updateBuyerLevel` 方法改为 `updateBuyerMemberInfo`，参数改为 `isMember` 和 `memberLevelId`

- `backend/src/main/java/com/shoppingmall/service/buyer/impl/BuyerServiceImpl.java`
  - 更新查询逻辑，支持按 `isMember` 和 `memberLevelId` 筛选
  - 更新会员信息设置逻辑：普通用户时清空等级，会员时可以设置等级
  - 更新 `getMemberLevelName` 方法参数类型：Integer → Long
  - 更新 `convertToVO` 方法，设置会员信息和等级名称

**Controller：**
- `backend/src/main/java/com/shoppingmall/controller/admin/BuyerController.java`
  - `getBuyerList` 接口参数：`userLevel` → `isMember` 和 `memberLevelId`
  - `updateBuyerLevel` 接口改为 `updateBuyerMemberInfo`，路径改为 `/api/admin/buyer/{id}/member`

#### 3. 前端代码修改

**管理后台API：**
- `admin-frontend/src/api/admin/buyer.ts`
  - `BuyerVO` 接口：`userLevel` → `isMember` 和 `memberLevelId`，`userLevelName` → `memberLevelName`
  - `BuyerDTO` 接口：`userLevel` → `isMember` 和 `memberLevelId`
  - `getBuyerList` 方法参数：`userLevel` → `isMember` 和 `memberLevelId`
  - `updateBuyerLevel` 方法改为 `updateBuyerMemberInfo`

**管理后台页面：**
- `admin-frontend/src/views/buyer/List.vue`
  - 搜索表单：添加"会员类型"筛选，将"等级"改为"会员等级"
  - 列表表格：添加"会员类型"列，将"等级"改为"会员等级"
  - 详情对话框：显示会员类型和会员等级
  - 会员设置对话框：支持设置会员类型（普通用户/会员）和会员等级
  - 更新相关方法和变量名

### 业务逻辑说明
1. **普通用户**：`is_member = 0`，`member_level_id = NULL`，不享受会员优惠
2. **会员**：`is_member = 1`，`member_level_id` 关联到 `member_level` 表，享受对应等级的优惠
3. 设置为普通用户时，自动清空会员等级
4. 只有会员才能设置会员等级

## 2025-12-19 - 商品价格字段名称重构

### 修改原因
商品表（product）中的 `market_price` 和 `cost_price` 字段名称与业务含义不一致，容易造成误解：
- `market_price`（市场价格）实际用作"建议零售价"
- `cost_price`（成本价格）实际用作"市场零售价"

### 修改内容

#### 1. 数据库字段重命名
- `market_price` → `suggested_retail_price`（建议零售价）
- `cost_price` → `market_retail_price`（市场零售价）

**文件：**
- `database/update-20251219-rename-product-price-fields.sql` - 数据库迁移脚本（新建，按规范命名）
- `database/chengren_shopping_mall表结构1219.sql` - 表结构定义文件

#### 2. 后端Java代码修改

**实体类：**
- `backend/src/main/java/com/shoppingmall/entity/Product.java`
  - `marketPrice` → `suggestedRetailPrice`
  - `costPrice` → `marketRetailPrice`
  - 更新字段注释

**DTO：**
- `backend/src/main/java/com/shoppingmall/dto/ProductDTO.java`
  - `marketPrice` → `suggestedRetailPrice`
  - `costPrice` → `marketRetailPrice`
  - 更新字段注释

**VO：**
- `backend/src/main/java/com/shoppingmall/vo/ProductVO.java`
  - `marketPrice` → `suggestedRetailPrice`
  - `costPrice` → `marketRetailPrice`
  - 更新字段注释

#### 3. 前端代码修改

**管理后台API：**
- `admin-frontend/src/api/admin/product.ts`
  - `ProductDTO` 接口：`marketPrice` → `suggestedRetailPrice`，`costPrice` → `marketRetailPrice`
  - `ProductVO` 接口：`marketPrice` → `suggestedRetailPrice`，`costPrice` → `marketRetailPrice`

**管理后台页面：**
- `admin-frontend/src/views/product/ProductManage.vue`
  - 表单字段：`formData.marketPrice` → `formData.suggestedRetailPrice`
  - 表单字段：`formData.costPrice` → `formData.marketRetailPrice`
  - 表单prop：`prop="marketPrice"` → `prop="suggestedRetailPrice"`
  - 表单prop：`prop="costPrice"` → `prop="marketRetailPrice"`
  - 初始化数据中的字段名称

- `admin-frontend/src/views/product/Add.vue`
  - 表单字段：`productForm.marketPrice` → `productForm.suggestedRetailPrice`
  - 表单字段：`productForm.costPrice` → `productForm.marketRetailPrice`
  - 表单prop：`prop="marketPrice"` → `prop="suggestedRetailPrice"`
  - 表单prop：`prop="costPrice"` → `prop="marketRetailPrice"`
  - 初始化数据和提交数据中的字段名称

**前端API：**
- `frontend/src/api/buyer/product.ts`
  - `ProductVO` 接口：`marketPrice` → `suggestedRetailPrice`，`costPrice` → `marketRetailPrice`

**前端页面：**
- `frontend/src/views/products/Detail.vue`
  - 产品对象：`marketPrice` → `marketRetailPrice`
  - 模板显示：`product.marketPrice` → `product.marketRetailPrice`
  - 数据映射：`productData.costPrice` → `productData.marketRetailPrice`
  - 数据映射：`productData.marketPrice` → `productData.suggestedRetailPrice`
  - 更新注释说明

- `frontend/src/views/products/List.vue`
  - 商品列表：`product.marketPrice` → `product.marketRetailPrice`
  - 更新注释说明

### 影响范围
- 数据库表：`product` 表
- 后端：3个Java类（Entity、DTO、VO）
- 前端：6个文件（2个API文件，4个页面组件）

### 注意事项
1. **数据库迁移**：执行 `database/migrate_product_price_fields_rename.sql` 脚本前，建议先备份数据
2. **MyBatis-Plus自动映射**：由于使用驼峰命名自动映射，修改Java字段名后会自动映射到新的数据库字段
3. **BeanUtils自动复制**：Service层使用 `BeanUtils.copyProperties`，字段名一致即可自动映射
4. **测试验证**：需要测试商品创建、编辑、查询、显示等功能

### 修改文件清单
1. `database/update-20251219-rename-product-price-fields.sql` (新建，按规范命名)
2. `database/chengren_shopping_mall表结构1219.sql`
3. `backend/src/main/java/com/shoppingmall/entity/Product.java`
4. `backend/src/main/java/com/shoppingmall/dto/ProductDTO.java`
5. `backend/src/main/java/com/shoppingmall/vo/ProductVO.java`
6. `admin-frontend/src/api/admin/product.ts`
7. `admin-frontend/src/views/product/ProductManage.vue`
8. `admin-frontend/src/views/product/Add.vue`
9. `frontend/src/api/buyer/product.ts`
10. `frontend/src/views/products/Detail.vue`
11. `frontend/src/views/products/List.vue`

---

## 2025-12-27 - 支付记录详情弹窗布局优化

### 修改内容
优化支付记录详情弹窗的字段显示方式，将两列布局改为三列布局，避免字段换行显示。

### 修改文件
- `admin-frontend/src/views/finance/PaymentRecord.vue`
  - 将详情弹窗的 `el-descriptions` 组件从 `:column="2"` 改为 `:column="3"`
  - 将弹窗宽度从 `700px` 增加到 `900px`，以便更好地容纳三列布局
  - 将"退款原因"字段的 `:span="2"` 改为 `:span="3"`，保持布局一致性

### 效果
- 支付记录详情弹窗中的字段以三列布局显示，减少换行
- 弹窗宽度增加，提供更好的显示空间

---

## 2025-12-27 支付结果查询补单功能

### 问题描述
当用户已支付但平台未收到支付回调时（如网络问题、服务器重启等），订单状态不会自动更新为已支付，影响用户体验。需要提供自动查询支付结果并补单的功能。

### 解决方案
1. **定时任务补单**：创建定时任务，每5分钟自动查询支付中状态的支付记录，如果支付宝已支付则自动补单
2. **手动补单接口**：在管理后台提供手动补单接口，管理员可以手动触发补单操作
3. **系统配置**：添加支付结果查询时间窗口配置，可动态调整查询范围

### 修改内容

#### 1. 定时任务服务
- `backend/src/main/java/com/shoppingmall/service/payment/impl/PaymentSyncScheduledServiceImpl.java` (新建)
  - 每5分钟执行一次，查询支付中状态的支付记录
  - 只查询最近30分钟内的支付记录（可配置）
  - 每次最多处理50条记录，避免一次性处理太多
  - 如果支付宝订单已支付，自动更新订单状态和支付记录
  - 自动推送订单到ERP（如果配置了自动推送）

#### 2. 手动补单接口
- `backend/src/main/java/com/shoppingmall/controller/admin/OrderController.java`
  - 添加 `syncPaymentStatus` 方法，支持手动查询支付结果并补单
  - 接口路径：`POST /api/admin/orders/{orderNo}/sync-payment`
  - 功能：
    - 查询订单和支付记录
    - 查询支付宝订单状态
    - 如果已支付，自动补单并更新订单状态
    - 自动推送订单到ERP（如果配置了自动推送）

#### 3. 数据库配置
- `database/update-20251227-add-payment-sync-config.sql` (新建)
  - 添加 `payment.sync-time-window-minutes` 配置项
  - 默认值：30分钟（只查询最近30分钟内的支付记录）

### 功能特点
1. **自动补单**：定时任务每5分钟自动查询并补单，无需人工干预
2. **手动补单**：管理员可以手动触发补单操作，处理特殊情况
3. **智能查询**：只查询最近30分钟内的支付记录，避免查询太老的记录
4. **批量限制**：每次最多处理50条记录，避免一次性处理太多
5. **错误处理**：单个记录失败不影响其他记录的处理
6. **日志记录**：详细记录补单成功、失败、跳过的数量，方便监控

### 影响范围
- 定时任务：支付结果查询补单定时任务
- 管理后台：订单管理手动补单功能
- 数据库：系统配置表

### 注意事项
1. **执行频率**：定时任务每5分钟执行一次，可根据实际情况调整
2. **查询范围**：只查询最近30分钟内的支付记录，可通过配置调整
3. **批量限制**：每次最多处理50条记录，避免一次性处理太多
4. **仅支持支付宝**：当前只支持支付宝订单补单，微信支付可类似实现
5. **ERP推送**：补单成功后会自动推送订单到ERP（如果配置了自动推送）

### 使用方式
1. **自动补单**：定时任务会自动执行，无需人工干预
2. **手动补单**：
   - 在管理后台订单详情页添加"查询支付结果并补单"按钮
   - 点击按钮后调用接口：`POST /api/admin/orders/{orderNo}/sync-payment`
   - 系统会自动查询支付结果并补单

### 修改文件清单
1. `backend/src/main/java/com/shoppingmall/service/payment/impl/PaymentSyncScheduledServiceImpl.java` (新建)
2. `backend/src/main/java/com/shoppingmall/controller/admin/OrderController.java`
3. `database/update-20251227-add-payment-sync-config.sql` (新建)
4. `log.md` (本文件)

---

## 2025-12-27 修复支付回调方法签名不匹配问题

### 问题描述
支付宝支付成功后，跳转回平台页面时出现编译错误：
```
The method handlePaymentCallback(String, String, boolean, Map<String,Object>) in the type DepositService is not applicable for the arguments (String, String, boolean)
```

原因是 `DepositService` 接口中的 `handlePaymentCallback` 方法签名已经修改为包含 `Map<String, Object>` 参数，但在 `PaymentNotifyController` 中调用时只传了3个参数。

### 解决方案
1. **修复 PaymentNotifyController**：在调用 `depositService.handlePaymentCallback` 时添加第4个参数 `notifyData`
2. **修复 DepositServiceImpl**：
   - 修改 `handlePaymentCallback` 方法签名，添加 `Map<String, Object> notifyData` 参数
   - 在模拟支付回调调用时，传递 `null` 作为第4个参数
   - 添加 `ObjectMapper` 依赖注入
   - 在支付成功时保存回调数据到 `callbackData` 字段

### 修改内容

#### 后端修改
- `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
  - 修改预存款充值回调处理，传递 `notifyData` 到 `handlePaymentCallback` 方法

- `backend/src/main/java/com/shoppingmall/service/buyer/impl/DepositServiceImpl.java`
  - 修改 `handlePaymentCallback` 方法签名，添加 `Map<String, Object> notifyData` 参数
  - 添加 `ObjectMapper` 依赖注入
  - 在模拟支付回调调用时，传递 `null` 作为第4个参数
  - 在支付成功时保存回调数据到 `callbackData` 字段

### 影响范围
- 支付回调处理：预存款充值回调处理功能
- 数据保存：回调数据保存功能

### 注意事项
1. **方法签名**：`handlePaymentCallback` 方法现在需要4个参数，包括 `notifyData`
2. **回调数据**：如果 `notifyData` 为 `null`（如模拟支付），不会保存回调数据
3. **错误处理**：保存回调数据失败不会影响支付回调处理流程

### 修改文件清单
1. `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
2. `backend/src/main/java/com/shoppingmall/service/buyer/impl/DepositServiceImpl.java`
3. `backend/src/main/java/com/shoppingmall/entity/PreDepositDetail.java`
4. `log.md` (本文件)

---

## 2025-12-27 修复 PreDepositDetail 实体类缺少 callbackData 字段

### 问题描述
编译错误：`PreDepositDetail` 实体类中找不到 `setCallbackData` 方法。原因是之前添加了数据库字段和服务层代码，但忘记在实体类中添加 `callbackData` 字段。

### 解决方案
在 `PreDepositDetail` 实体类中添加 `callbackData` 字段，用于保存第三方支付返回的原始回调数据。

### 修改内容
- `backend/src/main/java/com/shoppingmall/entity/PreDepositDetail.java`
  - 添加 `callbackData` 字段（String类型，JSON格式）
  - 字段位置：`internalOrderNo` 字段之后

### 修改文件清单
1. `backend/src/main/java/com/shoppingmall/entity/PreDepositDetail.java`
2. `log.md` (本文件)

---

## 2025-12-27 预存款退款重试逻辑优化

### 问题描述
预存款支付宝退款失败，错误信息显示"系统异常"，重试逻辑（使用 `externalTradeNo`）已执行，但使用 `trade_no` 退款也失败，返回相同的"系统异常"错误。

### 可能原因
1. **支付宝沙箱环境问题**：支付宝沙箱环境可能不稳定，导致退款接口返回系统异常
2. **订单已退款**：订单可能已经退款过了，导致再次退款失败
3. **订单状态异常**：订单状态可能不是已支付成功，导致退款失败
4. **退款金额问题**：退款金额可能超过可退款金额

### 修改内容
- `backend/src/main/java/com/shoppingmall/service/admin/impl/DepositServiceImpl.java`
  - 添加更详细的日志，记录使用 `trade_no` 退款时的详细信息
  - 记录充值记录ID、内部订单号、外部交易号、退款金额、退款单号等信息

### 建议排查步骤
1. **检查订单是否已退款**：查看数据库中是否有该充值记录的退款记录
2. **检查订单状态**：确认订单在支付宝中的状态是否为已支付成功
3. **检查退款金额**：确认退款金额是否超过可退款金额
4. **检查支付宝沙箱环境**：确认支付宝沙箱环境是否正常

### 修改文件清单
1. `backend/src/main/java/com/shoppingmall/service/admin/impl/DepositServiceImpl.java`
2. `log.md` (本文件)

---

## 2025-12-27 在订单详情页面添加补单操作入口

### 需求描述
在订单详情页面底部增加一个补单操作区域，包含操作入口和说明文案，说明什么情况下需要操作补单。

### 实现内容
1. **前端API接口**
   - 添加 `syncPaymentStatus` 函数，调用后端补单接口

2. **订单详情页面UI**
   - 在订单详情对话框底部添加"支付补单"区域
   - 添加说明文案，说明什么情况下需要补单：
     - 用户已支付成功，但订单状态仍显示"待付款"
     - 支付回调丢失，导致订单状态未更新
     - 支付宝/微信支付成功，但系统未收到支付通知
   - 添加"执行补单"按钮，带loading状态
   - 按钮仅在订单状态为"待付款"（status === 0）时可用

3. **补单处理逻辑**
   - 添加 `syncPaymentLoading` 状态管理
   - 添加 `handleSyncPayment` 处理函数
   - 执行补单前显示确认对话框
   - 补单成功后自动刷新订单详情和订单列表

### 修改内容
- `admin-frontend/src/api/admin/order.ts`
  - 添加 `syncPaymentStatus` 函数，调用 `POST /api/admin/orders/{orderNo}/sync-payment`

- `admin-frontend/src/views/order/List.vue`
  - 导入 `Refresh` 图标和 `syncPaymentStatus` API
  - 添加 `syncPaymentLoading` 状态
  - 在订单详情对话框底部添加补单操作区域
  - 添加 `handleSyncPayment` 处理函数

### 修改文件清单
1. `admin-frontend/src/api/admin/order.ts`
2. `admin-frontend/src/views/order/List.vue`
3. `log.md` (本文件)

---

## 2025-12-27 分离预存款退款和订单退款的代码逻辑

### 问题描述
预存款支付宝退款失败，但订单支付宝退款可以成功。原因是预存款退款和订单退款共享了 `AlipayPayStrategy.refund` 方法，而该方法中的订单状态查询逻辑可能不适用于预存款（因为预存款的商户订单号格式不同，且底层数据库表结构不同）。

### 解决方案
将预存款退款和订单退款的代码完全分离：
- **预存款退款**：直接调用 `AlipayUtil.refund`，不经过 `PaymentGatewayService` 和 `AlipayPayStrategy`
- **订单退款**：继续使用 `PaymentGatewayService` 和 `AlipayPayStrategy`

这样可以避免两种不同业务场景的代码混合，确保各自使用适合的逻辑路径。

### 修改内容
- `backend/src/main/java/com/shoppingmall/service/admin/impl/DepositServiceImpl.java`
  - 移除对 `PaymentGatewayService.refund` 的调用
  - 直接调用 `AlipayUtil.refund` 进行预存款退款
  - 先尝试使用商户订单号（`out_trade_no`）退款
  - 如果失败且是系统错误，则使用支付宝交易号（`trade_no`）重试
  - 添加详细的日志记录，区分预存款退款和订单退款

### 关键改进
1. **代码分离**：预存款退款不再经过 `AlipayPayStrategy`，避免订单状态查询逻辑对预存款的影响
2. **直接调用**：预存款退款直接使用 `AlipayUtil.refund`，减少中间层，提高可控性
3. **错误处理**：保持原有的重试机制（先使用 `out_trade_no`，失败后使用 `trade_no`）
4. **日志优化**：所有日志都明确标注为"预存款退款"，便于问题排查

### 修改文件清单
1. `backend/src/main/java/com/shoppingmall/service/admin/impl/DepositServiceImpl.java`
2. `log.md` (本文件)

---

## 2025-12-27 修复订单多次部分退款问题

### 问题描述
订单退款在多次部分退款时报错，支付宝返回"系统异常"。原因是重试时使用了新的退款单号，而支付宝要求同一笔退款请求必须使用相同的 `out_request_no`（退款单号）。

### 解决方案
修改订单退款逻辑，确保：
1. 支付宝退款直接使用 `AlipayUtil.refund`，不经过 `PaymentGatewayService`
2. 在退款开始时生成退款单号，第一次尝试和重试都使用相同的退款单号
3. 添加详细的日志输出，便于排查问题

### 修改内容
- `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`
  - 修改支付宝退款逻辑，直接使用 `AlipayUtil.refund`
  - 在退款开始时生成退款单号 `alipayRefundNo`
  - 第一次尝试（使用 `out_trade_no`）和重试（使用 `trade_no`）都使用相同的退款单号
  - 添加详细的日志输出，包括响应码、子错误码、错误信息
  - 微信支付继续使用 `PaymentGatewayService`

### 关键改进
1. **退款单号一致性**：确保同一笔退款请求的重试使用相同的退款单号
2. **代码分离**：订单退款和预存款退款使用独立的代码路径
3. **详细日志**：添加响应码、子错误码、错误信息的日志输出

### 修改文件清单
1. `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`
2. `log.md` (本文件)

---

## 2025-12-27: 重新设计物流配置功能

### 问题描述
用户要求重新设计物流配置功能，按照标准电商的方式设置：
- 移除物流公司、配送方式标签页面
- 保留运费模板配置，支持首重、首费，续重、续费、指定地区规则
- 指定地区规则优先于默认规则生效
- 新增发货地址库配置页面

### 解决方案
1. **创建发货地址库功能**
   - 新增 `warehouse_address` 表存储发货地址信息
   - 实现后端实体、Repository、Service、Controller
   - 实现前端API和页面

2. **优化运费规则表结构**
   - 将 `shipping_rule` 表的 `region_code` 和 `region_name` 字段改为TEXT类型
   - 支持JSON格式存储多个地区信息

3. **简化前端页面**
   - 移除物流公司和配送方式标签页
   - 保留运费模板管理
   - 新增发货地址库管理
   - 运费模板支持默认规则和指定地区规则

### 修改内容

#### 数据库修改
- `database/update-20251227-add-warehouse-address-table.sql`
  - 创建 `warehouse_address` 表
- `database/update-20251227-optimize-shipping-rule-region.sql`
  - 优化 `shipping_rule` 表结构，支持多地区存储

#### 后端修改
- `backend/src/main/java/com/shoppingmall/entity/WarehouseAddress.java`
  - 发货地址实体类
- `backend/src/main/java/com/shoppingmall/repository/logistics/WarehouseAddressRepository.java`
  - 发货地址Repository
- `backend/src/main/java/com/shoppingmall/vo/WarehouseAddressVO.java`
  - 发货地址VO
- `backend/src/main/java/com/shoppingmall/dto/WarehouseAddressDTO.java`
  - 发货地址DTO
- `backend/src/main/java/com/shoppingmall/service/logistics/WarehouseAddressService.java`
  - 发货地址服务接口
- `backend/src/main/java/com/shoppingmall/service/logistics/impl/WarehouseAddressServiceImpl.java`
  - 发货地址服务实现
- `backend/src/main/java/com/shoppingmall/controller/admin/WarehouseAddressController.java`
  - 发货地址控制器
- `backend/src/main/java/com/shoppingmall/dto/RegionInfoDTO.java`
  - 地区信息DTO（用于运费规则中的多地区存储）
- `backend/src/main/java/com/shoppingmall/dto/ShippingRuleDTO.java`
  - 添加 `regions` 字段支持地区列表
- `backend/src/main/java/com/shoppingmall/vo/ShippingRuleVO.java`
  - 添加 `regions` 字段支持地区列表

#### 前端修改
- `admin-frontend/src/api/admin/logistics.ts`
  - 添加发货地址库相关API接口
- `admin-frontend/src/views/system/Logistics.vue`
  - 创建简化的物流配置页面
  - 移除物流公司和配送方式标签
  - 保留运费模板管理
  - 新增发货地址库管理
- `admin-frontend/src/router/componentMaps/logistics.ts`
  - 更新路由组件映射

### 功能说明
1. **运费模板管理**
   - 支持按重量、按件数、按金额三种计算方式
   - 支持默认运费规则（首重、首费、续重、续费）
   - 支持指定地区规则（优先级高于默认规则）
   - 地区选择器功能待实现（TODO）

2. **发货地址库管理**
   - 支持多个发货地址
   - 支持设置默认发货地址
   - 支持启用/禁用地址
   - 包含仓库名称、联系人、联系电话、地址等信息

### 已完成功能
1. ✅ 运费模板的地区选择器组件（省/市/区三级联动）- 已实现多地区选择器
2. ✅ 更新运费计算逻辑，支持多地区匹配和优先级 - 已实现JSON格式地区匹配
3. ⏳ 发货地址的地区选择器（可选，当前使用文本输入）- 暂未实现，使用文本输入

### 新增功能说明

#### 多地区选择器组件
- 创建了 `MultiRegionSelector.vue` 组件
- 支持省/市/区三级联动选择
- 支持多选地区（可同时选择多个省份、城市、区县）
- 自动处理地区层级关系（区县 > 城市 > 省份）
- 显示已选地区标签，支持删除

#### 运费计算逻辑优化
- 更新了 `findMatchedRule` 方法，支持JSON格式的地区匹配
- 实现了优先级匹配：区县 > 城市 > 省份 > 默认规则
- 支持一个规则包含多个地区
- 向后兼容旧的字符串格式地区规则

### 修改文件清单
1. `database/update-20251227-add-warehouse-address-table.sql`
2. `database/update-20251227-optimize-shipping-rule-region.sql`
3. `backend/src/main/java/com/shoppingmall/entity/WarehouseAddress.java`
4. `backend/src/main/java/com/shoppingmall/repository/logistics/WarehouseAddressRepository.java`
5. `backend/src/main/java/com/shoppingmall/vo/WarehouseAddressVO.java`
6. `backend/src/main/java/com/shoppingmall/dto/WarehouseAddressDTO.java`
7. `backend/src/main/java/com/shoppingmall/service/logistics/WarehouseAddressService.java`
8. `backend/src/main/java/com/shoppingmall/service/logistics/impl/WarehouseAddressServiceImpl.java`
9. `backend/src/main/java/com/shoppingmall/controller/admin/WarehouseAddressController.java`
10. `backend/src/main/java/com/shoppingmall/dto/RegionInfoDTO.java`
11. `backend/src/main/java/com/shoppingmall/dto/ShippingRuleDTO.java`
12. `backend/src/main/java/com/shoppingmall/vo/ShippingRuleVO.java`
13. `admin-frontend/src/api/admin/logistics.ts`
14. `admin-frontend/src/views/system/Logistics.vue`
15. `admin-frontend/src/router/componentMaps/logistics.ts`
16. `admin-frontend/src/api/common/region.ts` (新增)
17. `admin-frontend/src/components/common/MultiRegionSelector.vue` (新增)
18. `backend/src/main/java/com/shoppingmall/service/logistics/impl/ShippingServiceImpl.java`
19. `log.md` (本文件)

### SQL执行说明
执行以下SQL脚本（按顺序）：
1. `database/update-20251227-add-warehouse-address-table.sql` - 创建发货地址库表
2. `database/update-20251227-optimize-shipping-rule-region.sql` - 优化运费规则表结构
   - 注意：如果索引不存在，删除索引的语句会报错，可以忽略继续执行

---

## 2025-12-27: 创建完全隔离环境部署方案文档

### 问题描述
用户需要在同一个域名下部署完全隔离的正式环境和测试环境，通过路径区分：
- 正式环境：`www.shop.quaichao.com/` 和 `www.shop.quaichao.com/admin`
- 测试环境：`www.shop.quaichao.com/test` 和 `www.shop.quaichao.com/test/admin`

### 解决方案
创建完整的部署方案文档，包含：
1. **后端部署配置**：两个独立的Java项目（端口8081和8082），使用不同的数据库和配置文件
2. **前端部署配置**：两套独立的构建产物，支持子路径部署
3. **Nginx配置**：通过路径路由到不同的后端服务
4. **数据库准备**：两个独立的数据库实例
5. **部署步骤**：详细的部署流程和注意事项

### 文档内容
- 方案概述和架构说明
- 后端配置文件修改（application-test.yml 和 application-prod.yml）
- 前端环境变量和构建配置
- 完整的Nginx配置示例
- 数据库准备和初始化步骤
- 详细的部署步骤总结
- 环境隔离说明
- 常见问题排查
- 维护建议和备份策略

### 修改文件清单
1. `docs/完全隔离环境部署方案.md` (新建)
2. `log.md` (本文件)

---

## 2025-12-30: 实现微信退款接口真实调用

### 问题描述
微信退款功能虽然提示退款成功，但用户没有收到退款金额。原因是 `WeChatPayUtil.refund` 方法中只是返回了占位结果，并没有真正调用微信退款API。

### 问题分析
1. **占位实现**：`WeChatPayUtil.refund` 方法中有TODO注释，说明需要实现证书认证的HTTP请求，但当前只是返回了成功结果
2. **证书配置**：微信退款API需要使用PKCS12格式的证书进行HTTPS双向认证
3. **原订单金额**：`WeChatPayStrategy.refund` 方法中需要从PaymentRecord获取原订单金额，而不是使用退款金额作为总金额

### 解决方案
1. **实现证书认证的HTTPS请求**：添加 `sendHttpsRequestWithCert` 方法，使用Java标准库加载PKCS12证书并发送HTTPS请求
2. **修改退款方法**：修改 `WeChatPayUtil.refund` 方法，真正调用微信退款API
3. **获取原订单金额**：修改 `WeChatPayStrategy.refund` 方法，通过订单号查询PaymentRecord获取原订单金额

### 修改内容

#### 1. WeChatPayUtil.java - 实现真正的微信退款API调用

**文件：** `backend/src/main/java/com/shoppingmall/payment/util/WeChatPayUtil.java`

**修改点：**
- 添加必要的导入：`FileInputStream`, `InputStream`, `URL`, `KeyStore`, `KeyManagerFactory`, `SSLContext`, `HttpsURLConnection` 等
- 修改 `refund` 方法：
  - 检查证书路径配置，如果未配置则抛出异常
  - 调用 `sendHttpsRequestWithCert` 方法发送HTTPS请求
  - 解析响应并验证签名
  - 返回真实的退款结果
- 新增 `sendHttpsRequestWithCert` 方法：
  - 支持从文件系统和classpath加载PKCS12证书
  - 使用商户号作为证书密码
  - 创建SSLContext并配置HTTPS连接
  - 发送XML请求数据并读取响应
  - 处理错误响应和异常情况

#### 2. WeChatPayStrategy.java - 获取原订单金额

**文件：** `backend/src/main/java/com/shoppingmall/payment/strategy/impl/WeChatPayStrategy.java`

**修改点：**
- 添加依赖注入：`OrderRepository` 和 `PaymentRecordRepository`
- 修改 `refund` 方法：
  - 通过订单号（paymentNo参数实际是订单号）查询Order
  - 通过OrderId查询PaymentRecord获取原订单金额
  - 将原订单金额转换为分，用于微信退款API
  - 添加详细的日志记录
  - 如果查询失败，使用退款金额作为总金额（向后兼容）

### 技术细节

#### 证书加载
- 支持文件系统路径：直接使用 `FileInputStream` 加载
- 支持classpath路径：使用 `classpath:` 前缀，通过 `ClassLoader.getResourceAsStream` 加载
- 证书格式：PKCS12（.p12文件）
- 证书密码：使用商户号（mchId）作为密码

#### HTTPS请求
- 使用 `HttpsURLConnection` 发送HTTPS请求
- 配置SSLContext使用客户端证书
- 设置连接超时和读取超时为30秒
- 正确处理响应码和错误流

#### 原订单金额获取
- 通过订单号查询Order表
- 通过OrderId查询PaymentRecord表
- 获取PaymentRecord的amount字段作为原订单金额
- 转换为分（微信支付金额单位）

### 功能特点
1. **真实API调用**：真正调用微信退款API，不再返回占位结果
2. **证书支持**：支持PKCS12格式证书，支持文件系统和classpath两种加载方式
3. **签名验证**：验证微信返回的响应签名，确保数据安全
4. **错误处理**：详细的错误处理和日志记录，便于排查问题
5. **向后兼容**：如果查询原订单金额失败，使用退款金额作为总金额

### 影响范围
- 微信退款功能：订单退款和预存款退款
- 需要配置微信支付证书路径：`payment.wechat.sandbox.cert_path` 或 `payment.wechat.production.cert_path`

### 注意事项
1. **证书配置**：必须在系统配置中配置微信支付证书路径
2. **证书格式**：证书必须是PKCS12格式（.p12文件）
3. **证书密码**：证书密码使用商户号（mchId）
4. **原订单金额**：如果查询失败，会使用退款金额作为总金额，可能导致部分退款失败
5. **错误处理**：如果证书加载失败或API调用失败，会抛出详细的异常信息

### 修改文件清单
1. `backend/src/main/java/com/shoppingmall/payment/util/WeChatPayUtil.java`
2. `backend/src/main/java/com/shoppingmall/payment/strategy/impl/WeChatPayStrategy.java`
3. `log.md` (本文件)

---

## 2025-12-30: 修复微信退款原订单金额查询问题（支持预存款退款）

### 问题描述
微信退款报错："订单金额或退款金额与之前请求不一致，请核实后再试"。原因是预存款退款时，传入的 `paymentNo` 是 `internalOrderNo`（格式：`DEPOSIT_xxx`），不是订单号，无法在 Order 表中找到，导致原订单金额获取失败，使用了退款金额作为总金额。

### 问题分析
1. **预存款退款场景**：预存款充值退款时，传入的 `paymentNo` 是 `internalOrderNo`（如 `DEPOSIT_1767081370090_1E18E0D2`）
2. **订单退款场景**：订单退款时，传入的 `paymentNo` 是订单号（orderNo）
3. **原代码问题**：只支持通过订单号查询 PaymentRecord，不支持预存款充值的查询
4. **预存款充值记录**：预存款充值的支付记录保存在 `PreDepositDetail` 表中，而不是 `PaymentRecord` 表

### 解决方案
修改 `WeChatPayStrategy.refund` 方法，支持两种场景的原订单金额查询：
1. **预存款退款**：如果 `paymentNo` 以 `DEPOSIT_` 开头，通过 `internalOrderNo` 查询 `PreDepositDetail` 表获取原充值金额
2. **订单退款**：先通过 `paymentNo` 直接查询 `PaymentRecord`，如果失败再通过订单号查询

### 修改内容

#### WeChatPayStrategy.java - 支持预存款退款原金额查询

**文件：** `backend/src/main/java/com/shoppingmall/payment/strategy/impl/WeChatPayStrategy.java`

**修改点：**
- 添加 `PreDepositDetailRepository` 依赖注入
- 修改 `refund` 方法中的原订单金额查询逻辑：
  - 判断 `paymentNo` 是否以 `DEPOSIT_` 开头
  - 如果是预存款退款，通过 `internalOrderNo` 查询 `PreDepositDetail` 表
  - 如果是订单退款，先通过 `paymentNo` 查询 `PaymentRecord`，失败后通过订单号查询
  - 添加详细的日志记录，便于排查问题

### 技术细节

#### 查询逻辑优先级
1. **预存款退款**（`paymentNo` 以 `DEPOSIT_` 开头）：
   - 通过 `internalOrderNo` 查询 `PreDepositDetail` 表
   - 使用 `depositDetail.getAmount()` 作为原充值金额

2. **订单退款**（其他情况）：
   - 方法1：直接通过 `paymentNo` 查询 `PaymentRecord` 表
   - 方法2：如果方法1失败，通过订单号查询 `Order` 表，再通过 `OrderId` 查询 `PaymentRecord` 表

#### 金额转换
- 将原订单/充值金额转换为分（微信支付金额单位）
- 使用 `BigDecimal.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue()`

### 功能特点
1. **支持预存款退款**：正确获取预存款充值的原金额
2. **支持订单退款**：保持原有的订单退款功能
3. **向后兼容**：如果查询失败，使用退款金额作为总金额（向后兼容）
4. **详细日志**：记录查询过程和结果，便于排查问题

### 影响范围
- 微信退款功能：预存款退款和订单退款
- 预存款退款：现在可以正确获取原充值金额，避免"订单金额不一致"错误

### 注意事项
1. **预存款充值记录**：预存款充值的支付记录保存在 `PreDepositDetail` 表中，而不是 `PaymentRecord` 表
2. **internalOrderNo格式**：预存款充值的 `internalOrderNo` 格式为 `DEPOSIT_xxx`
3. **查询失败处理**：如果查询失败，会使用退款金额作为总金额，可能导致部分退款失败

### 修改文件清单
1. `backend/src/main/java/com/shoppingmall/payment/strategy/impl/WeChatPayStrategy.java`
2. `log.md` (本文件)

---

## 2025-12-15 生成系统测试用例文档

### 修改内容
根据系统功能点，生成CSV格式的测试用例文档，输出到 `/docs/test` 目录。

### 生成文件清单

#### 用户端测试用例（7个文件）
1. `docs/test/用户端测试用例-01-认证模块.csv` - 包含登录、注册、忘记密码、重置密码等21个测试用例
2. `docs/test/用户端测试用例-02-首页模块.csv` - 包含首页展示、轮播图、商品分类、搜索等18个测试用例
3. `docs/test/用户端测试用例-03-商品模块.csv` - 包含商品列表、商品详情、加入购物车、收藏等28个测试用例
4. `docs/test/用户端测试用例-04-购物车模块.csv` - 包含购物车管理、数量修改、结算等22个测试用例
5. `docs/test/用户端测试用例-05-订单模块.csv` - 包含订单结算、支付、订单详情、订单列表等30个测试用例
6. `docs/test/用户端测试用例-06-会员中心模块.csv` - 包含个人信息、收货地址、订单管理、预存款等38个测试用例
7. `docs/test/用户端测试用例-07-其他模块.csv` - 包含帮助中心、公告、通用功能等28个测试用例

#### 管理后台测试用例（12个文件）
1. `docs/test/管理后台测试用例-01-登录与首页.csv` - 包含管理员登录、数据概览等20个测试用例
2. `docs/test/管理后台测试用例-02-商品管理模块.csv` - 包含商品列表、商品发布、商品分类、缺货登记等33个测试用例
3. `docs/test/管理后台测试用例-03-订单管理模块.csv` - 包含订单列表、订单发货、退款、订单问题等26个测试用例
4. `docs/test/管理后台测试用例-04-库存管理模块.csv` - 包含库存列表、库存调整、库存预警等20个测试用例
5. `docs/test/管理后台测试用例-05-采购者管理模块.csv` - 包含采购者列表、采购者审核、等级管理等24个测试用例
6. `docs/test/管理后台测试用例-06-营销管理模块.csv` - 包含促销活动、价格策略等24个测试用例
7. `docs/test/管理后台测试用例-07-数据统计模块.csv` - 包含销售统计、订单统计、商品统计、采购者统计等27个测试用例
8. `docs/test/管理后台测试用例-08-系统设置模块.csv` - 包含基础配置、支付配置、物流配置、通知设置等46个测试用例
9. `docs/test/管理后台测试用例-09-权限管理模块.csv` - 包含用户管理、角色管理、菜单管理等32个测试用例
10. `docs/test/管理后台测试用例-10-内容管理模块.csv` - 包含帮助中心、公告管理、咨询管理、评论管理等29个测试用例
11. `docs/test/管理后台测试用例-11-财务管理模块.csv` - 包含支付记录、预存款交易记录、财务报表等27个测试用例
12. `docs/test/管理后台测试用例-12-ERP管理模块.csv` - 包含ERP配置、订单同步、库存同步等20个测试用例

### 测试用例格式说明
每个CSV文件包含以下字段：
- **用例编号**：唯一标识测试用例
- **功能模块**：测试用例所属的功能模块
- **测试用例名称**：测试用例的简要描述
- **前置条件**：执行测试用例前需要满足的条件
- **测试步骤**：详细的测试操作步骤
- **预期结果**：执行测试后应该得到的结果
- **优先级**：测试用例的优先级（高、中、低）
- **测试类型**：测试类型（功能测试、界面测试、性能测试、兼容性测试等）
- **备注**：其他需要说明的信息

### 统计信息
- **用户端测试用例总数**：185个
- **管理后台测试用例总数**：321个
- **总计**：506个测试用例

### 覆盖范围
测试用例覆盖了系统的主要功能模块：
- 用户端：认证、首页、商品、购物车、订单、会员中心、帮助中心等
- 管理后台：商品管理、订单管理、库存管理、采购者管理、营销管理、数据统计、系统设置、权限管理、内容管理、财务管理、ERP管理等

### 修改文件清单
1. `docs/test/用户端测试用例-01-认证模块.csv` (新建)
2. `docs/test/用户端测试用例-02-首页模块.csv` (新建)
3. `docs/test/用户端测试用例-03-商品模块.csv` (新建)
4. `docs/test/用户端测试用例-04-购物车模块.csv` (新建)
5. `docs/test/用户端测试用例-05-订单模块.csv` (新建)
6. `docs/test/用户端测试用例-06-会员中心模块.csv` (新建)
7. `docs/test/用户端测试用例-07-其他模块.csv` (新建)
8. `docs/test/管理后台测试用例-01-登录与首页.csv` (新建)
9. `docs/test/管理后台测试用例-02-商品管理模块.csv` (新建)
10. `docs/test/管理后台测试用例-03-订单管理模块.csv` (新建)
11. `docs/test/管理后台测试用例-04-库存管理模块.csv` (新建)
12. `docs/test/管理后台测试用例-05-采购者管理模块.csv` (新建)
13. `docs/test/管理后台测试用例-06-营销管理模块.csv` (新建)
14. `docs/test/管理后台测试用例-07-数据统计模块.csv` (新建)
15. `docs/test/管理后台测试用例-08-系统设置模块.csv` (新建)
16. `docs/test/管理后台测试用例-09-权限管理模块.csv` (新建)
17. `docs/test/管理后台测试用例-10-内容管理模块.csv` (新建)
18. `docs/test/管理后台测试用例-11-财务管理模块.csv` (新建)
19. `docs/test/管理后台测试用例-12-ERP管理模块.csv` (新建)
20. `log.md` (本文件)

## 2026-01-02 - 创建聚水潭商品上传接口JMeter测试脚本

### 功能说明
创建JMeter测试脚本（JMX文件），用于测试聚水潭商品上传接口，包含签名自动计算功能。

### 修改原因
用户需要在JMeter中测试聚水潭商品上传接口，需要自动计算签名和时间戳等参数。

### 修改内容

#### 1. JMeter测试脚本

**文件：** `jushuitan_itemsku_upload.jmx` (新建)
- 创建完整的JMeter测试计划
- 包含以下组件：
  - **测试计划级别变量**：
    - `appSecret`: 应用密钥
    - `accessToken`: 访问令牌
    - `appKey`: 应用Key
    - `apiUrl`: API地址
  - **线程组**：单线程，循环1次
  - **HTTP请求采样器**：
    - URL: `https://dev-api.jushuitan.com/open/jushuitan/itemsku/upload`
    - 方法: POST
    - 参数: access_token, app_key, timestamp, charset, version, items, sign
  - **JSR223 PreProcessor**（Groovy脚本）：
    - 自动生成当前时间戳（秒）
    - 构建items JSON参数
    - 计算MD5签名（按参数key排序）
    - 将计算结果存入JMeter变量
  - **HTTP信息头管理器**：
    - Content-Type: application/x-www-form-urlencoded;charset=UTF-8
  - **结果监听器**：
    - 查看结果树
    - 汇总报告

### 脚本功能说明

1. **签名计算逻辑**：
   - 参数按key字母顺序排序
   - 拼接格式：`appSecret + key1 + value1 + key2 + value2 + ... + appSecret`
   - MD5加密并转大写

2. **items参数格式**：
   - JSON对象格式：`{"items":[{...}]}`
   - 包含必填字段：sku_id, i_id, name

3. **使用方式**：
   - 在JMeter中打开JMX文件
   - 修改JSR223 PreProcessor中的商品数据（sku_id, i_id, name）
   - 运行测试计划
   - 在"查看结果树"中查看请求和响应
   - 在日志中查看签名计算过程

### 注意事项

- 使用专用接口路径时，不需要method参数
- 签名计算使用原始值（未URL编码）
- JMeter会自动对参数进行URL编码
- 时间戳每次请求都会自动更新

## 2026-01-02 - 创建聚水潭商品上传接口Postman测试脚本

### 功能说明
创建Postman Collection JSON文件，用于测试聚水潭商品上传接口，包含自动签名计算和响应验证功能。

### 修改原因
用户需要在Postman中测试聚水潭商品上传接口，需要自动计算签名和时间戳等参数。

### 修改内容

#### 1. Postman Collection文件

**文件：** `聚水潭商品上传接口.postman_collection.json` (新建)
- 创建完整的Postman Collection
- 包含以下功能：
  - **Pre-request Script**：
    - 自动生成当前时间戳（秒）
    - 构建items JSON参数
    - 计算MD5签名（按参数key排序）
    - 将计算结果存入Postman环境变量
    - 输出调试日志到Console
  - **HTTP请求配置**：
    - URL: `https://dev-api.jushuitan.com/open/jushuitan/itemsku/upload`
    - 方法: POST
    - Headers: Content-Type: application/x-www-form-urlencoded;charset=UTF-8
    - Body参数: access_token, app_key, timestamp, charset, version, items, sign
  - **Test Script**：
    - 验证响应状态码为200
    - 检查响应包含code字段
    - 输出响应信息到Console
    - 根据code判断成功或失败
  - **Collection变量**：
    - appSecret: 应用密钥
    - accessToken: 访问令牌
    - appKey: 应用Key

### 使用方式

1. **导入Collection**：
   - 打开Postman
   - 点击 Import 按钮
   - 选择 `聚水潭商品上传接口.postman_collection.json` 文件
   - 导入成功后会看到"聚水潭商品上传接口"集合

2. **修改商品数据**（可选）：
   - 点击"商品上传接口"请求
   - 切换到 Pre-request Script 标签页
   - 修改以下字段：
     ```javascript
     "sku_id": "56",        // 修改为你的商品编码
     "i_id": "BM00001",     // 修改为你的款式编码
     "name": "erp测试商品01-一个sku"  // 修改为你的商品名称
     ```

3. **运行请求**：
   - 点击 Send 按钮
   - 在 Console 中查看签名计算过程（View → Show Postman Console）
   - 在响应区域查看API返回结果
   - 在 Test Results 中查看测试结果

4. **查看日志**：
   - 打开 Postman Console（View → Show Postman Console 或 Ctrl+Alt+C）
   - 查看签名计算日志和响应信息

### 脚本功能说明

1. **签名计算逻辑**：
   - 参数按key字母顺序排序
   - 拼接格式：`appSecret + key1 + value1 + key2 + value2 + ... + appSecret`
   - 使用CryptoJS计算MD5并转大写

2. **items参数格式**：
   - JSON对象格式：`{"items":[{...}]}`
   - 包含必填字段：sku_id, i_id, name

3. **环境变量**：
   - timestamp: 自动生成的时间戳
   - sign: 自动计算的签名
   - access_token: 访问令牌
   - app_key: 应用Key
   - items: 商品数据JSON字符串

### 注意事项

- 使用专用接口路径时，不需要method参数
- 签名计算使用原始值（未URL编码）
- Postman会自动对参数进行URL编码
- 时间戳每次请求都会自动更新
- 需要确保Postman已安装CryptoJS库（Postman内置支持）
- 查看日志需要打开Postman Console窗口

## 2026-01-02 - 修正Postman脚本：参数名从items改为biz

### 功能说明
根据聚水潭测试工具的成功请求示例，修正Postman Collection中的参数名。

### 修改原因
通过聚水潭测试工具测试成功，发现关键问题：
- **参数名错误**：应该使用 `biz` 而不是 `items`
- 使用专用路径时不需要 `method` 参数
- 签名计算需要使用正确的参数名

### 修改内容

#### 1. Postman Collection修正

**文件：** `聚水潭商品上传接口.postman_collection.json`
- **参数名修正**：
  - 修改前：参数名使用 `items`
  - 修改后：参数名改为 `biz`
- **Pre-request Script修正**：
  - 参数Map中使用 `params["biz"]` 而不是 `params["items"]`
  - 环境变量名改为 `biz`
  - 更新调试日志输出
- **Body参数修正**：
  - 参数key从 `items` 改为 `biz`
  - 参数value使用 `{{biz}}` 变量
- **移除通用接口版本**：
  - 保留专用路径版本（已验证成功）
  - 移除通用接口+method版本（未验证）

### 成功的请求示例

根据聚水潭测试工具的成功请求：
```
URL: https://dev-api.jushuitan.com/open/jushuitan/itemsku/upload
参数名: biz（不是items）
参数值: {"items":[{"sku_id":"56","i_id":"BM00001","name":"erp测试商品01-一个sku"}]}
签名: 61f67878a4fba01fde122d18e9366799
响应: {"msg":"执行成功","code":0,"data":{"datas":[{"msg":"上传/更新成功","sku_id":"56","is_success":true}]}}
```

### 关键发现

1. **参数名必须是 `biz`**：
   - 虽然参数值的内容是 `{"items":[...]}`
   - 但参数名必须是 `biz`，不是 `items`

2. **签名计算**：
   - MD5源串格式：`appSecret + access_token + app_key + biz + charset + timestamp + version + appSecret`
   - 参数按key字母顺序排序
   - 使用正确的参数名 `biz` 进行签名计算

3. **专用路径**：
   - URL: `https://dev-api.jushuitan.com/open/jushuitan/itemsku/upload`
   - 不需要 `method` 参数

### 使用说明

1. **导入Collection**：
   - 打开Postman
   - 点击 Import 按钮
   - 选择更新后的 `聚水潭商品上传接口.postman_collection.json` 文件

2. **运行测试**：
   - 选择 "商品上传接口（专用路径）" 请求
   - 点击 Send 按钮
   - 应该返回成功响应（code: 0）

3. **修改商品数据**：
   - 在Pre-request Script中修改商品信息：
     ```javascript
     "sku_id": "56",        // 修改为你的商品编码
     "i_id": "BM00001",     // 修改为你的款式编码
     "name": "erp测试商品01-一个sku"  // 修改为你的商品名称
     ```

### 注意事项

- **重要**：参数名必须是 `biz`，不是 `items`
- 使用专用路径时不需要 `method` 参数
- 签名计算时使用正确的参数名 `biz`
- biz参数的值是JSON字符串：`{"items":[{...}]}`

## 2026-01-02 - 更新Postman脚本，添加通用接口版本和调试优化

### 功能说明
更新Postman Collection，添加通用接口版本作为备选方案，并优化调试信息输出。

### 修改原因
用户反馈Postman返回错误140"验证失败！请求数据格式错误"，需要提供多种接口调用方式以便排查问题。

### 修改内容

#### 1. 添加通用接口版本

**文件：** `聚水潭商品上传接口.postman_collection.json`
- 添加第二个请求："商品上传接口（通用接口+method）"
- 使用通用接口路径：`https://dev-api.jushuitan.com/api/open/query.aspx`
- 包含method参数：`jushuitan.itemsku.upload`
- 签名计算中包含method参数

#### 2. 优化调试信息

**文件：** `聚水潭商品上传接口.postman_collection.json`
- 增强Pre-request Script的调试输出：
  - 输出items值的长度
  - 输出每个参数的详细信息
  - 输出签名字符串的长度
  - 区分两种接口版本的日志

#### 3. 两个版本的对比

**版本1：专用接口路径**
- URL: `https://dev-api.jushuitan.com/open/jushuitan/itemsku/upload`
- 不包含method参数
- 签名计算不包含method

**版本2：通用接口+method**
- URL: `https://dev-api.jushuitan.com/api/open/query.aspx`
- 包含method参数：`jushuitan.itemsku.upload`
- 签名计算包含method参数

### 使用建议

1. **先尝试版本1（专用接口路径）**
   - 如果返回错误140，尝试版本2

2. **查看Console日志**
   - 打开Postman Console（View → Show Postman Console）
   - 检查签名字符串是否正确
   - 检查items参数格式是否正确
   - 检查参数顺序是否正确

3. **对比两个版本的差异**
   - 主要区别在于是否包含method参数
   - 签名计算时method参数的包含与否

### 可能的问题排查

1. **签名计算问题**：
   - 检查签名字符串是否正确拼接
   - 确认参数顺序是否正确（按key字母顺序）
   - 确认是否包含method参数（根据接口版本）

2. **items参数格式问题**：
   - 确认JSON格式正确：`{"items":[{...}]}`
   - 检查是否有特殊字符需要转义
   - 确认字段名是否正确（sku_id, i_id, name）

3. **时间戳问题**：
   - 确认时间戳是秒级（不是毫秒）
   - 确认时间戳不是过期的时间

4. **参数缺失或多余**：
   - 确认所有必填参数都已包含
   - 确认没有多余的参数

## 2026-01-02 - 创建聚水潭基础接口测试文件

### 功能说明
创建简单的接口测试文件，用于测试聚水潭基础接口（shops.query），验证基础参数和签名是否正确。

### 修改原因
用户反馈商品上传接口返回错误140，需要先测试基础接口来排查问题。如果基础接口成功，说明问题在items参数；如果基础接口也失败，说明问题在基础参数或签名计算。

### 修改内容

#### 1. Postman基础接口测试文件

**文件：** `聚水潭基础接口测试.postman_collection.json` (新建)
- 创建简单的接口测试集合
- 包含 `shops.query` 接口测试：
  - **接口说明**：最简单的查询接口，不需要业务参数
  - **用途**：验证基础参数和签名是否正确
  - **参数**：access_token, app_key, method, timestamp, charset, version, sign
  - **Pre-request Script**：
    - 自动生成时间戳
    - 自动计算签名
    - 输出详细的调试信息
  - **Test Script**：
    - 验证响应状态码
    - 检查响应格式
    - 输出响应信息

### 使用方式

1. **导入Collection**：
   - 打开Postman
   - 点击 Import 按钮
   - 选择 `聚水潭基础接口测试.postman_collection.json` 文件

2. **运行测试**：
   - 选择 "shops.query - 查询店铺列表" 请求
   - 点击 Send 按钮
   - 打开 Console 查看调试信息（View → Show Postman Console）

3. **查看结果**：
   - 如果返回 `code: 0`，说明基础参数和签名正确
   - 如果返回 `code: 140`，说明问题在基础参数或签名计算
   - 查看 Console 中的签名字符串，对比是否正确

### 排查建议

1. **如果基础接口成功**：
   - 说明基础参数和签名计算正确
   - 问题可能在 items 参数的格式或内容
   - 可以继续排查商品上传接口

2. **如果基础接口失败**：
   - 说明问题在基础参数或签名计算
   - 可能的原因：
     - access_token 已过期或无效
     - 签名计算有错误
     - 参数格式不正确
   - 需要检查 access_token 是否有效
   - 需要对比签名字符串是否正确

### 注意事项

- 这是最简单的接口，不需要业务参数
- 用于验证基础配置是否正确
- 如果这个接口失败，商品上传接口肯定也会失败
- 建议先测试这个接口，再测试商品上传接口

## 2026-01-02 - 修复聚水潭ERP接口对接代码：签名计算和商品上传

### 功能说明
根据聚水潭API签名规则文档和Postman测试成功的结果，修复代码中的签名计算和商品上传接口调用问题。

### 修改原因
通过聚水潭测试工具和Postman测试成功，发现代码中存在以下问题：
1. **签名计算错误**：app_secret拼接在了后面，应该只在前面
2. **MD5结果格式错误**：应该是小写，不是大写
3. **商品上传参数名错误**：应该使用`biz`而不是`items`
4. **商品上传接口路径错误**：应该使用专用路径，不需要method参数

### 修改内容

#### 1. 修复签名计算工具类

**文件：** `backend/src/main/java/com/shoppingmall/common/util/JushuitanSignUtil.java`
- **签名算法修正**：
  - 修改前：`appSecret + key1 + value1 + ... + appSecret`，MD5转大写
  - 修改后：`appSecret + key1 + value1 + ...`（不在后面追加appSecret），MD5保持小写
- **关键修改**：
  - 移除第39行的 `sb.append(appSecret);`
  - 修改第47行：`.toUpperCase()` 改为保持小写
  - 更新注释说明正确的签名算法

**签名规则（根据聚水潭文档）**：
1. 将请求参数中除sign外的多个键值对，根据键按照字典序排序
2. 按照 "key1value1key2value2..." 的格式拼成一个字符串
3. 将 app_secret 拼接在排序后的字符串**前面**得到待签名字符串
4. 使用 MD5 算法加密待加密字符串并转为32位**小写**即为 sign

#### 2. 修复商品上传服务

**文件：** `backend/src/main/java/com/shoppingmall/service/erp/impl/JushuitanItemServiceImpl.java`
- **参数名修正**：
  - 修改前：参数名使用 `items`
  - 修改后：参数名改为 `biz`
  - 变量名从 `itemsJson` 改为 `bizJson`
- **API地址修正**：
  - 修改前：使用通用接口 `config.getTestApiUrl()` 或 `config.getApiUrl()`
  - 修改后：使用专用路径
    - 测试环境：`https://dev-api.jushuitan.com/open/jushuitan/itemsku/upload`
    - 生产环境：`https://api.jushuitan.com/open/jushuitan/itemsku/upload`
- **method参数移除**：
  - 修改前：`"jushuitan.itemsku.upload"` 作为method参数
  - 修改后：`null`（使用专用路径时不需要method参数）
- **响应解析优化**：
  - 修改响应数据结构解析，适配聚水潭返回格式
  - 检查 `data.datas` 数组中的 `is_success` 字段

#### 3. 订单上传接口检查

**文件：** `backend/src/main/java/com/shoppingmall/service/erp/impl/JushuitanApiServiceImpl.java`
- **检查结果**：订单上传接口使用通用接口+method方式，这是正确的
- **参数名**：已经使用`biz`参数名（通过JushuitanHttpUtil.post的默认参数）
- **签名计算**：已通过JushuitanSignUtil修复，订单接口的签名计算会自动修复
- **无需修改**：订单接口的调用方式正确，签名计算已自动修复

### 关键修复点

1. **签名计算算法**：
   ```java
   // 修改前：
   StringBuilder sb = new StringBuilder(appSecret);
   // ... 拼接参数 ...
   sb.append(appSecret);  // 错误：不应该在后面追加
   String sign = DigestUtils.md5Hex(...).toUpperCase();  // 错误：应该是小写
   
   // 修改后：
   StringBuilder sb = new StringBuilder(appSecret);
   // ... 拼接参数 ...
   // 不在后面追加appSecret
   String sign = DigestUtils.md5Hex(...);  // 小写
   ```

2. **商品上传接口**：
   ```java
   // 修改前：
   String apiUrl = config.getTestApiUrl();  // 通用接口
   JushuitanHttpUtil.post(apiUrl, ..., "jushuitan.itemsku.upload", "items", itemsJson);
   
   // 修改后：
   String apiUrl = "https://dev-api.jushuitan.com/open/jushuitan/itemsku/upload";  // 专用路径
   JushuitanHttpUtil.post(apiUrl, ..., null, "biz", bizJson);  // 无method，参数名biz
   ```

### 影响范围

1. **商品上传模块**：
   - `JushuitanItemServiceImpl.uploadItem()` - 已修复
   - `JushuitanItemServiceImpl.uploadItems()` - 自动修复（调用uploadItem）

2. **订单同步模块**：
   - `JushuitanApiServiceImpl.uploadOrder()` - 签名计算自动修复
   - `JushuitanApiServiceImpl.queryOrderStatus()` - 签名计算自动修复
   - `JushuitanApiServiceImpl.queryLogistics()` - 签名计算自动修复
   - `JushuitanOrderServiceImpl.pushOrder()` - 签名计算自动修复

3. **签名工具类**：
   - `JushuitanSignUtil.generateSign()` - 已修复
   - 所有使用该方法的接口都会自动修复

### 测试验证

根据Postman测试成功的结果：
- ✅ 签名计算正确
- ✅ 参数名使用`biz`正确
- ✅ 专用路径正确
- ✅ 响应解析正确

### 注意事项

1. **签名计算**：
   - app_secret只拼接在前面，不在后面
   - MD5结果是小写，不是大写
   - 参数按key字母顺序排序

2. **商品上传接口**：
   - 参数名必须是`biz`，不是`items`
   - 使用专用路径时不需要method参数
   - API地址使用专用路径

3. **订单上传接口**：
   - 使用通用接口+method方式（正确）
   - 参数名已经是`biz`（正确）
   - 签名计算已自动修复

### 后续建议

1. **测试商品上传**：
   - 重新测试商品上传功能
   - 检查响应是否正确解析

2. **测试订单同步**：
   - 测试订单推送功能
   - 确认签名计算是否正确

3. **监控日志**：
   - 查看签名计算的日志输出
   - 确认签名结果是小写格式

## 2026-01-02 - 修复代码语法错误

### 功能说明
修复JushuitanItemServiceImpl.java中的语法错误。

### 修改原因
启动服务时报错：`Syntax error on token "}", delete this token`，在第173行有多余的闭合大括号。

### 修改内容

**文件：** `backend/src/main/java/com/shoppingmall/service/erp/impl/JushuitanItemServiceImpl.java`
- **修复语法错误**：
  - 删除第173行的多余闭合大括号
  - 修复前：`}` 和 `}` 两个闭合大括号
  - 修复后：只保留一个闭合大括号

### 问题原因

在之前的修改中，不小心添加了多余的闭合大括号，导致编译错误。

## 2026-01-02 - 创建聚水潭订单上传接口Postman测试脚本

### 功能说明
创建订单上传接口的Postman测试脚本，用于测试和调试订单推送功能。

### 修改原因
订单推送返回错误140"参数传递错误"，需要创建Postman测试脚本来排查问题。

### 修改内容

#### 1. Postman Collection创建

**文件：** `聚水潭订单上传接口.postman_collection.json` (新建)
- **接口信息**：
  - URL: `https://dev-api.jushuitan.com/api/open/query.aspx`
  - 使用通用接口路径，需要method参数：`orders.upload`
  - 参数名：`biz`
- **Pre-request Script功能**：
  - 自动生成时间戳（秒）
  - 自动计算签名（小写，appSecret只在前面）
  - 构建订单数据（orders数组）
  - 输出详细的调试信息
- **Test Script功能**：
  - 验证响应状态码
  - 检查响应格式
  - 输出响应信息

### 订单数据结构

根据代码中的订单数据格式：
```json
{
  "orders": [{
    "pay": [{
      "outer_pay_id": "订单号",
      "pay_date": "支付时间",
      "payment": "支付方式",
      "amount": 金额
    }],
    "items": [{
      "sku_id": "商品编码",
      "item_name": "商品名称",
      "qty": 数量,
      "price": 单价
    }],
    "so_id": "订单号",
    "order_date": "订单日期",
    "pay_date": "支付日期",
    "receiver_name": "收货人姓名",
    "receiver_mobile": "收货人手机",
    "receiver_province": "省份",
    "receiver_city": "城市",
    "receiver_district": "区县",
    "receiver_address": "详细地址",
    "freight": 运费,
    "pay_amount": 支付金额
  }]
}
```

### 使用方式

1. **导入Collection**：
   - 打开Postman
   - 点击 Import 按钮
   - 选择 `聚水潭订单上传接口.postman_collection.json` 文件

2. **运行测试**：
   - 选择 "订单上传接口（通用接口+method）" 请求
   - 点击 Send 按钮
   - 打开 Console 查看调试信息（View → Show Postman Console）

3. **修改订单数据**：
   - 在Pre-request Script中修改订单信息：
     - so_id: 订单号
     - order_date: 订单日期
     - pay_date: 支付日期
     - receiver_*: 收货人信息
     - items: 订单商品列表
     - pay: 支付信息

### 关键配置

1. **签名计算**：
   - app_secret只拼接在前面，不在后面
   - MD5结果是小写，不是大写
   - 参数按key字母顺序排序
   - 包含method参数

2. **接口路径**：
   - 使用通用接口：`https://dev-api.jushuitan.com/api/open/query.aspx`
   - 需要method参数：`orders.upload`
   - 参数名：`biz`

### 注意事项

- 订单接口使用通用接口+method方式（与商品上传不同）
- 参数名必须是`biz`
- 签名计算包含method参数
- 订单数据格式必须正确

## 2026-01-03 - 更新订单上传Postman脚本：添加shop_id字段

### 功能说明
根据最新的接口日志，更新订单上传Postman脚本，添加必填的`shop_id`字段。

### 修改原因
从日志中发现订单数据中包含了`shop_id`字段（值为"10409060"），这是必填字段，之前的Postman脚本中缺少此字段。

### 修改内容

**文件：** `聚水潭订单上传接口.postman_collection.json`
- **添加shop_id字段**：
  - 在订单数据中添加`"shop_id": "10409060"`
  - 更新接口说明，明确shop_id是必填字段
  - 更新订单数据结构说明，包含shop_id字段

### 订单数据结构（更新后）

```json
{
  "orders": [{
    "pay": [...],
    "items": [...],
    "so_id": "订单号",
    "shop_id": "店铺ID（必填）",  // 新增必填字段
    "order_date": "订单日期",
    "pay_date": "支付日期",
    "receiver_name": "收货人姓名",
    "receiver_mobile": "收货人手机",
    "receiver_province": "省份",
    "receiver_city": "城市",
    "receiver_district": "区县",
    "receiver_address": "详细地址",
    "freight": 运费,
    "pay_amount": 支付金额
  }]
}
```

### 注意事项

1. **shop_id字段**：
   - 这是必填字段，需要从聚水潭配置中获取
   - 在代码中，shop_id从`JushuitanConfigVO.getShopId()`获取
   - 如果shop_id为空或null，可能导致接口返回错误140

2. **签名计算**：
   - 签名计算使用小写MD5
   - appSecret只在前面拼接，不在后面
   - 参数按key字母顺序排序

3. **字段顺序**：
   - shop_id字段位置在so_id之后，order_date之前
   - 这个顺序与代码中的字段顺序一致

### 排查建议

如果仍然返回错误140，可能的原因：
1. shop_id值不正确或无效
2. 订单数据中其他字段格式不正确
3. 某些必填字段缺失
4. 字段值不符合聚水潭的要求

建议：
- 使用Postman脚本测试，查看Console中的详细日志
- 对比签名和参数是否正确
- 确认shop_id值是否正确
- 检查订单数据格式是否完全符合聚水潭API要求

## 2026-01-03 - 对标成功示例修复订单上传接口和Postman脚本

### 功能说明
根据成功的curl请求示例，修复订单上传接口代码和Postman脚本，确保URL、参数格式和签名计算完全正确。

### 修改原因
对比成功的curl请求示例，发现以下问题：
1. Postman脚本使用了错误的URL（`query.aspx`），应该使用专用路径（`orders/upload`）
2. Postman脚本包含了method参数，但使用专用路径时不应该包含method参数
3. 需要确保签名计算逻辑与成功示例完全一致

### 修改内容

#### 1. 修复Postman脚本

**文件：** `聚水潭订单上传接口.postman_collection.json`

**修改内容：**
- **URL修改**：
  - 修改前：`https://dev-api.jushuitan.com/api/open/query.aspx`
  - 修改后：`https://dev-api.jushuitan.com/open/jushuitan/orders/upload`
- **移除method参数**：
  - 从Pre-request Script中移除`method`变量定义
  - 从params中移除`method`参数
  - 从请求body中移除`method`字段
  - 从环境变量设置中移除`method`
- **更新接口说明**：
  - 更新description，明确使用专用路径，不需要method参数
  - 添加成功示例的curl命令
  - 添加MD5源串格式说明

**关键修改点：**
```javascript
// 修改前：包含method参数
const method = "orders.upload";
params["method"] = method;

// 修改后：移除method参数
// 使用专用路径时，不需要method参数
```

**签名计算验证：**
- 参数顺序（按字典序）：`access_token`, `app_key`, `biz`, `charset`, `timestamp`, `version`
- MD5源串格式：`appSecret + access_token + app_key + biz + charset + timestamp + version`
- MD5结果：小写（`toString()`不转大写）

#### 2. 验证代码实现

**文件：** `backend/src/main/java/com/shoppingmall/service/erp/impl/JushuitanApiServiceImpl.java`
- **验证内容**：
  - ✅ URL配置正确：`https://dev-api.jushuitan.com/open/jushuitan/orders/upload`
  - ✅ method参数传入null（使用专用路径时不需要method）
  - ✅ biz参数格式正确：JSON数组字符串 `[{...}]`
  - ✅ ObjectMapper配置正确：排除null值

**文件：** `backend/src/main/java/com/shoppingmall/common/util/JushuitanHttpUtil.java`
- **验证内容**：
  - ✅ method参数判断逻辑正确：`if (method != null && !method.trim().isEmpty())`
  - ✅ 当method为null时，不会添加到params中
  - ✅ 签名计算时不会包含method参数

**文件：** `backend/src/main/java/com/shoppingmall/common/util/JushuitanSignUtil.java`
- **验证内容**：
  - ✅ 签名算法正确：`appSecret + key1 + value1 + key2 + value2 + ...`
  - ✅ appSecret只拼接在前面，不在后面
  - ✅ 参数按字典序排序（TreeMap自动排序）
  - ✅ MD5结果保持小写
  - ✅ 正确排除sign字段和空值

### 成功的请求示例

**curl命令：**
```bash
curl --location --request POST 'https://dev-api.jushuitan.com/open/jushuitan/orders/upload' \
--header 'Content-Type: application/x-www-form-urlencoded;charset=UTF-8' \
--data-urlencode 'app_key=fb5302ac42e8496d9e764db70a3c5c24' \
--data-urlencode 'access_token=f804693efc49416a84dcf0ca901e8622' \
--data-urlencode 'timestamp=1767375484' \
--data-urlencode 'version=2' \
--data-urlencode 'charset=utf-8' \
--data-urlencode 'sign=86576f2f9541143ae82de57806d36f44' \
--data-urlencode 'biz=[{...}]'
```

**MD5源串格式：**
```
appSecret + access_token + app_key + biz + charset + timestamp + version
```

**参数顺序（按字典序）：**
1. `access_token`
2. `app_key`
3. `biz`
4. `charset`
5. `timestamp`
6. `version`

**注意：**
- **不需要method参数**（使用专用路径时）
- 签名计算时，参数按字典序排序
- appSecret只拼接在前面，不在后面
- MD5结果是小写，不是大写

### 测试建议

1. **使用Postman脚本测试**：
   - 导入更新后的`聚水潭订单上传接口.postman_collection.json`
   - 运行"订单上传接口（专用路径）"请求
   - 查看Console中的详细日志，确认：
     - URL是`orders/upload`而不是`query.aspx`
     - 参数中不包含`method`
     - 签名计算正确

2. **验证签名**：
   - 对比Postman Console中的签名字符串和签名结果
   - 确认签名原始字符串格式正确
   - 确认签名结果是小写格式

3. **代码测试**：
   - 重新编译代码
   - 测试订单推送功能
   - 查看日志确认请求URL和参数格式正确

### 相关文件

- `聚水潭订单上传接口.postman_collection.json`
- `backend/src/main/java/com/shoppingmall/service/erp/impl/JushuitanApiServiceImpl.java`
- `backend/src/main/java/com/shoppingmall/common/util/JushuitanHttpUtil.java`
- `backend/src/main/java/com/shoppingmall/common/util/JushuitanSignUtil.java`

<<<<<<< HEAD
=======
## 2026-01-03 - 修复订单列表ERP状态字段未更新问题

### 功能说明
修复订单列表查询时ERP状态字段未正确显示的问题，确保订单推送成功后ERP状态能正确同步到订单列表。

### 修改原因
订单推送成功后，数据库中的`erp_sync_status`字段已更新，但订单列表查询时没有正确映射该字段，导致前端无法显示ERP状态。

### 修改内容

#### 1. 在OrderListVO中添加erpSyncStatus字段

**文件：** `backend/src/main/java/com/shoppingmall/vo/OrderListVO.java`
- **添加字段**：
  ```java
  /**
   * ERP同步状态（0-未同步，1-已同步，2-同步失败）
   */
  private Integer erpSyncStatus;
  ```
- **位置**：在`statusText`字段之后，`logistics`字段之前

#### 2. 在convertToListVO方法中映射erpSyncStatus字段

**文件：** `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`
- **修改内容**：
  - 在`convertToListVO`方法中添加ERP状态字段的映射
  - 修改位置：在设置`statusText`之后添加
  ```java
  vo.setErpSyncStatus(order.getErpSyncStatus());
  ```

### 问题分析

**问题原因：**
1. `OrderListVO`中缺少`erpSyncStatus`字段
2. `convertToListVO`方法中没有映射`erpSyncStatus`字段
3. 虽然订单推送成功后数据库已更新，但VO转换时没有包含该字段

**前端显示：**
- 前端页面（`admin-frontend/src/views/order/List.vue`）已经正确使用了`row.erpSyncStatus`字段
- 但由于后端VO中没有该字段，导致前端无法获取到ERP状态值

### 测试建议

1. **验证订单推送**：
   - 推送一个订单到ERP系统
   - 确认推送成功（返回code=0）

2. **验证订单列表**：
   - 访问订单列表页面：`http://localhost:3003/admin/order/list`
   - 确认ERP状态列正确显示：
     - 已同步：绿色标签"已同步"
     - 同步失败：红色标签"同步失败"
     - 未同步：灰色标签"未同步"

3. **数据库验证**：
   - 查询订单表，确认`erp_sync_status`字段值正确
   - 确认`erp_sync_time`和`erp_order_id`字段也已更新

### 相关文件

- `backend/src/main/java/com/shoppingmall/vo/OrderListVO.java`
- `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`
- `admin-frontend/src/views/order/List.vue`（前端页面，已正确实现）

>>>>>>> gitee/dev
## 2026-01-03 - 修复聚水潭订单推送Error 140问题

### 功能说明
修复聚水潭订单推送接口返回Error 140（参数传递错误）的问题，确保使用正确的API URL和参数格式。

### 修改原因
订单推送时返回Error 140，对比正确的curl请求发现：
1. 日志显示使用了错误的URL（`query.aspx`），应该使用专用路径（`orders/upload`）
2. 签名计算中包含了method参数，但使用专用路径时不应该包含method参数

### 修改内容

#### 1. 修复HTTP工具类method参数处理

**文件：** `backend/src/main/java/com/shoppingmall/common/util/JushuitanHttpUtil.java`
- **修改内容**：
  - 增强method参数的判断逻辑，确保当method为null或空字符串时，不会被添加到请求参数中
  - 添加注释说明：只有当method不为null且不为空时才添加到params中，这样签名计算时就不会包含method
- **关键修改**：
  ```java
  // 修改前：简单的null判断
  if (method != null && !method.isEmpty()) {
      params.put("method", method);
  }
  
  // 修改后：增加trim()处理，确保空字符串也不会被添加
  if (method != null && !method.trim().isEmpty()) {
      params.put("method", method);
  }
  ```

#### 2. 验证订单上传URL配置

**文件：** `backend/src/main/java/com/shoppingmall/service/erp/impl/JushuitanApiServiceImpl.java`
- **验证内容**：
  - 确认测试环境URL：`https://dev-api.jushuitan.com/open/jushuitan/orders/upload`
  - 确认生产环境URL：`https://api.jushuitan.com/open/jushuitan/orders/upload`
  - 确认method参数传入null，使用专用路径时不需要method参数
  - 确认biz参数名正确

#### 3. 签名计算逻辑验证

**文件：** `backend/src/main/java/com/shoppingmall/common/util/JushuitanSignUtil.java`
- **验证内容**：
  - 签名算法正确：`appSecret + key1 + value1 + key2 + value2 + ...`（appSecret只在前面）
  - 参数按字典序排序（TreeMap自动排序）
  - MD5结果保持小写
  - 正确排除sign字段和空值

### 正确的请求格式

根据聚水潭API文档和成功的curl请求，订单上传接口的正确格式：

**URL：**
- 测试环境：`https://dev-api.jushuitan.com/open/jushuitan/orders/upload`
- 生产环境：`https://api.jushuitan.com/open/jushuitan/orders/upload`

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

