package com.gooshare.controller;

import com.gooshare.common.Result;
import com.gooshare.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "购物车管理")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Operation(summary = "加入购物车")
    @PostMapping
    public Result addCart(@RequestParam("itemId") Integer itemId,
                          @RequestParam("count") Integer count) {
        return cartService.addCart(itemId, count);
    }

    @Operation(summary = "展示购物车")
    @GetMapping
    public Result showCart() {
        return cartService.showCart();
    }

    @Operation(summary = "更新购物车商品数量")
    @PostMapping("/update")
    public Result updateCart(@RequestParam("itemId") Integer itemId,
                             @RequestParam("count") Integer count) {
        return cartService.updateCart(itemId, count);
    }

    @Operation(summary = "移除购物车商品")
    @PostMapping("/delete")
    public Result deleteCartItem(@RequestParam("itemId") Integer itemId) {
        return cartService.deleteCart(itemId);
    }
}