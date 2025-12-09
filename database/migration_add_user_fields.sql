-- ============================================
-- 用户表字段扩展迁移脚本
-- 添加注册功能所需的额外字段
-- 执行时间: 2025-01-XX
-- ============================================

USE `chengren_shopping_mall`;

-- 添加用户表新字段
ALTER TABLE `sys_user` 
ADD COLUMN `operator` VARCHAR(50) DEFAULT NULL COMMENT '运营人员' AFTER `phone`,
ADD COLUMN `birthday` DATE DEFAULT NULL COMMENT '出生日期' AFTER `gender`,
ADD COLUMN `zip_code` VARCHAR(10) DEFAULT NULL COMMENT '邮编' AFTER `address`,
ADD COLUMN `fixed_phone` VARCHAR(20) DEFAULT NULL COMMENT '固定电话' AFTER `phone`,
ADD COLUMN `security_question` VARCHAR(255) DEFAULT NULL COMMENT '安全问题' AFTER `zip_code`,
ADD COLUMN `security_answer` VARCHAR(255) DEFAULT NULL COMMENT '安全问题答案' AFTER `security_question`,
ADD COLUMN `wangwang` VARCHAR(50) DEFAULT NULL COMMENT '旺旺账号' AFTER `security_answer`;

-- 添加索引（如果需要根据这些字段查询）
-- ALTER TABLE `sys_user` ADD KEY `idx_operator` (`operator`);
-- ALTER TABLE `sys_user` ADD KEY `idx_birthday` (`birthday`);

-- 验证字段是否添加成功
-- SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = 'chengren_shopping_mall' 
--   AND TABLE_NAME = 'sys_user' 
--   AND COLUMN_NAME IN ('operator', 'birthday', 'zip_code', 'fixed_phone', 'security_question', 'security_answer', 'wangwang');


