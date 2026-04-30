package com.djw.autopartsbackend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author dengjiawen
 * @since 2026-01-19
 */
@Data
public class InventoryStatisticsDTO {

    @ExcelProperty("统计周期")
    private String period;

    @ExcelProperty("入库数量")
    private Integer inboundQuantity;

    @ExcelProperty("出库数量")
    private Integer outboundQuantity;

    @ExcelProperty("入库金额")
    private BigDecimal inboundAmount;

    @ExcelProperty("出库金额")
    private BigDecimal outboundAmount;

    @ExcelProperty("库存净变化")
    private Integer netChange;
}
