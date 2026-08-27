package com.gooshare.mapper;

import com.gooshare.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    User loginByUsername(String username,String password);

//    String phoneIsOrNotExist(String phone);

    User getUserByPhone(String phone);
    @Select("SELECT username FROM user WHERE id = #{id}")
    String getUserById(int id);
}
