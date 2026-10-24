package com.example.consumer.client;

import com.example.consumer.configuration.LoginClientFeignConfiguration;
import com.example.core.dto.UserDTO;
import com.example.core.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
@FeignClient(value = "${cluster.provider.cluster-provider}", path = "/provider/login", contextId = "LoginClient", configuration = LoginClientFeignConfiguration.class)
public interface LoginClient {

    @PostMapping
    UserDTO login(@RequestBody UserVO userVO);

//    class LoginClientFeignConfiguration {
//
//        @Bean
//        public Decoder decoder() {
//            return new LoginClientDecoder();
//        }
//    }
//
//    class LoginClientDecoder implements Decoder {
//
//        @Override
//        public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
//            String responseJson = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
//            ResponseData responseData = JsonUtils.parseObject(responseJson, ResponseData.class);
//            if (responseData == null) {
//                throw new ClusterException("调用返回值为空！");
//            }
//            return responseData.getData();
//        }
//    }

}
