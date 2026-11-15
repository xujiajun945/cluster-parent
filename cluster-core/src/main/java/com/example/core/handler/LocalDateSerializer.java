package com.example.core.handler;

import com.example.core.util.TimeUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.LocalDate;

/**
 * LocalDate类 jackson序列化方式
 *
 * @author xujiajun
 * @since 2026/10/28
 */
public class LocalDateSerializer extends StdSerializer<LocalDate> {

        private static final String PATTERN = "yyyy:MM:dd";

        public LocalDateSerializer() {
            super(LocalDate.class);
        }

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            String dateTimeStr = TimeUtils.format(value, PATTERN);
            gen.writeString(dateTimeStr);
        }
    }