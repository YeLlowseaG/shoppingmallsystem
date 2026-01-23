package com.shoppingmall.service.product;

import com.shoppingmall.entity.Product;
import com.shoppingmall.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 商品描述异步更新服务
 * 独立服务类，确保@Async注解生效
 * 
 * @author ShoppingMall Team
 * @date 2026-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDescriptionAsyncService {

    private final ProductRepository productRepository;

    /**
     * 异步更新商品描述字段
     * 将description字段的保存与基本信息分离，提升保存接口响应速度
     * 
     * @param productId 商品ID
     * @param description 商品描述（富文本，可能包含大量图片数据）
     */
    @Async("taskExecutor")
    public void updateProductDescriptionAsync(Long productId, String description) {
        try {
            if (productId == null) {
                log.warn("商品ID为空，无法更新description字段");
                return;
            }
            
            if (description == null) {
                log.debug("商品{}的description为空，跳过更新", productId);
                return;
            }
            
            log.info("开始异步更新商品{}的description字段，内容长度: {} 字符", productId, description.length());
            
            Product product = productRepository.selectById(productId);
            if (product == null) {
                log.warn("商品{}不存在，无法更新description字段", productId);
                return;
            }
            
            product.setDescription(description);
            productRepository.updateById(product);
            
            log.info("商品{}的description字段已异步更新完成", productId);
        } catch (Exception e) {
            log.error("异步更新商品{}的description字段失败", productId, e);
            // 异步更新失败不影响主流程，只记录错误日志
        }
    }
}


