# 修改日志

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
