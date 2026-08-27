package com.gooshare.service.impl;

import static com.gooshare.common.RedisConstants.*;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.gooshare.dto.UserDTO;
import com.gooshare.dto.UserLoginDTO;
import com.gooshare.mapper.UserMapper;
import com.gooshare.service.UserService;
import com.gooshare.entity.User;
import com.gooshare.utils.UserHolder;
import com.gooshare.vo.UserLoginVO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    JSONConfig config = JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public UserLoginVO loginByPassword(String username,String password) {
        User user = userMapper.loginByUsername(username, password);
        UserLoginVO userLoginVO = new UserLoginVO();
        if(user == null){
            return null;
        }
        userLoginVO.setId(user.getId());
        userLoginVO.setUsername(user.getUsername());
        String token = userLoginVO.getUsername() + UUID.randomUUID().toString();
        userLoginVO.setToken(token);
        stringRedisTemplate.opsForValue().set(LOGIN_PREFIX+token,JSONUtil.toJsonStr(user,config),1L, TimeUnit.HOURS);
        return userLoginVO;
    }

    @Override
    public boolean getCode(String phone){
        User user = userMapper.getUserByPhone(phone);
        if(user == null){
            System.out.println(user);
            return false;
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_PREFIX+phone,code,5L, TimeUnit.MINUTES);
        System.out.println(code);
        return true;
    }

    @Override
    public UserLoginVO loginByCode(String phone, String code){
        User user = userMapper.getUserByPhone(phone);
        if(user == null){
            return null;
        }
        String code1 = stringRedisTemplate.opsForValue().get(LOGIN_CODE_PREFIX+phone);
        if(code1 == null){
            return null;
        }
        String token = user.getUsername() + UUID.randomUUID().toString() + code1;
        stringRedisTemplate.opsForValue().set(LOGIN_PREFIX+token, JSONUtil.toJsonStr(user,config),60L, TimeUnit.MINUTES);

        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setId(user.getId());
        userLoginVO.setUsername(user.getUsername());
        userLoginVO.setToken(token);
        return userLoginVO;
    }


}
