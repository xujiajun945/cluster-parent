package com.example.core.config;

import com.example.core.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * app上下文容器
 *
 * @author xujiajun
 * @since 2026/11/12
 */
@Setter
@Getter
public class ClusterAppContext {

    private Long userId;

    private UserDTO user;

    private String auth;

    private String cookies;
}
