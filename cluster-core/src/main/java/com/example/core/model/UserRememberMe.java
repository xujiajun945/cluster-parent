package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

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
    private Date createdTime;

    /**
     * Updated At
     */
    private Date updatedTime;

    /**
     * Deleted At
     */
    private Date deletedTime;

    /**
     * Deleted Flag
     */
    private Boolean isDeleted;
}