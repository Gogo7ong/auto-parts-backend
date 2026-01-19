package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.dto.InventoryDTO;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.mapper.InventoryMapper;
import com.djw.autopartsbackend.mapper.PartMapper;
import com.djw.autopartsbackend.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Autowired
    private PartMapper partMapper;

    @Override
    public Inventory getByPartId(Long partId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getPartId, partId);
        return this.getOne(wrapper);
    }

    @Override
    public boolean updateStock(Long partId, Integer quantity) {
        Inventory inventory = this.getByPartId(partId);
        if (inventory == null) {
            return false;
        }
        inventory.setStockQuantity(quantity);
        return this.updateById(inventory);
    }

    @Override
    public boolean adjustStock(Long partId, Integer adjustQuantity, String reason) {
        Inventory inventory = this.getByPartId(partId);
        if (inventory == null) {
            return false;
        }
        Integer newQuantity = inventory.getStockQuantity() + adjustQuantity;
        if (newQuantity < 0) {
            return false;
        }
        inventory.setStockQuantity(newQuantity);
        return this.updateById(inventory);
    }

    @Override
    public List<Inventory> getLowStockParts() {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.inSql(Inventory::getPartId,
                "SELECT id FROM part WHERE min_stock > 0 AND id IN (SELECT part_id FROM inventory WHERE stock_quantity <= min_stock)");
        return this.list(wrapper);
    }

    @Override
    public Page<Inventory> pageQuery(Page<Inventory> page, String keyword) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.inSql(Inventory::getPartId,
                    "SELECT id FROM part WHERE part_name LIKE '%" + keyword + "%' OR part_code LIKE '%" + keyword + "%'");
        }
        wrapper.orderByDesc(Inventory::getLastUpdateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Page<InventoryDTO> pageQueryWithPartInfo(Page<InventoryDTO> page, String keyword, Boolean lowStock) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.inSql(Inventory::getPartId,
                    "SELECT id FROM part WHERE part_name LIKE '%" + keyword + "%' OR part_code LIKE '%" + keyword + "%'");
        }
        
        if (lowStock) {
            wrapper.inSql(Inventory::getPartId,
                    "SELECT id FROM part WHERE min_stock > 0 AND id IN (SELECT part_id FROM inventory WHERE stock_quantity <= min_stock)");
        }
        
        wrapper.orderByDesc(Inventory::getLastUpdateTime);
        
        Page<Inventory> inventoryPage = new Page<>(page.getCurrent(), page.getSize());
        Page<Inventory> result = this.page(inventoryPage, wrapper);
        
        List<InventoryDTO> dtoList = convertToDTOList(result.getRecords());
        
        Page<InventoryDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), result.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Override
    public List<InventoryDTO> getLowStockPartsWithInfo() {
        List<Inventory> inventoryList = getLowStockParts();
        return convertToDTOList(inventoryList);
    }

    private List<InventoryDTO> convertToDTOList(List<Inventory> inventoryList) {
        if (inventoryList == null || inventoryList.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> partIds = inventoryList.stream()
                .map(Inventory::getPartId)
                .collect(Collectors.toList());
        
        List<Part> partList = partMapper.selectBatchIds(partIds);
        Map<Long, Part> partMap = partList.stream()
                .collect(Collectors.toMap(Part::getId, part -> part));
        
        return inventoryList.stream().map(inventory -> {
            InventoryDTO dto = new InventoryDTO();
            dto.setId(inventory.getId());
            dto.setPartId(inventory.getPartId());
            dto.setQuantity(inventory.getStockQuantity());
            dto.setWarehouseLocation(inventory.getWarehouseLocation());
            dto.setUpdateTime(inventory.getLastUpdateTime());
            
            Part part = partMap.get(inventory.getPartId());
            if (part != null) {
                dto.setPartNo(part.getPartCode());
                dto.setPartName(part.getPartName());
                dto.setBrand(part.getBrand());
                dto.setMinQuantity(part.getMinStock());
                dto.setMaxQuantity(part.getMinStock() * 5);
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
}
