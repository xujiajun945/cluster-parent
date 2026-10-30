package com.example.consumer.web;

import com.example.consumer.service.LoginService;
import com.example.core.dto.LoginDTO;
import com.example.core.model.ResponseData;
import com.example.core.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xujiajun
 * @since 2026/10/29
 */
@RestController
@RequestMapping(value = "/view")
public class ModeAndViewController {

    @Autowired
    private LoginService loginService;

    @PostMapping(value = "/login")
    public ResponseData login(@RequestBody UserVO userVO) {
        LoginDTO loginDTO = loginService.normalLogin(userVO);
        return new ResponseData(loginDTO);
    }

    @GetMapping(value = "/login/check")
    public ResponseData checkLogin() {
        UserVO userVO = loginService.checkLogin();
        return new ResponseData(userVO);
    }

    @DeleteMapping("/logout")
    public ResponseData logout() {
        loginService.normalLogout();
        return new ResponseData();
    }
}
