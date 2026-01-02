-- 聚水潭配置添加 access_token 字段
-- 用于API调用时的access_token参数
-- 日期：2025-12-31

-- 添加生产环境和测试环境的 access_token 字段
ALTER TABLE `jushuitan_config`
ADD COLUMN `access_token` VARCHAR(100) DEFAULT NULL COMMENT '生产环境AccessToken' AFTER `app_secret`,
ADD COLUMN `test_access_token` VARCHAR(100) DEFAULT 'b7e3b1e24e174593af8ca5c397e53dad' COMMENT '测试环境AccessToken（聚水潭公共测试账号）' AFTER `test_app_secret`;

-- 查看表结构
DESC `jushuitan_config`;
