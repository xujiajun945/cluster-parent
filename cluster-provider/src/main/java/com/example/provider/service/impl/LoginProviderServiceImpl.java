package com.example.provider.service.impl;

import com.example.core.dto.UserDTO;
import com.example.core.model.User;
import com.example.core.vo.UserVO;
import com.example.provider.dal.dao.LoginProviderMapper;
import com.example.provider.service.LoginProviderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
@Service
public class LoginProviderServiceImpl implements LoginProviderService {

    @Autowired
    private LoginProviderMapper loginProviderMapper;

    @Override
    public UserDTO login(UserVO userVO) {
        String username = userVO.getUsername();
        String password = userVO.getPassword();
        User user = loginProviderMapper.getByUsernameAndPassword(username, password);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }
}
