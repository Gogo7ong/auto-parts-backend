package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.dto.SalesOrderDTO;
import com.djw.autopartsbackend.entity.SalesOrder;
import com.djw.autopartsbackend.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Tag(name = "销售管理", description = "销售订单管理接口")
@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @Operation(summary = "分页查询销售订单列表")
    @GetMapping("/page")
    public Result<PageResult<SalesOrder>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status) {
        Page<SalesOrder> pagination = new Page<>(page, pageSize);
        Page<SalesOrder> result = salesOrderService.pageQuery(pagination, orderNo, customerName, status);
        return Result.success(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "根据ID查询销售订单详情（包含明细）")
    @GetMapping("/{id}/detail")
    public Result<SalesOrderDTO> getDetail(@PathVariable Long id) {
        SalesOrderDTO dto = salesOrderService.getOrderWithItems(id);
        return Result.success(dto);
    }

    @Operation(summary = "根据ID查询销售订单")
    @GetMapping("/{id}")
    public Result<SalesOrder> getById(@PathVariable Long id) {
        SalesOrder order = salesOrderService.getById(id);
        return Result.success(order);
    }

    @Operation(summary = "新增销售订单（包含明细）")
    @PostMapping("/with-items")
    public Result<Void> addWithItems(@RequestBody SalesOrderDTO dto) {
        salesOrderService.createOrderWithItems(dto);
        return Result.success();
    }

    @Operation(summary = "新增销售订单")
    @PostMapping
    public Result<Void> add(@RequestBody SalesOrder order) {
        salesOrderService.save(order);
        return Result.success();
    }

    @Operation(summary = "更新销售订单")
    @PutMapping
    public Result<Void> update(@RequestBody SalesOrder order) {
        salesOrderService.updateById(order);
        return Result.success();
    }

    @Operation(summary = "出库")
    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id, @RequestParam Long warehouseUserId, @RequestParam String warehouseUserName) {
        boolean success = salesOrderService.shipOrder(id, warehouseUserId, warehouseUserName);
        return success ? Result.success() : Result.error("出库失败");
    }

    @Operation(summary = "完成销售订单")
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        boolean success = salesOrderService.completeOrder(id);
        return success ? Result.success() : Result.error("操作失败");
    }

    @Operation(summary = "退货")
    @PutMapping("/{id}/return")
    public Result<Void> returnOrder(@PathVariable Long id) {
        boolean success = salesOrderService.returnOrder(id);
        return success ? Result.success() : Result.error("操作失败");
    }

    @Operation(summary = "删除销售订单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        salesOrderService.removeById(id);
        return Result.success();
    }
}
