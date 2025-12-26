# 支付宝支付调试状态

## 最新更新 (2025-12-26)

### 已完成的工作

#### 1. 代码修复和优化

**Commit 1680aff** (最新)
- 添加测试服务器部署指南

**Commit ad97c00**
- 在 `AlipayUtil.java` 中添加详细的签名调试日志
- 日志内容：
  - 待签名字符串的完整内容
  - 使用的私钥前20个字符
  - 生成的签名前20个字符

**Commit aa90361**
- 修复支付宝回调接口 `@PostMapping` 改为 `@RequestMapping`
- 支持 GET 和 POST 两种请求方式
- 原因：支付宝会先用 GET 请求验证回调URL是否可访问

#### 2. 数据库配置

已在测试数据库 `shopping_mall_test` 中配置：

```sql
-- 支付宝沙箱环境配置
payment.alipay.enabled = 1
payment.alipay.env = sandbox
payment.alipay.sandbox.appid = 9021000157630236  -- Jie的沙箱APPID
payment.alipay.sandbox.gateway = https://openapi-sandbox.dl.alipaydev.com/gateway.do
payment.alipay.sandbox.private_key = [Jie提供的应用私钥]
payment.alipay.sandbox.public_key = [支付宝公钥，从Jie截图中获取]
payment.alipay.notify_url = http://43.139.206.84:8081/api/buyer/payment/alipay/notify
```

## 当前问题

### invalid-signature 错误

**错误信息**：
```
invalid-signature: 验签出错，建议检查签名字符串或待签名私钥与应用公钥是否匹配
```

**支付宝的验证字符串**（从错误日志中获取）：
```
app_id=9021000157630236&biz_content={"out_trade_no":"20251226145307244908","total_amount":"9.00","subject":"订单支付：20251226145307244908","product_code":"FAST_INSTANT_TRADE_PAY"}&charset=utf-8&method=alipay.trade.page.pay&notify_url=http://43.139.206.84:8081/api/buyer/payment/alipay/notify&sign_type=RSA2&timestamp=2025-12-26 15:33:57&version=1.0
```

**根本原因分析**：

支付宝使用我们的签名验证过程：
1. 我们使用**应用私钥**对请求参数进行签名
2. 支付宝使用**应用公钥**验证签名
3. 如果公钥和私钥不是一对，验签必然失败

**可能的原因**：
- 数据库中存储的私钥与支付宝开放平台配置的公钥不匹配
- Jie可能使用了不同的密钥对

### 测试服务器日志分析

最近的测试（时间戳：15:33:57）显示：
- ❌ **没有出现新增的【签名调试】日志**
- 说明测试服务器还未拉取最新代码（Commit ad97c00）

## 下一步行动计划

### 立即执行

1. **部署最新代码到测试服务器**
   - 参考 `DEPLOYMENT_GUIDE.md` 文档
   - 确保拉取到 Commit ad97c00
   - 重启服务

2. **测试并收集调试日志**
   - 创建新的支付订单
   - 从日志中提取【签名调试】信息
   - 特别关注"待签名字符串"

3. **对比签名字符串**
   - 将我们生成的待签名字符串与支付宝返回的验证字符串对比
   - 如果完全一致 → 100%确定是密钥对不匹配
   - 如果不一致 → 检查参数构造逻辑

### Jie需要执行的任务

**场景A：如果签名字符串一致（最有可能）**

1. 登录支付宝开放平台
   - 网址：https://open.alipay.com/develop/sandbox/app
   - 选择沙箱应用：9021000157630236

2. 检查"应用公钥"配置
   - 与数据库中的私钥是否配对

3. 解决方案二选一：

   **方案A**：使用当前数据库中的私钥
   - 从数据库导出私钥：
     ```sql
     SELECT config_value FROM system_config
     WHERE config_key = 'payment.alipay.sandbox.private_key';
     ```
   - 使用以下命令生成对应的公钥：
     ```bash
     openssl rsa -in private_key.pem -pubout -out public_key.pem
     ```
   - 在支付宝平台更新"应用公钥"

   **方案B**：重新生成完整的密钥对
   - 生成新的RSA2密钥对（2048位）
   - 将新的应用公钥上传到支付宝平台
   - 将新的应用私钥更新到数据库

**场景B：如果签名字符串不一致（可能性较小）**

检查我们的参数构造逻辑：
- 字符编码（UTF-8）
- 参数排序规则
- URL编码方式
- 特殊字符处理

## 测试数据

### 本地测试留存的支付表单

文件位置：`/tmp/buyer1_alipay_payment.html`

订单信息：
- 订单号：20251226101706877599
- 金额：9.00元
- 时间戳：2025-12-26 14:18:38
- 签名：Q2fNNta/Iga/ivJ4PsH... (完整签名见文件)

这个表单可以用来对比参数格式和签名生成逻辑。

## 常见问题参考

根据支付宝开发者社区的常见解决方案：

1. **私钥格式问题**
   - ✅ 已确认使用 PKCS8 格式
   - ✅ 代码中正确处理了私钥头尾标记

2. **字符编码问题**
   - ✅ 已确认使用 UTF-8 编码
   - ✅ 参数和签名都使用 StandardCharsets.UTF_8

3. **密钥对不匹配** ⭐⭐⭐
   - ❗ 这是最有可能的原因
   - 需要Jie验证支付宝平台配置

4. **参数构造错误**
   - 待通过调试日志验证
   - 需要对比支付宝的验证字符串

## 技术细节

### 签名生成流程

文件：`src/main/java/com/shoppingmall/payment/util/AlipayUtil.java`

```java
// 1. 获取待签名字符串（参数按key排序并URL编码）
String signContent = getSignContent(params);

// 2. 使用SHA256withRSA算法签名
Signature signature = Signature.getInstance("SHA256withRSA");
signature.initSign(privateKey);
signature.update(content.getBytes(StandardCharsets.UTF_8));
byte[] signBytes = signature.sign();

// 3. Base64编码返回
return Base64.getEncoder().encodeToString(signBytes);
```

### 回调接口修复

文件：`src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java:83`

```java
// 修复前：只支持POST
@PostMapping("/alipay/notify")

// 修复后：支持GET和POST
@RequestMapping(value = "/alipay/notify", method = {RequestMethod.GET, RequestMethod.POST})
```

原因：支付宝在发送实际回调前，会先发送GET请求验证URL是否可访问。

## 相关链接

- [支付宝沙箱环境](https://open.alipay.com/develop/sandbox/app)
- [支付宝签名验签](https://opendocs.alipay.com/common/02mriz)
- [RSA密钥生成工具](https://opendocs.alipay.com/common/02kipl)

## 联系信息

- 代码仓库：https://github.com/YeLlowseaG/shoppingmallsystem
- 测试环境：http://43.139.206.84:8081
- 前端地址：http://43.139.206.84:3002
