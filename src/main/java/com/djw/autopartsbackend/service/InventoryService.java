package com.djw.autopartsbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djw.autopartsbackend.dto.InventoryDTO;
import com.djw.autopartsbackend.entity.Inventory;

import java.util.List;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
public interface InventoryService extends IService<Inventory> {

    Inventory getByPartId(Long partId);

    boolean updateStock(Long partId, Integer quantity);

    boolean adjustStock(Long partId, Integer adjustQuantity, String reason);

    List<Inventory> getLowStockParts();

    Page<Inventory> pageQuery(Page<Inventory> page, String keyword);

    Page<InventoryDTO> pageQueryWithPartInfo(Page<InventoryDTO> page, String keyword, Boolean lowStock);

    List<InventoryDTO> getLowStockPartsWithInfo();
}
