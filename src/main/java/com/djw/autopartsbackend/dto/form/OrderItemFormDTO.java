package com.djw.autopartsbackend.dto.form;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemFormDTO {
    private Long partId;
    private String partNo;
    private String partName;
    private Integer quantity;
    private BigDecimal price;
}

