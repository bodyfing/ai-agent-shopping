package com.gooshare.service;

import com.gooshare.vo.UserLoginVO;

public interface UserService {
    UserLoginVO loginByPassword(String username,String password);

    boolean getCode(String phone);

    UserLoginVO loginByCode(String phone, String code);

//    CodeLoginVO loginByPhone(String phone);
}
