package com.gooshare.dto;

import com.gooshare.common.IntentType;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class IntentResult {


    private IntentType intent;


    //关键词
    private String keyword;


    //品牌名称
    private String brand;


    //品牌id
    private Long brandId;


    //一级分类名称
    private String category;


    //一级分类id
    private Long categoryId;


    //二级分类名称
    private String categoryItem;


    //二级分类id
    private Long categoryItemId;


    private BigDecimal minPrice;


    private BigDecimal maxPrice;

}
