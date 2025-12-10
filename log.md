# 修改日志

## 2025-12-10

### 为公告管理和帮助中心管理列表添加状态字段查询
- 已为公告管理列表和帮助中心文章管理列表添加状态字段查询功能
- 主要修改内容：
  1. **后端接口修改**：
     - `backend/src/main/java/com/shoppingmall/service/admin/AnnouncementService.java`：添加status参数到getAnnouncementList方法
     - `backend/src/main/java/com/shoppingmall/service/admin/impl/AnnouncementServiceImpl.java`：在查询条件中添加status字段过滤
     - `backend/src/main/java/com/shoppingmall/controller/admin/AnnouncementController.java`：在控制器中添加status请求参数
     - `backend/src/main/java/com/shoppingmall/service/admin/HelpService.java`：添加status参数到getArticleList方法
     - `backend/src/main/java/com/shoppingmall/service/admin/impl/HelpServiceImpl.java`：在查询条件中添加status字段过滤
     - `backend/src/main/java/com/shoppingmall/controller/admin/HelpController.java`：在控制器中添加status请求参数
  2. **前端API修改**：
     - `admin-frontend/src/api/admin/announcement.ts`：getAnnouncementPage方法添加status参数
     - `admin-frontend/src/api/admin/help.ts`：getHelpArticlePage方法添加status参数
  3. **前端页面修改**：
     - `admin-frontend/src/views/announcement/Index.vue`：在搜索表单中添加状态下拉选择框，支持按状态筛选公告
     - `admin-frontend/src/views/help/Index.vue`：在文章管理搜索表单中添加状态下拉选择框，支持按状态筛选文章
- 功能说明：
  - 公告管理列表和帮助中心文章管理列表现在都支持按状态（全部/启用/禁用）进行筛选
  - 状态筛选为可选参数，不选择时显示全部数据
  - 重置搜索时会清空状态筛选条件

## 2025-12-10

### 统一前端日期格式显示：移除T字符
- 已统一处理所有前端页面（管理后台和用户端）的日期时间显示格式，确保不显示ISO 8601格式中的T字符
- 主要修改内容：
  1. **管理后台列表页面日期格式化**：
     - `admin-frontend/src/views/announcement/Index.vue`：为publishDate、createTime、updateTime添加格式化
     - `admin-frontend/src/views/help/Index.vue`：为createTime添加格式化
     - `admin-frontend/src/views/order/List.vue`：为orderDate添加格式化
     - `admin-frontend/src/views/buyer/List.vue`：为createTime添加格式化，并更新详情对话框
     - `admin-frontend/src/views/buyer/Audit.vue`：为createTime添加格式化，并更新详情对话框
     - `admin-frontend/src/views/deposit/Record.vue`：为createTime添加格式化，并更新详情对话框
     - `admin-frontend/src/views/permission/User.vue`：为lastLoginTime添加格式化
  2. **管理后台详情对话框日期格式化**：
     - 所有详情对话框中的日期时间字段都使用formatDateTime函数格式化
     - 确保详情页面显示的日期格式与列表页面一致
  3. **前端用户端日期格式化统一**：
     - `frontend/src/views/member/DepositBalance.vue`：使用utils中的formatDateTime替换本地函数
     - `frontend/src/views/member/Orders.vue`：使用utils中的formatDateTime替换本地函数
     - `frontend/src/views/news/List.vue`：使用utils中的formatDate，统一日期格式化
     - `frontend/src/views/news/Detail.vue`：使用utils中的formatDate
  4. **统一使用工具函数**：
     - 所有页面统一使用`@/utils`中的`formatDateTime`和`formatDate`函数
     - dayjs库可以正确处理ISO 8601格式（包括带T的格式），输出为"YYYY-MM-DD HH:mm:ss"格式
- 日期格式说明：
  - **日期时间格式**：YYYY-MM-DD HH:mm:ss（例如：2025-12-10 22:57:42）
  - **日期格式**：YYYY-MM-DD（例如：2025-12-10）
  - 不再显示ISO 8601格式中的T字符（例如：不再显示2025-12-10T22:57:42）
- 修改效果：
  - ✅ 所有管理后台列表页面的日期时间字段都正确格式化
  - ✅ 所有管理后台详情对话框的日期时间字段都正确格式化
  - ✅ 前端用户端所有页面的日期时间字段都统一使用工具函数
  - ✅ 日期格式统一，不再出现T字符
- 所有前端页面的日期格式已统一处理

## 2025-12-10

### 修复公告管理页面：显示创建时间和更新时间字段
- 已修复公告管理页面列表的创建时间显示问题，并添加了更新时间字段
- 主要修改内容：
  1. **修复后端VO** (`backend/src/main/java/com/shoppingmall/vo/AnnouncementVO.java`)：
     - 添加 `createTime` 字段（之前只有 `updateTime`）
     - 确保VO包含完整的创建时间和更新时间信息
  2. **添加更新时间列** (`admin-frontend/src/views/announcement/Index.vue`)：
     - 在列表中添加"更新时间"列，显示在"创建时间"列之后
     - 列宽设置为180px，与创建时间列保持一致
  3. **确认自动更新机制**：
     - 后端实体类 `Announcement.java` 中的 `updateTime` 字段使用了 `@TableField(fill = FieldFill.INSERT_UPDATE)`
     - 当编辑更新公告数据时，`updateTime` 字段会自动更新为当前时间
     - 由 `MyBatisPlusMetaObjectHandler` 自动处理时间字段的填充
- 字段说明：
  - **创建时间**：公告首次创建的时间，不会改变
  - **更新时间**：公告最后修改的时间，每次编辑更新时自动更新
- 修改效果：
  - ✅ 创建时间字段正常显示
  - ✅ 更新时间字段已添加并正常显示
  - ✅ 编辑更新公告时，更新时间自动更新为当前时间
- 公告管理页面时间字段显示已修复

### 优化公告管理页面：添加排序规则说明
- 已在公告管理页面的新增/编辑对话框中，在排序字段下方添加排序规则说明
- 主要修改内容：
  1. **添加排序规则说明** (`admin-frontend/src/views/announcement/Index.vue`)：
     - 在排序字段（`el-form-item`）下方添加说明文案
     - 说明内容："提示：数字越小越靠前，相同发布日期时按排序字段升序排列"
     - 使用 `form-tip` 样式类，与富文本编辑器说明保持一致
- 排序规则说明：
  - 公告列表首先按发布日期倒序排列（最新日期在前）
  - 相同发布日期的公告，按排序字段升序排列（数字越小越靠前）
  - 例如：排序字段为1的公告会排在排序字段为2的公告前面
- 修改效果：
  - ✅ 用户能够清楚了解排序规则，正确设置排序字段
  - ✅ 说明文案样式统一，与富文本编辑器说明保持一致
- 公告管理页面排序规则说明已添加

### 优化公告详情页面：移除上一篇/下一篇功能，添加返回列表入口
- 已优化公告详情页面，移除上一篇/下一篇功能，在页面底部添加返回公告列表入口
- 主要修改内容：
  1. **移除上一篇/下一篇功能** (`frontend/src/views/news/Detail.vue`)：
     - 移除上一篇/下一篇导航区域
     - 移除 `prevAnnouncement` 和 `nextAnnouncement` 变量
     - 移除 `getPrevAnnouncement` 和 `getNextAnnouncement` API调用
     - 移除相关的样式定义（`.navigation`）
  2. **添加返回公告列表入口**：
     - 在页面底部（正文内容下方）添加"返回公告列表"链接
     - 使用 `ArrowLeft` 图标，带左箭头样式
     - 链接指向 `/news` 公告列表页面
     - 添加居中样式和hover效果
  3. **简化数据加载逻辑**：
     - 移除加载上一篇/下一篇的代码
     - 只加载当前公告详情
     - 简化错误处理逻辑
- 修改效果：
  - ✅ 页面更简洁，移除了不常用的上一篇/下一篇功能
  - ✅ 添加了明显的返回列表入口，用户体验更好
  - ✅ 代码更简洁，减少了不必要的API调用
- 公告详情页面优化已完成

### 修复公告详情页面：上一篇/下一篇按钮点击无反应问题
- 已修复公告详情页面点击上一篇/下一篇按钮没有反应的问题
- 主要修改内容：
  1. **添加路由参数监听** (`frontend/src/views/news/Detail.vue`)：
     - 使用 `watch` 监听 `route.params.id` 的变化
     - 当路由参数变化时，自动重新加载公告详情和上一篇/下一篇数据
     - 确保点击上一篇/下一篇链接后，页面能正确更新内容
  2. **优化数据加载逻辑**：
     - 在加载新数据前，先清空之前的数据（announcement、prevAnnouncement、nextAnnouncement）
     - 确保切换公告时不会显示旧数据
  3. **导入watch函数**：
     - 从 `vue` 中导入 `watch` 函数
- 问题原因：
  - 点击上一篇/下一篇链接时，路由会变化（如从 `/news/3` 变为 `/news/4`）
  - 但是组件只在 `onMounted` 时加载数据，没有监听路由参数的变化
  - 导致点击链接后路由变化了，但页面内容没有更新
- 解决方案：
  - 添加 `watch` 监听路由参数 `route.params.id` 的变化
  - 当参数变化时，自动调用 `loadAnnouncementDetail` 重新加载数据
  - 在加载新数据前清空旧数据，避免显示错误内容
- 修改效果：
  - ✅ 点击上一篇/下一篇链接后，页面能正确跳转并更新内容
  - ✅ 路由参数变化时，自动重新加载对应的公告详情
  - ✅ 切换公告时不会显示旧数据，用户体验更好
- 公告详情页面上一篇/下一篇功能已修复

### 优化公告详情页面：完善上一篇/下一篇功能并移除分页信息
- 已优化公告详情页面，完善上一篇/下一篇功能，移除分页信息显示
- 主要修改内容：
  1. **移除分页信息显示** (`frontend/src/views/news/Detail.vue`)：
     - 移除"1/341"分页信息显示区域
     - 移除相关的样式定义
  2. **移除不需要的变量**：
     - 移除 `currentIndex` 和 `totalCount` 变量
     - 移除 `generateMockData` 函数（不再需要模拟数据）
  3. **优化上一篇/下一篇加载逻辑**：
     - 将上一篇和下一篇的加载改为独立的try-catch块
     - 确保即使一个加载失败，也不影响另一个的加载
     - 优化错误处理，确保返回null而不是抛出错误
  4. **优化API调用** (`frontend/src/api/common/announcement.ts`)：
     - 在 `getPrevAnnouncement` 和 `getNextAnnouncement` 中添加错误处理
     - 确保API调用失败时返回null而不是抛出错误
     - 使用 `.then(data => data || null).catch(() => null)` 确保始终返回null或数据
- 修改效果：
  - ✅ 分页信息已移除，页面更简洁
  - ✅ 上一篇/下一篇功能更稳定，即使API调用失败也不会影响页面显示
  - ✅ 错误处理更完善，确保用户体验
  - ✅ 代码更简洁，移除了不必要的变量和函数
- 公告详情页面优化已完成

## 2025-12-10

### 完成管理后台公告管理页面开发
- 已完成管理后台公告管理页面的前后端开发，支持公告的增删改查和富文本编辑
- 主要修改内容：
  1. **创建公告DTO类** (`backend/src/main/java/com/shoppingmall/dto/AnnouncementDTO.java`)：
     - 公告数据传输对象，包含标题、内容、图片数组、发布日期、排序、状态等字段
     - 使用Jakarta验证注解进行参数校验
  2. **创建管理后台公告Service层**：
     - `AnnouncementService.java`：公告管理服务接口（管理后台使用）
       - `getAnnouncementList`：分页查询公告列表，支持标题模糊查询
       - `getAnnouncementById`：根据ID获取公告信息
       - `addAnnouncement`：新增公告
       - `updateAnnouncement`：更新公告
       - `deleteAnnouncement`：删除公告（逻辑删除）
       - `updateAnnouncementStatus`：启用/禁用公告
     - `AnnouncementServiceImpl.java`：公告管理服务实现类
       - 自动转换图片数组为JSON字符串存储
       - 自动解析JSON字符串为图片数组返回
       - 支持标题模糊查询
       - 按发布日期倒序、排序字段升序排列
  3. **创建管理后台公告Controller** (`backend/src/main/java/com/shoppingmall/controller/admin/AnnouncementController.java`)：
     - `GET /api/admin/announcement/list`：分页查询公告列表
     - `GET /api/admin/announcement/{id}`：获取公告详情
     - `POST /api/admin/announcement`：新增公告
     - `PUT /api/admin/announcement/{id}`：更新公告
     - `DELETE /api/admin/announcement/{id}`：删除公告
     - `PUT /api/admin/announcement/{id}/status`：启用/禁用公告
  4. **创建管理后台公告API接口文件** (`admin-frontend/src/api/admin/announcement.ts`)：
     - 定义公告DTO和VO类型
     - 实现所有公告管理相关的API调用函数
  5. **创建管理后台公告管理页面** (`admin-frontend/src/views/announcement/Index.vue`)：
     - 公告列表展示：支持分页、标题搜索
     - 新增/编辑公告：支持富文本编辑（使用QuillEditor，参考帮助中心）
     - 富文本编辑器配置：支持标题、加粗、斜体、下划线、删除线、颜色、背景色、列表、对齐、链接、图片、清除格式
     - 表单字段：标题、内容（富文本）、发布日期、排序、状态
     - 操作功能：新增、编辑、删除、启用/禁用
     - 完整的表单验证和错误处理
  6. **添加数据库菜单** (`database/update-20251210-add-announcement-menu.sql`)：
     - 在"内容管理"下添加"公告管理"二级菜单（ID=42）
     - 菜单路径：`/admin/content/announcement`，组件：`announcement/Index`
     - 权限标识：`admin:announcement:list`
     - 为超级管理员和运营人员角色分配菜单权限
  7. **更新路由配置** (`admin-frontend/src/router/index.ts`)：
     - 添加公告管理页面的组件映射：`announcement/Index`
- 功能特点：
  - ✅ 支持富文本编辑，与帮助中心模块一致
  - ✅ 支持图片URL数组，自动转换JSON格式
  - ✅ 支持标题模糊查询
  - ✅ 支持分页查询，默认每页10条
  - ✅ 支持新增、编辑、删除、启用/禁用公告
  - ✅ 完整的表单验证和错误处理
  - ✅ 按发布日期倒序排列，最新公告在前
- 富文本编辑器功能：
  - 标题：H1-H6
  - 文本格式：加粗、斜体、下划线、删除线
  - 颜色：文字颜色、背景颜色
  - 列表：有序列表、无序列表
  - 对齐：左对齐、居中、右对齐
  - 插入：链接、图片
  - 工具：清除格式
- 管理后台公告管理页面开发已完成

## 2025-12-10

### 修复页面空白问题：添加404路由和错误处理
- 修复了重启服务后访问页面出现空白的问题
- 主要修改内容：
  1. **添加404路由处理** (`admin-frontend/src/router/index.ts`)：
     - 添加了 `/admin/:pathMatch(.*)*` 路由，捕获所有未匹配的admin路径
     - 添加了根路径404处理，自动重定向到登录页或首页
     - 在路由守卫中添加路由存在性检查，如果路由不存在则根据登录状态重定向
     - 添加了 `router.onError` 错误处理，处理组件加载失败的情况
  2. **添加全局错误处理** (`admin-frontend/src/main.ts`)：
     - 添加了 `app.config.errorHandler` 全局错误处理器
     - 添加了 `unhandledrejection` 事件监听器，处理未捕获的Promise错误
     - 当组件加载失败时，自动重定向到首页（已登录）或登录页（未登录）
- 问题原因：
  - 当路由不存在或组件加载失败时，Vue Router会显示空白页面
  - 没有404路由处理，导致访问不存在的路由时页面空白
  - 没有错误处理机制，组件加载失败时无法自动恢复
- 解决方案：
  - 添加404路由，捕获所有未匹配的路由
  - 在路由守卫中检查路由是否存在，不存在则重定向
  - 添加路由错误处理和全局错误处理，处理组件加载失败的情况
  - 根据用户登录状态，重定向到首页（已登录）或登录页（未登录）

## 2025-12-10

### 修复编译错误：创建PreDeposit实体类和修复Lombok配置
- 修复了启动服务时的编译错误
- 主要修改内容：
  1. **创建PreDeposit实体类** (`backend/src/main/java/com/shoppingmall/entity/PreDeposit.java`)：
     - 预存款实体类，对应 `pre_deposit` 表
     - 字段包括：id、userId、balance、availableBalance、frozenBalance、updateTime
     - 使用MyBatis-Plus注解和Lombok的@Data注解
  2. **修复Maven编译插件配置** (`backend/pom.xml`)：
     - 在maven-compiler-plugin中添加annotationProcessorPaths配置
     - 配置Lombok注解处理器路径，确保Lombok能正确生成getter/setter方法
     - 移除了不必要的fork和executable配置，简化编译配置
- 问题原因：
  - `PreDeposit.java` 文件为空，导致编译错误
  - Maven编译插件未正确配置Lombok注解处理器，导致Lombok生成的getter/setter方法无法被识别
- 解决方案：
  - 根据数据库表结构创建完整的PreDeposit实体类
  - 在maven-compiler-plugin中显式配置Lombok注解处理器
  - 建议执行 `mvn clean compile` 清理并重新编译项目

## 2025-12-10

### 完成公告模块后端接口和数据库表对接
- 已完成公告模块的后端接口和数据库表对接，支持富文本内容和图片，参考帮助中心模块设计
- 主要修改内容：
  1. **创建数据库表** (`database/update-20251210-add-announcement-table.sql`)：
     - 创建 `announcement` 表：公告表，支持富文本内容和图片
     - 字段包括：id、title、content（LONGTEXT，HTML格式）、images（TEXT，JSON格式的图片URL数组）、publish_date（发布日期）、sort（排序）、status（状态）、deleted（逻辑删除）、create_time、update_time
     - 添加必要的索引：publish_date、status、sort、deleted
     - 初始化6条测试数据
  2. **创建实体类** (`backend/src/main/java/com/shoppingmall/entity/Announcement.java`)：
     - 公告实体类，对应 `announcement` 表
     - 使用MyBatis-Plus注解：@TableName、@TableId、@TableLogic、@TableField
     - 支持逻辑删除和自动填充时间
  3. **创建Repository层** (`backend/src/main/java/com/shoppingmall/repository/announcement/AnnouncementRepository.java`)：
     - 公告数据访问接口，继承BaseMapper
  4. **创建VO类** (`backend/src/main/java/com/shoppingmall/vo/AnnouncementVO.java`)：
     - 公告视图对象，包含完整的公告信息
     - images字段为List<String>类型，方便前端使用
  5. **创建Service层**：
     - `AnnouncementService.java`：公告服务接口（前端使用）
       - `getAnnouncementList`：分页查询公告列表（只返回启用的公告）
       - `getAnnouncementById`：根据ID获取公告详情
       - `getPrevAnnouncement`：获取上一篇公告
       - `getNextAnnouncement`：获取下一篇公告
     - `AnnouncementServiceImpl.java`：公告服务实现类
       - 只返回启用的公告（status=1，deleted=0）
       - 按发布日期倒序、排序字段升序排列
       - 上一篇/下一篇逻辑：先按发布日期，再按排序字段
       - 自动解析图片JSON数组
  6. **创建Controller层** (`backend/src/main/java/com/shoppingmall/controller/common/AnnouncementController.java`)：
     - `GET /api/common/announcement/list`：分页查询公告列表
     - `GET /api/common/announcement/{id}`：获取公告详情
     - `GET /api/common/announcement/{id}/prev`：获取上一篇公告
     - `GET /api/common/announcement/{id}/next`：获取下一篇公告
- 功能特点：
  - ✅ 支持富文本内容（HTML格式），与帮助中心模块一致
  - ✅ 支持图片URL数组（JSON格式），自动解析为List<String>
  - ✅ 只返回启用的公告，已删除或禁用的公告不显示
  - ✅ 按发布日期倒序排列，最新公告在前
  - ✅ 支持上一篇/下一篇导航，方便用户浏览
  - ✅ 分页查询支持，默认每页20条
  - ✅ 完整的参数校验和异常处理
- 接口说明：
  - **公告列表接口** (`GET /api/common/announcement/list`)：
    - 请求参数：pageNum（页码，默认1）、pageSize（每页大小，默认20）
    - 返回结果：分页结果，包含records（列表）、total（总数）、current（当前页）、size（每页大小）、pages（总页数）
  - **公告详情接口** (`GET /api/common/announcement/{id}`)：
    - 请求参数：id（公告ID）
    - 返回结果：公告详情，包含完整的HTML内容和图片数组
  - **上一篇/下一篇接口** (`GET /api/common/announcement/{id}/prev` 和 `/next`)：
    - 请求参数：id（当前公告ID）
    - 返回结果：上一篇/下一篇公告，如果没有则返回null
- 公告模块后端接口和数据库表对接已完成

### 优化公告列表页面：移除左侧红色日期显示
- 已优化公告列表页面，移除标题左侧的红色日期文字，只保留右侧的发布时间
- 主要修改内容：
  1. **修改公告列表模板** (`frontend/src/views/news/List.vue`)：
     - 移除左侧的红色日期标签（`date-label`）
     - 保留标题和右侧的发布时间
  2. **优化样式**：
     - 移除 `.date-label` 相关样式
     - 调整 `.item-left` 样式，移除 `gap: 15px`（因为不再需要日期和标题之间的间距）
- 修改效果：
  - ✅ 公告列表页面更简洁，只显示标题和右侧发布时间
  - ✅ 避免日期信息重复显示
  - ✅ 布局更清晰，符合用户需求
- 公告列表页面优化已完成

### 完成预存款交易记录管理页面开发
- 已完成预存款交易记录管理页面的前后端开发，包括数据库菜单、后端接口和前端页面
- 主要修改内容：
  1. **数据库菜单脚本** (`database/update-20251210-add-deposit-record-menu.sql`)：
     - 在"采购者管理"下添加"预存款交易记录"二级菜单（ID=42）
     - 菜单路径：`/admin/buyer/deposit`，组件：`deposit/Record`
     - 权限标识：`admin:deposit:list`
     - 为超级管理员和运营人员角色分配菜单权限
  2. **后端DTO和VO**：
     - `AdminDepositQueryDTO.java` - 管理后台预存款查询DTO，支持多条件查询
     - `AdminDepositRecordVO.java` - 管理后台预存款交易记录VO，包含完整的交易信息
  3. **后端Service层** (`DepositService.java` 和 `DepositServiceImpl.java`)：
     - `getDepositRecordList` - 分页查询预存款交易记录，支持多条件筛选
     - `getDepositRecordById` - 根据ID获取交易记录详情
     - 支持用户名模糊搜索（通过先查询用户ID再查询记录）
     - 支持事件类型、交易类型、状态、订单号、外部交易号、内部订单号、日期范围等查询条件
     - 自动关联用户信息，填充用户名
     - 自动转换类型和状态为中文名称
  4. **后端Controller层** (`DepositController.java`)：
     - `GET /api/admin/deposit/records` - 分页查询预存款交易记录
     - `GET /api/admin/deposit/record/{id}` - 获取交易记录详情
  5. **前端API** (`admin-frontend/src/api/admin/deposit.ts`)：
     - 定义 `DepositQueryDTO` 和 `DepositRecordVO` 类型
     - 实现 `getDepositRecordList` 和 `getDepositRecordById` API调用
  6. **前端页面** (`admin-frontend/src/views/deposit/Record.vue`)：
     - 搜索表单：支持用户名、用户ID、事件类型、交易类型、状态、订单号、外部交易号、内部订单号、日期范围等查询
     - 列表展示：显示记录ID、用户名、用户ID、事件、类型、状态、金额信息（存入/支出/冻结/解冻）、余额信息、支付方式、订单号、交易号、创建时间等
     - 金额显示：存入金额显示绿色，支出金额显示红色，冻结/解冻金额显示不同颜色
     - 状态标签：不同状态使用不同颜色的标签显示
     - 订单号链接：可点击跳转到订单列表页面
     - 详情对话框：显示完整的交易记录信息，包括所有字段
     - 分页功能：支持分页查询和每页大小调整
  7. **路由配置** (`admin-frontend/src/router/index.ts`)：
     - 添加预存款交易记录页面路由，路径：`/admin/deposit`
     - 设置权限标识：`admin:deposit:list`
- 功能特点：
  - ✅ 支持多条件组合查询（用户名、用户ID、事件类型、交易类型、状态、订单号、交易号、日期范围等）
  - ✅ 用户名模糊搜索（通过先查询用户ID再查询记录）
  - ✅ 完整的交易记录信息展示（金额、余额、订单、交易号等）
  - ✅ 金额颜色区分（存入绿色、支出红色、冻结/解冻不同颜色）
  - ✅ 状态标签显示（不同状态不同颜色）
  - ✅ 订单号可点击跳转
  - ✅ 详情对话框查看完整信息
  - ✅ 分页查询支持
- 查询条件说明：
  - **用户名**：模糊搜索，支持部分匹配
  - **用户ID**：精确查询
  - **事件类型**：在线充值、预存款支付、预存款退款、代充值
  - **交易类型**：充值(1)、消费(2)、退款(3)
  - **状态**：待审核(0)、已通过(1)、已拒绝(2)、支付中(3)、已超时(4)
  - **订单号/外部交易号/内部订单号**：精确查询
  - **日期范围**：支持按创建时间范围查询
- 预存款交易记录管理页面开发已完成

## 2025-12-10

### 最新公告模块页面开发
- 已完成最新公告模块的前端页面开发，包括公告列表页面和公告详情页面，参考截图1:1仿照设计
- 主要修改内容：
  1. **创建公告API接口文件** (`frontend/src/api/common/announcement.ts`)：
     - 定义 `Announcement` 接口：包含id、title、content、publishDate、updateTime等字段
     - 定义 `PageResult` 接口：分页响应数据结构
     - 实现 `getAnnouncementList`：获取公告列表（分页）
     - 实现 `getAnnouncementById`：根据ID获取公告详情
     - 实现 `getPrevAnnouncement`：获取上一篇公告
     - 实现 `getNextAnnouncement`：获取下一篇公告
  2. **创建公告列表页面** (`frontend/src/views/news/List.vue`)：
     - 复用公共组件：TopBar、Header、Navbar、Footer
     - 实现面包屑导航：首页 > 最新公告
     - 实现公告列表展示：每个公告显示日期标签、标题、发布日期
     - 实现分页功能：支持上一页、下一页、页码跳转、输入框跳转到指定页
     - 支持点击公告项跳转到详情页面
     - 预留真实API接口对接入口，暂时使用模拟数据
  3. **创建公告详情页面** (`frontend/src/views/news/Detail.vue`)：
     - 复用公共组件：TopBar、Header、Navbar、Footer
     - 实现面包屑导航：首页 > 最新公告 > 公告标题
     - 实现公告详情展示：标题、发布日期、最后更新时间、正文内容（HTML格式）
     - 实现上一篇/下一篇导航：支持跳转到相邻公告
     - 实现分页信息显示：当前公告序号/总公告数
     - 预留真实API接口对接入口，暂时使用模拟数据
  4. **更新路由配置** (`frontend/src/router/index.ts`)：
     - 添加 `/news` 路由：公告列表页面
     - 添加 `/news/:id` 路由：公告详情页面
     - 设置页面标题和访问权限（游客可访问）
- 页面特点：
  - ✅ 1:1仿照截图设计，布局和样式保持一致
  - ✅ 复用公共组件（顶部、菜单栏、搜索栏、页尾），保持页面风格统一
  - ✅ 公告列表页面：日期标签红色显示，标题可点击，右侧显示发布日期
  - ✅ 公告详情页面：标题加粗显示，发布日期和更新时间清晰展示，正文支持HTML内容渲染
  - ✅ 支持上一篇/下一篇导航，方便用户浏览相邻公告
  - ✅ 完整的分页功能，支持跳转到指定页
  - ✅ 响应式设计，支持移动端访问
  - ✅ 预留真实API接口对接入口，方便后续集成
- 功能特点：
  - ✅ 公告列表支持分页查询，每页显示20条
  - ✅ 公告详情支持HTML内容渲染，包括段落、标题、列表等
  - ✅ 支持上一篇/下一篇导航，提升用户体验
  - ✅ 面包屑导航清晰显示当前位置
  - ✅ 点击公告项自动跳转到详情页面
- 最新公告模块页面开发已完成

## 2025-12-10

### 修复帮助中心左侧栏展开和文章显示问题
- 已修复帮助中心左侧栏点击后无法展开下一级文章名称的问题
- 已实现默认全部展开所有分类
- 主要修改内容：
  1. **添加文章列表存储** (`frontend/src/views/help/Index.vue`)：
     - 添加 `categoryArticlesMap` 来存储每个分类的文章列表（用于左侧栏显示）
     - 实现 `loadCategoryArticles` 函数：加载分类的文章列表并存储到Map中
  2. **修改左侧栏模板**：
     - 在子分类下显示该子分类的文章列表（文章名称）
     - 如果分类没有子分类，直接在该分类下显示文章列表
     - 文章名称可点击，点击后加载文章详情
  3. **优化分类点击逻辑**：
     - 修改 `handleCategoryClick`：点击分类时，加载该分类及其所有子分类的文章列表
     - 如果有子分类，加载所有子分类的文章；如果没有子分类，加载该分类的文章
     - 默认加载第一篇文章
  4. **添加从左侧栏点击文章的功能**：
     - 实现 `handleArticleClickFromSidebar` 函数：处理从左侧栏点击文章的操作
     - 确保相关分类展开，设置活动分类，加载文章详情
  5. **实现默认全部展开**：
     - 在 `onMounted` 中，默认展开所有分类（添加到 `expandedCategories`）
     - 初始化时加载所有分类的文章列表（用于左侧栏显示）
  6. **添加样式**：
     - 添加 `.sub-category-wrapper` 样式：子分类包装器
     - 添加 `.article-items` 样式：文章列表容器
     - 添加 `.article-item-sidebar` 样式：左侧栏文章项，包括hover和active状态
     - 文章项支持缩进显示（子分类下的文章缩进更多）
- 功能特点：
  - ✅ 左侧栏点击分类后，展开显示该分类下的文章名称
  - ✅ 如果有子分类，在子分类下显示文章名称
  - ✅ 如果分类没有子分类，直接显示该分类下的文章名称
  - ✅ 默认全部展开所有分类
  - ✅ 文章名称可点击，点击后加载文章详情
  - ✅ 支持从左侧栏直接点击文章，无需先点击分类
- 修复的问题：
  - ✅ 修复了点击分类后无法展开显示文章名称的问题
  - ✅ 修复了默认不展开的问题，现在默认全部展开
  - ✅ 修复了左侧栏不显示文章名称的问题
- 帮助中心左侧栏展开和文章显示问题已修复

## 2025-12-10

### 修复帮助中心页面文章数据显示问题
- 已修复帮助中心页面只显示分类数据，不显示文章数据的问题
- 主要修改内容：
  1. **修复前端帮助中心页面逻辑** (`frontend/src/views/help/Index.vue`)：
     - 修复了将分类树的 `children`（子分类）误当作文章处理的问题
     - 正确区分分类和文章：分类树的 `children` 是子分类，不是文章
     - 添加 `articleList` 状态来存储当前分类下的文章列表
     - 添加 `getHelpArticlesByCategory` API 调用来获取文章列表
     - 实现 `handleSubCategoryClick` 函数：点击子分类时加载该分类下的文章列表
     - 实现 `loadArticlesByCategory` 函数：根据分类ID加载文章列表
     - 优化 `handleCategoryClick` 函数：如果分类没有子分类，直接加载该分类下的文章列表
     - 修复 `handleArticleClick` 函数：正确处理文章点击，加载文章详情
     - 添加 `backToList` 函数：从文章详情返回文章列表
     - 优化初始化逻辑：正确处理URL参数中的articleId，加载对应的文章和文章列表
  2. **更新前端模板**：
     - 添加文章列表显示区域：当有文章列表且未显示文章详情时，显示文章列表
     - 文章列表项可点击，点击后加载文章详情
     - 在文章详情页面添加"返回列表"按钮，方便用户切换文章
     - 优化文章详情显示：添加文章标题和返回按钮的头部区域
  3. **添加样式**：
     - 添加 `.article-list` 样式：文章列表的样式
     - 添加 `.article-item` 样式：文章列表项的样式，包括hover和active状态
     - 添加 `.article-header` 样式：文章详情头部的样式，包含标题和返回按钮
- 功能特点：
  - ✅ 正确区分分类和文章，分类树的children是子分类，不是文章
  - ✅ 点击分类时，如果是叶子分类（没有子分类），直接加载该分类下的文章列表
  - ✅ 点击子分类时，加载该子分类下的文章列表
  - ✅ 点击文章列表中的文章时，加载并显示文章详情
  - ✅ 支持从文章详情返回文章列表
  - ✅ 支持URL参数传递articleId，页面刷新后保持当前文章
  - ✅ 默认加载第一个分类的文章列表和第一篇文章
- 修复的问题：
  - ✅ 修复了前端将子分类误当作文章处理的问题
  - ✅ 修复了文章数据无法正确加载和显示的问题
  - ✅ 修复了点击分类或子分类时无法获取文章列表的问题
- 帮助中心页面文章数据显示问题已修复

## 2025-12-10

### 帮助中心文章编辑集成富文本编辑器
- 已完成帮助中心文章编辑页面的富文本编辑器集成
- 主要修改内容：
  1. **安装依赖**：
     - 添加 `quill@1.3.7`：富文本编辑器核心库
     - 添加 `@vueup/vue-quill@latest`：Vue 3 的 Quill 封装组件
  2. **修改文章编辑对话框** (`admin-frontend/src/views/help/Index.vue`)：
     - 将普通 textarea 替换为 QuillEditor 富文本编辑器
     - 调整对话框宽度从 900px 增加到 1000px，以适应富文本编辑器
     - 配置编辑器工具栏：标题、加粗、斜体、下划线、删除线、颜色、背景色、列表、对齐、链接、图片、清除格式
     - 设置编辑器高度为 400px，最小编辑区域高度为 350px
     - 保留额外图片URL数组功能作为补充（可选）
  3. **编辑器配置**：
     - 使用 snow 主题（带工具栏的经典主题）
     - 支持 HTML 格式内容
     - 配置完整的工具栏功能
     - 添加占位符提示
- 功能特点：
  - ✅ 支持富文本编辑，包括文本格式设置（加粗、斜体、下划线等）
  - ✅ 支持标题设置（H1-H6）
  - ✅ 支持颜色和背景色设置
  - ✅ 支持有序列表和无序列表
  - ✅ 支持文本对齐
  - ✅ 支持插入链接
  - ✅ 支持插入图片（通过工具栏图片按钮）
  - ✅ 支持清除格式
  - ✅ 编辑器样式与 Element Plus 风格统一
  - ✅ 保留额外图片URL数组功能作为补充
- 编辑器工具栏功能：
  - 标题：H1-H6
  - 文本格式：加粗、斜体、下划线、删除线
  - 颜色：文字颜色、背景颜色
  - 列表：有序列表、无序列表
  - 对齐：左对齐、居中、右对齐
  - 插入：链接、图片
  - 工具：清除格式
- 帮助中心文章编辑富文本编辑器集成已完成

## 2025-12-10

### 修复Bean名称冲突：HelpController
- 已修复两个HelpController的Bean名称冲突问题
- 主要修改内容：
  1. **修复 common/HelpController.java**：
     - 在`@RestController`注解中指定bean名称为`commonHelpController`
  2. **修复 admin/HelpController.java**：
     - 在`@RestController`注解中指定bean名称为`adminHelpController`
- 问题原因：
  - 两个HelpController类都使用了默认的bean名称`helpController`
  - Spring无法区分这两个同名的bean，导致`ConflictingBeanDefinitionException`异常
- 修改效果：
  - ✅ 两个HelpController现在使用不同的bean名称（`commonHelpController`和`adminHelpController`）
  - ✅ 解决了Bean名称冲突问题，后端服务可以正常启动
  - ✅ 不影响现有的API接口功能
- Bean名称冲突已修复

### 修复编译错误：lambda表达式变量问题
- 已修复DepositServiceImpl中lambda表达式引用的局部变量问题
- 主要修改内容：
  1. **修复 DepositServiceImpl.java**：
     - 在lambda表达式中使用`paymentResponse.getMockExternalTradeNo()`时，将其提取为final变量
     - Java要求lambda表达式中引用的局部变量必须是final或effectively final
     - 将`paymentResponse.getMockExternalTradeNo()`提取为`final String mockExternalTradeNo`
- 问题原因：
  - lambda表达式中使用了`paymentResponse`变量，但该变量在lambda外部被赋值，不是effectively final
  - Java编译器不允许在lambda中引用非final的局部变量
- 修改效果：
  - ✅ 编译错误已修复
  - ✅ lambda表达式正常工作
  - ✅ 符合Java语言规范
- 编译错误已修复

### 修复编译错误：javax.validation 导入问题
- 已修复帮助中心DTO类中的验证注解导入错误
- 主要修改内容：
  1. **修复 HelpCategoryDTO.java**：
     - 将 `javax.validation.constraints` 改为 `jakarta.validation.constraints`
     - Spring Boot 3.x 使用 Jakarta EE 规范，验证注解从 javax 迁移到 jakarta
  2. **修复 HelpArticleDTO.java**：
     - 将 `javax.validation.constraints` 改为 `jakarta.validation.constraints`
- 问题原因：
  - Spring Boot 3.x 使用 Jakarta EE 9+，所有 javax.* 包都迁移到了 jakarta.*
  - 验证注解 `@NotNull` 和 `@NotBlank` 需要使用 `jakarta.validation.constraints` 包
- 修改效果：
  - ✅ 编译错误已修复
  - ✅ 验证注解正常工作
  - ✅ 符合 Spring Boot 3.x 规范
- 编译错误已修复

## 2025-12-10

### 完成帮助中心后端代码对接和管理后台开发
- 已完成帮助中心的后端代码对接，包括数据库表设计、后端代码和管理后台页面开发
- 主要修改内容：
  1. **数据库表设计** (`database/update-20251210-add-help-center-tables.sql`)：
     - 创建 `help_category` 表：帮助中心分类表，支持树形结构（parent_id）
     - 创建 `help_article` 表：帮助中心文章表，支持HTML内容和图片URL数组
     - 添加必要的索引：parent_id、category_id、status、sort等
     - 初始化示例数据：6个分类和4篇文章
  2. **后端实体类**：
     - `HelpCategory.java`：帮助中心分类实体类
     - `HelpArticle.java`：帮助中心文章实体类
  3. **Repository数据访问层**：
     - `HelpCategoryRepository.java`：分类数据访问接口
     - `HelpArticleRepository.java`：文章数据访问接口
  4. **DTO和VO类**：
     - `HelpCategoryDTO.java`：分类数据传输对象
     - `HelpArticleDTO.java`：文章数据传输对象（支持图片数组）
     - `HelpCategoryVO.java`：分类视图对象（支持树形结构）
     - `HelpArticleVO.java`：文章视图对象
  5. **Service业务逻辑层**：
     - `common/HelpService.java`：前端使用的帮助中心服务接口
     - `common/impl/HelpServiceImpl.java`：前端服务实现类（只返回启用的分类和文章）
     - `admin/HelpService.java`：管理后台使用的帮助中心管理服务接口
     - `admin/impl/HelpServiceImpl.java`：管理后台服务实现类（支持增删改查、状态管理）
  6. **Controller控制层**：
     - `common/HelpController.java`：前端帮助中心控制器
       - `GET /api/common/help/categories`：获取分类树
       - `GET /api/common/help/articles`：根据分类ID获取文章列表
       - `GET /api/common/help/article/{id}`：根据文章ID获取文章详情
     - `admin/HelpController.java`：管理后台帮助中心管理控制器
       - 分类管理：获取分类树、新增、编辑、删除、启用/禁用
       - 文章管理：分页查询、新增、编辑、删除、启用/禁用
  7. **管理后台页面** (`admin-frontend/src/views/help/Index.vue`)：
     - 使用Tabs切换分类管理和文章管理
     - 分类管理：树形表格展示，支持新增、编辑、删除、启用/禁用
     - 文章管理：列表展示，支持搜索、分页、新增、编辑、删除、启用/禁用
     - 文章编辑：支持HTML内容编辑和图片URL数组配置
     - 使用Element Plus组件：el-table、el-dialog、el-form、el-tree-select等
  8. **管理后台API接口** (`admin-frontend/src/api/admin/help.ts`)：
     - 定义帮助中心分类和文章的DTO、VO类型
     - 实现分类和文章的增删改查API调用
  9. **菜单配置** (`database/update-20251210-add-help-center-menu.sql`)：
     - 添加"内容管理"一级菜单（ID=40）
     - 添加"帮助中心"二级菜单（ID=41）
     - 为超级管理员和运营人员角色分配菜单权限
  10. **前端API对接** (`frontend/src/api/common/help.ts`)：
      - 更新 `getHelpCategories`：对接真实后端API，转换树形结构
      - 更新 `getHelpArticlesByCategory`：对接真实后端API
      - 更新 `getHelpArticleById`：对接真实后端API，转换数据格式
- 功能特点：
  - ✅ 支持分类的树形结构管理（支持多级分类）
  - ✅ 支持文章的富文本内容编辑（HTML格式）
  - ✅ 支持图片URL数组配置（可在文章中插入多张图片）
  - ✅ 支持分类和文章的启用/禁用状态管理
  - ✅ 支持分类和文章的排序功能
  - ✅ 前端只显示启用的分类和文章
  - ✅ 管理后台支持完整的增删改查功能
  - ✅ 文章内容支持HTML格式，可以插入图片、链接等
  - ✅ 图片通过URL数组管理，支持多张图片
- 接口说明：
  - **前端接口**（`/api/common/help`）：
    - 只返回启用的分类和文章
    - 分类以树形结构返回
    - 文章包含完整的HTML内容和图片数组
  - **管理后台接口**（`/api/admin/help`）：
    - 支持分类和文章的完整CRUD操作
    - 支持状态管理（启用/禁用）
    - 支持分页查询文章列表
    - 支持按分类和标题搜索文章
- 帮助中心后端代码对接和管理后台开发已完成

## 2025-12-10

### 添加支付服务接口：预留微信支付宝对接，支持模拟支付测试
- 已完成支付服务接口开发，预留微信、支付宝对接入口，支持模拟支付方便测试
- 主要修改内容：
  1. **创建支付服务接口** (`PaymentService.java`)：
     - 定义 `createPayment` 方法：创建支付订单
     - 定义 `verifyCallback` 方法：验证支付回调数据
  2. **创建支付DTO类**：
     - `PaymentRequestDTO.java` - 支付请求DTO，包含订单号、金额、支付方式、币别等信息
     - `PaymentResponseDTO.java` - 支付响应DTO，包含支付URL、二维码URL、支付参数等
  3. **实现支付服务** (`PaymentServiceImpl.java`)：
     - 支持模拟支付模式（默认启用，方便测试）
     - 预留真实支付接口对接入口（TODO注释）
     - 模拟支付时生成模拟外部交易号
     - 可通过配置文件 `payment.mock.enabled` 控制是否启用模拟支付
  4. **集成支付服务到充值流程** (`DepositServiceImpl.java`)：
     - 修改 `recharge` 方法返回类型为 `PaymentResponseDTO`
     - 在充值后调用支付服务创建支付订单
     - 如果是模拟支付模式，自动模拟支付成功（延迟1秒后自动调用回调）
     - 真实支付模式下，返回支付URL供前端跳转
  5. **更新充值接口** (`DepositController.java`)：
     - 修改返回类型为 `PaymentResponseDTO`
     - 返回完整的支付响应信息
  6. **添加支付配置** (`application.yml`)：
     - 添加 `payment.mock.enabled` 配置项，默认true（模拟支付模式）
- 功能特点：
  - ✅ 支持模拟支付模式，方便测试（默认启用）
  - ✅ 预留真实支付接口对接入口，方便后续替换
  - ✅ 模拟支付时自动模拟支付成功，1秒后自动更新余额
  - ✅ 真实支付时返回支付URL，前端可跳转到支付页面
  - ✅ 可通过配置文件切换模拟/真实支付模式
  - ✅ 完善的日志记录，记录支付流程
- 使用说明：
  - **模拟支付模式**（默认）：
    - 配置：`payment.mock.enabled=true`
    - 充值后自动模拟支付成功，1秒后余额自动到账
    - 生成模拟外部交易号（WX或ALI开头）
  - **真实支付模式**：
    - 配置：`payment.mock.enabled=false`
    - 需要在 `PaymentServiceImpl` 中实现真实的微信/支付宝支付接口
    - 返回真实的支付URL，前端跳转到支付页面
    - 支付完成后，支付平台回调 `/payment/callback` 接口
- 后续对接真实支付接口：
  - 在 `PaymentServiceImpl.createPayment` 方法中实现真实支付接口调用
  - 在 `PaymentServiceImpl.verifyCallback` 方法中实现回调验证逻辑
  - 根据微信/支付宝的SDK文档实现具体逻辑
- 支付服务接口开发已完成

## 2025-12-10

### 重命名数据库更新脚本文件以符合命名规范
- 已根据数据库更新规范重命名预存款明细表更新脚本文件
- 主要修改内容：
  1. **重命名脚本文件**：
     - 将 `database/update_pre_deposit_detail.sql` 重命名为 `database/update-20251210-add-pre-deposit-detail-fields.sql`
     - 符合数据库更新脚本命名规范：`update-YYYYMMDD-description.sql`
  2. **更新脚本格式** (`database/update-20251210-add-pre-deposit-detail-fields.sql`)：
     - 添加规范的脚本头部说明（更新脚本名称、更新日期、更新说明、作者）
     - 添加注释说明 MySQL 的 ALTER TABLE ADD COLUMN 不支持 IF NOT EXISTS
     - 添加注释说明索引添加的幂等性问题
     - 保持原有功能不变，扩展预存款明细表，增加交易记录所需字段
- 修改效果：
  - ✅ 脚本文件命名符合数据库更新规范
  - ✅ 脚本格式符合规范要求，包含完整的头部说明
  - ✅ 添加了幂等性说明，提醒开发者注意重复执行的问题
  - ✅ 便于团队协作和版本管理
- 数据库更新脚本文件已重命名并符合规范

## 2025-12-10

### 实现帮助中心页面前端开发
- 已完成帮助中心页面的前端开发，参考截图1:1仿照设计
- 主要修改内容：
  1. **创建帮助中心页面组件** (`frontend/src/views/help/Index.vue`)：
     - 复用公共组件：TopBar、Header、Navbar、Footer
     - 实现面包屑导航，显示当前位置路径
     - 实现左侧帮助中心导航栏，包含分类和子分类
     - 实现右侧主内容区，支持图片和文字显示
     - 支持分类展开/收起功能
     - 支持文章点击加载内容
     - 支持URL参数传递文章ID，页面刷新后保持当前文章
     - 默认加载第一个分类的第一个文章
  2. **更新帮助中心API** (`frontend/src/api/common/help.ts`)：
     - 更新 `getHelpArticleById` 函数，添加模拟数据
     - 包含多个帮助文章的模拟数据，支持图片和HTML内容
     - 模拟数据包括：顾客必读、会员等级折扣、订单的几种状态、简单的购物流程、体贴的售后服务、支付方式、配送方式等
     - 每个文章包含标题、HTML内容、图片URL数组等字段
  3. **添加路由配置** (`frontend/src/router/index.ts`)：
     - 添加 `/help` 路由，指向帮助中心页面
     - 设置页面标题为"帮助中心"
     - 设置为不需要登录认证（游客可访问）
- 页面特点：
  - ✅ 1:1仿照截图设计，布局和样式保持一致
  - ✅ 复用公共组件（顶部、菜单栏、搜索栏、页尾），保持页面风格统一
  - ✅ 左侧导航栏支持分类展开/收起，当前选中项高亮显示
  - ✅ 右侧内容区支持图片和文字混合显示
  - ✅ 支持HTML内容渲染，包括标题、段落、列表等
  - ✅ 响应式设计，支持移动端访问
  - ✅ 支持URL参数传递，页面刷新后保持当前文章
  - ✅ 预留真实API接口对接入口，方便后续集成
- 帮助中心页面前端开发已完成

## 2025-12-10

### 完善预存款充值功能：优化支付回调和超时处理
- 已完成预存款充值功能的完善，优化支付回调处理，添加超时处理机制
- 主要修改内容：
  1. **扩展数据库表结构** (`database/update-20251210-add-pre-deposit-detail-fields.sql`)：
     - 添加 `internal_order_no` 字段，用于精确查找充值记录
     - 添加 `internal_order_no` 字段索引
     - 添加 `status` 和 `create_time` 联合索引，优化超时查询性能
  2. **修改实体类** (`PreDepositDetail.java`)：
     - 添加 `internalOrderNo` 字段，用于存储内部订单号
  3. **优化充值方法** (`DepositServiceImpl.java`)：
     - 在创建充值记录时保存 `internalOrderNo`，用于后续精确查找
  4. **优化支付回调处理** (`DepositServiceImpl.java`)：
     - 使用 `internalOrderNo` 精确查找充值记录，替代之前的备注模糊匹配
     - 添加重复回调防护：
       - 检查记录状态，如果已通过且外部交易号一致，直接返回（幂等性）
       - 如果已通过但外部交易号不一致，抛出异常
       - 如果已拒绝，抛出异常
     - 完善日志记录，记录重复回调情况
  5. **添加支付超时处理**：
     - 创建 `DepositScheduledService` 接口和 `DepositScheduledServiceImpl` 实现类
     - 实现定时任务，每分钟检查一次超时的充值记录
     - 超时时间设置为30分钟（可配置）
     - 自动将超时的充值记录标记为已拒绝，并在备注中添加"[支付超时]"标记
     - 异常处理：定时任务中的异常不影响系统运行，只记录日志
  6. **启用定时任务功能** (`ShoppingMallApplication.java`)：
     - 添加 `@EnableScheduling` 注解，启用Spring定时任务功能
- 功能特点：
  - ✅ 精确查找：使用 `internalOrderNo` 精确查找充值记录，避免备注匹配的不确定性
  - ✅ 重复回调防护：防止支付平台重复回调导致余额重复增加
  - ✅ 幂等性保证：相同的外部交易号重复回调时，直接返回成功，不重复处理
  - ✅ 支付超时处理：自动处理超过30分钟未支付的充值记录
  - ✅ 完善的日志记录：记录所有关键操作和异常情况
  - ✅ 异常安全：定时任务异常不影响系统运行
- 技术改进：
  - **查找方式优化**：从备注模糊匹配改为 `internalOrderNo` 精确匹配
  - **状态检查**：回调处理前检查记录状态，防止重复处理
  - **超时机制**：定时任务自动清理超时记录，保持数据一致性
  - **索引优化**：添加必要的索引，提升查询性能
- 使用说明：
  - **支付回调**：
    - 支付平台回调时，使用 `internalOrderNo` 精确查找充值记录
    - 如果记录已处理，检查外部交易号是否一致
    - 一致则直接返回（幂等性），不一致则抛出异常
  - **超时处理**：
    - 定时任务每分钟执行一次
    - 检查创建时间超过30分钟的待审核充值记录
    - 自动标记为已拒绝，并添加超时标记
- 预存款充值功能完善已完成

### 优化预存款功能：支持真实外部交易号和统一备注格式
- 已完成预存款功能优化，支持保存真实外部交易号，统一备注格式
- 主要修改内容：
  1. **扩展数据库表结构** (`database/update_pre_deposit_detail.sql`)：
     - 添加 `external_trade_no` 字段，用于存储微信/支付宝返回的真实外部交易号
     - 添加 `external_trade_no` 字段索引，优化查询性能
  2. **修改实体类** (`PreDepositDetail.java`)：
     - 添加 `externalTradeNo` 字段，用于存储外部交易号
  3. **修改Service接口** (`DepositService.java`)：
     - 添加 `handlePaymentCallback` 方法：支付回调处理，更新外部交易号和状态
     - 添加 `depositPayment` 方法：预存款支付（用于订单支付）
     - 添加 `depositRefund` 方法：预存款退款（用于订单退款）
  4. **优化充值逻辑** (`DepositServiceImpl.java`)：
     - 修改 `recharge` 方法：生成内部订单号用于跟踪，不再直接更新余额
     - 充值记录初始状态为待审核，等待支付回调确认后再更新余额
     - 初始备注使用内部订单号，支付回调后更新为真实外部交易号
     - 备注格式：`预存款充值:外部交易号(真实外部交易号)`
  5. **实现支付回调处理** (`DepositServiceImpl.java`)：
     - 实现 `handlePaymentCallback` 方法
     - 根据内部订单号查找充值记录
     - 更新外部交易号和备注
     - 支付成功时更新预存款余额和状态
     - 支付失败时更新状态为已拒绝
  6. **实现预存款支付方法** (`DepositServiceImpl.java`)：
     - 实现 `depositPayment` 方法
     - 检查预存款余额是否充足
     - 扣除预存款余额
     - 创建支付记录，备注格式：`预存款支付:订单号{订单号}`
     - 关联订单ID和订单号
  7. **实现预存款退款方法** (`DepositServiceImpl.java`)：
     - 实现 `depositRefund` 方法
     - 增加预存款余额
     - 创建退款记录，备注格式：`预存款退款:订单号{订单号}`
     - 关联订单ID和订单号
  8. **添加支付回调接口** (`DepositController.java`)：
     - 添加 `POST /api/buyer/member/deposit/payment/callback` 接口
     - 接收微信/支付宝支付回调数据
     - 解析内部订单号、外部交易号、交易状态
     - 调用Service处理回调
- 功能特点：
  - ✅ 充值记录保存真实的外部交易号（从支付接口回调获取）
  - ✅ 支付和退款记录正确关联订单号
  - ✅ 统一的备注格式：
    - 充值：`预存款充值:外部交易号(真实外部交易号)`
    - 支付：`预存款支付:订单号{订单号}`
    - 退款：`预存款退款:订单号{订单号}`
  - ✅ 充值流程改为异步处理：创建记录后等待支付回调，回调成功后再更新余额
  - ✅ 完善的支付回调处理，支持支付成功和失败两种状态
  - ✅ 预存款支付和退款方法，方便订单模块调用
- 使用说明：
  - **充值流程**：
    1. 调用充值接口创建充值记录（状态：待审核）
    2. 调用支付接口发起支付（使用返回的内部订单号）
    3. 支付完成后，支付平台回调 `/payment/callback` 接口
    4. 回调接口更新外部交易号和状态，更新预存款余额
  - **订单支付**：
    - 调用 `depositPayment(userId, orderId, orderNo, amount)` 方法
    - 自动扣除预存款余额，创建支付记录
  - **订单退款**：
    - 调用 `depositRefund(userId, orderId, orderNo, amount)` 方法
    - 自动增加预存款余额，创建退款记录
- 预存款功能优化已完成

### 前端预存款功能对接真实API
- 已完成预存款充值和我的预存款页面的前端API对接
- 主要修改内容：
  1. **创建预存款API服务** (`frontend/src/api/buyer/deposit.ts`)：
     - 定义 `DepositRechargeDTO` - 充值请求DTO
     - 定义 `DepositQueryDTO` - 查询条件DTO
     - 定义 `DepositRecordVO` - 交易记录VO
     - 定义 `DepositBalanceVO` - 余额和记录列表VO
     - 实现 `rechargeDeposit` - 预存款充值API调用
     - 实现 `getDepositBalance` - 获取预存款余额和交易记录API调用
  2. **修改预存款充值页面** (`frontend/src/views/member/DepositRecharge.vue`)：
     - 导入 `rechargeDeposit` API函数和 `useRouter`
     - 替换模拟支付为真实API调用
     - 充值成功后自动跳转到预存款页面
     - 完善错误处理和用户提示
  3. **修改我的预存款页面** (`frontend/src/views/member/DepositBalance.vue`)：
     - 导入 `getDepositBalance` API函数和 `DepositRecordVO` 类型
     - 删除模拟数据生成函数 `generateMockRecords`
     - 替换模拟数据为真实API调用
     - 修复分页逻辑：后端已分页，直接使用返回的数据
     - 修复类型定义：使用 `DepositRecordVO` 替代本地 `DepositRecord` 接口
     - 完善金额字段处理：支持null/undefined，使用Number转换确保正确显示
     - 修复分页变化时自动加载数据：`handlePageChange`、`handleSizeChange`、`handleGoToPage` 都会调用 `loadRecords`
- 功能特点：
  - ✅ 预存款充值功能完全对接后端API
  - ✅ 我的预存款页面完全对接后端API
  - ✅ 支持分页查询，分页变化时自动加载数据
  - ✅ 支持按操作类型和时间范围筛选
  - ✅ 完善的错误处理和用户提示
  - ✅ 充值成功后自动跳转到预存款页面
  - ✅ 金额字段安全处理，避免null/undefined错误
- 前端预存款功能API对接已完成

### 实现预存款功能后端代码
- 已完成预存款充值和我的预存款页面的后端代码开发
- 主要修改内容：
  1. **扩展数据库表结构** (`database/update_pre_deposit_detail.sql`)：
     - 扩展 `pre_deposit_detail` 表，增加交易记录所需字段
     - 新增字段：存入金额、支出金额、冻结金额、解冻金额、当前余额、可用余额、事件描述、备注、关联订单ID、关联订单号
     - 添加相关索引，优化查询性能
  2. **创建实体类**：
     - `PreDeposit.java` - 预存款实体类，对应 `pre_deposit` 表
     - `PreDepositDetail.java` - 预存款明细实体类，对应 `pre_deposit_detail` 表
  3. **创建Repository层**：
     - `PreDepositRepository.java` - 预存款数据访问层
     - `PreDepositDetailRepository.java` - 预存款明细数据访问层
  4. **创建DTO类**：
     - `DepositRechargeDTO.java` - 预存款充值DTO，包含金额、币别、支付方式
     - `DepositQueryDTO.java` - 预存款查询DTO，包含分页、操作类型、时间范围等筛选条件
  5. **创建VO类**：
     - `DepositBalanceVO.java` - 预存款余额VO，包含余额、可用余额、交易记录列表
     - `DepositRecordVO.java` - 预存款交易记录VO，包含事件、金额、余额、时间、备注等信息
  6. **创建Service层**：
     - `DepositService.java` - 预存款服务接口
     - `DepositServiceImpl.java` - 预存款服务实现类
     - 实现充值功能：创建充值订单，更新预存款余额，记录交易明细
     - 实现余额查询功能：获取预存款余额和交易记录，支持分页和筛选
  7. **创建Controller层**：
     - `DepositController.java` - 预存款控制器
     - 实现接口：
       - `POST /api/buyer/member/deposit/recharge` - 预存款充值接口
       - `GET /api/buyer/member/deposit/balance` - 获取预存款余额和交易记录接口
- 功能特点：
  - ✅ 支持预存款充值，支持微信支付和支付宝支付
  - ✅ 自动创建预存款账户（如果不存在）
  - ✅ 充值成功后自动更新预存款余额和可用余额
  - ✅ 记录完整的交易明细，包括存入金额、支出金额、冻结金额、解冻金额、当前余额、可用余额
  - ✅ 支持按操作类型筛选（预存款支付、在线充值、预存款退款、代充值）
  - ✅ 支持按时间范围筛选（起始时间、结束时间）
  - ✅ 支持分页查询交易记录
  - ✅ 交易记录按创建时间倒序排列
  - ✅ 完整的参数校验和异常处理
  - ✅ 事务管理，确保数据一致性
- 接口说明：
  - **充值接口** (`POST /api/buyer/member/deposit/recharge`)：
    - 请求参数：amount（金额，必填，最小0.01）、currency（币别，必填）、paymentMethod（支付方式，必填）
    - 返回结果：支付订单号（用于后续支付回调）
    - 功能：创建充值订单，如果是线上支付（微信/支付宝），直接通过并更新余额
  - **余额查询接口** (`GET /api/buyer/member/deposit/balance`)：
    - 请求参数：pageNum（页码，默认1）、pageSize（每页大小，默认10）、operationType（操作类型，可选）、startDate（起始时间，可选）、endDate（结束时间，可选）
    - 返回结果：预存款余额、可用余额、交易记录列表、总记录数
    - 功能：获取当前用户的预存款余额和交易记录，支持筛选和分页
- 预存款功能后端代码开发已完成

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

