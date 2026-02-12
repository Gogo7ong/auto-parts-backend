package com.djw.autopartsbackend.dto.form;

import lombok.Data;

import java.util.List;

@Data
public class SalesOrderFormDTO {
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String remark;
    private List<OrderItemFormDTO> items;
}

