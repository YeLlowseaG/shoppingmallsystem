-- 修复帮助中心文章表 content 字段类型
-- 创建时间: 2025-12-10
-- 更新说明: 将 content 字段从 TEXT 改为 LONGTEXT，以支持富文本编辑器插入大量内容（包括图片）

-- 修改 help_article 表的 content 字段类型为 LONGTEXT
ALTER TABLE `help_article` 
MODIFY COLUMN `content` LONGTEXT NOT NULL COMMENT '文章内容（HTML格式，支持富文本和图片）';

