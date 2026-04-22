package com.djw.autopartsbackend.common.annotation;

/**
 * 操作类型枚举
 * 
 * @author dengjiawen
 * @since 2026-02-17
 */
public enum OperationType {

    /**
     * 新增
     */
    CREATE("新增"),

    /**
     * 修改
     */
    UPDATE("修改"),

    /**
     * 删除
     */
    DELETE("删除"),

    /**
     * 查询
     */
    QUERY("查询"),

    /**
     * 导入
     */
    IMPORT("导入"),

    /**
     * 导出
     */
    EXPORT("导出"),

    /**
     * 登录
     */
    LOGIN("登录"),

    /**
     * 登出
     */
    LOGOUT("登出"),

    /**
     * 授权
     */
    GRANT("授权"),

    /**
     * 审核
     */
    APPROVE("审核"),

    /**
     * 入库
     */
    INVENTORY_IN("入库"),

    /**
     * 出库
     */
    INVENTORY_OUT("出库"),

    /**
     * 其他
     */
    OTHER("其他");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
