# B2B成人用品采购系统技术架构设计文档

## 文档说明

- **系统名称**: B2B成人用品采购平台
- **技术栈**: SpringBoot + Vue3 + MySQL
- **架构模式**: 前后端分离
- **文档版本**: v1.0
- **创建日期**: 2025-12-04

## 目录

1. [技术栈选型](#1-技术栈选型)
2. [系统架构设计](#2-系统架构设计)
3. [模块划分](#3-模块划分)
4. [数据库设计](#4-数据库设计)
5. [前后端分离架构](#5-前后端分离架构)
6. [安全方案](#6-安全方案)
7. [性能优化方案](#7-性能优化方案)
8. [部署方案](#8-部署方案)
9. [开发规范](#9-开发规范)

---

## 1. 技术栈选型

### 1.1 后端技术栈

#### 核心框架
- **Spring Boot 3.x**: 主框架，提供自动配置、快速开发能力
- **Spring Security**: 安全框架，处理认证和授权
- **Spring Data JPA**: 数据持久化框架，简化数据库操作
- **MyBatis Plus**: ORM框架，提供强大的CRUD功能

#### 数据库相关
- **MySQL 8.0**: 主数据库，存储业务数据
- **MyBatis Plus**: 数据库操作增强工具

#### 缓存组件
- **Caffeine**: 本地缓存库，用于热点数据缓存（商品信息、用户信息等）
- **Spring Cache**: Spring缓存抽象，统一缓存接口

#### 工具类库
- **Hutool**: Java工具类库，提供常用工具方法
- **Lombok**: 简化Java代码，减少样板代码
- **Jackson**: JSON序列化/反序列化
- **Apache Commons**: 通用工具类库

#### 第三方集成
- **支付宝SDK**: 在线支付集成
- **微信支付SDK**: 微信支付集成
- **阿里云短信**: 短信验证服务
- **邮件服务**: 邮件发送（密码找回、通知等）

#### 文件存储
- **本地文件存储**: 服务器本地目录存储文件（商品图片、用户头像等）
- **Nginx静态文件服务**: 通过Nginx提供静态文件访问
- **文件路径管理**: 文件路径存储在数据库中

#### 其他组件
- **Swagger/OpenAPI**: API文档生成工具
- **Logback**: 日志框架
- **Quartz**: 定时任务调度
- **WebSocket**: 实时消息推送

### 1.2 前端技术栈

#### 核心框架
- **Vue 3.x**: 渐进式JavaScript框架，使用Composition API
- **TypeScript**: 类型安全的JavaScript超集
- **Vite**: 前端构建工具，提供快速开发体验

#### UI框架
- **Element Plus**: 基于Vue3的组件库
- **Vue Router 4.x**: 前端路由管理
- **Pinia**: 状态管理库（Vuex的替代品）

#### 工具库
- **Axios**: HTTP客户端，用于API请求
- **Day.js**: 日期处理库
- **ECharts**: 数据可视化图表库
- **VueUse**: Vue组合式函数工具集

#### 开发工具
- **ESLint**: 代码质量检查
- **Prettier**: 代码格式化
- **TypeScript**: 类型检查

### 1.3 开发工具

- **IDE**: IntelliJ IDEA / VS Code
- **版本控制**: Git
- **API测试**: Postman / Apifox
- **数据库管理**: Navicat / DBeaver
- **项目管理**: Maven / npm

---

## 2. 系统架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                     用户层                                │
│  ┌──────────────┐          ┌──────────────┐            │
│  │  采购者前端  │          │  管理后台前端 │            │
│  │  (Vue3)     │          │  (Vue3)      │            │
│  └──────────────┘          └──────────────┘            │
└─────────────────────────────────────────────────────────┘
                          │
                          │ HTTP/HTTPS
                          │
┌─────────────────────────────────────────────────────────┐
│                   网关层                                  │
│  ┌──────────────────────────────────────────────────┐  │
│  │          Nginx (反向代理/负载均衡)                 │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
                          │
┌─────────────────────────────────────────────────────────┐
│                   应用层                                  │
│  ┌──────────────────────────────────────────────────┐  │
│  │         Spring Boot 应用集群                      │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐      │  │
│  │  │ 应用实例1│  │ 应用实例2│  │ 应用实例N│      │  │
│  │  └──────────┘  └──────────┘  └──────────┘      │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
┌───────▼──────┐  ┌───────▼──────┐  ┌───────▼──────┐
│   MySQL      │  │  Caffeine    │  │  本地文件    │
│   主从复制    │  │  本地缓存     │  │   存储      │
└──────────────┘  └──────────────┘  └──────────────┘
```

### 2.2 分层架构

#### 2.2.1 后端分层

```
Controller层 (控制层)
    ↓
Service层 (业务逻辑层)
    ↓
Repository层 (数据访问层)
    ↓
Entity层 (实体层)
    ↓
Database (数据库)
```

**各层职责说明**:

- **Controller层**: 接收HTTP请求，参数校验，调用Service，返回响应
- **Service层**: 业务逻辑处理，事务管理，调用Repository
- **Repository层**: 数据访问，SQL执行，数据持久化
- **Entity层**: 数据模型，对应数据库表结构
- **DTO/VO层**: 数据传输对象，用于前后端数据交互

#### 2.2.2 前端分层

```
Views层 (页面组件)
    ↓
Components层 (公共组件)
    ↓
Stores层 (状态管理)
    ↓
Services层 (API服务)
    ↓
Utils层 (工具函数)
```

**各层职责说明**:

- **Views层**: 页面级组件，路由对应的页面
- **Components层**: 可复用的UI组件
- **Stores层**: 全局状态管理（用户信息、购物车等）
- **Services层**: API请求封装，统一处理请求/响应
- **Utils层**: 工具函数（日期格式化、数据验证等）

---

## 3. 模块划分

### 3.1 后端模块划分

基于Spring Boot的单模块Maven项目结构（适合小团队快速开发）：

```
shopping-mall-system/
├── pom.xml                        # Maven配置文件
├── src/
│   ├── main/
│   │   ├── java/com/shoppingmall/
│   │   │   ├── ShoppingMallApplication.java  # Spring Boot启动类
│   │   │   ├── common/            # 公共模块
│   │   │   │   ├── util/          # 核心工具类（日期、字符串、加密等）
│   │   │   │   ├── security/      # 安全相关（JWT、加密等）
│   │   │   │   ├── exception/     # 异常处理
│   │   │   │   └── config/        # 配置类（缓存、文件上传等）
│   │   │   ├── controller/        # 控制器层
│   │   │   │   ├── buyer/         # 采购者端API
│   │   │   │   └── admin/         # 管理端API
│   │   │   ├── service/           # 业务服务层
│   │   │   │   ├── user/          # 用户服务
│   │   │   │   ├── product/       # 商品服务
│   │   │   │   ├── order/         # 订单服务
│   │   │   │   ├── payment/       # 支付服务
│   │   │   │   ├── logistics/     # 物流服务
│   │   │   │   ├── marketing/    # 营销服务
│   │   │   │   └── statistics/    # 统计服务
│   │   │   ├── repository/        # 数据访问层
│   │   │   │   ├── user/          # 用户数据访问
│   │   │   │   ├── product/       # 商品数据访问
│   │   │   │   ├── order/         # 订单数据访问
│   │   │   │   └── ...
│   │   │   ├── entity/           # 实体类
│   │   │   ├── dto/               # 数据传输对象
│   │   │   └── vo/                # 视图对象
│   │   └── resources/
│   │       ├── application.yml    # 主配置文件
│   │       ├── application-dev.yml # 开发环境配置
│   │       ├── application-prod.yml # 生产环境配置
│   │       └── mapper/            # MyBatis Mapper XML
│   └── test/                      # 测试代码
└── docs/                          # 文档目录
```

**单模块结构的优势**：
- ✅ 结构简单，适合2人小团队快速开发
- ✅ 代码组织清晰，通过包结构实现分层
- ✅ 开发效率高，无需处理模块间依赖
- ✅ 构建部署简单，直接打包即可
- ✅ 后续如需扩展，可重构为多模块结构

### 3.2 前端模块划分

基于Vue3的项目结构：

```
shopping-mall-frontend/
├── src/
│   ├── api/                      # API接口定义
│   │   ├── buyer/                # 采购者端API
│   │   └── admin/                # 管理端API
│   ├── assets/                   # 静态资源
│   ├── components/               # 公共组件
│   │   ├── common/               # 通用组件
│   │   ├── layout/               # 布局组件
│   │   └── business/             # 业务组件
│   ├── views/                    # 页面组件
│   │   ├── buyer/                # 采购者端页面
│   │   └── admin/                # 管理端页面
│   ├── stores/                   # 状态管理
│   │   ├── user.ts               # 用户状态
│   │   ├── cart.ts               # 购物车状态
│   │   └── order.ts              # 订单状态
│   ├── router/                   # 路由配置
│   ├── utils/                    # 工具函数
│   ├── styles/                   # 样式文件
│   ├── types/                    # TypeScript类型定义
│   └── main.ts                   # 入口文件
├── public/                       # 公共文件
└── package.json
```

---

## 4. 数据库设计

### 4.1 数据库选型

- **主数据库**: MySQL 8.0
  - 支持事务、外键约束
  - 主从复制，读写分离
  - 支持JSON数据类型
  - 字符集: utf8mb4

- **本地缓存**: Caffeine
  - 缓存热点数据（商品信息、用户信息等）
  - 高性能本地内存缓存
  - 支持过期策略和大小限制
  - 适合单机部署场景

### 4.2 核心数据表设计

#### 4.2.1 用户相关表

**用户表 (sys_user)**
```sql
- id: 主键
- username: 用户名（唯一）
- email: 邮箱
- password: 密码（加密）
- real_name: 真实姓名
- gender: 性别
- phone: 手机号
- region: 地区（JSON格式存储省市区）
- address: 详细地址
- user_level: 用户等级（普通/VIP/金牌）
- status: 状态（待审核/已激活/已禁用）
- create_time: 创建时间
- update_time: 更新时间
```

**用户审核表 (sys_user_audit)**
```sql
- id: 主键
- user_id: 用户ID
- audit_status: 审核状态
- audit_comment: 审核意见
- auditor_id: 审核人ID
- audit_time: 审核时间
```

**收货地址表 (user_address)**
```sql
- id: 主键
- user_id: 用户ID
- receiver_name: 收货人姓名
- receiver_phone: 收货人电话
- province: 省份
- city: 城市
- district: 区县
- detail_address: 详细地址
- zip_code: 邮编
- is_default: 是否默认地址
- create_time: 创建时间
```

#### 4.2.2 商品相关表

**商品分类表 (product_category)**
```sql
- id: 主键
- parent_id: 父分类ID
- category_name: 分类名称
- level: 分类级别（1/2/3）
- sort_order: 排序
- status: 状态
```

**商品表 (product)**
```sql
- id: 主键
- product_code: 商品编码/SKU
- product_name: 商品名称
- category_id: 分类ID
- main_image: 主图URL
- images: 商品图片（JSON数组）
- description: 商品描述
- base_price: 基础批发价
- stock: 库存数量
- sales_count: 销量
- status: 状态（上架/下架）
- create_time: 创建时间
- update_time: 更新时间
```

**商品价格表 (product_price)**
```sql
- id: 主键
- product_id: 商品ID
- user_level: 用户等级
- price: 等级价格
- min_quantity: 最小数量（用于阶梯价格）
- max_quantity: 最大数量
```

**商品库存表 (product_stock)**
```sql
- id: 主键
- product_id: 商品ID
- available_stock: 可用库存
- locked_stock: 锁定库存（下单锁定）
- total_stock: 总库存
- warning_threshold: 预警阈值
```

#### 4.2.3 订单相关表

**订单表 (order)**
```sql
- id: 主键
- order_no: 订单号（唯一）
- user_id: 用户ID
- total_amount: 订单总金额
- shipping_fee: 运费
- tax: 税金
- actual_amount: 实付金额
- order_status: 订单状态
- payment_method: 支付方式
- payment_status: 支付状态
- shipping_address: 收货地址（JSON）
- order_remark: 订单备注
- delivery_date: 配送日期
- delivery_time: 配送时间段
- create_time: 创建时间
- pay_time: 支付时间
- ship_time: 发货时间
- complete_time: 完成时间
```

**订单商品表 (order_item)**
```sql
- id: 主键
- order_id: 订单ID
- product_id: 商品ID
- product_name: 商品名称（快照）
- product_image: 商品图片（快照）
- product_code: 商品编码（快照）
- price: 单价（快照）
- quantity: 数量
- subtotal: 小计金额
- weight: 商品重量
```

**订单物流表 (order_logistics)**
```sql
- id: 主键
- order_id: 订单ID
- logistics_company: 物流公司
- logistics_no: 物流单号
- shipping_time: 发货时间
- tracking_info: 物流跟踪信息（JSON）
```

#### 4.2.4 购物车表

**购物车表 (cart)**
```sql
- id: 主键
- user_id: 用户ID
- product_id: 商品ID
- quantity: 数量
- create_time: 创建时间
- update_time: 更新时间
```

#### 4.2.5 支付相关表

**预存款表 (pre_deposit)**
```sql
- id: 主键
- user_id: 用户ID
- balance: 余额
- available_balance: 可用余额
- frozen_balance: 冻结余额
- update_time: 更新时间
```

**预存款明细表 (pre_deposit_detail)**
```sql
- id: 主键
- user_id: 用户ID
- amount: 金额
- type: 类型（充值/消费/退款）
- status: 状态（待审核/已通过/已拒绝）
- payment_method: 支付方式
- payment_voucher: 支付凭证URL
- audit_time: 审核时间
- create_time: 创建时间
```

**支付记录表 (payment_record)**
```sql
- id: 主键
- order_id: 订单ID
- payment_no: 支付流水号
- payment_method: 支付方式
- amount: 支付金额
- payment_status: 支付状态
- payment_time: 支付时间
- callback_data: 回调数据（JSON）
```

#### 4.2.6 其他表

**站内消息表 (message)**
```sql
- id: 主键
- sender_id: 发送人ID
- receiver_id: 接收人ID
- title: 消息标题
- content: 消息内容
- message_type: 消息类型
- is_read: 是否已读
- create_time: 创建时间
```

**商品收藏表 (product_favorite)**
```sql
- id: 主键
- user_id: 用户ID
- product_id: 商品ID
- create_time: 创建时间
```

**缺货登记表 (out_of_stock_registration)**
```sql
- id: 主键
- user_id: 用户ID
- product_id: 商品ID
- notify_when_available: 到货通知
- create_time: 创建时间
```

### 4.3 数据库优化策略

1. **索引优化**
   - 主键索引
   - 唯一索引（username, email, order_no等）
   - 普通索引（user_id, product_id, order_status等）
   - 复合索引（根据查询场景）

2. **分表策略**
   - 订单表按时间分表（按月/年）
   - 订单明细表按订单ID分表

3. **读写分离**
   - 主库：写操作
   - 从库：读操作
   - 使用ShardingSphere或MyCat实现

4. **缓存策略**
   - Caffeine本地缓存热点数据（商品信息、用户信息）
   - 查询结果缓存（商品列表、分类树）
   - 缓存失效策略（主动失效、定时失效）
   - 缓存大小限制和过期时间配置

**Caffeine本地缓存配置示例**:
```java
@Configuration
public class CacheConfig {
    @Bean
    public Cache<String, Object> localCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000)                    // 最大缓存条目数
            .expireAfterWrite(10, TimeUnit.MINUTES) // 写入后10分钟过期
            .expireAfterAccess(5, TimeUnit.MINUTES)  // 访问后5分钟过期
            .recordStats()                          // 启用统计
            .build();
    }
}

// 使用示例
@Service
public class ProductService {
    @Autowired
    private Cache<String, Object> cache;
    
    public Product getProduct(Long id) {
        String key = "product:" + id;
        return (Product) cache.get(key, k -> {
            return productRepository.findById(id);
        });
    }
}
```

**缓存使用场景**:
- 商品信息缓存（商品详情、价格等）
- 用户信息缓存（用户基本信息）
- 商品分类树缓存
- 热点查询结果缓存

**注意事项**:
- 本地缓存仅适用于单机部署
- 多实例部署时，缓存不共享
- 应用重启后缓存会丢失
- 适合缓存读多写少的数据

5. **文件存储策略**
   - 本地文件存储（服务器目录）
   - 文件目录结构规划（按类型、日期分类）
   - 文件访问路径管理（相对路径存储）
   - 文件备份机制（定期备份）

**本地文件存储配置示例**:
```yaml
# application.yml
file:
  upload:
    path: /data/uploads              # 文件存储根目录
    max-size: 10MB                   # 单文件最大大小
    allowed-types: jpg,jpeg,png,gif,pdf  # 允许的文件类型
    image-path: /data/uploads/images  # 图片存储路径
    document-path: /data/uploads/documents  # 文档存储路径
```

**文件目录结构**:
```
/data/uploads/
├── images/                    # 图片文件
│   ├── products/              # 商品图片
│   │   ├── 2025/
│   │   │   └── 12/
│   │   └── ...
│   └── avatars/               # 用户头像
├── documents/                  # 文档文件
└── temp/                      # 临时文件
```

**Nginx静态文件配置**:
```nginx
# Nginx配置
location /uploads/ {
    alias /data/uploads/;
    expires 30d;                    # 缓存30天
    add_header Cache-Control "public, immutable";
    
    # 文件类型限制
    location ~* \.(jpg|jpeg|png|gif)$ {
        access_log off;
    }
}
```

**数据库存储文件路径**:
```sql
-- 商品表存储相对路径
product.main_image: /uploads/images/products/2025/12/product_001.jpg

-- 或存储文件名，路径通过配置获取
product.main_image: product_001.jpg
```

---

## 5. 前后端分离架构

### 5.1 接口设计规范

#### 5.1.1 RESTful API设计

**URL规范**:
```
GET    /api/buyer/products          # 获取商品列表
GET    /api/buyer/products/{id}     # 获取商品详情
POST   /api/buyer/orders            # 创建订单
PUT    /api/buyer/orders/{id}       # 更新订单
DELETE /api/buyer/orders/{id}       # 删除订单
```

**HTTP状态码**:
- 200: 成功
- 201: 创建成功
- 400: 请求参数错误
- 401: 未授权
- 403: 无权限
- 404: 资源不存在
- 500: 服务器错误

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2025-12-04T10:00:00"
}
```

#### 5.1.2 统一响应封装

**后端响应类**:
```java
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
}
```

**前端响应拦截**:
```typescript
axios.interceptors.response.use(
  response => {
    if (response.data.code === 200) {
      return response.data.data;
    } else {
      // 处理业务错误
      return Promise.reject(response.data);
    }
  },
  error => {
    // 处理HTTP错误
    return Promise.reject(error);
  }
);
```

### 5.2 跨域处理

**后端配置**:
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // 配置跨域规则
    }
}
```

**前端代理配置** (Vite):
```javascript
export default {
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}
```

### 5.3 认证授权

#### 5.3.1 JWT Token认证

**Token生成**:
```java
// 生成JWT Token
String token = JwtUtil.generateToken(userId, username);
```

**Token存储**:
- 前端: localStorage 或 sessionStorage
- 后端: JWT无状态Token（如需黑名单，可使用数据库表存储）

**Token传递**:
```typescript
// 请求拦截器添加Token
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

#### 5.3.2 权限控制

**后端权限注解**:
```java
@PreAuthorize("hasRole('BUYER')")
@GetMapping("/api/buyer/orders")
public Result<List<Order>> getOrders() {
    // ...
}
```

**前端路由守卫**:
```typescript
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  if (to.meta.requiresAuth && !token) {
    next('/login');
  } else {
    next();
  }
});
```

---

## 6. 安全方案

### 6.1 数据安全

1. **密码加密**
   - 使用BCrypt加密存储
   - 密码强度验证
   - 禁止明文传输

2. **SQL注入防护**
   - 使用参数化查询
   - MyBatis使用#{}而非${}
   - 输入参数校验

3. **XSS防护**
   - 输入数据转义
   - 输出数据编码
   - 使用CSP（内容安全策略）

4. **CSRF防护**
   - Token验证
   - SameSite Cookie
   - 验证Referer

### 6.2 接口安全

1. **接口限流**
   - 使用内存计数器实现限流（单机场景）
   - 防止接口被恶意调用
   - 基于IP或用户ID限流
   - 如需分布式限流，可后续引入Redis

2. **参数校验**
   - 使用Bean Validation
   - 前后端双重校验
   - 防止非法参数

3. **敏感信息脱敏**
   - 日志中脱敏
   - 接口返回脱敏
   - 手机号、邮箱等脱敏

### 6.3 文件上传安全

1. **文件类型校验**
   - 白名单机制
   - 文件扩展名检查
   - MIME类型检查

2. **文件大小限制**
   - 单文件大小限制
   - 总文件大小限制

3. **文件存储**
   - 本地文件存储（服务器目录）
   - 文件访问权限控制（Nginx配置）
   - 防止文件被恶意访问（路径校验、文件类型限制）
   - 文件备份机制（定期备份重要文件）

---

## 7. 性能优化方案

### 7.1 后端性能优化

1. **数据库优化**
   - 索引优化
   - 查询优化（避免N+1查询）
   - 连接池配置
   - 慢查询监控

2. **缓存策略**
   - Caffeine本地缓存热点数据
   - 缓存预热（应用启动时加载热点数据）
   - 缓存穿透防护（空值缓存）
   - 缓存击穿防护（互斥锁）
   - 缓存雪崩防护（过期时间随机化）

3. **异步处理**
   - 使用@Async处理异步任务
   - 消息队列处理耗时操作
   - 邮件发送异步化
   - 日志记录异步化

4. **连接池优化**
   - 数据库连接池（HikariCP）
   - HTTP连接池

### 7.2 前端性能优化

1. **代码优化**
   - 代码分割（Code Splitting）
   - 懒加载路由
   - 组件懒加载
   - Tree Shaking

2. **资源优化**
   - 图片压缩
   - 图片懒加载
   - CDN加速
   - 静态资源缓存

3. **请求优化**
   - 请求合并
   - 防抖节流
   - 请求缓存
   - 取消重复请求

4. **渲染优化**
   - 虚拟滚动（长列表）
   - 虚拟列表
   - 使用v-show替代v-if（频繁切换）
   - 计算属性缓存

### 7.3 系统性能指标

- **响应时间**: API响应时间 < 200ms（95%）
- **并发支持**: 支持1000+并发用户
- **数据库**: 查询响应 < 100ms
- **缓存命中率**: > 80%

---

## 8. 部署方案

### 8.1 部署架构

```
                    ┌─────────────┐
                    │   Nginx     │
                    │  (负载均衡)  │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐       ┌────▼────┐       ┌────▼────┐
   │ 应用实例1│       │ 应用实例2│       │ 应用实例N│
   │:8080    │       │:8081    │       │:8082    │
   └────┬────┘       └────┬────┘       └────┬────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐       ┌────▼────┐       ┌────▼────┐
   │ MySQL   │       │Caffeine │       │本地文件 │
   │ 主从    │       │本地缓存  │       │ 存储    │
   └─────────┘       └─────────┘       └─────────┘
```

### 8.2 部署步骤

#### 8.2.1 后端部署

1. **打包应用**
   ```bash
   mvn clean package -DskipTests
   ```

2. **运行应用**
   ```bash
   java -jar shopping-mall-system.jar
   ```

3. **使用Docker部署**（推荐）
   ```dockerfile
   FROM openjdk:17-jre-slim
   COPY target/shopping-mall-system.jar app.jar
   ENTRYPOINT ["java", "-jar", "/app.jar"]
   ```

#### 8.2.2 前端部署

1. **构建项目**
   ```bash
   npm run build
   ```

2. **部署到Nginx**
   - 将dist目录内容部署到Nginx
   - 配置Nginx反向代理

3. **Nginx配置示例**
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       location / {
           root /usr/share/nginx/html;
           index index.html;
           try_files $uri $uri/ /index.html;
       }
       
       location /api {
           proxy_pass http://backend:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

### 8.3 环境配置

**开发环境**:
- 本地开发
- 热更新
- 调试模式

**测试环境**:
- 独立测试服务器
- 测试数据库
- 测试数据

**生产环境**:
- 生产服务器
- 主从数据库
- 本地缓存（Caffeine）
- 负载均衡（可选，单机部署时不需要）
- 监控告警

---

## 9. 开发规范

### 9.1 代码规范

#### 9.1.1 Java代码规范

- 遵循阿里巴巴Java开发手册
- 使用统一的代码格式化配置
- 类名使用大驼峰（PascalCase）
- 方法名、变量名使用小驼峰（camelCase）
- 常量使用大写下划线（UPPER_SNAKE_CASE）

#### 9.1.2 TypeScript/Vue代码规范

- 遵循Vue官方风格指南
- 使用ESLint + Prettier
- 组件名使用PascalCase
- 文件名使用kebab-case
- 使用TypeScript严格模式

### 9.2 接口规范

1. **统一响应格式**
2. **统一错误码**
3. **统一参数校验**
4. **API文档完善**

### 9.3 数据库规范

1. **表命名**: 小写下划线（snake_case）
2. **字段命名**: 小写下划线
3. **索引命名**: idx_表名_字段名
4. **外键命名**: fk_表名_字段名

### 9.4 Git规范

1. **分支管理**
   - master: 生产环境
   - develop: 开发环境
   - feature/*: 功能分支
   - hotfix/*: 热修复分支

2. **提交规范**
   ```
   feat: 新功能
   fix: 修复bug
   docs: 文档更新
   style: 代码格式调整
   refactor: 代码重构
   test: 测试相关
   chore: 构建/工具相关
   ```

---

## 10. 技术难点与解决方案

### 10.1 库存并发控制

**问题**: 高并发下库存扣减可能出现超卖

**解决方案**:
1. 数据库乐观锁（version字段，推荐）
2. 数据库行锁（SELECT FOR UPDATE）
3. 库存预扣机制
4. 如需多实例部署，可后续引入Redis分布式锁

### 10.2 订单号生成

**问题**: 保证订单号唯一性、有序性

**解决方案**:
1. 数据库自增ID + 业务前缀（推荐，简单可靠）
2. 雪花算法（Snowflake，分布式场景）
3. 数据库序列（Sequence）
4. UUID（不推荐，无序）

### 10.3 支付回调处理

**问题**: 支付回调的幂等性和安全性

**解决方案**:
1. 回调幂等性校验（订单状态检查）
2. 签名验证
3. 异步处理回调
4. 回调日志记录

### 10.4 文件上传

**问题**: 大文件上传、断点续传

**解决方案**:
1. 分片上传（大文件分片上传到服务器）
2. 断点续传（支持上传中断后继续）
3. 文件上传到服务器本地目录
4. 上传进度显示
5. 文件路径存储在数据库
6. 通过Nginx提供静态文件访问

---

## 11. 监控与日志

### 11.1 系统监控

1. **应用监控**
   - Spring Boot Actuator
   - Prometheus + Grafana
   - 应用性能监控（APM）

2. **数据库监控**
   - 慢查询监控
   - 连接数监控
   - 性能指标监控

3. **服务器监控**
   - CPU、内存、磁盘监控
   - 网络监控
   - 告警通知

### 11.2 日志管理

1. **日志级别**
   - ERROR: 错误日志
   - WARN: 警告日志
   - INFO: 信息日志
   - DEBUG: 调试日志

2. **日志存储**
   - 本地文件存储
   - ELK（Elasticsearch + Logstash + Kibana）
   - 日志轮转

3. **日志内容**
   - 请求日志（请求参数、响应结果）
   - 业务日志（关键操作）
   - 异常日志（异常堆栈）

---

## 12. 总结

本技术架构设计文档基于SpringBoot + Vue3 + MySQL技术栈，采用前后端分离架构，支持B2B成人用品采购平台的核心业务需求。

**核心特点**:
- ✅ 前后端分离，职责清晰
- ✅ 单模块设计，结构简单，适合小团队快速开发
- ✅ 通过包结构实现分层，代码组织清晰
- ✅ 安全可靠，性能优化
- ✅ 可扩展，可维护
- ✅ 标准化开发流程

**后续优化方向**:
- 引入Redis缓存（当需要多实例部署或分布式缓存时）
- 迁移到OSS对象存储（当文件数量大或需要高可用时）
- 消息队列集成（RabbitMQ/Kafka）
- 搜索引擎集成（Elasticsearch）
- 容器化部署（Docker + K8s）
- 重构为多模块结构（当团队规模扩大或需要更细粒度管理时）
- 微服务化改造（Spring Cloud，当系统规模扩大时）

---

## 附录

### A. 技术栈版本

- Spring Boot: 3.1.x
- Vue: 3.3.x
- MySQL: 8.0.x
- Caffeine: 3.x
- JDK: 17
- Node.js: 18.x

### B. 参考文档

- Spring Boot官方文档
- Vue3官方文档
- MySQL官方文档
- Element Plus文档

---

**文档版本**: v1.0  
**最后更新**: 2025-12-04  
**维护人员**: 开发团队

