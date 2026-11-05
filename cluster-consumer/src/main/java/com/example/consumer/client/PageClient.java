package com.example.consumer.client;

import com.example.core.model.Employees;
import com.example.core.model.Pager;
import com.example.core.query.EmployeesQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author xujiajun
 * @since 2026/11/5
 */
@FeignClient(name = "${cluster.provider.cluster-provider}", path = "/page", contextId = "PageClient")
public interface PageClient {

    @GetMapping(value = "/employees")
    Pager<Employees> getByQuery(EmployeesQuery query);
}
