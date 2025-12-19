
USE chengren_shopping_mall;

-- ============================================
-- 1. 备份检查（可选，建议在生产环境执行前先备份）
-- ============================================
-- CREATE TABLE sys_user_backup_20251219 AS SELECT * FROM sys_user;

-- ============================================
-- 2. 添加 is_member 字段
-- ============================================
-- 说明：添加会员标识字段，0-普通用户，1-会员
ALTER TABLE `sys_user` 
ADD COLUMN `is_member` tinyint NOT NULL DEFAULT '0' COMMENT '是否会员（0-普通用户，1-会员）' AFTER `wangwang`;

-- ============================================
-- 3. 将 user_level 字段改为 member_level_id
-- ============================================
-- 说明：将 user_level 改为 member_level_id，类型改为 bigint，用于关联 member_level 表
--       如果原 user_level 有值且大于0，将其作为 member_level_id
--       同时设置 is_member = 1（表示是会员）
ALTER TABLE `sys_user` 
CHANGE COLUMN `user_level` `member_level_id` bigint DEFAULT NULL COMMENT '会员等级ID（关联 member_level 表，普通用户为 NULL）' AFTER `is_member`;

-- ============================================
-- 4. 数据迁移：将原有 user_level 数据迁移到新字段
-- ============================================
-- 说明：如果原 user_level 有值且大于0，将其作为 member_level_id，并设置 is_member = 1
UPDATE `sys_user` 
SET 
    `member_level_id` = CASE 
        WHEN `member_level_id` > 0 THEN `member_level_id` 
        ELSE NULL 
    END,
    `is_member` = CASE 
        WHEN `member_level_id` > 0 THEN 1 
        ELSE 0 
    END;

-- ============================================
-- 5. 更新索引
-- ============================================
-- 删除旧的 user_level 索引（如果存在）
DROP INDEX  `idx_user_level` ON `sys_user`;

-- 添加新的索引
ALTER TABLE `sys_user` 
ADD KEY `idx_is_member` (`is_member`),
ADD KEY `idx_member_level_id` (`member_level_id`);

-- ============================================
-- 6. 验证修改结果
-- ============================================
-- 查看表结构，确认字段已添加和修改
DESCRIBE `sys_user`;

-- ============================================
-- 7. 数据完整性验证
-- ============================================
-- 查询验证（检查字段是否存在且数据完整）
SELECT 
    id,
    username,
    is_member,
    member_level_id,
    status
FROM `sys_user` 
WHERE `deleted` = 0 
LIMIT 10;

-- 验证数据迁移结果
SELECT 
    is_member,
    COUNT(*) as count,
    COUNT(CASE WHEN member_level_id IS NOT NULL THEN 1 END) as has_level_count
FROM `sys_user` 
WHERE `deleted` = 0 
GROUP BY is_member;
