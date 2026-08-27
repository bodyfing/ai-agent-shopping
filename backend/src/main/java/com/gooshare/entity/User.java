package com.gooshare.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Schema(description = "用户 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "头像 URL", example = "https://example.com/avatar.png")
    private String avatar;

    @Schema(description = "角色")
    private Long role;

    @Schema(description = "创建时间", example = "2025-03-09T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
