package com.example.consumer.web;

import com.example.consumer.service.LoginService;
import com.example.core.dto.LoginDTO;
import com.example.core.model.ResponseData;
import com.example.core.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping(value = "/consumer/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/jwt")
    public ResponseData JWTLogin(@RequestBody UserVO userVO) {
        LoginDTO loginDTO = loginService.JWTLogin(userVO);
        return new ResponseData(loginDTO);
    }

    @PostMapping(value = "/normal")
    public ResponseData normalLogin(@RequestBody UserVO userVO) {
        LoginDTO loginDTO = loginService.normalLogin(userVO);
        return new ResponseData(loginDTO);
    }
}
