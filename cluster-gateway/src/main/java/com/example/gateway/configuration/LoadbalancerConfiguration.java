package com.example.gateway.configuration;

import com.example.core.handler.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * 负载均衡配置
 *
 * @author xujiajun
 * @since 2026/11/13
 */
@LoadBalancerClients(
        {
                // todo 写死测试的value改为配置文件读取
                @LoadBalancerClient(name = "cluster-consumer-dev", configuration = ClusterNacosLoadbalancerClient.class)
        }
)
@Configuration
public class LoadbalancerConfiguration {

    @LoadBalanced
    @Bean
    public WebClient.Builder loadBalancedWebClientBuilder() {
        ConnectionProvider provider = ConnectionProvider.builder("webclient-pool")
                // 最大连接数
                .maxConnections(5)
                // 借不到连接的等待超时
                .pendingAcquireTimeout(Duration.ofSeconds(3))
                // 空闲回收
                .maxIdleTime(Duration.ofMinutes(5))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .responseTimeout(Duration.ofSeconds(3));

        // 自定义webClient的json序列化
        JsonMapper jsonMapper = webClientJsonMapper();
        return WebClient.builder()
                .codecs(clientCodecConfigurer -> {
                    // 直接传入 JsonMapper，完全兼容
                    JacksonJsonEncoder encoder = new JacksonJsonEncoder(jsonMapper);
                    JacksonJsonDecoder decoder = new JacksonJsonDecoder(jsonMapper);

                    clientCodecConfigurer.defaultCodecs().jacksonJsonEncoder(encoder);
                    clientCodecConfigurer.defaultCodecs().jacksonJsonDecoder(decoder);

                    // 网关必调：放大响应体内存限制，默认256KB很容易超限
                    clientCodecConfigurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024);
                })
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    /**
     * 用带负载均衡的 Builder 构建 WebClient 实例
     */
    @Bean
    public WebClient webClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder.build();
    }

    private JsonMapper webClientJsonMapper() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        simpleModule.addSerializer(new LocalDateSerializer());
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
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
}
