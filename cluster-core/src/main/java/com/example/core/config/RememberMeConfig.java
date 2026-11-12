package com.example.core.config;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author xujiajun
 * @since 2026/11/12
 */
@Setter
@Getter
public class RememberMeConfig {

    private String cookieName;

    // seconds
    private int maxAge;

    private String path;

    private String domain;

    // HTTPS环境才发送
    private boolean secure;

    // JS无法读取，防御XSS盗取cookie
    private boolean httpOnly;
}
