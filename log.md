## 2025-12-14 - 修复会员价格计算：user_level直接关联member_level.id

### 功能说明
修复会员价格计算逻辑，使 `sys_user.user_level` 字段直接存储 `member_level.id`（会员等级ID），实现正确的关联关系。

### 问题分析
之前的实现使用 `sort_order` 来映射，但这种方式存在问题：
- `member_level` 表中的等级是动态配置的，可能有任意数量（如4个等级：普卡、银卡、金卡、钻石）
- 通过 `sort_order` 映射不够直观，且依赖于排序规则
- 最合理的关联方式应该是直接通过 `member_level.id` 关联

### 修改方案
改为直接通过 `member_level.id` 关联：
- `sys_user.user_level` 字段直接存储 `member_level.id`（会员等级ID）
- 如果 `user_level` 为 null 或 0，则使用默认等级（第一个等级，通常是 id=1 的普卡会员）
- 根据 `user_level` 值在 `member_level` 表中查找对应的等级记录

### 修改文件
1. `backend/src/main/java/com/shoppingmall/service/buyer/impl/CartServiceImpl.java` - 修复关联逻辑
2. `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java` - 修复关联逻辑

### 具体修改
- **移除sort_order映射**：不再使用 `sort_order` 来映射
- **直接通过ID关联**：`sys_user.user_level` 直接存储 `member_level.id`，通过ID查找对应的会员等级
- **容错处理**：如果 `user_level` 为 null 或 0，或找不到匹配的等级，使用第一个等级（默认）；如果还是没有找到，返回原价

### 功能特性
- ✅ 直接通过 `member_level.id` 关联，关系清晰明确
- ✅ 支持任意数量的会员等级（如4个：普卡、银卡、金卡、钻石）
- ✅ 不依赖于 `sort_order`，更灵活
- ✅ 容错处理完善，确保系统稳定运行
- ✅ 符合数据库设计最佳实践

### 技术细节
- **关联方式**：`sys_user.user_level` = `member_level.id`
- **查找逻辑**：遍历会员等级列表，查找 `id` 匹配的等级
- **默认处理**：如果 `user_level` 为 null 或 0，或找不到匹配等级，使用第一个等级（默认）
- **数据库配置**：根据 `member_level` 表配置，当前有4个等级（id: 1=普卡, 2=银卡, 3=金卡, 4=钻石）

### 数据库说明
根据 `member_level` 表配置：
- id=1: 普卡会员（折扣率 100）
- id=2: 银卡会员（折扣率 95）
- id=3: 金卡会员（折扣率 90）
- id=4: 钻石会员（折扣率 80）

`sys_user.user_level` 应存储这些 id 值（1, 2, 3, 4），系统会根据这些值查找对应的会员等级并应用相应的折扣率。

---

## 2025-12-14 - 修改会员价格计算逻辑，使用会员等级折扣率

### 功能说明
修改会员价格字段的计算逻辑，改为根据会员等级表的折扣率来计算会员价格。不同会员等级可能有不同的折扣率，价格保留两位小数。

### 修改文件

#### 后端
1. `backend/src/main/java/com/shoppingmall/service/buyer/impl/CartServiceImpl.java` - 修改购物车会员价格计算逻辑
2. `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java` - 修改订单会员价格计算逻辑

### 具体修改

#### 1. CartServiceImpl 修改
- **添加依赖**：注入 `MemberLevelService` 用于获取会员等级信息
- **移除旧逻辑**：移除 `getPriceByUserLevel` 和 `convertUserLevelToString` 方法（不再使用价格表）
- **新增方法**：添加 `calculateMemberPrice` 方法，根据会员等级折扣率计算会员价格
- **计算逻辑**：
  - 获取所有启用的会员等级（按排序号排序）
  - 根据用户的 `userLevel`（0,1,2）映射到会员等级列表的索引
  - 如果索引超出范围，使用第一个等级（默认）
  - 使用公式：`会员价格 = 销售价格 × (折扣率 / 100.00)`
  - 保留两位小数（使用 `setScale(2, BigDecimal.ROUND_HALF_UP)`）

#### 2. OrderServiceImpl 修改
- **添加依赖**：注入 `MemberLevelService` 用于获取会员等级信息
- **移除旧逻辑**：移除 `getPriceByUserLevel` 和 `convertUserLevelToString` 方法
- **新增方法**：添加 `calculateMemberPrice` 方法，与 CartServiceImpl 中的逻辑一致
- **价格计算**：在创建订单时，使用新的会员价格计算逻辑
- **金额计算**：订单金额和小计都保留两位小数

### 功能特性
- ✅ 会员价格根据会员等级表的折扣率动态计算
- ✅ 不同会员等级享受不同的折扣率
- ✅ 价格统一保留两位小数
- ✅ 购物车和订单使用相同的计算逻辑
- ✅ 异常处理完善，计算失败时返回原价

### 技术细节
- **计算公式**：`会员价格 = 销售价格 × (折扣率 / 100.00)`
- **折扣率说明**：95.00 表示 95折，100.00 表示无折扣
- **用户等级映射**：userLevel (0,1,2) 映射到会员等级列表的索引 (0,1,2)
- **精度控制**：使用 `BigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP)` 保留两位小数
- **默认处理**：如果没有会员等级或计算失败，返回原价（销售价格）

### 影响范围
- ✅ 购物车页面（`/cart`）：会员价格根据折扣率计算
- ✅ 结算页面（`/cart/checkout`）：会员价格根据折扣率计算
- ✅ 订单创建：订单中的商品价格根据折扣率计算
- ✅ 所有涉及会员价格的地方都统一使用新的计算逻辑

### 影响
- ✅ 会员价格计算更加灵活，可以根据会员等级动态调整
- ✅ 不再依赖商品价格表（product_price），简化了价格管理
- ✅ 价格计算统一，确保购物车和订单价格一致
- ✅ 支持不同会员等级享受不同折扣，提升用户体验

---

## 2025-12-14 - 创建聚水潭ERP发货业务对接方案文档

### 功能说明
创建聚水潭ERP发货业务对接方案文档，详细说明如何将当前系统的订单发货业务与聚水潭ERP平台对接。

### 创建文件

#### 文档
1. `docs/聚水潭ERP发货业务对接方案.md` - 完整的对接方案文档

### 文档内容

#### 1. 对接概述
- 对接目标：订单推送、发货回调、状态同步
- 对接方式：系统主动推送订单，ERP回调通知发货
- 参考文档链接

#### 2. 需要创建的文件
- 数据库表：ERP订单同步记录表
- 实体类：ErpOrderSync
- DTO类：JushuitanOrderDTO、JushuitanShipCallbackDTO
- Repository：ErpOrderSyncRepository
- 服务类：JushuitanService接口和实现
- 控制器：JushuitanController
- 配置类：JushuitanConfig

#### 3. 需要修改的文件
- 订单服务：在支付成功后推送订单，在发货时检查ERP状态
- 配置文件：添加聚水潭ERP配置项
- 系统配置表：添加ERP相关配置

#### 4. 数据流程设计
- 订单推送流程：支付成功 → 检查配置 → 创建同步记录 → 推送订单 → 更新状态
- 发货回调流程：ERP发货 → 回调接口 → 验证签名 → 更新订单状态和物流信息
- 重试机制：失败订单自动重试，支持定时任务处理

#### 5. 技术实现细节
- HTTP客户端配置
- 签名生成和验证
- 数据格式转换
- 异常处理和重试机制
- 定时任务设计

#### 6. 配置说明
- 系统配置项说明
- 聚水潭平台配置要求

#### 7. 接口设计
- 订单推送接口（系统 → 聚水潭）
- 发货回调接口（聚水潭 → 系统）

#### 8. 数据库设计
- ERP订单同步记录表结构
- 字段说明和索引设计

#### 9. 实现步骤
- 分5个阶段详细说明实现步骤

#### 10. 注意事项
- 数据一致性
- 安全性
- 性能优化
- 错误处理
- 测试建议

#### 11. 后续扩展
- 订单状态同步
- 库存同步
- 商品同步

### 功能特性
- ✅ 完整的对接方案文档
- ✅ 详细的实现步骤说明
- ✅ 数据流程设计
- ✅ 技术实现细节
- ✅ 配置和接口设计
- ✅ 注意事项和测试建议

### 技术细节
- 文档包含完整的对接方案
- 涵盖数据库设计、代码结构、接口设计等
- 提供分阶段实现步骤
- 包含注意事项和最佳实践

### 影响
- ✅ 为聚水潭ERP对接提供完整的实施方案
- ✅ 开发人员可以根据文档进行开发
- ✅ 包含详细的实现步骤和注意事项
- ✅ 为后续扩展功能提供参考

---

## 2025-12-14 - 屏蔽等级管理中的积分相关功能

### 功能说明
屏蔽等级管理中的积分相关字段和功能，包括积分区间、最低积分、最高积分等，因为业务上不再需要积分功能。

### 修改文件

#### 前端
1. `admin-frontend/src/views/buyer/Level.vue` - 屏蔽积分相关字段的显示和输入

#### 后端
1. `backend/src/main/java/com/shoppingmall/dto/MemberLevelDTO.java` - 移除积分字段的验证注解
2. `backend/src/main/java/com/shoppingmall/service/member/impl/MemberLevelServiceImpl.java` - 移除积分区间验证逻辑和积分区间显示文本生成
3. `backend/src/main/java/com/shoppingmall/controller/admin/MemberLevelController.java` - 屏蔽根据积分获取会员等级的接口

### 具体修改

#### 1. 前端页面修改
- **列表页**：屏蔽"积分区间"列的显示
- **表单页**：屏蔽"最低积分"和"最高积分"输入框
- **表单验证**：移除积分字段的验证规则
- **表单数据**：保留字段但不再使用（避免类型错误）

#### 2. 后端DTO修改
- **移除验证注解**：注释掉 `minPoints` 和 `maxPoints` 字段的 `@NotNull` 和 `@Min` 验证注解
- **保留字段**：保留字段定义，避免数据库表结构变更

#### 3. 后端Service修改
- **移除积分区间验证**：注释掉 `validatePointsRange` 方法的调用
- **设置默认值**：在创建和更新时，自动设置 `minPoints = 0` 和 `maxPoints = null`
- **移除积分区间显示文本**：注释掉积分区间显示文本的生成逻辑，设置为空字符串

#### 4. 后端Controller修改
- **屏蔽接口**：注释掉 `getMemberLevelByPoints` 接口（根据积分获取会员等级）

### 功能特性
- ✅ 前端不再显示积分相关字段
- ✅ 前端不再要求输入积分相关数据
- ✅ 后端不再验证积分区间
- ✅ 后端不再生成积分区间显示文本
- ✅ 根据积分获取会员等级的接口已屏蔽
- ✅ 保留字段定义，避免数据库表结构变更

### 技术细节
- 使用注释方式屏蔽功能，便于后续恢复
- 保留字段定义，避免类型错误和数据库表结构变更
- 设置默认值（minPoints=0, maxPoints=null），确保数据一致性
- 积分区间显示文本设置为空字符串，避免前端显示异常

### 影响
- ✅ 等级管理功能简化，不再依赖积分
- ✅ 界面更加简洁，移除了不需要的积分字段
- ✅ 代码逻辑更加清晰，专注于折扣率等核心功能
- ✅ 为后续可能的积分功能恢复预留了空间（通过注释）

---

## 2025-12-14 - 实现订单问题管理功能

### 功能说明
实现完整的订单问题管理功能，包括用户端订单问题提交和管理端订单问题查询、处理功能。

### 创建文件

#### 数据库
1. `database/update-20251214-create-order-message-table.sql` - 创建订单问题表
2. `database/update-20251214-add-order-message-menu.sql` - 添加订单问题菜单和权限

#### 后端
1. `backend/src/main/java/com/shoppingmall/entity/OrderMessage.java` - 订单问题实体类
2. `backend/src/main/java/com/shoppingmall/dto/OrderMessageDTO.java` - 订单问题提交DTO
3. `backend/src/main/java/com/shoppingmall/dto/OrderMessageQueryDTO.java` - 订单问题查询DTO
4. `backend/src/main/java/com/shoppingmall/dto/OrderMessageHandleDTO.java` - 订单问题处理DTO
5. `backend/src/main/java/com/shoppingmall/vo/OrderMessageVO.java` - 订单问题VO
6. `backend/src/main/java/com/shoppingmall/repository/order/OrderMessageRepository.java` - 订单问题Repository
7. `backend/src/main/java/com/shoppingmall/service/buyer/OrderMessageService.java` - 用户端订单问题服务接口
8. `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderMessageServiceImpl.java` - 用户端订单问题服务实现
9. `backend/src/main/java/com/shoppingmall/service/admin/OrderMessageService.java` - 管理端订单问题服务接口
10. `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderMessageServiceImpl.java` - 管理端订单问题服务实现
11. `backend/src/main/java/com/shoppingmall/controller/buyer/OrderMessageController.java` - 用户端订单问题控制器
12. `backend/src/main/java/com/shoppingmall/controller/admin/OrderMessageController.java` - 管理端订单问题控制器

#### 前端
1. `frontend/src/api/buyer/order-message.ts` - 用户端订单问题API
2. `admin-frontend/src/api/admin/orderMessage.ts` - 管理端订单问题API
3. `admin-frontend/src/views/order/OrderMessage.vue` - 管理端订单问题列表页面

### 修改文件

#### 前端
1. `frontend/src/views/order/OrderMessage.vue` - 对接后端API，实现订单问题提交
2. `admin-frontend/src/router/componentMaps/order.ts` - 添加订单问题页面组件映射

### 功能特性

#### 1. 用户端功能
- **提交订单问题**：支持两种类型的问题提交
  - **我已付款**：提交付款金额、付款方式、付款时间等信息
  - **我有问题**：提交问题标题和内容
- **数据验证**：完整的表单验证，确保必填字段已填写
- **订单验证**：验证订单是否存在且属于当前用户

#### 2. 管理端功能
- **订单问题列表**：分页查询订单问题，支持多条件筛选
  - 按订单号搜索
  - 按问题类型筛选（我已付款/我有问题）
  - 按处理状态筛选（待处理/处理中/已处理/已关闭）
  - 按日期范围筛选
- **问题详情查看**：查看订单问题的完整信息
- **问题处理**：管理员可以处理订单问题
  - 设置处理状态（处理中/已处理/已关闭）
  - 添加处理备注
  - 记录处理人和处理时间

### 数据库设计

#### order_message 表结构
- `id` - 主键ID
- `order_no` - 订单号
- `user_id` - 用户ID
- `message_type` - 消息类型（1-我已付款，2-我有问题）
- `title` - 问题标题（我有问题时必填）
- `content` - 问题内容（我有问题时必填）
- `payment_amount` - 付款金额（我已付款时必填）
- `payment_method` - 付款方式（我已付款时必填）
- `payment_date` - 付款日期（我已付款时必填）
- `payment_time` - 付款时间（我已付款时必填）
- `remarks` - 备注
- `status` - 处理状态（0-待处理，1-处理中，2-已处理，3-已关闭）
- `handler_id` - 处理人ID（管理员）
- `handler_name` - 处理人姓名
- `handle_time` - 处理时间
- `handle_remark` - 处理备注
- `create_time` - 创建时间
- `update_time` - 更新时间

### API接口

#### 用户端
- **POST /api/buyer/order-messages** - 创建订单问题/消息

#### 管理端
- **GET /api/admin/order-messages/page** - 分页查询订单问题列表
- **GET /api/admin/order-messages/{id}** - 获取订单问题详情
- **PUT /api/admin/order-messages/handle** - 处理订单问题

### 技术细节
- 使用 MyBatis-Plus 进行数据访问
- 前后端分离架构，RESTful API 设计
- 完整的表单验证和错误处理
- 支持分页查询和多条件筛选
- 状态管理：待处理 -> 处理中 -> 已处理/已关闭

### 菜单配置
需要在数据库的菜单表中添加"订单问题"菜单项：
- 父菜单：订单管理
- 菜单名称：订单问题
- 组件路径：order/OrderMessage
- 菜单类型：二级菜单

### 影响
- ✅ 用户可以通过订单详情页提交订单问题
- ✅ 管理员可以查看和处理所有订单问题
- ✅ 完善了订单问题处理流程
- ✅ 提升了客户服务质量

---

## 2025-12-13 - 修复预存款余额列表订单号跳转问题

### 功能说明
修复预存款余额列表页面（`/member/deposit/balance`）中，当事件类型为"预存款支付"时，点击事件无法跳转到订单详情页面的问题。之前提示"无法获取订单号"。

### 修改文件

#### 后端
1. `backend/src/main/java/com/shoppingmall/vo/DepositRecordVO.java` - 添加 orderNo 字段
2. `backend/src/main/java/com/shoppingmall/service/buyer/impl/DepositServiceImpl.java` - 在转换方法中赋值 orderNo

#### 前端
1. `frontend/src/api/buyer/deposit.ts` - 在 DepositRecordVO 接口中添加 orderNo 字段
2. `frontend/src/views/member/DepositBalance.vue` - 修复订单号提取逻辑，优先使用 orderNo 字段

### 具体修改

#### 问题分析
- **缺少订单号字段**：后端的 `DepositRecordVO` 没有包含 `orderNo` 字段，导致前端无法直接获取订单号
- **正则表达式不匹配**：前端的正则表达式 `/订单号\((\d+)\)/` 匹配的是圆括号格式，但后端备注使用的是花括号格式 `订单号{xxx}`
- **数据获取方式不当**：前端只能从备注中提取订单号，但备注格式可能变化，不够可靠

#### 修复方案

##### 1. 后端添加订单号字段
- **DepositRecordVO**：添加 `orderNo` 字段，用于直接返回订单号
- **转换方法**：在 `convertToRecordVO` 方法中，将 `PreDepositDetail` 的 `orderNo` 赋值给 VO

##### 2. 前端优化订单号获取逻辑
- **优先使用 orderNo 字段**：如果记录中有 `orderNo` 字段，直接使用
- **备用方案**：如果没有 `orderNo` 字段，则从备注中提取（支持花括号和圆括号两种格式）
- **正则表达式优化**：修改为 `/订单号[{(](\d+)[})]/`，同时支持 `订单号{xxx}` 和 `订单号(xxx)` 两种格式

### 功能特性
- ✅ 预存款支付事件点击可直接跳转到订单详情页面
- ✅ 预存款退款事件点击可直接跳转到订单详情页面
- ✅ 优先使用 orderNo 字段，更可靠
- ✅ 支持从备注中提取订单号作为备用方案
- ✅ 兼容花括号和圆括号两种备注格式

### 技术细节
- 后端 VO 添加 `orderNo` 字段，类型为 `String`
- 前端接口类型定义添加 `orderNo?: string`（可选字段）
- 正则表达式：`/订单号[{(](\d+)[})]/` 匹配 `订单号{xxx}` 或 `订单号(xxx)` 格式
- 跳转路径：`/order/detail?orderNumber=xxx`

### 影响
- ✅ 修复了预存款余额列表页面无法跳转到订单详情的问题
- ✅ 提升了用户体验，用户可以方便地查看相关订单
- ✅ 增强了代码的健壮性，支持多种数据格式

---

## 2025-12-13 - 修复预存款支付余额扣减和消费记录生成问题

### 功能说明
修复订单通过预存款支付成功后，没有生成消费类型的预存款交易记录和没有扣减预存款余额的问题。

### 修改文件

#### 后端
1. `backend/src/main/java/com/shoppingmall/service/buyer/impl/DepositServiceImpl.java` - 修复 depositPayment 方法

### 具体修改

#### 问题分析
- **NullPointerException风险**：如果预存款账户的余额字段（balance 或 availableBalance）为 null，直接调用 `compareTo` 或 `subtract` 方法会抛出 NullPointerException
- **异常处理不完善**：原代码在检查余额时，如果账户不存在或余额为null，会抛出异常，但没有区分具体情况

#### 修复方案

##### 1. 处理余额为null的情况
- **分离账户检查和余额检查**：先检查账户是否存在，再检查余额
- **使用默认值**：如果余额为null，使用 `BigDecimal.ZERO` 作为默认值
- **安全计算**：在计算新余额前，先获取当前余额（处理null值）

##### 2. 完善异常信息
- **账户不存在**：明确提示"预存款账户不存在，请先充值"
- **余额不足**：显示当前余额和需要支付的金额，便于用户了解情况

##### 3. 增强日志
- **记录扣减后余额**：在日志中记录扣减后的余额，便于排查问题

### 功能特性
- ✅ 正确处理余额为null的情况，避免NullPointerException
- ✅ 预存款支付成功后正确扣减余额
- ✅ 预存款支付成功后正确创建消费类型的交易记录
- ✅ 完善的异常提示，提升用户体验
- ✅ 增强的日志记录，便于问题排查

### 技术细节
- 使用三元运算符处理null值：`preDeposit.getBalance() != null ? preDeposit.getBalance() : BigDecimal.ZERO`
- 分离账户检查和余额检查，避免在检查余额时抛出NullPointerException
- 使用事务保证余额扣减和记录创建的一致性
- 消费记录类型：`DepositType.CONSUME`（值为2）

### 影响
- ✅ 修复了预存款支付时可能出现的NullPointerException
- ✅ 确保预存款支付成功后正确扣减余额
- ✅ 确保预存款支付成功后正确创建消费记录
- ✅ 提升系统稳定性和用户体验

---

## 2025-12-13 - 修复商品销量统计逻辑

### 功能说明
修复商品列表页面（`/admin/product/list`）的销量数据统计问题，按照标准电商的扣减逻辑来统计商品销量：订单完成时增加销量，订单取消/退款/退货时扣减销量。

### 修改文件

#### 后端
1. `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java` - 添加销量更新逻辑
2. `backend/src/main/java/com/shoppingmall/service/admin/impl/FinanceServiceImpl.java` - 添加退款时的销量扣减逻辑

### 具体修改

#### 1. 订单确认收货时增加销量
- **confirmReceipt 方法**：订单状态从"已发货"变为"已完成"时，调用 `updateProductSalesCount` 增加商品销量
- **销量计算**：将订单中每个商品的数量累加到对应商品的销量中

#### 2. 订单取消时扣减销量
- **cancelOrder 方法**：如果订单之前是"已完成"状态，在取消订单时扣减销量
- **防止重复扣减**：只有已完成状态的订单取消时才扣减，待付款订单取消不扣减（因为从未增加过）

#### 3. 订单退款时扣减销量
- **refundPaymentRecord 方法**：在 FinanceServiceImpl 中，当订单全额退款且订单之前是"已完成"状态时，扣减销量
- **注入依赖**：添加 OrderItemRepository 和 ProductRepository 依赖

#### 4. 添加更新商品销量方法
- **updateProductSalesCount 方法**：
  - 查询订单的所有商品项
  - 根据 increase 参数决定增加或扣减销量
  - 扣减时确保销量不为负数
  - 更新商品表的 salesCount 字段

### 功能特性
- ✅ 订单完成时自动增加商品销量
- ✅ 订单取消时（如果之前已完成）自动扣减销量
- ✅ 订单退款时（如果之前已完成）自动扣减销量
- ✅ 销量数据准确，符合标准电商统计逻辑
- ✅ 防止销量为负数

### 技术细节
- 销量存储在商品表的 `salesCount` 字段中
- 订单完成时：销量 += 订单商品数量
- 订单取消/退款/退货时：销量 -= 订单商品数量（如果之前已完成）
- 扣减时使用 `Math.max(0, newSalesCount)` 确保销量不为负数
- 使用事务保证数据一致性

### 标准电商销量统计规则
1. **订单完成（COMPLETED）**：增加销量
2. **订单取消（CANCELLED）**：如果之前已完成，扣减销量
3. **订单退款（REFUNDED）**：如果之前已完成，扣减销量
4. **订单退货（RETURNED）**：如果之前已完成，扣减销量（预留接口）

### 影响
- ✅ 商品列表的销量数据准确反映实际销售情况
- ✅ 符合标准电商的销量统计逻辑
- ✅ 订单状态变化时自动更新销量，数据实时准确
- ✅ 支持订单取消和退款时的销量扣减

---

## 2025-12-14 - 修复商品编辑页面重量数据保存问题

### 功能说明
修复商品编辑页面（`/admin/product/list`）中重量数据无法保存的问题，确保编辑商品时能正确保存重量数据到数据库。

### 修改文件

#### 前端
1. `admin-frontend/src/api/admin/product.ts` - 在 ProductVO 和 ProductDTO 接口中添加 weight 字段

#### 后端
1. `backend/src/main/java/com/shoppingmall/service/product/impl/ProductServiceImpl.java` - 修复 weight 字段的类型转换问题

### 具体修改

#### 1. 前端接口定义修复

##### ProductVO 接口
- **添加 weight 字段**：`weight?: number` - 商品重量（克）
- **添加其他缺失字段**：
  - `brandId?: number | null` - 品牌ID
  - `brandName?: string` - 品牌名称
  - `marketPrice?: number` - 市场价格
  - `costPrice?: number` - 成本价格
  - `warningStock?: number` - 警戒库存

##### ProductDTO 接口
- **添加 weight 字段**：`weight?: number` - 商品重量（克）
- **添加其他缺失字段**：
  - `brandId?: number | null` - 品牌ID
  - `marketPrice?: number` - 市场价格
  - `costPrice?: number` - 成本价格
  - `warningStock?: number` - 警戒库存
  - `enableSpec?: boolean` - 是否启用规格

#### 2. 后端类型转换修复

##### 问题分析
- **Product 实体类**中 weight 字段类型为 `Integer`（数据库存储）
- **ProductDTO** 中 weight 字段类型为 `BigDecimal`（API 传输）
- **ProductVO** 中 weight 字段类型为 `BigDecimal`（API 返回）
- 使用 `BeanUtils.copyProperties` 时，`BigDecimal` 无法直接复制到 `Integer`，导致 weight 字段丢失

##### 修复方案

###### updateProduct 方法
- **排除 weight 字段**：在 `BeanUtils.copyProperties` 的排除列表中添加 `"weight"`
- **手动转换**：将 `ProductDTO.weight`（BigDecimal）转换为 `Product.weight`（Integer）
  ```java
  if (productDTO.getWeight() != null) {
      product.setWeight(productDTO.getWeight().intValue());
  } else {
      product.setWeight(null);
  }
  ```

###### createProduct 方法
- **排除 weight 字段**：在 `BeanUtils.copyProperties` 的排除列表中添加 `"weight"`
- **手动转换**：同上，确保创建商品时 weight 字段也能正确保存

###### convertToVO 方法
- **排除 weight 字段**：在 `BeanUtils.copyProperties` 的排除列表中添加 `"weight"`
- **手动转换**：将 `Product.weight`（Integer）转换为 `ProductVO.weight`（BigDecimal）
  ```java
  if (product.getWeight() != null) {
      vo.setWeight(BigDecimal.valueOf(product.getWeight()));
  } else {
      vo.setWeight(null);
  }
  ```

### 问题原因
1. **类型不匹配**：Product 实体使用 `Integer`，而 DTO/VO 使用 `BigDecimal`
2. **自动复制失败**：`BeanUtils.copyProperties` 无法自动转换 `BigDecimal` 到 `Integer`
3. **前端接口缺失**：前端 TypeScript 接口定义中缺少 `weight` 字段

### 功能特性
- ✅ 商品编辑页面正确显示重量数据
- ✅ 支持编辑和保存商品重量
- ✅ 创建商品时 weight 字段正确保存
- ✅ 查询商品时 weight 字段正确返回
- ✅ 类型转换正确处理，避免数据丢失

### 技术细节
- **数据库存储**：weight 字段使用 `Integer` 类型（单位：克）
- **API 传输**：DTO 和 VO 使用 `BigDecimal` 类型，支持小数精度
- **类型转换**：
  - 保存时：`BigDecimal.intValue()` 转换为 Integer（取整）
  - 查询时：`BigDecimal.valueOf(Integer)` 转换为 BigDecimal
- **空值处理**：正确处理 null 值，避免空指针异常

### 影响
- ✅ 商品编辑页面重量字段可以正常保存
- ✅ 修复了重量数据无法保存到数据库的问题
- ✅ 创建和更新商品时 weight 字段都能正确处理
- ✅ 完善了前后端接口定义，提升代码可维护性

---

## 2025-12-13 - 优化订单详情页面收货人信息显示

### 功能说明
优化订单详情页面（`/order/detail`）的收货人信息模块，屏蔽不需要的字段，并修复商品重量计算逻辑。

### 修改文件

#### 前端
1. `frontend/src/views/order/Detail.vue` - 屏蔽配送方式和送货时间字段，修复重量显示

#### 后端
1. `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java` - 修复商品重量计算逻辑

### 具体修改

#### 1. 前端修改

##### 屏蔽字段
- **屏蔽"配送方式"字段**：从收货人信息左侧区域移除"配送方式"显示项
- **屏蔽"送货时间"字段**：从收货人信息右侧区域移除"送货时间"显示项

##### 修复重量显示
- **添加 formatWeight 函数**：格式化重量显示，统一显示为克（g）
  - 商品表保存的重量单位是克（g）
  - 统一按照克（g）来显示，不进行单位转换
- **修复重量计算**：使用 formatWeight 函数格式化显示，确保数据正确

#### 2. 后端修改

##### 修复重量计算逻辑
- **问题分析**：
  - OrderItem.weight 存储的是单个商品的重量（克）
  - 原代码只累加了单个商品重量，没有乘以数量
  - 原代码还错误地乘以了 1000，导致数据不正确
- **修复方案**：
  - 移除错误的乘以 1000 的转换（OrderItem.weight 已经是克）
  - 将每个商品的重量乘以数量，然后累加得到总重量
  - 总重量单位保持为克

### 功能特性
- ✅ 收货人信息模块更加简洁，只显示必要信息
- ✅ 商品重量计算正确，考虑了商品数量
- ✅ 重量显示统一为克（g），与商品表单位一致
- ✅ 提升用户体验，信息更加清晰

### 技术细节
- 前端使用 formatWeight 函数格式化重量显示
- 后端计算总重量时，将每个商品的重量乘以数量后累加
- OrderItem.weight 单位是克，不需要再转换
- 商品表（Product）保存的重量单位是克（g），统一按照克（g）来显示

### 影响
- ✅ 订单详情页面收货人信息更加简洁
- ✅ 商品重量数据计算正确
- ✅ 重量显示更加友好，自动选择合适的单位
- ✅ 提升用户体验

---

## 2025-12-13 - 完善商品收藏页面加入购物车功能

### 功能说明
完善商品收藏页面（`/member/favorites/products`）的加入购物车功能，参考商品详情页面的加入购物车逻辑，实现完整的加入购物车流程。

### 修改文件

#### 前端
1. `frontend/src/views/member/Favorites.vue` - 实现加入购物车功能

### 具体修改

#### 1. 导入必要的模块
- **导入 useRouter**：用于未登录时跳转到登录页面
- **导入购物车API**：`addToCart` 和 `AddCartDTO` 类型
- **导入购物车Store**：`useCartStore` 用于更新购物车数量

#### 2. 添加状态管理
- **addingToCart状态**：使用对象记录每个商品的加载状态，支持多个商品同时操作

#### 3. 实现加入购物车函数
- **handleAddToCart函数**：
  - 检查商品信息是否存在
  - 检查商品库存（如果库存为0或负数，提示缺货）
  - 设置加载状态，防止重复点击
  - 构建购物车数据（productId + quantity: 1）
  - 调用加入购物车API
  - 更新购物车数量（通过cartStore）
  - 显示成功提示
  - 完善的错误处理（包括401未登录跳转）

#### 4. 按钮状态优化
- **添加loading状态**：按钮显示"加入中..."文字和加载动画
- **添加disabled状态**：加载时禁用按钮，防止重复提交

### 功能特性
- ✅ 完整的加入购物车流程
- ✅ 商品库存检查，缺货时提示用户
- ✅ 加载状态显示，提升用户体验
- ✅ 自动更新购物车数量
- ✅ 未登录时自动跳转到登录页面
- ✅ 完善的错误处理和用户提示

### 技术细节
- 使用 `Record<number, boolean>` 类型记录每个商品的加载状态
- 默认购买数量为1（收藏页面不支持选择数量）
- 参考商品详情页面的实现逻辑，保持代码一致性
- 使用购物车Store统一管理购物车数量

### 影响
- ✅ 用户可以在收藏页面直接加入购物车
- ✅ 提升用户体验，操作更加便捷
- ✅ 购物车数量实时更新
- ✅ 与商品详情页面的加入购物车逻辑保持一致

---

<<<<<<< HEAD
## 2025-12-14 - 会员首页订单统计功能对接后端

### 功能说明
将会员首页（`/member`）的订单相关模块功能与后端对接，实现订单统计数据的实时获取和按钮跳转功能。

### 创建文件

#### 后端
1. `backend/src/main/java/com/shoppingmall/vo/OrderStatisticsVO.java` - 订单统计VO

### 修改文件

#### 后端
1. `backend/src/main/java/com/shoppingmall/service/buyer/OrderService.java` - 添加订单统计方法接口
2. `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java` - 实现订单统计方法
3. `backend/src/main/java/com/shoppingmall/controller/buyer/OrderController.java` - 添加订单统计接口
4. `backend/src/main/java/com/shoppingmall/controller/user/StockNotificationController.java` - 修复API路径和用户ID获取方式

#### 前端
1. `frontend/src/api/buyer/order.ts` - 添加获取订单统计的API方法
2. `frontend/src/api/buyer/stock-notification.ts` - 修复API路径
3. `frontend/src/views/member/Index.vue` - 对接订单统计API并实现按钮跳转

### 具体修改

#### 1. 后端实现

##### 订单统计VO
- **OrderStatisticsVO**：包含未付款订单数量、已发货订单数量、已作废订单数量

##### 订单统计服务
- **OrderService接口**：添加 `getOrderStatistics(Long userId)` 方法
- **OrderServiceImpl实现**：
  - 统计未付款订单（status = 0）
  - 统计已发货订单（status = 2）
  - 统计已作废订单（status = 4）

##### 订单统计接口
- **GET /api/buyer/orders/statistics**：获取当前用户的订单统计信息

##### 修复缺货登记API路径
- **StockNotificationController**：将路径从 `/api/user/stock-notification` 改为 `/api/buyer/stock-notification`
- **获取用户ID方式**：从 `request.getAttribute("userId")` 获取（JWT拦截器已设置）

#### 2. 前端实现

##### API接口
- **getOrderStatistics**：调用后端订单统计接口

##### 会员首页
- **数据获取**：页面加载时调用 `fetchOrderStatistics` 获取订单统计数据
- **按钮跳转**：
  - "付款"按钮：跳转到订单列表页面，筛选未付款订单（status=pending_payment）
  - "查看"按钮（已发货）：跳转到订单列表页面，筛选已发货订单（status=shipped）
  - "查看"按钮（已作废）：跳转到订单列表页面，筛选已作废订单（status=cancelled）

##### 修复缺货登记API路径
- 所有API调用路径从 `/api/user/` 改为 `/api/buyer/`

### 功能特性
- ✅ 订单统计数据实时从后端获取
- ✅ 未付款订单数量统计
- ✅ 已发货订单数量统计
- ✅ 已作废订单数量统计
- ✅ 按钮跳转到对应状态的订单列表
- ✅ 修复缺货登记API的404错误

### 技术细节
- 使用 MyBatis-Plus 的 `selectCount` 方法统计订单数量
- 根据订单状态（orderStatus）进行筛选统计
- 前端使用 Vue Router 的 `query` 参数传递订单状态筛选条件
- JWT拦截器自动设置userId到request attribute中

### API接口

#### GET /api/buyer/orders/statistics
获取订单统计信息

**响应数据：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "unpaidOrderCount": 5,
    "shippedOrderCount": 12,
    "cancelledOrderCount": 3
  }
}
```

### 影响
- ✅ 会员首页订单统计数据实时更新
- ✅ 提升用户体验，快速查看不同状态的订单
- ✅ 修复缺货登记API的404错误
- ✅ 统一API路径规范，使用 `/api/buyer/` 前缀

---

## 2025-12-13 - 屏蔽评价商品按钮

### 功能说明
暂时屏蔽用户端订单列表页面（`/member/transaction/orders`）已完成订单的"评价商品"按钮功能。

### 修改文件

#### 前端
1. `frontend/src/views/member/Orders.vue` - 注释评价商品按钮

### 具体修改

#### 1. 模板修改
- **屏蔽评价按钮**：将已完成订单（status === 3）的"评价商品"按钮代码注释掉
- **保留代码结构**：使用注释方式屏蔽，便于后续恢复功能

### 功能特性
- ✅ 已完成订单不再显示"评价商品"按钮
- ✅ 代码保留在注释中，便于后续恢复
- ✅ 不影响其他订单状态的功能

### 技术细节
- 使用 HTML 注释方式屏蔽按钮显示
- 保留原有的条件判断和事件处理代码结构
- 如需恢复，只需取消注释即可

### 影响
- ✅ 已完成订单页面不再显示评价按钮
- ✅ 简化已完成订单的操作选项
- ✅ 代码结构保留，便于后续功能恢复

---

## 2025-12-13 - 修改确认收货按钮样式

### 功能说明
将用户端订单列表页面（`/member/transaction/orders`）的确认收货按钮从文字链接样式改为蓝色按钮样式，提升按钮的可见性和用户体验。

### 修改文件

#### 前端
1. `frontend/src/views/member/Orders.vue` - 修改确认收货按钮样式

### 具体修改

#### 1. 模板修改
- **按钮类型**：将确认收货按钮从 `type="text"`（文字按钮）改为 `type="primary"`（蓝色按钮）
- **移除自定义类**：移除 `class="confirm-receipt-btn"`，使用 Element Plus 默认的 primary 按钮样式
- **保持按钮大小**：继续使用 `size="small"` 保持按钮大小一致

#### 2. 样式修改
- **移除文字按钮样式**：删除 `.confirm-receipt-btn` 的文字颜色和悬停样式定义
- **简化样式代码**：移除不再需要的确认收货按钮自定义样式，使用 Element Plus 默认样式

### 功能特性
- ✅ 确认收货按钮显示为蓝色按钮，更加醒目
- ✅ 按钮样式统一，符合 Element Plus 设计规范
- ✅ 提升按钮的可见性和可点击性
- ✅ 保持按钮大小一致，不影响页面布局

### 技术细节
- Element Plus 的 `type="primary"` 按钮默认显示为蓝色
- 按钮使用 `size="small"` 保持与页面其他元素的大小协调
- 移除了自定义的文字按钮样式，使用框架默认样式

### 影响
- ✅ 确认收货按钮更加醒目，提升用户体验
- ✅ 按钮样式统一，界面更加规范
- ✅ 减少自定义样式，代码更加简洁

---

## 2025-12-13 - 移除用户端订单列表的选择功能

### 功能说明
移除用户端订单列表页面（`/member/transaction/orders`）的全选、合并付款、导出订单功能，并移除列表的复选框选择功能，简化页面操作。

### 修改文件

#### 前端
1. `frontend/src/views/member/Orders.vue` - 移除选择相关功能和UI

### 具体修改

#### 1. 模板修改
- **移除顶部操作栏**：删除包含"全选"、"合并付款"、"导出订单"按钮的操作栏
- **移除底部操作栏**：删除页面底部的相同操作栏
- **移除表格复选框列**：删除表头的复选框列和数据行的复选框列
- **更新空数据提示**：将 colspan 从 7 调整为 6（移除复选框列后）

#### 2. 脚本修改
- **移除状态变量**：删除 `selectAll` 和 `selectedOrders` 响应式变量
- **移除相关函数**：
  - 删除 `handleSelectAll` 函数（全选/取消全选）
  - 删除 `handleSelectOrder` 函数（选择单个订单）
  - 删除 `handleMergePayment` 函数（合并付款）
  - 删除 `handleExportOrders` 函数（导出订单）
- **清理加载逻辑**：移除 `loadOrderList` 函数中重置选中状态的代码

#### 3. 样式修改
- **移除操作栏样式**：删除 `.order-actions` 相关的所有样式定义

### 功能特性
- ✅ 列表不再显示复选框，简化界面
- ✅ 移除批量操作功能（全选、合并付款、导出）
- ✅ 页面更加简洁，专注于订单查看和管理
- ✅ 减少不必要的交互，提升用户体验

### 技术细节
- 表格列数从 7 列减少到 6 列
- 移除了所有与订单选择相关的状态管理和事件处理
- 保留了订单查看、确认收货、评价等核心功能

### 影响
- ✅ 简化了用户端订单列表页面
- ✅ 移除了未实现或不需要的批量操作功能
- ✅ 界面更加清晰，减少用户困惑
- ✅ 提升页面加载和渲染性能

---

## 2025-12-13 - 添加订单确认收货功能

### 功能说明
在用户端订单列表页面（`/member/transaction/orders`）为已发货订单添加"确认收货"功能，用户确认收货后订单状态自动更新为"已完成"。

### 修改文件

#### 前端
1. `frontend/src/views/member/Orders.vue` - 添加确认收货按钮和处理函数

### 具体修改

#### 1. 模板修改
- **添加确认收货按钮**：在已发货订单（status === 2）的状态区域添加"确认收货"按钮
- **按钮位置**：按钮显示在物流信息下方，与已完成订单的"评价商品"按钮位置对应
- **按钮样式**：使用绿色文字按钮，与确认收货的语义相符

#### 2. 脚本修改
- **导入 ElMessageBox**：用于显示确认对话框
- **实现 handleConfirmReceipt 函数**：
  - 显示确认对话框，提示用户确认收货
  - 调用 `confirmReceipt` API 接口确认收货
  - 成功后显示成功提示并刷新订单列表
  - 失败时显示错误提示

#### 3. 样式修改
- **添加 confirm-receipt-actions 样式**：与 review-actions 样式保持一致
- **添加 confirm-receipt-btn 样式**：绿色文字按钮，hover 时颜色加深

### 功能特性
- ✅ 已发货订单显示"确认收货"按钮
- ✅ 点击按钮时显示确认对话框，防止误操作
- ✅ 确认收货后订单状态自动更新为"已完成"
- ✅ 确认收货后自动刷新订单列表，显示最新状态
- ✅ 错误处理和用户提示完善

### 技术细节
- 使用 Element Plus 的 `ElMessageBox.confirm` 显示确认对话框
- 调用后端已有的 `/api/buyer/orders/{orderNo}/confirm` 接口
- 确认收货后订单状态从"已发货"（status = 2）更新为"已完成"（status = 3）
- 按钮样式使用绿色，符合确认收货的语义

### 影响
- ✅ 用户可以在订单列表页面直接确认收货
- ✅ 完善了订单流程，从"已发货"到"已完成"的闭环
- ✅ 提升用户体验，操作更加便捷
- ✅ 订单状态更新及时，数据实时同步

---

## 2025-12-13 - 实现会员等级管理模块

### 功能说明
实现会员等级管理功能，支持动态配置会员等级（等级名称、积分区间、折扣率、排序），为后续的积分、折扣功能做支撑。功能整合到采购者管理下的"等级管理"页面。

### 创建文件

#### 数据库
1. `database/update-20251213-create-member-level-table.sql` - 创建会员等级表

#### 后端
1. `backend/src/main/java/com/shoppingmall/entity/MemberLevel.java` - 会员等级实体类
2. `backend/src/main/java/com/shoppingmall/repository/member/MemberLevelRepository.java` - 会员等级Repository
3. `backend/src/main/java/com/shoppingmall/dto/MemberLevelDTO.java` - 会员等级DTO
4. `backend/src/main/java/com/shoppingmall/vo/MemberLevelVO.java` - 会员等级VO
5. `backend/src/main/java/com/shoppingmall/service/member/MemberLevelService.java` - 会员等级服务接口
6. `backend/src/main/java/com/shoppingmall/service/member/impl/MemberLevelServiceImpl.java` - 会员等级服务实现
7. `backend/src/main/java/com/shoppingmall/controller/admin/MemberLevelController.java` - 会员等级控制器

#### 前端
1. `admin-frontend/src/api/admin/memberLevel.ts` - 会员等级API接口
2. `admin-frontend/src/views/buyer/Level.vue` - 会员等级管理页面（整合到采购者管理下）

#### 修改文件
1. `admin-frontend/src/router/componentMaps/buyer.ts` - 添加 buyer/Level 组件映射
2. `admin-frontend/src/router/componentMap.ts` - 移除 memberComponentMap 引用

### 功能特性

#### 1. 会员等级配置
- 等级名称：支持自定义等级名称（如：普通会员、银卡会员、金卡会员、钻石会员）
- 积分区间：支持设置最低积分和最高积分（最高积分可为空，表示无上限）
- 折扣率：支持设置折扣率（如：95.00表示95折，100.00表示无折扣）
- 排序号：支持设置排序号，数字越小越靠前
- 状态：支持启用/禁用等级
- 描述：支持添加等级描述

#### 2. 数据验证
- 积分区间验证：确保最高积分大于最低积分
- 积分区间重叠验证：防止不同等级的积分区间重叠
- 折扣率验证：确保折扣率在0.01-100.00之间

#### 3. 功能接口
- 分页查询会员等级列表（支持按等级名称、状态筛选）
- 获取所有启用的会员等级列表
- 根据ID获取会员等级详情
- 根据积分获取对应的会员等级（用于后续自动升级功能）
- 创建会员等级
- 更新会员等级
- 删除会员等级
- 更新会员等级状态（启用/禁用）

#### 4. 前端功能
- 会员等级列表展示（支持搜索、筛选、分页）
- 新增/编辑会员等级（表单验证）
- 删除会员等级（确认提示）
- 启用/禁用会员等级（确认提示）
- 积分区间和折扣率友好显示

### 数据库设计

#### member_level 表结构
- `id` - 主键ID
- `level_name` - 等级名称
- `min_points` - 最低积分（包含）
- `max_points` - 最高积分（不包含，NULL表示无上限）
- `discount_rate` - 折扣率（如：95.00表示95折）
- `sort_order` - 排序号
- `status` - 状态（0-禁用，1-启用）
- `description` - 等级描述
- `create_time` - 创建时间
- `update_time` - 更新时间

#### 默认数据
- 普通会员：0-1000积分，无折扣
- 银卡会员：1000-5000积分，98折
- 金卡会员：5000-20000积分，95折
- 钻石会员：20000积分及以上，9折

### 技术细节
- 后端使用 MyBatis-Plus 进行数据访问
- 前端使用 Vue 3 + Element Plus
- 积分区间验证逻辑：检查新区间是否与已存在区间重叠
- 折扣率计算：后续商品价格计算时使用 `原价 × (折扣率 / 100)`

### 后续扩展
- 会员积分管理：根据订单消费自动累计积分
- 会员等级自动升级：根据积分自动升级会员等级
- 商品折扣计算：根据会员等级自动计算商品折扣价格

### 菜单位置
- 访问路径：`/admin/buyer/level`
- 菜单位置：采购者管理 > 等级管理（使用已有的菜单ID 21）

### 影响
- ✅ 支持动态配置会员等级
- ✅ 为后续积分、折扣功能提供基础支撑
- ✅ 会员等级系统可扩展性强
- ✅ 功能整合到现有采购者管理模块，无需新增菜单
=======
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
>>>>>>> 5458440fecd82d506cdc58f0f27d62af4704809d

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

<<<<<<< HEAD
## 2025-12-13 - 会员列表对接会员等级模块

### 功能说明
将会员列表页面（`/admin/buyer/list`）的会员等级功能与会员等级模块对接，包括搜索、列表显示和编辑功能，使用真实的会员等级数据而非硬编码的等级选项。

### 修改文件

#### 后端
1. `backend/src/main/java/com/shoppingmall/service/buyer/impl/BuyerServiceImpl.java` - 修改convertToVO方法，从member_level表查询等级名称

#### 前端
1. `admin-frontend/src/views/buyer/List.vue` - 修改会员列表页面，使用真实的会员等级数据

### 具体修改

#### 1. 后端修改
- **注入MemberLevelService**：在BuyerServiceImpl中注入MemberLevelService，用于查询会员等级信息
- **添加等级缓存机制**：使用Map缓存会员等级ID和名称的映射关系，提高查询性能
- **修改convertToVO方法**：将硬编码的等级名称（普通、VIP、金牌）改为从member_level表查询
- **添加getMemberLevelName方法**：根据等级ID从缓存或数据库查询等级名称
- **添加refreshMemberLevelCache方法**：刷新会员等级缓存，支持动态更新

#### 2. 前端修改
- **加载会员等级列表**：页面加载时调用`getAllEnabledMemberLevels`获取所有启用的会员等级
- **搜索功能**：搜索表单的等级下拉框使用真实的会员等级数据，而非硬编码选项
- **编辑功能**：修改等级对话框使用真实的会员等级数据，支持选择任意启用的等级
- **等级标签颜色**：根据等级在列表中的位置动态设置标签颜色（第一个用info，中间用warning，最后一个用success）

### 技术细节

#### 后端实现
- 使用缓存机制减少数据库查询次数
- 缓存失效时自动重新加载
- 支持会员等级的动态配置，无需修改代码

#### 前端实现
- 使用`getAllEnabledMemberLevels` API获取所有启用的会员等级
- 搜索和编辑功能统一使用会员等级数据
- 等级标签颜色根据等级在列表中的位置动态设置

### 功能特性
- ✅ 搜索功能：支持按会员等级筛选会员列表
- ✅ 列表显示：正确显示会员的等级名称（从member_level表查询）
- ✅ 编辑功能：支持修改会员等级，可选择任意启用的等级
- ✅ 动态配置：会员等级变更后，列表和编辑功能自动使用新的等级数据
- ✅ 性能优化：后端使用缓存机制，减少数据库查询

### 影响
- ✅ 会员列表页面完全对接会员等级模块
- ✅ 支持动态配置会员等级，无需修改代码
- ✅ 搜索、列表、编辑功能统一使用会员等级数据
- ✅ 提升用户体验，等级管理更加灵活

---

## 2025-12-13 - 完善数据看板页面功能

### 功能说明
完善数据看板页面（`/admin/dashboard`），对接后端接口，实现完整的数据统计和可视化展示功能。

### 创建文件

#### 后端
1. `backend/src/main/java/com/shoppingmall/vo/DashboardVO.java` - 数据看板VO，定义返回数据结构
2. `backend/src/main/java/com/shoppingmall/service/admin/DashboardService.java` - 数据看板服务接口
3. `backend/src/main/java/com/shoppingmall/service/admin/impl/DashboardServiceImpl.java` - 数据看板服务实现类
4. `backend/src/main/java/com/shoppingmall/controller/admin/DashboardController.java` - 数据看板控制器

#### 前端
1. `admin-frontend/src/api/admin/dashboard.ts` - 数据看板API接口

### 修改文件

#### 前端
1. `admin-frontend/src/views/dashboard/Index.vue` - 完善数据看板页面，对接后端接口，添加图表展示

### 功能特性

#### 1. 核心指标统计
- 今日订单数：统计当天的订单数量
- 今日销售额：统计当天的销售总额
- 待处理订单数：统计待付款和已付款未发货的订单数量
- 库存预警：统计库存预警的商品数量

#### 2. 总体数据统计
- 总订单数：统计所有订单数量
- 总销售额：统计所有订单的销售总额
- 总用户数：统计注册用户总数
- 总商品数：统计商品总数

#### 3. 数据可视化
- 最近7天销售趋势：使用折线图展示最近7天的销售额趋势
- 最近7天订单趋势：使用折线图展示最近7天的订单数量趋势
- 订单状态分布：使用饼图展示订单状态分布情况（待付款、已付款未发货、已发货、已完成、已取消）

#### 4. 数据格式化
- 数字格式化：大于10000的数字自动转换为"万"单位显示
- 货币格式化：金额自动转换为"万"或"亿"单位显示，提升可读性

### 技术细节

#### 后端实现
- 使用 MyBatis-Plus 进行数据查询
- 通过 OrderRepository 查询订单相关数据
- 通过 UserRepository 查询用户数据
- 通过 ProductRepository 查询商品数据
- 通过 StockService 获取库存预警数据
- 按日期范围统计最近7天的销售和订单趋势
- 统计订单状态分布（待付款、已付款未发货、已发货、已完成、已取消）

#### 前端实现
- 使用 Vue 3 Composition API
- 使用 ECharts 进行数据可视化
- 图表类型：折线图（销售趋势、订单趋势）、饼图（订单状态分布）
- 响应式设计：图表自动适应窗口大小变化
- 错误处理：API调用失败时显示错误提示

#### 图表配置
- 销售趋势图：面积折线图，绿色主题，显示最近7天销售额
- 订单趋势图：面积折线图，蓝色主题，显示最近7天订单数
- 订单状态分布图：环形饼图，不同状态使用不同颜色标识

### API接口

#### GET /api/admin/dashboard/statistics
获取数据看板统计信息

**响应数据：**
```json
{
  "todayOrders": 125,
  "todaySales": 125680.00,
  "pendingOrders": 23,
  "stockWarnings": 5,
  "totalOrders": 15230,
  "totalSales": 15236800.00,
  "totalUsers": 1250,
  "totalProducts": 856,
  "salesTrend": [
    { "date": "12-07", "sales": 125680.00 },
    ...
  ],
  "orderTrend": [
    { "date": "12-07", "count": 125 },
    ...
  ],
  "orderStatusStatistics": {
    "pendingPayment": 15,
    "paidNotShipped": 8,
    "shipped": 45,
    "completed": 1520,
    "cancelled": 12
  }
}
```

### 影响
- ✅ 数据看板页面功能完整，展示核心业务指标
- ✅ 支持数据可视化，直观展示业务趋势
- ✅ 对接后端接口，数据实时更新
- ✅ 提升管理员对业务数据的感知能力
- ✅ 图表响应式设计，适配不同屏幕尺寸

---

## 2025-12-13 - 调整订单列表页面列宽

### 修改内容
调整订单列表页面（`/admin/order/list`）的列表列宽，根据字段数据特点设置更合适的列宽，提升页面显示效果。

### 修改文件

#### 前端
1. `admin-frontend/src/views/order/List.vue` - 调整表格列宽

### 具体修改

#### 列宽调整详情
- **订单号列**：从 180px 调整为 200px，为较长的订单号提供显示空间
- **收货人列**：从 120px 调整为 130px，稍微增加显示空间
- **订单描述列**：从固定宽度 300px 改为 min-width 350px，使用自适应宽度，为包含多个商品的描述提供更多显示空间
- **下单日期列**：从 180px 调整为 190px，确保日期时间完整显示
- **总金额列**：从 120px 调整为 130px，并添加右对齐，提升金额显示效果
- **状态列**：从 120px 调整为 140px，并添加居中对齐，为较长的状态文本（如"已付款未发货"）提供显示空间
- **操作列**：从 300px 调整为 320px，为多个操作按钮提供更充足的显示空间

### 技术细节
- 根据字段实际数据内容调整列宽，避免内容被截断
- 订单描述列使用 min-width，支持自适应扩展，更好地显示包含多个商品的描述
- 总金额列添加右对齐，符合金额显示习惯
- 状态列添加居中对齐，提升视觉效果

### 影响
- ✅ 列表列宽更加合理，内容显示更完整
- ✅ 提升页面视觉效果和用户体验
- ✅ 避免重要信息被截断
- ✅ 订单描述列自适应，更好地展示订单内容

---

## 2025-12-13 - 修复订单页面样式错误

### 修改内容
修复前端订单页面（`frontend/src/views/member/Orders.vue`）中的 SASS 样式编译错误，解决不匹配的大括号问题。

### 修改文件

#### 前端
1. `frontend/src/views/member/Orders.vue` - 修复样式嵌套结构

### 具体修改

#### 样式结构修复
- 将 `.logistics-details` 样式块正确地嵌套在 `.logistics-info` 内部
- 移除了多余的大括号，修复了第 371 行附近的不匹配 `}` 错误
- 调整样式嵌套结构，使其与 HTML 模板结构保持一致

### 问题原因
- `.logistics-details` 原本错误地放置在 `.logistics-info` 外部，导致样式嵌套结构不匹配
- 存在多余的大括号，导致 SASS 编译器报错

### 技术细节
- SASS 样式嵌套必须与 HTML 结构保持一致
- `.logistics-details` 在 HTML 中位于 `.logistics-info` 内部，样式也应该这样嵌套
- 修复后样式结构：`.order-status > .logistics-info > .logistics-details`

### 影响
- ✅ 修复了 SASS 编译错误，页面可以正常加载
- ✅ 样式结构更加清晰，与 HTML 结构保持一致
- ✅ 物流详情样式正确应用
=======
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
>>>>>>> 5458440fecd82d506cdc58f0f27d62af4704809d
��复支付页面支付密码错误时出现两个重复错误提示的问题，优化错误处理逻辑。

### 修改文件

#### 前端
1. frontend/src/views/order/Payment.vue - 优化错误处理，移除重复的错误提示

### 具体修改

#### 1. 移除重复的错误提示
- 在 `processPayment` 函数的 catch 块中移除 `ElMessage.error` 调用
- 因为 `request.ts` 的响应拦截器已经统一处理并显示了错误消息
- 避免同一个错误被显示两次

#### 2. 优化支付密码错误处理
- 当支付密码错误时，自动重新打开密码输入对话框
- 清空密码输入框，让用户重新输入
- 改善用户体验，避免用户需要手动重新点击付款按钮

#### 3. 优化密码清空逻辑
- 在 finally 块中，只在非预存款支付时清空密码
- 预存款支付失败时保留密码输入框状态，方便用户查看和重新输入
- 避免支付失败后密码被意外清空

### 技术细节
- 错误消息统一由 `request.ts` 的响应拦截器处理
- 通过检查错误消息内容判断是否为支付密码错误
- 使用 `showPaymentPasswordDialog` 控制对话框显示状态

### 影响
- ✅ 修复了支付密码错误时出现两个重复提示的问题
- ✅ 支付密码错误时自动重新打开输入对话框，提升用户体验
- ✅ 错误处理逻辑更加清晰，避免重复提示
- ✅ 密码输入框状态管理更加合理

---

## 2025-12-12 - 购物结算页面预存款余额对接后端

### 修改内容
在购物结算页面（/cart/checkout）对接后端API，获取并显示预存款余额数据。

### 修改文件

#### 前端
1. frontend/src/views/cart/Checkout.vue - 对接预存款余额API

### 具体修改

#### 1. 导入API和依赖
- 导入 `getDepositBalance` API函数用于获取预存款数据
- 导入 `useUserStore` 用于检查用户登录状态

#### 2. 添加预存款余额加载函数
- 创建 `loadDepositBalance` 函数调用后端API获取预存款余额
- 使用 `availableBalance` 字段作为显示的余额（可用余额）
- 添加错误处理，对于401未授权错误不显示提示，其他错误静默处理

#### 3. 页面加载时获取数据
- 在 `onMounted` 生命周期钩子中调用 `loadDepositBalance`
- 仅在用户已登录时调用API获取数据
- 与加载地址列表和购物车商品并行执行

### 技术细节
- 后端API: `/api/buyer/member/deposit/balance`
- 返回字段: `availableBalance`（可用余额）
- 仅在用户已登录时调用API获取数据
- 错误处理：401错误不显示提示，其他错误静默处理，保持页面正常使用

### 影响
- ✅ 购物结算页面预存款余额从后端实时获取
- ✅ 用户登录后自动加载预存款余额数据
- ✅ 未登录用户不显示错误提示，保持良好用户体验
- ✅ 预存款余额数据与后端数据库保持同步
- ✅ 支付方式选择时显示准确的预存款余额

---

## 2025-12-12 - 完善支付密码逻辑和提示

### 修改内容
完善支付环节的支付密码验证逻辑，支持"未设置支付密码时，默认使用登录密码"的功能，并在支付密码输入界面添加提示语。

### 修改文件

#### 后端
1. backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java - 修改支付密码验证逻辑

#### 前端
1. frontend/src/views/order/Payment.vue - 在支付密码输入对话框添加提示语

### 具体修改

#### 1. 后端支付密码验证逻辑优化
- 修改 `OrderServiceImpl.java` 中的支付密码验证逻辑
- 如果用户未设置过支付密码（`paymentPassword` 为空），则使用登录密码进行验证
- 如果用户已设置过支付密码，则使用支付密码进行验证
- 移除了"请先设置支付密码"的异常抛出，改为自动使用登录密码作为默认支付密码

#### 2. 前端支付密码输入界面优化
- 在支付密码输入对话框中添加黄色提示条
- 提示内容："(如未设置过支付密码,默认支付密码为您的账号登陆密码!)"
- 提示样式与修改支付密码页面保持一致（黄色背景、边框、文字颜色）

### 技术细节
- 支付密码验证逻辑与修改支付密码逻辑保持一致
- 使用BCrypt进行密码验证
- 提示样式使用与修改支付密码页面相同的设计风格

### 影响
- ✅ 用户未设置支付密码时，可以使用登录密码进行支付
- ✅ 支付密码输入界面有明确的提示信息，提升用户体验
- ✅ 支付密码验证逻辑统一，避免用户困惑
- ✅ 与修改支付密码页面的提示保持一致

---

## 2025-12-12 - 实现修改预存款支付密码功能

### 修改内容
在会员中心个人设置下增加"修改预存款支付密码"菜单页面，参考截图1:1仿真实现前端页面和后端对接。如果用户未设置过支付密码，默认支付密码为账号登录密码。

### 修改文件

#### 前端
1. frontend/src/components/member/MemberSidebar.vue - 添加"修改预存款支付密码"菜单项
2. frontend/src/views/member/PaymentPassword.vue - 创建修改预存款支付密码页面
3. frontend/src/router/index.ts - 添加修改预存款支付密码路由
4. frontend/src/api/buyer/user.ts - 添加修改支付密码API调用方法

#### 后端
1. backend/src/main/java/com/shoppingmall/service/user/UserService.java - 添加修改支付密码接口方法
2. backend/src/main/java/com/shoppingmall/service/user/impl/UserServiceImpl.java - 实现修改支付密码方法
3. backend/src/main/java/com/shoppingmall/controller/buyer/UserController.java - 添加修改支付密码API接口

### 具体修改

#### 1. 前端菜单和路由
- 在MemberSidebar组件中添加"修改预存款支付密码"菜单项（位于"修改密码"和"收货地址"之间）
- 添加路由映射和自动判断逻辑
- 在路由配置中添加 `/member/settings/payment-password` 路由

#### 2. 前端页面实现
- 创建PaymentPassword.vue页面，参考截图1:1仿真
- 页面包含：
  - 标题："预存款支付密码修改"
  - 黄色提示条："(如未设置过支付密码,默认支付密码为您的账号登陆密码!)"
  - 三个输入框：原支付密码、新支付密码、确认新支付密码
  - 保存按钮（灰色样式）
- 使用表格布局（table），左侧标签，右侧输入框
- 表单验证：原支付密码必填，新支付密码必填且长度6-20字符，确认密码必须与新密码一致
- 密码输入框支持显示/隐藏密码功能

#### 3. 后端API实现
- 在UserService接口中添加 `changePaymentPassword` 方法
- 在UserServiceImpl中实现修改支付密码逻辑：
  - 验证原支付密码：如果用户未设置过支付密码（paymentPassword为空），则使用登录密码验证；如果已设置，则使用支付密码验证
  - 使用BCrypt加密新支付密码并保存
- 在UserController中添加 `PUT /api/buyer/user/payment-password` 接口

#### 4. 前端API调用
- 在user.ts中添加 `changePaymentPassword` 方法，调用后端API

### 技术细节
- 后端API: `PUT /api/buyer/user/payment-password`
- 请求参数: `oldPaymentPassword`（原支付密码）、`newPaymentPassword`（新支付密码）
- 密码加密: 使用BCrypt加密存储
- 默认密码逻辑: 如果用户未设置过支付密码，默认使用登录密码作为支付密码

### 影响
- ✅ 用户可以在会员中心修改预存款支付密码
- ✅ 支持首次设置支付密码（使用登录密码验证）
- ✅ 前端页面样式与截图保持一致
- ✅ 完整的表单验证和错误提示
- ✅ 修改成功后清空表单并提示成功信息

---

## 2025-12-12 - 会员首页预存款数据对接后端

### 修改内容
在会员首页（/member）对接后端API，获取并显示预存款余额和可用余额数据。

### 修改文件
1. frontend/src/views/member/Index.vue

### 具体修改
1. **导入API函数**: 导入 `getDepositBalance` API函数用于获取预存款数据
2. **添加生命周期钩子**: 使用 `onMounted` 在组件挂载时获取预存款数据
3. **实现数据获取函数**: 创建 `fetchDepositBalance` 函数调用后端API获取预存款余额和可用余额
4. **错误处理**: 添加错误处理逻辑，对于401未授权错误不显示提示，其他错误显示友好提示
5. **数据绑定**: 将API返回的 `depositBalance` 和 `availableBalance` 字段绑定到页面显示

### 技术细节
- 后端API: `/api/buyer/member/deposit/balance`
- 返回字段: `depositBalance`（预存款余额）、`availableBalance`（可用余额）
- 仅在用户已登录时调用API获取数据

### 影响
- ✅ 会员首页预存款余额和可用余额从后端实时获取
- ✅ 用户登录后自动加载预存款数据
- ✅ 未登录用户不显示错误提示，保持良好用户体验
- ✅ 预存款数据与后端数据库保持同步

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

## 2025-12-12 - 订单库存管理和自动取消功能

### 修改内容
完善订单创建时的库存扣减机制，并实现待付款订单的自动取消功能。

### 修改文件

#### 配置文件
1. backend/src/main/resources/application.yml - 添加订单超时时间配置参数

#### 后端
1. backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java - 添加库存扣减和恢复逻辑
2. backend/src/main/java/com/shoppingmall/service/buyer/OrderScheduledService.java - 创建订单定时任务接口
3. backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderScheduledServiceImpl.java - 实现订单自动取消定时任务

### 具体修改

#### 1. 配置参数
- 在`application.yml`中添加`order.payment-timeout-hours`配置项，默认值为6小时
- 支持通过配置文件修改订单超时时间，方便运维调整

#### 2. 库存扣减机制

**创建订单时扣减库存**
- 在`OrderServiceImpl.createOrder`方法中添加库存扣减逻辑
- 扣减`product`表的`stock`字段（商品库存）
- 扣减`product_stock`表的`availableStock`字段（可用库存）
- 增加`product_stock`表的`lockedStock`字段（锁定库存）
- 如果`product_stock`记录不存在，自动创建新记录

**库存扣减流程**
1. 验证商品库存是否足够
2. 创建订单成功后，立即扣减库存
3. 同时更新`product`表和`product_stock`表
4. 确保库存数据一致性

#### 3. 订单自动取消机制

**定时任务**
- 创建`OrderScheduledService`接口和`OrderScheduledServiceImpl`实现类
- 使用`@Scheduled(fixedRate = 60000)`注解，每分钟执行一次
- 自动查找超过配置时间未支付的待付款订单
- 自动取消超时订单并恢复库存

**自动取消流程**
1. 定时任务每分钟执行一次
2. 查找创建时间超过配置时间的待付款订单
3. 对每个超时订单：
   - 恢复`product`表的库存
   - 恢复`product_stock`表的可用库存
   - 减少`product_stock`表的锁定库存
   - 更新订单状态为已取消
4. 记录详细的日志信息

#### 4. 手动取消订单时恢复库存

**取消订单方法优化**
- 在`OrderServiceImpl.cancelOrder`方法中添加库存恢复逻辑
- 用户手动取消订单时，自动恢复已扣减的库存
- 确保库存数据准确性

### 功能特性

1. **库存管理**
   - ✅ 创建订单时立即扣减库存，防止超卖
   - ✅ 同时更新`product`表和`product_stock`表，保持数据一致性
   - ✅ 订单取消时自动恢复库存，避免库存损失

2. **自动取消机制**
   - ✅ 定时任务自动检查超时订单
   - ✅ 可配置的超时时间（默认6小时）
   - ✅ 自动恢复超时订单的库存
   - ✅ 异常处理完善，单个订单处理失败不影响其他订单

3. **配置灵活性**
   - ✅ 超时时间可通过配置文件修改
   - ✅ 支持不同环境使用不同的超时时间
   - ✅ 默认值6小时，符合常见业务需求

### 技术实现

1. **库存扣减**
   - 使用数据库事务保证原子性
   - 先验证库存，再扣减，避免并发问题
   - 同时更新两个库存表，保持数据同步

2. **定时任务**
   - 使用Spring的`@Scheduled`注解
   - 每分钟执行一次，及时处理超时订单
   - 异常处理机制，确保定时任务异常不影响系统运行

3. **库存恢复**
   - 订单取消时（手动或自动）都恢复库存
   - 恢复逻辑与扣减逻辑对应，确保数据准确性
   - 处理边界情况（如库存记录不存在）

### 影响
- ✅ 订单创建时立即扣减库存，防止超卖问题
- ✅ 待付款订单自动取消，释放被占用的库存
- ✅ 库存管理更加精确，避免库存数据不一致
- ✅ 提升系统自动化程度，减少人工干预
- ✅ 配置灵活，可根据业务需求调整超时时间
---

## 2025-12-12 - 完善支付页面功能

### 修改内容
完善支付页面（/order/payment），实现3种支付方式：预存款支付、支付宝支付、微信支付。预存款支付需要验证支付密码并检查余额；支付宝和微信支付使用mock模拟支付回调。

### 修改文件

#### 数据库
1. database/update-20251212-add-payment-password.sql - 添加支付密码字段到用户表

#### 后端
1. backend/src/main/java/com/shoppingmall/entity/User.java - 添加支付密码字段
2. backend/src/main/java/com/shoppingmall/entity/PaymentRecord.java - 创建支付记录实体类
3. backend/src/main/java/com/shoppingmall/repository/payment/PaymentRecordRepository.java - 创建支付记录Repository
4. backend/src/main/java/com/shoppingmall/dto/OrderPaymentDTO.java - 创建订单支付DTO
5. backend/src/main/java/com/shoppingmall/service/buyer/OrderService.java - 添加订单支付方法接口
6. backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java - 实现订单支付方法
7. backend/src/main/java/com/shoppingmall/controller/buyer/OrderController.java - 添加订单支付接口
8. backend/src/main/java/com/shoppingmall/controller/buyer/PaymentController.java - 创建支付回调控制器

#### 前端
1. frontend/src/api/buyer/order.ts - 添加订单支付API接口
2. frontend/src/views/order/Payment.vue - 完善支付页面功能

### 具体修改

#### 1. 数据库修改
- 在用户表（sys_user）中添加支付密码字段（payment_password），用于存储BCrypt加密的支付密码

#### 2. 后端修改

**实体类**
- 在User实体类中添加paymentPassword字段
- 创建PaymentRecord实体类，用于存储支付记录

**服务层**
- 在OrderService接口中添加payOrder方法
- 在OrderServiceImpl中实现payOrder方法：
  - 预存款支付：验证支付密码、检查余额、扣款、更新订单和支付记录
  - 支付宝/微信支付：创建支付订单、返回支付URL、创建待支付记录

**控制器**
- 在OrderController中添加订单支付接口（POST /api/buyer/orders/{orderNo}/pay）
- 创建PaymentController，提供支付回调接口（POST /api/buyer/payment/callback）和模拟支付成功接口（POST /api/buyer/payment/mock/success）

#### 3. 前端修改

**API接口**
- 在order.ts中添加OrderPaymentDTO接口和payOrder方法

**支付页面**
- 添加预存款余额加载功能
- 添加支付密码输入对话框
- 实现预存款支付流程：检查余额、输入密码、调用支付接口
- 实现支付宝/微信支付流程：调用支付接口、模拟支付回调、更新订单状态
- 添加支付状态loading提示

### 功能特性

1. **预存款支付**
   - ✅ 自动加载并显示预存款余额
   - ✅ 支付前检查余额是否足够
   - ✅ 需要输入支付密码进行验证
   - ✅ 支付成功后立即更新订单状态和支付记录
   - ✅ 自动创建预存款消费记录

2. **支付宝/微信支付**
   - ✅ 创建支付订单并返回支付URL
   - ✅ 支持mock模拟支付回调
   - ✅ 支付回调后自动更新订单状态和支付记录
   - ✅ 支付成功后跳转到订单详情页面

3. **支付记录**
   - ✅ 所有支付方式都会创建支付记录（payment_record表）
   - ✅ 支付记录包含订单ID、支付方式、金额、状态等信息
   - ✅ 支付宝/微信支付回调数据保存到支付记录中

### 影响
- ✅ 支付页面功能完善，支持3种支付方式
- ✅ 预存款支付安全性提升，需要支付密码验证
- ✅ 支付宝/微信支付支持mock模拟，方便测试
- ✅ 所有支付数据都会更新到交易记录表和订单表
- ✅ 提升用户体验，支付流程更加顺畅
---