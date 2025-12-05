-- ============================================
-- 初始化基础数据脚本
-- ============================================

USE `chengren_shopping_mall`;

-- ============================================
-- 1. 初始化商品分类数据
-- ============================================

-- 一级分类
INSERT INTO `product_category` (`parent_id`, `category_name`, `level`, `sort_order`, `status`) VALUES
(0, '情趣用品', 1, 1, 1),
(0, '健康护理', 1, 2, 1),
(0, '情趣内衣', 1, 3, 1),
(0, '其他', 1, 99, 1);

-- 获取一级分类ID（假设自增从1开始）
SET @category_level1_1 = LAST_INSERT_ID();
SET @category_level1_2 = @category_level1_1 + 1;
SET @category_level1_3 = @category_level1_1 + 2;
SET @category_level1_4 = @category_level1_1 + 3;

-- 二级分类（情趣用品）
INSERT INTO `product_category` (`parent_id`, `category_name`, `level`, `sort_order`, `status`) VALUES
(@category_level1_1, '男用器具', 2, 1, 1),
(@category_level1_1, '女用器具', 2, 2, 1),
(@category_level1_1, '润滑剂', 2, 3, 1),
(@category_level1_1, '安全套', 2, 4, 1);

-- 二级分类（健康护理）
INSERT INTO `product_category` (`parent_id`, `category_name`, `level`, `sort_order`, `status`) VALUES
(@category_level1_2, '护理用品', 2, 1, 1),
(@category_level1_2, '清洁用品', 2, 2, 1);

-- 二级分类（情趣内衣）
INSERT INTO `product_category` (`parent_id`, `category_name`, `level`, `sort_order`, `status`) VALUES
(@category_level1_3, '女士内衣', 2, 1, 1),
(@category_level1_3, '男士内衣', 2, 2, 1);

-- ============================================
-- 2. 初始化平台管理员账号
-- ============================================
-- 密码：admin123（BCrypt加密后的值，实际使用时需要替换为真实加密值）
-- 注意：这里使用占位符，实际部署时需要生成真实的BCrypt加密密码
INSERT INTO `sys_user` (`username`, `email`, `password`, `real_name`, `user_level`, `status`) VALUES
('admin', 'admin@shoppingmall.com', '$2a$10$placeholder', '系统管理员', '管理员', '已激活');

-- ============================================
-- 3. 初始化系统配置（如果需要配置表）
-- ============================================
-- 注意：根据实际需求，可能需要创建系统配置表
-- 这里暂时不创建，后续根据需求添加

-- ============================================
-- 初始化完成提示
-- ============================================
SELECT '基础数据初始化完成！' AS message;

