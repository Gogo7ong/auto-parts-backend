package com.djw.autopartsbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djw.autopartsbackend.entity.PurchaseOrder;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
public interface PurchaseOrderService extends IService<PurchaseOrder> {

    PurchaseOrder getByOrderNo(String orderNo);

    Page<PurchaseOrder> pageQuery(Page<PurchaseOrder> page, String orderNo, String supplier, String status);

    boolean approveOrder(Long orderId, Long approveUserId, String approveUserName);

    boolean completeOrder(Long orderId);
}
