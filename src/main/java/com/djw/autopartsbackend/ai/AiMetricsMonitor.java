package com.djw.autopartsbackend.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI调用监控器
 * 监控AI调用的性能指标和健康状态
 *
 * @author dengjiawen
 * @since 2026-04-01
 */
@Slf4j
@Component
public class AiMetricsMonitor {

    /**
     * 调用计数器
     */
    private final AtomicLong totalCalls = new AtomicLong(0);
    private final AtomicLong successCalls = new AtomicLong(0);
    private final AtomicLong failedCalls = new AtomicLong(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);

    /**
     * 工具调用统计
     */
    private final Map<String, ToolStats> toolStatsMap = new ConcurrentHashMap<>();

    /**
     * 最近错误信息
     */
    private final Map<String, ErrorRecord> recentErrors = new ConcurrentHashMap<>();
    private static final int MAX_ERROR_RECORDS = 10;

    /**
     * 记录AI调用开始
     *
     * @param callId 调用ID
     * @return 开始时间
     */
    public LocalDateTime recordCallStart(String callId) {
        totalCalls.incrementAndGet();
        return LocalDateTime.now();
    }

    /**
     * 记录AI调用成功
     *
     * @param callId    调用ID
     * @param startTime 开始时间
     * @param tokensUsed 使用的Token数
     */
    public void recordCallSuccess(String callId, LocalDateTime startTime, int tokensUsed) {
        successCalls.incrementAndGet();
        long latencyMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
        totalLatencyMs.addAndGet(latencyMs);

        log.info("AI调用成功: callId={}, 耗时={}ms, tokens={}", callId, latencyMs, tokensUsed);
    }

    /**
     * 记录AI调用失败
     *
     * @param callId    调用ID
     * @param startTime 开始时间
     * @param error     错误信息
     */
    public void recordCallFailure(String callId, LocalDateTime startTime, String error) {
        failedCalls.incrementAndGet();
        long latencyMs = Duration.between(startTime, LocalDateTime.now()).toMillis();

        // 记录错误
        ErrorRecord record = new ErrorRecord(callId, error, LocalDateTime.now(), latencyMs);
        recentErrors.put(callId, record);

        // 限制错误记录数量
        if (recentErrors.size() > MAX_ERROR_RECORDS) {
            recentErrors.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue((a, b) -> a.timestamp.compareTo(b.timestamp)))
                    .limit(recentErrors.size() - MAX_ERROR_RECORDS)
                    .forEach(e -> recentErrors.remove(e.getKey()));
        }

        log.error("AI调用失败: callId={}, 耗时={}ms, error={}", callId, latencyMs, error);
    }

    /**
     * 记录工具调用
     *
     * @param toolName 工具名称
     * @param success  是否成功
     * @param latencyMs 耗时（毫秒）
     */
    public void recordToolCall(String toolName, boolean success, long latencyMs) {
        ToolStats stats = toolStatsMap.computeIfAbsent(toolName, k -> new ToolStats());
        stats.callCount.incrementAndGet();
        if (success) {
            stats.successCount.incrementAndGet();
        } else {
            stats.failureCount.incrementAndGet();
        }
        stats.totalLatencyMs.addAndGet(latencyMs);

        log.debug("工具调用: tool={}, success={}, latency={}ms", toolName, success, latencyMs);
    }

    /**
     * 获取监控指标
     *
     * @return 监控指标
     */
    public Metrics getMetrics() {
        long total = totalCalls.get();
        long success = successCalls.get();
        long failed = failedCalls.get();
        long avgLatency = total > 0 ? totalLatencyMs.get() / total : 0;

        return new Metrics(
                total,
                success,
                failed,
                total > 0 ? (double) success / total * 100 : 0,
                avgLatency,
                toolStatsMap.size(),
                recentErrors.size()
        );
    }

    /**
     * 获取工具统计
     *
     * @return 工具统计Map
     */
    public Map<String, ToolStats> getToolStats() {
        return new ConcurrentHashMap<>(toolStatsMap);
    }

    /**
     * 获取最近错误
     *
     * @return 错误记录Map
     */
    public Map<String, ErrorRecord> getRecentErrors() {
        return new ConcurrentHashMap<>(recentErrors);
    }

    /**
     * 重置统计
     */
    public void reset() {
        totalCalls.set(0);
        successCalls.set(0);
        failedCalls.set(0);
        totalLatencyMs.set(0);
        toolStatsMap.clear();
        recentErrors.clear();
        log.info("AI监控统计已重置");
    }

    /**
     * 监控指标
     */
    public record Metrics(
            long totalCalls,
            long successCalls,
            long failedCalls,
            double successRate,
            long avgLatencyMs,
            int toolCount,
            int errorCount
    ) {}

    /**
     * 工具统计
     */
    public static class ToolStats {
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong failureCount = new AtomicLong(0);
        private final AtomicLong totalLatencyMs = new AtomicLong(0);

        public long getCallCount() { return callCount.get(); }
        public long getSuccessCount() { return successCount.get(); }
        public long getFailureCount() { return failureCount.get(); }
        public long getAvgLatencyMs() {
            long count = callCount.get();
            return count > 0 ? totalLatencyMs.get() / count : 0;
        }
    }

    /**
     * 错误记录
     */
    public record ErrorRecord(
            String callId,
            String error,
            LocalDateTime timestamp,
            long latencyMs
    ) {}
}
