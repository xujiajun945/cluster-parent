package com.example.core.util;

import com.example.core.config.ClusterAppContext;
import com.example.core.dto.UserDTO;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xujiajun
 * @since 2026/10/24
 */
@Slf4j
public class LoginUtils {

    private static final ThreadLocal<ClusterAppContext> contextHolder = new ThreadLocal<>();

    public static void setContext(ClusterAppContext context) {
        contextHolder.set(context);
    }

    public static ClusterAppContext getContext() {
        return contextHolder.get();
    }

    public static void removeContext() {
        contextHolder.remove();
    }

    public static @Nullable Long getUserId() {
        ClusterAppContext context = contextHolder.get();
        if (context != null && context.getUserId() != null) {
            return context.getUserId();
        }
        HttpServletRequest request = getRequest();
        Long userIdInAttribute = (Long) request.getAttribute("userId");
        if (userIdInAttribute != null) {
            return userIdInAttribute;
        }
        // jwt获取userId
        Long userId = verifyJwtAndGetUserId(request);
        if (userId != null && userId > 0) {
            return userId;
        }
        // session获取userDTO
        UserDTO user = getUserFromSession(request.getSession());
        if (user == null || user.getId() == null || user.getId() <= 0) {
            return null;
        }
        return user.getId();
    }

    public static UserDTO getUser() {
        ClusterAppContext context = contextHolder.get();
        if (context != null && context.getUser() != null) {
            return context.getUser();
        }
        return getUserFromSession();
    }

    public static UserDTO getUserFromSession() {
        HttpServletRequest request = getRequest();
        HttpSession session = request.getSession();
        return getUserFromSession(session);
    }

    public static UserDTO getUserFromSession(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (null == user || null == user.getId()) {
            return null;
        }
        return user;
    }

    public static void setUserToSession(UserDTO user) {
        HttpSession session = getSession();
        session.setAttribute("user", user);
    }

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        return request;
    }

    public static HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();
        return response;
    }

    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    public static HttpSession getSession(boolean flag) {
        return getRequest().getSession(flag);
    }

    public static @Nullable Long verifyJwtAndGetUserId(HttpServletRequest request) {
        String auth = request.getHeader("auth");
        if (StringUtils.isEmpty(auth) || "null".equalsIgnoreCase(auth)) {
            return null;
        }
        try {
            String subject = JwtTokenUtils.verifyToken(auth);
            Map<String, Object> userJwtInfo = JsonUtils.parseMap(subject, HashMap.class, String.class, Object.class);
            Long userId = Long.valueOf(String.valueOf(userJwtInfo.get("userId")));
            if (userId > 0) {
                return userId;
            }
        } catch (Exception e) {
            log.error("校验jwt失败！", e);
        }
        return null;
    }

    public static String getAuth() {
        ClusterAppContext context = contextHolder.get();
        if (context != null) {
            return context.getAuth();
        }
        return getRequest().getHeader("auth");
    }

    public static String getHeaderCookies() {
        ClusterAppContext context = contextHolder.get();
        if (context != null) {
            return context.getCookies();
        }
        return getHeaderCookiesFromRequest(getRequest());
    }

    public static Cookie getCookie(String cookieName) {
        if (cookieName == null) {
            return null;
        }
        Cookie[] cookies = getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static Cookie[] getCookies() {
        ClusterAppContext context = contextHolder.get();
        if (context != null) {
            String headerCookies = context.getCookies();
            return cookieStringToCookieArray(headerCookies);
        }
        return getRequest().getCookies();
    }

    public static @Nullable Cookie getCookieFromRequest(String cookieName) {
        HttpServletRequest request = getRequest();
        return getCookieFromRequest(request, cookieName);
    }

    public static @Nullable Cookie getCookieFromRequest(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    public static @Nullable String getHeaderCookiesFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        StringBuilder cookieBuilder = new StringBuilder();
        for (Cookie cookie : cookies) {
            cookieBuilder.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
        }
        return cookieBuilder.toString();
    }

    private static @Nullable Cookie[] cookieStringToCookieArray(String cookieString) {
        if (cookieString == null || cookieString.isEmpty()) {
            return null;
        }
        String[] cookies = cookieString.split(";");
        Cookie[] cookieArray = new Cookie[cookies.length];
        for (int i = 0; i < cookies.length; i++) {
            String[] cookiePair = cookies[i].trim().split("=");
            if (cookiePair.length == 2) {
                cookieArray[i] = new Cookie(cookiePair[0], cookiePair[1]);
            }
        }
        return cookieArray;
    }

}
