package com.example.provider.service;

import com.example.core.dto.UserDTO;
import com.example.core.dto.UserRememberMeDTO;

/**
 *
 * @author xujiajun
 * @since 2026/11/12
 */
public interface UserService {

    Integer addUserRememberMe(UserRememberMeDTO userRememberMeDTO);

    UserRememberMeDTO getUserRememberMeBySeries(String series);

    String refreshUserRememberMe(String series);

    UserRememberMeDTO getUserRememberMeByUserId(Long userId);

    UserDTO getUserById(Long userId);
}
