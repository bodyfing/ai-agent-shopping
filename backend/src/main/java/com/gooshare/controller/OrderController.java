package com.gooshare.controller;

import com.gooshare.common.Result;
import com.gooshare.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单管理", description = "处理商品下单及交易逻辑")
@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "创建普通订单")
    @PostMapping
    public Result addOrder(@RequestParam("itemId") Integer itemId,
                           @RequestParam("price") Integer price,
                           @RequestParam("count") Integer count,
                           @RequestParam("sellerId") Integer sellerId) {
        return orderService.addOrder(itemId, price, count, sellerId);
    }
}