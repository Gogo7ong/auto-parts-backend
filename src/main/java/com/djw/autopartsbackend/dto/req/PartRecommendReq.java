package com.djw.autopartsbackend.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 配件推荐请求
 *
 * @author dengjiawen
 * @since 2026-04-01
 */
@Schema(description = "配件推荐请求")
public class PartRecommendReq {

    @Schema(description = "车型", required = true, example = "大众帕萨特")
    @NotBlank(message = "车型不能为空")
    private String carModel;

    @Schema(description = "配件分类", required = true, example = "滤清器")
    @NotBlank(message = "配件分类不能为空")
    private String category;

    @Schema(description = "预算范围", example = "100-500元")
    private String budget;

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }
}
