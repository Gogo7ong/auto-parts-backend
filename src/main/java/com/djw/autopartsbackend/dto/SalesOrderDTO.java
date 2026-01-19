package com.djw.autopartsbackend.dto;

import com.djw.autopartsbackend.entity.SalesOrder;
import com.djw.autopartsbackend.entity.SalesOrderItem;
import lombok.Data;

import java.util.List;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
@Data
public class SalesOrderDTO {

    private SalesOrder order;

    private List<SalesOrderItem> items;
}
