package com.shoppingmall.service.erp.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.util.JushuitanHttpUtil;
import com.shoppingmall.entity.JushuitanConfig;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.mapper.JushuitanConfigMapper;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.sku.ProductSkuRepository;
import com.shoppingmall.service.erp.JushuitanInventoryService;
import com.shoppingmall.service.erp.JushuitanItemService;
import com.shoppingmall.vo.SyncResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聚水潭库存同步服务实现
 *
 * @author ShoppingMall Team
 * @date 2026-01-15
 */
@Slf4j
@Service
public class JushuitanInventoryServiceImpl implements JushuitanInventoryService {

    @Resource
    private JushuitanConfigMapper jushuitanConfigMapper;

    @Resource
    private ProductRepository productRepository;

    @Resource
    private ProductSkuRepository productSkuRepository;
    
    @Resource
    private com.shoppingmall.mapper.ProductSyncLogMapper productSyncLogMapper;
    
    @Resource
    @Lazy  // 延迟加载，避免循环依赖：JushuitanInventoryService -> JushuitanItemService -> ProductService -> StockService -> JushuitanInventoryService
    private JushuitanItemService jushuitanItemService;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 记录正在同步的商品ID，防止并发重复同步
    private static final Set<Long> syncingProductIds = ConcurrentHashMap.newKeySet();

    /**
     * 保存库存同步日志
     */
    private void saveSyncLog(Product product, String requestData, String responseData, 
                           String errorCode, String errorMessage, Integer syncStatus) {
        try {
            com.shoppingmall.entity.ProductSyncLog syncLog = new com.shoppingmall.entity.ProductSyncLog();
            syncLog.setProductId(product.getId());
            syncLog.setProductCode(product.getProductCode());
            syncLog.setProductName(product.getProductName());
            syncLog.setEnvType(getEnvType());
            syncLog.setSyncType("INVENTORY_SYNC"); // 库存同步类型
            syncLog.setRequestData(requestData);
            syncLog.setResponseData(responseData);
            syncLog.setErrorCode(errorCode);
            syncLog.setErrorMessage(errorMessage);
            syncLog.setSyncStatus(syncStatus);
            syncLog.setRetryCount(0);
            
            productSyncLogMapper.insert(syncLog);
            log.info("库存同步日志保存成功，商品ID: {}", product.getId());
        } catch (Exception e) {
            log.error("保存库存同步日志失败", e);
        }
    }
    
    /**
     * 获取环境类型
     */
    private String getEnvType() {
        try {
            JushuitanConfig config = getEnabledConfig();
            if (config != null && "production".equals(config.getEnvType())) {
                return "production";
            }
        } catch (Exception e) {
            log.error("获取环境类型失败", e);
        }
        return "test";
    }

    @Override
    public boolean syncInventory(Long productId) {
        try {
            log.info("===== 开始同步商品库存到聚水潭ERP =====");
            log.info("商品ID: {}", productId);

            // 1. 获取聚水潭配置
            JushuitanConfig config = getEnabledConfig();
            if (config == null) {
                log.error("聚水潭配置未启用");
                return false;
            }

            // 2. 获取商品信息
            Product product = productRepository.selectById(productId);
            if (product == null) {
                log.error("商品不存在: productId={}", productId);
                return false;
            }

            // 3. 检查商品是否有SKU
            List<ProductSku> skus = getProductSkus(productId);
            if (skus != null && !skus.isEmpty()) {
                // 有SKU的商品，同步每个SKU的库存
                log.info("商品有{}个SKU，同步每个SKU的库存", skus.size());
                for (ProductSku sku : skus) {
                    if (!syncSkuInventory(sku.getId())) {
                        log.warn("SKU {} 库存同步失败，但继续同步其他SKU", sku.getId());
                    }
                }
                return true;
            } else {
                // 无SKU的商品，直接同步商品库存
                log.info("商品无SKU，直接同步商品库存");
                return syncSingleProductInventory(productId, config, product);
            }

        } catch (Exception e) {
            log.error("同步商品库存到聚水潭失败: productId={}", productId, e);
            return false;
        }
    }

    @Override
    public int syncInventories(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            log.warn("商品ID列表为空，跳过批量同步");
            return 0;
        }

        int successCount = 0;
        for (Long productId : productIds) {
            if (syncInventory(productId)) {
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public boolean syncSkuInventory(Long skuId) {
        try {
            log.info("===== 开始同步SKU库存到聚水潭ERP =====");
            log.info("SKU ID: {}", skuId);

            // 1. 获取聚水潭配置
            JushuitanConfig config = getEnabledConfig();
            if (config == null) {
                log.error("聚水潭配置未启用");
                return false;
            }

            // 2. 获取SKU信息
            ProductSku sku = productSkuRepository.selectById(skuId);
            if (sku == null) {
                log.error("SKU不存在: skuId={}", skuId);
                return false;
            }

            // 3. 获取商品信息
            Product product = productRepository.selectById(sku.getProductId());
            if (product == null) {
                log.error("商品不存在: productId={}", sku.getProductId());
                return false;
            }

            // 4. 构建请求参数 - 对于SKU库存，直接从ProductSku获取库存
            Integer skuStock = sku.getStock() != null ? sku.getStock() : 0;
            Map<String, Object> request = buildInventoryRequest(config, product, sku, skuStock);
            String requestJson = objectMapper.writeValueAsString(request);
            log.info("库存同步请求参数: {}", requestJson);

            // 5. 发送请求到聚水潭
            String apiUrl = getInventoryApiUrl(config);
            String response = JushuitanHttpUtil.post(
                    apiUrl,
                    getAppKey(config),
                    getAppSecret(config),
                    getAccessToken(config),
                    null, // 库存接口不需要method参数
                    "biz", // 业务参数名，与商品资料同步接口保持一致
                    requestJson
            );

            // 6. 解析响应
            boolean success = parseResponse(response);
            
            // 7. 保存同步日志（与商品资料同步接口保持一致，只保存一次）
            if (success) {
                saveSyncLog(product, requestJson, response, null, null, 1);
            } else {
                // 解析错误码和错误信息，提供更详细的错误提示
                String errorCode = "SYNC_FAILED";
                String errorMessage = "库存同步失败";
                try {
                    Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
                    Integer code = (Integer) responseMap.get("code");
                    String msg = (String) responseMap.get("msg");
                    if (code != null && code == 190) {
                        errorCode = "API_PERMISSION_DENIED";
                        errorMessage = "无API权限，请前往聚水潭开放平台申请库存盘点接口（inventoryv2/upload）的API权限";
                    } else if (msg != null) {
                        errorMessage = msg;
                    }
                } catch (Exception e) {
                    log.warn("解析错误信息失败", e);
                }
                saveSyncLog(product, requestJson, response, errorCode, errorMessage, 0);
            }
            
            return success;

        } catch (Exception e) {
            log.error("同步SKU库存到聚水潭失败: skuId={}", skuId, e);
            
            // 尝试获取商品信息以便记录日志
            try {
                ProductSku sku = productSkuRepository.selectById(skuId);
                if (sku != null) {
                    Product product = productRepository.selectById(sku.getProductId());
                    if (product != null) {
                        saveSyncLog(product, null, null, "SYSTEM_ERROR", e.getMessage(), 0);
                    }
                }
            } catch (Exception logEx) {
                log.error("保存异常日志失败", logEx);
            }
            
            return false;
        }
    }

    @Override
    public int syncSkuInventories(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            log.warn("SKU ID列表为空，跳过批量同步");
            return 0;
        }

        int successCount = 0;
        for (Long skuId : skuIds) {
            if (syncSkuInventory(skuId)) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 同步单个无SKU商品的库存
     */
    private boolean syncSingleProductInventory(Long productId, JushuitanConfig config, Product product) {
        try {
            // 直接使用product.stock作为库存（不再使用product_stock表）
            Integer stock = product.getStock();
            if (stock == null) {
                stock = 0;
            }

            // 构建请求参数
            Map<String, Object> request = buildInventoryRequest(config, product, null, stock);
            String requestJson = objectMapper.writeValueAsString(request);
            log.info("库存同步请求参数: {}", requestJson);

            // 发送请求到聚水潭
            String apiUrl = getInventoryApiUrl(config);
            String response = JushuitanHttpUtil.post(
                    apiUrl,
                    getAppKey(config),
                    getAppSecret(config),
                    getAccessToken(config),
                    null, // 库存接口不需要method参数
                    "biz", // 业务参数名，与商品资料同步接口保持一致
                    requestJson
            );

            // 解析响应
            boolean success = parseResponse(response);
            
            // 保存同步日志（与商品资料同步接口保持一致，只保存一次）
            if (success) {
                saveSyncLog(product, requestJson, response, null, null, 1);
            } else {
                // 解析错误码和错误信息，提供更详细的错误提示
                String errorCode = "SYNC_FAILED";
                String errorMessage = "库存同步失败";
                try {
                    Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
                    Integer code = (Integer) responseMap.get("code");
                    String msg = (String) responseMap.get("msg");
                    if (code != null && code == 190) {
                        errorCode = "API_PERMISSION_DENIED";
                        errorMessage = "无API权限，请前往聚水潭开放平台申请库存盘点接口（inventoryv2/upload）的API权限";
                    } else if (msg != null) {
                        errorMessage = msg;
                    }
                } catch (Exception e) {
                    log.warn("解析错误信息失败", e);
                }
                saveSyncLog(product, requestJson, response, errorCode, errorMessage, 0);
            }
            
            return success;

        } catch (Exception e) {
            log.error("同步单个商品库存失败: productId={}", productId, e);
            saveSyncLog(product, null, null, "SYSTEM_ERROR", e.getMessage(), 0);
            return false;
        }
    }

    /**
     * 构建库存同步请求参数
     * @param config 聚水潭配置
     * @param product 商品信息
     * @param sku SKU信息（有SKU时传入）
     * @param stock 库存数量（无SKU商品时传入product.stock，有SKU时传入sku.stock）
     */
    private Map<String, Object> buildInventoryRequest(JushuitanConfig config, Product product, ProductSku sku, Integer stock) {
        Map<String, Object> request = new HashMap<>();

        // 外部单号，使用商品编码或SKU编码 + 时间戳
        String soId = (sku != null ? sku.getSkuCode() : product.getProductCode()) + "_" + System.currentTimeMillis();
        request.put("so_id", soId);

        // 仓库类型：主仓=1
        request.put("warehouse", "1");

        // 盘点类型：check（全量盘点）
        request.put("type", "check");

        // 注意：不传 wms_co_id 字段，让系统默认操作主仓
        // 如果传入 wms_co_id 但分仓与主仓不存在关联关系，会报错（错误码130）
        // 根据接口文档，wms_co_id 为可选字段，不填时默认操作主仓

        // 自动确认单据
        request.put("is_confirm", true);

        // 备注
        request.put("remark", "系统自动同步库存");

        // 商品明细列表
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();

        // SKU ID：使用商品编码或SKU编码作为聚水潭的sku_id - 与店铺商品资料上传保持一致
        String skuId = sku != null ? sku.getSkuCode() : product.getProductCode();
        if (sku != null && (skuId == null || skuId.isEmpty())) {
            skuId = product.getProductCode() + "_" + sku.getId();
        }
        item.put("sku_id", skuId);

        // 盘点数量：直接使用传入的stock参数（有SKU时是sku.stock，无SKU时是product.stock）
        Integer qty = stock != null ? stock : 0;
        item.put("qty", qty);

        // 批次号（可选）
        item.put("batch_id", "");

        // 有效期至（可选）
        item.put("expiration_date", "");

        // 生产日期（可选）
        item.put("produced_date", "");

        items.add(item);
        request.put("items", items);

        return request;
    }

    /**
     * 获取商品的SKU列表
     */
    private List<ProductSku> getProductSkus(Long productId) {
        QueryWrapper<ProductSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.eq("status", 1); // 只查询启用的SKU
        return productSkuRepository.selectList(queryWrapper);
    }

    // 已移除getProductStock方法，不再使用product_stock表

    /**
     * 获取聚水潭库存盘点接口地址
     */
    private String getInventoryApiUrl(JushuitanConfig config) {
        if ("test".equals(config.getEnvType())) {
            // 测试环境
            return "https://dev-api.jushuitan.com/open/jushuitan/inventoryv2/upload";
        } else {
            // 生产环境
            return "https://openapi.jushuitan.com/open/jushuitan/inventoryv2/upload";
        }
    }

    /**
     * 获取启用的聚水潭配置
     */
    private JushuitanConfig getEnabledConfig() {
        QueryWrapper<JushuitanConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("enabled", 1);
        return jushuitanConfigMapper.selectOne(queryWrapper);
    }

    /**
     * 获取AppKey
     */
    private String getAppKey(JushuitanConfig config) {
        if ("test".equals(config.getEnvType())) {
            return config.getTestAppKey();
        } else {
            return config.getAppKey();
        }
    }

    /**
     * 获取AppSecret
     */
    private String getAppSecret(JushuitanConfig config) {
        if ("test".equals(config.getEnvType())) {
            return config.getTestAppSecret();
        } else {
            return config.getAppSecret();
        }
    }

    /**
     * 获取AccessToken
     */
    private String getAccessToken(JushuitanConfig config) {
        if ("test".equals(config.getEnvType())) {
            return config.getTestAccessToken();
        } else {
            return config.getAccessToken();
        }
    }

    /**
     * 获取ShopId
     */
    private String getShopId(JushuitanConfig config) {
        if ("test".equals(config.getEnvType())) {
            return config.getTestShopId();
        } else {
            return config.getShopId();
        }
    }

    /**
     * 解析聚水潭响应
     */
    private boolean parseResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(response, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

            // 检查响应状态
            Integer code = (Integer) result.get("code");
            Boolean isSuccess = (Boolean) result.get("issuccess");

            // 聚水潭API返回成功的情况
            if (code != null && code == 0) {
                log.info("聚水潭库存同步成功");
                return true;
            } else {
                // 根据不同的错误码或issuccess字段判断失败情况
                String msg = (String) result.get("msg");
                
                // 特殊处理API权限错误（错误码190）
                if (code != null && code == 190) {
                    log.error("聚水潭库存同步失败: code={}, msg={}。请前往聚水潭开放平台申请库存盘点接口（inventoryv2/upload）的API权限", code, msg);
                } else {
                    log.error("聚水潭库存同步失败: code={}, msg={}, isSuccess={}", code, msg, isSuccess);
                }
                return false;
            }
        } catch (Exception e) {
            log.error("解析聚水潭响应失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public SyncResult syncProductAndInventory(Long productId) {
        SyncResult result = new SyncResult();
        result.setProductId(productId);
        
        // 防重复同步检查：检查最近30秒内是否有同步记录（无论成功失败）
        LocalDateTime thirtySecondsAgo = LocalDateTime.now().minusSeconds(30);
        QueryWrapper<com.shoppingmall.entity.ProductSyncLog> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("product_id", productId);
        checkWrapper.in("sync_type", "UPLOAD_ITEM", "UPLOAD_SHOP_ITEM", "INVENTORY_SYNC");
        // 不限制sync_status，检查所有同步记录（成功和失败都算）
        checkWrapper.ge("create_time", thirtySecondsAgo);
        checkWrapper.orderByDesc("create_time");
        checkWrapper.last("LIMIT 1");
        
        List<com.shoppingmall.entity.ProductSyncLog> recentLogs = productSyncLogMapper.selectList(checkWrapper);
        if (recentLogs != null && !recentLogs.isEmpty()) {
            com.shoppingmall.entity.ProductSyncLog recentLog = recentLogs.get(0);
            log.warn("商品{}在最近30秒内已经同步过（同步类型: {}, 同步状态: {}, 同步时间: {}），跳过重复同步", 
                    productId, recentLog.getSyncType(), 
                    recentLog.getSyncStatus() == 1 ? "成功" : "失败",
                    recentLog.getCreateTime());
            result.setSuccess(false);
            result.setMessage("该商品在最近30秒内已经同步过，请勿重复同步");
            result.setItemSyncSuccess(recentLog.getSyncType().contains("ITEM") && recentLog.getSyncStatus() == 1);
            result.setInventorySyncSuccess("INVENTORY_SYNC".equals(recentLog.getSyncType()) && recentLog.getSyncStatus() == 1);
            return result;
        }
        
        // 使用Set防止并发调用重复同步
        if (!syncingProductIds.add(productId)) {
            log.warn("商品{}正在同步中，跳过重复调用", productId);
            result.setSuccess(false);
            result.setMessage("该商品正在同步中，请勿重复调用");
            return result;
        }
        
        try {
            // 步骤1: 同步商品资料
            log.info("===== 开始完整同步流程: 商品资料 + 库存 =====");
            log.info("商品ID: {}", productId);
            
            log.info("步骤1: 开始同步商品资料: productId={}", productId);
            boolean itemSuccess = jushuitanItemService.uploadItem(productId);
            
            if (!itemSuccess) {
                log.error("商品资料同步失败，终止同步流程: productId={}", productId);
                result.setSuccess(false);
                result.setItemSyncSuccess(false);
                result.setStep("ITEM_SYNC");
                result.setMessage("商品资料同步失败，无法继续同步库存");
                // 注意：这里不需要手动移除 syncingProductIds，因为 finally 块会处理
                return result;
            }
            
            log.info("商品资料同步成功: productId={}", productId);
            result.setItemSyncSuccess(true);
            
            // 商品资料同步成功后，等待5秒，确保ERP系统已处理完商品资料
            log.info("商品资料同步成功，等待5秒后开始同步库存: productId={}", productId);
            try {
                Thread.sleep(5000L); // 等待5秒
                log.info("等待完成，开始同步库存: productId={}", productId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待被中断: productId={}", productId);
            }
            
            // 步骤2: 同步库存（带重试机制）
            log.info("步骤2: 开始同步库存: productId={}", productId);
            boolean inventorySuccess = syncInventoryWithRetry(productId, 3, result);
            
            if (inventorySuccess) {
                result.setSuccess(true);
                result.setMessage("商品资料和库存同步成功");
                log.info("完整同步流程成功: productId={}", productId);
            } else {
                result.setSuccess(false);
                result.setStep("INVENTORY_SYNC");
                result.setMessage("商品资料同步成功，但库存同步失败，请尝试人工操作ERP库存同步！");
                log.warn("完整同步流程部分失败: productId={}, 商品资料成功，库存失败", productId);
            }
            
            result.setInventorySyncSuccess(inventorySuccess);
            return result;
            
        } catch (Exception e) {
            log.error("同步商品和库存异常: productId={}", productId, e);
            result.setSuccess(false);
            result.setMessage("同步过程发生异常: " + e.getMessage());
            result.setErrorDetail(e.getMessage());
            return result;
        } finally {
            // 同步完成后，从Set中移除
            syncingProductIds.remove(productId);
        }
    }

    /**
     * 同步库存（带重试机制）
     * 
     * @param productId 商品ID
     * @param maxRetries 最大重试次数（不包括首次尝试）
     * @param result 同步结果对象（用于记录重试次数）
     * @return 是否成功
     */
    private boolean syncInventoryWithRetry(Long productId, int maxRetries, SyncResult result) {
        int attempt = 0;
        Exception lastException = null;
        String lastErrorDetail = null;
        
        // 总共尝试 maxRetries + 1 次（1次初始 + maxRetries次重试）
        while (attempt <= maxRetries) {
            try {
                if (attempt > 0) {
                    log.info("库存同步重试第{}次: productId={}", attempt, productId);
                    // 重试前等待，避免频繁请求（递增等待时间：1s, 2s, 3s）
                    Thread.sleep(1000L * attempt);
                } else {
                    log.info("库存同步首次尝试: productId={}", productId);
                }
                
                boolean success = syncInventory(productId);
                
                if (success) {
                    log.info("库存同步成功: productId={}, 尝试次数={}", productId, attempt + 1);
                    result.setInventoryRetryCount(attempt);
                    return true;
                } else {
                    log.warn("库存同步失败: productId={}, 尝试次数={}", productId, attempt + 1);
                    lastErrorDetail = "库存同步失败（尝试次数: " + (attempt + 1) + "）";
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("库存同步重试等待被中断: productId={}", productId, e);
                lastException = e;
                lastErrorDetail = "重试等待被中断: " + e.getMessage();
                break; // 中断时退出循环
            } catch (Exception e) {
                log.error("库存同步异常: productId={}, 尝试次数={}", productId, attempt + 1, e);
                lastException = e;
                lastErrorDetail = "同步异常: " + e.getMessage();
            }
            
            attempt++;
        }
        
        // 所有尝试都失败
        log.error("库存同步最终失败: productId={}, 总尝试次数={}", productId, maxRetries + 1);
        result.setInventoryRetryCount(maxRetries + 1);
        if (lastErrorDetail != null) {
            result.setErrorDetail(lastErrorDetail);
        }
        if (lastException != null) {
            log.error("最后一次异常信息", lastException);
        }
        
        return false;
    }
}