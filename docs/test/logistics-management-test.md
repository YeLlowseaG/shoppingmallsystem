# 物流管理模块测试说明

## 一、数据库初始化

### 1. 执行数据库脚本

按以下顺序执行SQL脚本：

1. **物流管理表结构**（如果数据库已存在，执行此脚本）：
   ```sql
   -- 执行 update-20251209-add-logistics-tables.sql
   -- 创建物流公司表、配送方式表、运费模板表、运费规则表
   ```

2. **物流管理菜单**（添加菜单和权限）：
   ```sql
   -- 执行 update-20251209-add-logistics-menu.sql
   -- 添加物流管理菜单，为管理员角色分配权限
   ```

### 2. 验证数据库

执行以下SQL验证表是否创建成功：

```sql
-- 检查表是否存在
SHOW TABLES LIKE 'logistics_company';
SHOW TABLES LIKE 'shipping_method';
SHOW TABLES LIKE 'shipping_template';
SHOW TABLES LIKE 'shipping_rule';

-- 检查菜单是否添加
SELECT * FROM sys_menu WHERE id = 35;

-- 检查权限是否分配
SELECT * FROM sys_role_menu WHERE menu_id = 35;
```

## 二、后端接口测试

### 1. 启动后端服务

确保后端服务正常运行（默认端口：8081）

### 2. 测试接口

#### 2.1 物流公司管理接口

**获取物流公司列表（分页）**
```http
GET /api/admin/logistics/company/list?page=1&pageSize=10
Authorization: Bearer {token}
```

**获取所有启用的物流公司**
```http
GET /api/admin/logistics/company/all
Authorization: Bearer {token}
```

**获取物流公司详情**
```http
GET /api/admin/logistics/company/{id}
Authorization: Bearer {token}
```

**新增物流公司**
```http
POST /api/admin/logistics/company
Authorization: Bearer {token}
Content-Type: application/json

{
  "companyCode": "test",
  "companyName": "测试物流公司",
  "companyShortName": "测试",
  "contactPhone": "13800138000",
  "website": "http://www.test.com",
  "sortOrder": 0,
  "status": 1
}
```

**更新物流公司**
```http
PUT /api/admin/logistics/company/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "companyCode": "test",
  "companyName": "测试物流公司（更新）",
  "status": 1
}
```

**删除物流公司**
```http
DELETE /api/admin/logistics/company/{id}
Authorization: Bearer {token}
```

**启用/禁用物流公司**
```http
PUT /api/admin/logistics/company/{id}/status?status=0
Authorization: Bearer {token}
```

#### 2.2 配送方式管理接口

**获取配送方式列表（分页）**
```http
GET /api/admin/shipping/method/list?page=1&pageSize=10
Authorization: Bearer {token}
```

**获取所有启用的配送方式（买家端）**
```http
GET /api/buyer/shipping/methods
Authorization: Bearer {token}
```

**计算运费**
```http
POST /api/buyer/shipping/calculate
Authorization: Bearer {token}
Content-Type: application/json

{
  "shippingMethodId": 1,
  "totalWeight": 5.5,
  "totalQuantity": 3,
  "totalAmount": 100.00,
  "province": "广东省",
  "city": "深圳市",
  "district": "南山区"
}
```

#### 2.3 运费模板管理接口

**获取运费模板列表（分页）**
```http
GET /api/admin/shipping/template/list?page=1&pageSize=10
Authorization: Bearer {token}
```

**获取运费模板详情（包含规则）**
```http
GET /api/admin/shipping/template/{id}
Authorization: Bearer {token}
```

**新增运费模板**
```http
POST /api/admin/shipping/template
Authorization: Bearer {token}
Content-Type: application/json

{
  "templateName": "测试运费模板",
  "calculationType": 1,
  "defaultFirstWeight": 1.0,
  "defaultFirstPrice": 10.00,
  "defaultContinueWeight": 1.0,
  "defaultContinuePrice": 5.00,
  "freeShippingAmount": 200.00,
  "status": 1,
  "rules": []
}
```

## 三、前端管理后台测试

### 1. 启动前端服务

```bash
cd admin-frontend
npm install
npm run dev
```

访问：http://localhost:3001

### 2. 登录管理后台

使用管理员账号登录：
- 用户名：admin
- 密码：123456（或实际密码）

### 3. 测试物流管理页面

#### 3.1 访问物流管理页面

1. 登录后，在左侧菜单中找到"系统设置"
2. 展开"系统设置"，点击"物流管理"
3. 应该能看到物流管理页面，包含三个标签页：
   - 物流公司
   - 配送方式
   - 运费模板

#### 3.2 测试物流公司管理

**新增物流公司**
1. 点击"新增物流公司"按钮
2. 填写表单：
   - 公司编码：test_company
   - 公司名称：测试物流公司
   - 公司简称：测试
   - 联系电话：13800138000
   - 官网地址：http://www.test.com
   - 排序：0
   - 状态：启用
3. 点击"确定"保存
4. 验证：列表中应该显示新添加的物流公司

**编辑物流公司**
1. 在列表中找到要编辑的物流公司
2. 点击"编辑"按钮
3. 修改信息后保存
4. 验证：列表中显示更新后的信息

**启用/禁用物流公司**
1. 点击"启用"或"禁用"按钮
2. 验证：状态标签应该更新

**删除物流公司**
1. 点击"删除"按钮
2. 确认删除
3. 验证：列表中不再显示该物流公司

**搜索和分页**
1. 在搜索框输入关键词，点击"查询"
2. 验证：列表应该根据关键词过滤
3. 修改每页显示数量，验证分页功能

#### 3.3 测试配送方式管理

**新增配送方式**
1. 切换到"配送方式"标签页
2. 点击"新增配送方式"按钮
3. 填写表单：
   - 配送方式编码：test_method
   - 配送方式名称：测试配送
   - 物流公司：选择已创建的物流公司
   - 基础运费：10.00
   - 计算方式：固定运费
   - 状态：启用
4. 点击"确定"保存
5. 验证：列表中应该显示新添加的配送方式

**编辑配送方式**
1. 点击"编辑"按钮
2. 修改信息后保存
3. 验证：列表中显示更新后的信息

**测试不同计算方式**
- 固定运费：设置基础运费即可
- 按重量：需要设置基础运费（作为首重价格）
- 按件数：需要设置基础运费（作为首件价格）
- 按金额：需要设置基础运费
- 运费模板：需要选择已创建的运费模板

#### 3.4 测试运费模板管理

**新增运费模板**
1. 切换到"运费模板"标签页
2. 点击"新增运费模板"按钮
3. 填写表单：
   - 模板名称：测试运费模板
   - 计算方式：按重量
   - 默认首重：1.0 kg
   - 默认首重价格：10.00
   - 默认续重：1.0 kg
   - 默认续重价格：5.00
   - 包邮金额：200.00
   - 状态：启用
4. 点击"确定"保存
5. 验证：列表中应该显示新添加的运费模板

**查看运费规则**
1. 点击"查看规则"按钮
2. 验证：应该显示运费规则对话框（如果有规则）

**编辑运费模板**
1. 点击"编辑"按钮
2. 修改信息后保存
3. 验证：列表中显示更新后的信息

## 四、买家端测试

### 1. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

访问：http://localhost:3000

### 2. 测试配送方式获取

**获取配送方式列表**
1. 登录买家端
2. 进入购物车，点击"去结算"
3. 在结算页面，应该能看到配送方式选择
4. 验证：配送方式列表应该显示所有启用的配送方式

**测试运费计算**
1. 在结算页面选择配送方式
2. 填写收货地址
3. 验证：运费应该根据配送方式和订单信息自动计算

## 五、常见问题排查

### 1. 菜单不显示

**问题**：登录后看不到"物流管理"菜单

**排查步骤**：
1. 检查菜单是否已添加：
   ```sql
   SELECT * FROM sys_menu WHERE id = 35;
   ```
2. 检查角色权限是否分配：
   ```sql
   SELECT * FROM sys_role_menu WHERE menu_id = 35;
   ```
3. 检查管理员角色ID：
   ```sql
   SELECT * FROM sys_role WHERE role_code = 'ADMIN';
   ```
4. 重新登录，清除浏览器缓存

### 2. API接口404错误

**问题**：前端调用API返回404

**排查步骤**：
1. 检查后端服务是否启动
2. 检查API路径是否正确
3. 检查Controller的@RequestMapping路径
4. 检查前端API调用的路径

### 3. 数据加载失败

**问题**：页面显示"加载失败"

**排查步骤**：
1. 打开浏览器开发者工具，查看Network请求
2. 检查请求的响应状态码
3. 查看后端日志，检查是否有错误
4. 检查数据库表是否已创建
5. 检查数据库连接配置

### 4. 权限不足

**问题**：提示"无权限访问该页面"

**排查步骤**：
1. 检查管理员账号是否分配了正确的角色
2. 检查角色是否分配了物流管理菜单权限
3. 重新登录，刷新权限信息

## 六、测试检查清单

- [ ] 数据库表创建成功
- [ ] 菜单添加成功
- [ ] 权限分配成功
- [ ] 后端接口正常响应
- [ ] 物流公司管理功能正常
- [ ] 配送方式管理功能正常
- [ ] 运费模板管理功能正常
- [ ] 搜索和分页功能正常
- [ ] 新增、编辑、删除功能正常
- [ ] 启用/禁用功能正常
- [ ] 买家端可以获取配送方式列表
- [ ] 运费计算功能正常

## 七、后续优化建议

1. **运费规则管理**：可以在运费模板编辑页面添加运费规则的增删改功能
2. **地区选择**：运费规则中的地区选择可以使用级联选择器
3. **批量操作**：支持批量启用/禁用、批量删除
4. **导入导出**：支持物流公司、配送方式的批量导入导出
5. **运费计算优化**：优化运费计算逻辑，支持更复杂的计算规则
6. **物流跟踪**：集成第三方物流API，实现物流跟踪功能

