package com.djw.autopartsbackend.service;

import com.djw.autopartsbackend.dto.StockOperationParam;

/**
 * 库存操作服务
 *
 * @author dengjiawen
 * @since 2026-04-01
 */
public interface InventoryOperationService {

    /**
     * 执行库存变动并记录库存流水
     *
     * @param operationParam 库存操作参数
     */
    void recordOperation(StockOperationParam operationParam);
}
