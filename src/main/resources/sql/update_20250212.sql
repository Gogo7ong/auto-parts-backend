/**
 * @author dengjiawen
 * @since 2026-02-12
 */

-- 使用数据库
USE `auto-parts-backend`;

-- ============================================
-- 1. 供应商表
-- ============================================
CREATE TABLE IF NOT EXISTS `supplier` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `supplier_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '供应商编码',
    `supplier_name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
    `contact_person` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `address` VARCHAR(200) COMMENT '地址',
    `email` VARCHAR(100) COMMENT '邮箱',
    `bank_account` VARCHAR(50) COMMENT '银行账户',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_supplier_code` (`supplier_code`),
    INDEX `idx_supplier_name` (`supplier_name`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

-- 插入示例供应商数据
INSERT INTO `supplier` (`supplier_code`, `supplier_name`, `contact_person`, `phone`, `address`, `email`, `bank_account`, `status`) VALUES
('SUP001', '博世汽车配件有限公司', '张三', '13800138001', '上海市浦东新区张江高科技园区', 'zhangsan@bosch.com', '6222021234567890123', 1),
('SUP002', '曼牌滤清器中国分公司', '李四', '13800138002', '北京市朝阳区建国门外大街', 'lisi@mann-filter.com', '6222021234567890124', 1),
('SUP003', 'NGK火花塞中国总代理', '王五', '13800138003', '广州市天河区珠江新城', 'wangwu@ngk.com', '6222021234567890125', 1),
('SUP004', '马勒汽车配件有限公司', '赵六', '13800138004', '深圳市南山区科技园', 'zhaoliu@mahle.com', '6222021234567890126', 1);

-- ============================================
-- 2. 客户表
-- ============================================
CREATE TABLE IF NOT EXISTS `customer` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `customer_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '客户编码',
    `customer_name` VARCHAR(100) NOT NULL COMMENT '客户名称',
    `contact_person` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `address` VARCHAR(200) COMMENT '地址',
    `email` VARCHAR(100) COMMENT '邮箱',
    `credit_level` TINYINT DEFAULT 1 COMMENT '信用等级：1-5级',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_customer_code` (`customer_code`),
    INDEX `idx_customer_name` (`customer_name`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- 插入示例客户数据
INSERT INTO `customer` (`customer_code`, `customer_name`, `contact_person`, `phone`, `address`, `email`, `credit_level`, `status`) VALUES
('CUS001', '北京汽车修理厂', '陈经理', '13900139001', '北京市海淀区中关村大街', 'chen@bjrepair.com', 5, 1),
('CUS002', '上海大众汽车4S店', '刘经理', '13900139002', '上海市闵行区吴中路', 'liu@svw.com', 5, 1),
('CUS003', '广州广汽本田4S店', '黄经理', '13900139003', '广州市番禺区迎宾路', 'huang@ghac.com', 4, 1),
('CUS004', '深圳市顺达汽修', '周老板', '13900139004', '深圳市宝安区西乡街道', 'zhou@shunda.com', 3, 1),
('CUS005', '杭州吉利汽车服务站', '吴站长', '13900139005', '杭州市拱墅区石祥路', 'wu@geely.com', 4, 1);

-- ============================================
-- 3. 数据字典表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_dict` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型：category, unit, status等',
    `dict_type_name` VARCHAR(50) COMMENT '字典类型名称',
    `dict_code` VARCHAR(50) NOT NULL COMMENT '字典编码',
    `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `remark` VARCHAR(255) COMMENT '备注',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_dict_type` (`dict_type`),
    INDEX `idx_dict_code` (`dict_code`),
    UNIQUE KEY `uk_type_code` (`dict_type`, `dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

-- 插入数据字典数据
INSERT INTO `sys_dict` (`dict_type`, `dict_type_name`, `dict_code`, `dict_name`, `sort_order`, `status`) VALUES
-- 配件分类
('category', '配件分类', 'engine', '发动机系统', 1, 1),
('category', '配件分类', 'brake', '制动系统', 2, 1),
('category', '配件分类', 'transmission', '传动系统', 3, 1),
('category', '配件分类', 'electrical', '电气系统', 4, 1),
('category', '配件分类', 'chassis', '底盘系统', 5, 1),
('category', '配件分类', 'body', '车身及附件', 6, 1),
-- 单位
('unit', '计量单位', 'piece', '个', 1, 1),
('unit', '计量单位', 'set', '套', 2, 1),
('unit', '计量单位', 'pair', '对', 3, 1),
('unit', '计量单位', 'box', '盒', 4, 1),
('unit', '计量单位', 'meter', '米', 5, 1),
('unit', '计量单位', 'liter', '升', 6, 1),
-- 支付方式
('pay_type', '支付方式', 'cash', '现金', 1, 1),
('pay_type', '支付方式', 'card', '刷卡', 2, 1),
('pay_type', '支付方式', 'wechat', '微信支付', 3, 1),
('pay_type', '支付方式', 'alipay', '支付宝', 4, 1),
('pay_type', '支付方式', 'transfer', '银行转账', 5, 1);

-- ============================================
-- 4. 操作日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：ADD, UPDATE, DELETE, QUERY, EXPORT, LOGIN, LOGOUT',
    `operation_module` VARCHAR(50) COMMENT '操作模块',
    `operation_desc` VARCHAR(255) COMMENT '操作描述',
    `request_method` VARCHAR(10) COMMENT '请求方法：GET, POST, PUT, DELETE',
    `request_url` VARCHAR(255) COMMENT '请求URL',
    `request_params` TEXT COMMENT '请求参数',
    `response_data` TEXT COMMENT '响应数据',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(50) COMMENT '操作人姓名',
    `operator_ip` VARCHAR(50) COMMENT '操作IP',
    `operation_time` BIGINT COMMENT '执行时长（毫秒）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    `error_msg` TEXT COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_operation_module` (`operation_module`),
    INDEX `idx_operator_id` (`operator_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 5. 修改采购订单表，添加供应商外键
-- ============================================
ALTER TABLE `purchase_order` 
ADD COLUMN `supplier_id` BIGINT COMMENT '供应商ID' AFTER `order_no`,
ADD INDEX `idx_supplier_id` (`supplier_id`);

-- ============================================
-- 6. 修改销售订单表，添加客户外键
-- ============================================
ALTER TABLE `sales_order` 
ADD COLUMN `customer_id` BIGINT COMMENT '客户ID' AFTER `order_no`,
ADD INDEX `idx_customer_id` (`customer_id`);

-- ============================================
-- 7. 修改配件表，添加分类外键关联（使用字典）
-- ============================================
-- 注意：category字段保持varchar，与字典dict_code关联
-- 添加分类索引
ALTER TABLE `part` ADD INDEX `idx_category` (`category`);
