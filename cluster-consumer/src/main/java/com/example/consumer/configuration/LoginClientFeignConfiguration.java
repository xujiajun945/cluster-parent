package com.example.consumer.configuration;

import com.example.core.model.GenericResponse;
import com.example.core.util.JsonUtils;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
public class LoginClientFeignConfiguration {

    @Bean
    public Decoder decoder() {
        return new LoginClientDecoder();
    }

    public static class LoginClientDecoder implements Decoder {

        @Override
        public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
            String json = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            GenericResponse<?> genericResponse = JsonUtils.parseGenericResponse(json, type);
            return genericResponse.getData();
        }
    }
}
