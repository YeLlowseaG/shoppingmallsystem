# 修改日志

## 2026-01-08 - 修复首页广告位跳转问题：标题和图片统一使用广告配置的链接

### 问题描述
首页楼层广告位（如 floor_1）配置了商品分类跳转（link_type=1, link_value=1），但点击后跳转的地址不正确。例如配置的是分类ID=1，但实际跳转到了 categoryId=5。

### 原因分析
1. 在 `Index.vue` 中，楼层数据使用了硬编码的 `floorConfig` 映射（如 floor_1 对应 categoryId: 5），而不是使用广告数据中的 `linkValue`
2. 在 `CategoryFloor.vue` 中，标题栏点击使用的是传入的 `categoryId`（硬编码值），而不是广告的 `linkValue`
3. 大图广告没有点击事件处理

### 解决方案
1. **修改 `frontend/src/views/home/Index.vue`**：
   - 在楼层数据中添加 `adLinkType` 和 `adLinkValue` 字段，从广告数据中获取
   - 将广告的 `linkType` 和 `linkValue` 传递给 `CategoryFloor` 组件

2. **修改 `frontend/src/components/home/CategoryFloor.vue`**：
   - 添加 `adLinkType` 和 `adLinkValue` 属性接收广告链接配置
   - 创建统一的 `goToAdTarget()` 方法处理跳转逻辑，优先使用广告配置的 `linkValue`
   - 标题栏点击调用 `goToAdTarget()` 方法
   - 大图广告添加点击事件，也调用 `goToAdTarget()` 方法
   - 如果广告没有配置链接或链接类型为0（无链接），则回退使用 `categoryId`

### 修改内容

1. ✅ `frontend/src/views/home/Index.vue`
   - 更新 `floorData` 类型定义，添加 `adLinkType?: number` 和 `adLinkValue?: string` 字段
   - 在生成楼层数据时，从广告对象中获取 `linkType` 和 `linkValue` 并赋值给 `adLinkType` 和 `adLinkValue`
   - 在模板中将 `adLinkType` 和 `adLinkValue` 传递给 `CategoryFloor` 组件

2. ✅ `frontend/src/components/home/CategoryFloor.vue`
   - 在 `Props` 接口中添加 `adLinkType?: number` 和 `adLinkValue?: string` 属性
   - 创建 `goToAdTarget()` 方法，根据 `adLinkType` 处理不同类型的跳转：
     - `linkType=1`：商品分类 → `/products?categoryId=${adLinkValue}`
     - `linkType=2`：商品详情 → `/products/${adLinkValue}`
     - `linkType=3`：促销活动 → `/products?type=${adLinkValue}`
     - `linkType=4`：外部链接 → 新窗口打开
   - 修改 `goToCategory()` 方法，调用 `goToAdTarget()` 方法
   - 为大图广告添加 `@click="goToAdTarget"` 事件

### 修改后的效果
- ✅ 标题栏点击优先使用广告配置的 `linkValue`，而不是硬编码的 `categoryId`
- ✅ 大图广告点击也使用广告配置的 `linkValue` 进行跳转
- ✅ 标题和图片点击跳转到相同的位置（都使用广告配置的链接）
- ✅ 如果广告没有配置链接，则回退使用默认的 `categoryId`
- ✅ 支持所有广告链接类型：商品分类、商品详情、促销活动、外部链接

---

## 2025-12-27 - 商品列表和库存列表分类选择改为级联选择器

### 修改内容

1. **商品列表页面 (`admin-frontend/src/views/product/ProductManage.vue`)**
   - 将搜索栏的分类下拉选择从 `el-select` 改为 `el-cascader`，支持多级树展开
   - 将编辑对话框中的分类选择从 `el-select` 改为 `el-cascader`
   - 添加 `cascaderProps` 配置，与商品发布页面保持一致
   - 修改 `loadProductList` 函数，处理级联选择器返回的数组值（取最后一个值）
   - 修改 `handleSubmit` 函数，处理编辑时的分类ID（级联选择器返回数组）
   - 更新 `searchForm.categoryId` 的类型定义，支持数组类型

2. **库存列表页面 (`admin-frontend/src/views/inventory/Index.vue`)**
   - 将搜索栏的分类下拉选择从 `el-select` 改为 `el-cascader`，支持多级树展开
   - 添加 `cascaderProps` 配置，与商品发布页面保持一致
   - 修改 `loadInventoryList` 函数，处理级联选择器返回的数组值（取最后一个值）
   - 修改 `handleReset` 函数，重置时使用 `undefined` 而不是空字符串
   - 更新 `searchForm.categoryId` 的类型定义，支持数组类型

### 技术细节

- 级联选择器配置：
  ```typescript
  const cascaderProps = {
    value: 'id',
    label: 'categoryName',
    children: 'children',
    checkStrictly: true
  }
  ```
- 级联选择器返回数组时，取最后一个值作为分类ID：
  ```typescript
  const categoryId = Array.isArray(searchForm.value.categoryId)
    ? searchForm.value.categoryId[searchForm.value.categoryId.length - 1]
    : searchForm.value.categoryId
  ```

### 变更原因

统一商品列表、库存列表和商品发布页面的分类选择方式，使用多级树展开的级联选择器，提升用户体验。

---

## 2025-12-27 - 更新部署文档为子域名架构（移除路径方式）

### 修改内容

1. **更新服务器部署检查清单.md**
   - 移除测试环境路径方式配置
   - 简化目录结构说明（只保留正式环境）
   - 移除测试环境后端项目配置
   - 简化前端构建说明（只保留正式环境）
   - 更新访问地址说明（移除路径方式测试环境）

2. **更新完全隔离环境部署方案.md**
   - 更新方案概述，说明测试环境也使用子域名（test前缀）
   - 移除测试环境路径方式配置
   - 更新前端环境变量说明（移除test环境路径配置）
   - 更新Vite配置说明（移除路径相关配置）
   - 更新部署步骤说明（简化配置）

3. **更新前端Vite配置**
   - `frontend/vite.config.ts`：移除测试环境路径相关proxy配置，简化build配置
   - `admin-frontend/vite.config.ts`：移除测试环境路径相关proxy配置，简化build配置，修复语法错误

4. **更新后端配置文件**
   - `application-prod.yml`：添加HTTPS代理支持配置
   - `application-test.yml`：更新测试环境前端URL为test前缀子域名，添加HTTPS代理支持

### 架构变更

**正式环境架构（子域名方式）**：
- 用户端：https://www.shop.quaichao.com/
- 管理后台：https://admin.quaichao.com
- API服务：https://api.quaichao.com

**测试环境架构（如需，使用test前缀子域名）**：
- 用户端：https://test-www.shop.quaichao.com
- 管理后台：https://test-admin.quaichao.com
- API服务：https://test-api.quaichao.com

**移除的路径方式**：
- ~~测试环境用户端：www.shop.quaichao.com/test~~
- ~~测试环境管理后台：www.shop.quaichao.com/test/admin~~

### 变更原因

1. **统一架构**：所有环境均使用子域名方式，架构更清晰
2. **避免混淆**：路径方式容易与前端路由冲突
3. **便于扩展**：子域名方式更便于独立配置和管理
4. **符合规范**：符合现代Web应用部署最佳实践

### 代码修改清单

1. ✅ `frontend/vite.config.ts` - 移除路径相关配置
2. ✅ `admin-frontend/vite.config.ts` - 移除路径相关配置，修复语法
3. ✅ `backend/src/main/resources/application-prod.yml` - 添加HTTPS代理支持
4. ✅ `backend/src/main/resources/application-test.yml` - 更新为test前缀子域名

### 需要执行的配置

1. 创建前端环境变量文件（.env.production），配置VITE_API_BASE_URL=https://api.quaichao.com
2. 在宝塔面板创建三个独立站点（正式环境）
3. 为三个域名分别申请SSL证书
4. 在管理后台配置支付回调地址为https://api.quaichao.com/api/buyer/payment/{wechat|alipay}/notify

## 2025-01-08 - 管理后台路由支持根路径访问（方案一：兼容模式）

### 修改内容

1. **更新路由配置 (admin-frontend/src/router/index.ts)**
   - 添加根路径 `/login` 重定向到 `/admin/login`（支持独立子域名访问）
   - 添加根路径 `/` 重定向到 `/admin/dashboard`（支持独立子域名访问）
   - 保留所有现有的 `/admin/*` 路径（兼容开发环境）
   - 更新路由守卫，同时支持根路径和 `/admin` 路径两种格式
   - 更新404路由处理，根路径和 `/dashboard` 路径重定向到 `/admin/dashboard`

2. **更新 NotFound 组件 (admin-frontend/src/components/NotFound.vue)**
   - 支持根路径格式的检查（`/dashboard`, `/login`, `/`）

3. **更新主入口文件 (admin-frontend/src/main.ts)**
   - 更新访问地址日志，说明支持两种访问方式

### 访问方式

**独立子域名访问（生产环境）**：
- `https://admin.quaichao.com/` → 自动重定向到 `/admin/dashboard`
- `https://admin.quaichao.com/login` → 自动重定向到 `/admin/login`

**开发环境访问（兼容模式）**：
- `http://localhost:3003/admin/` → 直接访问 `/admin/dashboard`
- `http://localhost:3003/admin/login` → 直接访问登录页

### 变更原因

1. **支持独立子域名**：管理后台部署在 `admin.quaichao.com` 时，用户可以直接通过根路径访问，无需 `/admin` 前缀
2. **保持向后兼容**：保留 `/admin/*` 路径，确保开发环境和现有代码不受影响
3. **用户体验优化**：独立子域名访问时，URL更简洁（`/login` 而不是 `/admin/login`）

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 添加根路径重定向，更新路由守卫
2. ✅ `admin-frontend/src/components/NotFound.vue` - 支持根路径格式检查
3. ✅ `admin-frontend/src/main.ts` - 更新访问地址日志

### 注意事项

- 所有动态路由仍然基于 `/admin` 路径添加，确保兼容性
- 组件中的路径跳转代码（如 `router.push('/admin/login')`）仍然有效
- 路由守卫会自动处理两种路径格式的转换

## 2025-01-08 - 修复开发环境登录页访问问题

### 问题描述

开发环境访问 `http://localhost:3003/admin/login` 时，直接跳转到了 `http://localhost:3003/admin/dashboard`，显示空白页。

### 问题原因

1. 路由守卫中，如果用户已登录，访问登录页会跳转到 dashboard
2. 但是 dashboard 路由可能还没有被添加（因为需要菜单数据）
3. 路由匹配逻辑有问题，导致登录页无法正常访问

### 修复内容

1. **优化路由守卫逻辑顺序** (`admin-frontend/src/router/index.ts`)
   - 优先处理登录页：如果是登录页，直接允许访问（无论是否登录）
   - 如果已登录访问登录页，会检查 dashboard 路由是否存在后再跳转
   - 如果 dashboard 路由不存在，会尝试加载菜单数据或允许访问登录页
   - 调整登录检查顺序：在检查路由匹配之前先检查是否需要登录
   - 修复未登录用户访问 dashboard 时的处理逻辑

### 修复后的行为

**未登录用户**：
- 访问 `http://localhost:3003/admin/login` → 正常显示登录页 ✅
- 访问 `http://localhost:3003/admin/dashboard` → 自动跳转到登录页 ✅

**已登录用户**：
- 访问 `http://localhost:3003/admin/login` → 自动跳转到 dashboard（如果路由存在）✅
- 访问 `http://localhost:3003/admin/dashboard` → 正常显示 dashboard ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 优化路由守卫逻辑顺序，修复登录页访问问题

## 2025-01-08 - 修复已登录用户访问登录页时的跳转问题

### 问题描述

已登录用户访问 `http://localhost:3003/admin/login` 时，没有自动跳转到 dashboard 页面。

### 问题原因

1. 路由守卫中检查 dashboard 路由是否存在的逻辑不够准确
2. 即使有菜单数据，dashboard 路由可能还没有被添加
3. 路由查找方式不够全面，可能找不到已添加的 dashboard 路由

### 修复内容

1. **优化登录页跳转逻辑** (`admin-frontend/src/router/index.ts`)
   - 改进 dashboard 路由查找逻辑，支持多种路径格式（`dashboard`、`/dashboard`、`/admin/dashboard`）
   - 如果找不到路由，会通过路由名称查找（包含 `dashboard` 的路由）
   - 如果路由不存在，会强制添加路由并等待更长时间（200ms）
   - 添加详细的日志输出，便于调试
   - 无论路由是否存在，都会跳转到 dashboard（Layout 组件会处理路由不存在的情况）

### 修复后的行为

**已登录用户访问登录页**：
- 访问 `http://localhost:3003/admin/login` → 自动跳转到 `/admin/dashboard` ✅
- 如果 dashboard 路由不存在，会自动添加路由后再跳转 ✅
- 添加了详细的日志输出，便于排查问题 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 优化登录页跳转逻辑，改进路由查找方式

## 2025-01-08 - 修复 dashboard 页面空白问题

### 问题描述

登录后访问 `http://localhost:3003/admin/dashboard`，页面显示空白。

### 问题原因

1. 当访问 dashboard 时，如果路由未匹配，代码会直接 `next()` 允许访问
2. Layout 组件会渲染，但 router-view 中没有对应的子路由，导致空白
3. dashboard 路由可能还没有被添加，或者路由查找逻辑有问题

### 修复内容

1. **优化路由守卫逻辑** (`admin-frontend/src/router/index.ts`)
   - 在检查路由匹配之前，先特殊处理 dashboard 路径
   - 如果访问 dashboard 但路由未匹配，会先尝试添加路由
   - 如果路由添加成功，会重新导航到 dashboard
   - 如果路由添加失败，也会允许访问，但 Layout 会显示提示
   - 增加等待时间（200ms）确保路由添加完成

2. **优化 Layout 组件** (`admin-frontend/src/components/Layout/index.vue`)
   - 使用 router-view 的 slot，当没有组件时显示提示和重试按钮
   - 添加 `handleRetry` 函数，可以重新加载菜单数据并添加路由
   - 添加样式支持，美化提示界面

### 修复后的行为

**已登录用户访问 dashboard**：
- 访问 `http://localhost:3003/admin/dashboard` → 如果路由未匹配，会自动添加路由并重新导航 ✅
- 如果路由添加失败，会显示友好的提示和重试按钮 ✅
- 添加了详细的日志输出，便于排查问题 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 优化 dashboard 路由处理逻辑
2. ✅ `admin-frontend/src/components/Layout/index.vue` - 添加 router-view slot 支持和重试功能

## 2025-01-08 - 修复登录成功后未跳转问题

### 问题描述

登录成功后，接口返回成功，但没有跳转到系统首页（dashboard）。

### 问题原因

1. 登录成功后，路由添加和跳转的时序问题
2. 路由添加后没有等待足够的时间，导致跳转时路由还未完全添加
3. 使用 `router.push` 可能在历史记录中留下登录页

### 修复内容

1. **优化登录成功后的跳转逻辑** (`admin-frontend/src/views/auth/Login.vue`)
   - 在设置 store 状态后，等待 50ms 确保状态已更新
   - 添加路由后，等待 300ms 确保路由已完全添加
   - 使用 `router.replace` 而不是 `router.push`，避免在历史记录中留下登录页
   - 添加错误处理，如果 replace 失败，会尝试使用 push
   - 添加详细的日志输出，便于调试

### 修复后的行为

**登录成功**：
- 设置 token、adminInfo、menus、permissions ✅
- 等待 store 状态更新完成 ✅
- 添加路由并等待路由添加完成 ✅
- 跳转到 `/admin/dashboard` ✅
- 添加了详细的日志输出，便于排查问题 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/views/auth/Login.vue` - 优化登录成功后的跳转逻辑，增加等待时间和错误处理

## 2025-01-08 - 修复路由重复添加问题

### 问题描述

控制台一直输出路由添加信息，路由被重复添加，导致性能问题和日志混乱。

### 问题原因

1. 路由守卫在路由添加后会重新执行（`next({ ...to, replace: true })`）
2. 路由守卫再次检查时，可能路由还没有完全注册好，导致重复添加
3. 没有防止重复添加的机制
4. 日志输出过多，影响调试

### 修复内容

1. **添加防止重复添加的机制** (`admin-frontend/src/router/index.ts`)
   - 添加 `isAddingRoutes` 标志，防止重复添加路由
   - 在路由添加过程中，如果再次调用 `addRoutes`，直接返回
   - 在路由守卫中，如果正在添加路由，直接允许访问，避免重复添加

2. **优化路由查找逻辑**
   - 改进 dashboard 路由查找方式，更准确地判断路由是否存在
   - 如果路由已匹配，直接允许访问，不再重复检查

3. **优化日志输出**
   - 路由添加的详细日志只在开发环境输出
   - 减少不必要的日志输出

### 修复后的行为

**路由添加**：
- 路由只会添加一次，不会重复添加 ✅
- 路由添加过程中，如果再次调用，会跳过 ✅
- 路由守卫不会导致重复添加 ✅
- 日志输出更清晰，只在开发环境输出详细日志 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 添加防止重复添加机制，优化路由查找逻辑和日志输出

## 2025-01-08 - 修复刷新页面后路由丢失问题

### 问题描述

登录成功后可以正常访问 dashboard，但是刷新页面后，页面显示空白，路由匹配到了 404 路由。

### 问题原因

1. 刷新页面后，路由守卫执行时，路由还没有被添加（因为菜单数据是从 localStorage 恢复的，但路由需要重新添加）
2. 路由守卫检查路由匹配时，发现路由未匹配，匹配到了 404 路由
3. 路由添加逻辑在路由匹配检查之后，导致刷新时路由无法正确恢复

### 修复内容

1. **优化路由守卫逻辑** (`admin-frontend/src/router/index.ts`)
   - 在路由守卫的最开始，检查是否已登录且有菜单数据
   - 如果 admin 路由没有子路由，或者当前路径未匹配且不是登录页，先添加路由
   - 等待路由添加完成后，重新匹配路由并导航
   - 添加详细的日志输出，便于调试

### 修复后的行为

**刷新页面**：
- 刷新页面后，路由守卫会先检查路由是否已添加 ✅
- 如果路由未添加，会先添加路由，然后再检查匹配 ✅
- 路由添加完成后，会重新导航到当前路径 ✅
- 添加了详细的日志输出，便于排查问题 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 在路由守卫开始时就检查并添加路由，确保刷新时路由能正确恢复

## 2025-01-08 - 修复刷新页面后匹配到404路由的问题

### 问题描述

刷新页面后，路由匹配到了 404 路由，导致页面空白。修复后出现死循环问题。

### 问题原因

1. 刷新页面时，路由还没有被添加
2. `/admin/dashboard` 路径匹配到了 404 路由 `/admin/:pathMatch(.*)*`
3. 之前的逻辑中，如果路由已匹配（即使是404），就不会添加路由
4. 导致刷新时无法正确恢复路由

### 修复内容

1. **优化路由守卫逻辑** (`admin-frontend/src/router/index.ts`)
   - 在路由守卫的最开始，优先处理404路由的情况
   - 如果匹配到404路由，且已登录，检查是否需要添加路由
   - 如果没有子路由，添加路由并重新导航
   - 避免死循环：如果路由已匹配且不是404，直接允许访问

### 修复后的行为

**刷新页面**：
- 如果匹配到404路由，会先检查并添加路由 ✅
- 路由添加成功后，会重新导航到正确路径 ✅
- 避免了死循环问题 ✅
- 刷新后页面能正常显示 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 优化路由守卫逻辑，优先处理404路由情况，确保刷新时路由能正确恢复

## 2025-01-08 - 修改根路径访问逻辑，根据登录状态跳转

### 修改内容

1. **修改根路径处理逻辑** (`admin-frontend/src/router/index.ts`)
   - 移除根路径 `/` 的静态重定向
   - 在路由守卫中添加根路径处理逻辑
   - 根据登录状态决定跳转：
     - 已登录 → 跳转到 `/admin/dashboard`
     - 未登录 → 跳转到 `/admin/login`

### 修改后的行为

**访问根路径 `http://localhost:3003/`**：
- 如果已登录 → 自动跳转到 `http://localhost:3003/admin/dashboard` ✅
- 如果未登录 → 自动跳转到 `http://localhost:3003/admin/login` ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 移除根路径静态重定向，在路由守卫中根据登录状态动态跳转

## 2025-01-08 - 彻底修复刷新页面后路由丢失问题

### 问题描述

登录成功后，刷新首页出现空白，路由匹配到了404路由，说明路由没有正确恢复。

### 问题原因

1. 刷新页面时，路由守卫执行时，store 可能还没有从 localStorage 恢复数据
2. 路由守卫中的路由添加逻辑执行顺序有问题
3. 路由添加后等待时间不够，导致路由还没有完全注册就继续执行
4. 路由匹配检查不够准确，没有排除404路由

### 修复内容

1. **优化路由守卫逻辑** (`admin-frontend/src/router/index.ts`)
   - 在路由守卫开始时，确保 store 已从 localStorage 恢复数据
   - 如果菜单数据为空，尝试从 localStorage 恢复
   - 优化路由添加和匹配的逻辑：
     - 优先检查并添加路由（在所有其他检查之前）
     - 如果没有子路由，或者匹配到了404路由，或者路由未匹配，都需要添加路由
     - 增加等待时间到 500ms，确保路由已完全添加
     - 改进路由匹配检查，确保不是404路由才认为匹配成功
     - 添加详细的日志输出，便于调试

### 修复后的行为

**刷新页面**：
- 路由守卫开始时，会先确保 store 已初始化 ✅
- 如果菜单数据为空，会从 localStorage 恢复 ✅
- 优先检查并添加路由，确保路由已添加 ✅
- 路由添加后，会正确匹配并导航 ✅
- 避免了匹配到404路由的问题 ✅

### 代码修改清单

1. ✅ `admin-frontend/src/router/index.ts` - 优化路由守卫逻辑，确保 store 初始化，优先添加路由，改进匹配检查

## 2025-01-XX - 修复路由添加后空白页面问题

### 问题
- 刷新 `http://localhost:3003/admin/dashboard` 后出现空白页面
- 路由添加成功后，检查 `hasChildrenAfter` 时发现仍然没有子路由
- 日志显示："刷新页面：路由添加后仍然没有子路由，允许访问，Layout 会处理"

### 修复
- 添加了 `nextTick` 导入，确保 Vue Router 路由表更新后再检查
- 优化了路由添加后的检查逻辑：
  - 使用 `nextTick()` 和 `setTimeout` 等待路由表更新
  - 无论是否有子路由，都尝试重新匹配路由
  - 如果路由匹配成功，重新导航到目标路径
  - 如果路由未匹配但目标是 dashboard，允许访问让 Layout 处理
- 在 `addRoutes` 函数中添加了路由添加验证日志，输出子路由数量

### 修改文件
- `admin-frontend/src/router/index.ts`
  - 添加 `nextTick` 导入
  - 优化路由添加后的检查逻辑
  - 添加路由添加验证日志

## 2025-01-XX - 修复刷新页面时跳转到 dashboard 的问题

### 问题
- 访问 `http://localhost:3003/admin/product/list` 后刷新页面，会跳转到 `http://localhost:3003/admin/dashboard`
- 原因是路由守卫中，当路由未匹配时，如果用户已登录，会强制跳转到 dashboard

### 修复
- 优化了路由未匹配时的处理逻辑：
  - 如果路由添加后仍然未匹配，允许访问让 Layout 处理，而不是跳转到 dashboard
  - 如果路由已存在但未匹配，也允许访问让 Layout 处理
  - 移除了强制跳转到 dashboard 的逻辑（第 507-509 行）
  - 只有在真正找不到路由且未登录时才跳转到登录页
  - 已登录但路由不存在时，允许访问让 Layout 处理（Layout 会显示404或重定向）
- 改进了路由匹配检查，使用 `router.resolve(to.path)` 并排除 404 路由
- 添加了 `nextTick()` 等待路由表更新

### 修改文件
- `admin-frontend/src/router/index.ts`
  - 优化路由未匹配时的处理逻辑（第 452-515 行）
  - 改进路由匹配检查，排除 404 路由
  - 修复 TypeScript 类型检查错误（第 197 行）

## 2025-01-XX - 修复刷新 dashboard 页面空白问题

### 问题
- 刷新 `http://localhost:3003/admin/dashboard` 后出现空白页面
- 路由添加成功后，`adminRoute.children` 仍然是空的（子路由数量: 0）
- 重新导航时匹配到了路由，但再次进入路由守卫时，又匹配到了 `admin-404`
- 日志显示："路由添加验证：admin 路由的子路由数量: 0"

### 问题原因
- `router.addRoute('admin', route)` 添加路由后，`router.getRoutes()` 返回的路由对象的 `children` 属性可能没有立即更新
- Vue Router 的路由表更新是异步的，需要更多时间
- `router.resolve(to.path)` 可以正确匹配到路由，但 `adminRoute.children` 可能还是空的
- 使用 `next({ ...to, replace: true })` 会重新触发路由守卫，但此时路由可能还没有完全注册好，导致又匹配到了404

### 修复
- 优化路由添加后的检查逻辑：
  - 不再依赖 `adminRoute.children` 来检查路由是否已添加
  - 直接使用 `router.resolve(to.path)` 来检查路由是否匹配
  - 增加等待时间到 400ms，确保路由表已更新
  - 如果路由匹配成功，使用 `next()` 而不是 `next({ ...to, replace: true })`，避免重新触发路由守卫
  - 在重新导航前，再次检查路由是否匹配，避免重新触发路由守卫时匹配到404
- 优化404路由处理逻辑：
  - 如果有子路由但匹配到了404，等待一下后重新匹配
  - 如果路由存在但匹配到了404，可能是路由表更新延迟，等待后重新匹配

### 修改文件
- `admin-frontend/src/router/index.ts`
  - 优化路由添加后的检查逻辑（第 274-314 行）
  - 改进404路由处理逻辑（第 315-350 行）
  - 增加等待时间，确保路由表已更新
  - 使用 `next()` 而不是 `next({ ...to, replace: true })`，避免重新触发路由守卫

## 2025-01-08 - 彻底修复刷新页面后匹配到404路由导致空白页的问题

### 问题描述
- 刷新页面后，路由匹配到了 404 路由 `/admin/:pathMatch(.*)*`，导致页面显示空白
- 路由添加成功后，虽然路由已添加到路由表，但当前导航仍然匹配到404路由
- 日志显示："路由守卫 - 当前路径: /admin/dashboard 匹配的路由: ['/admin/:pathMatch(.*)*'] 路由名称: admin-404"
- 路由添加验证显示："admin 路由的子路由数量: 0"，说明路由添加后 children 属性没有立即更新

### 问题根本原因
1. **404路由在动态路由之前定义**：404路由 `/admin/:pathMatch(.*)*` 在路由配置中预先定义（第59-67行），导致在动态路由添加之前，所有 `/admin/*` 路径都会匹配到404路由
2. **路由匹配顺序问题**：Vue Router 按照路由定义的顺序进行匹配，通配符路由会优先匹配，导致动态路由无法正确匹配
3. **路由守卫逻辑过于复杂**：之前的修复尝试通过复杂的逻辑来处理，但无法从根本上解决问题

### 修复内容（基于网上最佳实践）

1. **移除预先定义的404路由** (`admin-frontend/src/router/index.ts`)
   - 移除了路由配置中预先定义的 `/admin/:pathMatch(.*)*` 404路由（第59-67行）
   - 这样可以确保动态路由能够优先匹配

2. **在动态路由添加之后再添加404路由** (`admin-frontend/src/router/index.ts`)
   - 在 `addRoutes` 函数中，在所有动态路由添加完成之后，再添加404路由
   - 使用 `router.addRoute('admin', { path: ':pathMatch(.*)*', ... })` 将404路由作为 admin 路由的子路由添加
   - 检查404路由是否已存在，避免重复添加

3. **简化路由守卫逻辑** (`admin-frontend/src/router/index.ts`)
   - 简化了路由守卫中的路由添加和匹配逻辑
   - 如果检测到路由未添加或匹配到404路由，直接添加路由并使用 `next({ ...to, replace: true })` 重新导航
   - 移除了复杂的等待和重试逻辑，因为现在404路由会在动态路由之后添加，不会出现匹配问题

### 修复后的行为
**刷新页面**：
- 刷新页面后，如果路由未添加，会先添加路由（包括动态路由和404路由）✅
- 路由添加完成后，使用 `next({ ...to, replace: true })` 重新导航 ✅
- 由于404路由在动态路由之后添加，动态路由会优先匹配 ✅
- 避免了页面空白的问题 ✅

### 代码修改清单
1. ✅ `admin-frontend/src/router/index.ts` - 移除预先定义的404路由，在动态路由添加之后再添加404路由，简化路由守卫逻辑

### 参考来源
- Vue Router 动态路由最佳实践：404路由应该在动态路由添加之后再添加
- 网上相关问题的解决方案：延迟添加404路由，确保动态路由优先匹配

## 2025-01-08 - 修复直接访问URL时跳转到dashboard的问题

### 问题描述
- 直接在浏览器输入 `http://localhost:3003/admin/buyer/list` 等URL时，会自动跳转到 `http://localhost:3003/admin/dashboard`
- 刷新页面可以正常显示，但直接访问URL会跳转

### 问题原因
1. 路由守卫在检查路由是否添加时，没有排除登录页
2. 当访问 `/admin/login` 时，如果路由未添加，会触发路由添加逻辑并重新导航到登录页
3. 由于用户已登录，登录页的逻辑会跳转到 dashboard
4. 这导致即使访问其他URL（如 `/admin/buyer/list`），如果路由未添加，也会先导航到登录页，然后跳转到 dashboard

### 修复内容
1. **排除登录页的路由添加逻辑** (`admin-frontend/src/router/index.ts`)
   - 在路由守卫的早期检查中，添加 `isLoginPage` 判断
   - 排除登录页（`/admin/login` 和 `/login`），避免在登录页触发路由添加和重新导航
   - 确保只有访问非登录页的 admin 路径时，才会触发路由添加逻辑

### 修复后的行为
**直接访问URL**：
- 访问 `http://localhost:3003/admin/buyer/list` → 如果路由未添加，会添加路由并正确导航到该页面 ✅
- 不会因为访问登录页而跳转到 dashboard ✅
- 刷新页面和直接访问URL都能正常工作 ✅

### 代码修改清单
1. ✅ `admin-frontend/src/router/index.ts` - 在路由守卫早期检查中排除登录页，避免在登录页触发路由添加逻辑

## 2025-01-08 - 修复登录页路由检查逻辑，使用 router.resolve 检查路由是否存在

### 问题描述
- 直接访问 `/admin/buyer/list` 等URL时，仍然会跳转到 `/admin/dashboard`
- 日志显示：登录页的逻辑在检查 dashboard 路由是否存在时，使用 `adminRoute?.children?.find()` 检查，但路由添加后 `children` 属性不会立即更新
- 导致即使路由已添加，检查结果仍然是 `false`，然后跳转到 dashboard

### 问题原因
1. 登录页的逻辑使用 `adminRoute?.children?.find()` 来检查路由是否存在
2. `router.addRoute()` 后，`router.getRoutes()` 返回的路由对象的 `children` 属性可能不会立即更新
3. 即使路由已添加，检查结果仍然是 `false`，导致不必要的跳转

### 修复内容
1. **优化登录页的路由检查逻辑** (`admin-frontend/src/router/index.ts`)
   - 使用 `router.resolve('/admin/dashboard')` 来检查路由是否存在，而不是检查 `children` 属性
   - `router.resolve()` 可以正确匹配到已添加的路由，即使 `children` 属性还没有更新
   - 简化登录页的逻辑：如果路由未添加，先添加路由，然后直接跳转（路由守卫会处理路由匹配）

### 修复后的行为
**直接访问URL**：
- 访问 `http://localhost:3003/admin/buyer/list` → 如果路由未添加，会添加路由并正确导航到该页面 ✅
- 登录页的逻辑使用 `router.resolve()` 检查路由，可以正确识别已添加的路由 ✅
- 避免了因为 `children` 属性未更新而导致的错误跳转 ✅

### 代码修改清单
1. ✅ `admin-frontend/src/router/index.ts` - 优化登录页的路由检查逻辑，使用 `router.resolve()` 而不是检查 `children` 属性

## 2025-01-08 - 在 dashboard 页面添加刷新数据按钮

### 修改内容
1. **添加刷新按钮** (`admin-frontend/src/views/dashboard/Index.vue`)
   - 在页面顶部标题旁边添加"刷新数据"按钮
   - 按钮带有刷新图标，点击时显示加载状态
   - 只有在有权限时才显示刷新按钮

2. **实现刷新功能**
   - 添加 `handleRefresh` 函数，实现数据刷新逻辑
   - 刷新时先销毁现有图表实例，然后重新加载数据和初始化图表
   - 刷新成功后显示成功提示

3. **优化页面布局**
   - 使用 flex 布局，让标题和按钮在同一行显示
   - 标题和按钮之间自动对齐

### 功能说明
- 点击"刷新数据"按钮后，会重新加载所有统计数据
- 同时会重新初始化所有图表（销售趋势、订单趋势、订单状态分布）
- 刷新过程中按钮显示加载状态，防止重复点击
- 刷新成功后显示成功提示

### 代码修改清单
1. ✅ `admin-frontend/src/views/dashboard/Index.vue` - 添加刷新按钮和刷新功能，优化页面布局

---

## 2025-01-XX 修复正式环境API请求失败问题

### 问题描述
正式环境（https://www.shop.quaichao.com）出现以下错误：
1. **API请求全部失败**：所有API请求返回"请求失败"错误
   - 加载系统配置失败
   - 加载导航菜单失败
   - 加载商品分类失败
   - 加载页脚数据失败
   - 获取验证码失败
2. **CSS文件404错误**：layer.css、laydate.css、code.css文件找不到
3. **Chrome扩展错误**：浏览器扩展导致的语法错误（不影响功能）

### 问题原因
1. **API地址配置错误**：`frontend/.env.production`文件中`VITE_API_BASE_URL`设置为相对路径`/api`，导致构建后的代码无法正确请求API
2. **构建脚本覆盖环境变量**：`scripts/build-user-prod.bat`中设置了空的环境变量，覆盖了`.env.production`文件的配置

### 解决方案
1. **修改环境变量文件**：`frontend/.env.production`
   - 将`VITE_API_BASE_URL`从`/api`改为`https://api.quaichao.com`
   - 确保正式环境使用完整的HTTPS URL进行API请求

2. **修改构建脚本**：`scripts/build-user-prod.bat`
   - 移除覆盖环境变量的代码（`set VITE_API_BASE_URL=`）
   - 让Vite自动读取`.env.production`文件中的配置

### 代码修改清单
1. ✅ `frontend/.env.production` - 修改API地址为`https://api.quaichao.com`
2. ✅ `scripts/build-user-prod.bat` - 移除环境变量覆盖代码，使用.env.production配置

### 后续操作
1. 重新构建前端项目：`npm run build -- --mode production`
2. 重新部署构建产物到服务器
3. 验证API请求是否正常

---

## 2025-01-XX 修复地区API请求返回HTML而非JSON数据问题

### 问题描述
正式环境（https://www.shop.quaichao.com/register）访问地区接口时：
- API请求 `https://www.shop.quaichao.com/api/regions/provinces` 返回200状态码
- 但返回的是HTML页面内容（前端index.html），而不是JSON数据
- 本地开发环境正常

### 问题原因
`frontend/src/api/common/region.ts` 文件直接使用 `axios` 而不是统一的 `request` 工具，导致：
1. **未使用环境变量**：没有使用 `VITE_API_BASE_URL`（`https://api.quaichao.com`）
2. **请求路径错误**：使用相对路径 `/api/regions`，请求发送到 `https://www.shop.quaichao.com/api/regions/provinces`
3. **被前端路由捕获**：该路径被前端路由规则捕获，返回HTML页面而不是代理到后端API

### 解决方案
修改 `frontend/src/api/common/region.ts` 文件：
1. 将 `import axios from 'axios'` 改为 `import request from '@/utils/request'`
2. 将所有 `axios.get()` 调用改为 `request.get()`
3. 移除手动解析响应数据的代码（`request` 工具会自动处理响应拦截器，直接返回 `data`）

### 代码修改清单
1. ✅ `frontend/src/api/common/region.ts` - 修改为使用统一的request工具
   - 修改导入语句：`axios` → `request`
   - 修改 `getProvinces()` 函数
   - 修改 `getChildrenByParentId()` 函数
   - 修改 `getRegionByCode()` 函数
   - 修改 `getFullPathByCode()` 函数

### 修改后的效果
- ✅ 请求会正确发送到：`https://api.quaichao.com/api/regions/provinces`
- ✅ 使用统一的请求拦截器和错误处理
- ✅ 与其他API文件保持一致的使用方式

### 后续操作
1. 重新构建前端项目：`npm run build -- --mode production`
2. 重新部署构建产物到服务器
3. 验证地区选择功能是否正常

---

## 2026-01-08 - 修复正式环境图片上传和访问问题

### 问题描述
1. **图片上传失败**：`https://admin.quaichao.com/api/common/upload/image` 返回 405 Not Allowed
2. **图片访问失败**：上传成功后，图片无法正常访问，返回 404 Not Found

### 问题原因
1. **405错误**：`admin.quaichao.com` 和 `www.shop.quaichao.com` 的Nginx配置中缺少 `/api/` 的location块，无法将API请求代理到后端
2. **404错误**：两个站点的Nginx配置中缺少 `/uploads/` 的location块，无法提供静态文件服务

### 解决方案

#### 1. 修复Nginx配置 - 添加API反向代理
在 `admin.quaichao.com` 和 `www.shop.quaichao.com` 的Nginx配置中，在 `location /` 之前添加：

```nginx
# API反向代理（后端服务）
location /api/ {
    proxy_pass http://127.0.0.1:8081;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    
    proxy_connect_timeout 60s;
    proxy_send_timeout 60s;
    proxy_read_timeout 60s;
    
    proxy_buffering off;
    proxy_request_buffering off;
}
```

**关键点**：
- `proxy_pass` 必须**不带尾部斜杠**（`http://127.0.0.1:8081`），这样才能保留完整路径 `/api/common/upload/image`
- 如果使用 `proxy_pass http://127.0.0.1:8081/;`（带斜杠），Nginx会去掉 `/api/` 前缀，导致路径错误

#### 2. 修复Nginx配置 - 添加文件上传目录静态服务
在 `admin.quaichao.com` 和 `www.shop.quaichao.com` 的Nginx配置中，在 `location /api/` 之后、`location /` 之前添加：

```nginx
# 文件上传目录（静态文件服务）
location /uploads/ {
    alias /www/wwwroot/shopping-mall-backend-prod/uploads/;
    expires 30d;
    add_header Cache-Control "public, immutable";
    
    # 允许访问图片和文档文件
    location ~* \.(jpg|jpeg|png|gif|pdf|doc|docx|xls|xlsx)$ {
        access_log off;
    }
    
    # 禁止访问可执行文件
    location ~* \.(php|jsp|asp|aspx|sh|bat|exe)$ {
        deny all;
    }
}
```

**关键点**：
- `alias` 路径必须以 `/` 结尾
- `location /uploads/` 必须在 `location /` 之前，确保优先匹配
- 路径指向实际的上传文件存储目录：`/www/wwwroot/shopping-mall-backend-prod/uploads/`

#### 3. 配置顺序
正确的配置顺序（从上到下）：
1. `location /api/` - API反向代理
2. `location /uploads/` - 文件上传目录静态服务
3. `location /` - 前端静态文件

### 修改的文件
- `docs/完全隔离环境部署方案.md`
  - 更新了 `4.3.1 用户端站点配置（www.shop.quaichao.com）`
  - 更新了 `4.3.2 管理后台站点配置（admin.quaichao.com）`
  - 添加了 `/api/` 和 `/uploads/` 的location配置块

### 修改后的效果
- ✅ 图片上传功能正常：`POST https://admin.quaichao.com/api/common/upload/image` 返回200
- ✅ 管理后台图片可以正常访问：`https://admin.quaichao.com/uploads/images/2026/01/xxx.jpg`
- ✅ 用户端图片可以正常访问：`https://www.shop.quaichao.com/uploads/images/2026/01/xxx.jpg`
- ✅ API请求正常：两个站点都可以通过 `/api/` 访问后端服务

### 注意事项
1. **配置顺序很重要**：`location /api/` 和 `location /uploads/` 必须在 `location /` 之前
2. **proxy_pass路径**：必须不带尾部斜杠，否则会去掉location匹配的部分
3. **alias路径**：必须以 `/` 结尾，且指向实际的文件存储目录
4. **配置后需要重载Nginx**：
   ```bash
   nginx -t  # 测试配置
   nginx -s reload  # 重载配置
   ```

## 2026-01-08 - 修复正式环境订单详情页图片不显示问题

### 问题描述
正式环境（https://admin.quaichao.com）订单详情页的商品图片无法显示，但商品列表页的图片可以正常显示，用户端也可以正常显示。

### 问题原因
1. **图片URL拼接错误**：订单详情页使用了 `getImageUrl` 函数处理图片路径
2. **使用了错误的baseURL**：`getImageUrl` 函数使用 `import.meta.env.VITE_API_BASE_URL`（`https://api.quaichao.com`）来拼接图片路径
3. **路径错误**：图片路径 `/uploads/images/...` 被拼接成 `https://api.quaichao.com/uploads/images/...`，但图片实际应该通过当前域名（`https://admin.quaichao.com`）访问
4. **对比**：商品列表页直接使用 `row.mainImage`，没有调用 `getImageUrl`，所以能正常显示

### 解决方案
修改 `admin-frontend/src/views/order/List.vue` 中的 `getImageUrl` 函数：
- 将 `import.meta.env.VITE_API_BASE_URL` 改为 `window.location.origin`
- 对于以 `/` 开头的相对路径，使用当前域名而不是API服务器域名
- 确保图片资源通过当前域名访问，而不是API服务器

### 代码修改清单
1. ✅ `admin-frontend/src/views/order/List.vue` - 修改 `getImageUrl` 函数，使用 `window.location.origin` 而不是 `VITE_API_BASE_URL`

### 修改后的效果
- ✅ 订单详情页的图片可以正常显示：`https://admin.quaichao.com/uploads/images/2026/01/xxx.jpg`
- ✅ 与商品列表页和用户端的图片显示方式保持一致
- ✅ 图片路径正确：使用当前域名而不是API服务器域名

### 技术说明
- 图片资源（`/uploads/...`）应该通过当前域名访问，因为Nginx配置中 `/uploads/` 路径会提供静态文件服务
- API请求（`/api/...`）才需要通过 `VITE_API_BASE_URL` 访问后端服务器
- 使用 `window.location.origin` 可以动态获取当前域名，适配不同环境（开发/测试/生产）

## 2026-01-08 - 用户端首页广告位模块优化：动态读取广告名称并隐藏禁用/删除的广告位

### 问题描述
用户端首页的广告位模块标题（如"7F 其他情趣"）是硬编码的，需要从管理后台读取广告名称。如果管理后台禁用或删除了某个广告位，前端仍然会显示该模块。

### 需求
1. 广告位模块的标题需要从管理后台读取广告名称（`adName`）
2. 如果管理后台禁用或删除了某个广告位，前端不应该显示该模块
3. 已删除、禁用或未添加的广告位整个模块应该隐藏

### 解决方案
修改 `frontend/src/views/home/Index.vue`：
1. **动态生成楼层**：根据广告数据动态生成楼层，而不是固定7个楼层
2. **使用广告名称**：从广告数据中读取 `adName` 作为楼层标题
3. **自动过滤**：后端API `getAllFloorAdvertisements()` 已经只返回启用状态的广告，前端只需要根据返回的广告数据生成楼层
4. **配置映射**：创建广告位置与分类ID的映射配置，将广告位置（如 `floor_1`）映射到对应的分类ID和标题颜色

### 代码修改清单
1. ✅ `frontend/src/views/home/Index.vue` - 修改楼层生成逻辑
   - 移除硬编码的7个楼层组件
   - 改为使用 `v-for` 动态生成楼层
   - 根据广告数据动态生成楼层数据
   - 使用广告的 `adName` 作为楼层标题
   - 从广告位置（`adPosition`）中提取楼层编号（如 `floor_1` -> `1F`）
   - 创建 `floorConfig` 配置对象，映射广告位置到分类ID和标题颜色

### 修改后的效果
- ✅ 广告位模块标题从管理后台读取：使用广告的 `adName` 字段
- ✅ 禁用或删除的广告位自动隐藏：后端API只返回启用状态的广告，前端只显示有广告的楼层
- ✅ 动态楼层生成：根据实际启用的广告数量动态生成楼层，不再固定7个
- ✅ 配置化管理：通过 `floorConfig` 配置广告位置与分类ID的映射关系

### 技术说明
- 后端API `getAllFloorAdvertisements()` 已经实现了状态过滤（只返回 `status=1` 的广告）和时间过滤
- 前端只需要根据返回的广告数据生成楼层，如果某个广告位被禁用或删除，就不会出现在返回列表中
- 广告位置格式：`floor_1`, `floor_2`, ..., `floor_7`
- 楼层编号格式：从 `floor_1` 提取为 `1F`，从 `floor_2` 提取为 `2F`，以此类推

## 2026-01-08 - 修复广告位标题显示和管理后台广告位置选项

### 问题描述
1. 用户端首页广告位标题显示为"2F 2F楼层广告"，前面的"2F"不需要，应该只显示广告名称
2. 管理后台广告位置下拉选项缺少了楼层广告5、楼层广告6、楼层广告7

### 修复内容

#### 1. 修改CategoryFloor组件标题显示
- **文件**：`frontend/src/components/home/CategoryFloor.vue`
- **修改**：移除楼层编号（`floorNumber`），标题栏只显示广告名称（`categoryName`）
- **效果**：标题从"2F 2F楼层广告"改为"2F楼层广告"（只显示管理后台配置的广告名称）

#### 2. 管理后台添加楼层广告5、6、7选项
- **文件**：`admin-frontend/src/views/website/Advertisement.vue`
- **修改位置**：
  1. 搜索表单的广告位置下拉选项（第17-20行）
  2. 编辑表单的广告位置下拉选项（第116-119行）
  3. `getPositionText` 函数的映射（第262-265行）
- **添加内容**：
  - `<el-option label="楼层广告5" value="floor_5" />`
  - `<el-option label="楼层广告6" value="floor_6" />`
  - `<el-option label="楼层广告7" value="floor_7" />`
  - 在 `positionMap` 中添加对应的映射

### 代码修改清单
1. ✅ `frontend/src/components/home/CategoryFloor.vue` - 移除楼层编号显示，只显示广告名称
2. ✅ `admin-frontend/src/views/website/Advertisement.vue` - 添加楼层广告5、6、7选项

### 修改后的效果
- ✅ 广告位标题只显示广告名称：不再显示"2F"前缀，直接显示管理后台配置的广告名称
- ✅ 管理后台可以配置楼层广告5、6、7：下拉选项中包含所有7个楼层广告位置
- ✅ 广告位置文本显示正确：列表和详情中正确显示楼层广告5、6、7的文本

