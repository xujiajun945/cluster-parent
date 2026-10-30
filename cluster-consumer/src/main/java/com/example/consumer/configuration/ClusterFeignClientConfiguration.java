package com.example.consumer.configuration;

import com.example.core.exception.ClusterException;
import com.example.core.model.GenericResponse;
import com.example.core.model.ResponseData;
import com.example.core.util.JsonUtils;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author xujiajun
 * @since 2026/10/20
 */
public class ClusterFeignClientConfiguration {

    @Bean
    public Request.Options options() {
        return new Request.Options(2000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS, true);
    }

    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new UserErrorDecoder();
    }

    @Bean
    public Encoder encoder(ObjectProvider<FeignHttpMessageConverters> objectFactory) {
        return new SpringEncoder(objectFactory);
    }

    @Bean
    public Decoder decoder() {
        return new ClassicDecoder();
    }

    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }

    @Slf4j
    public static class ClassicDecoder implements Decoder {

        @Override
        public Object decode(Response response, Type type) throws IOException, FeignException {
            // 获取原始的返回内容
            String json = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            Class<?> dataClass = typeToClass(type);
            GenericResponse<?> genericResponse = JsonUtils.parse(json, GenericResponse.class, dataClass);
            Object data = genericResponse.getData();
            return data;
        }

        private Class<?> typeToClass(Type type) {
            String dataTypeName = type.getTypeName();
            Class<?> dataClass;
            try {
                dataClass = Class.forName(dataTypeName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return dataClass;
        }
    }

    @Slf4j
    public static class UserErrorDecoder implements ErrorDecoder {

        @Override
        public Exception decode(String methodKey, Response response) {
            Exception exception = null;
            try {
                // 获取原始的返回内容
                String json = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                ResponseData responseData = JsonUtils.parseObject(json, ResponseData.class);

                // 若状态码为400,抛出ResponseException
                if (response.status() == HttpStatus.BAD_REQUEST.value()) {
                    exception = new ClusterException(responseData.getCode(), responseData.getMessage());
                }

//                // 若状态码为401,抛出LoginException
//                if (response.status() == HttpStatus.UNAUTHORIZED.value()) {
//                    exception = new LoginException(responseData.getCode(), responseData.getMessage());
//                }
//
//                // 若状态码为403,抛出PermissionException
//                if (response.status() == HttpStatus.FORBIDDEN.value()) {
//                    exception = new PermissionException(responseData.getCode(), responseData.getMessage());
//                }

                // 否则,抛出RuntimeException
                if (exception == null) {
                    exception = new RuntimeException(responseData.getMessage());
                }
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
            return exception;
        }
    }
}
