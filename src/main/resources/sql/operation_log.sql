-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    `module` VARCHAR(50) COMMENT '操作模块',
    `operation_type` VARCHAR(20) COMMENT '操作类型',
    `description` VARCHAR(255) COMMENT '操作描述',
    `request_method` VARCHAR(10) COMMENT '请求方法',
    `request_url` VARCHAR(500) COMMENT '请求 URL',
    `request_params` TEXT COMMENT '请求参数',
    `response_result` TEXT COMMENT '响应结果',
    `operator_id` BIGINT COMMENT '操作人 ID',
    `operator_name` VARCHAR(50) COMMENT '操作人姓名',
    `operator_ip` VARCHAR(50) COMMENT '操作人 IP',
    `execution_time` BIGINT COMMENT '执行时长（毫秒）',
    `status` TINYINT DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
    `error_message` VARCHAR(1000) COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_operator_id` (`operator_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
