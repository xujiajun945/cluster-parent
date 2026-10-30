package com.example.core.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xujiajun
 * @since 2026/10/29
 */
@Setter
@Getter
public class MysqlColumn {

    private String fieldName;

    private String dataType;

    private Integer fieldLen;

    private Boolean notNull;

    private Boolean uniq;

    private Boolean incr;

    private Boolean defaultValueNotNull;

    private String defaultValue;

    private String note;

    private Boolean primary;
}
