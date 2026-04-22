package com.djw.autopartsbackend.dto;

import com.djw.autopartsbackend.entity.PurchaseOrder;
import com.djw.autopartsbackend.entity.PurchaseOrderItem;
import lombok.Data;

import java.util.List;

/**
 * @author dengjiawen
 * @since 2026-01-19
 */
@Data
public class PurchaseOrderDTO {

    private PurchaseOrder order;

    private List<PurchaseOrderItem> items;
}
