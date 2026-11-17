package com.example.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class UserRememberMeTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * User ID
     */
    private Long userId;

    /**
     * user_remember_me data
     */
    private Object data;

    /**
     * Deleted Flag
     */
    private Boolean deleted;
}
