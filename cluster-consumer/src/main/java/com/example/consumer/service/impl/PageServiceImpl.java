package com.example.consumer.service.impl;

import com.example.consumer.client.PageClient;
import com.example.consumer.service.PageService;
import com.example.core.dto.EmployeesDTO;
import com.example.core.model.Pager;
import com.example.core.query.EmployeesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author xujiajun
 * @since 2026/11/5
 */
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private PageClient pageClient;

    @Override
    public Pager<EmployeesDTO> getByQuery(EmployeesQuery query) {
        Pager<EmployeesDTO> pager = pageClient.getByQuery(query);
        return pager;
    }
}
