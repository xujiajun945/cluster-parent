package com.example.consumer.service;

import com.example.core.vo.CreateTableVO;
import com.example.core.vo.DiySqlVO;
import com.example.core.vo.MysqlVO;

import java.io.IOException;

/**
 * @author xujiajun
 * @since 2026/10/29
 */
public interface MysqlShellService {

    MysqlVO showTables(String databaseName) throws IOException, InterruptedException;

    MysqlVO showColumns(String databaseName, String tableName) throws IOException, InterruptedException;

    MysqlVO showCreateTable(String dataBaseName, String tableName) throws IOException, InterruptedException;

    String createTable(CreateTableVO createTableVO) throws IOException, InterruptedException;

    MysqlVO diySql(DiySqlVO diySqlVO) throws IOException, InterruptedException;
}
