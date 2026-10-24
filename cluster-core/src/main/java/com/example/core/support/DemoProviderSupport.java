package com.example.core.support;

import com.example.core.vo.DemoVO;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
public interface DemoProviderSupport {

    @GetMapping("/{id}")
    DemoVO firstProvider(@PathVariable("id") Integer id);

    @GetMapping("/demo")
    DemoVO getFromProviderByMap(@SpringQueryMap Map<String, String> queryMap);
}
