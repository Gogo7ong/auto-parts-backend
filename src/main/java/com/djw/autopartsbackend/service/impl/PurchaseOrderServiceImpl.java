package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.dto.PurchaseOrderDTO;
import com.djw.autopartsbackend.entity.PurchaseOrder;
import com.djw.autopartsbackend.entity.PurchaseOrderItem;
import com.djw.autopartsbackend.mapper.PurchaseOrderMapper;
import com.djw.autopartsbackend.mapper.PurchaseOrderItemMapper;
import com.djw.autopartsbackend.service.InventoryLogService;
import com.djw.autopartsbackend.service.PurchaseOrderService;
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
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private InventoryLogService inventoryLogService;

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
        PurchaseOrder order = dto.getOrder();
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        this.save(order);

        List<PurchaseOrderItem> items = dto.getItems();
        if (items != null && !items.isEmpty()) {
            for (PurchaseOrderItem item : items) {
                item.setOrderId(order.getId());
                purchaseOrderItemMapper.insert(item);
            }
        }
        return true;
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
        boolean success = this.updateById(order);

        if (success) {
            LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PurchaseOrderItem::getOrderId, orderId);
            List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(wrapper);

            for (PurchaseOrderItem item : items) {
                inventoryLogService.recordInbound(
                        item.getPartId(),
                        item.getQuantity(),
                        order.getOrderNo(),
                        approveUserId,
                        approveUserName,
                        "采购订单审核入库"
                );
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
