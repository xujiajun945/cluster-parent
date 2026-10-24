package com.example.provider.service;

import com.example.core.dto.UserDTO;
import com.example.core.vo.UserVO;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
public interface LoginProviderService {

    UserDTO login(UserVO userVO);
}
