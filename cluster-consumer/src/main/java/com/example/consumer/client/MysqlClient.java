package com.example.consumer.client;

import com.example.consumer.configuration.ClusterFeignClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *
 * @author xujiajun
 * @since 2026/11/1
 */
@FeignClient(value = "${cluster.provider.cluster-provider}", path = "/mysql",
        contextId = "mysqlClient", configuration = ClusterFeignClientConfiguration.class)
public interface MysqlClient {

    @GetMapping(value = "/show_databases")
    List<String> showDatabases();

    @GetMapping(value = "/show_tables")
    List<String> showTables(@RequestParam("databaseName") String databaseName);
}
