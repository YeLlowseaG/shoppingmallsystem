package com.shoppingmall.service.erp.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.util.JushuitanHttpUtil;
import com.shoppingmall.dto.JushuitanItemDTO;
import com.shoppingmall.entity.JushuitanConfig;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.entity.ProductSyncLog;
import com.shoppingmall.mapper.JushuitanConfigMapper;
import com.shoppingmall.mapper.ProductSyncLogMapper;
import com.shoppingmall.service.erp.JushuitanItemService;
import com.shoppingmall.service.product.ProductService;
import com.shoppingmall.service.sku.ProductSkuService;
import com.shoppingmall.vo.ProductVO;
import com.shoppingmall.vo.ProductSkuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 聚水潭商品同步服务实现
 *
 * @author ShoppingMall Team
 * @date 2025-12-31
 */
@Slf4j
@Service
public class JushuitanItemServiceImpl implements JushuitanItemService {

    @Resource
    private ProductService productService;

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private JushuitanConfigMapper jushuitanConfigMapper;

    @Resource
    private ProductSyncLogMapper productSyncLogMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean uploadItem(Long productId) {
        ProductSyncLog syncLog = new ProductSyncLog();
        syncLog.setProductId(productId);
        syncLog.setSyncType("UPLOAD_ITEM");
        syncLog.setSyncStatus(2); // 处理中
        syncLog.setRetryCount(0);

        try {
            log.info("===== 开始上传商品到聚水潭 =====");
            log.info("商品ID: {}", productId);

            // 1. 获取聚水潭配置
            JushuitanConfig config = getEnabledConfig();
            if (config == null) {
                log.error("聚水潭配置未启用");
                saveSyncLog(syncLog, null, null, "CONFIG_ERROR", "聚水潭配置未启用", 0);
                return false;
            }

            // 记录环境类型
            syncLog.setEnvType(config.getEnvType());

            // 2. 获取商品信息
            ProductVO productVO = productService.getProductById(productId, null);
            if (productVO == null) {
                log.error("商品不存在: productId={}", productId);
                saveSyncLog(syncLog, null, null, "PRODUCT_NOT_FOUND", "商品不存在", 0);
                return false;
            }

            // 记录商品信息
            syncLog.setProductCode(productVO.getProductCode());
            syncLog.setProductName(productVO.getProductName());

            // 3. 判断是否启用规格，如果启用规格则获取SKU列表，每个SKU单独上传
            boolean enableSpec = productVO.getEnableSpec() != null && productVO.getEnableSpec() == 1;
            List<ProductSkuVO> skuList = null;
            
            if (enableSpec) {
                // 获取SKU列表
                skuList = productSkuService.getSkusByProductId(productId, null);
                if (skuList == null || skuList.isEmpty()) {
                    log.warn("商品启用了规格但SKU列表为空: productId={}", productId);
                    // 如果没有SKU，按普通商品处理
                    enableSpec = false;
                }
            }

            // 4. 准备API配置
            String apiUrl;
            if ("test".equals(config.getEnvType())) {
                apiUrl = "https://dev-api.jushuitan.com/open/jushuitan/itemsku/upload";
            } else {
                apiUrl = "https://openapi.jushuitan.com/open/jushuitan/itemsku/upload";
            }

            String appKey = getAppKey(config);
            String appSecret = getAppSecret(config);
            String accessToken = getAccessToken(config);

            log.info("API地址: {}", apiUrl);
            log.info("App Key: {}", appKey);
            log.info("商品是否启用规格: {}", enableSpec);

            // 5. 如果启用规格，每个SKU单独上传
            if (enableSpec && skuList != null && !skuList.isEmpty()) {
                int successCount = 0;
                int totalCount = skuList.size();
                StringBuilder allErrorMsg = new StringBuilder();
                
                // 用于收集所有SKU的请求和响应数据
                List<Map<String, Object>> allRequestData = new ArrayList<>();
                List<Map<String, Object>> allResponseData = new ArrayList<>();

                for (ProductSkuVO sku : skuList) {
                    try {
                        // 转换为聚水潭格式（带SKU信息）
                        JushuitanItemDTO itemDTO = convertToJushuitanItemWithSku(productVO, sku);

                        // 构建请求参数
                        Map<String, Object> itemsData = new HashMap<>();
                        itemsData.put("items", Collections.singletonList(itemDTO));
                        String bizJson = objectMapper.writeValueAsString(itemsData);

                        log.info("上传SKU: skuId={}, skuCode={}, 数据: {}", sku.getId(), sku.getSkuCode(), bizJson);

                        // 记录请求数据
                        Map<String, Object> requestRecord = new HashMap<>();
                        requestRecord.put("skuId", sku.getId());
                        requestRecord.put("skuCode", sku.getSkuCode());
                        requestRecord.put("request", itemsData);
                        allRequestData.add(requestRecord);

                        // 调用聚水潭API
                        String response = JushuitanHttpUtil.post(
                            apiUrl,
                            appKey,
                            appSecret,
                            accessToken,
                            null,
                            "biz",
                            bizJson
                        );

                        log.info("聚水潭API响应: {}", response);

                        // 记录响应数据
                        Map<String, Object> responseRecord = new HashMap<>();
                        responseRecord.put("skuId", sku.getId());
                        responseRecord.put("skuCode", sku.getSkuCode());
                        try {
                            Map<String, Object> responseMapObj = objectMapper.readValue(response, Map.class);
                            responseRecord.put("response", responseMapObj);
                        } catch (Exception e) {
                            // 如果解析失败，直接保存原始字符串
                            responseRecord.put("response", response);
                        }
                        allResponseData.add(responseRecord);

                        // 解析响应
                        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
                        Integer code = (Integer) responseMap.get("code");

                        if (code != null && code == 0) {
                            // 检查是否有错误数据
                            Map<String, Object> dataObj = (Map<String, Object>) responseMap.get("data");
                            if (dataObj != null && dataObj.containsKey("datas")) {
                                List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataObj.get("datas");
                                boolean hasError = false;
                                for (Map<String, Object> errorData : dataList) {
                                    Boolean isSuccess = (Boolean) errorData.get("is_success");
                                    if (isSuccess == null || !isSuccess) {
                                        hasError = true;
                                        String msg = (String) errorData.get("msg");
                                        allErrorMsg.append("SKU ").append(sku.getSkuCode())
                                                .append(": ").append(msg).append("; ");
                                        log.error("SKU上传失败: skuId={}, skuCode={}, error={}", 
                                                sku.getId(), sku.getSkuCode(), msg);
                                    }
                                }
                                if (!hasError) {
                                    successCount++;
                                    log.info("SKU上传成功: skuId={}, skuCode={}", sku.getId(), sku.getSkuCode());
                                }
                            } else {
                                successCount++;
                                log.info("SKU上传成功: skuId={}, skuCode={}", sku.getId(), sku.getSkuCode());
                            }
                        } else {
                            String msg = (String) responseMap.get("msg");
                            allErrorMsg.append("SKU ").append(sku.getSkuCode())
                                    .append(": ").append(msg).append("; ");
                            log.error("SKU上传失败: skuId={}, skuCode={}, code={}, msg={}", 
                                    sku.getId(), sku.getSkuCode(), code, msg);
                        }
                    } catch (Exception e) {
                        log.error("上传SKU到聚水潭失败: skuId={}, skuCode={}", sku.getId(), sku.getSkuCode(), e);
                        allErrorMsg.append("SKU ").append(sku.getSkuCode())
                                .append(": ").append(e.getMessage()).append("; ");
                        
                        // 即使异常也要记录请求数据
                        try {
                            JushuitanItemDTO itemDTO = convertToJushuitanItemWithSku(productVO, sku);
                            Map<String, Object> itemsData = new HashMap<>();
                            itemsData.put("items", Collections.singletonList(itemDTO));
                            Map<String, Object> requestRecord = new HashMap<>();
                            requestRecord.put("skuId", sku.getId());
                            requestRecord.put("skuCode", sku.getSkuCode());
                            requestRecord.put("request", itemsData);
                            requestRecord.put("error", e.getMessage());
                            allRequestData.add(requestRecord);
                        } catch (Exception ex) {
                            log.warn("记录异常SKU请求数据失败", ex);
                        }
                    }
                }

                // 将所有请求和响应数据合并为JSON字符串
                String requestDataJson = null;
                String responseDataJson = null;
                try {
                    if (!allRequestData.isEmpty()) {
                        requestDataJson = objectMapper.writeValueAsString(allRequestData);
                    }
                    if (!allResponseData.isEmpty()) {
                        responseDataJson = objectMapper.writeValueAsString(allResponseData);
                    }
                } catch (Exception e) {
                    log.error("序列化请求/响应数据失败", e);
                }

                // 保存同步日志
                String errorMsg = allErrorMsg.length() > 0 ? allErrorMsg.toString() : null;
                int syncStatus = (successCount == totalCount) ? 1 : 0;
                saveSyncLog(syncLog, requestDataJson, responseDataJson, 
                        syncStatus == 1 ? null : "PARTIAL_FAILURE", 
                        errorMsg, syncStatus);

                log.info("商品SKU批量上传完成: productId={}, total={}, success={}", 
                        productId, totalCount, successCount);
                return successCount == totalCount;
            } else {
                // 未启用规格，按普通商品上传
                JushuitanItemDTO itemDTO = convertToJushuitanItem(productVO);

                // 构建请求参数
                Map<String, Object> itemsData = new HashMap<>();
                itemsData.put("items", Collections.singletonList(itemDTO));
                String bizJson = objectMapper.writeValueAsString(itemsData);

                log.info("商品数据(biz): {}", bizJson);

                String response = JushuitanHttpUtil.post(
                    apiUrl,
                    appKey,
                    appSecret,
                    accessToken,
                    null,
                    "biz",
                    bizJson
                );

                log.info("聚水潭API响应: {}", response);

                // 解析响应
                Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
                Integer code = (Integer) responseMap.get("code");

                if (code != null && code == 0) {
                    // 检查是否有错误数据
                    Map<String, Object> dataObj = (Map<String, Object>) responseMap.get("data");
                    if (dataObj != null && dataObj.containsKey("datas")) {
                        List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataObj.get("datas");
                        if (dataList == null || dataList.isEmpty()) {
                            log.info("商品上传成功: productId={}, skuId={}", productId, itemDTO.getSkuId());
                            saveSyncLog(syncLog, bizJson, response, null, null, 1);
                            return true;
                        } else {
                            // 检查是否有失败的数据
                            boolean hasError = false;
                            String errorMsg = "";
                            for (Map<String, Object> errorData : dataList) {
                                Boolean isSuccess = (Boolean) errorData.get("is_success");
                                if (isSuccess == null || !isSuccess) {
                                    hasError = true;
                                    String msg = (String) errorData.get("msg");
                                    errorMsg += msg + ";";
                                    log.error("商品上传部分失败: skuId={}, error={}", errorData.get("sku_id"), msg);
                                }
                            }
                            if (hasError) {
                                saveSyncLog(syncLog, bizJson, response, String.valueOf(code), errorMsg, 0);
                                return false;
                            } else {
                                log.info("商品上传成功: productId={}, skuId={}", productId, itemDTO.getSkuId());
                                saveSyncLog(syncLog, bizJson, response, null, null, 1);
                                return true;
                            }
                        }
                    } else {
                        // 没有data或datas字段，认为成功
                        log.info("商品上传成功: productId={}, skuId={}", productId, itemDTO.getSkuId());
                        saveSyncLog(syncLog, bizJson, response, null, null, 1);
                        return true;
                    }
                } else {
                    String msg = (String) responseMap.get("msg");
                    log.error("商品上传失败: code={}, msg={}", code, msg);
                    saveSyncLog(syncLog, bizJson, response, String.valueOf(code), msg, 0);
                    return false;
                }
            }

        } catch (Exception e) {
            log.error("上传商品到聚水潭失败: productId={}", productId, e);
            saveSyncLog(syncLog, null, null, "EXCEPTION", e.getMessage(), 0);
            return false;
        }
    }

    /**
     * 保存同步日志
     */
    private void saveSyncLog(ProductSyncLog syncLog, String requestData, String responseData,
                            String errorCode, String errorMessage, Integer syncStatus) {
        syncLog.setRequestData(requestData);
        syncLog.setResponseData(responseData);
        syncLog.setErrorCode(errorCode);
        syncLog.setErrorMessage(errorMessage);
        syncLog.setSyncStatus(syncStatus);
        try {
            productSyncLogMapper.insert(syncLog);
        } catch (Exception e) {
            log.error("保存商品同步日志失败", e);
        }
    }

    @Override
    public int uploadItems(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }

        // 限制最多200个（聚水潭API限制）
        if (productIds.size() > 200) {
            log.warn("批量上传商品数量超过200个，将只上传前200个");
            productIds = productIds.subList(0, 200);
        }

        int successCount = 0;
        for (Long productId : productIds) {
            if (uploadItem(productId)) {
                successCount++;
            }
        }

        log.info("批量上传商品完成: total={}, success={}", productIds.size(), successCount);
        return successCount;
    }

    @Override
    public boolean uploadSku(Long skuId) {
        // TODO: 实现SKU级别的上传
        log.warn("SKU级别上传暂未实现: skuId={}", skuId);
        return false;
    }

    @Override
    public int uploadSkus(List<Long> skuIds) {
        // TODO: 实现SKU级别的批量上传
        log.warn("SKU级别批量上传暂未实现: skuIds={}", skuIds);
        return 0;
    }

    /**
     * 将商品转换为聚水潭格式（未启用规格的商品）
     */
    private JushuitanItemDTO convertToJushuitanItem(ProductVO product) {
        JushuitanItemDTO dto = new JushuitanItemDTO();

        // 三个必填字段
        dto.setSkuId(product.getProductCode());  // 商品编码（必填）- 使用productCode
        dto.setIId(product.getProductCode());  // 款式编码（必填）
        dto.setName(product.getProductName());  // 商品名称（必填）

        // brand字段：传品牌名称
        if (product.getBrandName() != null && !product.getBrandName().isEmpty()) {
            dto.setBrand(product.getBrandName());
        }

        // s_price字段：传商品的基础价
        if (product.getBasePrice() != null) {
            dto.setSPrice(product.getBasePrice());
        }

        // market_price字段：传商品的市场零售价
        if (product.getMarketRetailPrice() != null) {
            dto.setMarketPrice(product.getMarketRetailPrice());
        }

        // unit字段：传商品的计量单位
        if (product.getUnit() != null && !product.getUnit().isEmpty()) {
            dto.setUnit(product.getUnit());
        }

        // weight字段：未启用规格时，获取商品表(product)的重量字段数据，转换为kg传值
        if (product.getWeight() != null) {
            // 将g转换为kg（商品表weight字段单位：g）
            BigDecimal weightInKg = product.getWeight().divide(new BigDecimal("1000"), 3, BigDecimal.ROUND_HALF_UP);
            dto.setWeight(weightInKg);
        }

        // properties_value字段：未启用规格时不传

        return dto;
    }

    /**
     * 将商品和SKU转换为聚水潭格式（启用规格的商品）
     */
    private JushuitanItemDTO convertToJushuitanItemWithSku(ProductVO product, ProductSkuVO sku) {
        JushuitanItemDTO dto = new JushuitanItemDTO();

        // 三个必填字段
        // sku_id使用SKU编码，如果没有则使用商品编码+SKU ID
        String skuId = sku.getSkuCode();
        if (skuId == null || skuId.isEmpty()) {
            skuId = product.getProductCode() + "_" + sku.getId();
        }
        dto.setSkuId(skuId);
        dto.setIId(product.getProductCode());  // 款式编码（必填）
        dto.setName(product.getProductName());  // 商品名称（必填）

        // brand字段：传品牌名称
        if (product.getBrandName() != null && !product.getBrandName().isEmpty()) {
            dto.setBrand(product.getBrandName());
        }

        // s_price字段：如果启用SKU，优先使用SKU的基础价；否则使用商品的基础价
        if (sku.getPrice() != null) {
            dto.setSPrice(sku.getPrice());
        } else if (product.getBasePrice() != null) {
            dto.setSPrice(product.getBasePrice());
        }

        // market_price字段：如果启用SKU，则获取SKU的市场零售价字段值
        if (sku.getMarketRetailPrice() != null) {
            dto.setMarketPrice(sku.getMarketRetailPrice());
        } else if (product.getMarketRetailPrice() != null) {
            // 如果SKU没有市场零售价，使用商品的市场零售价
            dto.setMarketPrice(product.getMarketRetailPrice());
        }

        // unit字段：传商品的计量单位
        if (product.getUnit() != null && !product.getUnit().isEmpty()) {
            dto.setUnit(product.getUnit());
        }

        // weight字段：启用SKU时，优先读取product_sku表的重量字段，如果没有则使用商品表(product)的重量字段，转换为kg传值
        BigDecimal weightInGrams = null;
        if (sku.getWeight() != null) {
            // 优先使用product_sku表的weight字段（单位：g）
            weightInGrams = sku.getWeight();
        } else if (product.getWeight() != null) {
            // 如果SKU没有重量，使用商品表(product)的weight字段（单位：g）
            weightInGrams = product.getWeight();
        }
        if (weightInGrams != null) {
            // 将g转换为kg
            BigDecimal weightInKg = weightInGrams.divide(new BigDecimal("1000"), 3, BigDecimal.ROUND_HALF_UP);
            dto.setWeight(weightInKg);
        }

        // properties_value字段：如果启用商品规格，则获取"规格属性+规格值"拼接传输
        // 根据接口文档示例"properties_value": "蓝色;XXL"，格式为：值1;值2（用分号分隔）
        // 但用户要求"规格属性+规格值"，理解为：属性名:值1;属性名:值2
        // 按照用户要求，拼接为"属性:值"格式，例如"颜色:蓝色;尺码:XXL"
        if (sku.getSpecCombination() != null && !sku.getSpecCombination().isEmpty()) {
            try {
                // 解析specCombination JSON字符串，格式如：{"颜色":"蓝色","尺码":"XXL"}
                Map<String, String> specMap = objectMapper.readValue(
                    sku.getSpecCombination(), 
                    new TypeReference<Map<String, String>>() {}
                );
                
                // 按照用户要求"规格属性+规格值"，拼接为"属性:值"格式
                List<String> specParts = new ArrayList<>();
                for (Map.Entry<String, String> entry : specMap.entrySet()) {
                    // 格式：属性名:值，例如"颜色:蓝色"
                    specParts.add(entry.getKey() + ":" + entry.getValue());
                }
                
                if (!specParts.isEmpty()) {
                    // 用分号连接，例如"颜色:蓝色;尺码:XXL"
                    String propertiesValue = String.join(";", specParts);
                    dto.setPropertiesValue(propertiesValue);
                }
            } catch (Exception e) {
                log.warn("解析SKU规格组合失败: skuId={}, specCombination={}", sku.getId(), sku.getSpecCombination(), e);
                // 如果解析失败，不设置properties_value字段
            }
        }

        return dto;
    }

    /**
     * 将相对路径转换为绝对URL
     */
    private String convertToAbsoluteUrl(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        // 如果已经是完整URL，直接返回
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        // 拼接域名前缀 - 这里需要配置实际的域名
        // TODO: 从配置文件读取域名
        String domain = "http://localhost:8081";  // 临时使用本地地址，实际应该是真实域名

        if (path.startsWith("/")) {
            return domain + path;
        } else {
            return domain + "/" + path;
        }
    }

    /**
     * 获取启用的聚水潭配置
     */
    private JushuitanConfig getEnabledConfig() {
        QueryWrapper<JushuitanConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", 1);
        wrapper.last("LIMIT 1");
        return jushuitanConfigMapper.selectOne(wrapper);
    }

    /**
     * 获取API地址
     */
    private String getApiUrl(JushuitanConfig config) {
        if ("test".equals(config.getEnvType())) {
            return config.getTestApiUrl().replace("/api/open/query.aspx", "/open/webapi/itemapi");
        } else {
            return config.getApiUrl().replace("/api/open/query.aspx", "/open/webapi/itemapi");
        }
    }

    /**
     * 获取App Key
     */
    private String getAppKey(JushuitanConfig config) {
        if ("test".equals(config.getEnvType())) {
            return config.getTestAppKey();
        } else {
            return config.getAppKey();
        }
    }

    /**
     * 获取App Secret
     */
    private String getAppSecret(JushuitanConfig config) {
        if ("test".equals(config.getEnvType())) {
            return config.getTestAppSecret();
        } else {
            return config.getAppSecret();
        }
    }

    /**
     * 获取Access Token
     */
    private String getAccessToken(JushuitanConfig config) {
        if ("test".equals(config.getEnvType())) {
            return config.getTestAccessToken();
        } else {
            return config.getAccessToken();
        }
    }
}
