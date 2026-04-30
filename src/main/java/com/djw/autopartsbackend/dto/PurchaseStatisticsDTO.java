package com.djw.autopartsbackend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author dengjiawen
 * @since 2026-01-19
 */
@Data
public class PurchaseStatisticsDTO {

    @ExcelProperty("统计周期")
    private String period;

    @ExcelProperty("订单数量")
    private Integer orderCount;

    @ExcelProperty("采购总额")
    private BigDecimal totalAmount;

    @ExcelProperty("平均订单金额")
    private BigDecimal avgOrderAmount;

    @ExcelProperty("采购数量")
    private Integer totalQuantity;
}
