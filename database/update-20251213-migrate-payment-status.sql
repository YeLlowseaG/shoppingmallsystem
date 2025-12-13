-- 支付状态数据迁移脚本
-- 将旧的支付状态值迁移到新的状态值
-- 旧状态：0-待支付，1-已支付，2-已退款，3-已失败
-- 新状态：0-待支付，1-支付中，2-已支付，3-已关闭，4-已失败，5-已退款

-- 1. 更新 payment_record 表的支付状态
-- 旧值1（已支付）-> 新值2（已支付）
UPDATE payment_record 
SET payment_status = 2 
WHERE payment_status = 1;

-- 旧值2（已退款）-> 新值5（已退款）
UPDATE payment_record 
SET payment_status = 5 
WHERE payment_status = 2;

-- 旧值3（已失败）-> 新值4（已失败）
UPDATE payment_record 
SET payment_status = 4 
WHERE payment_status = 3;

-- 旧值0（待支付）保持不变，仍为0（待支付）

-- 2. 更新 order 表的支付状态
-- 旧值1（已支付）-> 新值2（已支付）
UPDATE `order` 
SET payment_status = 2 
WHERE payment_status = 1;

-- 旧值2（已退款）-> 新值5（已退款）
UPDATE `order` 
SET payment_status = 5 
WHERE payment_status = 2;

-- 旧值0（待支付）保持不变，仍为0（待支付）

-- 注意：如果 order 表中存在 payment_status = 3 的记录（旧系统中可能不存在），需要手动处理
-- 如果存在，可以将其设置为 4（已失败）或根据业务逻辑处理

-- 3. 更新数据库表注释（可选，用于文档说明）
 ALTER TABLE payment_record MODIFY COLUMN payment_status TINYINT DEFAULT 0 
 COMMENT '支付状态（0-待支付，1-支付中，2-已支付，3-已关闭，4-已失败，5-已退款）';

ALTER TABLE `order` MODIFY COLUMN payment_status TINYINT DEFAULT 0 
COMMENT '支付状态（0-待支付，1-支付中，2-已支付，3-已关闭，4-已失败，5-已退款）';

