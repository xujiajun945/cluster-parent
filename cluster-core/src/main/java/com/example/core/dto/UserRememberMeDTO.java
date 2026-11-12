package com.example.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *
 * @author xujiajun
 * @since 2026/11/10
 */
@Setter
@Getter
public class UserRememberMeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String series;
    private String token;
    private String secret;
}
