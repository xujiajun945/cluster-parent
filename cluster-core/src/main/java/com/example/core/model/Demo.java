package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@Setter
@Getter
public class Demo {

    private Integer id;

    private Integer age;

    private String name;

    private LocalDateTime time;
}
