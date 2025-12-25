# 修改日志

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
