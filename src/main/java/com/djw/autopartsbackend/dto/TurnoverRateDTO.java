package com.djw.autopartsbackend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author dengjiawen
 * @since 2026-01-19
 */
@Data
public class TurnoverRateDTO {

    @ExcelProperty("配件ID")
    private Long partId;

    @ExcelProperty("配件名称")
    private String partName;

    @ExcelProperty("配件编号")
    private String partCode;

    @ExcelProperty("当前库存")
    private Integer currentStock;

    @ExcelProperty("近三月出库数量")
    private Integer totalOutbound;

    @ExcelProperty("库存周转率")
    private BigDecimal turnoverRate;

    @ExcelProperty("月均出库数量")
    private BigDecimal avgMonthlyOutbound;
}
