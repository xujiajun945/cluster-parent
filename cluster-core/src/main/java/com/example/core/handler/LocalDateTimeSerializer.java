package com.example.core.handler;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime类 jackson序列化方式
 *
 * @author xujiajun
 * @since 2026/10/28
 */
public class LocalDateTimeSerializer extends ValueSerializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        String dateTimeStr = FORMATTER.format(value);
        gen.writeString(dateTimeStr);
    }
}