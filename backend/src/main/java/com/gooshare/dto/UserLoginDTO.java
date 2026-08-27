package com.gooshare.dto;

import com.gooshare.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO extends UserLoginVO {

    @Schema(name = "用户名", example = "李伟、张婷婷")
    private String username;

    @Schema(name = "密码", example = "123456")
    private String password;


}
