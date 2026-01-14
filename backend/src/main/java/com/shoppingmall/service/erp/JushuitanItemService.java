package com.shoppingmall.service.erp;

import java.util.List;

/**
 * 聚水潭商品同步服务
 *
 * @author ShoppingMall Team
 * @date 2025-12-31
 */
public interface JushuitanItemService {

    /**
     * 上传单个商品到聚水潭
     *
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean uploadItem(Long productId);

    /**
     * 批量上传商品到聚水潭
     *
     * @param productIds 商品ID列表（最多500个）
     * @return 成功数量
     */
    int uploadItems(List<Long> productIds);

    /**
     * 上传商品SKU到聚水潭
     *
     * @param skuId SKU ID
     * @return 是否成功
     */
    boolean uploadSku(Long skuId);

    /**
     * 批量上传商品SKU到聚水潭
     *
     * @param skuIds SKU ID列表（最多500个）
     * @return 成功数量
     */
    int uploadSkus(List<Long> skuIds);

    /**
     * 上传店铺商品资料到聚水潭
     *
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean uploadShopItem(Long productId);

    /**
     * 批量上传店铺商品资料到聚水潭
     *
     * @param productIds 商品ID列表（最多50个）
     * @return 成功数量
     */
    int uploadShopItems(List<Long> productIds);

    /**
     * 上传SKU的店铺商品资料到聚水潭
     *
     * @param skuId SKU ID
     * @return 是否成功
     */
    boolean uploadShopSku(Long skuId);

    /**
     * 批量上传SKU的店铺商品资料到聚水潭
     *
     * @param skuIds SKU ID列表（最多50个）
     * @return 成功数量
     */
    int uploadShopSkus(List<Long> skuIds);
}
