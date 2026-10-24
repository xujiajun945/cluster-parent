package com.example.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
@Setter
@Getter
public class LoginDTO {

    private UserDTO userDTO;

    private String token;
}
