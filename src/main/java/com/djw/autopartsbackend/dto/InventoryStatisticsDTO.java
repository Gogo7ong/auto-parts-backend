package com.djw.autopartsbackend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
@Data
public class InventoryStatisticsDTO {

    private String period;

    private Integer inboundQuantity;

    private Integer outboundQuantity;

    private BigDecimal inboundAmount;

    private BigDecimal outboundAmount;

    private Integer netChange;
}
