package com.example.consumer.client;

import com.example.consumer.configuration.ClusterFeignClientConfiguration;
import com.example.core.dto.EmployeesDTO;
import com.example.core.model.Pager;
import com.example.core.query.EmployeesQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author xujiajun
 * @since 2026/11/5
 */
@FeignClient(name = "${cluster.provider.cluster-provider}", path = "/mysql",
        contextId = "pageClient", configuration = ClusterFeignClientConfiguration.class)
public interface PageClient {

    @GetMapping(value = "/employees")
    Pager<EmployeesDTO> getByQuery(@SpringQueryMap EmployeesQuery query);
}
