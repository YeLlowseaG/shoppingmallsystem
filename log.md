## 2025-12-14 - 重构Header组件配置读取逻辑

### 功能说明
重构Header组件，移除所有前端写死的默认值，改为完全从后台配置读取。同时修复logo图片加载失败时显示"JINGVO 净果"文案的问题。

### 修改文件

#### 前端
1. `frontend/src/components/home/Header.vue` - 移除所有写死的默认值，改为从后台配置读取，修复alt属性问题

### 具体修改

#### 问题分析
- **写死的默认值**：前端代码中写死了logo、name、servicePhone、consultPhone、qrcode和热门关键词的默认值
- **配置来源**：这些配置应该完全从后台读取，不需要前端写死
- **alt属性问题**：logo图片的alt属性绑定到`siteConfig.name`，当图片加载失败时会显示"JINGVO 净果"文案

#### 修复方案
- **移除所有默认值**：
  - `siteConfig` 初始值改为空字符串
  - `hotKeywords` 初始值改为空数组
- **添加条件渲染**：
  - Logo只在`siteConfig.logo`存在时显示
  - 服务热线只在`siteConfig.servicePhone`存在时显示
  - 咨询热线只在`siteConfig.consultPhone`存在时显示
  - 二维码只在`siteConfig.qrcode`存在时显示
  - 热门关键词只在数组不为空时显示
- **修复alt属性**：
  - 将logo图片的`alt`属性改为空字符串`alt=""`
  - 添加CSS样式隐藏图片加载失败时显示的alt文本
- **移除错误处理中的默认配置注释**：不再保持默认配置

### 功能特性
- ✅ 所有配置完全从后台读取，前端不写死任何默认值
- ✅ Logo图片加载失败时不显示任何文案
- ✅ 配置不存在时不显示对应元素，避免显示空内容
- ✅ 热门关键词只在有配置时显示

### 技术细节
- 使用`v-if`条件渲染，只在配置存在时显示元素
- Logo图片的`alt`属性设置为空字符串
- 使用CSS隐藏图片加载失败时的alt文本：
  ```scss
  font-size: 0;
  line-height: 0;
  text-indent: -9999px;
  overflow: hidden;
  ```

### 影响
- ✅ 配置完全由后台管理，前端代码更加灵活
- ✅ 修复了logo图片加载失败时显示文案的问题
- ✅ 提升了代码的可维护性，配置变更无需修改前端代码
- ✅ 避免了显示空内容，提升用户体验

---

## 2025-12-14 - 移除页面顶部Logo占位符文字

### 功能说明
移除页面顶部Header组件中logo占位符图片上的"JINGVO"文字显示，只保留纯色logo图片，不显示任何文案。

### 修改文件

#### 前端
1. `frontend/src/components/home/Header.vue` - 移除logo占位符URL中的text参数

### 具体修改

#### 问题分析
- **占位符文字**：logo占位符URL中包含`?text=JINGVO`参数，导致占位符图片上显示"JINGVO"文字
- **用户需求**：只需要显示logo图片，不需要显示任何文案

#### 修复方案
- **移除文字参数**：将logo占位符URL从 `https://via.placeholder.com/150x60/E4393C/ffffff?text=JINGVO` 改为 `https://via.placeholder.com/150x60/E4393C/ffffff`
- **保留图片**：只显示纯色logo图片，不包含任何文字

### 功能特性
- ✅ Logo占位符图片不再显示"JINGVO"文字
- ✅ 只显示纯色logo图片
- ✅ 如果使用实际logo图片，也不会显示额外文案

### 技术细节
- 占位符URL中的`?text=JINGVO`参数会在图片上显示文字
- 移除该参数后，占位符图片为纯色，不包含文字
- `siteConfig.name`仅用于img标签的alt属性，不会在页面上显示

### 影响
- ✅ Logo区域更加简洁，只显示图片
- ✅ 符合用户需求，不显示额外文案
- ✅ 如果后续使用实际logo图片，也不会显示文字

---

## 2025-12-14 - 优化商品详情页接口调用逻辑

### 功能说明
优化商品详情页面（`/products/:id`）的接口调用逻辑，只在用户已登录时才调用需要登录的检查接口，避免未登录时产生401错误。包括：
1. 缺货登记检查接口
2. 收藏状态检查接口

### 修改文件

#### 前端
1. `frontend/src/views/products/Detail.vue` - 添加登录状态判断，只在用户已登录时检查缺货登记状态和收藏状态

### 具体修改

#### 问题分析
- **401错误**：未登录用户访问商品详情页时，会调用以下接口：
  - `/api/buyer/stock-notification/check/{productId}` - 缺货登记检查
  - `/api/buyer/favorites/check/{productId}` - 收藏状态检查
- **接口要求**：这些接口需要用户登录才能获取用户ID，检查该用户的相关状态
- **用户体验**：未登录时调用接口会产生401错误，虽然前端已处理，但仍会产生不必要的请求

#### 修复方案
- **导入用户Store**：添加 `useUserStore` 导入和实例化
- **缺货登记检查优化**：在 `checkStockRegisterStatus` 函数中，先判断用户是否已登录
  - 如果用户未登录，直接设置 `hasRegisteredStock.value = false`，不调用接口
- **收藏状态检查优化**：在 `checkFavoriteStatus` 函数中，先判断用户是否已登录
  - 如果用户未登录，直接设置 `isFavorited.value = false`，不调用接口
  - 添加错误处理，发生错误时默认为未收藏

### 功能特性
- ✅ 未登录用户访问商品详情页时，不会调用需要登录的检查接口
- ✅ 避免产生401错误，减少不必要的网络请求
- ✅ 提升用户体验，减少控制台错误信息
- ✅ 已登录用户正常检查缺货登记状态和收藏状态

### 技术细节
- 使用 `userStore.userInfo` 判断用户是否已登录
- 未登录时直接返回，不调用后端接口
- 保持原有的错误处理逻辑，确保代码健壮性
- 错误发生时设置合理的默认值

### 影响
- ✅ 减少未登录时的401错误请求（缺货登记和收藏检查）
- ✅ 提升页面加载性能（减少不必要的API调用）
- ✅ 改善用户体验，避免控制台错误信息
- ✅ 代码逻辑更加清晰，只在需要时调用接口

---

## 2025-12-14 - 完善商品列表页面加入购物车功能

### 功能说明
完善商品列表页面（`/products?type=new`）的加入购物车功能，和后端API对接，参考商品详情页面的加入购物车逻辑。

### 修改文件

#### 前端
1. `frontend/src/components/products/ProductCard.vue` - 完善加入购物车功能

### 具体修改

#### 1. 导入购物车相关依赖
- **导入API**：从 `@/api/buyer/cart` 导入 `addToCart` API 和 `AddCartDTO` 类型
- **导入Store**：导入 `useCartStore` 用于更新购物车数量
- **添加状态**：添加 `addingToCart` 响应式变量，用于控制加载状态

#### 2. 完善加入购物车函数
- **商品验证**：
  - 检查商品ID是否存在
  - 检查商品状态（如果已下架则提示）
  - 检查库存（如果库存为0则提示）
- **API调用**：
  - 构建 `AddCartDTO` 对象，包含商品ID和数量（默认为1）
  - 调用 `addToCartAPI` 添加商品到购物车
  - 成功后更新购物车数量（调用 `cartStore.updateCartCount()`）
- **错误处理**：
  - 401错误：提示用户登录并跳转到登录页
  - 其他错误：显示后端返回的错误信息
- **用户体验**：
  - 添加加载状态，防止重复点击
  - 显示成功提示
  - 阻止事件冒泡，避免触发跳转到详情页

#### 3. 更新按钮UI
- **加载状态**：按钮显示加载动画和"加入中..."文字
- **禁用状态**：加载时禁用按钮，防止重复提交

### 功能特性
- ✅ 商品列表页面可以正常加入购物车
- ✅ 和后端API完全对接
- ✅ 商品状态和库存验证
- ✅ 登录状态检查，未登录时提示并跳转
- ✅ 加载状态显示，提升用户体验
- ✅ 自动更新购物车数量
- ✅ 错误处理完善，提示友好

### 技术细节
- 使用 `addToCart` API 添加商品到购物车
- 使用 `useCartStore` 更新购物车数量
- 默认数量为1，用户可以在详情页修改数量
- 支持商品状态和库存检查（如果商品数据包含这些字段）
- 使用 `e.stopPropagation()` 阻止事件冒泡

### 参考实现
- 参考了商品详情页面（`Detail.vue`）的加入购物车逻辑
- 保持了一致的用户体验和错误处理方式

### 影响
- ✅ 用户可以在商品列表页面直接加入购物车，无需跳转到详情页
- ✅ 提升了购物体验，减少了操作步骤
- ✅ 与商品详情页面的功能保持一致

---

## 2025-12-14 - 优化会员中心首页消息功能

### 功能说明
优化会员中心首页（`/member`）的消息相关功能，包括：
1. "您的未读消息"点击查看按钮跳转到收件箱页面
2. 屏蔽"NEW 新功能展示"文案
3. 点击"通知"按钮跳转到收件箱页面
4. 添加获取未读消息数量的功能

### 修改文件

#### 前端
1. `frontend/src/views/member/Index.vue` - 添加查看收件箱跳转功能和获取未读消息数量
2. `frontend/src/components/member/MemberHeaderBar.vue` - 屏蔽"NEW 新功能展示"文案，添加通知按钮跳转功能

### 具体修改

#### 1. 会员中心首页（Index.vue）
- **添加查看收件箱函数**：实现 `handleViewInbox` 函数，跳转到 `/member/site-messages/inbox`
- **添加点击事件**：为"查看"按钮添加 `@click="handleViewInbox"` 事件
- **添加获取未读消息数量**：
  - 导入 `getUnreadCount` API
  - 实现 `fetchUnreadMessageCount` 函数获取未读消息数量
  - 在 `onMounted` 中调用获取未读消息数量

#### 2. 会员中心头部栏（MemberHeaderBar.vue）
- **屏蔽"NEW 新功能展示"文案**：使用注释方式屏蔽该文案显示
- **添加通知按钮跳转功能**：
  - 导入 `useRouter`
  - 实现 `handleNotificationClick` 函数，跳转到 `/member/site-messages/inbox`
  - 为"通知"按钮添加 `@click="handleNotificationClick"` 事件

### 功能特性
- ✅ "您的未读消息"点击查看按钮可跳转到收件箱页面
- ✅ "NEW 新功能展示"文案已屏蔽，不再显示
- ✅ 点击"通知"按钮可跳转到收件箱页面
- ✅ 会员中心首页自动获取并显示未读消息数量

### 技术细节
- 使用 Vue Router 的 `router.push` 进行页面跳转
- 使用 `getUnreadCount` API 获取未读消息数量
- 错误处理：401 错误（未登录）时静默失败，不显示错误提示

### 影响
- ✅ 提升用户体验，用户可以快速访问收件箱
- ✅ 界面更加简洁，移除了不需要的"NEW 新功能展示"文案
- ✅ 未读消息数量实时显示，用户可以及时了解消息状态

---

## 2025-12-14 - 修复MessageServiceImpl中lambda表达式变量引用错误

### 功能说明
修复 `MessageServiceImpl.java` 文件中 lambda 表达式引用的本地变量必须是最终变量或实际上的最终变量的编译错误。

### 修改文件

#### 后端
1. `backend/src/main/java/com/shoppingmall/service/buyer/impl/MessageServiceImpl.java` - 修复 senderNameMap 变量声明，使其成为 effectively final

### 具体修改

#### 问题分析
- **编译错误**：从lambda 表达式引用的本地变量必须是最终变量或实际上的最终变量
- **错误原因**：`senderNameMap` 变量先被初始化为 `Map.of()`，然后在 if 块中被重新赋值，导致它不是 effectively final 的，无法在 lambda 表达式中使用

#### 修复方案
- **声明为 final**：将 `senderNameMap` 声明为 `final` 变量
- **使用 if-else 结构**：在 if-else 块中分别赋值，确保变量只被赋值一次，成为 effectively final

### 功能特性
- ✅ 修复了编译错误，项目可以正常编译
- ✅ lambda 表达式可以正常使用 senderNameMap 变量

### 技术细节
- Java lambda 表达式中引用的局部变量必须是 final 或 effectively final 的
- 使用 `final` 关键字声明变量，并在 if-else 块中分别赋值，确保变量只被赋值一次

### 影响
- ✅ 修复了编译错误，项目可以正常启动
- ✅ lambda 表达式可以正常访问 senderNameMap 变量

---

## 2025-12-14 - 修复MessageServiceImpl中UserRepository导入错误

### 功能说明
修复 `MessageServiceImpl.java` 文件中 `UserRepository` 的导入路径错误，将导入路径从 `com.shoppingmall.repository.UserRepository` 修正为 `com.shoppingmall.repository.user.UserRepository`。

### 修改文件

#### 后端
1. `backend/src/main/java/com/shoppingmall/service/buyer/impl/MessageServiceImpl.java` - 修复 UserRepository 导入路径

### 具体修改

#### 问题分析
- **编译错误**：`UserRepository` 类找不到符号
- **错误原因**：导入路径错误，`UserRepository` 实际位于 `com.shoppingmall.repository.user` 包中，而不是 `com.shoppingmall.repository` 包中

#### 修复方案
- **修正导入语句**：将 `import com.shoppingmall.repository.UserRepository;` 改为 `import com.shoppingmall.repository.user.UserRepository;`

### 功能特性
- ✅ 修复了编译错误，项目可以正常编译
- ✅ 导入路径正确，可以正常使用 UserRepository

### 技术细节
- `UserRepository` 位于 `com.shoppingmall.repository.user` 包中
- 使用 MyBatis-Plus 的 BaseMapper 接口

### 影响
- ✅ 修复了编译错误，项目可以正常启动
- ✅ MessageServiceImpl 可以正常使用 UserRepository 查询用户信息

---

## 2025-12-14 - 实现站内消息收件箱功能

### 功能说明
实现会员中心站内消息模块的收件箱功能，用户可以查看接收到的站内消息通知信息，包括系统消息、订单消息等。

### 修改文件

#### 前端
1. `frontend/src/components/member/MemberSidebar.vue` - 修改侧边栏，只保留收件箱菜单，屏蔽其他菜单
2. `frontend/src/router/index.ts` - 添加收件箱路由配置

#### 创建文件

##### 前端
1. `frontend/src/views/member/Inbox.vue` - 收件箱页面组件
2. `frontend/src/api/buyer/message.ts` - 站内消息API接口

##### 后端
1. `backend/src/main/java/com/shoppingmall/entity/Message.java` - 站内消息实体类
2. `backend/src/main/java/com/shoppingmall/repository/MessageRepository.java` - 站内消息Repository
3. `backend/src/main/java/com/shoppingmall/vo/MessageVO.java` - 站内消息VO
4. `backend/src/main/java/com/shoppingmall/vo/MessagePageVO.java` - 消息分页响应VO
5. `backend/src/main/java/com/shoppingmall/dto/MessageQueryDTO.java` - 消息查询DTO
6. `backend/src/main/java/com/shoppingmall/service/buyer/MessageService.java` - 站内消息服务接口
7. `backend/src/main/java/com/shoppingmall/service/buyer/impl/MessageServiceImpl.java` - 站内消息服务实现
8. `backend/src/main/java/com/shoppingmall/controller/buyer/MessageController.java` - 站内消息控制器

### 具体修改

#### 1. 侧边栏菜单优化
- **屏蔽其他菜单**：只保留"收件箱"菜单项，屏蔽"发送消息"、"草稿箱"、"发件箱"、"给管理员发消息"等菜单
- **路由映射**：更新路由映射，收件箱跳转到 `/member/site-messages/inbox`
- **自动激活**：添加路由自动判断逻辑，当访问收件箱页面时自动激活对应菜单

#### 2. 收件箱页面设计
- **页面布局**：参考预存款余额页面布局，使用会员中心标准布局
- **消息列表**：
  - 显示消息标题、内容、时间
  - 区分已读/未读消息（未读消息高亮显示）
  - 显示消息类型标签（系统消息、订单消息等）
  - 支持点击消息查看详情或跳转到关联订单
- **操作功能**：
  - 全部标记为已读
  - 刷新消息列表
  - 分页显示
- **消息类型**：
  - 普通消息（0）
  - 系统消息（1）- 显示蓝色标签
  - 订单消息（2）- 显示绿色标签，可跳转到订单详情
  - 其他（3）

#### 3. 后端API接口
- **获取收件箱消息列表**：`GET /api/buyer/messages/inbox`
  - 支持分页查询
  - 支持按消息类型筛选
  - 支持按已读状态筛选
  - 返回未读消息数量
- **标记消息为已读**：`PUT /api/buyer/messages/{id}/read`
- **全部标记为已读**：`PUT /api/buyer/messages/read-all`
- **删除消息**：`DELETE /api/buyer/messages/{id}`
- **获取未读消息数量**：`GET /api/buyer/messages/unread-count`

#### 4. 数据模型
- **Message实体**：对应数据库 `message` 表
  - 支持逻辑删除
  - 包含发送人ID、接收人ID、标题、内容、消息类型、已读状态、订单号等字段
- **消息类型**：使用 `MessageType` 常量类
  - NORMAL = 0（普通消息）
  - SYSTEM = 1（系统消息）
  - ORDER = 2（订单消息）
  - OTHER = 3（其他）

### 功能特性
- ✅ 收件箱页面，显示站内消息列表
- ✅ 区分已读/未读消息，未读消息高亮显示
- ✅ 消息类型标签显示（系统消息、订单消息等）
- ✅ 支持点击消息查看详情
- ✅ 订单消息支持跳转到订单详情页面
- ✅ 全部标记为已读功能
- ✅ 分页显示消息列表
- ✅ 显示未读消息数量
- ✅ 响应式设计，支持多设备访问

### 技术细节
- 使用 Element Plus 组件库构建UI
- 使用 MyBatis-Plus 进行数据库操作
- 支持逻辑删除，不会真正删除数据
- 消息按创建时间倒序排列
- 自动获取发送人姓名（如果存在）
- 权限验证：只能查看自己的消息

### 影响
- ✅ 用户可以方便地查看站内消息通知
- ✅ 提升用户体验，及时了解系统通知和订单消息
- ✅ 简化了站内消息模块，只保留核心的收件箱功能
- ✅ 为后续扩展消息功能打下基础

---

## 2025-12-14 - 修复订单详情页面跳转问题

### 功能说明
修复订单详情页面（`/order/detail`）中"我已付款"和"我有问题"按钮无法正确跳转到订单问题填写页面的问题。

### 修改文件

#### 前端
1. `frontend/src/views/order/Detail.vue` - 修复按钮跳转逻辑，确保订单号正确传递
2. `frontend/src/views/order/OrderMessage.vue` - 增强参数验证和错误处理

### 具体修改

#### 1. 订单详情页面跳转逻辑优化

##### handleMarkAsPaid 函数
- **添加订单号验证**：在跳转前检查订单号是否存在
- **备用方案**：如果 `orderNumber.value` 为空，从 `route.query.orderNumber` 获取
- **错误提示**：如果订单号不存在，显示警告并阻止跳转

##### handleHaveQuestion 函数
- **添加订单号验证**：同上，确保订单号存在
- **备用方案**：从路由参数中获取订单号
- **错误提示**：如果订单号不存在，显示警告并阻止跳转

#### 2. 订单问题页面参数处理优化

##### onMounted 函数
- **参数验证**：检查订单号是否存在，如果不存在则跳转回订单列表
- **类型验证**：验证消息类型是否为 'paid' 或 'question'
- **默认值处理**：如果没有指定类型，默认使用 'question'
- **错误处理**：订单号缺失时显示错误提示并跳转

### 问题原因
- 订单详情页面在加载时，`orderNumber.value` 可能还没有被正确设置
- 跳转时只使用了 `orderNumber.value`，没有备用方案
- 订单问题页面缺少参数验证，可能导致页面显示异常

### 功能特性
- ✅ 按钮跳转时确保订单号正确传递
- ✅ 支持从多个来源获取订单号（响应式变量或路由参数）
- ✅ 参数验证完善，避免页面异常
- ✅ 错误提示友好，引导用户正确操作

### 技术细节
- 使用 `orderNumber.value || route.query.orderNumber` 确保订单号获取
- 在跳转前验证订单号是否存在
- 在目标页面验证必要参数，缺失时自动跳转

### 影响
- ✅ 修复了"我已付款"按钮无法跳转的问题
- ✅ 修复了"我有问题"按钮无法跳转的问题
- ✅ 提升了用户体验，避免页面异常
- ✅ 增强了代码的健壮性

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
已创建菜单和权限脚本：`database/update-20251214-add-order-message-menu.sql`

**菜单信息：**
- 菜单ID: 55
- 父菜单：订单管理 (parent_id=3)
- 菜单名称：订单问题
- 菜单路径：message（完整路径：/admin/order/message）
- 组件路径：order/OrderMessage
- 菜单类型：二级菜单 (menu_type=1)
- 权限标识：admin:order:message:list
- 排序号：2（在订单列表之后）
- 图标：ChatLineRound

**权限分配：**
- 超级管理员 (role_id=1)：自动分配
- 运营人员 (role_id=2)：自动分配（如果存在）
- 客服人员 (role_id=5)：自动分配（如果存在）

**执行脚本：**
```sql
-- 执行菜单和权限脚本
source database/update-20251214-add-order-message-menu.sql;
```

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
