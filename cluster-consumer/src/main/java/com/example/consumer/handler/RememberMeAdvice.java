package com.example.consumer.handler;

import com.example.consumer.client.UserClient;
import com.example.core.config.RememberMeConfig;
import com.example.core.dto.UserRememberMeDTO;
import com.example.core.dto.UserRememberMeTaskDTO;
import com.example.core.model.UserRememberMeTask;
import com.example.core.util.ClusterExecutor;
import com.example.core.util.LoginUtils;
import com.example.core.util.TokenGeneratorUtil;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class RememberMeAdvice {

    @Autowired
    private ClusterExecutor clusterExecutor;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RememberMeConfig config;

    @Pointcut(value = "execution(* com.example.consumer.service.impl.LoginServiceImpl.realLogin(..))")
    public void pointcut() {}

    @AfterReturning("pointcut()")
    public void afterReturning() {
        // 查询当前userRememberMe是否存在，只有不存在才会写一条
        Long userId = LoginUtils.getUserId();
        LoginUtils.getContext().setUserId(userId);
        UserRememberMeDTO userRememberMeDTO = userClient.getUserRememberMeByUserId(userId);
        if (userRememberMeDTO != null) {
            log.info("已经存在userRememberMe，不再重复写！");
            return;
        }
        log.info("Adding remember me cookie");
        String series = TokenGeneratorUtil.generateSeries();
        String token = TokenGeneratorUtil.generateToken(series);
        // 保存用户rememberMe
        UserRememberMeDTO dto = new UserRememberMeDTO();
        dto.setUserId(userId);
        dto.setSeries(series);
        dto.setToken(token);
        clusterExecutor.execute("saveUserRememberMe",
                () -> userClient.addUserRememberMe(dto),
                e -> {
                    log.info("保存用户rememberMe失败，写入schedule任务，userId：{}", userId);
                    UserRememberMeTaskDTO task = new UserRememberMeTaskDTO();
                    task.setUserId(userId);
                    task.setData(dto);
                    userClient.addUserRememberMeTask(task);
        });
        String cookieValue = TokenGeneratorUtil.encodeCookieValue(series, token);
        Cookie cookie = new Cookie(config.getCookieName(), cookieValue);
        cookie.setPath(config.getPath());
        cookie.setDomain(config.getDomain());
        cookie.setSecure(config.isSecure());
        cookie.setHttpOnly(config.isHttpOnly());
        cookie.setMaxAge(config.getMaxAge());
        LoginUtils.getResponse().addCookie(cookie);
    }
}
