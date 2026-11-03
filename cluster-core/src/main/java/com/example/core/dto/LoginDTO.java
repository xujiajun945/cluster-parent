package com.example.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
@Setter
@Getter
public class LoginDTO implements Serializable {

    private UserDTO userDTO;

    private String token;
}
