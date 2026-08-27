package com.gooshare.config;

import com.gooshare.interceptor.LoginInterceptor;
import com.gooshare.interceptor.RefreshTokenInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截器 1：拦截所有路径，只负责尝试获取用户并刷新 Token 有效期
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                .addPathPatterns("/**")
                .order(0);

        // 拦截器 2：负责卡人。没登录的用户，除了排除掉的，其余都不让过
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/user/getCode",
                        "/user/loginByCode",
                        "/user/loginByPassword",
                        "/item",
                        "/item/**",
                        "/item/search/**",
//                        "/item/browser/**",
                        "/item/ranking"
//                        "/follow/**",
//                        "/follow/common",
//                        "/follow/common/**",
//                        "/comment/show",
//                        "/comment/show/*",
//                        "/browser/**",
//                        "/comment",
//                        "/comment/reply",
//                        "/cart",
//                        "/seckill/coupon/**",
//                        "/seckill/order",
//                        "/publish/image",
//                        "/publish",
//                        "/inbox"
                )
                .order(1);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")      // 允许所有路径
                .allowedOriginPatterns("*") // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
