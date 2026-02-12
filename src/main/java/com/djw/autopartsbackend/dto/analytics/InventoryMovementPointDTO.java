package com.djw.autopartsbackend.dto.analytics;

import lombok.Data;

@Data
public class InventoryMovementPointDTO {
    private String period;
    private int inboundQuantity;
    private int outboundQuantity;
    private int netQuantity;
}

