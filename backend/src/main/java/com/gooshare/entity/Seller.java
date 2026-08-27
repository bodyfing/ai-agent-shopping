package com.gooshare.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("seller")
public class Seller {

    // 【修改点 1】Java 属性名强制要求首字母小写！大写的 Id 会导致 JSON 序列化和 getter/setter 找不到
    @TableId("id")
    @Schema(name = "卖家ID")
    private Integer id;

    // 【核心解法】用 @TableField 指定数据库里真实的列名
    // 假设数据库里的列名叫 seller_name
    @TableField("username")
    @Schema(name = "卖家用户名")
    private String name;

}
