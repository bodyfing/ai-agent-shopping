package com.gooshare.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("item")
public class ItemInfo {

    @Schema(name = "商品ID")
    Integer id;

    @Schema(name = "商品名称")
    String title;

    @Schema(name = "二手价格")
    Integer price;

    @Schema(name = "数量")
    Long stock;

    @Schema(name = "商品图片")
    String imageURL;

    @Schema(name = "描述")
    String description;

    @Schema(name = "点赞条数")
    Integer likeCount;

    @Schema(name = "收藏数")
    Integer collectCount;

    @Schema(name = "浏览量")
    Integer browserCount;

    @Schema(name = "是否被点赞")
    Boolean liked;

    @Schema(name = "是否被收藏")
    Boolean collected;

    @Schema(name = "品牌ID")
    Long brandId;

    @Schema(name = "品牌名称")
    String brand;

    @Schema(name = "主分类id")
    Long categoryId;

    @Schema(name = "子分类id")
    Long categoryItemId;

    @Schema(name = "分类主题")
    String category;

    @Schema(name = "分类子标题")
    String categoryItem;

    @Schema(name = "创建时间")
    String createTime;

    @Schema(name = "卖家头像")
    String avatar;

    @Schema(name = "卖家id")
    Long sellerId;

    @Schema(name = "卖家用户名")
    String seller;

    @Schema(name = "卖家所在地")
    String location;

    @Schema(name = "是否关注卖家")
    Boolean isFollowed;


}
