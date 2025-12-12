-- 创建系统配置表
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_name` varchar(200) NOT NULL COMMENT '配置名称',
  `config_desc` varchar(500) COMMENT '配置描述',
  `config_type` varchar(50) NOT NULL DEFAULT 'text' COMMENT '配置类型：text/textarea/image/number',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';

-- 插入默认系统配置数据
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`) VALUES
('site.logo', 'https://via.placeholder.com/150x60/E4393C/ffffff?text=JINGVO', '网站Logo', '网站顶部Logo图片URL', 'image', 1, 1),
('site.name', 'JINGVO 净果', '网站名称', '网站名称，用于SEO和显示', 'text', 2, 1),
('site.service_phone', '400-166-1683', '服务热线', '客户服务热线电话', 'text', 3, 1),
('site.consult_phone', '13049338552', '咨询热线', '业务咨询热线电话', 'text', 4, 1),
('site.qrcode', 'https://via.placeholder.com/60x60/666666/ffffff?text=QR', '微信二维码', '微信客服二维码图片URL', 'image', 5, 1),
('site.keywords', '成人用品,情趣用品,成人玩具', '网站关键词', '网站SEO关键词', 'textarea', 6, 1),
('site.description', '专业的成人用品B2B平台，提供优质的成人用品批发服务', '网站描述', '网站SEO描述', 'textarea', 7, 1),
('site.copyright', '© 2025 JINGVO 净果. All rights reserved.', '版权信息', '网站底部版权信息', 'text', 8, 1),
('site.icp', '京ICP备12345678号', 'ICP备案号', '网站ICP备案号', 'text', 9, 1),
('search.hot_keywords', '飞机杯,跳蛋名器,情趣跳蛋,情趣内衣,安全套,延时喷雾', '热门搜索词', '搜索框下方显示的热门关键词，用逗号分隔', 'textarea', 10, 1);