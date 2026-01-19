-- 为配件表添加逻辑删除字段
ALTER TABLE `part` ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除';

-- 为用户表添加逻辑删除字段（如果不存在）
ALTER TABLE `user` ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除';

-- 为采购订单表添加逻辑删除字段
ALTER TABLE `purchase_order` ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除';

-- 为销售订单表添加逻辑删除字段
ALTER TABLE `sales_order` ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除';

-- 为库存表添加逻辑删除字段
ALTER TABLE `inventory` ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除';
