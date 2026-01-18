package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.entity.SalesOrder;
import com.djw.autopartsbackend.mapper.SalesOrderMapper;
import com.djw.autopartsbackend.service.SalesOrderService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Service
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesOrderService {

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
    public boolean shipOrder(Long orderId, Long warehouseUserId, String warehouseUserName) {
        SalesOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        if (!"PENDING".equals(order.getStatus())) {
            return false;
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
    public boolean returnOrder(Long orderId) {
        SalesOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        order.setStatus("RETURNED");
        return this.updateById(order);
    }
}
