package com.djw.autopartsbackend.controller;

import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.dto.*;
import com.djw.autopartsbackend.security.RequireRole;
import com.djw.autopartsbackend.service.ExportService;
import com.djw.autopartsbackend.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * @author dengjiawen
 * @since 2026-01-19
 */
@Tag(name = "数据统计", description = "数据统计接口")
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ExportService exportService;

    @Operation(summary = "出入库统计")
    @GetMapping("/inventory")
    public Result<java.util.List<InventoryStatisticsDTO>> getInventoryStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "month") String periodType) {
        String error = validateStatisticsParams(startDate, endDate, periodType);
        if (error != null) {
            return Result.error(400, error);
        }
        periodType = normalizePeriodType(periodType);
        java.util.List<InventoryStatisticsDTO> data = statisticsService.getInventoryStatistics(startDate, endDate, periodType);
        return Result.success(data);
    }

    @Operation(summary = "库存周转率统计")
    @GetMapping("/turnover-rate")
    public Result<java.util.List<TurnoverRateDTO>> getTurnoverRateStatistics() {
        java.util.List<TurnoverRateDTO> data = statisticsService.getTurnoverRateStatistics();
        return Result.success(data);
    }

    @Operation(summary = "销售统计")
    @GetMapping("/sales")
    public Result<java.util.List<SalesStatisticsDTO>> getSalesStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "month") String periodType) {
        String error = validateStatisticsParams(startDate, endDate, periodType);
        if (error != null) {
            return Result.error(400, error);
        }
        periodType = normalizePeriodType(periodType);
        java.util.List<SalesStatisticsDTO> data = statisticsService.getSalesStatistics(startDate, endDate, periodType);
        return Result.success(data);
    }

    @Operation(summary = "采购统计")
    @GetMapping("/purchase")
    public Result<java.util.List<PurchaseStatisticsDTO>> getPurchaseStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "month") String periodType) {
        String error = validateStatisticsParams(startDate, endDate, periodType);
        if (error != null) {
            return Result.error(400, error);
        }
        periodType = normalizePeriodType(periodType);
        java.util.List<PurchaseStatisticsDTO> data = statisticsService.getPurchaseStatistics(startDate, endDate, periodType);
        return Result.success(data);
    }

    @Operation(summary = "导出销售统计报表")
    @GetMapping("/export/sales")
    @RequireRole({"ADMIN"})
    public void exportSalesStatistics(
            HttpServletResponse response,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "month") String periodType) throws Exception {
        if (writeValidationErrorIfNeeded(response, startDate, endDate, periodType)) {
            return;
        }
        periodType = normalizePeriodType(periodType);
        exportService.exportSalesStatistics(response, startDate, endDate, periodType);
    }

    @Operation(summary = "导出采购统计报表")
    @GetMapping("/export/purchase")
    @RequireRole({"ADMIN"})
    public void exportPurchaseStatistics(
            HttpServletResponse response,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "month") String periodType) throws Exception {
        if (writeValidationErrorIfNeeded(response, startDate, endDate, periodType)) {
            return;
        }
        periodType = normalizePeriodType(periodType);
        exportService.exportPurchaseStatistics(response, startDate, endDate, periodType);
    }

    private boolean writeValidationErrorIfNeeded(HttpServletResponse response,
                                                 LocalDate startDate,
                                                 LocalDate endDate,
                                                 String periodType) throws IOException {
        String error = validateStatisticsParams(startDate, endDate, periodType);
        if (error == null) {
            return false;
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":400,\"message\":\"" + error + "\"}");
        return true;
    }

    private String validateStatisticsParams(LocalDate startDate, LocalDate endDate, String periodType) {
        if (startDate == null || endDate == null) {
            return "开始日期和结束日期不能为空";
        }
        if (startDate.isAfter(endDate)) {
            return "开始日期不能晚于结束日期";
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > 366) {
            return "导出时间范围不能超过一年";
        }
        if (!Set.of("day", "month", "year").contains(normalizePeriodType(periodType))) {
            return "统计周期只能是 day、month 或 year";
        }
        return null;
    }

    private String normalizePeriodType(String periodType) {
        return periodType == null ? "month" : periodType.trim().toLowerCase();
    }
}
