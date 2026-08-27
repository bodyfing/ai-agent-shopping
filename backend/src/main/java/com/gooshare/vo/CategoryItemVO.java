package com.gooshare.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分类子项详情")
public class CategoryItemVO {

    @Schema(description = "子项ID", example = "1")
    private Integer id;

    @Schema(description = "子项名称", example = "手机/平板")
    private String itemName;

}