package com.example.consumer.configuration;

import com.example.core.exception.ClusterException;
import com.example.core.model.ResponseData;
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
            String responseJson = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            ResponseData responseData = JsonUtils.parseObject(responseJson, ResponseData.class);
            if (responseData == null) {
                throw new ClusterException("调用返回值为空！");
            }
            return responseData.getData();
        }
    }
}
