package com.gooshare.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryItemMapper {


    @Select("""
            select id
            from category_item
            where item_name = #{itemName}
            """)
    Long selectIdByName(
            @Param("itemName") String itemName
    );

}