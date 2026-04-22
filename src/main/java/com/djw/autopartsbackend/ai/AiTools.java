package com.djw.autopartsbackend.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.djw.autopartsbackend.dto.InventoryDTO;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.service.InventoryService;
import com.djw.autopartsbackend.service.PartService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
}
