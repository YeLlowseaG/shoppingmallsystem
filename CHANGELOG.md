# 更新日志 (CHANGELOG)

## 2025-12-06 - 后端服务启动与API测试

### 主要工作内容

#### 1. Java运行环境配置
- 安装Java 17 LTS版本 (OpenJDK Eclipse Temurin 17.0.17)
- 配置JAVA_HOME环境变量
- 更新Maven配置使用Java 17

#### 2. 数据库配置与问题修复
- 修复MySQL数据库密码配置(更新为12345678)
- 移除Druid连接池依赖,使用Spring Boot默认的HikariCP
- 解决Druid与HikariCP配置冲突导致的H2 Driver错误
- 创建数据库`chengren_shopping_mall`
- 执行schema.sql创建表结构
- 执行init_data.sql导入初始数据

#### 3. 认证配置优化
- 修改JWT认证拦截器配置(`WebMvcConfig.java`)
- 允许匿名访问买家端商品相关接口:
  - `/api/buyer/product/**`
  - `/api/buyer/product-category/**`

#### 4. 后端服务启动
- 成功编译项目
- 启动Spring Boot后端服务(端口8080)
- 服务状态:运行正常

#### 5. API接口测试
测试了以下接口,全部正常工作:

- **商品分页查询** `GET /api/buyer/product/page`
  - 返回状态: 200 OK
  - 功能: 分页获取已上架商品列表

- **商品分类树** `GET /api/buyer/product-category/tree`
  - 返回状态: 200 OK
  - 功能: 获取完整的商品分类树结构(包含子分类)
  - 已有分类:
    - 情趣用品(男用器具、女用器具、润滑剂、安全套)
    - 健康护理(护理用品、清洁用品)
    - 情趣内衣(女士内衣、男士内衣)
    - 其他

### 技术栈
- Java 17 LTS
- Spring Boot 3.1.5
- MyBatis Plus 3.5.4.1
- MySQL 8.0.33
- HikariCP连接池
- Maven 3.9.11

### 已修复的问题
1. ✅ H2 Driver ClassNotFoundException - 由Druid依赖冲突引起
2. ✅ 数据库不存在错误 - 创建并初始化数据库
3. ✅ 401未登录错误 - 配置匿名访问商品接口
4. ✅ Bean命名冲突 - 为admin和buyer端Controller添加明确的Bean名称

### 配置文件修改
- `pom.xml`: 移除Druid依赖
- `application.yml`: 更新数据库密码,简化数据源配置
- `WebMvcConfig.java`: 添加商品接口匿名访问权限

### 下一步计划
1. 修改用户端首页对接API获取数据
2. 修改用户端商品列表页对接API
3. 修改用户端商品详情页对接API

---

**注意**: 后端服务目前运行在 http://localhost:8080
