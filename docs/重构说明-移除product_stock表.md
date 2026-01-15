# 重构说明：移除product_stock表

## 重构概述

本次重构移除了 `product_stock` 表，统一使用 `product.stock` 和 `product_sku.stock` 来管理库存。

## 重构原因

1. **数据冗余**：`product.stock` 和 `product_stock.total_stock` 重复存储
2. **设计不一致**：有SKU的商品无法使用锁定库存功能
3. **维护复杂**：需要同步更新多个表的库存数据

## 重构方案

### 核心设计变更

1. **库存存储**：
   - 无SKU商品：使用 `product.stock` 存储总库存
   - 有SKU商品：使用 `product_sku.stock` 存储每个SKU的库存，`product.stock` 存储汇总库存

2. **锁定库存逻辑**：
   - 不再使用 `product_stock.locked_stock` 字段
   - 创建订单时直接扣减库存，未支付订单的库存处于"锁定"状态
   - 未支付订单取消时恢复库存

3. **库存扣减时机**：
   - **创建订单时**：扣减库存（`product.stock` 或 `product_sku.stock`）
   - **支付成功时**：不需要再次扣减（创建订单时已扣减）
   - **订单取消时**：只有未支付订单取消时才恢复库存，已支付订单取消时不恢复（商品已卖出）
   - **订单退款时**：不恢复库存（商品已卖出，只是退钱）

## 修改的文件清单

### 1. 新增文件
- `backend/src/main/java/com/shoppingmall/common/util/StockUtil.java`
  - 库存工具类，用于计算可用库存（考虑未支付订单的锁定）

### 2. 修改的核心服务

#### 订单服务
- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderServiceImpl.java`
  - 创建订单：扣减库存（product.stock或product_sku.stock）
  - 修改 `cancelOrder()` 方法：只有未支付订单取消时才恢复库存
  - 移除 `deductStockOnPaymentSuccess()` 方法：支付成功时不需要扣减库存

- `backend/src/main/java/com/shoppingmall/service/admin/impl/OrderServiceImpl.java`
  - 修改 `cancelOrder()` 方法：只有未支付订单取消时才恢复库存
  - 修改 `refundOrder()` 方法：移除库存恢复逻辑（退款不恢复库存）

- `backend/src/main/java/com/shoppingmall/service/buyer/impl/OrderScheduledServiceImpl.java`
  - 修改超时订单取消逻辑：恢复库存（订单未支付，创建时已扣减）

#### 支付服务
- `backend/src/main/java/com/shoppingmall/controller/buyer/PaymentController.java`
  - 支付成功回调：调用库存扣减方法

- `backend/src/main/java/com/shoppingmall/payment/controller/PaymentNotifyController.java`
  - 支付成功回调：调用库存扣减方法

#### 库存管理服务
- `backend/src/main/java/com/shoppingmall/service/admin/impl/StockServiceImpl.java`
  - 移除所有 `product_stock` 表相关操作
  - 修改 `getStockPage()`：使用product表和product_sku表查询
  - 修改 `adjustStock()`：直接更新product.stock
  - 修改 `getStockStatistics()`：从product表和product_sku表统计
  - 修改 `convertToVO()`：移除ProductStock参数

#### SKU服务
- `backend/src/main/java/com/shoppingmall/service/sku/impl/ProductSkuServiceImpl.java`
  - 移除 `updateProductTotalStock()` 中对 `product_stock` 表的同步逻辑
  - 只更新 `product.stock` 字段

#### 商品服务
- `backend/src/main/java/com/shoppingmall/service/product/impl/ProductServiceImpl.java`
  - 移除 `ProductStockRepository` 依赖

#### ERP库存推送服务
- `backend/src/main/java/com/shoppingmall/service/erp/impl/JushuitanInventoryServiceImpl.java`
  - 移除 `ProductStockRepository` 依赖
  - 修改 `syncSingleProductInventory()`：使用 `product.stock` 而不是 `product_stock.available_stock`
  - 修改 `buildInventoryRequest()`：参数改为直接传入库存数量

#### 其他服务
- `backend/src/main/java/com/shoppingmall/service/admin/impl/DashboardServiceImpl.java`
  - 移除 `ProductStockRepository` 依赖

## 数据迁移说明

### 迁移步骤

1. **备份数据**：
   ```sql
   -- 备份product_stock表数据（如果需要）
   CREATE TABLE product_stock_backup AS SELECT * FROM product_stock;
   ```

2. **同步库存数据**：
   ```sql
   -- 将product_stock.total_stock同步到product.stock（如果product.stock为空或不同）
   UPDATE product p
   INNER JOIN product_stock ps ON p.id = ps.product_id
   SET p.stock = ps.total_stock
   WHERE p.stock IS NULL OR p.stock != ps.total_stock;
   ```

3. **同步预警阈值**：
   ```sql
   -- 将product_stock.warning_threshold同步到product.warning_stock
   UPDATE product p
   INNER JOIN product_stock ps ON p.id = ps.product_id
   SET p.warning_stock = ps.warning_threshold
   WHERE p.warning_stock IS NULL OR p.warning_stock != ps.warning_threshold;
   ```

4. **验证数据一致性**：
   ```sql
   -- 检查是否有库存不一致的商品
   SELECT p.id, p.product_code, p.stock as product_stock, ps.total_stock as stock_table_stock
   FROM product p
   INNER JOIN product_stock ps ON p.id = ps.product_id
   WHERE p.stock != ps.total_stock;
   ```

5. **删除product_stock表**（确认无误后）：
   ```sql
   DROP TABLE IF EXISTS product_stock;
   ```

## 注意事项

1. **库存扣减**：
   - 创建订单时立即扣减库存
   - 如果订单超时未支付，定时任务会自动取消订单并恢复库存

2. **库存恢复**：
   - 只有未支付订单取消时才恢复库存
   - 已支付订单取消或退款时不恢复库存（商品已卖出）

3. **SKU库存同步**：
   - 当SKU库存更新时，会自动汇总更新 `product.stock`
   - 当商品库存更新时，不会自动分配到SKU（避免覆盖用户设置的SKU库存）

4. **ERP库存推送**：
   - 无SKU商品：推送 `product.stock`
   - 有SKU商品：推送每个SKU的 `product_sku.stock`

## 测试建议

1. **订单创建测试**：
   - 创建订单时验证可用库存检查是否正常
   - 验证未支付订单是否影响可用库存计算

2. **支付流程测试**：
   - 支付成功时验证库存是否正确扣减
   - 验证SKU库存和商品库存是否同步更新

3. **订单取消测试**：
   - 未支付订单取消：验证无需恢复库存
   - 已支付订单取消：验证库存是否正确恢复

4. **库存管理测试**：
   - 库存调整功能是否正常
   - 库存查询和统计是否准确

5. **ERP推送测试**：
   - 验证库存同步到ERP是否正常
   - 验证有SKU和无SKU商品的库存推送是否正确

## 回滚方案

如果重构后出现问题，可以按以下步骤回滚：

1. 恢复 `product_stock` 表结构
2. 从备份恢复 `product_stock` 表数据
3. 恢复相关代码文件（使用Git回滚）
4. 重新部署应用

## 完成时间

2026-01-15

