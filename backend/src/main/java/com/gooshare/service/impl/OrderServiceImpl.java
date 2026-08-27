package com.gooshare.service.impl;

import com.gooshare.common.Result;
import static com.gooshare.common.RedisConstants.*;
import com.gooshare.mapper.OrderMapper;
import com.gooshare.service.OrderService;
import com.gooshare.utils.UserHolder;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result addOrder(Integer itemId, Integer price, Integer count, Integer sellerId){
        Long userId = UserHolder.getUser().getId();
        String lockKey = ORDER_PREFIX + userId;
        Boolean canAdd = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofSeconds(5));
        if (BooleanUtils.isFalse(canAdd)) {
            return Result.error("5s内不可重复下单");
        }
        String orderId = generateOrderNo(userId);

        if(!orderMapper.subStock(itemId,count)){
            return Result.error();
        }

        orderMapper.addOrder(orderId,itemId, price, count, sellerId);

        return Result.success(orderId);
    }

    public String generateOrderNo(Long userId) {
        // 1. 获取当前日期
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 2. 构造 Redis Key（每天一个 Key，方便统计和重置）
        String key = "order:no:" + dateStr;

        // 3. Redis 原子递增
        Long sequence = stringRedisTemplate.opsForValue().increment(key);

        // 4. 拼接订单号：日期 + 用户ID后4位 + 序列号(左侧补0)
        // 示例：20260313 + 1234(用户) + 000567(序列)
        String userPart = String.format("%04d", userId % 10000);
        String seqPart = String.format("%06d", sequence % 1000000);

        return dateStr + userPart + seqPart;
    }
}
