package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Tag(name = "库存管理", description = "库存管理接口")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Operation(summary = "分页查询库存列表")
    @GetMapping("/page")
    public Result<PageResult<Inventory>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Inventory> page = new Page<>(current, size);
        Page<Inventory> result = inventoryService.pageQuery(page, keyword);
        return Result.success(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "根据配件ID查询库存")
    @GetMapping("/part/{partId}")
    public Result<Inventory> getByPartId(@PathVariable Long partId) {
        Inventory inventory = inventoryService.getByPartId(partId);
        return Result.success(inventory);
    }

    @Operation(summary = "获取低库存预警配件列表")
    @GetMapping("/low-stock")
    public Result<List<Inventory>> getLowStockParts() {
        List<Inventory> list = inventoryService.getLowStockParts();
        return Result.success(list);
    }

    @Operation(summary = "更新库存数量")
    @PutMapping("/stock")
    public Result<Void> updateStock(@RequestParam Long partId, @RequestParam Integer quantity) {
        boolean success = inventoryService.updateStock(partId, quantity);
        return success ? Result.success() : Result.error("更新失败");
    }
}
