package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.djw.autopartsbackend.common.BusinessException;
import com.djw.autopartsbackend.dto.StockOperationParam;
import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.InventoryLog;
import com.djw.autopartsbackend.mapper.InventoryLogMapper;
import com.djw.autopartsbackend.mapper.InventoryMapper;
import com.djw.autopartsbackend.service.InventoryOperationService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 库存操作服务实现
 *
 * @author dengjiawen
 * @since 2026-04-01
 */
@Service
public class InventoryOperationServiceImpl implements InventoryOperationService {

    private static final int MAX_RETRY_TIMES = 3;

    private final InventoryMapper inventoryMapper;

    private final InventoryLogMapper inventoryLogMapper;

    public InventoryOperationServiceImpl(InventoryMapper inventoryMapper,
                                         InventoryLogMapper inventoryLogMapper) {
        this.inventoryMapper = inventoryMapper;
        this.inventoryLogMapper = inventoryLogMapper;
    }

    /**
     * 执行库存变动并记录库存流水
     *
     * @param operationParam 库存操作参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordOperation(StockOperationParam operationParam) {
        validateOperationParam(operationParam);
        ensureInventoryExists(operationParam.getPartId());

        for (int retry = 0; retry < MAX_RETRY_TIMES; retry++) {
            Inventory inventory = getInventoryByPartId(operationParam.getPartId());
            if (inventory == null) {
                ensureInventoryExists(operationParam.getPartId());
                continue;
            }

            int beforeQuantity = inventory.getStockQuantity() == null ? 0 : inventory.getStockQuantity();
            int afterQuantity = calculateAfterQuantity(operationParam, beforeQuantity);
            int actualChangeQuantity = afterQuantity - beforeQuantity;

            if (actualChangeQuantity == 0) {
                return;
            }
            if (afterQuantity < 0) {
                throw new BusinessException("库存不足");
            }

            int updated = inventoryMapper.updateStockQuantityWithCheck(operationParam.getPartId(), beforeQuantity, afterQuantity);
            if (updated > 0) {
                saveInventoryLog(operationParam, beforeQuantity, afterQuantity, actualChangeQuantity);
                return;
            }
        }

        throw new BusinessException("库存更新失败，请稍后重试");
    }

    /**
     * 校验库存操作参数
     *
     * @param operationParam 库存操作参数
     */
    private void validateOperationParam(StockOperationParam operationParam) {
        if (operationParam == null) {
            throw new BusinessException("库存操作参数不能为空");
        }
        if (operationParam.getPartId() == null) {
            throw new BusinessException("配件ID不能为空");
        }
        if (operationParam.getOperationType() == null) {
            throw new BusinessException("库存操作类型不能为空");
        }
        if (operationParam.getTargetQuantity() == null && operationParam.getChangeQuantity() == null) {
            throw new BusinessException("库存变动数量不能为空");
        }
        if (operationParam.getTargetQuantity() != null && operationParam.getTargetQuantity() < 0) {
            throw new BusinessException("库存数量不能小于0");
        }
    }

    /**
     * 计算库存变动后的数量
     *
     * @param operationParam 库存操作参数
     * @param beforeQuantity 变动前库存
     * @return 变动后库存
     */
    private int calculateAfterQuantity(StockOperationParam operationParam, int beforeQuantity) {
        if (operationParam.getTargetQuantity() != null) {
            return operationParam.getTargetQuantity();
        }
        return beforeQuantity + operationParam.getChangeQuantity();
    }

    /**
     * 确保库存记录存在
     *
     * @param partId 配件ID
     */
    private void ensureInventoryExists(Long partId) {
        Inventory inventory = getInventoryByPartId(partId);
        if (inventory != null) {
            return;
        }

        Inventory newInventory = new Inventory();
        newInventory.setPartId(partId);
        newInventory.setStockQuantity(0);
        try {
            inventoryMapper.insert(newInventory);
        } catch (DuplicateKeyException ignored) {
        }
    }

    /**
     * 根据配件ID查询库存
     *
     * @param partId 配件ID
     * @return 库存记录
     */
    private Inventory getInventoryByPartId(Long partId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getPartId, partId);
        return inventoryMapper.selectOne(wrapper);
    }

    /**
     * 保存库存流水
     *
     * @param operationParam      库存操作参数
     * @param beforeQuantity      变动前库存
     * @param afterQuantity       变动后库存
     * @param actualChangeQuantity 实际变动数量
     */
    private void saveInventoryLog(StockOperationParam operationParam,
                                  int beforeQuantity,
                                  int afterQuantity,
                                  int actualChangeQuantity) {
        InventoryLog log = new InventoryLog();
        log.setPartId(operationParam.getPartId());
        log.setOperationType(operationParam.getOperationType().getCode());
        log.setQuantity(actualChangeQuantity);
        log.setBeforeQuantity(beforeQuantity);
        log.setAfterQuantity(afterQuantity);
        log.setOperatorId(operationParam.getOperatorId());
        log.setOperatorName(resolveOperatorName(operationParam));
        log.setRelatedOrderNo(operationParam.getRelatedOrderNo());
        log.setRemark(resolveRemark(operationParam));
        inventoryLogMapper.insert(log);
    }

    /**
     * 获取操作人名称
     *
     * @param operationParam 库存操作参数
     * @return 操作人名称
     */
    private String resolveOperatorName(StockOperationParam operationParam) {
        if (StringUtils.hasText(operationParam.getOperatorName())) {
            return operationParam.getOperatorName();
        }
        return "系统";
    }

    /**
     * 获取备注信息
     *
     * @param operationParam 库存操作参数
     * @return 备注信息
     */
    private String resolveRemark(StockOperationParam operationParam) {
        if (StringUtils.hasText(operationParam.getRemark())) {
            return operationParam.getRemark();
        }
        return operationParam.getOperationType().getDescription();
    }
}
