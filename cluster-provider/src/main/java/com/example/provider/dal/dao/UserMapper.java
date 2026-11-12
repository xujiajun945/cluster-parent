package com.example.provider.dal.dao;

import com.example.core.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 *
 * @author xujiajun
 * @since 2026/11/13
 */
@Mapper
public interface UserMapper {

    @Select("SELECT * FROM demo01.user WHERE id = #{userId}")
    User getById(@Param("userId") Long userId);
}
