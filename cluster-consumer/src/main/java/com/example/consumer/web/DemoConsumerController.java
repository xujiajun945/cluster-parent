package com.example.consumer.web;

import com.alibaba.nacos.api.exception.NacosException;
import com.example.consumer.client.DemoClient;
import com.example.consumer.service.DemoNacosConfigService;
import com.example.core.model.DemoNacosConfig;
import com.example.core.model.ResponseData;
import com.example.core.vo.DemoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@RestController
@RequestMapping(value = "/demo/consumer")
public class DemoConsumerController {

    @Autowired
    private DemoNacosConfigService configService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DemoClient demoClient;

    @GetMapping
    public ResponseData nacosConfigSelect(@RequestParam("dataId") String dataId, @RequestParam("group") String group) throws NacosException {
        String config = configService.getConfig(dataId, group);
        return new ResponseData(config);
    }

    @PostMapping
    public ResponseData nacosConfigAdd(@RequestBody DemoNacosConfig demoNacosConfig) throws NacosException {
        configService.publishConfig(demoNacosConfig);
        return new ResponseData();
    }

    @DeleteMapping
    public  ResponseData nacosConfigDelete(@RequestHeader("dataId") String dataId, @RequestHeader("group") String group, @RequestHeader("key") String key) throws NacosException {
        configService.deleteConfig(dataId, group, key);
        return new ResponseData();
    }

    @GetMapping(value = "/rest/get_demo/{id}")
    public ResponseData restTemplateDemo01(@PathVariable("id") Integer id) {
        ResponseData forObject = restTemplate.getForObject("http://cluster-provider:8080/demo/provider/" + id, ResponseData.class);
        return forObject;
    }

    @GetMapping("/feign/get_demo/{id}")
    public ResponseData feignClientDemo01(@PathVariable("id") Integer id) {
        DemoVO demoVO = demoClient.firstProvider(id);
        return new ResponseData(demoVO);
    }

    @GetMapping("/feign/get_demo")
    public ResponseData feignClientDemo02(@RequestParam("id") Integer id, @RequestParam("name") String name) {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("id", String.valueOf(id));
        queryMap.put("name", name);
        DemoVO demoVO = demoClient.getFromProviderByMap(queryMap);
        return new ResponseData(demoVO);
    }

    @GetMapping(value = "/json_mapper")
    public ResponseData testJsonMapperConverter() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", null);
        Date date = new Date(System.currentTimeMillis());
        result.put("date", date);
        LocalDateTime now = LocalDateTime.now();
        result.put("now", now);
        LocalDateTime next = null;
        result.put("next", next);

        LocalDate nowDate = LocalDate.now();
        result.put("nowDate", nowDate);
        return new ResponseData(result);
    }
}
