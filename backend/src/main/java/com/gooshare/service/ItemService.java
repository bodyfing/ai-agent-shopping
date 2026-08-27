package com.gooshare.service;

import com.gooshare.common.Result;
import com.gooshare.dto.CategoryDTO;
import com.gooshare.vo.CategoryVO;
import com.gooshare.entity.ItemInfo;
import com.gooshare.vo.ItemVO;
import com.gooshare.dto.ItemSearchRequest;

import java.util.List;

public interface ItemService {
    List<ItemInfo> getItem();

    List<CategoryVO> getCategory();

    List<ItemVO> findItemByCategory(CategoryDTO filterDTO);

    ItemInfo getItem(Integer id);

    Result like(Integer itemId);

    Result collect(Integer itemId);

    Result browser(Integer itemId);

    Result getRanking();

    Result search(String keyword);

    /**
     * 旧意图识别链路暂时保留。
     * 等 /ai/chat-v2 稳定后暴力流
     */

    List<ItemInfo> search(ItemSearchRequest request);
}
