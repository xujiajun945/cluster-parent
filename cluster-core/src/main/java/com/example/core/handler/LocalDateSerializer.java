package com.example.core.handler;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * LocalDate类 jackson序列化方式
 *
 * @author xujiajun
 * @since 2026/10/28
 */
public class LocalDateSerializer extends StdSerializer<LocalDate> {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd");

        public LocalDateSerializer() {
            super(LocalDate.class);
        }

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            String dateTimeStr = FORMATTER.format(value);
            gen.writeString(dateTimeStr);
        }
    }