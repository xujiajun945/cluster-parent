package com.example.provider.web;

import com.example.core.dto.UserDTO;
import com.example.core.model.ResponseData;
import com.example.core.vo.UserVO;
import com.example.provider.service.LoginProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
@RestController
@RequestMapping(value = "/provider/login")
public class LoginProviderController {

    @Autowired
    private LoginProviderService loginProviderService;

    @PostMapping
    public ResponseData login(@RequestBody UserVO userVO) {
        UserDTO userDTO = loginProviderService.login(userVO);
        return new ResponseData(userDTO);
    }
}
