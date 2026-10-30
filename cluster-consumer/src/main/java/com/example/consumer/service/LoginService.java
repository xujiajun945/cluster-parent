package com.example.consumer.service;

import com.example.core.dto.LoginDTO;
import com.example.core.vo.UserVO;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
public interface LoginService {

    LoginDTO JWTLogin(UserVO userVO);

    LoginDTO normalLogin(UserVO userVO);

    UserVO checkLogin();

    void normalLogout();
}
