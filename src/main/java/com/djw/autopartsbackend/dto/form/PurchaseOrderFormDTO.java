package com.djw.autopartsbackend.dto.form;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseOrderFormDTO {
    private String supplier;
    private String supplierContact;
    private String supplierPhone;
    private String remark;
    private List<OrderItemFormDTO> items;
}

