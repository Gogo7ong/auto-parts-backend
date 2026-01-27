package com.djw.autopartsbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory_log")
public class InventoryLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long partId;

    private String operationType;

    private Integer quantity;

    private Integer beforeQuantity;

    private Integer afterQuantity;

    private Long operatorId;

    private String operatorName;

    private String relatedOrderNo;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 配件编号（非数据库字段，关联查询填充）
     */
    @TableField(exist = false)
    private String partNo;

    /**
     * 配件名称（非数据库字段，关联查询填充）
     */
    @TableField(exist = false)
    private String partName;
}
