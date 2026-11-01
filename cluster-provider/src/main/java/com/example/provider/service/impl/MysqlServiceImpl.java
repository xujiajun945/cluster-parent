package com.example.provider.service.impl;

import com.example.provider.service.MysqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author xujiajun
 * @since 2026/11/1
 */
@Service
public class MysqlServiceImpl implements MysqlService {

    @Autowired
    private DataSource dataSource;

    @Override
    public List<String> showDatabases() throws SQLException {
        List<String> result = new ArrayList<>();

        String sql = "SHOW DATABASES";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
            resultSet.close();
        }
        return result;
    }
}
