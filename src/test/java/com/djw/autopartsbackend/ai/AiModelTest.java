package com.djw.autopartsbackend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * AI model integration tests.
 */
@Disabled("Requires external API credentials and network access.")
public class AiModelTest {

    interface TestAssistant {
        String chat(String message);
    }

    @Test
    public void testToolCalling() {
        ChatModel model = buildModel();

        Object testTools = new Object() {
            @dev.langchain4j.agent.tool.Tool("Query part information by part code.")
            public String getPartByCode(String partCode) {
                return "Part code: " + partCode + ", name: Oil filter, brand: Mann, price: 58";
            }
        };

        TestAssistant assistant = AiServices.builder(TestAssistant.class)
                .chatModel(model)
                .tools(testTools)
                .build();

        String answer = assistant.chat("Please query part code FLT-001.");
        System.out.println("AI response: " + answer);
    }

    @Test
    public void testQwen3Max() {
        ChatModel model = buildModel();

        String answer = model.chat("Hello, please introduce yourself in one sentence.");
        System.out.println("AI response: " + answer);
    }

    private ChatModel buildModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("AI_TEST_API_KEY"))
                .modelName("qwen3-max")
                .baseUrl("https://api.qnaigc.com/v1")
                .build();
    }
}
