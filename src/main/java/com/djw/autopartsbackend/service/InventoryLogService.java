package com.djw.autopartsbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djw.autopartsbackend.entity.InventoryLog;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
public interface InventoryLogService extends IService<InventoryLog> {

    Page<InventoryLog> pageQuery(Page<InventoryLog> page, Long partId, String operationType, String relatedOrderNo);

    void recordInbound(Long partId, Integer quantity, String relatedOrderNo, Long operatorId, String operatorName, String remark);

    void recordOutbound(Long partId, Integer quantity, String relatedOrderNo, Long operatorId, String operatorName, String remark);
}
