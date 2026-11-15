package com.example.core.handler;

import com.example.core.util.TimeUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDateTime;

/**
 *
 * @author xujiajun
 * @since 2026/11/15
 */
public class LocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {

    private static final String PATTERN ="yyyy/MM/dd HH:mm:ss";

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        return TimeUtils.parseLocalDateTime(value, PATTERN);
    }
}
