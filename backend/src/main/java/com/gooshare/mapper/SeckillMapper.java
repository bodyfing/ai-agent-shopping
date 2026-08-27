package com.gooshare.mapper;

import com.gooshare.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SeckillMapper {
    List<Coupon> getCouponsById(Integer itemId);

    void addSeckillOrder(String userId, String orderId, String seckillId);

    @Select("SELECT COUNT(*) FROM coupon_order WHERE user_id = #{userId} AND coupon_id = #{couponId}")
    int countOrder(String userId, String couponId);

    @Update("UPDATE coupon SET remain_stock = remain_stock - 1 WHERE id = #{id} AND remain_stock > 0")
    int updateStock(String id);
}
