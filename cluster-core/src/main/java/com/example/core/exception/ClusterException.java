package com.example.core.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xujiajun
 * @since 2026/10/20
 */
@Setter
@Getter
public class ClusterException extends RuntimeException {

    private Integer code;

    private String message;

    public ClusterException() {
        super();
    }

    public ClusterException(String message) {
        super(message);
        this.message = message;
    }

    public ClusterException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
