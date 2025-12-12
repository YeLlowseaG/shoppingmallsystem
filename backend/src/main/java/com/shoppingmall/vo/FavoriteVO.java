package com.shoppingmall.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏VO
 */
@Data
public class FavoriteVO {
    
    /**
     * 收藏ID
     */
    private Long id;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品主图
     */
    private String mainImage;
    
    /**
     * 商品价格
     */
    private BigDecimal basePrice;
    
    /**
     * 用户等级价格
     */
    private BigDecimal userLevelPrice;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 销量
     */
    private Integer salesCount;
    
    /**
     * 商品状态
     */
    private String status;
    
    /**
     * 收藏时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}