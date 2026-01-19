package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.djw.autopartsbackend.dto.*;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.InventoryLog;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.entity.SalesOrderItem;
import com.djw.autopartsbackend.mapper.InventoryLogMapper;
import com.djw.autopartsbackend.mapper.InventoryMapper;
import com.djw.autopartsbackend.mapper.PartMapper;
import com.djw.autopartsbackend.mapper.SalesOrderItemMapper;
import com.djw.autopartsbackend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private PartMapper partMapper;

    @Autowired
    private SalesOrderItemMapper salesOrderItemMapper;

    @Override
    public List<InventoryStatisticsDTO> getInventoryStatistics(LocalDate startDate, LocalDate endDate, String periodType) {
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(InventoryLog::getCreateTime, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        List<InventoryLog> logs = inventoryLogMapper.selectList(wrapper);

        Map<String, InventoryStatisticsDTO> resultMap = new LinkedHashMap<>();

        DateTimeFormatter formatter;
        if ("month".equals(periodType)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        } else if ("quarter".equals(periodType)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-Q");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }

        for (InventoryLog log : logs) {
            String period = log.getCreateTime().format(formatter);
            InventoryStatisticsDTO dto = resultMap.computeIfAbsent(period, k -> {
                InventoryStatisticsDTO temp = new InventoryStatisticsDTO();
                temp.setPeriod(k);
                temp.setInboundQuantity(0);
                temp.setOutboundQuantity(0);
                temp.setInboundAmount(BigDecimal.ZERO);
                temp.setOutboundAmount(BigDecimal.ZERO);
                temp.setNetChange(0);
                return temp;
            });

            if ("IN".equals(log.getOperationType())) {
                dto.setInboundQuantity(dto.getInboundQuantity() + log.getQuantity());
            } else if ("OUT".equals(log.getOperationType())) {
                dto.setOutboundQuantity(dto.getOutboundQuantity() + log.getQuantity());
            }
        }

        for (InventoryStatisticsDTO dto : resultMap.values()) {
            dto.setNetChange(dto.getInboundQuantity() - dto.getOutboundQuantity());
        }

        return new ArrayList<>(resultMap.values());
    }

    @Override
    public List<TurnoverRateDTO> getTurnoverRateStatistics() {
        List<Inventory> inventories = inventoryMapper.selectList(null);
        List<TurnoverRateDTO> result = new ArrayList<>();

        LocalDate threeMonthsAgo = LocalDate.now().minus(3, ChronoUnit.MONTHS);

        for (Inventory inventory : inventories) {
            Part part = partMapper.selectById(inventory.getPartId());
            if (part == null) continue;

            LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InventoryLog::getPartId, inventory.getPartId())
                    .eq(InventoryLog::getOperationType, "OUT")
                    .ge(InventoryLog::getCreateTime, threeMonthsAgo.atStartOfDay());
            List<InventoryLog> outboundLogs = inventoryLogMapper.selectList(wrapper);

            int totalOutbound = outboundLogs.stream().mapToInt(InventoryLog::getQuantity).sum();
            int currentStock = inventory.getStockQuantity();

            BigDecimal avgMonthlyOutbound = new BigDecimal(totalOutbound).divide(new BigDecimal(3), 2, RoundingMode.HALF_UP);

            BigDecimal turnoverRate;
            if (currentStock > 0 && avgMonthlyOutbound.compareTo(BigDecimal.ZERO) > 0) {
                turnoverRate = avgMonthlyOutbound.divide(new BigDecimal(currentStock), 4, RoundingMode.HALF_UP);
            } else {
                turnoverRate = BigDecimal.ZERO;
            }

            TurnoverRateDTO dto = new TurnoverRateDTO();
            dto.setPartId(part.getId());
            dto.setPartName(part.getPartName());
            dto.setPartCode(part.getPartCode());
            dto.setCurrentStock(currentStock);
            dto.setTotalOutbound(totalOutbound);
            dto.setTurnoverRate(turnoverRate);
            dto.setAvgMonthlyOutbound(avgMonthlyOutbound);

            result.add(dto);
        }

        return result.stream().sorted(Comparator.comparing(TurnoverRateDTO::getTurnoverRate).reversed()).collect(Collectors.toList());
    }

    @Override
    public List<SalesStatisticsDTO> getSalesStatistics(LocalDate startDate, LocalDate endDate, String periodType) {
        LambdaQueryWrapper<SalesOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(SalesOrderItem::getCreateTime, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        List<SalesOrderItem> items = salesOrderItemMapper.selectList(wrapper);

        Map<String, List<SalesOrderItem>> groupedByPeriod = new LinkedHashMap<>();
        DateTimeFormatter formatter;

        if ("day".equals(periodType)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        } else if ("month".equals(periodType)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy");
        }

        for (SalesOrderItem item : items) {
            String period = item.getCreateTime().format(formatter);
            groupedByPeriod.computeIfAbsent(period, k -> new ArrayList<>()).add(item);
        }

        List<SalesStatisticsDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<SalesOrderItem>> entry : groupedByPeriod.entrySet()) {
            List<SalesOrderItem> periodItems = entry.getValue();
            int orderCount = (int) periodItems.stream().map(SalesOrderItem::getOrderId).distinct().count();
            int totalQuantity = periodItems.stream().mapToInt(SalesOrderItem::getQuantity).sum();
            BigDecimal totalAmount = periodItems.stream()
                    .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgOrderAmount = orderCount > 0 ? totalAmount.divide(new BigDecimal(orderCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            SalesStatisticsDTO dto = new SalesStatisticsDTO();
            dto.setPeriod(entry.getKey());
            dto.setOrderCount(orderCount);
            dto.setTotalAmount(totalAmount);
            dto.setAvgOrderAmount(avgOrderAmount);
            dto.setTotalQuantity(totalQuantity);

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<PurchaseStatisticsDTO> getPurchaseStatistics(LocalDate startDate, LocalDate endDate, String periodType) {
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryLog::getOperationType, "IN")
                .between(InventoryLog::getCreateTime, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        List<InventoryLog> logs = inventoryLogMapper.selectList(wrapper);

        Map<String, List<InventoryLog>> groupedByPeriod = new LinkedHashMap<>();
        DateTimeFormatter formatter;

        if ("day".equals(periodType)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        } else if ("month".equals(periodType)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy");
        }

        for (InventoryLog log : logs) {
            String period = log.getCreateTime().format(formatter);
            groupedByPeriod.computeIfAbsent(period, k -> new ArrayList<>()).add(log);
        }

        List<PurchaseStatisticsDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<InventoryLog>> entry : groupedByPeriod.entrySet()) {
            List<InventoryLog> periodLogs = entry.getValue();
            int orderCount = (int) periodLogs.stream().map(InventoryLog::getRelatedOrderNo).filter(Objects::nonNull).distinct().count();
            int totalQuantity = periodLogs.stream().mapToInt(InventoryLog::getQuantity).sum();

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal avgOrderAmount = BigDecimal.ZERO;

            PurchaseStatisticsDTO dto = new PurchaseStatisticsDTO();
            dto.setPeriod(entry.getKey());
            dto.setOrderCount(orderCount);
            dto.setTotalAmount(totalAmount);
            dto.setAvgOrderAmount(avgOrderAmount);
            dto.setTotalQuantity(totalQuantity);

            result.add(dto);
        }

        return result;
    }
}
