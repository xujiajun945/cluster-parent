package com.example.core.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User Remember Me Task
 * user_remember_me_task
 */
@Data
public class UserRememberMeTask implements Serializable {

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
     * cas version
     */
    private Integer version;

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
    private Boolean deleted;

}