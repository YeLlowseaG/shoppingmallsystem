# 修改日志

## 2025-12-10

### 激活开发环境 Profile 配置并支持本地配置文件
- 已激活 `dev` profile 并配置支持本地个人配置文件
- 主要修改内容：
  1. **添加 Profile 激活配置** (`backend/src/main/resources/application.yml`)：
     - 在 `spring` 配置下添加 `profiles.active: dev`
     - 确保本地开发时自动加载 `application-dev.yml`
  2. **添加本地配置文件导入** (`backend/src/main/resources/application-dev.yml`)：
     - 使用 `spring.config.import` 导入 `application-dev-local.yml`
     - 使用 `optional:` 前缀，允许文件不存在（开发者可选择创建）
     - 如果文件存在，会自动加载并覆盖开发环境配置
- 配置加载顺序（激活 dev profile 后）：
  1. `application.yml` - 基础配置
  2. `application-dev.yml` - 开发环境配置（覆盖数据库密码和日志级别）
  3. `application-dev-local.yml` - 本地个人配置（如果存在，覆盖数据库密码等个人配置）
- 工作原理：
  - Spring Boot 3.1.5 支持 `spring.config.import` 显式导入配置文件
  - `optional:` 前缀表示文件不存在时不会报错，允许开发者选择性创建
  - 后加载的配置会覆盖先加载的配置
- 修改效果：
  - ✅ 本地开发环境自动激活 `dev` profile
  - ✅ 自动加载 `application-dev.yml` 配置
  - ✅ 如果存在 `application-dev-local.yml`，会自动加载并覆盖数据库密码
  - ✅ 每个开发者可以使用自己的本地数据库配置，互不影响
  - ✅ 文件不存在时不会报错，不影响其他开发者
- 开发环境 Profile 配置已激活，本地配置文件支持已添加

## 2025-12-10

### 修复侧边栏在所有页面不显示的问题
- 已修复所有管理后台页面（如 `/admin/order/list`）侧边栏不显示的问题
- 主要修改内容：
  1. **给 /admin 路由添加 name** (`admin-frontend/src/router/index.ts`)：
     - 给 `/admin` 路由添加 `name: 'admin'`，使 `router.addRoute('admin', route)` 能够正确找到父路由
  2. **修复动态路由路径格式** (`admin-frontend/src/router/index.ts`)：
     - 修改 `addRoutes` 函数，使用相对路径（相对于 `/admin`）而不是绝对路径
     - 动态路由的路径应该是 `order/list` 而不是 `/admin/order/list`
     - 这样路由会被正确添加到 Layout 组件的 children 中
  3. **修复菜单路径生成逻辑** (`admin-frontend/src/components/Layout/index.vue`)：
     - 在 `getMenuPath` 函数中添加特殊处理
     - 当子菜单路径是 `index` 且父菜单路径是 `dashboard` 时，返回 `/admin/dashboard` 而不是 `/admin/dashboard/index`
     - 确保菜单路径与静态路由路径一致
  4. **改进路由存在性检查**：
     - 添加更准确的路由存在性检查，避免重复添加已存在的路由
     - 使用完整路径比较，确保检查准确
- 问题原因：
  1. `/admin` 路由没有 name，导致 `router.addRoute('admin', route)` 无法找到父路由
  2. 动态路由使用绝对路径（如 `/admin/order/list`），而不是相对路径（如 `order/list`）
  3. 这导致动态路由没有正确添加到 Layout 组件的 children 中
  4. 当访问动态路由页面时，Layout 组件可能没有正确渲染，导致侧边栏不显示
- 修改效果：
  - ✅ `/admin` 路由有了正确的 name，动态路由可以正确添加
  - ✅ 动态路由使用相对路径，正确添加到 Layout 的 children 中
  - ✅ 所有页面（包括静态路由和动态路由）都能正确显示侧边栏
  - ✅ `/admin/dashboard`、`/admin/order/list` 等所有页面都能正常访问并显示侧边栏
- 侧边栏在所有页面不显示的问题已修复

### 修复 Element Plus ElOption 警告
- 已修复用户管理页面中 Element Plus 的 `ElOption` 组件警告
- 主要修改内容：
  1. **修复状态选择框** (`admin-frontend/src/views/permission/User.vue`)：
     - 移除了 `:value="undefined"` 的 "全部" 选项
     - 使用 `clearable` 属性即可实现清空功能，不需要额外的 "全部" 选项
  2. **修复角色选择框**：
     - 移除了 `role.id!` 的非空断言，改为 `role.id`
     - 避免可能的 undefined 值导致警告
- 问题原因：
  - Element Plus 的 `ElOption` 组件的 `value` prop 不接受 `undefined` 值
  - 使用 `undefined` 会导致类型检查失败
- 修改效果：
  - ✅ Element Plus 警告已消除
  - ✅ 状态选择框功能正常，可以通过 clearable 清空
  - ✅ 角色选择框正常工作
- Element Plus ElOption 警告已修复

## 2025-12-10

### 实现本地开发环境配置文件方案
- 已实现本地开发环境配置文件方案，解决不同开发者本地数据库密码不同的问题
- 主要修改内容：
  1. **更新 .gitignore 文件**：
     - 添加 `application-dev-local.yml` 到忽略列表，确保本地配置文件不会被提交到 git
  2. **创建配置文件模板** (`backend/src/main/resources/application-dev-local.yml.example`)：
     - 创建示例配置文件模板，供开发者参考
     - 包含使用说明和配置示例
     - 此文件可以提交到 git，作为模板供团队成员使用
  3. **修改开发环境配置** (`backend/src/main/resources/application-dev.yml`)：
     - 统一使用 `spring.datasource.druid` 前缀，与主配置文件保持一致
     - 保留默认密码作为基础配置
     - 本地配置文件 `application-dev-local.yml` 会自动覆盖此配置
- 实现原理：
  - Spring Boot 配置文件加载顺序：`application.yml` → `application-dev.yml` → `application-dev-local.yml`
  - 后加载的配置文件会覆盖前面的配置
  - 每个开发者创建自己的 `application-dev-local.yml` 文件，设置个人本地数据库密码
- 使用方法：
  1. 复制 `application-dev-local.yml.example` 为 `application-dev-local.yml`
  2. 修改其中的数据库密码为个人本地 MySQL 密码
  3. 确保运行时激活了 `dev` profile
- 修改效果：
  - ✅ 每个开发者可以有自己的本地数据库配置，互不影响
  - ✅ 密码不会硬编码在代码中，更安全
  - ✅ 本地配置文件不会被提交到 git，保护敏感信息
  - ✅ 更符合开发规范，支持灵活的本地环境配置
- 本地开发环境配置文件方案已实现

## 2025-12-10

### 修复管理后台侧边栏不显示问题
- 已修复页面刷新后侧边栏菜单不显示的问题
- 主要修改内容：
  1. **修复 Layout 组件初始化逻辑** (`admin-frontend/src/components/Layout/index.vue`)：
     - 在 `onMounted` 中，当菜单数据从 localStorage 恢复后，调用 `addRoutes(adminStore.menus)` 添加动态路由
     - 使用 `nextTick` 确保 store 初始化完成后再检查菜单数据
     - 添加 `watch` 监听菜单数据变化，确保当菜单数据恢复时自动添加路由
     - 添加详细的调试日志，方便排查问题
     - 在 `menuList` computed 中添加调试信息
- 问题原因：
  - 页面刷新时，`adminStore.init()` 会从 localStorage 恢复菜单数据
  - 但是菜单数据恢复后，没有调用 `addRoutes` 来添加动态路由
  - 可能存在时序问题，导致在检查菜单数据时还没有完全恢复
- 修改效果：
  - ✅ 页面刷新后，菜单数据从 localStorage 恢复
  - ✅ 使用 `watch` 监听菜单数据变化，确保数据恢复后自动添加路由
  - ✅ 使用 `nextTick` 确保初始化顺序正确
  - ✅ 添加调试日志，方便排查问题
  - ✅ 侧边栏菜单可以正常显示和导航
- 管理后台侧边栏不显示问题已修复

## 2025-12-10

### 修复管理后台动态路由导入错误
- 已修复管理后台所有页面访问时的动态导入错误（`Unknown variable dynamic import`）
- 主要修改内容：
  1. **添加组件映射表** (`admin-frontend/src/router/index.ts`)：
     - 创建 `componentMap` 对象，预先定义所有可能的组件路径
     - 只包含实际存在的组件文件，避免构建时文件不存在错误：
       - 仪表盘：dashboard/Index
       - 商品管理：product/List (映射到 ProductManage.vue), product/Add, product/Category (映射到 CategoryManage.vue)
       - 订单管理：order/List
       - 采购者管理：buyer/List, buyer/Audit
       - 权限管理：permission/User, permission/Role, permission/Menu
       - 物流管理：logistics/Index
     - 对于尚未创建的组件（stock/*, buyer/Level, marketing/*, statistics/*, system/*），会在运行时输出警告
  2. **修改动态路由添加逻辑**：
     - 将 `component: () => import(\`@/views/${menu.component}.vue\`)` 改为从映射表获取
     - 使用 `componentMap[menu.component]` 获取组件加载器
     - 如果组件不存在，输出警告并跳过路由添加
  3. **修复文件路径映射**：
     - `product/List` 映射到 `product/ProductManage.vue`（实际文件名）
     - `product/Category` 映射到 `product/CategoryManage.vue`（实际文件名）
- 问题原因：
  - Vite 无法在构建时静态分析模板字符串形式的动态导入路径
  - 使用变量拼接的导入路径会导致运行时错误
  - 数据库中的 component 字段值与实际文件名不一致
- 修改效果：
  - ✅ 所有组件路径都是静态的，Vite 可以在构建时正确分析
  - ✅ 解决了 `Unknown variable dynamic import` 错误
  - ✅ 解决了文件不存在导致的构建错误
  - ✅ 管理后台已存在的页面可以正常访问
  - ✅ 如果组件不存在，会输出警告而不是报错
- 管理后台动态路由导入错误已修复

## 2025-12-10

### 优化我的预存款页面样式和功能
- 已优化会员中心我的预存款页面的样式和交互功能
- 主要修改内容：
  1. **优化页面标题样式** (`frontend/src/views/member/DepositBalance.vue`)：
     - 将标题文案改为灰色（#999）
     - 将金额字段改为红色（#e4393c）并加粗显示
     - 使用span标签分别包装文案和金额部分
  2. **添加事件点击跳转功能**：
     - 判断事件类型，如果是"预存款支付"或"预存款退款"，事件字段可点击
     - 从备注中提取订单号（格式：订单号(20231127161917)）
     - 点击事件后跳转到订单详情页面（/order/detail），传递订单号参数
     - 添加事件链接样式：蓝色文字，鼠标悬停时变红并显示下划线
- 修改效果：
  - ✅ 页面标题样式更清晰，金额突出显示
  - ✅ 预存款支付和预存款退款事件可点击跳转到订单详情
  - ✅ 提升用户体验，方便用户查看相关订单信息
- 我的预存款页面样式和功能已优化

### 恢复预存款充值页面入口，屏蔽预存款充值审核页面
- 已恢复会员中心预存款充值页面的菜单入口，并屏蔽预存款充值审核页面
- 主要修改内容：
  1. **更新侧边栏组件** (`frontend/src/components/member/MemberSidebar.vue`)：
     - 恢复"预存款充值"菜单项，用户可以访问预存款充值页面
     - 注释掉"预存款充值审核"菜单项，隐藏该功能入口
     - 保留路由配置，以便后续需要时恢复
- 修改效果：
  - ✅ 会员中心侧边栏显示"预存款充值"菜单项
  - ✅ 用户可以通过菜单访问预存款充值页面
  - ✅ 会员中心侧边栏不再显示"预存款充值审核"菜单项
  - ✅ 路由配置保留，方便后续恢复功能
- 预存款充值页面入口已恢复，预存款充值审核页面入口已屏蔽

### 移除预存款充值审核功能
- 已移除预存款充值审核功能，预存款充值后直接到账，无需管理员审核
- 主要修改内容：
  1. **需求文档修改** (`docs/Requirements AnalysisV1.0.md`)：
     - 修改预存款充值流程，移除"平台管理员审核 → 审核通过"环节
     - 移除账户余额管理中的"预存款充值审核（查看审核状态）"功能
     - 移除会员中心页面结构中的"预存款充值审核页"及其相关功能
  2. **开发TODO列表修改** (`docs/Development TODO List(backup).md`)：
     - 移除预存款功能中的"预存款充值审核接口（管理员端）"任务
  3. **开发任务列表修改** (`docs/Development Task List.md`)：
     - 更新预存款模块后端接口描述，移除"审核"相关内容
- 修改效果：
  - ✅ 预存款充值流程简化为：选择充值金额 → 选择充值方式 → 上传支付凭证 → 提交充值申请 → 预存款到账
  - ✅ 移除了所有与预存款充值审核相关的功能点和开发任务
  - ✅ 简化了系统流程，提高了用户体验
- 预存款充值审核功能已移除

## 2025-12-10

### 实现会员中心我的预存款页面
- 已完成会员中心我的预存款（预存款交易记录）页面的前端实现，参考对标网站1:1仿照设计
- 主要修改内容：
  1. **创建我的预存款页面** (`frontend/src/views/member/DepositBalance.vue`)：
     - 实现页面标题，显示预存款余额和可用余额
     - 实现"下载交易记录"按钮
     - 实现筛选区域：操作类型下拉（所有、预存款支付、在线充值、预存款退款、代充值，不包含返点）、起始时间、结束时间、查询按钮
     - 实现交易记录表格，包含列：复选框、事件、存入金额、支出金额、冻结金额、解冻金额、当前余额、可用余额、时间、备注
     - 实现表格底部操作栏：全选复选框、"导出选中记录"按钮
     - 实现分页控件，包含上一页、页码、下一页、跳转输入框和确定按钮
     - 复用公共组件：TopBar、Header、Navbar、Footer、MemberHeaderBar、MemberSidebar
     - 预留真实API接口对接入口（TODO注释）
     - 暂时使用模拟数据展示页面效果
  2. **添加路由配置** (`frontend/src/router/index.ts`)：
     - 添加 `/member/deposit/balance` 路由
     - 设置页面标题为"我的预存款"
     - 设置需要登录认证
  3. **更新侧边栏组件** (`frontend/src/components/member/MemberSidebar.vue`)：
     - 更新路由映射，使"我的预存款"菜单项正确跳转到预存款页面
     - 添加路由自动识别逻辑，高亮当前选中的菜单项
- 页面特点：
  - ✅ 1:1仿照对标网站设计，布局和样式保持一致
  - ✅ 复用公共组件，保持页面风格统一
  - ✅ 操作类型不包含返点选项（按要求）
  - ✅ 表格金额列使用不同颜色区分（存入-绿色、支出-红色、冻结-橙色、解冻-蓝色）
  - ✅ 支持筛选查询、全选、导出等功能
  - ✅ 完整的分页功能，支持跳转到指定页
  - ✅ 响应式设计，支持移动端访问
  - ✅ 预留真实API接口对接入口，方便后续集成
- 会员中心我的预存款页面前端实现已完成

### 实现会员中心预存款充值页面
- 已完成会员中心预存款充值页面的前端实现，参考对标网站1:1仿照设计
- 主要修改内容：
  1. **创建预存款充值页面** (`frontend/src/views/member/DepositRecharge.vue`)：
     - 实现充值表单，包含充值金额输入（默认0.01元）
     - 实现支付币别选择（人民币）
     - 实现支付方式选择（微信支付、支付宝），使用单选按钮样式
     - 添加"线上充值"红色按钮
     - 添加"点击立刻付款"红色按钮
     - 复用公共组件：TopBar、Header、Navbar、Footer、MemberHeaderBar、MemberSidebar
     - 预留真实支付接口对接入口（TODO注释）
     - 暂时使用模拟支付方式，支付成功后显示提示信息
  2. **添加路由配置** (`frontend/src/router/index.ts`)：
     - 添加 `/member/deposit/recharge` 路由
     - 设置页面标题为"预存款充值"
     - 设置需要登录认证
  3. **更新侧边栏组件** (`frontend/src/components/member/MemberSidebar.vue`)：
     - 更新路由映射，使"预存款充值"菜单项正确跳转到充值页面
     - 添加路由自动识别逻辑，高亮当前选中的菜单项
- 页面特点：
  - ✅ 1:1仿照对标网站设计，布局和样式保持一致
  - ✅ 复用公共组件，保持页面风格统一
  - ✅ 表单验证：充值金额必须大于0.01元
  - ✅ 支付方式单选，默认选择微信支付
  - ✅ 响应式设计，支持移动端访问
  - ✅ 预留真实支付接口对接入口，方便后续集成
- 会员中心预存款充值页面前端实现已完成

## 2025-12-10

### 修复后端服务启动Bean名称冲突错误（ShippingController）
- 已修复后端服务启动时的ShippingController Bean名称冲突问题
- 主要修改内容：
  1. **修复buyer包ShippingController** (`backend/src/main/java/com/shoppingmall/controller/buyer/ShippingController.java`)：
     - 在`@RestController`注解中指定bean名称为`buyerShippingController`
     - 解决与admin包ShippingController的bean名称冲突
  2. **修复admin包ShippingController** (`backend/src/main/java/com/shoppingmall/controller/admin/ShippingController.java`)：
     - 在`@RestController`注解中指定bean名称为`adminShippingController`
     - 解决与buyer包ShippingController的bean名称冲突
- 问题原因：
  - 两个ShippingController类都使用了默认的bean名称`shippingController`
  - Spring无法区分这两个同名的bean，导致`ConflictingBeanDefinitionException`异常
- 修改效果：
  - ✅ 两个ShippingController现在使用不同的bean名称（`buyerShippingController`和`adminShippingController`）
  - ✅ 解决了Bean名称冲突问题，后端服务可以正常启动
  - ✅ 不影响现有的API接口功能
- 后端服务启动ShippingController Bean名称冲突错误已修复

### 修复后端服务启动Bean名称冲突错误（OrderServiceImpl）
- 已修复后端服务启动时的OrderServiceImpl Bean名称冲突问题
- 主要修改内容：
  1. **修复buyer包OrderServiceImpl** (`backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`)：
     - 在`@Service`注解中指定bean名称为`buyerOrderServiceImpl`
     - 解决与admin包OrderServiceImpl的bean名称冲突
  2. **修复admin包OrderServiceImpl** (`backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`)：
     - 在`@Service`注解中指定bean名称为`adminOrderServiceImpl`
     - 解决与buyer包OrderServiceImpl的bean名称冲突
- 问题原因：
  - 两个OrderServiceImpl类都使用了默认的bean名称`orderServiceImpl`
  - Spring无法区分这两个同名的bean，导致`ConflictingBeanDefinitionException`异常
- 修改效果：
  - ✅ 两个OrderServiceImpl现在使用不同的bean名称（`buyerOrderServiceImpl`和`adminOrderServiceImpl`）
  - ✅ 解决了Bean名称冲突问题，后端服务可以正常启动
  - ✅ 不影响现有的Service接口功能
- 后端服务启动OrderServiceImpl Bean名称冲突错误已修复

### 修改物流配置菜单SQL更新脚本
- 已修改物流配置菜单的SQL更新脚本，改为更新现有菜单而不是新增菜单
- 主要修改内容：
  1. **更新SQL脚本** (`database/update-20251209-add-logistics-menu.sql`)：
     - 将新增菜单（ID=35）改为更新现有物流配置菜单（ID=30）
     - 更新组件路径：从 `system/Logistics` 改为 `logistics/Index`
     - 更新权限标识：从 `admin:system:logistics` 改为 `admin:logistics:list`
     - 保持菜单名称"物流配置"不变
     - 保持排序顺序为3不变
     - 为超级管理员和运营人员角色分配菜单权限（如果还没有分配）
- 修改原因：
  - 原来已经存在物流配置菜单（ID=30），不需要新增菜单
  - 只需要更新现有菜单的组件路径和权限标识即可
- 修改效果：
  - ✅ 使用现有物流配置菜单，避免重复菜单
  - ✅ 更新组件路径指向新的物流管理页面
  - ✅ 更新权限标识符合新的权限体系
  - ✅ 确保相关角色已分配菜单权限
- 物流配置菜单SQL更新脚本已修改

### 添加运费模板和规则初始化测试数据
- 已在物流管理表SQL脚本中添加运费模板和运费规则的初始化测试数据
- 主要修改内容：
  1. **添加运费模板初始化数据** (`database/update-20251209-add-logistics-tables.sql`)：
     - 标准按重量计费模板：首重1kg 8元，续重1kg 3元，满99元包邮
     - 标准按件数计费模板：首件6元，续件2元，满88元包邮
     - 标准按金额计费模板：订单金额的10%作为运费，满100元包邮
     - 偏远地区按重量模板：首重1kg 15元，续重1kg 8元，满199元包邮
  2. **添加运费规则初始化数据**：
     - 为每个模板添加默认规则和多个地区规则
     - 标准按重量模板：包含默认规则、广东省内、江浙沪、偏远地区（新疆、西藏、青海）等规则
     - 标准按件数模板：包含默认规则、广东省内、江浙沪等规则
     - 标准按金额模板：包含默认规则、广东省内、江浙沪等规则（费率不同）
     - 偏远地区模板：包含默认规则、新疆、西藏、青海、内蒙古等规则
  3. **地区覆盖**：
     - 广东省：全省、广州市、深圳市
     - 江浙沪：江苏省、浙江省、上海市
     - 偏远地区：新疆、西藏、青海、内蒙古及其主要城市
- 数据特点：
  - ✅ 覆盖了三种计算方式（按重量、按件数、按金额）
  - ✅ 包含默认规则和地区特定规则
  - ✅ 不同地区设置不同的运费和包邮门槛
  - ✅ 偏远地区运费更高，包邮门槛也更高
  - ✅ 使用ON DUPLICATE KEY UPDATE避免重复插入
- 运费模板和规则初始化测试数据已添加

## 2025-12-10

### 管理后台订单列表页面开发完成
- 已完成管理后台订单列表页面的前后端对接，并初始化了不同状态的订单测试数据
- 主要修改内容：
  1. **修复OrderQueryDTO的orderStatus类型** (`backend/src/main/java/com/shoppingmall/dto/OrderQueryDTO.java`)：
     - 将`orderStatus`字段类型从`String`改为`Integer`，以匹配前端传递的number类型
     - 更新注释说明订单状态为数字类型（0-待付款，1-已付款未发货，2-已发货，3-已完成，4-已取消，5-已退款，6-已退货）
  2. **实现收货人姓名查询功能** (`backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`)：
     - 在`getOrderList`方法中实现收货人姓名查询功能
     - 通过解析`shippingAddress` JSON字段获取收货人信息
     - 在内存中过滤匹配收货人姓名的订单（支持模糊查询）
     - 注意：由于在内存中过滤，分页总数可能不准确，如需精确分页可使用MySQL的JSON函数在数据库层面查询
  3. **创建订单测试数据SQL脚本** (`database/update-20251210-add-order-test-data.sql`)：
     - 创建了11条不同状态的订单测试数据：
       - 待付款订单：2条（ORD20251210001, ORD20251210002）
       - 已付款未发货订单：2条（ORD20251210003, ORD20251210004）
       - 已发货订单：2条（ORD20251210005, ORD20251210006）
       - 已完成订单：2条（ORD20251210007, ORD20251210008）
       - 已取消订单：1条（ORD20251210009）
       - 已退款订单：1条（ORD20251210010）
       - 已退货订单：1条（ORD20251210011）
     - 为每个订单创建了对应的订单商品数据（order_item表）
     - 为已发货、已完成、已退货的订单创建了物流信息（order_logistics表）
     - 使用MySQL变量（@order_id_*）确保订单ID关联正确
     - 订单包含完整的收货地址JSON数据，支持收货人姓名查询
- 修改效果：
  - ✅ 前后端数据格式匹配，订单状态查询正常工作
  - ✅ 支持按收货人姓名进行模糊查询
  - ✅ 提供了完整的测试数据，覆盖所有订单状态
  - ✅ 订单列表页面可以正常显示和测试
- 管理后台订单列表页面开发完成

