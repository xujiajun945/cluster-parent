package com.example.core.vo;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author xujiajun
 * @since 2026/10/29
 */
@Setter
@Getter
public class MysqlVO {

    private String result;

    private String error;

    private String databaseName;

    private String tableName;
}
