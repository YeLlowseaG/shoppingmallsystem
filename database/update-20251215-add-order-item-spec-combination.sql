-- 添加spec_combination字段到order_item表
ALTER TABLE order_item ADD COLUMN spec_combination VARCHAR(255) COMMENT 'SKU规格组合（快照）';