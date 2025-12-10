-- 测试商品数据
-- 为每个二级分类添加至少4个商品

USE chengren_shopping_mall;

-- 5. 男用器具 (category_id=5)
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, stock, sales_count, status, deleted) VALUES
('P0001', 'AV女优三穴仿真名器 [虞姬]', 5, 'https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product1', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '高仿真AV女优倒模，三穴设计，极致体验', 134.00, 100, 256, 1, 0),
('P0002', '双穴情欲小翘臀 飞机杯', 5, 'https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product2', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '丰满翘臀造型，双穴设计，柔软仿真', 89.00, 150, 432, 1, 0),
('P0003', '极上爆乳美模名器', 5, 'https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product3', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '酥胸把位设计，Q弹柔软，真实触感', 156.00, 80, 189, 1, 0),
('P0004', '深喉洁音震动双穴飞机杯', 5, 'https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product4', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '震动功能，深喉快感，双穴设计', 178.00, 60, 321, 1, 0),
('P0005', '娇嫩酥胸小萌妹名器', 5, 'https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product5', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '少女系列，小巧可爱，柔软Q弹', 112.00, 120, 276, 1, 0),
('P0006', '好色人妻巨乳倒模', 5, 'https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product6', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '巨乳造型，成熟人妻系列，真实倒模', 239.00, 50, 145, 1, 0);

-- 6. 女用器具 (category_id=6)
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, stock, sales_count, status, deleted) VALUES
('P0007', '粉红小猪震动棒 G点按摩', 6, 'https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product7', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '可爱小猪造型，10频震动，G点精准刺激', 99.00, 200, 567, 1, 0),
('P0008', '玩趣甜心小象震动棒', 6, 'https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product8', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '软萌小象设计，静音震动，防水可冲洗', 89.00, 180, 423, 1, 0),
('P0009', '真实阴道倒模震动器', 6, 'https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product9', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '真人倒模，高仿真通道，双马达震动', 168.00, 90, 234, 1, 0),
('P0010', '日本NPG大名器震动棒', 6, 'https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product10', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '日本进口，大尺寸设计，强劲震动', 198.00, 70, 178, 1, 0),
('P0011', '丝滑少女唇震动棒', 6, 'https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product11', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '少女系列，小巧便携，USB充电', 79.00, 220, 489, 1, 0),
('P0012', '粉嫩小玫瑰吮吸按摩器', 6, 'https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product12', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '玫瑰造型，吮吸+震动双重刺激', 145.00, 110, 312, 1, 0);

-- 7. 润滑剂 (category_id=7)
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, stock, sales_count, status, deleted) VALUES
('P0013', '水溶性人体润滑液 50ml', 7, 'https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product13', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '清爽不黏腻，易清洗，安全无刺激', 19.90, 500, 1234, 1, 0),
('P0014', '硅基长效润滑液 100ml', 7, 'https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product14', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '持久润滑，防水不易干，超长效', 39.90, 300, 876, 1, 0),
('P0015', '杜蕾斯K-Y润滑剂 82g', 7, 'https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product15', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '知名品牌，医用级别，温和亲肤', 45.00, 400, 1567, 1, 0),
('P0016', '冈本润滑液 清爽型 60ml', 7, 'https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product16', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '日本原装进口，清爽配方，不油腻', 35.00, 350, 987, 1, 0),
('P0017', '热感润滑液 50ml', 7, 'https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product17', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '微热配方，增加快感，安全温和', 29.90, 280, 654, 1, 0);

-- 8. 安全套 (category_id=8)
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, stock, sales_count, status, deleted) VALUES
('P0018', '杜蕾斯至薄装安全套 12只', 8, 'https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product18', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '0.04mm超薄，亲肤体验，大品牌保障', 49.00, 600, 2345, 1, 0),
('P0019', '冈本003至薄安全套 10只', 8, 'https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product19', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '0.03mm极薄，日本原装，真实触感', 78.00, 450, 1876, 1, 0),
('P0020', '冈本001极薄安全套 3只装', 8, 'https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product20', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '0.01mm顶级超薄，如同无物，高端系列', 128.00, 200, 567, 1, 0),
('P0021', '杜蕾斯持久装延时套 12只', 8, 'https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product21', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '延时配方，持久耐用，安全可靠', 55.00, 500, 1432, 1, 0),
('P0022', '网易严选安全套 24只装', 8, 'https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product22', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '高性价比，0.05mm薄度，大容量', 39.90, 800, 3456, 1, 0);

-- 9. 护理用品 (category_id=9)
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, stock, sales_count, status, deleted) VALUES
('P0023', '女性私处护理液 200ml', 9, 'https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product23', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '温和抑菌，弱酸配方，呵护健康', 29.90, 400, 876, 1, 0),
('P0024', '男士私处护理液 250ml', 9, 'https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product24', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '清爽去异味，温和不刺激', 35.00, 300, 543, 1, 0),
('P0025', '延时喷剂 15ml', 9, 'https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product25', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '持久耐用，温和配方，安全有效', 59.00, 250, 987, 1, 0),
('P0026', '保健按摩精油 100ml', 9, 'https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product26', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '天然植物提取，滋养温和', 49.00, 200, 432, 1, 0),
('P0027', '男士增大精油 50ml', 9, 'https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product27', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '天然成分，按摩保健，温和有效', 69.00, 180, 654, 1, 0);

-- 10. 清洁用品 (category_id=10)
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, stock, sales_count, status, deleted) VALUES
('P0028', '情趣用品清洁液 150ml', 10, 'https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product28', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '专业清洁，抑菌消毒，温和不伤材质', 25.00, 350, 765, 1, 0),
('P0029', '器具清洁泡沫喷雾 200ml', 10, 'https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product29', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '泡沫清洁，方便快捷，深层除菌', 32.00, 300, 543, 1, 0),
('P0030', '玩具清洁湿巾 50片装', 10, 'https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product30', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '便携湿巾，随用随取，温和清洁', 18.90, 450, 987, 1, 0),
('P0031', '紫外线消毒盒', 10, 'https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product31', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', 'UV紫外线消毒，99.9%杀菌，安全卫生', 89.00, 150, 234, 1, 0),
('P0032', '器具收纳消毒盒', 10, 'https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product32', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '收纳+消毒二合一，私密安全', 78.00, 200, 345, 1, 0);

-- 11. 女士内衣 (category_id=11)
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, stock, sales_count, status, deleted) VALUES
('P0033', '黑色蕾丝透视套装', 11, 'https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product33', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '性感蕾丝，若隐若现，诱惑魅力', 79.00, 300, 876, 1, 0),
('P0034', '红色激情连体衣', 11, 'https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product34', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '激情红色，连体设计，性感撩人', 89.00, 250, 654, 1, 0),
('P0035', '情趣护士服套装', 11, 'https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product35', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '角色扮演，护士制服，增添情趣', 95.00, 200, 543, 1, 0),
('P0036', '学生制服三件套', 11, 'https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product36', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '清纯学生装，青春活力，角色扮演', 98.00, 180, 432, 1, 0),
('P0037', 'OL职业套装', 11, 'https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product37', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '职场女性，知性魅力，优雅性感', 105.00, 150, 321, 1, 0);

-- 12. 男士内衣 (category_id=12)
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, stock, sales_count, status, deleted) VALUES
('P0038', '性感透视网纱三角裤', 12, 'https://via.placeholder.com/800x800/E84393/ffffff?text=Product38', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '网纱透视，性感诱惑，情趣满分', 35.00, 400, 765, 1, 0),
('P0039', '冰丝无痕情趣内裤', 12, 'https://via.placeholder.com/800x800/E84393/ffffff?text=Product39', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '冰丝材质，清爽舒适，无痕设计', 28.00, 500, 1234, 1, 0),
('P0040', '大象鼻子趣味内裤', 12, 'https://via.placeholder.com/800x800/E84393/ffffff?text=Product40', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '趣味造型，幽默搞笑，增添乐趣', 39.00, 350, 876, 1, 0),
('P0041', 'U凸设计运动内裤', 12, 'https://via.placeholder.com/800x800/E84393/ffffff?text=Product41', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', 'U型凸起设计，舒适透气', 45.00, 300, 654, 1, 0),
('P0042', '豹纹性感平角裤', 12, 'https://via.placeholder.com/800x800/E84393/ffffff?text=Product42', '["https://via.placeholder.com/800x800?text=1", "https://via.placeholder.com/800x800?text=2"]', '豹纹图案，野性魅力，性感时尚', 42.00, 280, 543, 1, 0);
