package com.djw.autopartsbackend.dto.analytics;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AnalyticsOverviewDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String granularity;

    private AnalyticsKpiDTO kpi;

    private List<InventoryMovementPointDTO> inventoryMovement;
    private List<OrderAmountPointDTO> sales;
    private List<OrderAmountPointDTO> purchase;
    private List<TurnoverPartDTO> turnoverTop;
}

