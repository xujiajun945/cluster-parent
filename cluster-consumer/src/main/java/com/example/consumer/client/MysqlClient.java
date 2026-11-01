package com.example.consumer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 *
 * @author xujiajun
 * @since 2026/11/1
 */
@FeignClient(value = "${cluster.provider.cluster-provider}", path = "/mysql", contextId = "MysqlClient")
public interface MysqlClient {

    @GetMapping(value = "/show_databases")
    List<String> showDatabases();
}
