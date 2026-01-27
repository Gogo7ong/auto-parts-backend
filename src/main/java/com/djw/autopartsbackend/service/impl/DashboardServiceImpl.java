package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.InventoryLog;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.service.DashboardService;
import com.djw.autopartsbackend.service.InventoryService;
import com.djw.autopartsbackend.service.InventoryLogService;
import com.djw.autopartsbackend.service.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表板统计服务实现
 *
 * @author dengjiawen
 * @since 2025-01-27
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private PartService partService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryLogService inventoryLogService;

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
        purchaseWrapper.eq(InventoryLog::getOperationType, "IN")
                .between(InventoryLog::getCreateTime, todayStart, todayEnd);
        long todayPurchase = inventoryLogService.list(purchaseWrapper).stream()
                .mapToLong(log -> log.getQuantity() * getPartPrice(log.getPartId()))
                .sum();
        stats.put("todayPurchase", todayPurchase);

        // 今日销售金额
        LambdaQueryWrapper<InventoryLog> salesWrapper = new LambdaQueryWrapper<>();
        salesWrapper.eq(InventoryLog::getOperationType, "OUT")
                .between(InventoryLog::getCreateTime, todayStart, todayEnd);
        long todaySales = inventoryLogService.list(salesWrapper).stream()
                .mapToLong(log -> log.getQuantity() * getPartPrice(log.getPartId()))
                .sum();
        stats.put("todaySales", todaySales);

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
