package com.example.core.handler;

import com.example.core.util.TimeUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.LocalDate;

/**
 *
 * @author xujiajun
 * @since 2026/11/15
 */
public class LocalDateDeserializer extends StdDeserializer<LocalDate> {

    private static final String PATTERN = "yyyy:MM:dd";

    public LocalDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        return TimeUtils.parseLocalDate(value, PATTERN);
    }
}
