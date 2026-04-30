package com.djw.autopartsbackend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author dengjiawen
 * @since 2026-01-19
 */
@Data
public class SalesStatisticsDTO {

    @ExcelProperty("统计周期")
    private String period;

    @ExcelProperty("订单数量")
    private Integer orderCount;

    @ExcelProperty("销售总额")
    private BigDecimal totalAmount;

    @ExcelProperty("平均订单金额")
    private BigDecimal avgOrderAmount;

    @ExcelProperty("销售数量")
    private Integer totalQuantity;
}
