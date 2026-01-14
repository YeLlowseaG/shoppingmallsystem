-- ============================================
-- 修复聚水潭配置中的URL格式问题
-- 创建时间：2025-01-14
-- 说明：去除URL字段中的多余空格和反引号
-- ============================================

-- 1. 修复生产环境API URL（去除空格和反引号）
UPDATE `jushuitan_config` 
SET `api_url` = 'https://api.jushuitan.com/api/open/query.aspx'
WHERE `api_url` LIKE '%`%' OR `api_url` LIKE '% %';

-- 2. 修复测试环境API URL（去除空格和反引号）
UPDATE `jushuitan_config` 
SET `test_api_url` = 'https://dev-api.jushuitan.com/api/open/query.aspx'
WHERE `test_api_url` LIKE '%`%' OR `test_api_url` LIKE '% %';

-- 3. 验证修复结果
SELECT 
    id,
    enabled,
    env_type,
    api_url AS '生产环境API地址',
    test_api_url AS '测试环境API地址',
    app_key,
    auto_push_order AS '自动推送订单',
    auto_sync_product AS '自动同步商品',
    auto_pull_logistics AS '自动拉取物流'
FROM `jushuitan_config`;

-- ============================================
-- 说明：
-- 1. 原配置中的URL包含了多余的空格和反引号，例如：
--    ' `https://api.jushuitan.com/api/open/query.aspx` '
-- 2. 这些多余的字符会导致API请求失败
-- 3. 代码已经修改为自动去除URL前后的空格
-- 4. 建议执行此SQL脚本修复数据库中的配置
-- ============================================
