package com.gooshare.dto;

import com.gooshare.common.ItemSortType;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemSearchRequest {


    private String keyword;

    private Long brandId;

    private Long categoryId;

    private Long categoryItemId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer limit = 5;

    private ItemSortType sortType = ItemSortType.LATEST;

//    将意图识别结果转换为统一搜索参数
    public static ItemSearchRequest from(IntentResult intent){

        if(intent == null){
            throw new IllegalArgumentException("" +
                    "搜索意图不可为空"
            );
        }

        ItemSearchRequest request = new ItemSearchRequest();
        request.setKeyword(normalize(intent.getKeyword()));
        request.setBrandId(intent.getBrandId());
        request.setCategoryId(intent.getCategoryId());
        request.setCategoryItemId(intent.getCategoryItemId());
        request.setMinPrice(intent.getMinPrice());
        request.setMaxPrice(intent.getMaxPrice());
        request.setLimit(5);
        request.setMaxPrice(intent.getMaxPrice());
        request.setSortType(ItemSortType.LATEST);
        request.setLimit(5);

        return request;
    }

    // 清理模型可能返回的前后空格
    private static String normalize(String value){

        if(value == null){
            return null;
        }

        // 去掉字符串最前面和最后面的空格
        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;

    }

    private static boolean hasText(String value){
        return value != null && !value.isEmpty();
    }
    /**
     * 判断是否至少提供了一个搜索条件。
     * 防止无条件查询整张商品表。
     */
    public boolean hasSearchCondition(){
        return hasText(keyword)
                || brandId != null
                || categoryId != null
                || categoryItemId != null
                || minPrice != null
                || maxPrice != null;


    }
    /**
     * 将返回数量限制在 1～10。
     */
    public int safeLimit(){
        if(limit == null || limit < 1){
            return 5;
        }

        return Math.min(limit, 10);
    }

    /**
     * 用户没有指定排序时，默认按照最新发布排序
     */
    public ItemSortType safeSortType(){
        if(sortType == null){
            return ItemSortType.LATEST;
        }
        return sortType;
    }
}
