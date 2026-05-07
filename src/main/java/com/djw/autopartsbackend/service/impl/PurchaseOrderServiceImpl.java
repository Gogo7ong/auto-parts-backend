package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.common.BusinessException;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrderWithItems(PurchaseOrderDTO dto) {
        validateOrderItems(dto.getItems());
        PurchaseOrder order = dto.getOrder();

        if (!StringUtils.hasText(order.getOrderNo())) {
            order.setOrderNo(generateOrderNo());
        }

        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.ZERO);
        this.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderItem item : dto.getItems()) {
            item.setOrderId(order.getId());
            fillTotalPrice(item);
            purchaseOrderItemMapper.insert(item);
            totalAmount = totalAmount.add(Optional.ofNullable(item.getTotalPrice()).orElse(BigDecimal.ZERO));
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
        if (!"PENDING".equals(existing.getStatus())) {
            throw new BusinessException(400, "只有待审核的采购订单可以编辑");
        }
        validateOrderItems(dto.getItems());

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
        for (PurchaseOrderItem item : dto.getItems()) {
            item.setId(null);
            item.setOrderId(orderId);
            fillTotalPrice(item);
            purchaseOrderItemMapper.insert(item);
            totalAmount = totalAmount.add(Optional.ofNullable(item.getTotalPrice()).orElse(BigDecimal.ZERO));
        }

        order.setTotalAmount(totalAmount);
        return this.updateById(order);
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
        PurchaseOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        if (!"PENDING".equals(order.getStatus())) {
            return false;
        }

        order.setStatus("APPROVED");
        order.setApproveUserId(approveUserId);
        order.setApproveUserName(approveUserName);
        order.setApproveTime(LocalDateTime.now());
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long orderId) {
        PurchaseOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        if (!"APPROVED".equals(order.getStatus())) {
            return false;
        }

        LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderItem::getOrderId, orderId);
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(wrapper);
        validateOrderItems(items);

        for (PurchaseOrderItem item : items) {
            StockOperationParam operationParam = new StockOperationParam();
            operationParam.setPartId(item.getPartId());
            operationParam.setOperationType(InventoryOperationType.PURCHASE_IN);
            operationParam.setChangeQuantity(item.getQuantity());
            operationParam.setRelatedOrderNo(order.getOrderNo());
            operationParam.setOperatorId(order.getApproveUserId());
            operationParam.setOperatorName(order.getApproveUserName());
            operationParam.setRemark("采购订单完成入库");
            inventoryOperationService.recordOperation(operationParam);
        }

        order.setStatus("COMPLETED");
        return this.updateById(order);
    }

    private String generateOrderNo() {
        String dateStr = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = (int) (Math.random() * 900) + 100;
        return "PO" + dateStr + randomNum;
    }

    private void fillTotalPrice(PurchaseOrderItem item) {
        if (item.getTotalPrice() == null) {
            BigDecimal unitPrice = Optional.ofNullable(item.getUnitPrice()).orElse(BigDecimal.ZERO);
            int qty = Optional.ofNullable(item.getQuantity()).orElse(0);
            item.setTotalPrice(unitPrice.multiply(new BigDecimal(qty)));
        }
    }

    private void validateOrderItems(List<PurchaseOrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(400, "订单明细不能为空");
        }
        for (PurchaseOrderItem item : items) {
            if (item.getPartId() == null) {
                throw new BusinessException(400, "配件ID不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException(400, "数量必须大于0");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, "单价必须大于0");
            }
        }
    }
}
