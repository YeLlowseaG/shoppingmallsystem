-- 为各分类补充商品数据，确保每个分类都有10个商品

-- 男用器具分类 (category_id=5) - 补充4个商品
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, sale_price, market_price, cost_price, stock, sales_count, warning_stock, weight, status, brand_id, create_time, update_time) VALUES
('P000050007', '私密花园仿真名器 [樱花]', 5, 'https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product7', '["https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product7"]', '樱花粉嫩设计，仿真度极高，柔软舒适', 128.00, 118.00, 158.00, 65.00, 80, 15, 10, 350.00, 1, NULL, NOW(), NOW()),
('P000050008', '激情双穴少女名器', 5, 'https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product8', '["https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product8"]', '少女造型设计，双穴体验，柔嫩触感', 98.00, 88.00, 128.00, 49.00, 75, 28, 10, 280.00, 1, NULL, NOW(), NOW()),
('P000050009', '豪华振动加热名器', 5, 'https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product9', '["https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product9"]', '智能加热功能，多频振动，极致享受', 268.00, 248.00, 328.00, 134.00, 45, 12, 10, 580.00, 1, NULL, NOW(), NOW()),
('P000050010', '蜜桃臀部仿真器', 5, 'https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product10', '["https://via.placeholder.com/800x800/FF6B9D/ffffff?text=Product10"]', '蜜桃造型臀部，真实触感，Q弹柔软', 156.00, 146.00, 196.00, 78.00, 65, 22, 10, 420.00, 1, NULL, NOW(), NOW());

-- 女用器具分类 (category_id=6) - 补充4个商品
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, sale_price, market_price, cost_price, stock, sales_count, warning_stock, weight, status, brand_id, create_time, update_time) VALUES
('P000060007', '玫瑰金震动棒 [高端]', 6, 'https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product7', '["https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product7"]', '玫瑰金外观，10频震动，静音设计', 188.00, 168.00, 238.00, 94.00, 85, 35, 10, 180.00, 1, NULL, NOW(), NOW()),
('P000060008', '智能APP控制震动器', 6, 'https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product8', '["https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product8"]', '手机APP远程控制，多种模式，防水设计', 298.00, 278.00, 358.00, 149.00, 55, 18, 10, 220.00, 1, NULL, NOW(), NOW()),
('P000060009', '蝴蝶穿戴震动器', 6, 'https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product9', '["https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product9"]', '蝴蝶造型，可穿戴设计，隐蔽舒适', 158.00, 148.00, 198.00, 79.00, 70, 25, 10, 95.00, 1, NULL, NOW(), NOW()),
('P000060010', '加热仿真震动棒', 6, 'https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product10', '["https://via.placeholder.com/800x800/9D50BB/ffffff?text=Product10"]', '体感温度加热，仿真材质，医用硅胶', 228.00, 208.00, 288.00, 114.00, 60, 20, 10, 280.00, 1, NULL, NOW(), NOW());

-- 润滑剂分类 (category_id=7) - 补充5个商品
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, sale_price, market_price, cost_price, stock, sales_count, warning_stock, weight, status, brand_id, create_time, update_time) VALUES
('P000070006', '芦荟保湿润滑液 100ml', 7, 'https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product6', '["https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product6"]', '天然芦荟提取，保湿滋润，温和无刺激', 45.00, 38.00, 58.00, 22.50, 200, 68, 20, 120.00, 1, NULL, NOW(), NOW()),
('P000070007', '玫瑰香型人体润滑油', 7, 'https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product7', '["https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product7"]', '天然玫瑰香味，丝滑质地，增进情趣', 52.00, 45.00, 68.00, 26.00, 180, 55, 20, 130.00, 1, NULL, NOW(), NOW()),
('P000070008', '冰火两重天润滑凝胶', 7, 'https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product8', '["https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product8"]', '冷热交替感受，刺激体验，安全配方', 68.00, 58.00, 88.00, 34.00, 150, 42, 20, 110.00, 1, NULL, NOW(), NOW()),
('P000070009', '长效保湿润滑剂 200ml', 7, 'https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product9', '["https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product9"]', '长效保湿配方，持久润滑，大容量装', 78.00, 68.00, 98.00, 39.00, 120, 38, 20, 220.00, 1, NULL, NOW(), NOW()),
('P000070010', '私密护理清洁凝胶', 7, 'https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product10', '["https://via.placeholder.com/800x800/74B9FF/ffffff?text=Product10"]', '私密部位专用，温和清洁，pH平衡', 35.00, 28.00, 48.00, 17.50, 250, 85, 20, 150.00, 1, NULL, NOW(), NOW());

-- 安全套分类 (category_id=8) - 补充5个商品
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, sale_price, market_price, cost_price, stock, sales_count, warning_stock, weight, status, brand_id, create_time, update_time) VALUES
('P000080006', '超薄0.01mm避孕套 12只装', 8, 'https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product6', '["https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product6"]', '极致超薄设计，真实触感，安全可靠', 88.00, 78.00, 118.00, 44.00, 300, 125, 30, 50.00, 1, NULL, NOW(), NOW()),
('P000080007', '螺纹颗粒型安全套', 8, 'https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product7', '["https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product7"]', '螺纹颗粒设计，增强刺激，双重快感', 65.00, 55.00, 88.00, 32.50, 280, 95, 30, 45.00, 1, NULL, NOW(), NOW()),
('P000080008', '草莓香味安全套 10只装', 8, 'https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product8', '["https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product8"]', '天然草莓香味，增进情趣，优质材质', 42.00, 35.00, 58.00, 21.00, 350, 148, 30, 40.00, 1, NULL, NOW(), NOW()),
('P000080009', '加大号宽松型安全套', 8, 'https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product9', '["https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product9"]', '加大号设计，宽松舒适，不紧绷', 58.00, 48.00, 78.00, 29.00, 200, 75, 30, 55.00, 1, NULL, NOW(), NOW()),
('P000080010', '延时持久安全套 8只装', 8, 'https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product10', '["https://via.placeholder.com/800x800/FFD93D/ffffff?text=Product10"]', '苯佐卡因成分，延时持久，增强体验', 95.00, 85.00, 128.00, 47.50, 180, 65, 30, 48.00, 1, NULL, NOW(), NOW());

-- 护理用品分类 (category_id=9) - 补充5个商品
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, sale_price, market_price, cost_price, stock, sales_count, warning_stock, weight, status, brand_id, create_time, update_time) VALUES
('P000090006', '私密部位护理湿巾 30片', 9, 'https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product6', '["https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product6"]', '私密护理专用，温和无刺激，便携装', 28.00, 22.00, 38.00, 14.00, 400, 188, 40, 180.00, 1, NULL, NOW(), NOW()),
('P000090007', '抗菌私密护理喷雾', 9, 'https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product7', '["https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product7"]', '抗菌除臭，私密护理，随时清新', 48.00, 38.00, 68.00, 24.00, 250, 95, 25, 120.00, 1, NULL, NOW(), NOW()),
('P000090008', '玫瑰精油护理按摩油', 9, 'https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product8', '["https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product8"]', '天然玫瑰精油，滋养护理，芳香怡人', 88.00, 78.00, 118.00, 44.00, 150, 58, 20, 150.00, 1, NULL, NOW(), NOW()),
('P000090009', '私密紧致护理凝胶', 9, 'https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product9', '["https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product9"]', '天然植物提取，紧致护理，安全温和', 128.00, 118.00, 168.00, 64.00, 120, 45, 20, 100.00, 1, NULL, NOW(), NOW()),
('P000090010', '蜂蜜保湿护理乳液', 9, 'https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product10', '["https://via.placeholder.com/800x800/FD79A8/ffffff?text=Product10"]', '蜂蜜滋润配方，深层保湿，呵护肌肤', 68.00, 58.00, 88.00, 34.00, 200, 78, 25, 160.00, 1, NULL, NOW(), NOW());

-- 女士内衣分类 (category_id=11) - 补充5个商品
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, sale_price, market_price, cost_price, stock, sales_count, warning_stock, weight, status, brand_id, create_time, update_time) VALUES
('P000110006', '蕾丝透视诱惑文胸', 11, 'https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product6', '["https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product6"]', '精致蕾丝工艺，透视设计，性感迷人', 158.00, 138.00, 198.00, 79.00, 80, 48, 15, 80.00, 1, NULL, NOW(), NOW()),
('P000110007', '开档连体情趣内衣', 11, 'https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product7', '["https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product7"]', '连体设计，开档便利，性感撩人', 188.00, 168.00, 238.00, 94.00, 60, 35, 15, 120.00, 1, NULL, NOW(), NOW()),
('P000110008', '丝绸睡裙套装', 11, 'https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product8', '["https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product8"]', '真丝材质，柔滑舒适，优雅性感', 268.00, 248.00, 328.00, 134.00, 45, 28, 10, 150.00, 1, NULL, NOW(), NOW()),
('P000110009', '学生制服角色扮演', 11, 'https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product9', '["https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product9"]', '学生制服款式，角色扮演，青春活力', 128.00, 108.00, 168.00, 64.00, 70, 42, 15, 180.00, 1, NULL, NOW(), NOW()),
('P000110010', '网纱透明三点式', 11, 'https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product10', '["https://via.placeholder.com/800x800/55EFC4/ffffff?text=Product10"]', '网纱透明材质，三点式设计，极致诱惑', 88.00, 78.00, 118.00, 44.00, 90, 55, 15, 60.00, 1, NULL, NOW(), NOW());

-- 男士内衣分类 (category_id=12) - 补充5个商品
INSERT INTO product (product_code, product_name, category_id, main_image, images, description, base_price, sale_price, market_price, cost_price, stock, sales_count, warning_stock, weight, status, brand_id, create_time, update_time) VALUES
('P000120006', '性感丁字裤 男士专用', 12, 'https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product6', '["https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product6"]', '丁字裤设计，性感撩人，透气舒适', 68.00, 58.00, 88.00, 34.00, 120, 38, 20, 50.00, 1, NULL, NOW(), NOW()),
('P000120007', '开档四角内裤', 12, 'https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product7', '["https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product7"]', '开档设计，便利实用，弹性舒适', 45.00, 38.00, 58.00, 22.50, 150, 65, 20, 70.00, 1, NULL, NOW(), NOW()),
('P000120008', '网眼透气内裤', 12, 'https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product8', '["https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product8"]', '网眼透气设计，夏季专用，清爽舒适', 38.00, 32.00, 48.00, 19.00, 200, 85, 25, 45.00, 1, NULL, NOW(), NOW()),
('P000120009', 'C字裤超低腰设计', 12, 'https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product9', '["https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product9"]', 'C字裤造型，超低腰设计，时尚性感', 58.00, 48.00, 78.00, 29.00, 100, 32, 15, 35.00, 1, NULL, NOW(), NOW()),
('P000120010', '竹纤维抗菌内裤', 12, 'https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product10', '["https://via.placeholder.com/800x800/A29BFE/ffffff?text=Product10"]', '竹纤维材质，天然抗菌，健康环保', 88.00, 78.00, 118.00, 44.00, 80, 28, 15, 60.00, 1, NULL, NOW(), NOW());