package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.dto.PurchaseOrderDTO;
import com.djw.autopartsbackend.entity.PurchaseOrder;
import com.djw.autopartsbackend.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

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
    public Result<Void> addWithItems(@RequestBody PurchaseOrderDTO dto, HttpServletRequest request) {
        // 设置创建人信息
        Map<String, Object> userInfo = getUserInfo(request);
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
    public Result<Void> add(@RequestBody PurchaseOrder order, HttpServletRequest request) {
        // 自动生成订单编号
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            String orderNo = generateOrderNo();
            order.setOrderNo(orderNo);
        }
        
        // 设置创建人信息
        Map<String, Object> userInfo = getUserInfo(request);
        if (order.getCreateUserId() == null) {
            order.setCreateUserId((Long) userInfo.get("userId"));
        }
        if (order.getCreateUserName() == null) {
            order.setCreateUserName((String) userInfo.get("username"));
        }
        
        purchaseOrderService.save(order);
        return Result.success();
    }
    
    private Map<String, Object> getUserInfo(HttpServletRequest request) {
        Map<String, Object> userInfo = new java.util.HashMap<>();
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
    
    private String generateOrderNo() {
        // 生成采购订单编号：PO + 年月日 + 3位随机数
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = (int) (Math.random() * 900) + 100;
        return "PO" + dateStr + randomNum;
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
