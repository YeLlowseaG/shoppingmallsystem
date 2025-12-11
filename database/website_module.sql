-- ============================================
-- Website模块数据库表
-- 用于管理网站内容配置（轮播图、品牌、广告位等）
-- ============================================

USE `chengren_shopping_mall`;

-- ============================================
-- 轮播图表
-- ============================================
CREATE TABLE IF NOT EXISTS `website_banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(100) NOT NULL COMMENT '轮播图标题',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `link_type` TINYINT NOT NULL DEFAULT 0 COMMENT '链接类型（0-无链接，1-商品分类，2-商品详情，3-促销活动，4-外部链接）',
  `link_value` VARCHAR(200) DEFAULT NULL COMMENT '链接值（根据link_type不同，存储categoryId/productId/promotionId/url）',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序（数字越小越靠前）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='轮播图表';

-- ============================================
-- 品牌表
-- ============================================
CREATE TABLE IF NOT EXISTS `website_brand` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `brand_name` VARCHAR(50) NOT NULL COMMENT '品牌名称',
  `logo_url` VARCHAR(500) NOT NULL COMMENT 'Logo图片URL',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '品牌描述',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序（数字越小越靠前）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='品牌表';

-- ============================================
-- 广告位表
-- ============================================
CREATE TABLE IF NOT EXISTS `website_advertisement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ad_name` VARCHAR(100) NOT NULL COMMENT '广告名称',
  `ad_position` VARCHAR(50) NOT NULL COMMENT '广告位置（floor_1/floor_2.../brand_side_1/brand_side_2）',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `link_type` TINYINT NOT NULL DEFAULT 0 COMMENT '链接类型（0-无链接，1-商品分类，2-商品详情，3-促销活动，4-外部链接）',
  `link_value` VARCHAR(200) DEFAULT NULL COMMENT '链接值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_position` (`ad_position`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='广告位表';

-- ============================================
-- 插入初始数据
-- ============================================

-- 插入轮播图示例数据
INSERT INTO `website_banner` (`title`, `image_url`, `link_type`, `link_value`, `sort_order`, `status`) VALUES
('新品上市', 'https://via.placeholder.com/1200x450/FF6B9D/ffffff?text=新品上市', 3, 'new', 1, 1),
('热销商品', 'https://via.placeholder.com/1200x450/9D50BB/ffffff?text=热销商品', 3, 'hot', 2, 1),
('特价活动', 'https://via.placeholder.com/1200x450/6C5CE7/ffffff?text=特价活动', 3, 'summer2024', 3, 1);

-- 插入品牌示例数据
INSERT INTO `website_brand` (`brand_name`, `logo_url`, `sort_order`, `status`) VALUES
('虚姬', 'https://via.placeholder.com/120x60/FF6B9D/ffffff?text=虚姬', 1, 1),
('Angus', 'https://via.placeholder.com/120x60/9D50BB/ffffff?text=Angus', 2, 1),
('杜蕾斯', 'https://via.placeholder.com/120x60/6C5CE7/ffffff?text=Durex', 3, 1),
('冈本', 'https://via.placeholder.com/120x60/FFD93D/ffffff?text=Okamoto', 4, 1),
('欧姿丹', 'https://via.placeholder.com/120x60/FD79A8/ffffff?text=欧姿丹', 5, 1),
('杰士邦', 'https://via.placeholder.com/120x60/74B9FF/ffffff?text=杰士邦', 6, 1),
('DESIRE', 'https://via.placeholder.com/120x60/A29BFE/ffffff?text=DESIRE', 7, 1),
('IC KISTOY', 'https://via.placeholder.com/120x60/FD79A8/ffffff?text=IC', 8, 1),
('Beten', 'https://via.placeholder.com/120x60/FF6B9D/ffffff?text=Beten', 9, 1),
('NO17', 'https://via.placeholder.com/120x60/6C5CE7/ffffff?text=NO17', 10, 1),
('W Dibe', 'https://via.placeholder.com/120x60/FFD93D/ffffff?text=Dibe', 11, 1),
('LE', 'https://via.placeholder.com/120x60/74B9FF/ffffff?text=LE', 12, 1),
('久慕之', 'https://via.placeholder.com/120x60/A29BFE/ffffff?text=久慕之', 13, 1),
('NU smile', 'https://via.placeholder.com/120x60/FD79A8/ffffff?text=NU', 14, 1),
('爱巢', 'https://via.placeholder.com/120x60/FF6B9D/ffffff?text=爱巢', 15, 1),
('百乐', 'https://via.placeholder.com/120x60/9D50BB/ffffff?text=百乐', 16, 1);

-- 插入广告位示例数据（首页楼层广告）
INSERT INTO `website_advertisement` (`ad_name`, `ad_position`, `image_url`, `link_type`, `link_value`, `sort_order`, `status`) VALUES
('1F楼层广告', 'floor_1', 'https://via.placeholder.com/800x400/FF6B9D/ffffff?text=Floor+1', 1, '5', 1, 1),
('2F楼层广告', 'floor_2', 'https://via.placeholder.com/800x400/9D50BB/ffffff?text=Floor+2', 1, '6', 1, 1),
('3F楼层广告', 'floor_3', 'https://via.placeholder.com/800x400/74B9FF/ffffff?text=Floor+3', 1, '7', 1, 1),
('4F楼层广告', 'floor_4', 'https://via.placeholder.com/800x400/FFD93D/ffffff?text=Floor+4', 1, '8', 1, 1),
('5F楼层广告', 'floor_5', 'https://via.placeholder.com/800x400/FD79A8/ffffff?text=Floor+5', 1, '9', 1, 1),
('6F楼层广告', 'floor_6', 'https://via.placeholder.com/800x400/55EFC4/ffffff?text=Floor+6', 1, '11', 1, 1),
('7F楼层广告', 'floor_7', 'https://via.placeholder.com/800x400/A29BFE/ffffff?text=Floor+7', 1, '12', 1, 1),
('品牌区侧边广告1', 'brand_side_1', 'https://via.placeholder.com/280x220/6C5CE7/ffffff?text=好货来袭', 3, 'promo1', 1, 1),
('品牌区侧边广告2', 'brand_side_2', 'https://via.placeholder.com/280x220/FF6B9D/ffffff?text=敬请期待', 3, 'promo2', 2, 1);
