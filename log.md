# 修改日志

## 2025-12-26 - 优化支付超时时间配置

### 功能说明
根据电商平台行业标准和B2B业务特点，优化了支付超时时间的默认配置，使其更加合理和符合实际业务需求。

### 修改原因
- 原默认值6小时过长，参考淘宝15分钟、京东30分钟等主流平台
- B2B平台需要平衡用户体验和库存管理效率
- 支付中状态通常不会持续太久，需要及时释放资源

### 修改内容

**超时时间调整：**

| 配置项 | 原默认值 | 新默认值 | 说明 |
|--------|---------|---------|------|
| 订单支付超时时间 | 6小时 | **4小时** | B2B平台建议4-6小时，给企业用户充足的决策和审批时间 |
| 支付记录自动取消时间 | 6小时 | **2小时** | 支付中状态通常不会持续太久，2小时足够完成支付流程 |
| 预存款记录自动取消时间 | 6小时 | **1小时** | 充值流程相对简单，不需要太长的等待时间 |

### 修改文件

**数据库配置脚本：**
- `database/update-20251226-add-system-configs.sql` - 订单支付超时时间改为4小时
- `database/update-20251226-add-payment-record-timeout-config.sql` - 支付记录超时时间改为2小时
- `database/update-20251226-add-deposit-record-timeout-config.sql` - 预存款记录超时时间改为1小时

**后端代码：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderScheduledServiceImpl.java` - 默认值改为4小时
- `backend/src/main/java/com/shoppingmall/service/payment/impl/PaymentRecordScheduledServiceImpl.java` - 默认值改为2小时
- `backend/src/main/java/com/shoppingmall/service/deposit/impl/DepositScheduledServiceImpl.java` - 默认值改为1小时

### 时间设置说明

**订单支付超时（4小时）：**
- 适合B2B企业采购场景
- 给企业用户充足的决策和审批时间
- 平衡库存管理和用户体验

**支付记录自动取消（2小时）：**
- 支付中状态通常不会持续太久
- 2小时足够用户完成支付流程
- 避免长时间占用支付通道

**预存款充值超时（1小时）：**
- 充值流程相对简单
- 不需要太长的等待时间
- 及时释放资源

### 注意事项

- 所有配置都支持在管理后台动态修改
- 修改后立即生效，无需重启服务
- 建议根据实际业务数据（超时率）进行微调
- 如果超时率 > 10%，可考虑适当延长
- 如果超时率 < 5%，可考虑适当缩短

---

## 2025-12-26 - 预存款支付中记录自动取消功能

### 功能说明
实现预存款支付中状态的记录自动取消功能，类似支付记录自动取消机制。当预存款记录处于"支付中"状态超过配置的时间（默认6小时）后，系统会自动将其更新为"已超时"状态。

### 修改原因
- 预存款支付中状态的记录如果长时间未完成支付，需要自动更新为已超时
- 避免预存款记录长期处于支付中状态，影响数据统计和业务处理
- 与支付记录自动取消机制保持一致，提升系统自动化程度

### 修改内容

#### 数据库配置

**新增配置项：**
- `database/update-20251226-add-deposit-record-timeout-config.sql`
  - `deposit.record-timeout-hours`：预存款记录自动取消时间（小时，默认：6）
    - 配置支付中状态的预存款记录自动超时时间
    - 支持在管理后台动态修改，修改后立即生效

#### 后端代码修改

**1. 预存款定时任务服务接口（新建）：**
- `backend/src/main/java/com/shoppingmall/service/deposit/DepositScheduledService.java`
  - **功能**：定义预存款定时任务服务接口
  - **方法**：
    - `cancelTimeoutDepositRecords()`：自动取消超时的支付中预存款记录

**2. 预存款定时任务服务实现类（新建）：**
- `backend/src/main/java/com/shoppingmall/service/deposit/impl/DepositScheduledServiceImpl.java`
  - **功能**：实现预存款自动取消定时任务
  - **特性**：
    - 每小时执行一次，检查超时的支付中预存款记录（降低系统压力）
    - 从数据库读取最新配置，确保配置修改后立即生效
    - 将超时的支付中记录状态更新为"已超时"（TIMEOUT）
    - 记录详细日志，便于问题排查
  - **方法**：
    - `getDepositRecordTimeoutHours()`：从数据库读取预存款记录自动取消时间配置
    - `cancelTimeoutDepositRecords()`：执行自动取消超时预存款记录的任务

### 技术细节

**定时任务配置：**
- 执行频率：每小时执行一次（`@Scheduled(fixedRate = 3600000)`）
- 事务支持：使用 `@Transactional` 确保数据一致性
- 异常处理：单个记录处理失败不影响其他记录的处理
- **优化说明**：相比每分钟执行，每小时执行可大幅降低系统压力，同时仍能及时处理超时记录

**状态流转：**
- 支付中（PAYING，状态值：3）→ 已超时（TIMEOUT，状态值：4）

**配置管理：**
- 配置键：`deposit.record-timeout-hours`
- 默认值：1小时（已优化，原为6小时）
- 配置类型：number
- 支持在管理后台动态修改

**注意：** 此服务与现有的 `buyer.DepositScheduledService`（处理待审核状态）功能不同，两者互不冲突。

### 使用说明

1. **执行SQL脚本**：
   ```sql
   -- 执行数据库脚本添加配置项
   source database/update-20251226-add-deposit-record-timeout-config.sql
   ```

2. **配置自动取消时间**：
   - 登录管理后台
   - 进入"系统管理" → "系统配置"
   - 找到"预存款记录自动取消时间"配置项
   - 修改时间值（单位：小时）
   - 保存后立即生效，无需重启服务

3. **查看日志**：
   - 定时任务执行日志会记录在应用日志中
   - 可通过日志查看自动更新的预存款记录详情

### 注意事项

- 定时任务会在应用启动后自动运行，无需手动启动
- 配置修改后，下次定时任务执行时立即生效
- 已超时的预存款记录不会再次被处理
- 建议根据实际业务需求调整自动取消时间

---

## 2025-12-26 - 支付记录自动取消功能

### 功能说明
实现支付中状态的支付记录自动取消功能，类似订单自动取消机制。当支付记录处于"支付中"状态超过配置的时间（默认6小时）后，系统会自动将其关闭。

### 修改原因
- 支付中状态的支付记录如果长时间未完成支付，需要自动关闭
- 避免支付记录长期处于支付中状态，影响数据统计和业务处理
- 与订单自动取消机制保持一致，提升系统自动化程度

### 修改内容

#### 数据库配置

**新增配置项：**
- `database/update-20251226-add-payment-record-timeout-config.sql`
  - `payment.record-timeout-hours`：支付记录自动取消时间（小时，默认：6）
    - 配置支付中状态的支付记录自动关闭时间
    - 支持在管理后台动态修改，修改后立即生效

#### 后端代码修改

**1. 支付记录定时任务服务接口（新建）：**
- `backend/src/main/java/com/shoppingmall/service/payment/PaymentRecordScheduledService.java`
  - **功能**：定义支付记录定时任务服务接口
  - **方法**：
    - `cancelTimeoutPaymentRecords()`：自动取消超时的支付中支付记录

**2. 支付记录定时任务服务实现类（新建）：**
- `backend/src/main/java/com/shoppingmall/service/payment/impl/PaymentRecordScheduledServiceImpl.java`
  - **功能**：实现支付记录自动取消定时任务
  - **特性**：
    - 每小时执行一次，检查超时的支付中支付记录（降低系统压力）
    - 从数据库读取最新配置，确保配置修改后立即生效
    - 将超时的支付中记录状态更新为"已关闭"（CLOSED）
    - 记录详细日志，便于问题排查
  - **方法**：
    - `getPaymentRecordTimeoutHours()`：从数据库读取支付记录自动取消时间配置
    - `cancelTimeoutPaymentRecords()`：执行自动取消超时支付记录的任务

### 技术细节

**定时任务配置：**
- 执行频率：每小时执行一次（`@Scheduled(fixedRate = 3600000)`）
- 事务支持：使用 `@Transactional` 确保数据一致性
- 异常处理：单个记录处理失败不影响其他记录的处理
- **优化说明**：相比每分钟执行，每小时执行可大幅降低系统压力，同时仍能及时处理超时记录

**状态流转：**
- 支付中（PAYING，状态值：1）→ 已关闭（CLOSED，状态值：3）

**配置管理：**
- 配置键：`payment.record-timeout-hours`
- 默认值：2小时（已优化，原为6小时）
- 配置类型：number
- 支持在管理后台动态修改

### 使用说明

1. **执行SQL脚本**：
   ```sql
   -- 执行数据库脚本添加配置项
   source database/update-20251226-add-payment-record-timeout-config.sql
   ```

2. **配置自动取消时间**：
   - 登录管理后台
   - 进入"系统管理" → "系统配置"
   - 找到"支付记录自动取消时间"配置项
   - 修改时间值（单位：小时）
   - 保存后立即生效，无需重启服务

3. **查看日志**：
   - 定时任务执行日志会记录在应用日志中
   - 可通过日志查看自动关闭的支付记录详情

### 注意事项

- 定时任务会在应用启动后自动运行，无需手动启动
- 配置修改后，下次定时任务执行时立即生效
- 已关闭的支付记录不会再次被处理
- 建议根据实际业务需求调整自动取消时间

---

## 2025-12-26 - 系统配置改为数据库管理（邮箱、订单、应用配置）

### 功能说明
将邮箱配置、订单配置、应用配置从配置文件改为数据库配置管理，支持管理员在后台动态修改，无需重启服务即可生效。

### 修改原因
- 配置文件修改需要重启服务，不便于运维管理
- 需要支持动态配置，提升系统灵活性
- 统一在系统配置管理页面管理，便于维护
- 支持配置历史记录和权限控制

### 修改内容

#### 数据库配置

**新增配置项：**
- `database/update-20251226-add-system-configs.sql`
  - **邮箱配置**：
    - `mail.host`：邮件服务器地址（默认：smtp.qq.com）
    - `mail.port`：邮件服务器端口（默认：587）
    - `mail.username`：发件人邮箱
    - `mail.password`：邮箱授权码
  - **订单配置**：
    - `order.payment-timeout-hours`：订单支付超时时间（小时，默认：6）
  - **应用配置**：
    - `app.password.reset.token-expire-minutes`：密码重置令牌有效期（分钟，默认：30）

#### 后端代码修改

**1. 邮件配置服务（新建）：**
- `backend/src/main/java/com/shoppingmall/service/impl/MailConfigService.java`
  - **功能**：动态创建 `JavaMailSender`，每次发送邮件时从数据库读取最新配置
  - **方法**：
    - `createMailSender()`：创建邮件发送器（使用最新配置）
    - `getFromEmail()`：获取发件人邮箱地址

**2. 邮件服务实现类：**
- `backend/src/main/java/com/shoppingmall/service/impl/EmailServiceImpl.java`
  - **移除 `JavaMailSender` 注入**：改为使用 `MailConfigService` 动态创建
  - **修改 `sendPasswordResetEmail()` 方法**：每次发送邮件时动态创建 `JavaMailSender`

**3. 订单定时任务服务：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderScheduledServiceImpl.java`
  - **移除 `@Value` 注解**：不再从配置文件读取订单配置
  - **添加 `SystemConfigService` 依赖**：用于从数据库读取配置
  - **添加 `getPaymentTimeoutHours()` 方法**：从数据库读取订单支付超时时间
  - **修改 `cancelTimeoutOrders()` 方法**：每次执行定时任务时读取最新配置

**4. 用户服务实现类：**
- `backend/src/main/java/com/shoppingmall/service/user/impl/UserServiceImpl.java`
  - **移除 `@Value` 注解**：不再从配置文件读取密码重置令牌有效期
  - **添加 `SystemConfigService` 依赖**：用于从数据库读取配置
  - **添加 `getTokenExpireMinutes()` 方法**：从数据库读取令牌有效期
  - **修改 `forgotPassword()` 方法**：每次使用时读取最新配置

### 功能特性
- ✅ 邮箱配置可动态修改（服务器地址、端口、账号、授权码）
- ✅ 订单支付超时时间可动态修改
- ✅ 密码重置令牌有效期可动态修改
- ✅ 配置修改后立即生效，无需重启服务
- ✅ 提供默认值作为兜底机制
- ✅ 配置格式验证（端口、有效期等数字类型）

### 配置读取策略

1. **邮箱配置**：
   - 每次发送邮件时从数据库读取最新配置
   - 动态创建 `JavaMailSender`，确保使用最新配置
   - 配置缺失时抛出异常，提示管理员配置

2. **订单配置**：
   - 定时任务每次执行时读取最新配置
   - 配置格式错误时使用默认值（6小时）

3. **密码重置令牌有效期**：
   - 每次使用时读取最新配置
   - 配置格式错误时使用默认值（30分钟）

### 使用说明

1. **执行数据库脚本**：
   ```bash
   mysql -u root -p < database/update-20251226-add-system-configs.sql
   ```

2. **在管理后台修改配置**：
   - 登录管理后台，进入"系统设置" -> "基础配置"
   - 找到以下配置项进行编辑：
     - `mail.host`：邮件服务器地址
     - `mail.port`：邮件服务器端口
     - `mail.username`：发件人邮箱
     - `mail.password`：邮箱授权码
     - `order.payment-timeout-hours`：订单支付超时时间
     - `app.password.reset.token-expire-minutes`：密码重置令牌有效期

3. **配置修改后立即生效**：
   - 邮箱配置：下次发送邮件时生效
   - 订单配置：下次定时任务执行时生效
   - 密码重置令牌有效期：下次使用时生效

### 注意事项

1. **邮箱配置**：
   - 修改邮箱配置后，下次发送邮件时自动使用新配置
   - 如果配置错误，邮件发送会失败，需要检查配置
   - 邮箱授权码不是登录密码，需要在邮箱设置中生成

2. **订单配置**：
   - 修改订单支付超时时间后，下次定时任务执行时生效
   - 建议设置为合理的值（如6-24小时）

3. **密码重置令牌有效期**：
   - 修改后，新生成的令牌使用新的有效期
   - 已生成的令牌仍使用原来的有效期

4. **配置格式验证**：
   - 端口和有效期必须是数字
   - 配置格式错误时使用默认值，并记录警告日志

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/service/impl/MailConfigService.java` - 邮件配置服务（新建）
- ✅ `backend/src/main/java/com/shoppingmall/service/impl/EmailServiceImpl.java` - 邮件服务实现类
- ✅ `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderScheduledServiceImpl.java` - 订单定时任务服务
- ✅ `backend/src/main/java/com/shoppingmall/service/user/impl/UserServiceImpl.java` - 用户服务实现类
- ✅ `database/update-20251226-add-system-configs.sql` - 数据库配置初始化脚本

### 技术细节
- **配置读取优先级**：数据库配置 > 默认值
- **动态配置更新**：每次使用时读取最新配置，确保配置修改后立即生效
- **错误处理**：配置格式错误时使用默认值，并记录警告日志
- **性能考虑**：配置读取有缓存机制（`SystemConfigService`），性能影响可忽略

---

## 2025-12-26 - 密码重置邮件内容改为系统配置管理

### 功能说明
将密码重置邮件的内容（主题和正文）改为通过系统配置管理，支持管理员在后台自定义邮件模板，无需修改代码即可调整邮件内容。

### 修改原因
- 邮件内容硬编码在代码中，不便于修改
- 需要支持自定义邮件模板，适应不同业务场景
- 需要支持模板变量替换，动态生成邮件内容
- 提升系统的灵活性和可配置性

### 修改内容

#### 数据库配置

**新增配置项：**
- `database/update-20251226-add-mail-template-config.sql`
  - **`mail.password-reset.subject`**：密码重置邮件主题模板
    - 支持变量：`{username}`（用户名）、`{platform}`（平台名称）
    - 默认值：`密码重置验证码 - B2B采购平台`
  - **`mail.password-reset.content`**：密码重置邮件正文模板
    - 支持变量：`{username}`（用户名）、`{resetCode}`（验证码）、`{resetUrl}`（重置链接）、`{expireMinutes}`（有效期分钟数）、`{platform}`（平台名称）
    - 默认值：包含完整邮件正文模板
  - **`app.platform.name`**：平台名称
    - 默认值：`B2B采购平台`
    - 用于邮件模板等场景

#### 后端代码修改

**邮件服务实现类：**
- `backend/src/main/java/com/shoppingmall/service/impl/EmailServiceImpl.java`
  - **移除 `@Value` 注解**：不再从配置文件读取邮件相关配置
  - **添加 `SystemConfigService` 依赖**：用于从数据库读取配置
  - **添加配置读取方法**：
    - `getFromEmail()`：从数据库读取发件人邮箱（`mail.username`）
    - `getFrontendUrl()`：从数据库读取前端地址（`app.frontend.url`）
    - `getPlatformName()`：从数据库读取平台名称（`app.platform.name`）
  - **添加模板处理方法**：
    - `replaceTemplateVariables()`：替换模板变量
    - `getPasswordResetSubjectTemplate()`：获取邮件主题模板（带默认值）
    - `getPasswordResetContentTemplate()`：获取邮件正文模板（带默认值）
  - **修改 `sendPasswordResetEmail()` 方法**：
    - 从数据库读取邮件主题和正文模板
    - 准备模板变量映射（用户名、验证码、重置链接、有效期、平台名称）
    - 使用 `replaceTemplateVariables()` 替换模板变量
    - 发送邮件

### 功能特性
- ✅ 邮件主题和正文模板可配置
- ✅ 支持模板变量替换（`{username}`, `{resetCode}`, `{resetUrl}`, `{expireMinutes}`, `{platform}`）
- ✅ 配置修改后立即生效，无需重启服务
- ✅ 提供默认模板作为兜底机制
- ✅ 平台名称可配置，便于品牌定制

### 模板变量说明

支持的模板变量：
- `{username}` - 用户名
- `{resetCode}` - 重置验证码
- `{resetUrl}` - 重置密码链接（完整URL）
- `{expireMinutes}` - 验证码有效期（分钟数）
- `{platform}` - 平台名称（从 `app.platform.name` 配置读取）

### 使用说明

1. **在系统配置管理页面修改邮件模板**：
   - 登录管理后台，进入"系统设置" -> "基础配置"
   - 找到 `mail.password-reset.subject`（邮件主题）和 `mail.password-reset.content`（邮件正文）
   - 编辑模板内容，使用 `{变量名}` 作为占位符
   - 保存后立即生效

2. **模板示例**：
   ```
   主题：密码重置验证码 - {platform}
   
   正文：
   尊敬的 {username} 用户：
   
   您申请了密码重置，请使用以下验证码重置您的密码：
   
   验证码：{resetCode}
   
   或者点击以下链接直接重置密码：
   {resetUrl}
   
   此验证码有效期为{expireMinutes}分钟，请及时操作。
   如果您没有申请密码重置，请忽略此邮件。
   
   {platform}
   ```

3. **注意事项**：
   - 模板变量必须使用大括号包裹，如 `{username}`
   - 变量名区分大小写
   - 如果模板中使用了不存在的变量，该变量不会被替换（保持原样）
   - 建议在修改模板前先备份原模板

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/service/impl/EmailServiceImpl.java` - 邮件服务实现类
- ✅ `database/update-20251226-add-mail-template-config.sql` - 数据库配置初始化脚本

### 技术细节
- **配置读取优先级**：数据库配置 > 默认值
- **模板变量替换**：使用 `String.replace()` 方法替换模板变量
- **错误处理**：如果配置缺失，使用默认模板，确保邮件发送功能正常

---

## 2025-12-26 - 订单支付页面添加支付状态弹窗功能

### 功能说明
为订单支付页面添加类似预存款充值的支付状态弹窗功能，提升用户体验。支付过程中显示支付状态弹窗，支付成功后自动跳转到订单详情页面并显示支付成功提示。

### 修改原因
- 订单支付页面缺少支付状态反馈
- 用户支付后不知道支付是否成功
- 需要类似预存款充值的支付状态弹窗功能
- 支付成功后需要跳转到订单详情页面并提示支付成功

### 修改内容

#### 前端代码修改

**订单支付页面：**
- `frontend/src/views/order/Payment.vue`
  - **添加支付状态弹窗组件**：
    - 支付中状态（`paying`）：显示加载动画，提供"我已付款"和"付款有问题"按钮
    - 已付款状态（`paid`）：显示成功图标，自动跳转到订单详情页面
    - 付款有问题状态（`problem`）：显示错误图标，提供"联系客服"、"重新支付"、"关闭"按钮
  - **添加图标导入**：`Loading`、`CircleCheck`、`CircleClose`
  - **添加支付状态相关变量**：
    - `showPaymentStatusDialog`：控制弹窗显示
    - `paymentStatus`：支付状态（`paying` | `paid` | `problem` | `''`）
    - `currentPaymentOrderNo`：当前支付订单号
  - **修改 `processPayment` 方法**：
    - 预存款支付：显示"已付款"弹窗，2秒后跳转到订单详情页面
    - 模拟支付：显示"支付中"弹窗，支付成功后显示"已付款"弹窗
    - 真实支付（支付宝/微信）：显示"支付中"弹窗，在新窗口打开支付页面
    - 支付失败：显示"付款有问题"弹窗
  - **添加支付状态弹窗相关方法**：
    - `handleMarkAsPaid`：我已付款，跳转到订单详情页面
    - `handlePaymentProblem`：付款有问题，切换到问题状态
    - `handleContactService`：联系客服
    - `handleRetryPayment`：重新支付
    - `closePaymentStatusDialog`：关闭弹窗
  - **修改 `onMounted` 方法**：
    - 检查URL参数中的 `paymentStatus=success`
    - 如果存在，显示"已付款"弹窗和成功提示
    - 2秒后跳转到订单详情页面
  - **添加支付状态弹窗CSS样式**：
    - 支付中图标：蓝色，旋转动画
    - 成功图标：绿色
    - 错误图标：红色
    - 状态标题和描述样式
    - 按钮布局样式

### 功能特性
- ✅ 支付中状态弹窗（显示加载动画）
- ✅ 支付成功状态弹窗（自动跳转到订单详情）
- ✅ 支付问题状态弹窗（提供联系客服和重新支付选项）
- ✅ 支付成功后自动跳转到订单详情页面
- ✅ 订单详情页面显示支付成功提示
- ✅ 支持预存款支付、支付宝支付、微信支付

### 技术细节
- **支付状态流转**：
  - 预存款支付：直接成功 → 显示"已付款"弹窗 → 跳转订单详情
  - 模拟支付：显示"支付中" → 调用模拟接口 → 显示"已付款"弹窗 → 跳转订单详情
  - 真实支付：显示"支付中" → 打开支付页面 → 用户完成支付 → 回调跳转 → 显示"已付款"弹窗 → 跳转订单详情
- **弹窗控制**：
  - 支付中状态：不允许关闭（`show-close="false"`）
  - 已付款/问题状态：允许关闭
- **跳转逻辑**：
  - 支付成功回调：URL参数 `paymentStatus=success`
  - 跳转目标：`/order/detail?orderNumber={orderNo}`

### 使用说明
1. **支付流程**：
   - 用户选择支付方式，点击"立刻付款"
   - 如果是预存款支付，输入支付密码后直接成功
   - 如果是支付宝/微信支付，显示"支付中"弹窗，在新窗口打开支付页面
   - 用户完成支付后，支付宝回调跳转回订单支付页面
   - 系统检测到 `paymentStatus=success`，显示"已付款"弹窗
   - 2秒后自动跳转到订单详情页面

2. **支付状态弹窗操作**：
   - **我已付款**：跳转到订单详情页面，查看订单状态
   - **付款有问题**：切换到问题状态，提供联系客服和重新支付选项
   - **联系客服**：显示客服联系方式
   - **重新支付**：关闭弹窗，用户可以重新点击付款按钮

### 影响范围
- ✅ `frontend/src/views/order/Payment.vue` - 订单支付页面

### 注意事项
1. **支付中状态**：弹窗不允许关闭，防止用户误操作
2. **支付成功回调**：依赖URL参数 `paymentStatus=success`，需要确保后端回调正确设置
3. **跳转逻辑**：支付成功后会自动跳转到订单详情页面，并清除URL参数
4. **用户体验**：支付状态弹窗提供清晰的状态反馈，提升用户体验

---

## 2025-12-26 - 前端地址改为数据库配置方式

### 功能说明
将前端地址配置从配置文件改为数据库配置方式，支持在管理后台动态修改，无需重启服务即可生效。

### 修改原因
- 前端地址可能会变化（开发环境、测试环境、生产环境）
- 使用数据库配置可以动态修改，不需要重启服务
- 可以在管理后台配置，更方便管理
- 统一管理，与其他系统配置保持一致

### 修改内容

#### 1. 后端代码修改

**Controller：**
- `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
  - 添加 `SystemConfigService` 依赖注入
  - 优化 `getFrontendUrl` 方法：
    - **优先级1**：从数据库配置读取（`app.frontend.url`）
    - **优先级2**：从 `Referer` 头提取（排除支付宝域名和API路径）
    - **优先级3**：内网穿透场景（natapp等）
    - **优先级4**：配置文件默认值（`app.frontend.url`）
  - 添加详细日志记录每个优先级的使用情况

#### 2. 数据库脚本

**SQL脚本：**
- `database/update-20251226-add-frontend-url-config.sql`（新建）
  - 添加 `app.frontend.url` 配置项到 `system_config` 表
  - 默认值：`http://localhost:3002`
  - 配置类型：`text`
  - 状态：启用（`status = 1`）
  - 使用 `ON DUPLICATE KEY UPDATE` 确保幂等性

### 功能特性
- ✅ 支持从数据库动态读取前端地址配置
- ✅ 支持在管理后台修改前端地址
- ✅ 修改后立即生效，无需重启服务
- ✅ 多级降级方案，确保系统稳定性
- ✅ 详细日志记录，便于排查问题

### 技术细节
- **配置键**：`app.frontend.url`
- **优先级顺序**：
  1. 数据库配置（`system_config` 表）
  2. Referer头（排除支付宝域名）
  3. 内网穿透场景（natapp）
  4. 配置文件默认值（`application.yml`）
- **日志级别**：INFO级别记录每个优先级的使用情况

### 使用说明
1. **执行数据库脚本**：
   ```sql
   -- 执行 database/update-20251226-add-frontend-url-config.sql
   ```

2. **管理后台配置**：
   - 登录管理后台
   - 进入"系统设置" -> "基础配置"
   - 找到"前端地址"配置项
   - 修改为实际的前端地址（如：`http://your-domain.com`）
   - 保存后立即生效

3. **多环境配置**：
   - 开发环境：`http://localhost:3002`
   - 测试环境：`http://test.example.com`
   - 生产环境：`https://www.example.com`

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
- ✅ `database/update-20251226-add-frontend-url-config.sql`（新建）

### 注意事项
1. **数据库配置优先级最高**：如果数据库中有配置，会优先使用数据库配置
2. **配置格式**：前端地址应该包含协议（http/https），不需要末尾斜杠
3. **立即生效**：修改数据库配置后，下次支付回调时会立即使用新地址
4. **降级方案**：如果数据库配置不存在或读取失败，会自动降级到其他方案

---

## 2025-12-26 - 修复预存款充值支付成功回调跳转404问题

### 问题描述
预存款支付宝支付成功后，跳转回调页面出现404错误。错误URL：`http://n44a6799.natappfree.cc/member/deposit/recharge?paymentStatus=success`

### 问题原因
`getFrontendUrl` 方法从支付宝回调的请求URL中提取前端地址，但支付宝回调的请求URL是后端地址（`http://n44a6799.natappfree.cc/api/buyer/payment/alipay/return`），导致提取出来的前端地址也是后端地址，重定向URL错误。

### 解决方案
1. **添加配置支持**：使用 `@Value` 注解注入 `app.frontend.url` 配置项作为默认前端地址
2. **优化提取逻辑**：
   - 优先从 `Referer` 头提取前端地址（排除支付宝域名和API路径）
   - 内网穿透场景：前端和后端使用同一个域名，保持原域名
   - 其他场景：使用配置的默认前端地址
3. **添加详细日志**：记录前端地址提取过程，便于排查问题

### 修改内容

**Controller：**
- `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
  - 添加 `@Value("${app.frontend.url:http://localhost:3002}")` 注入默认前端地址
  - 优化 `getFrontendUrl` 方法：
    - 优先从 `Referer` 头提取（排除支付宝域名和API路径）
    - 内网穿透场景特殊处理（保持原域名）
    - 其他场景使用配置的默认前端地址
    - 添加详细日志记录提取过程

### 功能特性
- ✅ 支持从配置文件读取前端地址
- ✅ 优化内网穿透场景的前端地址提取
- ✅ 添加详细日志便于排查问题
- ✅ 支持多种场景的前端地址提取

### 技术细节
- **配置项**：`app.frontend.url`（默认值：`http://localhost:3002`）
- **提取优先级**：
  1. Referer头（排除支付宝域名和API路径）
  2. 内网穿透场景：保持原域名
  3. 配置的默认前端地址
- **日志级别**：INFO级别记录提取过程

### 使用说明
1. **配置前端地址**：
   - 在 `application.yml` 中配置 `app.frontend.url`
   - 生产环境建议配置为实际的前端域名

2. **内网穿透场景**：
   - 如果前端和后端使用同一个域名（如natapp），系统会自动识别
   - 确保前端和后端使用相同的协议和域名

3. **查看日志**：
   - 支付回调时会记录前端地址提取过程
   - 如果提取失败，会使用配置的默认地址

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`

### 注意事项
1. **配置文件**：确保 `application.yml` 中配置了正确的前端地址
2. **内网穿透**：如果使用内网穿透工具，确保前端和后端使用相同的域名
3. **生产环境**：生产环境需要配置正确的前端域名

---

## 2025-12-26 - 添加预存款支付并发控制（悲观锁）

### 功能说明
为预存款支付、退款、充值回调等关键操作添加数据库悲观锁（`SELECT FOR UPDATE`），防止并发操作导致余额超支或数据不一致问题。

### 修改原因
- **风险评估**：预存款支付没有做冻结业务，存在并发风险
- **问题场景**：用户同时支付多个订单时，可能出现余额超支（如余额100元，同时支付80元和50元，都通过余额检查，导致余额变成-30元）
- **当前保护措施不足**：虽然使用了 `@Transactional` 事务，但 `selectOne` 和 `updateById` 之间没有锁保护

### 修改内容

#### 后端代码修改

**Service实现：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/DepositServiceImpl.java`
  - **`depositPayment` 方法**（预存款支付）：
    - 在查询预存款账户时添加 `.last("FOR UPDATE")` 悲观锁
    - 防止并发支付导致余额超支
    - 添加注释说明锁的作用
  - **`depositRefund` 方法**（预存款退款）：
    - 在查询预存款账户时添加 `.last("FOR UPDATE")` 悲观锁
    - 防止并发退款导致余额不一致
    - 优化余额计算逻辑，处理null值
  - **`handlePaymentCallback` 方法**（支付回调处理）：
    - 在查询预存款账户时添加 `.last("FOR UPDATE")` 悲观锁
    - 防止并发充值回调导致余额不一致
    - 优化余额计算逻辑，处理null值

### 功能特性
- ✅ 预存款支付使用悲观锁，防止并发超支
- ✅ 预存款退款使用悲观锁，防止余额不一致
- ✅ 充值回调使用悲观锁，防止余额不一致
- ✅ 优化余额计算逻辑，处理null值情况

### 技术细节
- **悲观锁实现**：使用 `SELECT FOR UPDATE` 在事务中对预存款账户行加锁
- **锁范围**：只锁定当前用户的预存款账户行，不影响其他用户
- **事务隔离**：配合 `@Transactional` 注解，确保事务内数据一致性
- **性能影响**：行锁粒度小，性能影响可控，适合高并发场景

### 风险评估对比

| 风险项 | 修改前 | 修改后 |
|--------|--------|--------|
| 并发支付超支 | 🔴 高风险 | ✅ 已解决 |
| 余额显示不准确 | 🟡 中等风险 | ✅ 已解决 |
| 并发退款不一致 | 🟡 中等风险 | ✅ 已解决 |
| 并发充值不一致 | 🟡 中等风险 | ✅ 已解决 |

### 使用说明
1. **预存款支付**：
   - 用户支付订单时，系统会自动对预存款账户加锁
   - 确保同一用户同时支付多个订单时，余额检查是串行的
   - 防止余额超支问题

2. **预存款退款**：
   - 退款时对预存款账户加锁
   - 确保退款操作的原子性

3. **充值回调**：
   - 支付回调处理时对预存款账户加锁
   - 防止并发回调导致余额重复增加

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/service/buyer/impl/DepositServiceImpl.java`

### 注意事项
1. **锁粒度**：只锁定当前用户的预存款账户行，不影响其他用户的操作
2. **事务要求**：必须在事务中使用，确保锁在事务提交后释放
3. **性能考虑**：行锁性能影响小，适合高并发场景
4. **未来优化**：如果未来需要更复杂的资金管理（如订单取消解冻、退款冻结等），可以考虑实现冻结机制

### 相关文档
- `docs/预存款冻结金额业务说明.md` - 冻结金额业务说明文档

---

## 2025-12-26 - 修复支付宝支付回调问题（订单支付和预存款充值）

### 问题描述
1. **订单支付回调问题**：支付宝支付成功后，`return_url` 直接指向前端页面，导致404错误
2. **预存款充值回调问题**：预存款充值支付成功后，没有跳转到充值成功页面

### 解决方案
1. **统一回调接口**：`return_url` 必须指向后端接口，后端验证签名后重定向到前端页面
2. **区分订单类型**：根据订单号前缀（`DEPOSIT_`）判断是订单支付还是预存款充值
3. **统一异步回调**：订单支付和预存款充值都使用统一的异步回调接口

---

## 2025-12-26 - 添加支付宝支付成功回调功能（订单支付）

### 功能说明
添加支付宝支付成功后的同步回调功能，用户支付成功后可以跳转回平台的订单详情页面，并显示支付成功提示。

### 修改原因
- 支付宝支付成功后，用户停留在支付宝的成功页面，无法自动跳转回平台
- 需要添加 `return_url` 参数，让用户支付成功后跳转到订单详情页面
- 需要在订单详情页面显示支付成功提示

### 修改内容

#### 1. 后端代码修改

**工具类：**
- `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`
  - 修改 `createPagePayment` 方法签名，添加 `returnUrl` 参数
  - 清理 `returnUrl` 中的特殊字符
  - 将 `return_url` 参数添加到签名参数中（必须参与签名）
  - 记录 `returnUrl` 的清理前后日志

**支付策略：**
- `backend/src/main/java/com/shoppingmall/payment/strategy/impl/AlipayPayStrategy.java`
  - **关键修复**：`return_url` 必须指向后端接口，不能直接指向前端页面
  - 在 `createPayment` 方法中构建 `return_url`
  - 从 `notifyUrl` 中提取基础URL（协议+域名+端口）
  - 构建 `return_url`：`/api/buyer/payment/alipay/return`（后端接口）
  - 后端接口会验证签名后重定向到前端页面：`/order/detail?orderNumber={orderNo}&paymentStatus=success`
  - 将 `returnUrl` 传递给 `AlipayUtil.createPagePayment`

**回调控制器：**
- `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
  - **关键修复**：分离同步回调和异步回调接口
  - 新增 `alipayReturn` 方法（GET）：处理同步回调（return_url）
    - 验证签名
    - 处理业务逻辑
    - 重定向到前端订单详情页面
  - 修改 `alipayNotify` 方法（POST）：处理异步回调（notify_url）
    - 验证签名
    - 处理业务逻辑
    - 返回"success"
  - 添加 `getFrontendUrl` 方法，从请求中提取前端地址
  - 添加 `escapeHtml` 方法，防止XSS攻击

**DTO：**
- `backend/src/main/java/com/shoppingmall/dto/PaymentRequestDTO.java`
  - 添加 `frontendUrl` 字段，用于传递前端地址

**Service：**
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
  - 在 `payOrder` 方法中，如果 `OrderPaymentDTO` 包含 `frontendUrl`，则传递给支付请求

#### 2. 前端代码修改

**订单详情页面：**
- `frontend/src/views/order/Detail.vue`
  - 在 `loadOrderDetail` 方法中检查URL参数 `paymentStatus`
  - 如果 `paymentStatus=success`，显示支付成功提示
  - 清除URL参数中的 `paymentStatus`，避免刷新时重复提示

### 功能特性
- ✅ 支付宝支付成功后自动跳转到订单详情页面
- ✅ 订单详情页面显示支付成功提示
- ✅ 支持同步回调（return_url）和异步回调（notify_url）
- ✅ 自动从notifyUrl提取前端地址
- ✅ 支持手动指定前端地址

### 技术细节
- **同步回调（return_url）**：
  - **重要**：`return_url` 必须指向后端接口，不能直接指向前端页面
  - 后端接口：`/api/buyer/payment/alipay/return`（GET请求）
  - 支付宝会带着回调参数跳转到这个接口
  - 后端验证签名 → 处理业务逻辑 → 重定向到前端页面
  - 前端页面URL格式：`/order/detail?orderNumber={orderNo}&paymentStatus=success`
  - 使用JavaScript和meta refresh双重跳转，兼容性更好
  
- **异步回调（notify_url）**：
  - 后端接口：`/api/buyer/payment/alipay/notify`（POST请求）
  - 支付宝服务器主动调用
  - 返回"success"字符串
  - 处理订单状态更新等业务逻辑

- **return_url构建**：
  - 从 `notifyUrl` 中提取基础URL（协议+域名+端口）
  - 拼接后端接口路径：`/api/buyer/payment/alipay/return`
  - 如果提取失败，使用默认值或从notifyUrl替换路径

- **前端地址提取**：
  - 从请求的Referer或请求URL中提取
  - 如果提取失败，使用默认值 `http://localhost:3002`
  - 支持手动指定 `frontendUrl`

### 使用说明
1. **支付流程**：
   - 用户选择支付宝支付
   - 跳转到支付宝支付页面
   - 支付成功后，支付宝会调用 `return_url`（同步回调）
   - 系统验证签名后，重定向到订单详情页面
   - 订单详情页面显示支付成功提示

2. **回调处理**：
   - 同步回调（GET）：验证签名 → 处理业务逻辑 → 重定向到订单详情
   - 异步回调（POST）：验证签名 → 处理业务逻辑 → 返回"success"

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`
- ✅ `backend/src/main/java/com/shoppingmall/payment/strategy/impl/AlipayPayStrategy.java`
- ✅ `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
- ✅ `backend/src/main/java/com/shoppingmall/dto/PaymentRequestDTO.java`
- ✅ `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
- ✅ `frontend/src/views/order/Detail.vue`

### 注意事项
1. **return_url必须指向后端接口**：
   - ❌ 错误：`return_url` 直接指向前端页面（会导致404，因为前端无法处理支付宝回调参数）
   - ✅ 正确：`return_url` 指向后端接口 `/api/buyer/payment/alipay/return`
   - 后端接口验证签名后，再重定向到前端页面

2. **return_url必须参与签名**：支付宝要求 `return_url` 参数必须参与签名

3. **前端地址配置**：生产环境需要配置正确的前端地址

4. **URL编码**：订单号需要进行URL编码，避免特殊字符问题

5. **重定向方式**：使用JavaScript和meta refresh双重跳转，兼容性更好

6. **安全防护**：重定向URL需要进行HTML转义，防止XSS攻击

---

## 2025-12-26 - 支付宝支付签名问题总结

### 问题总结

在调试支付宝支付功能时，遇到了 `invalid-signature` 验签错误。经过排查，发现两个关键问题：

#### 问题1：charset参数处理
- **错误理解**：认为 `charset` 参数只放在URL中，不参与签名
- **正确做法**：`charset` 参数必须**同时**满足：
  1. 放在URL查询字符串中（`gateway.do?charset=utf-8`）
  2. 参与签名（支付宝验签时会包含它）

#### 问题2：参数值URL编码
- **错误做法**：签名时对参数值进行URL编码（`timestamp=2025-12-26+15%3A57%3A58`）
- **正确做法**：签名时使用**原始值**（`timestamp=2025-12-26 15:57:58`）
- **原因**：支付宝验签字符串中的参数值都是未编码的原始值

### 核心要点

1. ✅ `charset` 参数必须放在URL中，同时参与签名
2. ✅ 签名时使用原始值，不进行URL编码
3. ✅ 参数按字典序排序
4. ✅ 确保私钥和公钥匹配

### 详细文档

已创建详细的排查指南：`docs/支付宝支付签名问题排查指南.md`

---

## 2025-12-26 - 修复支付宝支付验签错误并添加详细日志

### 功能说明
修复支付宝支付时的验签错误（invalid-signature），添加详细的日志用于排查签名问题。

### 修改原因
- 支付宝支付时报错：`invalid-signature`，验签出错
- 错误信息：`请确认charset参数放在了URL查询字符串中且各参数值使用charset参数指示的字符集编码`
- 需要添加详细日志排查签名生成和参数编码问题

### 修改内容

#### 1. 修复签名参数问题（关键修复）

**工具类：**
- `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`
  - **修复关键问题**：将 `charset` 参数从签名参数中移除，放在URL查询字符串中
    - 之前：`charset` 参数包含在签名参数中
    - 现在：`charset` 参数不参与签名，只放在URL查询字符串中
    - 支付宝要求：`charset` 必须放在URL查询字符串中，不参与签名
    - 签名时不包含 `charset` 参数
  - **添加详细日志**：
    - 在 `createPagePayment` 方法中添加签名前的参数列表日志
    - 记录订单号、金额、AppID等关键信息
    - 记录生成的签名值
  - **优化 `getSignContent` 方法**：
    - 明确跳过 `sign` 参数和空值
    - 添加每个参数的编码前后值日志（debug级别）
    - 记录完整的待签名字符串
  - **优化 `sign` 方法**：
    - 添加签名过程的详细日志
    - 支持处理 `RSA PRIVATE KEY` 格式的私钥（兼容性）
    - 记录私钥长度和签名结果长度
    - 异常时记录私钥前100字符用于排查
  - **优化 `buildFormHtmlWithCharset` 方法**：
    - 移除URL中的 `charset` 参数（因为已包含在表单参数中）
    - 添加表单参数列表日志
    - 记录生成的表单HTML长度

### 问题分析
1. **根本原因**：`charset` 参数需要参与签名
   - 支付宝要求：`charset` 参数必须放在URL查询字符串中（如：`gateway.do?charset=utf-8`）
   - 但同时：`charset` 参数也必须参与签名（支付宝验签时会从URL中读取并包含在验签字符串中）
   - 正确做法：
     - `charset` 放在URL查询字符串中（作为URL参数）
     - `charset` 也包含在签名参数中（参与签名）
   - 支付宝验签字符串格式：`...&charset=utf-8&...`（说明charset参与了验签）

2. **参数编码**（关键修复）：
   - **签名时**：使用**原始值**（不进行URL编码）
   - **原因**：支付宝验签字符串中的参数值都是未编码的原始值（如：`timestamp=2025-12-26 15:57:58`）
   - **表单提交时**：浏览器会自动对表单参数进行URL编码
   - **支付宝处理**：接收到编码后的值，但在验签前会先解码，使用解码后的值进行验签
   - **正确做法**：签名时使用原始值，确保与支付宝验签时使用的值一致

3. **私钥格式**：
   - 支持 `BEGIN PRIVATE KEY` 格式（PKCS#8）
   - 也支持 `BEGIN RSA PRIVATE KEY` 格式（兼容性）

### 功能特性
- ✅ 修复签名参数问题（`charset` 放在URL中，同时参与签名）
- ✅ 添加详细的签名过程日志
- ✅ 记录签名前后的参数值
- ✅ 记录待签名字符串（包含 `charset`）
- ✅ 记录未编码版本的签名字符串（用于对比）
- ✅ 记录签名结果
- ✅ 支持多种私钥格式
- ✅ 异常时记录关键信息用于排查

### 技术细节
- **签名参数顺序**：按字典序排序（支付宝要求）
- **参数编码**：**使用原始值，不进行URL编码**（关键修复）
  - 之前错误：签名时对参数值进行URL编码（`timestamp=2025-12-26+15%3A57%3A58`）
  - 现在正确：签名时使用原始值（`timestamp=2025-12-26 15:57:58`）
  - 原因：支付宝验签字符串中的参数值都是未编码的原始值
- **签名算法**：SHA256withRSA（RSA2）
- **参数过滤**：跳过 `sign` 参数和空值参数
- **日志级别**：
  - INFO：关键步骤和结果
  - DEBUG：详细的参数信息

### 使用说明
1. **查看日志**：
   - 支付时查看日志中的"支付宝支付签名参数"部分
   - 检查待签名字符串是否正确
   - 检查签名是否成功生成

2. **排查问题**：
   - 如果仍然报错，检查日志中的参数列表
   - 确认私钥格式是否正确
   - 确认参数编码是否正确

3. **常见问题**：
   - 私钥格式错误：确保私钥包含 `BEGIN PRIVATE KEY` 和 `END PRIVATE KEY`
   - **参数编码错误**：签名时**不要**对参数值进行URL编码，使用原始值
   - 参数缺失：确保包含所有必需参数（包括 `charset`）
   - 时间戳格式：使用原始格式（`2025-12-26 15:57:58`），不要编码为 `2025-12-26+15%3A57%3A58`

### 影响范围
- ✅ `backend/src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`

### 注意事项
1. **`charset` 参数处理**：
   - ✅ 必须放在URL查询字符串中（如：`gateway.do?charset=utf-8`）
   - ✅ 同时必须参与签名（支付宝验签时会包含它）
   - 注意：虽然charset在URL中，但签名时需要把它当作表单参数一样处理
2. **签名参数**：包含所有表单参数 + charset参数（charset在URL中，但参与签名）
3. **参数编码**：签名时所有参数值需要进行URL编码（UTF-8）
3. **日志级别**：建议在生产环境将日志级别设置为 INFO，避免过多日志
4. **敏感信息**：日志中会记录私钥的部分信息，注意日志安全
5. **参数顺序**：确保参数按字典序排序（代码已实现）
6. **编码格式**：确保所有参数使用 UTF-8 编码

---

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
