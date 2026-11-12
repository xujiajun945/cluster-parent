package com.example.provider.dal.dao;

import com.example.core.model.UserRememberMe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRememberMeMapper {

    int insertSelective(UserRememberMe record);

    UserRememberMe getActiveBySeries(@Param("series") String series);

    int deleteBySeries(@Param("series") String series);

    UserRememberMe getActiveByUserId(@Param("userId") Long userId);
}