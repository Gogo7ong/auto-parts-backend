package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.djw.autopartsbackend.common.enums.InventoryOperationType;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.InventoryLog;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.service.DashboardService;
import com.djw.autopartsbackend.service.InventoryService;
import com.djw.autopartsbackend.service.InventoryLogService;
import com.djw.autopartsbackend.service.PartService;
import com.djw.autopartsbackend.service.PurchaseOrderService;
import com.djw.autopartsbackend.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.djw.autopartsbackend.entity.PurchaseOrder;
import com.djw.autopartsbackend.entity.SalesOrder;

/**
 * 仪表板统计服务实现
 *
 * @author dengjiawen
 * @since 2026-01-27
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private PartService partService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryLogService inventoryLogService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private SalesOrderService salesOrderService;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 配件总数
        long totalParts = partService.count();
        stats.put("totalParts", totalParts);

        // 库存预警数量（库存低于最小库存的配件数量）
        List<Inventory> allInventory = inventoryService.list();
        long lowStockCount = 0;
        for (Inventory inventory : allInventory) {
            // stockQuantity 可能为 null，必须先判空再做 int 比较，否则自动拆箱抛 NPE
            if (inventory.getStockQuantity() == null) {
                continue;
            }
            Part part = partService.getById(inventory.getPartId());
            if (part != null && part.getMinStock() != null && inventory.getStockQuantity() < part.getMinStock()) {
                lowStockCount++;
            }
        }
        stats.put("lowStockCount", lowStockCount);

        // 今日采购金额
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LambdaQueryWrapper<InventoryLog> purchaseWrapper = new LambdaQueryWrapper<>();
        purchaseWrapper.eq(InventoryLog::getOperationType, InventoryOperationType.PURCHASE_IN.getCode())
                .between(InventoryLog::getCreateTime, todayStart, todayEnd);
        long todayPurchase = inventoryLogService.list(purchaseWrapper).stream()
                .filter(log -> log.getQuantity() != null)
                .mapToLong(log -> (long) log.getQuantity() * getPartPrice(log.getPartId()))
                .sum();
        stats.put("todayPurchase", todayPurchase);

        // 今日销售金额
        LambdaQueryWrapper<InventoryLog> salesWrapper = new LambdaQueryWrapper<>();
        salesWrapper.eq(InventoryLog::getOperationType, InventoryOperationType.SALES_OUT.getCode())
                .between(InventoryLog::getCreateTime, todayStart, todayEnd);
        long todaySales = inventoryLogService.list(salesWrapper).stream()
                .filter(log -> log.getQuantity() != null)
                .mapToLong(log -> Math.abs((long) log.getQuantity()) * getPartPrice(log.getPartId()))
                .sum();
        stats.put("todaySales", todaySales);

        // 库存充足率（库存 >= 最低库存的配件占比）
        long totalInventoryCount = allInventory.size();
        long sufficientCount = totalInventoryCount - lowStockCount;
        int stockSufficientRate = totalInventoryCount > 0
                ? (int) (sufficientCount * 100 / totalInventoryCount)
                : 100;
        stats.put("stockSufficientRate", stockSufficientRate);

        // 待处理订单数（采购订单 PENDING + 销售订单 PENDING）
        LambdaQueryWrapper<PurchaseOrder> pendingPurchaseWrapper = new LambdaQueryWrapper<>();
        pendingPurchaseWrapper.eq(PurchaseOrder::getStatus, "PENDING");
        long pendingPurchase = purchaseOrderService.count(pendingPurchaseWrapper);

        LambdaQueryWrapper<SalesOrder> pendingSalesWrapper = new LambdaQueryWrapper<>();
        pendingSalesWrapper.eq(SalesOrder::getStatus, "PENDING");
        long pendingSales = salesOrderService.count(pendingSalesWrapper);

        stats.put("pendingOrders", pendingPurchase + pendingSales);

        return stats;
    }

    /**
     * 获取配件价格
     *
     * @param partId 配件ID
     * @return 价格
     */
    private Long getPartPrice(Long partId) {
        Part part = partService.getById(partId);
        if (part == null || part.getUnitPrice() == null) {
            return 0L;
        }
        return part.getUnitPrice().longValue();
    }
}
