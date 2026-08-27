package com.gooshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    @Schema(name = "分类主题", description = "商品分类主题")
    String groupTitle;

    @Schema(name = "选中的子分类id", description = "主题的子分类")
    Long selectedItem;
}
