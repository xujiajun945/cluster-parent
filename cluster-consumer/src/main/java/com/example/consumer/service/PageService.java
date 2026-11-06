package com.example.consumer.service;

import com.example.core.dto.EmployeesDTO;
import com.example.core.model.Pager;
import com.example.core.query.EmployeesQuery;

/**
 *
 * @author xujiajun
 * @since 2026/11/5
 */
public interface PageService {

    Pager<EmployeesDTO> getByQuery(EmployeesQuery query);
}
