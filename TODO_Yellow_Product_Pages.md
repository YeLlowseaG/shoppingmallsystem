# 商品模块核心页面开发计划

**开发范围**: 首页 + 商品列表页 + 商品详情页
**开发周期**: 3周
**开发分支**: feature/yellow-modules

---

## 🎯 开发目标

完成买家端商品展示的完整链路：**首页浏览 → 商品列表 → 商品详情**

---

## Week 1: 后端接口开发

### 商品相关接口

- [ ] `GET /api/buyer/categories` - 获取三级分类树
- [ ] `GET /api/buyer/products` - 商品列表（分页、筛选、排序、搜索）
- [ ] `GET /api/buyer/products/{id}` - 商品详情
- [ ] `GET /api/buyer/products/new` - 新品推荐（首页用）
- [ ] `GET /api/buyer/products/hot` - 热销商品（首页用）
- [ ] `GET /api/buyer/products/special` - 特价商品（首页用）

### 核心逻辑

- 根据用户等级计算商品价格（未登录显示基础价）
- 商品列表支持分类、价格、关键词筛选
- 商品列表支持排序（价格升/降、销量、新品）

---

## Week 2: 前端页面开发

### 1. 首页 `/`

**顶部导航**
- Logo + 搜索框 + 分类菜单 + 用户信息 + 购物车图标

**轮播图**
- 首页 Banner

**商品推荐区**
- 新品推荐（8-10个商品卡片）
- 热销商品（8-10个商品卡片）
- 特价商品（8-10个商品卡片）

**商品卡片组件（可复用）**
- 图片 + 名称 + 价格 + 库存 + 加购按钮

---

### 2. 商品列表页 `/products`

**左侧分类栏**
- 三级分类树

**顶部筛选区**
- 面包屑导航
- 价格区间筛选
- 排序选择（价格、销量、新品）

**商品展示区**
- 商品卡片网格（每行4个）
- 加载状态（Skeleton）
- 空状态提示

**分页器**
- 显示总数、页码、跳转

---

### 3. 商品详情页 `/products/:id`

**左侧图片区**
- 主图展示 + 缩略图列表

**右侧信息区**
- 商品名称 + 编号
- 价格展示（基础价 + 会员价）
- 库存显示
- 数量选择器
- 操作按钮（加购 + 立即购买 + 收藏）

**底部详情**
- Tabs（商品详情、商品参数）
- 相关商品推荐

---

## Week 3: 联调优化

### 前后端联调
- 测试所有页面数据加载
- 处理边界情况（缺货、下架、404）

### 用户体验优化
- 图片懒加载
- 搜索防抖
- Skeleton加载状态
- Toast提示优化
- 空状态设计

### 提交代码
- 提交到 feature/yellow-modules
- 合并到 dev
- 推送到 Gitee
- 通知 jie 审查

---

## 📁 文件结构

### 后端
```
backend/src/main/java/com/shoppingmall/
├── controller/buyer/ProductController.java
├── service/product/ProductService.java
├── service/product/impl/ProductServiceImpl.java
├── repository/product/ProductRepository.java
├── repository/product/CategoryRepository.java
├── entity/Product.java
├── entity/ProductCategory.java
├── dto/ProductQueryDTO.java
├── vo/ProductListVO.java
└── vo/ProductDetailVO.java
```

### 前端
```
frontend/src/
├── views/
│   ├── home/Index.vue              # 首页
│   ├── products/List.vue            # 商品列表
│   └── products/Detail.vue          # 商品详情
├── components/
│   ├── ProductCard.vue              # 商品卡片
│   ├── CategoryTree.vue             # 分类树
│   └── Header.vue                   # 顶部导航
└── api/buyer/product.ts             # 商品API
```

---

## 🤝 与 jie 的协作

- 依赖用户登录状态（获取用户等级显示对应价格）
- 商品接口后续会被订单模块调用
