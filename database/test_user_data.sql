-- ============================================
-- 采购者用户测试数据
-- 用于用户端登录测试
-- ============================================

USE `chengren_shopping_mall`;

-- ============================================
-- 插入2条采购者测试用户数据
-- ============================================
-- 注意：密码字段使用BCrypt加密，密码明文为 123456
-- 可以使用Java代码生成：EncryptUtil.bcryptEncode("123456")
-- 或使用在线BCrypt生成工具：https://bcrypt-generator.com/
-- 
-- 重要提示：如果密码验证失败，请使用以下方式生成新的BCrypt hash：
-- 1. 运行Java代码：EncryptUtil.bcryptEncode("123456")
-- 2. 或使用在线工具：https://bcrypt-generator.com/（输入密码：123456，rounds：10）
-- 3. 然后将生成的hash值替换到下面的password字段中

-- 测试用户1：buyer1
-- 用户名：buyer1
-- 密码：123456（BCrypt加密后的值）
-- 状态：1-已激活（可以直接登录）
INSERT INTO `sys_user` (
    `username`, 
    `email`, 
    `password`, 
    `real_name`, 
    `gender`, 
    `phone`, 
    `region`, 
    `address`, 
    `user_level`, 
    `status`, 
    `deleted`
) VALUES (
    'buyer1',
    'buyer1@test.com',
    '$2a$10$GYcMnv3gVVlUEl3fyNcRSesIAMtiajUc2s7puY0y4Msk1sCSOgVGi',
    '测试采购者1',
    1,
    '13800138001',
    '{"province":"广东省","city":"深圳市","district":"南山区"}',
    '科技园南区',
    0,
    1,
    0
);

-- 测试用户2：buyer2
-- 用户名：buyer2
-- 密码：123456（BCrypt加密后的值）
-- 状态：1-已激活（可以直接登录）
INSERT INTO `sys_user` (
    `username`, 
    `email`, 
    `password`, 
    `real_name`, 
    `gender`, 
    `phone`, 
    `region`, 
    `address`, 
    `user_level`, 
    `status`, 
    `deleted`
) VALUES (
    'buyer2',
    'buyer2@test.com',
    '$2a$10$GYcMnv3gVVlUEl3fyNcRSesIAMtiajUc2s7puY0y4Msk1sCSOgVGi',
    '测试采购者2',
    0,
    '13800138002',
    '{"province":"北京市","city":"北京市","district":"朝阳区"}',
    '建国路88号',
    1,
    1,
    0
);

-- ============================================
-- 创建用户审核记录（可选，用于测试审核功能）
-- ============================================
-- 为测试用户1创建审核记录
INSERT INTO `sys_user_audit` (
    `user_id`, 
    `audit_status`, 
    `audit_comment`, 
    `audit_time`
) 
SELECT 
    id, 
    1, 
    '测试用户，自动通过审核', 
    NOW()
FROM `sys_user` 
WHERE `username` = 'buyer1';

-- 为测试用户2创建审核记录
INSERT INTO `sys_user_audit` (
    `user_id`, 
    `audit_status`, 
    `audit_comment`, 
    `audit_time`
) 
SELECT 
    id, 
    1, 
    '测试用户，自动通过审核', 
    NOW()
FROM `sys_user` 
WHERE `username` = 'buyer2';

-- ============================================
-- 测试数据说明
-- ============================================
-- 测试用户1：
--   用户名：buyer1
--   密码：123456
--   邮箱：buyer1@test.com
--   手机号：13800138001
--   用户等级：0-普通
--   状态：1-已激活（可以直接登录）
--
-- 测试用户2：
--   用户名：buyer2
--   密码：123456
--   邮箱：buyer2@test.com
--   手机号：13800138002
--   用户等级：1-VIP
--   状态：1-已激活（可以直接登录）
--
-- 注意：
-- 1. 密码字段使用的是BCrypt加密后的值
-- 2. 如果密码验证失败，请使用以下Java代码生成新的BCrypt hash：
--    EncryptUtil.bcryptEncode("123456")
-- 3. 或者使用在线工具生成：https://bcrypt-generator.com/
-- 4. 两个测试用户的状态都是1（已激活），可以直接用于登录测试

SELECT '采购者测试用户数据插入完成！' AS message;
SELECT '测试用户1：buyer1 / 123456' AS user1;
SELECT '测试用户2：buyer2 / 123456' AS user2;
SELECT '两个用户状态均为1（已激活），可以直接登录' AS status_info;

