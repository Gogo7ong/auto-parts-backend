package com.djw.autopartsbackend.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;

/**
 * AI模型测试
 *
 * @author dengjiawen
 * @since 2026-04-01
 */
public class AiModelTest {

    /**
     * AI助手接口（测试用）
     */
    interface TestAssistant {
        String chat(String message);
    }

    /**
     * 测试工具调用
     */
    @Test
    public void testToolCalling() {
        // 创建模型
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey("sk-b0b1e1add22ad9e8278b295910581ca9ef808cee8a152fe92006c899a83466e3")
                .modelName("qwen3-max")
                .baseUrl("https://api.qnaigc.com/v1")
                .build();

        // 创建测试工具
        Object testTools = new Object() {
            @dev.langchain4j.agent.tool.Tool("查询配件信息，根据配件编号返回配件详情")
            public String getPartByCode(String partCode) {
                return "配件编号: " + partCode + ", 名称: 机油滤清器, 品牌: 曼牌, 价格: 58元";
            }
        };

        // 创建AI服务
        TestAssistant assistant = AiServices.builder(TestAssistant.class)
                .chatLanguageModel(model)
                .tools(testTools)
                .build();

        // 测试工具调用
        String answer = assistant.chat("帮我查询配件编号为FLT-001的配件信息");
        System.out.println("AI回复: " + answer);
    }

    @Test
    public void testQwen3Max() {
        // 直接测试API连接
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey("sk-b0b1e1add22ad9e8278b295910581ca9ef808cee8a152fe92006c899a83466e3")
                .modelName("qwen3-max")
                .baseUrl("https://api.qnaigc.com/v1")
                .build();

        String answer = model.generate("你好，请用一句话介绍你自己");
        System.out.println("AI回复: " + answer);
    }
}
