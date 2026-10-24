package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
@Setter
@Getter
public class User {

    private Integer id;

    private String username;

    private String password;

    private String name;

    private String phone;

    private Date birth;
}
