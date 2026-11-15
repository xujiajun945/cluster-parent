package com.example.core.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Session cookie config.
 *
 * @author xujiajun
 * @since 2026/11/15
 */
@Setter
@Getter
public class SessionCookieConfig {

    private String cookieName;

    private String path;

    private String domain;

    private Integer maxAge;

    private Boolean secure;

    private Boolean httpOnly;
}
