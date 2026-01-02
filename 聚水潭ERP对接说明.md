# 聚水潭ERP对接说明文档

## 一、测试环境配置

### 1.1 环境信息
- **测试API地址**: `https://dev-api.jushuitan.com/api/open/query.aspx`
- **生产API地址**: `https://api.jushuitan.com/api/open/query.aspx`

### 1.2 认证凭据（测试环境）
```
APP Key: fb5302ac42e8496d9e764db70a3c5c24
APP Secret: a804ba835d0a4c849336f4e2e7695ddf
Access Token: f804693efc49416a84dcf0ca901e8622
Refresh Token: 46f26b7842ff4b68a4f0e61df6c6426f
```

## 二、API调用方式

### 2.1 通用接口规范

聚水潭使用统一的API入口：`/api/open/query.aspx`，通过 `method` 参数区分不同的业务接口。

### 2.2 公共参数

所有API请求都需要包含以下公共参数：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| app_key | String | 是 | 应用Key |
| access_token | String | 是 | 访问令牌（测试环境必填） |
| timestamp | Long | 是 | UNIX时间戳（秒） |
| charset | String | 是 | 字符集，固定值：utf-8 |
| version | String | 是 | API版本，固定值：2 |
| method | String | 是 | 接口方法名 |
| sign | String | 是 | 请求签名（MD5，大写） |

### 2.3 签名算法

```java
// 1. 将参数按key排序（不包括sign）
TreeMap<String, String> params = new TreeMap<>();
params.put("access_token", accessToken);
params.put("app_key", appKey);
params.put("charset", "utf-8");
params.put("method", method);
params.put("timestamp", String.valueOf(timestamp));
params.put("version", "2");
// 如果有业务参数，也加入签名
params.put("items", itemsJson);

// 2. 拼接签名字符串
StringBuilder sb = new StringBuilder(appSecret);
for (Map.Entry<String, String> entry : params.entrySet()) {
    sb.append(entry.getKey()).append(entry.getValue());
}
sb.append(appSecret);

// 3. 计算MD5并转大写
String sign = DigestUtils.md5Hex(sb.toString().getBytes(StandardCharsets.UTF_8)).toUpperCase();
```

签名字符串格式：
```
{appSecret}{key1}{value1}{key2}{value2}...{appSecret}
```

示例：
```
a804ba835d0a4c849336f4e2e7695ddfaccess_tokenf804693efc49416a84dcf0ca901e8622app_keyfb5302ac42e8496d9e764db70a3c5c24charsetutf-8methodshops.querytimestamp1767330954version2a804ba835d0a4c849336f4e2e7695ddf
```

## 三、商品上传接口

### 3.1 接口信息

- **Method**: `jushuitan.itemsku.upload`
- **业务参数名**: `items`
- **业务参数格式**: JSON对象，包含items数组

### 3.2 请求参数示例

公共参数：
```
app_key=fb5302ac42e8496d9e764db70a3c5c24
access_token=f804693efc49416a84dcf0ca901e8622
method=jushuitan.itemsku.upload
timestamp=1767330954
charset=utf-8
version=2
sign=计算的签名值
```

业务参数（items）：
```json
{
  "items": [
    {
      "i_id": "001C0812",
      "sku_id": "001C0812",
      "name": "短袖112",
      "short_name": "短袖12",
      "item_type": "成品",
      "sku_pic": "Sku_pic11",
      "pic": "1231",
      "pic_big": "Pic_big1",
      "enabled": -1,
      "shelf_life": 10,
      "batch_enabled": true,
      "s_price": 100,
      "c_price": 0,
      "market_price": 300,
      "weight": 1.52,
      "w": 3,
      "h": 1,
      "l": 1,
      "properties_value": "蓝色;XXL",
      "stock_disabled": true,
      "is_series_number": true,
      "supplier_name": "供应商1",
      "supplier_i_id": "sup_i_id12",
      "supplier_sku_id": "sup_sku_id12",
      "brand": "NIKE12",
      "vc_name": "虚拟分类12",
      "c_name": "",
      "unit": "",
      "sku_code": "648125644612",
      "remark": "123",
      "labels": [],
      "hand_day": 180,
      "rejectLifecycle": 10,
      "lockupLifecycle": 20,
      "adventLifecycle": 30,
      "production_licence": "SC001",
      "other_1": "2.11",
      "other_2": "2.21",
      "other_3": "2.31",
      "other_4": "2.41",
      "other_5": "2.51",
      "other_price_1": 1.12,
      "other_price_2": 1.22,
      "other_price_3": 1.32,
      "other_price_4": 1.42,
      "other_price_5": 1.52,
      "other_code": "",
      "CategoryPropertys": {
        "年份": "2021",
        "季节": "春",
        "波段": "春一波",
        "面料成分": "面料",
        "面料类别": "针织类",
        "执行标准": "2019",
        "安全技术类别": " B类",
        "计划上市日期": "2020-12-24"
      }
    }
  ]
}
```

### 3.3 必填字段

根据聚水潭文档，最少必填字段（简化版）：
```json
{
  "items": [
    {
      "sku_id": "商品编码",
      "i_id": "款式编码",
      "name": "商品名称"
    }
  ]
}
```

### 3.4 完整HTTP请求示例

```bash
curl -X POST "https://dev-api.jushuitan.com/api/open/query.aspx" \
  -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" \
  -d "access_token=f804693efc49416a84dcf0ca901e8622" \
  -d "app_key=fb5302ac42e8496d9e764db70a3c5c24" \
  -d "method=jushuitan.itemsku.upload" \
  -d "timestamp=1767330954" \
  -d "charset=utf-8" \
  -d "version=2" \
  -d 'items={"items":[{"sku_id":"001C0812","i_id":"001C0812","name":"短袖112"}]}' \
  -d "sign=计算的签名值"
```

## 四、当前代码实现

### 4.1 核心文件

1. **签名工具类**: `/backend/src/main/java/com/shoppingmall/common/util/JushuitanSignUtil.java`
2. **HTTP工具类**: `/backend/src/main/java/com/shoppingmall/common/util/JushuitanHttpUtil.java`
3. **商品同步服务**: `/backend/src/main/java/com/shoppingmall/service/erp/impl/JushuitanItemServiceImpl.java`

### 4.2 调用流程

```java
// 1. 获取配置
JushuitanConfig config = getEnabledConfig();
String apiUrl = config.getTestApiUrl(); // 测试环境
String appKey = config.getTestAppKey();
String appSecret = config.getTestAppSecret();
String accessToken = config.getTestAccessToken();

// 2. 构建商品数据
JushuitanItemDTO itemDTO = new JushuitanItemDTO();
itemDTO.setSkuId(String.valueOf(product.getId()));
itemDTO.setIId(product.getProductCode());
itemDTO.setName(product.getProductName());

// 3. 构建items参数（注意：值是包含items数组的JSON对象）
Map<String, Object> itemsData = new HashMap<>();
itemsData.put("items", Collections.singletonList(itemDTO));
String itemsJson = objectMapper.writeValueAsString(itemsData);
// 结果: {"items":[{...}]}

// 4. 调用API
String response = JushuitanHttpUtil.post(
    apiUrl,                          // API地址
    appKey,                          // APP Key
    appSecret,                       // APP Secret
    accessToken,                     // Access Token
    "jushuitan.itemsku.upload",      // Method参数
    "items",                         // 业务参数名
    itemsJson                        // 业务参数值
);
```

### 4.3 HTTP工具类实现要点

```java
public static String post(String apiUrl, String appKey, String appSecret,
                         String accessToken, String method,
                         String bizParamName, String bizParams) {
    // 1. 构建参数
    Map<String, String> params = new HashMap<>();
    params.put("access_token", accessToken);
    params.put("app_key", appKey);
    params.put("charset", "utf-8");
    params.put("version", "2");

    if (method != null && !method.isEmpty()) {
        params.put("method", method);
    }

    // 时间戳（秒）
    long timestamp = java.time.Instant.now().getEpochSecond();
    params.put("timestamp", String.valueOf(timestamp));

    // 业务参数
    if (bizParams != null && !bizParams.isEmpty()) {
        params.put(bizParamName, bizParams);
    }

    // 2. 生成签名
    String sign = JushuitanSignUtil.generateSign(appSecret, params);
    params.put("sign", sign);

    // 3. 构建表单数据（URL编码）
    StringBuilder formData = new StringBuilder();
    for (Map.Entry<String, String> entry : params.entrySet()) {
        if (formData.length() > 0) {
            formData.append("&");
        }
        formData.append(entry.getKey()).append("=")
               .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
    }

    // 4. 发送POST请求
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/x-www-form-urlencoded;charset=UTF-8"));

    HttpEntity<String> requestEntity = new HttpEntity<>(formData.toString(), headers);
    ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

    return response.getBody();
}
```

## 五、响应格式

### 5.1 成功响应

```json
{
  "code": 0,
  "issuccess": true,
  "msg": "操作成功",
  "data": []
}
```

### 5.2 失败响应

```json
{
  "code": 140,
  "issuccess": false,
  "msg": "参数传递错误",
  "requestId": "1a0c641317673269678562437e3694"
}
```

### 5.3 部分成功响应

当部分商品上传失败时：
```json
{
  "code": 0,
  "issuccess": true,
  "msg": "操作成功",
  "data": [
    {
      "sku_id": "001C0812",
      "message": "商品编码已存在"
    }
  ]
}
```

## 六、常见错误

### 6.1 Error 140 - 参数传递错误

**可能原因**：
1. 签名计算错误
2. 时间戳格式错误（应该是秒，不是毫秒）
3. 参数名错误
4. 业务参数格式错误
5. Access Token无效或过期

**解决方法**：
- 检查签名算法是否正确
- 确认时间戳是UNIX秒数
- 验证access_token是否有效
- 确认业务参数格式符合文档要求

### 6.2 Read Timeout - 读取超时

**可能原因**：
1. 接口路径错误（服务器找不到接口）
2. 网络问题
3. 服务器处理超时

**解决方法**：
- 确认API路径正确
- 检查method参数是否正确
- 增加超时时间

### 6.3 验证失败！请求数据格式错误

**可能原因**：
- 业务参数JSON格式不正确
- 缺少必填字段

**解决方法**：
- 检查items参数的JSON格式
- 确保包含必填字段：sku_id, i_id, name

## 七、当前状态与待解决问题

### 7.1 已完成
- ✅ 签名算法实现
- ✅ HTTP工具类实现
- ✅ 商品转换逻辑
- ✅ 同步日志记录
- ✅ 前端界面（商品同步日志查询）

### 7.2 待解决
- ❌ Error 140错误 - 参数传递错误
  - 可能是access_token无效
  - 可能是签名算法有问题
  - 需要确认测试环境的认证方式

- ❌ 接口超时问题
  - 需要确认正确的API路径
  - 可能需要使用不同的method参数

### 7.3 需要验证的信息
1. 测试环境的access_token获取方式
2. 商品上传接口的正确method参数
3. 是否需要先授权才能调用API
4. items参数的具体格式要求

## 八、下一步计划

1. **验证认证凭据**
   - 确认access_token是否有效
   - 测试简单的查询接口（如shops.query）
   - 如果失败，需要重新获取token

2. **确认接口规范**
   - 查看聚水潭官方文档
   - 确认商品上传接口的method参数
   - 确认items参数的具体格式

3. **调试优化**
   - 添加更详细的日志
   - 对比官方示例代码
   - 联系聚水潭技术支持

## 九、参考资源

- 聚水潭开放平台文档: https://open.jushuitan.com/
- 测试环境后台: （需要提供）
- 技术支持: （需要提供）

---

**文档更新时间**: 2026-01-02
**当前版本**: v1.0
**维护人**: 开发团队
