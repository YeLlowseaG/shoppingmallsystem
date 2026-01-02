-- 商品同步日志表
CREATE TABLE IF NOT EXISTS `product_sync_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_code` VARCHAR(100) COMMENT '商品编码',
  `product_name` VARCHAR(200) COMMENT '商品名称',
  `env_type` VARCHAR(20) NOT NULL DEFAULT 'production' COMMENT '环境类型（test=测试环境，production=生产环境）',
  `sync_type` VARCHAR(50) NOT NULL COMMENT '同步类型（UPLOAD_ITEM-上传商品，UPDATE_ITEM-更新商品）',
  `sync_status` TINYINT NOT NULL DEFAULT 0 COMMENT '同步状态（0-失败，1-成功，2-处理中）',
  `error_code` VARCHAR(50) COMMENT '错误代码',
  `error_message` TEXT COMMENT '错误信息',
  `request_data` TEXT COMMENT '请求数据（JSON格式）',
  `response_data` TEXT COMMENT '响应数据（JSON格式）',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_sync_type` (`sync_type`),
  KEY `idx_sync_status` (`sync_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_env_type` (`env_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品同步日志表';
