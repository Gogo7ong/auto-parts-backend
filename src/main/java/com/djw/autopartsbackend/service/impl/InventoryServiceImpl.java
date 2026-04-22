package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.common.enums.InventoryOperationType;
import com.djw.autopartsbackend.dto.InventoryDTO;
import com.djw.autopartsbackend.dto.StockOperationParam;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.mapper.InventoryMapper;
import com.djw.autopartsbackend.mapper.PartMapper;
import com.djw.autopartsbackend.service.InventoryOperationService;
import com.djw.autopartsbackend.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dengjiawen
 * @since 2026-01-18
 */
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    private final PartMapper partMapper;
    private final InventoryOperationService inventoryOperationService;

    public InventoryServiceImpl(PartMapper partMapper, InventoryOperationService inventoryOperationService) {
        this.partMapper = partMapper;
        this.inventoryOperationService = inventoryOperationService;
    }

    @Override
    public Inventory getByPartId(Long partId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getPartId, partId);
        return this.getOne(wrapper);
    }

    @Override
    public boolean updateStock(Long partId, Integer quantity, Long operatorId, String operatorName, String reason) {
        StockOperationParam operationParam = new StockOperationParam();
        operationParam.setPartId(partId);
        operationParam.setOperationType(InventoryOperationType.SET);
        operationParam.setTargetQuantity(quantity);
        operationParam.setOperatorId(operatorId);
        operationParam.setOperatorName(operatorName);
        operationParam.setRemark(reason);
        inventoryOperationService.recordOperation(operationParam);
        return true;
    }

    @Override
    public boolean adjustStock(Long partId, Integer adjustQuantity, String reason, Long operatorId, String operatorName) {
        StockOperationParam operationParam = new StockOperationParam();
        operationParam.setPartId(partId);
        operationParam.setOperationType(InventoryOperationType.ADJUST);
        operationParam.setChangeQuantity(adjustQuantity);
        operationParam.setOperatorId(operatorId);
        operationParam.setOperatorName(operatorName);
        operationParam.setRemark(reason);
        inventoryOperationService.recordOperation(operationParam);
        return true;
    }

    @Override
    public List<Inventory> getLowStockParts() {
        // 兼容旧接口：返回 Inventory 列表（不含配件信息）
        List<InventoryDTO> dtos = baseMapper.listLowStockWithPart();
        List<Long> partIds = dtos.stream().map(InventoryDTO::getPartId).distinct().collect(Collectors.toList());
        if (partIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Inventory::getPartId, partIds);
        return this.list(wrapper);
    }

    @Override
    public Page<Inventory> pageQuery(Page<Inventory> page, String keyword) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            LambdaQueryWrapper<Part> partWrapper = new LambdaQueryWrapper<>();
            partWrapper.select(Part::getId)
                    .and(w -> w.like(Part::getPartName, keyword).or().like(Part::getPartCode, keyword));
            List<Part> parts = partMapper.selectList(partWrapper);
            List<Long> partIds = parts.stream().map(Part::getId).collect(Collectors.toList());
            if (partIds.isEmpty()) {
                Page<Inventory> empty = new Page<>(page.getCurrent(), page.getSize(), 0);
                empty.setRecords(List.of());
                return empty;
            }
            wrapper.in(Inventory::getPartId, partIds);
        }
        wrapper.orderByDesc(Inventory::getLastUpdateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Page<InventoryDTO> pageQueryWithPartInfo(Page<InventoryDTO> page, String keyword, Boolean lowStock) {
        return baseMapper.pageQueryWithPart(page, keyword, lowStock);
    }

    @Override
    public List<InventoryDTO> getLowStockPartsWithInfo() {
        return baseMapper.listLowStockWithPart();
    }
}

