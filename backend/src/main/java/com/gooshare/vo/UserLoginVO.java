package com.gooshare.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO {

    @Schema(name = "id")
    private Long id;

    @Schema(name = "返回的用户名",example = "李伟、刘婷婷")
    private String username;

    //有数据泄露风险，不建议传送给前端
//    @Schema(name = "返回的密码", description = "用于验证", example = "123456")
//    private String password;

    @Schema(name = "Token")
    private String token;
}
