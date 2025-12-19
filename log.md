# 修改日志

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
