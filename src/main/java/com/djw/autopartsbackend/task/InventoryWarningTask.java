package com.djw.autopartsbackend.task;

import com.djw.autopartsbackend.entity.Inventory;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.mapper.InventoryMapper;
import com.djw.autopartsbackend.mapper.PartMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存预警定时任务
 * 定期检查库存，生成预警信息
 * 
 * @author dengjiawen
 * @since 2026-02-17
 */
@Slf4j
@Component
public class InventoryWarningTask {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private PartMapper partMapper;

    /**
     * 是否启用库存预警检查
     */
    @Value("${task.inventory-warning.enabled:true}")
    private boolean enabled;

    /**
     * 库存预警检查 Cron 表达式
     * 默认每天上午 9 点执行
     */
    @Value("${task.inventory-warning.cron:0 0 9 * * ?}")
    private String cronExpression;

    /**
     * 库存预警检查
     * 检查所有配件的库存，找出低于安全库存的配件
     */
    @Scheduled(cron = "${task.inventory-warning.cron:0 0 9 * * ?}")
    public void checkInventoryWarning() {
        if (!enabled) {
            log.info("库存预警任务已禁用，跳过执行");
            return;
        }

        log.info("开始执行库存预警检查任务，时间：{}", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            // 查询所有配件
            List<Part> parts = partMapper.selectList(null);
            
            if (parts == null || parts.isEmpty()) {
                log.info("没有找到配件数据");
                return;
            }

            // 统计预警信息
            Map<String, Object> warningStats = new HashMap<>();
            warningStats.put("totalParts", parts.size());
            warningStats.put("warningCount", 0);

            StringBuilder warningReport = new StringBuilder();
            warningReport.append("库存预警报告\n");
            warningReport.append("检查时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            warningReport.append("配件总数：").append(parts.size()).append("\n\n");

            // 检查每个配件的库存
            for (Part part : parts) {
                Inventory inventory = inventoryMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getPartId, part.getId())
                );

                // 如果没有库存记录，跳过
                if (inventory == null) {
                    continue;
                }

                // 检查是否低于安全库存
                int minStock = part.getMinStock() != null ? part.getMinStock() : 10;
                int currentStock = inventory.getStockQuantity() != null ? inventory.getStockQuantity() : 0;

                if (currentStock < minStock) {
                    int warningCount = (int) warningStats.get("warningCount");
                    warningStats.put("warningCount", warningCount + 1);

                    warningReport.append("【预警】配件：").append(part.getPartName())
                        .append(" (编号：").append(part.getPartCode()).append(")\n");
                    warningReport.append("  当前库存：").append(currentStock)
                        .append(" | 安全库存：").append(minStock)
                        .append(" | 缺口：").append(minStock - currentStock).append("\n\n");

                    log.warn("库存预警：配件 {} ({}), 当前库存：{}, 安全库存：{}", 
                        part.getPartName(), part.getPartCode(), currentStock, minStock);
                }
            }

            // 输出统计信息
            log.info("库存预警检查完成，总计 {} 个配件，{} 个配件需要补货", 
                warningStats.get("totalParts"), warningStats.get("warningCount"));

            if ((int) warningStats.get("warningCount") > 0) {
                log.info("\n{}", warningReport.toString());
            }

        } catch (Exception e) {
            log.error("库存预警检查失败：{}", e.getMessage(), e);
        }
    }

}
