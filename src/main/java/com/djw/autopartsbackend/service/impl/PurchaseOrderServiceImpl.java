package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.entity.PurchaseOrder;
import com.djw.autopartsbackend.mapper.PurchaseOrderMapper;
import com.djw.autopartsbackend.service.PurchaseOrderService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

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
