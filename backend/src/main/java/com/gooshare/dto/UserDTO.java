package com.gooshare.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class UserDTO {
    private Integer id;
    private String username;
    private String icon;
}
