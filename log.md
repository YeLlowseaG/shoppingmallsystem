## 2025-12-12 - 添加预警阈值同步逻辑，以库存表为权威数据源

### 修改内容
添加同步逻辑，确保 `product.warning_stock` 与 `product_stock.warning_threshold` 保持一致，以 `product_stock.warning_threshold` 为权威数据源。

### 修改文件

#### 后端
1. backend/src/main/java/com/shoppingmall/service/admin/impl/StockServiceImpl.java - 添加预警阈值同步方法
2. backend/src/main/java/com/shoppingmall/service/product/impl/ProductServiceImpl.java - 修改创建库存记录逻辑，添加预警阈值同步

### 具体修改

#### 1. 添加预警阈值同步方法
- 在`StockServiceImpl`中添加`syncProductWarningStock`方法
- 以`product_stock.warning_threshold`为权威数据源，同步更新`product.warning_stock`
- 与现有的`syncProductStock`方法保持一致的设计模式

#### 2. 在更新预警阈值时同步
- 在`StockServiceImpl.updateWarningThreshold`方法中
- 更新`product_stock.warning_threshold`后，调用`syncProductWarningStock`同步到`product.warning_stock`

#### 3. 在调整库存时同步预警阈值
- 在`StockServiceImpl.adjustStock`方法中
- 如果`StockDTO`中提供了`warningThreshold`，更新后同步到`product.warning_stock`

#### 4. 创建库存记录时的同步逻辑
- 在`ProductServiceImpl.createOrUpdateProductStock`方法中
- 创建新库存记录时：
  - 如果`product.warning_stock`存在，使用它作为`product_stock.warning_threshold`的初始值
  - 如果不存在，使用默认值10
  - 创建后，以`product_stock.warning_threshold`为准，同步回`product.warning_stock`
- 更新现有库存记录时：
  - 更新后，以`product_stock.warning_threshold`为准，同步回`product.warning_stock`

### 技术细节
- 同步逻辑采用"以库存表为准"的原则
- 所有更新`product_stock.warning_threshold`的地方都会同步更新`product.warning_stock`
- 同步失败不会影响主业务流程（使用try-catch捕获异常）
- 保留`product.warning_stock`字段，但实际数据以`product_stock.warning_threshold`为准

### 影响
- ✅ `product.warning_stock`和`product_stock.warning_threshold`保持一致
- ✅ 以`product_stock.warning_threshold`为权威数据源
- ✅ 所有更新预警阈值的操作都会自动同步
- ✅ 创建库存记录时会正确初始化预警阈值并同步

---

## 2025-12-12 - 修改商品列表和库存列表排序为按创建时间倒序

### 修改内容
将商品列表和库存列表的排序方式改为按商品创建时间倒序排列。

### 修改文件

#### 后端
1. backend/src/main/java/com/shoppingmall/service/product/impl/ProductServiceImpl.java - 修改商品列表排序
2. backend/src/main/java/com/shoppingmall/service/admin/impl/StockServiceImpl.java - 修改库存列表排序

### 具体修改

#### 1. 商品列表排序修改
- 在`ProductServiceImpl.getProductPage`方法中
- 将排序从`orderByDesc(Product::getSalesCount)`（按销量降序）改为`orderByDesc(Product::getCreateTime)`（按创建时间倒序）
- 影响页面：`http://localhost:3003/admin/product/list`

#### 2. 库存列表排序修改
- 在`StockServiceImpl.getStockPage`方法中
- 将排序从`orderByDesc(Product::getUpdateTime)`（按更新时间倒序）改为`orderByDesc(Product::getCreateTime)`（按创建时间倒序）
- 影响页面：`http://localhost:3003/admin/stock/list`

### 技术细节
- 使用MyBatis-Plus的`orderByDesc`方法进行倒序排序
- 排序字段：`Product::getCreateTime`（商品创建时间）
- 最新创建的商品会显示在列表最前面

### 影响
- ✅ 商品列表按创建时间倒序排列，最新创建的商品显示在最前面
- ✅ 库存列表按创建时间倒序排列，最新创建的商品显示在最前面
- ✅ 两个列表的排序方式保持一致

---

## 2025-12-12 - 修复库存列表功能并增加商品状态搜索和库存预警功能

### 修改内容
1. 修复库存列表页面商品图片显示问题
2. 在搜索功能中增加商品状态字段查询
3. 增加库存预警相关功能（预警筛选、预警状态显示、预警阈值设置）

### 修改文件

#### 后端
1. backend/src/main/java/com/shoppingmall/dto/StockQueryDTO.java - 添加productStatus字段
2. backend/src/main/java/com/shoppingmall/controller/admin/StockController.java - 添加productStatus参数
3. backend/src/main/java/com/shoppingmall/service/admin/impl/StockServiceImpl.java - 添加商品状态筛选逻辑

#### 前端
1. admin-frontend/src/api/admin/stock.ts - 添加productStatus参数
2. admin-frontend/src/views/stock/List.vue - 修复图片显示、添加状态搜索、添加预警功能

### 具体修改

#### 1. 修复商品图片显示问题
- 添加`getImageUrl`函数，处理图片URL的相对路径和绝对路径
- 如果URL是相对路径（以`/`开头），添加基础URL前缀
- 如果URL已经是完整URL（以`http://`或`https://`开头），直接使用
- 为`el-image`组件添加错误处理，显示占位图标
- 添加图片预览功能，点击图片可以放大查看

#### 2. 增加商品状态搜索功能
- 在`StockQueryDTO`中添加`productStatus`字段（Integer类型，0-下架，1-上架）
- 在`StockController`的`getStockPage`方法中添加`productStatus`参数
- 在`StockServiceImpl`的`getStockPage`方法中添加商品状态筛选逻辑
- 在前端搜索表单中添加商品状态下拉选择框
- 支持筛选上架、下架或全部商品

#### 3. 增加库存预警功能
- 在搜索表单中添加"预警筛选"下拉框，支持筛选仅预警商品或全部商品
- 在库存列表表格中添加"预警阈值"列，显示每个商品的预警阈值
- 在库存列表表格中添加"预警状态"列，使用标签显示预警/正常状态
- 在"可用库存"列中，如果商品处于预警状态，使用红色高亮显示
- 在操作列中添加"设置预警"按钮，可以快速设置商品的预警阈值
- 添加`handleSetThreshold`函数，通过弹窗输入框设置预警阈值
- 调用`updateWarningThreshold` API更新预警阈值

### 技术细节
- 图片URL处理：使用环境变量`VITE_API_BASE_URL`作为基础URL
- 商品状态：0表示下架，1表示上架
- 预警判断：当可用库存 <= 预警阈值时，商品处于预警状态
- 预警阈值设置：使用`ElMessageBox.prompt`弹窗输入，验证输入值必须大于等于0

### 影响
- ✅ 库存列表页面商品图片能够正确显示
- ✅ 支持按商品状态筛选库存列表
- ✅ 支持按预警状态筛选库存列表
- ✅ 库存列表显示预警阈值和预警状态
- ✅ 可以快速设置商品的预警阈值
- ✅ 预警商品在列表中高亮显示，便于识别

---

## 2025-12-12 - 修复购物车结算页面地址编辑和新增逻辑

### 修改内容
修复购物车结算页面地址编辑和新增的逻辑：
1. 编辑操作时默认勾选"保存本次收货地址"
2. 编辑操作保存后更新原地址，不新增记录
3. 只有选择"其他收货地址"保存才是新增操作

### 修改文件

#### 前端
1. frontend/src/views/cart/Checkout.vue - 修复地址编辑和新增逻辑

### 具体修改

#### 1. 添加编辑地址ID跟踪
- 添加`editingAddressId`变量，用于保存正在编辑的地址ID
- 区分编辑和新增操作

#### 2. 修复handleEditAddress函数
- 保存正在编辑的地址ID到`editingAddressId`
- 设置`saveAddress: true`，编辑时默认勾选"保存本次收货地址"

#### 3. 修复handlePlaceOrder函数
- 导入`updateAddress`函数
- 在保存地址时，判断是编辑还是新增：
  - 如果`editingAddressId`不为null，调用`updateAddress`更新地址
  - 如果`editingAddressId`为null，调用`addAddress`新增地址
- 编辑时使用原地址ID，新增时使用新创建的地址ID

#### 4. 优化watch监听逻辑
- 选择"其他收货地址"时，清空`editingAddressId`，确保是新增操作

### 技术细节
- 编辑操作：`editingAddressId`有值，保存时更新地址
- 新增操作：`editingAddressId`为null，保存时新增地址
- 通过`editingAddressId`区分编辑和新增，逻辑更清晰

### 影响
- ✅ 编辑操作时默认勾选"保存本次收货地址"
- ✅ 编辑操作保存后更新原地址，不新增记录
- ✅ 只有选择"其他收货地址"保存才是新增操作
- ✅ 提升用户体验，编辑和新增逻辑更清晰

---

## 2025-12-12 - 修复购物车结算页面编辑地址的bug

### 修改内容
修复购物车结算页面点击已有地址的"编辑"按钮时，不应该定位到"其他收货地址"选项，也不应该清空已有地址的输入信息。

### 修改文件

#### 前端
1. frontend/src/views/cart/Checkout.vue - 修复编辑地址逻辑

### 具体修改

#### 1. 修复handleEditAddress函数
- 移除`selectedAddressId.value = 'other'`的设置
- 编辑地址时，保持当前选中的地址ID不变
- 只设置`showAddressForm.value = true`来显示编辑表单
- 填充表单数据，不触发watch清空逻辑

#### 2. 优化watch监听逻辑
- 添加`oldVal`参数，判断是否从非'other'变为'other'
- 只有当从非'other'变为'other'时才清空表单
- 避免编辑地址时（selectedAddressId不变）触发清空

### 技术细节
- 编辑地址时：不改变selectedAddressId，只显示表单并填充数据
- 选择"其他收货地址"时：selectedAddressId变为'other'，触发watch清空表单
- 通过oldVal判断，确保只有真正的选择操作才清空表单

### 影响
- ✅ 编辑地址时不会定位到"其他收货地址"选项
- ✅ 编辑地址时不会清空已有地址的输入信息
- ✅ 只有选择"其他收货地址"时才会清空表单
- ✅ 提升用户体验，编辑和新增逻辑更清晰

---

## 2025-12-12 - 修复购物车结算页面选择其他收货地址时清空表单

### 修改内容
修复购物车结算页面选择"其他收货地址"时，自动清空所有收货人信息输入框，相当于重置为新增状态。

### 修改文件

#### 前端
1. frontend/src/views/cart/Checkout.vue - 添加watch监听，选择"其他收货地址"时清空表单

### 具体修改

#### 1. 添加watch监听逻辑
- 导入`watch`函数
- 监听`selectedAddressId`的变化
- 当选择"other"（其他收货地址）时，清空所有表单字段：
  - 省市区字段
  - 详细地址
  - 邮编
  - 收货人姓名
  - 联系电话
  - 联系手机
  - 保存地址选项
- 同时清空地区选择器数据（regionData）
- 显示地址表单（showAddressForm = true）

### 技术细节
- 使用Vue的watch API监听selectedAddressId的变化
- 当值变为'other'时，重置addressForm为初始状态
- 清空regionData，确保地区选择器也重置
- 自动显示地址表单，方便用户填写

### 影响
- ✅ 选择"其他收货地址"时自动清空所有输入框
- ✅ 提供清晰的表单重置体验
- ✅ 避免用户需要手动清空已填写的旧数据
- ✅ 提升用户体验，表单状态更清晰

---

## 2025-12-12 - 修复购物车结算页面地址编辑地区显示问题

### 修改内容
修复购物车结算页面编辑收货地址时，地区数据不显示的问题。

### 修改文件

#### 前端
1. frontend/src/views/cart/Checkout.vue - 添加地区ID查找功能

### 具体修改

#### 1. 添加地区ID查找功能
- 添加`loadRegionIdsByName`函数，根据省市区名称查找对应的ID
- 在`handleEditAddress`中调用该函数，将查找到的ID设置到`regionData`中
- 通过三级查找：省份 -> 城市 -> 区县，确保找到正确的ID
- 如果查找失败，不影响表单数据，用户仍可以重新选择

### 技术细节
- 根据名称查找ID：通过遍历所有省份、城市、区县来匹配名称
- 异步加载：地区数据加载是异步的，需要等待数据加载完成
- 与AddressEdit.vue使用相同的逻辑，保持一致性

### 影响
- ✅ 购物车结算页面编辑地址时能够正确显示已保存的地区数据
- ✅ 提升用户体验，编辑地址时无需重新选择地区
- ✅ 与收货地址编辑页面保持一致的行为

---

## 2025-12-12 - 修复收货地址编辑和列表显示问题

### 修改内容
修复收货地址编辑页面地区数据不显示的问题，以及地址列表需要拼接省市区显示的问题。

### 修改文件

#### 前端
1. frontend/src/views/member/AddressEdit.vue - 修复编辑页面地区数据加载
2. frontend/src/views/member/Address.vue - 修复地址列表显示，拼接省市区
3. frontend/src/components/common/RegionSelector.vue - 优化watch逻辑，支持动态加载子级数据

### 具体修改

#### 1. 修复编辑页面地区数据不显示
- 添加`loadRegionIdsByName`函数，根据省市区名称查找对应的ID
- 在`loadAddressData`中调用该函数，将查找到的ID设置到`regionData`中
- 通过三级查找：省份 -> 城市 -> 区县，确保找到正确的ID
- 如果查找失败，不影响表单数据，用户仍可以重新选择

#### 2. 修复地址列表显示
- 添加`formatFullAddress`函数，将省市区和详细地址拼接成完整地址
- 格式：`省份 城市 区县 详细地址`
- 在地址列表的地址列中使用该函数格式化显示

#### 3. 优化RegionSelector组件
- 优化watch逻辑，当外部值变化时，自动加载对应的子级数据
- 如果省份ID变化，重新加载城市列表
- 如果城市ID变化，重新加载区县列表
- 确保组件能够正确响应外部数据变化

### 技术细节
- 根据名称查找ID：通过遍历所有省份、城市、区县来匹配名称
- 地址拼接：使用空格连接省市区和详细地址
- 异步加载：地区数据加载是异步的，需要等待数据加载完成

### 影响
- ✅ 编辑页面能够正确显示已保存的地区数据
- ✅ 地址列表显示完整的省市区地址信息
- ✅ RegionSelector组件能够正确响应外部数据变化
- ✅ 提升用户体验，编辑地址时无需重新选择地区

---

## 2025-12-12 - 对接3个页面的地区选择功能到后端

### 修改内容
将购物车结算页面、用户注册页面和收货地址编辑页面的地区选择功能对接后端region表数据，使用统一的RegionSelector组件。

### 修改文件

#### 前端
1. frontend/src/components/common/RegionSelector.vue - 扩展change事件，同时返回名称
2. frontend/src/views/cart/Checkout.vue - 替换el-cascader为RegionSelector组件
3. frontend/src/views/auth/Register.vue - 替换硬编码地区数据为RegionSelector组件
4. frontend/src/views/member/AddressEdit.vue - 替换硬编码地区数据为RegionSelector组件

### 具体修改

#### 1. RegionSelector组件增强
- change事件同时返回ID、编码和名称
- 新增字段：provinceName、cityName、districtName
- 方便页面直接使用名称提交给后端API

#### 2. Checkout.vue（购物车结算页面）
- 引入RegionSelector组件
- 替换el-cascader组件
- 更新表单数据结构：region数组改为province、city、district三个独立字段
- 添加handleRegionChange处理函数，将选中的ID转换为名称
- 更新地址验证逻辑
- 更新地址提交逻辑，使用名称而非ID

#### 3. Register.vue（用户注册页面）
- 引入RegionSelector组件
- 删除硬编码的地区数据（provinces、cities、districts、regionData）
- 删除handleProvinceChange和handleCityChange函数
- 添加regionData和handleRegionChange
- registerForm已有province、city、district字段，直接使用

#### 4. AddressEdit.vue（收货地址编辑页面）
- 引入RegionSelector组件
- 删除硬编码的地区数据
- 删除handleProvinceChange和handleCityChange函数
- 添加regionData和handleRegionChange
- 编辑时加载地址数据，regionData暂时清空（后续可扩展根据名称查询ID）

### 技术细节
- 后端API期望接收省市区名称（字符串），而非ID
- RegionSelector组件返回ID和名称，页面使用名称提交
- 编辑地址时，由于只有名称没有ID，暂时需要用户重新选择（后续可扩展API支持根据名称查询ID）
- 所有页面统一使用RegionSelector组件，代码更简洁，维护更方便

### 影响
- ✅ 3个页面的地区选择功能已对接后端region表数据
- ✅ 使用统一的RegionSelector组件，代码复用性更好
- ✅ 删除硬编码的地区数据，数据更完整准确
- ✅ 支持完整的省市区三级数据选择
- ✅ 前端localStorage缓存，减少API调用

---

## 2025-12-12 - 修复编译错误：javax.annotation包不存在

### 修改内容
修复Spring Boot 3.x中javax.annotation包不存在的问题，将PostConstruct注解的导入从javax.annotation改为jakarta.annotation。

### 修改文件

#### 后端
1. backend/src/main/java/com/shoppingmall/service/common/impl/RegionServiceImpl.java - 修复PostConstruct导入

### 具体修改

#### 1. 修复导入语句
- 将`import javax.annotation.PostConstruct;`改为`import jakarta.annotation.PostConstruct;`
- Spring Boot 3.x使用Jakarta EE规范，javax包已被jakarta包替代

### 技术细节
- Spring Boot 3.x基于Jakarta EE 9+，所有javax.*包都已迁移到jakarta.*
- PostConstruct注解用于标记在依赖注入完成后执行的方法
- 在RegionServiceImpl中用于应用启动时预加载地区数据

### 影响
- ✅ 修复编译错误，服务可以正常启动
- ✅ 符合Spring Boot 3.x的Jakarta EE规范
- ✅ 地区数据预加载功能正常工作

---

## 2025-12-12 - 修复首页公开接口401错误

### 修改内容
修复未登录用户访问首页时，系统配置和导航菜单接口返回401错误的问题，将这两个公开接口添加到拦截器排除列表中。

### 修改文件

#### 后端
1. backend/src/main/java/com/shoppingmall/common/config/WebMvcConfig.java - 添加公开接口到排除列表

### 具体修改

#### 1. 添加公开接口到拦截器排除列表
- 在JWT拦截器的`excludePathPatterns`中添加`/api/buyer/system/config/public`，允许游客访问系统公开配置接口
- 在JWT拦截器的`excludePathPatterns`中添加`/api/buyer/navigation/**`，允许游客访问导航菜单模块的所有接口

### 技术细节
- 这两个接口是首页必需的公开数据，不需要登录即可访问
- `/api/buyer/system/config/public` - 获取系统公开配置（如网站名称、客服电话等）
- `/api/buyer/navigation/menus` - 获取导航菜单列表
- 通过添加到排除列表，这些接口不再被JWT拦截器拦截

### 影响
- ✅ 未登录用户访问首页时，系统配置和导航菜单接口正常返回数据
- ✅ 首页可以正常加载，不再出现401错误
- ✅ 提升游客体验，首页功能完整可用

---

## 2025-12-12 - 实现多级地区数据管理功能

### 修改内容
实现完整的省市区三级地区数据管理功能，支持用户注册和收货地址的多级地区选择。使用数据库存储地区数据，采用Caffeine本地缓存优化性能，前端使用localStorage缓存减少API调用。

### 修改文件

#### 数据库
1. database/update-20251212-create-region-table.sql - 创建地区表结构
2. database/import_regions.py - 地区数据导入脚本（JSON转SQL）

#### 后端
1. backend/src/main/java/com/shoppingmall/common/config/CacheConfig.java - 添加地区缓存配置
2. backend/src/main/java/com/shoppingmall/entity/Region.java - 地区实体类
3. backend/src/main/java/com/shoppingmall/repository/common/RegionRepository.java - 地区数据访问层
4. backend/src/main/java/com/shoppingmall/vo/RegionVO.java - 地区VO类
5. backend/src/main/java/com/shoppingmall/service/common/RegionService.java - 地区服务接口
6. backend/src/main/java/com/shoppingmall/service/common/impl/RegionServiceImpl.java - 地区服务实现类
7. backend/src/main/java/com/shoppingmall/controller/common/RegionController.java - 地区控制器

#### 前端
1. frontend/src/api/common/region.ts - 地区API服务（包含localStorage缓存）
2. frontend/src/components/common/RegionSelector.vue - 统一地区选择组件

### 具体修改

#### 1. 数据库设计
- 创建`region`表，包含字段：id、code、name、parent_id、level、sort_order、status等
- 建立索引：code唯一索引、parent_id索引、level索引、复合索引(parent_id, level)
- 支持三级结构：省/直辖市(level=1)、市(level=2)、区/县(level=3)

#### 2. 后端实现
- **缓存配置**：添加`regionCache`和`regionTreeCache`两个Caffeine缓存Bean
  - regionCache：最大50000条，24小时过期，用于缓存所有地区数据
  - regionTreeCache：最大10条，24小时过期，用于缓存树形结构
- **实体类**：Region实体，支持树形结构（children字段）
- **Repository层**：
  - selectByLevel：根据级别查询
  - selectByParentId：根据父级ID查询子级
  - selectByCode：根据编码查询
  - selectAllEnabled：查询所有启用的地区（用于预加载）
- **Service层**：
  - getProvinces：获取所有省份
  - getChildrenByParentId：根据父级ID获取子级
  - getByCode：根据编码获取地区
  - getFullPathByCode：根据编码获取完整路径（省-市-区）
  - preloadRegions：应用启动时预加载所有地区数据到缓存
  - clearCache：清除缓存
- **Controller层**：提供RESTful API接口
  - GET /api/regions/provinces - 获取所有省份
  - GET /api/regions/children/{parentId} - 获取子级地区
  - GET /api/regions/code/{code} - 根据编码获取地区
  - GET /api/regions/path/{code} - 根据编码获取完整路径

#### 3. 前端实现
- **API服务**（region.ts）：
  - 使用localStorage缓存，24小时过期
  - getProvinces：获取省份列表（带缓存）
  - getChildrenByParentId：获取子级地区（带缓存）
  - getRegionByCode：根据编码获取地区
  - getFullPathByCode：根据编码获取完整路径
  - clearRegionCache：清除所有地区缓存
- **统一组件**（RegionSelector.vue）：
  - 三级联动选择器（省-市-区）
  - 支持v-model双向绑定
  - 懒加载：按需加载城市和区县数据
  - 支持初始值设置
  - 发出change事件，包含ID和编码信息

#### 4. 数据导入脚本
- 创建Python脚本`import_regions.py`
- 将`docs/regions.json`转换为SQL INSERT语句
- 自动处理三级结构关系，生成parent_id
- 生成SQL文件：`update-20251212-import-regions-data.sql`

### 性能优化

#### 后端优化
1. **Caffeine本地缓存**：
   - 应用启动时预加载所有地区数据
   - 缓存命中率高，减少数据库查询
   - 24小时过期时间，适合地区数据很少变化的场景
2. **数据库优化**：
   - 建立复合索引(parent_id, level)，优化按父级和级别查询
   - 单列索引：code、parent_id、level
3. **查询优化**：
   - 批量查询子级数据，避免N+1查询
   - 使用@Select注解，直接SQL查询，性能更好

#### 前端优化
1. **localStorage缓存**：
   - 24小时过期时间
   - 减少API调用次数
   - 提升用户体验
2. **懒加载策略**：
   - 初始只加载省份列表
   - 选择省份后加载城市
   - 选择城市后加载区县
3. **组件复用**：
   - 统一地区选择组件，可在多个页面复用
   - 减少代码重复

### 技术细节
- 地区编码：使用6位数字编码（如：110000表示北京市）
- 树形结构：通过parent_id建立父子关系
- 级别标识：1-省/直辖市，2-市，3-区/县
- 缓存策略：多级缓存（Caffeine + localStorage）
- 数据预加载：应用启动时自动预加载，减少首次查询延迟

### 使用说明
1. **执行数据库脚本**：
   ```sql
   -- 1. 创建表
   source database/update-20251212-create-region-table.sql;
   
   -- 2. 导入数据（需要先运行Python脚本生成SQL）
   python database/import_regions.py
   source database/update-20251212-import-regions-data.sql;
   ```
2. **前端使用组件**：
   ```vue
   <RegionSelector 
     v-model="regionData"
     @change="handleRegionChange"
   />
   ```

### 影响
- ✅ 完整的省市区三级地区数据支持
- ✅ 高性能缓存策略，减少数据库查询
- ✅ 前端localStorage缓存，减少API调用
- ✅ 统一地区选择组件，便于复用
- ✅ 支持用户注册和收货地址的地区选择
- ✅ 应用启动时预加载，首次查询无延迟

---

## 2025-12-12 - Token过期自动跳转登录页

### 修改内容
优化token过期处理逻辑，当系统检测到token过期时，不显示错误提示，直接跳转到登录页面。

### 修改文件

#### 前端
1. frontend/src/utils/request.ts - 优化token过期处理逻辑

### 具体修改

#### 1. 响应拦截器优化
- 在成功响应拦截器中，当返回`code === 401`且消息包含"Token已过期"或"Token无效"时，不显示错误提示，直接清除token并跳转到登录页
- 在错误响应拦截器中，当HTTP状态码为401且错误消息包含"Token已过期"、"Token无效"或"未登录"时，不显示错误提示，直接清除token并跳转到登录页
- 其他401错误仍然显示错误提示

#### 2. 修复类型错误
- 将`AxiosRequestConfig`改为`InternalAxiosRequestConfig`，修复axios版本更新后的类型兼容性问题

### 技术细节
- 检测token过期的条件：`code === 401` 且消息包含"Token已过期"、"Token无效"或"未登录"
- 跳转前自动清除localStorage中的token
- 使用`router.push('/login')`跳转到登录页
- 不显示`ElMessage.error`提示，避免用户看到多个错误提示

### 影响
- ✅ Token过期时自动跳转登录页，不显示错误提示
- ✅ 提升用户体验，避免页面出现多个错误提示
- ✅ 统一处理token过期逻辑，代码更加清晰
- ✅ 修复了axios类型兼容性问题

---

## 2025-12-12 - 修复支付页面重复错误提示问题

### 修改内容
修复支付页面支付密码错误时出现两个重复错误提示的问题，优化错误处理逻辑。

### 修改文件

#### 前端
1. frontend/src/views/order/Payment.vue - 优化错误处理，移除重复的错误提示

### 具体修改

#### 1. 移除重复的错误提示
- 在 `processPayment` 函数的 catch 块中移除 `ElMessage.error` 调用
- 因为 `request.ts` 的响应拦截器已经统一处理并显示了错误消息
- 避免同一个错误被显示两次

#### 2. 优化支付密码错误处理
- 当支付密码错误时，自动重新打开密码输入对话框
- 清空密码输入框，让用户重新输入
- 改善用户体验，避免用户需要手动重新点击付款按钮

#### 3. 优化密码清空逻辑
- 在 finally 块中，只在非预存款支付时清空密码
- 预存款支付失败时保留密码输入框状态，方便用户查看和重新输入
- 避免支付失败后密码被意外清空

### 技术细节
- 错误消息统一由 `request.ts` 的响应拦截器处理
- 通过检查错误消息内容判断是否为支付密码错误
- 使用 `showPaymentPasswordDialog` 控制对话框显示状态

### 影响
- ✅ 修复了支付密码错误时出现两个重复提示的问题
- ✅ 支付密码错误时自动重新打开输入对话框，提升用户体验
- ✅ 错误处理逻辑更加清晰，避免重复提示
- ✅ 密码输入框状态管理更加合理

---

## 2025-12-12 - 购物结算页面预存款余额对接后端

### 修改内容
在购物结算页面（/cart/checkout）对接后端API，获取并显示预存款余额数据。

### 修改文件

#### 前端
1. frontend/src/views/cart/Checkout.vue - 对接预存款余额API

### 具体修改

#### 1. 导入API和依赖
- 导入 `getDepositBalance` API函数用于获取预存款数据
- 导入 `useUserStore` 用于检查用户登录状态

#### 2. 添加预存款余额加载函数
- 创建 `loadDepositBalance` 函数调用后端API获取预存款余额
- 使用 `availableBalance` 字段作为显示的余额（可用余额）
- 添加错误处理，对于401未授权错误不显示提示，其他错误静默处理

#### 3. 页面加载时获取数据
- 在 `onMounted` 生命周期钩子中调用 `loadDepositBalance`
- 仅在用户已登录时调用API获取数据
- 与加载地址列表和购物车商品并行执行

### 技术细节
- 后端API: `/api/buyer/member/deposit/balance`
- 返回字段: `availableBalance`（可用余额）
- 仅在用户已登录时调用API获取数据
- 错误处理：401错误不显示提示，其他错误静默处理，保持页面正常使用

### 影响
- ✅ 购物结算页面预存款余额从后端实时获取
- ✅ 用户登录后自动加载预存款余额数据
- ✅ 未登录用户不显示错误提示，保持良好用户体验
- ✅ 预存款余额数据与后端数据库保持同步
- ✅ 支付方式选择时显示准确的预存款余额

---

## 2025-12-12 - 完善支付密码逻辑和提示

### 修改内容
完善支付环节的支付密码验证逻辑，支持"未设置支付密码时，默认使用登录密码"的功能，并在支付密码输入界面添加提示语。

### 修改文件

#### 后端
1. backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java - 修改支付密码验证逻辑

#### 前端
1. frontend/src/views/order/Payment.vue - 在支付密码输入对话框添加提示语

### 具体修改

#### 1. 后端支付密码验证逻辑优化
- 修改 `OrderServiceImpl.java` 中的支付密码验证逻辑
- 如果用户未设置过支付密码（`paymentPassword` 为空），则使用登录密码进行验证
- 如果用户已设置过支付密码，则使用支付密码进行验证
- 移除了"请先设置支付密码"的异常抛出，改为自动使用登录密码作为默认支付密码

#### 2. 前端支付密码输入界面优化
- 在支付密码输入对话框中添加黄色提示条
- 提示内容："(如未设置过支付密码,默认支付密码为您的账号登陆密码!)"
- 提示样式与修改支付密码页面保持一致（黄色背景、边框、文字颜色）

### 技术细节
- 支付密码验证逻辑与修改支付密码逻辑保持一致
- 使用BCrypt进行密码验证
- 提示样式使用与修改支付密码页面相同的设计风格

### 影响
- ✅ 用户未设置支付密码时，可以使用登录密码进行支付
- ✅ 支付密码输入界面有明确的提示信息，提升用户体验
- ✅ 支付密码验证逻辑统一，避免用户困惑
- ✅ 与修改支付密码页面的提示保持一致

---

## 2025-12-12 - 实现修改预存款支付密码功能

### 修改内容
在会员中心个人设置下增加"修改预存款支付密码"菜单页面，参考截图1:1仿真实现前端页面和后端对接。如果用户未设置过支付密码，默认支付密码为账号登录密码。

### 修改文件

#### 前端
1. frontend/src/components/member/MemberSidebar.vue - 添加"修改预存款支付密码"菜单项
2. frontend/src/views/member/PaymentPassword.vue - 创建修改预存款支付密码页面
3. frontend/src/router/index.ts - 添加修改预存款支付密码路由
4. frontend/src/api/buyer/user.ts - 添加修改支付密码API调用方法

#### 后端
1. backend/src/main/java/com/shoppingmall/service/user/UserService.java - 添加修改支付密码接口方法
2. backend/src/main/java/com/shoppingmall/service/user/impl/UserServiceImpl.java - 实现修改支付密码方法
3. backend/src/main/java/com/shoppingmall/controller/buyer/UserController.java - 添加修改支付密码API接口

### 具体修改

#### 1. 前端菜单和路由
- 在MemberSidebar组件中添加"修改预存款支付密码"菜单项（位于"修改密码"和"收货地址"之间）
- 添加路由映射和自动判断逻辑
- 在路由配置中添加 `/member/settings/payment-password` 路由

#### 2. 前端页面实现
- 创建PaymentPassword.vue页面，参考截图1:1仿真
- 页面包含：
  - 标题："预存款支付密码修改"
  - 黄色提示条："(如未设置过支付密码,默认支付密码为您的账号登陆密码!)"
  - 三个输入框：原支付密码、新支付密码、确认新支付密码
  - 保存按钮（灰色样式）
- 使用表格布局（table），左侧标签，右侧输入框
- 表单验证：原支付密码必填，新支付密码必填且长度6-20字符，确认密码必须与新密码一致
- 密码输入框支持显示/隐藏密码功能

#### 3. 后端API实现
- 在UserService接口中添加 `changePaymentPassword` 方法
- 在UserServiceImpl中实现修改支付密码逻辑：
  - 验证原支付密码：如果用户未设置过支付密码（paymentPassword为空），则使用登录密码验证；如果已设置，则使用支付密码验证
  - 使用BCrypt加密新支付密码并保存
- 在UserController中添加 `PUT /api/buyer/user/payment-password` 接口

#### 4. 前端API调用
- 在user.ts中添加 `changePaymentPassword` 方法，调用后端API

### 技术细节
- 后端API: `PUT /api/buyer/user/payment-password`
- 请求参数: `oldPaymentPassword`（原支付密码）、`newPaymentPassword`（新支付密码）
- 密码加密: 使用BCrypt加密存储
- 默认密码逻辑: 如果用户未设置过支付密码，默认使用登录密码作为支付密码

### 影响
- ✅ 用户可以在会员中心修改预存款支付密码
- ✅ 支持首次设置支付密码（使用登录密码验证）
- ✅ 前端页面样式与截图保持一致
- ✅ 完整的表单验证和错误提示
- ✅ 修改成功后清空表单并提示成功信息

---

## 2025-12-12 - 会员首页预存款数据对接后端

### 修改内容
在会员首页（/member）对接后端API，获取并显示预存款余额和可用余额数据。

### 修改文件
1. frontend/src/views/member/Index.vue

### 具体修改
1. **导入API函数**: 导入 `getDepositBalance` API函数用于获取预存款数据
2. **添加生命周期钩子**: 使用 `onMounted` 在组件挂载时获取预存款数据
3. **实现数据获取函数**: 创建 `fetchDepositBalance` 函数调用后端API获取预存款余额和可用余额
4. **错误处理**: 添加错误处理逻辑，对于401未授权错误不显示提示，其他错误显示友好提示
5. **数据绑定**: 将API返回的 `depositBalance` 和 `availableBalance` 字段绑定到页面显示

### 技术细节
- 后端API: `/api/buyer/member/deposit/balance`
- 返回字段: `depositBalance`（预存款余额）、`availableBalance`（可用余额）
- 仅在用户已登录时调用API获取数据

### 影响
- ✅ 会员首页预存款余额和可用余额从后端实时获取
- ✅ 用户登录后自动加载预存款数据
- ✅ 未登录用户不显示错误提示，保持良好用户体验
- ✅ 预存款数据与后端数据库保持同步

---

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

## 2025-12-12 - 订单库存管理和自动取消功能

### 修改内容
完善订单创建时的库存扣减机制，并实现待付款订单的自动取消功能。

### 修改文件

#### 配置文件
1. backend/src/main/resources/application.yml - 添加订单超时时间配置参数

#### 后端
1. backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java - 添加库存扣减和恢复逻辑
2. backend/src/main/java/com/shoppingmall/service/buyer/OrderScheduledService.java - 创建订单定时任务接口
3. backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderScheduledServiceImpl.java - 实现订单自动取消定时任务

### 具体修改

#### 1. 配置参数
- 在`application.yml`中添加`order.payment-timeout-hours`配置项，默认值为6小时
- 支持通过配置文件修改订单超时时间，方便运维调整

#### 2. 库存扣减机制

**创建订单时扣减库存**
- 在`OrderServiceImpl.createOrder`方法中添加库存扣减逻辑
- 扣减`product`表的`stock`字段（商品库存）
- 扣减`product_stock`表的`availableStock`字段（可用库存）
- 增加`product_stock`表的`lockedStock`字段（锁定库存）
- 如果`product_stock`记录不存在，自动创建新记录

**库存扣减流程**
1. 验证商品库存是否足够
2. 创建订单成功后，立即扣减库存
3. 同时更新`product`表和`product_stock`表
4. 确保库存数据一致性

#### 3. 订单自动取消机制

**定时任务**
- 创建`OrderScheduledService`接口和`OrderScheduledServiceImpl`实现类
- 使用`@Scheduled(fixedRate = 60000)`注解，每分钟执行一次
- 自动查找超过配置时间未支付的待付款订单
- 自动取消超时订单并恢复库存

**自动取消流程**
1. 定时任务每分钟执行一次
2. 查找创建时间超过配置时间的待付款订单
3. 对每个超时订单：
   - 恢复`product`表的库存
   - 恢复`product_stock`表的可用库存
   - 减少`product_stock`表的锁定库存
   - 更新订单状态为已取消
4. 记录详细的日志信息

#### 4. 手动取消订单时恢复库存

**取消订单方法优化**
- 在`OrderServiceImpl.cancelOrder`方法中添加库存恢复逻辑
- 用户手动取消订单时，自动恢复已扣减的库存
- 确保库存数据准确性

### 功能特性

1. **库存管理**
   - ✅ 创建订单时立即扣减库存，防止超卖
   - ✅ 同时更新`product`表和`product_stock`表，保持数据一致性
   - ✅ 订单取消时自动恢复库存，避免库存损失

2. **自动取消机制**
   - ✅ 定时任务自动检查超时订单
   - ✅ 可配置的超时时间（默认6小时）
   - ✅ 自动恢复超时订单的库存
   - ✅ 异常处理完善，单个订单处理失败不影响其他订单

3. **配置灵活性**
   - ✅ 超时时间可通过配置文件修改
   - ✅ 支持不同环境使用不同的超时时间
   - ✅ 默认值6小时，符合常见业务需求

### 技术实现

1. **库存扣减**
   - 使用数据库事务保证原子性
   - 先验证库存，再扣减，避免并发问题
   - 同时更新两个库存表，保持数据同步

2. **定时任务**
   - 使用Spring的`@Scheduled`注解
   - 每分钟执行一次，及时处理超时订单
   - 异常处理机制，确保定时任务异常不影响系统运行

3. **库存恢复**
   - 订单取消时（手动或自动）都恢复库存
   - 恢复逻辑与扣减逻辑对应，确保数据准确性
   - 处理边界情况（如库存记录不存在）

### 影响
- ✅ 订单创建时立即扣减库存，防止超卖问题
- ✅ 待付款订单自动取消，释放被占用的库存
- ✅ 库存管理更加精确，避免库存数据不一致
- ✅ 提升系统自动化程度，减少人工干预
- ✅ 配置灵活，可根据业务需求调整超时时间
---

## 2025-12-12 - 完善支付页面功能

### 修改内容
完善支付页面（/order/payment），实现3种支付方式：预存款支付、支付宝支付、微信支付。预存款支付需要验证支付密码并检查余额；支付宝和微信支付使用mock模拟支付回调。

### 修改文件

#### 数据库
1. database/update-20251212-add-payment-password.sql - 添加支付密码字段到用户表

#### 后端
1. backend/src/main/java/com/shoppingmall/entity/User.java - 添加支付密码字段
2. backend/src/main/java/com/shoppingmall/entity/PaymentRecord.java - 创建支付记录实体类
3. backend/src/main/java/com/shoppingmall/repository/payment/PaymentRecordRepository.java - 创建支付记录Repository
4. backend/src/main/java/com/shoppingmall/dto/OrderPaymentDTO.java - 创建订单支付DTO
5. backend/src/main/java/com/shoppingmall/service/buyer/OrderService.java - 添加订单支付方法接口
6. backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java - 实现订单支付方法
7. backend/src/main/java/com/shoppingmall/controller/buyer/OrderController.java - 添加订单支付接口
8. backend/src/main/java/com/shoppingmall/controller/buyer/PaymentController.java - 创建支付回调控制器

#### 前端
1. frontend/src/api/buyer/order.ts - 添加订单支付API接口
2. frontend/src/views/order/Payment.vue - 完善支付页面功能

### 具体修改

#### 1. 数据库修改
- 在用户表（sys_user）中添加支付密码字段（payment_password），用于存储BCrypt加密的支付密码

#### 2. 后端修改

**实体类**
- 在User实体类中添加paymentPassword字段
- 创建PaymentRecord实体类，用于存储支付记录

**服务层**
- 在OrderService接口中添加payOrder方法
- 在OrderServiceImpl中实现payOrder方法：
  - 预存款支付：验证支付密码、检查余额、扣款、更新订单和支付记录
  - 支付宝/微信支付：创建支付订单、返回支付URL、创建待支付记录

**控制器**
- 在OrderController中添加订单支付接口（POST /api/buyer/orders/{orderNo}/pay）
- 创建PaymentController，提供支付回调接口（POST /api/buyer/payment/callback）和模拟支付成功接口（POST /api/buyer/payment/mock/success）

#### 3. 前端修改

**API接口**
- 在order.ts中添加OrderPaymentDTO接口和payOrder方法

**支付页面**
- 添加预存款余额加载功能
- 添加支付密码输入对话框
- 实现预存款支付流程：检查余额、输入密码、调用支付接口
- 实现支付宝/微信支付流程：调用支付接口、模拟支付回调、更新订单状态
- 添加支付状态loading提示

### 功能特性

1. **预存款支付**
   - ✅ 自动加载并显示预存款余额
   - ✅ 支付前检查余额是否足够
   - ✅ 需要输入支付密码进行验证
   - ✅ 支付成功后立即更新订单状态和支付记录
   - ✅ 自动创建预存款消费记录

2. **支付宝/微信支付**
   - ✅ 创建支付订单并返回支付URL
   - ✅ 支持mock模拟支付回调
   - ✅ 支付回调后自动更新订单状态和支付记录
   - ✅ 支付成功后跳转到订单详情页面

3. **支付记录**
   - ✅ 所有支付方式都会创建支付记录（payment_record表）
   - ✅ 支付记录包含订单ID、支付方式、金额、状态等信息
   - ✅ 支付宝/微信支付回调数据保存到支付记录中

### 影响
- ✅ 支付页面功能完善，支持3种支付方式
- ✅ 预存款支付安全性提升，需要支付密码验证
- ✅ 支付宝/微信支付支持mock模拟，方便测试
- ✅ 所有支付数据都会更新到交易记录表和订单表
- ✅ 提升用户体验，支付流程更加顺畅
---