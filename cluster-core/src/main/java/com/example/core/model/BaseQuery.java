package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *
 * @author xujiajun
 * @since 2026/11/5
 */
@Setter
@Getter
public class BaseQuery implements Serializable {

    private Integer pageNo;

    private Integer rows;

    private Integer start;
}
