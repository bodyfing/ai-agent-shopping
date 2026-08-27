package com.gooshare.service;

import com.gooshare.common.Result;
import com.gooshare.entity.Coupon;

import java.util.List;

public interface SeckillService {
    List<Coupon> getCoupons(Integer itemId);

    Result addSeckillOrder(Coupon coupon);
}
