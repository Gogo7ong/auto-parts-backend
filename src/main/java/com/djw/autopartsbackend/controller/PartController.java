package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.common.annotation.OperationLog;
import com.djw.autopartsbackend.common.annotation.OperationType;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.entity.PurchaseOrderItem;
import com.djw.autopartsbackend.entity.SalesOrderItem;
import com.djw.autopartsbackend.mapper.InventoryMapper;
import com.djw.autopartsbackend.mapper.PurchaseOrderItemMapper;
import com.djw.autopartsbackend.mapper.SalesOrderItemMapper;
import com.djw.autopartsbackend.security.RequireRole;
import com.djw.autopartsbackend.service.PartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dengjiawen
 * @since 2026-01-18
 */
@Tag(name = "配件管理", description = "配件信息管理接口")
@RestController
@RequestMapping("/api/parts")
public class PartController {

    @Autowired
    private PartService partService;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private SalesOrderItemMapper salesOrderItemMapper;

    @Operation(summary = "获取所有配件（用于下拉选择）")
    @GetMapping("/all")
    public Result<List<Part>> getAll() {
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Part::getStatus, 1);
        List<Part> list = partService.list(wrapper);
        return Result.success(list);
    }

    @Operation(summary = "分页查询配件列表")
    @GetMapping("/page")
    public Result<PageResult<Part>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String partCode,
            @RequestParam(required = false) String partName,
            @RequestParam(required = false) String category) {
        Page<Part> pagination = new Page<>(page, pageSize);
        Page<Part> result = partService.pageQuery(pagination, partCode, partName, category);
        return Result.success(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "根据ID查询配件详情")
    @GetMapping("/{id}")
    public Result<Part> getById(@PathVariable Long id) {
        Part part = partService.getById(id);
        return Result.success(part);
    }

    @OperationLog(module = "配件管理", type = OperationType.CREATE, description = "新增配件")
    @Operation(summary = "新增配件")
    @PostMapping
    @RequireRole({"ADMIN", "WAREHOUSE"})
    public Result<Void> add(@RequestBody Part part) {
        if (partService.checkPartCodeExists(part.getPartCode(), null)) {
            return Result.error("配件编号已存在");
        }
        partService.save(part);
        return Result.success();
    }

    @OperationLog(module = "配件管理", type = OperationType.UPDATE, description = "更新配件信息")
    @Operation(summary = "更新配件信息")
    @PutMapping
    @RequireRole({"ADMIN", "WAREHOUSE"})
    public Result<Void> update(@RequestBody Part part) {
        if (partService.checkPartCodeExists(part.getPartCode(), part.getId())) {
            return Result.error("配件编号已存在");
        }
        partService.updateById(part);
        return Result.success();
    }

    @OperationLog(module = "配件管理", type = OperationType.DELETE, description = "删除配件")
    @Operation(summary = "删除配件")
    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    public Result<Void> delete(@PathVariable Long id) {
        Integer stockQuantity = inventoryMapper.selectList(new LambdaQueryWrapper<com.djw.autopartsbackend.entity.Inventory>()
                        .eq(com.djw.autopartsbackend.entity.Inventory::getPartId, id))
                .stream()
                .map(com.djw.autopartsbackend.entity.Inventory::getStockQuantity)
                .filter(java.util.Objects::nonNull)
                .reduce(0, Integer::sum);
        if (stockQuantity != 0) {
            return Result.error(400, "配件仍有库存，不能删除");
        }
        Long purchaseRefs = purchaseOrderItemMapper.selectCount(new LambdaQueryWrapper<PurchaseOrderItem>()
                .eq(PurchaseOrderItem::getPartId, id));
        Long salesRefs = salesOrderItemMapper.selectCount(new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getPartId, id));
        if (purchaseRefs > 0 || salesRefs > 0) {
            return Result.error(400, "配件已被订单引用，不能删除");
        }
        partService.removeById(id);
        return Result.success();
    }
}
