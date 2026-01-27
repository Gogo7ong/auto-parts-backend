package com.djw.autopartsbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.djw.autopartsbackend.entity.InventoryLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Mapper
public interface InventoryLogMapper extends BaseMapper<InventoryLog> {

    /**
     * 关联查询库存流水及配件信息
     *
     * @param page 分页对象
     * @param partId 配件ID
     * @param operationType 操作类型
     * @param relatedOrderNo 关联单号
     * @return 包含配件信息的库存流水列表
     */
    @Select("<script>" +
            "SELECT il.*, p.part_code as partNo, p.part_name as partName " +
            "FROM inventory_log il " +
            "LEFT JOIN part p ON il.part_id = p.id " +
            "<where>" +
            "  <if test='partId != null'>AND il.part_id = #{partId}</if>" +
            "  <if test='operationType != null and operationType != \"\"'>AND il.operation_type = #{operationType}</if>" +
            "  <if test='relatedOrderNo != null and relatedOrderNo != \"\"'>AND il.related_order_no = #{relatedOrderNo}</if>" +
            "</where>" +
            "ORDER BY il.create_time DESC" +
            "</script>")
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<InventoryLog> pageQueryWithPart(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<InventoryLog> page,
            Long partId,
            String operationType,
            String relatedOrderNo);
}
