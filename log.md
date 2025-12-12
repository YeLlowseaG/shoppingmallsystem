## 2025-12-12 - 修复支付页面重复错误提示问题

### 修改内容
修复支付页面支付密码错误时出现两个重复错误提示的问题，优化错误处理逻辑。

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