package com.example.consumer.web;

import com.alibaba.nacos.api.exception.NacosException;
import com.example.consumer.service.DemoNacosConfigService;
import com.example.core.model.NacosConfig;
import com.example.core.model.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@RestController
@RequestMapping(value = "/demo/consumer")
public class DemoConsumerController {

    @Autowired
    private DemoNacosConfigService configService;

    @GetMapping
    public ResponseData nacosConfigSelect(@RequestParam("dataId") String dataId, @RequestParam("group") String group) throws NacosException {
        String config = configService.getConfig(dataId, group);
        return new ResponseData(config);
    }

    @PostMapping
    public ResponseData nacosConfigAdd(@RequestBody NacosConfig nacosConfig) throws NacosException {
        configService.publishConfig(nacosConfig);
        return new ResponseData();
    }
}
