package com.djw.autopartsbackend.dto.analytics;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TurnoverPartDTO {
    private Long partId;
    private String partCode;
    private String partName;

    private int salesQuantity;
    private int inboundQuantity;
    private int outboundQuantity;

    private int startStock;
    private int endStock;
    private BigDecimal avgInventory;
    private BigDecimal turnoverRate;
}

