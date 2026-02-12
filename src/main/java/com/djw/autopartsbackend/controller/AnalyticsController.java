package com.djw.autopartsbackend.controller;

import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.dto.analytics.AnalyticsOverviewDTO;
import com.djw.autopartsbackend.security.RequireRole;
import com.djw.autopartsbackend.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "数据分析", description = "新版统计分析接口")
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Operation(summary = "统计概览（一次返回：KPI + 时间序列 + 周转Top）")
    @GetMapping("/overview")
    @RequireRole({"ADMIN", "WAREHOUSE", "SALESMAN"})
    public Result<AnalyticsOverviewDTO> overview(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "day") String granularity,
            @RequestParam(defaultValue = "10") Integer topN
    ) {
        return Result.success(analyticsService.getOverview(startDate, endDate, granularity, topN));
    }
}
