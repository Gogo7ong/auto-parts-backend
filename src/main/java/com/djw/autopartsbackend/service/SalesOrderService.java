package com.djw.autopartsbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djw.autopartsbackend.entity.SalesOrder;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
public interface SalesOrderService extends IService<SalesOrder> {

    SalesOrder getByOrderNo(String orderNo);

    Page<SalesOrder> pageQuery(Page<SalesOrder> page, String orderNo, String customerName, String status);

    boolean shipOrder(Long orderId, Long warehouseUserId, String warehouseUserName);

    boolean completeOrder(Long orderId);

    boolean returnOrder(Long orderId);
}
