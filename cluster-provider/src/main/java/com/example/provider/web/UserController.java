package com.example.provider.web;

import com.example.core.dto.UserRememberMeDTO;
import com.example.core.model.ResponseData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author xujiajun
 * @since 2026/11/10
 */
@RestController
@RequestMapping(value = "/provider/user")
public class UserController {

    // todo xujiajun 2026/11/10 添加用户rememberMe接口
    @PostMapping(value = "/user_remember_me")
    public ResponseData addUserRememberMe(@RequestBody UserRememberMeDTO userRememberMeDTO) {
        return null;
    }
}
