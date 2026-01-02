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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

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

            // 3. 转换为聚水潭格式
            JushuitanItemDTO itemDTO = convertToJushuitanItem(productVO);

            // 4. 构建请求参数 - items参数的值是包含items数组的JSON对象
            Map<String, Object> itemsData = new HashMap<>();
            itemsData.put("items", Collections.singletonList(itemDTO));
            String itemsJson = objectMapper.writeValueAsString(itemsData);

            // 5. 调用聚水潭API - 使用通用接口 + method参数
            String apiUrl = "test".equals(config.getEnvType()) ? config.getTestApiUrl() : config.getApiUrl();

            String appKey = getAppKey(config);
            String appSecret = getAppSecret(config);
            String accessToken = getAccessToken(config);

            log.info("API地址: {}", apiUrl);
            log.info("App Key: {}", appKey);
            log.info("商品数据: {}", itemsJson);

            String response = JushuitanHttpUtil.post(
                apiUrl,
                appKey,
                appSecret,
                accessToken,
                "jushuitan.itemsku.upload",  // method参数
                "items",  // 参数名
                itemsJson  // items参数值: {"items":[...]}
            );

            log.info("聚水潭API响应: {}", response);

            // 6. 解析响应
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            Integer code = (Integer) responseMap.get("code");

            if (code != null && code == 0) {
                // 检查是否有错误数据
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseMap.get("data");
                if (dataList == null || dataList.isEmpty()) {
                    log.info("商品上传成功: productId={}, skuId={}", productId, itemDTO.getSkuId());
                    saveSyncLog(syncLog, itemsJson, response, null, null, 1);
                    return true;
                } else {
                    // 有错误信息
                    String errorMsg = "";
                    for (Map<String, Object> errorData : dataList) {
                        String msg = (String) errorData.get("message");
                        errorMsg += msg + ";";
                        log.error("商品上传部分失败: skuId={}, error={}", errorData.get("sku_id"), msg);
                    }
                    saveSyncLog(syncLog, itemsJson, response, String.valueOf(code), errorMsg, 0);
                    return false;
                }
            } else {
                String msg = (String) responseMap.get("msg");
                log.error("商品上传失败: code={}, msg={}", code, msg);
                saveSyncLog(syncLog, itemsJson, response, String.valueOf(code), msg, 0);
                return false;
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
     * 将商品转换为聚水潭格式
     */
    private JushuitanItemDTO convertToJushuitanItem(ProductVO product) {
        JushuitanItemDTO dto = new JushuitanItemDTO();

        // 三个必填字段
        dto.setSkuId(String.valueOf(product.getId()));  // 商品编码（必填）
        dto.setIId(product.getProductCode());  // 款式编码（必填）
        dto.setName(product.getProductName());  // 商品名称（必填）

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
