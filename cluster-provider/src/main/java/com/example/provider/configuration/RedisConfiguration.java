package com.example.provider.configuration;

import com.example.core.handler.LocalDateSerializer;
import com.example.core.handler.LocalDateTimeSerializer;
import com.example.core.util.ValkeyClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.valkey.springframework.data.valkey.connection.ValkeyStandaloneConfiguration;
import io.valkey.springframework.data.valkey.connection.lettuce.LettuceConnectionFactory;
import io.valkey.springframework.data.valkey.core.ValkeyTemplate;
import io.valkey.springframework.data.valkey.serializer.JacksonJsonValkeySerializer;
import io.valkey.springframework.data.valkey.serializer.StringValkeySerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 *
 * @author xujiajun
 * @since 2026/10/26
 */
@Configuration
public class RedisConfiguration {

    @Value(value = "${spring.data.valkey.host}")
    private String valkeyHostName;

    @ConfigurationProperties(prefix = "spring.data.valkey")
    @Bean(value = "valkeyStandaloneConfiguration")
    public ValkeyStandaloneConfiguration valkeyStandaloneConfiguration() {
        ValkeyStandaloneConfiguration configuration = new ValkeyStandaloneConfiguration();
        configuration.setHostName(valkeyHostName);
        return configuration;
    }


    @ConditionalOnMissingBean
    @Bean
    public ValkeyTemplate<String, Object> valkeyTemplate(@Qualifier("valkeyStandaloneConfiguration") ValkeyStandaloneConfiguration configuration) {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(configuration);
        factory.start();

        ValkeyTemplate<String, Object> valkeyTemplate = new ValkeyTemplate<>();
        valkeyTemplate.setConnectionFactory(factory);

        StringValkeySerializer stringValkeySerializer = new StringValkeySerializer();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        simpleModule.addSerializer(new LocalDateSerializer());
        JsonMapper jsonMapper = JsonMapper.builder()
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
        JacksonJsonValkeySerializer<Object> jsonValueSerializer = new JacksonJsonValkeySerializer<>(jsonMapper, Object.class);

        valkeyTemplate.setEnableDefaultSerializer(false);
        valkeyTemplate.setKeySerializer(stringValkeySerializer);
        valkeyTemplate.setValueSerializer(jsonValueSerializer);
//        valkeyTemplate.setHashKeySerializer();
//        valkeyTemplate.setHashValueSerializer();
        valkeyTemplate.afterPropertiesSet();
        return valkeyTemplate;
    }

    @Bean
    public ValkeyClient valkeyClient(ValkeyTemplate<String, Object> valkeyTemplate) {
        return new ValkeyClient(valkeyTemplate);
    }

}
