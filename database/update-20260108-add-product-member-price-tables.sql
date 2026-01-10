-- ----------------------------
-- 会员价逻辑修改：创建商品和SKU会员价关联表
-- Date: 2026-01-08
-- Description: 支持按会员等级设置不同的会员价，移除折扣率计算逻辑
-- ----------------------------

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for product_member_price
-- ----------------------------
DROP TABLE IF EXISTS `product_member_price`;
CREATE TABLE `product_member_price` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `member_level_id` bigint NOT NULL COMMENT '会员等级ID',
  `member_price` decimal(10,2) NOT NULL COMMENT '会员价',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_level` (`product_id`, `member_level_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_member_level_id` (`member_level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品会员价关联表';

-- ----------------------------
-- Table structure for product_sku_member_price
-- ----------------------------
DROP TABLE IF EXISTS `product_sku_member_price`;
CREATE TABLE `product_sku_member_price` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `member_level_id` bigint NOT NULL COMMENT '会员等级ID',
  `member_price` decimal(10,2) NOT NULL COMMENT '会员价',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_level` (`sku_id`, `member_level_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_member_level_id` (`member_level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='SKU会员价关联表';

SET FOREIGN_KEY_CHECKS=1;

