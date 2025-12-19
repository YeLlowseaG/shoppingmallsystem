# 修改日志

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
