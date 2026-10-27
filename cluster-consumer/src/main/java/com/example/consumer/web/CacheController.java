package com.example.consumer.web;

import com.example.consumer.util.ValkeyClient;
import com.example.core.model.ResponseData;
import com.example.core.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author xujiajun
 * @since 2026/10/27
 */
@RestController
@RequestMapping(value = "/consumer/cache")
public class CacheController {

    @Autowired
    private ValkeyClient valkeyClient;


    @GetMapping
    public ResponseData getCacheDemo(@RequestParam("key") String key) {
        UserVO value = valkeyClient.get(key, UserVO.class);
        return new ResponseData(value);
    }

    @PostMapping
    public ResponseData setCacheDemo(@RequestBody UserVO userVO) {
        String key = userVO.getCode();
        valkeyClient.set(key, userVO);
        return new ResponseData();
    }

}
