package com.example.consumer.handler;

import com.example.core.config.ClusterAppContext;
import com.example.core.dto.UserDTO;
import com.example.core.util.LoginUtils;
import com.example.core.util.TaskUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 集群应用上下文拦截器
 *
 * @author xujiajun
 * @since 2026/11/12
 */
@Component
@Slf4j
public class ClusterAppContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("创建集群应用上下文");
        ClusterAppContext context = new ClusterAppContext();
        Long userId = (Long) request.getAttribute("userId");
        context.setUserId(userId);
        HttpSession session = request.getSession(false);
        if (session != null) {
            UserDTO user = LoginUtils.getUserFromSession(session);
            context.setUser(user);
        }
        String auth = request.getHeader("auth");
        context.setAuth(auth);
        String headerCookies = LoginUtils.getHeaderCookiesFromRequest(request);
        context.setCookies(headerCookies);
        // 讲上下文对象设置到LoginUtils和TaskUtils里
        LoginUtils.setContext(context);
        TaskUtils.setContext(context);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // 清理资源
        log.info("清理资源...");
        TaskUtils.removeContext();
        LoginUtils.removeContext();
    }
}
