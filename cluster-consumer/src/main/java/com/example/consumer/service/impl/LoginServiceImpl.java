package com.example.consumer.service.impl;

import com.example.consumer.client.LoginClient;
import com.example.consumer.service.LoginService;
import com.example.core.dto.LoginDTO;
import com.example.core.dto.UserDTO;
import com.example.core.util.JsonUtils;
import com.example.core.util.JwtTokenUtils;
import com.example.core.util.LoginUtils;
import com.example.core.util.TimeUtils;
import com.example.core.vo.UserVO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        HttpSession session = LoginUtils.getSession();
        log.info("当前session生成时间： {}", TimeUtils.parse(session.getCreationTime()));
        LoginUtils.setUserToSession(userDTO);
        session.setMaxInactiveInterval(60 * 60 * 2);
        return loginDTO;
    }

    @Override
    public LoginDTO normalLogin(UserVO userVO) {
        LoginDTO loginDTO = new LoginDTO();

        HttpSession session = LoginUtils.getSession();
        log.info("当前session生成时间： {}", TimeUtils.parse(session.getCreationTime()));

        UserDTO userFromSession = LoginUtils.getUserFromSession(session);
        if (null != userFromSession && null != userFromSession.getId()) {
            log.info("用户已经登录过了！{}", userFromSession);
            loginDTO.setUserDTO(userFromSession);
            return loginDTO;
        }

        UserDTO userDTO = loginClient.login(userVO);
        if (userDTO == null || userDTO.getId() == null || userDTO.getId() <= 0) {
            throw new RuntimeException("登录失败！");
        }
        LoginUtils.setUserToSession(userDTO);
        session.setMaxInactiveInterval(60 * 60 * 2);
        loginDTO.setUserDTO(userDTO);
        return loginDTO;
    }

    @Override
    public UserVO checkLogin() {
        HttpSession session = LoginUtils.getSession(false);
        UserVO userVO = new UserVO();
        if (session == null) {
            return userVO;
        }
        UserDTO user = (UserDTO) session.getAttribute("user");

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
