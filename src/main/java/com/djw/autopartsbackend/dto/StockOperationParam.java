package com.djw.autopartsbackend.dto;

import com.djw.autopartsbackend.common.enums.InventoryOperationType;
import lombok.Data;

/**
 * 库存操作参数
 *
 * @author dengjiawen
 * @since 2026-04-01
 */
@Data
public class StockOperationParam {

    private Long partId;

    private InventoryOperationType operationType;

    private Integer changeQuantity;

    private Integer targetQuantity;

    private Long operatorId;

    private String operatorName;

    private String relatedOrderNo;

    private String remark;
}
