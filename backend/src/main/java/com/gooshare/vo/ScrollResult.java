package com.gooshare.vo;

import lombok.Data;

import java.util.List;

/**
 * 滚动分页查询结果返回值对象
 */
@Data
public class ScrollResult {
    //查询对象的集合
    private List<?> list;

    //下次查询的起始位置
    private Long minTime;

    //下次查询的偏移量
    private Integer offset;
}