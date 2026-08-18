package com.garment.mes.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 全局 JSON 时间格式：
 * LocalDateTime → yyyy-MM-dd HH:mm:ss
 * LocalDate     → yyyy-MM-dd
 * LocalTime     → HH:mm:ss
 * 解决前端表格直接展示 ISO 时间戳（如 2026-08-17T10:30:00）的问题。
 */
@Configuration
public class JacksonConfig {

    static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** 反序列化时兼容的日期时间格式（含 ISO 与纯日期） */
    private static final List<DateTimeFormatter> DATETIME_PATTERNS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DATE);
    private static final List<DateTimeFormatter> DATE_PATTERNS = Arrays.asList(
            DATE,
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    private static final List<DateTimeFormatter> TIME_PATTERNS = Arrays.asList(
            TIME,
            DateTimeFormatter.ofPattern("HH:mm"));

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.serializers(
                    new LocalDateTimeSerializer(DATETIME),
                    new LocalDateSerializer(DATE),
                    new LocalTimeSerializer(TIME));
            builder.deserializers(
                    new LenientLocalDateTimeDeserializer(),
                    new LenientLocalDateDeserializer(),
                    new LenientLocalTimeDeserializer());
        };
    }

    /** 宽松 LocalDateTime 反序列化：支持 yyyy-MM-dd / ISO / yyyy-MM-dd HH:mm:ss */
    static class LenientLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public Class<?> handledType() {
            return LocalDateTime.class;
        }

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            String text = p.getText().trim();
            if (text.isEmpty()) {
                return null;
            }
            for (DateTimeFormatter f : DATETIME_PATTERNS) {
                try {
                    return LocalDateTime.parse(text, f);
                } catch (Exception ignored) {
                    // 尝试下一种格式
                }
            }
            // 最后兜底：LocalDate 解析为当天零点
            try {
                return LocalDate.parse(text, DATE).atStartOfDay();
            } catch (Exception e) {
                throw new IOException("无法解析日期时间: " + text, e);
            }
        }
    }

    /** 宽松 LocalDate 反序列化 */
    static class LenientLocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public Class<?> handledType() {
            return LocalDate.class;
        }

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            String text = p.getText().trim();
            if (text.isEmpty()) {
                return null;
            }
            for (DateTimeFormatter f : DATE_PATTERNS) {
                try {
                    return LocalDate.parse(text, f);
                } catch (Exception ignored) {
                    // 尝试下一种格式
                }
            }
            throw new IOException("无法解析日期: " + text);
        }
    }

    /** 宽松 LocalTime 反序列化 */
    static class LenientLocalTimeDeserializer extends JsonDeserializer<LocalTime> {
        @Override
        public Class<?> handledType() {
            return LocalTime.class;
        }

        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            String text = p.getText().trim();
            if (text.isEmpty()) {
                return null;
            }
            for (DateTimeFormatter f : TIME_PATTERNS) {
                try {
                    return LocalTime.parse(text, f);
                } catch (Exception ignored) {
                    // 尝试下一种格式
                }
            }
            throw new IOException("无法解析时间: " + text);
        }
    }
}
