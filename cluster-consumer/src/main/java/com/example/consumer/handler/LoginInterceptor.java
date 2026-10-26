package com.example.consumer.handler;

import com.example.consumer.util.JwtTokenUtils;
import com.example.core.dto.UserDTO;
import com.example.core.exception.ClusterException;
import com.example.core.util.JsonUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xujiajun
 * @since 2026/10/22
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        HttpSession session = request.getSession();
        LocalDateTime createTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(session.getCreationTime()), ZoneId.systemDefault());
        log.info("session是否新生成？ {}", session.isNew());
        log.info("session生成时间： {}", createTime);
        String token = request.getHeader("auth");
        if (StringUtils.isNotEmpty(token)) {
            String subject = JwtTokenUtils.verifyToken(token);
            Map<String, Object> subjectMap = JsonUtils.parseMap(subject, HashMap.class, String.class, Object.class);
            Object userId = subjectMap.get("userId");
            if (null != userId) {
                log.info("校验jwtToken成功！ userId: {}", userId);
                return true;
            }
        }
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (null == user || null == user.getId()) {
            throw new ClusterException("未登录或登录失效！");
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        log.info("我是LoginInterceptor的postHandle方法！！！");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        log.info("我是LoginInterceptor的afterCompletion方法！！！");
    }
}
