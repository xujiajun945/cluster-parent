package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User Remember Me
 * user_remember_me
 */
@Setter
@Getter
public class UserRememberMe implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * Series
     */
    private String series;

    /**
     * Token
     */
    private String token;

    /**
     * secret
     */
    private String secret;

    /**
     * User ID
     */
    private Long userId;

    /**
     * Created At
     */
    private LocalDateTime createdTime;

    /**
     * Updated At
     */
    private LocalDateTime updatedTime;

    /**
     * Deleted At
     */
    private LocalDateTime deletedTime;

    /**
     * Deleted Flag
     */
    private Boolean isDeleted;
}