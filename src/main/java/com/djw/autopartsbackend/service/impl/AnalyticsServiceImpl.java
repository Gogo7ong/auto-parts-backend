package com.djw.autopartsbackend.service.impl;

import com.djw.autopartsbackend.dto.analytics.*;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.mapper.AnalyticsMapper;
import com.djw.autopartsbackend.service.AnalyticsService;
import com.djw.autopartsbackend.service.InventoryService;
import com.djw.autopartsbackend.service.PartService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsMapper analyticsMapper;
    private final PartService partService;
    private final InventoryService inventoryService;

    public AnalyticsServiceImpl(AnalyticsMapper analyticsMapper, PartService partService, InventoryService inventoryService) {
        this.analyticsMapper = analyticsMapper;
        this.partService = partService;
        this.inventoryService = inventoryService;
    }

    @Override
    public AnalyticsOverviewDTO getOverview(LocalDate startDate, LocalDate endDate, String granularity, Integer topN) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("日期范围不合法");
        }

        String normalizedGranularity = normalizeGranularity(granularity);
        int normalizedTopN = topN == null ? 10 : Math.max(1, Math.min(50, topN));

        LocalDateTime startTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(endDate, LocalTime.MAX);

        List<InventoryMovementPointDTO> inventoryMovement = fillInventorySeries(
                startDate,
                endDate,
                normalizedGranularity,
                analyticsMapper.inventoryMovementSeries(startTime, endTime, normalizedGranularity)
        );

        List<OrderAmountPointDTO> sales = fillOrderSeries(
                startDate,
                endDate,
                normalizedGranularity,
                analyticsMapper.salesSeries(startTime, endTime, normalizedGranularity)
        );

        List<OrderAmountPointDTO> purchase = fillOrderSeries(
                startDate,
                endDate,
                normalizedGranularity,
                analyticsMapper.purchaseSeries(startTime, endTime, normalizedGranularity)
        );

        List<TurnoverPartDTO> turnoverTop = buildTurnoverTop(
                analyticsMapper.turnoverTopByOutbound(startTime, endTime, normalizedTopN)
        );

        AnalyticsKpiDTO kpi = buildKpi(inventoryMovement, sales, purchase);
        kpi.setTotalParts(partService.count());
        kpi.setLowStockCount(getLowStockCount());

        AnalyticsOverviewDTO dto = new AnalyticsOverviewDTO();
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setGranularity(normalizedGranularity);
        dto.setKpi(kpi);
        dto.setInventoryMovement(inventoryMovement);
        dto.setSales(sales);
        dto.setPurchase(purchase);
        dto.setTurnoverTop(turnoverTop);
        return dto;
    }

    private String normalizeGranularity(String granularity) {
        if (!StringUtils.hasText(granularity)) {
            return "day";
        }
        String g = granularity.trim().toLowerCase(Locale.ROOT);
        return "month".equals(g) ? "month" : "day";
    }

    private List<InventoryMovementPointDTO> fillInventorySeries(
            LocalDate startDate,
            LocalDate endDate,
            String granularity,
            List<InventoryMovementPointDTO> raw
    ) {
        Map<String, InventoryMovementPointDTO> map = raw == null
                ? new HashMap<>()
                : raw.stream().collect(Collectors.toMap(InventoryMovementPointDTO::getPeriod, Function.identity(), (a, b) -> a));

        List<String> periods = generatePeriods(startDate, endDate, granularity);
        List<InventoryMovementPointDTO> result = new ArrayList<>(periods.size());
        for (String period : periods) {
            InventoryMovementPointDTO item = map.get(period);
            if (item == null) {
                item = new InventoryMovementPointDTO();
                item.setPeriod(period);
                item.setInboundQuantity(0);
                item.setOutboundQuantity(0);
            }
            item.setNetQuantity(item.getInboundQuantity() - item.getOutboundQuantity());
            result.add(item);
        }
        return result;
    }

    private List<OrderAmountPointDTO> fillOrderSeries(
            LocalDate startDate,
            LocalDate endDate,
            String granularity,
            List<OrderAmountPointDTO> raw
    ) {
        Map<String, OrderAmountPointDTO> map = raw == null
                ? new HashMap<>()
                : raw.stream().collect(Collectors.toMap(OrderAmountPointDTO::getPeriod, Function.identity(), (a, b) -> a));

        List<String> periods = generatePeriods(startDate, endDate, granularity);
        List<OrderAmountPointDTO> result = new ArrayList<>(periods.size());
        for (String period : periods) {
            OrderAmountPointDTO item = map.get(period);
            if (item == null) {
                item = new OrderAmountPointDTO();
                item.setPeriod(period);
                item.setOrderCount(0);
                item.setQuantity(0);
                item.setAmount(BigDecimal.ZERO);
            } else if (item.getAmount() == null) {
                item.setAmount(BigDecimal.ZERO);
            }
            result.add(item);
        }
        return result;
    }

    private List<String> generatePeriods(LocalDate startDate, LocalDate endDate, String granularity) {
        List<String> periods = new ArrayList<>();
        if ("month".equals(granularity)) {
            YearMonth start = YearMonth.from(startDate);
            YearMonth end = YearMonth.from(endDate);
            YearMonth current = start;
            while (!current.isAfter(end)) {
                periods.add(current.toString());
                current = current.plusMonths(1);
            }
            return periods;
        }

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            periods.add(current.toString());
            current = current.plusDays(1);
        }
        return periods;
    }

    private AnalyticsKpiDTO buildKpi(
            List<InventoryMovementPointDTO> inventoryMovement,
            List<OrderAmountPointDTO> sales,
            List<OrderAmountPointDTO> purchase
    ) {
        AnalyticsKpiDTO kpi = new AnalyticsKpiDTO();

        int inQty = inventoryMovement.stream().mapToInt(InventoryMovementPointDTO::getInboundQuantity).sum();
        int outQty = inventoryMovement.stream().mapToInt(InventoryMovementPointDTO::getOutboundQuantity).sum();
        kpi.setInventoryInboundQuantity(inQty);
        kpi.setInventoryOutboundQuantity(outQty);
        kpi.setInventoryNetQuantity(inQty - outQty);

        int salesQty = sales.stream().mapToInt(OrderAmountPointDTO::getQuantity).sum();
        int salesOrders = sales.stream().mapToInt(OrderAmountPointDTO::getOrderCount).sum();
        BigDecimal salesAmount = sales.stream().map(OrderAmountPointDTO::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        kpi.setSalesQuantity(salesQty);
        kpi.setSalesOrderCount(salesOrders);
        kpi.setSalesAmount(salesAmount);

        int purchaseQty = purchase.stream().mapToInt(OrderAmountPointDTO::getQuantity).sum();
        int purchaseOrders = purchase.stream().mapToInt(OrderAmountPointDTO::getOrderCount).sum();
        BigDecimal purchaseAmount = purchase.stream().map(OrderAmountPointDTO::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        kpi.setPurchaseQuantity(purchaseQty);
        kpi.setPurchaseOrderCount(purchaseOrders);
        kpi.setPurchaseAmount(purchaseAmount);

        return kpi;
    }

    private long getLowStockCount() {
        List<Inventory> inventories = inventoryService.list();
        if (inventories == null || inventories.isEmpty()) {
            return 0;
        }

        Set<Long> partIds = inventories.stream().map(Inventory::getPartId).collect(Collectors.toSet());
        Map<Long, Part> parts = partService.listByIds(partIds).stream().collect(Collectors.toMap(Part::getId, Function.identity(), (a, b) -> a));

        long count = 0;
        for (Inventory inventory : inventories) {
            Part part = parts.get(inventory.getPartId());
            if (part == null || part.getMinStock() == null) {
                continue;
            }
            if (inventory.getStockQuantity() != null && inventory.getStockQuantity() < part.getMinStock()) {
                count++;
            }
        }
        return count;
    }

    private List<TurnoverPartDTO> buildTurnoverTop(List<TurnoverPartDTO> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        for (TurnoverPartDTO item : raw) {
            int inbound = item.getInboundQuantity();
            int outbound = item.getOutboundQuantity();
            int endStock = item.getEndStock();
            int startStock = Math.max(0, endStock - (inbound - outbound));

            item.setStartStock(startStock);
            item.setSalesQuantity(outbound);

            BigDecimal avgInventory = new BigDecimal(startStock + endStock).divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);
            item.setAvgInventory(avgInventory);

            BigDecimal turnoverRate;
            if (avgInventory.compareTo(BigDecimal.ZERO) <= 0) {
                turnoverRate = BigDecimal.ZERO;
            } else {
                turnoverRate = new BigDecimal(outbound).divide(avgInventory, 4, RoundingMode.HALF_UP);
            }
            item.setTurnoverRate(turnoverRate);
        }

        return raw;
    }
}

