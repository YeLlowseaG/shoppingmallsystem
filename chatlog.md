# Shopping Mall System 开发日志

## 2025-12-09 Session

### 任务：启动服务并对接数据

#### 1. 启动服务
**成功启动三个服务：**
- ✅ 后端服务 (端口 8081)
- ✅ 用户前端 (端口 3000)  
- ✅ 管理员前端 (端口 3001)

**遇到的问题及解决：**

1. **编译错误：DataSourceConfig.java** - Druid 依赖冲突
   - 解决：删除 DataSourceConfig.java，使用 Spring Boot 默认的 HikariCP

2. **编译错误：CaptchaUtil.java 不存在**
   - 解决：从 dev 分支恢复 CaptchaUtil.java 文件

3. **编译错误：FileUtil.java 不存在**
   - 解决：从 dev 分支恢复 FileUtil.java 文件

4. **运行错误：MyBatis sqlSessionFactory 配置问题**
   - 原因：主类中排除了 DataSourceAutoConfiguration
   - 解决：移除 `@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})` 中的 exclude 参数

5. **运行错误：Caffeine Cache Bean 不存在**
   - 解决：从 dev 分支恢复 CacheConfig.java 文件

**最终恢复/创建的文件：**
- `CaptchaUtil.java` - 验证码工具类
- `FileUtil.java` - 文件工具类  
- `CacheConfig.java` - Caffeine 缓存配置
- 删除 `DataSourceConfig.java` - 移除 Druid 配置

#### 2. 前后端对接检查

**后端 API 状态：** ✅ 已准备好
- `GET /api/buyer/product/page` - 商品列表（分页）
- `GET /api/buyer/product/{id}` - 商品详情
- `GET /api/buyer/product/hot` - 热门商品
- `GET /api/buyer/product/recommend/{categoryId}` - 推荐商品

**数据库状态：** ⚠️ 表结构存在，但无数据
- product 表记录数：0

**前端状态：** 🔄 使用 Mock 数据
- 首页 (Index.vue)：硬编码楼层数据
- 列表页 (List.vue)：使用 `generateMockProducts()` 生成模拟数据
- 详情页 (Detail.vue)：硬编码商品详情

#### 3. 下一步任务

**进行中：**
- [ ] 向数据库添加测试商品数据（每个三级分类至少 4 个商品）
- [ ] 修改前端代码，将 Mock 数据替换为真实 API 调用

**待完成：**
- [ ] 对接首页热门商品和推荐商品
- [ ] 对接商品列表页
- [ ] 对接商品详情页

---

### 任务：修复管理后台菜单配置问题

**背景：** 管理后台左侧菜单点击后跳转 URL 错误，需要全面检查和修复菜单配置。

#### 问题列表及解决方案

**1. 菜单路径配置错误**
- 问题：点击"商品列表"跳转到 `/admin/list` 而非 `/admin/product/list`
- 原因：数据库 `sys_menu` 表中路径配置存在重复（子菜单路径 `product/list` + 父菜单路径 `/product`）
- 解决：
  ```sql
  UPDATE sys_menu SET path = 'list' WHERE id = 11;  -- 商品列表
  UPDATE sys_menu SET path = 'category' WHERE id = 13;  -- 商品分类
  UPDATE sys_menu SET path = 'list' WHERE id = 19;  -- 采购者列表
  UPDATE sys_menu SET path = 'audit' WHERE id = 20;  -- 采购者审核
  ```

**2. Layout 组件路径生成逻辑错误**
- 问题：`getMenuPath()` 函数未正确处理父子菜单路径拼接
- 解决：修改 `admin-frontend/src/components/Layout/index.vue:168`
  - 函数签名改为 `getMenuPath(menu: MenuVO, parent?: MenuVO)`
  - 模板调用改为 `getMenuPath(child, menu)`

**3. 动态路由在页面刷新后未注册**
- 问题：刷新后菜单显示正常但点击报错 "No match found"
- 原因：`onMounted` 钩子调用 `adminStore.init()` 恢复了 localStorage 中的菜单，但未调用 `addRoutes()`
- 解决：在 Layout 组件 `onMounted` 中添加：
  ```typescript
  if (adminStore.menus && adminStore.menus.length > 0) {
    addRoutes(adminStore.menus)
  }
  ```

**4. Vue Router 警告：父路由未找到**
- 问题：`[Vue Router warn]: Parent route "admin" not found when adding child route`
- 原因：`router/index.ts` 中父路由缺少 `name` 属性
- 解决：添加 `name: 'admin'` 到 `/admin` 路由配置

**5. Vite 动态导入限制**
- 问题：`Error: Unknown variable dynamic import: ../views/order/List.vue`
- 原因：Vite 的 `import()` 只支持单层变量，无法处理 `order/List` 这样的多级路径
- 解决：创建显式 componentMap 映射表，在 `router/index.ts` 中预声明所有组件路径：
  ```typescript
  const componentMap: Record<string, any> = {
    'dashboard/Index': () => import('@/views/dashboard/Index.vue'),
    'product/Add': () => import('@/views/product/Add.vue'),
    'order/List': () => import('@/views/order/List.vue'),
    // ... 共 25 个组件映射
  }
  ```

**6. 缺少 Vue 组件文件**
- 问题：数据库配置的 17 个菜单项对应的组件文件不存在
- 解决：创建所有缺失的组件文件：
  - product/Add.vue
  - order/List.vue
  - stock/* (List, Warning, Adjust, Statistics)
  - buyer/Level.vue
  - marketing/* (Promotion, Price)
  - statistics/* (Sales, Order, Product, Buyer)
  - system/* (Basic, Payment, Logistics, Notification)

#### 数据库菜单结构调整

```sql
-- 标准化父菜单路径（移除前导斜杠）
UPDATE sys_menu SET path = TRIM(LEADING '/' FROM path)
WHERE menu_type = 0 AND parent_id = 0 AND id > 1;

-- 调整首页结构
UPDATE sys_menu SET parent_id = 0, path = 'dashboard' WHERE id = 10;
DELETE FROM sys_menu WHERE id = 1;
```

#### 最终状态
✅ 所有 25 个管理后台菜单项配置正确
✅ 菜单点击跳转 URL 正确
✅ 动态路由注册正常（登录和刷新场景）
✅ 所有组件文件已创建
✅ 符合 Vite 构建工具要求

#### 技术要点
- **菜单路径规则：** 父菜单使用相对路径（如 `product`），子菜单也使用相对路径（如 `list`），最终拼接为 `/admin/product/list`
- **动态路由注册：** 必须在 Layout 组件的 `onMounted` 中调用，处理刷新场景
- **Vite 限制：** 动态导入的变量只能表示一层深度的文件名，需要显式映射表

---

### 任务：添加测试商品数据

**需求：** 每个三级分类至少添加 4 个测试商品

#### 遇到的问题

**1. 商品状态字段类型不匹配**
- 问题：API 返回空列表，虽然数据库有 42 条记录
- 原因：数据库 `status` 字段为 `TINYINT` 存储 `1/0`，但 Java 实体类为 `String` 类型，查询条件 `status = "上架"` 无法匹配数字 `1`
- 解决：
  ```sql
  ALTER TABLE product MODIFY COLUMN status VARCHAR(20) DEFAULT '下架';
  UPDATE product SET status = '上架' WHERE status = '1';
  UPDATE product SET status = '下架' WHERE status = '0';
  ```

**2. 测试数据创建**
- 创建文件：`database/test_products.sql`
- 数据量：42 个商品，覆盖 8 个三级分类
- 每个分类：4-6 个商品不等
- 分类覆盖：
  - 充气娃娃系列（全身款、半身款、局部款、智能款）
  - 名器倒模系列（AV女优款、3D仿真款、智能加热款、便携款）
  - 自慰器系列（飞机杯、跳蛋、前列腺按摩器、震动棒）
  - 延时喷剂系列（持久喷剂、延时湿巾、口服产品、麻醉喷剂）
  - 润滑液系列（水溶性、硅基、高黏度、冰感型）
  - SM用品系列（手铐脚铐、眼罩口塞、鞭类、束缚绳）
  - 情趣内衣系列（透视装、护士装、学生装、开档装）
  - 避孕套系列（超薄、螺纹颗粒、延时、香味）

#### API 验证成功
- ✅ `GET /api/buyer/product/page` - 返回 42 条商品记录
- ✅ 数据正确显示在用户前端商品列表页
- ✅ 管理后台商品列表也能正常显示

---

### 任务：继续修复管理后台菜单

#### 扫描项目发现的问题

**验证结果：**
- ✅ 所有 26 个 Vue 组件文件已创建
- ✅ 所有组件已添加到路由映射表
- ✅ 数据库现有 25 个活动菜单项（menu_type=1, deleted=0）
- ⚠️ 菜单 ID 16（库存预警）组件已创建但在数据库中可能已被删除或标记为 deleted

**已创建的组件清单（26个）：**
```
admin-frontend/src/views/auth/Login.vue
admin-frontend/src/views/buyer/Audit.vue
admin-frontend/src/views/buyer/Level.vue
admin-frontend/src/views/buyer/List.vue
admin-frontend/src/views/dashboard/Index.vue
admin-frontend/src/views/marketing/Price.vue
admin-frontend/src/views/marketing/Promotion.vue
admin-frontend/src/views/order/List.vue
admin-frontend/src/views/permission/Menu.vue
admin-frontend/src/views/permission/Role.vue
admin-frontend/src/views/permission/User.vue
admin-frontend/src/views/product/Add.vue
admin-frontend/src/views/product/CategoryManage.vue
admin-frontend/src/views/product/ProductManage.vue
admin-frontend/src/views/statistics/Buyer.vue
admin-frontend/src/views/statistics/Order.vue
admin-frontend/src/views/statistics/Product.vue
admin-frontend/src/views/statistics/Sales.vue
admin-frontend/src/views/stock/Adjust.vue
admin-frontend/src/views/stock/List.vue
admin-frontend/src/views/stock/Statistics.vue
admin-frontend/src/views/stock/Warning.vue  ← 新增
admin-frontend/src/views/system/Basic.vue
admin-frontend/src/views/system/Logistics.vue
admin-frontend/src/views/system/Notification.vue
admin-frontend/src/views/system/Payment.vue
```

**路由映射表已更新：** `admin-frontend/src/router/index.ts` 包含全部 26 个组件映射

#### 菜单配置机制确认

**管理后台菜单完全基于数据库 `sys_menu` 表动态加载：**

1. **数据库配置验证**
   - 25 个活动菜单项（menu_type=1, deleted=0）
   - 9 个目录菜单（menu_type=0）
   - 所有菜单都配置了 `permission`、`icon`、`path`、`component`
   - 采购者审核菜单（ID: 20）状态为禁用（status=0）

2. **动态菜单加载流程**
   ```
   登录 API (/api/admin/user/login)
     ↓
   返回 menus 数组（来自 sys_menu 表）
     ↓
   保存到 Pinia store + localStorage
     ↓
   调用 addRoutes() 动态注册路由
     ↓
   Layout 组件渲染侧边栏
     ↓
   路由守卫检查 permission 权限
   ```

3. **菜单配置字段映射**
   - `path` → 路由路径
   - `component` → Vue 组件文件路径
   - `permission` → 权限标识（路由守卫验证）
   - `icon` → Element Plus 图标
   - `menuName` → 显示名称
   - `sortOrder` → 排序
   - `status` → 启用/禁用
   - `parent_id` → 父子层级关系

4. **数据库菜单配置示例**
   ```sql
   -- 顶级菜单：数据概览
   id=10, menu_name='数据概览', path='dashboard', 
   component='dashboard/Index', permission='admin:dashboard:view'
   
   -- 父菜单：商品管理（目录）
   id=2, menu_type=0, path='product', component='Layout'
   
   -- 子菜单：商品列表
   id=11, parent_id=2, path='list', 
   component='product/ProductManage', permission='admin:product:list'
   ```

**配置原则：**
- 修改菜单只需更新 `sys_menu` 表
- 用户重新登录后生效
- 前端组件需预先创建并添加到路由映射表

---

## 技术栈
- 后端：Spring Boot 3.1.5 + MyBatis Plus + MySQL 8.0
- 前端：Vue 3 + TypeScript + Vite + Element Plus
- 数据库连接池：HikariCP
- 缓存：Caffeine
- 构建工具：Vite 5.0
