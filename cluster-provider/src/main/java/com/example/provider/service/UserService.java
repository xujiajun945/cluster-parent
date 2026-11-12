package com.example.provider.service;

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
}
