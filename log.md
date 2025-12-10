# 修改日志

## 2025-12-10

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

