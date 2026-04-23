package com.djw.autopartsbackend.service;

import com.djw.autopartsbackend.ai.AiAssistant;
import com.djw.autopartsbackend.ai.AiMetricsMonitor;
import com.djw.autopartsbackend.ai.AiUsageTracker;
import com.djw.autopartsbackend.ai.KnowledgeBaseService;
import com.djw.autopartsbackend.common.BusinessException;
import com.djw.autopartsbackend.dto.resp.PartRecommendationResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * AI智能客服服务
 * 使用LangChain4j的AI Services，支持工具调用和会话记忆
 *
 * @author dengjiawen
 * @since 2026-01-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiCustomerService {

    private final AiAssistant aiAssistant;
    private final AiUsageTracker usageTracker;
    private final AiMetricsMonitor metricsMonitor;
    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 与AI进行对话（无会话记忆）
     *
     * @param userMessage 用户消息
     * @return AI回复
     */
    public String chat(String userMessage) {
        log.info("AI对话请求: {}", userMessage);
        try {
            String answer = aiAssistant.chat(userMessage);
            log.info("AI对话响应成功");
            return answer;
        } catch (Exception e) {
            log.error("AI对话失败: {}", e.getMessage(), e);
            throw new BusinessException("AI服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * 与AI进行对话（支持会话记忆）
     *
     * @param userMessage     用户消息
     * @param conversationId 会话ID（如果为空则生成新会话ID）
     * @return AI回复
     */
    public String chatWithHistory(String userMessage, String conversationId) {
        log.info("AI对话请求: conversationId={}, message={}", conversationId, userMessage);
        try {
            // 如果没有会话ID，生成一个新的
            String memoryId = (conversationId != null && !conversationId.isEmpty()) 
                    ? conversationId 
                    : UUID.randomUUID().toString();
            
            String answer = aiAssistant.chat(memoryId, userMessage);
            log.info("AI对话响应成功, conversationId={}", memoryId);
            return answer;
        } catch (Exception e) {
            log.error("AI对话失败: {}", e.getMessage(), e);
            throw new BusinessException("AI服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * 与AI进行流式对话（支持会话记忆）
     * 逐token返回，提升用户体验
     *
     * 注意：由于LangChain4j的流式API与工具调用存在兼容性问题，
     * 这里使用非流式API获取完整响应后，模拟流式输出效果
     *
     * @param userMessage     用户消息
     * @param conversationId 会话ID
     * @param onNext         token回调函数
     * @param onComplete     完成回调
     * @param onError        错误回调
     */
    public void chatStreamWithHistory(String userMessage, String conversationId,
                                       Consumer<String> onNext, Runnable onComplete, Consumer<Throwable> onError) {
        log.info("AI流式对话请求: conversationId={}, message={}", conversationId, userMessage);
        try {
            String memoryId = (conversationId != null && !conversationId.isEmpty())
                    ? conversationId
                    : UUID.randomUUID().toString();

            // LangChain4j 1.0.0 已修复 streaming+tools 的 NPE（response cannot be null）
            // 使用 TokenStream 真实流式输出，streamingChatModel、工具调用、会话记忆均已在 AiConfig 中配置
            aiAssistant.chatStream(memoryId, userMessage)
                    .onPartialResponse(onNext)
                    .onCompleteResponse(response -> {
                        log.info("AI流式对话完成: conversationId={}", memoryId);
                        onComplete.run();
                    })
                    .onError(throwable -> {
                        log.error("AI流式对话失败: {}", throwable.getMessage(), throwable);
                        onError.accept(new BusinessException("AI服务暂时不可用，请稍后重试", throwable));
                    })
                    .start();
        } catch (Exception e) {
            log.error("AI流式对话失败: {}", e.getMessage(), e);
            onError.accept(new BusinessException("AI服务暂时不可用，请稍后重试", e));
        }
    }

    /**
     * 生成新的会话ID
     *
     * @return 新会话ID
     */
    public String newConversationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 推荐配件（结构化输出）
     *
     * @param carModel 车型
     * @param category 配件分类
     * @param budget   预算
     * @return 配件推荐结果
     */
    public PartRecommendationResp recommendParts(String carModel, String category, String budget) {
        log.info("AI配件推荐请求: carModel={}, category={}, budget={}", carModel, category, budget);
        String callId = UUID.randomUUID().toString();
        var startTime = metricsMonitor.recordCallStart(callId);

        try {
            // 临时关闭 RAG 检索增强，后续按配置开关恢复
            // String context = knowledgeBaseService.searchAsContext(carModel + " " + category);
            String context = "";

            PartRecommendationResp result = aiAssistant.recommendParts(
                    "车型: " + carModel + ", 配件分类: " + category + ", 预算: " + budget,
                    context,
                    carModel,
                    category,
                    budget != null ? budget : "不限"
            );

            int tokens = usageTracker.countTokens(carModel + category + budget + context);
            metricsMonitor.recordCallSuccess(callId, startTime, tokens);

            log.info("AI配件推荐成功: callId={}", callId);
            return result;
        } catch (Exception e) {
            metricsMonitor.recordCallFailure(callId, startTime, e.getMessage());
            log.error("AI配件推荐失败: {}", e.getMessage(), e);
            throw new BusinessException("AI推荐服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * 动态角色对话
     *
     * @param message 用户消息
     * @param role    角色
     * @param domain  专业领域
     * @return AI回复
     */
    public String chatWithRole(String message, String role, String domain) {
        log.info("AI角色对话请求: role={}, domain={}", role, domain);
        String callId = UUID.randomUUID().toString();
        var startTime = metricsMonitor.recordCallStart(callId);

        try {
            String answer = aiAssistant.chatWithRole(message, role, domain);

            int tokens = usageTracker.countTokens(message) + usageTracker.countTokens(answer);
            metricsMonitor.recordCallSuccess(callId, startTime, tokens);

            return answer;
        } catch (Exception e) {
            metricsMonitor.recordCallFailure(callId, startTime, e.getMessage());
            throw new BusinessException("AI服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * 导入知识库文档
     *
     * @param content 文档内容
     */
    public void importKnowledge(String content) {
        log.warn("RAG功能已临时关闭，跳过知识库文档导入, 长度: {}", content.length());
        // knowledgeBaseService.importDocument(content);
    }

    /**
     * 检索知识库
     *
     * @param query 查询文本
     * @return 相关文档列表
     */
    public List<String> searchKnowledge(String query) {
        log.warn("RAG功能已临时关闭，返回空检索结果, query={}", query);
        // return knowledgeBaseService.search(query);
        return List.of();
    }

    /**
     * 获取AI使用统计
     *
     * @return 使用统计
     */
    public AiUsageTracker.GlobalUsage getUsageStats() {
        return usageTracker.getGlobalUsage();
    }

    /**
     * 获取AI监控指标
     *
     * @return 监控指标
     */
    public AiMetricsMonitor.Metrics getMetrics() {
        return metricsMonitor.getMetrics();
    }
}
