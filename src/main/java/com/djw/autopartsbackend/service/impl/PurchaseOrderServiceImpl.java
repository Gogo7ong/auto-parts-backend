package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.common.enums.InventoryOperationType;
import com.djw.autopartsbackend.dto.PurchaseOrderDTO;
import com.djw.autopartsbackend.dto.StockOperationParam;
import com.djw.autopartsbackend.entity.PurchaseOrder;
import com.djw.autopartsbackend.entity.PurchaseOrderItem;
import com.djw.autopartsbackend.mapper.PurchaseOrderItemMapper;
import com.djw.autopartsbackend.mapper.PurchaseOrderMapper;
import com.djw.autopartsbackend.service.InventoryOperationService;
import com.djw.autopartsbackend.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * @author dengjiawen
 * @since 2026-01-18
 */
@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private InventoryOperationService inventoryOperationService;

    @Override
    public PurchaseOrder getByOrderNo(String orderNo) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getOrderNo, orderNo);
        return this.getOne(wrapper);
    }

    @Override
    public Page<PurchaseOrder> pageQuery(Page<PurchaseOrder> page, String orderNo, String supplier, String status) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), PurchaseOrder::getOrderNo, orderNo)
                .like(StringUtils.hasText(supplier), PurchaseOrder::getSupplier, supplier)
                .eq(StringUtils.hasText(status), PurchaseOrder::getStatus, status)
                .orderByDesc(PurchaseOrder::getCreateTime);
        return this.page(page, wrapper);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PurchaseOrderServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrderWithItems(PurchaseOrderDTO dto) {
        PurchaseOrder order = dto.getOrder();

        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo(generateOrderNo());
        }

        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.ZERO);
        this.save(order);
        log.info("创建采购订单成功，订单ID: {}, 订单号: {}", order.getId(), order.getOrderNo());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseOrderItem> items = dto.getItems();
        log.info("采购订单明细数量: {}", items != null ? items.size() : 0);
        if (items != null && !items.isEmpty()) {
            for (PurchaseOrderItem item : items) {
                item.setOrderId(order.getId());
                if (item.getTotalPrice() == null) {
                    BigDecimal unitPrice = Optional.ofNullable(item.getUnitPrice()).orElse(BigDecimal.ZERO);
                    int qty = Optional.ofNullable(item.getQuantity()).orElse(0);
                    item.setTotalPrice(unitPrice.multiply(new BigDecimal(qty)));
                }
                purchaseOrderItemMapper.insert(item);
                log.info("保存采购订单明细成功，明细ID: {}, 配件ID: {}, 数量: {}", item.getId(), item.getPartId(), item.getQuantity());
                totalAmount = totalAmount.add(Optional.ofNullable(item.getTotalPrice()).orElse(BigDecimal.ZERO));
            }
        }

        order.setTotalAmount(totalAmount);
        this.updateById(order);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderWithItems(Long orderId, PurchaseOrderDTO dto) {
        PurchaseOrder existing = this.getById(orderId);
        if (existing == null) {
            return false;
        }

        PurchaseOrder order = dto.getOrder();
        order.setId(orderId);
        if (!StringUtils.hasText(order.getOrderNo())) {
            order.setOrderNo(existing.getOrderNo());
        }
        if (!StringUtils.hasText(order.getStatus())) {
            order.setStatus(existing.getStatus());
        }

        LambdaQueryWrapper<PurchaseOrderItem> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(PurchaseOrderItem::getOrderId, orderId);
        purchaseOrderItemMapper.delete(deleteWrapper);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseOrderItem> items = dto.getItems();
        if (items != null && !items.isEmpty()) {
            for (PurchaseOrderItem item : items) {
                item.setId(null);
                item.setOrderId(orderId);
                if (item.getTotalPrice() == null) {
                    BigDecimal unitPrice = Optional.ofNullable(item.getUnitPrice()).orElse(BigDecimal.ZERO);
                    int qty = Optional.ofNullable(item.getQuantity()).orElse(0);
                    item.setTotalPrice(unitPrice.multiply(new BigDecimal(qty)));
                }
                purchaseOrderItemMapper.insert(item);
                totalAmount = totalAmount.add(Optional.ofNullable(item.getTotalPrice()).orElse(BigDecimal.ZERO));
            }
        }

        order.setTotalAmount(totalAmount);
        return this.updateById(order);
    }

    private String generateOrderNo() {
        String dateStr = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = (int) (Math.random() * 900) + 100;
        return "PO" + dateStr + randomNum;
    }

    @Override
    public PurchaseOrderDTO getOrderWithItems(Long orderId) {
        PurchaseOrder order = this.getById(orderId);
        if (order == null) {
            return null;
        }

        LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderItem::getOrderId, orderId);
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(wrapper);

        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setOrder(order);
        dto.setItems(items);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveOrder(Long orderId, Long approveUserId, String approveUserName) {
        log.info("开始审批采购订单，订单ID: {}, 审批人: {}({})", orderId, approveUserName, approveUserId);
        PurchaseOrder order = this.getById(orderId);
        if (order == null) {
            log.warn("审批失败：订单不存在，订单ID: {}", orderId);
            return false;
        }
        log.info("订单状态: {}, 订单号: {}", order.getStatus(), order.getOrderNo());
        if (!"PENDING".equals(order.getStatus())) {
            log.warn("审批失败：订单状态不是PENDING，当前状态: {}", order.getStatus());
            return false;
        }
        order.setStatus("APPROVED");
        order.setApproveUserId(approveUserId);
        order.setApproveUserName(approveUserName);
        order.setApproveTime(LocalDateTime.now());
        boolean success = this.updateById(order);

        if (success) {
            LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PurchaseOrderItem::getOrderId, orderId);
            List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(wrapper);
            log.info("审批采购订单，订单ID: {}, 明细数量: {}", orderId, items.size());

            for (PurchaseOrderItem item : items) {
                log.info("生成库存流水，配件ID: {}, 数量: {}", item.getPartId(), item.getQuantity());
                StockOperationParam operationParam = new StockOperationParam();
                operationParam.setPartId(item.getPartId());
                operationParam.setOperationType(InventoryOperationType.PURCHASE_IN);
                operationParam.setChangeQuantity(item.getQuantity());
                operationParam.setRelatedOrderNo(order.getOrderNo());
                operationParam.setOperatorId(approveUserId);
                operationParam.setOperatorName(approveUserName);
                operationParam.setRemark("采购订单审核入库");
                inventoryOperationService.recordOperation(operationParam);
            }
        }
        return success;
    }

    @Override
    public boolean completeOrder(Long orderId) {
        PurchaseOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        if (!"APPROVED".equals(order.getStatus())) {
            return false;
        }
        order.setStatus("COMPLETED");
        return this.updateById(order);
    }
}
