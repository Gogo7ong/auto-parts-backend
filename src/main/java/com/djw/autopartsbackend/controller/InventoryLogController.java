package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.entity.InventoryLog;
import com.djw.autopartsbackend.service.InventoryLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author dengjiawen
 * @since 2026-01-19
 */
@Tag(name = "库存流水", description = "库存流水查询接口")
@RestController
@RequestMapping("/api/inventory-logs")
public class InventoryLogController {

    @Autowired
    private InventoryLogService inventoryLogService;

    @Operation(summary = "分页查询库存流水")
    @GetMapping("/page")
    public Result<PageResult<InventoryLog>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long partId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String relatedOrderNo,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<InventoryLog> page = new Page<>(current, size);
        Page<InventoryLog> result = inventoryLogService.pageQuery(page,
                partId,
                operationType,
                relatedOrderNo,
                parseStartTime(startTime),
                parseEndTime(endTime));
        return Result.success(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "根据ID查询库存流水详情")
    @GetMapping("/{id}")
    public Result<InventoryLog> getById(@PathVariable Long id) {
        InventoryLog log = inventoryLogService.getById(id);
        return Result.success(log);
    }

    /**
     * 解析开始时间
     *
     * @param startTime 开始时间字符串
     * @return 开始时间
     */
    private LocalDateTime parseStartTime(String startTime) {
        if (!StringUtils.hasText(startTime)) {
            return null;
        }
        return LocalDate.parse(startTime).atStartOfDay();
    }

    /**
     * 解析结束时间
     *
     * @param endTime 结束时间字符串
     * @return 结束时间
     */
    private LocalDateTime parseEndTime(String endTime) {
        if (!StringUtils.hasText(endTime)) {
            return null;
        }
        return LocalDate.parse(endTime).atTime(23, 59, 59);
    }
}
