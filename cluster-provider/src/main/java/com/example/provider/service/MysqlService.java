package com.example.provider.service;

import com.example.core.dto.EmployeesDTO;
import com.example.core.model.Pager;
import com.example.core.query.EmployeesQuery;

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

    Pager<EmployeesDTO> getEmployeesPageByQuery(EmployeesQuery query);
}
