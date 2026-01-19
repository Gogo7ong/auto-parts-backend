package com.djw.autopartsbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory")
public class Inventory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long partId;

    private Integer stockQuantity;

    private String warehouseLocation;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastUpdateTime;
}
