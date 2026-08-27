package com.gooshare.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BrandMapper {


    @Select("""
            select id
            from brand
            where name = #{brandName}
            """)
    Long selectIdByName(
            @Param("brandName") String brandName
    );

}