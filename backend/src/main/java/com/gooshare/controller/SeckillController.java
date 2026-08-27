package com.gooshare.controller;

import com.gooshare.common.Result;
import com.gooshare.service.SeckillService;
import com.gooshare.entity.Coupon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "秒杀优惠券管理")
@RestController
@RequestMapping("seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @Operation(summary = "获取商品关联的优惠券列表")
    @GetMapping("/coupon/{itemId}")
    public Result getCoupon(@PathVariable Integer itemId) {
        List<Coupon> coupons = seckillService.getCoupons(itemId);
        return Result.success(coupons);
    }

    @Operation(summary = "创建秒杀/优惠券订单")
    @PostMapping("/order")
    public Result order(@RequestBody Coupon coupon) {
        return seckillService.addSeckillOrder(coupon);
    }
}