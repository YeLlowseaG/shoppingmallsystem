package com.shoppingmall.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 商品保存事件
 * 当商品被保存时发布此事件
 *
 * @author ShoppingMall Team
 * @date 2025-01-14
 */
@Getter
public class ProductPublishedEvent extends ApplicationEvent {

    /**
     * 商品ID
     */
    private final Long productId;

    /**
     * 商品编码
     */
    private final String productCode;

    /**
     * 商品名称
     */
    private final String productName;

    public ProductPublishedEvent(Object source, Long productId, String productCode, String productName) {
        super(source);
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
    }
}
