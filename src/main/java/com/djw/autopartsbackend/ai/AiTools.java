package com.djw.autopartsbackend.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.djw.autopartsbackend.dto.InventoryDTO;
import com.djw.autopartsbackend.dto.PurchaseOrderDTO;
import com.djw.autopartsbackend.dto.SalesOrderDTO;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.InventoryLog;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.entity.PurchaseOrder;
import com.djw.autopartsbackend.entity.SalesOrder;
import com.djw.autopartsbackend.service.InventoryLogService;
import com.djw.autopartsbackend.service.InventoryService;
import com.djw.autopartsbackend.service.PartService;
import com.djw.autopartsbackend.service.PurchaseOrderService;
import com.djw.autopartsbackend.service.SalesOrderService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI工具类 - 提供数据库查询能力
 * 使用LangChain4j的@Tool注解，让AI能够调用这些方法查询数据库
 *
 * @author dengjiawen
 * @since 2026-01-18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiTools {

    private final PartService partService;
    private final InventoryService inventoryService;
    private final PurchaseOrderService purchaseOrderService;
    private final SalesOrderService salesOrderService;
    private final InventoryLogService inventoryLogService;

    /**
     * 查询所有配件品牌
     *
     * @return 品牌列表
     */
    @Tool("查询系统中所有配件品牌，返回品牌名称列表")
    public List<String> getAllBrands() {
        log.info("AI工具调用: getAllBrands");
        List<Part> parts = partService.listEnabled();
        return parts.stream()
                .map(Part::getBrand)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 按品牌查询配件
     *
     * @param brand 品牌名称
     * @return 配件列表
     */
    @Tool("按品牌查询配件，返回该品牌下的所有配件信息，包括配件编号、名称、规格、价格等")
    public List<PartInfo> getPartsByBrand(String brand) {
        log.info("AI工具调用: getPartsByBrand, brand={}", brand);
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Part::getBrand, brand)
                .eq(Part::getStatus, 1)
                .orderByAsc(Part::getPartCode);
        List<Part> parts = partService.list(wrapper);
        return parts.stream()
                .map(this::toPartInfo)
                .collect(Collectors.toList());
    }

    /**
     * 按分类查询配件
     *
     * @param category 分类名称
     * @return 配件列表
     */
    @Tool("按分类查询配件，返回该分类下的所有配件信息。常见分类包括：滤清器、制动系统、点火系统、电气系统、传动系统、轮胎、润滑油、车身附件、照明系统、冷却系统、燃油系统、悬挂系统、车身部件")
    public List<PartInfo> getPartsByCategory(String category) {
        log.info("AI工具调用: getPartsByCategory, category={}", category);
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Part::getCategory, category)
                .eq(Part::getStatus, 1)
                .orderByAsc(Part::getPartCode);
        List<Part> parts = partService.list(wrapper);
        return parts.stream()
                .map(this::toPartInfo)
                .collect(Collectors.toList());
    }

    /**
     * 按配件编号查询
     *
     * @param partCode 配件编号
     * @return 配件信息
     */
    @Tool("按配件编号查询配件详细信息，包括价格、规格、库存等")
    public PartInfo getPartByCode(String partCode) {
        log.info("AI工具调用: getPartByCode, partCode={}", partCode);
        Part part = partService.getByPartCode(partCode);
        if (part == null) {
            return null;
        }
        PartInfo info = toPartInfo(part);
        // 查询库存
        Inventory inventory = inventoryService.getByPartId(part.getId());
        if (inventory != null) {
            info.setStockQuantity(inventory.getStockQuantity());
            info.setWarehouseLocation(inventory.getWarehouseLocation());
        }
        return info;
    }

    /**
     * 按配件名称模糊查询
     *
     * @param partName 配件名称关键字
     * @return 配件列表
     */
    @Tool("按配件名称模糊查询配件，支持部分名称匹配")
    public List<PartInfo> searchPartsByName(String partName) {
        log.info("AI工具调用: searchPartsByName, partName={}", partName);
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Part::getPartName, partName)
                .eq(Part::getStatus, 1)
                .orderByAsc(Part::getPartCode);
        List<Part> parts = partService.list(wrapper);
        return parts.stream()
                .map(this::toPartInfo)
                .collect(Collectors.toList());
    }

    /**
     * 查询所有配件分类
     *
     * @return 分类列表
     */
    @Tool("查询系统中所有配件分类，返回分类名称列表")
    public List<String> getAllCategories() {
        log.info("AI工具调用: getAllCategories");
        List<Part> parts = partService.listEnabled();
        return parts.stream()
                .map(Part::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 查询库存不足的配件
     *
     * @return 库存不足的配件列表
     */
    @Tool("查询库存不足的配件（库存低于最小阈值），返回需要补货的配件信息")
    public List<PartInfo> getLowStockParts() {
        log.info("AI工具调用: getLowStockParts");
        List<InventoryDTO> lowStockList = inventoryService.getLowStockPartsWithInfo();
        return lowStockList.stream()
                .map(dto -> {
                    PartInfo info = new PartInfo();
                    info.setPartCode(dto.getPartNo());
                    info.setPartName(dto.getPartName());
                    info.setBrand(dto.getBrand());
                    info.setStockQuantity(dto.getQuantity());
                    info.setMinStock(dto.getMinQuantity());
                    info.setWarehouseLocation(dto.getWarehouseLocation());
                    return info;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询配件库存
     *
     * @param partCode 配件编号
     * @return 库存信息
     */
    @Tool("查询指定配件的库存数量和仓库位置")
    public StockInfo getPartStock(String partCode) {
        log.info("AI工具调用: getPartStock, partCode={}", partCode);
        Part part = partService.getByPartCode(partCode);
        if (part == null) {
            return null;
        }
        Inventory inventory = inventoryService.getByPartId(part.getId());
        StockInfo info = new StockInfo();
        info.setPartCode(partCode);
        info.setPartName(part.getPartName());
        if (inventory != null) {
            info.setStockQuantity(inventory.getStockQuantity());
            info.setWarehouseLocation(inventory.getWarehouseLocation());
            info.setMinStock(part.getMinStock());
            info.setLowStock(inventory.getStockQuantity() < part.getMinStock());
        } else {
            info.setStockQuantity(0);
            info.setLowStock(true);
        }
        return info;
    }

    // ==================== 采购订单 ====================

    /**
     * 按状态查询采购订单
     *
     * @param status 订单状态（pending-待审批, approved-已审批, completed-已完成, cancelled-已取消）
     * @return 采购订单列表
     */
    @Tool("按状态查询采购订单，status 可选值：pending（待审批）、approved（已审批）、completed（已完成）、cancelled（已取消）")
    public List<PurchaseOrderInfo> getPurchaseOrdersByStatus(String status) {
        log.info("AI工具调用: getPurchaseOrdersByStatus, status={}", status);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getStatus, status)
                .orderByDesc(PurchaseOrder::getCreateTime)
                .last("LIMIT 20");
        return purchaseOrderService.list(wrapper).stream()
                .map(this::toPurchaseOrderInfo)
                .collect(Collectors.toList());
    }

    /**
     * 按供应商查询采购订单
     *
     * @param supplier 供应商名称（支持模糊匹配）
     * @return 采购订单列表
     */
    @Tool("按供应商名称查询采购订单，支持模糊匹配")
    public List<PurchaseOrderInfo> getPurchaseOrdersBySupplier(String supplier) {
        log.info("AI工具调用: getPurchaseOrdersBySupplier, supplier={}", supplier);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(PurchaseOrder::getSupplier, supplier)
                .orderByDesc(PurchaseOrder::getCreateTime)
                .last("LIMIT 20");
        return purchaseOrderService.list(wrapper).stream()
                .map(this::toPurchaseOrderInfo)
                .collect(Collectors.toList());
    }

    /**
     * 按单号查询采购订单详情（含明细）
     *
     * @param orderNo 采购单号
     * @return 采购订单详情
     */
    @Tool("按采购单号查询采购订单详细信息，包含订单明细（配件、数量、单价）")
    public PurchaseOrderDetail getPurchaseOrderDetail(String orderNo) {
        log.info("AI工具调用: getPurchaseOrderDetail, orderNo={}", orderNo);
        PurchaseOrder order = purchaseOrderService.getByOrderNo(orderNo);
        if (order == null) {
            return null;
        }
        PurchaseOrderDTO dto = purchaseOrderService.getOrderWithItems(order.getId());
        PurchaseOrderDetail detail = new PurchaseOrderDetail();
        detail.setOrder(toPurchaseOrderInfo(dto.getOrder()));
        if (dto.getItems() != null) {
            detail.setItems(dto.getItems().stream().map(item -> {
                OrderItemInfo info = new OrderItemInfo();
                info.setPartCode(item.getPartCode());
                info.setPartName(item.getPartName());
                info.setQuantity(item.getQuantity());
                info.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().toPlainString() : "0");
                info.setTotalPrice(item.getTotalPrice() != null ? item.getTotalPrice().toPlainString() : "0");
                return info;
            }).collect(Collectors.toList()));
        }
        return detail;
    }

    /**
     * 查询最近N天的采购订单
     *
     * @param days 天数（如 7 表示最近7天）
     * @return 采购订单列表
     */
    @Tool("查询最近N天内创建的采购订单，days 为天数整数，如 7 表示最近7天")
    public List<PurchaseOrderInfo> getRecentPurchaseOrders(int days) {
        log.info("AI工具调用: getRecentPurchaseOrders, days={}", days);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(PurchaseOrder::getCreateTime, since)
                .orderByDesc(PurchaseOrder::getCreateTime)
                .last("LIMIT 50");
        return purchaseOrderService.list(wrapper).stream()
                .map(this::toPurchaseOrderInfo)
                .collect(Collectors.toList());
    }

    // ==================== 销售订单 ====================

    /**
     * 按状态查询销售订单
     *
     * @param status 订单状态（pending-待处理, shipped-已发货, completed-已完成, returned-已退货）
     * @return 销售订单列表
     */
    @Tool("按状态查询销售订单，status 可选值：pending（待处理）、shipped（已发货）、completed（已完成）、returned（已退货）")
    public List<SalesOrderInfo> getSalesOrdersByStatus(String status) {
        log.info("AI工具调用: getSalesOrdersByStatus, status={}", status);
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getStatus, status)
                .orderByDesc(SalesOrder::getCreateTime)
                .last("LIMIT 20");
        return salesOrderService.list(wrapper).stream()
                .map(this::toSalesOrderInfo)
                .collect(Collectors.toList());
    }

    /**
     * 按客户名称查询销售订单
     *
     * @param customerName 客户名称（支持模糊匹配）
     * @return 销售订单列表
     */
    @Tool("按客户名称查询销售订单，支持模糊匹配")
    public List<SalesOrderInfo> getSalesOrdersByCustomer(String customerName) {
        log.info("AI工具调用: getSalesOrdersByCustomer, customerName={}", customerName);
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SalesOrder::getCustomerName, customerName)
                .orderByDesc(SalesOrder::getCreateTime)
                .last("LIMIT 20");
        return salesOrderService.list(wrapper).stream()
                .map(this::toSalesOrderInfo)
                .collect(Collectors.toList());
    }

    /**
     * 按单号查询销售订单详情（含明细）
     *
     * @param orderNo 销售单号
     * @return 销售订单详情
     */
    @Tool("按销售单号查询销售订单详细信息，包含订单明细（配件、数量、单价）")
    public SalesOrderDetail getSalesOrderDetail(String orderNo) {
        log.info("AI工具调用: getSalesOrderDetail, orderNo={}", orderNo);
        SalesOrder order = salesOrderService.getByOrderNo(orderNo);
        if (order == null) {
            return null;
        }
        SalesOrderDTO dto = salesOrderService.getOrderWithItems(order.getId());
        SalesOrderDetail detail = new SalesOrderDetail();
        detail.setOrder(toSalesOrderInfo(dto.getOrder()));
        if (dto.getItems() != null) {
            detail.setItems(dto.getItems().stream().map(item -> {
                OrderItemInfo info = new OrderItemInfo();
                info.setPartCode(item.getPartCode());
                info.setPartName(item.getPartName());
                info.setQuantity(item.getQuantity());
                info.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().toPlainString() : "0");
                info.setTotalPrice(item.getTotalPrice() != null ? item.getTotalPrice().toPlainString() : "0");
                return info;
            }).collect(Collectors.toList()));
        }
        return detail;
    }

    /**
     * 查询最近N天的销售订单
     *
     * @param days 天数（如 7 表示最近7天）
     * @return 销售订单列表
     */
    @Tool("查询最近N天内创建的销售订单，days 为天数整数，如 7 表示最近7天")
    public List<SalesOrderInfo> getRecentSalesOrders(int days) {
        log.info("AI工具调用: getRecentSalesOrders, days={}", days);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SalesOrder::getCreateTime, since)
                .orderByDesc(SalesOrder::getCreateTime)
                .last("LIMIT 50");
        return salesOrderService.list(wrapper).stream()
                .map(this::toSalesOrderInfo)
                .collect(Collectors.toList());
    }

    // ==================== 库存流水 ====================

    /**
     * 查询最近N天的库存流水
     *
     * @param days 天数
     * @return 库存流水列表
     */
    @Tool("查询最近N天的库存出入库流水记录，days 为天数整数")
    public List<InventoryLogInfo> getRecentInventoryLogs(int days) {
        log.info("AI工具调用: getRecentInventoryLogs, days={}", days);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(InventoryLog::getCreateTime, since)
                .orderByDesc(InventoryLog::getCreateTime)
                .last("LIMIT 50");
        return inventoryLogService.list(wrapper).stream()
                .map(this::toInventoryLogInfo)
                .collect(Collectors.toList());
    }

    /**
     * 按配件编号查询库存流水
     *
     * @param partCode 配件编号
     * @return 该配件的库存流水列表
     */
    @Tool("按配件编号查询该配件的库存出入库流水历史")
    public List<InventoryLogInfo> getInventoryLogsByPart(String partCode) {
        log.info("AI工具调用: getInventoryLogsByPart, partCode={}", partCode);
        Part part = partService.getByPartCode(partCode);
        if (part == null) {
            return List.of();
        }
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryLog::getPartId, part.getId())
                .orderByDesc(InventoryLog::getCreateTime)
                .last("LIMIT 30");
        return inventoryLogService.list(wrapper).stream()
                .map(this::toInventoryLogInfo)
                .collect(Collectors.toList());
    }

    // ==================== 报表导出 ====================

    @Tool("生成销售统计报表导出链接。startDate 和 endDate 使用 yyyy-MM-dd 格式，periodType 可选 day、month、year。dateRangeExpression 用于传递“最近一个月”“本月”“上个月”“最近7天”“最近30天”“今年”“去年”等相对时间表达；如果用户明确给出日期，则传 startDate 和 endDate；如果用户使用相对时间，则优先传 dateRangeExpression。用户要求导出销售报表、销售统计、销售订单统计时调用。")
    public ExportLinkInfo exportSalesStatistics(String startDate, String endDate, String periodType, String dateRangeExpression) {
        log.info("AI工具调用: exportSalesStatistics, startDate={}, endDate={}, periodType={}, dateRangeExpression={}", startDate, endDate, periodType, dateRangeExpression);
        LocalDateRange range = resolveDateRange(dateRangeExpression, startDate, endDate);
        String period = normalizePeriodType(periodType);
        String url = "/api/statistics/export/sales" + buildDateQuery(range, period);
        return exportLink("销售统计报表", url);
    }

    @Tool("生成采购统计报表导出链接。startDate 和 endDate 使用 yyyy-MM-dd 格式，periodType 可选 day、month、year。dateRangeExpression 用于传递“最近一个月”“本月”“上个月”“最近7天”“最近30天”“今年”“去年”等相对时间表达；如果用户明确给出日期，则传 startDate 和 endDate；如果用户使用相对时间，则优先传 dateRangeExpression。用户要求导出采购报表、采购统计、采购订单统计时调用。")
    public ExportLinkInfo exportPurchaseStatistics(String startDate, String endDate, String periodType, String dateRangeExpression) {
        log.info("AI工具调用: exportPurchaseStatistics, startDate={}, endDate={}, periodType={}, dateRangeExpression={}", startDate, endDate, periodType, dateRangeExpression);
        LocalDateRange range = resolveDateRange(dateRangeExpression, startDate, endDate);
        String period = normalizePeriodType(periodType);
        String url = "/api/statistics/export/purchase" + buildDateQuery(range, period);
        return exportLink("采购统计报表", url);
    }

    // ==================== 供应商 ====================

    /**
     * 查询系统中所有供应商名称
     *
     * @return 供应商名称列表（去重排序）
     */
    @Tool("查询系统中所有供应商的名称列表，供应商信息来自采购订单历史")
    public List<String> getAllSuppliers() {
        log.info("AI工具调用: getAllSuppliers");
        return purchaseOrderService.list(
                new LambdaQueryWrapper<PurchaseOrder>().select(PurchaseOrder::getSupplier)
        ).stream()
                .map(PurchaseOrder::getSupplier)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ==================== 私有转换方法 ====================

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ExportLinkInfo exportLink(String title, String url) {
        ExportLinkInfo info = new ExportLinkInfo();
        info.setTitle(title);
        info.setUrl(url);
        info.setMethod("GET");
        info.setRequiresToken(true);
        info.setTip("请使用当前登录 token 下载该文件。");
        return info;
    }

    private String buildDateQuery(LocalDateRange range, String periodType) {
        return "?startDate=" + encode(range.startDate().toString())
                + "&endDate=" + encode(range.endDate().toString())
                + "&periodType=" + encode(periodType);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private LocalDateRange resolveDateRange(String dateRangeExpression, String startDate, String endDate) {
        LocalDateRange relativeRange = resolveRelativeDateRange(dateRangeExpression);
        if (relativeRange != null) {
            return relativeRange;
        }
        return resolveDateRange(startDate, endDate);
    }

    private LocalDateRange resolveDateRange(String startDate, String endDate) {
        LocalDate now = LocalDate.now();
        LocalDate start = parseDateOrDefault(startDate, now.minusMonths(1));
        LocalDate end = parseDateOrDefault(endDate, now);
        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }
        return new LocalDateRange(start, end);
    }

    private LocalDateRange resolveRelativeDateRange(String dateRangeExpression) {
        String expression = normalizeDateRangeExpression(dateRangeExpression);
        if (expression.isEmpty()) {
            return null;
        }

        LocalDate now = LocalDate.now();
        if ("本月".equals(expression)) {
            return new LocalDateRange(now.withDayOfMonth(1), now);
        }
        if ("上个月".equals(expression) || "上月".equals(expression)) {
            LocalDate start = now.minusMonths(1).withDayOfMonth(1);
            LocalDate end = now.withDayOfMonth(1).minusDays(1);
            return new LocalDateRange(start, end);
        }
        if ("今年".equals(expression) || "本年".equals(expression)) {
            return new LocalDateRange(now.withDayOfYear(1), now);
        }
        if ("去年".equals(expression) || "上年".equals(expression) || "上一年".equals(expression)) {
            LocalDate start = now.minusYears(1).withDayOfYear(1);
            LocalDate end = now.withDayOfYear(1).minusDays(1);
            return new LocalDateRange(start, end);
        }

        if (expression.startsWith("最近") || expression.startsWith("近")) {
            Integer dayCount = extractRelativeCount(expression, "天");
            if (dayCount == null) {
                dayCount = extractRelativeCount(expression, "日");
            }
            if (dayCount != null) {
                return new LocalDateRange(now.minusDays(Math.max(dayCount - 1L, 0L)), now);
            }

            Integer monthCount = extractRelativeCount(expression, "个月");
            if (monthCount == null) {
                monthCount = extractRelativeCount(expression, "月");
            }
            if (monthCount != null) {
                return new LocalDateRange(now.minusMonths(monthCount), now);
            }

            Integer yearCount = extractRelativeCount(expression, "年");
            if (yearCount != null) {
                return new LocalDateRange(now.minusYears(yearCount), now);
            }
        }

        return null;
    }

    private String normalizeDateRangeExpression(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace(" ", "")
                .replace("　", "")
                .replace("以内", "")
                .replace("之内", "")
                .trim();
    }

    private Integer extractRelativeCount(String expression, String unit) {
        int prefixLength = expression.startsWith("最近") ? 2 : (expression.startsWith("近") ? 1 : 0);
        if (prefixLength == 0) {
            return null;
        }
        int unitIndex = expression.indexOf(unit, prefixLength);
        if (unitIndex <= prefixLength) {
            return null;
        }
        String countText = expression.substring(prefixLength, unitIndex);
        if (countText.endsWith("个")) {
            countText = countText.substring(0, countText.length() - 1);
        }
        return parsePositiveNumber(countText);
    }

    private Integer parsePositiveNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int number = Integer.parseInt(value);
            return number > 0 ? number : null;
        } catch (NumberFormatException ignored) {
        }

        return switch (value) {
            case "一", "壹", "两", "俩" -> 1;
            case "二", "贰" -> 2;
            case "三", "叁" -> 3;
            case "四", "肆" -> 4;
            case "五", "伍" -> 5;
            case "六", "陆" -> 6;
            case "七", "柒" -> 7;
            case "八", "捌" -> 8;
            case "九", "玖" -> 9;
            case "十" -> 10;
            case "十一" -> 11;
            case "十二" -> 12;
            default -> parseChineseTens(value);
        };
    }

    private Integer parseChineseTens(String value) {
        int tenIndex = value.indexOf('十');
        if (tenIndex < 0) {
            return null;
        }
        String tensPart = value.substring(0, tenIndex);
        String onesPart = value.substring(tenIndex + 1);

        Integer tens = tensPart.isEmpty() ? 1 : parsePositiveNumber(tensPart);
        Integer ones = onesPart.isEmpty() ? 0 : parsePositiveNumber(onesPart);
        if (tens == null || ones == null) {
            return null;
        }
        return tens * 10 + ones;
    }

    private LocalDate parseDateOrDefault(String value, LocalDate defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }

    private String normalizePeriodType(String periodType) {
        if (periodType == null || periodType.isBlank()) {
            return "month";
        }
        String value = periodType.trim().toLowerCase();
        if ("day".equals(value) || "month".equals(value) || "year".equals(value)) {
            return value;
        }
        return "month";
    }

    private record LocalDateRange(LocalDate startDate, LocalDate endDate) {
    }

    private PurchaseOrderInfo toPurchaseOrderInfo(PurchaseOrder o) {
        PurchaseOrderInfo info = new PurchaseOrderInfo();
        info.setOrderNo(o.getOrderNo());
        info.setSupplier(o.getSupplier());
        info.setStatus(o.getStatus());
        info.setTotalAmount(o.getTotalAmount() != null ? o.getTotalAmount().toPlainString() : "0");
        info.setCreateUserName(o.getCreateUserName());
        info.setCreateTime(o.getCreateTime() != null ? o.getCreateTime().format(DT_FMT) : "");
        info.setRemark(o.getRemark());
        return info;
    }

    private SalesOrderInfo toSalesOrderInfo(SalesOrder o) {
        SalesOrderInfo info = new SalesOrderInfo();
        info.setOrderNo(o.getOrderNo());
        info.setCustomerName(o.getCustomerName());
        info.setCustomerPhone(o.getCustomerPhone());
        info.setStatus(o.getStatus());
        info.setTotalAmount(o.getTotalAmount() != null ? o.getTotalAmount().toPlainString() : "0");
        info.setCreateUserName(o.getCreateUserName());
        info.setCreateTime(o.getCreateTime() != null ? o.getCreateTime().format(DT_FMT) : "");
        info.setRemark(o.getRemark());
        return info;
    }

    private InventoryLogInfo toInventoryLogInfo(InventoryLog l) {
        InventoryLogInfo info = new InventoryLogInfo();
        info.setOperationType(l.getOperationType());
        info.setQuantity(l.getQuantity());
        info.setBeforeQuantity(l.getBeforeQuantity());
        info.setAfterQuantity(l.getAfterQuantity());
        info.setRelatedOrderNo(l.getRelatedOrderNo());
        info.setOperatorName(l.getOperatorName());
        info.setCreateTime(l.getCreateTime() != null ? l.getCreateTime().format(DT_FMT) : "");
        info.setRemark(l.getRemark());
        return info;
    }

    /**
     * 转换为PartInfo
     */
    private PartInfo toPartInfo(Part part) {
        PartInfo info = new PartInfo();
        info.setPartCode(part.getPartCode());
        info.setPartName(part.getPartName());
        info.setSpecification(part.getSpecification());
        info.setBrand(part.getBrand());
        info.setSupplier(part.getSupplier());
        info.setUnitPrice(part.getUnitPrice());
        info.setCategory(part.getCategory());
        info.setUnit(part.getUnit());
        info.setMinStock(part.getMinStock());
        info.setDescription(part.getDescription());
        return info;
    }

    /**
     * 配件信息DTO
     */
    @lombok.Data
    public static class PartInfo {
        private String partCode;
        private String partName;
        private String specification;
        private String brand;
        private String supplier;
        private java.math.BigDecimal unitPrice;
        private String category;
        private String unit;
        private Integer minStock;
        private String description;
        private Integer stockQuantity;
        private String warehouseLocation;
    }

    /**
     * 库存信息DTO
     */
    @lombok.Data
    public static class StockInfo {
        private String partCode;
        private String partName;
        private Integer stockQuantity;
        private Integer minStock;
        private String warehouseLocation;
        private Boolean lowStock;
    }

    /**
     * 采购订单摘要DTO
     */
    @lombok.Data
    public static class PurchaseOrderInfo {
        private String orderNo;
        private String supplier;
        private String status;
        private String totalAmount;
        private String createUserName;
        private String createTime;
        private String remark;
    }

    /**
     * 采购订单详情DTO（含明细）
     */
    @lombok.Data
    public static class PurchaseOrderDetail {
        private PurchaseOrderInfo order;
        private List<OrderItemInfo> items;
    }

    /**
     * 销售订单摘要DTO
     */
    @lombok.Data
    public static class SalesOrderInfo {
        private String orderNo;
        private String customerName;
        private String customerPhone;
        private String status;
        private String totalAmount;
        private String createUserName;
        private String createTime;
        private String remark;
    }

    /**
     * 销售订单详情DTO（含明细）
     */
    @lombok.Data
    public static class SalesOrderDetail {
        private SalesOrderInfo order;
        private List<OrderItemInfo> items;
    }

    /**
     * 订单明细行DTO（采购/销售通用）
     */
    @lombok.Data
    public static class OrderItemInfo {
        private String partCode;
        private String partName;
        private Integer quantity;
        private String unitPrice;
        private String totalPrice;
    }

    /**
     * 库存流水DTO
     */
    @lombok.Data
    public static class InventoryLogInfo {
        private String operationType;
        private Integer quantity;
        private Integer beforeQuantity;
        private Integer afterQuantity;
        private String relatedOrderNo;
        private String operatorName;
        private String createTime;
        private String remark;
    }

    /**
     * 报表导出链接DTO
     */
    @lombok.Data
    public static class ExportLinkInfo {
        private String title;
        private String url;
        private String method;
        private Boolean requiresToken;
        private String tip;
    }
}
