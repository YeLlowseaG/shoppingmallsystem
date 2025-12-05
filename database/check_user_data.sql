-- ============================================
-- 检查采购者测试用户数据
-- 用于排查登录问题
-- ============================================

USE `chengren_shopping_mall`;

-- 检查buyer1用户数据
SELECT 
    id,
    username,
    email,
    LEFT(password, 30) AS password_prefix,
    LENGTH(password) AS password_length,
    real_name,
    status,
    user_level,
    deleted,
    create_time
FROM `sys_user` 
WHERE `username` = 'buyer1';

-- 检查buyer2用户数据
SELECT 
    id,
    username,
    email,
    LEFT(password, 30) AS password_prefix,
    LENGTH(password) AS password_length,
    real_name,
    status,
    user_level,
    deleted,
    create_time
FROM `sys_user` 
WHERE `username` = 'buyer2';

-- 检查用户审核记录
SELECT 
    ua.id,
    ua.user_id,
    u.username,
    ua.audit_status,
    ua.audit_comment,
    ua.audit_time
FROM `sys_user_audit` ua
LEFT JOIN `sys_user` u ON ua.user_id = u.id
WHERE u.username IN ('buyer1', 'buyer2');

