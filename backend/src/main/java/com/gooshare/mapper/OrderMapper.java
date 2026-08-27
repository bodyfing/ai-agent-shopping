package com.gooshare.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper {

    void addOrder(String orderId, Integer itemId, Integer price, Integer sellerId, Integer userId);

    @Update("UPDATE item SET stock = stock - #{count} WHERE id = #{itemId}")
    boolean subStock(Integer itemId, Integer count);
}
