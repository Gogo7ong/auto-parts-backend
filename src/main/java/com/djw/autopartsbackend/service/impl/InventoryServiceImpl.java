package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.mapper.InventoryMapper;
import com.djw.autopartsbackend.mapper.PartMapper;
import com.djw.autopartsbackend.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

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
}
