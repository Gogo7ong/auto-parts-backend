package com.djw.autopartsbackend.mapper;

import com.djw.autopartsbackend.dto.analytics.InventoryMovementPointDTO;
import com.djw.autopartsbackend.dto.analytics.OrderAmountPointDTO;
import com.djw.autopartsbackend.dto.analytics.TurnoverPartDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AnalyticsMapper {

    @Select("<script>" +
            "SELECT " +
            "  <choose>" +
            "    <when test='granularity == \"month\"'>DATE_FORMAT(il.create_time, '%Y-%m')</when>" +
            "    <otherwise>DATE_FORMAT(il.create_time, '%Y-%m-%d')</otherwise>" +
            "  </choose> AS period, " +
            "  SUM(CASE WHEN il.operation_type IN ('PURCHASE_IN', 'RETURN_IN', 'IN') THEN ABS(il.quantity) ELSE 0 END) AS inboundQuantity, " +
            "  SUM(CASE WHEN il.operation_type IN ('SALES_OUT', 'OUT') THEN ABS(il.quantity) ELSE 0 END) AS outboundQuantity " +
            "FROM inventory_log il " +
            "WHERE il.create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY period " +
            "ORDER BY period ASC" +
            "</script>")
    List<InventoryMovementPointDTO> inventoryMovementSeries(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("granularity") String granularity
    );

    @Select("<script>" +
            "SELECT " +
            "  <choose>" +
            "    <when test='granularity == \"month\"'>DATE_FORMAT(soi.create_time, '%Y-%m')</when>" +
            "    <otherwise>DATE_FORMAT(soi.create_time, '%Y-%m-%d')</otherwise>" +
            "  </choose> AS period, " +
            "  COUNT(DISTINCT soi.order_id) AS orderCount, " +
            "  COALESCE(SUM(soi.quantity), 0) AS quantity, " +
            "  COALESCE(SUM(soi.unit_price * soi.quantity), 0) AS amount " +
            "FROM sales_order_item soi " +
            "WHERE soi.create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY period " +
            "ORDER BY period ASC" +
            "</script>")
    List<OrderAmountPointDTO> salesSeries(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("granularity") String granularity
    );

    @Select("<script>" +
            "SELECT " +
            "  <choose>" +
            "    <when test='granularity == \"month\"'>DATE_FORMAT(poi.create_time, '%Y-%m')</when>" +
            "    <otherwise>DATE_FORMAT(poi.create_time, '%Y-%m-%d')</otherwise>" +
            "  </choose> AS period, " +
            "  COUNT(DISTINCT poi.order_id) AS orderCount, " +
            "  COALESCE(SUM(poi.quantity), 0) AS quantity, " +
            "  COALESCE(SUM(poi.unit_price * poi.quantity), 0) AS amount " +
            "FROM purchase_order_item poi " +
            "WHERE poi.create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY period " +
            "ORDER BY period ASC" +
            "</script>")
    List<OrderAmountPointDTO> purchaseSeries(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("granularity") String granularity
    );

    @Select("<script>" +
            "SELECT " +
            "  p.id AS partId, " +
            "  p.part_code AS partCode, " +
            "  p.part_name AS partName, " +
            "  COALESCE(i.stock_quantity, 0) AS endStock, " +
            "  COALESCE(SUM(CASE WHEN il.operation_type = 'IN' THEN il.quantity ELSE 0 END), 0) AS inboundQuantity, " +
            "  COALESCE(SUM(CASE WHEN il.operation_type = 'OUT' THEN il.quantity ELSE 0 END), 0) AS outboundQuantity " +
            "FROM part p " +
            "LEFT JOIN inventory i ON i.part_id = p.id " +
            "LEFT JOIN inventory_log il ON il.part_id = p.id AND il.create_time BETWEEN #{startTime} AND #{endTime} " +
            "WHERE p.deleted = 0 " +
            "GROUP BY p.id, p.part_code, p.part_name, i.stock_quantity " +
            "ORDER BY outboundQuantity DESC " +
            "LIMIT #{topN}" +
            "</script>")
    List<TurnoverPartDTO> turnoverTopByOutbound(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("topN") Integer topN
    );
}

