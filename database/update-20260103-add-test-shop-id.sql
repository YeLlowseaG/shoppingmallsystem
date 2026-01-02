-- 添加测试环境店铺ID字段
-- 执行时间：2026-01-03

USE chengren_shopping_mall;

-- 添加 test_shop_id 字段
ALTER TABLE jushuitan_config
ADD COLUMN test_shop_id VARCHAR(50) DEFAULT NULL COMMENT '测试环境店铺ID' AFTER test_access_token;

-- 更新 shop_id 字段注释
ALTER TABLE jushuitan_config
MODIFY COLUMN shop_id VARCHAR(50) DEFAULT NULL COMMENT '生产环境店铺ID';
