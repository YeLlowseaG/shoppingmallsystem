# 前端项目说明

## 项目结构

本项目包含两个独立的前端项目：

### 1. 采购者端（frontend）

- **项目名称**：`shopping-mall-buyer-frontend`
- **项目目录**：`frontend/`
- **开发端口**：3000
- **访问路径**：`/`（根路径）
- **用途**：采购者（Buyer）使用的前台系统
- **主要功能**：
  - 用户注册、登录
  - 商品浏览、搜索
  - 购物车管理
  - 订单管理
  - 会员中心

### 2. 管理后台（admin-frontend）

- **项目名称**：`shopping-mall-admin-frontend`
- **项目目录**：`admin-frontend/`
- **开发端口**：3001
- **访问路径**：`/admin`
- **用途**：平台管理员（Admin）使用的后台管理系统
- **主要功能**：
  - 管理员登录
  - 仪表盘
  - 商品管理
  - 订单管理
  - 用户管理
  - 数据统计

## 开发指南

### 采购者端开发

```bash
# 进入采购者端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器（端口3000）
npm run dev

# 构建生产版本
npm run build
```

### 管理后台开发

```bash
# 进入管理后台目录
cd admin-frontend

# 安装依赖
npm install

# 启动开发服务器（端口3001）
npm run dev

# 构建生产版本
npm run build
```

## 部署说明

### 构建两个项目

```bash
# 构建采购者端
cd frontend
npm run build
# 输出目录：frontend/dist/

# 构建管理后台
cd admin-frontend
npm run build
# 输出目录：admin-frontend/dist/
```

### Nginx部署

1. **采购者端**：
   - 将 `frontend/dist/` 目录内容复制到 `/usr/share/nginx/html/buyer/`
   - 访问地址：`http://localhost/` 或 `http://buyer.example.com`

2. **管理后台**：
   - 将 `admin-frontend/dist/` 目录内容复制到 `/usr/share/nginx/html/admin/`
   - 访问地址：`http://localhost/admin` 或 `http://admin.example.com`

### Nginx配置

参考 `config/nginx/nginx.conf` 配置文件，已配置好两个前端项目的路由。

## 项目特点

### 独立性

- ✅ 两个项目完全独立
- ✅ 各自有独立的 `package.json` 和依赖管理
- ✅ 各自有独立的构建配置
- ✅ 各自有独立的开发端口
- ✅ 各自有独立的路由和状态管理

### Token管理

- **采购者端**：使用 `token`（存储在 localStorage）
- **管理后台**：使用 `admin_token`（存储在 localStorage）
- 避免两个端的Token冲突

### API接口

- **采购者端**：调用 `/api/buyer/*` 接口
- **管理后台**：调用 `/api/admin/*` 接口

## 注意事项

1. **开发时**：两个项目可以同时运行，使用不同端口（3000 和 3001）
2. **构建时**：需要分别构建两个项目
3. **部署时**：需要分别部署两个项目的构建产物
4. **Token管理**：两个端使用不同的Token存储键，避免冲突

## 后续开发

### 采购者端待开发功能

- [ ] 商品列表、详情页
- [ ] 购物车功能
- [ ] 订单管理
- [ ] 会员中心
- [ ] 收货地址管理

### 管理后台待开发功能

- [ ] 管理员登录API对接
- [ ] 商品管理模块
- [ ] 订单管理模块
- [ ] 用户管理模块
- [ ] 数据统计模块


