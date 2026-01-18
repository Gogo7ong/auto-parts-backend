package com.djw.autopartsbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sales_order_item")
public class SalesOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long partId;

    private String partCode;

    private String partName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private Integer shippedQuantity;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
