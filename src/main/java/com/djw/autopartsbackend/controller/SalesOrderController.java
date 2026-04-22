package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.common.annotation.OperationLog;
import com.djw.autopartsbackend.common.annotation.OperationType;
import com.djw.autopartsbackend.dto.SalesOrderDTO;
import com.djw.autopartsbackend.dto.form.OrderItemFormDTO;
import com.djw.autopartsbackend.dto.form.SalesOrderFormDTO;
import com.djw.autopartsbackend.entity.SalesOrder;
import com.djw.autopartsbackend.entity.SalesOrderItem;
import com.djw.autopartsbackend.mapper.UserMapper;
import com.djw.autopartsbackend.security.JwtService;
import com.djw.autopartsbackend.security.RequireRole;
import com.djw.autopartsbackend.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author dengjiawen
 * @since 2026-01-18
 */
@Tag(name = "销售管理", description = "销售订单管理接口")
@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserMapper userMapper;

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
    @RequireRole({"ADMIN", "SALESMAN"})
    public Result<Void> addWithItems(
            @RequestBody SalesOrderDTO dto,
            @RequestHeader(value = "token", required = false) String token) {
        fillCreateUserInfo(dto, token);
        salesOrderService.createOrderWithItems(dto);
        return Result.success();
    }

    @OperationLog(module = "销售管理", type = OperationType.CREATE, description = "新增销售订单")
    @Operation(summary = "新增销售订单")
    @PostMapping
    @RequireRole({"ADMIN", "SALESMAN"})
    public Result<Void> add(
            @RequestBody SalesOrderFormDTO form,
            @RequestHeader(value = "token", required = false) String token) {
        SalesOrderDTO dto = toSalesOrderDTO(form);
        fillCreateUserInfo(dto, token);
        salesOrderService.createOrderWithItems(dto);
        return Result.success();
    }

    @Operation(summary = "更新销售订单（包含明细）")
    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "SALESMAN"})
    public Result<Void> updateWithItems(@PathVariable Long id, @RequestBody SalesOrderFormDTO form) {
        boolean success = salesOrderService.updateOrderWithItems(id, toSalesOrderDTO(form));
        return success ? Result.success() : Result.error("更新失败");
    }

    @Operation(summary = "更新销售订单")
    @PutMapping
    @RequireRole({"ADMIN", "SALESMAN"})
    public Result<Void> update(@RequestBody SalesOrder order) {
        salesOrderService.updateById(order);
        return Result.success();
    }

    @OperationLog(module = "销售管理", type = OperationType.INVENTORY_OUT, description = "销售出库")
    @Operation(summary = "出库")
    @PutMapping("/{id}/ship")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    public Result<Void> ship(
            @PathVariable Long id,
            @RequestHeader(value = "token", required = false) String token,
            @RequestParam(required = false) Long warehouseUserId,
            @RequestParam(required = false) String warehouseUserName) {
        if (warehouseUserId == null || warehouseUserName == null || warehouseUserName.isEmpty()) {
            if (token == null || token.isEmpty()) {
                return Result.error(401, "未登录");
            }
            Long userId = jwtService.parseUserId(token);
            var user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error(401, "账号不存在或已禁用");
            }
            warehouseUserId = user.getId();
            warehouseUserName = user.getUsername();
        }

        boolean success = salesOrderService.shipOrder(id, warehouseUserId, warehouseUserName);
        return success ? Result.success() : Result.error("出库失败");
    }

    @OperationLog(module = "销售管理", type = OperationType.UPDATE, description = "完成销售订单")
    @Operation(summary = "完成销售订单")
    @PutMapping("/{id}/complete")
    @RequireRole({"ADMIN", "SALESMAN"})
    public Result<Void> complete(@PathVariable Long id) {
        boolean success = salesOrderService.completeOrder(id);
        return success ? Result.success() : Result.error("操作失败");
    }

    @OperationLog(module = "销售管理", type = OperationType.INVENTORY_IN, description = "销售退货")
    @Operation(summary = "退货")
    @PutMapping("/{id}/return")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    public Result<Void> returnOrder(@PathVariable Long id) {
        boolean success = salesOrderService.returnOrder(id);
        return success ? Result.success() : Result.error("操作失败");
    }

    @OperationLog(module = "销售管理", type = OperationType.DELETE, description = "删除销售订单")
    @Operation(summary = "删除销售订单")
    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> delete(@PathVariable Long id) {
        salesOrderService.removeById(id);
        return Result.success();
    }

    private SalesOrderDTO toSalesOrderDTO(SalesOrderFormDTO form) {
        SalesOrder order = new SalesOrder();
        order.setCustomerName(form.getCustomerName());
        order.setCustomerPhone(form.getCustomerPhone());
        order.setCustomerAddress(form.getCustomerAddress());
        order.setRemark(form.getRemark());

        List<SalesOrderItem> items = Optional.ofNullable(form.getItems()).orElse(List.of()).stream()
                .map(this::toSalesItem)
                .collect(Collectors.toList());

        SalesOrderDTO dto = new SalesOrderDTO();
        dto.setOrder(order);
        dto.setItems(items);
        return dto;
    }

    /**
     * 填充创建用户信息
     *
     * @param dto   销售订单DTO
     * @param token 用户token
     */
    private void fillCreateUserInfo(SalesOrderDTO dto, String token) {
        if (dto.getOrder() == null) {
            return;
        }
        if (token != null && !token.isEmpty()) {
            Long userId = jwtService.parseUserId(token);
            var user = userMapper.selectById(userId);
            if (user != null) {
                dto.getOrder().setCreateUserId(user.getId());
                dto.getOrder().setCreateUserName(user.getRealName());
            }
        }
    }

    private SalesOrderItem toSalesItem(OrderItemFormDTO item) {
        SalesOrderItem entity = new SalesOrderItem();
        entity.setPartId(item.getPartId());
        entity.setPartCode(item.getPartNo());
        entity.setPartName(item.getPartName());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getPrice());
        if (item.getPrice() != null && item.getQuantity() != null) {
            entity.setTotalPrice(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        return entity;
    }
}

