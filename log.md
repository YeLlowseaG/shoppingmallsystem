# 修改日志

## 2025-12-20 - 修复商品关闭规格后仍显示规格选择器的问题

### 问题说明
商品原本启用了规格，在管理后台关闭规格后，商品详情页面仍然显示灰色的规格值（如"标准"），且无法选中。

### 问题原因
前端商品详情页面在加载时，只检查了是否有SKU数据，没有检查商品的`enableSpec`字段。即使商品在管理后台关闭了规格（`enableSpec = 0`），如果数据库中还有SKU数据，前端仍然会加载并显示规格选择器。

### 修复内容

#### 前端代码修改

**ProductVO接口：**
- `frontend/src/api/buyer/product.ts`
  - 在 `ProductVO` 接口中添加 `enableSpec` 字段：是否启用规格（0-否，1-是）

**商品详情页面：**
- `frontend/src/views/products/Detail.vue`
  - 在商品数据中添加 `enableSpec` 字段映射
  - 修改规格选择器显示逻辑：
    - 只有当 `enableSpec === 1` 且有SKU数据时，才显示规格选择器
    - 如果 `enableSpec === 0`，即使有SKU数据，也不显示规格选择器，而是显示基础库存信息
  - 修改商品详情加载逻辑：
    - 只有当 `enableSpec === 1` 且有SKU数据时，才加载规格属性
    - 如果 `enableSpec === 0`，清空所有规格相关数据

### 修复效果
- ✅ 商品关闭规格后，不再显示规格选择器
- ✅ 显示基础库存信息（无规格商品）
- ✅ 避免显示灰色的、无法选中的规格值
- ✅ 根据商品的`enableSpec`字段正确判断是否显示规格

### 技术细节
- **判断逻辑**：`product.enableSpec === 1 && productSpecKeys.length > 0`
- **数据清理**：当`enableSpec === 0`时，清空`productSpecKeys`、`productSkuList`、`currentSku`等规格相关数据
- **向后兼容**：后端ProductVO已有`enableSpec`字段，前端只需正确使用即可

---

## 2025-12-20 - 修复商品详情页面购买咨询和商品评论模块显示问题

### 问题说明
1. 购买咨询模块中，"*联系人姓名："字段出现两个必填星号，且标签可能换行
2. 商品评论模块中，必填的星号不是红色

### 问题原因
- 购买咨询和商品评论模块在label中手动添加了星号（如`label="*联系人姓名："`），但Element Plus的form-item会自动为required字段添加红色星号，导致显示两个星号
- 商品评论模块的必填字段没有使用Element Plus的`required`属性，所以星号不是红色的
- 标签可能因为内容过长而换行

### 修复内容

#### 前端代码修改

**商品详情页面：**
- `frontend/src/views/products/Detail.vue`
  - **购买咨询模块**：
    - 移除label中的手动星号（`*联系人姓名：` → `联系人姓名：`）
    - 移除label中的手动星号（`*咨询内容：` → `咨询内容：`）
    - 为必填字段添加`required`属性，让Element Plus自动显示红色星号
  - **商品评论模块**：
    - 移除label中的手动星号（`*评论标题：` → `评论标题：`）
    - 移除label中的手动星号（`*联系方式：` → `联系方式：`）
    - 移除label中的手动星号（`*评论内容：` → `评论内容：`）
    - 为所有必填字段添加`required`属性，让Element Plus自动显示红色星号
  - **样式优化**：
    - 添加CSS样式，确保表单标签不换行（`white-space: nowrap`）

### 修复效果
- ✅ 购买咨询模块：只显示一个红色必填星号，标签不换行
- ✅ 商品评论模块：必填星号显示为红色，标签不换行
- ✅ 统一使用Element Plus的标准必填标识方式

### 技术细节
- **Element Plus必填标识**：使用`required`属性，Element Plus会自动在label前添加红色星号
- **防止换行**：使用`white-space: nowrap`和`word-break: keep-all`确保标签不换行
- **样式作用域**：使用`:deep()`选择器确保样式能够穿透Element Plus组件

---

## 2025-12-20 - 修复商品详情接口isMember字段返回错误问题

### 问题说明
会员用户登录后访问商品详情接口，后端返回的 `isMember` 字段为 0，而不是 1。

### 问题原因
商品详情接口 `/api/buyer/product/**` 被排除在JWT拦截器之外，导致即使已登录用户访问，拦截器也不会处理，`userId` 不会被设置到 request attribute 中。因此后端 `ProductServiceImpl.convertToVO` 方法中获取的 `userId` 为 `null`，导致 `isMember` 被设置为 0。

### 修复内容

#### 后端代码修改

**JWT认证拦截器：**
- `backend/src/main/java/com/shoppingmall/common/security/JwtAuthenticationInterceptor.java`
  - 修改拦截器逻辑，支持可选认证：
    - 对于商品详情、分类、网站内容、导航等公开接口，支持可选认证
    - 如果有 token，验证并设置 `userId` 到 request attribute
    - 如果没有 token 或 token 无效，允许继续访问（作为游客），不设置 `userId`
    - 其他接口仍然必须登录

**WebMvcConfig配置：**
- `backend/src/main/java/com/shoppingmall/common/config/WebMvcConfig.java`
  - 移除商品详情、分类、网站内容、导航等接口的排除配置
  - 这些接口现在会经过拦截器，但支持可选认证（有token就设置userId，没有token就允许通过）

### 修复逻辑
- **可选认证机制**：
  - 商品详情等公开接口允许游客访问（不需要token）
  - 但如果用户提供了有效的token，拦截器会验证并设置 `userId`
  - 后端根据 `userId` 判断用户是否是会员，并返回正确的 `isMember` 和 `memberPrice`
- **数据流程**：
  1. 用户访问商品详情接口（带token）
  2. 拦截器验证token，设置 `userId` 到 request attribute
  3. `ProductController` 获取 `userId` 并传递给 `ProductService`
  4. `ProductServiceImpl` 根据 `userId` 查询用户信息，判断 `isMember`
  5. 返回正确的 `isMember` 和 `memberPrice` 给前端

### 技术细节
- **路径匹配**：使用 `uri.contains("/product/")` 等条件判断是否为公开接口
- **异常处理**：token无效时不抛出异常，允许继续访问（作为游客）
- **向后兼容**：未登录用户仍然可以访问商品详情，只是 `isMember` 为 0

### 影响范围
- ✅ 商品详情接口（`/api/buyer/product/{id}`）
- ✅ 商品分类接口（`/api/buyer/product-category/**`）
- ✅ 网站内容接口（`/api/buyer/website/**`）
- ✅ 导航接口（`/api/buyer/navigation/**`）

---

## 2025-12-20 - 商品详情页面价格显示问题修复

### 问题说明
会员用户登录后，商品详情页面没有正确显示"会员价"标签和会员价。

### 问题原因
前端判断逻辑不够清晰，没有完全依赖后端返回的isMember字段。

### 修复内容

#### 前端代码修改

**商品详情页面：**
- `frontend/src/views/products/Detail.vue`
  - 优化 `getPriceLabel` 方法：完全依赖后端返回的isMember字段（1-会员，0-普通用户）
  - 优化 `getDisplayPrice` 方法：完全依赖后端返回的isMember字段和memberPrice
  - 添加调试日志（仅开发环境）：方便排查问题

### 修复逻辑
- **价格标签显示**：根据后端返回的 `isMember === 1` 判断显示"会员价"或"商品价格"
- **价格显示**：
  - 会员用户（isMember === 1）：显示后端计算的会员价（memberPrice）
  - 普通用户（isMember === 0 或 undefined）：显示原价（basePrice 或 SKU的price）
- **数据来源**：完全依赖后端根据用户ID判断并返回的isMember和memberPrice字段

### 技术细节
- 后端已根据用户ID判断isMember字段（ProductServiceImpl.convertToVO）
- 后端已根据用户会员状态计算memberPrice（calculateMemberPriceForProduct）
- 前端完全依赖后端返回的数据，不再使用userStore作为主要判断依据

---

## 2025-12-20 - 商品详情页面价格逻辑修改

### 功能说明
修改商品详情页面的价格逻辑，参考购物车页面的价格逻辑实现：
1. 根据用户是否是会员动态显示价格标签（普通用户显示"商品价格"，会员显示"会员价"）
2. 会员价计算优先级：
   - 第一优先级：商品/SKU配置的固定会员价（enableMemberPrice = 1 且 memberPrice > 0）
   - 第二优先级：根据会员等级折扣率计算
   - 普通用户：返回原价
3. SKU处理：有SKU时优先使用SKU的价格和会员价配置

### 修改原因
- 商品详情页面的价格逻辑需要与购物车页面保持一致
- 需要根据用户会员状态动态显示价格标签
- 会员价计算需要遵循统一的优先级规则

### 修改内容

#### 后端代码修改

**ProductVO：**
- `backend/src/main/java/com/shoppingmall/vo/ProductVO.java`
  - 添加 `isMember` 字段：用户是否是会员（0-普通用户，1-会员）

**ProductServiceImpl：**
- `backend/src/main/java/com/shoppingmall/service/product/impl/ProductServiceImpl.java`
  - 修改 `convertToVO` 方法：
    - 添加用户会员状态判断，设置 `isMember` 字段
    - 修改会员价计算逻辑，参考购物车的逻辑
    - 优先使用商品配置的固定会员价，如果没有则根据会员等级折扣率计算
    - 非会员直接返回原价
  - 添加 `calculateMemberPriceForProduct` 方法：计算商品的会员价格
  - 添加 `calculateMemberPriceForSku` 方法：计算SKU的会员价格
  - 修改 `calculateMemberPriceByDiscount` 方法：根据会员等级折扣率计算会员价格

#### 前端代码修改

**ProductVO接口：**
- `frontend/src/api/buyer/product.ts`
  - 在 `ProductVO` 接口中添加 `isMember` 字段

**商品详情页面：**
- `frontend/src/views/products/Detail.vue`
  - 修改价格显示逻辑：
    - 添加 `getPriceLabel` 方法：根据用户是否是会员动态显示价格标签
    - 添加 `getDisplayPrice` 方法：根据用户是否是会员返回对应价格
    - 修改价格显示模板，使用新的方法动态显示价格
  - 在商品数据中添加 `isMember` 字段映射

### 功能特性
- ✅ 根据用户会员状态动态显示价格标签
- ✅ 会员价计算遵循统一优先级规则
- ✅ SKU价格优先使用SKU配置
- ✅ 非会员用户显示原价
- ✅ 与购物车页面价格逻辑保持一致

### 技术细节
- **会员价计算优先级**：
  1. 检查用户是否是会员（非会员返回原价）
  2. 检查商品/SKU是否配置了固定会员价（有则使用）
  3. 根据会员等级折扣率计算会员价
- **价格显示逻辑**：
  - 会员用户：显示"会员价"标签和会员价
  - 普通用户：显示"商品价格"标签和原价
- **SKU处理**：有SKU时优先使用SKU的价格和会员价配置

### 影响范围
- ✅ 商品详情页面的价格显示
- ✅ 商品详情API返回的ProductVO
- ✅ SKU价格计算逻辑

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
