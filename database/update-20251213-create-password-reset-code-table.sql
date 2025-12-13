-- 密码重置验证码记录表
-- 用于记录找回密码时发送的验证码信息
-- 创建时间: 2025-12-13

USE chengren_shopping_mall;

DROP TABLE IF EXISTS `password_reset_code`;
CREATE TABLE `password_reset_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `email` varchar(100) NOT NULL COMMENT '邮箱地址',
  `code` varchar(20) NOT NULL COMMENT '验证码/重置令牌',
  `code_type` varchar(20) NOT NULL DEFAULT 'RESET_PASSWORD' COMMENT '验证码类型（RESET_PASSWORD-密码重置）',
  `email_content` text COMMENT '发送的邮件内容',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-未使用，1-已使用，2-已过期）',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_code` (`code`),
  KEY `idx_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_user_code` (`user_id`, `code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='密码重置验证码记录表';
