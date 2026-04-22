package com.djw.autopartsbackend.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * Jackson 序列化配置
 * 统一配置日期时间格式，使 LocalDateTime 序列化为 yyyy-MM-dd HH:mm:ss
 *
 * @author dengjiawen
 * @since 2026-04-22
 */
@Configuration
public class JacksonConfig {

    /**
     * 日期时间格式
     */
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 自定义 Jackson ObjectMapper 构建器
     *
     * @return Jackson2ObjectMapperBuilderCustomizer
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder
                .serializers(
                        new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)),
                        new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN))
                )
                .deserializers(
                        new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)),
                        new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN))
                );
    }
}
