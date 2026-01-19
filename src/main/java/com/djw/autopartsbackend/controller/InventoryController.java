package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.dto.InventoryDTO;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public Result<PageResult<InventoryDTO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") Boolean lowStock) {
        Page<InventoryDTO> pagination = new Page<>(page, pageSize);
        Page<InventoryDTO> result = inventoryService.pageQueryWithPartInfo(pagination, keyword, lowStock);
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
    public Result<List<InventoryDTO>> getLowStockParts() {
        List<InventoryDTO> list = inventoryService.getLowStockPartsWithInfo();
        return Result.success(list);
    }

    @Operation(summary = "调整库存")
    @PostMapping("/adjust")
    public Result<Void> adjustInventory(@RequestBody Map<String, Object> data) {
        Long partId = Long.valueOf(data.get("partId").toString());
        Integer adjustQuantity = Integer.valueOf(data.get("adjustQuantity").toString());
        String reason = data.get("reason").toString();
        boolean success = inventoryService.adjustStock(partId, adjustQuantity, reason);
        return success ? Result.success() : Result.error("调整失败");
    }

    @Operation(summary = "更新库存数量")
    @PutMapping("/stock")
    public Result<Void> updateStock(@RequestParam Long partId, @RequestParam Integer quantity) {
        boolean success = inventoryService.updateStock(partId, quantity);
        return success ? Result.success() : Result.error("更新失败");
    }
}
