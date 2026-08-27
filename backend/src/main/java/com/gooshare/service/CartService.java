package com.gooshare.service;

import com.gooshare.common.Result;

public interface CartService {
    Result addCart(Integer itemId, Integer count);

    Result showCart();

    Result updateCart(Integer itemId, Integer count);

    Result deleteCart(Integer itemId);
}
