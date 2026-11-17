package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
@Setter
@Getter
public class User {

    private Long id;

    private String username;

    private String password;

    private String name;

    private String phone;

    private LocalDateTime birth;
}
