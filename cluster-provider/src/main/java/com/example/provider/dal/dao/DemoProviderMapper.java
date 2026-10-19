package com.example.provider.dal.dao;

import com.example.core.model.Demo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@Mapper
public interface DemoProviderMapper {

    @Select("SELECT * FROM demo01 WHERE id = #{id}")
    Demo getById(@Param("id") Integer id);
}
