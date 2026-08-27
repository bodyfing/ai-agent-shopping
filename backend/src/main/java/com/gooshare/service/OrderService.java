package com.gooshare.service;

import com.gooshare.common.Result;

public interface OrderService {
    Result addOrder(Integer itemId, Integer price, Integer count, Integer sellerId);
}
