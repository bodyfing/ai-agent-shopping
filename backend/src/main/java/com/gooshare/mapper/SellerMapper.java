package com.gooshare.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SellerMapper {

    @Select("SELECT user_id FROM seller WHERE id = #{id}")
    public Long selectUserId(Long id);
}
