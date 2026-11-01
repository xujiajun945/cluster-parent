package com.example.provider.service;

import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author xujiajun
 * @since 2026/11/1
 */
public interface MysqlService {

    List<String> showDatabases() throws SQLException;

    List<String> showTables(String databaseName) throws SQLException;
}
