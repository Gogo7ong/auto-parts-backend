package com.djw.autopartsbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI配置属性
 *
 * @author dengjiawen
 * @since 2026-01-18
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.openai")
public class AiProperties {

    /**
     * OpenAI API Key
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String modelName = "qwen3-max";

    /**
     * API Base URL
     */
    private String baseUrl;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 温度参数（0.0-2.0），控制回复的随机性
     * 值越低回复越确定，值越高回复越随机
     */
    private Double temperature = 0.7;

    /**
     * 最大Token数量
     */
    private Integer maxTokens = 2000;

    /**
     * 请求超时时间（秒）
     */
    private Integer timeout = 60;
}
