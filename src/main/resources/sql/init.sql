-- 创建数据库
CREATE DATABASE IF NOT EXISTS `auto-parts-backend` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `auto-parts-backend`;

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    `description` VARCHAR(255) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 插入角色数据
INSERT INTO `role` (`role_name`, `role_code`, `description`) VALUES
('管理员', 'ADMIN', '系统管理员，拥有所有权限'),
('仓库员', 'WAREHOUSE', '仓库管理员，负责配件和库存管理'),
('销售员', 'SALESMAN', '销售人员，负责销售订单管理');

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`role_id`) REFERENCES `role`(`id`) ON DELETE RESTRICT,
    INDEX `idx_username` (`username`),
    INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入管理员用户（密码：123456，使用 BCrypt 加密）
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `role_id`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', '13800138000', 'admin@example.com', 1, 1);

-- 配件表
CREATE TABLE IF NOT EXISTS `part` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `part_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '配件编号',
    `part_name` VARCHAR(100) NOT NULL COMMENT '配件名称',
    `specification` VARCHAR(100) COMMENT '规格',
    `brand` VARCHAR(50) COMMENT '品牌',
    `supplier` VARCHAR(100) COMMENT '供应商',
    `unit_price` DECIMAL(10, 2) NOT NULL COMMENT '单价',
    `category` VARCHAR(50) COMMENT '分类',
    `unit` VARCHAR(20) DEFAULT '件' COMMENT '单位',
    `min_stock` INT DEFAULT 10 COMMENT '最小库存阈值',
    `description` TEXT COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_part_code` (`part_code`),
    INDEX `idx_part_name` (`part_name`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配件表';

-- 库存表
CREATE TABLE IF NOT EXISTS `inventory` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `part_id` BIGINT NOT NULL UNIQUE COMMENT '配件ID',
    `stock_quantity` INT DEFAULT 0 COMMENT '库存数量',
    `warehouse_location` VARCHAR(100) COMMENT '仓库位置',
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (`part_id`) REFERENCES `part`(`id`) ON DELETE CASCADE,
    INDEX `idx_stock_quantity` (`stock_quantity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- 库存流水表
CREATE TABLE IF NOT EXISTS `inventory_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `part_id` BIGINT NOT NULL COMMENT '配件ID',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型：IN-入库，OUT-出库，TRANSFER-调拨，CHECK-盘点',
    `quantity` INT NOT NULL COMMENT '数量',
    `before_quantity` INT NOT NULL COMMENT '操作前数量',
    `after_quantity` INT NOT NULL COMMENT '操作后数量',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(50) COMMENT '操作人姓名',
    `related_order_no` VARCHAR(50) COMMENT '关联订单号',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`part_id`) REFERENCES `part`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`operator_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    INDEX `idx_part_id` (`part_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_related_order_no` (`related_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

-- 采购订单表
CREATE TABLE IF NOT EXISTS `purchase_order` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    `supplier` VARCHAR(100) NOT NULL COMMENT '供应商',
    `supplier_contact` VARCHAR(50) COMMENT '供应商联系人',
    `supplier_phone` VARCHAR(20) COMMENT '供应商电话',
    `total_amount` DECIMAL(12, 2) DEFAULT 0.00 COMMENT '总金额',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核，APPROVED-已审核，COMPLETED-已完成，CANCELLED-已取消',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `create_user_name` VARCHAR(50) COMMENT '创建人姓名',
    `approve_user_id` BIGINT COMMENT '审核人ID',
    `approve_user_name` VARCHAR(50) COMMENT '审核人姓名',
    `approve_time` DATETIME COMMENT '审核时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`create_user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`approve_user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购订单表';

-- 采购订单明细表
CREATE TABLE IF NOT EXISTS `purchase_order_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `part_id` BIGINT NOT NULL COMMENT '配件ID',
    `part_code` VARCHAR(50) COMMENT '配件编号',
    `part_name` VARCHAR(100) COMMENT '配件名称',
    `quantity` INT NOT NULL COMMENT '数量',
    `unit_price` DECIMAL(10, 2) NOT NULL COMMENT '单价',
    `total_price` DECIMAL(12, 2) NOT NULL COMMENT '总价',
    `received_quantity` INT DEFAULT 0 COMMENT '已入库数量',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`order_id`) REFERENCES `purchase_order`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`part_id`) REFERENCES `part`(`id`) ON DELETE RESTRICT,
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_part_id` (`part_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购订单明细表';

-- 销售订单表
CREATE TABLE IF NOT EXISTS `sales_order` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    `customer_name` VARCHAR(100) NOT NULL COMMENT '客户名称',
    `customer_phone` VARCHAR(20) COMMENT '客户电话',
    `customer_address` VARCHAR(255) COMMENT '客户地址',
    `total_amount` DECIMAL(12, 2) DEFAULT 0.00 COMMENT '总金额',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待出库，SHIPPED-已出库，COMPLETED-已完成，RETURNED-已退货',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `create_user_name` VARCHAR(50) COMMENT '创建人姓名',
    `warehouse_user_id` BIGINT COMMENT '出库人ID',
    `warehouse_user_name` VARCHAR(50) COMMENT '出库人姓名',
    `warehouse_time` DATETIME COMMENT '出库时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`create_user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`warehouse_user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单表';

-- 销售订单明细表
CREATE TABLE IF NOT EXISTS `sales_order_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `part_id` BIGINT NOT NULL COMMENT '配件ID',
    `part_code` VARCHAR(50) COMMENT '配件编号',
    `part_name` VARCHAR(100) COMMENT '配件名称',
    `quantity` INT NOT NULL COMMENT '数量',
    `unit_price` DECIMAL(10, 2) NOT NULL COMMENT '单价',
    `total_price` DECIMAL(12, 2) NOT NULL COMMENT '总价',
    `shipped_quantity` INT DEFAULT 0 COMMENT '已出库数量',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`order_id`) REFERENCES `sales_order`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`part_id`) REFERENCES `part`(`id`) ON DELETE RESTRICT,
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_part_id` (`part_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单明细表';
