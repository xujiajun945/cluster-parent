package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xujiajun
 * @since 2026/10/15
 */
@Setter
@Getter
public class ResponseData {

    private Integer code;

    private String message;

    private Object data;

    public ResponseData() {
        this(1, "ok");
    }

    public ResponseData(Object data) {
        this();
        this.data = data;
    }

    public ResponseData(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseData(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
