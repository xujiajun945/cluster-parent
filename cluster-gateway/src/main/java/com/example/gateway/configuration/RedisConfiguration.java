package com.example.gateway.configuration;

import com.example.core.config.SessionCookieConfig;
import com.example.core.config.ValkeySessionConfig;
import com.example.core.handler.LocalDateDeserializer;
import com.example.core.handler.LocalDateSerializer;
import com.example.core.handler.LocalDateTimeDeserializer;
import com.example.core.handler.LocalDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.valkey.springframework.data.valkey.connection.lettuce.LettuceConnectionFactory;
import io.valkey.springframework.data.valkey.core.ReactiveValkeyTemplate;
import io.valkey.springframework.data.valkey.serializer.JacksonJsonValkeySerializer;
import io.valkey.springframework.data.valkey.serializer.StringValkeySerializer;
import io.valkey.springframework.data.valkey.serializer.ValkeySerializationContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * <h2>网关层redis配置</h2>
 * <p>
 *     对于网关侧的spring session，需要注意的点很多
 *     方案1：网关接入spring session （本项目采用的就是方案1）
 *          1）网关原生没有cookieSerializer包装，业务侧的spring session有cookieSerializer包装，一个很容易出问题的地方
 *             就是cookieSerializer默认会将spring session的id做base64加密（可关闭） 但是网关没有cookieSerializer，
 *             会导致两边sessionId对不上
 *          2）所有接入同一个spring session体系的微服务（包括网关）需要对其redis的所有配置，连接，cookie的name，path等
 *             还有redis的序列化反序列化方式
 *          3）网关侧应该尽量保证不对spring session进行操作，只做鉴权
 *     方案2：网关侧不直接接入spring session，不对业务侧session做save，手动从redis拿，然后序列化之后鉴权判断，不做任何写入操作
 *     <b>对于生产使用，应该尽量使用方案2，甚至完全借助JWT</b>
 * </p>
 *
 * @author xujiajun
 * @since 2026/10/26
 */
@EnableSpringWebSession
@Configuration
public class RedisConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public ReactiveValkeyTemplate<String, Object> reactiveValkeyTemplate(LettuceConnectionFactory lettuceConnectionFactory) {

        StringValkeySerializer stringValkeySerializer = new StringValkeySerializer();

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        simpleModule.addSerializer(new LocalDateSerializer());
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        JsonMapper jsonMapper = JsonMapper.builder()
                .defaultTimeZone(TimeZone.getDefault())
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                // 通过该方法对mapper对象进行设置，所有序列化的对象都将按改规则进行系列化
                // Include.Include.ALWAYS 默认
                // Include.NON_DEFAULT 属性为默认值不序列化
                // Include.NON_EMPTY 属性为 空（""） 或者为 NULL 都不序列化，则返回的json是没有这个字段的。这样对移动端会更省流量
                // Include.NON_NULL 属性为NULL 不序列化
                .changeDefaultPropertyInclusion(value -> JsonInclude.Value.ALL_ALWAYS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(simpleModule)
                .build();
        JacksonJsonValkeySerializer<Object> jsonValueSerializer = new JacksonJsonValkeySerializer<>(jsonMapper, Object.class);


        ValkeySerializationContext<String, Object> context = ValkeySerializationContext.<String, Object>newSerializationContext()
                .string(stringValkeySerializer)
                .key(stringValkeySerializer)
                .value(jsonValueSerializer)
                .hashKey(stringValkeySerializer)
                .hashValue(jsonValueSerializer)
                .build();
        ReactiveValkeyTemplate<String, Object> reactiveValkeyTemplate = new ReactiveValkeyTemplate<>(lettuceConnectionFactory, context);
        return reactiveValkeyTemplate;
    }

    @Bean
    public ValkeyReactiveSessionRepository sessionRepository(ReactiveValkeyTemplate<String, Object> template, ValkeySessionConfig config) {
        return new ValkeyReactiveSessionRepository(template, config);
    }

    /**
     * Cookie解析器：控制cookie名称、path、SameSite、Secure等
     */
    @Bean
    public WebSessionIdResolver webSessionIdResolver(SessionCookieConfig config) {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        // 修改cookie名称，默认是 SESSION
        String cookieName = config.getCookieName();
        if (StringUtils.isNotBlank(cookieName)) {
            resolver.setCookieName(cookieName);
        }
        resolver.addCookieInitializer(cookieBuilder -> {
            // todo xujiajun 从配置文件读
            cookieBuilder.path("/");
            cookieBuilder.sameSite("Lax");
            cookieBuilder.httpOnly(true);
            // cookieBuilder.secure(true); // HTTPS环境打开
            String domain = config.getDomain();
            if (StringUtils.isNotBlank(domain)) {
                cookieBuilder.domain(domain);
            }
        });
        return resolver;
    }

}
