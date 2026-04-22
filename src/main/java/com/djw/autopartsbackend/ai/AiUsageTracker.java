package com.djw.autopartsbackend.ai;

import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI使用量追踪器
 * 统计Token使用量和成本
 *
 * @author dengjiawen
 * @since 2026-04-01
 */
@Slf4j
@Component
public class AiUsageTracker {

    /**
     * Tokenizer for counting tokens
     */
    private final Tokenizer tokenizer;

    /**
     * 会话Token使用统计
     */
    private final Map<String, SessionUsage> sessionUsageMap = new ConcurrentHashMap<>();

    /**
     * 全局Token使用统计
     */
    private final AtomicLong totalInputTokens = new AtomicLong(0);
    private final AtomicLong totalOutputTokens = new AtomicLong(0);

    /**
     * 价格配置（每1000 tokens的价格，单位：元）
     * 通义千问 qwen-max 价格参考
     */
    private static final double INPUT_PRICE_PER_1K = 0.02;
    private static final double OUTPUT_PRICE_PER_1K = 0.06;

    public AiUsageTracker() {
        // 使用GPT-3.5的tokenizer作为近似（通义千问类似）
        this.tokenizer = new OpenAiTokenizer("gpt-3.5-turbo");
    }

    /**
     * 计算文本的Token数量
     *
     * @param text 文本内容
     * @return Token数量
     */
    public int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return tokenizer.estimateTokenCountInText(text);
    }

    /**
     * 记录会话Token使用
     *
     * @param sessionId    会话ID
     * @param inputTokens  输入Token数
     * @param outputTokens 输出Token数
     */
    public void recordUsage(String sessionId, int inputTokens, int outputTokens) {
        // 更新会话统计
        SessionUsage usage = sessionUsageMap.computeIfAbsent(sessionId, k -> new SessionUsage());
        usage.inputTokens.addAndGet(inputTokens);
        usage.outputTokens.addAndGet(outputTokens);
        usage.requestCount.incrementAndGet();

        // 更新全局统计
        totalInputTokens.addAndGet(inputTokens);
        totalOutputTokens.addAndGet(outputTokens);

        // 计算本次成本
        double cost = calculateCost(inputTokens, outputTokens);
        log.info("会话 {} Token使用: 输入={}, 输出={}, 本次成本={:.4f}元", 
                sessionId, inputTokens, outputTokens, cost);
    }

    /**
     * 获取会话使用统计
     *
     * @param sessionId 会话ID
     * @return 使用统计
     */
    public SessionUsage getSessionUsage(String sessionId) {
        return sessionUsageMap.getOrDefault(sessionId, new SessionUsage());
    }

    /**
     * 获取全局使用统计
     *
     * @return 使用统计
     */
    public GlobalUsage getGlobalUsage() {
        return new GlobalUsage(
                totalInputTokens.get(),
                totalOutputTokens.get(),
                calculateCost(totalInputTokens.get(), totalOutputTokens.get())
        );
    }

    /**
     * 计算成本
     *
     * @param inputTokens  输入Token数
     * @param outputTokens 输出Token数
     * @return 成本（元）
     */
    public double calculateCost(long inputTokens, long outputTokens) {
        double inputCost = (inputTokens / 1000.0) * INPUT_PRICE_PER_1K;
        double outputCost = (outputTokens / 1000.0) * OUTPUT_PRICE_PER_1K;
        return inputCost + outputCost;
    }

    /**
     * 重置统计
     */
    public void reset() {
        sessionUsageMap.clear();
        totalInputTokens.set(0);
        totalOutputTokens.set(0);
        log.info("AI使用统计已重置");
    }

    /**
     * 会话使用统计
     */
    public static class SessionUsage {
        private final AtomicLong inputTokens = new AtomicLong(0);
        private final AtomicLong outputTokens = new AtomicLong(0);
        private final AtomicLong requestCount = new AtomicLong(0);

        public long getInputTokens() {
            return inputTokens.get();
        }

        public long getOutputTokens() {
            return outputTokens.get();
        }

        public long getRequestCount() {
            return requestCount.get();
        }

        public double getCost(AiUsageTracker tracker) {
            return tracker.calculateCost(inputTokens.get(), outputTokens.get());
        }
    }

    /**
     * 全局使用统计
     */
    public record GlobalUsage(
            long totalInputTokens,
            long totalOutputTokens,
            double totalCost
    ) {}
}
