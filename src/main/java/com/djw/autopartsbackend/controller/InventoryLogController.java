package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.entity.InventoryLog;
import com.djw.autopartsbackend.service.InventoryLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author dengjiawen
 * @since 2025-01-19
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
            @RequestParam(required = false) String relatedOrderNo) {
        Page<InventoryLog> page = new Page<>(current, size);
        Page<InventoryLog> result = inventoryLogService.pageQuery(page, partId, operationType, relatedOrderNo);
        return Result.success(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "根据ID查询库存流水详情")
    @GetMapping("/{id}")
    public Result<InventoryLog> getById(@PathVariable Long id) {
        InventoryLog log = inventoryLogService.getById(id);
        return Result.success(log);
    }
}
