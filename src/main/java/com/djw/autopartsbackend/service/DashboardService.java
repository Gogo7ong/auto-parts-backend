package com.djw.autopartsbackend.service;

import java.util.Map;

/**
 * 仪表板统计服务接口
 *
 * @author dengjiawen
 * @since 2025-01-27
 */
public interface DashboardService {

    /**
     * 获取仪表板统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getStats();
}
