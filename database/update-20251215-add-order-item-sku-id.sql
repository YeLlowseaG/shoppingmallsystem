-- 添加sku_id字段到order_item表
ALTER TABLE order_item ADD COLUMN sku_id BIGINT(20) COMMENT 'SKU ID（快照）';