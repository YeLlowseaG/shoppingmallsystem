-- 帮助中心数据库表设计
-- 创建时间: 2025-12-10

-- 1. 帮助中心分类表
CREATE TABLE IF NOT EXISTS `help_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '父分类ID（0表示顶级分类）',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `sort` INT(11) NOT NULL DEFAULT 0 COMMENT '排序（数字越小越靠前）',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帮助中心分类表';

-- 2. 帮助中心文章表
CREATE TABLE IF NOT EXISTS `help_article` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` BIGINT(20) NOT NULL COMMENT '分类ID',
  `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
  `content` LONGTEXT NOT NULL COMMENT '文章内容（HTML格式，支持富文本和图片）',
  `images` TEXT COMMENT '图片URL数组（JSON格式：["url1","url2"]）',
  `sort` INT(11) NOT NULL DEFAULT 0 COMMENT '排序（数字越小越靠前）',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帮助中心文章表';

-- 初始化帮助中心分类数据
INSERT INTO `help_category` (`id`, `parent_id`, `name`, `sort`, `status`) VALUES
(1, 0, '新手上路', 1, 1),
(2, 0, '购物指南', 2, 1),
(3, 0, '支付/配送方式', 3, 1),
(4, 0, '购物条款', 4, 1),
(5, 0, '代销会员使用帮助', 5, 1),
(6, 0, '批发会员使用帮助', 6, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `sort` = VALUES(`sort`);

-- 初始化帮助中心文章数据（示例）
INSERT INTO `help_article` (`id`, `category_id`, `title`, `content`, `images`, `sort`, `status`) VALUES
(1, 1, '顾客必读', '<h3>如何订购商品?</h3><p>您可以通过网站浏览商品并直接下单，也可以联系客服进行订购。</p>', '[]', 1, 1),
(2, 1, '会员等级折扣', '<h3>会员等级说明</h3><p>我们的会员分为多个等级，不同等级享受不同的折扣优惠。</p>', '[]', 2, 1),
(3, 2, '简单的购物流程', '<h3>购物流程</h3><p>我们的购物流程非常简单，只需几个步骤即可完成。</p>', '[]', 1, 1),
(4, 3, '支付方式', '<h3>支持的支付方式</h3><p>我们支持多种支付方式，包括在线支付、预存款支付等。</p>', '[]', 1, 1)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `content` = VALUES(`content`), `sort` = VALUES(`sort`);

