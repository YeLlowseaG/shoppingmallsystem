-- 商品表新增条码和计量单位字段
-- 用于支持批量导入功能
-- 更新日期: 2025-12-23

USE chengren_shopping_mall;

-- 新增条码字段
ALTER TABLE product
ADD COLUMN barcode VARCHAR(100) NULL COMMENT '条码' AFTER product_code;

-- 新增计量单位字段
ALTER TABLE product
ADD COLUMN unit VARCHAR(20) NULL COMMENT '计量单位' AFTER barcode;

-- 为条码字段创建索引（可选，提高查询效率）
CREATE INDEX idx_barcode ON product(barcode);
