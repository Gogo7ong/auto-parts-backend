package com.djw.autopartsbackend.ai;

import com.djw.autopartsbackend.dto.resp.PartRecommendationResp;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI智能客服助手接口
 * 使用LangChain4j的AI Services，支持工具调用和会话记忆
 *
 * @author dengjiawen
 * @since 2026-03-31
 */
@SystemMessage("""
        你是汽车配件管理系统的智能客服助手。
        
        你拥有强大的数据库查询能力，可以查询系统中的配件信息。你需要帮助用户解答关于汽车配件的问题，包括：
        - 配件的型号、规格、价格查询
        - 配件的适用车型和品牌
        - 配件的库存情况和仓库位置
        - 配件分类和供应商信息
        - 库存预警和补货建议
        
        当用户询问配件相关信息时，请主动使用工具查询数据库，基于真实数据回答。
        
        回答要求：
        1. 优先使用工具查询数据库中的真实数据
        2. 如果数据库中没有相关信息，可以基于通用知识补充说明
        3. 回答要简洁、专业、友好
        4. 如果遇到不确定的问题，请建议用户联系人工客服
        5. 涉及价格时，请明确说明是系统中的单价
        """)
public interface AiAssistant {

    /**
     * 与AI进行对话（支持会话记忆）
     *
     * @param memoryId    会话ID（用于关联历史对话）
     * @param userMessage 用户消息
     * @return AI回复
     */
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);

    /**
     * 与AI进行对话（无会话记忆）
     *
     * @param userMessage 用户消息
     * @return AI回复
     */
    String chat(@UserMessage String userMessage);

    /**
     * 与AI进行流式对话（支持会话记忆）
     * 逐token返回，提升用户体验
     *
     * @param memoryId    会话ID（用于关联历史对话）
     * @param userMessage 用户消息
     * @return Token流
     */
    TokenStream chatStream(@MemoryId String memoryId, @UserMessage String userMessage);

    /**
     * 与AI进行流式对话（无会话记忆）
     *
     * @param userMessage 用户消息
     * @return Token流
     */
    TokenStream chatStream(@UserMessage String userMessage);

    /**
     * 推荐配件（结构化输出）
     * 根据用户需求返回结构化的配件推荐结果
     *
     * @param carModel 车型
     * @param category 配件分类
     * @param budget   预算范围
     * @return 配件推荐结果
     */
    @SystemMessage("""
            你是专业的汽车配件顾问。请根据用户的需求推荐合适的配件。
            使用工具查询数据库中的真实配件信息，然后返回结构化的推荐结果。
            以下是检索到的知识库参考信息：
            {{context}}
            每个推荐都要包含推荐理由和匹配度评分。
            """)
    PartRecommendationResp recommendParts(
            @UserMessage("车型: {{carModel}}, 配件分类: {{category}}, 预算: {{budget}}") String message,
            @V("context") String context,
            @V("carModel") String carModel,
            @V("category") String category,
            @V("budget") String budget
    );

    /**
     * 动态提示词对话
     * 支持动态角色和专业领域设置
     *
     * @param role    角色类型（如：销售顾问、技术专家、售后客服）
     * @param domain  专业领域（如：发动机、制动系统、电气系统）
     * @param message 用户消息
     * @return AI回复
     */
    @SystemMessage("""
            你是汽车配件管理系统的{{role}}，专注于{{domain}}领域。
            请用专业但易懂的方式回答用户问题。
            优先使用工具查询数据库中的真实数据。
            """)
    String chatWithRole(
            @UserMessage("{{message}}") String message,
            @V("role") String role,
            @V("domain") String domain
    );
}
