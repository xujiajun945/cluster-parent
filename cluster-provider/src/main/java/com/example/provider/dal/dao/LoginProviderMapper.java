package com.example.provider.dal.dao;

import com.example.core.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author xujiajun
 * @since 2026/10/21
 */
@Mapper
public interface LoginProviderMapper {

    @Select("SELECT * FROM user WHERE username = #{username} AND password = #{password}")
    User getByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}
