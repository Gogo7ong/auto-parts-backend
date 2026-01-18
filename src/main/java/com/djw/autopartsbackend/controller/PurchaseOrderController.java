package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.entity.PurchaseOrder;
import com.djw.autopartsbackend.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Tag(name = "采购管理", description = "采购订单管理接口")
@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Operation(summary = "分页查询采购订单列表")
    @GetMapping("/page")
    public Result<PageResult<PurchaseOrder>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String status) {
        Page<PurchaseOrder> page = new Page<>(current, size);
        Page<PurchaseOrder> result = purchaseOrderService.pageQuery(page, orderNo, supplier, status);
        return Result.success(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "根据ID查询采购订单详情")
    @GetMapping("/{id}")
    public Result<PurchaseOrder> getById(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderService.getById(id);
        return Result.success(order);
    }

    @Operation(summary = "新增采购订单")
    @PostMapping
    public Result<Void> add(@RequestBody PurchaseOrder order) {
        purchaseOrderService.save(order);
        return Result.success();
    }

    @Operation(summary = "更新采购订单")
    @PutMapping
    public Result<Void> update(@RequestBody PurchaseOrder order) {
        purchaseOrderService.updateById(order);
        return Result.success();
    }

    @Operation(summary = "审核采购订单")
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestParam Long approveUserId, @RequestParam String approveUserName) {
        boolean success = purchaseOrderService.approveOrder(id, approveUserId, approveUserName);
        return success ? Result.success() : Result.error("审核失败");
    }

    @Operation(summary = "完成采购订单")
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        boolean success = purchaseOrderService.completeOrder(id);
        return success ? Result.success() : Result.error("操作失败");
    }

    @Operation(summary = "删除采购订单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseOrderService.removeById(id);
        return Result.success();
    }
}
