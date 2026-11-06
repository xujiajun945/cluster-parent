package com.example.consumer.web;

import com.example.consumer.service.PageService;
import com.example.core.dto.EmployeesDTO;
import com.example.core.model.Pager;
import com.example.core.model.ResponseData;
import com.example.core.query.EmployeesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author xujiajun
 * @since 2026/11/5
 */
@RestController
@RequestMapping(value = "/page")
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping(value = "/employees")
    public ResponseData pageEmployees(EmployeesQuery query) {
        Pager<EmployeesDTO> pager = pageService.getByQuery(query);
        return new ResponseData(pager);
    }
}
