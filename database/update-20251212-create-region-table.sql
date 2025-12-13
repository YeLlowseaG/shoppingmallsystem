-- 地区表
-- 用于存储省市区三级地区数据
-- 创建时间: 2025-12-12

DROP TABLE IF EXISTS `region`;
CREATE TABLE `region` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(20) NOT NULL COMMENT '地区编码（如：110000）',
  `name` varchar(100) NOT NULL COMMENT '地区名称',
  `parent_id` bigint DEFAULT NULL COMMENT '父级地区ID（NULL表示顶级）',
  `level` tinyint NOT NULL COMMENT '级别（1-省/直辖市，2-市，3-区/县）',
  `sort_order` int DEFAULT 0 COMMENT '排序顺序',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`),
  KEY `idx_parent_level` (`parent_id`, `level`) COMMENT '复合索引：按父级和级别查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='地区表';

