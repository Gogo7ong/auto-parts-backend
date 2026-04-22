package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.common.enums.InventoryOperationType;
import com.djw.autopartsbackend.dto.SalesOrderDTO;
import com.djw.autopartsbackend.dto.StockOperationParam;
import com.djw.autopartsbackend.entity.SalesOrder;
import com.djw.autopartsbackend.entity.SalesOrderItem;
import com.djw.autopartsbackend.mapper.SalesOrderMapper;
import com.djw.autopartsbackend.mapper.SalesOrderItemMapper;
import com.djw.autopartsbackend.service.InventoryOperationService;
import com.djw.autopartsbackend.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * @author dengjiawen
 * @since 2026-01-18
 */
@Service
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesOrderService {

    @Autowired
    private SalesOrderItemMapper salesOrderItemMapper;

    @Autowired
    private InventoryOperationService inventoryOperationService;

    @Override
    public SalesOrder getByOrderNo(String orderNo) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getOrderNo, orderNo);
        return this.getOne(wrapper);
    }

    @Override
    public Page<SalesOrder> pageQuery(Page<SalesOrder> page, String orderNo, String customerName, String status) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), SalesOrder::getOrderNo, orderNo)
                .like(StringUtils.hasText(customerName), SalesOrder::getCustomerName, customerName)
                .eq(StringUtils.hasText(status), SalesOrder::getStatus, status)
                .orderByDesc(SalesOrder::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrderWithItems(SalesOrderDTO dto) {
        SalesOrder order = dto.getOrder();
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo(generateOrderNo());
        }
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        order.setTotalAmount(java.math.BigDecimal.ZERO);
        this.save(order);

        List<SalesOrderItem> items = dto.getItems();
        if (items != null && !items.isEmpty()) {
            java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
            for (SalesOrderItem item : items) {
                item.setOrderId(order.getId());
                if (item.getTotalPrice() == null) {
                    java.math.BigDecimal unitPrice = Optional.ofNullable(item.getUnitPrice()).orElse(java.math.BigDecimal.ZERO);
                    int qty = Optional.ofNullable(item.getQuantity()).orElse(0);
                    item.setTotalPrice(unitPrice.multiply(new java.math.BigDecimal(qty)));
                }
                if (item.getUnitPrice() == null && item.getQuantity() != null && item.getQuantity() > 0 && item.getTotalPrice() != null) {
                    item.setUnitPrice(item.getTotalPrice().divide(new java.math.BigDecimal(item.getQuantity()), 2, java.math.RoundingMode.HALF_UP));
                }
                salesOrderItemMapper.insert(item);
                totalAmount = totalAmount.add(Optional.ofNullable(item.getTotalPrice()).orElse(java.math.BigDecimal.ZERO));
            }
            order.setTotalAmount(totalAmount);
            this.updateById(order);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderWithItems(Long orderId, SalesOrderDTO dto) {
        SalesOrder existing = this.getById(orderId);
        if (existing == null) {
            return false;
        }

        SalesOrder order = dto.getOrder();
        order.setId(orderId);
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo(existing.getOrderNo());
        }
        if (order.getStatus() == null || order.getStatus().isEmpty()) {
            order.setStatus(existing.getStatus());
        }

        // 重置明细：先删后插（毕设场景足够；如需审计可改为差量更新）
        LambdaQueryWrapper<SalesOrderItem> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SalesOrderItem::getOrderId, orderId);
        salesOrderItemMapper.delete(deleteWrapper);

        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        List<SalesOrderItem> items = dto.getItems();
        if (items != null && !items.isEmpty()) {
            for (SalesOrderItem item : items) {
                item.setId(null);
                item.setOrderId(orderId);
                if (item.getTotalPrice() == null) {
                    java.math.BigDecimal unitPrice = Optional.ofNullable(item.getUnitPrice()).orElse(java.math.BigDecimal.ZERO);
                    int qty = Optional.ofNullable(item.getQuantity()).orElse(0);
                    item.setTotalPrice(unitPrice.multiply(new java.math.BigDecimal(qty)));
                }
                salesOrderItemMapper.insert(item);
                totalAmount = totalAmount.add(Optional.ofNullable(item.getTotalPrice()).orElse(java.math.BigDecimal.ZERO));
            }
        }

        order.setTotalAmount(totalAmount);
        return this.updateById(order);
    }

    private String generateOrderNo() {
        String dateStr = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = (int) (Math.random() * 900) + 100;
        return "SO" + dateStr + randomNum;
    }

    @Override
    public SalesOrderDTO getOrderWithItems(Long orderId) {
        SalesOrder order = this.getById(orderId);
        if (order == null) {
            return null;
        }

        LambdaQueryWrapper<SalesOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrderItem::getOrderId, orderId);
        List<SalesOrderItem> items = salesOrderItemMapper.selectList(wrapper);

        SalesOrderDTO dto = new SalesOrderDTO();
        dto.setOrder(order);
        dto.setItems(items);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(Long orderId, Long warehouseUserId, String warehouseUserName) {
        SalesOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        if (!"PENDING".equals(order.getStatus())) {
            return false;
        }

        LambdaQueryWrapper<SalesOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrderItem::getOrderId, orderId);
        List<SalesOrderItem> items = salesOrderItemMapper.selectList(wrapper);

        for (SalesOrderItem item : items) {
            StockOperationParam operationParam = new StockOperationParam();
            operationParam.setPartId(item.getPartId());
            operationParam.setOperationType(InventoryOperationType.SALES_OUT);
            operationParam.setChangeQuantity(-item.getQuantity());
            operationParam.setRelatedOrderNo(order.getOrderNo());
            operationParam.setOperatorId(warehouseUserId);
            operationParam.setOperatorName(warehouseUserName);
            operationParam.setRemark("销售订单出库");
            inventoryOperationService.recordOperation(operationParam);
        }

        order.setStatus("SHIPPED");
        order.setWarehouseUserId(warehouseUserId);
        order.setWarehouseUserName(warehouseUserName);
        order.setWarehouseTime(LocalDateTime.now());
        return this.updateById(order);
    }

    @Override
    public boolean completeOrder(Long orderId) {
        SalesOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            return false;
        }
        order.setStatus("COMPLETED");
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnOrder(Long orderId) {
        SalesOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        if (!"SHIPPED".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) {
            return false;
        }

        LambdaQueryWrapper<SalesOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrderItem::getOrderId, orderId);
        List<SalesOrderItem> items = salesOrderItemMapper.selectList(wrapper);

        for (SalesOrderItem item : items) {
            StockOperationParam operationParam = new StockOperationParam();
            operationParam.setPartId(item.getPartId());
            operationParam.setOperationType(InventoryOperationType.RETURN_IN);
            operationParam.setChangeQuantity(item.getQuantity());
            operationParam.setRelatedOrderNo(order.getOrderNo());
            operationParam.setOperatorId(order.getWarehouseUserId());
            operationParam.setOperatorName(order.getWarehouseUserName());
            operationParam.setRemark("销售订单退货");
            inventoryOperationService.recordOperation(operationParam);
        }

        order.setStatus("RETURNED");
        return this.updateById(order);
    }
}
