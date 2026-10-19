package com.example.consumer.handler;

import com.alibaba.nacos.api.exception.NacosException;
import com.example.core.model.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author xujiajun
 * @since 2026/10/17
 */
@RestControllerAdvice
@Slf4j
public class ProviderConsumerControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(exception = NacosException.class)
    public ResponseData nacosExceptionHandler(NacosException e) {
        log.error("nacos 异常：", e);
        return new ResponseData(500, "error", e);
    }
}
