package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.dto.PurchaseOrderDTO;
import com.djw.autopartsbackend.dto.form.OrderItemFormDTO;
import com.djw.autopartsbackend.dto.form.PurchaseOrderFormDTO;
import com.djw.autopartsbackend.entity.PurchaseOrder;
import com.djw.autopartsbackend.entity.PurchaseOrderItem;
import com.djw.autopartsbackend.mapper.UserMapper;
import com.djw.autopartsbackend.security.JwtService;
import com.djw.autopartsbackend.security.RequireRole;
import com.djw.autopartsbackend.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "分页查询采购订单列表")
    @GetMapping("/page")
    public Result<PageResult<PurchaseOrder>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String status) {
        Page<PurchaseOrder> pagination = new Page<>(page, pageSize);
        Page<PurchaseOrder> result = purchaseOrderService.pageQuery(pagination, orderNo, supplier, status);
        return Result.success(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "根据ID查询采购订单详情（包含明细）")
    @GetMapping("/{id}/detail")
    public Result<PurchaseOrderDTO> getDetail(@PathVariable Long id) {
        PurchaseOrderDTO dto = purchaseOrderService.getOrderWithItems(id);
        return Result.success(dto);
    }

    @Operation(summary = "根据ID查询采购订单")
    @GetMapping("/{id}")
    public Result<PurchaseOrder> getById(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderService.getById(id);
        return Result.success(order);
    }

    @Operation(summary = "新增采购订单（包含明细）")
    @PostMapping("/with-items")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    public Result<Void> addWithItems(@RequestBody PurchaseOrderDTO dto, HttpServletRequest request) {
        // 兼容旧逻辑：允许通过 header 传创建人信息
        var userInfo = getUserInfo(request);
        PurchaseOrder order = dto.getOrder();
        if (order.getCreateUserId() == null) {
            order.setCreateUserId((Long) userInfo.get("userId"));
        }
        if (order.getCreateUserName() == null) {
            order.setCreateUserName((String) userInfo.get("username"));
        }
        purchaseOrderService.createOrderWithItems(dto);
        return Result.success();
    }

    @Operation(summary = "新增采购订单")
    @PostMapping
    @RequireRole({"ADMIN", "WAREHOUSE"})
    public Result<Void> add(@RequestBody PurchaseOrderFormDTO form, HttpServletRequest request) {
        PurchaseOrderDTO dto = toPurchaseOrderDTO(form);

        // 兼容旧逻辑：允许通过 header 传创建人信息
        var userInfo = getUserInfo(request);
        PurchaseOrder order = dto.getOrder();
        if (order.getCreateUserId() == null) {
            order.setCreateUserId((Long) userInfo.get("userId"));
        }
        if (order.getCreateUserName() == null) {
            order.setCreateUserName((String) userInfo.get("username"));
        }

        purchaseOrderService.createOrderWithItems(dto);
        return Result.success();
    }

    @Operation(summary = "更新采购订单（包含明细）")
    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "WAREHOUSE"})
    public Result<Void> updateWithItems(@PathVariable Long id, @RequestBody PurchaseOrderFormDTO form) {
        boolean success = purchaseOrderService.updateOrderWithItems(id, toPurchaseOrderDTO(form));
        return success ? Result.success() : Result.error("更新失败");
    }

    @Operation(summary = "更新采购订单")
    @PutMapping
    @RequireRole({"ADMIN", "WAREHOUSE"})
    public Result<Void> update(@RequestBody PurchaseOrder order) {
        purchaseOrderService.updateById(order);
        return Result.success();
    }

    @Operation(summary = "审核采购订单")
    @PutMapping("/{id}/approve")
    @RequireRole({"ADMIN"})
    public Result<Void> approve(
            @PathVariable Long id,
            @RequestHeader(value = "token", required = false) String token,
            @RequestParam(required = false) Long approveUserId,
            @RequestParam(required = false) String approveUserName
    ) {
        if (approveUserId == null || approveUserName == null || approveUserName.isEmpty()) {
            if (token == null || token.isEmpty()) {
                return Result.error(401, "未登录");
            }
            Long userId = jwtService.parseUserId(token);
            var user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error(401, "账号不存在或已禁用");
            }
            approveUserId = user.getId();
            approveUserName = user.getUsername();
        }

        boolean success = purchaseOrderService.approveOrder(id, approveUserId, approveUserName);
        return success ? Result.success() : Result.error("审核失败");
    }

    @Operation(summary = "完成采购订单")
    @PutMapping("/{id}/complete")
    @RequireRole({"ADMIN"})
    public Result<Void> complete(@PathVariable Long id) {
        boolean success = purchaseOrderService.completeOrder(id);
        return success ? Result.success() : Result.error("操作失败");
    }

    @Operation(summary = "删除采购订单")
    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> delete(@PathVariable Long id) {
        purchaseOrderService.removeById(id);
        return Result.success();
    }

    private java.util.Map<String, Object> getUserInfo(HttpServletRequest request) {
        java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
        String userId = request.getHeader("userId");
        String username = request.getHeader("username");
        if (userId == null || userId.isEmpty()) {
            userId = "1";
        }
        if (username == null || username.isEmpty()) {
            username = "系统管理员";
        }
        userInfo.put("userId", Long.parseLong(userId));
        userInfo.put("username", username);
        return userInfo;
    }

    @SuppressWarnings("unused")
    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = (int) (Math.random() * 900) + 100;
        return "PO" + dateStr + randomNum;
    }

    private PurchaseOrderDTO toPurchaseOrderDTO(PurchaseOrderFormDTO form) {
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(form.getSupplier());
        order.setSupplierContact(form.getSupplierContact());
        order.setSupplierPhone(form.getSupplierPhone());
        order.setRemark(form.getRemark());

        List<PurchaseOrderItem> items = Optional.ofNullable(form.getItems()).orElse(List.of()).stream()
                .map(this::toPurchaseItem)
                .collect(Collectors.toList());

        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setOrder(order);
        dto.setItems(items);
        return dto;
    }

    private PurchaseOrderItem toPurchaseItem(OrderItemFormDTO item) {
        PurchaseOrderItem entity = new PurchaseOrderItem();
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

