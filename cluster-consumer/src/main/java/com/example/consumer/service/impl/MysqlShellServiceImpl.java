package com.example.consumer.service.impl;

import com.example.consumer.service.MysqlShellService;
import com.example.core.model.MysqlColumn;
import com.example.core.vo.CreateTableVO;
import com.example.core.vo.DiySqlVO;
import com.example.core.vo.MysqlVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author xujiajun
 * @since 2026/10/29
 */
@Slf4j
@Service
public class MysqlShellServiceImpl implements MysqlShellService {

    @Value("${cluster-consumer.datasource.ip}")
    private String ip;

    @Value("${cluster-consumer.datasource.username}")
    private String username;

    @Value("${cluster-consumer.datasource.password}")
    private String password;

    private String winBaseCommand = "mysql -t -h%s -u%s -p%s %s -e \"%s\"";

    private String linuxBaseCommand = "mysql -t -h%s -u%s -p%s %s -e '%s'";

    private String mysqlInsecureWarn = "mysql: [Warning] Using a password on the command line interface can be insecure.";

    @Override
    public MysqlVO showTables(String databaseName) throws IOException {
        String sql = "SHOW TABLES";
        Process exec = buildProcess(databaseName, sql);
        String info = invoke(exec);

        MysqlVO mysqlVO = new MysqlVO();
        mysqlVO.setDatabaseName(databaseName);
        mysqlVO.setResult(info);
        return mysqlVO;
    }

    @Override
    public MysqlVO showColumns(String databaseName, String tableName) throws IOException {
        String sql = "SHOW COLUMNS FROM " + tableName;
        Process exec = buildProcess(databaseName, sql);
        String info = invoke(exec);

        MysqlVO mysqlVO = new MysqlVO();
        mysqlVO.setDatabaseName(databaseName);
        mysqlVO.setTableName(tableName);
        mysqlVO.setResult(info);
        return mysqlVO;
    }

    @Override
    public MysqlVO showCreateTable(String databaseName, String tableName) throws IOException {
        String sql = "SHOW CREATE TABLE " + tableName;

        Process exec = buildProcess(databaseName, sql);
        String info = invoke(exec);

        MysqlVO mysqlVO = new MysqlVO();
        mysqlVO.setDatabaseName(databaseName);
        mysqlVO.setTableName(tableName);
        mysqlVO.setResult(info);
        return mysqlVO;
    }

    @Override
    public String createTable(CreateTableVO createTableVO) throws IOException {
        String sql = buildCreateTableSql(createTableVO);
        String databaseName = createTableVO.getDatabaseName();
        Process exec = buildProcess(databaseName, sql);
        String info = invoke(exec);
        return info;
    }

    @Override
    public MysqlVO diySql(DiySqlVO diySqlVO) throws IOException {
        String sql = diySqlVO.getSql();
        String databaseName = diySqlVO.getDatabaseName();
        Process exec = buildProcess(databaseName, sql);
        String info = invoke(exec);

        MysqlVO mysqlVO = new MysqlVO();
        mysqlVO.setDatabaseName(databaseName);
        mysqlVO.setResult(info);
        return mysqlVO;
    }

    private String buildCreateTableSql(CreateTableVO createTableVO) {
        String tableNote = createTableVO.getTableNote();
        String tableName = createTableVO.getTableName();
        String primaryKeyName = createTableVO.getPrimaryKeyName();
        List<MysqlColumn> columns = createTableVO.getColumns();

        String header = "CREATE TABLE `" + tableName + "` (\n";

        StringBuilder columnsBuilder = new StringBuilder(header);
        for (MysqlColumn column : columns) {

            columnsBuilder
                    .append("\t`")
                    .append(column.getFieldName())
                    .append("` ")
                    .append(column.getDataType())
                    .append(column.getDataType().equalsIgnoreCase("datetime") ? " " : "(" + column.getFieldLen() + ") ")
                    .append(column.getNotNull() || column.getPrimary() ? "NOT NULL " : "")
                    .append(column.getUniq() && !column.getPrimary() ? "UNIQUE " : "")
                    .append(column.getIncr() ? "AUTO_INCREMENT" : "");
            // 判断有无默认值
            if (column.getDefaultValueNotNull()) {
                boolean hasSingleQuote = switch (column.getDataType().toLowerCase()) {
                    case "int", "bigint", "tinyint" -> false;
                    default -> true;
                };
                columnsBuilder
                        .append(" DEFAULT ")
                        .append(!hasSingleQuote || "current_timestamp".equalsIgnoreCase(column.getDefaultValue()) ? "" : "'")
                        .append(column.getDefaultValue())
                        .append(!hasSingleQuote || "current_timestamp".equalsIgnoreCase(column.getDefaultValue()) ? "" : "'");
            }
            columnsBuilder
                    .append(" COMMENT '")
                    .append(column.getNote())
                    .append("',\n");
        }
        String index = "\tPRIMARY KEY(" + primaryKeyName + ")\n";
        String lastLine = ") ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT '"+ tableNote +"'";
        columnsBuilder.append(index).append(lastLine);
//        String sql = header + createTableVO.getTableName() + " " + "(\n" +
//                "\t" + createTableVO.getFieldName() + " BIGINT(20) NOT NULL AUTO_INCREMENT,\n" +
//                "\tuser_questionnaire_version_id BIGINT(20) NOT NULL DEFAULT 0 COMMENT '用户版本表id',\n" +
//                "\tquestion_id BIGINT(20) NOT NULL DEFAULT 0 COMMENT '问卷问题表id',\n" +
//                "\ttype INT(10) NOT NULL DEFAULT 0 COMMENT '题目类型(单选, 多选等)',\n" +
//                "\tanswer VARCHAR(500) NOT NULL DEFAULT '' COMMENT '用户给出的答案',\n" +
//                "\tcreate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
//                "\tupdate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',\n" +
//                "\tis_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',\n" +
//                "\tPRIMARY KEY(id)\n" +
//                ") ENGINE = INNODB DEFAULT CHARSET = utf8 COMMENT '用户答案表';";
        String sql = columnsBuilder.toString();
        log.info("创建表，sql： {}", sql);
        return sql;
    }

    private Process buildProcess(String databaseName, String sql) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        Process exec;
        if (os.contains("win")) {
            String cmd = String.format(winBaseCommand, ip, username, password, databaseName, sql);
            log.info("os: windows  cmd: {}", cmd);
            exec = Runtime.getRuntime().exec(cmd);
        } else {
            String cmd = String.format(linuxBaseCommand, ip, username, password, databaseName, sql);
            log.info("os: linux  cmd: {}", cmd);
            exec = Runtime.getRuntime().exec(new String[]{"bash", "-c", cmd});
        }
        return exec;
    }

    private String invoke(Process exec) throws IOException {
        // io
        InputStream inputStream = exec.getInputStream();
        InputStream errorStream = exec.getErrorStream();
        byte[] infoBytes = inputStream.readAllBytes();
        byte[] errorBytes = errorStream.readAllBytes();
        String info = new String(infoBytes, StandardCharsets.UTF_8);
        String error = new String(errorBytes, StandardCharsets.UTF_8);
        inputStream.close();
        errorStream.close();
        log.info("==================== std ====================");
        log.info(info);
        log.info("==================== error ====================");
        log.error(error);
        String result = info + "\n" + error.replace(mysqlInsecureWarn, "");
        return result;
    }

}
