package com.djw.autopartsbackend.config;

import com.djw.autopartsbackend.ai.AiAssistant;
import com.djw.autopartsbackend.ai.AiTools;
import com.djw.autopartsbackend.ai.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI配置类
 * 配置LangChain4j的AI Services，支持工具调用和会话记忆
 *
 * @author dengjiawen
 * @since 2026-01-18
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final AiProperties aiProperties;
    private final AiTools aiTools;
    private final RedisChatMemoryStore chatMemoryStore;

    /**
     * 创建聊天模型 Bean
     *
     * @return ChatLanguageModel
     */
    @Bean
    @ConditionalOnProperty(prefix = "ai.openai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ChatLanguageModel chatLanguageModel() {
        log.info("初始化 OpenAiChatModel, 模型名称: {}, Base URL: {}", aiProperties.getModelName(), aiProperties.getBaseUrl());
        return OpenAiChatModel.builder()
                .apiKey(aiProperties.getApiKey())
                .modelName(aiProperties.getModelName())
                .baseUrl(aiProperties.getBaseUrl())
                .temperature(aiProperties.getTemperature())
                .maxTokens(aiProperties.getMaxTokens())
                .build();
    }

    /**
     * 创建流式聊天模型 Bean
     * 用于流式响应，提升用户体验
     *
     * @return StreamingChatLanguageModel
     */
    @Bean
    @ConditionalOnProperty(prefix = "ai.openai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        log.info("初始化 OpenAiStreamingChatModel, 模型名称: {}, Base URL: {}", aiProperties.getModelName(), aiProperties.getBaseUrl());
        return OpenAiStreamingChatModel.builder()
                .apiKey(aiProperties.getApiKey())
                .modelName(aiProperties.getModelName())
                .baseUrl(aiProperties.getBaseUrl())
                .temperature(aiProperties.getTemperature())
                .maxTokens(aiProperties.getMaxTokens())
                .build();
    }

    /**
     * 创建AI助手服务
     * 集成工具调用和会话记忆功能
     *
     * @param chatLanguageModel 聊天模型
     * @return AiAssistant
     */
    @Bean
    @ConditionalOnProperty(prefix = "ai.openai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AiAssistant aiAssistant(ChatLanguageModel chatLanguageModel, 
                                     StreamingChatLanguageModel streamingChatLanguageModel) {
        log.info("初始化 AiAssistant, 支持工具调用、会话记忆和流式响应");
        
        // 创建会话记忆提供者，每个会话保留最近20条消息
        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();
        
        return AiServices.builder(AiAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .tools(aiTools)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }
}
