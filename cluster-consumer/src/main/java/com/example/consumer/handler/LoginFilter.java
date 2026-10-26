package com.example.consumer.handler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.io.IOException;

/**
 * @author xujiajun
 * @since 2026/10/24
 */
@Slf4j
@Order(value = 1)
@WebFilter(urlPatterns = "/demo/consumer/*", filterName = "loginFilter")
public class LoginFilter extends HttpFilter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        super.doFilter(request, response, chain);
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            String cookieName = cookie.getName();
            log.info("查看cookieName： {}", cookieName);
            if ("clusterSession".equals(cookieName)) {
                String clusterSession = cookie.getAttribute("clusterSession");
                log.info("查看clusterSession的value： {}", clusterSession);
//                cookie.setMaxAge(60 * 2);
//                cookie.setPath("/");
//                cookie.setHttpOnly(true);
//                response.addCookie(cookie);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

}
