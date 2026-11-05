package com.example.provider.manager;

import com.example.core.model.Employees;
import com.example.core.util.ValkeyClient;
import com.example.provider.dal.dao.EmployeesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 *
 * @author xujiajun
 * @since 2026/11/5
 */
@Component
public class EmployeesManager {

    private static final String NAMESPACE = "";

    private static final Duration NONE_DATA_EXPIRE_TIME = Duration.ofMinutes(1);

    @Autowired
    private EmployeesMapper employeesMapper;

    @Autowired
    private ValkeyClient valkeyClient;

    public Employees getById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        String key = NAMESPACE + id;
        Employees employees = valkeyClient.get(key, Employees.class);
        if (employees != null) {
            return employees;
        }
        synchronized (key.intern()) {
            employees = valkeyClient.get(key, Employees.class);
            if (employees != null) {
                return employees;
            }
            employees = employeesMapper.getById(id);
            if (employees == null) {
                valkeyClient.set(key, new Employees(), NONE_DATA_EXPIRE_TIME);
            } else {
                valkeyClient.set(key, employees);
            }
        }
        return employees;
    }

    public void add(Employees employees) {
        if (employees != null) {
            synchronized (this) {
                employeesMapper.insert(employees);
                Integer id = employees.getEmpNo();
                valkeyClient.set(NAMESPACE + id, employees);
            }
        }
    }
}
