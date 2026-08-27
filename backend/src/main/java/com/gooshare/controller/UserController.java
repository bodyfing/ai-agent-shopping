package com.gooshare.controller;

import cn.hutool.core.util.PhoneUtil;
import cn.hutool.json.JSONUtil;
import com.gooshare.common.Result;
import com.gooshare.service.UserService;
import com.gooshare.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "登录接口", description = "注册、登录、验证码功能")
@Slf4j
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户密码登录")
    @PostMapping("/loginByPassword")
    public Result loginByPassword(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");

        log.info("用户尝试密码登录，用户名: {}", username);
        UserLoginVO userLoginVO = userService.loginByPassword(username, password);
        if(userLoginVO == null){
            return Result.error("用户不存在或密码错误！");
        }
        return Result.success(JSONUtil.toJsonStr(userLoginVO));
    }

    @Operation(summary = "发送手机验证码")
    @PostMapping("/getCode")
    public Result getCode(@RequestParam("phone") String phone) {
        if(!PhoneUtil.isPhone(phone)){
            return Result.error("手机号格式不正确！");
        }
        boolean isExist = userService.getCode(phone);
        if(!isExist){
            return Result.error("手机号不存在！");
        }
        return Result.success();
    }

    @Operation(summary = "手机验证码登录")
    @PostMapping("/loginByCode")
    public Result loginByCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        if(!PhoneUtil.isPhone(phone)){
            return Result.error("手机号格式不正确！");
        }
        UserLoginVO userLoginVO = userService.loginByCode(phone, code);
        if(userLoginVO == null){
            return Result.error("手机不存在或者验证码不正确！");
        }
        return Result.success(JSONUtil.toJsonStr(userLoginVO));
    }
}