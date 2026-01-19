package com.djw.autopartsbackend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
@Data
public class PurchaseStatisticsDTO {

    private String period;

    private Integer orderCount;

    private BigDecimal totalAmount;

    private BigDecimal avgOrderAmount;

    private Integer totalQuantity;
}
