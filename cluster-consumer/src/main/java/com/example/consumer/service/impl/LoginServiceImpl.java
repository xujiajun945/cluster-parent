package com.example.consumer.service.impl;

import com.example.consumer.client.LoginClient;
import com.example.consumer.service.LoginService;
import com.example.consumer.util.JwtTokenUtils;
import com.example.consumer.util.LoginUtils;
import com.example.core.dto.LoginDTO;
import com.example.core.dto.UserDTO;
import com.example.core.exception.ClusterException;
import com.example.core.util.JsonUtils;
import com.example.core.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private LoginClient loginClient;

    @Override
    public LoginDTO JWTLogin(UserVO userVO) {
        UserDTO userDTO = loginClient.login(userVO);

        Map<String, Object> subjectMap = new HashMap<>();
        subjectMap.put("userId", userDTO.getId());
        subjectMap.put("name", userDTO.getName());
        String subjectJson = JsonUtils.toJson(subjectMap);
        String token = JwtTokenUtils.createToken(subjectJson);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUserDTO(userDTO);
        loginDTO.setToken(token);

        HttpServletRequest request = LoginUtils.getRequest();
        HttpSession session = request.getSession();
        long creationTime = session.getCreationTime();
        LocalDateTime createTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(creationTime), ZoneId.systemDefault());
        log.info("当前session生成时间： {}", createTime);
        session.setAttribute("user", userDTO);
        session.setMaxInactiveInterval(60 * 60 * 2);
        return loginDTO;
    }

    @Override
    public LoginDTO normalLogin(UserVO userVO) {
        HttpServletRequest request = LoginUtils.getRequest();
        HttpSession session = request.getSession();
        long creationTime = session.getCreationTime();
        LocalDateTime createTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(creationTime), ZoneId.systemDefault());
        log.info("当前session生成时间： {}", createTime);

        Object user = session.getAttribute("user");
        LoginDTO loginDTO = new LoginDTO();

        if (null != user && null != ((UserDTO) user).getId()) {
            log.info("用户已经登录过了！{}", user);
            loginDTO.setUserDTO((UserDTO) user);
            return loginDTO;
        }

        UserDTO userDTO = loginClient.login(userVO);
        if (userDTO == null || userDTO.getId() == null) {
            throw new ClusterException("登录失败！");
        }

        session.setAttribute("user", userDTO);
        session.setMaxInactiveInterval(60 * 60 * 2);
        loginDTO.setUserDTO(userDTO);
        return loginDTO;
    }

    @Override
    public UserVO checkLogin() {
        HttpSession session = LoginUtils.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        UserVO userVO = new UserVO();
        if (user != null) {
            BeanUtils.copyProperties(user, userVO);
        }
        return userVO;
    }

    @Override
    public void normalLogout() {
        HttpSession session = LoginUtils.getSession();
        session.invalidate();
    }
}
