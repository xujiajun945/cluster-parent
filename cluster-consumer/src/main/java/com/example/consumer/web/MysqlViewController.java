package com.example.consumer.web;

import com.example.consumer.service.MysqlShellService;
import com.example.core.model.ResponseData;
import com.example.core.vo.CreateTableVO;
import com.example.core.vo.DiySqlVO;
import com.example.core.vo.MysqlVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author xujiajun
 * @since 2026/10/29
 */
@RestController
@RequestMapping("/mysql")
public class MysqlViewController {

    @Autowired
    private MysqlShellService shellService;

    @GetMapping(value = "/show_tables")
    public ResponseData showTables(@RequestParam(value = "databaseName", defaultValue = "demo01") String databaseName) throws IOException {
        MysqlVO result = shellService.showTables(databaseName);
        return new ResponseData(result);
    }

    @GetMapping(value = "/show_columns")
    public ResponseData showColumns(@RequestParam("databaseName") String databaseName,
                                    @RequestParam(value = "tableName") String tableName) throws IOException {
        MysqlVO result = shellService.showColumns(databaseName, tableName);
        return new ResponseData(result);
    }

    @GetMapping(value = "/show_create_table")
    public ResponseData showCreateTable(@RequestParam("databaseName") String databaseName,
                                        @RequestParam(value = "tableName") String tableName) throws IOException {
        MysqlVO result = shellService.showCreateTable(databaseName, tableName);
        return new ResponseData(result);
    }

    @PostMapping(value = "/create_table")
    public ResponseData createTable(@RequestBody CreateTableVO createTableVO) throws IOException {
        String result = shellService.createTable(createTableVO);
        return new ResponseData(result);
    }

    @PostMapping(value = "/diy_sql")
    public ResponseData diySql(@RequestBody DiySqlVO diySqlVO) throws IOException {
        MysqlVO mysqlVO1 = shellService.diySql(diySqlVO);
        return new ResponseData(mysqlVO1);
    }
}
