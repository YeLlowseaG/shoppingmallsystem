-- 公告数据库表设计
-- 创建时间: 2025-12-10
-- 更新说明: 创建公告表，支持富文本内容和图片

-- 公告表
CREATE TABLE IF NOT EXISTS `announcement` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` LONGTEXT NOT NULL COMMENT '公告内容（HTML格式，支持富文本和图片）',
  `images` TEXT COMMENT '图片URL数组（JSON格式：["url1","url2"]）',
  `publish_date` DATE NOT NULL COMMENT '发布日期',
  `sort` INT(11) NOT NULL DEFAULT 0 COMMENT '排序（数字越小越靠前）',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_publish_date` (`publish_date`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告表';

-- 初始化公告测试数据
INSERT INTO `announcement` (`id`, `title`, `content`, `images`, `publish_date`, `sort`, `status`) VALUES
(1, '安全套税率调整及价格变动的通知', 
 '<p>尊敬的客户：</p><p>您好！衷心感谢您长期以来对公司的信任与支持，近期，根据国家最新颁布的税收政策法规，自2026年1月1日起，避孕套由免征税调整为征收13%的增值税。鉴于税率调整这一情况，公司商城在售的避孕套产品价格也将相应做出改变。在收到各品牌方关于价格调整的具体通知后，公司会对商城内避孕套产品的价格进行合理调整。为了避免因价格调整给您带来不必要的困扰，建议您在后续选购避孕套产品时，密切关注商城发布的最新公告。公告中会详细说明各品牌、各款式避孕套调整后的具体价格信息，方便您提前做好购物规划。再次感谢您的理解与支持。</p>', 
 '[]', 
 '2025-12-10', 
 1, 
 1),
(2, '冈本品牌价格调整通知', 
 '<p>尊敬的客户：</p><p>您好！冈本品牌部分产品价格将于2025年12月15日起进行调整，具体调整信息请关注商城公告。</p>', 
 '[]', 
 '2025-12-08', 
 2, 
 1),
(3, '杜蕾斯部分产品下架说明', 
 '<p>尊敬的客户：</p><p>您好！由于产品更新换代，杜蕾斯部分旧款产品将于2025年12月20日起下架，新款产品将陆续上架，敬请关注。</p>', 
 '[]', 
 '2025-12-05', 
 3, 
 1),
(4, '拼多多平台价格限制通知', 
 '<p>尊敬的客户：</p><p>您好！根据拼多多平台政策要求，部分商品在拼多多平台的价格将进行调整，具体调整信息请关注商城公告。</p>', 
 '[]', 
 '2025-12-01', 
 4, 
 1),
(5, '临时暂停发货通知', 
 '<p>尊敬的客户：</p><p>您好！由于仓库盘点，2025年11月30日至12月2日期间将暂停发货，12月3日起恢复正常发货。给您带来的不便，敬请谅解。</p>', 
 '[]', 
 '2025-11-28', 
 5, 
 1),
(6, '新品牌入驻公告', 
 '<p>尊敬的客户：</p><p>您好！我们很高兴地宣布，新品牌将于2025年12月1日正式入驻商城，更多优质商品等待您的选购。</p>', 
 '[]', 
 '2025-11-25', 
 6, 
 1)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `content` = VALUES(`content`), `publish_date` = VALUES(`publish_date`);

