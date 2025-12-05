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

### 6. 其他表

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

1. 执行 `schema.sql` 创建所有表结构和索引
2. 执行 `init_data.sql` 初始化基础数据

## 注意事项

- 所有表都使用 `utf8mb4` 字符集，支持emoji等特殊字符
- 使用逻辑删除（deleted字段），不物理删除数据
- 时间字段使用 `DATETIME` 类型，自动维护创建和更新时间
- JSON字段用于存储复杂结构数据（地区、地址、图片数组等）
- 金额字段使用 `DECIMAL(10,2)` 类型，精确到分

