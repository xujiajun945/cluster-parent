package com.example.core.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
@Setter
@Getter
public class UserVO implements Serializable {

    private String username;

    private String password;

    private String code;
}
