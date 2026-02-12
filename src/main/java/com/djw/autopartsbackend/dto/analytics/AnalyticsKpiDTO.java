package com.djw.autopartsbackend.dto.analytics;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnalyticsKpiDTO {
    private long totalParts;
    private long lowStockCount;

    private int inventoryInboundQuantity;
    private int inventoryOutboundQuantity;
    private int inventoryNetQuantity;

    private int salesQuantity;
    private int salesOrderCount;
    private BigDecimal salesAmount;

    private int purchaseQuantity;
    private int purchaseOrderCount;
    private BigDecimal purchaseAmount;
}

