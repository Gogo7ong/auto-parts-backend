package com.djw.autopartsbackend.dto.form;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class SalesOrderFormDTO {
    @NotBlank(message = "客户名称不能为空")
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String remark;
    @Valid
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemFormDTO> items;
}
