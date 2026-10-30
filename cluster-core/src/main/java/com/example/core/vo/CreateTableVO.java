package com.example.core.vo;

import com.example.core.model.MysqlColumn;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * @author xujiajun
 * @since 2026/10/29
 */
@Setter
@Getter
public class CreateTableVO {

    private String databaseName;

    private String tableName;

    private String primaryKeyName;

    private String tableNote;

    private List<MysqlColumn> columns;
}
