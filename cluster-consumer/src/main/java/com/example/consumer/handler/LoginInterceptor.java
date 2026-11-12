package com.example.consumer.handler;

import com.example.consumer.client.UserClient;
import com.example.core.config.RememberMeConfig;
import com.example.core.dto.UserDTO;
import com.example.core.dto.UserRememberMeDTO;
import com.example.core.exception.ClusterException;
import com.example.core.util.LoginUtils;
import com.example.core.util.TimeUtils;
import com.example.core.util.TokenGeneratorUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author xujiajun
 * @since 2026/10/22
 */
@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private RememberMeConfig rememberMeConfig;

    @Lazy
    @Autowired
    private UserClient userClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 校验jwt token
        Long userId = LoginUtils.verifyJwtAndGetUserId(request);
        if (userId != null && userId > 0) {
            request.setAttribute("userId", userId);
            log.info("jwt用户已登录，userId：{}", userId);
            return true;
        }
        // 校验session
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.info("session生成时间：{}", TimeUtils.parse(session.getCreationTime()));
            UserDTO user = LoginUtils.getUserFromSession(session);
            if (null != user && null != user.getId() && user.getId() > 0) {
                request.setAttribute("userId", user.getId());
                return true;
            }
        }
        // 校验remember me
        Cookie rememberMeCookie = LoginUtils.getCookieFromRequest(request, rememberMeConfig.getCookieName());
        if (rememberMeCookie == null) {
            throw new ClusterException("未登录或登录失效！");
        }
        String value = rememberMeCookie.getValue();
        String[] parts = TokenGeneratorUtil.decodeCookieValue(value);
        if (parts == null) {
            throw new ClusterException("rememberMe token格式异常！");
        }
        String series = parts[0];
        String token = parts[1];
        // 验证token
        UserRememberMeDTO userRememberMe = userClient.getUserRememberMeBySeries(series);
        if (userRememberMe == null || !token.equals(userRememberMe.getToken())) {
            throw new ClusterException("rememberMe token校验失败！");
        }
        boolean result = TokenGeneratorUtil.validateCookieValue(value, userRememberMe.getSecret());
        if (!result) {
            throw new ClusterException("rememberMe token解密失败！");
        }
        request.setAttribute("userId", userRememberMe.getUserId());
        // 设置需要刷新remember me token
        try {
            String newToken = userClient.refreshUserRememberMe(series);
            log.info("刷新remember me token：{}", newToken);
            String cookieValue = TokenGeneratorUtil.encodeCookieValue(series, newToken);
            // 将请求带过来的remember me cookie的值更新 方便feign或者其他http调用透传
            rememberMeCookie.setValue(cookieValue);
            Cookie cookie = new Cookie(rememberMeConfig.getCookieName(), cookieValue);
            cookie.setPath(rememberMeConfig.getPath());
            cookie.setDomain(rememberMeConfig.getDomain());
            cookie.setSecure(rememberMeConfig.isSecure());
            cookie.setHttpOnly(rememberMeConfig.isHttpOnly());
            cookie.setMaxAge(rememberMeConfig.getMaxAge());
            response.addCookie(cookie);
        } catch (Exception e) {
            log.error("刷新remember me token异常：{}", e.getMessage());
            // todo xujiajun 处理刷新remember me token异常
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
