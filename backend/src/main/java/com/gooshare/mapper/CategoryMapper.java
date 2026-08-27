package com.gooshare.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper {


    @Select("""
            select id
            from category
            where name = #{name}
            """)
    Long selectIdByName(@Param("name") String name);

}