package com.example.gateway.client;

import com.example.core.dto.UserDTO;
import com.example.core.dto.UserRememberMeDTO;
import com.example.core.model.ResponseData;
import com.example.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 *
 * @author xujiajun
 * @since 2026/11/15
 */
@Component
public class UserClient {

    @Value("${cluster-gateway.providers.cluster-provider}")
    private String serviceId;

    @Autowired
    private WebClient webClient;

    public Mono<UserRememberMeDTO> getUserRememberMeBySeries(String series) {
        return webClient.get()
                .uri("http://{serviceId}/provider/user/user_remember_me/series?series={series}", serviceId, series)
                .retrieve()
                .bodyToMono(ResponseData.class)
                .map(responseData -> convert(responseData.getData(), UserRememberMeDTO.class));
    }

    public Mono<UserDTO> getUserById(Long userId) {
        return webClient.get()
                .uri("http://{serviceId}/provider/user/{userId}", serviceId, userId)
                .retrieve()
                .bodyToMono(ResponseData.class)
                .map(responseData -> convert(responseData.getData(), UserDTO.class));
    }

    public Mono<String> refreshUserRememberMe(String series) {
        return webClient.put()
                .uri("http://{serviceId}/provider/user/user_remember_me/refresh?series={series}", serviceId, series)
                .retrieve()
                .bodyToMono(ResponseData.class)
                .map(responseData -> String.valueOf(responseData.getData()));
    }

    private <T> T convert(Object origin, Class<T> tClass) {
        return JsonUtils.convertObject(origin, tClass);
    }
}
