# 数据库设计文档

## 数据库信息

- **数据库名**: shopping_mall
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_general_ci
- **MySQL版本**: 8.0+

## 表结构说明

### 1. 用户相关表

- **sys_user**: 用户表，存储采购者和平台管理员的基本信息
- **sys_user_audit**: 用户审核表，记录用户注册审核信息
- **user_address**: 收货地址表，存储用户的收货地址信息

### 2. 商品相关表

- **product_category**: 商品分类表，支持三级分类
- **product**: 商品表，存储商品基本信息
- **product_price**: 商品价格表，支持不同用户等级的价格
- **product_stock**: 商品库存表，管理商品库存信息

### 3. 订单相关表

- **order**: 订单表，存储订单基本信息
- **order_item**: 订单商品表，存储订单中的商品明细（快照）
- **order_logistics**: 订单物流表，存储物流信息

### 4. 购物车表

- **cart**: 购物车表，存储用户购物车中的商品

### 5. 支付相关表

- **pre_deposit**: 预存款表，存储用户预存款余额
- **pre_deposit_detail**: 预存款明细表，记录预存款变动明细
- **payment_record**: 支付记录表，记录所有支付流水

### 6. 物流管理相关表

- **logistics_company**: 物流公司表，存储物流公司基本信息
- **shipping_method**: 配送方式表，存储配送方式配置（固定运费、按重量、按件数、按金额、运费模板等）
- **shipping_template**: 运费模板表，存储运费模板配置
- **shipping_rule**: 运费规则表，存储运费规则（支持按地区设置不同运费）

### 7. 其他表

- **message**: 站内消息表，存储系统消息和用户消息
- **product_favorite**: 商品收藏表，存储用户收藏的商品
- **out_of_stock_registration**: 缺货登记表，记录用户缺货登记信息

## 索引说明

### 基础索引

所有表都包含以下基础索引：
- **主键索引（PRIMARY KEY）**: 所有表的主键
- **唯一索引（UNIQUE KEY）**: 用户名、邮箱、订单号、商品编码等唯一字段
- **普通索引（KEY）**: 外键字段、状态字段、时间字段等常用查询字段

### 复合索引

为了优化查询性能，还创建了以下复合索引：
- **订单表**: `idx_user_status_time`（用户ID+订单状态+创建时间）、`idx_status_time`（订单状态+创建时间）
- **商品表**: `idx_category_status_sales`（分类ID+状态+销量）
- **消息表**: `idx_receiver_read_time`（接收人ID+已读状态+创建时间）
- **预存款明细表**: `idx_user_type_time`（用户ID+类型+创建时间）
- **商品价格表**: `idx_product_level`（商品ID+用户等级）

## 使用说明

### 新建数据库

1. 执行 `chengren_shopping_mall-20251209.sql` 创建所有表结构和索引（完整数据库结构）

### 更新已有数据库

1. 按时间顺序执行所有更新脚本：
   - `update-20251209-add-logistics-tables.sql` - 添加物流管理相关表
   - 后续的更新脚本按日期顺序执行

### 注意事项

- 更新脚本必须按时间顺序执行
- 每个更新脚本都可以重复执行（幂等性）
- 执行前建议备份数据库

## 数据库更新规则

为了便于团队协作和版本管理，数据库脚本更新遵循以下规则：

### 1. 文件命名规范

- **完整数据库脚本**: `chengren_shopping_mall-YYYYMMDD.sql`（完整数据库结构，用于新建数据库）
- **更新脚本**: `update-YYYYMMDD-description.sql`（增量更新脚本，用于已有数据库的更新）

示例：
- `update-20251210-add-logistics-tables.sql` - 2025年12月10日添加物流管理表
- `update-20251215-add-product-tags.sql` - 2025年12月15日添加商品标签字段

### 2. 更新脚本编写规范

#### 2.1 基本原则

- ✅ **只增加，不删除**：所有更新脚本只能添加字段、表、索引等，不能删除已有字段
- ✅ **向后兼容**：新增字段必须设置默认值或允许NULL，确保不影响现有数据
- ✅ **幂等性**：脚本可以重复执行而不报错（使用 `IF NOT EXISTS` 等语句）

#### 2.2 脚本结构

每个更新脚本应包含以下部分：

```sql
-- ============================================
-- 更新脚本: update-YYYYMMDD-description.sql
-- 更新日期: YYYY-MM-DD
-- 更新说明: 简要说明本次更新的内容
-- 作者: 姓名
-- ============================================

-- 1. 添加新表（如果需要）
CREATE TABLE IF NOT EXISTS `new_table` (
  ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='新表说明';

-- 2. 添加新字段（使用 ALTER TABLE ADD COLUMN）
ALTER TABLE `existing_table` 
ADD COLUMN IF NOT EXISTS `new_column` VARCHAR(100) DEFAULT NULL COMMENT '新字段说明' AFTER `existing_column`;

-- 3. 添加新索引（如果需要）
CREATE INDEX IF NOT EXISTS `idx_new_index` ON `existing_table` (`column_name`);

-- 4. 更新数据（如果需要）
UPDATE `table_name` SET `column_name` = 'default_value' WHERE `column_name` IS NULL;
```

#### 2.3 字段添加规范

- **必须设置默认值或允许NULL**：新增字段必须设置 `DEFAULT` 值或允许 `NULL`，避免影响现有数据
- **使用 AFTER 指定位置**：新增字段时使用 `AFTER column_name` 指定字段位置，保持表结构清晰
- **添加注释**：所有新增字段必须添加 `COMMENT` 说明字段用途

示例：
```sql
-- ✅ 正确：允许NULL或设置默认值
ALTER TABLE `order` 
ADD COLUMN IF NOT EXISTS `new_field` VARCHAR(100) DEFAULT NULL COMMENT '新字段说明' AFTER `order_no`;

-- ✅ 正确：设置默认值
ALTER TABLE `order` 
ADD COLUMN IF NOT EXISTS `status_desc` VARCHAR(50) DEFAULT '' COMMENT '状态描述' AFTER `order_status`;

-- ❌ 错误：不允许NULL且无默认值（会影响现有数据）
ALTER TABLE `order` 
ADD COLUMN `new_field` VARCHAR(100) NOT NULL COMMENT '新字段说明';
```

#### 2.4 索引添加规范

- **使用 IF NOT EXISTS**：确保脚本可以重复执行
- **命名规范**：索引名称使用 `idx_` 前缀，如 `idx_user_status_time`
- **复合索引**：按查询频率和选择性排序字段

示例：
```sql
-- ✅ 正确：使用 IF NOT EXISTS
CREATE INDEX IF NOT EXISTS `idx_user_status_time` ON `order` (`user_id`, `order_status`, `create_time`);

-- ❌ 错误：不使用 IF NOT EXISTS（重复执行会报错）
CREATE INDEX `idx_user_status_time` ON `order` (`user_id`, `order_status`, `create_time`);
```

#### 2.5 表添加规范

- **使用 IF NOT EXISTS**：确保脚本可以重复执行
- **包含完整结构**：包括字段、索引、注释等
- **遵循命名规范**：表名使用下划线命名，如 `logistics_company`

示例：
```sql
-- ✅ 正确：使用 IF NOT EXISTS
CREATE TABLE IF NOT EXISTS `new_table` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '名称',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='新表说明';
```

### 3. 更新流程

1. **开发阶段**：
   - 在本地数据库进行开发和测试
   - 确认更新内容无误

2. **提交更新脚本**：
   - 创建新的更新脚本文件（按命名规范）
   - 在脚本开头添加更新说明（日期、作者、内容）
   - 提交到版本控制系统

3. **团队同步**：
   - 团队成员拉取最新代码
   - 按时间顺序执行更新脚本
   - 更新本地数据库

4. **生产环境部署**：
   - 在测试环境验证更新脚本
   - 备份生产数据库
   - 按顺序执行更新脚本
   - 验证数据完整性

### 4. 注意事项

- ⚠️ **禁止删除字段**：为了向后兼容，不允许删除已有字段。如需废弃字段，可以：
  - 在字段注释中标注"已废弃"
  - 在代码中不再使用该字段
  - 保留字段结构，避免影响其他同事的代码

- ⚠️ **禁止修改字段类型**：修改字段类型可能导致数据丢失或类型转换错误。如需修改：
  - 添加新字段（新类型）
  - 迁移数据到新字段
  - 更新代码使用新字段
  - 旧字段保留（标注已废弃）

- ⚠️ **禁止修改字段名**：修改字段名会影响现有代码。如需修改：
  - 添加新字段（新名称）
  - 迁移数据到新字段
  - 更新代码使用新字段
  - 旧字段保留（标注已废弃）

- ⚠️ **测试验证**：每次更新脚本提交前，必须在本地测试环境验证：
  - 脚本可以正常执行
  - 脚本可以重复执行（幂等性）
  - 不影响现有数据和功能

### 5. 更新脚本示例

- **实际更新脚本**: `update-20251209-add-logistics-tables.sql` - 添加物流管理相关表的完整示例
- **模板示例**: `update-example.sql` - 更新脚本的模板文件，包含各种常见场景的示例

**参考示例文件了解详细的编写规范**

## 注意事项

- 所有表都使用 `utf8mb4` 字符集，支持emoji等特殊字符
- 使用逻辑删除（deleted字段），不物理删除数据
- 时间字段使用 `DATETIME` 类型，自动维护创建和更新时间
- JSON字段用于存储复杂结构数据（地区、地址、图片数组等）
- 金额字段使用 `DECIMAL(10,2)` 类型，精确到分
- **数据库更新必须遵循更新规则，确保团队协作顺畅**

