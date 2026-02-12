package com.djw.autopartsbackend.dto.analytics;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderAmountPointDTO {
    private String period;
    private int orderCount;
    private int quantity;
    private BigDecimal amount;
}

