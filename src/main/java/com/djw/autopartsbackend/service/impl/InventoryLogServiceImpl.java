package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.entity.InventoryLog;
import com.djw.autopartsbackend.mapper.InventoryLogMapper;
import com.djw.autopartsbackend.service.InventoryLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author dengjiawen
 * @since 2026-01-19
 */
@Service
public class InventoryLogServiceImpl extends ServiceImpl<InventoryLogMapper, InventoryLog> implements InventoryLogService {

    @Override
    public Page<InventoryLog> pageQuery(Page<InventoryLog> page,
                                        Long partId,
                                        String operationType,
                                        String relatedOrderNo,
                                        LocalDateTime startTime,
                                        LocalDateTime endTime) {
        return baseMapper.pageQueryWithPart(page, partId, operationType, relatedOrderNo, startTime, endTime);
    }
}
