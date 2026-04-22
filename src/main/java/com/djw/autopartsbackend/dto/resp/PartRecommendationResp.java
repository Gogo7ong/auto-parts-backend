package com.djw.autopartsbackend.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 配件推荐响应 - 结构化输出
 * AI返回的配件推荐结果，包含推荐理由
 *
 * @author dengjiawen
 * @since 2026-04-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "配件推荐响应")
public class PartRecommendationResp {

    @Schema(description = "推荐的配件列表")
    private List<RecommendedPart> parts;

    @Schema(description = "推荐总结")
    private String summary;

    @Schema(description = "是否需要人工确认")
    private Boolean needHumanConfirm;

    /**
     * 推荐的配件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "推荐的配件")
    public static class RecommendedPart {

        @Schema(description = "配件编号")
        private String partCode;

        @Schema(description = "配件名称")
        private String partName;

        @Schema(description = "品牌")
        private String brand;

        @Schema(description = "单价")
        private BigDecimal unitPrice;

        @Schema(description = "库存数量")
        private Integer stockQuantity;

        @Schema(description = "推荐理由")
        private String reason;

        @Schema(description = "匹配度评分（0-100）")
        private Integer matchScore;

        @Schema(description = "仓库位置")
        private String warehouseLocation;
    }
}
