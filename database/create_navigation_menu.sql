-- 创建导航菜单配置表
DROP TABLE IF EXISTS `navigation_menu`;
CREATE TABLE `navigation_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `menu_name` varchar(100) NOT NULL COMMENT '菜单名称',
  `menu_url` varchar(200) NOT NULL COMMENT '菜单链接',
  `menu_type` varchar(50) NOT NULL DEFAULT 'link' COMMENT '菜单类型：link-直接链接，category-分类，brand-品牌，type-类型',
  `menu_params` varchar(500) COMMENT '菜单参数，JSON格式存储',
  `icon` varchar(100) COMMENT '菜单图标',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `target` varchar(20) DEFAULT '_self' COMMENT '打开方式：_self-当前窗口，_blank-新窗口',
  `description` varchar(500) COMMENT '菜单描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='导航菜单配置表';

-- 插入默认导航菜单数据
INSERT INTO `navigation_menu` (`menu_name`, `menu_url`, `menu_type`, `menu_params`, `sort_order`, `status`, `description`) VALUES
('首页', '/', 'link', NULL, 1, 1, '网站首页'),
('新品专区', '/products', 'type', '{"type": "new"}', 2, 1, '新品商品专区'),
('虚姬-Angus', '/products', 'brand', '{"brand": "angus"}', 3, 1, '虚姬品牌专区'),
('特惠区', '/products', 'type', '{"type": "special"}', 4, 1, '特价优惠商品区'),
('两性培训营', '/training', 'link', NULL, 5, 1, '两性知识培训'),
('最新公告', '/news', 'link', NULL, 6, 1, '网站最新公告'),
('合作开店', '/cooperation', 'link', NULL, 7, 1, '合作开店信息'),
('实体店热销', '/stores', 'link', NULL, 8, 1, '实体店热销商品');