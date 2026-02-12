package com.djw.autopartsbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.dto.InventoryDTO;
import com.djw.autopartsbackend.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Select("<script>" +
            "SELECT " +
            "  i.id as id, " +
            "  i.part_id as partId, " +
            "  i.stock_quantity as quantity, " +
            "  i.warehouse_location as warehouseLocation, " +
            "  i.last_update_time as updateTime, " +
            "  p.part_code as partNo, " +
            "  p.part_name as partName, " +
            "  p.brand as brand, " +
            "  p.min_stock as minQuantity, " +
            "  (p.min_stock * 5) as maxQuantity " +
            "FROM inventory i " +
            "LEFT JOIN part p ON i.part_id = p.id " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (p.part_name LIKE CONCAT('%', #{keyword}, '%') OR p.part_code LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "  <if test='lowStock != null and lowStock'>" +
            "    AND p.min_stock &gt; 0 AND i.stock_quantity &lt;= p.min_stock" +
            "  </if>" +
            "</where>" +
            "ORDER BY i.last_update_time DESC" +
            "</script>")
    Page<InventoryDTO> pageQueryWithPart(Page<InventoryDTO> page, String keyword, Boolean lowStock);

    @Select("<script>" +
            "SELECT " +
            "  i.id as id, " +
            "  i.part_id as partId, " +
            "  i.stock_quantity as quantity, " +
            "  i.warehouse_location as warehouseLocation, " +
            "  i.last_update_time as updateTime, " +
            "  p.part_code as partNo, " +
            "  p.part_name as partName, " +
            "  p.brand as brand, " +
            "  p.min_stock as minQuantity, " +
            "  (p.min_stock * 5) as maxQuantity " +
            "FROM inventory i " +
            "LEFT JOIN part p ON i.part_id = p.id " +
            "WHERE p.min_stock &gt; 0 AND i.stock_quantity &lt;= p.min_stock " +
            "ORDER BY i.stock_quantity ASC, i.last_update_time DESC" +
            "</script>")
    List<InventoryDTO> listLowStockWithPart();
}
