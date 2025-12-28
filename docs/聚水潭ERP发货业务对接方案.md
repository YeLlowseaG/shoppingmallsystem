# 聚水潭ERP发货业务对接方案

## 一、对接概述

### 1.1 对接目标
将当前系统的订单发货业务与聚水潭ERP平台对接，实现：
- 订单支付成功后自动推送到聚水潭ERP
- 聚水潭ERP发货后自动回调更新系统订单状态和物流信息
- 支持订单状态同步和异常处理

### 1.2 对接方式
- **订单推送**：系统主动推送订单到聚水潭ERP（创建销售单）
- **发货回调**：聚水潭ERP发货后通过回调接口通知系统
- **状态查询**：可选，支持主动查询ERP订单状态

### 1.3 参考文档
- 聚水潭开放平台文档：https://openweb.jushuitan.com/doc?docId=170
- API接口文档：需要根据实际聚水潭账号获取

## 二、需要创建的文件

### 2.1 数据库表

#### ERP订单同步记录表（erp_order_sync）
```sql
CREATE TABLE `erp_order_sync` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  `erp_order_no` VARCHAR(100) DEFAULT NULL COMMENT 'ERP订单号',
  `sync_status` TINYINT NOT NULL DEFAULT 0 COMMENT '同步状态（0-待推送，1-推送成功，2-推送失败，3-已发货）',
  `sync_time` DATETIME DEFAULT NULL COMMENT '同步时间',
  `error_message` TEXT COMMENT '错误信息',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_sync_status` (`sync_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP订单同步记录表';
```

**文件位置**：`database/update-20251214-create-erp-sync-table.sql`

### 2.2 后端实体类

#### ERP订单同步记录实体（ErpOrderSync.java）
**文件位置**：`backend/src/main/java/com/shoppingmall/entity/ErpOrderSync.java`

```java
package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ERP订单同步记录实体类
 */
@Data
@TableName("erp_order_sync")
public class ErpOrderSync {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    private String orderNo;
    private String erpOrderNo;
    
    /**
     * 同步状态（0-待推送，1-推送成功，2-推送失败，3-已发货）
     */
    private Integer syncStatus;
    
    private LocalDateTime syncTime;
    private String errorMessage;
    private Integer retryCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### 2.3 后端DTO类

#### 聚水潭订单推送DTO（JushuitanOrderDTO.java）
**文件位置**：`backend/src/main/java/com/shoppingmall/dto/JushuitanOrderDTO.java`

```java
package com.shoppingmall.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 聚水潭订单推送DTO
 */
@Data
public class JushuitanOrderDTO {
    /**
     * 订单号（系统订单号）
     */
    private String orderNo;
    
    /**
     * 订单时间
     */
    private String orderTime;
    
    /**
     * 买家信息
     */
    private BuyerInfo buyerInfo;
    
    /**
     * 收货地址
     */
    private ShippingAddress shippingAddress;
    
    /**
     * 订单商品明细
     */
    private List<OrderItem> items;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 运费
     */
    private BigDecimal shippingFee;
    
    /**
     * 订单备注
     */
    private String remark;
    
    @Data
    public static class BuyerInfo {
        private String name;
        private String phone;
        private String email;
    }
    
    @Data
    public static class ShippingAddress {
        private String province;
        private String city;
        private String district;
        private String address;
        private String zipCode;
        private String receiverName;
        private String receiverPhone;
        private String receiverMobile;
    }
    
    @Data
    public static class OrderItem {
        private String productCode;  // 商品编码
        private String productName;  // 商品名称
        private Integer quantity;     // 数量
        private BigDecimal price;     // 单价
        private BigDecimal subtotal; // 小计
    }
}
```

#### 聚水潭发货回调DTO（JushuitanShipCallbackDTO.java）
**文件位置**：`backend/src/main/java/com/shoppingmall/dto/JushuitanShipCallbackDTO.java`

```java
package com.shoppingmall.dto;

import lombok.Data;

/**
 * 聚水潭发货回调DTO
 */
@Data
public class JushuitanShipCallbackDTO {
    /**
     * ERP订单号
     */
    private String erpOrderNo;
    
    /**
     * 系统订单号
     */
    private String orderNo;
    
    /**
     * 物流公司
     */
    private String logisticsCompany;
    
    /**
     * 物流单号
     */
    private String logisticsNo;
    
    /**
     * 发货时间
     */
    private String shipTime;
    
    /**
     * 签名（用于验证）
     */
    private String sign;
}
```

### 2.4 后端Repository

#### ERP订单同步Repository（ErpOrderSyncRepository.java）
**文件位置**：`backend/src/main/java/com/shoppingmall/repository/erp/ErpOrderSyncRepository.java`

```java
package com.shoppingmall.repository.erp;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoppingmall.entity.ErpOrderSync;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpOrderSyncRepository extends BaseMapper<ErpOrderSync> {
}
```

### 2.5 后端服务

#### 聚水潭ERP服务接口（JushuitanService.java）
**文件位置**：`backend/src/main/java/com/shoppingmall/service/erp/JushuitanService.java`

```java
package com.shoppingmall.service.erp;

import com.shoppingmall.dto.JushuitanOrderDTO;
import com.shoppingmall.dto.JushuitanShipCallbackDTO;

/**
 * 聚水潭ERP服务接口
 */
public interface JushuitanService {
    /**
     * 推送订单到聚水潭ERP
     * @param orderId 订单ID
     * @return ERP订单号
     */
    String pushOrderToErp(Long orderId);
    
    /**
     * 处理聚水潭发货回调
     * @param callbackDTO 回调数据
     */
    void handleShipCallback(JushuitanShipCallbackDTO callbackDTO);
    
    /**
     * 重试推送失败的订单
     * @param orderNo 订单号
     */
    void retryPushOrder(String orderNo);
    
    /**
     * 验证回调签名
     * @param callbackDTO 回调数据
     * @return 是否验证通过
     */
    boolean verifyCallbackSign(JushuitanShipCallbackDTO callbackDTO);
}
```

#### 聚水潭ERP服务实现（JushuitanServiceImpl.java）
**文件位置**：`backend/src/main/java/com/shoppingmall/service/erp/impl/JushuitanServiceImpl.java`

主要实现：
- HTTP客户端调用聚水潭API
- 订单数据格式转换
- 签名生成和验证
- 异常处理和重试机制

### 2.6 后端控制器

#### 聚水潭回调控制器（JushuitanController.java）
**文件位置**：`backend/src/main/java/com/shoppingmall/controller/erp/JushuitanController.java`

```java
package com.shoppingmall.controller.erp;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.JushuitanShipCallbackDTO;
import com.shoppingmall.service.erp.JushuitanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 聚水潭ERP回调控制器
 */
@RestController
@RequestMapping("/api/erp/jushuitan")
@RequiredArgsConstructor
public class JushuitanController {
    
    private final JushuitanService jushuitanService;
    
    /**
     * 发货回调接口
     */
    @PostMapping("/callback/ship")
    public Result<?> shipCallback(@RequestBody JushuitanShipCallbackDTO callbackDTO) {
        // 验证签名
        if (!jushuitanService.verifyCallbackSign(callbackDTO)) {
            return Result.error(401, "签名验证失败");
        }
        
        // 处理发货回调
        jushuitanService.handleShipCallback(callbackDTO);
        
        return Result.success("回调处理成功");
    }
}
```

### 2.7 配置类

#### 聚水潭配置类（JushuitanConfig.java）
**文件位置**：`backend/src/main/java/com/shoppingmall/config/JushuitanConfig.java`

```java
package com.shoppingmall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 聚水潭ERP配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jushuitan")
public class JushuitanConfig {
    /**
     * 是否启用聚水潭对接
     */
    private Boolean enabled = false;
    
    /**
     * API地址
     */
    private String apiUrl;
    
    /**
     * AppKey
     */
    private String appKey;
    
    /**
     * AppSecret
     */
    private String appSecret;
    
    /**
     * Token
     */
    private String token;
    
    /**
     * 回调地址
     */
    private String callbackUrl;
    
    /**
     * 重试次数
     */
    private Integer maxRetryCount = 3;
    
    /**
     * 重试间隔（秒）
     */
    private Integer retryInterval = 60;
}
```

## 三、需要修改的文件

### 3.1 订单服务修改

#### OrderServiceImpl.java（管理端）
**文件位置**：`backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`

**修改内容**：
- 在 `shipOrder` 方法中，如果订单已推送到ERP，则不再手动发货
- 添加ERP推送状态检查

#### OrderServiceImpl.java（用户端）
**文件位置**：`backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`

**修改内容**：
- 在订单支付成功后（`payOrder` 方法），自动推送订单到聚水潭ERP
- 如果推送失败，记录错误信息，但不影响订单支付流程

### 3.2 配置文件修改

#### application.yml
**文件位置**：`backend/src/main/resources/application.yml`

**添加配置**：
```yaml
# 聚水潭ERP配置
jushuitan:
  enabled: false  # 是否启用聚水潭对接
  api-url: https://api.jushuitan.com  # API地址（根据实际文档修改）
  app-key: your-app-key  # AppKey
  app-secret: your-app-secret  # AppSecret
  token: your-token  # Token
  callback-url: http://your-domain.com/api/erp/jushuitan/callback/ship  # 回调地址
  max-retry-count: 3  # 最大重试次数
  retry-interval: 60  # 重试间隔（秒）
```

### 3.3 系统配置表

在系统配置表中添加ERP相关配置项：
- `erp.jushuitan.enabled` - 是否启用聚水潭对接
- `erp.jushuitan.api_url` - API地址
- `erp.jushuitan.app_key` - AppKey
- `erp.jushuitan.app_secret` - AppSecret
- `erp.jushuitan.token` - Token

## 四、数据流程设计

### 4.1 订单推送流程

```
订单支付成功
    ↓
检查是否启用ERP对接
    ↓
创建ERP同步记录（状态：待推送）
    ↓
转换订单数据为聚水潭格式
    ↓
调用聚水潭API推送订单
    ↓
成功 → 更新同步记录（状态：推送成功，记录ERP订单号）
失败 → 更新同步记录（状态：推送失败，记录错误信息）
    ↓
如果失败且未达到最大重试次数，加入重试队列
```

### 4.2 发货回调流程

```
聚水潭ERP发货
    ↓
调用系统回调接口
    ↓
验证签名
    ↓
查询订单同步记录（根据ERP订单号或系统订单号）
    ↓
更新订单状态为"已发货"
    ↓
更新物流信息（物流公司、物流单号）
    ↓
更新同步记录（状态：已发货）
    ↓
返回成功响应
```

### 4.3 重试机制

- 推送失败的订单自动加入重试队列
- 定时任务每分钟检查待重试的订单
- 重试间隔：60秒
- 最大重试次数：3次
- 超过最大重试次数后，需要手动重试

## 五、技术实现细节

### 5.1 HTTP客户端

使用Spring的 `RestTemplate` 或 `WebClient` 调用聚水潭API：

```java
@Bean
public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    // 设置超时时间
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(10000);
    restTemplate.setRequestFactory(factory);
    return restTemplate;
}
```

### 5.2 签名生成

根据聚水潭API文档要求生成签名：

```java
/**
 * 生成签名
 */
private String generateSign(Map<String, Object> params, String appSecret) {
    // 1. 参数按key排序
    // 2. 拼接成字符串
    // 3. 加上appSecret
    // 4. MD5加密
    // 5. 转大写
    // 具体实现根据聚水潭文档要求
}
```

### 5.3 数据映射

订单数据转换为聚水潭格式：

```java
/**
 * 转换订单为聚水潭格式
 */
private JushuitanOrderDTO convertToJushuitanOrder(Order order, List<OrderItem> items, ShippingAddressDTO address) {
    JushuitanOrderDTO dto = new JushuitanOrderDTO();
    dto.setOrderNo(order.getOrderNo());
    dto.setOrderTime(order.getPayTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    
    // 转换收货地址
    JushuitanOrderDTO.ShippingAddress shippingAddress = new JushuitanOrderDTO.ShippingAddress();
    shippingAddress.setProvince(address.getProvince());
    shippingAddress.setCity(address.getCity());
    shippingAddress.setDistrict(address.getDistrict());
    shippingAddress.setAddress(address.getAddress());
    shippingAddress.setReceiverName(address.getName());
    shippingAddress.setReceiverPhone(address.getPhone());
    shippingAddress.setReceiverMobile(address.getMobile());
    dto.setShippingAddress(shippingAddress);
    
    // 转换商品明细
    List<JushuitanOrderDTO.OrderItem> orderItems = items.stream().map(item -> {
        JushuitanOrderDTO.OrderItem orderItem = new JushuitanOrderDTO.OrderItem();
        orderItem.setProductCode(item.getProductCode());
        orderItem.setProductName(item.getProductName());
        orderItem.setQuantity(item.getQuantity());
        orderItem.setPrice(item.getPrice());
        orderItem.setSubtotal(item.getSubtotal());
        return orderItem;
    }).collect(Collectors.toList());
    dto.setItems(orderItems);
    
    dto.setTotalAmount(order.getTotalAmount());
    dto.setShippingFee(order.getShippingFee());
    dto.setRemark(order.getOrderRemark());
    
    return dto;
}
```

### 5.4 异常处理

```java
try {
    // 调用聚水潭API
    String erpOrderNo = pushOrderToErp(orderId);
    // 更新同步记录
    updateSyncRecord(orderNo, 1, erpOrderNo, null);
} catch (Exception e) {
    log.error("推送订单到ERP失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
    // 更新同步记录为失败
    updateSyncRecord(orderNo, 2, null, e.getMessage());
    // 如果未达到最大重试次数，加入重试队列
    if (retryCount < maxRetryCount) {
        scheduleRetry(orderNo);
    }
}
```

### 5.5 定时任务

创建定时任务处理重试：

```java
@Component
public class ErpSyncRetryTask {
    
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void retryFailedOrders() {
        // 查询推送失败的订单（状态=2，重试次数<最大重试次数）
        // 检查是否达到重试间隔
        // 重新推送订单
    }
}
```

## 六、配置说明

### 6.1 系统配置

在系统配置表中添加以下配置项：

| 配置键 | 配置值 | 说明 |
|--------|--------|------|
| erp.jushuitan.enabled | true/false | 是否启用聚水潭对接 |
| erp.jushuitan.api_url | https://api.jushuitan.com | API地址 |
| erp.jushuitan.app_key | xxx | AppKey |
| erp.jushuitan.app_secret | xxx | AppSecret |
| erp.jushuitan.token | xxx | Token |
| erp.jushuitan.callback_url | http://xxx/api/erp/jushuitan/callback/ship | 回调地址 |

### 6.2 聚水潭平台配置

在聚水潭ERP平台配置：
1. 设置回调地址：`http://your-domain.com/api/erp/jushuitan/callback/ship`
2. 配置API权限：订单推送、发货回调
3. 获取AppKey、AppSecret、Token

## 七、接口设计

### 7.1 订单推送接口（系统 → 聚水潭）

**接口地址**：根据聚水潭API文档（通常是创建销售单接口）

**请求方式**：POST

**请求参数**：
```json
{
  "orderNo": "ORD20251214123456",
  "orderTime": "2025-12-14 10:30:00",
  "buyerInfo": {
    "name": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com"
  },
  "shippingAddress": {
    "province": "广东省",
    "city": "深圳市",
    "district": "南山区",
    "address": "科技园南路123号",
    "zipCode": "518000",
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "receiverMobile": "13800138000"
  },
  "items": [
    {
      "productCode": "PROD001",
      "productName": "商品名称",
      "quantity": 2,
      "price": 99.00,
      "subtotal": 198.00
    }
  ],
  "totalAmount": 198.00,
  "shippingFee": 0.00,
  "remark": "订单备注"
}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "erpOrderNo": "JST20251214123456"
  }
}
```

### 7.2 发货回调接口（聚水潭 → 系统）

**接口地址**：`POST /api/erp/jushuitan/callback/ship`

**请求参数**：
```json
{
  "erpOrderNo": "JST20251214123456",
  "orderNo": "ORD20251214123456",
  "logisticsCompany": "顺丰快递",
  "logisticsNo": "SF1234567890",
  "shipTime": "2025-12-14 15:30:00",
  "sign": "签名值"
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "回调处理成功",
  "data": null
}
```

## 八、数据库设计

### 8.1 ERP订单同步记录表

**表名**：`erp_order_sync`

**字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| order_id | BIGINT | 订单ID（关联order表） |
| order_no | VARCHAR(50) | 订单号（唯一） |
| erp_order_no | VARCHAR(100) | ERP订单号 |
| sync_status | TINYINT | 同步状态（0-待推送，1-推送成功，2-推送失败，3-已发货） |
| sync_time | DATETIME | 同步时间 |
| error_message | TEXT | 错误信息 |
| retry_count | INT | 重试次数 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**索引**：
- 主键：id
- 唯一索引：order_no
- 普通索引：order_id, sync_status

## 九、实现步骤

### 9.1 第一阶段：基础框架搭建
1. 创建数据库表 `erp_order_sync`
2. 创建实体类 `ErpOrderSync`
3. 创建Repository `ErpOrderSyncRepository`
4. 创建配置类 `JushuitanConfig`
5. 在 `application.yml` 中添加配置

### 9.2 第二阶段：订单推送功能
1. 创建DTO类 `JushuitanOrderDTO`
2. 创建服务接口 `JushuitanService`
3. 实现服务类 `JushuitanServiceImpl`
   - 实现订单数据转换
   - 实现HTTP调用
   - 实现签名生成
   - 实现异常处理
4. 修改订单支付服务，添加推送逻辑

### 9.3 第三阶段：发货回调功能
1. 创建回调DTO `JushuitanShipCallbackDTO`
2. 创建回调控制器 `JushuitanController`
3. 实现回调处理逻辑
   - 签名验证
   - 订单状态更新
   - 物流信息更新

### 9.4 第四阶段：重试机制
1. 创建定时任务 `ErpSyncRetryTask`
2. 实现重试逻辑
3. 添加重试队列管理

### 9.5 第五阶段：管理功能
1. 在管理后台添加ERP同步记录查询页面
2. 支持手动重试推送失败的订单
3. 支持查看推送日志和错误信息

## 十、注意事项

### 10.1 数据一致性
- 订单推送失败不影响订单支付流程
- 发货回调失败需要记录日志，支持手动处理
- 支持订单状态手动同步

### 10.2 安全性
- 回调接口必须验证签名
- API密钥存储在配置文件中，不要硬编码
- 生产环境使用HTTPS

### 10.3 性能优化
- 订单推送使用异步方式，避免阻塞主流程
- 重试任务使用线程池，避免影响系统性能
- 批量处理推送失败的订单

### 10.4 错误处理
- 网络异常：自动重试
- API异常：记录错误信息，支持手动重试
- 数据异常：记录详细日志，通知管理员

### 10.5 测试建议
1. 使用聚水潭测试环境进行对接测试
2. 测试订单推送功能
3. 测试发货回调功能
4. 测试异常场景（网络异常、API异常等）
5. 测试重试机制

## 十一、后续扩展

### 11.1 订单状态同步
- 支持主动查询ERP订单状态
- 定时同步订单状态

### 11.2 库存同步
- 支持从ERP同步库存数据
- 支持库存预警

### 11.3 商品同步
- 支持从ERP同步商品信息
- 支持商品价格同步

## 十二、参考资料

- 聚水潭开放平台：https://openweb.jushuitan.com/
- API文档：https://openweb.jushuitan.com/doc?docId=170
- 技术支持：联系聚水潭技术支持获取详细API文档

---

**文档版本**：v1.0  
**创建日期**：2025-12-14  
**最后更新**：2025-12-14





















