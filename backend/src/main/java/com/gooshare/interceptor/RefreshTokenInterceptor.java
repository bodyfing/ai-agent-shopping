package com.gooshare.interceptor;

import cn.hutool.json.JSONUtil;
import com.gooshare.entity.User;
import com.gooshare.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

import static com.gooshare.common.RedisConstants.*;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1 获取 token
        String token = request.getHeader("authorization");
        if(token == null || token.isBlank()){
            return true;
        }
        // 2 基于token获取redis中的用户
        String str = stringRedisTemplate.opsForValue().get(LOGIN_PREFIX+token);

        // Token 不存在或已经过期时，不解析空字符串。
        // 后续由 LoginInterceptor 对受保护接口统一返回 401。
        if(str == null || str.isBlank()){
            return true;
        }

        // 3 解析用户并存入 UserHolder
        User user = JSONUtil.toBean(str,User.class);
        UserHolder.saveUser(user);
        // 4 刷新有效期
        stringRedisTemplate.expire(LOGIN_PREFIX+token,60L, TimeUnit.MINUTES);
        return true;

    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
