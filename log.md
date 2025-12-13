# 修改日志

## 2025-12-13 - SKU库存管理系统完整实现

### 修改内容
实现完整的SKU商品规格库存管理系统，包括数据库表结构、后端服务、前端管理界面和买家端规格选择器。

### 修改文件
1. **数据库**:
   - `database/yellow_20251213_create_sku_tables.sql` - SKU相关表结构
   - `database/yellow_20251213_migrate_existing_products_to_sku.sql` - 数据迁移脚本

2. **后端实体和服务**:
   - `backend/src/main/java/com/shoppingmall/entity/ProductSku.java`
   - `backend/src/main/java/com/shoppingmall/entity/ProductSpecKey.java`
   - `backend/src/main/java/com/shoppingmall/entity/ProductSpecValue.java`
   - `backend/src/main/java/com/shoppingmall/service/sku/`
   - `backend/src/main/java/com/shoppingmall/controller/admin/ProductSkuController.java`
   - `backend/src/main/java/com/shoppingmall/controller/buyer/BuyerProductSkuController.java`

3. **前端管理界面**:
   - `admin-frontend/src/views/product/Add.vue` - 商品规格配置UI
   - `admin-frontend/src/views/product/ProductManage.vue` - 商品编辑规格管理
   - `admin-frontend/src/api/admin/sku.ts` - SKU管理API

4. **前端买家界面**:
   - `frontend/src/components/product/SpecSelector.vue` - 规格选择器组件
   - `frontend/src/views/products/Detail.vue` - 商品详情规格集成
   - `frontend/src/api/buyer/sku.ts` - 买家端SKU API

### 具体功能
1. **SKU规格管理**: 支持多规格属性配置，自动生成SKU组合
2. **价格库存管理**: 每个SKU独立价格和库存设置
3. **规格选择器**: 买家端智能规格联动选择，缺货置灰
4. **库存统一管理**: 库存管理页面整合，简化为一级菜单
5. **立即购买功能**: 集成SKU规格到订单创建流程

### 数据库表结构
- `product_spec_key`: 商品规格属性表（颜色、尺寸等）
- `product_spec_value`: 商品规格值表（红色、L码等）
- `product_sku`: 商品SKU表（规格组合+价格库存）

### 影响
- ✅ 支持多规格商品销售管理
- ✅ 精确的库存控制和价格管理
- ✅ 买家端流畅的规格选择体验
- ✅ 完整的订单SKU信息记录

---

## 2025-12-13 - 修改支付记录页面操作按钮样式

### 修改内容
将支付记录页面（`/admin/finance/payment-record`）的操作按钮样式修改为与预存款交易记录页面一致。

### 修改文件

#### 前端
1. `admin-frontend/src/views/finance/PaymentRecord.vue` - 修改操作列按钮样式

### 具体修改

#### 1. 操作按钮样式调整
- 将"查看"按钮从 `type="primary" link size="small"` 改为 `type="primary" size="small"`（去掉 link 属性）
- 将按钮文字从"查看"改为"查看详情"
- 将"退款"按钮从 `type="danger" link size="small"` 改为 `type="danger" size="small"`（去掉 link 属性）
- 将操作列宽度从 150 调整为 280，与预存款交易记录页面保持一致

### 技术细节
- Element Plus 按钮：去掉 `link` 属性后，按钮显示为普通按钮样式而非文本链接样式
- 操作列宽度：从 150px 增加到 280px，为按钮提供更充足的显示空间
- 按钮文字：统一使用"查看详情"而非"查看"，提升用户体验

### 影响
- ✅ 操作按钮样式与预存款交易记录页面保持一致
- ✅ 按钮更加醒目，提升用户体验
- ✅ 操作列宽度增加，按钮显示更美观

---

## 2025-12-10 - 购物车页面屏蔽批发优惠价字段

### 修改内容
在购物车页面（/cart）中屏蔽批发优惠价字段的显示。

### 修改文件
1. frontend/src/views/cart/Index.vue

### 具体修改
1. **删除表头列**: 移除购物车表格表头中的"批发优惠价"列
2. **删除表体单元格**: 移除购物车商品列表中显示批发优惠价的单元格
3. **调整空购物车提示**: 将空购物车行的colspan从10调整为9（因为删除了一列）

### 影响
- ✅ 购物车页面不再显示批发优惠价字段
- ✅ 保持其他功能正常（销售价格、会员价等字段正常显示）
- ✅ 表格布局自动调整，不影响页面美观
---

## 2025-12-10 - 购物结算页面优化收货地址和收货人信息校验

### 修改内容
优化购物结算页面（/cart/checkout）的收货地址和收货人信息处理逻辑。

### 修改文件
1. frontend/src/views/cart/Checkout.vue

### 具体修改
1. **清空默认数据**: 
   - 将addressForm的初始化默认值全部清空（region、detailAddress、zipCode、receiverName、receiverPhone、receiverMobile）
   - 当用户没有收货地址数据时，收货地址输入框不再显示默认数据

2. **完善收货人信息校验**:
   - 收货人姓名（receiverName）为必填字段
   - 手机（receiverMobile）和电话（receiverPhone）至少填写一项
   - 在handlePlaceOrder函数中添加完整的表单校验逻辑

3. **优化地址加载逻辑**:
   - 在loadAddressList函数中，当没有收货地址时，自动显示地址表单并清空所有默认值
   - 加载失败时也清空表单默认值

### 影响
- ✅ 没有收货地址数据时，输入框不再显示默认数据
- ✅ 收货人信息必填校验完善，确保下单时信息完整
- ✅ 提升用户体验，避免使用错误的默认数据
---

## 2025-12-10 - 购物车页面默认勾选所有商品

### 修改内容
在购物车页面（/cart）中，默认勾选所有商品。

### 修改文件
1. frontend/src/views/cart/Index.vue

### 具体修改
1. **修改默认选中状态**: 在loadCartList函数中，将商品项的selected属性从false改为true，使所有商品默认被勾选

### 影响
- ✅ 购物车页面加载时，所有商品默认被勾选
- ✅ 提升用户体验，方便用户直接进行批量操作或结算
- ✅ 全选状态会根据所有商品的选中状态自动更新
---

## 2024年 - 创建需求分析文档

### 修改内容
- 创建了 `docs/需求分析文档.md` 文件
- 文档包含以下主要内容：
  1. 项目概述
  2. 功能需求分析（用户端功能、管理端功能）
  3. 第三方对接设计（支付、物流、短信、邮件、第三方登录、文件存储、地图服务）
  4. 技术架构设计（后端Spring Boot架构、前端Vue3架构、数据库设计概要）
  5. 开发计划概要
  6. 部署方案概要
  7. 注意事项

### 技术栈
- 后端：Spring Boot + MySQL + Redis
- 前端：Vue3 + Vite + Element Plus
- 第三方对接：支付宝、微信支付、快递100、阿里云短信/OSS等

### 文件位置
- `docs/需求分析文档.md`
