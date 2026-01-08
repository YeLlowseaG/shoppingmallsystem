-- 公告表索引优化
-- 创建时间: 2026-01-08
-- 更新说明: 优化公告列表查询性能，添加复合索引

-- 添加复合索引优化排序查询
-- 索引顺序：deleted（WHERE条件） -> publish_date（排序） -> sort（排序）
-- 用于优化管理后台和用户端的公告列表查询
ALTER TABLE `announcement` 
ADD INDEX `idx_deleted_publish_sort` (`deleted`, `publish_date` DESC, `sort` ASC);

-- 添加复合索引优化带status条件的查询（用户端使用）
-- 索引顺序：deleted（WHERE条件） -> status（WHERE条件） -> publish_date（排序） -> sort（排序）
ALTER TABLE `announcement` 
ADD INDEX `idx_deleted_status_publish_sort` (`deleted`, `status`, `publish_date` DESC, `sort` ASC);

