package com.example.provider.web;

import com.example.core.dto.EmployeesDTO;
import com.example.core.model.Pager;
import com.example.core.model.ResponseData;
import com.example.core.query.EmployeesQuery;
import com.example.provider.service.MysqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author xujiajun
 * @since 2026/11/1
 */
@RestController
@RequestMapping(value = "/mysql")
public class MysqlController {

    @Autowired
    private MysqlService mysqlService;

    @GetMapping(value = "/show_databases")
    public ResponseData showDatabases() throws SQLException {
        List<String> result = mysqlService.showDatabases();
        return new ResponseData(result);
    }

    @GetMapping(value = "/show_tables")
    public ResponseData showTables(@RequestParam("databaseName") String databaseName) throws SQLException {
        List<String> result = mysqlService.showTables(databaseName);
        return new ResponseData(result);
    }

    @GetMapping(value = "/employees")
    public ResponseData getByQuery(EmployeesQuery query) {
        Pager<EmployeesDTO> pager = mysqlService.getEmployeesPageByQuery(query);
        return new ResponseData(pager);
    }
}
