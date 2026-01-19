package com.djw.autopartsbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
@Data
@Schema(description = "库存信息DTO")
public class InventoryDTO {

    @Schema(description = "库存ID")
    private Long id;

    @Schema(description = "配件ID")
    private Long partId;

    @Schema(description = "配件编号")
    private String partNo;

    @Schema(description = "配件名称")
    private String partName;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "库存数量")
    private Integer quantity;

    @Schema(description = "最低库存")
    private Integer minQuantity;

    @Schema(description = "最高库存")
    private Integer maxQuantity;

    @Schema(description = "仓库位置")
    private String warehouseLocation;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
