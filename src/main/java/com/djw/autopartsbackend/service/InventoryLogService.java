package com.djw.autopartsbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djw.autopartsbackend.entity.InventoryLog;

import java.time.LocalDateTime;

/**
 * @author dengjiawen
 * @since 2026-01-19
 */
public interface InventoryLogService extends IService<InventoryLog> {

    /**
     * 分页查询库存流水
     *
     * @param page 分页参数
     * @param partId 配件ID
     * @param operationType 操作类型
     * @param relatedOrderNo 关联单号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    Page<InventoryLog> pageQuery(Page<InventoryLog> page,
                                 Long partId,
                                 String operationType,
                                 String relatedOrderNo,
                                 LocalDateTime startTime,
                                 LocalDateTime endTime);
}
