package com.example.provider.web;

import com.example.core.model.ResponseData;
import com.example.core.vo.DemoVO;
import com.example.provider.service.DemoProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
