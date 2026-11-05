package com.example.provider.dal.dao;

import com.example.core.model.Employees;
import com.example.core.query.EmployeesQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 *
 * @author xujiajun
 * @since 2026/11/5
 */
@Mapper
public interface EmployeesMapper {

    Integer countByQuery(EmployeesQuery query);

    List<Employees> listByQuery(EmployeesQuery query);

    Employees getById(Integer id);

    void insert(Employees employees);
}
