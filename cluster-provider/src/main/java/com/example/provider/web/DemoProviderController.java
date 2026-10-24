package com.example.provider.web;

import com.example.core.model.ResponseData;
import com.example.core.vo.DemoVO;
import com.example.provider.service.DemoProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author xujiajun
 * @since 2026/10/15
 */
@RestController
@RequestMapping(value = "/demo/provider")
public class DemoProviderController {

    @Autowired
    private DemoProviderService demoProviderService;

    @GetMapping(value = "/{id}")
    public ResponseData firstProvider(@PathVariable("id") Integer id) {
        DemoVO demoVO = demoProviderService.getById(id);
        return new ResponseData(demoVO);
    }

    @GetMapping(value = "/demo")
    public ResponseData getFromProviderByMap(@RequestParam Map<String, String> queryMap) {
        Integer id = Integer.valueOf(queryMap.get("id"));
        String name = queryMap.get("name");
        DemoVO demoVO = demoProviderService.getByIdAndName(id, name);
        return new ResponseData(demoVO);
    }
    
}
