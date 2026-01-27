package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.InventoryLog;
import com.djw.autopartsbackend.mapper.InventoryLogMapper;
import com.djw.autopartsbackend.service.InventoryLogService;
import com.djw.autopartsbackend.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
@Service
public class InventoryLogServiceImpl extends ServiceImpl<InventoryLogMapper, InventoryLog> implements InventoryLogService {

    @Autowired
    private InventoryService inventoryService;

    @Override
    public Page<InventoryLog> pageQuery(Page<InventoryLog> page, Long partId, String operationType, String relatedOrderNo) {
        return baseMapper.pageQueryWithPart(page, partId, operationType, relatedOrderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordInbound(Long partId, Integer quantity, String relatedOrderNo, Long operatorId, String operatorName, String remark) {
        Inventory inventory = inventoryService.getByPartId(partId);
        if (inventory == null) {
            inventory = new Inventory();
            inventory.setPartId(partId);
            inventory.setStockQuantity(0);
            inventoryService.save(inventory);
        }

        Integer beforeQuantity = inventory.getStockQuantity();
        Integer afterQuantity = beforeQuantity + quantity;

        InventoryLog log = new InventoryLog();
        log.setPartId(partId);
        log.setOperationType("IN");
        log.setQuantity(quantity);
        log.setBeforeQuantity(beforeQuantity);
        log.setAfterQuantity(afterQuantity);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRelatedOrderNo(relatedOrderNo);
        log.setRemark(remark);
        this.save(log);

        inventory.setStockQuantity(afterQuantity);
        inventoryService.updateById(inventory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordOutbound(Long partId, Integer quantity, String relatedOrderNo, Long operatorId, String operatorName, String remark) {
        Inventory inventory = inventoryService.getByPartId(partId);
        if (inventory == null || inventory.getStockQuantity() < quantity) {
            throw new RuntimeException("库存不足");
        }

        Integer beforeQuantity = inventory.getStockQuantity();
        Integer afterQuantity = beforeQuantity - quantity;

        InventoryLog log = new InventoryLog();
        log.setPartId(partId);
        log.setOperationType("OUT");
        log.setQuantity(quantity);
        log.setBeforeQuantity(beforeQuantity);
        log.setAfterQuantity(afterQuantity);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRelatedOrderNo(relatedOrderNo);
        log.setRemark(remark);
        this.save(log);

        inventory.setStockQuantity(afterQuantity);
        inventoryService.updateById(inventory);
    }
}
