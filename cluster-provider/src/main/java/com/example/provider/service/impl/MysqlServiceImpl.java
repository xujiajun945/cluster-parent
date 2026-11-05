package com.example.provider.service.impl;

import com.example.core.dto.EmployeesDTO;
import com.example.core.model.Pager;
import com.example.core.query.EmployeesQuery;
import com.example.provider.dal.dao.EmployeesMapper;
import com.example.provider.service.MysqlService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private EmployeesMapper employeesMapper;

    @Override
    public List<String> showDatabases() throws SQLException {
        List<String> result = new ArrayList<>();

        String sql = "SHOW DATABASES";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
        }
        return result;
    }

    @Override
    public List<String> showTables(String databaseName) throws SQLException {
        List<String> result = new ArrayList<>();

        String sql = "SHOW TABLES FROM " + databaseName;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
        }
        return result;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    @Override
    public Pager<EmployeesDTO> getEmployeesPageByQuery(EmployeesQuery query) {
        Integer total = employeesMapper.countByQuery(query);
        Pager<EmployeesDTO> pager = new Pager<>(query.getPageNo(), query.getRows(), total);
        if (total > 0) {
            Integer start = pager.position();
            query.setStart(start);
            List<EmployeesDTO> employeesList = employeesMapper.listByQuery(query)
                    .stream()
                    .map(employees -> {
                        EmployeesDTO dto = new EmployeesDTO();
                        BeanUtils.copyProperties(employees, dto);
                        return dto;
                    })
                    .toList();
            pager.setList(employeesList);
        }
        return pager;
    }
}
