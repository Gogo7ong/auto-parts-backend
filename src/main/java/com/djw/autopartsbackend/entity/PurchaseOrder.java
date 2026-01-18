package com.djw.autopartsbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("purchase_order")
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private String supplier;

    private String supplierContact;

    private String supplierPhone;

    private BigDecimal totalAmount;

    private String status;

    private Long createUserId;

    private String createUserName;

    private Long approveUserId;

    private String approveUserName;

    private LocalDateTime approveTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
