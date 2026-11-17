package com.example.provider.dal.dao;

import com.example.core.model.UserRememberMeTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRememberMeTaskMapper {

    int insert(UserRememberMeTask record);

    UserRememberMeTask getById(Long id);

    int updateById(UserRememberMeTask record);

    int deleteById(Long id);

    UserRememberMeTask getNotDeleteByUserId(@Param("userId") Long userId);
}