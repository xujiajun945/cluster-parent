package com.example.consumer.util;

import com.example.core.dto.UserDTO;
import com.example.core.exception.ClusterException;
import com.example.core.util.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xujiajun
 * @since 2026/10/24
 */
public class LoginUtils {

    public static Integer getUserId() {
        HttpServletRequest request = getRequest();
        Integer userIdInAttribute = (Integer) request.getAttribute("userId");
        if (userIdInAttribute != null) {
            return userIdInAttribute;
        }

        String auth = request.getHeader("auth");
        if (StringUtils.isNotEmpty(auth)) {
            String subject = JwtTokenUtils.verifyToken(auth);
            Map<String, Object> userJwtInfo = JsonUtils.parseMap(subject, HashMap.class, String.class, Object.class);
            Integer userId = (Integer) userJwtInfo.get("userId");
            if (null != userId) {
                return userId;
            }
        }
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (null == user || null == user.getId()) {
            throw new ClusterException("用户登录信息不存在！！");
        }
        return user.getId();
    }

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        return request;
    }

    public static HttpServletResponse getReponse() {
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

}
