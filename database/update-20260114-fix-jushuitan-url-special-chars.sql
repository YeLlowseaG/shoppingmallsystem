-- ============================================
-- 修复聚水潭配置中的URL格式问题（去除反引号和空格）
-- 创建时间：2025-01-14
-- ============================================

-- 1. 修复生产环境API URL（去除反引号和空格）
UPDATE `jushuitan_config` 
SET `api_url` = 'https://openapi.jushuitan.com/api/open/query.aspx'
WHERE `api_url` LIKE '%`%' OR `api_url` LIKE '% %';

-- 2. 修复测试环境API URL（去除反引号和空格）
UPDATE `jushuitan_config` 
SET `test_api_url` = 'https://dev-api.jushuitan.com/api/open/query.aspx'
WHERE `test_api_url` LIKE '%`%' OR `test_api_url` LIKE '% %';

-- 3. 修复测试环境回调URL（去除反引号和空格）
UPDATE `jushuitan_config` 
SET `test_callback_url` = 'https://anika-cronish-tanesha.ngrok-free.dev/api/common/erp/callback/logistics'
WHERE `test_callback_url` LIKE '%`%' OR `test_callback_url` LIKE '% %';

-- 4. 验证修复结果
SELECT 
    id,
    enabled,
    env_type,
    api_url AS '生产环境API地址',
    test_api_url AS '测试环境API地址',
    test_callback_url AS '测试环境回调地址',
    app_key,
    auto_push_order AS '自动推送订单',
    auto_sync_product AS '自动同步商品',
    auto_pull_logistics AS '自动拉取物流'
FROM `jushuitan_config`;

-- ============================================
-- 说明：
-- 1. 原配置中的URL包含了反引号 ` 和空格，例如：
--    ' `https://api.jushuitan.com/api/open/query.aspx` '
-- 2. 这些特殊字符会导致配置加载异常，从而无法正确读取 auto_push_order 等配置
-- 3. 执行此SQL脚本后，配置将恢复正常
-- 4. 建议重启后端服务以加载新的配置
-- ============================================
