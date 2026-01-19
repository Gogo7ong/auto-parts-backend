package com.djw.autopartsbackend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
@Data
public class TurnoverRateDTO {

    private Long partId;

    private String partName;

    private String partCode;

    private Integer currentStock;

    private Integer totalOutbound;

    private BigDecimal turnoverRate;

    private BigDecimal avgMonthlyOutbound;
}
