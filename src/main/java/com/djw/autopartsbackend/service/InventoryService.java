package com.djw.autopartsbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djw.autopartsbackend.dto.InventoryDTO;
import com.djw.autopartsbackend.entity.Inventory;

import java.util.List;

/**
 * @author dengjiawen
 * @since 2026-01-18
 */
public interface InventoryService extends IService<Inventory> {

    Inventory getByPartId(Long partId);

    /**
     * 直接设置库存数量
     *
     * @param partId     配件ID
     * @param quantity   目标库存数量
     * @param operatorId 操作人ID
     * @param operatorName 操作人名称
     * @param reason     调整原因
     * @return 是否成功
     */
    boolean updateStock(Long partId, Integer quantity, Long operatorId, String operatorName, String reason);

    /**
     * 调整库存数量
     *
     * @param partId       配件ID
     * @param adjustQuantity 调整数量，正数表示增加，负数表示减少
     * @param reason        调整原因
     * @param operatorId    操作人ID
     * @param operatorName  操作人名称
     * @return 是否成功
     */
    boolean adjustStock(Long partId, Integer adjustQuantity, String reason, Long operatorId, String operatorName);

    List<Inventory> getLowStockParts();

    Page<Inventory> pageQuery(Page<Inventory> page, String keyword);

    Page<InventoryDTO> pageQueryWithPartInfo(Page<InventoryDTO> page, String keyword, Boolean lowStock);

    List<InventoryDTO> getLowStockPartsWithInfo();
}
