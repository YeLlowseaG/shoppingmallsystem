-- 为系统配置表添加分类字段
-- 日期：2026-01-05
-- 说明：为system_config表添加category字段，用于对配置进行分类管理，方便查询

-- 添加分类字段
ALTER TABLE `system_config` 
ADD COLUMN `category` varchar(50) DEFAULT NULL COMMENT '配置分类：site-网站基础, payment-支付配置, app-应用配置, mail-邮件配置, order-订单配置, wechat.work-企业微信' 
AFTER `config_type`;

-- 为现有数据设置分类
UPDATE `system_config` SET `category` = 'site' WHERE `config_key` LIKE 'site.%' OR `config_key` LIKE 'search.%';
UPDATE `system_config` SET `category` = 'payment' WHERE `config_key` LIKE 'payment.%' OR `config_key` LIKE 'deposit.%';
UPDATE `system_config` SET `category` = 'app' WHERE `config_key` LIKE 'app.%';
UPDATE `system_config` SET `category` = 'mail' WHERE `config_key` LIKE 'mail.%';
UPDATE `system_config` SET `category` = 'order' WHERE `config_key` LIKE 'order.%';
UPDATE `system_config` SET `category` = 'wechat.work' WHERE `config_key` LIKE 'wechat.work.%';

