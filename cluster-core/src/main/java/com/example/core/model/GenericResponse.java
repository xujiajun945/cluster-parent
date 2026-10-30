package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xujiajun
 * @since 2026/10/22
 */
@Setter
@Getter
public class GenericResponse<T> {

    private Integer code;

    private String message;

    private T data;

    public GenericResponse() {
        this(1, "ok");
    }

    public GenericResponse(T data) {
        this();
        this.data = data;
    }

    public GenericResponse(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public GenericResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
