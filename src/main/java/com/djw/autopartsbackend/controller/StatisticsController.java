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

import java.time.LocalDate;

/**
 * @author dengjiawen
 * @since 2025-01-19
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
        java.util.List<SalesStatisticsDTO> data = statisticsService.getSalesStatistics(startDate, endDate, periodType);
        return Result.success(data);
    }

    @Operation(summary = "采购统计")
    @GetMapping("/purchase")
    public Result<java.util.List<PurchaseStatisticsDTO>> getPurchaseStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "month") String periodType) {
        java.util.List<PurchaseStatisticsDTO> data = statisticsService.getPurchaseStatistics(startDate, endDate, periodType);
        return Result.success(data);
    }

    @Operation(summary = "导出出入库统计报表")
    @GetMapping("/export/inventory")
    @RequireRole({"ADMIN"})
    public void exportInventoryStatistics(
            HttpServletResponse response,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "month") String periodType) throws Exception {
        exportService.exportInventoryStatistics(response, startDate, endDate, periodType);
    }

    @Operation(summary = "导出库存周转率统计报表")
    @GetMapping("/export/turnover-rate")
    @RequireRole({"ADMIN"})
    public void exportTurnoverRateStatistics(HttpServletResponse response) throws Exception {
        exportService.exportTurnoverRateStatistics(response);
    }

    @Operation(summary = "导出销售统计报表")
    @GetMapping("/export/sales")
    @RequireRole({"ADMIN"})
    public void exportSalesStatistics(
            HttpServletResponse response,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "month") String periodType) throws Exception {
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
        exportService.exportPurchaseStatistics(response, startDate, endDate, periodType);
    }
}
