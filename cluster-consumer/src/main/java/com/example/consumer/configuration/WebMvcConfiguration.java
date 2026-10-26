package com.example.consumer.configuration;

import com.example.consumer.handler.LoginInterceptor;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * @author xujiajun
 * @since 2026/10/23
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        WebMvcConfigurer.super.configureMessageConverters(builder);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/consumer/login/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("*")
                // 允许发送cookie
                .allowCredentials(true);
        registry.addMapping("/demo/consumer/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("*")
                .allowCredentials(true);
        WebMvcConfigurer.super.addCorsMappings(registry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/demo/consumer/**")
                .includeHttpMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                .order(0);
        WebMvcConfigurer.super.addInterceptors(registry);
    }

    @Bean
    @ConditionalOnMissingBean(JsonMapper.class)
    public JsonMapper jsonMapper() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        simpleModule.addSerializer(new LocalDateSerializer());
        return JsonMapper.builder()
                .defaultTimeZone(TimeZone.getDefault())
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                // 通过该方法对mapper对象进行设置，所有序列化的对象都将按改规则进行系列化
                // Include.Include.ALWAYS 默认
                // Include.NON_DEFAULT 属性为默认值不序列化
                // Include.NON_EMPTY 属性为 空（""） 或者为 NULL 都不序列化，则返回的json是没有这个字段的。这样对移动端会更省流量
                // Include.NON_NULL 属性为NULL 不序列化
                .changeDefaultPropertyInclusion(value -> JsonInclude.Value.ALL_NON_NULL)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(simpleModule)
                .build();
    }

    static class LocalDateTimeSerializer extends ValueSerializer<LocalDateTime> {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            String dateTimeStr = FORMATTER.format(value);
            gen.writeString(dateTimeStr);
        }
    }

    static class LocalDateSerializer extends StdSerializer<LocalDate> {

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

}
