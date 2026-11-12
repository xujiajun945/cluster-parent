package com.example.consumer.service.impl;

import com.example.consumer.client.LoginClient;
import com.example.consumer.client.UserClient;
import com.example.consumer.service.LoginService;
import com.example.consumer.util.JwtTokenUtils;
import com.example.consumer.util.LoginUtils;
import com.example.core.dto.LoginDTO;
import com.example.core.dto.UserDTO;
import com.example.core.dto.UserRememberMeDTO;
import com.example.core.util.ClusterExecutor;
import com.example.core.util.JsonUtils;
import com.example.core.util.TimeUtils;
import com.example.core.util.TokenGeneratorUtil;
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

    @Autowired
    private UserClient userClient;

    @Autowired
    private ClusterExecutor clusterExecutor;

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
        LoginDTO loginDTO = new LoginDTO();

        HttpSession session = LoginUtils.getSession();
        long creationTime = session.getCreationTime();
        LocalDateTime createTime = TimeUtils.parse(creationTime);
        log.info("当前session生成时间： {}", createTime);

        Object user = session.getAttribute("user");
        if (null != user && null != ((UserDTO) user).getId()) {
            log.info("用户已经登录过了！{}", user);
            loginDTO.setUserDTO((UserDTO) user);
            return loginDTO;
        }

        UserDTO userDTO = loginClient.login(userVO);
        if (userDTO == null || userDTO.getId() == null || userDTO.getId() <= 0) {
            throw new RuntimeException("登录失败！");
        }
        // 保存用户rememberMe
        Long userId = userDTO.getId();
        clusterExecutor.execute("saveUserRememberMe", () -> {
            UserRememberMeDTO dto = new UserRememberMeDTO();
            dto.setUserId(userId);
            String series = TokenGeneratorUtil.generateSeries();
            String token = TokenGeneratorUtil.generateToken(series);
            dto.setSeries(series);
            dto.setToken(token);
            userClient.addUserRememberMe(dto);
        }, e -> {
            log.info("保存用户rememberMe失败，写入schedule任务，userId：{}", userId);
            // 写入schedule任务 todo xujiajun 2026/11/10
        });

        session.setAttribute("user", userDTO);
        session.setMaxInactiveInterval(60 * 60 * 2);
        loginDTO.setUserDTO(userDTO);
        // todo xujiajun 生成remember me cookie并下发
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
