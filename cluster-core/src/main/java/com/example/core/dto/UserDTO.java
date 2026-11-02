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
public class UserDTO implements Serializable {

    private Integer id;

    private String username;

    private String password;

    private String name;
}
