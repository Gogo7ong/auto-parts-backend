package com.djw.autopartsbackend.controller;

import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 仪表板统计接口
 *
 * @author dengjiawen
 * @since 2025-01-27
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表板统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = dashboardService.getStats();
        return Result.success(stats);
    }
}
