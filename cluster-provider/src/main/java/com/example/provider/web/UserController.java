package com.example.provider.web;

import com.example.core.dto.UserRememberMeDTO;
import com.example.core.model.ResponseData;
import com.example.provider.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author xujiajun
 * @since 2026/11/10
 */
@RestController
@RequestMapping(value = "/provider/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/user_remember_me")
    public ResponseData addUserRememberMe(@RequestBody UserRememberMeDTO userRememberMeDTO) {
        Integer effctiveRows = userService.addUserRememberMe(userRememberMeDTO);
        return new ResponseData(effctiveRows);
    }

    @GetMapping(value = "/user_remember_me/series")
    public ResponseData getUserRememberMeBySeries(@RequestParam("series") String series) {
        UserRememberMeDTO userRememberMeDTO = userService.getUserRememberMeBySeries(series);
        return new ResponseData(userRememberMeDTO);
    }

    @PutMapping(value = "/user_remember_me/refresh")
    public ResponseData refreshUserRememberMe(@RequestParam("series") String series) {
        String newToken = userService.refreshUserRememberMe(series);
        return new ResponseData(newToken);
    }
}
