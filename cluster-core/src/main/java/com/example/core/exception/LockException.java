package com.example.core.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LockException extends RuntimeException {

    private Integer code;

    private String message;

    public LockException() {
        super();
    }

    public LockException(String message) {
        super(message);
        this.message = message;
    }

    public LockException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
