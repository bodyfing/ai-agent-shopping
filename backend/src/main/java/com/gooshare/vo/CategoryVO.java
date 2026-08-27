package com.gooshare.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品分类大类信息（包含子项）")
public class CategoryVO {

    @Schema(description = "大类ID", example = "1")
    private Integer id;

    @Schema(description = "分类名称", example = "数码电子")
    private String name;

    @Schema(description = "分类图标名/Emoji", example = "💻")
    private String icon;

    @Schema(description = "该大类下的子分类列表")
    private List<CategoryItemVO> categoryItems;
}