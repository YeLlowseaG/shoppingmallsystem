-- 购买咨询表
CREATE TABLE `consultation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `user_id` bigint COMMENT '用户ID（可为空，支持匿名咨询）',
  `contact_name` varchar(50) NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `contact_email` varchar(100) COMMENT '联系邮箱',
  `consultation_content` text NOT NULL COMMENT '咨询内容',
  `reply_content` text COMMENT '回复内容',
  `status` tinyint DEFAULT 0 COMMENT '状态：0-待回复，1-已回复，2-已关闭',
  `reply_time` datetime COMMENT '回复时间',
  `reply_admin_id` bigint COMMENT '回复管理员ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购买咨询表';