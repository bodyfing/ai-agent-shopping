package com.gooshare.service.impl;

import com.gooshare.common.Result;
import static com.gooshare.common.RedisConstants.*;

import com.gooshare.consumer.SeckillOrderConsumer;
import com.gooshare.mapper.SeckillMapper;
import com.gooshare.service.SeckillService;
import com.gooshare.utils.RedisIdWorker;
import com.gooshare.utils.UserHolder;
import com.gooshare.entity.Coupon;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private SeckillMapper seckillMapper;


    private DefaultRedisScript<Long> seckillRedisScript;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void  init()
    {
        //1、加载Lua
        seckillRedisScript = new DefaultRedisScript<>();
        seckillRedisScript.setLocation(new ClassPathResource("scripts/seckill.lua"));
        seckillRedisScript.setResultType(Long.class);
        //2、创建消息组
        try{
            stringRedisTemplate.opsForStream().createGroup(
                    "stream:orders",
                    ReadOffset.latest(),
                    "g1"
            );
        }catch (Exception ex){

        }
    }

    @Override
    public List<Coupon> getCoupons(Integer itemId){
        return seckillMapper.getCouponsById(itemId);

    }

    @Override
    public Result addSeckillOrder(Coupon coupon){
        //1 创建keys
        Long seckillId = coupon.getId();
        Long userId = UserHolder.getUser().getId();

        String stockKey = SECKILL_STOCK_PREFIX + seckillId;

        String orderKey = SECKILL_ORDER_PREFIX + seckillId;

        Long orderId = redisIdWorker.nextId("order");

        List<String> keys = Arrays.asList(stockKey,orderKey);
        //2 Lua脚本，进行redis扣减库存操作
        Long result = (Long) stringRedisTemplate.execute(
                seckillRedisScript,
                keys,
                seckillId.toString(),
                userId.toString(),
                orderId.toString()
        );
        if(result == null){
            return Result.error("系统异常");
        }
        if(result == 1){
            return Result.error("库存不足！");
        }
        else if(result == 2){
            return Result.error("用户不可重复下单！");
        }
        // result = 0 说明成功抢占资格

        return Result.success(orderId);
    }
}
