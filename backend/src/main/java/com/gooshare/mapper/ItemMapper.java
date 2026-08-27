package com.gooshare.mapper;

import com.gooshare.vo.CategoryVO;
import com.gooshare.entity.ItemInfo;
import com.gooshare.vo.ItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ItemMapper {

    List<ItemInfo> getItem();

    List<CategoryVO> getCategory();

    List<ItemVO> filterItem(String groupTitle, Long selectedItem);

    ItemInfo getItemById(Integer id);

    Boolean selectLiked(Integer itemId,Integer userId);

    Integer likeById(Integer itemId, Integer userId, LocalDateTime now);

    void addCountLike(Integer itemId);

    void deleteLiked(Integer itemId, Integer userId, LocalDateTime now);

    void subLikeCount(Integer itemId);

    void addCountCollect(Integer itemId);

    void subCountCollect(Integer itemId);

    void addBrowserCount(Integer itemId);

    List<ItemInfo> getItemByIds(List<Integer> itemIds);

    List<ItemInfo> searchByTitle(String keyword);

    List<ItemInfo>  getItemsByIds(List<Long> ids);

    List<ItemInfo> searchItems(
            @Param("keyword") String keyword,
            @Param("brandId") Long brandId,
            @Param("categoryId") Long categoryId,
            @Param("categoryItemId") Long categoryItemId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("limit") Integer limit,
            @Param("sortType") String sortType
    );
}
