package com.djw.autopartsbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("part")
public class Part {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String partCode;

    private String partName;

    private String specification;

    private String brand;

    private String supplier;

    private BigDecimal unitPrice;

    private String category;

    private String unit;

    private Integer minStock;

    private String description;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
