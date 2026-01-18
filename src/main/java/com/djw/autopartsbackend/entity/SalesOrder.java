package com.djw.autopartsbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sales_order")
public class SalesOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private String customerName;

    private String customerPhone;

    private String customerAddress;

    private BigDecimal totalAmount;

    private String status;

    private Long createUserId;

    private String createUserName;

    private Long warehouseUserId;

    private String warehouseUserName;

    private LocalDateTime warehouseTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
