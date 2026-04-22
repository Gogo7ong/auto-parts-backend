package com.djw.autopartsbackend.common.enums;

/**
 * 库存操作类型
 *
 * @author dengjiawen
 * @since 2026-04-01
 */
public enum InventoryOperationType {

    PURCHASE_IN("PURCHASE_IN", "采购入库"),

    SALES_OUT("SALES_OUT", "销售出库"),

    RETURN_IN("RETURN_IN", "销售退货入库"),

    ADJUST("ADJUST", "手动调整"),

    SET("SET", "库存设置");

    private final String code;

    private final String description;

    InventoryOperationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
