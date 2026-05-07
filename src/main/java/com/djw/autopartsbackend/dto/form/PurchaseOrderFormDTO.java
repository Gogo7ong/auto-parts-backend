package com.djw.autopartsbackend.dto.form;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class PurchaseOrderFormDTO {
    @NotBlank(message = "供应商不能为空")
    private String supplier;
    private String supplierContact;
    private String supplierPhone;
    private String remark;
    @Valid
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemFormDTO> items;
}
