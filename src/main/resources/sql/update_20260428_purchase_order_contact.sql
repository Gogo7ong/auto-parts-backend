USE `auto-parts-backend`;

ALTER TABLE `purchase_order`
    ADD COLUMN `supplier_contact` VARCHAR(50) COMMENT '供应商联系人' AFTER `supplier`,
    ADD COLUMN `supplier_phone` VARCHAR(20) COMMENT '供应商电话' AFTER `supplier_contact`;
