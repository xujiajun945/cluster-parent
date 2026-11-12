package com.example.provider.service.impl;

import com.example.core.dto.UserRememberMeDTO;
import com.example.core.model.UserRememberMe;
import com.example.core.util.TokenGeneratorUtil;
import com.example.provider.dal.dao.UserRememberMeMapper;
import com.example.provider.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author xujiajun
 * @since 2026/11/12
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRememberMeMapper userRememberMeMapper;

    @Override
    public Integer addUserRememberMe(UserRememberMeDTO userRememberMeDTO) {
        UserRememberMe userRememberMe = new UserRememberMe();
        userRememberMe.setUserId(userRememberMeDTO.getUserId());
        userRememberMe.setSeries(userRememberMeDTO.getSeries());
        userRememberMe.setToken(userRememberMeDTO.getToken());
        return userRememberMeMapper.insertSelective(userRememberMe);
    }

    @Override
    public UserRememberMeDTO getUserRememberMeBySeries(String series) {
        UserRememberMe userRememberMe = userRememberMeMapper.getBySeries(series, false);
        if (userRememberMe == null) {
            return null;
        }
        UserRememberMeDTO userRememberMeDTO = new UserRememberMeDTO();
        userRememberMeDTO.setUserId(userRememberMe.getUserId());
        userRememberMeDTO.setSeries(userRememberMe.getSeries());
        userRememberMeDTO.setToken(userRememberMe.getToken());
        userRememberMeDTO.setSecret(userRememberMe.getSecret());
        return userRememberMeDTO;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    @Override
    public String refreshUserRememberMe(String series) {
        UserRememberMe old = userRememberMeMapper.getBySeries(series, false);
        if (old == null) {
            return null;
        }
        userRememberMeMapper.deleteBySeries(series);
        // 插入一条新的
        UserRememberMe newOne = new UserRememberMe();
        newOne.setUserId(old.getUserId());
        newOne.setSeries(series);
        // 生成新的token
        String newSecret = TokenGeneratorUtil.generate16BitToken();
        String newToken = TokenGeneratorUtil.generateToken(series, newSecret);
        newOne.setToken(newToken);
        userRememberMeMapper.insertSelective(newOne);
        return newToken;
    }
}
