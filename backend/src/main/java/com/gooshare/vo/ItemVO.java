package com.gooshare.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemVO {
    /**
     * 商品ID
     */
    @Schema(description = "商品ID", example = "1、2、3...")
    private Integer id;

    /**
     * 商品标题
     */
    @Schema(description = "商品标题", example = "高数教材")
    private String title;

    /**
     * 商品价格
     */
    @Schema(description = "商品价格")
    private BigDecimal price;

    /**
     * 卖家昵称/姓名
     */
    @Schema(description = "卖家姓名", example = "李伟")
    private String seller;

    /**
     * 商品封面URL
     */
    @Schema(description = "商品图片URL列表")
    private String imageURL;
}
