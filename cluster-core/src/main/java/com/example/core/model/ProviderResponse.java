package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xujiajun
 * @since 2026/10/22
 */
@Setter
@Getter
public class ProviderResponse<T> {

    private Integer code;

    private String message;

    private T data;

    public ProviderResponse() {
        this(1, "ok");
    }

    public ProviderResponse(T data) {
        this();
        this.data = data;
    }

    public ProviderResponse(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ProviderResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
