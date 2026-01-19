package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.dto.SalesOrderDTO;
import com.djw.autopartsbackend.entity.SalesOrder;
import com.djw.autopartsbackend.entity.SalesOrderItem;
import com.djw.autopartsbackend.mapper.SalesOrderMapper;
import com.djw.autopartsbackend.mapper.SalesOrderItemMapper;
import com.djw.autopartsbackend.service.InventoryLogService;
import com.djw.autopartsbackend.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Service
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesOrderService {

    @Autowired
    private SalesOrderItemMapper salesOrderItemMapper;

    @Autowired
    private InventoryLogService inventoryLogService;

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
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        this.save(order);

        List<SalesOrderItem> items = dto.getItems();
        if (items != null && !items.isEmpty()) {
            for (SalesOrderItem item : items) {
                item.setOrderId(order.getId());
                salesOrderItemMapper.insert(item);
            }
        }
        return true;
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
            inventoryLogService.recordOutbound(
                    item.getPartId(),
                    item.getQuantity(),
                    order.getOrderNo(),
                    warehouseUserId,
                    warehouseUserName,
                    "销售订单出库"
            );
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
            inventoryLogService.recordInbound(
                    item.getPartId(),
                    item.getQuantity(),
                    order.getOrderNo(),
                    order.getWarehouseUserId(),
                    order.getWarehouseUserName(),
                    "销售订单退货"
            );
        }

        order.setStatus("RETURNED");
        return this.updateById(order);
    }
}
