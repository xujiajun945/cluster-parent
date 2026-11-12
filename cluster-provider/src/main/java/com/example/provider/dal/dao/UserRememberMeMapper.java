package com.example.provider.dal.dao;

import com.example.core.model.UserRememberMe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRememberMeMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UserRememberMe record);

    int insertSelective(UserRememberMe record);

    UserRememberMe selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserRememberMe record);

    UserRememberMe getBySeries(@Param("series") String series, @Param("deleted") boolean deleted);

    int deleteBySeries(@Param("series") String series);
}