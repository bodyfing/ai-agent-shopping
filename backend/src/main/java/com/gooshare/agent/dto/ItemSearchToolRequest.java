package com.gooshare.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.gooshare.common.ItemSortType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemSearchToolRequest {

    @JsonPropertyDescription(
            "商品核心关键词，例如游戏机、手机、教材，不包含价格"
    )
    private String keyword;

    @JsonPropertyDescription(
            "品牌名称，例如任天堂、苹果、华为；用户未说明时为空"
    )
    private String brand;

    @JsonPropertyDescription(
            "一级分类名称；只有用户明确说出分类时才填写"
    )
    private String category;

    @JsonPropertyDescription(
            "二级分类名称，例如手机、平板电脑；用户未说明时为空"
    )
    private String categoryItem;

    @JsonPropertyDescription(
            "最低价格；用户没有最低价要求时为空"
    )
    private BigDecimal minPrice;

    @JsonPropertyDescription(
            "最高价格；用户没有最高价要求时为空"
    )
    private BigDecimal maxPrice;

    @JsonPropertyDescription(
            "最多返回多少条商品，范围1～10，默认5"
    )
    private Integer limit = 5;

    @JsonPropertyDescription(
            "排序方式：LATEST表示最新发布，PRICE_ASC表示价格从低到高，" +
                    "PRICE_DESC表示价格从高到低；用户未说明时使用LATEST"
    )
    private ItemSortType sortType = ItemSortType.LATEST;
}
