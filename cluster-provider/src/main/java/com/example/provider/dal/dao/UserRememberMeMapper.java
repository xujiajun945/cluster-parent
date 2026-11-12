package com.example.provider.dal.dao;

import com.example.core.model.UserRememberMe;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRememberMeMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UserRememberMe record);

    int insertSelective(UserRememberMe record);

    UserRememberMe selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserRememberMe record);

}